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
    $::currentSubTask = "Download";
    
    ::statusMessage(sprintf("Tileset (%d,%d,%d) around %.2f,%.2f (complexity %d)", $req->ZXY, ($N+$S)/2, ($W+$E)/2, $req->{'complexity'}),1,0);
    
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

    my $beforeDownload = time();
    my ($FullDataFile, $reason) = $self->downloadData();
    if (!$FullDataFile)
    {
        ::statusMessage($reason, 1, 0);
        return (0, $reason);
    }
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
         # temporary debug: measure time it takes to render:
         my ($success, $empty, $reason) = $self->RenderTile($layer, $req->Y, $req->Z, $N, $S, $W, $E, 0,0 , $ImgW, $ImgH, $ImgH);

        #----------
        if (!$success)
        {   # Failed to render tiles, $empty contains error reason
            ::addFault("renderer",1);
            ::statusMessage($reason, 1, 0);
            return (0, $reason);
        }
        else
        {   # successfully rendered, so reset renderer faults
            ::resetFault("renderer");
        }

        #----------
        # This directory is now ready for upload.
        # move it up one folder, so it can be picked up.
        # Unless we have moved everything to the local slippymap already
        if (!$Config->get("LocalSlippymap"))
        {
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
    }

    ::keepLog($$,"GenerateTileset","stop",'x='.$req->X.',y='.$req->Y.',z='.$req->Z." for layers ".$req->layers_str);

    # Cleaning up of tmpdirs etc. are called in the destructor DESTROY
    return (1, "");
}

#------------------------------------------------------------------

=pod 

=head3 downloadData

Download the area for the tileset (whole or in stripes, as required)
into $self->{JobDir}

B<parameter>: none

B<returns>: (filename, reason) 
I<filename>: resulting data osm filename (without path) on success, 'undef' on failure.
I<reason>: string describing the error.

