


# TODO
# distance calc for ways
# area labels in the middle
# read style from file
# LAYERS, bridges and tunnels (collect objects in separate hashes...)
# enhanced legend

# waybegrenzungen, farbe, dicke
# relations (scan, then convert to ways, preserve layers!)
# bg color

use strict ;
use warnings ;

use OSM::osm ;
use OSM::osmgraph 2.3 ;

my $programName = "mapgen.pl" ;
my $usage = "mapgen.pl file.osm out.png size" ; # svg name is automatic
my $version = "0.1" ;

my @legend = (	"Farm", 	"lightgreen",
	 	"Forest", 	"lightbrown", 
		"Water", 	"lightblue", 
		"Primary(way)/road block(node)", "red", 
		"Motorway", 	"blue", 
		"Secondary", 	"orange", 
		"Tertiary", 	"green", 
		"Residential", 	"darkgray", 
		"Track", 	"darkgray", 
		"camp site", 	"green", 
		"hospital", 	"pink", 
		"Collapsed", 	"black") ;
# AREAS
my $areaIndexKey = 0 ;
my $areaIndexValue = 1 ;
my $areaIndexColor = 2 ;
my @areas = () ;
# tag value color
push @areas, [qw (waterway riverbank lightblue)] ;
push @areas, [qw (natural water lightblue)] ;
push @areas, [qw (landuse forest lightbrown)] ;
push @areas, [qw (landuse farm lightgreen)] ;
push @areas, [qw (landuse village_green lightgreen)] ;
push @areas, [qw (landuse residential lightgray)] ;
push @areas, [qw (aeroway runway gray)] ;
push @areas, [qw (aeroway taxiway gray)] ;

# NODES
my $nodeIndexTag = 0 ;
my $nodeIndexValue = 1 ;
my $nodeIndexColor = 2 ;
my $nodeIndexThickness = 3 ;
my $nodeIndexLabel = 4 ;
my $nodeIndexLabelColor = 5 ;
my $nodeIndexLabelSize = 6 ;
my $nodeIndexLabelOffset = 7 ;
my @nodes = () ;
# tag value color thickness label label-color label-size label-offset
push @nodes, [qw (building collapsed black 3 none black 0 0)] ;
push @nodes, [qw (earthquake:damage collapsed_building black 3 none black 0 0)] ;
push @nodes, [qw (highway obstacle red 8 none black 0 0)] ;
push @nodes, [qw (barrier obstacle red 8 none black 0 0)] ;
push @nodes, [qw (earthquake:damage spontaneous_camp green 6 none black 0 0)] ;
push @nodes, [qw (earthquake:damage spontaneous_campsite green 6 none black 0 0)] ;
push @nodes, [qw (earthquake:damage people_camping green 6 none black 0 0)] ;
push @nodes, [qw (earthquake:damage landslide darkgray 8 none black 0 0)] ;
push @nodes, [qw (tourism camp_site green 6 none black 0 0)] ;
push @nodes, [qw (amenity hospital pink 6 name black 1 0)] ;
push @nodes, [qw (hospital field pink 4 name black 1 0)] ;
push @nodes, [qw (place city black 0 name black 3 0)] ;
push @nodes, [qw (place city black 0 pcode:2 blue 3 10)] ;
push @nodes, [qw (place town black 0 name black 2 0)] ;
push @nodes, [qw (place town black 0 pcode:2 blue 2 10)] ;
push @nodes, [qw (place suburb black 0 name black 2 0)] ;
push @nodes, [qw (place suburb black 0 pcode:2 blue 2 10)] ;
push @nodes, [qw (place village black 0 name black 2 0)] ;
push @nodes, [qw (place village black 0 pcode:2 blue 2 10)] ;


