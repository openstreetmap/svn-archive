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

package osmsegment;

use FindBin qw($RealBin);
use lib "$RealBin/../perl";

use strict;

# class: path, unsurfaced, minor, estate, street, secondary, primary or motorway
#foot: has to be no, unofficial or yes
# horse: same as foot
# bike: same as foot
# car: no or yes


sub new {
    my $this = shift;
    my $class = ref($this) || $this;
    my $obj = bless {
	FROM => 0,
	TO => 0,
	UID => 0,
	TAGS => "",
	KEYVALUE => {},
        @_
	}, $class;
    return $obj;
}

sub add_key_value {
    my $self = shift;
    my $key = shift;
    my $value = shift;

    $self->{KEYVALUE}->{$key} = $value;
    if ($key eq "class" and $value eq "motorway") {
	$self->add_key_value ("car", "yes");
    }
    if ($key eq "class" and $value eq "street") {
	$self->add_key_value ("car", "yes");
    }
    if ($key eq "class" and $value eq "path") {
	$self->add_key_value ("foot", "yes");
	$self->add_key_value ("bike", "yes");
    }
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

sub set_from {
    my $self = shift;
    my $val = shift;
    $self->{FROM} = $val;
}

sub get_from {
    my $self = shift;
    return $self->{FROM};;
}

sub set_to {
    my $self = shift;
    my $val = shift;
    $self->{TO} = $val;
}

sub get_to {
    my $self = shift;
    return $self->{TO};
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
##	print STDERR "TAG ITEM: $item\n";
	my ($name, $val) = split ("=", $item);
	$self->add_key_value ($name, $val);
    }
}

sub get_tags {
    my $self = shift;
    my $res = "";
    foreach my $k (keys %{$self->{KEYVALUE}}) {
	my $val = $self->{KEYVALUE}->{$k};
	$res .= "$k=$val;";
    }
    return $res;
}

sub get_class {
    my $self = shift;
    return $self->{KEYVALUE}->{"class"};
}

sub get_car {
    my $self = shift;
    return $self->{KEYVALUE}->{"car"};
}

sub get_bike {
    my $self = shift;
    return $self->{KEYVALUE}->{"bike"};
}

sub get_foot {
    my $self = shift;
    return $self->{KEYVALUE}->{"foot"};
}

sub get_name {
    my $self = shift;
    return $self->{KEYVALUE}->{"name"};
}


sub print {
    my $self = shift;
    my $from = $self->get_from ();
    my $to = $self->get_to ();
    my $uid = $self->get_uid ();
    my $tags = $self->get_tags ();
    print "OSMSEGMENT: $from $to $uid '$tags'\n";
}

sub update_osm_segment {
    my $self = shift;
    my $username = shift;
    my $password = shift;

    my $from = $self->get_from ();
    my $to = $self->get_to ();
    my $tags = $self->get_tags ();

    my $uid = $self->get_uid ();
    if ($uid) {
	return osmutil::update_segment ($uid, $from, $to, $tags, $username,
					$password);
    }
}


return 1;
