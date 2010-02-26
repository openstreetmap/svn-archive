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

# 0.03 enhanced legend, center label in areas
# 0.04 dash styles, shaping of ways, own perl module, 
#      only svg support, no more png (use i.e.inkscape for cmd line conversion
#      layers of ways; draw only really closed areas
#      getopt, embedded pdf and png creation
# 0.05 grid implemented [-grid=INT]
#      clip function implemented [-clip]
#      street directory [-dir], shows grid squares if [-grid=INT] is enabled
#      place drawing
#      [-legend]
# 0.06 [-help]
#      font-families, sizes and offsets for texts
#      ignore case when searching for given place 
#      [-gridcolor]
#      basic multipolygon recognition      
#      [-bgcolor]
#      multipolygon with holes
#      change of style file
# 0.07 [-tagstat] allows tag examination; statistic data about usage of tags
#      multi label with § and #
#      areas now with labels in center of area
#      [-declutter]
#      [-scale + additional parameters]
#      [-ruler]
#      [-rulercolor]
# 0.08 print information paper size and paper fit
#      icons for nodes
#      multipolygon problems solved
#      declutter for icons
#      user's manual provided in pdf
#      all color names from svg can be used, even hex triplets like #FF00FF are accepted
#      § changed to !
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
#

# TODO
# -viewpng -viewpdf -viewsvg
# legend symbols larger
# STDERR outputs
# wild cards for rules (rule check function)
# module for style file reading and error handling
# color none for area / border, just print label in the middle
# tunnel with opacity value?
# nested relations for multipolygons?
# sub labelWay with intelligence (text dir/text length)
# label areas with getLabel and right formatting
# more intelligent declutter
# --------------------
# elevation lines
# style file check, color check, error messages, array for regex? Defaults
# move parts of code to pm, even a new pm
# sub key/value for rules
# edges for ways?
# see wiki
# maybe prevent double labels in vicinity of each other?

use strict ;
use warnings ;

use Getopt::Long ;
use OSM::osm ;
use OSM::mapgen 0.11 ;
use Math::Polygon ;

my $programName = "mapgen.pl" ;
my $version = "0.11" ;

my $usage = <<"END23" ;
perl mapgen.pl 
-help
-in=file.osm
-style=style.csv (original can be kept and maintained in OO sheet or MS Excel)
-out=file.svg (png and pdf names are automatic, DEFAULT=mapgen.svg)

-bgcolor=TEXT (color for background)
-size=<integer> (in pixels for x axis, DEFAULT=1024)
-clip=<integer> (percent data to be clipped on each side, 0=no clipping, DEFAULT=0)

-place=TEXT (Place to draw automatically; quotation marks can be used if necessary; OSMOSIS REQUIRED!)
-lonrad=FLOAT (radius for place width in km, DEFAULT=2)
-latrad=FLOAT (radius for place width in km, DEFAULT=2)

-declutter (declutter text; WARNING: some labels might be omitted; motorway and trunk will only be labeled in one direction)
-declutterminx=INTEGER (min distance for labels on x-axis in pixels; DEFAULT=100)
-declutterminy=INTEGER (min distance for labels on Y-axis in pixels; DEFAULT=10)

-oneways (add oneway arrows)
-onewaycolor=TEXT (color for oneway arrows)

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

-legend=INT (0=no legend; 1=legend; DEFAULT=1)
-ruler=INT (0=no ruler; 1=draw ruler; DEFAULT=1)
-rulercolor=TEXT (DEFAULT=black)
-scale (print scale)
-scalecolor=TEXT (set scale color; DEFAULT = black)
-scaleset=INTEGER (1:x preset for map scale; overrides -size=INTEGER! set correct printer options!)
-scaledpi=INTEGER (print resolution; DEFAULT = 300 dpi)

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
my $legendOpt = 1 ;
my $size = 1024 ; # default pic size longitude in pixels
my $bgColor = "white" ;
my $osmName = "" ; 
my $csvName = "" ; 
my $dirName = "" ; 
my $svgName = "mapgen.svg" ; 
my $pdfOpt = 0 ;
my $pngOpt = 0 ;
my $dirOpt = 0 ;
my $poiOpt = 0 ;
my $ppc = 5.5 ; 
my $place = "" ;
my $lonrad = 2 ;
my $latrad = 2 ;
my $helpOpt = 0 ;
my $tagStatOpt = 0 ;
my $declutterOpt = 0 ;
my $declutterMinX = 100 ;
my $declutterMinY = 10 ;
my $rulerOpt = 1 ;
my $rulerColor = "black" ;
my $scaleOpt = 0 ;
my $scaleDpi = 300 ;
my $scaleColor = "black" ;
my $scaleSet = 0 ;
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
my $wayIndexFilled = 5 ;
my $wayIndexLabel = 6 ;
my $wayIndexLabelColor = 7 ;
my $wayIndexLabelSize = 8 ;
my $wayIndexLabelFont = 9 ;
my $wayIndexLabelOffset = 10 ;
my $wayIndexLegend = 11 ;
my $wayIndexBaseLayer = 12 ;
my $wayIndexIcon = 13 ;
my $wayIndexFromScale = 14 ;
my $wayIndexToScale = 15 ;
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

