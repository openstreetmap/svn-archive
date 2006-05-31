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

package osm;

use FindBin qw($RealBin);
use lib "$RealBin/../perl";


use strict;

use osmnode;
use osmsegment;
use osmutil;

use XML::TokeParser;

sub new {
    my $this = shift;
    my $class = ref($this) || $this;
    my $obj = bless {
	USERNAME => "unspecified",
	PASSWORD => "unspecified",
	UIDTOSEGMENTMAP => {},
	UIDTONODEMAP => {},
	ITEMTOUID => {},
	UIDTOITEM => {},
	SEGCOLOUR => {},
        @_
	}, $class;
    $obj->{SEGCOLOUR}->{"none"} = "white";
    $obj->{SEGCOLOUR}->{"street"} = "lightsteelblue3";
#    $obj->{SEGCOLOUR}->{"primary"} = "orangered";
#    $obj->{SEGCOLOUR}->{"secondary"} = "orangered4";
    $obj->{SEGCOLOUR}->{"motorway"} = "navy";
#    $obj->{SEGCOLOUR}->{"unsurfaced"} = "green";
#    $obj->{SEGCOLOUR}->{"minor"} = "wheat3";
    $obj->{SEGCOLOUR}->{"path"} = "brown";
    $obj->{SEGCOLOUR}->{"train"} = "sienna3";
    return $obj;
}

sub clean {
    my $self = shift;
    $self->{UIDTOSEGMENTMAP} = {};
    $self->{UIDTONODEMAP} = {};
    $self->{ITEMTOUID} = {};
    $self->{UIDTOITEM} = {};
}

sub connect_uid_item {
    my $self = shift;
    my $uid = shift;
    my $item = shift;
    $self->{ITEMTOUID}->{$item} = $uid;
    $self->{UIDTOITEM}->{$uid} = $item;
##    print STDERR "$item --- $uid\n";
}

sub add_node {
    my $self = shift;
    my $n = shift;
##    push @{$self->{NODES}}, $n;
    my $uid = $n->get_uid ();
    $self->{UIDTONODEMAP}->{$uid} = $n;
}

sub get_nodes {
    my $self = shift;
    my  @res = ();
    foreach my $k (keys %{$self->{UIDTONODEMAP}}) {
	my $node = $self->{UIDTONODEMAP}->{$k};
	if ($node) {
	    push @res, $node;
	}
    }
    return @res;
#    return @{$self->{NODES}};
}

sub get_segment_colour {
    my $self = shift;
    my $class = shift;
    my $car = shift;
    my $bike = shift;
    my $foot = shift;
    my $res = "white";
    if ($self->{SEGCOLOUR}->{$class}) {
#	if ($car eq "yes" or $bike eq "yes" or $foot eq "yes") {
	    $res =  $self->{SEGCOLOUR}->{$class};
#	}
#	if ($class eq "train") {
#	    $res = $self->{SEGCOLOUR}->{$class};
#	}
    }
##    print "COL for:$class:$res:\n";
    return $res;
}

sub get_segment_classes {
    my $self = shift;
    return keys %{$self->{SEGCOLOUR}};
}

sub get_segment_keys {
    my $self = shift;
    my %keys;
    foreach my $s ($self->get_segments ()) {
	foreach my $k ($s->get_keys ()) {
	    $keys{$k} = 1;
	}
    }
    return keys %keys;
}


sub get_segment_values {
    my $self = shift;
    my $key = $self->{SEGMENTKEY};
    my %values;
    foreach my $s ($self->get_segments ()) {
	my $v = $s->get_key_value ($key);
	if ($v) {
	    $values{$v} = 1;
	}
    }
    return keys %values;
}



sub get_node {
    my $self = shift;
    my $uid = shift;
    my $node = $self->{UIDTONODEMAP}->{$uid};
    return $node;
}

sub get_segment {
    my $self = shift;
    my $uid = shift;
    my $segment = $self->{UIDTOSEGMENTMAP}->{$uid};
    return $segment;
}

sub get_node_from_item {
    my $self = shift;
    my $item = shift;
    my $uid = $self->{ITEMTOUID}->{$item};
##    print STDERR "UID: $uid\n";
    return $self->get_node ($uid);
}

sub get_segment_from_item {
    my $self = shift;
    my $item = shift;
    my $uid = $self->{ITEMTOUID}->{$item};
    return $self->get_segment ($uid);
}

