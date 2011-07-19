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


package mwMulti ; 

use strict ;
use warnings ;

use mwMap ;
use mwMisc ;
use mwFile ;
use mwLabel ;
use mwConfig ;
use mwRules ;

use Math::Polygon ;



use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw (	processMultipolygons

		 ) ;

my $newId = 0 ;

my %multiNodes = () ;
my %multiTags = () ;
my %multiPaths = () ;

my %wayUsed = () ;

# -------------------------------------------------------------------------

sub processMultipolygons {
	my $notDrawnMP = 0 ;
	my $mp = 0 ;
	print "draw multipolygons...\n" ;

	preprocessMultipolygons() ;

	foreach my $multiId (keys %multiTags) {

		my $ruleRef = getAreaRule ( \@{$multiTags{$multiId}} ) ;

		if (defined $ruleRef) {

			my $svgText = "" ;
			my $icon = "" ;
			if ($$ruleRef{'icon'} ne "none") {
				$icon = $$ruleRef{'icon'} ;
			}
			else {
				my $col = $$ruleRef{'color'} ;
				$svgText = "fill=\"$col\" " ;
			}

			my $ref = $multiPaths{$multiId}[0] ; # first, outer way
			my $size = areaSize ( $ref ) ;

			if ($size >= cv('minareasize') ) {
				drawArea ($svgText, $icon, $multiPaths{$multiId}, 1, "multi") ;
				$mp++ ;
			}
			else {
				$notDrawnMP++ ;
			}

			# LABELS
			my $name = "" ; my $ref1 ;
			($name, $ref1) = createLabel ( $multiTags{$multiId}, $$ruleRef{'label'}, 0, 0) ;

			if ( ( $$ruleRef{'label'} ne "none") and 
				( cv('nolabel') eq "1" ) and 
				($name eq "") ) 
			{ 
				$name = "NO LABEL" ; 
			}

			if ($name ne "") {
				my ($x, $y) = center (nodes2Coordinates( @{$multiNodes{$multiId}} ) ) ;

				# placeLabelAndIcon ($x,$y, 0, 0, $name, $test->[$wayIndexLabelColor], $test->[$wayIndexLabelSize], $test->[$wayIndexLabelFont], $ppc, "none", 0, 0, $allowIconMoveOpt, $halo) ;

				$svgText = "" ;
				my $iSize = $$ruleRef{'iconsize'} ;
				placeLabelAndIcon ($x, $y, 0, 0, $name, $svgText, $icon, $iSize, $iSize, "multi") ;

			} # if
		} # if rule
	} # foreach multi
	print "$mp multipolygon areas drawn, $notDrawnMP not drawn because they were too small.\n" ;
}

# ------------------------------------------------------------------------------------------

