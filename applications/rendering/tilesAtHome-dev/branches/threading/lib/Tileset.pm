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
use TahConf;
use Server;
use tahlib;
use tahproject;
use File::Copy;
use File::Path;
use GD 2 qw(:DEFAULT :cmp);
use LWP::Simple;

use threads;
use Thread::Semaphore;


#-----------------------------------------------------------------------------
# creates a new Tileset instance and returns it
# parameter is a request object with x,y,z, and layer atributes set
# $self->{WorkingDir} is a temporary directory that is only used by this job and
# which is deleted when the Tileset instance is not in use anymore.
#-----------------------------------------------------------------------------
sub new
{
    my $class = shift;
    my $req = shift;    #Request object
    my $child = shift;
    my $jobDir = shift;

    my $Config = TahConf->getConfig();

    my $self = {
        req => $req,
        Config => $Config,
        JobTime => undef,     # API fetching time for the job as timestamp
        bbox => undef,        # bbox of required tileset
        marg_bbox => undef,   # bbox of required tileset including margins
        childThread => 0,     # marks whether we are a parent or child thread
        };

    my $delTmpDir = 1-$Config->get('Debug');

    if($child) {
         $self->{bbox}= bbox->new(ProjectXY($req->ZXY)) if $req;
         $self->{JobDir} = $jobDir if $jobDir;
         $self->{childThread} = $child;
    }
    else {
        $self->{JobDir} = tempdir( 
            sprintf("%d_%d_%d_XXXXX",$self->{req}->ZXY),
            DIR      => $Config->get('WorkingDirectory'), 
            CLEANUP  => $delTmpDir,
            );
    }

    # create true color images by default
    GD::Image->trueColor(1);

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

    # Inkscape auto-backup/reset setup
    # Takes a backup copy of ~/.inkscape/preferences.xml if
    # AutoResetInkscapePrefs is turned on in config and we are using Inkscape
    # as rasterizer.
    # This backup copy is restored if Inkscape crashes and mentions
    # preferences.xml on STDERR
    # FIXME: this must check the integrity of the preference file first, otherwise we backup a broken config, which will later on be restored, leading to a failure loop
    if( $Config->get("AutoResetInkscapePrefs") == 1 &&
        $SVG::Rasterize::object->engine()->isa('SVG::Rasterize::Engine::Inkscape') ){

        $self->{inkscape_autobackup}{cfgfile} = glob('~/.inkscape/');
        if($self->{inkscape_autobackup}{cfgfile})
        {
            $self->{inkscape_autobackup}{cfgfile} .= "preferences.xml";
            $self->{inkscape_autobackup}{backupfile} = "$self->{inkscape_autobackup}{cfgfile}.bak"
                if defined($self->{inkscape_autobackup}{cfgfile});

            if( -f $self->{inkscape_autobackup}{cfgfile} ){
                if ( -s $self->{inkscape_autobackup}{cfgfile} == 0 ) {
                    #emty config file found! i delete it
                    unlink($self->{inkscape_autobackup}{cfgfile});
                }
                else {
                    copy($self->{inkscape_autobackup}{cfgfile}, $self->{inkscape_autobackup}{backupfile})
                        or do {
                            warn "Error doing backup of $self->{inkscape_autobackup}{cfgfile} to $self->{inkscape_autobackup}{backupfile}: $!\n";
                            delete($self->{inkscape_autobackup});
                    };
                }
            } else {
                delete($self->{inkscape_autobackup});
            }
        }
    }

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
    return if ($self->{childThread} || !defined $self->{childThread} || !defined $self->{Config});

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

    $::currentSubTask = "";
    ::keepLog($$,"GenerateTileset","start","x=".$req->X.',y='.$req->Y.',z='.$req->Z." for layers ".$req->layers_str);
    $self->{bbox}= bbox->new(ProjectXY($req->ZXY));

    my $usingThreads = 0;
    $usingThreads = 1 if (defined $::GlobalChildren->{ThreadedRenderer});


    if($usingThreads) {
      $::GlobalChildren->{ThreadedRenderer}->Reset();
      $::GlobalChildren->{ThreadedRenderer}->setRequest($self->{req});
      $::GlobalChildren->{ThreadedRenderer}->setJobDir($self->{JobDir});

      $::GlobalChildren->{optimizePngTasks}->dataReset();
    }

    ::statusMessage(sprintf("Tileset (%d,%d,%d) around %.2f,%.2f", $req->ZXY, $self->{bbox}->center), 1, 0);

    if($req->Z >= 12)
    {
        #------------------------------------------------------
        # Download data (returns full path to data.osm or 0)
        #------------------------------------------------------

        my $beforeDownload = time();

        # TODO: FIXME: remove it on the stable version! only for debuging and tests else { its the original}
        # add a optional testadata directory (download not the data)
        # DO NOT UPLOAD THE RESULTS REAL!
        my $testdatadir = File::Spec->join($Config->get("WorkingDirectory"), 'testdatadir');
        my $testdatafile = File::Spec->join($testdatadir, 'data.osm');
        my $FullDataFile = "";

        if($Config->get("useTestDirData") && $Config->get("debug") && $Config->get("UploadToDirectory")
             && -d $testdatadir && -f $testdatafile)
        {
            $FullDataFile = File::Spec->join($self->{JobDir}, 'data.osm');
            copy($testdatafile,$FullDataFile)
        }
        else {
            $FullDataFile = $self->downloadData($req->layers);
        }

        ::statusMessage("Download in ".(time() - $beforeDownload)." sec",1,10); 


        #------------------------------------------------------
        # Handle all layers, one after the other
        #------------------------------------------------------

        foreach my $layer ($req->layers)
        {
            # TileDirectory is the name of the directory for finished tiles
            my $TileDirectory = sprintf("%s_%d_%d_%d.dir", $Config->get($layer."_Prefix"), $req->ZXY);

            # JobDirectory is the directory where all final .png files are stored.
            # It is not used for temporary files.
            my $JobDirectory = File::Spec->join($self->{JobDir}, $TileDirectory);
            mkdir $JobDirectory;

            $self->generateNormalLayer($layer);
        }

        # this part is only in threaded modus in use
        if( $usingThreads )
        {
            #############
            # at this time is the client on work and the main process wait now
            #############
            $::GlobalChildren->{ThreadedRenderer}->wait();
    
            if(my $error = $::GlobalChildren->{ThreadedRenderer}->rendererError() ) {
             throw TilesetError "Render failure: $error", "renderer";
            }
    
            $::GlobalChildren->{optimizePngTasks}->wait();
            $::GlobalChildren->{optimizePngTasks}->dataReset();
    
            # now compress all the data
            foreach my $layer ($req->layers)
            {
                if ($Config->get("CreateTilesetFile") and !$Config->get("LocalSlippymap")) {
                    $self->createTilesetFile($layer);
                }
                else {
                    $self->createZipFile($layer);
                }
            }
        }
    }
    else
    {
        ###########################################
        ###########################################
        # lowzoom modus
        ###########################################
        ###########################################

        my %alllayers;
        my $maxlayer = ($req->Z < 6) ? 6 : 12;
        my $forkpid = 0;
        my $tileStichingAllowed = 0;

        foreach my $layer ($req->layers)
        {
            $alllayers{$layer}{generate} = 1;
            # work aroung "no such variable warning
            my %vl = $Config->varlist(lc("^${layer}_LowZoom"));
            if(%vl && (my $l = $vl{lc("${layer}_LowZoom")}))
            {
                my $text = "base";
                foreach my $lzlayer (split(",",$l))
                {
                    $alllayers{$lzlayer}{"is$text"} = 1;
                    $alllayers{$layer}{$text} = $lzlayer;
                    $text = "overlay";
                }
            }
            else
            {
                $alllayers{$layer}{direct} = 1;
            }
        }

        # Tile stiching Allowed?
        eval
        {
            require Image::Magick;
            if(($Image::Magick::VERSION cmp "6.4.5") < 0)
            {
              die "At least Version 6.4.5 of ImageMagick required to get usable results.";
            }
            Image::Magick->import();
            require LWP::Simple;
            require File::Compare;
            require OceanTiles;
            $self->{OceanTiles} = new OceanTiles();
            $self->{EmptyLandImageIM} = new Image::Magick(size=>'256x256');
            $self->{EmptyLandImageIM}->Read("xc:rgb(248,248,248)") and die;
            $self->{EmptySeaImageIM} = new Image::Magick(size=>'256x256');
            $self->{EmptySeaImageIM}->Read("xc:rgb(181,214,241)") and die;
        };

        if(!$@) {
            $tileStichingAllowed = 1;
        }


        if($tileStichingAllowed)
        {
            $::progress=0;
            $::progressPercent=0;

            $self->{NumTiles} = 0;
            # up-to maxlayer, as this needs to be downloaded as well
            my $t = 1;
            my $sum = 0;
            for(my $i = $req->Z; $i < $maxlayer; ++$i)
            {
                $sum += $t;
                $t *= 4;
            }
            foreach my $layer (keys %alllayers)
            {
                $self->{NumTiles} += $t if $alllayers{$layer}{isbase};
                $self->{NumTiles} += $sum if $alllayers{$layer}{isoverlay} || !$alllayers{$layer}{direct};
            }
            # $t is now number of tiles in $maxlayer
            # $sum is number of tiles up to $maxlayer

            if($Config->get("Fork") || $usingThreads)
            {
                $forkpid = fork() if !$usingThreads;
                if($forkpid == 0)
                {
                    $self->{childThread}=1 if !$usingThreads;
                    my @baselayers;
                    my @ovlayers;

                    foreach my $layer (keys %alllayers)
                    {
                        next if $alllayers{$layer}{direct};
                        push(@baselayers, $layer) if $alllayers{$layer}{isbase};
                        push(@ovlayers, $layer) if $alllayers{$layer}{isoverlay};
                    }
                    $self->{NumTiles} = $t*scalar(@baselayers) + $sum*scalar(@ovlayers) if !$usingThreads;

                    my $num = 2**($maxlayer-$req->Z);
                    my $startx = $req->X*$num;
                    my $starty = $req->Y*$num;
                    foreach my $layer (@baselayers)
                    {
                        $::currentSubTask = "download $layer";
                        for(my $i = 0; $i < $num; ++$i)
                        {
                            for(my $j = 0; $j < $num; ++$j)
                            {
                                $self->getFile($layer, $maxlayer, $startx+$i,
                                $starty+$j);
                            }
                        }
                    }
                    foreach my $layer (@ovlayers)
                    {
                        $::currentSubTask = "download $layer";
                        for(my $z = $req->Z; $z < $maxlayer; ++$z)
                        {
                            $num = 2**($z-$req->Z);
                            $startx = $req->X*$num;
                            $starty = $req->Y*$num;
                            for(my $i = 0; $i < $num; ++$i)
                            {
                                for(my $j = 0; $j < $num; ++$j)
                                {
                                    $self->getFile($layer, $z, $startx+$i,
                                    $starty+$j);
                                }
                            }
                        }
                    }
                    exit(1) if !$usingThreads;
                }
            }
        }

        # downloading osm data and generate layer
        foreach my $layer (keys %alllayers)
        {
            my $TileDirectory = sprintf("%s_%d_%d_%d.dir", $Config->get($layer."_Prefix"), $req->ZXY);
            my $JobDirectory = File::Spec->join($self->{JobDir}, $TileDirectory);
            mkdir $JobDirectory;
            if($alllayers{$layer}{direct})
            {
                my $beforeDownload = time();
                my $FullDataFile = $self->downloadData($layer);
                ::statusMessage("Download in ".(time() - $beforeDownload)." sec",1,10); 
                $self->generateNormalLayer($layer);

                # the renderer in threaded modus do not create Zips
                if( $usingThreads )
                {
                    $::GlobalChildren->{ThreadedRenderer}->wait();
    
                    if(my $error = $::GlobalChildren->{ThreadedRenderer}->rendererError() ) {
                      throw TilesetError "Render failure: $error", "renderer";
                    }
    
                    $::GlobalChildren->{optimizePngTasks}->wait();
                    $::GlobalChildren->{optimizePngTasks}->dataReset();

                    if ($Config->get("CreateTilesetFile") and !$Config->get("LocalSlippymap")) {
                        $self->createTilesetFile($layer);
                    }
                    else {
                        $self->createZipFile($layer);
                    }
                }
            }
        }


        if($tileStichingAllowed)
        {
            foreach my $layer ($req->layers)
            {
                # note - base layer can be created on the fly
                if(!$alllayers{$layer}{direct} && !$alllayers{$layer}{isbase})
                {
                    try
                    {
                        $self->lowZoom($req->ZXY, $maxlayer, $layer,
                        $alllayers{$layer}{base}, $alllayers{$layer}{overlay});
                    }
                    otherwise
                    {
                        my $E = shift;
                        if($forkpid)
                        {
                            kill 15, $forkpid;
                            waitpid($forkpid, 0);
                        }
                        throw $E;
                    };
                }
            }
            waitpid($forkpid, 0) if $forkpid;
        }
        else
        {
            ::statusMessage("Tile stiching not supported without Image::Magick.", 1, 1);
        }

        # wait of the last optimitePNG tasks
        if (defined $::GlobalChildren->{optimizePngTasks}) {
            $::GlobalChildren->{optimizePngTasks}->wait();
            $::GlobalChildren->{optimizePngTasks}->dataReset();
        }


        # now copy/cleanup the results
        foreach my $layer ($req->layers)
        {
            next if $alllayers{$layer}{direct};
            my $TileDirectory = sprintf("%s_%d_%d_%d.dir", $Config->get($layer."_Prefix"), $req->ZXY);
            my $JobDirectory = File::Spec->join($self->{JobDir}, $TileDirectory);
            my $hasdata = 0;
            my $file;
            opendir(DIR, $JobDirectory);
            while(defined($file = readdir(DIR)))
            {
                if($file =~ /^[a-z]+_$maxlayer/)
                {
                    if($Config->get("Debug"))
                    {
                        rename File::Spec->join($JobDirectory, $file),
                        File::Spec->join($self->{JobDir}, $file);
                    }
                    else
                    {
                        unlink(File::Spec->join($JobDirectory, $file));
                    }
                }
                else
                {
                    ++$hasdata;
                }
            }
            if($hasdata)
            {
                if ($Config->get("CreateTilesetFile") and !$Config->get("LocalSlippymap")) {
                    $self->createTilesetFile($layer);
                }
                else {
                    $self->createZipFile($layer);
                }
            }
        }
    }

    $::currentSubTask = "";
    ::keepLog($$,"GenerateTileset","stop",'x='.$req->X.',y='.$req->Y.',z='.$req->Z." for layers ".$req->layers_str);


    # cleanup children data
    if(defined $::GlobalChildren->{ThreadedRenderer}) {
        $::GlobalChildren->{ThreadedRenderer}->Reset();
    }

    # Cleaning up of tmpdirs etc. are called in the destructor DESTROY
}

