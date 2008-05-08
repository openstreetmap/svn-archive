#!/usr/bin/perl -w
#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home
#
# Contact Deelkar on the Openstreetmap wiki for help using this program
#-----------------------------------------------------------------------------
# Copyright 2006, Dirk-Lueder "Deelkar" Kreie
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
#-----------------------------------------------------------------------------

use strict;
use GD qw(:DEFAULT :cmp);
use Image::Magick;
use LWP::UserAgent;
use Math::Trig;
use File::Copy;
use File::Temp qw(tempfile);
use AppConfig qw(:argcount);
use FindBin qw($Bin);
use English '-no_match_vars';
use tahconfig;
use tahlib;
use tahproject;

our $Config = AppConfig->new({
                CREATE => 1,                      # Autocreate unknown config variables
                GLOBAL => {
                  DEFAULT  => undef,    # Create undefined Variables by default
                  ARGCOUNT => ARGCOUNT_ONE, # Simple Values (no arrays, no hashmaps)
                }
              });

$Config->define("help|usage!");
$Config->define("nodownload=s");
$Config->set("nodownload",0);
$Config->file("config.defaults", "layers.conf", "authentication.conf", "tahng.conf"); #first read configs in order, each (possibly) overwriting settings from the previous
$Config->args();              # overwrite config options with command line options
$Config->file("config.svn");  # overwrite with hardcoded values that must not be changed
ApplyConfigLogic($Config);
my %EnvironmentInfo = CheckConfig($Config);

my $Version = '$Revision$';
$Version =~ s/\$Revision:\s*(\d+)\s*\$/$1/;

# Keep track of progress
my ($progress,$progressJobs,$progressPercent,$currentSubTask) = (0,0,0,"none");

# keep track of the server time for current job
my $JobTime;

# Subdirectory for the current job (layer & z12 tileset),
# as used in sub GenerateTileset() and tileFilename()
my $JobDirectory;

#keep track of temporary files
my @tempfiles;

# We need to keep parent PID so that child get the correct files after fork()
my $parent_pid = $PID;
my $upload_pid = -1;

my $Mode = shift();
my $X = shift();
my $Y = shift();
die "Must specify tile coordinates\n" if( not defined $X or not defined $Y );
my $Zoom = shift();
if(not defined $Zoom)
{
    $Zoom = 12;
    statusMessage(" *** No zoomlevel specified! Assuming z12 *** ", "warning", $progressJobs, $progressPercent,1);
}

$JobDirectory = $Config->get("WorkingDirectory");
mkdir $JobDirectory unless -d $JobDirectory;
my $TempDir = $Config{WorkingDirectory} . $PID . "/"; # avoid upload.pl looking at the wrong PNG (Regression caused by batik support)
if (! -e $TempDir ) 
{
    mkdir($TempDir) or cleanUpAndDie("cannot create working directory $TempDir","EXIT",3,$PID);
}
elsif (! -d $TempDir )
{
    cleanUpAndDie("could not use $TempDir: is not a directory","EXIT",3,$PID);
}

if ($Mode eq "xy")
{
    GenerateTilesets($X, $Y, $Zoom);
}

#------------------------------------------------------
# Download data
#------------------------------------------------------
sub downloadData
{
    my ($bbox,$bboxref,$DataFile,$URLS) = @_;
    
    killafile($DataFile);

    my @tempfiles;
    push(@tempfiles, $DataFile);
    my $filelist = [];
    my $i=0;
    foreach my $URL (split(/ /,$URLS)) 
    {
        ++$i;
        my $partialFile = $Config->get("WorkingDirectory").$PID."/data-$PID-$bboxref-$i.osm";
        push(@{$filelist}, $partialFile);
        push(@tempfiles, $partialFile);
        statusMessage("Downloading: Map data for ".$Config->get("Layers")." to ".$partialFile, $currentSubTask, $progressJobs, $progressPercent,0);
        print "Download $URL\n" if ($Config->get("Debug"));
        DownloadFile($URL, $partialFile, 0);

        if (-s $partialFile == 0)
        {
            statusMessage("No data here...", $currentSubTask, $progressJobs, $progressPercent, 1);
            # if loop was requested just return  or else exit with an error. 
            # (to enable wrappers to better handle this situation 
            # i.e. tell the server the job hasn't been done yet)
            PutRequestBackToServer($X,$Y,$Zoom,"NoData");
            foreach my $file(@tempfiles) { killafile($file); }
            addFault("nodataXAPI",1);
            return cleanUpAndDie("GenerateTileset: no data!",$Mode,1,$PID),@tempfiles;
        }
        else
        {
            resetFault("nodataXAPI"); #reset to zero if data downloaded
        }
    }

    mergeOsmFiles($DataFile, $filelist);
    return 1,@tempfiles;
}

