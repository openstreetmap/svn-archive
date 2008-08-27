package SVG::Rasterize::Engine::BatikAgent;

use strict;
use warnings;

$__PACKAGE__::VERSION = '0.1';

use base qw(Class::Accessor SVG::Rasterize::Engine);
use Error qw(:try);

=pod
=head1 NAME

SVG::Rasterize::Engine::BatikAgent -- Batik-agent engine for SVG::Rasterize

=head1 DESCRIPTION

This module is only meant to be used by SVG::Rasterize.

=head1 ACCESSORS

=cut

__PACKAGE__->mk_accessors(qw(agent_path java_path host port heapsize classpath));

=pod

=head2 

Path to Batik JAR

=cut

sub agent_path {
    my $self = shift;

    unless( @_ || $self->{jar_path} ){ # We're getting and don't have a defined path
        foreach my $path ( File::Spec->path(), File::Spec->curdir() ){
            my($volume, $dir) = File::Spec->splitpath($path, 1);

            foreach my $name ( 'batik-agent.jar' ){
                my $filepath = File::Spec->catpath($volume, $dir, $name);
                return $self->jar_path($filepath) if -x $filepath;
            }
        }
        throw SVG::Rasterize::Engine::Batik::Error::Prerequisite("Couldn't find batik-agent.jar");
    }

    return $self->_agent_path_accessor(@_);
}

=pod

=head2 java_path

Path to java executable

=cut

sub java_path {
    my $self = shift;

    unless( @_ && $self->{java_path} ){ # We're getting and don't have a defined path
        foreach my $path ( File::Spec->path() ){
            my($volume, $dir) = File::Spec->splitpath($path, 1);

            foreach my $name ( 'java', 'java.exe' ){
                my $filepath = File::Spec->catpath($volume, $dir, $name);
                return $self->java_path($filepath) if -x $filepath;
            }
        }
        throw SVG::Rasterize::Engine::Batik::Error::Prerequisite("Couldn't find Java executable");
    }

    return $self->_java_path_accessor(@_);
}

=pod

=head1 METHODS

=cut

=pod

=head2 available()

Try to see if this engine can be used.

=cut

sub available {
    my $self = shift;

    try {
        return 1 if -r $self->agent_path() && -x $self->java_path();
    } catch SVG::Rasterize::Engine::Batik::Error::Prerequisite with {
        return 0;
    };

    return 0;
}

=pod

=head2 convert( \%params )

C<\%params> is a hash as described in SVG::Rasterize.

=cut

sub convert {
    my $self = shift;
    my %params = @_;

    my @cmd;

    @cmd = ('svg2png');
    push(@cmd, 'width='.$params{width}) if $params{width};
    push(@cmd, 'height='.$params{height}) if $params{height};
    push(@cmd, sprintf('area=%f,%f,%f,%f',
                       $params{left},
                       $params{top},
                       $params{right} - $params{left},
                       $params{top} - $params{bottom})
        ) if $params{left} && $params{bottom} && $params{right} && $params{top};
    push(@cmd, 'destination='.$params{outfile});
    push(@cmd, 'source='.$params{infile});

    my $result = $self->send_command( join("\n", @cmd) . "\n\n" );

    unless( $result eq 'OK' ){
        throw SVG::Rasterize::Engine::BatikAgent::Error::Runtime("Batik agent returned non-OK result \"$result\"");
    }
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
        push(@cmd, qw(start /B /LOW));
    }

    push(@cmd, $self->java_path());
    push(@cmd, '-Xms256M');
    push(@cmd, '-Xmx'.$self->heapsize()) if $self->heapsize();
    push(@cmd, '-cp', $self->classpath()) if $self->classpath();
    push(@cmd, 'org.tah.batik.ServerMain');
    push(@cmd, '-p', $self->port());

    my $pid;
    # On windows "start" will take care of the forking
    if( $^O eq "MSWin32" ){
        $pid = 0;
    } else {
        $pid = fork;
    }

    if( ! defined($pid) ){
        throw SVG::Rasterize::Engine::BatikAgent::Error::Runtime("Error forking before starting batik agent");
    } elsif( $pid == 0 ){
        run( \@cmd, \undef, \undef, \undef ) or
            throw SVG::Rasterize::Engine::BatikAgent::Error::Runtime($cmd[0]." returned non-zero status code $?");
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

    return unless $self->get_status();

    $self->send_command("stop\n\n");
}

=pod

=head2 send_command( $command )

Sends a command to the agent.

=cut

sub send_command {
    my $self = shift;
    my $command = shift;

    my $sock = new IO::Socket::INET( PeerAddr => $self->host(), PeerPort => $self->port(), Proto => 'tcp');
    throw SVG::Rasterize::Engine::BatikAgent::Error::Runtime::IOError("Error creating socket to the batik agent") unless $sock;

    print $sock $command;
    flush $sock;
    my $reply = <$sock>;
    $reply =~ s/\n//;
    close($sock);

    return $reply;
}

=pod

=head2 get_status()

Get agent status

=cut

sub get_status {
    my $self = shift;

    try {
        return 1 if send_command("status\n\n") eq "OK";
    } otherwise {
        # Just catch all errors and return false
    }

    return 0;
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
