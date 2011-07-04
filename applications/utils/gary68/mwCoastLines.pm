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


package mwCoastLines ; 

use strict ;
use warnings ;
use Math::Polygon ;
use List::Util qw[min max] ;

use mwMap ;
use mwFile ;
use mwConfig ;
use mwMisc ;

use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw ( 	processCoastLines

		 ) ;


sub nearestPoint {
#
# accepts x/y coordinates and returns nearest point on border of map to complete cut coast ways
#
	my $ref = shift ;
	my $x = $ref->[0] ;
	my $y = $ref->[1] ;
	my $xn ; my $yn ;
	my $min = 99999 ;
	# print "  NP: initial $x $y\n" ;
	my ($xmax, $ymax) = getDimensions() ;
	# print "  NP: dimensions $xmax $ymax\n" ;
	if ( abs ($xmax-$x) < $min) { # right
		$xn = $xmax ;
		$yn = $y ; 
		$min = abs ($xmax-$x) ;
	}
	if ( abs ($ymax-$y) < $min) { # bottom
		$xn = $x ;
		$yn = $ymax ; 
		$min = abs ($ymax-$y) ;
	}
	if ( abs ($x) < $min) { # left
		$xn = 0 ;
		$yn = $y ; 
		$min = abs ($x) ;
	}
	if ( abs ($y) < $min) { # top
		$xn = $x ;
		$yn = 0 ; 
	}
	# print "  NP: final $xn $yn\n" ;
	my @a = ($xn, $yn) ;
	return (\@a) ;
}



sub nextPointOnBorder {
#
# accepts x/y coordinates and returns next point on border - to complete coast rings with other polygons and corner points
# hints if returned point is a corner
#
	# right turns
	my ($x, $y) = @_ ;
	my ($xn, $yn) ;
	my $corner = 0 ;
	my ($xmax, $ymax) = getDimensions() ;
	if ($x == $xmax) { # right border
		if ($y < $ymax) {
			$xn = $xmax ; $yn = $y + 1 ;
		}
		else {
			$xn = $xmax - 1 ; $yn = $ymax ;
		}
	}
	else {
		if ($x == 0) { # left border
			if ($y > 0) {
				$xn = 0 ; $yn = $y - 1 ;
			}
			else {
				$xn = 1 ; $yn = 0 ;
			}
		}
		else {
			if ($y == $ymax) { # bottom border
				if ($x > 0) {
					$xn = $x - 1 ; $yn = $ymax ;
				}
				else {
					$xn = 0 ; $yn = $ymax - 1 ; 
				}
			}
			else {
				if ($y == 0) { # top border
					if ($x < $xmax) {
						$xn = $x + 1 ; $yn = 0 ;
					}
					else {
						$xn = $xmax ; $yn = 1 ; 
					}
				}
			}
		}
	}
	# print "NPOB: $x, $y --- finito $xn $yn\n" ;

	if ( ($xn == 0) and ($yn == 0) ) { $corner = 1 ; }
	if ( ($xn == 0) and ($yn == $ymax) ) { $corner = 1 ; }
	if ( ($xn == $xmax) and ($yn == 0) ) { $corner = 1 ; }
	if ( ($xn == $xmax) and ($yn == $ymax) ) { $corner = 1 ; }

	return ($xn, $yn, $corner) ;
}

# ---------------------------------------------------------------------------------

