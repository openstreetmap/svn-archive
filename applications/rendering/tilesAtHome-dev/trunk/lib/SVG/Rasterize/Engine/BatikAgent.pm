package SVG::Rasterize::Engine::BatikAgent;

use strict;
use warnings;

$__PACKAGE__::VERSION = '0.1';

use base qw(SVG::Rasterize::Engine::Batik);
use Error qw(:try);
use IO::Socket::INET;
use IPC::Run qw(run);

=pod
=head1 NAME

SVG::Rasterize::Engine::BatikAgent -- Batik-agent engine for SVG::Rasterize

=head1 DESCRIPTION

This module is only meant to be used by SVG::Rasterize.

=head1 ACCESSORS

=head2 host

=head2 port

=head2 heapsize

=cut

__PACKAGE__->mk_accessors(qw(host port heapsize));

=pod

=head1 METHODS

=head2 new(\%params) (constructor)

Create a new instance of this class. You can pass in parameters which
will then be set via their accessor

Returns: new instance of this class.

=cut

sub new {
    my ( $pkg, $params ) = @_;
    my $self = $pkg->SUPER::new($params);

    # Append batik-agent to the jar list
    $self->jar_list('batik-agent.jar', @{$self->jar_list()});

    # Set defaults for host and port
    $self->host('localhost') unless $self->host;
    $self->port('18123') unless $self->port;

    return $self;
}

=pod

=cut

# Not used in this module, so overridden
sub wrapper_available {
    return 0;
}

=pod

=head2 convert( \%params )

C<\%params> is a hash as described in SVG::Rasterize.

=cut

sub convert {
    my $self = shift;
    my %params = @_;

    my %area;
    if( $params{area} ){
        %area = %{ $params{area} }; # Make a copy
        $area{width} = abs($area{left} - $area{right});
        $area{height} = abs($area{top} - $area{bottom});
    }

    my @cmd = ('svg2png');
    push(@cmd, 'width='.$params{width}) if $params{width};
    push(@cmd, 'height='.$params{height}) if $params{height};
    push(@cmd, sprintf('area=%f,%f,%f,%f',
                       @area{'left','top','width','height'})
        ) if %area;
    push(@cmd, 'destination='.$params{outfile});
    push(@cmd, 'source='.$params{infile});

    print STDERR __PACKAGE__.": About to send:\n" . join("\n", @cmd) . "\n" if $self->rasterizer->debug;

    my $reply = $self->send_command( join("\n", @cmd) . "\n\n" );
    my ($result) = $reply =~ /^(\w+)/;
    unless( $result eq 'OK' ){
        throw SVG::Rasterize::Engine::BatikAgent::Error::Runtime("Batik agent returned non-OK result \"$result\"", { cmd => \@cmd, stdout => $reply } );
    }

    if( $self->rasterizer->debug() ){
        print STDERR __PACKAGE__."Returned result '$result'.\n";
        print STDERR __PACKAGE__."BatikAgent reply:\n$self->{stdout}\n";
    }

    try {
        $self->check_output($params{outfile});
    } catch SVG::Rasterize::Engine::Error::NoOutput with {
        # Add extra information about the rasterizer run and rethrow exception
        my $e = shift;
        $e->{cmd} = \@cmd;
        $e->{stdout} = $reply;
        $e->throw;
    };
}

=pod

=head2 start_agent()

Starts batik agent.

Returns true if it started sucessfully, 0 if it was already running and throws
an exception if something failed.

=cut

