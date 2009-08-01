use strict ;
use warnings ;

use OSM::osm ;
use OSM::osmgraph 2.0 ;

my $minLength = 0.05 ;
my $maxDistStub = 0.05 ;
my $colorStub = "brown" ;
my $colorFixme = "orange" ;
my $colorName = "black" ;
my $colorBug = "red" ;
my $colorRoute = "pink" ;

my $programName = "todomap.pl" ;
my $usage = "todomap.pl file.osm bugs.gpx route.gpx out.png size" ; # svg name is automatic
my $version = "1.0 (BETA 003)" ;

my $labelMinLength = 0.1 ; # min length of street so that it will be labeled / needs adjustment according to picture size

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
my $gpxName ; 
my $routeName ; 
my $pngName ;

my $gpxFile ;
my $routeFile ;

my %lon ; my %lat ;
my %placeName ;
my %ways ; # per node

my $size ;
my $lonMin ; my $latMin ; my $lonMax ; my $latMax ;

my $time0 ; my $time1 ;

# get parameter

$osmName = shift||'';
if (!$osmName)
{
	die (print $usage, "\n");
}

$gpxName = shift||'';
if (!$gpxName)
{
	die (print $usage, "\n");
}

$routeName = shift||'';
if (!$routeName)
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
	$size = 2048 ; # default size
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

