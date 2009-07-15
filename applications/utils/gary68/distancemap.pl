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

use strict ;
use warnings ;

use OSM::osm 4.0 ;
use OSM::osmgraph 2.0 ;

my $programName = "distancemap.pl" ;
my $usage = "distancemap.pl file.osm out.png size" ; # svg and other output names are automatic
my $version = "1.0 BETA (001)" ;

#
# ENTER INFORMATION IN THIS SECTION
# ---------------------------------
#

# enter destination node id here
my $root = 88453792 ;

# add all allowed pathes here
my %allowedPaths = () ;
$allowedPaths{"highway:residential"} = 1 ;
$allowedPaths{"highway:primary"} = 1 ;
$allowedPaths{"highway:secondary"} = 1 ;
$allowedPaths{"highway:tertiary"} = 1 ;
$allowedPaths{"highway:service"} = 1 ;
# etc...

#
#
#

my $infinity = "inf";
my %dist ;
my %edge ;
my %prev ;
my @s ;
my %usedNodes ;
my @unsolved ;
my %wayCount = () ;

my $labelMinLength = 0.1 ; # min length of street so that it will be labled / needs adjustment according to picture size

my $wayId ;
my $wayUser ;
my @wayNodes ;
my @wayTags ;
my $nodeId ;
my $nodeUser ;
my $nodeLat ;
my $nodeLon ;
my @nodeTags ;
my $aRef1 ;
my $aRef2 ;

my $osmName ; 
my $pngName ;

my %lon ; my %lat ;
my %placeName ;

my $size ;
my $lonMin ; my $latMin ; my $lonMax ; my $latMax ;

my $time0 ; my $time1 ;

# get parameter

$osmName = shift||'';
if (!$osmName)
{
	die (print $usage, "\n");
}

$pngName = shift||'';
if (!$pngName)
{
	die (print $usage, "\n");
}

$size = shift||'';
if (!$size)
{
	$size = 4096 ; # default size
}

print "\n$programName $version for file $osmName\n" ;

$time0 = time() ;

# get all node locations and place information

print "get node information...\n" ;
openOsmFile ($osmName) ;