sub generateNormalLayer
{
    my ($self,$layer) = @_;

    #reset progress for each layer
    $::progress=0;
    $::progressPercent=0;
    $::currentSubTask = $layer;

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

    if ($self->{Config}->get("Fork"))
    {   # Forking to render zoom levels in parallel
        $self->forkedRender($layer, $layerDataFile);
    }
    elsif ($self->{Config}->get("Cores") && defined $::GlobalChildren->{ThreadedRenderer} )
    {    # use threads for rendering zoom levels parallel
        $self->threadedRender($layer, $layerDataFile);
    }
    else
    {   # Non-forking render
        $self->nonForkedRender($layer, $layerDataFile);
    }
}

sub lowZoomFileName
{
    my ($self, $Layer, $Z, $X, $Y) = @_;

    my $prefixd = sprintf "%s_%d_%d_%d",$self->{Config}->get("${Layer}_Prefix"),
    $self->{req}->ZXY;
    my $prefix = sprintf "%s_%d_%d_%d",$self->{Config}->get("${Layer}_Prefix"),
    $Z,$X,$Y;
    return (File::Spec->join($self->{JobDir}, "$prefixd.dir", "$prefix.png"),
      "$Layer ($Z,$X,$Y)");
}

sub getFile {
    my ($self, $Layer, $Z, $X, $Y,$direct) = @_;

    # add a download job on threaded mot so i'm not a clild and return lets do the work from child
    if( !$direct && defined $::GlobalChildren->{ThreadedRenderer} )
    {
        if ( !$self->{childThread} ) {
            $::GlobalChildren->{ThreadedRenderer}->addDownloadjob($Layer, $Z, $X, $Y);
            return;
        }
        else {
            my $allFiles = $::GlobalChildren->{ThreadedRenderer}->getDownloadjobCount();
            my $DownloadPos = $::GlobalChildren->{ThreadedRenderer}->getDownloadjobPos();
            
            $::progressPercent = int(100 * $DownloadPos / $allFiles) ;
 #           ::statusMessage("Loading", 0, 10);
        }
    }
    else
    {

        ## child download status/lock
        if( !$self->{childThread} && defined $::GlobalChildren->{ThreadedRenderer} )
        {
            # download startet and not finished wait max xx sek and start then a new download
            if( $::GlobalChildren->{ThreadedRenderer}->getDownloadJobStatus($Layer, $Z, $X, $Y) eq 1 )
            {
                my $maxWaitCount = 60;
                while( --$maxWaitCount > 0 
                        && $::GlobalChildren->{ThreadedRenderer}->getDownloadJobStatus($Layer, $Z, $X, $Y) eq 1 )
                {
                    ::statusMessage("wait of download $Layer, $Z, $X, $Y ", 0, 10);
                    sleep 1;
                }
            }
    
            # 2 == download finished
            if( $::GlobalChildren->{ThreadedRenderer}->getDownloadJobStatus($Layer, $Z, $X, $Y) eq 2 )
            {
                return;
            }
        }

        ++$::progress;
        $::progressPercent = $::progress / $self->{NumTiles} * 100;
#        ::statusMessage("Loading $Layer($Z,$X,$Y)", 0, 10);
        
    }

    my ($pfile, $file) = $self->lowZoomFileName($Layer, $Z, $X, $Y);

    my $url = "";

    for(my $i = 0; $i < 3 && !-f $pfile; ++$i)
    {
        eval
        {
            if($self->{Config}->get('Debug'))
            {
                ::statusMessage("Download file $file",1,6);
            }
            $url = sprintf("http://tah.openstreetmap.org/Tiles/%s/%d/%d/%d.png",$Layer,$Z,$X,$Y);
            
            LWP::Simple::mirror($url,$pfile);
        };
        if($@) {
            unlink $pfile ;
            ::statusMessage("Download failed  $@ on $url",1,10);
            sleep 5;
        }
    }
    throw TilesetError "The image $file download failed", "lowzoom" if !-f $pfile;
}

