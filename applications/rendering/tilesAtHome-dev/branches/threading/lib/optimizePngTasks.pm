package optimizePngTasks;

use warnings;
use strict;
use File::Temp qw/ tempfile tempdir /;
use File::Copy;
use File::Path;
use Error qw(:try);
use TahConf;
use tahlib;
use threads;
use Thread::Semaphore;
use Time::HiRes qw ( sleep );

use Tileset;

sub new
{
    my $class  = shift;
    my $Config = TahConf->getConfig();

    my %sharedStack : shared;
    my @sharedFilelist : shared;
    my @sharedTranslist : shared;
    my $self = {
        Config    => $Config,
        SHARED    => \%sharedStack,
        DESTROYED => 0,
        children  => [],
    };

    $self->{SHARED}->{DESTROYED}       = 0;
    $self->{SHARED}->{JOBS}            = -1;
    $self->{SHARED}->{JOBSREADY}       = 0;
    $self->{SHARED}->{JOBSFILES}       = \@sharedFilelist;
    $self->{SHARED}->{JOBSTRANSPARENT} = \@sharedTranslist;
    $self->{SHARED}->{CHILDCRASH}      = 0;                   # TODO:

    bless $self, $class;
    return $self;
}

sub DESTROY
{

}

########
# kill all children threads
########
sub killAllChilds
{
    my $self = shift;

    # set the destroy flag (detached childrens!)
    $self->{'Semaphore'}->down();
    $self->{SHARED}->{DESTROYED} = 1;
    $self->{'Semaphore'}->up();

}

##########
# start and init my children
##########
sub startChildren
{
    my $self   = shift;
    my $Config = $self->{Config};

    $self->{tileset} = new Tileset( "", "2" );

    if ( $Config->get("Cores") )
    {
        $self->{'maxChildren'} = $Config->get("Cores");
        $self->{'children'}    = [];
        $self->{'Semaphore'}   = Thread::Semaphore->new();

        $::currentSubTask  = 'optimize';
        $::progressPercent = 0;
        ::statusMessage( "init " . $self->{'maxChildren'} . " optimizePNG Child Tasks", 0, 6 );
        $self->{SHARED}->{'progress'} = 0;
        for my $childID ( 1 .. $self->{'maxChildren'} )
        {
            $self->{'children'}->[$childID] = threads->create(
                sub {

                    threads->detach();

                    my $sleeping = 0;
                    while ( !$self->{SHARED}->{DESTROYED} )
                    {

                        sleep(0.1);

                        while ( $self->notStopped() && $self->{SHARED}->{JOBS} < $#{ $self->{SHARED}->{JOBSFILES} } )
                        {
                            my ( $png_file, $transparent ) = "";

                            # access: lock()
                            $self->{'Semaphore'}->down();

                            # get an other child the last job?
                            if ( $self->{SHARED}->{JOBS} < $#{ $self->{SHARED}->{JOBSFILES} } )
                            {
                                $self->{SHARED}->{JOBS}++;
                                my $pos = $self->{SHARED}->{JOBS};

                                $png_file    = $self->{SHARED}->{JOBSFILES}->[$pos];
                                $transparent = $self->{SHARED}->{JOBSTRANSPARENT}->[$pos];

                                $self->{SHARED}->{'progress'}++;
                                if ( $#{ $self->{SHARED}->{JOBSFILES} } > 0 )
                                {
                                    $::progressPercent = 100 * $self->{SHARED}->{'progress'} / ( $#{ $self->{SHARED}->{JOBSFILES} } + 1 );
                                }
                            }

                            # access: unlock()
                            $self->{'Semaphore'}->up();

                            if ($png_file)
                            {
                                if ( $self->notStopped() )
                                {
                                    #####
                                    # lets do my work now
                                    #####

                                    eval { $self->{tileset}->optimizePng( $png_file, $transparent ); };

                                    if ($@)
                                    {
                                        ::statusMessage( "ERROR: optimizePng return $@", 1, 10 );
                                    }

                                }

                                $self->{'Semaphore'}->down();
                                $self->{SHARED}->{JOBSREADY}++;
                                $self->{'Semaphore'}->up();
                            }
                        }

                    }
                    ::statusMessage( "optimizePNG child $childID exit", 1, 10 );

                }
            );    # threads->create(sub) end
        }    # for

    }

}    # sub startChildren

# add a new job ::addJob->($png_file,$transparent)
sub addJob
{
    my $self        = shift;
    my $png_file    = shift;
    my $transparent = shift;

    return if ( $png_file eq "" );

    $self->{'Semaphore'}->down();

    my $pos = $#{ $self->{SHARED}->{JOBSFILES} } + 1;

    $self->{SHARED}->{JOBSFILES}->[$pos]       = $png_file;
    $self->{SHARED}->{JOBSTRANSPARENT}->[$pos] = $transparent;

    $self->{'Semaphore'}->up();
}

# wait of all my jobs
sub wait
{
    my $self = shift;

    ::statusMessage( "Wait of my PNG optimize Children", 0, 6 );

    while ( $self->{SHARED}->{JOBSREADY} <= $#{ $self->{SHARED}->{JOBSFILES} } )
    {
        sleep(0.1);
    }

}

# reset my lists
sub dataReset
{
    my $self = shift;

    $self->{'Semaphore'}->down();

    $self->{SHARED}->{JOBS}      = -1;
    $self->{SHARED}->{JOBSREADY} = 0;
    undef @{ $self->{SHARED}->{JOBSFILES} };
    undef @{ $self->{SHARED}->{JOBSTRANSPARENT} };

    $self->{SHARED}->{'progress'} = 0;

    $self->{'Semaphore'}->up();
}

sub notStopped
{

    if ( $::GlobalChildren->{SHARED}->{STOPALL} )
    {
        return 0;
    }

    return 1;
}

1;
