package Tileset;

=pod

=head1 Tileset package

=head2 Copyright and Authors

Copyright 2006-2008, Dirk-Lueder Kreie, Sebastian Spaeth,
Matthias Julius and others

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

=head2 Description of functions 

=cut

use warnings;
use strict;
use File::Temp qw/ tempfile tempdir /;
use Error qw(:try);
use lib::TahConf;
use lib::Server;
use tahlib;
use tahproject;
use File::Copy;
use File::Path;
use GD qw(:DEFAULT :cmp);
use POSIX qw(locale_h);

#-----------------------------------------------------------------------------
# creates a new Tileset instance and returns it
# parameter is a request object with x,y,z, and layer atributes set
# $self->{WorkingDir} is a temporary directory that is only used by this job and
# which is deleted when the Tileset instance is not in use anymore.
#-----------------------------------------------------------------------------
sub new
{
    my $class = shift;
    my $Config = TahConf->getConfig();
    my $req = shift;    #Request object

    my $self = {
        req => $req,
        Config => $Config,
        JobTime => undef,     # API fetching time for the job as timestamp
        bbox => undef,        # bbox of required tileset
        marg_bbox => undef,   # bbox of required tileset including margins
        childThread => 0,     # marks whether we are a parent or child thread
        };

    my $delTmpDir = 1-$Config->get('Debug');

    $self->{JobDir} = tempdir( 
         sprintf("%d_%d_%d_XXXXX",$self->{req}->ZXY),
         DIR      => $Config->get('WorkingDirectory'), 
	 CLEANUP  => $delTmpDir,
         );

    # create blank comparison images
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

    $self->{EmptyLandImage} = $EmptyLandImage;
    $self->{EmptySeaImage} = $EmptySeaImage;
    $self->{BlackTileImage} = $BlackTileImage;

    bless $self, $class;
    return $self;
}

#-----------------------------------------------------------------------------
# Tileset destructor. Call cleanup in case we did not clean up properly earlier.
#-----------------------------------------------------------------------------
sub DESTROY
{
    my $self = shift;
    # Don't clean up in child threads
    return if ($self->{childThread});

    # only cleanup if we are the parent thread
    $self->cleanup();
}

#-----------------------------------------------------------------------------
# generate does everything that is needed to end up with a finished tileset
# that just needs compressing and uploading. It outputs status messages, and
# hands back the job to the server in case of critical errors.
#-----------------------------------------------------------------------------
sub generate
{
    my $self = shift;
    my $req =  $self->{req};
    my $Config = $self->{Config};
    
    ::keepLog($$,"GenerateTileset","start","x=".$req->X.',y='.$req->Y.',z='.$req->Z." for layers ".$req->layers_str);
    
    $self->{bbox}= bbox->new(ProjectXY($req->ZXY));

    ::statusMessage(sprintf("Tileset (%d,%d,%d) around %.2f,%.2f", $req->ZXY, $self->{bbox}->center), 1, 0);
    
    #------------------------------------------------------
    # Download data (returns full path to data.osm or 0)
    #------------------------------------------------------

    my $beforeDownload = time();
    my $FullDataFile = $self->downloadData();
    ::statusMessage("Download in ".(time() - $beforeDownload)." sec",1,10); 

    #------------------------------------------------------
    # Handle all layers, one after the other
    #------------------------------------------------------

    foreach my $layer($req->layers)
    {
        #reset progress for each layer
        $::progress=0;
        $::progressPercent=0;
        $::currentSubTask = $layer;
        
        # TileDirectory is the name of the directory for finished tiles
        my $TileDirectory = sprintf("%s_%d_%d_%d.dir", $Config->get($layer."_Prefix"), $req->ZXY);

        # JobDirectory is the directory where all final .png files are stored.
        # It is not used for temporary files.
        my $JobDirectory = File::Spec->join($self->{JobDir}, $TileDirectory);
        mkdir $JobDirectory;

        #------------------------------------------------------
        # Go through preprocessing steps for the current layer
        # This puts preprocessed files like data-maplint-closeareas.osm in $self->{JobDir}
        # and returns the file name of the resulting data file.
        #------------------------------------------------------

        my $layerDataFile = $self->runPreprocessors($layer);

        #------------------------------------------------------
        # Preprocessing finished, start rendering to SVG to PNG
        # $layerDataFile is just the filename
        #------------------------------------------------------

        if ($Config->get("Fork")) 
        {   # Forking to render zoom levels in parallel
            $self->forkedRender($layer, $layerDataFile);
        }
        else
        {   # Non-forking render
            $self->nonForkedRender($layer, $layerDataFile);
        }

        #----------
        # This directory is now ready for upload.
        # move it to the working directory, so it can be picked up.
        # Unless we have moved everything to the local slippymap already
        if (!$Config->get("LocalSlippymap"))
        {
            my $DestDir = File::Spec->join($Config->get("WorkingDirectory"), $TileDirectory);
            rename $JobDirectory, $DestDir;
        }
    }

    ::keepLog($$,"GenerateTileset","stop",'x='.$req->X.',y='.$req->Y.',z='.$req->Z." for layers ".$req->layers_str);

    # Cleaning up of tmpdirs etc. are called in the destructor DESTROY
}

