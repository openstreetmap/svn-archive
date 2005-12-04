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

package osmnode;

use FindBin qw($RealBin);
use lib "$RealBin/../perl";

use XML::TokeParser;

use strict;

use osmutil;

sub new {
    my $this = shift;
    my $class = ref($this) || $this;
    bless {
	LAT => 0,
	LON => 0,
	UID => 0,
	TAGS => "",
	FROMS => [],
	TOS => [],
	KEYVALUE => {},
        @_
	}, $class;
}


sub add_key_value {
    my $self = shift;
    my $key = shift;
    my $value = shift;
##    print STDERR "SETTAGS: $key - $value\n";
    $self->{KEYVALUE}->{$key} = $value;
}

sub key_value_hash {
    my $self = shift;
    return $self->{KEYVALUE}
}

sub have_key_values {
    my $self = shift;
    if ($self->{TAGS}) {
	return 1;
    } else {
	return 0;
    }
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

sub set_lon {
    my $self = shift;
    my $val = shift;
    $self->{LON} = $val;
}

sub get_lon {
    my $self = shift;
    return $self->{LON};;
}

sub set_uid {
    my $self = shift;
    my $val = shift;
    $self->{UID} = $val;
}

sub get_uid {
    my $self = shift;
    return $self->{UID};;
}

sub set_tags {
    my $self = shift;
    my $val = shift;
    $self->{TAGS} = $val;
##    print STDERR "VAL:$val:\n";
    my @items = split (";", $val);
    foreach my $item (@items) {
#	print STDERR "ITEM:$item\n";
	my ($name, $val) = split ("=", $item);
	$self->add_key_value ($name, $val);
    }
}

sub get_tags {
    my $self = shift;
    return $self->{TAGS};;
}

sub add_from {
    my $self = shift;
    my $uid = shift;
#    print STDERR "ADDING FROM: $uid\n";
    push @{$self->{FROMS}}, $uid;
}

sub add_to {
    my $self = shift;
    my $uid = shift;
#    print STDERR "ADDING TO: $uid\n";
    push @{$self->{TOS}}, $uid;
}

sub get_froms {
    my $self = shift;
    return @{$self->{FROMS}};
}

sub get_tos {
    my $self = shift;
    return @{$self->{TOS}};
}


sub print {
    my $self = shift;
    my $lat = $self->get_lat ();
    my $lon = $self->get_lon ();
    my $uid = $self->get_uid ();
    my $tags = $self->get_tags ();
    print "OSMNODE: $lat $lon $uid '$tags'\n";
}

sub parse_waypoint {
    my $self = shift;
    my $filename = shift;
    my $class = shift;
    my $creator = shift;
    if (-f "$filename") {
	my $p = XML::TokeParser->new("$filename");
	if (not $p) {
	    print STDERR "WARNING: Could not parse waypoint\n";
	    return 0;
	}
	my $wpname = "unspecified";
	while (my $t = $p->get_tag()) {
	    if ($t->is_start_tag) {
		my $name = "$t->[0]";
		if ($name eq "wpt") {
		    my $attr = $t->attr;
		    my $lat = $attr->{lat};
		    my $lon = $attr->{lon};
		    $self->set_lat ($lat);
		    $self->set_lon ($lon);
		    next;
		}
		if ($name eq "name") {
		    $wpname = $p->get_trimmed_text ("/name");
		}
	    }
	    if ($t->is_end_tag) {
		my $name = "$t->[0]";
		if ($name eq "/wpt") {
		    $self->set_tags ("name=$wpname;class=$class;creator=$creator");
		}
	    }
	}
    }
    return 1;
}

sub upload_osm_node {
    my $self = shift;
    my $username = shift;
    my $password = shift;

    my $lat = $self->get_lat ();
    my $lon = $self->get_lon ();
    my $tags = $self->get_tags ();

    my $uid = $self->get_uid ();
    if ($uid) {
	return osmutil::update_node ($uid, $lat, $lon, $tags, $username,
				     $password);
    } else {
	my $uid = osmutil::create_node ($lat, $lon, $tags, $username,
					$password);
	if ($uid < 0) {
	    return 0;
	} else {
	    return $uid;
	}
    }
}


return 1;