#-----------------------------------------------------------------------------
# Render a tile (and all subtiles, down to a certain depth)
#-----------------------------------------------------------------------------
sub GenerateTilesets ## TODO: split some subprocesses to own subs
{
    my ($X, $Y, $Zoom) = @_;
    $progress = 0;
    $progressPercent = 0;
    $progressJobs++;
    $currentSubTask = "getdata";
    
    $JobDirectory = $Config->get("WorkingDirectory").$PID;
    mkdir $JobDirectory unless -d $JobDirectory;

    my $maxCoords = (2 ** $Zoom - 1);
    
    if ( ($X < 0) or ($X > $maxCoords) or ($Y < 0) or ($Y > $maxCoords) )
    {
        #maybe do something else here
        die("\n Coordinates out of bounds (0..$maxCoords)\n");
    }

    my @lon;
    my @lat;
    my $Z=$Zoom+2;
    
    ($lon[0],$lon[1]) = ProjectL($X * 4, $Z);
    ($lon[2],$lon[3]) = ProjectL($X * 4 + 2, $Z);
    (undef,$lon[4]) = ProjectL($X, $Zoom);

    ($lat[0],$lat[1]) = Project($Y * 4, $Z);
    ($lat[2],$lat[3]) = Project($Y * 4 + 2, $Z);
    (undef,$lat[4]) = Project($Y, $Zoom);

    statusMessage(sprintf("Doing tileset $X,$Y (zoom $Zoom) (area around %f,%f)", $lat[2], $lon[2]), $currentSubTask, $progressJobs, $progressPercent, 1);

    ## we now build our bboxes:

    my $bboxRef;
    my %bbox;
    my $DataFile;

    $bboxRef = sprintf("%d-AreaAndLabels",$Zoom); # get everything outside the tile area that might affect the tile itself, like areas and large labels.

    my $N1 = $lat[0] + ($lat[0] - $lat[4]) * $Config->get("BorderN");
    my $S1 = $lat[4] - ($lat[0] - $lat[4]) * $Config->get("BorderS");
    my $E1 = $lon[4] + ($lon[4] - $lon[0]) * $Config->get("BorderE");
    my $W1 = $lon[0] - ($lon[4] - $lon[0]) * $Config->get("BorderW");

    # Adjust requested area to avoid boundary conditions
    # TODO: verify the current system cannot handle segments/ways crossing the 
    # 180/-180 deg meridian and implement proper handling of this case, until 
    # then use this workaround: 

    if($W1 < -180) {
      $W1 = -180; # api apparently can handle -180
    }
    if($E1 > 180) {
      $E1 = 180;
    }

    $bbox{$bboxRef} = sprintf("%f,%f,%f,%f",
      $W1, $S1, $E1, $N1);

    my $status;
    my $filelist = [];
    my $URLS = sprintf("%s%s/*[bbox=%s]",
         $Config->get("XAPIURL"), $Config->get("OSMVersion"), $bbox{$bboxRef});

    my $rootDataFile = $Config->get("WorkingDirectory").$PID."/data-$bboxRef.osm"; ## FIXME broken TODO: make sure tempdir is created.
    my $ppchainStart = $bboxRef; #the bit between data- and .osm is used as entry for the preprocessing chain later

    if ($Config->get("nodownload"))
    {
        copy $Config->get("nodownload"), $rootDataFile or die "no such file ".$Config->get("nodownload");
        # Get the server time for the data so we can assign it to the generated image (for tracking from when a tile actually is)
        $JobTime = [stat $Config->get("nodownload")]->[9]; ## TODO: change this to use the XAPI timestamp which is a more accurate measure for from when the data is
    }
    else
    {
        ($status,@tempfiles) = downloadData($bbox{$bboxRef},$bboxRef,$rootDataFile,$URLS);
        # Get the server time for the data so we can assign it to the generated image (for tracking from when a tile actually is)
        $JobTime = [stat $rootDataFile]->[9]; ## TODO: change this to use the XAPI timestamp which is a more accurate measure for from when the data is
    }

     ## build the 16 zoom+2 (usually z14) bboxes
     #  QR QC . SR SC: (QuadRow QuadColum SubRow SubColumn)
     #
     #  00.00  00.01  01.00  01.01
     #
     #  00.10  00.11  01.10  01.11
     #
     #  10.00  10.01  11.00  11.01
     #
     #  10.10  10.11  11.10  11.11

     for (my $QR="0"; $QR le "1"; $QR++)
     {
        for (my $QC="0"; $QC le "1"; $QC++)
        {
            for (my $SR="0"; $SR le "1"; $SR++)
            {
                for (my $SC="0"; $SC le "1"; $SC++)
                {
                     my $N1 = $lat[$QR * 2 + $SR];
                     my $S1 = $lat[$QR * 2 + $SR + 1];
                     my $W1 = $lon[$QC * 2 + $SC];
                     my $E1 = $lon[$QC * 2 + $SC + 1];
                     $bboxRef = sprintf("%d-%d-%d", $Z, $QR*2 + $QC, $SR*2 + $SC); # ref: 14-0..3-0..3 will be used later in stitching.
                     $bbox{$bboxRef} = sprintf("%f,%f,%f,%f", $W1, $S1, $E1, $N1);
                }
            }
        }
    }

    # Check for correct UTF8 (else inkscape will run amok later)
    # FIXME: This doesn't seem to catch all string errors that inkscape trips over.
    statusMessage("Checking for UTF-8 errors in $rootDataFile", $currentSubTask, $progressJobs, $progressPercent, 0);
    open(OSMDATA, $rootDataFile) or die ("could not open $rootDataFile for UTF-8 check");
    my @toCheck = <OSMDATA>;
    close(OSMDATA);
    while (my $osmline = shift @toCheck)
    {
      if (utf8::is_utf8($osmline)) # this might require perl 5.8.1 or an explicit use statement
      {
        statusMessage("found incorrect UTF-8 chars in $DataFile, job $X $Y  $Zoom", $currentSubTask, $progressJobs, $progressPercent, 1);
        PutRequestBackToServer($X,$Y,$Zoom,"BadUTF8");
        addFault("utf8",1);
        return cleanUpAndDie("GenerateTileset:UTF8 test failed",$Mode,1,$PID);
      }
    }
    resetFault("utf8"); #reset to zero if no UTF8 errors found.

    if ($Config->get("KeepDataFile"))
    {
        copy($rootDataFile, $Config->get("WorkingDirectory") . "/" . "data.osm");
    }
    
    $currentSubTask = "Preproc";
    
    #------------------------------------------------------
    # Handle all layers, one after the other
    #------------------------------------------------------

    foreach my $layer(split(/,/, $Config->get("Layers")))
    {
        #reset progress for each layer
        $progress=0;
        $progressPercent=0;
        $currentSubTask = $layer;
        
        $JobDirectory = sprintf("%s%s_%d_%d_%d.tmpdir",
                                $Config->get("WorkingDirectory").$PID."/",
                                $Config->get($layer."_Prefix"),
                                $Zoom, $X, $Y);
        mkdir $JobDirectory unless -d $JobDirectory;

        my $maxzoom = $Config->get($layer."_MaxZoom");
        
#        # Faff around
#        for (my $i = $Zoom ; $i <= $maxzoom ; $i++) 
#        {
#            killafile($Config->get("WorkingDirectory").$PID."/output-$parent_pid-z$i.svg");
#        }
        
        my $Margin = " " x ($Zoom - 8);
        #printf "%03d %s%d,%d: %1.2f - %1.2f, %1.2f - %1.2f\n", $Zoom, $Margin, $X, $Y, $S,$N, $W,$E;
        
        my $layerDataFile = PreProcess($ppchainStart, $layer, %bbox);
        #------------------------------------------------------
        # Preprocessing finished, start rendering
        #------------------------------------------------------

        # Add bounding box to osmarender
        # then set the data source
        # then transform it to SVG
        # and immediately render it.

        for (my $i = $Zoom ; $i <= $maxzoom and ($i < $Zoom + 2 or not $Config->get($layer."_Preprocessor") =~ /autocut/); $i++)
        {
            if (GenerateSVG($layerDataFile, $layer, $X, $Y, $i, $lat[0], $lat[4], $lon[0], $lon[4]))
            {
                foreach my $file(@tempfiles) { killafile($file) if (!$Config->get("Debug")); }
                return 0;
            }
            else
            {
                my $SvgFile = $Config->get("WorkingDirectory").$PID."/output-$parent_pid-$X-$Y-z$i.svg";
                # Find the size of the SVG file
                my ($ImgH,$ImgW,$Valid) = getSize($SvgFile);
                die "invalid coordinates" unless $Valid;

                # Render the tile(s)
                my ($success,$empty) = svg2png($SvgFile,$i,$Zoom,$layer,0,0,$ImgW,$ImgH,$X,$Y,
                                       $Config->get("WorkingDirectory").$PID."/".$layer."_".$X."_".$Y."_".$Zoom.".tmpdir"); ## FIXME: really necessary?

                die "couldn't render png" unless $success;
            }
        }
        for (my $i = $Zoom+2 ; $i <= $maxzoom and $Config->get($layer."_Preprocessor") =~ /autocut/; $i++)
        {
            for (my $ilat = 0 ; $ilat <= 3; $ilat++)
            {
                for (my $ilon = 0 ; $ilon <= 3; $ilon++)
                {
                    if (GenerateSVG($layerDataFile, $layer, $X*4+$ilon, $Y*4+$ilat, $i, $lat[$ilat], $lat[$ilat+1], $lon[$ilon], $lon[$ilon+1]))
                    {
                        foreach my $file(@tempfiles) { killafile($file) if (!$Config->get("Debug")); }
                        return 0;
                    }
                }
            }
        }
        
        
        

        #### FIXME: my ($success,$empty) = RenderTile($layer, $X, $Y, $Y, $Zoom, $Zoom, $lat[0], $lat[4], $lon[0], $lon[4], 0,0,$ImgW,$ImgH,$ImgH,0);
        if (!$success)
        {
            addFault("renderer",1);
            return cleanUpAndDie("GenerateTileset: could not render tileset",$Mode,1,$PID);
        }
        else
        {
            resetFault("renderer");
        }
        # Clean-up the SVG files
        for (my $i = $Zoom ; $i <= $maxzoom; $i++) 
        {
            killafile($Config->get("WorkingDirectory")."output-$parent_pid-z$i.svg") if (!$Config->get("Debug"));
        }


        #if $empty then the next zoom level was empty, so we only upload one tile unless RenderFullTileset is set.
        if ($empty == 1 && $Config->get("GatherBlankTiles")) 
        {
            my $Filename=sprintf("%s_%s_%s_%s.png",$Config->get($layer."_Prefix"), $Zoom, $X, $Y);
            my $oldFilename = sprintf("%s/%s",$JobDirectory, $Filename); 
            my $newFilename = sprintf("%s%s",$Config->get("WorkingDirectory"),$Filename);
            rename($oldFilename, $newFilename);
            rmdir($JobDirectory);
        }
        else
        {
            # This directory is now ready for upload.
            # How should errors in renaming be handled?
            my $Dir = $JobDirectory;
            $Dir =~ s|\.tmpdir|.dir|;
            rename $JobDirectory, $Dir;
        }

        if ($Config->get("LayerUpload")) 
        {
            uploadIfEnoughTiles();
        }
    }

    foreach my $file(@tempfiles) { killafile($file) if (!$Config->get("Debug")); }
    return 1;
}