#$lonMax += ($lonMax - $lonMin) * 20/100 ;

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
		
		# TODO DRAW HOUSES / BUILDINGS
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
			if (grep /^highway:/, $key) { 
				foreach my $n (@wayNodes) {
					if (defined $ways{$n}) {
						$ways{$n}++ ;
					}
					else {
						$ways{$n} = 1 ;
					}
				}
			}
			if ( ($key eq "highway:residential") or ($key eq "highway:pedestrian") ) {
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
			if ( ($key eq "highway:track") or ($key eq "highway:footway") or ($key eq "highway:steps") or ($key eq "highway:cycleway") ) {
				drawWay ("lightgray", 1, nodes2Coordinates(@wayNodes)) ;
			}

			# DRAW HOUSE NUMBER WAYS
			if (grep /addr:interpolation/, $key) {
				drawWay ("orange", 2, nodes2Coordinates(@wayNodes)) ;
			}
	
		}
	}	
	
	($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;


# DRAW PLACE NAMES

print "draw places...\n" ;

foreach $key (keys %placeName) {
	# drawNodeDot ($lon{$key}, $lat{$key}, "black", 4) ;
	drawTextPos ($lon{$key}, $lat{$key}, 0, 0, $placeName{$key}, "black", 2) ;
}










openOsmFile ($osmName) ;
($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
if ($nodeId != -1) {
	@nodeTags = @$aRef1 ;
}

while ($nodeId != -1) {

	my $building = 0 ;
	my $number = 0 ;
	my $fixme = 0 ;
	foreach my $tag (@nodeTags) {
		if ($tag->[0] eq "building") { $building = 1 ; }
		if ($tag->[0] eq "addr:housenumber") { $number = $tag->[1] ; }
		if ( grep /fixme/, $tag->[0]) { $fixme = 1 ; }
		if ( grep /fixme/, $tag->[1]) { $fixme = 1 ; }
		if ( grep /todo/, $tag->[0]) { $fixme = 1 ; }
		if ( grep /todo/, $tag->[1]) { $fixme = 1 ; }
	}

	# DRAW BUILDINGS FROM NODES
	if ($building) { drawNodeDot ($nodeLon, $nodeLat, "darkgray", 3) ; }
	if ($building and $number) { drawTextPos ($nodeLon, $nodeLat, 0, 0, $number, "black", 2) ; }

	if ($fixme) { 
		#print "fixme node found\n" ;
		drawNodeDot ($nodeLon, $nodeLat, $colorFixme, 5) ; 
		drawTextPos ($nodeLon, $nodeLat, 0, 0, "FIXME", $colorFixme, 2) ; 
	}

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
	my $name = "" ; my $ref = "" ; my $tracktype = "" ;
	my $residential = 0 ; my $linking = 0 ; my $track = 0 ; my $building = 0 ; my $number = 0 ; my $highway = 0 ;
	my $fixme = 0 ;

	if (scalar @wayNodes > 1) {
		foreach my $tag (@wayTags) {
			if ($tag->[0] eq "highway") { $highway = 1 ; }
			if ( ($tag->[0] eq "highway") and ($tag->[1] eq "residential") ) { $residential = 1 ; }
			if ( ($tag->[0] eq "highway") and ($tag->[1] eq "primary") ) { $linking = 1 ; }
			if ( ($tag->[0] eq "highway") and ($tag->[1] eq "secondary") ) { $linking = 1 ; }
			if ( ($tag->[0] eq "highway") and ($tag->[1] eq "tertiary") ) { $linking = 1 ; }
			if ( ($tag->[0] eq "highway") and ($tag->[1] eq "track") ) { $track = 1 ; }
			if ($tag->[0] eq "tracktype") { $tracktype = $tag->[1] ; }
			if ($tag->[0] eq "name") { $name = $tag->[1] ; }
			if ($tag->[0] eq "ref") { $ref = $tag->[1] ; }
			if ( grep /fixme/, $tag->[0]) { $fixme = 1 ; }
			if ( grep /fixme/, $tag->[1]) { $fixme = 1 ; }
			if ( grep /todo/, $tag->[0]) { $fixme = 1 ; }
			if ( grep /todo/, $tag->[1]) { $fixme = 1 ; }
			if ($tag->[0] eq "building") { $building = 1 ; }
			if ($tag->[0] eq "addr:housenumber") { $number = $tag->[1] ; }
		}
	
	}

	if ($building) { drawWay ("gray", 1, nodes2Coordinates(@wayNodes)) ; }
	if ($building and ($number != 0) ) { drawTextPos ($lon{$wayNodes[0]}, $lat{$wayNodes[0]}, 0, 0, $number, "black", 1) ; }

	if ($fixme) { 
		drawWay ($colorFixme, 3, nodes2Coordinates(@wayNodes)) ; 
#		print "fixme way found\n" ;
	}

	if ( ( ($residential) and ($name eq "") ) or
		( ($linking) and ($ref eq "") ) or
		( ($track) and ($tracktype eq "") ) ) { 
			drawWay ($colorName, 3, nodes2Coordinates(@wayNodes)) ; 
			my $index = int ($#wayNodes / 2) ;
			my $code = substr ($wayId, -4) ;
			drawTextPos ($lon{$wayNodes[$index]}, $lat{$wayNodes[$index]}, 0, 0, $code, $colorName, 2) ; 
	}


#	if ( ($linking) and ($ref eq "") ) { drawWay ($colorName, 3, nodes2Coordinates(@wayNodes)) ; }
#	if ( ($track) and ($tracktype eq "") ) { drawWay ($colorName, 3, nodes2Coordinates(@wayNodes)) ; }
		


	# STUBS, 3 DOTS

	if ($highway) {
		my $stub = 0 ;
		if (scalar @wayNodes > 3) {
			my ($dist1) = distance ($lon{$wayNodes[0]}, $lat{$wayNodes[0]}, $lon{$wayNodes[1]}, $lat{$wayNodes[1]}) + 
					distance ($lon{$wayNodes[1]}, $lat{$wayNodes[1]}, $lon{$wayNodes[2]}, $lat{$wayNodes[2]}) ;
			if ( ($dist1 < $maxDistStub) and ($ways{$wayNodes[0]} == 1) ) { $stub = 1 ; }
			$dist1 = distance ($lon{$wayNodes[-1]}, $lat{$wayNodes[-1]}, $lon{$wayNodes[-2]}, $lat{$wayNodes[-2]}) + 
					distance ($lon{$wayNodes[-2]}, $lat{$wayNodes[-2]}, $lon{$wayNodes[-3]}, $lat{$wayNodes[-3]}) ;
			if ( ($dist1 < $maxDistStub) and ($ways{$wayNodes[-1]} == 1) ) { $stub = 1 ; }

		}
		my $length = 0 ;
		my $i ;
		for ($i = 0; $i<$#wayNodes; $i++) {
			$length += distance ($lon{$wayNodes[$i]}, $lat{$wayNodes[$i]}, $lon{$wayNodes[$i+1]}, $lat{$wayNodes[$i+1]}) ;
		}
		if  ( ($length < $minLength)  and ( ($ways{$wayNodes[0]} == 1)  or ($ways{$wayNodes[-1]} == 1) ) ) { $stub = 1 ; }

		if ($stub == 1) {
			drawWay ($colorStub, 3, nodes2Coordinates(@wayNodes)) ;
			drawNodeDot ($lon{$wayNodes[0]}, $lat{$wayNodes[0]}, $colorStub, 3) ;
			drawNodeDot ($lon{$wayNodes[-1]}, $lat{$wayNodes[-1]}, $colorStub, 3) ;
		}
	}	
	
	($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;




# PROCESS GPX FILE

my $success ;
$success = open ($gpxFile, "<", $gpxName) ;
my $line ;
if ($success) {
	print "\nprocessing file: $gpxName\n" ;
	while ($line = <$gpxFile>) {
		if (grep /<wpt/, $line) {
			# if inside box draw bug!
			my ($lon) = ($line =~ /^\.*\<wpt lon=[\'\"]([-\d,\.]+)[\'\"]/) ;
			my ($lat) = ($line =~ /^.*\lat=[\'\"]([-\d,\.]+)[\'\"]/) ;
			my ($desc) = ($line =~ /<desc>(.+)<\/desc>/) ;
	#		print $lon, "\n" ;
	#		print $lat, "\n" ;
	#		print $desc, "\n" ;
			if ( (defined $lon) and (defined $lat) and (defined $desc) ) {
				if ( ($lon > $lonMin) and ($lon < $lonMax) and ($lat > $latMin) and ($lat < $latMax) ) {
					$desc =~ s/<!\[CDATA\[// ;
					$desc =~ s/\]\]>// ;
					drawNodeDot ($lon, $lat, $colorBug, 5) ;
					drawTextPos ($lon, $lat, 0, 0, $desc, "black", 2) 
				}
			}
		}
	}
	close ($gpxFile) ;
}
else {
	print "\nNOT processing file: $gpxName\n" ;
}

# PROCESS ROUTE FILE


$success = open ($routeFile, "<", $routeName) ;
my $first = 1 ;
my $lastLon ;
my $lastLat ;
if ($success) {
	print "\nprocessing file: $routeName\n" ;
	while ($line = <$routeFile>) {
		if (grep /<rtept/, $line) {
	#		print $line, "\n" ;
			# if inside box draw bug!
			my ($lon) = ($line =~ /^.*\lon=[\'\"]([-\d,\.]+)[\'\"]/) ;
			my ($lat) = ($line =~ /^.*\lat=[\'\"]([-\d,\.]+)[\'\"]/) ;
	#		print $lon, "\n" ;
	#		print $lat, "\n" ;
	#		print $desc, "\n" ;
			if ( (defined $lon) and (defined $lat) ) {
				if ( ($lon > $lonMin) and ($lon < $lonMax) and ($lat > $latMin) and ($lat < $latMax) ) {
					if ($first) {
						$first = 0 ;
						$lastLon = $lon ;
						$lastLat = $lat ;
					}
					else {
						drawWay ($colorRoute, 2, $lastLon, $lastLat, $lon, $lat) ; 
	#					print "$lastLon, $lastLat, $lon, $lat\n" ; 
						$lastLon = $lon ;
						$lastLat = $lat ;
					}
				}
			}
		}
	}
	close ($routeFile) ;
}
else {
	print "\nNOT processing file: $routeName\n" ;
}


# DRAW OTHER INFORMATION

print "draw other information...\n" ;

drawLegend (3, "Route", $colorRoute, "Stub", $colorStub, "Fixme etc.", $colorFixme, "No name, ref, type", $colorName, "Bug", $colorBug) ;
drawRuler ("darkgray") ;
drawHead ("gary68's todo map", "black", 2) ;
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

