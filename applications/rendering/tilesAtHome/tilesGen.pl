#!/usr/bin/perl
use LWP::UserAgent;
use Math::Trig;
use File::Copy;
use FindBin qw($Bin);
use tahconfig;
use tahlib;
use English '-no_match_vars';
use GD qw(:DEFAULT :cmp); 
use strict;
#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home
#
# Contact OJW on the Openstreetmap wiki for help using this program
#-----------------------------------------------------------------------------
# Copyright 2006, Oliver White, Etienne Cherdlu, Dirk-Lueder Kreie,
# Sebastian Spaeth and others
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
# Read the config file
my %Config = ReadConfig("tilesAtHome.conf", "general.conf", "authentication.conf", "layers.conf");
my %EnvironmentInfo = CheckConfig(%Config);

# Get version number from version-control system, as integer
my $Version = '$Revision$';
$Version =~ s/\$Revision:\s*(\d+)\s*\$/$1/;
printf STDERR "This is version %d (%s) of tilesgen running on %s\n", 
    $Version, $Config{ClientVersion}, $^O;

# check GD
eval GD::Image->trueColor(1);
if ($@ ne '') {
    print STDERR "please update your libgd to version 2 for TrueColor support";
    exit(3);
}

# Setup GD options
# currently unused (GD 2 truecolor mode)
#
#   my $numcolors = 256; # 256 is maximum for paletted output and should be used
#   my $dither = 0; # dithering on or off.
#
# dithering off should try to find a good palette, looks ugly on 
# neighboring tiles with different map features as the "optimal" 
# palette is chosen differently for different tiles.

# create a comparison blank image
my $EmptyLandImage = new GD::Image(256,256);
my $MapLandBackground = $EmptyLandImage->colorAllocate(248,248,248);
$EmptyLandImage->fill(127,127,$MapLandBackground);

my $EmptySeaImage = new GD::Image(256,256);
my $MapSeaBackground = $EmptySeaImage->colorAllocate(181,214,241);
$EmptySeaImage->fill(127,127,$MapSeaBackground);

# Some broken versions of Inkscape occasionally produce totally black
# output. We detect this case and throw an error when that happens.
my $BlackTileImage = new GD::Image(256,256);
my $BlackTileBackground = $BlackTileImage->colorAllocate(0,0,0);
$BlackTileImage->fill(127,127,$BlackTileBackground);

# set the progress indicator variables
my $currentSubTask;
my $progress = 0;
my $progressJobs = 0;
my $progressPercent = 0;

