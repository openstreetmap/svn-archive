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

                                    eval { $self->optimizePngClient( $png_file, $transparent ); };
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

#-----------------------------------------------------------------------------
# optimize a PNG file
#
# Parameters:
#   $png_file - file name of PNG file
#   $transparent - whether or not this is a transparent tile
#-----------------------------------------------------------------------------
sub optimizePngClient
{
    my $self        = shift();
    my $png_file    = shift();
    my $transparent = shift();

    my $Config = $self->{Config};

    my $optipngOptions = "-l 9";

    my $redirect   = ( $^O eq "MSWin32" ) ? "" : ">/dev/null";
    my $tmp_suffix = '.cut';
    my $tmp_file   = $png_file . $tmp_suffix;
    my ( undef, undef, $png_file_name ) = File::Spec->splitpath($png_file);

    my $cmd;
    if ($transparent)
    {

        # Don't quantize if it's transparent
        rename( $png_file, $tmp_file );
    }
    elsif ( ( $Config->get("PngQuantizer") || '' ) eq "pngnq" )
    {
        $cmd = sprintf( "\"%s\" -e .png%s -s1 -n256 %s %s", $Config->get("pngnq"), $tmp_suffix, $png_file, $redirect );

        ::statusMessage( "ColorQuantizing $png_file_name", 0, 6 );
        if ( ::runCommand( $cmd, $::PID ) )
        {

            # Color quantizing successful
            unlink($png_file);
        }
        else
        {

            # Color quantizing failed
            ::statusMessage( "ColorQuantizing $png_file_name with " . $Config->get("PngQuantizer") . " failed", 1, 0 );
            rename( $png_file, $tmp_file );
        }
    }
    else
    {
        ::statusMessage( "Not Color Quantizing $png_file_name, pngnq not installed?", 0, 6 );
        rename( $png_file, $tmp_file );
    }

    if ( $Config->get("PngOptimizer") eq "pngcrush" )
    {
        $cmd = sprintf( "\"%s\" %s -q %s %s %s", $Config->get("Pngcrush"), $optipngOptions, $tmp_file, $png_file, $redirect );
    }
    elsif ( $Config->get("PngOptimizer") eq "optipng" )
    {
        $cmd = sprintf(
            "\"%s\" %s -out %s %s",    #no quiet, because it even suppresses error output
            $Config->get("Optipng"),
            $tmp_file,
            $png_file,
            $redirect
        );
    }
    else
    {
        ::statusMessage( "PngOptimizer not configured (should not happen, update from svn, and check config file)", 1, 0 );
        ::talkInSleep( "Install a PNG optimizer and configure it.", 15 );
    }

    ::statusMessage( "Optimizing $png_file_name", 0, 6 );
    if ( ::runCommand( $cmd, $::PID ) )
    {
        unlink($tmp_file);
    }
    else
    {
        ::statusMessage( "Optimizing $png_file_name with " . $Config->get("PngOptimizer") . " failed", 1, 0 );
        rename( $tmp_file, $png_file );
    }
}

1;
