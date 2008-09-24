package SVG::Rasterize::Engine::Batik;

use strict;
use warnings;

$__PACKAGE__::VERSION = '0.1';

use base qw(Class::Accessor SVG::Rasterize::Engine);
use File::Spec;
use Error qw(:try);
use IPC::Run qw(run);

=pod
=head1 NAME

SVG::Rasterize::Engine::Batik -- Batik engine for SVG::Rasterize

=head1 DESCRIPTION

This module is only meant to be used by SVG::Rasterize.

=head1 ACCESSORS

=cut

__PACKAGE__->mk_accessors(qw(wrapper_path java_path wrapper_searchpaths java_searchpaths jar_searchpaths jar_list));

=pod

=head2 wrapper_path

Path to Batik wrapper

=cut

sub wrapper_path {
    my $self = shift;

    unless( @_ || $self->{wrapper_path} ){ # We're getting and don't have a defined path
        foreach my $path ( @{ $self->wrapper_searchpaths() } ){
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

=head2 java_path

Path to java executable

=cut

sub java_path {
    my $self = shift;

    unless( @_ || $self->{java_path} ){ # We're getting and don't have a defined path
        foreach my $path ( @{ $self->java_searchpaths() } ){
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
    if( $^O eq 'MSWin32' ){
        #FIXME: add good places to search here
        $self->jar_searchpaths(
            'c:\tilesAtHome\batik'
            );
    } else {
        $self->jar_searchpaths(
            '/usr/share/batik',
            '/usr/share/batik/lib',
            '/usr/share/batik-1.6/lib',
            '/usr/share/java',
            '/usr/share/java/lib',
            '/usr/lib/batik',
            '/usr/lib/batik/lib',
            '/usr/lib/batik-1.6',
            '/usr/lib/batik-1.6/lib',
            '/usr/lib/java',
            '/usr/lib/java/lib'
            );
    }
    $self->jar_list(
        'xercesImpl.jar',
        'batik-all.jar',
        'fop-transcoder.jar',
        'avalon-framework.jar',
        'commons-logging.jar',
        'commons-logging.jar',
        'commons-io.jar'
        );
    $self->java_searchpaths(
        File::Spec->path()
        );
    $self->wrapper_searchpaths(
        File::Spec->path()
        );

    foreach my $param ( keys(%$params) ){
        $self->$param( $params->{$param} );
    }

    return $self;
}

=pod

=head2 find_jar( $jarname )

Find a jar and return it's pathname.

Throws Prerequisite exceptions if it can't find the jar

=cut

sub find_jar {
    my $self = shift;
    my $jarname = shift;

    foreach my $path ( @{ $self->jar_searchpaths() } ){
        my($volume, $dir) = File::Spec->splitpath($path, 1);

        my $filepath = File::Spec->catpath($volume, $dir, $jarname);

        return $filepath if -r $filepath;
    }
    throw SVG::Rasterize::Engine::Batik::Error::Prerequisite("Couldn't find $jarname");
}

=pod

=head2 find_jars( @list )

Shortcut to run find_jar on many jars, returning them as a list

=cut

sub find_jars {
    my $self = shift;

    my @result;
    foreach my $jar ( @_ ){
        push(@result, $self->find_jar($jar));
    }

    return @result;
}

=pod

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
        return 1 if $self->find_jars(@{$self->jar_list()}) && -x $self->java_path();
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

    my %area;
    if( $params{area} ){
        %area = $params{area}->get_box_upperleft();
        $area{width} = $params{area}->get_box_width();
        $area{height} = $params{area}->get_box_height();
    }

    if( $self->jar_available() ){
        @cmd = ($self->java_path());
        push(@cmd, '-Xms256M');
        push(@cmd, '-Xmx'. ( $params{heapsize} ? $params{heapsize} : '512M' ) );
        push(@cmd, '-classpath', join(':', $self->find_jars( @{$self->jar_list()} )));
        push(@cmd, 'org.apache.batik.apps.rasterizer.Main');
        push(@cmd, '-scriptSecurityOff'); # It just crashes without this
        push(@cmd, '-w', $params{width}) if $params{width};
        push(@cmd, '-h', $params{height}) if $params{height};
        push(@cmd, '-a', sprintf('%f,%f,%f,%f',
                           @area{'left','top','width','height'})
            ) if %area;
        push(@cmd, '-d', $params{outfile});
        push(@cmd, $params{infile});
    } elsif( $self->wrapper_available() ){
        @cmd = ($self->wrapper_path(), '-m', 'image/png');
        push(@cmd, '-w', $params{width}) if $params{width};
        push(@cmd, '-h', $params{height}) if $params{height};
        push(@cmd, '-a', sprintf('%f,%f,%f,%f',
                           @area{'left','top','width','height'})
            ) if %area;
        push(@cmd, '-d', $params{outfile});
        push(@cmd, $params{infile});
    } else {
        throw SVG::Rasterize::Engine::Batik::Error::Prerequisite('No batik available');
    }

    my $stdout; my $stderr;

    #DEBUG:warn 'about to run '.join(' ', @cmd);
    my $result;
    try {
        $result = run( \@cmd, \undef, \$stdout, \$stderr )
    } otherwise {
        my $e = shift;
        throw SVG::Rasterize::Engine::Batik::Error::Runtime("Error running \"$cmd[0]\", run could not execute the command: $e");
    };
    #DEBUG
    #warn "status: $result\n\$!: $!\n\$?: $?";
    #warn "stdout: ".$stdout;
    #warn "stderr: ".$stderr;

    if( ! $result || $stderr =~ /Error/ ){
        my $error;

        if( $? == -1 ){
            $error = 'failed to execute: '.$!;
        } elsif( $? & 127 ){
            $error = 'died with signal '.($? & 127);
        } else {
            $error = 'exited with value '.($? >> 8);
        }

        throw SVG::Rasterize::Engine::Batik::Error::Runtime("Error running \"$cmd[0]\": $error", {cmd => \@cmd, stdout => $stdout, stderr => $stderr});
    }

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
