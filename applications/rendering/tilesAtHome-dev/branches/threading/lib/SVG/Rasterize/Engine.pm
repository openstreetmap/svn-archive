package SVG::Rasterize::Engine;

use strict;
use warnings;

$__PACKAGE__::VERSION = '0.1';

use base qw(Class::Accessor);
use Error;

=pod

=head1 NAME

SVG::Rasterize::Engine -- Base class for SVG::Rasterize engines

=head1 DESCRIPTION

This is a base class for SVG::Rasterize engine modules. The documentation here
describes the interface to be implemented by such modules.

At some point some helper functions might also be moved to this module.

=cut

=pod

=head1 METHODS

=head2 available()

Try to see if this engine can be used. Typically this will look for
program executables in C<$ENV{PATH}>.

=cut

sub available {
    # Implementors have to override this for their engine to be used if
    # SVG::Rasterize just looks for the first available renderer.
    return 0;
}

=pod

=head2 check_output( $file )

Checks the output file. For now it only checks to see if it's there and
have non-zero length.

Throws SVG::Rasterize::Engine::Error::NoOutput if there is no output.

=cut

sub check_output {
    my $self = shift;
    my $file = shift;

    unless( -e $file ){
        throw SVG::Rasterize::Engine::Error::NoOutput('Output file does not exist');
    }

    if( -z $file ){
        throw SVG::Rasterize::Engine::Error::NoOutput('Output file is 0-length');
    }
}

=pod

=head2 convert( \%params )

Do the actual convertion.

C<\%params> is a hash as described in SVG::Rasterize

=cut

sub convert {
#    my %params = @_;
#
#my $cmd = 
}

package SVG::Rasterize::Engine::Error;
use base qw(SVG::Rasterize::Error);

package SVG::Rasterize::Engine::Error::Prerequisite;
use base qw(SVG::Rasterize::Engine::Error);

package SVG::Rasterize::Engine::Error::Runtime;
use base qw(SVG::Rasterize::Engine::Error);

package SVG::Rasterize::Engine::Error::NoOutput;
use base qw(SVG::Rasterize::Engine::Error::Runtime);

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