# Recursively create (including any downloads necessary) a tile
sub lowZoom {
    my ($self, $Z, $X, $Y, $MaxZ, $OutputLayer, $BaseLayer, $CaptionLayer) = @_;

    $::currentSubTask = $OutputLayer;

    # Get tiles
    if($Z >= $MaxZ)
    {
        $self->getFile($BaseLayer,$Z,$X,$Y,1);
    }
    else
    {
        # Recursively get/create the 4 subtiles
        $self->lowZoom($Z+1,$X*2,$Y*2,$MaxZ,$OutputLayer,$BaseLayer,$CaptionLayer);
        $self->lowZoom($Z+1,$X*2+1,$Y*2,$MaxZ,$OutputLayer,$BaseLayer,$CaptionLayer);
        $self->lowZoom($Z+1,$X*2,$Y*2+1,$MaxZ,$OutputLayer,$BaseLayer,$CaptionLayer);
        $self->lowZoom($Z+1,$X*2+1,$Y*2+1,$MaxZ,$OutputLayer,$BaseLayer,$CaptionLayer);

        $self->getFile($CaptionLayer,$Z,$X,$Y,1) if $CaptionLayer;

        if( defined $::GlobalChildren->{optimizePngTasks} )
        {
            # wait only on the next lower zoomlevel
            if(defined $self->{myLastZ} && $self->{myLastZ} > $Z) {
                # wait of the optimizePng childs
                $::GlobalChildren->{optimizePngTasks}->wait();
                $::GlobalChildren->{optimizePngTasks}->dataReset();
            
                # wait of my downloads
#                $::GlobalChildren->{ThreadedRenderer}->waitDownloadJobs();
#                $::GlobalChildren->{ThreadedRenderer}->resetDownloadJobs();
            }

            $self->{myLastZ} = $Z;
        }


        # Create the tile from those subtiles
        $self->supertile($X,$Y,$Z,$OutputLayer,$BaseLayer,$CaptionLayer);
    }

}