# WAYS and small AREAS
my $wayIndexTag = 0 ;
my $wayIndexValue = 1 ;
my $wayIndexColor = 2 ;
my $wayIndexThickness = 3 ;
my $wayIndexFilled = 4 ;
my $wayIndexLabel = 5 ;
my $wayIndexLabelColor = 6 ;
my @ways = () ;
# key value color thickness label label-color
push @ways, [qw (highway residential darkgray 2 0 name darkgray)] ;
push @ways, [qw (highway unclassified darkgray 2 0 name darkgray)] ;
push @ways, [qw (highway service darkgray 2 0 name darkgray)] ;
push @ways, [qw (highway motorway blue 4 0 ref darkgray)] ;
push @ways, [qw (highway motorway_link blue 3 0 none darkgray)] ;
push @ways, [qw (highway trunk blue 3 0 ref darkgray)] ;
push @ways, [qw (highway trunk_link blue 2 0 none darkgray)] ;
push @ways, [qw (highway primary red 3 0 ref darkgray)] ;
push @ways, [qw (highway primary_link red 3 0 none darkgray)] ;
push @ways, [qw (highway secondary orange 3 0 ref darkgray)] ;
push @ways, [qw (highway secondary_link orange 2 0 none darkgray)] ;
push @ways, [qw (highway tertiary green 2 0 ref darkgray)] ;
push @ways, [qw (highway track darkgray 1 0 none darkgray)] ;

push @ways, [qw (waterway river lightblue 2 0 name darkgray)] ;
push @ways, [qw (waterway stream lightblue 1 0 name darkgray)] ;

push @ways, [qw (building yes gray 1 0 none darkgray)] ;
push @ways, [qw (building collapsed black 1 0 none darkgray)] ;

# WAY AREAS
push @ways, [qw (amenity hospital pink 3 1 name darkgray)] ;
push @ways, [qw (hospital field pink 2 1 name darkgray)] ;

push @ways, [qw (tourism camp_site green 2 1 name darkgray)] ;
push @ways, [qw (earthquake:damage landslide darkgray 2 1 name darkgray)] ;


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

my %memNodeTags ;
my %memWayTags ;
my %memWayNodes ;
my %memRelationTags ;
my %memRelationMembers ;

my $osmName ; 
my $pngName ;

my %lon ; my %lat ;

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
print "AREAS\n" ;
foreach my $area (@areas) {
	printf "%-15s %-15s %-10s\n", $area->[0], $area->[1], $area->[2] ;
}
print "\n" ;
print "WAYS\n" ;
foreach my $way (@ways) {
	printf "%-20s %-20s %-10s %-10s %-10s %-10s %-10s\n", $way->[0], $way->[1], $way->[2], $way->[3], $way->[4], $way->[5], $way->[6] ;
}
print "\n" ;
print "NODES\n" ;
foreach my $node (@nodes) {
	printf "%-20s %-20s %-10s %-10s %-10s %-10s %-10s %-10s\n", $node->[0], $node->[1], $node->[2], $node->[3], $node->[4], $node->[5], $node->[6], $node->[7] ;
}
print "\n" ;

$time0 = time() ;


# STORE DATA
print "reading osm file...\n" ;