sub add_segment {
    my $self = shift;
    my $s = shift;
    my $uid = $s->get_uid ();
    $self->{UIDTOSEGMENTMAP}->{$uid} = $s;

    my $from = $s->get_from ();
    my $fromnode = $self->get_node ($from);
    if ($fromnode) {
	$fromnode->add_from ($uid);
    }

    my $to = $s->get_to ();
    my $tonode = $self->get_node ($to);
    if ($tonode) {
	$tonode->add_to ($uid);
    }
}

sub get_segments {
    my $self = shift;
    my  @res = ();
    foreach my $k (keys %{$self->{UIDTOSEGMENTMAP}}) {
	my $seg = $self->{UIDTOSEGMENTMAP}->{$k};
	if ($seg) {
	    push @res, $seg;
	}
    }
    return @res;
}

sub get_segments_connected_to_node {
    my $self = shift;
    my $node_uid = shift;
    my @res = ();
    my $node = $self->get_node ($node_uid);
    if ($node) {
	my @froms = $node->get_froms ();
	foreach my $suid (@froms) {
	    print STDERR "MOVE FROM: $suid\n";
	    my $s = $self->get_segment ($suid);
	    push @res, $s;
	}
    }
    return @res;
}


sub get_position {
    my $self = shift;
    my $uid = shift;
    my @res = ();
    my $n = $self->{UIDTONODEMAP}->{$uid};
    if ($n) {
	my $lat = $n->get_lat ();
	my $lon = $n->get_lon ();
	@res = ($lat, $lon);
    }
    return @res;
}

sub set_username {
    my $self = shift;
    my $val = shift;
    $self->{USERNAME} = $val;
}

sub get_username {
    my $self = shift;
    return $self->{USERNAME};
}

sub set_password {
    my $self = shift;
    my $val = shift;
    $self->{PASSWORD} = $val;
}

sub get_password {
    my $self = shift;
    return $self->{PASSWORD};;
}




sub fetch {
    my $self = shift;
    my $landsat = shift;

    if (not -d "$ENV{HOME}/.osmpedit") {
	mkdir "$ENV{HOME}/.osmpedit";
    }

    if (not -d "$ENV{HOME}/.osmpedit/cache") {
	mkdir "$ENV{HOME}/.osmpedit/cache";
    }

    my $username  = $self->get_username ();
    my $password  = $self->get_password ();

    my ($west, $south, $east, $north) = $landsat->get_area ();

    my $data = curl::grab_osm ($west, $south, $east, $north, 
			       $username, $password);
##    print STDERR "$data\n";

    if ($data) {
	my $filename = "$ENV{HOME}/.osmpedit/cache/lastosm.xml";
	open XML, ">$filename" or die "Could not open $filename: $!";
	print XML "$data";
	close XML;
    } else {
	print STDERR "WARNING: Failed to read OSM data from server\n";
    }
}

sub parse {
    my $self = shift;
    my $landsat = shift;

    $landsat->get_canvas ()->delete ("osmwp");
    $landsat->get_canvas ()->delete ("osmnode");
    $landsat->get_canvas ()->delete ("osmsegment");
    $self->clean ();

    my $filename = "$ENV{HOME}/.osmpedit/cache/lastosm.xml";
    if (-e "$filename" and -s "$filename") {
	print STDERR "Parsing file: $filename\n";
	my $p = XML::TokeParser->new("$filename");
	if (not $p) {
	    print STDERR "WARNING: Could not parse osm data\n";
	    return;
	}
	my $t;
	my $current_node_or_segment = 0;
	while (1) {
	    eval {$t = $p->get_tag() };
	    if ($@) {
		print STDERR "Could not parse file: $@\n";
	    }
	    last unless $t;
	    if ($t->is_start_tag) {
		my $name = "$t->[0]";
##		print STDERR "$name\n";
		if ($name eq "node") {
		    my $attr = $t->attr;
		    my $lat = $attr->{lat};
		    my $lon = $attr->{lon};
		    my $uid = $attr->{uid};
		    print STDERR "NODE $lat $lon $uid\n";
##		    if ($tags) {
#			print STDERR "Node tags: $tags\n";
##		    }
		    my $node = new osmnode;
		    $node->set_lat ($lat);
		    $node->set_lon ($lon);
		    $node->set_uid ($uid);
		    $self->add_node ($node);
		    $current_node_or_segment = $node;
		}
		if ($name eq "tag") {
		    my $attr = $t->attr;
		    my $k = $attr->{k};
		    my $v = $attr->{v};
		    print STDERR "TAG $k: $v\n";
#		    $node->set_tags ($tags);
		    $current_node_or_segment->add_key_value ($k, $v);
		}
		if ($name eq "segment") {
		    my $attr = $t->attr;
		    my $from = $attr->{from};
		    my $to = $attr->{to};
		    my $uid = $attr->{uid};
		    print STDERR "SEGMENT $from $to $uid\n";
		    my $s = new osmsegment;
		    $s->set_from ($from);
		    $s->set_to ($to);
		    $s->set_uid ($uid);
##		    $s->set_tags ($tags);
		    $self->add_segment ($s);
		    $current_node_or_segment = $s;
		}
	    }
	}
    }
}