sub preprocessMultipolygons {
#
# preprecess all multipolygons
#

	my ($wayNodesRef, $wayTagsRef) = getWayPointers() ;
	my ($relationMembersRef, $relationTagsRef) = getRelationPointers() ;

	foreach my $relId (keys %$relationMembersRef) {
		my $isMulti = 0 ;
		foreach my $tag (@{$$relationTagsRef{$relId}}) {
			if ( ($tag->[0] eq "type") and ($tag->[1] eq "multipolygon") ) { $isMulti = 1 ; }
		}

		if ($isMulti) {
			if (cv('debug') eq "1") { print "\n---------------------------------------------------\n" ; }
			if (cv('debug') eq "1") { print "\nRelation $relId is multipolygon!\n" ; }
			
			# get inner and outer ways
			my (@innerWays) = () ; my (@outerWays) = () ;
			foreach my $member ( @{$$relationMembersRef{$relId}} ) {
				if ( ($member->[0] eq "way") and ($member->[2] eq "outer") and (defined @{$$wayNodesRef{$member->[1]}} ) ) { push @outerWays, $member->[1] ; }
				if ( ($member->[0] eq "way") and ($member->[2] eq "inner") and (defined @{$$wayNodesRef{$member->[1]}} )) { push @innerWays, $member->[1] ; }
			}
			if (cv('debug') eq "1") { print "OUTER WAYS: @outerWays\n" ; }
			if (cv('debug') eq "1") { print "INNER WAYS: @innerWays\n" ; }

			my ($ringsWaysRef, $ringsNodesRef) ;
			my @ringWaysInner = () ; my @ringNodesInner = () ; my @ringTagsInner = () ;
			# build rings inner
			if (scalar @innerWays > 0) {
				($ringsWaysRef, $ringsNodesRef) = buildRings (\@innerWays, 1) ;
				@ringWaysInner = @$ringsWaysRef ; 
				@ringNodesInner = @$ringsNodesRef ;
				for (my $ring=0; $ring<=$#ringWaysInner; $ring++) {
					if (cv('debug') eq "1") { print "INNER RING $ring: @{$ringWaysInner[$ring]}\n" ; }
					my $firstWay = $ringWaysInner[$ring]->[0] ;
					if (scalar @{$ringWaysInner[$ring]} == 1) {$wayUsed{$firstWay} = 1 ; } # way will be marked as used/drawn by multipolygon

					@{$ringTagsInner[$ring]} = @{$$wayTagsRef{$firstWay}} ; # ring will be tagged like first contained way
					if (cv('debug') eq "1") {
						print "tags from first way...\n" ;
						foreach my $tag (@{$$wayTagsRef{$firstWay}}) {
							print "  $tag->[0] - $tag->[1]\n" ;
						}
					}
					if ( (scalar @{$$wayTagsRef{$firstWay}}) == 0 ) {
						if (cv('debug') eq "1") { print "tags set to hole in mp.\n" ; }
						push @{$ringTagsInner[$ring]}, ["multihole", "yes"] ;
					}
				}
			}

			# build rings outer
			my @ringWaysOuter = () ; my @ringNodesOuter = () ; my @ringTagsOuter = () ;
			if (scalar @outerWays > 0) {
				($ringsWaysRef, $ringsNodesRef) = buildRings (\@outerWays, 1) ;
				@ringWaysOuter = @$ringsWaysRef ; # not necessary for outer
				@ringNodesOuter = @$ringsNodesRef ;
				for (my $ring=0; $ring<=$#ringWaysOuter; $ring++) {
					if (cv('debug') eq "1") { print "OUTER RING $ring: @{$ringWaysOuter[$ring]}\n" ; }
					my $firstWay = $ringWaysOuter[$ring]->[0] ;
					if (scalar @{$ringWaysOuter[$ring]} == 1) {$wayUsed{$firstWay} = 1 ; }
					@{$ringTagsOuter[$ring]} = @{$$relationTagsRef{$relId}} ; # tags from relation
					if (cv('debug') eq "1") {
						print "tags from relation...\n" ;
						foreach my $tag (@{$$relationTagsRef{$relId}}) {
							print "  $tag->[0] - $tag->[1]\n" ;
						}
					}
					if (scalar @{$$relationTagsRef{$relId}} == 1) {
						@{$ringTagsOuter[$ring]} = @{$$wayTagsRef{$firstWay}} ; # ring will be tagged like first way
 					}
				}
			} # outer
			
			my @ringNodesTotal = (@ringNodesInner, @ringNodesOuter) ;
			my @ringWaysTotal = (@ringWaysInner, @ringWaysOuter) ;
			my @ringTagsTotal = (@ringTagsInner, @ringTagsOuter) ;

			processRings (\@ringNodesTotal, \@ringWaysTotal, \@ringTagsTotal) ;
		} # multi

	} # relIds
}

# -----------------------------------------------------------------------------------------

