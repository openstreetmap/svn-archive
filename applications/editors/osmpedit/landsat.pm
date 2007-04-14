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

package landsat;

use FindBin qw($RealBin);
use lib "$RealBin/../perl";

use landsattile;

use strict;


sub new {
    my $this = shift;
    my $class = ref($this) || $this;
    bless {
	PIXELWIDTH => 600,
	PIXELHEIGHT => 500,
	CENTERLAT => 0,
	CENTERLON => 0,
	TRACKSHIDDEN => 0,
	NODESSHIDDEN => 0,
	SEGMENTSHIDDEN => 0,
	WAYSHIDDEN => 0,
	TRACKCOLLECTION => 0,
	FRAME => 0,
	CANVAS => 0,
	CURRENTTILE => 0,
	SCALE => 100,
	DELTALAT => 0.0005,
	DELTALON => 0.001,   # Gives ratio 1.2 in meter
	CLASSBUTTONMAP => {},
	NAMETOTILEMAP => {},
        @_
	}, $class;
}


sub set_center {
   my $self = shift;
   my $lat = shift;
   my $lon = shift;
   $self->{CENTERLAT} = $lat;
   $self->{CENTERLON} = $lon;
   my $dlat = $self->{DELTALAT};
   my $dlon = $self->{DELTALON};
   my $scale = $self->get_scale ();
   $self->set_area ($lon-$dlon*$scale/2, 
		    $lat-$dlat*$scale/2, $lon+$dlon*$scale/2, 
		    $lat+$dlat*$scale/2);
}

sub add_to_center {
   my $self = shift;
   my $lat = shift;
   my $lon = shift;
   $self->{CENTERLAT} += $lat;
   $self->{CENTERLON} += $lon;
   $self->{NORTH} += $lat;
   $self->{SOUTH} += $lat;
   $self->{WEST} += $lon;
   $self->{EAST} += $lon;
}

sub get_deltalat {
    my $self = shift;
    return $self->{DELTALAT};;
}

sub get_deltalon {
    my $self = shift;
    return $self->{DELTALON};;
}

sub get_lat {
    my $self = shift;
    return $self->{CENTERLAT};;
}

sub get_lon {
    my $self = shift;
    return $self->{CENTERLON};;
}

sub set_osm {
    my $self = shift;
    my $val = shift;
    $self->{OSM} = $val;
}

sub get_osm {
    my $self = shift;
    return $self->{OSM};
}

sub set_track_collection {
    my $self = shift;
    my $val = shift;
    $self->{TRACKCOLLECTION} = $val;
}

sub get_track_collection {
    my $self = shift;
    return $self->{TRACKCOLLECTION};
}

sub set_scale {
    my $self = shift;
    my $val = shift;
    $self->{SCALE} = $val;
    my $lat = $self->get_lat ();
    my $lon = $self->get_lon ();
    $self->set_center ($lat, $lon);
}

