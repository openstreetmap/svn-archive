#-----------------------------------------------------------------------------
#
#  Tirex/Backend.pm
#
#-----------------------------------------------------------------------------

use strict;
use warnings;

use File::Path;
use Time::HiRes;
use Socket;
use IO::Socket;
use GD;

use Tirex::Renderer;
use Tirex::Map;

#-----------------------------------------------------------------------------

package Tirex::Backend;

=head1 NAME

Tirex::Backend - Generic Tirex rendering backend

=head1 SYNOPSIS

 use Tirex::Backend::Test;
 my $backend = Tirex::Backend::Test->new($0);
 $backend->main();

=head1 DESCRIPTION

This is a parent class for rendering backends written in Perl. To use it create
a subclass (such as Tirex::Backend::Test).

=cut

my $keep_running;

=head1 METHODS

=head2 Tirex::Backend->new($name)

This class should not be instantiated. Create instances of a subclass instead.

=cut

sub new
{
    my $class = shift;
    my $self = bless {} => $class;

    $self->{'name'} = shift;

    $self->init();

    return $self;
}

=head2 $backend->main()

Core backend method. Call this directly after creating a subclass with new().
It will parse the config file(s), set everything up and then wait for rendering
requests and fullfill them by calling create_metatile().

=cut

sub main
{
    my $self = shift;

    my $renderer_name   = $ENV{'TIREX_RENDERD_NAME'}            or $self->error_disable('missing TIREX_RENDERD_NAME');
    my $port            = $ENV{'TIREX_RENDERD_PORT'}            or $self->error_disable('missing TIREX_RENDERD_PORT');
    my $syslog_facility = $ENV{'TIREX_RENDERD_SYSLOG_FACILITY'} or $self->error_disable('missing TIREX_RENDERD_SYSLOG_FACILITY');
    my $mapfiles        = $ENV{'TIREX_RENDERD_MAPFILES'}        or $self->error_disable('missing TIREX_RENDERD_MAPFILES');
    my $debug           = $ENV{'TIREX_RENDERD_DEBUG'}           or $self->error_disable('missing TIREX_RENDERD_DEBUG');
    my $pipe_fileno     = $ENV{'TIREX_RENDERD_PIPE_FILENO'}     or $self->error_disable('missing TIREX_RENDERD_PIPE_FILENO');
    my $alive_timeout   = $ENV{'TIREX_RENDERD_ALIVE_TIMEOUT'}   or $self->error_disable('missing TIREX_RENDERD_ALIVE_TIMEOUT');

    my @mapfiles = split(' ', $mapfiles);

    #-----------------------------------------------------------------------------

    ::openlog($self->{'name'}, $debug ? 'pid|perror' : 'pid', $syslog_facility);
    ::syslog('info', 'Renderer started (name=%s)', $renderer_name);

    my $pipe = IO::Handle->new();
    $pipe->fdopen($pipe_fileno, 'w');
    $pipe->autoflush(1);

    my $renderer = Tirex::Renderer->new( type => $self->type(), name => $renderer_name, port => $port, path => $0, procs => 0 );

    foreach my $file (@mapfiles)
    {
        my $map = Tirex::Map->new_from_configfile($file);
        ::syslog('info', 'map config found: %s', $map->to_s());
    }

    #-----------------------------------------------------------------------------

    my $socket = IO::Socket::INET->new(
        LocalAddr => 'localhost', 
        LocalPort => $port, 
        Proto     => 'udp', 
        ReuseAddr => 1,
    ) or $self->error_disable("Cannot open UDP socket: :$!");

    $SIG{'HUP'}  = \&Tirex::Backend::signal_handler;
    $SIG{'TERM'} = \&Tirex::Backend::signal_handler;
    $SIG{'INT'}  = \&Tirex::Backend::signal_handler;
    $SIG{'ALRM'} = \&Tirex::Backend::sigalrm_signal_handler;

    $keep_running = 1;

    while ($keep_running)
    {
        # send keepalive to parent
        $pipe->write('a', 1);    

        alarm($alive_timeout);

        # this will block waiting for new commands on socket
        # if a signal comes in (ALRM or from parent) it will return with EINTR
        my $msg = Tirex::Message->new_from_socket($socket);
        if (! $msg)
        {
            next if ($!{'EINTR'});
            $self->error_restart("error reading from socket: $!");
        }

        alarm(0);

        ::syslog('debug', 'got request: %s', $msg->to_s());

        my $map = Tirex::Map->get($msg->{'map'});

        if ($map)
        {
            my $metatile  = $msg->to_metatile();
            my $filename  = $map->get_tiledir() . '/' . $metatile->get_filename();

            ::syslog('debug', 'doing rendering (filename=%s)', $filename);
            my $t0 = [Time::HiRes::gettimeofday];
            my $image = $self->create_metatile($map, $metatile);
            $self->write_metatile($image, $filename, $metatile);

            $msg = $msg->reply();
            $msg->{'render_time'} = int(Time::HiRes::tv_interval($t0) * 1000); # in milliseconds

            ::syslog('debug', 'sending response: %s', $msg->to_s());
        }
        else
        {
            ::syslog('err', 'unknown map: %s', $msg->{'map'});
            $msg = $msg->reply('ERROR_UNKNOWN_MAP', "The map " . $msg->{'map'} . " is unknown to renderer " . $renderer_name);
        }
        $msg->send($socket) or $self->error_restart("error when sending: $!");

        ::syslog('debug', 'done with request');
    }

    ::syslog('info', 'shutting down %s', $self->{'name'});
}