#------------------------------------------------------------------

=pod 

=head3 downloadData

Download the area for the tileset (whole or in stripes, as required)
into $self->{JobDir}

B<parameter>: none

B<returns>: filename
I<filename>: resulting data osm filename (without path).

=cut
#-------------------------------------------------------------------
sub downloadData
{
    my $self = shift;
    my $req = $self->{req};
    my $Config = $self->{Config};

    $::progress = 0;
    $::progressPercent = 0;
    $::currentSubTask = "Download";
    
    # Adjust requested area to avoid boundary conditions
    my $N1 = $self->{bbox}->N + ($self->{bbox}->N-$self->{bbox}->S)*$Config->get("BorderNS");
    my $S1 = $self->{bbox}->S - ($self->{bbox}->N-$self->{bbox}->S)*$Config->get("BorderNS");
    my $E1 = $self->{bbox}->E + ($self->{bbox}->E-$self->{bbox}->W)*$Config->get("BorderWE");
    my $W1 = $self->{bbox}->W - ($self->{bbox}->E-$self->{bbox}->W)*$Config->get("BorderWE");
    $self->{marg_bbox} = bbox->new($N1,$E1,$S1,$W1);

    # TODO: verify the current system cannot handle segments/ways crossing the 
    # 180/-180 deg meridian and implement proper handling of this case, until 
    # then use this workaround: 

    if($W1 <= -180) {
      $W1 = -180; # api apparently can handle -180
    }
    if($E1 > 180) {
      $E1 = 180;
    }

    my $bbox = sprintf("%f,%f,%f,%f",
      $W1, $S1, $E1, $N1);

    my $DataFile = File::Spec->join($self->{JobDir}, "data.osm");
    
    my @predicates;
    foreach my $layer ($req->layers()) {
        my %layer_config = $Config->varlist("^${layer}_", 1);
        if (not $layer_config{"predicates"}) {
            @predicates = ();
            last;
        }
        my $predicates = $layer_config{"predicates"};
        # strip spaces in predicates
        $predicates =~ s/\s+//g;
        push(@predicates, split(/,/, $predicates));
    }

    my @OSMServers = (@predicates) ? split(/,/, $Config->get("XAPIServers")) : split(/,/, $Config->get("APIServers"));

    my $Server = Server->new();
    my $res;
    my $filelist;
    foreach my $OSMServer (@OSMServers) {
        my @URLS;
        if (@predicates) {
            foreach my $predicate (@predicates) {
                my $URL = $Config->get("XAPI_$OSMServer");
                $URL =~ s/%p/${predicate}/g;                # substitute %p place holder with predicate
                $URL =~ s/%v/$Config->get('OSMVersion')/ge; # substitute %v place holder with API version
                push(@URLS, $URL);
            }
        }
        else {
            my $URL = $Config->get("API_$OSMServer");
            $URL =~ s/%v/$Config->get('OSMVersion')/ge; # substitute %v place holder with API version
            push(@URLS, $URL);
        }

        $filelist = [];
        my $i=0;
        foreach my $URL (@URLS) {
            ++$i;
            my $partialFile = File::Spec->join($self->{JobDir}, "data-$i.osm");
            ::statusMessage("Downloading: Map data for " . $req->layers_str, 0, 3);
            
            # download tile data in one piece *if* the tile is not too complex
            if ($req->complexity() < 20_000_000) {
                my $currentURL = $URL;
                $currentURL =~ s/%b/${bbox}/g;
                print "Downloading: $currentURL\n" if ($Config->get("Debug"));
                try {
                    $Server->downloadFile($currentURL, $partialFile, 0);
                    push(@{$filelist}, $partialFile);
                    $res = 1;
                }
                catch ServerError with { # just do nothing if there was an error during download
                    my $err = shift();
                    print "Download failed: " . $err->text() . "\n" if ($Config->get("Debug"));;
                };
            }

            if ((! $res) and ($Config->get("FallBackToSlices"))) {
                ::statusMessage("Trying smaller slices",1,0);
                my $slice = (($E1 - $W1) / 10); # A slice is one tenth of the width 
                for (my $j = 1; $j <= 10; $j++) {
                    my $bbox = sprintf("%f,%f,%f,%f", $W1 + ($slice * ($j - 1)), $S1, $W1 + ($slice * $j), $N1);
                    my $currentURL = $URL;
                    $currentURL =~ s/%b/${bbox}/g;    # substitute bounding box place holder
                    $partialFile = File::Spec->join($self->{JobDir}, "data-$i-$j.osm");
                    for (my $k = 1; $k <= 3; $k++) {  # try each slice 3 times
                        ::statusMessage("Downloading map data (slice $j of 10)", 0, 3);
                        print "Downloading: $currentURL\n" if ($Config->get("Debug"));
                        try {
                            $Server->downloadFile($currentURL, $partialFile, 0);
                            $res = 1;
                        }
                        catch ServerError with {
                            my $err = shift();
                            print "Download failed: " . $err->text() . "\n" if ($Config->get("Debug"));;
                            my $message = ($k < 3) ? "Download of slice $j failed, trying again" : "Download of slice $j failed 3 times, giving up";
                            ::statusMessage($message, 0, 3);
                        };
                        last if ($res); # don't try again if download was successful
                    }
                    last if (!$res); # don't download remaining slices if one fails
                    push(@{$filelist}, $partialFile);
                }
            }
            if (!$res) {
                ::statusMessage("Download of data from $OSMServer failed", 0, 3);
                last; # don't download other URLs if this one failed
            } 
        } # foreach @URLS

        last if ($res); # don't try another server if the download was successful
    } # foreach @OSMServers

    if ($res) {   # Download of data succeeded
        ::statusMessage("Download of data complete", 1, 10);
    }
    else {
        my $OSMServers = join(',', @OSMServers);
        throw TilesetError "Download of data failed from $OSMServers", "nodata ($OSMServers)";
    }

    ::mergeOsmFiles($DataFile, $filelist);

    # Get the API date time for the data so we can assign it to the generated image (for tracking from when a tile actually is)
    $self->{JobTime} = [stat $DataFile]->[9];
    
    # Check for correct UTF8 (else inkscape will run amok later)
    # FIXME: This doesn't seem to catch all string errors that inkscape trips over.
    ::statusMessage("Checking for UTF-8 errors",0,3);
    if (my $line = ::fileUTF8ErrCheck($DataFile))
    {
        ::statusMessage(sprintf("found incorrect UTF-8 chars in line %d. job (%d,%d,%d)",$line, $req->ZXY),1,0);
        throw TilesetError "UTF8 test failed", "utf8";
    }
    ::resetFault("utf8"); #reset to zero if no UTF8 errors found.
    return ($DataFile ,"");
}


