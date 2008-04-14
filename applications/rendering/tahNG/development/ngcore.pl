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
use Image::Magick;
use LWP::UserAgent;
use Math::Trig;
use File::Copy;
use File::Temp qw(tempfile);
use AppConfig qw(:argcount);
use FindBin qw($Bin);
use English '-no_match_vars';
use tahconfig;
#use tahlib;
use tahproject;

my $Config = AppConfig->new({ 
            CREATE => 1,                      # Autocreate unknown config variables
            GLOBAL => {
                    DEFAULT  => "<undef>",    # Create undefined Variables by default
                    ARGCOUNT => ARGCOUNT_ONE, # Simple Values (no arrays, no hashmaps)
                }
        });

$Config->define("help|usage!");
$Config->file("config.defaults", "authentication.conf", "tahng.conf");
$Config->args();
$Config->file("config.svn");
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

GenerateTilesets($X, $Y, $Zoom);


sub statusMessage
{
    my $message = shift();
    print $message."\n";
}

sub resetFault
{
   1;
}

sub runCommand
{
    my ($cmd,$mainPID) = @_;
    my $retval = system($cmd);
    return $retval == 0;
}

#-----------------------------------------------------------------------------
# Delete a file if it exists
#-----------------------------------------------------------------------------
sub killafile($){
  my $file = shift();
  unlink $file if(-f $file);
}

