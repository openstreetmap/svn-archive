package ThreadedRenderer;

use warnings;
use strict;
use File::Temp qw/ tempfile tempdir /;
use File::Copy;
use File::Path;
use Error qw(:try);
use TahConf;
use tahlib;
use Tileset;
use Request;

use threads;
use Thread::Semaphore;
use Time::HiRes qw ( sleep );

sub new
{
    my $class  = shift;
    my $Config = TahConf->getConfig();

    my %sharedStack : shared;

    my $self = {
        Config    => $Config,
        SHARED    => \%sharedStack,
        DESTROYED => 0,
        children  => [],
    };

    my @sharedJobs : shared;
    my @sharedJobsLayer : shared;
    my @childStop : shared;
    my @sharedJobErrors : shared;
    my %sharedRequest : shared;

    $self->{SHARED}->{RENDERERJOBS}      = \@sharedJobs;
    $self->{SHARED}->{RENDERERJOBLAYER}  = \@sharedJobsLayer;
    $self->{SHARED}->{RENDERERJOBERRORS} = \@sharedJobErrors;
    $self->{SHARED}->{RENDERERJOBERROR}  = "";
    $self->{SHARED}->{RENDERERJOBSPOS}   = -1;
    $self->{SHARED}->{RENDERERJOBSREADY} = 0;
    $self->{SHARED}->{CHILDSTOP}         = \@childStop;         # for stop single clients
    $self->{SHARED}->{JOBDIR}            = "";
    $self->{SHARED}->{REQEST}            = \%sharedRequest;
    $self->{SHARED}->{MAXSVGFILESIZE}    = 0;

    # GenerateSVG

    my @sharedSvgJobs : shared;
    my @sharedSvgJobsLayer : shared;
    my @sharedSvgJobsLayerDataFile : shared;

    $self->{SHARED}->{GENERATESVGJOBS}         = \@sharedSvgJobs;
    $self->{SHARED}->{GENERATESVGJOBLAYER}     = \@sharedSvgJobsLayer;
    $self->{SHARED}->{GENERATESVGJOBLAYERDATA} = \@sharedSvgJobsLayerDataFile;
    $self->{SHARED}->{GENERATESVGJOBPOS}       = -1;
    $self->{SHARED}->{GENERATESVGJOBSREADY}    = 0;

    # Downloader

    my @sharedDownloadJobs : shared;

    $self->{SHARED}->{DOWNLOADJOBS}      = \@sharedDownloadJobs;
    $self->{SHARED}->{DOWNLOADJOBPOS}    = -1;
    $self->{SHARED}->{DOWNLOADJOBSREADY} = 0;

    $self->{'maxChildren'} = $Config->get("Cores");
    $self->{SHARED}->{'lastMaxChildren'} = $self->{'maxChildren'};

    bless $self, $class;
    return $self;
}

