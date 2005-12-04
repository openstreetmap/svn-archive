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

package trackpoint;

use FindBin qw($RealBin);
use lib "$RealBin/../perl";

use strict;


sub new {
    my $this = shift;
    my $class = ref($this) || $this;
    bless {
	LAT => 0,
	LON => 0,
	TIME => "",
	COURSE => 0,
	SPEED => 0,
	FIX => "",
	SAT => 0,
	HDOP => 0,
        @_
	}, $class;
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

sub print {
    my $self = shift;
    my $lat = $self->get_lat ();
    my $lon = $self->get_lon ();
    print "TP: $lat $lon\n";
}


return 1;
