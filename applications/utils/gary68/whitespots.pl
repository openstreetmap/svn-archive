# 
#
# whitespots.pl by gary68
#
#
#
# Copyright (C) 2010, Gerhard Schwanz
#
# This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 3 of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>
#
#
#
#

use strict ;
use warnings ;

use OSM::osm 5.1 ;
use OSM::QuadTree ;
use File::stat;
use Time::localtime;


my $program = "whitespots.pl" ;
my $usage = $program . " file.osm out.htm" ;
my $version = "2.2" ;

my %excludes = () ;
$excludes{"locality"} = 1 ;
$excludes{"farm"} = 1 ;
$excludes{"region"} = 1 ;
$excludes{"island"} = 1 ;

my $maxDist = 0.3 ;
my $limit = 1000 ;
my $highwayNodeLimit = 15 ;

my $wayId ; 
my $wayUser ; my @wayNodes ; my @wayTags ;
my $nodeId ; 
my $nodeUser ; my $nodeLat ; my $nodeLon ; my @nodeTags ;
my $aRef1 ; my $aRef2 ;

my %placeName = () ;
my %placeType = () ;
my %placeCount = () ;
my %placeNodes = () ;
my %evalNodes = () ;
my @result = () ;

my $time0 = time() ; my $time1 ; my $timeA ;

my $html ;
my $osmName ;
my $htmlName ;

my %lon ; my %lat ;

###############
# get parameter
###############
$osmName = shift||'';
if (!$osmName)
{
	die (print $usage, "\n");
}

$htmlName = shift||'';
if (!$htmlName)
{
	die (print $usage, "\n");
}

print "\n$program $version for file $osmName\n\n" ;


my $minLon = 999 ;
my $minLat = 999 ;
my $maxLon = -999 ;
my $maxLat = -999 ;


openOsmFile ($osmName) ;
print "pass 1: find places...\n" ;
my $placeCount = 0 ;
my $nodeCount = 0 ;