sub startChildren
{
    my $self   = shift;
    my $Config = $self->{Config};

    if ( $Config->get("Cores") )
    {

        # add renderer childs
        $self->{'rendererChildren'}  = [];
        $self->{'rendererSemaphore'} = Thread::Semaphore->new();

        ::statusMessage( "init " . $self->{'maxChildren'} . " Renderer Child Tasks", 0, 6 );

        for my $childID ( 1 .. $self->{'maxChildren'} )
        {
            $self->{SHARED}->{CHILDSTOP}->[$childID] = 0;
            $self->{'rendererChildren'}->[$childID] = threads->create(
                sub {

                    threads->detach();

                    my $Config = $self->{Config};
                    my $pos;
                    my $layer;
                    my $layerDataFile;
                    my $zoom;
                    my $oldJobDir = "";
                    my $req;

                    # wait of the global destroy flag or of the singel stop flag
                    while ( !$self->{SHARED}->{DESTROYED} )
                    {

                        # create new tileset for a new job
                        # TODO: bad way export some funktions to an external .pl file
                        $self->{'rendererSemaphore'}->down();
                        if ( $oldJobDir ne $self->{SHARED}->{JOBDIR} && $self->{SHARED}->{JOBDIR} ne "" )
                        {
                            $req = new Request;
                            $req->ZXY(
                                $self->{SHARED}->{REQEST}->{'z'},
                                $self->{SHARED}->{REQEST}->{'x'},
                                $self->{SHARED}->{REQEST}->{'y'}
                            );
                            $req->layers_str( $self->{SHARED}->{REQEST}->{'layers'} );

                            $self->{tileset} = new Tileset( $req, 1, $self->{SHARED}->{JOBDIR} );
                            $oldJobDir = $self->{SHARED}->{JOBDIR};
                        }
                        $self->{'rendererSemaphore'}->up();

                        # Renderer
                        while ( !$self->{SHARED}->{CHILDSTOP}->[$childID]
                            && $oldJobDir eq $self->{SHARED}->{JOBDIR}
                            && $self->newRendereJobAvailable() )
                        {
                            $zoom  = 0;
                            $layer = "";

                            # access: lock()
                            $self->{'rendererSemaphore'}->down();

                            # get an other child the last job?
                            if ( $self->newRendereJobAvailable() )
                            {
                                $self->{SHARED}->{RENDERERJOBSPOS}++;
                                $pos   = $self->{SHARED}->{RENDERERJOBSPOS};
                                $zoom  = $self->{SHARED}->{RENDERERJOBS}->[$pos];
                                $layer = $self->{SHARED}->{RENDERERJOBLAYER}->[$pos];
                            }

                            # access: unlock()
                            $self->{'rendererSemaphore'}->up();

                            if ( $zoom && $layer )
                            {
                                $::currentSubTask = "Renderer-$layer-z$zoom";

                                ####
                                # i do my work now
                                ####
                                if ( !$self->rendererError() )
                                {
                                    ::statusMessage( "Rendererclient $childID get job $pos zoom $zoom on layer $layer", 1, 10 );
                                    eval { $self->{tileset}->Render_new( $layer, $zoom ); };
                                    if ($@)
                                    {

                                        $self->setRendererError("$@");

                                        ::statusMessage( "ERROR: Rendererclient $childID Renderer return $@", 1, 10 );
                                    }
                                }
                                $self->{'rendererSemaphore'}->down();
                                $self->{SHARED}->{RENDERERJOBSREADY}++;
                                $self->{'rendererSemaphore'}->up();
                            }
                        }    # Renderer end

                        # GenerateSVG
                        while ($oldJobDir eq $self->{SHARED}->{JOBDIR}
                            && ( !$self->newRendereJobAvailable() || $self->{SHARED}->{CHILDSTOP}->[$childID] )
                            && $self->newGenerateSVGJobAvailable() )
                        {
                            $zoom          = 0;
                            $layer         = "";
                            $layerDataFile = "";

                            # let me just run if no new rendererjob is available or I'm stopped

                            # access: lock()
                            $self->{'rendererSemaphore'}->down();

                            # get an other child the last job?
                            if ( $self->newGenerateSVGJobAvailable() )
                            {
                                $self->{SHARED}->{GENERATESVGJOBPOS}++;
                                $pos           = $self->{SHARED}->{GENERATESVGJOBPOS};
                                $zoom          = $self->{SHARED}->{GENERATESVGJOBS}->[$pos];
                                $layer         = $self->{SHARED}->{GENERATESVGJOBLAYER}->[$pos];
                                $layerDataFile = $self->{SHARED}->{GENERATESVGJOBLAYERDATA}->[$pos];
                            }

                            # access: unlock()
                            $self->{'rendererSemaphore'}->up();

                            if ( $zoom && $layer && $layerDataFile )
                            {
                                $::currentSubTask = "GenSVG-$layer-z$zoom";

                                if ( !$self->rendererError() )
                                {
                                    ::statusMessage( "Rendererclient $childID get GenerateSVG job $pos zoom $zoom on layer $layer", 1, 10 );
                                    eval { $self->{tileset}->GenerateSVG( $layer, $zoom, $layerDataFile ); };
                                    if ($@)
                                    {

                                        $self->setRendererError("$@");
                                        ::statusMessage( "ERROR: Rendererclient $childID GenerateSVG return $@", 1, 10 );
                                    }
                                    else
                                    {
                                        $self->updateMaxRenderer("$layer-z$zoom.svg");

                                        ::statusMessage( "add renderjob zoom: $zoom ", 1, 10 );
                                        $self->addJob( $zoom, $layer );

                                    }
                                }
                                $self->{'rendererSemaphore'}->down();
                                $self->{SHARED}->{GENERATESVGJOBSREADY}++;
                                $self->{'rendererSemaphore'}->up();
                            }
                        }    # GenerateSVG end

                        # Download
                        # Download have the lowst prio
                        while ($oldJobDir eq $self->{SHARED}->{JOBDIR}
                            && ( !$self->newRendereJobAvailable() || $self->{SHARED}->{CHILDSTOP}->[$childID] )
                            && !$self->newGenerateSVGJobAvailable()
                            && $self->{SHARED}->{DOWNLOADJOBPOS} < $#{ $self->{SHARED}->{DOWNLOADJOBS} } )
                        {

                            $layer = "";
                            my ( $X, $Y, $Z, $joinedjob );

                            # let me just run if no new rendererjob is available or I'm stopped

                            # access: lock()
                            $self->{'rendererSemaphore'}->down();

                            # get an other child the last job?
                            if ( $self->{SHARED}->{DOWNLOADJOBPOS} < $#{ $self->{SHARED}->{DOWNLOADJOBS} } )
                            {
                                $self->{SHARED}->{DOWNLOADJOBPOS}++;
                                $pos       = $self->{SHARED}->{DOWNLOADJOBPOS};
                                $joinedjob = $self->{SHARED}->{DOWNLOADJOBS}->[$pos];
                                ( $layer, $Z, $X, $Y ) = split( ',', $joinedjob );

                            }

                            # access: unlock()
                            $self->{'rendererSemaphore'}->up();

                            if ($layer)
                            {
                                $::currentSubTask = "Download-$layer-z$Z";

                                if ( !$self->rendererError() )
                                {
#                                    ::statusMessage( "Rendererclient $childID get Download job $pos xyz $X,$Y,$Z on layer $layer", 1, 10 );
                                    eval { $self->{tileset}->getFile( $layer, $Z, $X, $Y ); };
                                    if ($@)
                                    {

                                        $self->setRendererError("$@");
                                        ::statusMessage( "ERROR: Rendererclient $childID Downloader return $@", 1, 10 );
                                    }
                                }
                                $self->{'rendererSemaphore'}->down();
                                $self->{SHARED}->{DOWNLOADJOBSREADY}++;
                                $self->{'rendererSemaphore'}->up();
                            }
                        }    # Download end

                        sleep(0.1);
                    }
                    ::statusMessage( "Renderer child $childID exit", 1, 10 );
                  }    #sub;
            );         #threads->create
        }    # for
    }
}

sub addJob
{
    my $self = shift;

    my $zoom  = shift;
    my $layer = shift;

    $self->{'rendererSemaphore'}->down();

    my $pos = $#{ $self->{SHARED}->{RENDERERJOBS} };
    $pos++;

    $self->{SHARED}->{RENDERERJOBS}->[$pos]     = $zoom;
    $self->{SHARED}->{RENDERERJOBLAYER}->[$pos] = $layer;

    $self->{'rendererSemaphore'}->up();

}

sub newRendereJobAvailable
{
    my $self = shift;

    return 1 if $self->{SHARED}->{RENDERERJOBSPOS} < $#{ $self->{SHARED}->{RENDERERJOBS} };

    return;
}

sub addGenerateSVGjob
{
    my $self = shift;

    my $layer         = shift;
    my $zoom          = shift;
    my $layerDataFile = shift;

    $self->{'rendererSemaphore'}->down();

    my $pos = $#{ $self->{SHARED}->{GENERATESVGJOBS} };
    $pos++;

    $self->{SHARED}->{GENERATESVGJOBS}->[$pos]         = $zoom;
    $self->{SHARED}->{GENERATESVGJOBLAYER}->[$pos]     = $layer;
    $self->{SHARED}->{GENERATESVGJOBLAYERDATA}->[$pos] = $layerDataFile;

    $self->{'rendererSemaphore'}->up();

}

sub newGenerateSVGJobAvailable
{
    my $self = shift;

    return 1 if $self->{SHARED}->{GENERATESVGJOBPOS} < $#{ $self->{SHARED}->{GENERATESVGJOBS} };

    return;
}


sub addDownloadjob
{
    my $self = shift;

    my $Layer = shift;
    my $Z     = shift;
    my $X     = shift;
    my $Y     = shift;

    $self->{'rendererSemaphore'}->down();

    my $pos = $#{ $self->{SHARED}->{DOWNLOADJOBS} };
    $pos++;

    $self->{SHARED}->{DOWNLOADJOBS}->[$pos] = join( ',', $Layer, $Z, $X, $Y );

    $self->{'rendererSemaphore'}->up();

}

sub getDownloadjobCount
{
    my $self = shift;

    return $#{ $self->{SHARED}->{DOWNLOADJOBS} } +1;
}

sub getDownloadjobPos
{
    my $self = shift;

    return $self->{SHARED}->{DOWNLOADJOBPOS} +1;
}

sub wait
{
    my $self = shift;
    my $deep = shift;
    
    # GenerateSVG wait
    while ( $self->{SHARED}->{GENERATESVGJOBSREADY} <= $#{ $self->{SHARED}->{GENERATESVGJOBS} } )
    {
       sleep(0.1);
    }

    # Renderer wait
    while ( $self->{SHARED}->{RENDERERJOBSREADY} <= $#{ $self->{SHARED}->{RENDERERJOBS} } )
    {
        sleep(0.1);
    }

    return if $deep;

    $self->wait(1);
}

sub waitDownloadJobs
{
    my $self = shift;

    # Download wait
    while ( $self->{SHARED}->{DOWNLOADJOBSREADY} <= $#{ $self->{SHARED}->{DOWNLOADJOBS} } )
    {
        sleep(0.1);
    }

}

sub resetDownloadJobs
{
    my $self = shift;

    $self->{'rendererSemaphore'}->down();

    undef @{ $self->{SHARED}->{DOWNLOADJOBS} };
    $self->{SHARED}->{DOWNLOADJOBPOS}    = -1;
    $self->{SHARED}->{DOWNLOADJOBSREADY} = 0;

    $self->{'rendererSemaphore'}->up();
    
}

# reset my lists
sub Reset
{
    my $self = shift;

    $self->{'rendererSemaphore'}->down();

    $::GlobalChildren->{SHARED}->{STOPALL} = 0;

    # Renderer
    undef @{ $self->{SHARED}->{RENDERERJOBS} };
    undef @{ $self->{SHARED}->{RENDERERJOBLAYER} };
    undef @{ $self->{SHARED}->{RENDERERJOBERRORS} };
    $self->{SHARED}->{RENDERERJOBERROR}  = "";
    $self->{SHARED}->{RENDERERJOBSPOS}   = -1;
    $self->{SHARED}->{RENDERERJOBSREADY} = 0;
    $self->{SHARED}->{MAXSVGFILESIZE}    = 0;

    # GenerateSVG
    undef @{ $self->{SHARED}->{GENERATESVGJOBS} };
    undef @{ $self->{SHARED}->{GENERATESVGJOBLAYER} };
    undef @{ $self->{SHARED}->{GENERATESVGJOBLAYERDATA} };
    $self->{SHARED}->{GENERATESVGJOBPOS}    = -1;
    $self->{SHARED}->{GENERATESVGJOBSREADY} = 0;

    # Download
    undef @{ $self->{SHARED}->{DOWNLOADJOBS} };
    $self->{SHARED}->{DOWNLOADJOBPOS}    = -1;
    $self->{SHARED}->{DOWNLOADJOBSREADY} = 0;

    $self->{'rendererSemaphore'}->up();

    # reset renderer limitation
    $self->setMaxRenderer( $self->{'maxChildren'} )

}

sub setJobDir
{
    my $self   = shift;
    my $jobDir = shift;

    $self->{'rendererSemaphore'}->down();

    $self->{SHARED}->{JOBDIR} = $jobDir;

    $self->{'rendererSemaphore'}->up();

}

sub rendererError
{
    my $self = shift;

    if ( $self->{SHARED}->{RENDERERJOBERROR} )
    {
        return ( $self->{SHARED}->{RENDERERJOBERROR} );
    }

    return;

}

sub setRendererError
{
    my $self  = shift;
    my $error = shift;

    $self->{'rendererSemaphore'}->down();

    $self->{SHARED}->{RENDERERJOBERROR} = $error;

    $::GlobalChildren->{SHARED}->{STOPALL} = 1;

    $self->{'rendererSemaphore'}->up();

}

# put the job request in a shared strukture "i can not share classes"
sub setRequest
{
    my $self = shift;
    my $req  = shift;

    my ( $z, $x, $y ) = $req->ZXY;
    $self->{'rendererSemaphore'}->down();

    $self->{SHARED}->{REQEST}->{'z'} = $z;
    $self->{SHARED}->{REQEST}->{'x'} = $x;
    $self->{SHARED}->{REQEST}->{'y'} = $y;

    $self->{SHARED}->{REQEST}->{'layers'} = $req->layers_str();

    $self->{'rendererSemaphore'}->up();

}

# set the maximum of paralel working renderer
# a 10 mb svg file consum ~ 1.3gb ram on a 64bit system
sub updateMaxRenderer
{
    my $self     = shift;
    my $filename = shift;

    my $Config = $self->{Config};

    $filename = File::Spec->join( $self->{SHARED}->{JOBDIR}, $filename );
    my @datafileStats = stat($filename);

    if ( $self->{SHARED}->{MAXSVGFILESIZE} < $datafileStats[7] )
    {
        $self->{'rendererSemaphore'}->down();
        $self->{SHARED}->{MAXSVGFILESIZE} = $datafileStats[7];
        $self->{'rendererSemaphore'}->up();
    }
    else
    {
        return;
    }

    my $caMemoryUsage = $self->{SHARED}->{MAXSVGFILESIZE} / 1024 / 7;

    if ( ( $caMemoryUsage * $self->{'maxChildren'} ) > $Config->get("MaxMemory") )
    {

        # too little memory
        my $newMaxChildren = int( $Config->get("MaxMemory") / $caMemoryUsage );

        return if ( $self->{SHARED}->{'lastMaxChildren'} <= $newMaxChildren );

        $self->setMaxRenderer($newMaxChildren);
    }
}

sub setMaxRenderer
{
    my $self           = shift;
    my $newMaxChildren = shift;

    $newMaxChildren = 1 if $newMaxChildren < 1;

    $newMaxChildren = $self->{'maxChildren'} if $newMaxChildren > $self->{'maxChildren'};

    return if ( $self->{SHARED}->{'lastMaxChildren'} == $newMaxChildren );

    if ( $self->{'maxChildren'} != $newMaxChildren )
    {
        ::statusMessage(
            "too little memory for the render job and "
              . $self->{'maxChildren'}
              . " Children, stop "
              . ( $self->{'maxChildren'} - $newMaxChildren )
              . " renderer childs from "
              . $self->{'maxChildren'},
            1, 10
        );
    }

    $self->{'rendererSemaphore'}->down();

    $self->{SHARED}->{'lastMaxChildren'} = $newMaxChildren;

    for ( my $i = $self->{'maxChildren'} ; $i > 0 ; $i-- )
    {
        if ($newMaxChildren)
        {

            # set child to on
            $newMaxChildren--;
            $self->{SHARED}->{CHILDSTOP}->[$i] = 0;
        }
        else
        {

            # set child to off
            $self->{SHARED}->{CHILDSTOP}->[$i] = 1;
        }
    }

    $self->{'rendererSemaphore'}->up();

}

1;