# Open a PNG file, and return it as a Magick image (or 0 if not found)
sub readLocalImage
{
    my ($self,$Layer,$Z,$X,$Y) = @_;
    my ($pfile, $file) = $self->lowZoomFileName($Layer, $Z, $X, $Y);

    my $imImage;
    throw TilesetError "The image $pfile is missing", "lowzoom" if !-f $pfile;
    my $Image;
    eval { $Image = GD::Image->newFromPng($pfile); };
    if(!$Image)
    {
      sleep 5;
      eval { $Image = GD::Image->newFromPng($pfile); };
      throw TilesetError "The image $file failed to load", "lowzoom" if !$Image;
    }

    # Detect empty tiles here:
    if (File::Compare::compare($pfile, "emptyland.png") == 0)
    {
        return 0 if($Layer eq "caption");
        $imImage = $self->{EmptyLandImageIM};
    }
    elsif (File::Compare::compare($pfile, "emptysea.png") == 0)
    {
        return 0 if($Layer eq "caption");
        $imImage = $self->{EmptySeaImageIM};
    }
    elsif (not ($Image->compare($self->{EmptyLandImage}) & GD_CMP_IMAGE))
    {
        return 0 if($Layer eq "caption");
        $imImage = $self->{EmptyLandImageIM};
    }
    elsif (not ($Image->compare($self->{EmptySeaImage}) & GD_CMP_IMAGE))
    {
        return 0 if($Layer eq "caption");
        $imImage = $self->{EmptySeaImageIM};
    }
    elsif (not ($Image->compare($self->{BlackTileImage}) & GD_CMP_IMAGE))
    {
        return 0 if($Layer eq "caption");
        if($Z == 12 && $self->{OceanTiles})
        {
            my $state = $self->{OceanTiles}->getState($X, $Y);
            if($state eq "sea")
            {
                ::statusMessage("Tile state mismatch for $file: mixed/black != sea", 1, 3);
                $imImage = $self->{EmptySeaImageIM};
            }
            elsif($state eq "land")
            {
                ::statusMessage("Tile state mismatch for $file: mixed/black != land", 1, 3);
                $imImage = $self->{EmptyLandImageIM};
            }
        }

        # make tile a dark blue, so someone fixes this error
        if(!$imImage)
        {
            ::statusMessage("Tile state mismatch for $file: mixed/black found", 1, 3);

            $imImage = new Image::Magick(size=>'256x256');
            $imImage->Read("xc:rgb(0,0,255)");
        }
    }
    # try to work around an ImageMagick bug with transparency in >= 6.4.3
    elsif($Layer eq "caption" && open(FILE,">",$pfile))
    {
        binmode FILE;
        $Image->trueColorToPalette();
        print FILE $Image->png;
        close FILE;
    }
    if($imImage && $Z == 12 && $Layer ne "caption" && $self->{OceanTiles})
    {
        my $state = $self->{OceanTiles}->getState($X, $Y);
        if($imImage == $self->{EmptySeaImageIM})
        {
            if($state ne "sea")
            {
                ::statusMessage("Tile state mismatch for $file: sea != $state", 1, 3);
                $imImage = $self->{EmptyLandImageIM} if($state eq "land");
            }
        }
        else
        {
            if($state ne "land")
            {
                ::statusMessage("Tile state mismatch for $file: land != $state", 1, 3);
                $imImage = $self->{EmptySeaImageIM} if($state eq "sea");
            }
        }
    }
    if(!$imImage)
    {
        $imImage = new Image::Magick;
        if (my $err = $imImage->Read($pfile))
        {
            throw TilesetError "The image $file failed to load: $err", "lowzoom";
        }
    }

    return($imImage);
}