=head2 $backend->create_metatile($map, $metatile)

Create a metatile.

This method has to be overwritten in subclasses.

=cut

sub create_metatile
{
    my $self     = shift;
    my $map      = shift;
    my $metatile = shift;

    Carp::croak('Overwrite create_metatile() method in subclass!');
}


=head2 $backend->write_metatile($image, $filename, $metatile)

Takes a single image the size of a metatile, cuts it into tiles and then
re-assembles those tiles into a metatile and write it to disk.

=cut

sub write_metatile
{
    my $self     = shift;
    my $image    = shift;
    my $filename = shift;
    my $metatile = shift;

    # metatile header
    my $meta = 'META' . pack('llll', $Tirex::METATILE_COLUMNS * $Tirex::METATILE_ROWS,
                                     $metatile->get_x(),
                                     $metatile->get_y(),
                                     $metatile->get_z());

    # cut metatile into tiles and create pngs for each
    my @pngs = ();
    foreach my $x (0..$Tirex::METATILE_COLUMNS-1)
    {
        foreach my $y (0..$Tirex::METATILE_ROWS-1)
        {
            my $tile = GD::Image->new($Tirex::PIXEL_PER_TILE, $Tirex::PIXEL_PER_TILE);
            $tile->copy($image, 0, 0, $x * $Tirex::PIXEL_PER_TILE, $y * $Tirex::PIXEL_PER_TILE, $Tirex::PIXEL_PER_TILE, $Tirex::PIXEL_PER_TILE);
            push(@pngs, $tile->png());
        }
    }

    # calculate and store byte offsets for each tile
    my $offset = length($meta) + ($Tirex::METATILE_COLUMNS * $Tirex::METATILE_ROWS * 2 * 4); # header + (number of tiles * (start offset and length) * 4 bytes for int32)
    foreach my $png (@pngs)
    {
        my $l = length($png);
        $meta .= pack('ll', $offset, $l);
        $offset += $l;
    }

    # add pngs to metatile
    $meta .= join('', @pngs);

    # check for directory and create if missing
    (my $dirname = $filename) =~ s{/[^/]*$}{};
    if (! -d $dirname)
    {
        File::Path::mkpath($dirname) or $self->error_disable("Can't create path $dirname: $!");
    }

    open(METATILE, '>', $filename) or $self->error_disable("Can't open $filename: $!");
    binmode(METATILE);
    print METATILE $meta;
    close(METATILE);
}

=head2 $backend->create_error_image($map, $metatile)

Create an error image in case a renderer didn't work. The error image is a
black/yellow checkerboard pattern.

This method can be overwritten in subclasses.

=cut

sub create_error_image
{
    my $self     = shift;
    my $map      = shift;
    my $metatile = shift;

    my $image = GD::Image->new($Tirex::PIXEL_PER_TILE * $Tirex::METATILE_COLUMNS,
                               $Tirex::PIXEL_PER_TILE * $Tirex::METATILE_ROWS);

    my $yellow = $image->colorAllocate(255, 255, 0);
    my $black  = $image->colorAllocate(  0,   0, 0);
    my @color  = ($yellow, $black);

    foreach my $x (0..2*$Tirex::METATILE_COLUMNS-1)
    {
        foreach my $y (0..2*$Tirex::METATILE_ROWS-1)
        {
            my $xpixel = $x * $Tirex::PIXEL_PER_TILE / 2;
            my $ypixel = $y * $Tirex::PIXEL_PER_TILE / 2;

            my $color_offset = ($x+$y) % 2;

            $image->filledRectangle($xpixel, $ypixel, $xpixel + $Tirex::PIXEL_PER_TILE - 1, $ypixel + $Tirex::PIXEL_PER_TILE - 1, $color[$color_offset]);
        }
    }

    return $image;
}

sub error_restart
{
    my $self = shift;

    ::syslog('err', @_);

    exit($Tirex::EXIT_CODE_RESTART);
}

sub error_disable
{
    my $self = shift;

    ::syslog('err', @_);

    exit($Tirex::EXIT_CODE_DISABLE);
}

#-----------------------------------------------------------------------------

sub signal_handler 
{
    $keep_running = 0;
    $SIG{'HUP'}  = \&signal_handler;
    $SIG{'INT'}  = \&signal_handler;
    $SIG{'TERM'} = \&signal_handler;
}

sub sigalrm_signal_handler
{
    $SIG{'ALRM'} = \&sigalrm_signal_handler;
}

#-----------------------------------------------------------------------------


1;

#-- THE END ------------------------------------------------------------------