#------------------------------------------------------
# Go through preprocessing steps for the current layer
#------------------------------------------------------
sub PreProcess
{
    my ($ppchainStart, $layer, %bbox) = @_;
    my @ppchain = ($ppchainStart);
    # config option may be empty, or a comma separated list of preprocessors
    foreach my $preprocessor(split /,/, $Config->get($layer."_Preprocessor"))
    {
        my $inputFile = sprintf("%sdata-%s.osm", 
            $Config->get("WorkingDirectory").$PID."/",
            join("-", @ppchain));
        push(@ppchain, $preprocessor);
        my $outputFile = sprintf("%sdata-%s.osm", 
            $Config->get("WorkingDirectory").$PID."/",
            join("-", @ppchain));

        if (-f $outputFile)
        {
            # no action; files for this preprocessing step seem to have been created 
            # by another layer already!
        }
        elsif ($preprocessor eq "maplint")
        {
            # Pre-process the data file using maplint
            # TODO may put this into a subroutine of its own
            my $Cmd = sprintf("%s \"%s\" tr %s %s > \"%s\"",
                    $Config->get("Niceness"),
                    $Config->get("XmlStarlet"),
                    "maplint/lib/run-tests.xsl",
                    "$inputFile",
                    "tmp.$PID");
            statusMessage("Running maplint", $currentSubTask, $progressJobs, $progressPercent,0);
            runCommand($Cmd,$PID);
            $Cmd = sprintf("%s \"%s\" tr %s %s > \"%s\"",
                    $Config->get("Niceness"),
                    $Config->get("XmlStarlet"),
                    "maplint/lib/convert-to-tags.xsl",
                    "tmp.$PID",
                    "$outputFile");
            statusMessage("Creating tags from maplint", $currentSubTask, $progressJobs, $progressPercent,0);
            runCommand($Cmd,$PID);
            killafile("tmp.$PID");
        }
        elsif ($preprocessor eq "close-areas")
        {
            my $Cmd = sprintf("%s perl close-areas.pl $X $Y $Zoom < %s > %s",
                    $Config->get("Niceness"),
                    "$inputFile",
                    "$outputFile");
            statusMessage("Running close-areas", $currentSubTask, $progressJobs, $progressPercent,0);
            runCommand($Cmd,$PID);
        }
        elsif ($preprocessor eq "attribution")
        {
            my $Cmd = sprintf("%s perl attribution.pl < %s > %s",
                    $Config->get("Niceness"),
                    "$inputFile",
                    "$outputFile");
            statusMessage("Running attribution", $currentSubTask, $progressJobs, $progressPercent,0);
            runCommand($Cmd,$PID);
        }
        elsif ($preprocessor eq "autocut")
        {
            statusMessage("Running autocut", $currentSubTask, $progressJobs, $progressPercent,0);
            my $autocutJobs = scalar(keys %bbox);
            my $progressAutocut = 0;
            my $DataFile;
            foreach my $bboxRef (sort (keys %bbox)) 
            {
                print $bboxRef.": ".$bbox{$bboxRef}."\n" if $Config->get("Debug");
                if ($bboxRef =~ m/AreaAndLabels/)
                {
                    $DataFile = $outputFile; 
                }
                else
                {
                    $DataFile = $Config->get("WorkingDirectory").$PID."/data-$bboxRef.osm";
                    push(@tempfiles, $DataFile);
                }
                statusMessage("processing ".$bboxRef,"autocut", $progressAutocut, $progressPercent,0);
                statusMessage("cropping $bbox{$bboxRef} from $inputFile to $DataFile","autocut", $progressAutocut, $progressPercent,1) if $Config->get("Verbose");
                cropDataToBBox(split(/,/,$bbox{$bboxRef}), $inputFile, $DataFile); 
                $progressAutocut++;
                $progressPercent=100*$progressAutocut/$autocutJobs;
                doneMessage("");
            }
            statusMessage("complete","autocut", $progressAutocut, $progressPercent,0);
            $progressPercent=0;
        }
        else
        {
            die "I have no preprocessor called '$preprocessor'";
        }
        push(@tempfiles, $outputFile);
    }

    my $layerDataFile = sprintf("data-%s.osm", join("-", @ppchain)); # Don't put working directory here, the path is relative to the rulesfile
    return $layerDataFile;
}