($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
if ($nodeId != -1) {
	@nodeTags = @$aRef1 ;
}

while ($nodeId != -1) {

	$lon{$nodeId} = $nodeLon ;	
	$lat{$nodeId} = $nodeLat ;	

	my $place = 0 ; my $name = 0 ; my $tag ; my $nameStr ;
	foreach $tag (@nodeTags) {
		my $tmp = $tag ;
		if ((grep /name:/ , $tag) and ( ($tmp =~ s/://g ) == 1 ) and 
			( ! (grep /place_name:/ , $tag)) ) { 
			$nameStr = $tag ; $nameStr =~ s/name:// ;
			$name = 1 ; 
		}
		if (grep /place:/, $tag)  { $place = 1 ; }
	}
	if (($place) and ($name)) {
		$placeName{$nodeId} = $nameStr ;
		#print $nodeId, " ", $nameStr, "\n" ;
	}

	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
	if ($nodeId != -1) {
		@nodeTags = @$aRef1 ;
	}
}

# calc area of pic

$lonMin = 999 ; $lonMax = -999 ; $latMin = 999 ; $latMax = -999 ;
my $key ;
foreach $key (keys %lon) {
	if ($lon{$key} > $lonMax) { $lonMax = $lon{$key} ; }
	if ($lon{$key} < $lonMin) { $lonMin = $lon{$key} ; }
	if ($lat{$key} > $latMax) { $latMax = $lat{$key} ; }
	if ($lat{$key} < $latMin) { $latMin = $lat{$key} ; }
}

initGraph ($size, $lonMin, $latMin, $lonMax, $latMax) ;

enableSVG () ;

# draw areas first

print "draw areas and init dijkstra data...\n" ;

($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {
	my $highway = "none" ;
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
		if (grep /^highway:/, $key) { $highway = $key ; }
	}	

	# init dijkstra data
	if ( (defined ($allowedPaths{$highway})) and (scalar (@wayNodes) > 1 ) ) {
		# set distances
		my $i ;
		for ($i=0; $i<$#wayNodes; $i++) {
			my $dist = distance ($lon{$wayNodes[$i]}, $lat{$wayNodes[$i]}, $lon{$wayNodes[$i+1]}, $lat{$wayNodes[$i+1]}) ;
			$edge{$wayNodes[$i]}{$wayNodes[$i+1]} = $dist ;
			$edge{$wayNodes[$i+1]}{$wayNodes[$i]} = $dist ;
		}

		# incr waycount per node (count > 0 --> crossing)
		foreach my $node (@wayNodes) {
			if (defined ($wayCount{$node})) {
				$wayCount{$node} ++ ;
			}
			else {
				$wayCount{$node} = 1 ;
			}
			$usedNodes{$node} = 1 ;
		}
	
	}	

	($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;

# DIJKSTRA
print "dijkstra running...\n" ;

# init unsolved nodes array
foreach my $node (keys %usedNodes) { push @unsolved, $node ; }

# alg
# all dist = infinity
foreach my $n (keys %usedNodes) { 
	$dist{$n} = $infinity ; 
	$prev{$n}=$n ; 
}

$dist{$root} = 0;

# loop while we have unsolved nodes
while (@unsolved) {
	my ($n, $n2) ;
	@unsolved = sort byDistance @unsolved;
	push @s, $n = shift @unsolved;
	foreach $n2 (keys %{$edge{$n}}) {
		if (($dist{$n2} eq $infinity) ||
			($dist{$n2} > ($dist{$n} + $edge{$n}{$n2}) )) {
			$dist{$n2} = $dist{$n} + $edge{$n}{$n2} ;
			$prev{$n2} = $n;
		}
	}
}




# draw ways

print "draw ways...\n" ;

openOsmFile ($osmName) ;
skipNodes () ;

($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {
	my $name = "" ; my $ref = "" ;
	my $length = 0 ;
	foreach $key (@wayTags) {
		if (grep /^name:/, $key) { $name = $key ; $name =~ s/name:// ; }
		if (grep /^ref:/, $key) { $ref = $key ; $ref =~ s/ref:// ; }
	}

	if (scalar @wayNodes > 1) {


		my $i ;
		for ($i = 0; $i<$#wayNodes; $i++) {
			$length += distance ($lon{$wayNodes[$i]}, $lat{$wayNodes[$i]}, $lon{$wayNodes[$i+1]}, $lat{$wayNodes[$i+1]}) ;
		}


		foreach $key (@wayTags) {
			if ($key eq "highway:residential") {
				drawWay ("gray", 1, nodes2Coordinates(@wayNodes)) ;
				if ( ($name ne "") and ($length > $labelMinLength) ) { labelWay ("black", 0, "", $name, -2, nodes2Coordinates(@wayNodes)) ; }
			}
			if ( ($key eq "highway:service") or ($key eq "highway:unclassified") ) {
				drawWay ("gray", 1, nodes2Coordinates(@wayNodes)) ;
			}
			if ( ($key eq "waterway:river") or ($key eq "waterway:stream") ) {
				drawWay ("lightblue", 1, nodes2Coordinates(@wayNodes)) ;
			}
			if ($key eq "highway:motorway") {
				drawWay ("blue", 3, nodes2Coordinates(@wayNodes)) ;
				if ( ($ref ne "") and ($length > $labelMinLength) ) { labelWay ("blue", 4, "", $ref, -8, nodes2Coordinates(@wayNodes)) ; }
			}
			if ($key eq "highway:motorway_link") {
				drawWay ("blue", 2, nodes2Coordinates(@wayNodes)) ;
			}
			if ($key eq "highway:trunk") {
				drawWay ("blue", 3, nodes2Coordinates(@wayNodes)) ;
				if ( ($ref ne "") and ($length > $labelMinLength) ) { labelWay ("blue", 4, "", $ref, -8, nodes2Coordinates(@wayNodes)) ; }
			}
			if ($key eq "highway:trunk_link") {
				drawWay ("blue", 2, nodes2Coordinates(@wayNodes)) ;
			}
			if ( ($key eq "highway:primary") or ($key eq "highway:primary_link") ) {
				drawWay ("red", 2, nodes2Coordinates(@wayNodes)) ;
			}
			if ( ($key eq "highway:primary") ) {
				if ( ($ref ne "") and ($length > $labelMinLength) ) { labelWay ("red", 2, "", $ref, -3, nodes2Coordinates(@wayNodes)) ;  }
			}
			if ($key eq "highway:secondary") {
				drawWay ("red", 2, nodes2Coordinates(@wayNodes)) ;
				if ( ($ref ne "") and ($length > $labelMinLength) ) { labelWay ("red", 2, "", $ref, -3, nodes2Coordinates(@wayNodes)) ;  }
			}
			if ($key eq "highway:tertiary") {
				drawWay ("gray", 2, nodes2Coordinates(@wayNodes)) ;
				if ( ($ref ne "") and ($length > $labelMinLength) ) { labelWay ("gray", 2, "", $ref, -3, nodes2Coordinates(@wayNodes)) ; }
			}
	#		if ($key eq "highway:track") {
	#			drawWay ("lightgray", 1, nodes2Coordinates(@wayNodes)) ;
	#		}
		}
	}	
	
	($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;

# draw place names

print "draw places and distances...\n" ;

foreach $key (keys %placeName) {
	# drawNodeDot ($lon{$key}, $lat{$key}, "black", 4) ;
	drawTextPos ($lon{$key}, $lat{$key}, 0, 0, $placeName{$key}, "black", 2) ;
}

# draw node dist information 
foreach my $node (keys %wayCount) {
	if ($wayCount{$node} > 1) {
		my $d = int ($dist{$node} * 1000) / 1000 ;
		drawTextPos ($lon{$node}, $lat{$node}, 0, 0, $d, "blue", 1) ;
	}
}

# draw other information

print "draw other information...\n" ;

drawTextPos ($lon{$root}, $lat{$root}, 0, 0, "DEST", "red", 4) ;

drawLegend (2, "Farm", "lightgreen", "Forest", "lightbrown", "Residential", "lightgray", "Water", "lightblue", "Primary", "red", "Motorway", "blue", "Legend", "black") ;
drawRuler ("darkgray") ;
drawHead ("gary68's distancemap", "black", 2) ;
drawFoot ("data by www.openstreetmap.org", "gray", 2) ;


writeGraph ($pngName) ;

my $svgName = $pngName ; $svgName =~ s/.png/.svg/ ;
writeSVG ($svgName) ;

# write gpx file and osm snippet
my $gpxName = $pngName ; $gpxName =~ s/.png/.gpx/ ;
my $gpxFile ;
open ($gpxFile, ">", $gpxName) or die ("can't open gpx output file") ;
printGPXHeader ($gpxFile) ;
foreach my $node (keys %wayCount) {
	if ($wayCount{$node} > 1) {
		printGPXWaypoint ($gpxFile, $lon{$node}, $lat{$node}, $dist{$node}) 
	}
}
printGPXFoot ($gpxFile) ;
close ($gpxFile) ;

my $osm2Name = $pngName ; $osm2Name =~ s/.png/.osm/ ;
my $osm2File ;
open ($osm2File, ">", $osm2Name) or die ("can't open osm output file") ;
foreach my $node (keys %wayCount) {
	if ($wayCount{$node} > 1) {
		my $id = 1000000000 + $node ;
		print $osm2File "  <node id=\"", $id, "\" timestamp=\"2009-07-14T00:00:00+00:00\" user=\"distancemap\"" ;
		print $osm2File " lat=\"", $lat{$node}, "\"" ;
		print $osm2File " lon=\"", $lon{$node}, "\">\n" ;
		print $osm2File "    <tag k=\"type\" v=\"distance\"/>\n" ;
		print $osm2File "    <tag k=\"distance\" v=\"", $dist{$node}, "\"/>\n" ;
		print $osm2File "  </node>\n" ;
	}
}
close ($osm2File) ;


$time1 = time() ;
print "\n$programName finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;


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



sub byDistance {
   $dist{$a} eq $infinity ? +1 :
   $dist{$b} eq $infinity ? -1 :
       $dist{$a} <=> $dist{$b};
}
