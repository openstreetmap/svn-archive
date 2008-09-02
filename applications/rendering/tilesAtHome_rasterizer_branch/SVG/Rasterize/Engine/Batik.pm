package SVG::Rasterize::Engine::Batik;

use strict;
use warnings;

$__PACKAGE__::VERSION = '0.1';

use base qw(Class::Accessor SVG::Rasterize::Engine);
use Error;

=pod
=head1 NAME

SVG::Rasterize::Engine::Batik -- Batik engine for SVG::Rasterize

=head1 DESCRIPTION

This module is only meant to be used by SVG::Rasterize.

=head1 ACCESSORS

=cut

__PACKAGE__->mk_accessors(qw(wrapper_path jar_path java_path));

=pod

=head2 wrapper_path

Path to Batik wrapper

=cut

sub wrapper_path {
    my $self = shift;

    unless( @_ || $self->{wrapper_path} ){ # We're getting and don't have a defined path
        foreach my $path ( File::Spec->path() ){
            my($volume, $dir) = File::Spec->splitpath($path, 1);

            foreach my $name ( 'rasterizer', 'rasterizer.exe' ){
                my $filepath = File::Spec->catpath($volume, $dir, $name);
                return $self->wrapper_path($filepath) if -x $filepath;
            }
        }
        throw SVG::Rasterize::Engine::Batik::Error::Prerequisite("Couldn't find batik wrapper");
    }

    return $self->_wrapper_path_accessor(@_);
}

=pod

=head2 

Path to Batik JAR

=cut

sub jar_path {
    my $self = shift;

    unless( @_ || $self->{jar_path} ){ # We're getting and don't have a defined path
        foreach my $path ( File::Spec->path() ){
            my($volume, $dir) = File::Spec->splitpath($path, 1);

            foreach my $name ( 'batik.jar' ){
                my $filepath = File::Spec->catpath($volume, $dir, $name);
                return $self->jar_path($filepath) if -x $filepath;
            }
        }
        throw SVG::Rasterize::Engine::Batik::Error::Prerequisite("Couldn't find batik jar");
    }

    return $self->_jar_path_accessor(@_);
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

=head2 available()

Try to see if this engine can be used.

=cut

sub wrapper_available {
    my $self = shift;

    try {
        return 1 if -x $self->wrapper_path();
    } catch SVG::Rasterize::Engine::Batik::Error::Prerequisite with {
        return 0;
    };
}

sub jar_available {
    my $self = shift;

    try {
        return 1 if -r $self->jar_path() && -x $self->java_path();
    } catch SVG::Rasterize::Engine::Batik::Error::Prerequisite with {
        return 0;
    };
}

sub available {
    my $self = shift;

    return 1 if $self->wrapper_available() || $self->jar_available();

    return 0;
}

=pod

=head2 convert( \%params )

C<\%params> is a hash as described in SVG::Rasterize, plus the optional
parameter heapsize which sets the Java VM maximum heap size. The default
is "512M".

=cut

sub convert {
    my $self = shift;
    my %params = @_;

    my @cmd;

    if( $self->wrapper_available() ){
        @cmd = ($self->wrapper_path(), '-m', 'image/png');
        push(@cmd, '-w', $params{width}) if $params{width};
        push(@cmd, '-h', $params{height}) if $params{height};
        push(@cmd, sprintf('-a %f,%f,%f,%f',
                           $params{left},
                           $params{top},
                           $params{right} - $params{left},
                           $params{top} - $params{bottom})
            ) if $params{left} && $params{bottom} && $params{right} && $params{top};
        push(@cmd, '-d', $params{outfile});
        push(@cmd, $params{infile});
    } elsif( $self->jar_available() ){
        @cmd = ($self->java_path(), '-Xms256M');
        push(@cmd, '-Xmx'.$params{heapsize}) if $params{heapsize};
        push(@cmd, '-jar', $self->jar_path());
        push(@cmd, '-w', $params{width}) if $params{width};
        push(@cmd, '-h', $params{height}) if $params{height};
        push(@cmd, sprintf('-a %f,%f,%f,%f',
                           $params{left},
                           $params{top},
                           $params{right} - $params{left},
                           $params{top} - $params{bottom})
            ) if $params{left} && $params{bottom} && $params{right} && $params{top};
        push(@cmd, '-d', $params{outfile});
        push(@cmd, $params{infile});
    } else {
        throw SVG::Rasterize::Engine::Batik::Error::Prerequisite('No batik available');
    }

    run( \@cmd, \undef, \$self->{stdout}, \$self->{stderr} ) or
        throw SVG::Rasterize::Engine::Batik::Error::Runtime($self->path()." returned non-zero status code $?", {stdout => $self->{stdout}, stderr => $self->{stderr}});

    $self->check_output($params{outfile});
}

package SVG::Rasterize::Engine::Batik::Error;
use base qw(SVG::Rasterize::Engine::Error);

package SVG::Rasterize::Engine::Batik::Error::Runtime;
use base qw(SVG::Rasterize::Engine::Batik::Error SVG::Rasterize::Engine::Error::Runtime);

package SVG::Rasterize::Engine::Batik::Error::Prerequisite;
use base qw(SVG::Rasterize::Engine::Batik::Error SVG::Rasterize::Engine::Error::Prerequisite);

1;

__END__

=pod

=head1 TO DO

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
