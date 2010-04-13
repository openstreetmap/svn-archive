#! /usr/bin/perl
#
# mapgen.pl
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

# 0.09 _lon and _lat for labes
#      [-coords] coordinates grid 
#      display routes
#      automatic label fit for labels on roads; [-ppc] replaces [-minlen]
#      stops for routes
# 0.10 icons for routes (routeicons dir)
#      [-routeicondist]
#      [-poi]
# 0.11 from and to scale added to style file and read routines
#      getXYRule incl. scale      
#      scale range for rules
#      area tile patterns implemented
#      error checking for osm file implemented
# 0.12 show only elements in legend that are shown in current scale
#      larger legend symbols
#      support for asterisk (wild card) in way and node rules (enter * for value)
#      more intelligent icon placement, 9 different positions around actual position
#      ppc changed to 5.7?
#      draw text pos new
# 0.13 tagstat changed to consider sub k/v
#      remark (COMMENT) lines for style file
#      [-rulescaleset]
#      way borders 
#      tagstat separated for nodes and ways
# 0.14 [-pad]
#      ocean rendering
#      [-allowiconmove]
# 0.15 -place also accepts node id
#      intelligent street labeling
#      faster size determination of svg files
#      right size and resolution for output files
# 0.16 [-halo]
#      placement of labels of ways improved
#      quad trees implemented for speed
# 0.17 comments
#      legend possible in lower right corner
#      steps with linecap=butt
#      subs for png and svg sizes
# 1.00 ---
# 1.01 [-clipbox] implemented
#
# TODO
# ------------------
# [ ] reading rule check for right number of keys and values ! else ERROR
# grid distance in meters
# -viewpng -viewpdf -viewsvg
# STDERR outputs
# style file error handling
# style file check, color check, error messages, array for regex? Defaults
# nested relations for multipolygons?
# see wiki
# maybe prevent double labels in vicinity of each other?

use strict ;
use warnings ;

use Math::Polygon ;
use Getopt::Long ;
use OSM::osm ;
use OSM::mapgen 1.01 ;
use OSM::mapgenRules 1.01 ;

my $programName = "mapgen.pl" ;
my $version = "1.01" ;

my $usage = <<"END23" ;
perl mapgen.pl 
-help
-in=file.osm
-style=style.csv (original can be kept and maintained in OO sheet or MS Excel)
-out=file.svg (png and pdf names are automatic, DEFAULT=mapgen.svg)

-bgcolor=TEXT (color for background)
-size=<integer> (in pixels for x axis, DEFAULT=1024)
-clip=<integer> (percent data to be clipped on each side, 0=no clipping, DEFAULT=0)
-clipbbox=<float>,<float>,<float>,<float> (left, right, bootom, top of bbox for clipping map out of data - more precise than -clip)
-pad=<INTEGER> (percent of white space around data in osm file, DEFAULT=0)

-place=TEXT (Place to draw automatically; quotation marks can be used if necessary; node id can also be given; OSMOSIS REQUIRED!)
-lonrad=FLOAT (radius for place width in km, DEFAULT=2)
-latrad=FLOAT (radius for place width in km, DEFAULT=2)