#------------------------------------------------------
# Download data
#------------------------------------------------------
sub downloadData
{
    my ($bbox,$bboxref,$DataFile) = @_;
    
    killafile($DataFile);
    my $URLS = sprintf("%s%s/*[bbox=%s]", $Config->get("XAPIURL"),$Config->get("OSMVersion"),$bbox);

    my @tempfiles;
    push(@tempfiles, $DataFile);
    my $filelist = [];
    my $i=0;
    foreach my $URL (split(/ /,$URLS)) 
    {
        ++$i;
        my $partialFile = $Config->get("WorkingDirectory")."data-$PID-$bboxref-$i.osm";
        push(@{$filelist}, $partialFile);
        push(@tempfiles, $partialFile);
        statusMessage("Downloading: Map data for ".$Config->get("Layers")." to ".$partialFile, $currentSubTask, $progressJobs, $progressPercent,0);
        print "Download $URL\n" if ($Config->get("Debug"));
        DownloadFile($URL, $partialFile, 0);

        if (-s $partialFile == 0)
        {
            if ($Zoom < 12)
            {
                statusMessage("No data here...", $currentSubTask, $progressJobs, $progressPercent, 1);
                # if loop was requested just return  or else exit with an error. 
                # (to enable wrappers to better handle this situation 
                # i.e. tell the server the job hasn't been done yet)
                PutRequestBackToServer($X,$Y,$Zoom,"NoData");
                foreach my $file(@tempfiles) { killafile($file); }
                addFault("nodataXAPI",1);
                return cleanUpAndDie("GenerateTileset: no data!",$Mode,1,$PID);
            }
        }
        else
        {
            if ($Zoom < 12)
            {
                resetFault("nodataXAPI"); #reset to zero if data downloaded
            }
            else 
            {
                resetFault("nodata"); #reset to zero if data downloaded
            }
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
    ($lon[0],$lon[4]) = ProjectL($X, $Zoom);

    ($lat[0],$lat[1]) = Project($Y * 4, $Z);
    ($lat[2],$lat[3]) = Project($Y * 4 + 2, $Z);
    ($lat[0],$lat[4]) = Project($Y, $Zoom);

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


    # now build the 16 zoom+2 (usually z14) bboxes

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
                    $N1 = $lat[$QR * 2 + $SR];
                    $S1 = $lat[$QR * 2 + $SR + 1];
                    $W1 = $lon[$QC * 2 + $SC];
                    $E1 = $lon[$QC * 2 + $SC + 1];
                    $bboxRef = sprintf("%d-%d-%d", $Z, $QR*2 + $QC, $SR*2 + $SC); # ref: 12-0..3-0..3 will be used later in stitching.
                    $bbox{$bboxRef} = sprintf("%f,%f,%f,%f", $W1, $S1, $E1, $N1);
                }
            }
        }
    }

    my $filelist = [];
    foreach $bboxRef (sort (keys %bbox)) 
    {
        print $bboxRef.": ".$bbox{$bboxRef}."\n" if $Config->get("Debug");
        $DataFile = $Config->get("WorkingDirectory").$PID."/data-$bboxRef.osm"; ## FIXME broken TODO: make sure tempdir is created.
        my ($status,@tempfiles) = downloadData($bbox{$bboxRef},$bboxRef,$DataFile);
        push(@{$filelist}, $DataFile) if (-s $DataFile > 0 and $status);
    }

    $DataFile = $Config->get("WorkingDirectory").$PID."/data-z12.osm"; # FIXME: the part between "data-" annd ".osm" should be a variable, it's needed later

    mergeOsmFiles($DataFile, $filelist);

    if ($Config->get("KeepDataFile"))
    {
        copy($DataFile, $Config->get("WorkingDirectory") . "/" . "data.osm");
    }
    
    $currentSubTask = "Preproc";
    
    # Get the server time for the data so we can assign it to the generated image (for tracking from when a tile actually is)
    $JobTime = [stat $DataFile]->[9]; ## TODO: change this to use the XAPI timestamp which is a more accurate measure for from when the data is
    
    # Check for correct UTF8 (else inkscape will run amok later)
    # FIXME: This doesn't seem to catch all string errors that inkscape trips over.
    statusMessage("Checking for UTF-8 errors in $DataFile", $currentSubTask, $progressJobs, $progressPercent, 0);
    open(OSMDATA, $DataFile) || die ("could not open $DataFile for UTF-8 check");
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
        my $layerDataFile;

        # Faff around
        for (my $i = $Zoom ; $i <= $maxzoom ; $i++) 
        {
            killafile($Config->get("WorkingDirectory").$PID."/output-$parent_pid-z$i.svg");
        }
        
        my $Margin = " " x ($Zoom - 8);
        #printf "%03d %s%d,%d: %1.2f - %1.2f, %1.2f - %1.2f\n", $Zoom, $Margin, $X, $Y, $S,$N, $W,$E;
        
        
        #------------------------------------------------------
        # Go through preprocessing steps for the current layer
        #------------------------------------------------------
        my @ppchain = ("z12");
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
            elsif ($preprocessor eq "mercator")
            {
                my $Cmd = sprintf("%s perl mercatorize.pl %s > %s",
                        $Config->get("Niceness"),
                        "$inputFile",
                        "$outputFile");
                statusMessage("Running Mercatorization", $currentSubTask, $progressJobs, $progressPercent,0);
                runCommand($Cmd,$PID);
            }
            else
            {
                die "I have no preprocessor called '$preprocessor'";
            }
## Uncomment to have the output files checked for validity
#            if( $preprocessor ne "maplint" )
#            {
#              runCommand( qq(xmllint --dtdvalid http://dev.openstreetmap.org/~kleptog/tilesAtHome-0.3.dtd --noout $outputFile), $PID );
#            }
            push(@tempfiles, $outputFile);
        }

        #------------------------------------------------------
        # Preprocessing finished, start rendering
        #------------------------------------------------------

        #$layerDataFile = sprintf("%sdata-%s.osm", $Config->get("WorkingDirectory"), join("-", @ppchain));
        $layerDataFile = sprintf("data-%s.osm", join("-", @ppchain)); # Don't put working directory here, the path is relative to the rulesfile
        
        # Add bounding box to osmarender
        # then set the data source
        # then transform it to SVG
        
        for (my $i = $Zoom ; $i <= $maxzoom; $i++)
        {
            if (GenerateSVG($layerDataFile, $layer, $X, $Y, $i, $lat[0], $lat[4], $lon[0], $lon[4]))
            {
                foreach my $file(@tempfiles) { killafile($file) if (!$Config->get("Debug")); }
                return 0;
            }
        }
        
        
        # Find the size of the SVG file
        my ($ImgH,$ImgW,$Valid) = getSize($Config->get("WorkingDirectory")."output-$parent_pid-z$maxzoom.svg");

        # Render it as loads of recursive tiles
        my ($success,$empty) = RenderTile($layer, $X, $Y, $Y, $Zoom, $Zoom, $lat[0], $lat[4], $lon[0], $lon[4], 0,0,$ImgW,$ImgH,$ImgH,0);
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


