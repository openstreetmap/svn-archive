package Tileset;

# Copyright 2006-20088, Dirk-Lueder Kreie, Sebastian Spaeth,
# Matthias Julius and others
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.

use strict;
use File::Temp qw/ tempfile tempdir /;
use lib::TahConf;
use tahlib;
use tahproject;
use File::Copy;
use File::Path;

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

    bless $self, $class;
    return $self;
}

#-----------------------------------------------------------------------------
# Tileset destructor. Call cleanup in case we did not clean up properly earlier.
#-----------------------------------------------------------------------------
sub DESTROY
{
    my $self = shift;
    if ($self->{childThread}) 
    {   # For whatever unknown reasons this function gets called for exiting child threads.
        # It really shouldn't but oh well. So protect us and only cleanup if we are the parent.
        ;
    } 
    else
    {
        # only cleanup if we are the parent thread
        $self->cleanup();
    }
}

#-----------------------------------------------------------------------------
# generate does everything that is needed to end up with a finished tileset
# that just needs compressing and uploading. It outputs status messages, and
# hands back the job to the server in case of critical errors.
# Returns (status, reason)
# status: 1=success, 0= failure
# reason: a string describing the error
#-----------------------------------------------------------------------------
sub generate
{
    my $self = shift;
    my $req =  $self->{req};
    my $Config = $self->{Config};
    
    ::keepLog($$,"GenerateTileset","start","x=".$req->X.',y='.$req->Y.',z='.$req->Z." for layers ".$req->layers_str);
    
    my ($N, $S) = Project($req->Y, $req->Z);
    my ($W, $E) = ProjectL($req->X, $req->Z);
    $self->{bbox}= bbox->new($N,$E,$S,$W);

    $::progress = 0;
    $::progressPercent = 0;
    $::progressJobs++;
    $::currentSubTask = "jobinit";
    
    ::statusMessage(sprintf("Doing tileset (%d,%d,%d) (area around %f,%f)", $req->ZXY, ($N+$S)/2, ($W+$E)/2),1,0);
    
    my $maxCoords = (2 ** $req->Z - 1);
    
    if ( ($req->X < 0) or ($req->X > $maxCoords) 
      or ($req->Y < 0) or ($req->Y > $maxCoords) )
    {
        my $reason = "Coordinates out of bounds (0..$maxCoords)";
        ::statusMessage($reason, 1, 0);
        return (0, $reason);
    }

    #------------------------------------------------------
    # Download data (returns full path to data.osm or 0)
    #------------------------------------------------------

    my ($FullDataFile, $reason) = $self->downloadData();
    if (!$FullDataFile)
    {
        ::statusMessage($reason, 1, 0);
        return (0, $reason);
    }

    #------------------------------------------------------
    # Handle all layers, one after the other
    #------------------------------------------------------

    foreach my $layer($req->layers)
    {
        #reset progress for each layer
        $::progress=0;
        $::progressPercent=0;
        $::currentSubTask = $layer;
        
        # JobDirectory is the directory where all final .png files are stored.
        # It is not used for temporary files.
        my $JobDirectory = File::Spec->join($self->{JobDir},
                                sprintf("%s_%d_%d_%d.dir",
                                $Config->get($layer."_Prefix"),
                                $req->ZXY));
        mkdir $JobDirectory;

        my $maxzoom = $Config->get($layer."_MaxZoom");
        my $layerDataFile;

        #------------------------------------------------------
        # Go through preprocessing steps for the current layer
        # This puts preprocessed files like data-maplint-closeareas.osm in $self->{JobDir}
        # and returns the file name of the resulting data file.
        #------------------------------------------------------

        ($layerDataFile, $reason) = $self->runPreprocessors($layer);
        if (!$layerDataFile)
        {
            ::statusMessage($reason, 1, 0);
            return (0, $reason);
        }

        #------------------------------------------------------
        # Preprocessing finished, start rendering to SVG
        # $layerDataFile is just the filename
        #------------------------------------------------------

        if ($Config->get("Fork")) 
        {   # Forking to render zoom levels in parallel
            if (!$self->forkedRender($layer, $maxzoom, $layerDataFile))
            {
                 my $reason = "Forked render failure";
                 ::addFault("renderer",1);
                 ::statusMessage($reason, 1, 0);
                 return (0, $reason);
	    }
        }
        else
        {   # Non-forking render
            for (my $zoom = $req->Z ; $zoom <= $maxzoom; $zoom++)
            {
                if (! $self->GenerateSVG($layerDataFile, $layer, $zoom))
                {
                    my $reason = "Render failure";
                    ::addFault("renderer",1);
                    ::statusMessage($reason, 1, 0);
                    return (0, $reason);
                }
            }
        }

        #------------------------------------------------------
        # Convert from SVG to PNG.
        #------------------------------------------------------
        
        # Find the size of the SVG file
        my ($ImgH,$ImgW,$Valid) = ::getSize(File::Spec->join($self->{JobDir},
                                                       "output-z$maxzoom.svg"));

        # Render it as loads of recursive tiles
        my ($success, $emptyOrErrorReason) = $self->RenderTile($layer, $req->Y, $req->Z, $N, $S, $W, $E, 0,0,$ImgW,$ImgH,$ImgH,0);

        #----------
        if (!$success)
        {   # Failed to render tiles, $empty contains error reason
            ::addFault("renderer",1);
            ::statusMessage($emptyOrErrorReason, 1, 0);
            return (0, $emptyOrErrorReason);
        }
        else
        {   # successfully rendered, so reset renderer faults
            ::resetFault("renderer");
        }

        #----------
        # This directory is now ready for upload.
        # move it up one folder, so it can be picked up
        my $dircomp;

        my @dirs = File::Spec->splitdir($JobDirectory);
        do { $dircomp = pop(@dirs); } until ($dircomp ne '');
        # we have now split off the last nonempty directory path
        # remove the next path component and add the dir name back.
        pop(@dirs);
        push(@dirs, $dircomp);
        my $DestDir = File::Spec->catdir(@dirs);
        rename $JobDirectory, $DestDir;
        # Finished moving directory one level up.
    }

    ::keepLog($$,"GenerateTileset","stop",'x='.$req->X.',y='.$req->Y.',z='.$req->Z." for layers ".$req->layers_str);

    # Cleaning up of tmpdirs etc. are called in the destructor DESTROY
    return (1, "");
}

