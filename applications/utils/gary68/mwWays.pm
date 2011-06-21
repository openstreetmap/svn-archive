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


package mwWays ; 

use strict ;
use warnings ;

use mwConfig ;
use mwFile ;
use mwRules ;
use mwMap ;
use mwMisc ;

use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw ( 	processWays 

		 ) ;


sub processWays {

	print "drawing ways/areas...\n" ;

	my $nodesRef; my $tagRef ;
	($nodesRef, $tagRef) = getWayPointers () ;

	foreach my $wayId (keys %$nodesRef) {
		my @tags = @{ $$tagRef{$wayId} } ;
		my $tagsString = "" ;

		# WAYS

		my $ruleRef = getWayRule (\@tags) ;
		if (defined $ruleRef) {
			my @nodes = @{ $$nodesRef{ $wayId } } ;

			my $layer = getValue ("layer", $$tagRef{$wayId}) ;
			if ( ! defined $layer ) { $layer = 0 ; }

			# TODO check for numeric!!!

			if ( ( $$ruleRef{'svgstringtop'} ne "" ) or ( $$ruleRef{'svgstringbottom'} ne "" ) ) {
				# TODO individual (NEEDS sizes anyway!!! for layers... or automatic?)
			}
			else {

				# top (actual way)
				my $size = $$ruleRef{'size'} ;
				my $color = $$ruleRef{'color'} ;
				my $svgString = "stroke=\"$color\" stroke-width=\"$size\" stroke-linecap=\"round\" fill=\"none\" stroke-linejoin=\"round\"" ;
				drawWay ( \@nodes, 1, $svgString, undef, $layer + $size/100 ) ;

				# bottom (border)
				$size = 2 * $$ruleRef{'bordersize'} + $$ruleRef{'size'} ;
				$color = $$ruleRef{'bordercolor'} ;
				$svgString = "stroke=\"$color\" stroke-width=\"$size\" stroke-linecap=\"round\" fill=\"none\" stroke-linejoin=\"round\"" ;
				drawWay ( \@nodes, 1, $svgString, undef, $layer -0.3 + $size/100 ) ;
			}

			# LABEL WAY

		}

		# AREAS

		$ruleRef = getAreaRule (\@tags) ;
		if (defined $ruleRef) {
			my $color = $$ruleRef{'color'} ;
			my $icon = $$ruleRef{'icon'} ;
			my $base = $$ruleRef{'base'} ;
			my $svgString = $$ruleRef{'svgstring'} ;
			my @nodes = @{ $$nodesRef{ $wayId } } ;
			my @ways = [@nodes] ;

			if ($svgString eq "") {
				$svgString = "fill=\"$color\" " ;
			}

			if ($base eq "yes") {
				drawArea ($svgString, $icon, \@ways, 1, "base") ;
			}
			else {
				drawArea ($svgString, $icon, \@ways, 1, "area") ;
			}

		} # Area
	}
}

1 ;