#-----------------------------------------------------------------------------
# GET a URL and save contents to file
#-----------------------------------------------------------------------------
sub DownloadFile 
{
    my ($URL, $File, $UseExisting) = @_;

    my $ua = LWP::UserAgent->new(keep_alive => 1, timeout => $Config->get("DownloadTimeout"));
    $ua->agent("tilesAtHome");
    $ua->env_proxy();

    if(!$UseExisting) 
    {
        killafile($File);
    }

    # Note: mirror sets the time on the file to match the server time. This
    # is important for the handling of JobTime later.
    $ua->mirror($URL, $File);

    doneMessage(sprintf("done, %d bytes", -s $File));
}
#-----------------------------------------------------------------------------
# Used to display task completion. Only for verbose mode.
#-----------------------------------------------------------------------------
sub doneMessage
{
    my ($msg,$Verbose) = @_;
    $msg = "done" if ($msg eq "");

    if ($Verbose)
    {
        print STDERR "$msg\n";
        return;
    }
}

#-----------------------------------------------------------------------------
# Merge multiple OSM files into one, making sure that elements are present in
# the destination file only once even if present in more than one of the input
# files.
# 
# This has become necessary in the course of supporting maplint, which would
# get upset about duplicate objects created by combining downloaded stripes.
#-----------------------------------------------------------------------------
sub mergeOsmFiles
{
    my ($destFile, $sourceFiles) = @_;
    my $existing = {};

    # If there's only one file, just copy the input to the output
    if( scalar(@$sourceFiles) == 1 )
    {
      copy $sourceFiles->[0], $destFile;
      killafile ($sourceFiles->[0]) if (! $Config->get("Debug"));
      return;
    }
    
    open (DEST, "> $destFile");

    print DEST qq(<?xml version="1.0" encoding="UTF-8"?>\n);
    my $header = 0;

    foreach my $sourceFile(@{$sourceFiles})
    {
        open(SOURCE, $sourceFile);
        while(<SOURCE>)
        {
            next if /^\s*<\?xml/;
            # We want to copy the version number, but only the first time (obviously)
            # Handle where the input doesn't have a version
            if (/^\s*<osm.*(?:version=([\d.'"]+))?/)
            {
              if( not $header )
              {
                my $version = $1 || "'".$Config->get("OSMVersion")."'";
                print DEST qq(<osm version=$version generator="tahlib.pm mergeOsmFiles" xmlns:osmxapi="http://www.informationfreeway.org/osmxapi/0.5">\n);
                $header = 1;
              }
              next;
            }
            last if (/^\s*<\/osm>/);
            if (/^\s*<(node|segment|way|relation) id="(\d+)".*(.)>/)
            {
                my ($what, $id, $slash) = ($1, $2, $3);
                my $key = substr($what, 0, 1) . $id;
                if (defined($existing->{$key}))
                {
                    # object exists already. skip!
                    next if ($slash eq "/");
                    while(<SOURCE>)
                    {
                        last if (/^\s*<\/$what>/);
                    }
                    next;
                }
                else
                {
                    # object didn't exist, note
                    $existing->{$key} = 1;
                }
            }
            print DEST;
        }
        close(SOURCE);
        killafile ($sourceFile) if (!$Config->get("Debug"));
    }
    print DEST "</osm>\n";
    close(DEST);
}

