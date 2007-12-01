#!/usr/bin/perl


use strict;

use Geo::ShapeFile;

our $min_lat = 35.5;
our $max_lat = 42.2;
our $min_lon = 26.0;
our $max_lon = 45.0;


#
#
#
sub read_shapes
{
	my ($filename) = @_;

	my $shapeFile = new Geo::ShapeFile($filename);
	print $shapeFile->shapes() . " shapes in " . $filename . "\n";

    my @bounds = $shapeFile->bounds();
    print "Bounds: " . join(", ", $shapeFile->bounds()) . "\n";

	# x_min, y_min, x_max, y_max
	my @ids = $shapeFile->shapes_in_area($min_lon, $max_lat, $max_lon, $min_lat);
	print $#ids . " shapes in target area\n";
	
	for (1 .. 1)
	{
	    my $shape = $shapeFile->get_shp_record($_);
	    my @points = $shape->points();
	    print "Shape # $_ is " . $shape . ", " . $#points . " points\n";
	    
	    foreach my $point (@points)
	    {
	        print "    Point " . $point . "\n";
	    }
	}
}


#
#   Run that script...
#

read_shapes("data/world_bnd_m");
read_shapes("data/world_boundaries_m");
