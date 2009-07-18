# 
#
# mappingquality.pl by gary68
#
# this program checks an osm file for the quality and quantity of the mapping of places
#
# usage: mappingquality.pl file.osm basename [size]
# prg will create several output names from basename by appending suffixes! size can be omitted and defaults to 2048.
#
#
#
# Copyright (C) 2009, Gerhard Schwanz
#
# This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 3 of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>
#
# Version 1.0
# Version 1.1
# - add picture support
# - add josm and history links
# - draw highway=service with lightgray
# - corrected hwy res length error in map
# Version 1.2
# - look for radius in osm data, display in pic
# - faster superior search
# - streetArray
# - auto find radius according residentials and nearby places
# - calc and print counts
# Version 2
# - add place areas
# - headers for csv files
# - solved two bugs (;:)
#
# Version 2.1
# - bug fixed
#
# Version 2.2
# - population bug fixed 
#



use strict ;
use warnings ;

use List::Util qw[min max] ;
use Math::Polygon ;
use OSM::osm 3.0 ;
use OSM::osmgraph 1.0 ;

#
# CONSTANTS
#
#
my $program = "mappingquality.pl" ;
my $usage = $program . " file.osm basename size" ;
my $version = "2.2" ;
#
my $iconRed = "unmapped_red.png" ; my $iconYellow = "unmapped_yellow.png" ;
my $colorRed = "#FF0000" ;
my $colorGreen = "#9BCD9B" ;
my $colorOrange = "#FF8C00" ;
my $colorGrey = "#AAAAAA" ;
#
# the next two constants define which places are checked for a node or a way
my $hashSpanCircle = 0.1 ; # 0.1 means 3 hashes in each dimension will be looked up, 0.2 means 5 each (resulting in 25). this is a performance issue!
my $hashSpanArea   = 0.1 ; 
#
my $minRad = 250 ; # min radius (m) for auto detection. smaller places will default.
#
# default distances if no other information is available
my %dists ; # in km // STAY BELOW 10km !!!
$dists{"city"} =  4.0 ;
$dists{"town"} =  1.0 ;
$dists{"suburb"} =  0.5 ;
$dists{"village"} =  0.5 ;
#
# thresholds for places
my %sparse ; 
$sparse{"city"} =  4800 ;
$sparse{"town"} =  400 ;
$sparse{"suburb"} =  60 ;
$sparse{"village"} =  60 ;
#
# thresholds for places
my %unmapped ; 
$unmapped{"city"} =  2400 ;
$unmapped{"town"} =  200 ;
$unmapped{"suburb"} =  30 ;
$unmapped{"village"} =  30 ;


my $place_count ;			# number places found
my $assignedAreas = 0 ;	# number of places that could be assigned to place nodes
my %count ;
my %unmappedCount ; my %sparselyCount ;

# variables for file parsing
my $wayId ; my $wayId1 ; my $wayId2 ;
my $wayUser ; my @wayNodes ; my @wayTags ;
my $nodeId ; my $nodeId2 ;
my $nodeUser ; my $nodeLat ; my $nodeLon ; my @nodeTags ;
my $aRef1 ; my $aRef2 ;

my $invalidWays = 0 ;			# ways with only one node, can not be processed

my $time0 = time() ; my $time1 ; my $timeA ;

# file names and handles
my $baseName ; my $osmName ; my $htmlName ; my $textName ; my $csvName ; my $pngName ; my $streetName ; 
my $html ; my $text ; my $csv ; my $png ; my $street ;

# place node information
# hash value negative if created by area!
my %placeName ; my %placeType ; my %placeRadius ; my %placeRadiusSource ;
my %placeNodes ; my %placePopulation ;
my %placeFixmeNodes ; my %placeFixmeWays ;
my %placeResidentials ; my %placeResidentialsNoname ; my %placeResidentialLength ;
my %placeRoads ; my %placeAmenities ; my %placeBuildings ;
my %placeHash ; my %placeSuperior ; my %placeSuperiorDist ; my %placeDistNext ; 
my %placeArea ;
my %placeStreets ; 

my $auto = 0 ;
my $autoCircle = 0 ;

my %placeStreetArray ;

# place way information
# hash value negative if created by program automatically (area)
my %placeWayLon ;
my %placeWayLat ;
my %placeWayPolygon ;	# contains polygon
my %placeWayType ;	# type of place
my %placeWayName ; 	# name of place to compare against place node
my %placeWayNodeId ; 	# nearest place node with same name
my %placeWayDist ; 	# dist to nearest place node
my %placeWayNodes ; 	# nodes of way
my %placeWayHash ;	# hash of place ways
my @placeWayNoName ;	# error report
my @placeWayOpen ;	# error report
my @unassignedAreas ;

my $areaCount = 0 ; my $areaOpenCount = 0 ; my $areaNoNameCount = 0 ;

my %lon ; my %lat ;
my %citiesAndTowns ;	# collect for nearest search optimization

#statistic variables
my %meanNodes ; my %meanHwyRes ; my %meanHwyResLen ; my %meanAmenities ; my %meanBuildings ;

my $wayCount ; my $nodeCount ; my $radCount = 0 ;
my $key ; my $key1 ; my $key2 ;

my @outArray ;		# sort output before print

# graphics variables
my $size ; my $lonMin ; my $latMin ; my $lonMax ; my $latMax ;

###############
# get parameter
###############
$osmName = shift||'';
if (!$osmName)
{
	die (print $usage, "\n");
}

$baseName = shift||'';
if (!$baseName)
{
	die (print $usage, "\n");
}

$size = shift||'';
if (!$size)
{
	$size = 2048 ; # default size
}

$htmlName = $baseName . ".htm" ;
$textName = $baseName . ".txt" ;
$csvName = $baseName . ".csv" ;
$pngName = $baseName . ".png" ;
$streetName = $baseName . "_streets.csv" ;

print "\n$program $version for file $osmName\n\n" ;

foreach (keys %dists) { $count{$_} = 0 ; } # init counts

######################
# get node information
######################
print "get node position information...\n" ;
openOsmFile ($osmName) ;

