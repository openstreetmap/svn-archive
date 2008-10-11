package SVG::Rasterize;

use strict;
use warnings;

$SVG::Rasterize::VERSION = '0.1';

use base qw(Class::Accessor);
use Module::Pluggable
    sub_name => 'engines',
    search_path => __PACKAGE__.'::Engine',
    instantiate => 'new',
    except => qr/\bError\b/;
use Error;

=pod
=head1 NAME

SVG::Rasterize -- Rasterizes an SVG file to PNG

=head1 SYNOPSIS

my $rasterizer = SVG::Rasterize->new();
foreach( $rasterizer->engines() ){
    print "engine $_ " .( $_->available() ? "available" : "not available") . "\n"
}
$rasterizer->engine('Inkscape'); # Set preferred rasterizer engine
$rasterizer->convert($infile, $outfile)
    or die "Couldn't rasterize $infile into $outfile: ".$rasterizer->error;

=head1 DESCRIPTION

This file implements a generic rasterizer class for SVG.

For now only PNG output is supported, but extending it to other bitmap formats
should be relatively simple.

=head1 ACCESSORS

=cut

SVG::Rasterize->mk_accessors(qw(engine));

=pod

=head2 engine

The engine to be used to rasterize. Defaults to the first available module
found with Module::Pluggable.

If it can't find any available rasterizer it throws an exception, so callers
might want to wrap it in C<eval{}>

When setting you can pass either a engine instance or a string, in the case
of a string it will search $self->engines() for something matching 
C</\b$value\b/>, throwing an exception if it finds either more or less than
one match. To be sure you know what renderer it will end up choosing pass a
full module name including all parent namespace names.

TODO: tests

=cut

sub engine {
    my $self = shift;

    unless( @_ || $self->{engine} ){ # We're getting and don't have a defined engine
        foreach my $engine ( $self->engines() ){
            return $engine if $engine->available();
        }
        die __PACKAGE__."->engine found no available rasterizer engine.\n";
    }

    if( @_ && ! ref($_[0]) ){ # We're setting and got a normal scalar instead of an engine instance
        my $engine = $_[0];

        my(@matches) = grep({ref($_) =~ /\b$engine\b/i} $self->engines());
        throw Error::Simple("Engine type \"$engine\" not found or not unique") unless $#matches == 0;

        return $self->_engine_accessor($matches[0]);
    }
        

    return $self->_engine_accessor(@_);
}

=pod

=head1 METHODS

=head2 new(\%params) (constructor)

Create a new instance of this class. You can pass in parameters which
will then be set via their accessor

Returns: new instance of this class.

=begin testing new 3

use SVG::Rasterize;
my $object = SVG::Rasterize->new();
ok(defined($object), 'new returns a defined value');
ok(ref($object), '... which is a reference');
ok($object->isa('SVG::Rasterize'), '... which isa SVG::Rasterize');

=end testing

=cut

#NOTE: new() is supplied by Class::Accessor

=pod

=head2 convert( \%params )

C<\%params> is a hash with key/value pairs. The following keys
have meaning:

infile: input svg file
outfile: output png file
width: width of the output in pixels
height: height of the output in pixels
area: a SVG::Rasterize::CoordinateBox describing the part of the svg
  to render. Defaults to rendering the entire drawing if not given.

This uses the engine accessor internally so it may also throw an
exception.

Returns: true on success, false on failure

=cut

sub convert {
    my $self = shift;
    my %params = @_;

    return $self->engine()->convert(%params);
}

package SVG::Rasterize::Error;
use base qw(Error);

sub new {
    my $self  = shift;
    my $text  = "" . shift;
    my $params = shift;

    local $Error::Depth = $Error::Depth + 1;

    $self->SUPER::new(-text => $text, %$params);
}

sub stringify {
    my $self = shift;
    my $text = $self->SUPER::stringify;
    $text .= sprintf(" at %s line %d.\n", $self->file, $self->line)
        unless($text =~ /\n$/s);
    $text;
}
1;

__END__

=pod

=head1 TO DO

Write some more tests

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