sub processRings {
#
# process rings of multipolygons and create path data for svg
#
	my ($ref1, $ref2, $ref3) = @_ ;
	my @ringNodes = @$ref1 ;
	my @ringWays = @$ref2 ;
	my @ringTags = @$ref3 ;
	my @polygon = () ;
	my @polygonSize = () ;
	my @ringIsIn = () ;
	my @stack = () ; # all created stacks
	my %selectedStacks = () ; # stacks selected for processing 
	my $actualLayer = 0 ; # for new tags
	# rings referenced by array index

	my ($lonRef, $latRef) = getNodePointers() ;
	my ($wayNodesRef, $wayTagsRef) = getWayPointers() ;

	# create polygons
	if (cv('debug') eq "1") { print "CREATING POLYGONS\n" ; }
	for (my $ring = 0 ; $ring <= $#ringWays; $ring++) {
		my @poly = () ;
		foreach my $node ( @{$ringNodes[$ring]} ) {
			push @poly, [$$lonRef{$node}, $$latRef{$node}] ;
		}
		my ($p) = Math::Polygon->new(@poly) ;
		$polygon[$ring] = $p ;
		$polygonSize[$ring] = $p->area ;
		if (cv('debug') eq "1") { 
			print "  POLYGON $ring - created, size = $polygonSize[$ring] \n" ; 
			foreach my $tag (@{$ringTags[$ring]}) {
				print "    $tag->[0] - $tag->[1]\n" ;
			}
		}
	}


	# create is_in list (unsorted) for each ring
	if (cv('debug') eq "1") { print "CALC isIn\n" ; }
	for (my $ring1=0 ; $ring1<=$#polygon; $ring1++) {
		my $res = 0 ;
		for (my $ring2=0 ; $ring2<=$#polygon; $ring2++) {
			if ($ring1 < $ring2) {
				$res = isIn ($polygon[$ring1], $polygon[$ring2]) ;
				if ($res == 1) { 
					push @{$ringIsIn[$ring1]}, $ring2 ; 
					if (cv('debug') eq "1") { print "  $ring1 isIn $ring2\n" ; }
				} 
				if ($res == 2) { 
					push @{$ringIsIn[$ring2]}, $ring1 ; 
					if (cv('debug') eq "1") { print "  $ring2 isIn $ring1\n" ; }
				} 
			}
		}
	}
	if (cv('debug') eq "1") {
		print "IS IN LIST\n" ;
		for (my $ring1=0 ; $ring1<=$#ringNodes; $ring1++) {
			if (defined @{$ringIsIn[$ring1]}) {
				print "  ring $ring1 isIn - @{$ringIsIn[$ring1]}\n" ;
			}
		}
		print "\n" ;
	}

	# sort is_in list, biggest first
	if (cv('debug') eq "1") { print "SORTING isIn\n" ; }
	for (my $ring=0 ; $ring<=$#ringIsIn; $ring++) {
		my @isIn = () ;
		foreach my $ring2 (@{$ringIsIn[$ring]}) {
			push @isIn, [$ring2, $polygonSize[$ring2]] ;
		}
		@isIn = sort { $a->[1] <=> $b->[1] } (@isIn) ; # sorted array

		my @isIn2 = () ; # only ring numbers
		foreach my $temp (@isIn) {
			push @isIn2, $temp->[0] ;
		}
		@{$stack[$ring]} = reverse (@isIn2) ; 
		push @{$stack[$ring]}, $ring ; # sorted descending and ring self appended
		if (cv('debug') eq "1") { print "  stack ring $ring sorted: @{$stack[$ring]}\n" ; }
	}

	# find tops and select stacks
	if (cv('debug') eq "1") { print "SELECTING STACKS\n" ; }
	my $actualStack = 0 ;
	for (my $stackNumber=0 ; $stackNumber<=$#stack; $stackNumber++) {
		# look for top element
		my $topElement = $stack[$stackNumber]->[(scalar @{$stack[$stackNumber]} - 1)] ;
		my $found = 0 ;
		for (my $stackNumber2=0 ; $stackNumber2<=$#stack; $stackNumber2++) {
			if ($stackNumber != $stackNumber2) {
				foreach my $ring (@{$stack[$stackNumber2]}) {
					if ($ring == $topElement) { 
						$found = 1 ;
						if (cv('debug') eq "1") { print "      element also found in stack $stackNumber2\n" ; }
					}
				}
			}
		}

		if ($found == 0) {
			@{$selectedStacks{$actualStack}} = @{$stack[$stackNumber]} ;
			$actualStack++ ;
			if (cv('debug') eq "1") { print "    stack $stackNumber has been selected.\n" ; }
		}
	
	}
	
	# process selected stacks

	if (cv('debug') eq "1") { print "PROCESS SELECTED STACKS\n" ; }
	# while stacks left
	while (scalar (keys %selectedStacks) > 0) {
		my (@k) = keys %selectedStacks ;
		if (cv('debug') eq "1") { print "  stacks available: @k\n" ; }
		my @nodes = () ;
		my @nodesOld ;
		my @processedStacks = () ;

		# select one bottom element 
		my $key = $k[0] ; # key of first stack
		if (cv('debug') eq "1") { print "  stack nr $key selected\n" ; }
		my $ringToDraw = $selectedStacks{$key}[0] ;
		if (cv('debug') eq "1") { print "  ring to draw: $ringToDraw\n" ; }

		push @nodesOld, @{$ringNodes[$ringToDraw]} ; # outer polygon
		push @nodes, [@{$ringNodes[$ringToDraw]}] ; # outer polygon as array

		# and remove ring from stacks; store processed stacks
		foreach my $k2 (keys %selectedStacks) {
			if ($selectedStacks{$k2}[0] == $ringToDraw) { 
				shift (@{$selectedStacks{$k2}}) ; 
				push @processedStacks, $k2 ;
				if (scalar @{$selectedStacks{$k2}} == 0) { delete $selectedStacks{$k2} ; }
				if (cv('debug') eq "1") { print "  removed $ringToDraw from stack $k2\n" ; }
			} 
		}

		# foreach stack in processed stacks
		foreach my $k (@processedStacks) {
			# if now bottom of a stack is hole, then add this polygon to points
			if (defined $selectedStacks{$k}) {
				my $tempRing = $selectedStacks{$k}[0] ;
				my $temp = $ringTags[$tempRing]->[0]->[0] ;
				if (cv('debug') eq "1") { print "           testing for hole: stack $k, ring $tempRing, tag $temp\n" ; }
				if ($ringTags[$tempRing]->[0]->[0] eq "multihole") {
					push @nodesOld, @{$ringNodes[$tempRing]} ;
					push @nodes, [@{$ringNodes[$tempRing]}] ;
					# print "      nodes so far: @nodes\n" ;
					# and remove this element from stack
					shift @{$selectedStacks{$k}} ;
					if (scalar @{$selectedStacks{$k}} == 0) { delete $selectedStacks{$k} ; }
					if (cv('debug') eq "1") { print "  ring $tempRing identified as hole\n" ; }
				}
			}
		}

		# add way

		@{$multiNodes{$newId}} = @nodesOld ;
		@{$multiTags{$newId}} = @{$ringTags[$ringToDraw]} ;
		@{$multiPaths{$newId}} = @nodes ;

		push @{$$wayTagsRef{$newId}}, ["layer", $actualLayer] ;
		$actualLayer++ ;

		if (cv('debug') eq "1") { 
			print "  DRAWN: $ringToDraw, wayId $newId\n" ; 
			foreach my $tag (@{$ringTags[$ringToDraw]}) {
				print "    k/v $tag->[0] - $tag->[1]\n" ;
			}
		}

		$newId++ ;

	} # (while)
}


1 ;