sub start_agent {
    my $self = shift;

    if( $self->get_status() ){
        return 0;
    }

    my @cmd;

    if( $^O eq "MSWin32" ){
        #FIXME: this is apparently an internal command in cmd.exe so we can't use it.
        #Find some other way of setting a low priority.
        #push(@cmd, qw(start /B /LOW));
    }

    push(@cmd, $self->java_path());
    push(@cmd, '-Xms256M');
    push(@cmd, '-Xmx'.$self->heapsize()) if $self->heapsize();
    push(@cmd, '-classpath', join( ($^O eq 'MSWin32' ? ';' : ':'),
                                   $self->find_jars( @{$self->jar_list()} )));
    push(@cmd, 'org.tah.batik.ServerMain');
    push(@cmd, '-p', $self->port());

    my $pid;
    if( $^O eq "MSWin32" ){
        # On windows "start" will take care of the forking
        $pid = 0;
    } else {
        # On *nix systems we daemonize
        $pid = fork;
        if( ! defined($pid) ){
            throw SVG::Rasterize::Engine::BatikAgent::Error::Runtime("Error forking before starting batik agent");
        } elsif( $pid == 0 ){
            setpgrp;
            close(STDIN);
            close(STDOUT);
            close(STDERR);
        }
    }

    if( $pid == 0 ){
        exec(@cmd) or
            throw SVG::Rasterize::Engine::BatikAgent::Error::Runtime("Error exec'ing \"$cmd[0]\": $!");
    }

    if( $^O eq "MSWin32" || $pid ){
        foreach( 0 .. 10 ){
            sleep(1);

            if( $self->get_status() ){
                return "BatikAgent started successfully";
            }
        }
    }

    throw SVG::Rasterize::Engine::BatikAgent::Error::Runtime("Timeout waiting for batik agent to start");
}

=pod

=head2 stop_agent()

Sends a stop command to the batik agent.

=cut

sub stop_agent {
    my $self = shift;

    return 0 if ! $self->get_status(); #FIXME: maybe this should throw an exception instead?

    my $answer = $self->send_command("stop\n\n");
    if( $answer eq 'OK' ){
        return 1;
    } else {
        return "Error: batik agent replied $answer";
    }
}

=pod

=head2 send_command( $command )

Sends a command to the agent.

=cut

sub send_command {
    my $self = shift;
    my $command = shift;

    my $sock = new IO::Socket::INET( PeerAddr => $self->host(), PeerPort => $self->port(), Proto => 'tcp');
    throw SVG::Rasterize::Engine::BatikAgent::Error::Runtime::IOError("Error creating socket to the batik agent: $!") unless $sock;

    print $sock $command;
    flush $sock;
    my $reply = join('', <$sock>);
    chomp($reply);
    close($sock);

    return $reply;
}

=pod

=head2 get_status()

Get agent status

=cut

sub get_status {
    my $self = shift;

    my $result;
    try {
        $result = $self->send_command("status\n\n");
    } otherwise {
        # We just ignore exceptions
    };

    if( defined($result) && $result eq 'OK' ){
        return 1;
    } else {
        return 0;
    }
}

package SVG::Rasterize::Engine::BatikAgent::Error;
use base qw(SVG::Rasterize::Engine::Error);

package SVG::Rasterize::Engine::BatikAgent::Error::Runtime;
use base qw(SVG::Rasterize::Engine::BatikAgent::Error SVG::Rasterize::Engine::Error::Runtime);

package SVG::Rasterize::Engine::BatikAgent::Error::Runtime::IOError;
use base qw(SVG::Rasterize::Engine::BatikAgent::Error::Runtime);

package SVG::Rasterize::Engine::BatikAgent::Error::Prerequisite;
use base qw(SVG::Rasterize::Engine::BatikAgent::Error SVG::Rasterize::Engine::Error::Prerequisite);

1;

__END__

=pod

=head1 TO DO

send_command should probably do about three times as much error checking.

This feels a bit dirty the way it currently inherits Batik then overrides
a few bits. Look at cleaner ways of doing it.

Maybe we should handle the autostart/stop on finished logic in here.

=head1 BUGS

Tell me if you find any.

=head1 COPYRIGHT

Same as Perl.

=head1 AUTHORS

Knut Arne Bjørndal <bob@cakebox.net>

Partially based on code from OpenStreetMap Tiles@Home, copyright 2006
Oliver White, Etienne Cherdlu, Dirk-Lueder Kreie, Sebastian Spaeth
and others

=cut