sub processCoastLines {
#
#
#
	print "check and process coastlines...\n" ;

	my $ref = shift ; # ref to all coast ways
	my @allWays = @$ref ;

	if (cv('debug') eq "1") { 
		print "COAST: " . scalar (@allWays) . " coast ways initially found.\n" ;
		print "COAST: ways: @allWays\n\n" ;
	}


	my ($lonRef, $latRef) = getNodePointers() ;
	my ($nodesRef, $tagRef) = getWayPointers() ;

	# check coast ways. eliminate invisible ways. eliminate points outside map.
	my @newWays = () ;
	foreach my $w ( @allWays ) {
		my @nodes = @{ $$nodesRef{ $w } } ;

		my $allIn = 1 ;
		my $allOut = 1 ; 
		foreach my $n ( @nodes ) {
			if ( pointInMap ($n) ) {
				$allOut = 0 ;
			}
			else {
				$allIn = 0 ;
			}
		}

		if ( $allIn ) {
			# use way as it is	
			push @newWays, $w ;
			if ( cv ('debug') eq "1" ) { print "COAST: way $w will be used unmodified.\n" ; }
		}
		elsif ( $allOut) {
			# do nothing
			if ( cv ('debug') eq "1" ) { print "COAST: way $w will NOT be used. outside map.\n" ; }
		}
		else {
			# eliminate all outside nodes at start and end of way, then use new way
			
			# eliminate outsides at start
			while ( (scalar @nodes >= 1) and ( ! pointInMap ($nodes[0]) ) ) {
				shift @nodes ;
			}

			# eliminate outsides at end
			while ( (scalar @nodes >= 1) and ( ! pointInMap ($nodes[-1]) ) ) {
				pop @nodes ;
			}

			if ( scalar @nodes >= 2 ) {
				@{ $$nodesRef{$w}} = @nodes ;
				push @newWays, $w ;
				if ( cv ('debug') eq "1" ) { print "COAST: modified way $w will be used.\n" ; }
			}
			else {
				if ( cv ('debug') eq "1" ) { print "COAST: way $w too short now.\n" ; }
			}

		}		

	}

	@allWays = @newWays ;



	if (cv('debug') eq "1") { 
		print "\nCOAST: " . scalar (@allWays) . " coast ways will be used.\n" ;
		print "COAST: ways: @allWays\n\n" ;
	}

	if (scalar @allWays > 0) {
		# build rings
		my ($refWays, $refNodes) = buildRings (\@allWays, 0) ;
		my @ringNodes = @$refNodes ; # contains all nodes of rings // array of arrays !
		if (cv('debug') eq "1") { print "COAST: " . scalar (@ringNodes) . " rings found.\n" ; }

		# convert rings to coordinate system
		my @ringCoordsOpen = () ; my @ringCoordsClosed = () ;
		for (my $i=0; $i<=$#ringNodes; $i++) {
			# print "COAST: initial ring $i\n" ;
			my @actualCoords = () ;
			foreach my $node (@{$ringNodes[$i]}) {
				push @actualCoords, [convert ($$lonRef{$node}, $$latRef{$node})] ;
			}
			if (${$ringNodes[$i]}[0] == ${$ringNodes[$i]}[-1]) {
				push @ringCoordsClosed, [@actualCoords] ; # islands
			}
			else {
				push @ringCoordsOpen, [@actualCoords] ;
			}
			# printRingCoords (\@actualCoords) ;
			my $num = scalar @actualCoords ;
			if (cv('debug') eq "1") { print "COAST: initial ring $i - $actualCoords[0]->[0],$actualCoords[0]->[1] -->> $actualCoords[-1]->[0],$actualCoords[-1]->[1]  nodes: $num\n" ; }
		}

		if (cv('debug') eq "1") { print "COAST: add points on border...\n" ; }
		foreach my $ring (@ringCoordsOpen) {
			# print "COAST:   ring $ring with border nodes\n" ;
			# add first point on border
			my $ref = nearestPoint ($ring->[0]) ;
			my @a = @$ref ;
			unshift @$ring, [@a] ;
			# add last point on border
			$ref = nearestPoint ($ring->[-1]) ;
			@a = @$ref ;
			push @$ring, [@a] ;
			# printRingCoords ($ring) ;
		}

		my @islandRings = @ringCoordsClosed ;
		if (cv('debug') eq "1") { print "COAST: " . scalar (@islandRings) . " islands found.\n" ; }
		@ringCoordsClosed = () ;

		# process ringCoordsOpen
		# add other rings, corners... 
		while (scalar @ringCoordsOpen > 0) { # as long as there are open rings
			if (cv('debug') eq "1") { print "COAST: building ring...\n" ; }
			my $ref = shift @ringCoordsOpen ; # get start ring
			my @actualRing = @$ref ;

			my $closed = 0 ; # mark as not closed
			my $actualX = $actualRing[-1]->[0] ;
			my $actualY = $actualRing[-1]->[1] ;

			my $actualStartX = $actualRing[0]->[0] ;  
			my $actualStartY = $actualRing[0]->[1] ;  

			if (cv('debug') eq "1") { print "COAST: actual and actualStart $actualX, $actualY   -   $actualStartX, $actualStartY\n" ; }

			my $corner ;
			while (!$closed) { # as long as this ring is not closed
				($actualX, $actualY, $corner) = nextPointOnBorder ($actualX, $actualY) ;
				# print "      actual $actualX, $actualY\n" ;
				my $startFromOtherPolygon = -1 ;
				# find matching ring if there is another ring
				if (scalar @ringCoordsOpen > 0) {
					for (my $i=0; $i <= $#ringCoordsOpen; $i++) {
						my @test = @{$ringCoordsOpen[$i]} ;
						# print "    test ring $i: ", $test[0]->[0], " " , $test[0]->[1] , "\n" ;
						if ( ($actualX == $test[0]->[0]) and ($actualY == $test[0]->[1]) ) {
							$startFromOtherPolygon = $i ;
							if (cv('debug') eq "1") { print "COAST:   matching start other polygon found i= $i\n" ; }
						}
					}
				}
				# process matching polygon, if present
				if ($startFromOtherPolygon != -1) { # start from other polygon {
					# append nodes
					# print "ARRAY TO PUSH: @{$ringCoordsOpen[$startFromOtherPolygon]}\n" ;
					push @actualRing, @{$ringCoordsOpen[$startFromOtherPolygon]} ;
					# set actual
					$actualX = $actualRing[-1]->[0] ;
					$actualY = $actualRing[-1]->[1] ;
					# drop p2 from opens
					splice @ringCoordsOpen, $startFromOtherPolygon, 1 ;
					if (cv('debug') eq "1") { print "COAST:   openring $startFromOtherPolygon added to actual ring\n" ; }
				}
				else {
					if ($corner) { # add corner to actual ring
						push @actualRing, [$actualX, $actualY] ;
						if (cv('debug') eq "1") { print "COAST:   corner $actualX, $actualY added to actual ring\n" ; }
					}
				}
				# check if closed
				if ( ($actualX == $actualStartX) and ($actualY == $actualStartY) ) {
					$closed = 1 ;
					push @actualRing, [$actualX, $actualY] ;
					push @ringCoordsClosed, [@actualRing] ;
					if (cv('debug') eq "1") { print "COAST:    ring now closed and moved to closed rings.\n" ; }
				}
			} # !closed
		} # open rings

		my $color = cv('oceancolor') ;

		# build islandRings polygons
		if (cv('debug') eq "1") { print "OCEAN: building island polygons\n" ; }
		my @islandPolygons = () ;
		if (scalar @islandRings > 0) {
			for (my $i=0; $i<=$#islandRings; $i++) {
				my @poly = () ;
				foreach my $node ( @{$islandRings[$i]} ) {
					push @poly, [$node->[0], $node->[1]] ;
				}
				my ($p) = Math::Polygon->new(@poly) ;
				$islandPolygons[$i] = $p ;
			}
		}
		
		# build ocean ring polygons
		if (cv('debug') eq "1") { print "OCEAN: building ocean polygons\n" ; }
		my @oceanPolygons = () ;
		if (scalar @ringCoordsClosed > 0) {
			for (my $i=0; $i<=$#ringCoordsClosed; $i++) {
				my @poly = () ;
				foreach my $node ( @{$ringCoordsClosed[$i]} ) {
					push @poly, [$node->[0], $node->[1]] ;
				}
				my ($p) = Math::Polygon->new(@poly) ;
				$oceanPolygons[$i] = $p ;
			}
		}
		else {
			if (scalar @islandRings > 0) {
				if (cv('debug') eq "1") { print "OCEAN: build ocean rect\n" ; }
				my @ocean = () ;
				my ($x, $y) = getDimensions() ;
				push @ocean, [0,0], [$x,0], [$x,$y], [0,$y], [0,0] ;
				push @ringCoordsClosed, [@ocean] ;
				my ($p) = Math::Polygon->new(@ocean) ;
				push @oceanPolygons, $p ;
			}
		}

		# finally create pathes for SVG
		for (my $i=0; $i<=$#ringCoordsClosed; $i++) {
		# foreach my $ring (@ringCoordsClosed) {
			my @ring = @{$ringCoordsClosed[$i]} ;
			my @array = () ;
			my @coords = () ;
			foreach my $c (@ring) {
				push @coords, $c->[0], $c->[1] ;
			}
			push @array, [@coords] ; 
			if (scalar @islandRings > 0) {
				for (my $j=0; $j<=$#islandRings; $j++) {
					# island in ring? 1:1 and coast on border?
					# if (isIn ($islandPolygons[$j], $oceanPolygons[$i]) == 1) {
					if ( (isIn ($islandPolygons[$j], $oceanPolygons[$i]) == 1) or 
						( (scalar @islandRings == 1) and (scalar @ringCoordsClosed == 1) ) )	{
						if (cv('debug') eq "1") { print "OCEAN: island $j in ocean $i\n" ; }
						my @coords = () ;
						foreach my $c (@{$islandRings[$j]}) {
							push @coords, $c->[0], $c->[1] ;		
						}
						push @array, [@coords] ;
					}
				}
			}


			# drawAreaOcean ($color, \@array) ;
			my $svgText = "fill=\"$color\" " ;
			drawArea($svgText, "none", \@array, 0, "base") ;

		}
	}
}

sub pointInMap {
	my ($n) = shift ;
	my ($sizeX, $sizeY) = getDimensions() ;
	my ($lonRef, $latRef) = getNodePointers() ;

	my ($x, $y) = convert ($$lonRef{$n}, $$latRef{$n}) ;

	my $ok = 0 ;
	if (
		( $x >= 0 ) and
		( $x <= $sizeX ) and
		( $y >= 0 ) and
		( $y <= $sizeY ) ) {
		$ok = 1 ;
	}
	return $ok ;		
}

1 ;


