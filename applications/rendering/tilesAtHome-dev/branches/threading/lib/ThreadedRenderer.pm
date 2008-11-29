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
    my @sharedJobsLayerDataFile : shared;
    my @childStop : shared;
    my @sharedJobErrors : shared;
    my %sharedRequest : shared;

    $self->{SHARED}->{RENDERERJOBS}         = \@sharedJobs;
    $self->{SHARED}->{RENDERERJOBLAYER}     = \@sharedJobsLayer;
    $self->{SHARED}->{RENDERERJOBLAYERDATA} = \@sharedJobsLayerDataFile;
    $self->{SHARED}->{RENDERERJOBERRORS}    = \@sharedJobErrors;
    $self->{SHARED}->{RENDERERJOBERROR}     = "";
    $self->{SHARED}->{RENDERERJOBSPOS}      = -1;
    $self->{SHARED}->{RENDERERJOBSREADY}    = 0;
    $self->{SHARED}->{CHILDSTOP}            = \@childStop;                 # for stop single clients
    $self->{SHARED}->{JOBDIR}               = "";
    $self->{SHARED}->{REQEST}               = \%sharedRequest;
    $self->{SHARED}->{MAXSVGFILESIZE}       = 0;

    $self->{'maxChildren'} = $Config->get("Cores");

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

                    my $req    = $self->{req};
                    my $Config = $self->{Config};
                    my $pos;
                    my $layer;
                    my $layerDataFile;
                    my $zoom;
                    my $oldJobDir = "";

                    # wait of the global destroy flag or of the singel stop flag
                    while ( !$self->{SHARED}->{DESTROYED} )
                    {

                        # create new tileset for a new job
                        # TODO: bad way export some funktions to an external .pl file
                        $self->{'rendererSemaphore'}->down();
                        if ( $oldJobDir ne $self->{SHARED}->{JOBDIR} && $self->{SHARED}->{JOBDIR} ne "" )
                        {
                            my $req = new Request;
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

                        while ( !$self->{SHARED}->{CHILDSTOP}->[$childID]
                            && $oldJobDir eq $self->{SHARED}->{JOBDIR}
                            && $self->{SHARED}->{RENDERERJOBSPOS} < $#{ $self->{SHARED}->{RENDERERJOBS} } )
                        {

                            # access: lock()
                            $self->{'rendererSemaphore'}->down();

                            $self->{SHARED}->{RENDERERJOBSPOS}++;
                            $pos           = $self->{SHARED}->{RENDERERJOBSPOS};
                            $zoom          = $self->{SHARED}->{RENDERERJOBS}->[$pos];
                            $layer         = $self->{SHARED}->{RENDERERJOBLAYER}->[$pos];
                            $layerDataFile = $self->{SHARED}->{RENDERERJOBLAYERDATA}->[$pos];

                            # access: unlock()
                            $self->{'rendererSemaphore'}->up();

                            ####
                            # i do my work now
                            ####
                            if ( !$self->rendererError() )
                            {
                                ::statusMessage(
                                    "Rendererclient $childID get job $pos zoom $zoom on layer $layer $layerDataFile",
                                    1, 10 );
                                eval { $self->{tileset}->Render( $layer, $zoom, $layerDataFile ); };
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

                        sleep 1;
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

    my $zoom          = shift;
    my $layer         = shift;
    my $layerDataFile = shift;

    $self->{'rendererSemaphore'}->down();

    my $pos = $#{ $self->{SHARED}->{RENDERERJOBS} };
    $pos++;

    $self->{SHARED}->{RENDERERJOBS}->[$pos]         = $zoom;
    $self->{SHARED}->{RENDERERJOBLAYER}->[$pos]     = $layer;
    $self->{SHARED}->{RENDERERJOBLAYERDATA}->[$pos] = $layerDataFile;

    $self->{'rendererSemaphore'}->up();

    $self->updateMaxRenderer($layerDataFile);    #TODO: move it to the svg generation after init new workflow
}

sub wait
{
    my $self = shift;

    while ( $self->{SHARED}->{RENDERERJOBSREADY} <= $#{ $self->{SHARED}->{RENDERERJOBS} } )
    {
        sleep 1;
    }
}

# reset my lists
sub Reset
{
    my $self = shift;

    $self->{'rendererSemaphore'}->down();

    $::GlobalChildren->{SHARED}->{STOPALL} = 0;

    undef @{ $self->{SHARED}->{RENDERERJOBS} };
    undef @{ $self->{SHARED}->{RENDERERJOBLAYER} };
    undef @{ $self->{SHARED}->{RENDERERJOBLAYERDATA} };
    undef @{ $self->{SHARED}->{RENDERERJOBERRORS} };
    $self->{SHARED}->{RENDERERJOBERROR}  = "";
    $self->{SHARED}->{RENDERERJOBSPOS}   = -1;
    $self->{SHARED}->{RENDERERJOBSREADY} = 0;
    $self->{SHARED}->{MAXSVGFILESIZE}    = 0;

    # reset renderer limitation
    for ( my $i = $self->{'maxChildren'} ; $i > 0 ; $i-- )
    {
        $self->{SHARED}->{CHILDSTOP}->[$i] = 0;
    }

    $self->{'rendererSemaphore'}->up();
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
#TODO: use svg files and not the osm files for calculate
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

    my $caMemoryUsage = $self->{SHARED}->{MAXSVGFILESIZE} / 1024 / 16;

    if ( ( $caMemoryUsage * $self->{'maxChildren'} ) > $Config->get("MaxMemory") )
    {

        # too little memory
        my $newMaxChildren = int( $Config->get("MaxMemory") / $caMemoryUsage );
        $newMaxChildren = 1 if $newMaxChildren < 1;

        ::statusMessage(
            "too little memory for the render job and "
              . $self->{'maxChildren'}
              . " Children, stopp "
              . ( $self->{'maxChildren'} - $newMaxChildren )
              . " renderer childs from "
              . $self->{'maxChildren'},
            1, 10
        );

        $self->{'rendererSemaphore'}->down();
        for ( my $stopCount = ( $self->{'maxChildren'} - $newMaxChildren ) ; $stopCount > 0 ; $stopCount-- )
        {
            $self->{SHARED}->{CHILDSTOP}->[$stopCount] = 1;

        }
        $self->{'rendererSemaphore'}->up();
    }
}

1;