#------------------------------------------------------
# Go through preprocessing steps for the current layer
# expects $self->{JobDir}/data.osm as input and produces
# $self->{JobDir}/dataList-of-preprocessors.osm
# parameter: (layername)
# returns:   filename (without path)
#-------------------------------------------------------------
sub runPreprocessors
{
    my $self = shift;
    my $layer= shift;
    my $req = $self->{req};
    my $Config = $self->{Config};

    my @ppchain = ();
    my $outputFile;

    # config option may be empty, or a comma separated list of preprocessors
    foreach my $preprocessor(split /,/, $Config->get($layer."_Preprocessor"))
    {
        my $inputFile = File::Spec->join($self->{JobDir},
                                         sprintf("data%s.osm", join("-", @ppchain)));
        push(@ppchain, $preprocessor);
        $outputFile = File::Spec->join($self->{JobDir},
                                          sprintf("data%s.osm", join("-", @ppchain)));

        if (-f $outputFile)
        {
            # no action; files for this preprocessing step seem to have been created 
                # by another layer already!
        }
        elsif ($preprocessor eq "maplint")
        {
            # Pre-process the data file using maplint
            my $Cmd = sprintf("%s \"%s\" tr %s %s > \"%s\"",
                    $Config->get("Niceness"),
                    $Config->get("XmlStarlet"),
                    "maplint/lib/run-tests.xsl",
                    "$inputFile",
                    "tmp.$$");
            ::statusMessage("Running maplint",0,3);
            ::runCommand($Cmd,$$);
            $Cmd = sprintf("%s \"%s\" tr %s %s > \"%s\"",
                        $Config->get("Niceness"),
                        $Config->get("XmlStarlet"),
                        "maplint/lib/convert-to-tags2.xsl",
                        "tmp.$$",
                        "$outputFile");
            ::statusMessage("Creating tags from maplint",0,3);
            ::runCommand($Cmd,$$);
            unlink("tmp.$$");
        }
        elsif ($preprocessor eq "close-areas")
        {
            my $Cmd = sprintf("%s perl close-areas.pl %d %d %d < %s > %s",
                        $Config->get("Niceness"),
                        $req->X,
                        $req->Y,
                        $req->Z,
                        "$inputFile",
                        "$outputFile");
            ::statusMessage("Running close-areas",0,3);
            ::runCommand($Cmd,$$);
        }
        elsif ($preprocessor eq "area-center")
        {
           if ($Config->get("JavaAvailable"))
           {
	       if ($Config->get("JavaVersion") >= 1.6)
               {
                   # use preprocessor only for XSLT for now. Using different algorithm for area center might provide inconsistent results"
                  # on tile boundaries. But XSLT is currently in minority and use different algorithm than orp anyway, so no difference.
                  my $Cmd = sprintf("%s java -cp %s com.bretth.osmosis.core.Osmosis -q -p org.tah.areaCenter.AreaCenterPlugin --read-xml %s --area-center --write-xml %s",
                               $Config->get("Niceness"),
                               join($Config->get("JavaSeparator"), "java/osmosis/osmosis.jar", "java/area-center.jar"),
                               $inputFile,
                               $outputFile);
                   ::statusMessage("Running area-center",0,3);
                   if (!::runCommand($Cmd,$$))
                   {
                       ::statusMessage("Area-center failed, ignoring",0,3);
                       copy($inputFile,$outputFile);
                   }
               } else 
               {
                   ::statusMessage("Java version at least 1.6 is required for area-center preprocessor",0,3);
                   copy($inputFile,$outputFile);
               }
           }
           else
           {
              copy($inputFile,$outputFile);
           }
        }
        elsif ($preprocessor eq "noop")
        {
            copy($inputFile,$outputFile);
        }
        else
        {
            throw TilesetError "Invalid preprocessing step '$preprocessor'", $preprocessor;
        }
    }

    # everything went fine. Get final filename and return it.
    my ($Volume, $path, $OSMfile) = File::Spec->splitpath($outputFile);
    return $OSMfile;
}