openOsmFile ($osmName) ;
($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
if ($nodeId != -1) {
	@nodeTags = @$aRef1 ;
}
while ($nodeId != -1) {

	$lon{$nodeId} = $nodeLon ;	
	$lat{$nodeId} = $nodeLat ;	
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

	if (scalar (@wayNodes) > 1) {
		@{$memWayTags{$wayId}} = @wayTags ;
		@{$memWayNodes{$wayId}} = @wayNodes ;
	}
	
	($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;



# calc area of pic and init

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


# BG AREAS

print "draw areas...\n" ;
foreach my $wayId (keys %memWayTags) {
	foreach $key (@{$memWayTags{$wayId}}) {
		foreach my $test (@areas) {
			if ( ($key->[0] eq $test->[$areaIndexKey]) and ($key->[1] eq $test->[$areaIndexValue]) ) {
				drawArea ($test->[$areaIndexColor], nodes2Coordinates( @{$memWayNodes{$wayId}} ) ) ;
			}
		}
	}
}


# NODES

print "draw nodes...\n" ;
foreach my $nodeId (keys %memNodeTags) {
	foreach my $tag (@{$memNodeTags{$nodeId}} ) {
		foreach my $test (@nodes) {
			if ( ($tag->[0] eq $test->[$nodeIndexTag]) and ($tag->[1] eq $test->[$nodeIndexValue]) ) {
				if ($test->[$nodeIndexThickness] > 0) {
					drawNodeDot ($lon{$nodeId}, $lat{$nodeId}, $test->[$nodeIndexColor], $test->[$nodeIndexThickness]) ;
				}

				if ($test->[$nodeIndexLabel] ne "none") {
					my $name = "" ;
					# get name
					foreach my $tag2 (@{$memNodeTags{$nodeId}}) {
						if ($tag2->[0] eq $test->[$nodeIndexLabel]) {
							$name = $tag2->[1] ;
						}
					}
					if ($name ne "") {
						drawTextPos ($lon{$nodeId}, $lat{$nodeId}, 0, -$test->[$nodeIndexLabelOffset], 
							$name, $test->[$nodeIndexLabelColor], $test->[$nodeIndexLabelSize]) ;
					}
				} # draw label
			} # tag found
		} # test
	} # tags
} # nodes


# WAYS

print "draw ways...\n" ;
foreach my $wayId (keys %memWayTags) {
	#print "wayid: $wayId\n" ;
	my $text = "" ; 
	my $length = 0 ;

	my $i ;
#	for ($i = 0; $i < ((scalar @{$memWayNodes{$wayId}})-1); $i++) {
#		$length += distance ($lon{ ${$memWayNodes{$wayId}}[$i] }, $lat{ ${$memWayNodes{$wayId}}[$i] }, 
#			$lon{ ${$memWayNodes{$wayId}}[$i+1] }, $lat{ ${$memWayNodes{$wayId}}[$i+1] }) ;
#	}
	$length = 1 ;

	foreach my $tag (@{$memWayTags{$wayId}}) {
		#print "  $tag->[0] $tag->[1]\n" ;
		foreach my $test (@ways) {
			if ( ($tag->[0] eq $test->[$wayIndexTag]) and ($tag->[1] eq $test->[$wayIndexValue]) ) {
				#print "    tag match\n" ;
				if ($test->[$wayIndexFilled] eq "0") {
					#print "      drawing way $test->[$wayIndexColor], $test->[$wayIndexThickness] ...\n" ;
					drawWay ($test->[$wayIndexColor], $test->[$wayIndexThickness], nodes2Coordinates(@{$memWayNodes{$wayId}})) ;
					if ($test->[$wayIndexLabel] ne "none") {
						foreach my $tag2 (@{$memWayTags{$wayId}}) {
							if ( ($tag2->[0] eq $test->[$wayIndexLabel]) and ($length > $labelMinLength) ) {
								labelWay ($test->[$wayIndexLabelColor], 0, "", $tag2->[1], -2, nodes2Coordinates(@{$memWayNodes{$wayId}})) ;
							}
						}
					}
				} # not filled
				else {
					if (${$memWayNodes{$wayId}}[0] == ${$memWayNodes{$wayId}}[-1]) {
						drawArea ($test->[$wayIndexColor], nodes2Coordinates( @{$memWayNodes{$wayId}} ) ) ;
						if ($test->[$wayIndexLabel] ne "none") {
							foreach my $tag2 (@{$memWayTags{$wayId}}) {
								if ($tag2->[0] eq $test->[$wayIndexLabel]) {
									# TODO calc middle of area
									drawTextPos ($lon{${$memWayNodes{$wayId}}[0]}, $lat{${$memWayNodes{$wayId}}[0]}, 0, 0, 
										$tag2->[1], $test->[$wayIndexLabelColor], 2) ;
								}
							}
						} # draw label
					} #closed
				} # filled
			} # tag found
		} # $test
	} # $tag
} # ways



# draw other information

print "draw legend etc. and write files...\n" ;

drawLegend (2, @legend) ;
drawRuler ("darkgray") ;
drawHead ("gary68's $programName $version", "black", 2) ;
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

