#    Copyright (C) 2005 Tommy Persson, tpe@ida.liu.se
#
#    This program is free software; you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation; either version 2 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program; if not, write to the Free Software
#    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

package landsattile;

use strict;

use FindBin qw($RealBin);
use lib "$RealBin/../perl";

use curl;

#rice:~/demdata/rg> wgs84tolocal 55.723 13.471 0
#-16.732004 -89.674796 -50.670343 --- revinge
#rice:~/demdata/rg> wgs84tolocal 55.724 13.472 0
#49.836450 19.512504 -50.670027 --- revinge
# 66.5685  109.18   0.6097
# 
# rice:~/demdata/rg> wgs84tolocal 55.724 13.4715 0
# 18.427472 20.570532 -50.670900 --- revinge
# rice:~/demdata/rg> wgs84tolocal 55.7235 13.472 0
# 47.961996 -36.139303 -50.669311 --- revinge
# 64.69  53.54  1.21
#
# 


sub new {
    my $this = shift;
    my $class = ref($this) || $this;
    bless {
        NAME => "UNSPECIFIED",
	WEST => 0,
	SOUTH => 0,
	EAST => 0,
	NORTH => 0,
	DATA => "",
	PIXELWIDTH => 600,
	PIXELHEIGHT => 500,
	LAT => 0,
	LON => 0,
	DELTALAT => 0.0005,
	DELTALON => 0.001,   # Gives ratio 1.2 in meter
	SCALE => 100,
	FILENAME => "",
	FRAME => 0,
	CANVAS => 0,
	IMAGE => 0,
	IMAGEITEM => 0,
	MARKERITEM => 0,
	MARKERITEM2 => 0,
	MARKERFLAG => 0,
        @_
	}, $class;
}

sub get_pixel_width {
    my $self = shift;
    return $self->{PIXELWIDTH};
}

sub get_pixel_height {
    my $self = shift;
    return $self->{PIXELHEIGHT};
}

sub set_filename {
    my $self = shift;
    my $val = shift;
    $self->{FILENAME} = $val;
}

sub get_filename {
    my $self = shift;
    return $self->{FILENAME};;
}

sub set_frame {
    my $self = shift;
    my $val = shift;
    $self->{FRAME} = $val;
}

sub get_frame {
    my $self = shift;
    return $self->{FRAME};;
}

sub set_canvas {
    my $self = shift;
    my $val = shift;
    $self->{CANVAS} = $val;
}

sub get_canvas {
    my $self = shift;
    return $self->{CANVAS};;
}

sub set_image {
    my $self = shift;
    my $val = shift;
    $self->{IMAGE} = $val;
}

sub get_image {
    my $self = shift;
    return $self->{IMAGE};;
}

sub set_scale {
    my $self = shift;
    my $val = shift;
    $self->{SCALE} = $val;
}

sub get_scale {
    my $self = shift;
    return $self->{SCALE};
}

sub set_data {
    my $self = shift;
    my $val = shift;
    $self->{DATA} = $val;
}

sub get_data {
    my $self = shift;
    return $self->{DATA};;
}

sub set_lat {
    my $self = shift;
    my $val = shift;
    $self->{LAT} = $val;
}

sub get_lat {
    my $self = shift;
    return $self->{LAT};;
}

sub set_deltalat {
    my $self = shift;
    my $val = shift;
    $self->{DELTALAT} = $val;
}

sub get_deltalat {
    my $self = shift;
    return $self->{DELTALAT};;
}

sub set_lon {
    my $self = shift;
    my $val = shift;
    $self->{LON} = $val;
}

sub get_lon {
    my $self = shift;
    return $self->{LON};;
}

sub set_deltalon {
    my $self = shift;
    my $val = shift;
    $self->{DELTALON} = $val;
}

sub get_deltalon {
    my $self = shift;
    return $self->{DELTALON};;
}

sub set_area {
    my $self = shift;
    my $west = shift;
    my $south = shift;
    my $east = shift;
    my $north = shift;
    $self->{WEST} = $west;
    $self->{SOUTH} = $south;
    $self->{EAST} = $east;
    $self->{NORTH} = $north;
}

sub get_area {
    my $self = shift;
    my $west = $self->{WEST};
    my $south = $self->{SOUTH};
    my $east = $self->{EAST};
    my $north = $self->{NORTH};
    return ($west, $south, $east, $north);
}

