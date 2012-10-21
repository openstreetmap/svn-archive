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
use LWP::Simple ;

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

my $overpassSource0 = "interpreter?data=node%5B%22name%22%3D%22NAME%22%5D%3Bout%20body%3B%0A" ;
my $overpassSource1 = "interpreter?data=node%5B%22name%22%3D%22NEAR%22%5D%3Bnode%28around%3ADIST%29%5B%22name%22%3D%22NAME%22%5D%3Bout%3B" ;
my $overpassSource3 = "interpreter?data=%28node%28BOTTOM%2CLEFT%2CTOP%2CRIGHT%29%3B%3C%3B%3E%3B%29%3Bout%20meta%3B" ;


sub readFile {

	my ($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1, @nodeTags) ;
	my ($wayId, $wayUser, $aRef2, @wayTags, @wayNodes) ;
	my ($relationId, $relationUser, @relationTags, @relationMembers) ;
	my %invalidWays ;


	my $osmName ;
	if (defined cv('in')) { $osmName = cv('in') ; }


	my $clipbbox = "" ;
	if (defined cv('clipbbox')) { $clipbbox = cv('clipbbox') ; }

	if ( cv('overpass') eq "1" ) {
		if ( cv('place') eq "" ) { die ("ERROR: option place not specified.\n") ; }

		my $overpassNear = cv('near') ;
		my $overpassDistance = cv('overpassdistance') ;
		my $overpassName = cv('place') ;
		my $overpassUrl1 = cv('overpassserver') . $overpassSource1 ;

		if ( cv('near') eq "" ) {
			$overpassUrl1 = cv('overpassserver') . $overpassSource0 ;
		}

		$overpassUrl1 =~ s/NEAR/$overpassNear/ ;
		$overpassUrl1 =~ s/DIST/$overpassDistance/ ;
		$overpassUrl1 =~ s/NAME/$overpassName/ ;

		if ( cv('debug') eq "1" ) { print "Overpass Query1: $overpassUrl1 ...\n" ; }
		print "Send Query 1 to overpass server..\n" ;
		my $result1 = get ( $overpassUrl1 ) ;
		if ( ! defined $result1 ) { die ("ERROR: bad overpass result!\n") ; }

		if ( cv('debug') eq "1" ) { print "\n$result1\n\n" ; }

		# get lon, lat

		my ($placeLon) = ( $result1 =~ /lon=\"([\d\.\-]+)/ ) ;
		my ($placeLat) = ( $result1 =~ /lat=\"([\d\.\-]+)/ ) ;

		if ((! defined $placeLon) or (! defined $placeLat)) { die ("ERROR: lon/lat could not be obtained from 1st overpass result.\n") ; }

		print "place $overpassName found:\n" ;
		print "lon= $placeLon\n" ;
		print "lat= $placeLat\n" ;


		# calc bbox

		my $overLeft = $placeLon - cv('lonrad')/(111.11 * cos ( $placeLat / 360 * 3.14 * 2 ) ) ;  
		my $overRight = $placeLon + cv('lonrad')/(111.11 * cos ( $placeLat / 360 * 3.14 * 2 ) ) ; 
		my $overTop = $placeLat + cv('latrad')/111.11 ; 
		my $overBottom = $placeLat - cv('latrad')/111.11 ;

		my $overpassUrl2 = cv('overpassserver') . $overpassSource3 ;
		$overpassUrl2 =~ s/LEFT/$overLeft/ ;
		$overpassUrl2 =~ s/RIGHT/$overRight/ ;
		$overpassUrl2 =~ s/TOP/$overTop/ ;
		$overpassUrl2 =~ s/BOTTOM/$overBottom/ ;


		if ( cv('debug') eq "1" ) { print "Overpass Query2: $overpassUrl2\n" ; }
		print "Send Query 2 to overpass server..\n" ;
		my $result2 = get ( $overpassUrl2 ) ;
		if ( ! defined $result2 ) { die ("ERROR: bad overpass result!\n") ; }

		# save


		my $opFileName = "overpass.osm" ;
		open (my $overFile, ">", $opFileName) ;
		print $overFile $result2 ;
		close ( $overFile ) ;

		setConfigValue ('in', $opFileName) ;
		$osmName = $opFileName ;
		# setConfigValue ('place', '') ;

		$clipbbox = "$overLeft,$overBottom,$overRight,$overTop" ;
		if ( cv('debug') eq "1" ) { print "clipbox: $clipbbox\n" ; }
	}

	if ( grep /\.pbf/, $osmName ) {
		my $newName = $osmName ;
		$newName =~ s/\.pbf/\.osm/i ;

		# osmosis
		print "call osmosis to convert pbf file to osm file.\n" ;
		`osmosis --read-pbf $osmName --write-xml $newName` ;

		# change config
		$osmName = $newName ;
		setConfigValue ("in", $newName) ;
	}


	# -place given? look for place and call osmosis

	my $placeFound = 0 ; my $placeLon ; my $placeLat ;
	if ( ( cv('place') ne "") and (cv('overpass') ne "1" ) ) {
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

	my $size = cv('size') ;

	# calc pic size

	if ( cv('scaleSet') != 0 ) {
		my $dist = distance ($lonMin, $latMin, $lonMax, $latMin) ;
		my $width = $dist / cv('scaleSet') * 1000 * 100 / 2.54 ; # inches
		$size = int ($width * 300) ;
	}

	if ( cv('maxTargetSize') ne "" ) {
		my @a = split /,/, cv('maxTargetSize') ;
		my $targetWidth = $a[0] ;
		my $targetHeight = $a[1] ;
		# print "TS: $targetWidth, $targetHeight [cm]\n" ;
		my $distLon = distance ($lonMin, $latMin, $lonMax, $latMin) ;
		my $distLat = distance ($lonMin, $latMin, $lonMin, $latMax) ;
		# print "TS: $distLon, $distLat [km]\n" ;
		my $scaleLon = ($distLon * 1000 * 100) / $targetWidth ;
		my $scaleLat = ($distLat * 1000 * 100) / $targetHeight ;
		my $targetScale = int $scaleLon ;
		if ( $scaleLat > $targetScale ) { $targetScale = int $scaleLat ; }
		# print "TS: $targetScale [1:n]\n" ;

		my $width = $distLon / $targetScale * 1000 * 100 / 2.54 ; # inches
		$size = int ($width * 300) ;
		print "Map width now $size [px] due to maxTargetSize parameter\n" ;		
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