($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
if ($nodeId != -1) {
	@nodeTags = @$aRef1 ;
}

while ($nodeId != -1) {

	$nodeCount++ ;
	my $place = 0 ;
	my $type = "unknown" ;
	my $name = "unknown" ;

	if ($nodeLon > $maxLon) {$maxLon = $nodeLon ; }
	if ($nodeLat > $maxLat) {$maxLat = $nodeLat ; }
	if ($nodeLon < $minLon) {$minLon = $nodeLon ; }
	if ($nodeLat < $minLat) {$minLat = $nodeLat ; }

	foreach my $t (@nodeTags) {
		if ( ($t->[0] eq "place") and ( ! defined $excludes{$t->[1]}) ) { 
			$place = 1 ; 
			$type = $t->[1] ; 
		}
		if ($t->[0] eq "name") { $name = $t->[1] ; }
	}

	if ($place) {
		$placeCount++ ;
		$placeName{$nodeId} = $name ;
		$placeType{$nodeId} = $type ;
		$placeCount{$nodeId} = 0 ;
		$lon{$nodeId} = $nodeLon ;
		$lat{$nodeId} = $nodeLat ;
	}

	# next
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
	if ($nodeId != -1) {
		@nodeTags = @$aRef1 ;
	}
}

closeOsmFile () ;

print "$nodeCount nodes read.\n" ; 
print "$placeCount places found.\n" ; 

# init quadtree

my $placeTree = OSM::QuadTree->new(  -xmin  => $minLon,
                                      -xmax  => $maxLon,
                                      -ymin  => $minLat,
                                      -ymax  => $maxLat,
                                      -depth => 6);

foreach my $p (keys %placeName) {
	$placeTree->add ($p, $lon{$p}, $lat{$p}, $lon{$p}, $lat{$p}) ;
}


openOsmFile ($osmName) ;
print "pass 2: find nodes near places...\n" ;


my $actualNode = 0 ;
my $actualPercent = 10 ;

($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
if ($nodeId != -1) {
	@nodeTags = @$aRef1 ;
}

while ($nodeId != -1) {

	$actualNode++ ;	
	if ($actualNode / $nodeCount * 100 >= $actualPercent)  {
		print "$actualPercent percent of pass 2 done...\n" ;
		$actualPercent += 10 ;
	}

	my $ref = $placeTree->getEnclosedObjects ($nodeLon-0.01, $nodeLat-0.01, $nodeLon+0.01, $nodeLat+0.01) ;
	my @placesTemp = @$ref ;
	# print $#placesTemp, " @placesTemp\n" ;

	foreach my $p (@placesTemp) {
		my $dist = distance ($nodeLon, $nodeLat, $lon{$p}, $lat{$p}) ;
		if ( ($dist < $maxDist) and ($p != $nodeId) ) {
			$placeNodes{$p}{$nodeId} = 1 ;
			$evalNodes{$nodeId} = 0 ;
		}
	}

	# next
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
	if ($nodeId != -1) {
		@nodeTags = @$aRef1 ;
	}
}



#############################
# identify check/against ways
#############################
print "pass 3: parsing ways...\n" ;
($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {	

	my $hw = 0 ;
	foreach my $tag (@wayTags) {
		if ($tag->[0] eq "highway") {
			$hw = 1 ;
		}
	} 

	if ($hw) {
		foreach my $n (@wayNodes) {
			if (defined $evalNodes{$n}) { $evalNodes{$n}++ ; }
		}
	}

	# next way
	($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;


foreach my $p (keys %placeName) {
	foreach my $e (keys %{$placeNodes{$p}}) {
		if ($evalNodes{$e} > 0) { $placeCount{$p}++ ; }
	}
}

foreach my $p (keys %placeName) {
	push @result, [$p, $placeCount{$p}] ;
}

@result = sort { $a->[1] <=> $b->[1] } @result ;

if ($#result > $limit) { @result = @result[0..$limit] ; }
print "result list limited to $limit entries.\n" ;



$time1 = time () ;


my @excludeArray = keys %excludes ;

##################
# PRINT HTML INFOS
##################
open ($html, ">", $htmlName) || die ("Can't open html output file") ;


printHTMLiFrameHeader ($html, "White spots by Gary68") ;

print $html "<H1>White spots by Gary68</H1>\n" ;
print $html "<p>Version ", $version, "</p>\n" ;
print $html "<H2>Statistics</H2>\n" ;
print $html "<p>", stringFileInfo ($osmName), "</p>\n" ;
print $html "<p>Max dist from place for nodes $maxDist</p>\n" ;
print $html "<p>Highway node limit to qualify for \"white spot\": $highwayNodeLimit</p>\n" ;
print $html "<p>Limit result list to $limit</p>\n" ;
print $html "<p>Excluded place types: @excludeArray</p>\n" ;

print $html "<H2>Data</H2>\n" ;
print $html "<table border=\"1\">\n";
print $html "<tr>\n" ;
print $html "<th>Highway Node Count</th>\n" ;
print $html "<th>Node Count</th>\n" ;
print $html "<th>Place name</th>\n" ;
print $html "<th>Place type</th>\n" ;
print $html "<th>Node history</th>\n" ;
print $html "<th>Link</th>\n" ;
print $html "<th>JOSM</th>\n" ;
print $html "<th>PIC</th>\n" ;
print $html "</tr>\n" ;

my $i = 0 ;

foreach my $res (@result) {

	my $node = $res->[0] ;
	my $nodeCount = $res->[1] ;
	my $name = $placeName{$node} ;
	my $type = $placeType{$node} ;
	my $nodes = scalar (keys %{$placeNodes{$node}}) ;

	if ($nodeCount <= $highwayNodeLimit) {
		$i++ ;
		print $html "<tr>\n" ;
		print $html "<td>", $nodeCount, "</td>\n" ;
		print $html "<td>", $nodes, "</td>\n" ;
		print $html "<td>", $name, "</td>\n" ;
		print $html "<td>", $type, "</td>\n" ;
		print $html "<td>", historyLink ("node", $node), "</td>\n" ;
		print $html "<td>", osmLink ($lon{$node}, $lat{$node}, 16) , "<br>\n" ;
		print $html osbLink ($lon{$node}, $lat{$node}, 16) , "<br>\n" ;
		print $html mapCompareLink ($lon{$node}, $lat{$node}, 16) , "</td>\n" ;
		print $html "<td>", josmLinkSelectNode ($lon{$node}, $lat{$node}, 0.01, $node), "</td>\n" ;
		print $html "<td>", picLinkMapnik ($lon{$node}, $lat{$node}, 15), "</td>\n" ;
		print $html "</tr>\n" ;
	}
}

print $html "</table>\n" ;



########
# FINISH
########

print $html "<p>$i places listed</p>\n" ;
print $html "<p>", stringTimeSpent ($time1-$time0), "</p>\n" ;
printHTMLFoot ($html) ;

close ($html) ;


print "\n$program finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;