# Check the on disk image tiles havn't been corrupted
if( -s "emptyland.png" != 67 )
{
    print STDERR "Corruption detected in emptyland.png. Trying to redownload from svn automatically.\n";
    statusMessage("Downloading: emptyland.png", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
    DownloadFile(
      "http://svn.openstreetmap.org/applications/rendering/tilesAtHome/emptyland.png", # TODO: should be svn update, instead of http get... 
      "emptyland.png",
      0); ## 0=delete old file from disk first
}
if( -s "emptysea.png" != 69 )
{
    print STDERR "Corruption detected in emptysea.png. Trying to redownload from svn automatically.\n";
    statusMessage("Downloading: emptysea.png", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
    DownloadFile(
      "http://svn.openstreetmap.org/applications/rendering/tilesAtHome/emptysea.png", # TODO: should be svn update, instead of http get... 
      "emptysea.png",
      0); ## 0=delete old file from disk first
}
# Check the on disk image tiles are now in order
if( -s "emptyland.png" != 67 or
    -s "emptysea.png" != 69 )
{
  print STDERR "\nAutomatic fix failed. Exiting.\n";
  exit(3);
}
# Setup map projection
my $LimitY = ProjectF(85.0511);
my $LimitY2 = ProjectF(-85.0511);
my $RangeY = $LimitY - $LimitY2;

# Create the working directory if necessary
mkdir $Config{WorkingDirectory} if(!-d $Config{WorkingDirectory});

# Subdirectory for the current job (layer & z12 tileset),
# as used in sub GenerateTileset() and tileFilename()
my $JobDirectory;

# keep track of time running
my $progstart = time();
my $dirent; 

# keep track of the server time for current job
my $JobTime;

# hash for MagicMkdir
my %madeDir;

# Handle the command-line
my $Mode = shift();

if($Mode eq "xy")
{
    # ----------------------------------
    # "xy" as first argument means you want to specify a tileset to render
    # ----------------------------------
    my $X = shift();
    my $Y = shift();
    my $Zoom = shift() || 12;
    GenerateTileset($X, $Y, $Zoom);
}
elsif ($Mode eq "loop") 
{
    # ----------------------------------
    # Continuously process requests from server
    # ----------------------------------

    # if this is a re-exec, we want to capture some of our status
    # information from the command line. this feature allows setting
    # any numeric variable by specifying "variablename=value" on the
    # command line after the keyword "reexec". Currently unsuitable 
    # for alphanumeric variables.
    
    if (shift() eq "reexec")
    {
        my $idleSeconds; my $idleFor;
        while(my $evalstr = shift())
        {
            die unless $evalstr =~ /^[A-Za-z]+=\d+/;
            eval('$'.$evalstr);
            #print "$evalstr\n";
        }
        setIdle($idleSeconds, 1);
        setIdle($idleFor, 0);
    }

    # this is the actual processing loop
    
    while(1) 
    {
        reExecIfRequired();
        my ($did_something, $message) = ProcessRequestsFromServer();
        uploadIfEnoughTiles();
        if ($did_something == 0) 
        {
            talkInSleep($message, 60);
        } 
        else 
        {
            setIdle(0,0);
        }
    }
}
elsif ($Mode eq "upload") 
{
    upload();
}
elsif ($Mode eq "upload_conditional") 
{
    uploadIfEnoughTiles();
}
elsif ($Mode eq "version") 
{
    exit(1);
}
elsif ($Mode eq "") 
{
    # ----------------------------------
    # Normal mode downloads request from server
    # ----------------------------------
    my ($did_something, $message) = ProcessRequestsFromServer();
    
    talkInSleep($message, 60) unless($did_something);
    
}
else
{
    # ----------------------------------
    # "help" as first argument tells how to use the program
    # ----------------------------------
    my $Bar = "-" x 78;
    print "\n$Bar\nOpenStreetMap tiles\@home client\n$Bar\n";
    print "Usage: \nNormal mode:\n  \"$0\", will download requests from server\n";
    print "Specific area:\n  \"$0 xy <x> <y> [z]\"\n  (x and y coordinates of a zoom-12 (default) tile in the slippy-map coordinate system)\n  See [[Slippy Map Tilenames]] on wiki.openstreetmap.org for details\nz is optional and can be used for low-zoom tilesets\n";
    print "Other modes:\n";
    print "  $0 loop - runs continuously\n";
    print "  $0 upload - uploads any tiles\n";
    print "  $0 upload_conditional - uploads tiles if there are many waiting\n";
    print "  $0 version - prints out version string and exits\n";
    print "\nGNU General Public license, version 2 or later\n$Bar\n";
}

sub uploadIfEnoughTiles
{
    my $Count = 0;
    my $ZipCount = 0;

    # compile a list of the "Prefix" values of all configured layers,
    # separated by |
    my $allowedPrefixes = join("|", 
        map($Config{"Layer.$_.Prefix"}, split(/,/,$Config{"Layers"})));

    if (opendir(my $dp, $Config{WorkingDirectory}))
    {
        while(my $File = readdir($dp))
        {
            $Count++ if ($File =~ /($allowedPrefixes)_.*\.png/);
            $Count += 200 if ($File =~ /($allowedPrefixes)_.*\.dir/);
        }
        closedir($dp);
    } 
    else
    {
        mkdir $Config{WorkingDirectory};
    }

    if (opendir(my $dp, $Config{WorkingDirectory}."/uploadable"))
    {
        while(my $File = readdir($dp))
        {
            $ZipCount++ if ($File =~ /\.zip/);
        }
        closedir($dp);
    }
    else 
    {
        mkdir $Config{WorkingDirectory}."/uploadable";
    }

    if (($Count >= 200) or ($ZipCount >= 1))
    {
        upload();
    }
    #else
    #{
    #    print "Not uploading yet, only $Count tiles\n";
    #}
}

sub upload
{
    ## Run upload directly because it uses same messaging as tilesGen.pl, 
    ## no need to hide output at all.

    my $UploadScript = "perl $Bin/upload.pl $progressJobs";
    my $retval = system($UploadScript);
    return $retval;
}

#-----------------------------------------------------------------------------
# Ask the server what tileset needs rendering next
#-----------------------------------------------------------------------------
sub ProcessRequestsFromServer 
{
    my $LocalFilename = "$Config{WorkingDirectory}request.txt";

    if ($Config{"LocalSlippymap"})
    {
        print "Config option LocalSlippymap is set. Downloading requests\n";
        print "from the server in this mode would take them from the tiles\@home\n";
        print "queue and never upload the results. Program aborted.\n";
        exit 1;
    }
    
    # ----------------------------------
    # Download the request, and check it
    # Note: to find out exactly what this server is telling you, 
    # add ?help to the end of the URL and view it in a browser.
    # It will give you details of other help pages available,
    # such as the list of fields that it's sending out in requests
    # ----------------------------------
    killafile($LocalFilename);
    my $RequestUrlString = $Config{RequestURL} . "?version=" . $Config{ClientVersion} . "&user=" . $Config{UploadUsername} . "&layers=". $Config{Layers};
    # DEBUG: print "using URL " . $RequestUrlString . "\n";
    statusMessage("Downloading: Request from server", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
    DownloadFile(
        $RequestUrlString, 
        $LocalFilename, 
        0);
    
    if(! -f $LocalFilename){
        return (0, "Error reading request from server");
    }

    # Read into memory
    open(my $fp, "<", $LocalFilename) || return;
    my $Request = <$fp>;
    chomp $Request;
    close $fp;
    
    # Parse the request
    my ($ValidFlag,$Version,$X,$Y,$Z,$ModuleName) = split(/\|/, $Request);
    
    # First field is always "OK" if the server has actually sent a request
    if ($ValidFlag eq "XX")
    {
        return (0, "Server has no work for us"); 
    }
    elsif ($ValidFlag ne "OK")
    {
        return (0, "Server dysfunctional");
    }
    
    # Check what format the results were in
    # If you get this message, please do check for a new version, rather than
    # commenting-out the test - it means the field order has changed and this
    # program no longer makes sense!
    if ($Version != 3)
    {
        print STDERR "\n";
        print STDERR "Server is speaking a different version of the protocol to us.\n";
        print STDERR "Check to see whether a new version of this program was released!\n";
        exit(2);
    }
    
    # Information text to say what's happening
    statusMessage("Got work from the \"$ModuleName\" server module", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
    
    # Create the tileset requested
    GenerateTileset($X, $Y, $Z);
    return (1, "");
}

sub PutRequestBackToServer 
{
    ## TODO: will not be called in some libGD abort situations
    my ($X,$Y,$Cause) = @_;

    ## do not do this if called in xy mode!
    return if($Mode eq "xy");
    
    my $Prio = $Config{ReRequestPrio};
    
    my $LocalFilename = $Config{WorkingDirectory}."/requesting.txt";
    
    killafile($LocalFilename); # maybe not necessary if DownloadFile is called correctly?
    
    # http://dev.openstreetmap.org/~ojw/NeedRender/?x=1&y=2&priority=0&src=test
    my $RequestUrlString = $Config{ReRequestURL} . "?x=" . $X . "&y=" . $Y . "&priority=" . $Prio . "&src=" . $Config{UploadUsername}. ":tahClientReRequest:" . $Cause;
    
    statusMessage("Putting Job ".$X.",".$Y." back to server", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
    DownloadFile(
        $RequestUrlString, 
        $LocalFilename, 
        0);
    
    if(! -f $LocalFilename)
    {
        return (0, "Error reading response from server");
    }
    
    # Read into memory
    open(my $fp, "<", $LocalFilename) || return;
    my $Request = <$fp>;
    chomp $Request;
    close $fp;
    
    ## TODO: Check response for "OK" or "Duplicate Entry" (which would be OK, too)
    
    killafile($LocalFilename); # don't leave old files laying around

}

#-----------------------------------------------------------------------------
# Render a tile (and all subtiles, down to a certain depth)
#-----------------------------------------------------------------------------
sub GenerateTileset 
{
    my ($X, $Y, $Zoom) = @_;
    
    my ($N, $S) = Project($Y, $Zoom);
    my ($W, $E) = ProjectL($X, $Zoom);
    
    $progress = 0;
    $progressPercent = 0;
    $progressJobs++;
    $currentSubTask = "jobinit";
    
    statusMessage(sprintf("Doing tileset $X,$Y (area around %f,%f)", ($N+$S)/2, ($W+$E)/2), $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent, 1);
    
    if ( ($X < 0) or ($X > 4095) or ($Y < 0) or ($Y > 4095) )
    {
        #maybe do something else here
        die("\n Coordinates out of bounds (0..4095)\n");
    }
    
    $currentSubTask = "Preproc";
    
    my $DataFile = "data-$PID.osm";
    
    # Adjust requested area to avoid boundary conditions
    my $N1 = $N + ($N-$S)*$Config{BorderN};
    my $S1 = $S - ($N-$S)*$Config{BorderS};
    my $E1 = $E + ($E-$W)*$Config{BorderE};
    my $W1 = $W - ($E-$W)*$Config{BorderW};


    # TODO: verify the current system cannot handle segments/ways crossing the 
    # 180/-180 deg meridian and implement proper handling of this case, until 
    # then use this workaround: 

    if($W1 <= -180) {
      $W1 = -180; # api apparently can handle -180
    }
    if($E1 > 180) {
      $E1 = 180;
    }

    #------------------------------------------------------
    # Download data
    #------------------------------------------------------
    killafile($DataFile);
    my $URLS = sprintf("http://www.openstreetmap.org/api/0.4/map?bbox=%f,%f,%f,%f",
      $W1, $S1, $E1, $N1);
    if ($Zoom < 12) {
	# We only need the bounding box for ways (they will be downloaded completly,
        # but need the extended bounding box for places (names from neighbouring tiles)
	$URLS = sprintf("http://www.informationfreeway.org/api/0.4/way[natural=*][bbox=%f,%f,%f,%f] http://www.informationfreeway.org/api/0.4/way[boundary=*][bbox=%f,%f,%f,%f] http://www.informationfreeway.org/api/0.4/way[landuse=*][bbox=%f,%f,%f,%f] http://www.informationfreeway.org/api/0.4/way[highway=motorway|motorway_link|trunk|primary|secondary][bbox=%f,%f,%f,%f] http://www.informationfreeway.org/api/0.4/way[waterway=river][bbox=%f,%f,%f,%f] http://www.informationfreeway.org/api/0.4/way[waterway=river][bbox=%f,%f,%f,%f] http://www.informationfreeway.org/api/0.4/way[railway=*][bbox=%f,%f,%f,%f] http://www.informationfreeway.org/api/0.4/node[place=*][bbox=%f,%f,%f,%f]", $W1, $S1, $E1, $N1, $W1, $S1, $E1, $N1, $W1, $S1, $E1, $N1, $W1, $S1, $E1, $N1, $W1, $S1, $E1, $N1, $W1, $S1, $E1, $N1, $W1, $S1, $E1, $N1, $W1, $S1, $E1, $N1);
    }
    my @tempfiles;
    push(@tempfiles, $DataFile);
    my $filelist = [];
    my $i=0;
    foreach my $URL (split(/ /,$URLS)) {
	++$i;
        my $partialFile = "data-$PID-$i.osm";
        push(@{$filelist}, $partialFile);
        push(@tempfiles, $partialFile);
        statusMessage("Downloading: Map data to $partialFile", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
        DownloadFile($URL, $partialFile, 0);

        if (-s $partialFile == 0)
            {
                printf("No data here...\n");
                # if loop was requested just return  or else exit with an error. 
                # (to enable wrappers to better handle this situation 
                # i.e. tell the server the job hasn't been done yet)
                PutRequestBackToServer($X,$Y,"NoData");
		foreach my $file(@tempfiles) { killafile($file); }
                return if ($Mode eq "loop");
                exit(1); 
            }
    }

    mergeOsmFiles($DataFile, $filelist);
    
    # Get the server time for the data so we can assign it to the generated image (for tracking from when a tile actually is)
    $JobTime = [stat $DataFile]->[9];
    
    # Check for correct UTF8 (else inkscape will run amok later)
    # FIXME: This doesn't seem to catch all string errors that inkscape trips over.
    statusMessage("Checking for UTF-8 errors in $DataFile", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent, 0);
    open(OSMDATA, $DataFile) || die ("could not open $DataFile for UTF-8 check");
    my @toCheck = <OSMDATA>;
    close(OSMDATA);
    while (my $osmline = shift @toCheck)
    {
      if (utf8::is_utf8($osmline)) # this might require perl 5.8.1 or an explicit use statement
      {
        statusMessage("found incorrect UTF-8 chars in $DataFile, job $X $Y  $Zoom", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent, 1);
        PutRequestBackToServer($X,$Y,"BadUTF8");
        return 0 if ($Mode eq "loop");
        exit(1);
      }
    }

    #------------------------------------------------------
    # Handle all layers, one after the other
    #------------------------------------------------------

    foreach my $layer(split(/,/, $Config{Layers}))
    {
        #reset progress for each layer
        $progress=0;
        $progressPercent=0;
        $currentSubTask = $layer;
        
        $JobDirectory = sprintf("%s/%s_%d_%d_%d.tmpdir",
                                $Config{WorkingDirectory},
                                $Config{"Layer.$layer.Prefix"},
                                $Zoom, $X, $Y);
        mkdir $JobDirectory unless -d $JobDirectory;

        my $maxzoom = $Config{"Layer.$layer.MaxZoom"};
        my $layerDataFile;

        # Faff around
        for (my $i = $Zoom ; $i <= $maxzoom ; $i++) 
        {
            killafile("$Config{WorkingDirectory}output-$PID-z$i.svg");
        }
        
        my $Margin = " " x ($Zoom - 8);
        #printf "%03d %s%d,%d: %1.2f - %1.2f, %1.2f - %1.2f\n", $Zoom, $Margin, $X, $Y, $S,$N, $W,$E;
        
    
        #------------------------------------------------------
        # Go through preprocessing steps for the current layer
        #------------------------------------------------------
        my @ppchain = ($PID);
        # config option may be empty, or a comma separated list of preprocessors
        foreach my $preprocessor(split /,/, $Config{"Layer.$layer.Preprocessor"})
        {
            my $inputFile = sprintf("data-%s.osm", 
                join("-", @ppchain));
            push(@ppchain, $preprocessor);
            my $outputFile = sprintf("data-%s.osm", 
                join("-", @ppchain));

            if (-f $outputFile)
            {
                # no action; files for this preprocessing step seem to have been created 
                # by another layer already!
            }
            elsif ($preprocessor eq "frollo")
            {
                # Pre-process the data file using frollo
                if (not frollo($inputFile, $outputFile))
                {
                    # stop rendering if frollo fails, as current osmarender is 
                    # dependent on frollo hints
                    PutRequestBackToServer($X,$Y,"FrolloFail");
                    return 0 if ($Mode eq "loop"); 
                    exit(1); 
                }
            }
            elsif ($preprocessor eq "maplint")
            {
                # Pre-process the data file using maplint
                # TODO may put this into a subroutine of its own
                my $Cmd = sprintf("%s \"%s\" tr %s %s > \"%s\"",
                        $Config{Niceness},
                        $Config{XmlStarlet},
                        "maplint/lib/run-tests.xsl",
                        "$inputFile",
                        "tmp.$PID");
                statusMessage("Running maplint", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
                runCommand($Cmd,$PID);
                $Cmd = sprintf("%s \"%s\" tr %s %s > \"%s\"",
                        $Config{Niceness},
                        $Config{XmlStarlet},
                        "maplint/lib/convert-to-tags.xsl",
                        "tmp.$PID",
                        "$outputFile");
                statusMessage("Creating tags from maplint", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
                runCommand($Cmd,$PID);
                killafile("tmp.$PID");
            }
            elsif ($preprocessor eq "close-areas")
            {
                my $Cmd = sprintf("%s perl close-areas.pl $X $Y $Zoom < %s > %s",
                        $Config{Niceness},
                        "$inputFile",
                        "$outputFile");
                statusMessage("Running close-areas", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
                runCommand($Cmd,$PID);
            }
            elsif ($preprocessor eq "mercator")
            {
                my $Cmd = sprintf("%s perl mercatorize.pl %s > %s",
                        $Config{Niceness},
                        "$inputFile",
                        "$outputFile");
                statusMessage("Running Mercatorization", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
                runCommand($Cmd,$PID);
            }
            else
            {
                die "Invalid preprocessing step '$preprocessor'";
            }
            push(@tempfiles, $outputFile);
        }

        #------------------------------------------------------
        # Preprocessing finished, start rendering
        #------------------------------------------------------

        $layerDataFile = sprintf("data-%s.osm", join("-", @ppchain));
        
        # Add bounding box to osmarender
        # then set the data source
        # then transform it to SVG
        for (my $i = $Zoom ; $i <= $maxzoom; $i++) 
        {
            # Create a new copy of rules file
            # don't need zoom or layer in name of file as we'll
            # process one after the other
            my $source = $Config{"Layer.$layer.Rules.$i"};
            copy($source, "map-features-$PID.xml")
                or die "Cannot make copy of $source";

            # Update the rules file  with details of what to do (where to get data, what bounds to use)
            AddBounds("map-features-$PID.xml",$W,$S,$E,$N);    
            SetDataSource($layerDataFile, "map-features-$PID.xml");

            # Render the file
            xml2svg(
                    "map-features-$PID.xml",
                    "$Config{WorkingDirectory}output-$PID-z$i.svg",
                    $i);

            # Delete temporary rules file
            killafile("map-features-$PID.xml");
        }

        # Find the size of the SVG file
        my ($ImgH,$ImgW,$Valid) = getSize("$Config{WorkingDirectory}output-$PID-z$maxzoom.svg");

        # Render it as loads of recursive tiles
        my $empty = RenderTile($layer, $X, $Y, $Y, $Zoom, $Zoom, $N, $S, $W, $E, 0,0,$ImgW,$ImgH,$ImgH,0);

        # Clean-up the SVG files
        for (my $i = $Zoom ; $i <= $maxzoom; $i++) 
        {
            killafile("$Config{WorkingDirectory}output-$PID-z$i.svg");
        }

        #if $empty then the next zoom level was empty, so we only upload one tile
        if ($empty == 1 && $Config{GatherBlankTiles}) 
        {
            my $Filename=sprintf("%s_%s_%s_%s.png",$Config{"Layer.$layer.Prefix"}, $Zoom, $X, $Y);
            my $oldFilename = sprintf("%s/%s",$JobDirectory, $Filename); 
            my $newFilename = sprintf("%s/%s",$Config{WorkingDirectory},$Filename);
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

        if ($Config{LayerUpload}) 
        {
            uploadIfEnoughTiles();
        }
    }

    foreach my $file(@tempfiles) { killafile($file); }
    return 1;
}

#-----------------------------------------------------------------------------
# Render a tile
#   $X, $Y - which tileset (Always the z12 tilenumbers)
#   $Ytile, $Zoom - which tilestripe
#   $ZOrig, the lowest zoom level which called tileset generation
#   $N, $S, $W, $E - bounds of the tile
#   $ImgX1,$ImgY1,$ImgX2,$ImgY2 - location of the tile in the SVG file
#-----------------------------------------------------------------------------
sub RenderTile 
{
    my ($layer, $X, $Y, $Ytile, $Zoom, $ZOrig, $N, $S, $W, $E, $ImgX1,$ImgY1,$ImgX2,$ImgY2,$ImageHeight,$empty) = @_;

    return if($Zoom > $Config{"Layer.$layer.MaxZoom"});
    
    # no need to render subtiles if empty
    return if($empty == 1);

    # Render it to PNG
    # printf "$Filename: Lat %1.3f,%1.3f, Long %1.3f,%1.3f, X %1.1f,%1.1f, Y %1.1f,%1.1f\n", $N,$S,$W,$E,$ImgX1,$ImgX2,$ImgY1,$ImgY2; 
    my $Width = 256 * (2 ** ($Zoom - $ZOrig));  # Pixel size of tiles  
    my $Height = 256; # Pixel height of tile

    # svg2png returns true if all tiles extracted were empty. this might break 
    # if a higher zoom tile would contain data that is not rendered at the 
    # current zoom level. 
    if (svg2png($Zoom, $ZOrig, $layer, $Width, $Height,$ImgX1,$ImgY1,$ImgX2,$ImgY2,$ImageHeight,$X,$Y,$Ytile) and !$Config{"Layer.$layer.RenderFullTileset"}) 
    {
        $empty=1;
    }

    # Get progress percentage 
    if($empty == 1) 
    {
        # leap forward because this tile and all higher zoom tiles of it are "done" (empty).
        for (my $j = $Config{"Layer.$layer.MaxZoom"}; $j >= $Zoom ; $j--) 
        {
            $progress += 2 ** ($Config{"Layer.$layer.MaxZoom"}-$j);
        }
    }
    else 
    {
        $progress += 1;
    }

    if (($progressPercent=$progress*100/(2**($Config{"Layer.$layer.MaxZoom"}-$ZOrig+1)-1)) == 100)
    {
        statusMessage("Finished $X,$Y for layer $layer", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent, 1);
    }
    else
    {
        if ($Config{Verbose})
        {
            printf STDERR "Job No. %d %1.1f %% done.\n",$progressJobs, $progressPercent;
        }
        else
        {
            statusMessage("Working", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
        }
    }

    # Sub-tiles
    my $MercY2 = ProjectF($N);
    my $MercY1 = ProjectF($S);
    my $MercYC = 0.5 * ($MercY1 + $MercY2);
    my $LatC = ProjectMercToLat($MercYC);

    my $ImgYCP = ($MercYC - $MercY1) / ($MercY2 - $MercY1);
    my $ImgYC = $ImgY1 + ($ImgY2 - $ImgY1) * $ImgYCP;

    my $YA = $Ytile * 2;
    my $YB = $YA + 1;

    RenderTile($layer, $X, $Y, $YA, $Zoom+1, $ZOrig, $N, $LatC, $W, $E, $ImgX1, $ImgYC, $ImgX2, $ImgY2,$ImageHeight,$empty);
    RenderTile($layer, $X, $Y, $YB, $Zoom+1, $ZOrig, $LatC, $S, $W, $E, $ImgX1, $ImgY1, $ImgX2, $ImgYC,$ImageHeight,$empty);

    return $empty;
}

#-----------------------------------------------------------------------------
# Project latitude in degrees to Y coordinates in mercator projection
#-----------------------------------------------------------------------------
sub ProjectF 
{
    my $Lat = DegToRad(shift());
    my $Y = log(tan($Lat) + sec($Lat));
    return($Y);
}
#-----------------------------------------------------------------------------
# Project Y to latitude bounds
#-----------------------------------------------------------------------------
sub Project 
{
    my ($Y, $Zoom) = @_;
    
    my $Unit = 1 / (2 ** $Zoom);
    my $relY1 = $Y * $Unit;
    my $relY2 = $relY1 + $Unit;
    
    $relY1 = $LimitY - $RangeY * $relY1;
    $relY2 = $LimitY - $RangeY * $relY2;
    
    my $Lat1 = ProjectMercToLat($relY1);
    my $Lat2 = ProjectMercToLat($relY2);
    return(($Lat1, $Lat2));  
}

#-----------------------------------------------------------------------------
# Convert Y units in mercator projection to latitudes in degrees
#-----------------------------------------------------------------------------
sub ProjectMercToLat($)
{
    my $MercY = shift();
    return(RadToDeg(atan(sinh($MercY))));
}

#-----------------------------------------------------------------------------
# Project X to longitude bounds
#-----------------------------------------------------------------------------
sub ProjectL 
{
    my ($X, $Zoom) = @_;
    
    my $Unit = 360 / (2 ** $Zoom);
    my $Long1 = -180 + $X * $Unit;
    return(($Long1, $Long1 + $Unit));  
}

#-----------------------------------------------------------------------------
# Angle unit-conversions
#-----------------------------------------------------------------------------
sub DegToRad($){return pi * shift() / 180;}
sub RadToDeg($){return 180 * shift() / pi;}

#-----------------------------------------------------------------------------
# Gets latest copy of osmarender from repository
#-----------------------------------------------------------------------------
sub UpdateOsmarender 
{
    foreach my $File(("osm-map-features.xml", "osmarender.xsl", "Osm_linkage.png", "somerights20.png"))
    {
        statusMessage("Downloading: Osmarender ($File)", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
        DownloadFile(
          "http://almien.co.uk/OSM/Places/Download/$File", 
          $File,
          1);
    }
    # TODO: should be config option. 
    # TODO: should be SVN. 
    # TODO: should be called at all
    # TODO: should update other aspects of the client as well
}


#-----------------------------------------------------------------------------
# Pre-process on OSM file (using frollo)
#-----------------------------------------------------------------------------
sub frollo 
{
    my($dataFile, $outputFile) = @_;
    my $Cmd = sprintf("%s \"%s\" tr %s %s > \"%s\"",
      $Config{Niceness},
      $Config{XmlStarlet},
      "frollo1.xsl",
      "$dataFile",
      "temp-$PID.osm"); 
    statusMessage("Frolloizing (part I) ...", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
    if(runCommand($Cmd,$PID))
    {
        my $Cmd = sprintf("%s \"%s\" tr %s %s > \"%s\"",
          $Config{Niceness},
          $Config{XmlStarlet},
          "frollo2.xsl",
          "temp-$PID.osm",
          "$outputFile");
        statusMessage("Frolloizing (part II) ...", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
        if(runCommand($Cmd,$PID))
        {
            statusMessage("Frollification successful", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
            killafile("temp-$PID.osm");
        }
        else 
        {
            statusMessage("Frollotation failure (part II)", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
            killafile("temp-$PID.osm");
            killafile($outputFile);
            return 0;
        }
    } 
    else 
    {
        statusMessage("Failure of Frollotron (part I)", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
        killafile("temp-$PID.osm");
        return 0;
    }
    
    return 1;
}

#-----------------------------------------------------------------------------
# Transform an OSM file (using osmarender) into SVG
#-----------------------------------------------------------------------------
sub xml2svg 
{
    my($MapFeatures, $SVG, $zoom) = @_;
    my $TSVG = "$SVG";
    my $NoBezier = $Config{NoBezier} || $zoom <= 11;

    if (!$NoBezier) 
    {
        $TSVG = "$SVG-temp.svg";
    }
    my $Cmd = sprintf("%s \"%s\" tr %s %s > \"%s\"",
      $Config{Niceness},
      $Config{XmlStarlet},
      "osmarender/osmarender.xsl",
      "$MapFeatures",
      $TSVG);

    statusMessage("Transforming zoom level $zoom", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
    runCommand($Cmd,$PID);
#-----------------------------------------------------------------------------
# Process lines to Bezier curve hinting
#-----------------------------------------------------------------------------
    if (!$NoBezier) 
    {   # do bezier curve hinting
        my $Cmd = sprintf("%s perl ./lines2curves.pl %s > %s",
          $Config{Niceness},
          $TSVG,
          $SVG);
        statusMessage("Beziercurvehinting zoom level $zoom", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
        runCommand($Cmd,$PID);
#-----------------------------------------------------------------------------        
# Sanitycheck for Bezier curve hinting, no output = bezier curve hinting failed
#-----------------------------------------------------------------------------
        my $filesize= -s $SVG;
        if (!$filesize) 
        {
            copy($TSVG,$SVG);
            statusMessage("Error on Bezier Curve hinting, rendering without bezier curves", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
        }
        killafile($TSVG);
    }
    else
    {   # don't do bezier curve hinting
        statusMessage("Bezier Curve hinting disabled.", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
    }

}

#-----------------------------------------------------------------------------
# Render a SVG file
# $ZOrig - the lowest zoom level of the tileset
# $X, $Y - tilemnumbers of the z12 tile containing the data we're working on
# $Ytile - the actual tilenumber in Y-coordinate of the zoom we are processing
#-----------------------------------------------------------------------------
sub svg2png 
{
    my($Zoom, $ZOrig, $layer, $SizeX, $SizeY, $X1, $Y1, $X2, $Y2, $ImageHeight, $X, $Y, $Ytile) = @_;
    
    my $TempFile = $Config{WorkingDirectory}."/$PID.png_part";
    
    my $stdOut = $Config{WorkingDirectory}."/".$PID.".stdout";
  
    my $Cmd = sprintf("%s%s \"%s\" -w %d -h %d --export-area=%f:%f:%f:%f --export-png=\"%s\" \"%s%s\" > %s", 
      $Config{i18n} ? "LC_ALL=C " : "",
      $Config{Niceness},
      $Config{Inkscape},
      $SizeX,
      $SizeY,
      $X1,$Y1,$X2,$Y2,
      $TempFile,
      $Config{WorkingDirectory},
      "output-$PID-z$Zoom.svg",
      $stdOut);
    
    # stop rendering the current job when inkscape fails
    statusMessage("Rendering", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
    if (not runCommand($Cmd,$PID))
    {
        statusMessage("$Cmd failed", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent, 1);
        ## TODO: check this actually gets the correct coords 
        PutRequestBackToServer($X,$Y,"BadSVG");
        return 0 if ($Mode eq "loop");
        exit(1);
    }

    killafile($stdOut);
    
    my $ReturnValue = splitImageX($TempFile, $layer, $ZOrig, $X, $Y, $Zoom, $Ytile); # returns true if tiles were all empty
    
    killafile($TempFile);
    
    return $ReturnValue; #return true if empty
}

sub writeToFile 
{
    open(my $fp, ">", shift()) || return;
    print $fp shift();
    close $fp;
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
    open(my $fpSvg,"<",$SVG);
    while(my $Line = <$fpSvg>)
    {
        if($Line =~ /height=\"(.*)px\" width=\"(.*)px\"/)
        {
            close $fpSvg;
            return(($1,$2,1));
        }
    }
    close $fpSvg;
    return((0,0,0));
}

#-----------------------------------------------------------------------------
# Temporary filename to store a tile
#-----------------------------------------------------------------------------
sub tileFilename 
{
    my($layer,$X,$Y,$Zoom) = @_;
    return(sprintf($Config{LocalSlippymap} ? "%s/%s/%d/%d/%d.png" : "%s/%s_%d_%d_%d.png",
        $Config{LocalSlippymap} ? $Config{WorkingDirectory} : $JobDirectory,
        $Config{"Layer.$layer.Prefix"},
        $Zoom,
        $X,
        $Y));
}

#-----------------------------------------------------------------------------
# Merge multiple OSM files into one, making sure that elements are present in
# the destination file only once even if present in more than one of the input
# files.
# 
# This has become necessary in the course of supporting maplint, which would
# get upset about duplicate objects created by combining downloaded stripes.
#-----------------------------------------------------------------------------
sub mergeOsmFiles()
{
    my ($destFile, $sourceFiles) = @_;
    my $existing = {};

    open (DEST, "> $destFile");

    # copying stub file replaced by simply using the introductory lines of 
    # the first file to be merged!
    #
    # first, copy stub
    # open (STUB, "stub.osm");
    # while(<STUB>)
    # {
    #     print DEST;
    # }
    # close(STUB);

    my $copy = 1; # copy <osm...> from first file
    foreach my $sourceFile(@{$sourceFiles})
    {
        open(SOURCE, $sourceFile);
        while(<SOURCE>)
        {
            if ($copy)
            {
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
            else
            {
                $copy = 1 if (/^\s*<osm /);
            }
        }
        close(SOURCE);
        $copy = 0;
    }
    print DEST "</osm>\n";
    close(DEST);
}

#-----------------------------------------------------------------------------
# Split a tileset image into tiles
#-----------------------------------------------------------------------------
sub splitImageX 
{
    my ($File, $layer, $ZOrig, $X, $Y, $Z, $Ytile) = @_;
  
    # Size of tiles
    my $Pixels = 256;
  
    # Number of tiles
    my $Size = 2 ** ($Z - $ZOrig);

    # Assume the tileset is empty by default
    my $allempty=1;
  
    # Load the tileset image
    statusMessage(sprintf("Splitting %s (%d x 1)", $File, $Size), $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent, 0);
    my $Image = newFromPng GD::Image($File);
  
    # Use one subimage for everything, and keep copying data into it
    my $SubImage = new GD::Image($Pixels,$Pixels);
  
    # For each subimage
    for(my $xi = 0; $xi < $Size; $xi++)
    {
        # Get a tiles'worth of data from the main image
        $SubImage->copy($Image,
          0,                   # Dest X offset
          0,                   # Dest Y offset
          $xi * $Pixels,       # Source X offset
          0,                   # Source Y offset # always 0 because we only cut from one row
          $Pixels,             # Copy width
          $Pixels);            # Copy height

        # Decide what the tile should be called
        my $Filename = tileFilename($layer, $X * $Size + $xi, $Ytile, $Z);
        MagicMkdir($Filename) if ($Config{"LocalSlippymap"});
   
        # Temporary filename
        my $Filename2 = "$Filename.cut";
        my $Basename = $Filename;   # used for statusMessage()
        $Basename =~ s|.*/||;

        # Check for black tile output
        if (not ($SubImage->compare($BlackTileImage) & GD_CMP_IMAGE)) 
        {
            print STDERR "\nERROR: Your inkscape has just produced a totally black tile. This usually indicates a broken Inkscape, please upgrade.\n";
            PutRequestBackToServer($X,$Y,"BlackTile");
            exit(3);
        }
        # Detect empty tile here:
        elsif (not($SubImage->compare($EmptyLandImage) & GD_CMP_IMAGE)) # libGD comparison returns true if images are different. (i.e. non-empty Land tile) so return the opposite (false) if the tile doesn''t look like an empty land tile
        {
            copy("emptyland.png", $Filename);
        }
        elsif (not($SubImage->compare($EmptySeaImage) & GD_CMP_IMAGE)) # same for Sea tiles
        {
            copy("emptysea.png",$Filename);
#            $allempty = 0; # TODO: enable this line if/when serverside empty tile methods is implemented. Used to make sure we                                     generate all blank seatiles in a tileset.
        }
        else
        {
            # If at least one tile is not empty set $allempty false:
            $allempty = 0;
    
            if ($Config{"Layer.$layer.Transparent"}) 
            {
                $SubImage->transparent($SubImage->colorAllocate(248,248,248));
            } 
            else 
            {
                $SubImage->transparent(-1);
            }

            # convert Tile to paletted file This *will* break stuff if different libGD versions are used
            # $SubImage->trueColorToPalette($dither,$numcolors);

            # Store the tile
            statusMessage(" -> $Basename", $Config{Verbose}, 0) if ($Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
            WriteImage($SubImage,$Filename2);
#-----------------------------------------------------------------------------
# Run pngcrush on each split tile, then delete the temporary cut file
#-----------------------------------------------------------------------------
            my $Redirect = ">/dev/null";
			if ($^O eq "MSWin32") {
				$Redirect = "";
			}
			my $Cmd = sprintf("%s %s -q %s %s %s",
				$Config{Pngcrush},
				$Config{Niceness},
				$Filename2,
				$Filename,
				$Redirect);

            statusMessage("Pngcrushing $Basename", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
            if(runCommand($Cmd,$PID))
            {
                unlink($Filename2);
            }
            else
            {
                statusMessage("Pngcrushing $Basename failed", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,1);
                rename($Filename2, $Filename);
            }
        }
        # Assign the job time to this file
        utime $JobTime, $JobTime, $Filename;
    }
    undef $SubImage;
    # tell the rendering queue wether the tiles are empty or not
    return $allempty;
}

#-----------------------------------------------------------------------------
# Write a GD image to disk
#-----------------------------------------------------------------------------
sub WriteImage 
{
    my ($Image, $Filename) = @_;
    
    # Get the image as PNG data
    my $png_data = $Image->png;
    
    # Store it
    open (my $fp, ">$Filename") || die;
    binmode $fp;
    print $fp $png_data;
    close $fp;
}

#-----------------------------------------------------------------------------
# Create a directory and all its parent directories
# (equivalent to a "mkdir -p" on Unix, but stores already-created dirs
# in a hash to avoid unnecessary system calls)
#-----------------------------------------------------------------------------
sub MagicMkdir
{
    my $file = shift;
    my @paths = split("/", $file);
    pop(@paths);
    my $dir = "";
    foreach my $path(@paths)
    {
        $dir .= "/".$path;
        if (!defined($madeDir{$dir}))
        {
            mkdir $dir;
            $madeDir{$dir}=1;
        }
    }
}

#-----------------------------------------------------------------------------
# A function to re-execute the program.  
#
# This function attempts to detect whether the perl script has changed
# since it was invoked initially, and if so, just runs the new version.
# This can be used to update the program while it is running (as it is
# sometimes hard to hit Ctrl-C at exactly the right moment!)
#-----------------------------------------------------------------------------
sub reExecIfRequired
{
    # until proven to work with other systems, only attempt a re-exec
    # on linux. 
    return unless ($^O eq "linux" || $^O eq "cygwin");

    my ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size,$atime,$mtime,
        $ctime,$blksize,$blocks) = stat($0);
    my $de = "$size/$mtime/$ctime";
    if (!defined($dirent))
    {
        $dirent = $de; 
        return;
    }
    elsif ($dirent ne $de)
    {
        statusMessage("tilesGen.pl has changed, re-start new version", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent, 1);
        exec "perl", $0, "loop", "reexec", 
            "progressJobs=$progressJobs", 
            "idleSeconds=" . getIdle(1), 
            "idleFor=" . getIdle(0), 
            "progstart=$progstart" or die;
    }
}

