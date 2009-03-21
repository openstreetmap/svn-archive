package SVG::Rasterize;

use strict;
use warnings;

$SVG::Rasterize::VERSION = '0.1';

use base qw(Class::Accessor);
use Module::Pluggable
    sub_name => 'engines',
    search_path => __PACKAGE__.'::Engine',
    require => 1,
    except => qr/\bError\b/;
use Error;
use IO::File;

=pod

=head1 NAME

SVG::Rasterize -- Rasterizes an SVG file to PNG

=head1 SYNOPSIS

    my $rasterizer = SVG::Rasterize->new();

    foreach( $rasterizer->engines() ){
        print "engine $_ " .( $_->new()->available() ? "available" : "not available") . "\n"
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

SVG::Rasterize->mk_accessors(qw(engine debug));

=pod

=head2 engine

The engine to be used to rasterize. Defaults to the first available module
found with Module::Pluggable.

If it can't find any available rasterizer it throws an exception, so callers
might want to wrap it in C<eval{}> or other exception handling mechanism.

When setting you can pass either a engine instance or a string, in the case
of a string it will search $self->engines() for something matching 
C</\b$value\b/>, throwing an exception if it finds either more or less than
one match. To be sure you know what renderer it will end up choosing pass a
full module name including all parent namespace names.

TODO: tests

=cut

sub engine {
    my $self = shift;

    if( !@_ && !$self->{engine} ){ # We're getting and don't have a defined engine
        foreach my $engine ( $self->engines() ){
            my $obj = $engine->new({rasterizer => $self});
            return $self->_engine_accessor($obj) if $obj->available();
        }
        die __PACKAGE__."->engine found no available rasterizer engine.\n";
    }

    if( @_ && ! ref($_[0]) ){ # We're setting and got a normal scalar instead of an engine instance
        my $engine = $_[0];

        my(@matches) = grep({$_ =~ /\b$engine\b/i} $self->engines());
        throw Error::Simple("Engine type \"$engine\" not found or not unique") unless $#matches == 0;

        return $self->_engine_accessor($matches[0]->new({rasterizer => $self}));
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

=over

=item infile
input svg file

=item outfile
output png file

=item width
width of the output in pixels

=item height
height of the output in pixels

=item area
the area of the SVG to convert. The coordinate system has 0,0 in the
upper left corner. If not given it defaults to rendering the entire
document. Given as a hashref with the following keys:

=over

=item left

=item right

=item top

=item bottom

=item type
Either absolute, meaning the coordinates are given in SVG units, or
relative, meaning coordinates are given as floating-point values with 0
being 0 and 1 being the document width or height.

=back

=back

This uses the engine accessor and getSize method internally, so may
throw exceptions as documented on those.

Returns: true on success, false on failure

=cut

sub convert {
    my $self = shift;
    my %params = @_;

    if( exists($params{area}) ){
        if( $params{area}{type} eq 'absolute' ){
        } elsif( $params{area}{type} eq 'relative' ){
            # Convert to absolute sizes

            my $size = $self->getSize($params{infile});

            $params{area}{left} *= $size->{width}{n};
            $params{area}{right} *= $size->{width}{n};
            $params{area}{top} *= $size->{height}{n};
            $params{area}{bottom} *= $size->{height}{n};
        } else {
            die "Application bug: area passed without type, or type other than absolute or relative";
        }
    }

    return $self->engine()->convert(%params);
}

=pod

=head2 getSize( $filename )

Get the width and height (with any units if specified) of an SVG file.

Returns a reference to a hash laid out like the following:

    {
        width => {
            n => 123,
            unit => 'px'
        },
        height => {
            n => 321
            unit => 'px'
        }
    }

If no unit is specified (SVG "user unit"), unit is an empty string.

Throws SVG::Rasterize::Error::CoordinateError if it can't open or
parse the file, or the coordinates are wrong.

=cut

sub getSize
{
    my $self = shift;
    my $file = shift;

    my $fh = IO::File->new($file, '<')
        or throw SVG::Rasterize::Error::CoordinateError("Error opening $file: $!");

    # OK, this is dirty, but we manually parse the SVG to avoid starting
    # a full parser just to look at a couple of parameters of the
    # document element.
    #
    # There are some cases that will definitely break it, like having
    # height/width parameters in another namespace (on the <svg>
    # element), but they should be extremely rare *crosses fingers*

    my $foundstart = 0;
    my $foundend = 0;
    my $buffer;
    while( my $line = <$fh> ){
        if( $line =~ s/.*(<(?:\S*:)?svg\b)/$1/ ){
            $foundstart = 1;
        }

        if( $foundstart && $line =~ s/>.*// ){
            $foundend = 1;
        }

        if( $foundstart ){
            $buffer .= $line;
        }

        if( $foundend ){
            $buffer =~ /\bwidth\s*=\s*([\'\"])(.+?)\1/;
            my $width = $2;
            $buffer =~ /\bheight\s*=\s*([\'\"])(.+?)\1/;
            my $height = $2;

            if( defined($width) && defined($height) ){
                $height =~ s/^(-?[\d\.]+)(em|ex|px|pt|pc|cm|mm|in|%)?$/$1/
                    or throw SVG::Rasterize::Error::CoordinateError( "Invalid height: '$height'" );
                my $heightunit = $2;
                $width =~ s/^(-?[\d\.]+)(em|ex|px|pt|pc|cm|mm|in|%)?$/$1/
                    or throw SVG::Rasterize::Error::CoordinateError( "Invalid width: '$width'" );
                my $widthunit = $2;

                return {
                    height => {
                        n => $height,
                        unit => defined($heightunit) ? $heightunit : ''
                    },
                    width => {
                        n => $width,
                        unit => defined($widthunit) ? $widthunit : ''
                    }
                };
            } else {
                throw SVG::Rasterize::Error::CoordinateError("Error parsing svg element for height/width parameters", {data => $buffer});
            }
        }
    }

    throw SVG::Rasterize::Error::CoordinateError("Error parsing svg element for height/width parameters: didn't find any <svg> element");
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

package SVG::Rasterize::Error::CoordinateError;
use base qw(SVG::Rasterize::Error);

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