#-------------------------------------------------------------------
# renders the tiles, using threads
# paramter: ($layer, $layerDataFile)
#-------------------------------------------------------------------
sub forkedRender
{
    my $self = shift;
    my ($layer, $layerDataFile) = @_;
    my $req = $self->{req};
    my $Config = $self->{Config};
    my $minzoom = $req->Z;
    my $maxzoom = $Config->get($layer."_MaxZoom");

    my $numThreads = 2 * $Config->get("Fork");
    my @pids;

    for (my $thread = 0; $thread < $numThreads; $thread ++) 
    {
        # spawn $numThreads threads
        my $pid = fork();
        if (not defined $pid) 
        {   # exit if asked to fork but unable to
            throw TilesetError "GenerateTileset: could not fork, exiting", "fatal";
        }
        elsif ($pid == 0) 
        {   # we are the child process
            $self->{childThread}=1;
            for (my $zoom = ($minzoom + $thread) ; $zoom <= $maxzoom; $zoom += $numThreads) 
            {
                try {
                    $self->Render($layer, $zoom, $layerDataFile)
                }
                otherwise {
                    # an error occurred while rendering.
                    # Thread exits and returns (255+)0 here
                    exit(0);
                }
            }
            # Rendering went fine, have thread return (255+)1
            exit(1);
        } else
        {   # we are the parent thread, record child pid
            push(@pids, $pid);
        }
    }

    # now wait that all child render processes exited and check their return value
    # retvalue >> 8 is the real ret value. wait returns -1 if there are no child processes
    my $success = 1;
    foreach my $pid(@pids)
    {
        waitpid($pid,0);
        $success &= ($? >> 8);
    }

    ::statusMessage("exit forked renderer returning $success",0,6);
    if (not $success) {
        throw TilesetError "at least one render thread returned an error", "renderer";
    }
}


