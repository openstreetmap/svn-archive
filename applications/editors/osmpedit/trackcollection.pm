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

package trackcollection;

use FindBin qw($RealBin);
use lib "$RealBin/../perl";

use track;

use strict;

sub new {
    my $this = shift;
    my $class = ref($this) || $this;
    bless {
	TRACKS => [],
	HIDDEN => 0,
        @_
	}, $class;
}

sub add_track {
    my $self = shift;
    my $t = shift;
    push @{$self->{TRACKS}}, $t;
}

sub get_tracks {
    my $self = shift;
    return @{$self->{TRACKS}};
}

sub get_n_tracks () {
    my $self = shift;
    return $#{$self->{TRACKS}}+1;
}

sub get_center {
    my $self = shift;
    my $n = 0;
    my $lat = 0;
    my $lon = 0;
    foreach my $t ($self->get_tracks ()) {
	my ($la, $lo) = $t->get_center ();
	$lat += $la;
	$lon += $lo;
	$n++;
    }
    if ($n > 0) {
	$lat /= $n;
	$lon /= $n;
    }
    return ($lat, $lon);
}


sub draw {
    my $self = shift;
    my $landsat = shift;
    $landsat->get_canvas ()->delete ("track");
    foreach my $t ($self->get_tracks ()) {
	$t->draw ($landsat);
    }
}


return 1;
