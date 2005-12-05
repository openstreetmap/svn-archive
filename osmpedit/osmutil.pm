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
    my $data = "<osm version='0.2'>
<node lon='$lon' tags='$tags' lat='$lat'/>
</osm>";
    print STDERR "DATA: $data\n";
##    my $resp = "dummy";
    my $uid = curl::put_data ("newnode", $data, $username, $password);
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

    my $data = "<osm version='0.2'>
<node lon='$lon' tags='$tags' uid='$uid'  lat='$lat'/>
v</osm>";

    return update_node_data ($uid, $data, $username, $password);

}

sub update_node_tags {
    my $uid = shift;
    my $tags = shift;
    my $username = shift;
    my $password = shift;

    my $data = get_node ($uid, $username, $password);
    print STDERR "UPDATE DATA $tags: $data\n";
    if ($data) {
	$data =~ s/tags.*?\s/$tags /;
	print STDERR "UPDATE DATA: $data\n";
	return update_node_data ($uid, $data, $username, $password);
    } else {
	return 0;
    }
}


sub get_node {
    my $uid = shift;
    my $username = shift;
    my $password = shift;
    my $resp = curl::get ("node/$uid", $username, $password);
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

sub create_segment {
    my $from = shift;
    my $to = shift;
    my $tags = shift;
    my $username = shift;
    my $password = shift;
    my $data = "<osm version='0.2'>
<segment tags='$tags' from='$from' to='$to'/>
</osm>";
    print STDERR "DATA: $data\n";
##    my $resp = "dummy";
    my $uid = curl::put_data ("newsegment", $data, $username, $password);
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
    return $resp;
}

sub update_segment {
    my $uid = shift;
    my $from = shift;
    my $to = shift;
    my $tags = shift;
    my $username = shift;
    my $password = shift;

    my $data = "<osm version='0.2'>
<segment tags='$tags' from='$from' to='$to' uid='$uid'/>
v</osm>";

    my $uid = curl::put_data ("segment/$uid", $data, $username, $password);
    if ($uid < 0) {
	return 0;
    }
    return 1;
}


return 1;