#-----------------------------------------------------------------------------
# Generate SVG for one zoom level
#   $layerDataFile - name of the OSM data file
#   $X, $Y - which tileset (Always the tilenumbers of the base zoom. i.e. z12)
#   $Zoom - which zoom currently is processsed
#   $N, $S, $W, $E - bounds of the tile
#-----------------------------------------------------------------------------
sub GenerateSVG 
{
    my ($layerDataFile, $layer, $X, $Y, $Zoom, $N, $S, $W, $E) = @_;
    # Create a new copy of rules file to allow background update
    # don't need layer in name of file as we'll
    # process one layer after the other
    my $error = 0;
    my $source = $Config->get($layer."_Rules.$Zoom");
    my $TempFeatures = $Config->get("WorkingDirectory").$PID."/map-features-$PID-$X-$Y-z$Zoom.xml";
    copy($source, $TempFeatures)
        or die "Cannot make copy of $source";

    # Update the rules file  with details of what to do (where to get data, what bounds to use)
    AddBounds($TempFeatures,$W,$S,$E,$N);
    SetDataSource($layerDataFile, $TempFeatures);

    # Render the file
    if (! xml2svg(
            $TempFeatures,
            $Config->get("WorkingDirectory").$PID."/output-$parent_pid-$X-$Y-z$Zoom.svg",
            $Zoom))
    {
        $error = 1;
        print "some error occured in xml2svg \n" if ($Config->get("Debug"));
    }
    # Delete temporary rules file
    killafile($TempFeatures) if (! $Config->get("Debug"));
    return $error;
}