sub supertile {
    my ($self,$X,$Y,$Z,$OutputLayer,$BaseLayer,$CaptionLayer) = @_;
    my $Config = $self->{Config};


    my ($pfile, $file) = $self->lowZoomFileName($BaseLayer, $Z, $X, $Y);
    my $Image;
    if(!-f $pfile)
    {
        ++$::progress;
        $::progressPercent = $::progress / $self->{NumTiles} * 100;
        # Load the subimages
        my $AA = $self->readLocalImage($BaseLayer,$Z+1,$X*2,$Y*2);
        my $BA = $self->readLocalImage($BaseLayer,$Z+1,$X*2+1,$Y*2);
        my $AB = $self->readLocalImage($BaseLayer,$Z+1,$X*2,$Y*2+1);
        my $BB = $self->readLocalImage($BaseLayer,$Z+1,$X*2+1,$Y*2+1);

        if($AA == $self->{EmptySeaImageIM}
        && $AB == $self->{EmptySeaImageIM}
        && $BA == $self->{EmptySeaImageIM}
        && $BB == $self->{EmptySeaImageIM})
        {
            ::statusMessage("Writing sea $file", 0, 6);
            copy("emptysea.png", $pfile);
            $Image = $self->{EmptySeaImageIM};
        }
        elsif($AA == $self->{EmptyLandImageIM}
        && $AB == $self->{EmptyLandImageIM}
        && $BA == $self->{EmptyLandImageIM}
        && $BB == $self->{EmptyLandImageIM})
        {
            ::statusMessage("Writing land $file", 0, 6);
            copy("emptyland.png", $pfile);
            $Image = $self->{EmptyLandImageIM};
        }
        else
        {
            $Image = Image::Magick->new(size=>'512x512');
            # Create the supertile
            $Image->ReadImage('xc:white');

            # Copy the subimages into the 4 quadrants
            foreach my $x (0, 1)
            {
                foreach my $y (0, 1)
                {
                    next unless (($Z < 9) || (($x == 0) && ($y == 0)));
                    $Image->Composite(image => $AA,
                                    geometry => sprintf("512x512+%d+%d", $x, $y),
                                    compose => "darken");

                    $Image->Composite(image => $BA,
                                    geometry => sprintf("512x512+%d+%d", $x + 256, $y),
                                    compose => "darken");

                    $Image->Composite(image => $AB,
                                    geometry => sprintf("512x512+%d+%d", $x, $y + 256),
                                    compose => "darken");

                    $Image->Composite(image => $BB,
                                    geometry => sprintf("512x512+%d+%d", $x + 256, $y + 256),
                                    compose => "darken");
                }
            }

            $Image->Scale(width => "256", height => "256");
            $Image->Set(type=>"Palette");
            $Image->Set(quality => 90); # compress image
            $Image->Write($pfile);
            $self->optimizePng($pfile, $Config->get("${BaseLayer}_Transparent"));
            ::statusMessage("Writing $file", 0, 6);
        }
    }
    if($CaptionLayer)
    {
        ++$::progress;
        $::progressPercent = $::progress / $self->{NumTiles} * 100;
        # CaptionFile can be empty --> nothing to do
        my $CaptionFile = $self->readLocalImage($CaptionLayer,$Z,$X,$Y);

        $Image = $self->readLocalImage($BaseLayer,$Z,$X,$Y) if !$Image;

        # Overlay the captions onto the tiled image and then write it
        if($CaptionFile)
        {
          # do not overwrite our test images
          if($Image == $self->{EmptySeaImageIM}
          || $Image == $self->{EmptyLandImageIM})
          {
            $Image = $Image->Clone;
          }
          $Image->Composite(image => $CaptionFile);
        }
        ($pfile, $file) = $self->lowZoomFileName($OutputLayer, $Z, $X, $Y);
        if($Image->Write($pfile) != 1)
        {
          throw TilesetError "The image $file failed to save", "lowzoom";
        }
        my $gdimage;
        eval { $gdimage = GD::Image->newFromPng($pfile); };
        throw TilesetError "The image $file failed to load", "lowzoom" if !$gdimage;
        if (not ($gdimage->compare($self->{EmptyLandImage}) & GD_CMP_IMAGE)) {
            ::statusMessage("Writing land $file", 0, 6);
            copy("emptyland.png", $pfile);
        }
        elsif (not ($gdimage->compare($self->{EmptySeaImage}) & GD_CMP_IMAGE)) {
            ::statusMessage("Writing sea $file", 0, 6);
            copy("emptysea.png", $pfile);
        }
        else
        {
            $self->optimizePng($pfile, $Config->get("${OutputLayer}_Transparent"));
            ::statusMessage("Writing $file", 0, 6);
        }
    }
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
    my ($self, @layers) = @_;
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

    my $bbox = sprintf("%f,%f,%f,%f", $W1, $S1, $E1, $N1);

    my $DataFile = File::Spec->join($self->{JobDir}, "data.osm");
    
    my @predicates;
    my $predicatesname = ($req->Z < 6 ? "world" : "lowzoom") . "predicates";
    
    foreach my $layer (@layers) {
        my %layer_config = $Config->varlist("^${layer}_", 1);
        if (not $layer_config{$predicatesname}) {
            @predicates = ();
            last;
        }
        my $predicates = $layer_config{$predicatesname};
        # strip spaces in predicates
        $predicates =~ s/\s+//g;
        push(@predicates, split(/,/, $predicates));
    }

    my @OSMServers = (@predicates) ? split(/,/, $Config->get("XAPIServers")) : split(/,/, $Config->get("APIServers"));

    my $Server = Server->new();
    my $res;
    my $reason;

    if ($req->priority() > 1) {
        my $firstServer = shift(@OSMServers);
        if($firstServer eq "API") {
            my $secondServer = shift(@OSMServers);
            unshift(@OSMServers, $firstServer);
            unshift(@OSMServers, $secondServer);
        } else {
            unshift(@OSMServers, $firstServer);
        }
    }

    my $filelist;
    foreach my $OSMServer (@OSMServers) {
        $self->{JobTime} = time();
        my @URLS;
        my @title;
        if (@predicates) {
            foreach my $predicate (@predicates) {
                my $URL = $Config->get("XAPI_$OSMServer");
                $URL =~ s/%p/${predicate}/g;                # substitute %p place holder with predicate
                $URL =~ s/%v/$Config->get('OSMVersion')/ge; # substitute %v place holder with API version
                push(@URLS, $URL);
                push(@title, $predicate);
            }
        }
        else {
            my $URL = $Config->get("API_$OSMServer");
            $URL =~ s/%v/$Config->get('OSMVersion')/ge; # substitute %v place holder with API version
            push(@URLS, $URL);
            push(@title, "map data");
        }

        $filelist = [];
        my $i=0;
        foreach my $URL (@URLS) {
            ++$i;
            my $partialFile = File::Spec->join($self->{JobDir}, "data-$i.osm");
            my $title = shift @title;
            ::statusMessage("Downloading $title for " . join(",",@layers) ." from ".$OSMServer, 0, 3);

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
                ::statusMessage("Trying smaller slices for $title from $OSMServer",1,0);
                my $slice = (($E1 - $W1) / 10); # A slice is one tenth of the width
                my $slicesdownloaded = 0;
                for (my $j = 1; $j <= 10; $j++) {
                    my $bbox = sprintf("%f,%f,%f,%f", $W1 + ($slice * ($j - 1)), $S1, $W1 + ($slice * $j), $N1);
                    my $currentURL = $URL;
                    my $trylimit = 3 + $slicesdownloaded;
                    $currentURL =~ s/%b/${bbox}/g;    # substitute bounding box place holder
                    $partialFile = File::Spec->join($self->{JobDir}, "data-$i-$j.osm");
                    $res = 0;
                    for (my $k = 1; $k <= $trylimit; $k++) {  # try each slice 3 times
                        if(($k % 4) == 0) {
                            ::talkInSleep("Sleeping for 30 seconds before the next retry", 30);
                        }
                        ::statusMessage("Downloading $title (slice $j of 10) from $OSMServer", 0, 3);
                        print "Downloading: $currentURL\n" if ($Config->get("Debug"));
                        try {
                            $Server->downloadFile($currentURL, $partialFile, 0);
                            $res = 1;
                            ++$slicesdownloaded;
                        }
                        catch ServerError with {
                            my $err = shift();
                            print "Download failed: " . $err->text() . "\n" if ($Config->get("Debug"));;
                            my $message = ($k < $trylimit) ? "Download of $title slice $j from $OSMServer failed, trying again" : "Download of $title slice $j from $OSMServer failed $trylimit times, giving up";
                            ::statusMessage($message, 0, 3);
                        };
                        last if ($res); # don't try again if download was successful
                    }
                    last if (!$res); # don't download remaining slices if one fails
                    push(@{$filelist}, $partialFile);
                }
                $res = ($slicesdownloaded == 10);
            }
            if (!$res) {
                ::statusMessage("Download of $title from $OSMServer failed", 0, 3);
                last; # don't download other URLs if this one failed
            } 
        } # foreach @URLS

        last if ($res); # don't try another server if the download was successful
    } # foreach @OSMServers

    if ($res) {   # Download of data succeeded
        ::statusMessage("Download of data complete", 1, 10);
    }
    else {
        # we need to have an additional full line error message here, as the exception will
        # ignore our partial displayed previous lines
        ::statusMessage("All servers tried for data download", 1, 3);
        my $OSMServers = join(',', @OSMServers);
        throw TilesetError "Download of data failed from $OSMServers", "nodata ($OSMServers)";
    }

    ($res, $reason) = ::mergeOsmFiles($DataFile, $filelist);
    if(!$res) {
        throw TilesetError "Striped download failed with: " . $reason;
    }

    # Check for correct UTF8 (else inkscape will run amok later)
    # FIXME: This doesn't seem to catch all string errors that inkscape trips over.
    ::statusMessage("Checking for UTF-8 errors",0,3);
    if (my $line = ::fileUTF8ErrCheck($DataFile))
    {
        ::statusMessage(sprintf("found incorrect UTF-8 chars in line %d. job (%d,%d,%d)",$line, $req->ZXY),1,0);
        throw TilesetError "UTF8 test failed", "utf8";
    }
    ::resetFault("utf8"); #reset to zero if no UTF8 errors found.
    return $DataFile;
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
            if($Config->get('Debug'))
            {
                ::statusMessage("Running close-areas ($Cmd)",0,3);
            }
            else
            {
                ::statusMessage("Running close-areas",0,3);
            }
            ::runCommand($Cmd,$$);
        }
        elsif ($preprocessor eq "area-center")
        {
           if ($Config->get("JavaAvailable"))
           {
	       if ($Config->get("JavaVersion") >= 1.6)
               {
                   my $javaHeapSize; ## default values for overridable config parameters go into config.defaults so as to ease maintenance.
                   if ($Config->get('JavaHeapSize'))
                   {
                       $javaHeapSize = $Config->get('JavaHeapSize');
                   }

                   # use preprocessor only for XSLT for now. Using different algorithm for area center might provide inconsistent results"
                   # on tile boundaries. But XSLT is currently in minority and use different algorithm than orp anyway, so no difference.
                   my $Cmd = sprintf("java -Xmx%s -cp %s com.bretth.osmosis.core.Osmosis -q -p org.tah.areaCenter.AreaCenterPlugin --read-xml %s --area-center --write-xml %s",
                               $javaHeapSize,
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

sub getzoom
{
    my $self = shift;
    my ($layer) = @_;
    my $req = $self->{req};
    my $Config = $self->{Config};

    my $minzoom = $req->Z;
    my $maxzoom = $Config->get($layer."_MaxZoom");
    if($minzoom < 6 && $maxzoom >= 6) {$maxzoom = 5;}
    elsif($minzoom < 12 && $maxzoom >= 12) {$maxzoom = 11;}
    return ($minzoom, $maxzoom);
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
    my ($minzoom, $maxzoom) = $self->getzoom($layer);

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

    if ($Config->get("CreateTilesetFile") and !$Config->get("LocalSlippymap")) {
        $self->createTilesetFile($layer);
    }
    else {
        $self->createZipFile($layer);
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
    my ($minzoom, $maxzoom) = $self->getzoom($layer);

    for (my $zoom = $req->Z ; $zoom <= $maxzoom; $zoom++) {
        $self->Render($layer, $zoom, $layerDataFile)
    }

    if (defined $::GlobalChildren->{optimizePngTasks}) {
        $::GlobalChildren->{optimizePngTasks}->wait();
        $::GlobalChildren->{optimizePngTasks}->dataReset();
    }


    if ($Config->get("CreateTilesetFile") and !$Config->get("LocalSlippymap")) {
        $self->createTilesetFile($layer);
    }
    else {
        $self->createZipFile($layer);
    }
}

#-------------------------------------------------------------------
# renders the tiles, not using threads
# paramter: ($layer, $layerDataFile)
#-------------------------------------------------------------------
sub threadedRender
{
    my $self = shift;
    my ($layer, $layerDataFile) = @_;
    my $req = $self->{req};
    my $Config = $self->{Config};
    my $minzoom = $req->Z;
    my $maxzoom = $Config->get($layer."_MaxZoom");

    # add GenerateSVG jobs. after finishing add he self the job to the renderer
    for (my $zoom = $maxzoom ; $zoom >= $req->Z; $zoom--) {

        ::statusMessage("add GenerateSVG job layer: $layer zoom: $zoom " ,1,10);
        $::GlobalChildren->{ThreadedRenderer}->addGenerateSVGjob($layer, $zoom, $layerDataFile);

    }

}

#-------------------------------------------------------------------
# renders the tiles for one zoom level
# paramter: ($layer, $zoom)
# this Renderer is only used from threaded children
#-------------------------------------------------------------------
sub Render_new
{
    my $self = shift;
    my ($layer, $zoom ) = @_;
    my $Config = $self->{Config};
    my $req = $self->{req};

    $::progress = 0;
    $::progressPercent = 0;
    $::currentSubTask = "$layer-z$zoom";

    my $stripes = 1;
    if ($Config->get("RenderStripes")) {
        my $level = $zoom - $req->Z;
        if ($level >= $Config->get("RenderStripes")) {
            $stripes = 4 ** ($level - $Config->get("RenderStripes") + 1);
            if ($stripes > 2 ** $level) {
                $stripes = 2 ** $level;
            }
        }
    }
    
    
    $self->RenderSVG($layer, $zoom, $stripes);

    $self->SplitTiles($layer, $zoom, $stripes);

    ::statusMessage("Renderer done", 1, 10);

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

    my $stripes = 1;
    if ($Config->get("RenderStripes")) {
        my $level = $zoom - $req->Z;
        if ($level >= $Config->get("RenderStripes")) {
            $stripes = 4 ** ($level - $Config->get("RenderStripes") + 1);
            if ($stripes > 2 ** $level) {
                $stripes = 2 ** $level;
            }
        }
    }
    
    $self->GenerateSVG($layer, $zoom, $layerDataFile);

    $self->RenderSVG($layer, $zoom, $stripes);

    $self->SplitTiles($layer, $zoom, $stripes);
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
    ::statusMessage("Generating SVG file", 0, 6);
 
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

    ::statusMessage("SVG done", 1, 10);
}


#-----------------------------------------------------------------------------
# Render SVG for one zoom level
#   $layer - layer to be processed
#   $zoom
#-----------------------------------------------------------------------------
sub RenderSVG
{
    my $self = shift;
    my ($layer, $zoom, $stripes) = @_;
    my $Config = $self->{Config};
    my $Req = $self->{req};
    
    # File locations
    my $svg_file = File::Spec->join($self->{JobDir},"$layer-z$zoom.svg");

    my $tile_size = 256; # Tiles are 256 pixels square
    # png_width/png_height is the width/height dimension of resulting PNG file
    my $png_width = $tile_size * (2 ** ($zoom - $Req->Z));
    my $png_height = $png_width / $stripes;

    for (my $stripe = 0; $stripe < $stripes; $stripe++) {
        my $png_file = File::Spec->join($self->{JobDir},"$layer-z$zoom-s$stripe.png");

        # Make a variable that points to the renderer to save lots of typing...
        my $rasterize = $SVG::Rasterize::object;
        my $engine = $rasterize->engine();

        my %rasterize_params = (
            infile => $svg_file,
            outfile => $png_file,
            width => $png_width,
            height => $png_height,
            area => {
                type => 'relative',
                left => 0,
                right => 1,
                top => $stripe / $stripes,
                bottom => ($stripe + 1) / $stripes
            }
        );

        if( ref($engine) =~ /batik/i && $Config->get('BatikJVMSize') ){
            $rasterize_params{heapsize} = $Config->get('BatikJVMSize');
        }

        ::statusMessage("Rendering",0,3);

        my $error = 0;
        try {
            $rasterize->convert(%rasterize_params);
        } catch SVG::Rasterize::Engine::Error::Prerequisite with {
            my $e = shift;

            ::statusMessage("Rasterizing failed because of unsatisfied prerequisite: $e",1,0);

            throw TilesetError("Exception in RenderSVG: $e");
        } catch SVG::Rasterize::Engine::Error::NoOutput with {
            my $e = shift;

            ::statusMessage("Rasterizing failed to create output: $e",1,0);
            print "Rasterize command: \"".join('", "', @{$e->{cmd}})."\"\n" if $e->{cmd};
            print "Rasterize engine STDOUT:".$e->{stdout}."\n" if $e->{stdout};
            print "Rasterize engine STDERR:".$e->{stderr}."\n" if $e->{stderr};

            $Req->is_unrenderable(1);
            throw TilesetError("Exception in RenderSVG: $e");
        } catch SVG::Rasterize::Engine::Inkscape::Error::Runtime with {
            my $e = shift;
            $_[1] = 1; # Set second parameter scalar to 1 so the next catch block is used

            my $corrupt = 0;
            if( $e->{stderr} =~ /preferences.xml/ ){
                $corrupt = 1;
                warn "* Inkscape preference file corrupt. Delete Inkscape's preferences.xml to continue\n";

                # TODO: move this to a central function + the stuff in $self->new()
                if( defined($self->{inkscape_autobackup}) && defined $self->{inkscape_autobackup}{cfgfile} ){
                    my $cfg = $self->{inkscape_autobackup}{cfgfile};
                    my $bak = $self->{inkscape_autobackup}{backupfile};
                    warn "   AutoResetInkscapePrefs set, trying to reset $cfg\n";
                    unlink $cfg if( -f $cfg ); 
                    # FIXME: check backup is correct before putting back, or check preferences OK before backup.
                    $corrupt = 0; #if( rename($bak, $cfg) ); # how do we deal with a defect backup?
                }
            }

            if( $corrupt ){
                ## this error is fatal because it needs human intervention before processing can continue
                addFault("fatal",1);
                throw TilesetError("Inkscape preference file corrupt. Delete to continue", 'fatal');
            }

        } catch SVG::Rasterize::Engine::Error::Runtime with {
            my $e = shift;

            ::statusMessage("Rasterizing failed with runtime exception: $e",1,0);
            print "Rasterize command: \"".join('", "', @{$e->{cmd}})."\"\n" if $e->{cmd};
            print "Rasterize engine STDOUT:".$e->{stdout}."\n" if $e->{stdout};
            print "Rasterize engine STDERR:".$e->{stderr}."\n" if $e->{stderr};

            $Req->is_unrenderable(1);
            throw TilesetError("Exception in RenderSVG: $e");
        };
    }
}

#-----------------------------------------------------------------------------
# Split PNG for one zoom level into tiles
#   $layer - layer to be processed
#   $zoom
#   $stripes - number of stripes the layer has been rendered
#-----------------------------------------------------------------------------
sub SplitTiles
{
    my $self = shift;
    my ($layer, $zoom, $stripes) = @_;
    my $Config = $self->{Config};
    my $Req = $self->{req};

    my $minzoom = $Req->Z;
    my $size = 2 ** ($zoom - $minzoom);
    my $minx = $Req->X * $size;
    my $miny = $Req->Y * $size;
    my $number_tiles = $size * $size;
    my $stripe_height = $size / $stripes;

    # Size of tiles
    my $pixels = 256;

    $::progress = 0;
    $::progressPercent = 0;

    # Use one subimage for everything, and keep copying data into it
    my $SubImage = new GD::Image($pixels, $pixels, 1);#$Config->get($layer."_Transparent") ? 1 : 0);

    my $i = 0;
    my ($x, $y);

    for (my $stripe = 0; $stripe <= $stripes - 1; $stripe++) {
        ::statusMessage("Splitting stripe $stripe",0,3);

        my $png_file = File::Spec->join($self->{JobDir},"$layer-z$zoom-s$stripe.png");

        if(! -f $png_file || -s $png_file == 0 ) {
            throw TilesetError "SplitTiles: Missing File $png_file not exists", "fatal";
        }

        my $Image = GD::Image->newFromPng($png_file);

        if( not defined $Image ) {
            throw TilesetError "SplitTiles: Missing File $png_file encountered", "fatal";
        }

        for (my $iy = 0; $iy <= $stripe_height - 1; $iy++) {
            for (my $ix = 0; $ix <= $size - 1; $ix++) {
                $x = $minx + $ix;
                $y = $miny + $iy + $stripe * $stripe_height;
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
                my $tile_file;
                if ($Config->get("LocalSlippymap")) {
                    my $tile_dir = File::Spec->join($Config->get("LocalSlippymap"), $Config->get("${layer}_Prefix"), $zoom, $x);
                    File::Path::mkpath($tile_dir);
                    $tile_file = File::Spec->join($tile_dir, sprintf("%d.png", $y));
                }
                else {
                    # Construct base png directory
                    my $tile_dir = File::Spec->join($self->{JobDir}, sprintf("%s_%d_%d_%d.dir", $Config->get("${layer}_Prefix"), $Req->ZXY));
                    File::Path::mkpath($tile_dir);
                    $tile_file = File::Spec->join($tile_dir, sprintf("%s_%d_%d_%d.png", $Config->get("${layer}_Prefix"), $zoom, $x, $y));
                }

                # libGD comparison returns true if images are different. (i.e. non-empty Land tile)
                # so return the opposite (false) if the tile doesn't look like an empty land tile

                # Check for black tile output
                if (not ($SubImage->compare($self->{BlackTileImage}) & GD_CMP_IMAGE)) {
                    throw TilesetError "SplitTiles: Black Tile encountered", "inkscape";
                }

                # Detect empty tile here:
                if (not ($SubImage->compare($self->{EmptyLandImage}) & GD_CMP_IMAGE)) { 
                    copy("emptyland.png", $tile_file);
                }
                # same for Sea tiles
                elsif (not($SubImage->compare($self->{EmptySeaImage}) & GD_CMP_IMAGE)) {
                    copy("emptysea.png", $tile_file);
                }
                else {
                    if ($Config->get($layer."_Transparent")) {
                        $SubImage->transparent($SubImage->colorAllocate(248, 248, 248));
                    }
                    else {
                        $SubImage->transparent(-1);
                    }
                    # Get the image as PNG data
                    my $png_data = $SubImage->png;

                    # Store it
                    open (my $fp, ">$tile_file") || throw TilesetError "SplitTiles: Could not open $tile_file for writing", "fatal";
                    binmode $fp;
                    print $fp $png_data;
                    close $fp;

                    $self->optimizePng($tile_file, $Config->get("${layer}_Transparent"));
                }
            }
        }
    }
}


#-----------------------------------------------------------------------------
# optimize a PNG file
#
# Parameters: 
#   $png_file - file name of PNG file
#   $transparent - whether or not this is a transparent tile
#-----------------------------------------------------------------------------
sub optimizePng
{
    my $self = shift();
    my $png_file = shift();
    my $transparent = shift();


    # do we use threading? then push the job and go return
    if(defined $::GlobalChildren->{optimizePngTasks}) {
        #childThread == 2 if the optimizePNG child
        if(defined $self->{childThread} && $self->{childThread} ne "2" ) {
            $::GlobalChildren->{optimizePngTasks}->addJob($png_file,$transparent);
            return;
        }
    }


    my $Config = $self->{Config};
    my $redirect = ($^O eq "MSWin32") ? "" : ">/dev/null";
    my $tmp_suffix = '.cut';
    my $tmp_file = $png_file . $tmp_suffix;
    my (undef, undef, $png_file_name) = File::Spec->splitpath($png_file);

    my $cmd;
    if ($transparent) {
        # Don't quantize if it's transparent
        rename($png_file, $tmp_file);
    }
    elsif (($Config->get("PngQuantizer")||'') eq "pngnq") {
        $cmd = sprintf("\"%s\" -e .png%s -s1 -n256 %s %s",
                       $Config->get("pngnq"),
                       $tmp_suffix,
                       $png_file,
                       $redirect);

        ::statusMessage("ColorQuantizing $png_file_name", 0, 6);
        if(::runCommand($cmd, $::PID)) {
            # Color quantizing successful
            unlink($png_file);
        }
        else {
            # Color quantizing failed
            ::statusMessage("ColorQuantizing $png_file_name with ".$Config->get("PngQuantizer")." failed", 1, 0);
            rename($png_file, $tmp_file);
        }
    }
    else {
        ::statusMessage("Not Color Quantizing $png_file_name, pngnq not installed?", 0, 6);
        rename($png_file, $tmp_file);
    }

    if ($Config->get("PngOptimizer") eq "pngcrush") {
        $cmd = sprintf("\"%s\" -q %s %s %s",
                       $Config->get("Pngcrush"),
                       $tmp_file,
                       $png_file,
                       $redirect);
    }
    elsif ($Config->get("PngOptimizer") eq "optipng") {
           $cmd = sprintf("\"%s\" %s -out %s %s", #no quiet, because it even suppresses error output
                          $Config->get("Optipng"),
                          $tmp_file,
                          $png_file,
                          $redirect);
    }
    else {
        ::statusMessage("PngOptimizer not configured (should not happen, update from svn, and check config file)", 1, 0);
        ::talkInSleep("Install a PNG optimizer and configure it.", 15);
    }

    ::statusMessage("Optimizing $png_file_name", 0, 6);
    if(::runCommand($cmd, $::PID)) {
        unlink($tmp_file);
    }
    else {
        ::statusMessage("Optimizing $png_file_name with " . $Config->get("PngOptimizer") . " failed", 1, 0);
        rename($tmp_file, $png_file);
    }
}


#-----------------------------------------------------------------------------
# Compress all PNG files from one directory, creating a .zip file.
#
# Parameters:
#   $layer - the layer for which the tileset is to be compressed
#-----------------------------------------------------------------------------
sub createZipFile
{
    my $self = shift();
    my $layer = shift();
    my $Config = $self->{Config};

    my ($z, $x, $y) = $self->{req}->ZXY();

    my $prefix = $Config->get("${layer}_Prefix");
    my $tile_dir = File::Spec->join($self->{JobDir},
                                    sprintf("%s_%d_%d_%d.dir",
                                            $prefix, $z, $x, $y));

    my $upload_dir = File::Spec->join($Config->get("WorkingDirectory"), "uploadable");
    if (! -d $upload_dir) {
        mkpath($upload_dir) or throw TilesetError "Could not create upload directory '$upload_dir': $!", "fatal";
    }

    my $zip_file = File::Spec->join($upload_dir,
                                    sprintf("%s_%d_%d_%d_%d.zip",
                                            $prefix, $z, $x, $y, ::GetClientId()));
    
    my $temp_file = File::Spec->join($self->{JobDir},
                                     sprintf("%s_%d_%d_%d_%d.zip",
                                             $prefix, $z, $x, $y, ::GetClientId()));

    # ZIP all the tiles into a single file
    # First zip into "$Filename.part" and move to "$Filename" when finished
    my $stdout = File::Spec->join($self->{JobDir}, "zip.stdout");
    my $zip_cmd;
    if ($Config->get("7zipWin")) {
        $zip_cmd = sprintf('"%s" %s "%s" "%s"',
                           $Config->get("Zip"),
                           "a -tzip",
                           $temp_file,
                           File::Spec->join($tile_dir,"*.png"));
    }
    else {
        $zip_cmd = sprintf('"%s" -r -j "%s" "%s" > "%s"',
                           $Config->get("Zip"),
                           $temp_file,
                           $tile_dir,
                           $stdout);
    }

    if (::dirEmpty($tile_dir)) {
        ::statusMessage("Skipping emtpy tileset directory: $tile_dir", 1, 0);
        return;
    }
    # Run the zip command
    ::runCommand($zip_cmd, $::PID) or throw TilesetError "Error running command '$zip_cmd'", "fatal";

    # stdout is currently never used, so delete it unconditionally    
    unlink($stdout);
    
    # rename to final name so any uploader could pick it up now
    move ($temp_file, $zip_file) or throw TilesetError "Could not move ZIP file: $!", "fatal";
}


#-----------------------------------------------------------------------------
# Pack all PNG files from one directory into a tileset file.
#
# Parameters:
#   $layer - the layer for which the tileset file is to be created
#-----------------------------------------------------------------------------
sub createTilesetFile
{
    my $self = shift();
    my $layer = shift();
    my $Config = $self->{Config};

    my ($z, $x, $y) = $self->{req}->ZXY();

    my $index_start = 8;                                    # start of tile index, currently 8
    my $levels = $Config->get("${layer}_MaxZoom") - $z + 1; # number of layers in a tileset file, usually 6
    my $tiles = ((4 ** $levels) - 1) / 3;                   # number of tiles, 1365 for 6 zoom levels
    my $data_offset = $index_start + (4 * ($tiles + 1));    # start offset of tile data, 5472 for 6 zoom levels
    my $size = 1;                                           # size of base zoom level, for t@h always 1

    my $userid = 0; # the server will fill this in

    my $prefix = $Config->get("${layer}_Prefix");
    my $tile_dir = File::Spec->join($self->{JobDir},
                                    sprintf("%s_%d_%d_%d.dir",
                                            $prefix, $z, $x, $y));

    my $upload_dir = File::Spec->join($Config->get("WorkingDirectory"), "uploadable");
    if (! -d $upload_dir) {
        mkpath($upload_dir) or throw TilesetError "Could not create upload directory '$upload_dir': $!", "fatal";
    }

    my $file_name = File::Spec->join($upload_dir,
                                     sprintf("%s_%d_%d_%d_%d.tileset",
                                             $prefix, $z, $x, $y, ::GetClientId()));
    
    my $temp_file = File::Spec->join($self->{JobDir},
                                     sprintf("%s_%d_%d_%d_%d.tileset",
                                             $prefix, $z, $x, $y, ::GetClientId()));

    my $currpos = $data_offset;
    open my $fh, ">$temp_file" or throw TilesetError "Couldn't open '$temp_file': $!", "fatal";
    binmode $fh;
    seek $fh, $currpos, 0 or throw TilesetError "Couldn't seek: $!", "fatal";

    my @offsets;
    for my $iz (0 .. $levels - 1) {
        my $width = 2**$iz;
        for my $iy (0 .. $width-1) {
            for my $ix (0 .. $width-1) {
                my $png_name = File::Spec->join($tile_dir,
                                                sprintf("%s_%d_%d_%d.png",
                                                        $prefix, $z + $iz, $x * $width + $ix, $y * $width + $iy));
                my $length = -s $png_name;
                if (! -e $png_name) {
                    push(@offsets, 0);
                }
                elsif ($length == 67) {
                    if ($Config->get("${layer}_Transparent")) {
                        #this is empty transparent
                        push(@offsets, 3);
                    }
                    else {
                        #this is empty land
                        push(@offsets, 2);
                    }
                }
                elsif ($length == 69) {
                    #this is empty sea
                    push(@offsets, 1);
                }
                else {
                    open my $png, "<$png_name" or throw TilesetError "Couldn't open file '$png_name': $!", "fatal";
                    binmode $png;
                    my $buffer;
                    if( read($png, $buffer, $length) != $length ) {
                        throw TilesetError "Read failed from '$png_name': $!", "fatal";
                    }
                    close $png;
                    print $fh $buffer or throw TilesetError "Write failed on output to '$temp_file': $!", "fatal";
                    push @offsets, $currpos;
                    $currpos += $length;
                }
            }
        }
    }

    if( scalar( @offsets ) != $tiles ) {
        throw TilesetError sprintf("Bad number of offsets: %d (should be %d)", scalar(@offsets), $tiles), "fatal";
    }

    my $emptyness = 0; #what type of emptyness (land/sea)
    if ($currpos == $data_offset) {
        #tileset is empty
        #check the top level tile for the type of emptyness, assume that all tiles are the same
        my $png_name = File::Spec->join($tile_dir, sprintf("%s_%d_%d_%d.png", $prefix, $z, $x, $y));
        my $length = -s $png_name;
        if ($length == 67) {
            if ($Config->get("${layer}_Transparent")) {
                #this is empty transparent
                $emptyness = 3;
            }
            else {
                #this is empty land
                $emptyness = 2;
            }
        }
        elsif ($length == 69) {
            $emptyness = 1; #this is empty sea
        }
        $currpos = $index_start;
    }
    else {
        #tileset is not empty
        push @offsets, $currpos;
        seek $fh, $index_start, 0;
        print $fh pack("V*", @offsets) or throw TilesetError "Write failed to '$temp_file' $!", "fatal";
    }

    seek $fh, 0, 0;
    print $fh pack("CCCCV", 2, $levels, $size, $emptyness, $userid) or throw TilesetError "Write failed to '$temp_file': $!", "fatal";

print "speicher metadata file\n";
print $self->generateMetaData($prefix) ."\n";
print "-------------__";
    seek $fh, $currpos, 0;
    print $fh $self->generateMetaData($prefix);
    close $fh;

    move($temp_file, $file_name) or throw TilesetError "Could not move tileset file '$temp_file' to '$file_name': $!", "fatal";
}


#------------------------------------------------------------------------------
# Assemble meta data in Tileset file format
# Parameters:
#   $layer_prefix
#
# Returns:
#   $meta_data - string
#------------------------------------------------------------------------------
sub generateMetaData
{
    my $self = shift();
    my $layer_prefix = shift();
    my $req = $self->{req};

    my $meta_template = <<EOS;
Layer: %s
Zoom: %d
X: %d
Y: %d
Osm-Timestamp: %d

EOS

    my $meta_data = sprintf($meta_template, $layer_prefix, $req->ZXY(), $self->{JobTime});
    return $meta_data;
}


#------------------------------------------------------------------
# remove temporary files etc
#-------------------------------------------------------------------
sub cleanup
{
    my $self = shift;
    my $Config = $self->{Config};

    # remove temporary job directory if 'Debug' is not set
    print STDERR "removing job dir ",$self->{JobDir},"\n\n" if $Config->get('Debug');
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
