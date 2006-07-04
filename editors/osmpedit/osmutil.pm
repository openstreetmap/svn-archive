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

package osmutil;

use FindBin qw($RealBin);
use lib "$RealBin/../perl";

use curl;

use strict;

sub create_node {
    my $lat = shift;
    my $lon = shift;
    my $tags = shift;
    my $username = shift;
    my $password = shift;

##    print STDERR "NEW NODE: $lat $lon\n";
    my $data = "<osm version='0.3'>\n";
    $data .= "  <node id='0' lon='$lon' lat='$lat'>\n";
    foreach my $k (keys %{$tags}) {
	my $val = $tags->{$k};
	$data .= "    <tag k=\"$k\" v=\"$val\"/>\n";
    }
    $data .= "  </node>\n";
    $data .= "</osm>\n";
    print STDERR "DATA: $data\n";
##    my $resp = "dummy";
    my $uid = curl::put_data ("node/0", $data, $username, $password);
    if ($uid < 0) {
	$uid = 0;
    }
    return $uid;
}

sub update_node_data {
    my $uid = shift;
    my $data = shift;
    my $username = shift;
    my $password = shift;

    if ($uid > 0) {
	print STDERR "DATA: $data\n";
	my $resp = curl::put_data ("node/$uid", $data, $username, $password);
	if ($resp < 0) {
	    return 0;
	}
    } else {
	return 0;
    }
    return 1;

}

sub update_node {
    my $uid = shift;
    my $lat = shift;
    my $lon = shift;
    my $tags = shift;
    my $username = shift;
    my $password = shift;

    my $data = "<osm version='0.3'>\n";
    $data .= "  <node lon='$lon' id='$uid'  lat='$lat'>\n";
    foreach my $k (keys %{$tags}) {
	my $val = $tags->{$k};
	$data .= "    <tag k=\"$k\" v=\"$val\"/>\n";
    }
    $data .= "  </node>\n";
    $data .= "</osm>";

    return update_node_data ($uid, $data, $username, $password);

}

sub get_node {
    my $uid = shift;
    my $username = shift;
    my $password = shift;
    my $resp = curl::get ("node/$uid", $username, $password);
    return $resp;
}

sub get_node_history {
    my $uid = shift;
    my $username = shift;
    my $password = shift;
    my $resp = curl::get ("node/$uid/history", $username, $password);
    return $resp;
}

sub get_nodes {
    my $username = shift;
    my $password = shift;
    my @uids = @_;
    my $uids = join (",", @uids);
    my $resp = curl::get ("nodes?nodes=$uids", $username, $password);
    return $resp;
}

sub delete_node {
    my $uid = shift;
    my $username = shift;
    my $password = shift;

    my $resp = curl::delete ("node/$uid", $username, $password);
    return $resp;
}

sub get_segment {
    my $uid = shift;
    my $username = shift;
    my $password = shift;
    my $resp = curl::get ("segment/$uid", $username, $password);
    return $resp;
}

sub get_segment_history {
    my $uid = shift;
    my $username = shift;
    my $password = shift;
    my $resp = curl::get ("segment/$uid/history", $username, $password);
    return $resp;
}

sub get_segment_ways {
    my $uid = shift;
    my $username = shift;
    my $password = shift;
    my $resp = curl::get ("segment/$uid/ways", $username, $password);
    return $resp;
}

sub get_segment_areas {
    my $uid = shift;
    my $username = shift;
    my $password = shift;
    my $resp = curl::get ("segment/$uid/areas", $username, $password);
    return $resp;
}

sub create_segment {
    my $from = shift;
    my $to = shift;
    my $tags = shift;
    my $username = shift;
    my $password = shift;
    my $data = "<osm version='0.3'>\n";
    $data .= "  <segment id='0' from='$from' to='$to'>\n";
    foreach my $k (keys %{$tags}) {
	my $val = $tags->{$k};
	$data .= "    <tag k=\"$k\" v=\"$val\"/>\n";
    }
    $data .= "  </segment>\n";
    $data .= "</osm>\n";
    print STDERR "DATA: $data\n";
##    my $resp = "dummy";
    my $uid = curl::put_data ("segment/0", $data, $username, $password);
    if ($uid < 0) {
	$uid = 0;
    }
    return $uid;
}

sub delete_segment {
    my $uid = shift;
    my $username = shift;
    my $password = shift;

    my $resp = curl::delete ("segment/$uid", $username, $password);
    if (not $resp) {
	print STDERR "WARNING osmutil: Deletion of segment $uid failed\n";
    }
    return $resp;
}

sub update_segment {
    my $uid = shift;
    my $from = shift;
    my $to = shift;
    my $tags = shift;
    my $username = shift;
    my $password = shift;

    my $data = "<osm version='0.3'>\n";
    $data .= "  <segment from='$from' to='$to' id='$uid'>\n";
    foreach my $k (keys %{$tags}) {
	my $val = $tags->{$k};
	$data .= "    <tag k=\"$k\" v=\"$val\"/>\n";
    }
    $data .= "  </segment>\n";
    $data .= "</osm>\n";

    my $uid = curl::put_data ("segment/$uid", $data, $username, $password);
    if ($uid < 0) {
	return 0;
    }
    return 1;
}


sub get_way {
    my $uid = shift;
    my $username = shift;
    my $password = shift;
    my $resp = curl::get ("way/$uid", $username, $password);
    return $resp;
}

sub get_way_history {
    my $uid = shift;
    my $username = shift;
    my $password = shift;
    my $resp = curl::get ("way/$uid/history", $username, $password);
    return $resp;
}

sub create_way {
    my $segs = shift;
    my $tags = shift;
    my $username = shift;
    my $password = shift;
    my $data = "<osm version=\"0.3\">\n";
    $data .= "  <way id=\"0\">\n";
    foreach my $s (@{$segs}) {
	$s =~ s/s//;
	$data .= "    <seg id=\"$s\"/>\n";
    }
    foreach my $k (keys %{$tags}) {
	my $val = $tags->{$k};
	$data .= "    <tag k=\"$k\" v=\"$val\"/>\n";
    }
    $data .= "  </way>\n";
    $data .= "</osm>\n";
    print STDERR "DATA: $data\n";
##    my $resp = "dummy";
    my $uid = curl::put_data ("way/0", $data, $username, $password);
    if ($uid < 0) {
	$uid = 0;
    }
    return $uid;
}

sub delete_way {
    my $uid = shift;
    my $username = shift;
    my $password = shift;

    my $resp = curl::delete ("way/$uid", $username, $password);
    if (not $resp) {
	print STDERR "WARNING osmutil: Deletion of way $uid failed\n";
    }
    return $resp;
}


sub update_way {
    my $uid = shift;
    my $segs = shift;
    my $tags = shift;
    my $username = shift;
    my $password = shift;

    my $data = "<osm version='0.3'>\n";
    $data .= "  <way id='$uid'>\n";
    foreach my $s (@{$segs}) {
	$s =~ s/s//;
	$data .= "    <seg id=\"$s\"/>\n";
    }
    foreach my $k (keys %{$tags}) {
	my $val = $tags->{$k};
	$data .= "    <tag k=\"$k\" v=\"$val\"/>\n";
    }
    $data .= "  </way>\n";
    $data .= "</osm>\n";

    my $uid = curl::put_data ("way/$uid", $data, $username, $password);
    if ($uid < 0) {
	return 0;
    }
    return 1;
}


return 1;
