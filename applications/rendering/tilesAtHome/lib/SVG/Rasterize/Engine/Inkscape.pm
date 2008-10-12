package SVG::Rasterize::Engine::Inkscape;

use strict;
use warnings;

$__PACKAGE__::VERSION = '0.1';

use base qw(Class::Accessor SVG::Rasterize::Engine);
use SVG::Rasterize::CoordinateBox;
use File::Spec;
use Error qw(:try);
use IPC::Run qw(run);

=pod
=head1 NAME

SVG::Rasterize::Engine::Inkscape -- Inkscape engine for SVG::Rasterize

=head1 DESCRIPTION

This module is only meant to be used by SVG::Rasterize.

=head1 ACCESSORS

=cut

__PACKAGE__->mk_accessors(qw(path searchpaths));

=pod

=head2 path

Path to inkscape executable. Will try to search for it automatically
unless it's set to something first.

Throws: Error::Simple("Couldn't find inkscape executable") if it searched
for the executable but couldn't find it.

=cut

sub path {
    my $self = shift;

    unless( @_ || $self->{path} ){ # We're getting and don't have a defined path
        foreach my $path ( @{ $self->searchpaths } ){
            my($volume, $dir) = File::Spec->splitpath($path, 1);

            foreach my $name ( 'inkscape', 'inkscape.exe' ){
                my $filepath = File::Spec->catpath($volume, $dir, $name);
                return $self->path($filepath) if -x $filepath;
            }
        }
        throw SVG::Rasterize::Engine::Inkscape::Error::Prerequisite("Couldn't find inkscape executable");
    }

    return $self->_path_accessor(@_);
}

=pod

=head1 METHODS

=head2 new(\%params) (constructor)

Create a new instance of this class. You can pass in parameters which
will then be set via their accessor

Returns: new instance of this class.

=cut

sub new {
    my ( $pkg, $params ) = @_;
    my $class = ref $pkg || $pkg;
    my $self = bless( {}, $class);

    # Defaults
    my @default_searchpaths = ( File::Spec->path() );
    if( $^O eq 'MSWin32' ){
        my($volume, $dir) = File::Spec->splitpath($ENV{PROGRAMFILES}, 1);
        $dir = File::Spec->catdir( $dir, 'Inkscape' );
        push(@default_searchpaths, File::Spec->catpath($volume, $dir) );

        push(@default_searchpaths, File::Spec->catpath( $ENV{HOMEDRIVE}, 'Inkscape' ));
    }

    $self->searchpaths(@default_searchpaths);

    foreach my $param ( keys(%$params) ){
        $self->$param( $params->{$param} );
    }

    return $self;
}

=pod

=head2 available()

Try to see if this engine can be used.

=cut

sub available {
    my $self = shift;

    try {
        my $path = $self->path();
        return -x $path;
    }
    otherwise {
        return 0;
    };
}

=pod

=head2 convert( \%params )

C<\%params> is a hash as described in SVG::Rasterize

Throws: Error::Simple("Inkscape returned non-zero status code X") with
an anonymous hash with stdout and stderr as keys in value.
Also calls C<path()> so see it for another possible exception.

=cut

sub convert {
    my $self = shift;
    my %params = @_;

    # Workaround for locale-related problems
    #FIXME: I think there's something about this needing to be set to the system locale on windows
    local $ENV{LC_NUMERIC} = 'C';

    # Make sure Inkscape can't find any X display.
    # Probably not possible to do anything like this on Windows, which is sad
    # because inkscape will then sometimes throw errors in a dialog box instead
    # of exiting and showing us something on stdout/stderr
    local $ENV{DISPLAY} = '';

    my %area;
    if( $params{area} ){
        %area = $params{area}->get_box_lowerleft();
    }

    my @cmd = ($self->path(), '-z');
    push(@cmd, '-w', $params{width}) if $params{width};
    push(@cmd, '-h', $params{height}) if $params{height};
    push(@cmd, sprintf('--export-area=%f:%f:%f:%f',
                       @area{'left','bottom','right','top'})
        ) if %area;
    push(@cmd, '--export-png='.$params{outfile});
    push(@cmd, $params{infile});

    run( \@cmd, \undef, \$self->{stdout}, \$self->{stderr} ) or
        throw SVG::Rasterize::Engine::Inkscape::Error::Runtime("Inkscape returned non-zero status code $?", {stdout => $self->{stdout}, stderr => $self->{stderr}});

    $self->check_output($params{outfile});
}

package SVG::Rasterize::Engine::Inkscape::Error;
use base qw(SVG::Rasterize::Engine::Error);

package SVG::Rasterize::Engine::Inkscape::Error::Runtime;
use base qw(SVG::Rasterize::Engine::Inkscape::Error SVG::Rasterize::Engine::Error::Runtime);

package SVG::Rasterize::Engine::Inkscape::Error::Prerequisite;
use base qw(SVG::Rasterize::Engine::Inkscape::Error SVG::Rasterize::Engine::Error::Prerequisite);

1;

__END__

=pod

=head1 TO DO

Update exception docs.

Have a look at the new --shell mode

=head1 BUGS

Contact me if you find any.

=head1 COPYRIGHT

Same as Perl.

=head1 AUTHORS

Knut Arne Bjørndal <bob@cakebox.net>

Partially based on code from OpenStreetMap Tiles@Home, copyright 2006
Oliver White, Etienne Cherdlu, Dirk-Lueder Kreie, Sebastian Spaeth
and others

=cut
