package OSM::QuadTree;

use strict;
use Carp;

our $VERSION = 0.1;

1;

###############################
#
# sub new() - constructor
#
# Arguments are a hash:
#
# -xmin  => minimum x value
# -xmax  => maximum x value
# -ymin  => minimum y value
# -ymax  => maximum y value
# -depth => depth of tree
#
# Creating a new QuadTree objects automatically
# segments the given area into quadtrees of the
# specified depth.
#
###############################

sub new {
    my $self  = shift;
    my $class = ref($self) || $self;

    my $obj   = bless {} => $class;

    $obj->{BACKREF} = {};
    $obj->{OBJECTS} = [];
    $obj->{ORIGIN}  = [0, 0];
    $obj->{SCALE}   = 1;

    my %args  = @_;

    for my $arg (qw/xmin ymin xmax ymax depth/) {
	unless (exists $args{"-$arg"}) {
	    carp "- must specify $arg";
	    return undef;
	}

	$obj->{uc $arg} = $args{"-$arg"};
    }

    $obj->_segment;

    return $obj;
}

###############################
#
# sub _segment() - private method
#
# This method does the actual segmentation
# and stores everything internally.
#
###############################

sub _segment {
    my $obj = shift;

    $obj->_addLevel(
		    $obj->{XMIN},
		    $obj->{YMIN},
		    $obj->{XMAX},
		    $obj->{YMAX},
		    1,             # current depth
		    0,             # current index
		    undef,         # parent index
		    );

}

###############################
#
# sub _addLevel() - private method
#
# This method segments a given area
# and adds a level to the tree.
#
###############################

sub _addLevel {
    my ($obj,
	$xmin,
	$ymin,
	$xmax,
	$ymax,
	$curDepth,
	$index,
	$parent,
	) = @_;

    $obj->{AREA}    [$index] = [$xmin, $ymin, $xmax, $ymax];
    $obj->{PARENT}  [$index] = $parent;
    $obj->{CHILDREN}[$index] = [];
    $obj->{OBJECTS} [$index] = [];

    if (defined $parent) {
	push @{$obj->{CHILDREN}[$parent]} => $index;
    }

    return if $curDepth == $obj->{DEPTH};

    my $xmid = $xmin + ($xmax - $xmin) / 2;
    my $ymid = $ymin + ($ymax - $ymin) / 2;

    # now segment in the following order (doesn't matter):
    # top left, top right, bottom left, bottom right
    $obj->_addLevel($xmin, $ymid, $xmid, $ymax,  # tl
		    $curDepth + 1, 4 * $index + 1, $index);
    $obj->_addLevel($xmid, $ymid, $xmax, $ymax,  # tr
		    $curDepth + 1, 4 * $index + 2, $index);
    $obj->_addLevel($xmin, $ymin, $xmid, $ymid,  # bl
		    $curDepth + 1, 4 * $index + 3, $index);
    $obj->_addLevel($xmid, $ymin, $xmax, $ymid,  # br
		    $curDepth + 1, 4 * $index + 4, $index);
}

###############################
#
# sub add() - public method
#
# This method adds an object to the tree.
# The arguments are a unique tag to identify
# the object, and the bounding box of the object.
# It automatically assigns the proper quadtree
# sections to each object.
#
###############################

sub add {
    my ($self,
	$objRef,
	@coords,
	) = @_;

    # assume that $objRef is unique.
    # assume coords are (xmin, ymix, xmax, ymax).

    # modify coords according to window.
    @coords = $self->_adjustCoords(@coords);

    ($coords[0], $coords[2]) = ($coords[2], $coords[0]) if
	$coords[2] < $coords[0];
    ($coords[1], $coords[3]) = ($coords[3], $coords[1]) if
	$coords[3] < $coords[1];

    $self->_addObjToChild(
			  0,        # current index
			  $objRef,
			  @coords,
			  );
}

###############################
#
# sub _addObjToChild() - private method
#
# This method is used internally. Given
# a tree segment, an object and its area,
# it checks to see whether the object is to
# be included in the segment or not.
# The object is not included if it does not
# overlap the segment.
#
###############################