#-------------------------------------------------------------------
# renders the tiles, not using threads
# paramter: ($layer, $layerDataFile)
#-------------------------------------------------------------------
sub nonForkedRender
{
    my $self = shift;
    my ($layer, $layerDataFile) = @_;
    my $req = $self->{req};
    my $Config = $self->{Config};
    my $minzoom = $req->Z;
    my $maxzoom = $Config->get($layer."_MaxZoom");

    for (my $zoom = $req->Z ; $zoom <= $maxzoom; $zoom++) {
        $self->Render($layer, $zoom, $layerDataFile)
    }
}


#-------------------------------------------------------------------
# renders the tiles for one zoom level
# paramter: ($layer, $zoom, $layerDataFile)
#-------------------------------------------------------------------
sub Render
{
    my $self = shift;
    my ($layer, $zoom, $layerDataFile) = @_;
    my $Config = $self->{Config};
    my $req = $self->{req};

    $::progress = 0;
    $::progressPercent = 0;
    $::currentSubTask = "$layer-z$zoom";
    
    $self->GenerateSVG($layer, $zoom, $layerDataFile);

    $self->RenderSVG($layer, $zoom);

    $self->SplitTiles($layer, $zoom);
}


#-----------------------------------------------------------------------------
# Generate SVG for one zoom level
#   $layer - layer to be processed
#   $zoom - which zoom currently is processsed
#   $layerDataFile - name of the OSM data file (which is in the JobDir)
#-----------------------------------------------------------------------------
sub GenerateSVG 
{
    my $self = shift;
    my ($layer, $zoom, $layerDataFile) = @_;
    my $Config = TahConf->getConfig();
 
    # Render the file (returns 0 on failure)
    if (! ::xml2svg(
            File::Spec->join($self->{JobDir}, $layerDataFile),
            $self->{bbox},
            $Config->get($layer . "_Rules." . $zoom),
            File::Spec->join($self->{JobDir}, "$layer-z$zoom.svg"),
            $zoom))
    {
        throw TilesetError "Render failure", "renderer";
    }
}


#-----------------------------------------------------------------------------
# Render SVG for one zoom level
#   $layer - layer to be processed
#   $zoom
#-----------------------------------------------------------------------------
sub RenderSVG
{
    my $self = shift;
    my ($layer, $zoom) = @_;
    my $Config = $self->{Config};
    my $req = $self->{req};

    # File locations
    my $svgFile = File::Spec->join($self->{JobDir},"$layer-z$zoom.svg");
    my $pngFile = File::Spec->join($self->{JobDir},"$layer-z$zoom.png");
    my $stdOut = File::Spec->join($self->{JobDir},"$layer-z$zoom.stdout");
    
    my $Cmd = "";

    my $TileSize = 256; # Tiles are 256 pixels square
    # PngSize is the width/height dimension of resulting PNG file
    my $PngSize = $TileSize * (2 ** ($zoom - $req->Z));

    # SVG excerpt in SVG units
    my ($Height, $Width, $Valid) = ::getSize(File::Spec->join($self->{JobDir}, "$layer-z$zoom.svg"));

    my ($Left, $Top) = (0, 0);
    if ($Config->get("Batik") == "1") { # batik as jar
        $Cmd = sprintf("%s%s java -Xms256M -Xmx%s -jar %s -w %d -h %d -a %f,%f,%f,%f -m image/png -d \"%s\" \"%s\" > %s", 
                       $Config->get("i18n") ? "LC_ALL=C " : "",
                       $Config->get("Niceness"),
                       $Config->get("BatikJVMSize"),
                       $Config->get("BatikPath"),
                       $PngSize,
                       $PngSize,
                       $Left, $Top, $Width, $Height,
                       $pngFile,
                       $svgFile,
                       $stdOut);
    }
    elsif ($Config->get("Batik") == "2") { # batik as executable (wrapper of some sort, i.e. on gentoo)
        $Cmd = sprintf("%s%s \"%s\" -w %d -h %d -a %f,%f,%f,%f -m image/png -d \"%s\" \"%s\" > %s",
                       $Config->get("i18n") ? "LC_ALL=C " : "",
                       $Config->get("Niceness"),
                       $Config->get("BatikPath"),
                       $PngSize,
                       $PngSize,
                       $Left,$Top,$Width,$Height,
                       $pngFile,
                       $svgFile,
                       $stdOut);
    }
    elsif ($Config->get("Batik") == "3") { # agent
        $Cmd = sprintf("svg2png\nwidth=%d\nheight=%d\narea=%f,%f,%f,%f\ndestination=%s\nsource=%s\nlog=%s\n\n", 
                       $PngSize,
                       $PngSize,
                       $Left,$Top,$Width,$Height,
                       $pngFile,
                       $svgFile,
                       $stdOut);
    }
    else {
        my $old_locale = ::setlocale(LC_NUMERIC);
        ::setlocale(LC_NUMERIC, "");

        $Cmd = sprintf("%s%s \"%s\" -z -w %d -h %d --export-area=%f:%f:%f:%f --export-png=\"%s\" \"%s\" > %s", 
                       $Config->get("i18n") ? "LC_ALL=C " : "",
                       $Config->get("Niceness"),
                       $Config->get("Inkscape"),
                       $PngSize,
                       $PngSize,
                       $Left, $Top, $Left + $Width, $Top + $Height,
                       $pngFile,
                       $svgFile,
                       $stdOut);

        ::setlocale(LC_NUMERIC, $old_locale);
    }
    
    # stop rendering the current job when inkscape fails
    ::statusMessage("Rendering",0,3);
    print STDERR "\n$Cmd\n" if ($Config->get("Debug"));

    my $commandResult = $Config->get("Batik") == "3" ? ::sendCommandToBatik($Cmd) eq "OK" : ::runCommand($Cmd, $$);
    if (!$commandResult or ! -e $pngFile ) {
        ::statusMessage("$Cmd failed",1,0);
        if ($Config->get("Batik") == "3" && !::getBatikStatus()) {
            ::statusMessage("Batik agent is not running, use $0 startBatik to start batik agent\n",1,0);
        }
        my $reason = "BadSVG (svg2png)";
        $req->is_unrenderable(1);
        throw TilesetError $reason;
    }
}