sub get_segment_canvas_coords {
    my $self = shift;
    my $landsat = shift;
    my $segment = shift;

    my $w = $landsat->get_pixel_width ();
    my $h = $landsat->get_pixel_height ();
    my ($west, $south, $east, $north) = $landsat->get_area ();
    my $dx = $east-$west;
    my $dy = $north-$south;

    my $from = $segment->get_from ();
    my $to = $segment->get_to ();
    my $uid = $segment->get_uid ();

    my ($flat, $flon) = $self->get_position ($from);
    my ($tlat, $tlon) = $self->get_position ($to);

    my $fromoutside = 0;
    my $tooutside = 0;
    if ($flat > $north or $flat < $south or $flon > $east or $flon<$west) {
	$fromoutside = 1;
    }

    if ($tlat > $north or $tlat < $south or $tlon > $east or $tlon<$west) {
	$tooutside = 1;
    }

    if ($fromoutside or $tooutside) { # change when clamping works...
	return ();
    }


##	print STDERR "DRAW SEGMENT: $flat $flon $tlat $tlon\n";

    my $x0 = ($flon-$west)/$dx*$w;
    my $y0 = $h-($flat-$south)/$dy*$h;
    my $x1 = ($tlon-$west)/$dx*$w;
    my $y1 = $h-($tlat-$south)/$dy*$h;
    return ($x0, $y0, $x1, $y1);
}


sub draw {
    my $self = shift;
    my $landsat = shift;

    my $can = $landsat->get_canvas ();
    my $w = $landsat->get_pixel_width ();
    my $h = $landsat->get_pixel_height ();
    my ($west, $south, $east, $north) = $landsat->get_area ();
    my $dx = $east-$west;
    my $dy = $north-$south;

##    $self->fetch_and_parse ($west, $south, $east, $north);
    
    print STDERR "DRAW OSM FOR $north $south $east $west\n";

    $can->delete ("osmsegment");
    $can->delete ("osmnode");

    foreach my $segment ($self->get_segments ()) {
	my $uid = $segment->get_uid ();
	

	my ($x0, $y0, $x1, $y1) = $self->get_segment_canvas_coords ($landsat,
								    $segment);
	next unless ($x0);

	my $colour = "white";
	my $class = $segment->get_class ();
	my $car = $segment->get_car ();
	my $foot = $segment->get_foot ();
	my $bike = $segment->get_bike ();
	if ($class) {
	    my $c = $self->get_segment_colour ($class, $car, $bike, $foot);
	    if ($c) {
		$colour = $c;
	    } else {
		print STDERR "SEGMENT CLASS: $class\n";
	    }
	}

        my $item = $can->create ('line', $x0, $y0, $x1, $y1,
#                      -arrow => "last",
				-fill => $colour,
				 -width => 2,
				-tag => "osmsegment");
	$self->connect_uid_item ($uid, $item);

    }


    foreach my $node ($self->get_nodes ()) {
	my $lat = $node->get_lat ();
	my $lon = $node->get_lon ();
	my $uid = $node->get_uid ();
	if ($lat > $north or $lat < $south or $lon > $east or $lon < $west) {
	    next;
	}
#	print STDERR "DRAW NODE: $lat $lon\n";
	my $x = ($lon-$west)/$dx*$w;
	my $y = $h-($lat-$south)/$dy*$h;
	my $item = $self->draw_node ($can, $x, $y, $node);
	$self->connect_uid_item ($uid, $item);
    }
}

