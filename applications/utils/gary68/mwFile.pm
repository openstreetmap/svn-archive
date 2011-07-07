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


package mwFile ; 

use strict ;
use warnings ;

use mwConfig ;
use mwMap ;
use mwLabel ;

use OSM::osm ;

use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw (	readFile 
		getNodePointers
		getWayPointers
		getRelationPointers
		 ) ;

my %lon ;
my %lat ;
my %memNodeTags ;

my %memWayNodes ;
my %memWayTags ;

my %memRelationMembers ;
my %memRelationTags ;



sub readFile {

	my ($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1, @nodeTags) ;
	my ($wayId, $wayUser, $aRef2, @wayTags, @wayNodes) ;
	my ($relationId, $relationUser, @relationTags, @relationMembers) ;
	my %invalidWays ;


	my $osmName ;
	if (defined cv('in')) { $osmName = cv('in') ; }

	my $clipbbox = "" ;
	if (defined cv('clipbbox')) { $clipbbox = cv('clipbbox') ; }

	# -place given? look for place and call osmosis

	my $placeFound = 0 ; my $placeLon ; my $placeLat ;
	if ( cv('place') ne "") {
		my ($placeId) = ( cv('place') =~ /([\d]+)/);
		if (!defined $placeId) { $placeId = -999999999 ; }
		print "looking for place...\n" ;

		my $placeFileName = "" ;
		if ( cv('placeFile') ne "" ) { 
			$placeFileName = cv('placeFile') ; 
		}
		else {
			$placeFileName = cv('in') ; 
		}

		openOsmFile ($placeFileName) ;
		($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
		if ($nodeId != -1) {
			@nodeTags = @$aRef1 ;
		}
		my $place = cv ('place') ;
		while ( ($nodeId != -1) and ($placeFound == 0) ) {
			my $placeNode = 0 ; my $placeName = 0 ;
			foreach my $tag ( @nodeTags ) {
				if ($tag->[0] eq "place") { $placeNode = 1 ; }
				if ( ($tag->[0] eq "name") and (grep /$place/i, $tag->[1]) ){ $placeName = 1 ; }
			}
			if ( (($placeNode == 1) and ($placeName == 1)) or ($placeId == $nodeId) ) {
				$placeFound = 1 ;
				$placeLon = $nodeLon ;
				$placeLat = $nodeLat ;
			}
			($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
			if ($nodeId != -1) {
				@nodeTags = @$aRef1 ;
			}
		}

		closeOsmFile() ;

		if ($placeFound == 1) {
			print "place $place found at " ;
			print "lon: $placeLon " ;
			print "lat: $placeLat\n" ;
			my $left = $placeLon - cv('lonrad')/(111.11 * cos ( $placeLat / 360 * 3.14 * 2 ) ) ;  
			my $right = $placeLon + cv('lonrad')/(111.11 * cos ( $placeLat / 360 * 3.14 * 2 ) ) ; 
			my $top = $placeLat + cv('latrad')/111.11 ; 
			my $bottom = $placeLat - cv('latrad')/111.11 ;

			print "call osmosis...\n" ;

			if ( cv('cie') eq "0" ) {
				print "OSMOSIS STRING: --bounding-box completeWays=yes completeRelations=yes bottom=$bottom top=$top left=$left right=$right\n" ;
				`osmosis --read-xml $osmName  --bounding-box completeWays=yes completeRelations=yes bottom=$bottom top=$top left=$left right=$right --write-xml ./temp.osm` ;
			}
			else {
				print "OSMOSIS STRING: --bounding-box clipIncompleteEntities=yes bottom=$bottom top=$top left=$left right=$right\n" ;
				`osmosis --read-xml $osmName  --bounding-box clipIncompleteEntities=yes  bottom=$bottom top=$top left=$left right=$right --write-xml ./temp.osm` ;
			}

			print "osmosis done.\n" ;

			$osmName = "./temp.osm" ;
			$clipbbox = "$left,$bottom,$right,$top" ;
		}
		else {
			print "ERROR: place $place not found.\n" ;
			die() ;
		}
	}



	# STORE DATA
	my $nr = 0 ; my $wr = 0 ; my $rr = 0 ;
	print "reading osm file...\n" ;

	openOsmFile ($osmName) ;
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
	if ($nodeId != -1) {
		@nodeTags = @$aRef1 ;
	}
	while ($nodeId != -1) {
		$nr++ ;
		$lon{$nodeId} = $nodeLon ; $lat{$nodeId} = $nodeLat ;	
		@{$memNodeTags{$nodeId}} = @nodeTags ;

		($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
		if ($nodeId != -1) {
			@nodeTags = @$aRef1 ;
		}
	}

	($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
	while ($wayId != -1) {
		$wr++ ;
		if (scalar (@wayNodes) > 1) {
			@{$memWayTags{$wayId}} = @wayTags ;
			@{$memWayNodes{$wayId}} = @wayNodes ;
			foreach my $node (@wayNodes) {
				if (!defined $lon{$node}) {
					print "  ERROR: way $wayId references node $node, which is not present!\n" ;
				}
			}
		}
		else {
			$invalidWays{$wayId} = 1 ;
		}
	
		($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
		if ($wayId != -1) {
			@wayNodes = @$aRef1 ;
			@wayTags = @$aRef2 ;
		}
	}


	($relationId, $relationUser, $aRef1, $aRef2) = getRelation () ;
	if ($relationId != -1) {
		@relationMembers = @$aRef1 ;
		@relationTags = @$aRef2 ;
	}

	while ($relationId != -1) {
		$rr++ ;
		@{$memRelationTags{$relationId}} = @relationTags ;
		@{$memRelationMembers{$relationId}} = @relationMembers ;

		foreach my $member (@relationMembers) {
			if ( ($member->[0] eq "node") and (!defined $lon{$member->[1]}) ) {
				print "  ERROR: relation $relationId references node $member->[1] which is not present!\n" ;
			}
			if ( ($member->[0] eq "way") and (!defined $memWayNodes{$member->[1]} ) and (!defined $invalidWays{$member->[1]}) ) {
				print "  ERROR: relation $relationId references way $member->[1] which is not present or invalid!\n" ;
			}
		}

		#next
		($relationId, $relationUser, $aRef1, $aRef2) = getRelation () ;
		if ($relationId != -1) {
			@relationMembers = @$aRef1 ;
			@relationTags = @$aRef2 ;
		}
	}

	closeOsmFile () ;

	print "read: $nr nodes, $wr ways and $rr relations.\n\n" ;

	# calc area of pic and init graphics
	my $lonMin = 999 ; my $lonMax = -999 ; my $latMin = 999 ; my $latMax = -999 ;
	foreach my $key (keys %lon) {
		if ($lon{$key} > $lonMax) { $lonMax = $lon{$key} ; }
		if ($lon{$key} < $lonMin) { $lonMin = $lon{$key} ; }
		if ($lat{$key} > $latMax) { $latMax = $lat{$key} ; }
		if ($lat{$key} < $latMin) { $latMin = $lat{$key} ; }
	}

	# clip picture if desired
	if ($clipbbox ne "") {
		my ($bbLeft, $bbBottom, $bbRight, $bbTop) = ($clipbbox =~ /([\d\-\.]+),([\d\-\.]+),([\d\-\.]+),([\d\-\.]+)/ ) ;
		# print "$bbLeft, $bbBottom, $bbRight, $bbTop\n" ;
		if (($bbLeft > $lonMax) or ($bbLeft < $lonMin)) { print "WARNING -clipbox left parameter outside data.\n" ; }
		if (($bbRight > $lonMax) or ($bbRight < $lonMin)) { print "WARNING -clipbox right parameter outside data.\n" ; }
		if (($bbBottom > $latMax) or ($bbBottom < $latMin)) { print "WARNING -clipbox bottom parameter outside data.\n" ; }
		if (($bbTop > $latMax) or ($bbTop < $latMin)) { print "WARNING -clipbox top parameter outside data.\n" ; }
		$lonMin = $bbLeft ;
		$lonMax = $bbRight ;
		$latMin = $bbBottom ;
		$latMax = $bbTop ;
	}
	else {
		if (defined cv('clip')) {
			if ( (cv('clip') > 0) and (cv('clip') < 100) ) { 
				my $clip = cv('clip') ;
				$clip = $clip / 100 ;
				$lonMin += ($lonMax-$lonMin) * $clip ;
				$lonMax -= ($lonMax-$lonMin) * $clip ;
				$latMin += ($latMax-$latMin) * $clip ;
				$latMax -= ($latMax-$latMin) * $clip ;
			}
		}
	}

	# pad picture if desired
	if (defined cv('pad')) {
		my $pad = cv('pad') ;
		if ( ($pad > 0) and ($pad < 100) ) { 
			$pad = $pad / 100 ;
			$lonMin -= ($lonMax-$lonMin) * $pad ;
			$lonMax += ($lonMax-$lonMin) * $pad ;
			$latMin -= ($latMax-$latMin) * $pad ;
			$latMax += ($latMax-$latMin) * $pad ;
		}
	}

	# calc pic size if scale is set
	my $size = cv('size') ;
	if ( cv('scaleSet') != 0 ) {
		my $dist = distance ($lonMin, $latMin, $lonMax, $latMin) ;
		my $width = $dist / cv('scaleSet') * 1000 * 100 / 2.54 ; # inches
		$size = int ($width * 300) ;
	}

	mwMap::initGraph ($size, $lonMin, $latMin, $lonMax, $latMax) ;

}

sub getNodePointers {
	return ( \%lon, \%lat, \%memNodeTags) ;
}

sub getWayPointers {
	return ( \%memWayNodes, \%memWayTags) ;
}

sub getRelationPointers {

	return ( \%memRelationMembers, \%memRelationTags) ;
}


1 ;