-declutter (declutter text; WARNING: some labels might be omitted; motorway and trunk will only be labeled in one direction)
-allowiconmove (allows icons to be moved if they don't fit the exact position)

-oneways (add oneway arrows)
-onewaycolor=TEXT (color for oneway arrows)
-halo=<FLOAT> (white halo width for point feature labels; DEFAULT=0)

-grid=<integer> (number parts for grid, 0=no grid, DEFAULT=0)
-gridcolor=TEXT (color for grid lines and labels (DEFAULT=black)
-coords (turn on coordinates grid)
-coordsexp=INTEGER (degrees to the power of ten for grid distance; DEFAULT=-2 equals 0.01 degrees)
-coordscolor=TEXT (set color of coordinates grid)
-dir (create street directory in separate file. if grid is enabled, grid squares will be added)
-poi (create list of pois)
-tagstat (lists keys and values used in osm file; program filters list to keep them short!!! see code array noListTags)

-routelabelcolor=TEXT (color for labels of routes)
-routelabelsize=INTEGER (DEFAULT=8)
-routelabelfont=TEXT (DEFAULT=sans-serif)
-routelabeloffset=INTEGER (DEFAULT=10)
-icondir=TEXT (dir for icons for routes; ./icondir/ i.e.; DEFAULT=./routeicons/ )
-routeicondist=INTEGER (dist in y direction for route icons on same route; DEFAULT=25)

-legend=INT (0=no legend; 1=legend in top left corner; 2 = legend in lower right corner; DEFAULT=1)-ruler=INT (0=no ruler; 1=draw ruler; DEFAULT=1)
-rulercolor=TEXT (DEFAULT=black)
-scale (print scale)
-scalecolor=TEXT (set scale color; DEFAULT = black)
-scaleset=INTEGER (1:x preset for map scale; overrides -size=INTEGER! set correct printer options!)
-scaledpi=INTEGER (print resolution; DEFAULT = 300 dpi)
-rulescaleset=INTEGER (determines the scale used to select rules; DEFAULT=0, meaning actual map scale is used to select rules)

-ppc=<float> (pixels needed per character using font size 10; DEFAULT=5.5)

-png (also produce png, inkscape must be installed, very big)
-pdf (also produce pdf, inkscape must be installed)

-verbose
-multionly (draws only areas of multipolygons; for test purposes)
END23

# command line things
my $optResult ;
my $verbose = 0 ;
my $multiOnly = 0 ;
my $grid = 0 ;
my $gridColor = "black" ;
my $clip = 0 ;
my $clipbbox = "" ;
my $pad = 0 ;
my $legendOpt = 1 ;
my $size = 1024 ; 		# default pic size longitude in pixels
my $bgColor = "white" ;
my $osmName = "" ; 
my $csvName = "" ; 
my $dirName = "" ; 
my $svgName = "mapgen.svg" ; 
my $pdfOpt = 0 ;
my $pngOpt = 0 ;
my $dirOpt = 0 ;
my $poiOpt = 0 ;
my $ppc = 6 ; 
my $place = "" ;
my $lonrad = 2 ;
my $latrad = 2 ;
my $helpOpt = 0 ;
my $tagStatOpt = 0 ;
my $declutterOpt = 0 ;
my $allowIconMoveOpt = 0 ;
my $rulerOpt = 1 ;
my $rulerColor = "black" ;
my $scaleOpt = 0 ;
my $scaleDpi = 300 ;
my $scaleColor = "black" ;
my $scaleSet = 0 ;
my $ruleScaleSet = 0 ;
my $coordsOpt = 0 ;
my $coordsExp = -2 ;
my $coordsColor = "black" ;
my $routeLabelColor = "black" ;
my $routeLabelSize = 8 ;
my $routeLabelFont = "sans-serif" ;
my $routeLabelOffset = 10 ;
my $iconDir = "./routeicons/" ;
my $routeIconDist = 25 ;
my $onewayOpt = 0 ;
my $onewayColor = "white" ;
my $halo = 0 ;

# keys from tags listed here will not be shown in tag stat
my @noListTags = sort qw (name width url source ref note phone operator opening_hours maxspeed maxheight maxweight layer is_in TODO addr:city addr:housenumber addr:country addr:housename addr:interpolation addr:postcode addr:street created_by description ele fixme FIXME website bridge tunnel time openGeoDB:auto_update  openGeoDB:community_identification_number openGeoDB:is_in openGeoDB:is_in_loc_id openGeoDB:layer openGeoDB:license_plate_code openGeoDB:loc_id openGeoDB:location openGeoDB:name openGeoDB:population openGeoDB:postal_codes openGeoDB:sort_name openGeoDB:telephone_area_code openGeoDB:type openGeoDB:version opengeodb:lat opengeodb:lon int_ref population postal_code wikipedia) ;

# NODES; column indexes for style file
my $nodeIndexTag = 0 ;
my $nodeIndexValue = 1 ;
my $nodeIndexColor = 2 ;
my $nodeIndexThickness = 3 ;
my $nodeIndexLabel = 4 ;
my $nodeIndexLabelColor = 5 ;
my $nodeIndexLabelSize = 6 ;
my $nodeIndexLabelFont = 7 ;
my $nodeIndexLabelOffset = 8 ;
my $nodeIndexLegend = 9 ;
my $nodeIndexIcon = 10 ;
my $nodeIndexIconSize = 11 ;
my $nodeIndexFromScale = 12 ;
my $nodeIndexToScale = 13 ;
my @nodes = () ;


# WAYS and small AREAS, as well as base layer info when flagged; column indexes for style file
my $wayIndexTag = 0 ;
my $wayIndexValue = 1 ;
my $wayIndexColor = 2 ;
my $wayIndexThickness = 3 ;
my $wayIndexDash = 4 ;
my $wayIndexBorderColor =  5 ;
my $wayIndexBorderThickness = 6 ;
my $wayIndexFilled = 7 ;
my $wayIndexLabel = 8 ;
my $wayIndexLabelColor = 9 ;
my $wayIndexLabelSize = 10 ;
my $wayIndexLabelFont = 11 ;
my $wayIndexLabelOffset = 12 ;
my $wayIndexLegend = 13 ;
my $wayIndexBaseLayer = 14 ;
my $wayIndexIcon = 15 ;
my $wayIndexFromScale = 16 ;
my $wayIndexToScale = 17 ;
my @ways = () ;

my $routeIndexRoute = 0 ;
my $routeIndexColor = 1 ; # colorSet!!! default if route doesn't have own color
my $routeIndexThickness = 2 ;
my $routeIndexDash = 3 ;
my $routeIndexOpacity = 4 ; # stroke opacity, values 0-100; 100 = fully blocking; 0 = transparent
my $routeIndexLabel = 5 ;
my $routeIndexStopThickness = 6 ;
my $routeIndexFromScale = 7 ;
my $routeIndexToScale = 8 ;
my @routes = () ;

# read data from file
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
my $relationId ;
my $relationUser ;
my @relationTags ;
my @relationMembers ;


# storage of data
my %memNodeTags ;
my %memWayTags ;
my %memWayNodes ;
my %invalidWays ;
my %memRelationTags ;
my %memRelationMembers ;
my %memWayPaths = () ;

# my %usedTags = () ; # for stats
my %wayUsed = () ; # used in multipolygon? then dont use again 
my %directory = () ; # street list
my %poiHash = () ;
my %wayLabels = () ;
my @labelCandidates = () ;

my %lon ; my %lat ;

my $lonMin ; my $latMin ; my $lonMax ; my $latMax ;

my $newId = -100000000; # global ! for multipolygon data (ways)

my $time0 ; my $time1 ;

# get parameter

$optResult = GetOptions ( 	"in=s" 		=> \$osmName,		# the in file, mandatory
				"style=s" 	=> \$csvName,		# the style file, mandatory
				"out:s"		=> \$svgName,		# outfile name or default
				"size:i"	=> \$size,		# specifies pic size longitude in pixels
				"legend:i"	=> \$legendOpt,		# legend?
				"bgcolor:s"	=> \$bgColor,		# background color
				"grid:i"	=> \$grid,		# specifies grid, number of parts
				"gridcolor:s"	=> \$gridColor,		# color used for grid and labels
				"coords"	=> \$coordsOpt,		# 
				"coordsexp:i"	=> \$coordsExp,		# 
				"coordscolor:s"	=> \$coordsColor,		# 
				"clip:i"	=> \$clip,		# specifies how many percent data to clip on each side
				"clipbbox:s"	=> \$clipbbox,		# bbox data for clipping map out of data
				"pad:i"		=> \$pad,		# specifies how many percent data to pad on each side
				"ppc:f"		=> \$ppc,		# pixels needed per label char in font size 10
				"pdf"		=> \$pdfOpt,		# specifies if pdf will be created
				"png"		=> \$pngOpt,		# specifies if png will be created
				"dir"		=> \$dirOpt,		# specifies if directory of streets will be created
				"poi"		=> \$poiOpt,		# specifies if directory of pois will be created
				"tagstat"	=> \$tagStatOpt,	# lists k/v used in osm file
				"declutter"	=> \$declutterOpt,
				"allowiconmove"	=> \$allowIconMoveOpt,
				"help"		=> \$helpOpt,		# 
				"oneways"	=> \$onewayOpt,
				"onewaycolor:s" => \$onewayColor,
				"place:s"	=> \$place,		# place to draw
				"lonrad:f"	=> \$lonrad,
				"latrad:f"	=> \$latrad,
				"halo:f"	=> \$halo,
				"ruler:i"	=> \$rulerOpt,
				"rulercolor:s"	=> \$rulerColor,
				"scale"		=> \$scaleOpt,
				"scaledpi:i"	=> \$scaleDpi,
				"scalecolor:s"	=> \$scaleColor,
				"scaleset:i"	=> \$scaleSet,
				"rulescaleset:i" => \$ruleScaleSet,
				"routelabelcolor:s"	=> \$routeLabelColor,		
				"routelabelsize:i"	=> \$routeLabelSize,		
				"routelabelfont:s"	=> \$routeLabelFont,		
				"routelabeloffset:i"	=> \$routeLabelOffset,		
				"routeicondist:i"	=> \$routeIconDist,
				"icondir:s"		=> \$iconDir,
				"multionly"	=> \$multiOnly,		# draw only areas from multipolygons
				"verbose" 	=> \$verbose) ;		# turns twitter on


if ($helpOpt eq "1") {
	print "\nINFO on http://wiki.openstreetmap.org/wiki/Mapgen.pl\n\n" ;
	print $usage . "\n" ;
	die() ;
}

if ($grid > 26) { 
	# max 26 because then letters are running out
	$grid = 26 ; 
	print "WARNING: grid set to 26 parts\n" ;
}
if ($grid < 0) { 
	# invalid number
	$grid = 0 ; 
	print "WARNING: grid set to 0\n" ;
}
if ( ($clip <0) or ($clip > 100) ) { 
	# validate clip factor
	$clip = 0 ; 
	print "WARNING: clip set to 0 percent\n" ;
}

print "\n$programName $version\n" ;
print "\n" ;
print "infile    = $osmName\n" ;
print "style     = $csvName\n" ;
print "outfile   = $svgName\n" ;
print "size      = $size (pixels)\n\n" ;

print "legend    = $legendOpt\n" ;
print "ruler     = $rulerOpt\n" ;
print "scaleOpt  = $scaleOpt\n" ;
print "scaleCol  = $scaleColor\n" ;
print "scaleDpi  = $scaleDpi\n" ;
print "scaleSet  = $scaleSet\n" ;
print "ruleScaleSet  = $ruleScaleSet\n\n" ;

print "clip        = $clip (percent)\n" ;
print "clipbbox    = $clipbbox\n" ;
print "pad         = $pad (percent)\n" ;
print "grid        = $grid (number)\n" ;
print "gridcolor   = $gridColor\n" ;
print "coordsOpt   = $coordsOpt\n" ;
print "coordsExp   = $coordsExp\n" ;
print "coordsColor = $coordsColor\n\n" ;

print "dir       = $dirOpt " ;
print "poiOpt    = $poiOpt\n" ;
print "ppc       = $ppc (pixels needed per character font size 10)\n" ;
print "declutter = $declutterOpt\n" ;
print "alloIconMoveOpt = $allowIconMoveOpt\n" ;

print "place     = $place " ;
print "lonrad    = $lonrad (km) " ;
print "latrad    = $latrad (km)\n\n" ;

print "routeLabelColor  = $routeLabelColor \n" ; 
print "routeLabelSize   = $routeLabelSize \n" ; 
print "routeLabelFont   = $routeLabelFont \n" ; 
print "routeLabelOffset = $routeLabelOffset\n" ; 
print "iconDir          = $iconDir\n" ; 
print "routeIconDist    = $routeIconDist\n\n" ; 

print "pdf       = $pdfOpt " ;
print "png       = $pngOpt\n\n" ;

print "multionly = $multiOnly " ;
print "verbose   = $verbose\n\n" ;

$time0 = time() ;

my ($ref1, $ref2, $ref3) = readRules ($csvName) ;
@nodes = @$ref1 ;
@ways = @$ref2 ;
@routes = @$ref3 ;

if ($verbose eq "1") {
	printRules() ;
}

# -place given? look for place and call osmosis
my $placeFound = 0 ; my $placeLon ; my $placeLat ;
if ($place ne "") {
	my ($placeId) = ($place =~ /([\d]+)/);
	if (!defined $placeId) { $placeId = -999999999 ; }
	print "looking for place...\n" ;
	openOsmFile ($osmName) ;
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
	if ($nodeId != -1) {
		@nodeTags = @$aRef1 ;
	}
	while ( ($nodeId != -1) and ($placeFound == 0) ) {
		my $placeNode = 0 ; my $placeName = 0 ;
		foreach my $tag	(@nodeTags) {
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
		my $left = $placeLon - $lonrad/(111.11 * cos ( $placeLat / 360 * 3.14 * 2 ) ) ;  
		my $right = $placeLon + $lonrad/(111.11 * cos ( $placeLat / 360 * 3.14 * 2 ) ) ; 
		my $top = $placeLat + $latrad/111.11 ; 
		my $bottom = $placeLat - $latrad/111.11 ;

		print "OSMOSIS STRING: --bounding-box-0.6 clipIncompleteEntities=true bottom=$bottom top=$top left=$left right=$right --write-xml-0.6\n" ;
		print "call osmosis...\n" ;
		`osmosis --read-xml-0.6 $osmName  --bounding-box-0.6 clipIncompleteEntities=true bottom=$bottom top=$top left=$left right=$right --write-xml-0.6 ./temp.osm` ;
		print "osmosis done.\n" ;
		$osmName = "./temp.osm" ;
	}
	else {
		print "ERROR: place $place not found.\n" ;
		die() ;
	}
}

# STORE DATA
print "reading osm file...\n" ;

openOsmFile ($osmName) ;
($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
if ($nodeId != -1) {
	@nodeTags = @$aRef1 ;
}
while ($nodeId != -1) {

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


# calc area of pic and init graphics
$lonMin = 999 ; $lonMax = -999 ; $latMin = 999 ; $latMax = -999 ;
foreach my $key (keys %lon) {
	if ($lon{$key} > $lonMax) { $lonMax = $lon{$key} ; }
	if ($lon{$key} < $lonMin) { $lonMin = $lon{$key} ; }
	if ($lat{$key} > $latMax) { $latMax = $lat{$key} ; }
	if ($lat{$key} < $latMin) { $latMin = $lat{$key} ; }
}

# clip picture if desired
if ($clipbbox ne "") {
	my ($bbLeft, $bbRight, $bbBottom, $bbTop) = ($clipbbox =~ /([\d\-\.]+),([\d\-\.]+),([\d\-\.]+),([\d\-\.]+)/ ) ;
	# print "$bbLeft, $bbRight, $bbBottom, $bbTop\n" ;
	if (($bbLeft > $lonMax) or ($bbLeft < $lonMin)) { die ("ERROR -clipbox left parameter outside data.") ; }
	if (($bbRight > $lonMax) or ($bbRight < $lonMin)) { die ("ERROR -clipbox right parameter outside data.") ; }
	if (($bbBottom > $latMax) or ($bbBottom < $latMin)) { die ("ERROR -clipbox bottom parameter outside data.") ; }
	if (($bbTop > $latMax) or ($bbTop < $latMin)) { die ("ERROR -clipbox top parameter outside data.") ; }
	$lonMin = $bbLeft ;
	$lonMax = $bbRight ;
	$latMin = $bbBottom ;
	$latMax = $bbTop ;
}
else {
	if ( ($clip > 0) and ($clip < 100) ) { 
		$clip = $clip / 100 ;
		$lonMin += ($lonMax-$lonMin) * $clip ;
		$lonMax -= ($lonMax-$lonMin) * $clip ;
		$latMin += ($latMax-$latMin) * $clip ;
		$latMax -= ($latMax-$latMin) * $clip ;
	}
}

# pad picture if desired
if ( ($pad > 0) and ($pad < 100) ) { 
	$pad = $pad / 100 ;
	$lonMin -= ($lonMax-$lonMin) * $pad ;
	$lonMax += ($lonMax-$lonMin) * $pad ;
	$latMin -= ($latMax-$latMin) * $pad ;
	$latMax += ($latMax-$latMin) * $pad ;
}

# calc pic size if scale is set
if ($scaleSet != 0) {
	my $dist = distance ($lonMin, $latMin, $lonMax, $latMin) ;
	print "INFO: distX (km) = $dist\n" ;
	my $width = $dist / $scaleSet * 1000 * 100 / 2.54 ; # inches
	print "INFO: width (in) = $width\n" ;
	$size = int ($width * $scaleDpi) ;
	print "INFO: sizeX set to $size pixels.\n" ;
	print "INFO: set print resolution to $scaleDpi dpi!\n\n" ;
}

initGraph ($size, $lonMin, $latMin, $lonMax, $latMax, $bgColor, $scaleDpi) ;
if ($onewayOpt eq "1") { initOneways ($onewayColor) ; }

my ($paper, $w, $h) = fitsPaper ($scaleDpi) ;
print "\nINFO: map fits paper $paper using $scaleDpi dpi.\n" ;
printf "INFO: map width : %4.1f (cm)\n", $w ;
printf "INFO: map height: %4.1f (cm)\n", $h ;
my $scaleValue = getScale ($scaleDpi) ;
print "INFO: map scale 1 : $scaleValue\n\n" ;

if ($ruleScaleSet == 0) { 
	$ruleScaleSet = $scaleValue ; 
}
else {
	print "INFO: using map rules for scale = $ruleScaleSet\n\n" ;
}

processCoastLines() ;

processRoutes () ;

processMultipolygons () ; # multipolygons, (routes)

# BG AREAS
print "draw background areas...\n" ;
foreach my $wayId (sort {$a <=> $b} keys %memWayTags) {
	if ($wayId>-100000000) {

		my ($test, $ruleNumber) = getWayRule (\@{$memWayTags{$wayId}}, \@ways, $ruleScaleSet) ;
		if (defined $test) {
			if ($test->[$wayIndexBaseLayer] != 1) { undef $test ; }
		}
		if (defined $test) {
			if ( ($memWayNodes{$wayId}[0] == $memWayNodes{$wayId}[-1]) and (!defined $wayUsed{$wayId}) )  {
				if ( $multiOnly == 0) {
					drawArea ($test->[$wayIndexColor], $test->[$wayIndexIcon], nodes2Coordinates( @{$memWayNodes{$wayId}} ) ) ;
					# LABELS
					my $name = "" ; my $ref1 ;
					($name, $ref1) = createLabel (\@{$memWayTags{$wayId}}, $test->[$wayIndexLabel], 0, 0) ;
					if ($name ne "") {
						my ($x, $y) = center (nodes2Coordinates(@{$memWayNodes{$wayId}})) ;
						placeLabelAndIcon ($x, $y, 0, 0, $name, $test->[$wayIndexLabelColor], $test->[$wayIndexLabelSize], $test->[$wayIndexLabelFont], $ppc, "none", 0, 0, $allowIconMoveOpt, $halo) ;
					}
				}
			}
		}
	}
}

print "draw multipolygons...\n" ;
foreach my $wayId (sort {$a <=>$b} keys %memWayTags) {
	if ($wayId <= -100000000) {
		my ($test, $ruleNumber) = getWayRule (\@{$memWayTags{$wayId}}, \@ways, $ruleScaleSet) ;
		if (defined $test) {
			drawAreaMP ($test->[$wayIndexColor], $test->[$wayIndexIcon], \@{$memWayPaths{$wayId}}, \%lon, \%lat  ) ;
			# LABELS
			my $name = "" ; my $ref1 ;
			($name, $ref1) = createLabel (\@{$memWayTags{$wayId}}, $test->[$wayIndexLabel], 0, 0) ;
			if ($name ne "") {
				my ($x, $y) = center (nodes2Coordinates(@{$memWayNodes{$wayId}})) ;
				placeLabelAndIcon ($x,$y, 0, 0, $name, $test->[$wayIndexLabelColor], $test->[$wayIndexLabelSize], $test->[$wayIndexLabelFont], $ppc, "none", 0, 0, $allowIconMoveOpt, $halo) ;
			}
		} #if
	} # if
} # foreach


if ($multiOnly eq "1") { 	# clear all data so nothing else will be drawn
	%memNodeTags = () ;
	%memWayTags = () ;
	%memWayNodes = () ;
}

# NODES
print "draw nodes...\n" ;
foreach my $nodeId (keys %memNodeTags) {
	my $test = getNodeRule (\@{$memNodeTags{$nodeId}}, \@nodes, $ruleScaleSet) ;
	if (defined $test) {
		$dirName = getValue ("name", \@{$memNodeTags{$nodeId}}) ;
		if ( ($poiOpt eq "1") and ($dirName ne "") ){
			if ($grid > 0) {
				$poiHash{$dirName}{gridSquare($lon{$nodeId}, $lat{$nodeId}, $grid)} = 1 ;
			}
			else {
				$poiHash{$dirName} = 1 ;
			}		}
		if ($test->[$nodeIndexThickness] > 0) {
			drawNodeDot ($lon{$nodeId}, $lat{$nodeId}, $test->[$nodeIndexColor], $test->[$nodeIndexThickness]) ;
		}
		if ( ($test->[$nodeIndexLabel] ne "none") or ($test->[$nodeIndexIcon] ne "none") ) {
			my $name = "" ; my $ref1 ;
			($name, $ref1) = createLabel (\@{$memNodeTags{$nodeId}}, $test->[$nodeIndexLabel], $lon{$nodeId}, $lat{$nodeId}) ;
			my @names = @$ref1 ;
			placeLabelAndIcon ($lon{$nodeId}, $lat{$nodeId}, 0, $test->[$nodeIndexThickness], $name, $test->[$nodeIndexLabelColor], $test->[$nodeIndexLabelSize], $test->[$nodeIndexLabelFont], $ppc, 
				$test->[$nodeIndexIcon], $test->[$nodeIndexIconSize], $test->[$nodeIndexIconSize], $allowIconMoveOpt, $halo) ;
		}
	} # defined $test
} # nodes


# WAYS
print "draw ways...\n" ;
foreach my $wayId (keys %memWayTags) {
	my $text = "" ; 

	# tunnels, bridges and layers
	my $tunnel = "no" ; my $bridge = "no" ; my $layer = 0 ; my $oneway = 0 ;
	foreach my $tag (@{$memWayTags{$wayId}}) {
		if ($tag->[0] eq "tunnel") { $tunnel = $tag->[1] ; }
		if ($tag->[0] eq "bridge") { $bridge = $tag->[1] ; }
		if ($tag->[0] eq "layer") { $layer = $tag->[1] ; }
		if (($tag->[0] eq "oneway") and (($tag->[1] eq "yes") or ($tag->[1] eq "true") or ($tag->[1] eq "1") ) ){ $oneway = 1 ; }
		if (($tag->[0] eq "oneway") and ($tag->[1] eq "-1") ){ $oneway = -1 ; }
	}

	# test variables for correct content
	if ($tunnel ne "yes") { $tunnel = "no" ; }
	if ($bridge ne "yes") { $bridge = "no" ; }
	my $found = 0 ;
	foreach (-5,-4,-3,-2,-1,0,1,2,3,4,5) { if ($layer == $_) { $found = 1 ; } }
	if ($found == 0) { $layer = 0 ; }

	my ($test, $ruleNumber) = getWayRule (\@{$memWayTags{$wayId}}, \@ways, $ruleScaleSet) ;
	if (defined $test) {
		if ($test->[$wayIndexFilled] eq "0") {
			if ( ($test->[$wayIndexBorderThickness] > 0) and ($test->[$wayIndexBorderColor ne "none"]) and ($tunnel ne "yes") and ($bridge ne "yes") ) {
				drawWay ($layer-.3, $test->[$wayIndexBorderColor], $test->[$wayIndexThickness]+2*$test->[$wayIndexBorderThickness], 0, nodes2Coordinates(@{$memWayNodes{$wayId}})) ;
			}
			if ($bridge eq "yes") {
				drawWayBridge ($layer-.04, "black", $test->[$wayIndexThickness]+4, 0, nodes2Coordinates(@{$memWayNodes{$wayId}})) ;
				drawWayBridge ($layer-.02, "white", $test->[$wayIndexThickness]+2, 0, nodes2Coordinates(@{$memWayNodes{$wayId}})) ;
			}
			if ($tunnel eq "yes") {
				drawWayBridge ($layer-.04, "black", $test->[$wayIndexThickness]+4, 11, nodes2Coordinates(@{$memWayNodes{$wayId}})) ;
				drawWayBridge ($layer-.02, "white", $test->[$wayIndexThickness]+2, 0, nodes2Coordinates(@{$memWayNodes{$wayId}})) ;
			}
			drawWay ($layer, $test->[$wayIndexColor], $test->[$wayIndexThickness], $test->[$wayIndexDash], nodes2Coordinates(@{$memWayNodes{$wayId}})) ;

			if (($onewayOpt eq "1") and ($oneway != 0) ) {
				addOnewayArrows (\@{$memWayNodes{$wayId}}, \%lon, \%lat, $oneway, $test->[$wayIndexThickness], $onewayColor, $layer) ;
			}
			if ($test->[$wayIndexLabel] ne "none") {
				my $name = "" ; my $ref1 ;
				($name, $ref1) = createLabel (\@{$memWayTags{$wayId}}, $test->[$wayIndexLabel],0, 0) ;
				my @names = @$ref1 ;
				if ($name ne "") { 
					addWayLabel ($wayId, $name, $ruleNumber) ; 
				}
				if ($dirOpt eq "1") {
					if ($grid > 0) {
						foreach my $node (@{$memWayNodes{$wayId}}) {
							foreach my $name (@names) {
								$directory{$name}{gridSquare($lon{$node}, $lat{$node}, $grid)} = 1 ;
							}
						}
					}
					else {
						foreach my $name (@names) {
							$directory{$name} = 1 ;
						}
					}
				}
			}
		} # not filled
		else {	# filled
			if ( ($wayId > -100000000) and (${$memWayNodes{$wayId}}[0] == ${$memWayNodes{$wayId}}[-1]) and (!defined $wayUsed{$wayId}) ) {
				if ( $test->[$wayIndexBaseLayer] == 0) { # only if not base layer area
					drawArea ($test->[$wayIndexColor], $test->[$wayIndexIcon], nodes2Coordinates( @{$memWayNodes{$wayId}} ) ) ; 
					if ($test->[$wayIndexLabel] ne "none") {
						my $name = "" ; my $ref1 ;
						($name, $ref1) = createLabel (\@{$memWayTags{$wayId}}, $test->[$wayIndexLabel], 0, 0) ;
						if ($name ne "") {
							my ($x, $y) = center (nodes2Coordinates(@{$memWayNodes{$wayId}})) ;
							placeLabelAndIcon ($x, $y, 0, 0, $name, $test->[$wayIndexLabelColor], $test->[$wayIndexLabelSize], $test->[$wayIndexLabelFont], $ppc, "none", 0, 0, $allowIconMoveOpt, $halo) ;
						}
					} # draw label
				} # if not base layer
			} #closed
		} # filled
	} # tag found
} # ways

preprocessWayLabels() ;
createWayLabels (\@labelCandidates, \@ways, $declutterOpt) ;


print declutterStat(), "\n" ;



# draw other information

print "draw legend etc. and write files...\n" ;

if ($legendOpt >= 1) {
	createLegend() ;
}

if ($scaleOpt eq "1") {
	printScale ($scaleDpi, $scaleColor) ;
}

if ($grid > 0) { drawGrid($grid, $gridColor) ; }

if ($coordsOpt eq "1") {
	drawCoords ($coordsExp, $coordsColor) ;
}

if ($rulerOpt == 1) {
	drawRuler ($rulerColor) ;
}

drawFoot ("gary68's $programName $version - data CC-BY-SA www.openstreetmap.org", "black", 10, "sans-serif") ;

writeSVG ($svgName) ;

if ($pdfOpt eq "1") {
	my ($pdfName) = $svgName ;
	$pdfName =~ s/\.svg/\.pdf/ ;
	print "creating pdf file $pdfName ...\n" ;
	`inkscape -A $pdfName $svgName` ;
}

if ($pngOpt eq "1") {
	my ($pngName) = $svgName ;
	$pngName =~ s/\.svg/\.png/ ;
	print "creating png file $pngName ...\n" ;
	`inkscape --export-dpi=$scaleDpi -e $pngName $svgName` ;
}

if ($dirOpt eq "1") {
	my $dirFile ;
	my $dirName = $svgName ;
	$dirName =~ s/\.svg/\_streets.txt/ ;
	print "creating dir file $dirName ...\n" ;
	open ($dirFile, ">", $dirName) or die ("can't open dir file\n") ;
	if ($grid eq "0") {
		foreach my $street (sort keys %directory) {
			print $dirFile "$street\n" ;
		}
	}
	else {
		foreach my $street (sort keys %directory) {
			print $dirFile "$street\t" ;
			foreach my $square (sort keys %{$directory{$street}}) {
				print $dirFile "$square " ;
			}
			print $dirFile "\n" ;
		}
	}
	close ($dirFile) ;
}

if ($poiOpt eq "1") {
	my $poiFile ;
	my $poiName = $svgName ;
	$poiName =~ s/\.svg/\_pois.txt/ ;
	print "creating poi file $poiName ...\n" ;
	open ($poiFile, ">", $poiName) or die ("can't open poi file\n") ;
	if ($grid eq "0") {
		foreach my $poi (sort keys %poiHash) {
			print $poiFile "$poi\n" ;
		}
	}
	else {
		foreach my $poi (sort keys %poiHash) {
			print $poiFile "$poi\t" ;
			foreach my $square (sort keys %{$poiHash{$poi}}) {
				print $poiFile "$square " ;
			}
			print $poiFile "\n" ;
		}
	}
	close ($poiFile) ;
}

if ($tagStatOpt eq "1") {
	my $tagFile ;
	my $tagName = $svgName ;
	$tagName =~ s/\.svg/\_tagstat.txt/ ;
	print "creating tagstat file $tagName ...\n" ;
	open ($tagFile, ">", $tagName) or die ("can't open tagstat file\n") ;

	my %usedTagsNodes = () ; my %rulesNodes = () ;
	my %usedTagsWays = () ; my %rulesWays = () ;
	print $tagFile "\n--------\nTAG STAT for nodes and ways\n--------\n" ;
	print $tagFile "\nOMITTED KEYS\n@noListTags\n\n" ;
	foreach my $node (keys %memNodeTags) { 
		foreach my $tag (@{$memNodeTags{$node}}) { $usedTagsNodes{$tag->[0]}{$tag->[1]}++ ;}
	}
	foreach my $way (keys %memWayTags) { 
		foreach my $tag (@{$memWayTags{$way}}) { $usedTagsWays{$tag->[0]}{$tag->[1]}++ ;}
	}
	foreach my $delete (@noListTags) { 
		delete $usedTagsNodes{$delete} ; 
		delete $usedTagsWays{$delete} ; 
	}

	foreach my $rule (@ways) { 
		my (@keys) = split /\|/, $rule->[$wayIndexTag] ;
		my (@values) = split /\|/, $rule->[$wayIndexValue] ;
		for (my $i=0; $i<=$#keys; $i++) {
			$rulesWays{$keys[$i]}{$values[$i]} = 1 ;
		}
	}
	foreach my $rule (@nodes) { 
		my @keys = split /\|/, $rule->[$nodeIndexTag] ;
		my @values = split /\|/, $rule->[$nodeIndexValue] ;
		for (my $i=0; $i<=$#keys; $i++) {
			$rulesNodes{$keys[$i]}{$values[$i]} = 1 ;
		}
	}

	my @sortedNodes = () ;
	foreach my $k (sort keys %usedTagsNodes) {
		foreach my $v (sort keys %{$usedTagsNodes{$k}}) {
			push @sortedNodes, [$usedTagsNodes{$k}{$v}, $k, $v] ;
		}
	}
	my @sortedWays = () ;
	foreach my $k (sort keys %usedTagsWays) {
		foreach my $v (sort keys %{$usedTagsWays{$k}}) {
			push @sortedWays, [$usedTagsWays{$k}{$v}, $k, $v] ;
		}
	}

	print $tagFile "TOP 30 LIST NODES:\n" ;
	@sortedNodes = sort { $a->[0] <=> $b->[0]} @sortedNodes ;
	@sortedNodes = reverse @sortedNodes ;
	my $i = 0 ; my $max = 29 ;
	if (scalar @sortedNodes <30) { $max = $#sortedNodes ; }
	for ($i = 0; $i<=$max; $i++) {
		my $ruleText = "-" ;
		if (defined $rulesNodes{$sortedNodes[$i]->[1]}{$sortedNodes[$i]->[2]}) { $ruleText = "RULE" ; }
		printf $tagFile "%-25s %-35s %6i %-6s\n", $sortedNodes[$i]->[1], $sortedNodes[$i]->[2], $sortedNodes[$i]->[0], $ruleText ;
	}
	print $tagFile "\n" ;

	print $tagFile "TOP 30 LIST WAYS:\n" ;
	@sortedWays = sort { $a->[0] <=> $b->[0]} @sortedWays ;
	@sortedWays = reverse @sortedWays ;
	$i = 0 ; $max = 29 ;
	if (scalar @sortedWays <30) { $max = $#sortedWays ; }
	for ($i = 0; $i<=$max; $i++) {
		my $ruleText = "-" ;
		if (defined $rulesWays{$sortedWays[$i]->[1]}{$sortedWays[$i]->[2]}) { $ruleText = "RULE" ; }
		printf $tagFile "%-25s %-35s %6i %-6s\n", $sortedWays[$i]->[1], $sortedWays[$i]->[2], $sortedWays[$i]->[0], $ruleText ;
	}
	print $tagFile "\n" ;

	print $tagFile "LIST NODES:\n" ;
	foreach my $k (sort keys %usedTagsNodes) {
		foreach my $v (sort keys %{$usedTagsNodes{$k}}) {
			my $ruleText = "-" ;
			if (defined $rulesNodes{$k}{$v}) { $ruleText = "RULE" ; }
			printf $tagFile "%-25s %-35s %6i %-6s\n", $k, $v, $usedTagsNodes{$k}{$v}, $ruleText ;
		}
	}
	print $tagFile "\n" ;
	print $tagFile "LIST WAYS:\n" ;
	foreach my $k (sort keys %usedTagsWays) {
		foreach my $v (sort keys %{$usedTagsWays{$k}}) {
			my $ruleText = "-" ;
			if (defined $rulesWays{$k}{$v}) { $ruleText = "RULE" ; }
			printf $tagFile "%-25s %-35s %6i %-6s\n", $k, $v, $usedTagsWays{$k}{$v}, $ruleText ;
		}
	}
	print $tagFile "\n" ;
	close ($tagFile) ;
}
$time1 = time() ;
print "\n$programName finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;


sub nodes2Coordinates {
#
# transform list of nodeIds to list of lons/lats
# straight array in and out
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

sub createLegend {
#
# creates legend in map
# only flagged item, only items used in map scale
#
	my $currentY = 20 ;
	my $step = 30 ;
	my $textX = 70 ;
	my $textOffset = -5 ;
	my $dotX = 40 ;
	my $areaSize = 12 ;
	my $wayStartX = 20 ;
	my $wayEndX = 60 ;
	my $areaStartX = 31 ;
	my $areaEndX = 55 ;
	my $count = 0 ;
	my $sizeLegend = 20 ;
	
	foreach my $node (@nodes) { 
		if ( ($node->[$nodeIndexLegend] == 1) and ($node->[$nodeIndexFromScale] <= $ruleScaleSet) and ($node->[$nodeIndexToScale] >= $ruleScaleSet) ) { 
			$count++ ; 
		}
	}
	foreach my $way (@ways) { 
		if ( ($way->[$wayIndexLegend] == 1)  and ($way->[$wayIndexFromScale] <= $ruleScaleSet) and ($way->[$wayIndexToScale] >= $ruleScaleSet) ) { 
			$count++ ; 
		}  
	}

	my ($xOffset, $yOffset) ;
	if ($legendOpt == 1) {
		$xOffset = 0 ;
		$yOffset = 0 ;
	}
	else { # == 2
		my ($sx, $sy) = getDimensions() ;
		$xOffset = $sx - 180 ;
		$yOffset = $sy - ($count*$step+15) ;
	}

	# erase background
	drawAreaPix ("white", "", $xOffset, $yOffset,
			$xOffset+180, $yOffset,
			$xOffset+180, $yOffset + $count*$step + 15,
			$xOffset, $yOffset + $count*$step + 15,
			$xOffset, $yOffset) ;
	
	foreach my $node (@nodes) { 
		if ( ($node->[$nodeIndexLegend] == 1) and ($node->[$nodeIndexFromScale] <= $ruleScaleSet) and ($node->[$nodeIndexToScale] >= $ruleScaleSet) ) { 
			drawNodeDotPix ($xOffset + $dotX, $yOffset+$currentY, $node->[$nodeIndexColor], $node->[$nodeIndexThickness]) ;
			drawTextPix ($xOffset+$textX, $yOffset+$currentY+$textOffset, $node->[$nodeIndexValue], "black", $sizeLegend, "Arial") ;
			$currentY += $step ;
		}  
	}

	foreach my $way (@ways) { 
		if ( ($way->[$wayIndexLegend] == 1)  and ($way->[$wayIndexFromScale] <= $ruleScaleSet) and ($way->[$wayIndexToScale] >= $ruleScaleSet) ) { 
			if ($way->[$wayIndexFilled] == 0) {
				if ( ($way->[$wayIndexBorderThickness] > 0) and ($way->[$wayIndexBorderColor ne "none"]) ) {
					drawWayPix ($way->[$wayIndexBorderColor], $way->[$wayIndexThickness]+2*$way->[$wayIndexBorderThickness], 0, $xOffset+$wayStartX, $yOffset+$currentY, $xOffset+$wayEndX, $yOffset+$currentY) ;
				}
				drawWayPix ($way->[$wayIndexColor], $way->[$wayIndexThickness], $way->[$wayIndexDash], $xOffset+$wayStartX, $yOffset+$currentY, $xOffset+$wayEndX, $yOffset+$currentY) ;
			} 
			else {
				drawAreaPix ($way->[$wayIndexColor], $way->[$wayIndexIcon], $xOffset+$areaStartX, $yOffset+$currentY-$areaSize, 
					$xOffset+$areaEndX, $yOffset+$currentY-$areaSize,
					$xOffset+$areaEndX, $yOffset+$currentY+$areaSize,
					$xOffset+$areaStartX, $yOffset+$currentY+$areaSize,
					$xOffset+$areaStartX, $yOffset+$currentY-$areaSize) ;
			}
			drawTextPix ($xOffset+$textX, $yOffset+$currentY+$textOffset, $way->[$wayIndexValue], "black", $sizeLegend, "Arial") ;
			$currentY += $step ;
		}  
	}
}

sub processMultipolygons {
#
# preprecess all multipolygons and create special ways for them, id < -100000000
#
	print "initializing multipolygon data...\n" ;
	foreach my $relId (keys %memRelationMembers) {
		my $isMulti = 0 ;
		foreach my $tag (@{$memRelationTags{$relId}}) {
			if ( ($tag->[0] eq "type") and ($tag->[1] eq "multipolygon") ) { $isMulti = 1 ; }
		}

		if ($isMulti) {
			if ($verbose eq "1") { print "\n---------------------------------------------------\n" ; }
			if ($verbose eq "1") { print "\nRelation $relId is multipolygon!\n" ; }
			
			# get inner and outer ways
			my (@innerWays) = () ; my (@outerWays) = () ;
			foreach my $member ( @{$memRelationMembers{$relId}} ) {
				if ( ($member->[0] eq "way") and ($member->[2] eq "outer") and (defined @{$memWayNodes{$member->[1]}} ) ) { push @outerWays, $member->[1] ; }
				if ( ($member->[0] eq "way") and ($member->[2] eq "inner") and (defined @{$memWayNodes{$member->[1]}} )) { push @innerWays, $member->[1] ; }
			}
			if ($verbose eq "1") { print "OUTER WAYS: @outerWays\n" ; }
			if ($verbose eq "1") { print "INNER WAYS: @innerWays\n" ; }

			my ($ringsWaysRef, $ringsNodesRef) ;
			my @ringWaysInner = () ; my @ringNodesInner = () ; my @ringTagsInner = () ;
			# build rings inner
			if (scalar @innerWays > 0) {
				($ringsWaysRef, $ringsNodesRef) = buildRings (\@innerWays, 1) ;
				@ringWaysInner = @$ringsWaysRef ; 
				@ringNodesInner = @$ringsNodesRef ;
				for (my $ring=0; $ring<=$#ringWaysInner; $ring++) {
					if ($verbose eq "1") { print "INNER RING $ring: @{$ringWaysInner[$ring]}\n" ; }
					my $firstWay = $ringWaysInner[$ring]->[0] ;
					if (scalar @{$ringWaysInner[$ring]} == 1) {$wayUsed{$firstWay} = 1 ; } # way will be marked as used/drawn by multipolygon

					@{$ringTagsInner[$ring]} = @{$memWayTags{$firstWay}} ; # ring will be tagged like first contained way
					if ($verbose eq "1") {
						print "tags from first way...\n" ;
						foreach my $tag (@{$memWayTags{$firstWay}}) {
							print "  $tag->[0] - $tag->[1]\n" ;
						}
					}
					if ( (scalar @{$memWayTags{$firstWay}}) == 0 ) {
						if ($verbose eq "1") { print "tags set to hole in mp.\n" ; }
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
					if ($verbose eq "1") { print "OUTER RING $ring: @{$ringWaysOuter[$ring]}\n" ; }
					my $firstWay = $ringWaysOuter[$ring]->[0] ;
					if (scalar @{$ringWaysOuter[$ring]} == 1) {$wayUsed{$firstWay} = 1 ; }
					@{$ringTagsOuter[$ring]} = @{$memRelationTags{$relId}} ; # tags from relation
					if ($verbose eq "1") {
						print "tags from relation...\n" ;
						foreach my $tag (@{$memRelationTags{$relId}}) {
							print "  $tag->[0] - $tag->[1]\n" ;
						}
					}
					if (scalar @{$memRelationTags{$relId}} == 1) {
						@{$ringTagsOuter[$ring]} = @{$memWayTags{$firstWay}} ; # ring will be tagged like first way
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

sub buildRings {
#
# accepts ref to array of ways and option if unclosed rings shoulf be returned
# closeOpt == 1 returns only closed rings
#
# returns two refs to arrays of arrays: ways and nodes
#
	my ($ref, $closeOpt) = @_ ;
	my (@allWays) = @$ref ;
	my @ringWays = () ;
	my @ringNodes = () ;
	my $ringCount = 0 ;

	# print "build rings for @allWays\n" ;
	if ($verbose eq "1" ) { print "BR: called.\n" ; }
	while ( scalar @allWays > 0) {
		# build new test ring
		my (@currentWays) = () ; my (@currentNodes) = () ;
		push @currentWays, $allWays[0] ;
		if ($verbose eq "1" ) { print "BR: initial way for next ring id= $allWays[0]\n" ; }
		push @currentNodes, @{$memWayNodes{$allWays[0]}} ;
		my $startNode = $currentNodes[0] ;
		my $endNode = $currentNodes[-1] ;
		if ($verbose eq "1" ) { print "BR: initial start and end node $startNode $endNode\n" ; }
		my $closed = 0 ;
		shift @allWays ; # remove first element 
		if ($startNode == $endNode) {	$closed = 1 ; }

		my $success = 1 ;
		while ( ($closed == 0) and ( (scalar @allWays) > 0) and ($success == 1) ) {
			# try to find new way
			if ($verbose eq "1" ) { print "TRY TO FIND NEW WAY\n" ; }
			$success = 0 ;
			if ($verbose eq "1" ) { print "BR: actual start and end node $startNode $endNode\n" ; }
			my $i = 0 ;
			while ( ($i < (scalar @allWays) ) and ($success == 0) ) {
				if ($verbose eq "1" ) { print "BR: testing way $i = $allWays[$i]\n" ; }
				if ($verbose eq "1" ) { print "BR:   rev in front?\n" ; }
				if ( $memWayNodes{$allWays[$i]}[0] == $startNode ) { 
					$success = 1 ;
					# reverse in front
					@currentWays = ($allWays[$i], @currentWays) ;
					@currentNodes = (reverse (@{$memWayNodes{$allWays[$i]}}), @currentNodes) ;
					splice (@allWays, $i, 1) ;
				}
				if ($success ==0) {
					if ($verbose eq "1" ) { print "BR:   app at end?\n" ; }
					if ( $memWayNodes{$allWays[$i]}[0] == $endNode)  { 
						$success = 1 ;
						# append at end
						@currentWays = (@currentWays, $allWays[$i]) ;
						@currentNodes = (@currentNodes, @{$memWayNodes{$allWays[$i]}}) ;
						splice (@allWays, $i, 1) ;
					}
				}
				if ($success ==0) {
					if ($verbose eq "1" ) { print "BR:   app in front?\n" ; }
					if ( $memWayNodes{$allWays[$i]}[-1] == $startNode) { 
						$success = 1 ;
						# append in front
						@currentWays = ($allWays[$i], @currentWays) ;
						@currentNodes = (@{$memWayNodes{$allWays[$i]}}, @currentNodes) ;
						splice (@allWays, $i, 1) ;
					}
				}
				if ($success ==0) {
					if ($verbose eq "1" ) { print "BR:   rev at end?\n" ; }
					if ( $memWayNodes{$allWays[$i]}[-1] == $endNode) { 
						$success = 1 ;
						# append reverse at the end
						@currentWays = (@currentWays, $allWays[$i]) ;
						@currentNodes = (@currentNodes, (reverse (@{$memWayNodes{$allWays[$i]}}))) ;
						splice (@allWays, $i, 1) ;
					}
				}
				$i++ ;
			} # look for new way that fits

			$startNode = $currentNodes[0] ;
			$endNode = $currentNodes[-1] ;
			if ($startNode == $endNode) { 
				$closed = 1 ; 
				if ($verbose eq "1" ) { print "BR: ring now closed\n" ;} 
			}
		} # new ring 
		
		# examine ring and act
		if ( ($closed == 1) or ($closeOpt == 0) ) {
			# eliminate double nodes in @currentNodes
			my $found = 1 ;
			while ($found) {
				$found = 0 ;
				LABCN: for (my $i=0; $i<$#currentNodes; $i++) {
					if ($currentNodes[$i] == $currentNodes[$i+1]) {
						$found = 1 ;
						splice @currentNodes, $i, 1 ;
						last LABCN ;
					}
				}
			}
			# add data to return data
			@{$ringWays[$ringCount]} = @currentWays ;
			@{$ringNodes[$ringCount]} = @currentNodes ;
			$ringCount++ ;
		}
	} 
	return (\@ringWays, \@ringNodes) ;
}

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

	# create polygons
	if ($verbose eq "1") { print "CREATING POLYGONS\n" ; }
	for (my $ring = 0 ; $ring <= $#ringWays; $ring++) {
		my @poly = () ;
		foreach my $node ( @{$ringNodes[$ring]} ) {
			push @poly, [$lon{$node}, $lat{$node}] ;
		}
		my ($p) = Math::Polygon->new(@poly) ;
		$polygon[$ring] = $p ;
		$polygonSize[$ring] = $p->area ;
		if ($verbose eq "1") { 
			print "  POLYGON $ring - created, size = $polygonSize[$ring] \n" ; 
			foreach my $tag (@{$ringTags[$ring]}) {
				print "    $tag->[0] - $tag->[1]\n" ;
			}
		}
	}


	# create is_in list (unsorted) for each ring
	if ($verbose eq "1") { print "CALC isIn\n" ; }
	for (my $ring1=0 ; $ring1<=$#polygon; $ring1++) {
		my $res = 0 ;
		for (my $ring2=0 ; $ring2<=$#polygon; $ring2++) {
			if ($ring1 < $ring2) {
				$res = isIn ($polygon[$ring1], $polygon[$ring2]) ;
				if ($res == 1) { 
					push @{$ringIsIn[$ring1]}, $ring2 ; 
					if ($verbose eq "1") { print "  $ring1 isIn $ring2\n" ; }
				} 
				if ($res == 2) { 
					push @{$ringIsIn[$ring2]}, $ring1 ; 
					if ($verbose eq "1") { print "  $ring2 isIn $ring1\n" ; }
				} 
			}
		}
	}
	if ($verbose eq "1") {
		print "IS IN LIST\n" ;
		for (my $ring1=0 ; $ring1<=$#ringNodes; $ring1++) {
			if (defined @{$ringIsIn[$ring1]}) {
				print "  ring $ring1 isIn - @{$ringIsIn[$ring1]}\n" ;
			}
		}
		print "\n" ;
	}

	# sort is_in list, biggest first
	if ($verbose eq "1") { print "SORTING isIn\n" ; }
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
		if ($verbose eq "1") { print "  stack ring $ring sorted: @{$stack[$ring]}\n" ; }
	}

	# find tops and select stacks
	if ($verbose eq "1") { print "SELECTING STACKS\n" ; }
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
						if ($verbose eq "1") { print "      element also found in stack $stackNumber2\n" ; }
					}
				}
			}
		}

		if ($found == 0) {
			@{$selectedStacks{$actualStack}} = @{$stack[$stackNumber]} ;
			$actualStack++ ;
			if ($verbose eq "1") { print "    stack $stackNumber has been selected.\n" ; }
		}
	
	}
	
	# process selected stacks

	if ($verbose eq "1") { print "PROCESS SELECTED STACKS\n" ; }
	# while stacks left
	while (scalar (keys %selectedStacks) > 0) {
		my (@k) = keys %selectedStacks ;
		if ($verbose eq "1") { print "  stacks available: @k\n" ; }
		my @nodes = () ;
		my @nodesOld ;
		my @processedStacks = () ;

		# select one bottom element 
		my $key = $k[0] ; # key of first stack
		if ($verbose eq "1") { print "  stack nr $key selected\n" ; }
		my $ringToDraw = $selectedStacks{$key}[0] ;
		if ($verbose eq "1") { print "  ring to draw: $ringToDraw\n" ; }

		push @nodesOld, @{$ringNodes[$ringToDraw]} ; # outer polygon
		push @nodes, [@{$ringNodes[$ringToDraw]}] ; # outer polygon as array

		# and remove ring from stacks; store processed stacks
		foreach my $k2 (keys %selectedStacks) {
			if ($selectedStacks{$k2}[0] == $ringToDraw) { 
				shift (@{$selectedStacks{$k2}}) ; 
				push @processedStacks, $k2 ;
				if (scalar @{$selectedStacks{$k2}} == 0) { delete $selectedStacks{$k2} ; }
				if ($verbose eq "1") { print "  removed $ringToDraw from stack $k2\n" ; }
			} 
		}

		# foreach stack in processed stacks
		foreach my $k (@processedStacks) {
			# if now bottom of a stack is hole, then add this polygon to points
			if (defined $selectedStacks{$k}) {
				my $tempRing = $selectedStacks{$k}[0] ;
				my $temp = $ringTags[$tempRing]->[0]->[0] ;
				if ($verbose eq "1") { print "           testing for hole: stack $k, ring $tempRing, tag $temp\n" ; }
				if ($ringTags[$tempRing]->[0]->[0] eq "multihole") {
					push @nodesOld, @{$ringNodes[$tempRing]} ;
					push @nodes, [@{$ringNodes[$tempRing]}] ;
					# print "      nodes so far: @nodes\n" ;
					# and remove this element from stack
					shift @{$selectedStacks{$k}} ;
					if (scalar @{$selectedStacks{$k}} == 0) { delete $selectedStacks{$k} ; }
					if ($verbose eq "1") { print "  ring $tempRing identified as hole\n" ; }
				}
			}
		}

		# add way
		@{$memWayNodes{$newId}} = @nodesOld ;
		@{$memWayTags{$newId}} = @{$ringTags[$ringToDraw]} ;
		@{$memWayPaths{$newId}} = @nodes ;
		push @{$memWayTags{$newId}}, ["layer", $actualLayer] ;
		$actualLayer++ ;
		if ($verbose eq "1") { 
			print "  DRAWN: $ringToDraw, wayId $newId\n" ; 
			foreach my $tag (@{$ringTags[$ringToDraw]}) {
				print "    k/v $tag->[0] - $tag->[1]\n" ;
			}
		}
		$newId -- ;
	} # (while)
}

sub isIn {
# checks two polygons
# return 0 = neither
# 1 = p1 is in p2
# 2 = p2 is in p1
	my ($p1, $p2) = @_ ;

	my ($p1In2) = 1 ;
	my ($p2In1) = 1 ;

	# p1 in p2 ?
	foreach my $pt1 ($p1->points) {
		if ($p2->contains ($pt1) ) {
			# good
		}
		else {
			$p1In2 = 0 ;
		}
	}

	# p2 in p1 ?
	foreach my $pt2 ($p2->points) {
		if ($p1->contains ($pt2) ) {
			# good
		}
		else {
			$p2In1 = 0 ;
		}
	}

	if ($p1In2 == 1) {
		return 1 ;
	}
	elsif ($p2In1 == 1) {
		return 2 ;
	}
	else {
		return 0 ;
	}
}

sub processRoutes {
#
# process route data
#
	my %routeColors = () ;
	my %actualColorIndex = () ;
	my %colorNumber = () ;
	my %wayRouteLabels = () ;
	my %wayRouteIcons = () ;
	my (%iconSizeX, %iconSizeY) ;

	# init before relation processing
	print "initializing route data...\n" ;
	foreach my $routeType (@routes) {
		print "  type: $routeType->[0]\n" ;
		$actualColorIndex{$routeType->[0]} = 0 ;

		# get route colors from	
		@{$routeColors{$routeType->[0]}} = split ( /;/, $routeType->[$routeIndexColor] ) ;
		$colorNumber{$routeType->[0]} = scalar @{$routeColors{$routeType->[0]}} ;
		print "  colors: @{$routeColors{$routeType->[0]}}\n" ;
	}
	print "end.\n" ;

	foreach my $relId (keys %memRelationTags) {
		my $relationType = getValue ("type", \@{$memRelationTags{$relId}}) ;
		if ( $relationType eq "route" ) {
			# look for rule
			my $routeType = getValue ("route", \@{$memRelationTags{$relId}}) ;

			foreach my $test (@routes) {
				if ( ($routeType eq $test->[$routeIndexRoute]) and ( $test->[$routeIndexFromScale] <= $ruleScaleSet) and ( $test->[$routeIndexToScale]>= $ruleScaleSet) ) {

					# new route detected
					if ($verbose eq "1" ) { print "rule found for $relId, $routeType.\n" ;	}
	
					my $color = getValue ("color", \@{$memRelationTags{$relId}}) ;
					if ($color eq "") {
						$color = getValue ("colour", \@{$memRelationTags{$relId}}) ;
					}
					if ($verbose eq "1" ) { print "  color from tags: $color\n" ;	}

					if ($color eq "") { 
						if ($verbose eq "1" ) { print "  actual color index: $actualColorIndex{$routeType}\n" ; }
						$color = $routeColors{$routeType}[$actualColorIndex{$routeType}] ; 
						$actualColorIndex{$routeType} = ($actualColorIndex{$routeType} + 1) % $colorNumber{$routeType} ;
					}
					if ($verbose eq "1" ) { print "  final color: $color\n" ; }


					# find icon
					my $iconName = getValue ("ref", \@{$memRelationTags{$relId}}) ;
					if ($iconName eq "") {
						getValue ("name", \@{$memRelationTags{$relId}})
					}

					my $file ;
					$iconName = $iconDir . $routeType . "-" . $iconName . ".svg" ;
					my $iconResult = open ($file, "<", $iconName) ;
					# print "  trying $iconName\n" ;
					if ($iconResult) { 
						if ($verbose eq "1") { print "  icon $iconName found!\n" ; }
						close ($file) ;
					} 

					if (!$iconResult) {
						$iconName =~ s/.svg/.png/ ; 
						# print "  trying $iconName\n" ;
						$iconResult = open ($file, "<", $iconName) ;
						if ($iconResult) { 
							if ($verbose eq "1") { print "  icon $iconName found!\n" ; }
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
						# print "route icon $iconName $x $y\n" ;
					}

					my ($label, $ref) = createLabel (\@{$memRelationTags{$relId}}, $test->[$routeIndexLabel]) ;
					if ($verbose eq "1" ) { print "  label: $label\n" ; }

					my $printIcon = "" ; if ($iconResult) { $printIcon=$iconName ; }
					printf "ROUTE %10s %10s %10s %30s %40s\n", $relId, $routeType, $color, $label, $printIcon ; 

					# collect ways

					my $mRef = getAllMembers ($relId, 0) ;
					my @tempMembers = @$mRef ;

					my @relWays = () ;
					# foreach my $member (@{$memRelationMembers{$relId}}) {
					foreach my $member (@tempMembers) {
						if ( ( ($member->[2] eq "none") or ($member->[2] eq "route") ) and ($member->[0] eq "way") ) { push @relWays, $member->[1] ; }
						if ( ( ($member->[2] eq "forward") or ($member->[2] eq "backward") ) and ($member->[0] eq "way") ) { push @relWays, $member->[1] ; }

						# stops
						if ( (grep /stop/, $member->[2]) and ($member->[0] eq "node") ) {
							# print "stop found in route $relId\n" ;
							if ($test->[$routeIndexStopThickness] > 0) {
								drawNodeDotRouteStops ($lon{$member->[1]}, $lat{$member->[1]}, $color, $test->[$routeIndexStopThickness]) ;
							}
						}
					}
					if ($verbose eq "1" ) { print "  ways: @relWays\n" ; }
					foreach my $w (@relWays) {
						drawWayRoute ($color, $test->[$routeIndexThickness], $test->[$routeIndexDash], $test->[$routeIndexOpacity], nodes2Coordinates (@{$memWayNodes{$w}} ) ) ;
						# $wayRouteLabels{$w} .= $label . " " ;
						$wayRouteLabels{$w}{$label} = 1 ;
						if ($iconResult) {						
							$wayRouteIcons{$w}{$iconName} = 1 ;
						}
					}
				} # rule found
			} # test rules
			# if ($verbose eq "1") { print "\n" ; }
		} # rel route
	}

	# label route ways after all relations have been processed
	foreach my $w (keys %wayRouteLabels) {
		if (scalar @{$memWayNodes{$w}} > 1) {
			my $label = "" ;
			foreach my $l (keys %{$wayRouteLabels{$w}}) {
				$label .= $l . " " ;
			} 

			my @way = @{$memWayNodes{$w}} ;
			if ($lon{$way[0]} > $lon{$way[-1]}) {
				@way = reverse (@way) ;
			}

			if (labelFitsWay (\@{$memWayNodes{$w}}, $label, $routeLabelFont, $routeLabelSize) ) {
				labelWay ($routeLabelColor, $routeLabelSize, $routeLabelFont, $label, $routeLabelOffset, nodes2Coordinates (@way) ) ;
			}
		}
	}

	foreach my $w (keys %wayRouteIcons) {
		my $offset = 0 ;
		my $nodeNumber = scalar @{$memWayNodes{$w}} ;
		if ($nodeNumber > 1) {
			my $node = $memWayNodes{$w}[int ($nodeNumber/2)] ;
			my $num = scalar (keys %{$wayRouteIcons{$w}}) ;
			$offset = int (-($num-1)*$routeIconDist/2) ; 

			foreach my $iconName (keys %{$wayRouteIcons{$w}}) {
				placeLabelAndIcon ($lon{$node}, $lat{$node}, $offset, 0, "", "none", 0, "", $ppc, $iconName, $iconSizeX{$iconName}, $iconSizeY{$iconName}, $allowIconMoveOpt, $halo) ;
				$offset += $routeIconDist ;
			}
		}
	}

}

sub getAllMembers {
#
# get all members of a relation recursively
# takes rel id and nesting level
# retruns ref to array with all members
#
	my ($relId, $nestingLevel) = @_ ;
	my @allMembers = () ;
	my $maxNestingLevel = 20 ;

	if ($nestingLevel > $maxNestingLevel) { 
		print "ERROR/WARNING nesting level of relations too deep. recursion stopped at depth $maxNestingLevel! relId=$relId\n" ;
	}
	else {
		foreach my $member (@{$memRelationMembers{$relId}}) {
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

	# calc waylen
	my $wayLength = 0 ; # in pixels
	for (my $i=0; $i<$#wayNodes; $i++) {
		my ($x1, $y1) = convert ($lon{$wayNodes[$i]}, $lat{$wayNodes[$i]}) ;
		my ($x2, $y2) = convert ($lon{$wayNodes[$i+1]}, $lat{$wayNodes[$i+1]}) ;
		$wayLength += sqrt ( ($x2-$x1)**2 + ($y2-$y1)**2 ) ;
	}


	# calc label len
	my $labelLength = length ($text) * $ppc / 10 * $size ; # in pixels

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

sub getNodeRule {
#
# takes nodetags and rules as references, and scale value
# returns ref to matching rule, undef if none found
#
	my ($ref1, $ref2, $scale) = @_ ;
	my @nodeTags = @$ref1 ;
	my @nodeRules = @$ref2 ;
	my $ruleFound ; undef $ruleFound ;

	RUL2: foreach my $rule (@nodeRules) {
		if ( ( $rule->[$nodeIndexFromScale] <= $scale) and ( $rule->[$nodeIndexToScale]>= $scale) ) {

			# get k/v pairs
			my @keys = split /\|/, $rule->[$nodeIndexTag] ;
			my @values = split /\|/, $rule->[$nodeIndexValue] ;
			my $allValid = 1 ; # assume all k/vs valid until proved otherwise

			# if (scalar @keys > 1) { print "multi rule\n" ;} 

			RUL1: for (my $i=0; $i<=$#keys; $i++) {
				my $found = 0 ;
				foreach my $tag (@nodeTags) {
					if ( ($tag->[0] eq $keys[$i]) and ( ($tag->[1] eq $values[$i]) or ($values[$i] eq "*") ) ) {
						$found = 1 ;
					}
				}
				if ($found == 0) { 
					$allValid = 0 ; 
					last RUL1 ;
				}
			}
			if ($allValid == 1) {
				# if (scalar @keys > 1) { print "multi node FOUND\n" ;} 
				$ruleFound = $rule ;
				last RUL2 ;
			}
		} # scale
	} # all rules

	return ($ruleFound) ;
}

sub getWayRule {
#
# takes waytags and rules as references, and scale value
# returns ref to matching rule, undef if none found
#
	my ($ref1, $ref2, $scale) = @_ ;
	my @wayTags = @$ref1 ;
	my @wayRules = @$ref2 ;
	my $ruleFound ; undef $ruleFound ;

	RUL4: foreach my $rule (@wayRules) {
		if ( ( $rule->[$wayIndexFromScale] <= $scale) and ( $rule->[$wayIndexToScale]>= $scale) ) {

			# get k/v pairs
			my @keys = split /\|/, $rule->[$wayIndexTag] ;
			my @values = split /\|/, $rule->[$wayIndexValue] ;
			my $allValid = 1 ; # assume all k/vs valid until proved otherwise

			RUL3: for (my $i=0; $i<=$#keys; $i++) {
				my $found = 0 ;
				foreach my $tag (@wayTags) {
					if ( ($tag->[0] eq $keys[$i]) and ( ($tag->[1] eq $values[$i]) or ($values[$i] eq "*") ) ) {
						$found = 1 ;
					}
				}
				if ($found == 0) { 
					$allValid = 0 ; 
					last RUL3 ;
				}
			}
			if ($allValid == 1) {
				# if (scalar @keys > 1) { print "multi WAY FOUND\n" ;} 
				$ruleFound = $rule ;
				last RUL4 ;
			}
		} # scale
	} # all rules
	my $ruleNumber ; undef $ruleNumber ;
	if (defined $ruleFound) {
		for (my $i=0; $i<=$#wayRules; $i++) {
			if ($wayRules[$i] == $ruleFound) { $ruleNumber = $i ; }
		}
	}	
	return ($ruleFound, $ruleNumber) ;
}


# ------------------------------------------------------------------------------------------------

sub processCoastLines {
#
#
#
	print "check and process coastlines...\n" ;
	# collect all coastline ways
	my @allWays = () ;
	foreach $wayId (keys %memWayNodes) {
		if (getValue ("natural", \@{$memWayTags{$wayId}}) eq "coastline" ) {
			push @allWays, $wayId ;
			if ($verbose eq "1") { print "COAST initial way $wayId start ${$memWayNodes{$wayId}}[0]  end ${$memWayNodes{$wayId}}[-1]\n" ; }
		}
	}
	if ($verbose eq "1") { print "COAST: " . scalar (@allWays) . " coastline ways found.\n" ; }

	if (scalar @allWays > 0) {
		# build rings
		my ($refWays, $refNodes) = buildRings (\@allWays, 0) ;
		my @ringNodes = @$refNodes ; # contains all nodes of rings // array of arrays !
		if ($verbose eq "1") { print "COAST: " . scalar (@ringNodes) . " rings found.\n" ; }

		# convert rings to coordinate system
		my @ringCoordsOpen = () ; my @ringCoordsClosed = () ;
		for (my $i=0; $i<=$#ringNodes; $i++) {
			# print "COAST: initial ring $i\n" ;
			my @actualCoords = () ;
			foreach my $node (@{$ringNodes[$i]}) {
				push @actualCoords, [convert ($lon{$node}, $lat{$node})] ;
			}
			if (${$ringNodes[$i]}[0] == ${$ringNodes[$i]}[-1]) {
				push @ringCoordsClosed, [@actualCoords] ; # islands
			}
			else {
				push @ringCoordsOpen, [@actualCoords] ;
			}
			# printRingCoords (\@actualCoords) ;
			my $num = scalar @actualCoords ;
			if ($verbose eq "1") { print "COAST: initial ring $i - $actualCoords[0]->[0],$actualCoords[0]->[1] -->> $actualCoords[-1]->[0],$actualCoords[-1]->[1]  nodes: $num\n" ; }
		}

		if ($verbose eq "1") { print "COAST: add points on border...\n" ; }
		foreach my $ring (@ringCoordsOpen) {
			# print "COAST:   ring $ring with border nodes\n" ;
			# add first point on border
			my $ref = nearestPoint ($ring->[0]) ;
			my @a = @$ref ;
			unshift @$ring, [@a] ;
			# add last point on border
			$ref = nearestPoint ($ring->[-1]) ;
			@a = @$ref ;
			push @$ring, [@a] ;
			# printRingCoords ($ring) ;
		}

		my @islandRings = @ringCoordsClosed ;
		if ($verbose eq "1") { print "COAST: " . scalar (@islandRings) . " islands found.\n" ; }
		@ringCoordsClosed = () ;

		# process ringCoordsOpen
		# add other rings, corners... 
		while (scalar @ringCoordsOpen > 0) { # as long as there are open rings
			if ($verbose eq "1") { print "COAST: building ring...\n" ; }
			my $ref = shift @ringCoordsOpen ; # get start ring
			my @actualRing = @$ref ;

			my $closed = 0 ; # mark as not closed
			my $actualX = $actualRing[-1]->[0] ;
			my $actualY = $actualRing[-1]->[1] ;

			my $actualStartX = $actualRing[0]->[0] ;  
			my $actualStartY = $actualRing[0]->[1] ;  

			if ($verbose eq "1") { print "COAST: actual and actualStart $actualX, $actualY   -   $actualStartX, $actualStartY\n" ; }

			my $corner ;
			while (!$closed) { # as long as this ring is not closed
				($actualX, $actualY, $corner) = nextPointOnBorder ($actualX, $actualY) ;
				# print "      actual $actualX, $actualY\n" ;
				my $startFromOtherPolygon = -1 ;
				# find matching ring if there is another ring
				if (scalar @ringCoordsOpen > 0) {
					for (my $i=0; $i <= $#ringCoordsOpen; $i++) {
						my @test = @{$ringCoordsOpen[$i]} ;
						# print "    test ring $i: ", $test[0]->[0], " " , $test[0]->[1] , "\n" ;
						if ( ($actualX == $test[0]->[0]) and ($actualY == $test[0]->[1]) ) {
							$startFromOtherPolygon = $i ;
							if ($verbose eq "1") { print "COAST:   matching start other polygon found i= $i\n" ; }
						}
					}
				}
				# process matching polygon, if present
				if ($startFromOtherPolygon != -1) { # start from other polygon {
					# append nodes
					# print "ARRAY TO PUSH: @{$ringCoordsOpen[$startFromOtherPolygon]}\n" ;
					push @actualRing, @{$ringCoordsOpen[$startFromOtherPolygon]} ;
					# set actual
					$actualX = $actualRing[-1]->[0] ;
					$actualY = $actualRing[-1]->[1] ;
					# drop p2 from opens
					splice @ringCoordsOpen, $startFromOtherPolygon, 1 ;
					if ($verbose eq "1") { print "COAST:   openring $startFromOtherPolygon added to actual ring\n" ; }
				}
				else {
					if ($corner) { # add corner to actual ring
						push @actualRing, [$actualX, $actualY] ;
						if ($verbose eq "1") { print "COAST:   corner $actualX, $actualY added to actual ring\n" ; }
					}
				}
				# check if closed
				if ( ($actualX == $actualStartX) and ($actualY == $actualStartY) ) {
					$closed = 1 ;
					push @actualRing, [$actualX, $actualY] ;
					push @ringCoordsClosed, [@actualRing] ;
					if ($verbose eq "1") { print "COAST:    ring now closed and moved to closed rings.\n" ; }
				}
			} # !closed
		} # open rings

		# get water color or default
		my $color = "lightblue" ;
		foreach my $way (@ways) {
			if ( ($way->[$wayIndexTag] eq "natural") and ($way->[$wayIndexValue] eq "water") ) {
				$color = $way->[$wayIndexColor] ;
			}
		}

		# build islandRings polygons
		if ($verbose eq "1") { print "OCEAN: building island polygons\n" ; }
		my @islandPolygons = () ;
		if (scalar @islandRings > 0) {
			for (my $i=0; $i<=$#islandRings; $i++) {
				my @poly = () ;
				foreach my $node ( @{$islandRings[$i]} ) {
					push @poly, [$node->[0], $node->[1]] ;
				}
				my ($p) = Math::Polygon->new(@poly) ;
				$islandPolygons[$i] = $p ;
			}
		}
		
		# build ocean ring polygons
		if ($verbose eq "1") { print "OCEAN: building ocean polygons\n" ; }
		my @oceanPolygons = () ;
		if (scalar @ringCoordsClosed > 0) {
			for (my $i=0; $i<=$#ringCoordsClosed; $i++) {
				my @poly = () ;
				foreach my $node ( @{$ringCoordsClosed[$i]} ) {
					push @poly, [$node->[0], $node->[1]] ;
				}
				my ($p) = Math::Polygon->new(@poly) ;
				$oceanPolygons[$i] = $p ;
			}
		}
		else {
			if (scalar @islandRings > 0) {
				if ($verbose eq "1") { print "OCEAN: build ocean rect\n" ; }
				my @ocean = () ;
				my ($x, $y) = getDimensions() ;
				push @ocean, [0,0], [$x,0], [$x,$y], [0,$y], [0,0] ;
				push @ringCoordsClosed, [@ocean] ;
				my ($p) = Math::Polygon->new(@ocean) ;
				push @oceanPolygons, $p ;
			}
		}

		# finally create pathes for SVG
		for (my $i=0; $i<=$#ringCoordsClosed; $i++) {
		# foreach my $ring (@ringCoordsClosed) {
			my @ring = @{$ringCoordsClosed[$i]} ;
			my @array = () ;
			my @coords = () ;
			foreach my $c (@ring) {
				push @coords, $c->[0], $c->[1] ;
			}
			push @array, [@coords] ; 
			if (scalar @islandRings > 0) {
				for (my $j=0; $j<=$#islandRings; $j++) {
					# island in ring? 1:1 and coast on border?
					# if (isIn ($islandPolygons[$j], $oceanPolygons[$i]) == 1) {
					if ( (isIn ($islandPolygons[$j], $oceanPolygons[$i]) == 1) or 
						( (scalar @islandRings == 1) and (scalar @ringCoordsClosed == 1) ) )	{
						if ($verbose eq "1") { print "OCEAN: island $j in ocean $i\n" ; }
						my @coords = () ;
						foreach my $c (@{$islandRings[$j]}) {
							push @coords, $c->[0], $c->[1] ;		
						}
						push @array, [@coords] ;
					}
				}
			}
			drawAreaOcean ($color, \@array) ;
		}
	}
}


sub nearestPoint {
#
# accepts x/y coordinates and returns nearest point on border of map to complete cut coast ways
#
	my $ref = shift ;
	my $x = $ref->[0] ;
	my $y = $ref->[1] ;
	my $xn ; my $yn ;
	my $min = 99999 ;
	# print "  NP: initial $x $y\n" ;
	my ($xmax, $ymax) = getDimensions() ;
	# print "  NP: dimensions $xmax $ymax\n" ;
	if ( abs ($xmax-$x) < $min) { # right
		$xn = $xmax ;
		$yn = $y ; 
		$min = abs ($xmax-$x) ;
	}
	if ( abs ($ymax-$y) < $min) { # bottom
		$xn = $x ;
		$yn = $ymax ; 
		$min = abs ($ymax-$y) ;
	}
	if ( abs ($x) < $min) { # left
		$xn = 0 ;
		$yn = $y ; 
		$min = abs ($x) ;
	}
	if ( abs ($y) < $min) { # top
		$xn = $x ;
		$yn = 0 ; 
	}
	# print "  NP: final $xn $yn\n" ;
	my @a = ($xn, $yn) ;
	return (\@a) ;
}


sub printRingCoords {
	my $ref = shift ;
	my @ringCoords = @$ref ;

	print "        ring coords\n" ;
	foreach my $c (@ringCoords) {
		print "$c->[0], $c->[1] *** " ;
	}
	print "\n" ;
}


sub nextPointOnBorder {
#
# accepts x/y coordinates and returns next point on border - to complete coast rings with other polygons and corner points
# hints if returned point is a corner
#
	# right turns
	my ($x, $y) = @_ ;
	my ($xn, $yn) ;
	my $corner = 0 ;
	my ($xmax, $ymax) = getDimensions() ;
	if ($x == $xmax) { # right border
		if ($y < $ymax) {
			$xn = $xmax ; $yn = $y + 1 ;
		}
		else {
			$xn = $xmax - 1 ; $yn = $ymax ;
		}
	}
	else {
		if ($x == 0) { # left border
			if ($y > 0) {
				$xn = 0 ; $yn = $y - 1 ;
			}
			else {
				$xn = 1 ; $yn = 0 ;
			}
		}
		else {
			if ($y == $ymax) { # bottom border
				if ($x > 0) {
					$xn = $x - 1 ; $yn = $ymax ;
				}
				else {
					$xn = 0 ; $yn = $ymax - 1 ; 
				}
			}
			else {
				if ($y == 0) { # top border
					if ($x < $xmax) {
						$xn = $x + 1 ; $yn = 0 ;
					}
					else {
						$xn = $xmax ; $yn = 1 ; 
					}
				}
			}
		}
	}
	# print "NPOB: $x, $y --- finito $xn $yn\n" ;

	if ( ($xn == 0) and ($yn == 0) ) { $corner = 1 ; }
	if ( ($xn == 0) and ($yn == $ymax) ) { $corner = 1 ; }
	if ( ($xn == $xmax) and ($yn == 0) ) { $corner = 1 ; }
	if ( ($xn == $xmax) and ($yn == $ymax) ) { $corner = 1 ; }

	return ($xn, $yn, $corner) ;
}
sub addWayLabel {
#
# collect all way label data before actual labeling
#
	my ($wayId, $name, $ruleNum) = @_ ;
	push @{ $wayLabels{$ruleNum}{$name} }, $wayId ;
}

sub preprocessWayLabels {
#
# preprocess way labels collected so far
# combine ways with same rule and name
# split ways where direction in longitude changes so labels will be readable later
# store result in @labelCandidates
#
	foreach my $ruleNum (keys %wayLabels) {
		# print "PPWL: ruleNum $ruleNum\n" ;
		my @ruleArray = @{$ways[$ruleNum]} ;
		# print "PPWL: processing rule $ruleArray[0] $ruleArray[1]\n" ;
		foreach my $name (keys %{$wayLabels{$ruleNum}}) {
			my (@ways) = @{$wayLabels{$ruleNum}{$name}} ;
			# print "PPWL:    processing name $name, " . scalar (@ways) . " ways\n" ;
			my ($waysRef, $nodesRef) = buildRings (\@ways, 0) ;
			my @segments = @$nodesRef ;
			# print "PPWL:    processing name $name, " . scalar (@segments) . " segments\n" ;

			my @newSegments = () ;
			foreach my $segment (@segments) {
				my @actual = @$segment ;
				# print "PPWL: Actual segment @actual\n" ;
				my $found = 1 ;
				while ($found) {
					$found = 0 ; my $sp = 0 ;
					# look for splitting point
					LABSP: for (my $i=1; $i<$#actual; $i++) {
						if ( (($lon{$actual[$i-1]} > $lon{$actual[$i]}) and ($lon{$actual[$i+1]} > $lon{$actual[$i]})) or 
							(($lon{$actual[$i-1]} < $lon{$actual[$i]}) and ($lon{$actual[$i+1]} < $lon{$actual[$i]})) ) {
							$found = 1 ;
							$sp = $i ;
							last LABSP ;
						}
					}
					if ($found == 1) {
						# print "\nname $name --- sp: $sp\n" ;
						# print "ACTUAL BEFORE: @actual\n" ;
						# create new seg
						my @newSegment = @actual[0..$sp] ;
						push @newSegments, [@newSegment] ;
						# print "NEW: @newSegment\n" ;

						# splice actual
						splice @actual, 0, $sp ;
						# print "ACTUAL AFTER: @actual\n\n" ;
					}
				}
				@$segment = @actual ;
			}

			push @segments, @newSegments ;

			foreach my $segment (@segments) {
				my (@wayNodes) = @$segment ;
				my @points = () ;

				if ($lon{$wayNodes[0]} > $lon{$wayNodes[-1]}) {
					if ( ($ruleArray[1] ne "motorway") and ($ruleArray[1] ne "trunk") ) {
						@wayNodes = reverse @wayNodes ;
					}
				}

				foreach my $node (@wayNodes) {
					push @points, convert ($lon{$node}, $lat{$node}) ;
				}
				# print "PPWL:      segment @wayNodes\n" ;
				# print "PPWL:      segment @points\n" ;

				my ($segmentLengthPixels) = 0 ; 
				for (my $i=0; $i<$#wayNodes; $i++) {
					my ($x1, $y1) = convert ($lon{$wayNodes[$i]}, $lat{$wayNodes[$i]}) ;
					my ($x2, $y2) = convert ($lon{$wayNodes[$i+1]}, $lat{$wayNodes[$i+1]}) ;
					$segmentLengthPixels += sqrt ( ($x2-$x1)**2 + ($y2-$y1)**2 ) ;
				}
				# print "$ruleNum, $wayIndexLabelSize\n" ;
				my ($labelLengthPixels) = length ($name) * $ppc / 10 * $ruleArray[$wayIndexLabelSize] ;

				# print "PPWL:        wayLen $segmentLengthPixels\n" ;
				# print "PPWL:        labLen $labelLengthPixels\n" ;
				push @labelCandidates, [$ruleNum, $name, $segmentLengthPixels, $labelLengthPixels, [@points]] ;
			}
		}
	}
}


