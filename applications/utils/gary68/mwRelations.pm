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


package mwRelations ; 

use strict ;
use warnings ;

use mwMap ;
use mwRules ;
use mwFile ;
use mwMisc ;
use mwLabel ;
use mwConfig ;

use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw ( processRoutes

		 ) ;

my $pathNumber = 0 ;

my %iconSizeX = () ;
my %iconSizeY = () ;


# --------------------------------------------------------------------------

sub processRoutes {
#
# process route data
#
	my %routeColors = () ; # will point to arrays of colors per route type
	my %actualColorIndex = () ; # which color is next
	my %colorNumber = () ; # number of colors per route type
	my %wayRouteLabels = () ; # labels to be used per way
	my %wayRouteIcons = () ; # icons to be used per way
	my (%iconSizeX, %iconSizeY) ;

	print "processing routes...\n" ;

	# init before relation processing
	# get colors per type and set actual index

	my $ref = getRouteColors() ;
	%routeColors = %$ref ;
	foreach my $type (keys %routeColors) {
		$colorNumber{$type} = scalar @{$routeColors{$type}} ;
		$actualColorIndex{$type} = 0 ;
	}

	my ($lonRef, $latRef) = getNodePointers() ;
	my ($wayNodesRef, $wayTagsRef) = getWayPointers() ;
	my ($relationMembersRef, $relationTagsRef) = getRelationPointers() ;

	foreach my $relId (keys %$relationTagsRef) {
		my $relationType = getValue ("type", $$relationTagsRef{$relId} ) ;
		if ( ! defined $relationType ) { $relationType = "" ; }

		if ( ( $relationType eq "route" ) and ( (cv('relid') == $relId) or (cv('relid') == 0) ) ) {

			my $ruleRef = getRouteRule( $$relationTagsRef{$relId} ) ;

			if (defined $ruleRef) {

				# new route detected
				if (cv('debug') eq "1" ) { print "ROUTE: rule found for $relId, $$ruleRef{'type'}.\n" ;	}

				# try to get color from relation tags first
				#
				my $color = getValue ("color", $$relationTagsRef{$relId} ) ;
				if ( ! defined $color) {
					$color = getValue ("colour", $$relationTagsRef{$relId} ) ;
				}

				# no color yet, then get color from rule
				#
				if ( ! defined $color) { 
					if (cv('debug') eq "1" ) { print "ROUTE:   actual color index: $actualColorIndex{ $$ruleRef{'type'} }\n" ; }
					$color = $routeColors{ $$ruleRef{'type'} }[$actualColorIndex{ $$ruleRef{'type'} }] ; 
					$actualColorIndex{ $$ruleRef{'type'} } = ($actualColorIndex{ $$ruleRef{'type'} } + 1) % $colorNumber{ $$ruleRef{'type'} } ;
				}
				if (cv('debug') eq "1" ) { print "ROUTE:   $relId final color: $color\n" ; }

				# find icon
				my $iconName = getValue ("ref", $$relationTagsRef{$relId} ) ;
				if ( ! defined $iconName ) {
					getValue ("name", $$relationTagsRef{$relId} )
				}
				if ( ! defined $iconName) { $iconName = "" ; }

				# look for route icon. svg first, then png

				my $file ;
				$iconName = cv('routeicondir') . $$ruleRef{'type'} . "-" . $iconName . ".svg" ;
				my $iconResult = open ($file, "<", $iconName) ;
				# print "  trying $iconName\n" ;
				if ($iconResult) { 
					if (cv('debug') eq "1") { print "ROUTE:   icon $iconName found!\n" ; }
					close ($file) ;
				} 

				if ( ! $iconResult) {
					$iconName =~ s/.svg/.png/ ; 
					# print "  trying $iconName\n" ;
					$iconResult = open ($file, "<", $iconName) ;
					if ($iconResult) { 
						if (cv('debug') eq "1") { print "ROUTE:   icon $iconName found!\n" ; }
						close ($file) ;
					} 
				}

				if ($iconResult) {
					my ($x, $y) ; undef $x ; undef $y ;
					if (grep /.svg/, $iconName) {
						($x, $y) = sizeSVG ($iconName) ;
						if ( ($x == 0) or ($y == 0) ) { 
							$x = 32 ; $y = 32 ; 
							print "WARNING: size of file $iconName could not be determined. Set to 32px x 32px\n" ;
						} 
					}

					if (grep /.png/, $iconName) {
						($x, $y) = sizePNG ($iconName) ;
					}

					$iconSizeX{$iconName} = $x ;
					$iconSizeY{$iconName} = $y ;
				}

				my ($label, $ref) = createLabel ( $$relationTagsRef{$relId}, $$ruleRef{'label'} ) ;

				my $printIcon = "" ; if ($iconResult) { $printIcon = $iconName ; }
					
				if (cv('verbose') eq "1" ) { 
					printf "ROUTE: route %10s %10s %10s %30s %40s\n", $relId, $$ruleRef{'type'}, $color, $label, $printIcon ; 
				}

				# collect ways

				my $mRef = getAllMembers ($relId, 0) ;
				my @tempMembers = @$mRef ;

				my @relWays = () ;
				foreach my $member (@tempMembers) {
					if ( ( ($member->[2] eq "none") or ($member->[2] eq "route") ) and ($member->[0] eq "way") ) { push @relWays, $member->[1] ; }
					if ( ( ($member->[2] eq "forward") or ($member->[2] eq "backward") ) and ($member->[0] eq "way") ) { push @relWays, $member->[1] ; }

					# TODO diversions, shortcuts?

					# stops
					if ( (grep /stop/, $member->[2]) and ($member->[0] eq "node") ) {
						if ( $$ruleRef{'nodesize'} > 0) {
							my $svgString = "fill=\"$color\" " ;
							drawCircle ($$lonRef{$member->[1]}, $$latRef{$member->[1]}, 1, $$ruleRef{'nodesize'}, 0, $svgString, 'routes') ;
						}
					}
				}

				if (cv('debug') eq "1" ) { print "ROUTE:   ways: @relWays\n" ; }

				foreach my $w (@relWays) {

					my $op = $$ruleRef{'opacity'} / 100 ;
					my $width = $$ruleRef{'size'} ;
					my $linecap = $$ruleRef{'linecap'} ;
					my $dashString = "" ;
					my $dash = $$ruleRef{'dash'} ;
					if ( $dash ne "") { $dashString = "stroke-dasharray=\"$dash\" " ; }
					my $svgString = "stroke=\"$color\" stroke-opacity=\"$op\" stroke-width=\"$width\" fill=\"none\" stroke-linejoin=\"round\" stroke-linecap=\"$linecap\" " . $dashString ;

					drawWay ($$wayNodesRef{$w}, 1, $svgString, "routes", undef) ;

					# collect labels and icons per way
					#
					$wayRouteLabels{$w}{$label} = 1 ;
					if ($iconResult) {						
						$wayRouteIcons{$w}{$iconName} = 1 ;
					}
				}

			} # rule found
			if (cv('debug') eq "1") { print "\n" ; }
		} # rel route
	} # relation

	# label route ways after all relations have been processed
	foreach my $w (keys %wayRouteLabels) {
		if ( (defined $$wayNodesRef{$w}) and (scalar @{$$wayNodesRef{$w}} > 1) ) {
			my $label = "" ;
			foreach my $l (keys %{$wayRouteLabels{$w}}) {
				$label .= $l . " " ;
			} 

			my @way = @{$$wayNodesRef{$w}} ;
			if ($$lonRef{$way[0]} > $$lonRef{$way[-1]}) {
				@way = reverse (@way) ;
			}

			if (labelFitsWay ( \@way, $label, cv('routelabelfont'), cv('routelabelsize') ) ) {
				my $pathName = "RoutePath" . $pathNumber ; 
				$pathNumber++ ;

				my @points = nodes2Coordinates( @way ) ;
				createPath ($pathName, \@points, "definitions") ;

				my $size = cv('routelabelsize') ;
				my $color = cv('routelabelcolor') ;
				my $svgText = "font-size=\"$size\" fill=\"$color\"" ;  
				pathText ($svgText, $label, $pathName, cv('routelabeloffset'), "middle", 50, "routes") ;
			}
		}
	}

	# place icons
	foreach my $w (keys %wayRouteIcons) {
		my $offset = 0 ;
		my $nodeNumber = scalar @{$$wayNodesRef{$w}} ;
		if ($nodeNumber > 1) {
			my $node = $$wayNodesRef{$w}[int ($nodeNumber / 2)] ;
			my $num = scalar (keys %{$wayRouteIcons{$w}}) ;
			$offset = int (-($num-1)* cv('routeicondist') / 2) ; 

			foreach my $iconName (keys %{$wayRouteIcons{$w}}) {

				my $size = 40 ;
				placeLabelAndIcon ($$lonRef{$node}, $$latRef{$node}, $offset, $size, "", "", $iconName, $iconSizeX{$iconName}, $iconSizeY{$iconName}, "routes") ;

				$offset += cv('routeicondist') ;
			}
		}
	}
}

