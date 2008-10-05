package SVG::Rasterize::CoordinateBox;

=pod

=head1 NAME

SVG::Rasterize::CoordinateBox -- Represents a box in a coordinate space

=head1 SYNOPSIS

# Put in the box and the full coordinate space as seen from your perspective
# and the module will automatically flip as necessary.
my %params = (
    box => {
        left => 5,
        right => 10,
        top => 10,
        bottom => 5
    },
    space => {
        left => 0,
        right => 20,
        top => 20,
        bottom => 0
    }
);
my $box = SVG::Rasterize::CoordinateBox->new(%params);

# $box can now be passed to SVG::Rasterize::render

=head1 DESCRIPTION

This class represents a box in a given coordinate system. The reason for it's
existence is that different renderers want different origins, and to do a flip
transform we need to know the full extent of the canvas.

=begin testing SETUP 1

use SVG::Rasterize::CoordinateBox;
my %params = (
    box => {
        left => 5,
        right => 10,
        top => 10,
        bottom => 5
    },
    space => {
        left => 0,
        right => 20,
        top => 20,
        bottom => 0
    }
);
my $box = SVG::Rasterize::CoordinateBox->new(\%params);
isa_ok( $box, 'SVG::Rasterize::CoordinateBox' );

=end testing

=head1 METHODS

=cut

use strict;
use warnings;

$SVG::Rasterize::CoordinateBox::VERSION = '0.1';

=pod

=head2 new( \%params ) (constructor)

Create a new instance of this class. Initialize the object with
a hash containing box and space, which point to anonymous hashes.
See the example for details.

Returns: new instance of this class.

=begin testing new 1

$params{box}{left} = 6;
is($box->{box}{left}, 5, "we can't rip the data out underneath it's feet");

=end testing

=cut

sub new {
    my ( $pkg, $params ) = @_;
    my $class = ref $pkg || $pkg;
    my $self = bless( {}, $class);

    # Copy the data so we don't end up having them changed underneath
    # our feet
    $self->{box} = { %{ $params->{box} } };
    $self->{space} = { %{ $params->{space} } };

    $self->{space}{height} = abs($self->{space}{top} - $self->{space}{bottom});

    return $self;
}

=pod

=head2 get_box_lowerleft

Returns the box coordinates transformed as necessary to have the origin
in the lower left corner

=begin testing get_box_lowerleft after new 1

my %expected = (left => 5, right => 10, top => 10, bottom => 5);
my %result = $box->get_box_lowerleft();
is_deeply( \%result, \%expected, 'get_box_lowerleft result' );

=end testing

=cut

sub get_box_lowerleft {
    my $self = shift;

    my %box = %{ $self->{box} };
    if( $self->{space}{top} < $self->{space}{bottom} ){
        $box{top} = $self->{space}{height} - $self->{box}{top};
        $box{bottom} = $self->{space}{height} - $self->{box}{bottom};
    }

    return %box;
}

=pod

=head2 get_box_upperleft

Returns the box coordinates transformed as necessary to have the origin
in the upper left corner

=begin testing get_box_upperleft after new 1

my %expected = (left => 5, right => 10, top => 10, bottom => 15);
my %result = $box->get_box_upperleft();
is_deeply( \%result, \%expected, 'get_box_upperleft result' );

=end testing

=cut

sub get_box_upperleft {
    my $self = shift;

    my %box = %{ $self->{box} };
    if( $self->{space}{top} > $self->{space}{bottom} ){
        $box{top} = $self->{space}{height} - $self->{box}{top};
        $box{bottom} = $self->{space}{height} - $self->{box}{bottom};
    }

    return %box;
}

=pod

=head2 get_box_height

Returns the height of the box

=begin testing get_box_height after new 1

is( $box->get_box_height(), 5, 'get_box_height' );

=end testing

=cut

sub get_box_height {
    my $self = shift;
    return abs($self->{box}{bottom} - $self->{box}{top});
}

=pod

=head2 get_box_width

Returns the width of the box

=begin testing get_box_width after new 1

is( $box->get_box_width(), 5, 'get_box_width' );

=end testing

=cut

sub get_box_width {
    my $self = shift;
    return abs($self->{box}{left} - $self->{box}{right});
}

1;

__END__

=pod

=head1 TO DO

This is very special-purpose, rigidly and stupidly coded, might be nice
to generalize it some.

Should test with initial coords from both origins, and with negative values.

=head1 BUGS

Let me know if you find any.

=head1 COPYRIGHT

Same as Perl.

=head1 AUTHORS

Knut Arne Bjørndal <bob@cakebox.net>

=cut