#-----------------------------------------------------------------------------
# Add bounding-box information to an osm-map-features file
#-----------------------------------------------------------------------------
sub AddBounds 
{
    my ($Filename,$W,$S,$E,$N,$Size) = @_;
    
    # Read the old file
    open(my $fpIn, "<", "$Filename");
    my $Data = join("",<$fpIn>);
    close $fpIn;
    die("no such $Filename") if(! -f $Filename);
    
    # Change some stuff
    my $BoundsInfo = sprintf(
      "<bounds minlat=\"%f\" minlon=\"%f\" maxlat=\"%f\" maxlon=\"%f\" />",
      $S, $W, $N, $E);
    
    $Data =~ s/(<!--bounds_mkr1-->).*(<!--bounds_mkr2-->)/$1\n<!-- Inserted by tilesGen -->\n$BoundsInfo\n$2/s;
    
    # Save back to the same location
    open(my $fpOut, ">$Filename");
    print $fpOut $Data;
    close $fpOut;
}

#-----------------------------------------------------------------------------
# Set data source file name in map-features file
#-----------------------------------------------------------------------------
sub SetDataSource 
{
    my ($Datafile, $Rulesfile) = @_;

    # Read the old file
    open(my $fpIn, "<", "$Rulesfile");
    my $Data = join("",<$fpIn>);
    close $fpIn;
    die("no such $Rulesfile") if(! -f $Rulesfile);

    $Data =~ s/(  data=\").*(  scale=\")/$1$Datafile\"\n$2/s;

    # Save back to the same location
    open(my $fpOut, ">$Rulesfile");
    print $fpOut $Data;
    close $fpOut;
}

#-----------------------------------------------------------------------------
# Get the width and height (in SVG units, must be pixels) of an SVG file
#-----------------------------------------------------------------------------
sub getSize($)
{
    my $SVG = shift();
    my ($width, $height);
    open(my $fpSvg,"<",$SVG);
    while(my $Line = <$fpSvg>)
    {
        if($Line =~ /height=\"(.*)px\" width=\"(.*)px\"/)
        {
            ($width, $height)=($2,$1);
            last;
        }
    }
    close $fpSvg;
    return($height,$width,1) if ($width and $height);
    print "$SVG has no width and height info";
    return((0,0,0));
}

#-----------------------------------------------------------------------------
# Transform an OSM file (using osmarender) into SVG
#-----------------------------------------------------------------------------
sub xml2svg 
{
    my($MapFeatures, $SVG, $zoom) = @_;
    my $TSVG = "$SVG";
    my $NoBezier = $Config->get("NoBezier") || $zoom <= 11;

    if (!$NoBezier) 
    {
        $TSVG = "$SVG-temp.svg";
    }

    if ($Config->get("Osmarender") eq "XSLT")
    {
        my $XslFile;

        $XslFile = "osmarender/osmarender.xsl";

        my $Cmd = sprintf("%s \"%s\" tr --maxdepth %s %s %s > \"%s\"",
          $Config->get("Niceness"),
          $Config->get("XmlStarlet"),
          $Config->get("XmlStarletMaxDepth"),
          $XslFile,
          "$MapFeatures",
          $TSVG);

        statusMessage("Transforming zoom level $zoom with XSLT", $currentSubTask, $progressJobs, $progressPercent,0);
        runCommand($Cmd,$PID);
    }
    elsif($Config->get("Osmarender") eq "orp")
    {
        chdir "orp";
        my $Cmd = sprintf("%s perl orp.pl -r %s -o %s",
          $Config->get("Niceness"),
          $MapFeatures,
          $TSVG);

        statusMessage("Transforming zoom level $zoom with or/p", $currentSubTask, $progressJobs, $progressPercent,0);
        runCommand($Cmd,$PID);
        chdir "..";
    }
    else
    {
        die "invalid Osmarender setting in config";
    }

    # look at temporary svg wether it really is a svg or just the 
    # xmlstarlet dump and exit if the latter.
    open(SVGTEST, "<", $TSVG) || return cleanUpAndDie("xml2svg failed to open svg",$Mode,3,$PID);
    my $TestLine = <SVGTEST>;
    chomp $TestLine;
    close SVGTEST;

    if (grep(!/</, $TestLine))
    {
       statusMessage("File $TSVG doesn't look like svg, aborting render.", $currentSubTask, $progressJobs, $progressPercent,1);
       return cleanUpAndDie("xml2svg failed",$Mode,3,$PID);
    }
#-----------------------------------------------------------------------------
# Process lines to Bezier curve hinting
#-----------------------------------------------------------------------------
    if (!$NoBezier) 
    {   # do bezier curve hinting
        my $Cmd = sprintf("%s perl ./lines2curves.pl %s > %s",
          $Config->get("Niceness"),
          $TSVG,
          $SVG);
        statusMessage("Beziercurvehinting zoom level $zoom", $currentSubTask, $progressJobs, $progressPercent,0);
        runCommand($Cmd,$PID);
#-----------------------------------------------------------------------------
# Sanitycheck for Bezier curve hinting, no output = bezier curve hinting failed
#-----------------------------------------------------------------------------
        my $filesize= -s $SVG;
        if (!$filesize) 
        {
            copy($TSVG,$SVG);
            statusMessage("Error on Bezier Curve hinting, rendering without bezier curves", $currentSubTask, $progressJobs, $progressPercent,0);
        }
        killafile($TSVG) if (!$Config->get("Debug"));
    }
    else
    {   # don't do bezier curve hinting
        statusMessage("Bezier Curve hinting disabled.", $currentSubTask, $progressJobs, $progressPercent,0);
    }
    return 1;
}


#-----------------------------------------------------------------------------
# Render a SVG file
# $ZOrig - the lowest zoom level of the tileset
# $X, $Y - tilemnumbers of the tileset
# SizeX,Y - png size (i.e. 256x256)
#-----------------------------------------------------------------------------
sub svg2png
{
    my($svgFile, $Zoom, $ZOrig, $layer, $X1, $Y1, $X2, $Y2, $X, $Y, $OutputDir) = @_;
    my $SizeX = 256 * 2**($Zoom - $ZOrig);
    my $SizeY = $SizeX;
    
    my $TempFile;
    my $stdOut;
    my $TempDir = $Config->get("WorkingDirectory") . $parent_pid . "/";
    (undef, $TempFile) = tempfile($layer."_".$PID."_part-XXXXXX", DIR => $TempDir, SUFFIX => ".png", OPEN => 0);
    (undef, $stdOut) = tempfile("$PID-XXXXXX", DIR => $Config->get("WorkingDirectory"), SUFFIX => ".stdout", OPEN => 0);

    
    my $Cmd = "";
    
    my $Left = $X1;
    my $Top = $Y1;
    my $Width = $X2 - $X1;
    my $Height = $Y2 - $Y1;
    
    if ($Config->get("Batik") == "1") # batik as jar
    {
        $Cmd = sprintf("%s%s java -Xms256M -Xmx%s -jar %s -w %d -h %d -a %f,%f,%f,%f -m image/png -d \"%s\" \"%s%s\" > %s", 
        $Config->get("i18n") ? "LC_ALL=C " : "",
        $Config->get("Niceness"),
        $Config->get("BatikJVMSize"),
        $Config->get("BatikPath"),
        $SizeX,
        $SizeY,
        $Left,$Top,$Width,$Height,
        $TempFile,
        $Config->get("WorkingDirectory") . $parent_pid . "/",
        $svgFile,
        $stdOut);
    }
    elsif ($Config->get("Batik") == "2") # batik as executable (wrapper of some sort, i.e. on gentoo)
    {
        $Cmd = sprintf("%s%s %s -w %d -h %d -a %f,%f,%f,%f -m image/png -d \"%s\" \"%s%s\" > %s",
        $Config->get("i18n") ? "LC_ALL=C " : "",
        $Config->get("Niceness"),
        $Config->get("BatikPath"),
        $SizeX,
        $SizeY,
        $Left,$Top,$Width,$Height,
        $TempFile,
        $Config->get("WorkingDirectory") . $parent_pid . "/",
        $svgFile,
        $stdOut);
    }
    else
    {
        $Cmd = sprintf("%s%s \"%s\" -z -w %d -h %d --export-area=%f:%f:%f:%f --export-png=\"%s\" \"%s%s\" > %s", 
        $Config->get("i18n") ? "LC_ALL=C " : "",
        $Config->get("Niceness"),
        $Config->get("Inkscape"),
        $SizeX,
        $SizeY,
        $X1,$Y1,$X2,$Y2,
        $TempFile,
        $Config->get("WorkingDirectory") . $parent_pid . "/",
        $svgFile,
        $stdOut);
    }
    
    # stop rendering the current job when inkscape fails
    statusMessage("Rendering", $currentSubTask, $progressJobs, $progressPercent,0);
    print STDERR "\n$Cmd\n" if ($Config->get("Debug"));
    if (not runCommand($Cmd,$PID) or ! -e $TempFile )
    {
        statusMessage("$Cmd failed", $currentSubTask, $progressJobs, $progressPercent, 1);
        ## TODO: check this actually gets the correct coords 
        PutRequestBackToServer($X,$Y,$ZOrig,"BadSVG");
        addFault("inkscape",1);
        cleanUpAndDie("svg2png failed",$Mode,3,$PID);
        return (0,0);
    }
    resetFault("inkscape"); # reset to zero if inkscape succeeds at least once
    killafile($stdOut) if (not $Config->get("Debug"));
    
    my $ReturnValue = splitImage($TempFile, $layer, $ZOrig, $X, $Y, $Zoom, $Xtile, $Ytile); # returns true if tiles were all empty
    
    killafile($TempFile) if (not $Config->get("Debug"));
    return (1,$ReturnValue); #return true if empty
}