($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
if ($nodeId != -1) {
	@nodeTags = @$aRef1 ;
}
while ($nodeId != -1) {
	$nodeCount++ ;
	$lon{$nodeId} = $nodeLon ; 	# store position
	$lat{$nodeId} = $nodeLat ; 
	my $place = 0 ;	my $name = "-" ; my $population = 0 ; my $type = "-" ; my $tag ; my $distance = 0 ;
	# find node information like name, place, population etc.
	foreach $tag (@nodeTags) {
		my $tmp1 = $tag ;
		my $colon = ($tmp1 =~ s/://g ) ;

		$tmp1 = $tag ;
		if ( ( grep (/^place:/, $tag) ) and ($colon == 1) ) {
			$type = $tag ; $type =~ s/place:// ;
			my $t ;
			foreach $t (keys %dists) {
				if ($t eq $type) { $place = 1 ; }
			}
		}
		my $tmp = $tag ;
		if ( (grep /^name:/, $tag) and ($colon == 1) ) { $name = $tag ; $name =~ s/name:// ; }
		if ( (grep /^place_name:/, $tag) and ($colon == 1) ) { $name = $tag ; $name =~ s/place_name:// ; }
		if (grep /population:/ , $tag) { 
			if (grep /openGeoDB:population:/ , $tag) {
				$population = $tag ; $population =~ s/openGeoDB:population:// ; 
			}
			else {
				if ( (grep /^population:/, $tag) and ($colon == 1) ) { $population =~ s/population:// ; }
			}
		}
		if (grep /^radius:/ , $tag) { 
			my $dTmp = $tag ; 
			$dTmp =~ s/place_// ;
			$dTmp =~ s/radius:// ;
			if (grep /km/ , $dTmp) {
				$dTmp =~ s/km// ;
				# TODO check for numeric
				$distance = $dTmp ;
				$radCount++ ;
			}
			else {
				if (grep /m/ , $dTmp) {
					$dTmp =~ s/m// ;
					# TODO check for numeric
					$distance = $dTmp / 1000 ;
					$radCount++ ;
				}
			}
		}
		if (grep /^diameter:/ , $tag) { 
			my $dTmp = $tag ; 
			$dTmp =~ s/place_// ;
			$dTmp =~ s/diameter:// ;
			if (grep /km/ , $dTmp) {
				$dTmp =~ s/km// ;
				# TODO check for numeric
				$distance = $dTmp / 2 ;
				$radCount++ ;
			}
			else {
				if (grep /m/ , $dTmp) {
					$dTmp =~ s/m// ;
					# TODO check for numeric
					$distance = $dTmp / 1000 / 2 ;
					$radCount++ ;
				}
			}
		}
	}
	$name =~ s/;/-/g ;
	if ( ($place == 1)  and ($name ne "-") ) {
		$place_count ++ ;
		$count{$type} ++ ;
		$lon{$nodeId} = $nodeLon ;
		$lat{$nodeId} = $nodeLat ;
		$placeArea{$nodeId} = 0 ;
		$placeType{$nodeId} = $type ;
		$placePopulation{$nodeId} = $population ;
		$placeName{$nodeId} =  $name ;
		$placeNodes{$nodeId} = 0 ;
		$placeFixmeNodes{$nodeId} = 0 ;
		$placeFixmeWays{$nodeId} = 0 ;
		$placeResidentials{$nodeId} = 0  ;
		$placeResidentialsNoname{$nodeId} = 0 ;
		$placeResidentialLength{$nodeId} = 0 ;
		$placeRoads{$nodeId} = 0  ;
		$placeAmenities{$nodeId} = 0 ;
		$placeBuildings{$nodeId} = 0 ;
		@{$placeStreetArray{$nodeId}} = (0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0) ;
		$placeDistNext{$nodeId} = 999 ;
		$placeRadius{$nodeId} = $dists{$type} ;	# default dist
		$placeRadiusSource{$nodeId} = "default" ;
		if ($distance != 0) {				# radius from osm data
			$placeRadius{$nodeId} = $distance ;
			$placeRadiusSource{$nodeId} = "osm" ;
		}
		if (($type eq "city") or ($type eq "town") ) {
			$citiesAndTowns{$nodeId} = 1 ;
		}
		push @{$placeHash{hashValue($nodeLon, $nodeLat)}}, $nodeId ; 
	}
	# next
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
	if ($nodeId != -1) {
		@nodeTags = @$aRef1 ;
	}
}
print "initial place node count: $place_count\n" ;
#foreach (keys %count) { print "count $_ $count{$_}\n" ; } 
#print "number of nodes with radius information found in osm: $radCount\n" ;

#############
# place areas
#############
print "looking for place areas...\n" ;
($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {	
	my $name = 0 ; my $wayName ;
	my $place = 0 ; my $pT ;
	my $open = 0 ;
	my $tag ;
	foreach $tag (@wayTags) {
		my $tmp1 = $tag ;
		if  ( ($tmp1 =~ s/://g ) == 1 ) {
			if (grep /^name:/, $tag) { $name = 1 ; $wayName = $tag ; $wayName =~ s/name:// ; }
			if (grep /^place_name:/, $tag) { $name = 1 ; $wayName = $tag ; $wayName =~ s/place_name:// ; }
			if (grep /^place:/, $tag) { $place = 1 ; $pT = $tag ; $pT =~ s/place:// ; }
		}
	}
	my $validPlace = 0 ;
	if ($place) {
		foreach $key (keys %dists) {
			if ($key eq $pT) { $validPlace = 1 ; }
		}
	}
	if ($place and $validPlace) {
		if ($wayNodes[0] != $wayNodes[-1]) { $open = 1 ; push @placeWayOpen, $wayId ; $areaOpenCount ++ ; }
		if ( $name and (!$open) ) {
			$areaCount ++ ;
			$placeWayName{$wayId} =  $wayName ;
			$placeWayType{$wayId} =  $pT ;
			$placeWayNodeId{$wayId} = 0 ;
			$placeWayDist{$wayId} = 50 ; # dist to nearest place node, init
			@{$placeWayNodes{$wayId}} = @wayNodes ;
			# put way in wayhash, calc middle first
			my $lo = 0 ; my $la = 0 ;
			foreach $key (@wayNodes) {
				$lo += $lon{$key} ; $la += $lat{$key} ;
			}
			$lo = $lo / scalar (@wayNodes) ; $la = $la / scalar (@wayNodes) ;
			$placeWayLon{$wayId} = $lo ;
			$placeWayLat{$wayId} = $la ;
			push @{$placeWayHash{hashValue($lo, $la)}}, $wayId ; 
			#print "place area found: $wayName $pT $lo $la\n" ;
		}
		else {
			$areaNoNameCount ++ ;
			push @placeWayNoName, $wayId ;
		}
	}
	# next way
	($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}
print "valid place areas found: $areaCount\n" ;
print "open areas found: $areaOpenCount\n" ;
print "areas without name found: $areaNoNameCount\n" ;

closeOsmFile () ;

#####################################################
# find place nodes for place areas, or create new one
#####################################################
print "process found areas...\n" ;
# try to locate placenodes that match the areas
my $keyWay ; my $keyNode ;
foreach $keyWay (keys %placeWayName) {
	#print "$placeWayName{$keyWay}\n" ;
	foreach $keyNode (keys %placeName) {
		#print "  $placeName{$keyNode} " ;
		my $d = distance ($lon{$keyNode}, $lat{$keyNode}, $placeWayLon{$keyWay}, $placeWayLat{$keyWay}) ;
		#print "$d\n" ;
		if ( ( $d < $placeWayDist{$keyWay}) and 
			($placeWayName{$keyWay} eq $placeName{$keyNode}) and 
			($placeWayType{$keyWay} eq $placeType{$keyNode}) ) {
			# place node nearer
			$placeWayDist{$keyWay} = $d ;
			$placeWayNodeId{$keyWay} = $keyNode ;
		}
	}
}

# assign way id to nodes, as back reference
foreach $keyWay (keys %placeWayName) {
	if ($placeWayNodeId{$keyWay} != 0) {				# area with found node
		$placeArea{$placeWayNodeId{$keyWay}} = $keyWay ;
		$placeRadius{$placeWayNodeId{$keyWay}} = 0 ;		# radius = 0 !!!
		$placeRadiusSource{$placeWayNodeId{$keyWay}} = "area" ;
		#print "*** place area $keyWay assigned to node $placeWayNodeId{$keyWay} / $placeWayName{$keyWay}\n" ;
		$assignedAreas ++ ;
	}
}

print "place areas that could be assigned to nodes: $assignedAreas\n" ;

# create nodes for not assigned areas and fill array
# these nodes will have a negative number as key and it will be the negative equivalent to the way/area key
foreach $keyWay (keys %placeWayName) {
	if ($placeWayNodeId{$keyWay} == 0) {
		push @unassignedAreas, $keyWay ;
		$placeWayNodeId{$keyWay} = -$keyWay ;
		$count{$placeWayType{$keyWay}} ++ ;
		$lon{-$keyWay} = $placeWayLon{$keyWay} ;
		$lat{-$keyWay} = $placeWayLat{$keyWay} ;
		$placeArea{-$keyWay} = $keyWay ;
		$placeType{-$keyWay} = $placeWayType{$keyWay} ;

		# TODO population
		$placePopulation{-$keyWay} = 0 ;

		$placeName{-$keyWay} =  $placeWayName{$keyWay} ;
		$placeNodes{-$keyWay} = 0 ;
		$placeFixmeNodes{-$keyWay} = 0 ;
		$placeFixmeWays{-$keyWay} = 0 ;
		$placeResidentials{-$keyWay} = 0  ;
		$placeResidentialsNoname{-$keyWay} = 0 ;
		$placeResidentialLength{-$keyWay} = 0 ;
		$placeRoads{-$keyWay} = 0  ;
		$placeAmenities{-$keyWay} = 0 ;
		$placeBuildings{-$keyWay} = 0 ;
		@{$placeStreetArray{-$keyWay}} = (0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0) ;
		$placeDistNext{-$keyWay} = 999 ;
		$placeRadius{-$keyWay} = 0 ;
		$placeRadiusSource{-$keyWay} = "area" ;
		if ( ($placeWayType{$keyWay} eq "city") or ($placeWayType{$keyWay} eq "town") ) {
			$citiesAndTowns{-$keyWay} = 1 ;
		}
	}
}

# create polygons
foreach $keyWay (keys %placeWayName) {
	my @points = () ; my $node ;
	foreach $node (@{$placeWayNodes{$keyWay}}) {
		push @points, [$lon{$node}, $lat{$node}] ;
	}
	$placeWayPolygon{$keyWay} = Math::Polygon->new(@points) ;
}

######################
# calc next place dist
######################
print "calc distances to next place...\n" ;
foreach $key1 (keys %placeName) {
	foreach $key2 (keys %placeName) {
		my $d = distance ($lon{$key1}, $lat{$key1}, $lon{$key2}, $lat{$key2}) ;
		if ( ($key1 != $key2) and ($d < $placeDistNext{$key1}) ) {
			$placeDistNext{$key1} = $d ;
		}
	}
}
	


########################
# calc auto place radius
########################
print "calc auto distances...\n" ;
# build array of street nodes
openOsmFile ($osmName) ;
skipNodes() ;
($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {	
	my $residential = 0 ;
	if (scalar (@wayNodes) >= 2) {
		foreach (@wayTags) {
			if ($_ eq "highway:residential") { $residential = 1 ; }
		}

		if ($residential) {
			my $resLon = ($lon{$wayNodes[0]} + $lon{$wayNodes[-1]}) / 2 ; 
			my $resLat = ($lat{$wayNodes[0]} + $lat{$wayNodes[-1]}) / 2 ; 

			my $lo ; my $la ;
			for ($lo=$resLon-$hashSpanCircle; $lo<=$resLon+$hashSpanCircle; $lo=$lo+0.1) {
				for ($la=$resLat-$hashSpanCircle; $la<=$resLat+$hashSpanCircle; $la=$la+0.1) {
					my ($hash_value) = hashValue ($lo, $la) ;
					my $key ;
					foreach $key ( @{$placeHash{$hash_value}} ) {
						my $i ;
						for ($i=0; $i<=$#wayNodes; $i++) {
							my $d = 1000 * distance ($lon{$wayNodes[$i]}, $lat{$wayNodes[$i]}, $lon{$key}, $lat{$key}) ;
							if ($d < 1500 ) {
								${$placeStreetArray{$key}}[(int($d/50))] ++ ;
							}
						}
					}
				}
			}
		}
	}
	else {
	}

	# next way
	($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}
closeOsmFile () ;

# parse street array and find place end by "hole" of 150m with no residential streets
foreach $key (keys %placeName) {
	my $r = -1 ;
	my $i ;
	my $autoCircleDetected = 0 ;

	if ( ($placeRadiusSource{$key} ne "osm") and ($placeRadiusSource{$key} ne "area") ) {
		for ($i=0; $i < scalar (@{$placeStreetArray{$key}})-2; $i++) {
			if (   (${$placeStreetArray{$key}}[$i] == 0) and 
			       (${$placeStreetArray{$key}}[$i+1] == 0) and 
			       (${$placeStreetArray{$key}}[$i+2] == 0)   and 
			       ($r == -1) ){
				$r = ($i+1) * 50 ;
			}
		}
		if  ( ($r >= $minRad) ) { 
			if ($r > $placeDistNext{$key}*1000/2) { $r = int ($placeDistNext{$key}*100/2) * 10 ; } # other node nearby, r = rounded to 10
			$auto++ ; $autoCircleDetected = 1 ; $autoCircle++ ;
			$placeRadius{$key} = $r / 1000 ;
			$placeRadiusSource{$key} = "auto circle" ;
		}

	}
}
print "number auto sized places: $auto\n" ;
print "number auto sized places by circle: $autoCircle\n" ;

#################################################
# parse nodes for nodeCount and other information
#################################################
print "get node distance and tag information...\n" ;

$timeA = time() ;
openOsmFile ($osmName) ;

($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
if ($nodeId != -1) {
	@nodeTags = @$aRef1 ;
}

while ($nodeId != -1) {
	# qualify node, parse for fixme etc
	my $fixme = 0 ;
	my $amenity = 0 ;
	my $tag ;
	foreach $tag (@nodeTags) {
		if (grep (/FIXME/i, $tag)) { $fixme = 1 ; }
		if (grep (/TODO/i, $tag)) { $fixme = 1 ; }
		if (grep (/incomplete/i, $tag)) { $fixme = 1 ; }
		if (grep (/^amenity:/, $tag)) { $amenity = 1 ; }
	}
	# look in vincinity of nodes
	my $lo ; my $la ;
	for ($lo=$nodeLon-$hashSpanCircle; $lo<=$nodeLon+$hashSpanCircle; $lo=$lo+0.1) {		
		for ($la=$nodeLat-$hashSpanCircle; $la<=$nodeLat+$hashSpanCircle; $la=$la+0.1) {
			my ($hash_value) = hashValue ($lo, $la) ;
			my $key ;
			foreach $key ( @{$placeHash{$hash_value}} ) {
				if (distance ($lon{$key}, $lat{$key}, $nodeLon, $nodeLat) < $placeRadius{$key}) {
					$placeNodes{$key} = $placeNodes{$key} + 1 ;
					if ($fixme) { $placeFixmeNodes{$key}++ }
					if ($amenity) { $placeAmenities{$key}++ }
				}
			}
		}
	}

	# look inside place areas (WIDER scope than above!!!)
	for ($lo=$nodeLon-$hashSpanArea; $lo<=$nodeLon+$hashSpanArea; $lo=$lo+0.1) {		
		for ($la=$nodeLat-$hashSpanArea; $la<=$nodeLat+$hashSpanArea; $la=$la+0.1) {
			my ($hash_value) = hashValue ($lo, $la) ;
			my $key ;
			foreach $key ( @{$placeWayHash{$hash_value}} ) {
				if ($placeWayPolygon{$key}->contains([$nodeLon, $nodeLat])) {
					$placeNodes{$placeWayNodeId{$key}} ++ ;
					if ($fixme) { $placeFixmeNodes{$placeWayNodeId{$key}}++ }
					if ($amenity) { $placeAmenities{$placeWayNodeId{$key}}++ }
				}
			}
		}
	}

	# next node
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
	if ($nodeId != -1) {
		@nodeTags = @$aRef1 ;
	}
}

################################
# parse ways for way information
################################
print "get way information...\n" ;

$timeA = time() ;
my $progress2 ;

($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {	
	$wayCount++ ;
	my $fixme = 0 ;
	my $residential = 0 ;
	my $Length = 0 ;
	my $road = 0 ;
	my $connecting = 0 ;
	my $name = 0 ;
	my $building = 0 ;
	my $tmpName = "-" ;
	if (scalar (@wayNodes) >= 2) {
		my $tag ; 
		foreach $tag (@wayTags) {
			my $tmp1 = $tag ;
			if  ( ($tmp1 =~ s/://g ) == 1 ) {
				if ( $tag eq "highway:residential" ) { $residential = 1 ; }
				if ( $tag eq "highway:service" ) { $residential = 1 ; }
				if ( $tag eq "highway:unclassified" ) { $residential = 1 ; }
				if ( $tag eq "highway:living_street" ) { $residential = 1 ; }
				if ( $tag eq "highway:primary" ) { $connecting = 1 ; }
				if ( $tag eq "highway:secondary" ) { $connecting = 1 ; }
				if ( $tag eq "highway:tertiary" ) { $connecting = 1 ; }
				if (grep (/^name:/, $tag)) { $name = 1 ; $tmpName = $tag ; $tmpName =~ s/name:// ; $tmpName =~ s/;/-/g ; }
				if ( $tag eq "highway:road" ) { $road = 1 ; }
				if ( $tag eq "building:yes" ) { $building = 1 ; }
				if (grep (/FIXME/i, $tag)) { $fixme = 1 ; }
				if (grep (/TODO/i, $tag)) { $fixme = 1 ; }
				if (grep (/incomplete/i, $tag)) { $fixme = 1 ; }
			}
		}

		if ($residential or $connecting) {
			# calc street length
			my $i ;
			for ($i = 0; $i<$#wayNodes; $i++) {
				$Length += distance ($lon{$wayNodes[$i]}, $lat{$wayNodes[$i]}, $lon{$wayNodes[$i+1]}, $lat{$wayNodes[$i+1]}) ;
			}
		}
		if ($residential or $connecting or $road or $building or $fixme) {
			my $resLon = ($lon{$wayNodes[0]} + $lon{$wayNodes[-1]}) / 2 ; 
			my $resLat = ($lat{$wayNodes[0]} + $lat{$wayNodes[-1]}) / 2 ; 

			# in node of node hash?
			my $lo ; my $la ;
			for ($lo=$resLon-$hashSpanCircle; $lo<=$resLon+$hashSpanCircle; $lo=$lo+0.1) {
				for ($la=$resLat-$hashSpanCircle; $la<=$resLat+$hashSpanCircle; $la=$la+0.1) {
					my ($hash_value) = hashValue ($lo, $la) ;
					my $key ;
					foreach $key ( @{$placeHash{$hash_value}} ) {
						if (distance ($resLon, $resLat, $lon{$key}, $lat{$key}) <  $placeRadius{$key} ) {
							if ($residential) { 
								$placeResidentials{$key}++ ; 
								$placeResidentialLength{$key} = $placeResidentialLength{$key} + $Length ; 
								if ($name == 0) { 
									$placeResidentialsNoname{$key}++ ; 
								} 
							} 
							if (($residential or $connecting) and ($name) ) {
								if (! defined (${$placeStreets{$key}}{$tmpName}) ) {
									${$placeStreets{$key}}{$tmpName} = $Length ; 
								}	
								else {
									${$placeStreets{$key}}{$tmpName} += $Length ;
								}
							}
							if ($road) { $placeRoads{$key}++ ; } 
							if ($building) { $placeBuildings{$key}++ ; } 
							if ($fixme) { $placeFixmeWays{$key}++ ; }
						}
					}
				}
			}

			# in wayHash area ?
			for ($lo=$resLon-$hashSpanArea; $lo<=$resLon+$hashSpanArea; $lo=$lo+0.1) {
				for ($la=$resLat-$hashSpanArea; $la<=$resLat+$hashSpanArea; $la=$la+0.1) {
					my ($hash_value) = hashValue ($lo, $la) ;
					my $key ;
					foreach $key ( @{$placeWayHash{$hash_value}} ) {
						if ( $placeWayPolygon{$key}->contains([$resLon, $resLat]) ) {
							if ($residential) { 
								$placeResidentials{$placeWayNodeId{$key}}++ ; 
								$placeResidentialLength{$placeWayNodeId{$key}} = $placeResidentialLength{$placeWayNodeId{$key}} + $Length ; 
								if ($name == 0) { $placeResidentialsNoname{$placeWayNodeId{$key}}++ ; } 
							} 
							if (($residential or $connecting) and ($name) ) {
								if (! defined (${$placeStreets{$placeWayNodeId{$key}}}{$tmpName}) ) {
									${$placeStreets{$placeWayNodeId{$key}}}{$tmpName} = $Length ; 
								}	
								else {
									${$placeStreets{$placeWayNodeId{$key}}}{$tmpName} += $Length ;
								}
							}
							if ($road) { $placeRoads{$placeWayNodeId{$key}}++ ; } 
							if ($building) { $placeBuildings{$placeWayNodeId{$key}}++ ; } 
							if ($fixme) { $placeFixmeWays{$placeWayNodeId{$key}}++ ; }
						}
					}
				}
			}
		}
	}
	else {
		$invalidWays++ ;
	}
	# next way
	($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;

print "number ways: $wayCount\n" ;
print "number invalid ways (1 node only): $invalidWays\n" ;

############
# calc means
############
print "calc means...\n" ;
# mean nodes
foreach $key (keys %dists) {
	my $nodes = 0 ;
	my $num = 0 ;
	foreach $key2 (keys %placeType) {
		if ($placeType{$key2} eq $key) { $num++ ; $nodes += $placeNodes{$key2} ; }
	}
	if ($num != 0) {
		$meanNodes{$key} = $nodes / $num ;
	}
	else {
		$meanNodes{$key} = 99999 ;
	}
}
# mean residentials
foreach $key (keys %dists) {
	my $occ = 0 ;
	my $num = 0 ;
	foreach $key2 (keys %placeType) {
		if ($placeType{$key2} eq $key) { $num++ ; $occ += $placeResidentials{$key2} ; }
	}
	if ($num != 0) {
		$meanHwyRes{$key} = $occ / $num ;
	}
	else {
		$meanHwyRes{$key} = 99999 ;
	}
}
# mean res. length
foreach $key (keys %dists) {
	my $sum = 0 ;
	my $num = 0 ;
	foreach $key2 (keys %placeType) {
		if ($placeType{$key2} eq $key) { $num++ ; $sum += $placeResidentialLength{$key2} ; }
	}
	if ($num != 0) {
		$meanHwyResLen{$key} = $sum / $num ;
	}
	else {
		$meanHwyResLen{$key} = 99999 ;
	}
}
# mean amenities
foreach $key (keys %dists) {
	my $sum = 0 ;
	my $num = 0 ;
	foreach $key2 (keys %placeType) {
		if ($placeType{$key2} eq $key) { $num++ ; $sum += $placeAmenities{$key2} ; }
	}
	if ($num != 0) {
		$meanAmenities{$key} = $sum / $num ;
	}
	else {
		$meanAmenities{$key} = 99999 ;
	}
}
# mean buildings
foreach $key (keys %dists) {
	my $sum = 0 ;
	my $num = 0 ;
	foreach $key2 (keys %placeType) {
		if ($placeType{$key2} eq $key) { $num++ ; $sum += $placeBuildings{$key2} ; }
	}
	if ($num != 0) {
		$meanBuildings{$key} = $sum / $num ;
	}
	else {
		$meanBuildings{$key} = 99999 ;
	}
}

#########################
# find superior city/town
#########################
print "find nearest city/town for smaller places...\n" ;
my $placekey1 ; my $placekey2 ; 
foreach $placekey1 (keys %placeName) {
	$placeSuperior {$placekey1} = "-" ;
	$placeSuperiorDist {$placekey1} = 9999 ;
	if (($placeType {$placekey1} eq "city") or ($placeType {$placekey1} eq "town")) {
		# do nothing
	} 
	else {
		foreach $placekey2 (keys %citiesAndTowns) {
			my $dist = distance ($lon{$placekey1}, $lat{$placekey1}, $lon{$placekey2}, $lat{$placekey2}) ;
			if (  ($dist < $placeSuperiorDist {$placekey1})  ) {
				$placeSuperior {$placekey1} = $placeName {$placekey2} ;
				$placeSuperiorDist {$placekey1} = $dist ;
			}
		}
	}
}

################################
# count unmapped/sparsely mapped
################################
foreach (keys %dists) { $unmappedCount{$_} = 0 ; $sparselyCount{$_} = 0 ; } 
foreach $key (keys %placeName) {
	if ($placeNodes{$key} < $unmapped{$placeType{$key}}) {
		$unmappedCount{$placeType{$key}} ++ ;
	}
	else {
		if ($placeNodes{$key} < $sparse{$placeType{$key}}) {
			$sparselyCount{$placeType{$key}} ++ ;
		}	
	}
}

##############
# DRAW PICTURE
##############
print "init picture...\n" ;
# calc boundaries and init
$lonMin = 999 ; $lonMax = -999 ; $latMin = 999 ; $latMax = -999 ;
foreach $key (keys %lon) {
	if ($lon{$key} > $lonMax) { $lonMax = $lon{$key} ; }
	if ($lon{$key} < $lonMin) { $lonMin = $lon{$key} ; }
	if ($lat{$key} > $latMax) { $latMax = $lat{$key} ; }
	if ($lat{$key} < $latMin) { $latMin = $lat{$key} ; }
}
initGraph ($size, $lonMin, $latMin, $lonMax, $latMax) ;

openOsmFile ($osmName) ;
skipNodes () ;
print "drawing areas...\n" ;
($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {
	foreach $key (@wayTags) {
		if ($key eq "waterway:riverbank") {
			drawArea ("lightblue", nodes2Coordinates(@wayNodes)) ;
		}
		if ($key eq "natural:water") {
			drawArea ("lightblue", nodes2Coordinates(@wayNodes)) ;
		}
		if ( ($key eq "landuse:forest") or ($key eq "natural:wood") ) {
			drawArea ("lightbrown", nodes2Coordinates(@wayNodes)) ;
		}
		if ( ($key eq "landuse:farm") or ($key eq "landuse:village_green") ) {
			drawArea ("lightgreen", nodes2Coordinates(@wayNodes)) ;
		}
		if ($key eq "landuse:residential") {
			drawArea ("lightgray", nodes2Coordinates(@wayNodes)) ;
		}
	}	
	# next
	($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;

################
# draw amenities
################
openOsmFile ($osmName) ;
print "drawing amenities...\n" ;

($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
if ($nodeId != -1) {
	@nodeTags = @$aRef1 ;
}
while ($nodeId != -1) {
	my $tag ; my $amenity = 0 ;
	foreach $tag (@nodeTags) {
		if (grep (/^amenity:/, $tag)) { $amenity = 1 ; }
	}
	if ($amenity) {
		drawNodeDot ($nodeLon, $nodeLat, "orange", 4) ; 
	}
	# next node
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
	if ($nodeId != -1) {
		@nodeTags = @$aRef1 ;
	}
}

###########
# draw ways
###########
print "drawing ways...\n" ;
($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {
	foreach $key (@wayTags) {
		if ($key eq "highway:residential") {
			drawWay ("darkgray", 2, nodes2Coordinates(@wayNodes)) ;
		}
		if ( ($key eq "highway:service") or ($key eq "highway:unclassified") or ($key eq "highway:road")) {
			drawWay ("gray", 1, nodes2Coordinates(@wayNodes)) ;
		}
		if ( ($key eq "waterway:river") or ($key eq "waterway:stream") ) {
			drawWay ("lightblue", 1, nodes2Coordinates(@wayNodes)) ;
		}
		if ($key eq "highway:motorway") {
			drawWay ("blue", 3, nodes2Coordinates(@wayNodes)) ;
		}
		if ($key eq "highway:motorway_link") {
			drawWay ("blue", 2, nodes2Coordinates(@wayNodes)) ;
		}
		if ($key eq "highway:trunk") {
			drawWay ("blue", 3, nodes2Coordinates(@wayNodes)) ;
		}
		if ($key eq "highway:trunk_link") {
			drawWay ("blue", 2, nodes2Coordinates(@wayNodes)) ;
		}
		if ( ($key eq "highway:primary") or ($key eq "highway:primary_link") ) {
			drawWay ("red", 2, nodes2Coordinates(@wayNodes)) ;
		}
		if ($key eq "highway:secondary") {
			drawWay ("red", 2, nodes2Coordinates(@wayNodes)) ;
		}
		if ($key eq "highway:tertiary") {
			drawWay ("darkgreen", 2, nodes2Coordinates(@wayNodes)) ;
		}
	}	
	# next	
	($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}
closeOsmFile () ;

####################################
# draw place names and areas/circles
####################################
print "draw places...\n" ;
foreach $key (keys %placeName) {
	my $data ;
	drawTextPos ($lon{$key}, $lat{$key}, 0, 0, $placeName{$key}, "black", 2) ;

	$data = $placeNodes{$key} . "Nds " . $placeResidentials{$key} ."Res " . int ($placeResidentialLength{$key} + 0.5) . "RLen " . $placeAmenities{$key} . "Am " . $placeBuildings{$key} . "Bld" ;
	drawTextPos ($lon{$key}, $lat{$key}, 0, -15, $data, "blue", 2) ;

	if ($placeRadiusSource{$key} eq "default") {
		drawCircleRadiusText ($lon{$key}, $lat{$key}, $placeRadius{$key}*1000, 1, "darkgray", "default " . $placeRadius{$key}*1000 . "m" ) ;
	}
	if ($placeRadiusSource{$key} eq "osm") {
		drawCircleRadiusText ($lon{$key}, $lat{$key}, $placeRadius{$key}*1000, 1, "blue", "osm " . $placeRadius{$key}*1000 . "m" ) ;
	}
	if ($placeRadiusSource{$key} eq "auto circle") {
		drawCircleRadiusText ($lon{$key}, $lat{$key}, $placeRadius{$key}*1000, 1, "tomato", "auto circle " . $placeRadius{$key}*1000 . "m" ) ;
	}
	if ($placeRadiusSource{$key} eq "area") {
		drawWay ("blue", 2, nodes2Coordinates( @{$placeWayNodes{$placeArea{$key}}} )) ; 
	}
}

########################
# draw other information
########################
print "draw other information and save picture...\n" ;
drawLegend (3, "Farm", "lightgreen", "Forest", "lightbrown", "Amenity", "orange", "Water", "lightblue", "Residential", "darkgray", "Tertiary", "darkgreen", "Primary/Secondary", "red", "Motorway", "blue", "Legend", "black") ;
drawRuler ("darkgray") ;
drawHead ("gary68's mapping quality for " . stringFileInfo ($osmName), "black", 3) ;
drawFoot ("data by www.openstreetmap.org", "gray", 2) ;
writeGraph ($pngName) ;

$time1 = time () ;

##################
# PRINT HTML INFOS
##################
print "write output files...\n" ;
open ($html, ">", $htmlName) || die ("Can't open html output file") ;
open ($text, ">", $textName) || die ("Can't open text output file") ;
open ($csv, ">", $csvName) || die ("Can't open csv output file") ;
print $csv stringFileInfo ($osmName), "\n\n" ;

printHTMLHeader ($html, "Mapping Quality by Gary68") ;
print $html "<H1>Mapping Quality by Gary68</H1>\n" ;
print $html "<p>Version ", $version, "</p>\n" ;
print $html "<H2>Info and statistics</H2>\n" ;
print $html "<p>", stringFileInfo ($osmName), "<br>\n" ;

# print counts to HTML
printHTMLTableHead ($html) ;
printHTMLTableHeadings ($html, "Type", "Count", "unmapped", "percent", "sparsely mapped", "percent") ; 
printHTMLRowStart ($html) ;
my $um = 0 ; my $sm = 0 ; my $ump ; my $smp ;
foreach $key (keys %unmapped) {
	$um += $unmappedCount{$key} ;
	$sm += $sparselyCount{$key} ;
} 
$ump = int ($um / $place_count *100) ;
$smp = int ($sm / $place_count * 100) ;
printHTMLCellLeft ($html, "total") ; printHTMLCellRight ($html, $place_count) ;
printHTMLCellRight ($html, $um) ; printHTMLCellRight ($html, $ump . "%") ;
printHTMLCellRight ($html, $sm) ; printHTMLCellRight ($html, $smp . "%") ;
printHTMLRowEnd ($html) ;
foreach $key (sort keys %count) { 
	if ($count{$key} > 0) {
		$ump = int ($unmappedCount{$key} / $count{$key} *100) ;
		$smp = int ($sparselyCount{$key} / $count{$key} * 100) ;
	}
	else {
		$ump = "-" ; $smp = "-" ;
	}
	printHTMLRowStart ($html) ;
	printHTMLCellLeft ($html, $key) ; 
	printHTMLCellRight ($html, $count{$key}) ; 
	printHTMLCellRight ($html, $unmappedCount{$key}) ; 
	printHTMLCellRight ($html, $ump . "%") ;
	printHTMLCellRight ($html, $sparselyCount{$key}) ; 
	printHTMLCellRight ($html, $smp . "%") ;
	printHTMLRowEnd ($html) ;
} 
printHTMLTableFoot ($html) ;

my $percent ;
if ($assignedAreas >0) { $percent = int ($assignedAreas / $areaCount * 100) ; } else { $percent = 0 ; }
print $html "<p>valid place areas found: $areaCount<br>\n" ;
print $html "areas that could be assigned to a place node: $assignedAreas, about $percent percent<br>\n" ;
print $html "open areas found: $areaOpenCount<br>\n" ;
print $html "areas without name found: $areaNoNameCount</p>\n" ;

print $html "<H3>Unassigned place areas</H3>\n" ;
printHTMLTableHead ($html) ;
printHTMLTableHeadings ($html, "way key", "way name", "way type", "JOSM") ; 
foreach $key (@unassignedAreas) {
	printHTMLTableRowLeft ($html, $key, $placeWayName{$key}, $placeWayType{$key}, josmLinkSelectWay ($placeWayLon{$key}, $placeWayLat{$key}, 0.05, $key)) ;
}
printHTMLTableFoot ($html) ;

print $html "<H3>Unused place ways</H3>\n" ;
print $html "<p>Open Areas: " ;
foreach $key (@placeWayOpen) {
	print $html historyLink ("way", $key), " " ;
}
print $html "</p>" ;
print $html "<p>Areas without name: " ;
foreach $key (@placeWayNoName) {
	print $html historyLink ("way", $key), " " ;
}
print $html "</p>" ;

print $html "<H3>Auto detected place sizes</H3>\n" ;
print $html "<p>total: $auto<br>\n" ;
print $html "circle: $autoCircle<br>\n" ;



# print parameters
print $html "<H3>Parameters</H3>" ;
printHTMLTableHead ($html) ;
printHTMLTableHeadings ($html, "Place", "Distance", "Sparse Threshold", "Unmapped Threshold") ; 
foreach $key2 (sort keys %dists) {
	print $html "<tr><td>$key2</td> <td align=\"right\">$dists{$key2}</td> <td align=\"right\">$sparse{$key2}</td> <td align=\"right\">$unmapped{$key2}</td></tr>\n" ;
}
printHTMLTableFoot ($html) ;

# print means
print $html "<H3>Mean numbers per place type / DE Mittelwerte</H3>" ;
printHTMLTableHead ($html) ;
printHTMLTableHeadings ($html, "Place", "Mean Nodes / Knoten", "Mean Hwy Res / Wohnstraßen", "Mean Hwy Res Length / Wohnstr.-Länge", "Mean Amenities / Annehmlichkeiten", "Mean Buildings / Gebäude") ;
foreach $key (sort keys %dists) {
	print $html "<tr>\n" ;
	print $html "<td>$key</td>" ;
	printf $html "<td align=\"right\">%i</td>\n", $meanNodes{$key} ;
	printf $html "<td align=\"right\">%i</td>\n", $meanHwyRes{$key} ;
	printf $html "<td align=\"right\">%i</td>\n", $meanHwyResLen{$key} ;
	printf $html "<td align=\"right\">%i</td>\n", $meanAmenities{$key} ;
	printf $html "<td align=\"right\">%i</td>\n", $meanBuildings{$key} ;
	print $html "</tr>\n" ;
}
printHTMLTableFoot ($html) ;

# print unmapped/sparsely mapped file information
print $text "lat\tlon\ttitle\tdescription\ticon\ticonSize\ticonOffset\n" ;

print $html "<H2>Details unmapped/sparsely mapped</H2>\n" ;
print $html "<p><strong>For details of all places see next section!</strong></p>" ;
print $html "<p>To decide whether a place is potentially mapped or sparsely mapped two numbers are used per place type to compare against " ;
print $html "the actual node count. Only nodes are counted that are located within a certain distance (per place) from the place node.</p>" ;
print $html "<p><strong>DE</strong> Um zu entscheiden, ob ein Ort kartografiert ist oder nicht werden zwei Schwellwerte je Ort-Typ herangezogen, gegen die die tatsächliche Node-Anzahl verglichen wird. Es werden nur Nodes innerhalb eines bestimmten Radius gewertet.</p>" ; 

printHTMLTableHead ($html) ;
printHTMLTableHeadings ($html, "Place", "Type", "Near", "Attribute", "Node count") ;
foreach $key (keys %placeType) {
	my $tmp ; my $icon ;
	if ($placeNodes{$key} < $sparse{$placeType{$key}}) {
		if ($placeNodes{$key} < $unmapped{$placeType{$key}}) {
			$tmp = "unmapped" ;
			$icon = $iconRed ;
		}
		else {
			$tmp = "sparsely mapped" ;
			$icon = $iconYellow ;
		}
		my $out =  "<tr>\n" ;
		$out = $out . "<td>" . $placeName{$key} . "</td>\n" ;
		$out = $out . "<td>" . $placeType{$key} . "</td>\n" ;
		$out = $out . "<td>" . $placeSuperior {$key} . "</td>\n" ;
		$out = $out . "<td>" . $tmp . "</td>\n" ;
		$out = $out . "<td align=\"right\">" . $placeNodes{$key} . "</td>\n" ;
		$out = $out . "</tr>\n" ;
		push @outArray, $out ;

		# slippy file
		print $text $lat {$key}, "\t" ;
		print $text $lon {$key}, "\t" ;
		print $text $placeName {$key}, "\t" ;
		print $text "potentially $tmp place\t" ;
		print $text "./", $icon, "\t" ;
		print $text "24,24", "\t" ;
		print $text "-12,-12", "\n" ;
	}
}
@outArray = sort @outArray ;
foreach (@outArray) { print $html $_ ; }
printHTMLTableFoot ($html) ;

close ($text) ;

@outArray = () ;

print $csv "Place;Type;NodeId;Rad(m);RadiusSource;AreaId;Population;Near;Nodes;FixmeNodes;Residentials;ResLength;ResNoName;Roads;FixmeWays;Amenities;Buildings\n" ;

print $html "<H2>Details all information</H2>\n" ;
print $html "<H3>Legend</H3>\n" ;
printHTMLTableHead ($html) ;
printHTMLTableHeadings ($html, "Column", "Meaning/Explanation", "<strong>DE</strong> Bedeutung/Erklärung") ;
printHTMLTableRowLeft ($html, "Name", "Place Name", "Ort Name") ;
# TODO if time
print $html "<tr><td>Type</td><td>Place Type</td><td>Ort Typ</td></tr>\n" ;
print $html "<tr><td>Popul.</td><td>Population from same key or OpenGeoDB entry</td><td>Einwohner</td></tr>\n" ;
print $html "<tr><td>Near</td><td>Location of place</td><td>Liegt in der Nähe von</td></tr>\n" ;
print $html "<tr><td>Nodes</td><td>Number of nodes inside place distance</td><td>Anzahl Knoten</td></tr>\n" ;
print $html "<tr><td>Fixme Nds</td><td>number of nodes with FIXME or similar tag</td><td>Anzahl FIXME Knoten</td></tr>\n" ;
print $html "<tr><td>Hwy Res</td><td>number of highways=residential inside place distance</td><td>Anzahl Wohnstraßen</td></tr>\n" ;
print $html "<tr><td>Res Len</td><td>length of these residentials</td><td>Länge der Wohnstraßen</td></tr>\n" ;
print $html "<tr><td>Res w/o n.</td><td>number of residentials without name inside place distance</td><td>Wohnstraßen ohne Name</td></tr>\n" ;
print $html "<tr><td>Roads</td><td>number of roads inside place distance</td><td>Anzahl Roads</td></tr>\n" ;
print $html "<tr><td>Fixme Ways</td><td>number of ways with FIXME or similar tag inside place distance</td><td>Anzahl FIXME ways</td></tr>\n" ;
print $html "<tr><td>Amenit.</td><td>number of amenities inside place distance</td><td>Anzahl Annhemlichkeiten</td></tr>\n" ;
print $html "<tr><td>Build.</td><td>number of buildings inside place distance</td><td>Anzahl Gebäude</td></tr>\n" ;
printHTMLTableFoot ($html) ;

# print data
print $html "<H3>Data</H3>\n" ;
printHTMLTableHead ($html) ;
printHTMLTableHeadings ($html, "Place", "Type", "Rad(km)", "Popul.", "Near", "Nodes", "FIXME Nds", "Hwy Res", "Res Len", "Res w/o n.", "Roads", "FIXME ways", "Ameni.", "Buildings") ;
foreach $key (keys %placeType) {
	my $out = "<tr>\n" ;
	$out = $out . "<td>" . $placeName{$key} . " " . historyLink ("node", $key) . " " . josmLinkSelectNode ($lon{$key}, $lat{$key}, 0.005, $key) . "</td>\n" ;
	$out = $out . "<td>" . $placeType{$key} . "</td>\n" ;
	$out = $out . "<td align=\"right\">" . $placeRadiusSource{$key} . " " . $placeRadius{$key} . "</td>\n" ;
	$out = $out . tableCellZeroRed ($placePopulation{$key}) ;
	$out = $out . "<td>" . $placeSuperior{$key} . "</td>\n" ;
	$out = $out . tableCellNodes ($placeNodes{$key}, $sparse{$placeType{$key}}, $unmapped{$placeType{$key}} )  ;
	$out = $out . tableCellZero ($placeFixmeNodes{$key} ) ;
	$out = $out . "<td align=\"right\">" . $placeResidentials{$key} . "</td>\n" ;
	my $tmp = int ($placeResidentialLength{$key} + 0.5 )  ;
	$out = $out . "<td align=\"right\">" . $tmp . "</td>\n" ;
	$out = $out . tableCellZero ($placeResidentialsNoname{$key} ) ;
	$out = $out . tableCellZero ($placeRoads{$key} ) ;
	$out = $out . tableCellZero ($placeFixmeWays{$key} ) ;
	$out = $out . tableCellZeroRed ($placeAmenities{$key} ) ;
	$out = $out . tableCellZeroRed ($placeBuildings{$key} ) ;
	$out = $out . "</tr>\n" ;
	push @outArray, $out ;

	$out = $placeName{$key} . ";" ;
	$out = $out . $placeType{$key} . ";" ;
	$out = $out . $key . ";" ;
	$out = $out . $placeRadius{$key}*1000 . ";" ;
	$out = $out . $placeRadiusSource{$key} . ";" ;
	$out = $out . $placeArea{$key} . ";" ;
	$out = $out . $placePopulation{$key} . ";" ;
	$out = $out . $placeSuperior {$key} . ";" ;
	$out = $out . $placeNodes{$key} . ";" ;
	$out = $out . $placeFixmeNodes{$key} . ";" ;
	$out = $out . $placeResidentials{$key} . ";" ;
	$out = $out . $tmp . ";" ; # RES LENGTH !
	$out = $out . $placeResidentialsNoname{$key} . ";" ;
	$out = $out . $placeRoads{$key} . ";" ;
	$out = $out . $placeFixmeWays{$key} . ";" ;
	$out = $out . $placeAmenities{$key} . ";" ;
	$out = $out . $placeBuildings{$key} ; # ATTENTION: no colon here!!!
	$out = $out . "\n" ;
	print $csv $out ;
}
@outArray = sort @outArray ;
foreach (@outArray) { print $html $_ ; }
printHTMLTableFoot ($html) ;

########
# FINISH
########
print $html "<p>", stringTimeSpent ($time1-$time0), "</p>\n" ;
printHTMLFoot ($html) ;
close ($html) ;
close ($text) ;
close ($csv) ;

open ($street, ">", $streetName) || die ("Can't open street csv output file") ;
print $street stringFileInfo ($osmName), "\n\n" ;
print $street "PlaceName;PlaceKey;Street;Length(m)\n" ;
my $k1 ; my $k2 ; my @printArray ;
foreach $k1 (keys %placeStreets) {
	foreach $k2 (sort keys %{$placeStreets{$k1}}) {
		my $len = int (${$placeStreets{$k1}}{$k2} * 1000) ;
		push @printArray, $placeName{$k1}. ";" . $k1. ";" . $k2. ";" . $len . "\n" ;
	}
}
@printArray = sort @printArray ;
foreach (@printArray) {
	print $street $_ ;
}
close ($street) ;


print "\n$program finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;

#############################################################################################################
# sub routines
#############################################################################################################

sub tableCellMean {
	my ($value, $mean) = @_ ;
	my $color ;

	if ($value >= (1.5 * $mean) ) {
		$color = $colorGreen ;	
	}
	else {
		if ($value >= $mean) {
			$color = $colorGrey ;	
		}
		else {
			if ($value >= 0.5 * $mean) {
				$color = $colorOrange ;	
			}
			else {
				$color = $colorRed ;	
			}
		}
	}
	return ("<td align=\"right\" bgcolor=\"" . $color . "\">" . $value . "</td>") ;
}

sub tableCellZero {
	my $value = shift ;
	my $color ;
	if ($value == 0 ) {
		$color = $colorGreen ;	
	}
	else
	{
		$color = $colorRed ;	
	}
	return ("<td align=\"right\" bgcolor=\"" . $color . "\">" . $value . "</td>") ;
}

sub tableCellZeroRed {
	my $value = shift ;
	my $color ;
	if ($value == 0 ) {
		$color = $colorRed ;	
	}
	else
	{
		$color = $colorGreen ;	
	}
	return ("<td align=\"right\" bgcolor=\"" . $color . "\">" . $value . "</td>") ;
}

sub tableCellNodes {
	my ($value, $sm, $um) = @_ ;
	my $color ;
	if ($value > $sm ) {
		$color = $colorGreen ;	
	}
	else {
		if ($value > $um) {
			$color = $colorOrange ;
		}
		else {
			$color = $colorRed ;
		}
	}
	return ("<td align=\"right\" bgcolor=\"" . $color . "\">" . $value . "</td>") ;
}


sub nodes2Coordinates {
#
# transform list of nodeIds to list of lons/lats
#
	my @nodes = @_ ;
	my $i ;
	my @result = () ;

	#print "in @nodes\n" ;

	for ($i=0; $i<=$#nodes; $i++) {
		push @result, $lon{$nodes[$i]} ;
		push @result, $lat{$nodes[$i]} ;
	}
	return @result ;
}