=cut
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

    # temporarily turn off locales
    no locale;
    my $bbox = sprintf("%f,%f,%f,%f",
      $W1, $S1, $E1, $N1);
    use locale;

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
        if (($req->Z >= 12 && $req->{complexity} < 20000000) || $req->Z < 12)
           {$res = ::DownloadFile($URL, $partialFile, 0)};

        if ((! $res) and ($req->Z < 12))
        {
            # Fetching of lowzoom data from OSMXAPI failed
            ::addFault("nodataXAPI",1);
            return (undef, "No data here! (OSMXAPI)")
        }

        my $reason = "no data here!";

        if ((! $res) and ($Config->get("FallBackToROMA")))
        {
            # download of normal z>=12 data failed
            ::statusMessage("Trying ROMA",1,0);
            $URL=sprintf("%s%s/map?bbox=%s",
              $Config->get("ROMAURL"),$Config->get("OSMVersion"),$bbox);
            $res = ::DownloadFile($URL, $partialFile, 0);
            if (! $res)
            {   # ROMA fallback failed too
                $reason .= " (ROMA)";
                ::addFault("nodataROMA",1);
                # do not return, in case we have other fallbacks configured.
            }
            else
            {   # ROMA fallback succeeded
                ::resetFault("nodataROMA"); #reset to zero if data downloaded
            }
        }
       
        if ((! $res) and ($Config->get("FallBackToXAPI")))
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
            ::statusMessage("Downloading: Map data for ".$req->layers_str." from OSMXAPI",0,3);
            print "Download\n$URL\n" if ($Config->get("Debug"));
            $res = ::DownloadFile($URL, $partialFile, 0);
            if (! $res)
            {   # OSMXAPI fallback failed too
                $reason .= " (OSMXAPI)";
                ::addFault("nodataXAPI",1);
                # do not return, in case we have other fallbacks configured.
            }
            else
            {   # OSMXAPI fallback succeeded
                ::resetFault("nodataXAPI"); #reset to zero if data downloaded
            }
        }
        
        if ((! $res) and ($Config->get("FallBackToSlices")))
        {
            ::statusMessage("Trying smaller slices",1,0);
            my $slice=(($E1-$W1)/10); # A chunk is one tenth of the width 
            my $tryN;
            my $slicedFailed = 0;
            for (my $j = 1 ; $j<=10 ; $j++)
            {
                $tryN = 1 unless ($slicedFailed); # each slice gets tried 3 times, we
                # assume the api is just a bit under load so it would
                # be wasteful to return the tileset with "no Data"
                $res = 0; #set false before next slice is downloaded, unless one of the previous slices failed.
                while (($tryN < 3) and (! $res) and (!$slicedFailed))
                {
                    $URL = sprintf("%s%s/map?bbox=%f,%f,%f,%f", 
                      $Config->get("APIURL"),$Config->get("OSMVersion"), ($W1+($slice*($j-1))), $S1, ($W1+($slice*$j)), $N1); 
                    $partialFile = File::Spec->join($self->{JobDir},"data-$i-$j.osm");
                    push(@{$filelist}, $partialFile);
                    ::statusMessage("Downloading: Map data (slice $j of 10)",0,3);
                    print "Download\n$URL\n" if ($Config->get("Debug"));
                    $res = ::DownloadFile($URL, $partialFile, 0);
                    
                    if ((! $res) and ($tryN >= 3))
                    {   # Sliced download failed too
                        ::addFault("nodata",1);
                        $reason .= " (sliced)";
                        $slicedFailed = 1;
                    }
                    elsif (! $res)
                    {
                        ::statusMessage("(slice $j of 10) failed on try $tryN, retrying",1,3);
                        ::talkInSleep("waiting before retry",10*$tryN);
                        $tryN++; #try again!
                    }
                    else
                    {   # Sliced download succeeded (at least one slice)
                        ::resetFault("nodata");
                    }
                }
            }
        }
        
        if ($res)
        {   # Download of data succeeded
            if ($req->Z < 12) ## FIXME: hardcoded zoom
            {
                ::resetFault("nodataXAPI"); #reset to zero if data downloaded
            }
        }
        else
        {
            ::statusMessage("download of data failed",1,0);
            return (undef, $reason);
        }
    } # foreach

    ::mergeOsmFiles($DataFile, $filelist);

    # Get the API date time for the data so we can assign it to the generated image (for tracking from when a tile actually is)
    $self->{JobTime} = [stat $DataFile]->[9];
    
    # Check for correct UTF8 (else inkscape will run amok later)
    # FIXME: This doesn't seem to catch all string errors that inkscape trips over.
    ::statusMessage("Checking for UTF-8 errors",0,3);
    if (my $line = ::fileUTF8ErrCheck($DataFile))
    {
        ::statusMessage(sprintf("found incorrect UTF-8 chars in line %d. job (%d,%d,%d)",$line, $req->ZXY),1,0);
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
            my $Cmd = sprintf("\"%s\" tr %s %s > \"%s\"",
                    $Config->get("XmlStarlet"),
                    "maplint/lib/run-tests.xsl",
                    "$inputFile",
                    "tmp.$$");
            ::statusMessage("Running maplint",0,3);
            ::runCommand($Cmd,$$);
            $Cmd = sprintf("\"%s\" tr %s %s > \"%s\"",
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
            my $Cmd = sprintf("perl close-areas.pl %d %d %d < %s > %s",
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
                if (! $self->GenerateSVG($layerDataFile, $layer, $zoom))
                {    # an error occurred while rendering.
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
    foreach my $pid(@pids)
    {
        waitpid($pid,0);
        $success &= ($? >> 8);
    }

    ::statusMessage("exit forked renderer returning $success",0,6);
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
#   $Zoom - the cuurent zoom level that we render
#   $N, $S, $W, $E - bounds of the tile
#   $ImgX1,$ImgY1,$ImgX2,$ImgY2 - location of the tile in the SVG file
#   $ImageHeight - Height of the entire SVG in SVG units
#   returns: (success, allEmpty, reason)
#-----------------------------------------------------------------------------
sub RenderTile 
{
    my $self = shift;
    my ($layer, $Ytile, $Zoom, $N, $S, $W, $E, $ImgX1,$ImgY1,$ImgX2,$ImgY2,$ImageHeight) = @_;
    my $Config = TahConf->getConfig();
    my $maxzoom = $Config->get($layer."_MaxZoom");
    my $req = $self->{req};
    my $forkval = $Config->get("Fork");

    return (1,1) if($Zoom > $maxzoom);
    
    # Render it to PNG
    printf "Tilestripe %s (%s,%s): Lat %1.3f,%1.3f, Long %1.3f,%1.3f, X %1.1f,%1.1f, Y %1.1f,%1.1f\n", 
            $Ytile,$req->X,$req->Y,$N,$S,$W,$E,$ImgX1,$ImgX2,$ImgY1,$ImgY2 if ($Config->get("Debug")); 

    my ($FullBigPNGFileName, $reason) = 
          ::svg2png($self->{JobDir}, $req, $Ytile, $Zoom,$ImgX1,$ImgY1,$ImgX2,$ImgY2,$ImageHeight);

    if (!$FullBigPNGFileName)
    {  # svg2png failed
       return (0, 0, $reason);
    }

    # splitImageX returns true if all tiles extracted were empty.
    # this might break if a higher zoom tile would contain data that is 
    # not rendered at the current zoom level. 

    (my $success,my $empty, $reason) = 
           ::splitImageX($layer, $req, $Zoom, $Ytile, $FullBigPNGFileName);
    if (!$success)
    {  # splitimage failed
       return (0, 0, $reason);
    }

    # If splitimage is empty Should we skip going further up the zoom level?
    if ($empty and !$Config->get($layer."_RenderFullTileset")) 
    {
        # leap forward because in progresscountingas this tile and 
        # all higher zoom tiles of it are "done" (empty).
        for (my $j = $maxzoom; $j >= $Zoom ; $j--)
        {
            $::progress += 2 ** ($maxzoom-$j);
        }
	return (1, 1, "All tiles empty");
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

    # we create Fork*2 rasterizer threads
    if ($forkval && $Zoom < ($req->Z + $forkval))
    {
        my $pid = fork();
        if (not defined $pid) 
        {
            ::cleanUpAndDie("RenderTile: could not fork, exiting","EXIT",4); # exit if asked to fork but unable to
        }
        elsif ($pid == 0) 
        {
            # we are the child process
            $self->{childThread}=1;
            my ($success, $empty, $reason) = $self->RenderTile($layer, $YA, $Zoom+1, $N, $LatC, $W, $E, $ImgX1, $ImgYC, $ImgX2, $ImgY2,$ImageHeight);
            # we can't talk to our parent other than through exit codes.
            exit($success);
        }
        else
        {
            my ($parent_success,$empty, $reason) = $self->RenderTile($layer, $YB, $Zoom+1, $LatC, $S, $W, $E, $ImgX1, $ImgY1, $ImgX2, $ImgYC,$ImageHeight);
            waitpid($pid,0);
            my $ChildExitValue = ($? >> 8);
            if (! ($ChildExitValue && $parent_success))
            {
                return (0, 0, "Forked rasterizer failed");
            }
        }
    }
    else
    {
        my ($success,$empty,$reason) = $self->RenderTile($layer, $YA, $Zoom+1, $N, $LatC, $W, $E, $ImgX1, $ImgYC, $ImgX2, $ImgY2,$ImageHeight);
        return (0, $empty, $reason) if (!$success);
        ($success,$empty,$reason) = $self->RenderTile($layer, $YB, $Zoom+1, $LatC, $S, $W, $E, $ImgX1, $ImgY1, $ImgX2, $ImgYC,$ImageHeight);
        return (0, $empty, $reason) if (!$success);
    }

    return (1, 0, "OK");
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