#-----------------------------------------------------------------------------
# Split PNG for one zoom level into tiles
#   $layer - layer to be processed
#   $zoom
#-----------------------------------------------------------------------------
sub SplitTiles
{
    my $self = shift;
    my ($layer, $zoom) = @_;
    my $Config = $self->{Config};
    my $req = $self->{req};

    my $pngFile = File::Spec->join($self->{JobDir},"$layer-z$zoom.png");
    my $minzoom = $req->Z;
    my $size = 2 ** ($zoom - $minzoom);
    my $minx = $req->X * $size;
    my $miny = $req->Y * $size;
    my $number_tiles = $size * $size;

    $::progress = 0;
    $::progressPercent = 0;

    ::statusMessage("Splitting",0,3);

    # Size of tiles
    my $pixels = 256;

    my $Image = newFromPng GD::Image($pngFile);
    my $ctime = time;
    # unless this is a transparent layer convert the image to 8 bit palette
#    $Image->trueColorToPalette(0, 256) unless ($Config->get($layer."_Transparent"));
    if( not defined $Image ) {
        throw TilesetError "SplitTiles: Missing File $pngFile encountered";
    }

    # Use one subimage for everything, and keep copying data into it
    my $SubImage = new GD::Image($pixels, $pixels, 1);#$Config->get($layer."_Transparent") ? 1 : 0);
    my $i = 0;
    my ($x, $y);
    for (my $iy = 0; $iy <= $size - 1; $iy++) {
        for (my $ix = 0; $ix <= $size - 1; $ix++) {
            $x = $minx + $ix;
            $y = $miny + $iy;
            $i++;
            $::progress = $i;
            $::progressPercent = $i / $number_tiles * 100;
            ::statusMessage("Writing tile $x $y", 0, 10); 
            # Get a tiles'worth of data from the main image
            $SubImage->copy($Image,
                            0,                   # Dest X offset
                            0,                   # Dest Y offset
                            $ix * $pixels,       # Source X offset
                            $iy * $pixels,       # Source Y offset
                            $pixels,             # Copy width
                            $pixels);            # Copy height

            # Decide what the tile should be called
            my $tileFile;
            if ($Config->get("LocalSlippymap")) {
                my $tileDir = File::Spec->join($Config->get("LocalSlippymap"), $Config->get("${layer}_Prefix"), $zoom, $x);
                File::Path::mkpath($tileDir);
                $tileFile = File::Spec->join($tileDir, sprintf("%d.png", $y));
            }
            else {
                # Construct base png directory
                my $tileDir = File::Spec->join($self->{JobDir}, sprintf("%s_%d_%d_%d.dir", $Config->get("${layer}_Prefix"), $req->ZXY));
                File::Path::mkpath($tileDir);
                $tileFile = File::Spec->join($tileDir, sprintf("%s_%d_%d_%d.png", $Config->get("${layer}_Prefix"), $zoom, $x, $y));
            }

            # libGD comparison returns true if images are different. (i.e. non-empty Land tile)
            # so return the opposite (false) if the tile doesn't look like an empty land tile

            # Check for black tile output
            if (not ($SubImage->compare($self->{BlackTileImage}) & GD_CMP_IMAGE)) {
                throw TilesetError "SplitTiles: Black Tile encountered", "inkscape";
            }

            # Detect empty tile here:
            if (not ($SubImage->compare($self->{EmptyLandImage}) & GD_CMP_IMAGE)) { 
                copy("emptyland.png", $tileFile);
            }
            # same for Sea tiles
            elsif (not($SubImage->compare($self->{EmptySeaImage}) & GD_CMP_IMAGE)) {
                copy("emptysea.png", $tileFile);
            }
            else {
                if ($Config->get($layer."_Transparent")) {
                    $SubImage->transparent($SubImage->colorAllocate(248, 248, 248));
                }
                else {
                    $SubImage->transparent(-1);
                }
                # Get the image as PNG data
                $SubImage->trueColorToPalette(0, 256) unless ($Config->get($layer."_Transparent"));
                my $png_data = $SubImage->png;

                # Store it
                open (my $fp, ">$tileFile") || throw TilesetError "SplitTiles: Could not open $tileFile for writing", "fatal";
                binmode $fp;
                print $fp $png_data;
                close $fp;
            }
        }
    }
}