sub set_center {
   my $self = shift;
   my $lat = shift;
   my $lon = shift;
   $self->set_lat ($lat);
   $self->set_lon ($lon);
   my $dlat = $self->{DELTALAT};
   my $dlon = $self->{DELTALON};
   my $scale = $self->get_scale ();
   $self->set_area ($lon-$dlon*$scale/2, 
		    $lat-$dlat*$scale/2, $lon+$dlon*$scale/2, 
		    $lat+$dlat*$scale/2);
}

sub load  {
    my $self = shift;
    my $west = $self->{WEST};
    my $south = $self->{SOUTH};
    my $east = $self->{EAST};
    my $north = $self->{NORTH};
    my $w = $self->{PIXELWIDTH};
    my $h = $self->{PIXELHEIGHT};
    my $lat = $self->get_lat ();
    my $lon = $self->get_lon ();
    my $scale = $self->get_scale ();
    my $filename = "landsat-$scale";
    if ($lat > 0) {
	$filename .= "+";
    }
    $filename .= "$lat";
    if ($lon > 0) {
	$filename .= "+";
    }
    $filename .= "$lon";
    $filename = "$ENV{HOME}/.osmpedit/cache/$filename.jpg";
    print STDERR "Check cache: $filename\n";

    if (not -d "$ENV{HOME}/.osmpedit") {
	mkdir "$ENV{HOME}/.osmpedit";
    }

    if (not -d "$ENV{HOME}/.osmpedit/cache") {
	mkdir "$ENV{HOME}/.osmpedit/cache";
    }

    if (-f "$filename") {
	open JPG, "<$filename" or die "Could not open file $filename: $!";
	{
	    local $/;
	    my $data = <JPG>;
	    $self->set_data ($data);
	}
    } else {
	my $data = curl::grab_landsat ($west, $south, $east, $north, $w, $h);
	if ($data < 0)  {
	    print STDERR "WARNING: Failed to load landsat image\n";
	    return;
	} else {
	    $self->set_data ($data);
	    open JPG, ">$filename" or die "Could not open file $filename: $!";
	    print JPG "$data";
	    close JPG;
	}
    }
    $self->set_filename ($filename);
    my $image = $self->get_frame ()->Photo (-format => "jpeg",
					    -file => "$filename"
					    );
    $self->set_image ($image);
}

sub save {
    my $self = shift;
    my $name = shift;
    open JPG, ">$name";
    print JPG "$self->{DATA}";
    close JPG;
}

sub display {
    my $self = shift;
    my $lat = shift;
    my $lon = shift;

    my $item = $self->{IMAGEITEM};
    my $can = $self->get_canvas();
    
    if (not $item) {
	if ($self->get_image ()) {
	    $item = $can->createImage (0, 0,
				       -anchor => "nw",
				       -image => $self->get_image (),
				       -tag => "image");
	    $self->{IMAGEITEM} = $item;
	}
	if ($self->{MARKERFLAG}) {
	    my $colour = "yellow";
	    my $obj = $can->create ('line', 0, 0, 0, 0,
				    -fill => $colour,
				    -tag => "imagemarker");
	    $self->{MARKERITEM} = $obj;
	    $obj = $can->create ('line', 0, 0, 0, 0,
				 -fill => $colour,
				 -tag => "imagemarker");
	    $self->{MARKERITEM2} = $obj;
	}

    }

    my $w = $self->get_pixel_width ();
    my $h = $self->get_pixel_height ();
    my ($west, $south, $east, $north) = $self->get_area ();
    my $clat = $self->get_lat ();
    my $clon = $self->get_lon ();

    my $x = -($lon-$clon)/($east-$west)*$w;
    my $y = -($clat-$lat)/($north-$south)*$h;

    $can->coords ($item, $x, $y); 
    $can->raise ($item, "image");

    if ($self->{MARKERFLAG}) {   
	$can->coords ($self->{MARKERITEM}, $x, $y, $x+600, $y+500);    
	$can->coords ($self->{MARKERITEM2}, $x+600, $y, $x, $y+500);    
	$can->raise ($self->{MARKERITEM}, "image");
	$can->raise ($self->{MARKERITEM2}, "image");
    }
}

return 1;