#------------------------------------------------------------------
# Download the area for the tileset (whole or in stripes, as required)
# into $self->{JobDir}
# returns: (filename, reason)
# filename: resulting data osm filename (without path) on success, 'undef' on failure
# reason:   string describing the error
#-------------------------------------------------------------------
sub downloadData
{
    my $self = shift;
    my $req = $self->{req};
    my $Config = $self->{Config};

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
    
    my $URLS = sprintf("%s%s/map?bbox=%s",
      $Config->get("APIURL"),$Config->get("OSMVersion"),$bbox);
    if ($req->Z < 12) 
    {
        # FIXME: zoom 12 hardcoded: assume lowzoom caption layer now!
        # only in xy mode since in loop mode a different method that does not depend on hardcoded zoomlevel will be used, where the layer is set by the server.
        if ($::Mode eq "xy") 
        {
            $req->layers("caption");
            ::statusMessage("Warning: lowzoom zoom detected, autoswitching to ".$req->layers_str." layer",1,0);
        }
        else
        {
            ::statusMessage("Warning: lowzoom zoom detected, but ".$req->layers_str." configured",1,0);
        }
        # Get the predicates for lowzoom caption layer, and build the URLS for them
        my $predicates = $Config->get($req->layers_str."_Predicates");
        # strip spaces in predicates because that is the separator used below
        $predicates =~ s/\s+//g;
        $URLS="";
        foreach my $predicate (split(/,/,$predicates)) {
            $URLS = $URLS . sprintf("%s%s/%s[bbox=%s] ",
                $Config->get("XAPIURL"),$Config->get("OSMVersion"),$predicate,$bbox);
        }
    }
    my $filelist = [];
    my $i=0;
    foreach my $URL (split(/ /,$URLS)) 
    {
        ++$i;
        my $partialFile = File::Spec->join($self->{JobDir},"data-$i.osm");
        push(@{$filelist}, $partialFile);
        ::statusMessage("Downloading: Map data for ".$req->layers_str,0,3);
        print "Download\n$URL\n" if ($Config->get("Debug"));
        
        my $res = undef;
        # download tile data in one piece *if* the tile is not too complex
        if ($req->Z >= 12 && $req->{complexity} < 20000000)
           {$res = ::DownloadFile($URL, $partialFile, 0)};

        if (! $res)
        {   # Download of data failed
            if ($req->Z < 12)
            {
                # Fetching of lowzoom data from OSMXAPI failed
                ::addFault("nodataXAPI",1);
                return (undef, "No data here! (OSMXAPI)")
            }
            elsif ($Config->get("FallBackToXAPI"))
            {
                # fetching of regular tileset data failed. Try OSMXAPI fallback
                ::statusMessage("Trying OSMXAPI",1,0);
                $bbox = $URL;
                $bbox =~ s/.*bbox=//;
                $URL=sprintf("%s%s/%s[bbox=%s] ",
                    $Config->get("XAPIURL"),
                    $Config->get("OSMVersion"),
                    "*",
                    $bbox);
                ::statusMessage("Downloading: Map data for ".$req->layers_str." to $partialFile",0,3);
                print "Download\n$URL\n" if ($Config->get("Debug"));
                my $res = ::DownloadFile($URL, $partialFile, 0);
                if (! $res)
                {   # OSMXAPI fallback failed too
                    my $reason = "no data here! (OSMXAPI)";
                    ::addFault("nodataXAPI",1);
                    return (undef, $reason);
                }
                else
                {   # OSMXAPI fallback succeeded
                    ::resetFault("nodataXAPI"); #reset to zero if data downloaded
                }
            }
            else
            {
                ::statusMessage("Trying smaller slices",1,0);
                my $slice=(($E1-$W1)/10); # A chunk is one tenth of the width 
                for (my $j = 1 ; $j<=10 ; $j++)
                {
                    $URL = sprintf("%s%s/map?bbox=%f,%f,%f,%f", 
                      $Config->get("APIURL"),$Config->get("OSMVersion"), ($W1+($slice*($j-1))), $S1, ($W1+($slice*$j)), $N1); 
                    $partialFile = File::Spec->join($self->{JobDir},"data-$i-$j.osm");
                    push(@{$filelist}, $partialFile);
                    ::statusMessage("Downloading: Map data to $partialFile (slice $j of 10)",0,3);
                    print "Download\n$URL\n" if ($Config->get("Debug"));
                    $res = ::DownloadFile($URL, $partialFile, 0);

                    if (! $res)
                    {   # Sliced download failed too
                        my $reason = "No data here (sliced)";
                        ::addFault("nodata",1);
                        return (undef, $reason);
                    }
                    else
                    {   # Sliced download succeeded (at least one slice)
                        ::resetFault("nodata");
                    }
                }
            }
        }
        else
        {   # Download of data succeeded in the first place

            if ($req->Z < 12) ## FIXME: hardcoded zoom
            {
                ::resetFault("nodataXAPI"); #reset to zero if data downloaded
            }
            else 
            {
                ::resetFault("nodata"); #reset to zero if data downloaded
            }
        }
    }

    ::mergeOsmFiles($DataFile, $filelist);

    # Get the API date time for the data so we can assign it to the generated image (for tracking from when a tile actually is)
    $self->{JobTime} = [stat $DataFile]->[9];
    
    # Check for correct UTF8 (else inkscape will run amok later)
    # FIXME: This doesn't seem to catch all string errors that inkscape trips over.
    ::statusMessage("Checking for UTF-8 errors in $DataFile",0,3);
    if (::fileUTF8ErrCheck($DataFile))
    {
        ::statusMessage(sprintf("found incorrect UTF-8 chars in %s, job (%d,%d,%d)",$DataFile, $req->ZXY),1,0);
        my $reason= ("UTF8 test failed");
        ::addFault("utf8",1);
        return (undef, $reason);
    }
    ::resetFault("utf8"); #reset to zero if no UTF8 errors found.
    return ($DataFile ,"");
}