#-----------------------------------------------------------------------------
# Render a tile
#   $Ytile, $Zoom - which tilestripe
#   $Zoom - the cuurent zoom level that we render
#   $N, $S, $W, $E - bounds of the tile
#   $ImgX1,$ImgY1,$ImgX2,$ImgY2 - location of the tile in the SVG file
#   $ImageHeight - Height of the entire SVG in SVG units
#   returns: allEmpty
#-----------------------------------------------------------------------------
sub RenderTile 
{
    my $self = shift;
    my ($layer, $Ytile, $Zoom, $N, $S, $W, $E, $ImgX1,$ImgY1,$ImgX2,$ImgY2,$ImageHeight) = @_;
    my $Config = TahConf->getConfig();
    my $maxzoom = $Config->get($layer."_MaxZoom");
    my $req = $self->{req};
    my $forkval = $Config->get("Fork");

    return 1 if($Zoom > $maxzoom);
    
    # Render it to PNG
    printf "Tilestripe %s (%s,%s): Lat %1.3f,%1.3f, Long %1.3f,%1.3f, X %1.1f,%1.1f, Y %1.1f,%1.1f\n", 
            $Ytile,$req->X,$req->Y,$N,$S,$W,$E,$ImgX1,$ImgX2,$ImgY1,$ImgY2 if ($Config->get("Debug")); 

    my ($FullBigPNGFileName, $reason) = 
          ::svg2png($self->{JobDir}, $req, $Ytile, $Zoom,$ImgX1,$ImgY1,$ImgX2,$ImgY2,$ImageHeight);

    if (!$FullBigPNGFileName)
    {  # svg2png failed
        throw TilesetError $reason, "renderer";
    }

    # splitImageX returns true if all tiles extracted were empty.
    # this might break if a higher zoom tile would contain data that is 
    # not rendered at the current zoom level. 

    (my $success,my $empty, $reason) = 
           ::splitImageXY($layer, $req, $Zoom, $Ytile, $FullBigPNGFileName);
    if (!$success)
    {  # splitimage failed
        throw TilesetError $reason, "renderer";
    }

    # If splitimage is empty Should we skip going further up the zoom level?
    if ($empty and !$Config->get($layer."_RenderFullTileset") and !$Config->get("CreateTilesetFile")) 
    {
        # leap forward because in progresscounting as this tile and 
        # all higher zoom tiles of it are "done" (empty).
        for (my $j = $maxzoom; $j >= $Zoom ; $j--)
        {
            $::progress += 2 ** $maxzoom-$j;
        }
	return 1;
    }

    # increase progress of tiles
    $::progress += 1;
    $::progressPercent = int( 100 * $::progress / (2**($maxzoom-$req->Z+1)-1) );
    # if forking, each thread does only 1/nth of tiles so multiply by numThreads
    ($::progressPercent *= 2*$forkval) if $forkval;

    if ($::progressPercent == 100)
    {
        ::statusMessage("Finished ".$req->X.",".$req->Y." for layer $layer",1,0);
    }
    (printf STDERR "Job No. %d %1.1f %% done.\n",$::progressJobs, $::progressPercent)
                    if ($Config->get("Verbose") >= 10);
    
    # Sub-tiles
    my $MercY2 = ProjectF($N); # get mercator coordinates for North border of tile
    my $MercY1 = ProjectF($S); # get mercator coordinates for South border of tile
    my $MercYC = 0.5 * ($MercY1 + $MercY2); # get center of tile in mercator
    my $LatC = ProjectMercToLat($MercYC); # reproject centerline to latlon

    my $ImgYCP = ($MercYC - $MercY1) / ($MercY2 - $MercY1); 
    my $ImgYC = $ImgY1 + ($ImgY2 - $ImgY1) * $ImgYCP;       # find mercator coordinates for bottom/top of subtiles

    my $YA = $Ytile * 2;
    my $YB = $YA + 1;

    if ($Config->get("CutFullTile"))
    {
        $ImgYC = $ImgY1;
        my $empty = $self->RenderTile($layer, $YA, $Zoom+1, $N, $S, $W, $E, $ImgX1, $ImgY1, $ImgX2, $ImgY2,$ImageHeight);
        return $empty;
    }
    else
    {
        # we create Fork*2 inkscape threads
        if ($forkval && $Zoom < ($req->Z + $forkval))
        {
            my $pid = fork();
            if (not defined $pid) 
            {
                throw TilesetError "RenderTile: could not fork, exiting", "fatal"; # exit if asked to fork but unable to
            }
            elsif ($pid == 0) 
            {
                # we are the child process
                $self->{childThread}=1;
                try {
                    my $empty = $self->RenderTile($layer, $YA, $Zoom+1, $N, $LatC, $W, $E, $ImgX1, $ImgYC, $ImgX2, $ImgY2,$ImageHeight);
                }
                otherwise {
                    exit 0;
                }
                # we can't talk to our parent other than through exit codes.
                exit 1;
            }
            else
            {
                $self->RenderTile($layer, $YB, $Zoom+1, $LatC, $S, $W, $E, $ImgX1, $ImgY1, $ImgX2, $ImgYC,$ImageHeight);
                waitpid($pid,0);
                my $ChildExitValue = ($? >> 8);
                if (!$ChildExitValue)
                {
                    throw TilesetError "Forked inkscape failed", "renderer";
                }
            }
        }
        else
        {
            my $empty = $self->RenderTile($layer, $YA, $Zoom+1, $N, $LatC, $W, $E, $ImgX1, $ImgYC, $ImgX2, $ImgY2,$ImageHeight);
            $empty = $self->RenderTile($layer, $YB, $Zoom+1, $LatC, $S, $W, $E, $ImgX1, $ImgY1, $ImgX2, $ImgYC,$ImageHeight);
            return $empty;
        }
        return 0;
    }
    return 0;
}


#------------------------------------------------------------------
# remove temporary files etc
#-------------------------------------------------------------------
sub cleanup
{
    my $self = shift;
    my $Config = $self->{Config};

    # remove temporary job directory if 'Debug' is not set
    print STDERR "removing job dir",$self->{JobDir},"\n\n" if $Config->get('Debug');
    rmtree $self->{JobDir} unless $Config->get('Debug');
}

#----------------------------------------------------------------------------------------
# bbox->new(N,E,S,W)
package bbox;
sub new
{
    my $class = shift;
    my $self={};
    ($self->{N},$self->{E},$self->{S},$self->{W}) = @_;
    bless $self, $class;
    return $self;
}

sub N { my $self = shift; return $self->{N};}
sub E { my $self = shift; return $self->{E};}
sub S { my $self = shift; return $self->{S};}
sub W { my $self = shift; return $self->{W};}

sub extents
{
    my $self = shift;
    return ($self->{N}, $self->{E}, $self->{S}, $self->{W});
}

sub center
{
    my $self = shift;
    return (($self->{N} + $self->{S}) / 2, ($self->{E} + $self->{W}) / 2);
}

#----------------------------------------------------------------------------------------
# error class for Tileset

package TilesetError;
use base 'Error::Simple';

1;
