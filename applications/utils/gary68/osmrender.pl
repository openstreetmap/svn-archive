use strict ;
use warnings ;

use OSM::osm ;
use OSM::osmgraph 2.0 ;

my $programName = "osmrender.pl" ;
my $usage = "osmrender.pl file.osm out.png size" ; # svg name is automatic
my $version = "2.0" ;

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
	$size = 1024 ; # default size
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

print "draw areas...\n" ;

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
	
	($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;

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

print "draw places...\n" ;

foreach $key (keys %placeName) {
	# drawNodeDot ($lon{$key}, $lat{$key}, "black", 4) ;
	drawTextPos ($lon{$key}, $lat{$key}, 0, 0, $placeName{$key}, "black", 2) ;
}

# draw other information

print "draw other information...\n" ;

drawLegend (2, "Farm", "lightgreen", "Forest", "lightbrown", "Residential", "lightgray", "Water", "lightblue", "Primary", "red", "Motorway", "blue", "Legend", "black") ;
drawRuler ("darkgray") ;
drawHead ("gary68's osmrender", "black", 2) ;
drawFoot ("data by www.openstreetmap.org", "gray", 2) ;


writeGraph ($pngName) ;

my $svgName = $pngName ; $svgName =~ s/.png/.svg/ ;
writeSVG ($svgName) ;

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