sub update_segment_key_value {
    my $self = shift;
    my $item = shift;
    my $key = shift;
    my $value = shift;
    my $s = $self->get_segment_from_item ($item);
    if ($s) {
	$s->add_key_value ($key, $value);
	$s->print ();
	my $username  = $self->get_username ();
	my $password  = $self->get_password ();
	$s->update_osm_segment ($username, $password);
    }
}

##sub update_segment_class {
##    my $self = shift;
##    my $item = shift;
##    my $s = $self->get_segment_from_item ($item);
##    if ($s) {
##	$s->print ();
##	my $username  = $self->get_username ();
##	my $password  = $self->get_password ();
##	$s->update_osm_segment ($username, $password);
##    }
##}

sub update_segment_colour {
    my $self = shift;
    my $item = shift;
    my $can = shift;

    my $s = $self->get_segment_from_item ($item);
    if ($s) {
	my $class = $s->get_class ();
	my $car = $s->get_car ();
	my $foot = $s->get_foot ();
	my $bike = $s->get_bike ();
	my $c = $self->get_segment_colour ($class, $car, $bike, $foot);
	$can->itemconfigure ($item, "-fill", $c);
    }
}

sub update_segments_key_colour {
    my $self = shift;
    my $key = shift;
    my $can = shift;
    $self->{SEGMENTKEY} = $key;
    foreach my $s ($self->get_segments ()) {
	my $uid = $s->get_uid ();
	my $item = $self->{UIDTOITEM}->{$uid};
	if ($s->is_key ($key)) {
	    my $tags = $s->get_tags ();
##	    print STDERR "IS KEY! $key - $tags\n";
	    $can->itemconfigure ($item, "-fill", "yellow");
	} else {
	    $self->update_segment_colour ($item, $can);
	}
    }

}

sub update_segments_value_colour {
    my $self = shift;
    my $value = shift;
    my $can = shift;
    my $key = $self->{SEGMENTKEY};
    $self->update_segments_key_colour ($key, $can);
    foreach my $s ($self->get_segments ()) {
	my $uid = $s->get_uid ();
	my $item = $self->{UIDTOITEM}->{$uid};
	if ($s->get_key_value ($key) eq $value) {
	    my $tags = $s->get_tags ();
##	    print STDERR "IS VALUE! $value - $tags\n";
	    $can->itemconfigure ($item, "-fill", "green");
	}
    }
}

sub draw_node {
    my $self = shift;
    my $can = shift;
    my $x = shift;
    my $y = shift;
    my $node = shift;
    my $r = 2;
    my $colour = "black";
    my $tag = "osmnode";
#    if ($node and $node->have_key_values ()) {
#	$colour = "yellow";
#	$r = 4;
#	$tag = "osmwp";
#    }
    my $obj = $can->create ('oval', $x-$r, $y-$r, $x+$r, $y+$r,
			    -fill => $colour,
			    -outline => $colour,
			    -tag => $tag);
    return $obj;
}


sub move_node {
    my $self = shift;
    my $item = shift;
    my $x = shift;
    my $y = shift;
    my $can = shift;

    my $node = $self->get_node_from_item ($item);
    if ($node) {
	my @froms = $node->get_froms ();
	my @tos = $node->get_tos ();

	foreach my $suid (@froms) {
	    my $sitem = $self->{UIDTOITEM}->{$suid};
	    if ($item) {
		my ($x0, $y0, $x1, $y1) = $can->coords ($sitem);
		$can->coords ($sitem, $x, $y, $x1, $y1);
	    }
	}

	foreach my $suid (@tos) {
	    my $sitem = $self->{UIDTOITEM}->{$suid};
	    if ($item) {
		my ($x0, $y0, $x1, $y1) = $can->coords ($sitem);
		$can->coords ($sitem, $x0, $y0, $x, $y);
	    }
	}

	
	my $r = 2;
	if ($node->have_key_values ()) {
	    $r = 4;
	}
	$can->coords ($item, $x-$r, $y-$r, $x+$r, $y+$r);
    }
}


sub create_node {
    my $self = shift;
    my $lat = shift;
    my $lon = shift;
    my $username  = $self->get_username ();
    my $password  = $self->get_password ();
    my $node = new osmnode;
    $node->set_lat ($lat);
    $node->set_lon ($lon);
    $node->add_key_value ("editor", "osmpedit-svn");
    my $tags = $node->get_tags ();

    my $uid = osmutil::create_node ($lat, $lon, $tags, $username, $password);
    if ($uid) {
	$node->set_uid ($uid);
	$self->add_node ($node);
    }
    return $uid;
}