sub get_scale {
    my $self = shift;
    return $self->{SCALE};
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

sub get_tile {
    my $self = shift;
    my $lat = shift;
    my $lon = shift;
    my $scale = shift;

    my $filename = "landsat-$scale";
    if ($lat > 0) {
	$filename .= "+";
    }
    $filename .= "$lat";
    if ($lon > 0) {
	$filename .= "+";
    }
    $filename .= "$lon";

    my $tile = $self->{NAMETOTILEMAP}->{$filename};
    if (not $tile) {
	$tile = new landsattile ();
	$tile->set_scale ($scale);  ### Hum, must be before set_center...
	$tile->set_center ($lat, $lon);
	$tile->set_frame ($self->get_frame ());
	$tile->set_canvas ($self->get_canvas ());
	$tile->load ();
	$self->{NAMETOTILEMAP}->{$filename} = $tile;
    }
    return $tile;
}


sub display {
    my $self = shift;

    my $lat = $self->{CENTERLAT};
    my $lon = $self->{CENTERLON};
    my $scale = $self->get_scale ();

    my ($clat, $clon) = $self->clamp_to_center_of_tile ($lat, $lon, $scale);

##    $self->get_canvas ()->lower ("markeritem", "image");

    my $tile = $self->get_tile ($clat, $clon, $scale);
    $tile->display ($lat, $lon);


    my $dlat = $self->get_deltalat ();  # 0.005
    my $dlon = $self->get_deltalon ();  # 0.001
    $dlat *= $scale;
    $dlon *= $scale;

    # eight tiles around
    $tile = $self->get_tile ($clat+$dlat, $clon, $scale);    
    $tile->display ($lat, $lon);
    $tile = $self->get_tile ($clat+$dlat, $clon+$dlon, $scale);    
    $tile->display ($lat, $lon);
    $tile = $self->get_tile ($clat, $clon+$dlon, $scale);    
    $tile->display ($lat, $lon);
    $tile = $self->get_tile ($clat-$dlat, $clon+$dlon, $scale);    
    $tile->display ($lat, $lon);
    $tile = $self->get_tile ($clat-$dlat, $clon, $scale);    
    $tile->display ($lat, $lon);
    $tile = $self->get_tile ($clat-$dlat, $clon-$dlon, $scale);    
    $tile->display ($lat, $lon);
    $tile = $self->get_tile ($clat, $clon-$dlon, $scale);    
    $tile->display ($lat, $lon);
    $tile = $self->get_tile ($clat+$dlat, $clon-$dlon, $scale);    
    $tile->display ($lat, $lon);
}

sub update_tracks {
    my $self = shift;
    $self->get_track_collection ()->draw ($self);
}

sub update_osm {
    my $self = shift;
    $self->get_osm ()->draw ($self);
}

sub display_tile {
    my $self = shift;
    my $lat = shift;
    my $lon = shift;
    my $scale = shift;

    #
    # Non landsat stuff, should be in another place
    #


}

sub load_osm {
    my $self = shift;
    my $osm = $self->get_osm ();
    $osm->fetch ($self);
    $osm->parse ($self);
    $self->update_osm ();
}


#sub display () {
#    my $self = shift;
#    my $tile = $self->get_tile ();
#    my $scale = $self->get_scale ();
#    my $lat = $tile->get_lat ();
#    my $lon = $tile->get_lon ();
#    $self->display_tile ($lat, $lon, $scale);
#}

sub north () {
    my $self = shift;
    my $scale = $self->get_scale ();
    my $deltalat = $self->get_deltalat ();
    my $lat = $self->get_lat ();
    my $lon = $self->get_lon ();
    $lat += ($deltalat*$scale);
    $self->set_center ($lat, $lon);
    $self->display ();
}

sub south () {
    my $self = shift;
    my $scale = $self->get_scale ();
    my $deltalat = $self->get_deltalat ();
    my $lat = $self->get_lat ();
    my $lon = $self->get_lon ();
    $lat -= ($deltalat*$scale);
    $self->set_center ($lat, $lon);
    $self->display ();
}

sub west () {
    my $self = shift;
    my $scale = $self->get_scale ();
    my $deltalon = $self->get_deltalon ();
    my $lat = $self->get_lat ();
    my $lon = $self->get_lon ();
    $lon -= ($deltalon*$scale);
    $self->set_center ($lat, $lon);
    $self->display ();
}

sub east () {
    my $self = shift;
    my $scale = $self->get_scale ();
    my $deltalon = $self->get_deltalon ();
    my $lat = $self->get_lat ();
    my $lon = $self->get_lon ();
    $lon += ($deltalon*$scale);
    $self->set_center ($lat, $lon);
    $self->display ();
}

sub zoomin () {
    my $self = shift;
    my $scale = $self->get_scale ();
    if ($scale > 25) {
	$scale /= 2;
    } else {
	if ($scale > 10) {
	    $scale = 10;
	}
    }
    print STDERR "Set scale to $scale\n";
    $self->set_scale ($scale);
    $self->display ();
}

sub zoomout () {
    my $self = shift;
    my $scale = $self->get_scale ();
    if ($scale < 11) {
	$scale = 25;
    } else {
	$scale *= 2;
    }
    print STDERR "Set scale to $scale\n";
    $self->set_scale ($scale);
    $self->display ();
}

sub fix_order {
    my $self = shift;
    my $can = $self->get_canvas ();
    my $top = "all";
    if ($can->find ("withtag", "image")) {
	$can->raise ("image", $top);
	my $top = "image";
    }
    if (not $self->{WAYSHIDDEN}) {
	if ($can->find ("withtag", "osmway")) {
	    $self->get_canvas ()->raise ("osmway", $top);
	    $top = "osmway";
	}
    } 
    if (not $self->{TRACKSHIDDEN}) {
	if ($can->find ("withtag", "track")) {
	    $self->get_canvas ()->raise ("track", $top);
	    $top = "track";
	}
    } 
    if (not $self->{SEGMENTSHIDDEN}) {
	if ($can->find ("withtag", "osmsegment")) {
	    $self->get_canvas ()->raise ("osmsegment", $top);
	    $top = "osmsegment";
	}
    } 
    if (not $self->{NODESHIDDEN}) {
	if ($can->find ("withtag", "osmnode")) {
	    $self->get_canvas ()->raise ("osmnode", $top);
	    $top = "osmnodes";
	}
    } 
}


sub toggle_tracks {
    my $self = shift;
    if ($self->{TRACKSHIDDEN}) {
	$self->{TRACKSHIDDEN}  = 0;
    } else {
	$self->{TRACKSHIDDEN} = 1;
    }
    $self->fix_order ();
}

sub toggle_nodes {
    my $self = shift;
    if ($self->{NODESHIDDEN}) {
	$self->{NODESHIDDEN}  = 0;
    } else {
	$self->{NODESHIDDEN} = 1;
    }
    $self->fix_order ();
}

sub toggle_segments {
    my $self = shift;
    if ($self->{SEGMENTSHIDDEN}) {
	$self->{SEGMENTSHIDDEN}  = 0;
    } else {
	$self->{SEGMENTSHIDDEN} = 1;
    }
    $self->fix_order ();
}

sub toggle_ways {
    my $self = shift;
    if ($self->{WAYSHIDDEN}) {
	$self->{WAYSHIDDEN}  = 0;
    } else {
	$self->{WAYSHIDDEN} = 1;
    }
    $self->fix_order ();
}

sub ctowgs84 {
    my $self = shift;
    my $x = shift;
    my $y = shift;

    my $w = $self->get_pixel_width ();
    my $h = $self->get_pixel_height ();
    my ($west, $south, $east, $north) = $self->get_area ();
    my $dx = $east-$west;
    my $dy = $north-$south;

    my $lat = $south + ($h-$y)/$h*$dy;
    my $lon = $west + $x/$w*$dx;

    return ($lat, $lon);
}

sub clamp_to_center_of_tile {
    my $self = shift;
    my $lat = shift;
    my $lon = shift;
    my $scale = shift; # 10 25 50 100 200 400 800 ...
    my $dlat = $self->get_deltalat ();  # 0.005
    my $dlon = $self->get_deltalon ();  # 0.001
    $dlat *= $scale;
    $dlon *= $scale;
#    my $flat = int (1/$dlat);
#    my $flon = int (1/$dlon);
    my $flat = (1/$dlat);
    my $flon = (1/$dlon);
    $lat = int ($lat*$flat+0.5)/$flat;
    if ($lon >= 0) {
	$lon = int ($lon*$flon+0.5)/$flon;
    } else {
	$lon = int ($lon*$flon-0.5)/$flon;
    }
    return ($lat, $lon);
}

sub get_pixel_width {
    my $self = shift;
    return $self->{PIXELWIDTH};
}

sub get_pixel_height {
    my $self = shift;
    return $self->{PIXELHEIGHT};
}


sub get_area {
    my $self = shift;
    my $west = $self->{WEST};
    my $south = $self->{SOUTH};
    my $east = $self->{EAST};
    my $north = $self->{NORTH};
    return ($west, $south, $east, $north);
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

sub fill_cache ($$$) {
    my $self = shift;
    my $max_new = shift || 1;
    my $can_frame = shift;

    my $scale = $self->get_scale ();
    my $deltalat = $self->get_deltalat ();
    my $deltalon = $self->get_deltalon ();
    my $lat = $self->get_lat ();
    my $lon = $self->get_lon ();
    my $size = 10;
    my $anz_seen=0;
    my $anz_new=0;

    print STDERR "Fill Cache for: $scale ($lat,$lon) +- $size tiles, max_new_tiles: $max_new\n";

    for my $scale_fac ( qw( 1 2 4 8 16 32 )){
	for my $test_scale ( ( $scale / $scale_fac, 
			       $scale * $scale_fac )
			     ) {
	    for my $dist ( 0..$size ) { # Start from actual position and get larger area in each loop
		for my $la ( (-$dist..$dist) ) {
		    for my $lo ( (-$dist..$dist) ) {
			#( 10,25,50,100,200,400,800,
			# 1600,3200,6400,12800,25600,51200 ) 
			last if $anz_new >= $max_new;
			$test_scale=10 if $test_scale<25;
			my ($la1, $lo1) = $self->clamp_to_center_of_tile
			    (
			     $lat+($deltalat*$test_scale*$la),
			     $lon+($deltalon*$test_scale*$lo),
			     $scale);
			$la1 = "+$la1" if $la1>0;
			$lo1 = "+$lo1" if $lo1>0;
			my $filename = 
			sprintf( "$ENV{HOME}/.osmpedit/cache/landsat-%d%s%s.jpg",
				 $test_scale,$la1,$lo1);
			#print STDERR "Check File:  $filename\n";
			if ( -s $filename ) {
			    $anz_seen++; # This number is wrong since the inner 
			    # tiles are looked at multiple times
			    next;
			}
			print STDERR "Fill Cache(new:$anz_new,seen:$anz_seen,max_new:$max_new)(dist:$dist): scale:$test_scale ($la1,$lo1)\n";
			my $tile = $self->get_tile ($la1+0.0, $lo1+0.0, $test_scale);
			$anz_new++;
			$anz_seen++;
		    }
		}
	    }
	}
    }
    $self->set_scale ($scale);
    $self->set_center ($lat, $lon);
    $self->display ();
    print STDERR "Fill Cache: existing: $anz_seen\n";
    $can_frame->after( 100000, 
		  sub{ 
		      printf "Timer %s\n",''.localtime(time());
		      $self->fill_cache(4,$can_frame);
		  });
}


return 1;