# --------------------------------------------------------------------------

sub getAllMembers {
#
# get all members of a relation recursively
# takes rel id and nesting level
# retruns ref to array with all members
#
	my ($relId, $nestingLevel) = @_ ;
	my @allMembers = () ;
	my $maxNestingLevel = 20 ;

	my ($relationMembersRef, $relationTagsRef) = getRelationPointers() ;

	if ($nestingLevel > $maxNestingLevel) { 
		print "ERROR/WARNING nesting level of relations too deep. recursion stopped at depth $maxNestingLevel! relId=$relId\n" ;
	}
	else {
		foreach my $member ( @{$$relationMembersRef{$relId}} ) {
			if ( ($member->[0] eq "way") or ($member->[0] eq "node") ) {
				push @allMembers, $member ;
			}
			if ( $member->[0] eq "relation" ) {
				my $ref = getAllMembers ($member->[1], $nestingLevel+1) ;
				push @allMembers, @$ref ;
			}
		}	
	}
	return \@allMembers ;
}

sub labelFitsWay {
	my ($refWayNodes, $text, $font, $size) = @_ ;
	my @wayNodes = @$refWayNodes ;

	my ($lonRef, $latRef) = getNodePointers() ;

	# calc waylen
	my $wayLength = 0 ; # in pixels
	for (my $i=0; $i<$#wayNodes; $i++) {
		my ($x1, $y1) = convert ($$lonRef{$wayNodes[$i]}, $$latRef{$wayNodes[$i]}) ;
		my ($x2, $y2) = convert ($$lonRef{$wayNodes[$i+1]}, $$latRef{$wayNodes[$i+1]}) ;
		$wayLength += sqrt ( ($x2-$x1)**2 + ($y2-$y1)**2 ) ;
	}


	# calc label len
	my $labelLength = length ($text) * cv('ppc') / 10 * $size ; # in pixels

	my $fit ;
	if ($labelLength < $wayLength) { $fit="fit" ; }	else { $fit = "NOFIT" ; }
	# print "labelFitsWay: $fit, $text, labelLen = $labelLength, wayLen = $wayLength\n" ;

	if ($labelLength < $wayLength) {
		return 1 ;
	}
	else {
		return 0 ;
	}
}


1 ;