sub create_segment {
    my $self = shift;
    my $from = shift;
    my $to = shift;
    my $class = shift;
    my $username  = $self->get_username ();
    my $password  = $self->get_password ();
    my $s = new osmsegment;
    $s->set_from ($from);
    $s->set_to ($to);

    print STDERR "Create segment with class: $class\n";

    if ($class) {
	$s->add_key_value ("class", $class);
    }
    $s->add_key_value ("editor", "osmpedit-svn");

    my $tags = $s->get_tags ();
    
    print STDERR "TAGS: $tags\n";

    my $uid = osmutil::create_segment ($from, $to, $tags, $username, $password);

    if ($uid) {
	$s->set_uid ($uid);
	$self->add_segment ($s);
    }
    return $uid;
}

sub update_node {
    my $self = shift;
    my $uid = shift;
    my $lat = shift;
    my $lon = shift;

    my $node = $self->get_node_from_item ($uid);
    my $username  = $self->get_username ();
    my $password  = $self->get_password ();

    $node->set_lat ($lat);
    $node->set_lon ($lon);
    $node->add_key_value ("editor", "osmpedit-svn");

    return $node->upload_osm_node ($username, $password);
}

sub delete {
    my $self = shift;
    my $obj = shift;
    my $can = shift;

    my $uid = $self->{ITEMTOUID}->{$obj};
    my $username  = $self->get_username ();
    my $password  = $self->get_password ();

    if ($uid) {
	print STDERR "DELETE IN SERVER: $uid\n";
	my $node = $self->get_node ($uid);

	my $username  = $self->get_username ();
	my $password  = $self->get_password ();
	my $resp = "";
	
	if ($node) {
	    my @froms = $node->get_froms ();
	    my @tos = $node->get_tos ();
	    if ($self->one_segment_exists (@froms) or 
		$self->one_segment_exists (@tos)) {
		print STDERR "Cannot delete node that is connected to a segment\n";
		return 0;
	    } else {
		print STDERR "Trying to delete node\n";
		$resp = osmutil::delete_node ($uid, $username, $password);
		$self->{UIDTONODEMAP}->{$uid} = 0;
	    }
	} else {
	    my $seg = $self->get_segment ($uid);
	    if ($seg) {
		print STDERR "Trying to delete segment\n";
		$resp = osmutil::delete_segment ($uid, $username, $password);
		$self->{UIDTOSEGMENTMAP}->{$uid} = 0;
	    }
	}
	print STDERR "RESP: $resp\n";
	if (not $resp) {
	    print STDERR "WARNING: Could not delete $obj\n";
	    return 0;
	}
	return 1;
    } else {
	print STDERR "WARNING: Could not delete $obj\n";
	return 0;
    }
}

sub key_value_hash {
    my $self = shift;
    my $item = shift;
    my $node = $self->get_node_from_item ($item);
#    print "NODE: ", $node->get_uid (), "\n";
    if ($node) {
#	print STDERR "Return key value hash ", $node->get_tags (), "\n";
	return $node->key_value_hash ();
    }
    my $seg = $self->get_segment_from_item ($item);
    if ($seg) {
	return $node->key_value_hash ();
    }
    return 0;
}

sub toggle_colour {
    my $self = shift;
    my $can = shift;
    my $class = shift;
    my @items = $can->find ("withtag", "osmwp");
    print STDERR "toggle_colour: $class\n";
    foreach my $item (@items) {
	my $keyvalues = $self->key_value_hash ($item);
	next unless ($keyvalues);
	if ($keyvalues->{"class"} eq $class) {
	    my $c = $can->itemcget ($item, -fill);
	    print STDERR "CURRENT COLOUR: $c\n";
	    my $col = "red";
	    if ($c eq "red") {
		$col = "yellow";
	    }
	    $can->itemconfigure ($item, -fill => $col);
	}
    }
}

sub one_segment_exists {
    my $self = shift;
    my @sids = @_;
    foreach my $uid (@sids) {
	print STDERR "CHECKID: $uid\n";
	if ($self->{UIDTOSEGMENTMAP}->{"$uid"}) {
	    return 1;
	}
    }
    return 0;
}

return 1;