sub _addObjToChild {
    my ($self,
	$index,
	$objRef,
	@coords,
	) = @_;

    # first check if obj overlaps current segment.
    # if not, return.
    my ($cxmin, $cymin, $cxmax, $cymax) = @{$self->{AREA}[$index]};

    return if
	$coords[0] > $cxmax ||
	$coords[2] < $cxmin ||
	$coords[1] > $cymax ||
	$coords[3] < $cymin;

    # Only add the object to the segment if we are at the last
    # level of the tree.
    # Else, keep traversing down.

    unless (@{$self->{CHILDREN}[$index]}) {
	push @{$self->{OBJECTS}[$index]}  => $objRef;    # points from leaf to object
	push @{$self->{BACKREF}{$objRef}} => $index;     # points from object to leaf

    } else {
	# Now, traverse down the hierarchy.
	for my $child (@{$self->{CHILDREN}[$index]}) {
	    $self->_addObjToChild(
				  $child,
				  $objRef,
				  @coords,
				  );
	}
    }
}

###############################
#
# sub delete() - public method
#
# This method deletes an object from the tree.
#
###############################

sub delete {
    my ($self,
	$objRef,
	) = @_;

    return unless exists $self->{BACKREF}{$objRef};

    for my $i (@{$self->{BACKREF}{$objRef}}) {
	$self->{OBJECTS}[$i] = grep {$_ ne $objRef} @{$self->{OBJECTS}[$i]};
    }

    delete $self->{BACKREF}{$objRef};
}

###############################
#
# sub getEnclosedObjects() - public method
#
# This method takes an area, and returns all objects
# enclosed in that area.
#
###############################

sub getEnclosedObjects {
    my ($self,
	@coords) = @_;

    $self->{TEMP} = [];

    @coords = $self->_adjustCoords(@coords);

    $self->_checkOverlap(
			 0,   # current index
			 @coords,
			 );

    # uniquify {TEMP}.
    my %temp;
    @temp{@{$self->{TEMP}}} = undef;

    # PS. I don't check explicitly if those objects
    # are enclosed in the given area. They are just
    # part of the segments that are enclosed in the
    # given area. TBD.

    return [keys %temp];
}

###############################
#
# sub _adjustCoords() - private method
#
# This method adjusts the given coordinates
# according to the stored window. This is used
# when we 'zoom in' to avoid searching in areas
# that are not visible in the canvas.
#
###############################

sub _adjustCoords {
    my ($self, @coords) = @_;

    # modify coords according to window.
    $_ = $self->{ORIGIN}[0] + $_ / $self->{SCALE}
	for $coords[0], $coords[2];
    $_ = $self->{ORIGIN}[1] + $_ / $self->{SCALE}
	for $coords[1], $coords[3];

    return @coords;
}

###############################
#
# sub _checkOverlap() - private method
#
# This method checks if the given coordinates overlap
# the specified tree segment. If not, nothing happens.
# If it does overlap, then it is called recuresively
# on all the segment's children. If the segment is a
# leaf, then its associated objects are pushed onto
# a temporary array for later access.
#
###############################

sub _checkOverlap {
    my ($self,
	$index,
	@coords,
	) = @_;

    # first check if obj overlaps current segment.
    # if not, return.
    my ($cxmin, $cymin, $cxmax, $cymax) = @{$self->{AREA}[$index]};

    return if
	$coords[0] >= $cxmax ||
	$coords[2] <= $cxmin ||
	$coords[1] >= $cymax ||
	$coords[3] <= $cymin;

    unless (@{$self->{CHILDREN}[$index]}) {
	push @{$self->{TEMP}} => @{$self->{OBJECTS}[$index]};
    } else {
	# Now, traverse down the hierarchy.
	for my $child (@{$self->{CHILDREN}[$index]}) {
	    $self->_checkOverlap(
				 $child,
				 @coords,
				 );
	}
    }
}

###############################
#
# sub setWindow() - public method
#
# This method takes an area as input, and
# sets it as the active window. All new
# calls to any method will refer to that area.
#
###############################

sub setWindow {
    my ($self, $sx, $sy, $s) = @_;

    $self->{ORIGIN}[0] += $sx / $self->{SCALE};
    $self->{ORIGIN}[1] += $sy / $self->{SCALE};
    $self->{SCALE}     *= $s;
}

###############################
#
# sub setWindow() - public method
# This resets the window.
#
###############################

sub resetWindow {
  my $self = shift;

  $self->{ORIGIN}[$_] = 0 for 0 .. 1;
  $self->{SCALE}      = 1;
}



