# 
# PERL mapweaver module by gary68
#
#
#
#
# Copyright (C) 2011, Gerhard Schwanz
#
# This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 3 of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>
#


package mwGPX ; 

use strict ;
use warnings ;

use OSM::gpx ;

use mwConfig ;
use mwMap ;

use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw (	processGPXFile
		 ) ;



sub processGPXFile {

	my ($ref1, $ref2, $ref3) = readGPXFile ( cv('gpx') ) ;

	my %wptHash = %$ref1 ;
	my %rteHash = %$ref2 ;
	my %trkHash = %$ref3 ;

	my $size = cv('gpxsize') ;
	my $color = cv('gpxcolor') ;

	foreach my $wptNr ( sort { $a <=> $b } keys %wptHash) {
		# print "WPT $wptNr: $wptHash{$wptNr}{'lon'} $wptHash{$wptNr}{'lat'}\n" ;
		if (defined $wptHash{$wptNr}{'name'}) { 
			# print "  name: $wptHash{$wptNr}{'name'}\n" ; 
		}
		if (defined $wptHash{$wptNr}{'ele'}) { 
			# print "  ele: $wptHash{$wptNr}{'ele'}\n" ; 
		}


		my $svgString = "fill=\"$color\" stroke=\"none\" " ;
		my $lon = $wptHash{$wptNr}{'lon'} ;
		my $lat = $wptHash{$wptNr}{'lat'} ;
		drawCircle ($lon, $lat, 1, 3*$size, 0, $svgString, 'gpx') ;

	}

	foreach my $rteNr ( sort { $a <=> $b } keys %rteHash) {
		# print "RTE $rteNr\n" ;

		my @coords = () ;

		foreach my $rteWptNr ( sort { $a <=> $b } keys %{$rteHash{$rteNr}}) {
			# print "   wpt $rteWptNr: $rteHash{$rteNr}{$rteWptNr}{'lon'} $rteHash{$rteNr}{$rteWptNr}{'lat'}\n" ;

			my $svgString = "fill=\"$color\" stroke=\"none\" " ;
			my $lon = $rteHash{$rteNr}{$rteWptNr}{'lon'} ;
			my $lat = $rteHash{$rteNr}{$rteWptNr}{'lat'} ;
			drawCircle ($lon, $lat, 1, 2*$size, 0, $svgString, 'gpx') ;

			my ($x, $y) = convert ($lon, $lat) ;
			push @coords, $x, $y ;
		}

		my $svgString = "" ;

		my $lc = "round" ;
		my $lj = "round" ;

		$svgString = "stroke=\"$color\" stroke-width=\"$size\" stroke-linecap=\"$lc\" fill=\"none\" stroke-linejoin=\"$lj\" " ;

		drawWay (\@coords, 0, $svgString, "gpx", undef) ;
	}

	foreach my $trkNr ( sort { $a <=> $b } keys %trkHash) {
		# print "TRK $trkNr\n" ;
		my %seg ;
		%seg = %{ $trkHash{$trkNr} } ;

		foreach my $segNr ( sort {$a <=> $b} keys %seg) {
			# print "  SEG $segNr\n" ;
			my %points ;
			%points = %{ $seg{$segNr}} ;
			foreach my $ptNr ( sort { $a <=> $b } keys %points) {
				# print "   trkpt $ptNr: $points{$ptNr}{'lon'} $points{$ptNr}{'lat'}\n" ;

				my $svgString = "fill=\"$color\" stroke=\"none\" " ;
				my $lon = $points{$ptNr}{'lon'} ;
				my $lat = $points{$ptNr}{'lat'} ;
				drawCircle ($lon, $lat, 1, $size, 0, $svgString, 'gpx') ;
			}
		}
	}

}





1 ;