#------------------------------------------------------
# Go through preprocessing steps for the current layer
# expects $self->{JobDir}/data.osm as input and produces
# $self->{JobDir}/dataList-of-preprocessors.osm
# parameter: (layername)
# returns:   (filename, reason)
#            filename is 0 in case of failure or filename (without path)
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
        elsif ($preprocessor eq "noop")
        {
            copy($inputFile,$outputFile);
        }
        else
        {
            return (0, "Invalid preprocessing step '$preprocessor'");
        }
    }

    # everything went fine. Get final filename and return it.
    my ($Volume, $path, $OSMfile) = File::Spec->splitpath($outputFile);
    return ($OSMfile, "");
}

#-------------------------------------------------------------------
# renders the tiles, using threads
# paramter: ($layer, $maxzoom)
# returns: 1 on success, 0 on failure
#-------------------------------------------------------------------
sub forkedRender
{
    my $self = shift;
    my ($layer, $maxzoom, $layerDataFile) = @_;
    my $req = $self->{req};
    my $Config = $self->{Config};
    my $minimum_zoom = $req->Z;

    my $numThreads = 2 * $Config->get("Fork");
    my @pids;
    my $success = 1;

    for (my $thread = 0; $thread < $numThreads; $thread ++) 
    {
        # spawn $numThreads threads
        my $pid = fork();
        if (not defined $pid) 
        {   # exit if asked to fork but unable to
            my $reason = "GenerateTileset: could not fork, exiting";
            ::statusMessage($reason, 1, 0);
            return 0;
        }
        elsif ($pid == 0) 
        {   # we are the child process
            $self->{childThread}=1;
            for (my $zoom = ($minimum_zoom + $thread) ; $zoom <= $maxzoom; $zoom += $numThreads) 
            {
		::statusMessage("Thread $thread renders zoom $zoom now",0,6);
                if (! $self->GenerateSVG($layerDataFile, $layer, $zoom))
                {    # an error occurred while rendering. Thread exits and returns (255+)0 here
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
    foreach my $pid(@pids)
    {
        waitpid($pid,0);
        $success &= ($? >> 8);
        ::statusMessage("thread $pid returned with value $?, leaving success at $success",1,6);
    }

    ::statusMessage("exit forked renderer returning $success",1,6);
    return $success;
}

#-----------------------------------------------------------------------------
# Generate SVG for one zoom level
#   $layerDataFile - name of the OSM data file (which is in the JobDir)
#   $Zoom - which zoom currently is processsed
#  returns: 1 on success, 0 on failure
#-----------------------------------------------------------------------------
sub GenerateSVG 
{
    my $self = shift;
    my ($layerDataFile, $layer, $Zoom) = @_;
    my $Config = TahConf->getConfig();

    # Create a new copy of rules file to allow background update
    # don't need layer in name of file as we'll
    # process one layer after the other
    my $success = 1;
    my $source = $Config->get($layer."_Rules.".$Zoom);
    my $TempFeatures = File::Spec->join($self->{JobDir}, "map-features-z$Zoom.xml");
    copy($source, $TempFeatures)
        or die "Cannot make copy of $source";

    # Update the rules file  with details of what to do (where to get data, what bounds to use)
    ::AddBounds($TempFeatures, $self->{bbox}->W,$self->{bbox}->S,$self->{bbox}->E,$self->{bbox}->N);
    ::SetDataSource($layerDataFile, $TempFeatures);

    # Render the file (returns 0 on failure)
    if (! ::xml2svg(
            $TempFeatures,
            File::Spec->join($self->{JobDir}, "output-z$Zoom.svg"),
            $Zoom))
    {
        $success = 0;
    }

    return $success;
}



#-----------------------------------------------------------------------------
# Render a tile
#   $Ytile, $Zoom - which tilestripe
#   $ZOrig, the lowest zoom level which called tileset generation (i.e. z12 for "normal" operation)
#   $N, $S, $W, $E - bounds of the tile
#   $ImgX1,$ImgY1,$ImgX2,$ImgY2 - location of the tile in the SVG file
#   $ImageHeight - Height of the entire SVG in SVG units
#   $empty - put forward "empty" tilestripe information.
#   returns: (success, allEmpty) (on failure allEmpty is a string describing the failure)
#-----------------------------------------------------------------------------
sub RenderTile 
{
    my $self = shift;
    my ($layer, $Ytile, $Zoom, $N, $S, $W, $E, $ImgX1,$ImgY1,$ImgX2,$ImgY2,$ImageHeight,$SkipEmpty) = @_;
    my $Config = TahConf->getConfig();
    my $req = $self->{req};

    return (1,1) if($Zoom > $Config->get($layer."_MaxZoom"));
    
    # no need to render subtiles if empty
    return (1,$SkipEmpty) if($SkipEmpty == 1);

    # Render it to PNG
    printf "Tilestripe %s (%s,%s): Lat %1.3f,%1.3f, Long %1.3f,%1.3f, X %1.1f,%1.1f, Y %1.1f,%1.1f\n",       $Ytile,$req->X,$req->Y,$N,$S,$W,$E,$ImgX1,$ImgX2,$ImgY1,$ImgY2 if ($Config->get("Debug")); 
    my $Width = 256 * (2 ** ($Zoom - $req->Z));  # Pixel size of tiles  
    my $Height = 256; # Pixel height of tile

    # svg2png returns true if all tiles extracted were empty. this might break 
    # if a higher zoom tile would contain data that is not rendered at the 
    # current zoom level. 
    my ($success,$empty) = ::svg2png($self->{JobDir},$layer, $req, $Ytile, $Zoom, $Width, $Height,$ImgX1,$ImgY1,$ImgX2,$ImgY2,$ImageHeight);
    if (!$success)
    {  # svg2png failed, so empty contains a string with the error reason
       return (0, $empty);
    }
    if ($empty and !$Config->get($layer."_RenderFullTileset")) 
    {
        $SkipEmpty=1;
    }

    # Get progress percentage 
    if($SkipEmpty == 1) 
    {
        # leap forward because this tile and all higher zoom tiles of it are "done" (empty).
        for (my $j = $Config->get($layer."_MaxZoom"); $j >= $Zoom ; $j--)
        {
            $::progress += 2 ** ($Config->get($layer."_MaxZoom")-$j);
        }
    }
    else
    {
        $::progress += 1;
    }

    if (($::progressPercent=$::progress*100/(2**($Config->get($layer."_MaxZoom")-$req->Z+1)-1)) == 100)
    {
        ::statusMessage("Finished ".$req->X.",".$req->Y." for layer $layer",1,0);
    }
    else
    {
        if ($Config->get("Verbose") >= 10)
        {
            printf STDERR "Job No. %d %1.1f %% done.\n",$::progressJobs, $::progressPercent;
        }
        else
        {
            ::statusMessage("Working",0,3);
        }
    }
    
    # Sub-tiles
    my $MercY2 = ProjectF($N); # get mercator coordinates for North border of tile
    my $MercY1 = ProjectF($S); # get mercator coordinates for South border of tile
    my $MercYC = 0.5 * ($MercY1 + $MercY2); # get center of tile in mercator
    my $LatC = ProjectMercToLat($MercYC); # reproject centerline to latlon

    my $ImgYCP = ($MercYC - $MercY1) / ($MercY2 - $MercY1); 
    my $ImgYC = $ImgY1 + ($ImgY2 - $ImgY1) * $ImgYCP;       # find mercator coordinates for bottom/top of subtiles

    my $YA = $Ytile * 2;
    my $YB = $YA + 1;

    # temporarily disable forking in inkscape until we get fork to work right.
    if (0 && ($Config->get("Fork") && $Zoom >= $req->Z && $Zoom < ($req->Z + $Config->get("Fork"))))
    {
        my $pid = fork();
        if (not defined $pid) 
        {
            ::cleanUpAndDie("RenderTile: could not fork, exiting","EXIT",4); # exit if asked to fork but unable to
        }
        elsif ($pid == 0) 
        {
            # we are the child process and can't talk to our parent other than through exit codes
            ($success,$empty) = $self->RenderTile($layer, $YA, $Zoom+1, $N, $LatC, $W, $E, $ImgX1, $ImgYC, $ImgX2, $ImgY2,$ImageHeight,$SkipEmpty);
            if ($success)
            {
                exit(0);
            }
            else
            {
                exit(1);
            }
        }
        else
        {
            ($success,$empty) = $self->RenderTile($layer, $YB, $Zoom+1, $LatC, $S, $W, $E, $ImgX1, $ImgY1, $ImgX2, $ImgYC,$ImageHeight,$SkipEmpty);
            waitpid($pid,0);
            my $ChildExitValue = $?; # we don't want the details, only if it exited normally or not.
            if ($ChildExitValue or !$success)
            {
                return (0, "Forked inkscape failed");
            }
        }
        if ($Zoom == $req->Z)
        {
            $::progressPercent=100 if (! $Config->get("Debug")); # workaround for not correctly updating %age in fork, disable in debug mode
            ::statusMessage("Finished ".$req->X.",".$req->Y." for layer $layer",1,0);
        }
    }
    else
    {
        my ($success,$emptyOrReason) = $self->RenderTile($layer, $YA, $Zoom+1, $N, $LatC, $W, $E, $ImgX1, $ImgYC, $ImgX2, $ImgY2,$ImageHeight,$SkipEmpty);
        return (0, $emptyOrReason) if (!$success);
        ($success,$emptyOrReason) = $self->RenderTile($layer, $YB, $Zoom+1, $LatC, $S, $W, $E, $ImgX1, $ImgY1, $ImgX2, $ImgYC,$ImageHeight,$SkipEmpty);
        return (0, $emptyOrReason) if (!$success);
    }

    return (1,$SkipEmpty); ## main call wants to know wether the entire tileset was empty so we return 1 for success and 1 if the tile was empty
}


#------------------------------------------------------------------
# remove temporary files etc
#-------------------------------------------------------------------
sub cleanup
{
    my $self = shift;
    my $Config = $self->{Config};

    # remove temporary job directory if 'Debug' is not set
    print STDERR "removing job dir",$self->{JobDir},"\n\n" if ($Config->get('Debug'));
    rmtree $self->{JobDir} unless $Config->get('Debug');
}

#----------------------------------------------------------------------------------------
# bbox->new(N,E,S,w)
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

1;