my %lon ; my %lat ;

my $lonMin ; my $latMin ; my $lonMax ; my $latMax ;

my $newId = -100000000; # global ! for multipolygon data (ways)
my $iconMax = 0 ;

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
				"ppc:f"		=> \$ppc,		# pixels needed per label char in font size 10
				"pdf"		=> \$pdfOpt,		# specifies if pdf will be created
				"png"		=> \$pngOpt,		# specifies if png will be created
				"dir"		=> \$dirOpt,		# specifies if directory of streets will be created
				"poi"		=> \$poiOpt,		# specifies if directory of pois will be created
				"tagstat"	=> \$tagStatOpt,	# lists k/v used in osm file
				"declutter"	=> \$declutterOpt,
				"declutterminx"	=> \$declutterMinX,
				"declutterminy"	=> \$declutterMinY,
				"help"		=> \$helpOpt,		# 
				"oneways"	=> \$onewayOpt,
				"onewaycolor:s" => \$onewayColor,
				"place:s"	=> \$place,		# place to draw
				"lonrad:f"	=> \$lonrad,
				"latrad:f"	=> \$latrad,
				"ruler:i"	=> \$rulerOpt,
				"rulercolor:s"	=> \$rulerColor,
				"scale"		=> \$scaleOpt,
				"scaledpi:i"	=> \$scaleDpi,
				"scalecolor:s"	=> \$scaleColor,
				"scaleset:s"	=> \$scaleSet,
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
	$grid = 26 ; 
	print "WARNING: grid set to 26 parts\n" ;
}
if ($grid < 0) { 
	$grid = 0 ; 
	print "WARNING: grid set to 0\n" ;
}
if ( ($clip <0) or ($clip > 100) ) { 
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
print "scaleSet  = $scaleSet\n\n" ;

print "clip        = $clip (percent)\n" ;
print "grid        = $grid (number)\n" ;
print "gridcolor   = $gridColor\n" ;
print "coordsOpt   = $coordsOpt\n" ;
print "coordsExp   = $coordsExp\n" ;
print "coordsColor = $coordsColor\n\n" ;

print "dir       = $dirOpt\n" ;
print "poiOpt    = $poiOpt\n" ;
print "ppc       = $ppc (pixels needed per charcter font size 10)\n" ;
print "declutter = $declutterOpt\n" ;
print "declutterX= $declutterMinX\n" ;
print "declutterY= $declutterMinY\n\n" ;

print "place     = $place\n" ;
print "lonrad    = $lonrad (km)\n" ;
print "latrad    = $latrad (km)\n\n" ;

print "routeLabelColor  = $routeLabelColor \n" ; 
print "routeLabelSize   = $routeLabelSize \n" ; 
print "routeLabelFont   = $routeLabelFont \n" ; 
print "routeLabelOffset = $routeLabelOffset\n" ; 
print "iconDir          = $iconDir\n" ; 
print "routeIconDist    = $routeIconDist\n\n" ; 

print "pdf       = $pdfOpt\n" ;
print "png       = $pngOpt\n\n" ;

print "multionly = $multiOnly\n" ;
print "verbose   = $verbose\n\n" ;


# READ STYLE File
print "read style file and preprocess tile icons for areas...\n" ;
open (my $csvFile, "<", $csvName) or die ("ERROR: style file not found.") ;
my $line = <$csvFile> ; # omit SECTION

# READ NODE RULES
$line = <$csvFile> ;
while (! grep /^\"SECTION/, $line) {
	my ($key, $value, $color, $thickness, $label, $labelColor, $labelSize, $labelFont, $labelOffset, $legend, $icon, $iconSize, $fromScale, $toScale) = ($line =~ /\"(.+)\" \"(.+)\" \"(.+)\" (\d+) \"(.+)\" \"(.+)\" (\d+) \"(.+)\" (\d+) (\d) \"(.+)\" (\d+) (\d+) (\d+)/ ) ;
	# print "N $key, $value, $color, $thickness, $label, $labelColor, $labelSize, $labelFont, $labelOffset, $legend, $icon, $iconSize, $fromScale, $toScale\n" ; 
	push @nodes, [$key, $value, $color, $thickness, $label, $labelColor, $labelSize, $labelFont, $labelOffset, $legend, $icon, $iconSize, $fromScale, $toScale] ;
	if ($iconSize>$iconMax) { $iconMax = $iconSize ; } 
	$line = <$csvFile> ;
}

# READ WAY RULES
$line = <$csvFile> ; # omit SECTION
while ( (! grep /^\"SECTION/, $line) and (defined $line) ) {
	my ($key, $value, $color, $thickness, $dash, $fill, $label, $labelColor, $labelSize, $labelFont, $labelOffset, $legend, $baseLayer, $areaIcon, $fromScale, $toScale) = 
		($line =~ /\"(.+)\" \"(.+)\" \"(.+)\" (\d+) (\d+) (\d+) \"(.+)\" \"(.+)\" (\d+) \"(.+)\" ([\d\-]+) (\d) (\d) \"(.+)\" (\d+) (\d+)/ ) ;
	# print "W $key, $value, $color, $thickness, $dash, $fill, $label, $labelColor, $labelSize, $labelFont, $labelOffset, $legend, $baseLayer, $areaIcon, $fromScale, $toScale\n" ; 
	push @ways, [$key, $value, $color, $thickness, $dash, $fill, $label, $labelColor, $labelSize, $labelFont, $labelOffset, $legend, $baseLayer, $areaIcon, $fromScale, $toScale] ;

	if (($areaIcon ne "") and ($areaIcon ne "none")) { addAreaIcon ($areaIcon) ; }

	$line = <$csvFile> ;
}

# READ ROUTE RULES#print "ROUTE LINE: $line\n" ;
$line = <$csvFile> ; # omit SECTION
#print "ROUTE LINE: $line\n" ;
while ( (! grep /^\"SECTION/, $line) and (defined $line) ) {
	#print "ROUTE LINE: $line\n" ;
	my ($route, $color, $thickness, $dash, $opacity, $label, $nodeThickness, $fromScale, $toScale) = ($line =~ /\"(.+)\" \"(.+)\" (\d+) (\d+) (\d+) \"(.+)\" (\d+) (\d+) (\d+)/ ) ;
	$opacity = $opacity / 100 ;
	push @routes, [$route, $color, $thickness, $dash, $opacity, $label, $nodeThickness, $fromScale, $toScale] ;	$line = <$csvFile> ;}
close ($csvFile) ;

if ($verbose eq "1") {
	print "WAYS/AREAS\n" ;
	foreach my $way (@ways) {
		printf "%-20s %-20s %-10s %-6s %-6s %-6s %-10s %-10s %-10s %-10s %-6s %-6s %-6s %-20s %-10s %-10s\n", $way->[0], $way->[1], $way->[2], $way->[3], $way->[4], $way->[5], $way->[6], $way->[7], $way->[8], $way->[9], $way->[10], $way->[11], $way->[12], $way->[13], $way->[14], $way->[15] ;
	}
	print "\n" ;
	print "NODES\n" ;	foreach my $node (@nodes) {		printf "%-20s %-20s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-20s %6s %-10s %-10s\n", $node->[0], $node->[1], $node->[2], $node->[3], $node->[4], $node->[5], $node->[6], $node->[7], $node->[8], $node->[9], $node->[10], $node->[11], $node->[12], $node->[13] ;	}
	print "\n" ;

	print "ROUTES\n" ;	foreach my $route (@routes) {		printf "%-20s %-20s %-10s %-10s %-10s %-10s %-10s %-10s %-10s\n", $route->[0], $route->[1], $route->[2], $route->[3], $route->[4], $route->[5], $route->[6], $route->[7], $route->[8] ;	}	print "\n" ;
}

$time0 = time() ;


# -place given? look for place and call osmosis
my $placeFound = 0 ; my $placeLon ; my $placeLat ;
if ($place ne "") {
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
		if ( ($placeNode == 1) and ($placeName == 1) ) {
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
		print "place $place found at.\n" ;
		print "lon: $placeLon\n" ;
		print "lat: $placeLat\n" ;
		my $left = $placeLon - $lonrad/(111.11 * cos ( $placeLat / 360 * 3.14 * 2 ) ) ;  
		my $right = $placeLon + $lonrad/(111.11 * cos ( $placeLat / 360 * 3.14 * 2 ) ) ; 
		my $top = $placeLat + $latrad/111.11 ; 
		my $bottom = $placeLat - $latrad/111.11 ;


		if ($verbose >= 1) { print "left $left\n" ; }
		if ($verbose >= 1) { print "right $right\n" ; }
		if ($verbose >= 1) { print "top $top\n" ; }
		if ($verbose >= 1) { print "bottom $bottom\n" ; }
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
			print "  ERROR: relation $relationId references way $member->[1] which is not present!\n" ;
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



# calc area of pic and init

$lonMin = 999 ; $lonMax = -999 ; $latMin = 999 ; $latMax = -999 ;
my $key ;
foreach $key (keys %lon) {
	if ($lon{$key} > $lonMax) { $lonMax = $lon{$key} ; }
	if ($lon{$key} < $lonMin) { $lonMin = $lon{$key} ; }
	if ($lat{$key} > $latMax) { $latMax = $lat{$key} ; }
	if ($lat{$key} < $latMin) { $latMin = $lat{$key} ; }
}

if ( ($clip > 0) and ($clip < 100) ) { 
	$clip = $clip / 100 ;
	$lonMin += ($lonMax-$lonMin) * $clip ;
	$lonMax -= ($lonMax-$lonMin) * $clip ;
	$latMin += ($latMax-$latMin) * $clip ;
	$latMax -= ($latMax-$latMin) * $clip ;
}

if ($scaleSet != 0) {
	my $dist = distance ($lonMin, $latMin, $lonMax, $latMin) ;
	print "INFO: distX (km) = $dist\n" ;
	my $width = $dist / $scaleSet * 1000 * 100 / 2.54 ; # inches
	print "INFO: width (in) = $width\n" ;
	$size = int ($width * $scaleDpi) ;
	print "INFO: sizeX set to $size pixels.\n" ;
	print "INFO: set print resolution to $scaleDpi dpi!\n\n" ;
}


initGraph ($size, $lonMin, $latMin, $lonMax, $latMax, $bgColor) ;
if ($onewayOpt eq "1") { initOneways ($onewayColor) ; }

my ($paper, $w, $h) = fitsPaper ($scaleDpi) ;
print "\nINFO: map fits paper $paper using $scaleDpi dpi.\n" ;
printf "INFO: map width : %4.1f (cm)\n", $w ;
printf "INFO: map height: %4.1f (cm)\n", $h ;
my $scaleValue = getScale ($scaleDpi) ;
print "INFO: map scale 1 : $scaleValue\n\n" ;

processRoutes () ;

processMultipolygons () ; # multipolygons, (routes)

# BG AREAS

print "draw background areas...\n" ;
foreach my $wayId (sort {$a <=>$b} keys %memWayTags) {
	if ($wayId>-100000000) {

		my $test = getWayRule (\@{$memWayTags{$wayId}}, \@ways, $scaleValue) ;
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
						#print "AREA name $name $x $y\n" ;
						#print "$x, $y, 0, 0, $name, $test->[$wayIndexLabelColor], $test->[$wayIndexLabelSize], $test->[$wayIndexLabelFont]\n" ;
						drawTextPos ($x, $y, 0, 0, $name, $test->[$wayIndexLabelColor], $test->[$wayIndexLabelSize], $test->[$wayIndexLabelFont], $declutterOpt, $declutterMinX, $declutterMinY) ;
					}
				}
			}
		}
	}
}

print "draw multipolygons...\n" ;
foreach my $wayId (sort {$a <=>$b} keys %memWayTags) {
	if ($wayId <= -100000000) {
		my $test = getWayRule (\@{$memWayTags{$wayId}}, \@ways, $scaleValue) ;
		if (defined $test) {
			drawAreaMP ($test->[$wayIndexColor], $test->[$wayIndexIcon], \@{$memWayPaths{$wayId}}, \%lon, \%lat  ) ;
			# LABELS
			my $name = "" ; my $ref1 ;
			($name, $ref1) = createLabel (\@{$memWayTags{$wayId}}, $test->[$wayIndexLabel], 0, 0) ;
			if ($name ne "") {
				my ($x, $y) = center (nodes2Coordinates(@{$memWayNodes{$wayId}})) ;
				#print "MP name $name $x $y\n" ;
				drawTextPos ($x, $y, 0, 0, $name, $test->[$wayIndexLabelColor], $test->[$wayIndexLabelSize], $test->[$wayIndexLabelFont], $declutterOpt, $declutterMinX, $declutterMinY) ;
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

	my $test = getNodeRule (\@{$memNodeTags{$nodeId}}, \@nodes, $scaleValue) ;
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
		if ($test->[$nodeIndexIcon] ne "none") {
			drawIcon ($lon{$nodeId}, $lat{$nodeId}, $test->[$nodeIndexIcon], $test->[$nodeIndexIconSize], $test->[$nodeIndexIconSize], $declutterOpt, $iconMax, 0) ;
		}

		if ($test->[$nodeIndexLabel] ne "none") {
			my $name = "" ; my $ref1 ;
			($name, $ref1) = createLabel (\@{$memNodeTags{$nodeId}}, $test->[$nodeIndexLabel], $lon{$nodeId}, $lat{$nodeId}) ;
			my @names = @$ref1 ;
			if ($name ne "") {
				drawTextPos ($lon{$nodeId}, $lat{$nodeId}, 0, -$test->[$nodeIndexLabelOffset], $name, $test->[$nodeIndexLabelColor], $test->[$nodeIndexLabelSize], $test->[$nodeIndexLabelFont], $declutterOpt, $declutterMinX, $declutterMinY) ;
			}
		} # draw label
	} # defined $test
} # nodes


# WAYS

print "draw ways...\n" ;
foreach my $wayId (keys %memWayTags) {
	# print "wayid: $wayId\n" ;
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

	my $test = getWayRule (\@{$memWayTags{$wayId}}, \@ways, $scaleValue) ;
	if (defined $test) {
		#print "    tag/scale match\n" ;
		if ($test->[$wayIndexFilled] eq "0") {
			#print "      drawing way $test->[$wayIndexColor], $test->[$wayIndexThickness] ...\n" ;
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
				if (labelFitsWay (\@{$memWayNodes{$wayId}}, $name, $test->[$wayIndexLabelFont], $test->[$wayIndexLabelSize]) ) {

					my $toLabel = 1 ;
					my @way = @{$memWayNodes{$wayId}} ;
					if ($lon{$memWayNodes{$wayId}[0]} > $lon{$memWayNodes{$wayId}[-1]}) {
						@way = reverse (@way) ;
						if ( ( ($test->[$wayIndexValue] eq "motorway") or ($test->[$wayIndexValue] eq "trunk") ) and ($declutterOpt eq "1") ) {
							$toLabel = 0 ;
						}
					}
					if ($toLabel == 1) {
						labelWay ($test->[$wayIndexLabelColor], $test->[$wayIndexLabelSize], $test->[$wayIndexLabelFont], $name, $test->[$wayIndexLabelOffset], nodes2Coordinates(@way)) ;
					}
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
		else {
			if ( ($wayId > -100000000) and (${$memWayNodes{$wayId}}[0] == ${$memWayNodes{$wayId}}[-1]) and (!defined $wayUsed{$wayId}) ) {
				if ( $test->[$wayIndexBaseLayer] == 0) { 
					drawArea ($test->[$wayIndexColor], $test->[$wayIndexIcon], nodes2Coordinates( @{$memWayNodes{$wayId}} ) ) ; 
					if ( ($test->[$wayIndexLabel] ne "none") and ( $test->[$wayIndexBaseLayer] == 0) ) {
						foreach my $tag2 (@{$memWayTags{$wayId}}) {
							if ($tag2->[0] eq $test->[$wayIndexLabel]) {
								my ($x, $y) = (0, 0) ; my $count = 0 ;
								foreach my $node (@{$memWayNodes{$wayId}}) {
									$x += $lon{$node} ; $y += $lat{$node} ; $count++ ;
								}
								$x = $x / $count ; $y = $y / $count ;
								drawTextPos ($x, $y, 0, 0, $tag2->[1], $test->[$wayIndexLabelColor], $test->[$wayIndexLabelSize], $test->[$wayIndexLabelFont], $declutterOpt, $declutterMinX, $declutterMinY) ;
							}
						}
					} # draw label
				}
			} #closed
		} # filled
	} # tag found
} # ways



# draw other information

print "draw legend etc. and write files...\n" ;

if ($legendOpt == 1) {
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
	`inkscape -e $pngName $svgName` ;
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
	my %usedTags = () ; my %rules = () ;
	print "\n--------\nTAG STAT for nodes and ways\n--------\n" ;
	print "\nOMITTED KEYS\n@noListTags\n\n" ;
	foreach my $node (keys %memNodeTags) { 
		foreach my $tag (@{$memNodeTags{$node}}) { $usedTags{$tag->[0]}{$tag->[1]}++ ;}
	}
	foreach my $way (keys %memWayTags) { 
		foreach my $tag (@{$memWayTags{$way}}) { $usedTags{$tag->[0]}{$tag->[1]}++ ;}
	}
	foreach my $delete (@noListTags) { delete $usedTags{$delete} ; }
	foreach my $rule (@ways) { $rules{$rule->[$wayIndexTag]}{$rule->[$wayIndexValue]} = 1 ;}
	foreach my $rule (@nodes) { $rules{$rule->[$nodeIndexTag]}{$rule->[$nodeIndexValue]} = 1 ;}

	my @sorted = () ;
	foreach my $k (sort keys %usedTags) {
		foreach my $v (sort keys %{$usedTags{$k}}) {
			push @sorted, [$usedTags{$k}{$v}, $k, $v] ;
		}
	}
	print "TOP 20 LIST:\n" ;
	@sorted = sort { $a->[0] <=> $b->[0]} @sorted ;
	@sorted = reverse @sorted ;
	my $i = 0 ; my $max = 19 ;
	if (scalar @sorted <20) { $max = $#sorted ; }
	for ($i = 0; $i<=$max; $i++) {
		my $ruleText = "-" ;
		if (defined $rules{$sorted[$i]->[1]}{$sorted[$i]->[2]}) { $ruleText = "RULE" ; }
		printf "%-25s %-35s %6i %-6s\n", $sorted[$i]->[1], $sorted[$i]->[2], $sorted[$i]->[0], $ruleText ;
	}
	print "\n" ;

	print "LIST:\n" ;
	foreach my $k (sort keys %usedTags) {
		foreach my $v (sort keys %{$usedTags{$k}}) {
			my $ruleText = "-" ;
			if (defined $rules{$k}{$v}) { $ruleText = "RULE" ; }
			printf "%-25s %-35s %6i %-6s\n", $k, $v, $usedTags{$k}{$v}, $ruleText ;
		}
	}
	print "\n" ;
}

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

sub createLegend {
	my $currentY = 20 ;
	my $step = 20 ;
	my $textX = 70 ;
	my $textOffset = -5 ;
	my $dotX = 40 ;
	my $areaSize = 8 ;
	my $wayStartX = 20 ;
	my $wayEndX = 60 ;
	my $areaStartX = 33 ;
	my $areaEndX = 47 ;
	my $count = 0 ;
	my $sizeLegend = 14 ;
	
	foreach (@nodes) { if ($_->[$nodeIndexLegend] == 1) { $count++ ; }  }
	foreach (@ways) { if ($_->[$wayIndexLegend] == 1) { $count++ ; }  }

	# erase background
	drawAreaPix ("white", "", 0, 0,
			180,0,
			180, $count*20 + 15,
			0, $count*20 + 15,
			0, 0) ;
	
	foreach my $node (@nodes) { 
		if ($node->[$nodeIndexLegend] == 1) { 
			drawNodeDotPix ($dotX, $currentY, $node->[$nodeIndexColor], $node->[$nodeIndexThickness]) ;
			drawTextPix ($textX, $currentY+$textOffset, $node->[$nodeIndexValue], "black", $sizeLegend, "Arial") ;
			$currentY += $step ;
		}  
	}

	foreach my $way (@ways) { 
		if ($way->[$wayIndexLegend] == 1) { 
			if ($way->[$wayIndexFilled] == 0) {
				drawWayPix ($way->[$wayIndexColor], $way->[$wayIndexThickness], $way->[$wayIndexDash], $wayStartX, $currentY, $wayEndX, $currentY) ;
			} 
			else {
				drawAreaPix ($way->[$wayIndexColor], $way->[$wayIndexIcon], $areaStartX, $currentY-$areaSize, 
					$areaEndX, $currentY-$areaSize,
					$areaEndX, $currentY+$areaSize,
					$areaStartX, $currentY+$areaSize,
					$areaStartX, $currentY-$areaSize) ;
			}
			drawTextPix ($textX, $currentY+$textOffset, $way->[$wayIndexValue], "black", $sizeLegend, "Arial") ;
			$currentY += $step ;
		}  
	}
}

sub processMultipolygons {
#
# 
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
				($ringsWaysRef, $ringsNodesRef) = buildRings (\@innerWays) ;
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

					# foreach my $tag (@{$ringTagsInner[$ring]}) { $usedTags{$tag->[0]}{$tag->[1]} = 1 ; } 
				}
			}

			# build rings outer
			my @ringWaysOuter = () ; my @ringNodesOuter = () ; my @ringTagsOuter = () ;
			if (scalar @outerWays > 0) {
				($ringsWaysRef, $ringsNodesRef) = buildRings (\@outerWays) ;
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
						#print "tags from first way...\n" ;
						#foreach my $tag (@{$memWayTags{$firstWay}}) {
						#	print "  $tag->[0] - $tag->[1]\n" ;
						#}
 					}


					# foreach my $tag (@{$ringTagsOuter[$ring]}) { $usedTags{$tag->[0]}{$tag->[1]} = 1 ; } 
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
	my ($ref) = shift ;
	my (@allWays) = @$ref ;
	my @ringWays = () ;
	my @ringNodes = () ;
	my $ringCount = 0 ;

	# print "build rings for @allWays\n" ;

	while ( scalar @allWays > 0) {
		# build new test ring
		my (@currentWays) = () ; my (@currentNodes) = () ;
		push @currentWays, $allWays[0] ;
		push @currentNodes, @{$memWayNodes{$allWays[0]}} ;
		my $startNode = $currentNodes[0] ;
		my $endNode = $currentNodes[-1] ;
		my $closed = 0 ;
		shift @allWays ; # remove first element 
		if ($startNode == $endNode) {	$closed = 1 ; }

		my $success = 1 ;
		while ( ($closed != 0) and ( (scalar @allWays) > 0) and ($success == 1) ) {
			# try to find new way
			$success = 0 ;

			my $i = 0 ;
			while ( ($i < (scalar @allWays) ) and ($success == 0) ) {
				if ( $memWayNodes{$allWays[$i]}[0] == $startNode ) { 
					$success = 1 ;
					# reverse in front
					@currentWays = ($allWays[$i], @currentWays) ;
					@currentNodes = (reverse (@{$memWayNodes{$allWays[$i]}}), @currentNodes) ;
					splice (@allWays, $i, 1) ;
				}
				if ( ( $memWayNodes{$allWays[$i]}[0] == $endNode) and ($success == 0) ) { 
					$success = 1 ;
					# append at end
					@currentWays = (@currentWays, $allWays[$i]) ;
					@currentNodes = (@currentNodes, @{$memWayNodes{$allWays[$i]}}) ;
					splice (@allWays, $i, 1) ;
				}
				if ( ( $memWayNodes{$allWays[$i]}[-1] == $startNode) and ($success == 0) ) { 
					$success = 1 ;
					# append in front
					@currentWays = ($allWays[$i], @currentWays) ;
					@currentNodes = (@{$memWayNodes{$allWays[$i]}}, @currentNodes) ;
					splice (@allWays, $i, 1) ;
				}
				if ( ( $memWayNodes{$allWays[$i]}[-1] == $endNode) and ($success == 0) ) { 
					$success = 1 ;
					# append reverse at the end
					@currentWays = (@currentWays, $allWays[$i]) ;
					@currentNodes = (@currentNodes, (reverse (@{$memWayNodes{$allWays[$i]}}))) ;
					splice (@allWays, $i, 1) ;
				}
				$i++ ;
			} # look for new way that fits

			$startNode = $currentNodes[0] ;
			$endNode = $currentNodes[-1] ;
			if ($startNode == $endNode) { $closed = 1 ; }

		} # new ring 
		
		# examine ring and act
		if ($closed == 1) {
			@{$ringWays[$ringCount]} = @currentWays ;
			@{$ringNodes[$ringCount]} = @currentNodes ;
			$ringCount++ ;
		}

	} 

	return (\@ringWays, \@ringNodes) ;
}

sub processRings {
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
		# should an existing layer tag be removed? TODO?
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
	#        1 = p1 is in p2
	#        2 = p2 is in p1
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

	my %routeColors = () ;
	my %actualColorIndex = () ;
	my %colorNumber = () ;
	my %wayRouteLabels = () ;
	my %wayRouteIcons = () ;

	# init before relation processing
	print "initializing route data...\n" ;
	foreach my $routeType (@routes) {
		print "  type: $routeType->[0]\n" ;
		$actualColorIndex{$routeType->[0]} = 0 ;

		# get route colors from	
		@{$routeColors{$routeType->[0]}} = split ( /;/, $routeType->[$routeIndexColor] ) ;
		$colorNumber{$routeType->[0]} = scalar @{$routeColors{$routeType->[0]}} ;
		print "  colors: @{$routeColors{$routeType->[0]}}\n\n" ;
	}
	print "end.\n" ;

	foreach my $relId (keys %memRelationTags) {
		my $relationType = getValue ("type", \@{$memRelationTags{$relId}}) ;
		if ( $relationType eq "route" ) {
			# look for rule
			my $routeType = getValue ("route", \@{$memRelationTags{$relId}}) ;

			foreach my $test (@routes) {
				if ( ($routeType eq $test->[$routeIndexRoute]) and ( $test->[$routeIndexFromScale] <= $scaleValue) and ( $test->[$routeIndexToScale]>= $scaleValue) ) {

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

					$iconName = $iconDir . $routeType . "-" . $iconName . ".svg" ;
					my $iconResult = open (my $file, "<", $iconName) ;
					# print "  trying $iconName\n" ;
					if ($iconResult) { 
						if ($verbose eq "1") { print "  icon $iconName found!\n" ; }
					} 

					if (!$iconResult) {
						$iconName =~ s/.svg/.png/ ; 
						# print "  trying $iconName\n" ;
						$iconResult = open (my $file, "<", $iconName) ;
						if ($iconResult) { 
							if ($verbose eq "1") { print "  icon $iconName found!\n" ; }
						} 
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
			if ($verbose eq "1") { print "\n" ; }
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
				# print "  $w $offset ICON $iconName drawn\n" ;
				drawIcon ($lon{$node}, $lat{$node}, $iconName, 0, 0, $declutterOpt, $routeIconDist-2, $offset) ;
				$offset += $routeIconDist ;
			}
		}
	}

}

sub getAllMembers {
#
# get all members of a relation recursively
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
	my ($ref1, $ref2, $scale) = @_ ;
	my @nodeTags = @$ref1 ;
	my @nodeRules = @$ref2 ;
	my $ruleFound ;

	foreach my $rule (@nodeRules) {
		foreach my $tag (@nodeTags) {
			if ( ($tag->[0] eq $rule->[$nodeIndexTag]) and ($tag->[1] eq $rule->[$nodeIndexValue]) ) {
				if ( ( $rule->[$nodeIndexFromScale] <= $scale) and ( $rule->[$nodeIndexToScale]>= $scale) ) {
					if (!defined $ruleFound) {
						$ruleFound = $rule ;
					}
				}
			}
		}
	}

	return ($ruleFound) ;
}

sub getWayRule {
	my ($ref1, $ref2, $scale) = @_ ;
	my @wayTags = @$ref1 ;
	my @wayRules = @$ref2 ;
	my $ruleFound ;

	foreach my $rule (@wayRules) {
		foreach my $tag (@wayTags) {
			if ( ($tag->[0] eq $rule->[$wayIndexTag]) and ($tag->[1] eq $rule->[$wayIndexValue]) ) {
				if ( ( $rule->[$wayIndexFromScale] <= $scale) and ( $rule->[$wayIndexToScale]>= $scale) ) {
					if (!defined $ruleFound) {
						$ruleFound = $rule ;
					}
				}
			}
		}
	}

	return ($ruleFound) ;
}



