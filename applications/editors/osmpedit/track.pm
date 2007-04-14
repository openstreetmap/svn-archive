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

use Carp qw(confess);

package track;

use FindBin qw($RealBin);
use lib "$RealBin/../perl";

use trackpoint;

use XML::TokeParser;

use strict;


sub new {
    my $this = shift;
    my $class = ref($this) || $this;
    bless {
	TRACK => [],
	MAXLAT => -1000000,
	MINLAT => 1000000,
	MAXLON => -1000000,
	MINLON => 1000000,
        @_
	}, $class;
}

sub add_track_point {
    my $self = shift;
    my $tp = shift;
    my $lat = $tp->get_lat ();
    my $lon = $tp->get_lon ();
    $self->{MAXLAT} = $lat if ($lat > $self->{MAXLAT});
    $self->{MINLAT} = $lat if ($lat < $self->{MINLAT});
    $self->{MAXLON} = $lon if ($lon > $self->{MAXLON});
    $self->{MINLON} = $lon if ($lon < $self->{MINLON});
    push @{$self->{TRACK}}, $tp;
}

sub get_track_points {
    my $self = shift;
    return @{$self->{TRACK}};
}

sub get_center {
    my $self = shift;
    return (($self->{MAXLAT}+$self->{MINLAT})/2, 
	    ($self->{MAXLON}+$self->{MINLON})/2);
}

sub parse_file {
    my $self = shift;
    my $filename = shift;
    print STDERR "Parse track: $filename\n";
    my $p;
    eval {
       $p = XML::TokeParser->new("$filename");
    };
    if ( $@ ) {
       print STDERR "WARNING: Could not parse file: $filename: $@\n";
       return;
    }
    if (not $p) {
	print STDERR "WARNING: Could not parse file: $filename\n";
	return;
    }
    my $trkpt;
    while (1) {
	eval {
	    $trkpt = $p->get_tag("trkpt");
	};
	warn "error in gttrck(): $@" if $@;
	last unless $trkpt;
	my $attr = $trkpt->attr;
	my $lat = $attr->{lat};
	my $lon = $attr->{lon};
#	print STDERR "$lat $lon\n";
	my $tp = new trackpoint ();
	$tp->set_lat ($lat);
	$tp->set_lon ($lon);
	$self->add_track_point ($tp);
    }
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
    
##    print STDERR "$north $south $east $west\n";

    foreach my $t ($self->get_track_points ()) {
	my $lat = $t->get_lat ();
	my $lon = $t->get_lon ();
	if ($lat > $north or $lat < $south or $lon > $east or $lon < $west) {
	    next;
	}

##	print STDERR "DRAW: $lat $lon\n";
	my $x = ($lon-$west)/$dx*$w;
	my $y = $h-($lat-$south)/$dy*$h;
	$self->draw_point ($can, $x, $y);
    }
}

sub draw_point {
    my $self = shift;
    my $can = shift;
    my $x = shift;
    my $y = shift;
    my $r = 1;
    my $obj = $can->create ('oval', $x-$r, $y-$r, $x+$r, $y+$r,
			    -fill => "cyan",
			    -outline => "cyan",
			    -tag => "track");
}


return 1;
