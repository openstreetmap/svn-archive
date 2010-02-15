# 
# PERL mapgen module by gary68
#
# This module contains a lot of useful graphic functions for working with osm files and data. This enables you (in conjunction with osm.pm)
# to easily draw custom maps.
# Have a look at the last (commented) function below. It is useful for your main program!
#
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
# USAGE
#
#
# center (lon, lat, lon, lat...)
# createLabel ($refTagArray, $styleLabelText)
# drawArea ($color, @nodes) - real world
# drawAreaMP
# drawAreaPix ($color, @nodes) - pixels
# drawGrid ($parts)
# drawHead ($text, $color, $size) / size (1..5) 
# drawIcon ($lon, $lat, $icon, $size) ;
# drawFoot ($text, $color, $size) / size (1..5) 
# drawNodeDot ($lon, $lat, $color, $size) / size (1..5) - real world
# drawNodeDotPix ($lon, $lat, $color, $size) / size (1..5) - pixels
# drawNodeCircle ($lon, $lat, $color, $size) / size (1..5) - real world
# drawNodeCirclePix ($lon, $lat, $color, $size) / size (1..5) - pixels
# drawRuler ($color)
# drawTextPix ($x, $y, $text, $color, $size) / size (1..5) top left = (0,0) 
# drawTextPos ($lon, $lat, $offX, $offY, $text, $color, $size) / size (1..5)
# drawWay ($layer, d$color, $size, $dash, @nodes) / size = thickness / real world
# drawWayBridge ($layer, d$color, $size, $dash, @nodes) / size = thickness / real world
# drawWayPix ($color, $size, $dash, @nodes) / size = thickness / pixels
# gridSquare ($lon, $lat) / returns grid square for directory
# fitsPaper ($x, $y, $dpi)
# initGraph ($sizeX, $left, $bottom, $right, $top) / real world coordinates, sizeX in pixels, Y automatic
# labelWay ($col, $size, $font, $text, $tSpan, @nodes) / size can be 0..5 (or bigger...) / $tSpan = offset to line/way
# printScale ($dpi, $color)
# writeSVG ($fileName)
#
#
# INTERNAL
# 
# convert ($x, $y)						-> ($x1, $y1) pixels in graph
#
# INFO
#
# graph top left coordinates: (0,0)
# size for lines = pixel width / thickness
# pass color as string, i.e. "black". list see farther down.
#
#

package OSM::mapgen ; #  

use strict ;
use warnings ;

use Math::Trig;
use File::stat;
use Time::localtime;
use List::Util qw[min max] ;
use Encode ;
use OSM::osm ;

use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

$VERSION = '0.08' ;

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw ( 	center
			createLabel
			drawArea 
			drawAreaMP
			drawAreaPix 
			drawCircleRadius 
			drawCircleRadiusText 
			drawHead 
			drawFoot 
			drawGrid
			drawIcon
			drawLegend 
			drawNodeDot 
			drawNodeDotPix 
			drawNodeCircle 
			drawNodeCirclePix 
			drawRuler 
			drawTextPix 
			drawTextPix2 
			drawTextPos 
			drawWay 
			drawWayBridge 
			drawWayPix 
			fitsPaper
			gridSquare
			initGraph 
			labelWay 
			printScale
			writeSVG ) ;

#
# constants
#

my %dashStyle = () ;
$dashStyle{1} = "15,5" ;
$dashStyle{2} = "11,5" ;
$dashStyle{3} = "7,5" ;
$dashStyle{4} = "3,5" ;
$dashStyle{10} = "2,2" ;
$dashStyle{11} = "4,4" ;
$dashStyle{12} = "6,6" ;
$dashStyle{13} = "8,8" ;
$dashStyle{14} = "10,10" ;
$dashStyle{20} = "0,2,0,4" ;
$dashStyle{21} = "0,4,0,8" ;
$dashStyle{22} = "0,6,0,12" ;
$dashStyle{23} = "0,8,0,16" ;

my $lineCap = "round" ;
my $lineJoin = "round" ;

#
# variables
#
my $image ;

my ($top, $bottom, $left, $right) ; # min and max real world coordinates
my ($sizeX, $sizeY) ; # pic size in pixels

my %svgOutputWaysNodes = () ;
my @svgOutputAreas = () ;
my @svgOutputText = () ;
my @svgOutputPixel = () ;
my @svgOutputPixelGrid = () ;
my @svgOutputDef = () ;
my @svgOutputPathText = () ;
my @svgOutputIcons = () ;
my $pathNumber = 0 ;
my $svgBaseFontSize = 10 ;

# clutter information
my %clutter = () ;
my %clutterIcon = () ;

sub initGraph {
#
# function initializes the picture, the colors and the background (white)
#
	my ($x, $l, $b, $r, $t, $color) = @_ ;	
	
	$sizeX = $x ;
	$sizeY = $x * ($t - $b) / ($r - $l) / cos ($t/360*3.14*2) ;
	$top = $t ;
	$left = $l ;
	$right = $r ;
	$bottom = $b ;

	drawArea ($color, $l, $t, $r, $t, $r, $b, $l, $b, $l, $t) ;
}

sub convert {
#
# converts real world coordinates to system graph pixel coordinates
#
	my ($x, $y) = @_ ;

	my ($x1) = int( ($x - $left) / ($right - $left) * $sizeX ) ;
	my ($y1) = int ($sizeY - int( ($y - $bottom) / ($top - $bottom) * $sizeY ) ) ;

	return ($x1, $y1) ;
}

sub gridSquare {
#
# returns grid square of given coordinates for directories
#
	my ($lon, $lat, $parts) = @_ ;
	my ($x, $y) = convert ($lon, $lat) ;
	# my $partsY = $sizeY / ($sizeX / $parts) ;
	my $xi = int ($x / ($sizeX / $parts)) + 1 ;
	my $yi = int ($y / ($sizeX / $parts)) + 1 ;
	return (chr($xi+64) . $yi) ;
}

sub drawIcon {
#
# draws icon por poi at given location. file must be png, or other supported format
#
	my ($lon, $lat, $icon, $size, , $declutter, $maxSize) = @_ ;
	my ($x, $y) = convert ($lon, $lat) ;
	$x -= $size/2 ;
	$y -= $size/2 ;

	# check for clutter condition
	my $cluttered = 0 ;
	if ($declutter eq "1") {
		foreach my $clutterX (keys %clutterIcon) {
			foreach my $clutterY (keys %{$clutterIcon{$clutterX}}) {
				my $distY = abs ($clutterY - $y) ;
				if ($distY > $maxSize) {
					# dist ok
				}
				else {
					my $distX = abs ($clutterX - $x) ;
					if ($distX < $maxSize) { 
						$cluttered = 1 ; 
					}
				}
			}
		}
	}

	if (!$cluttered) {
		push @svgOutputIcons, svgElementIcon ($x, $y, $icon, $size) ;
		$clutterIcon{$x}{$y} = 1 ;
	}
	else {
		print "WARNING: icon $icon omitted to prevent clutter!\n" ;
	}
}

sub svgElementIcon {
#
# create SVG text for icons
#
	my ($x, $y, $icon, $size) = @_ ;
	my ($out) = "<image x=\"" . $x . "\"" ;
	$out .= " y=\"" . $y . "\"" ;
	$out .= " width=\"" . $size . "\"" ;
	$out .= " height=\"" . $size . "\"" ;
	$out .= " xlink:href=\"" . $icon . "\" />" ;

	return ($out) ;	
}

sub drawHead {
#
# draws text on top left corner of the picture
#
	my ($text, $col, $size, $font) = @_ ;
	push @svgOutputText, svgElementText (20, 20, $text, $size, $font, $col) ;
}

sub drawFoot {
#
# draws text on bottom left corner of the picture, below legend
#
	my ($text, $col, $size, $font) = @_ ;
	push @svgOutputText, svgElementText (20, ($sizeY-20), $text, $size, $font, $col) ;
}


sub drawTextPos {
#
# draws text at given real world coordinates. however an offset can be given for not to interfere with node dot i.e.
#
	my ($lon, $lat, $offX, $offY, $text, $col, $size, $font, $declutter, $declutterMinX, $declutterMinY) = @_ ;
	my ($x1, $y1) = convert ($lon, $lat) ;
	$x1 = $x1 + $offX ;
	$y1 = $y1 - $offY ;

	my $cluttered = 0 ;
	if ($declutter eq "1") {
		foreach my $clutterX (keys %clutter) {
			foreach my $clutterY (keys %{$clutter{$clutterX}}) {
				my $distY = abs ($clutterY - $y1) ;
				if ($distY > $declutterMinY) {
					# dist ok
				}
				else {
					my $distX = abs ($clutterX - $x1) ;
					if ($distX < $declutterMinX) { 
						$cluttered = 1 ; 
					}
				}
			}
		}
	}

	if (!$cluttered) {
		push @svgOutputText, svgElementText ($x1, $y1, $text, $size, $font, $col) ;
		$clutter{$x1}{$y1} = $text ;
	}
	else {
		print "WARNING: label \"$text\" omitted to prevent clutter!\n" ;
	}
}


sub drawTextPix {
#
# draws text at pixel position
#
	my ($x1, $y1, $text, $col, $size, $font) = @_ ;

	push @svgOutputPixel, svgElementText ($x1, $y1+9, $text, $size, $font, $col) ;
}

sub drawTextPixGrid {
#
# draws text at pixel position. code goes to grid
#
	my ($x1, $y1, $text, $col, $size) = @_ ;

	push @svgOutputPixelGrid, svgElementText ($x1, $y1+9, $text, $size, "sans-serif", $col) ;
}

sub drawNodeDot {
#
# draws node as a dot at given real world coordinates
#
	my ($lon, $lat, $col, $size) = @_ ;
	my ($x1, $y1) = convert ($lon, $lat) ;
	push @{$svgOutputWaysNodes{0}}, svgElementCircleFilled ($x1, $y1, $size, $col) ;
}

sub drawNodeDotPix {
#
# draws node as a dot at given pixels
#
	my ($x1, $y1, $col, $size) = @_ ;
	push @svgOutputPixel, svgElementCircleFilled ($x1, $y1, $size, $col) ;
}


sub drawWay {
#
# draws way as a line at given real world coordinates. nodes have to be passed as array ($lon, $lat, $lon, $lat...)
# $size = thickness
#
	my ($layer, $col, $size, $dash, @nodes) = @_ ;
	my $i ;
	my @points = () ;

	for ($i=0; $i<$#nodes; $i+=2) {
		my ($x, $y) = convert ($nodes[$i], $nodes[$i+1]) ;
		push @points, $x ; push @points, $y ; 
	}
	push @{$svgOutputWaysNodes{$layer+$size/10}}, svgElementPolyline ($col, $size, $dash, @points) ;
}
sub drawWayBridge {
#
# draws way as a line at given real world coordinates. nodes have to be passed as array ($lon, $lat, $lon, $lat...)
# $size = thickness
#
	my ($layer, $col, $size, $dash, @nodes) = @_ ;
	my $i ;
	my @points = () ;

	for ($i=0; $i<$#nodes; $i+=2) {
		my ($x, $y) = convert ($nodes[$i], $nodes[$i+1]) ;
		push @points, $x ; push @points, $y ; 
	}
	push @{$svgOutputWaysNodes{$layer+$size/10}}, svgElementPolylineBridge ($col, $size, $dash, @points) ;
}

sub drawWayPix {
#
# draws way as a line at given pixels. nodes have to be passed as array ($x, $y, $x, $y...)
# $size = thickness
#
	my ($col, $size, $dash, @nodes) = @_ ;
	my $i ;
	my @points = () ;

	for ($i=0; $i<$#nodes; $i+=2) {
		my ($x, $y) = ($nodes[$i], $nodes[$i+1]) ;
		push @points, $x ; push @points, $y ; 
	}
	push @svgOutputPixel, svgElementPolyline ($col, $size, $dash, @points) ;
}

sub drawWayPixGrid {
#
# draws way as a line at given pixels. nodes have to be passed as array ($x, $y, $x, $y...)
# $size = thickness
#
	my ($col, $size, $dash, @nodes) = @_ ;
	my $i ;
	my @points = () ;

	for ($i=0; $i<$#nodes; $i+=2) {
		my ($x, $y) = ($nodes[$i], $nodes[$i+1]) ;
		push @points, $x ; push @points, $y ; 
	}
	push @svgOutputPixelGrid, svgElementPolyline ($col, $size, $dash, @points) ;
}


sub labelWay {
#
# labels a way
#
	my ($col, $size, $font, $text, $tSpan, @nodes) = @_ ;
	my $i ;
	my @points = () ;

	#print "labelWay: $col, $size, $font, $text\n" ;

	for ($i=0; $i<$#nodes; $i+=2) {
		my ($x, $y) = convert ($nodes[$i], $nodes[$i+1]) ;
		push @points, $x ; push @points, $y ; 
	}
	my $pathName = "Path" . $pathNumber ; $pathNumber++ ;
	push @svgOutputDef, svgElementPath ($pathName, @points) ;
	push @svgOutputPathText, svgElementPathText ($col, $size, $font, $text, $pathName, $tSpan) ;
}


sub drawArea {
#
# draws an area like waterway=riverbank or landuse=forest. 
# pass color as string and nodes as list (x1, y1, x2, y2...) - real world coordinates
#
	my ($col, @nodes) = @_ ;
	my $i ;
	my @points = () ;
	
	for ($i=0; $i<$#nodes; $i+=2) {
		my ($x1, $y1) = convert ($nodes[$i], $nodes[$i+1]) ;
		push @points, $x1 ; push @points, $y1 ; 
	}
	push @svgOutputAreas, svgElementPolygonFilled ($col, @points) ;
}

sub drawAreaPix {
#
# draws an area like waterway=riverbank or landuse=forest. 
# pass color as string and nodes as list (x1, y1, x2, y2...) - pixels
# used for legend
#
	my ($col, @nodes) = @_ ;
	my $i ;
	my @points = () ;
	for ($i=0; $i<$#nodes; $i+=2) {
		my ($x1, $y1) = ($nodes[$i], $nodes[$i+1]) ;
		push @points, $x1 ; push @points, $y1 ; 
	}
	push @svgOutputPixel, svgElementPolygonFilled ($col, @points) ;
}

sub drawAreaMP {
#
# draws an area like waterway=riverbank or landuse=forest. 
# pass color as string and nodes as list (x1, y1, x2, y2...) - real world coordinates
#
# receives ARRAY of ARRAY of NODES LIST! NOT coordinates list like other functions
#
	my ($col, $ref, $refLon, $refLat) = @_ ;
	# my %lon = %$refLon ;
	# my %lat = %$refLat ;
	my @ways = @$ref ;
	my $i ;
	my @array = () ;

	foreach my $way (@ways) {	
		my @actual = @$way ;
		# print "drawAreaMP - actual ring/way: @actual\n" ; 
			my @points = () ;
		for ($i=0; $i<$#actual; $i++) { # without last node! SVG command 'z'!
			my ($x1, $y1) = convert ( $$refLon{$actual[$i]}, $$refLat{$actual[$i]} ) ;
			push @points, $x1 ; push @points, $y1 ; 
		}
		push @array, [@points] ;
		# print "drawAreaMP - array pushed: @points\n" ; 
	}

	push @svgOutputAreas, svgElementMultiPolygonFilled ($col, \@array) ;
}



sub drawRuler {
#
# draws ruler in top right corner, size is automatic
#
	my $col = shift ;

	my $B ;
	my $B2 ;
	my $L ;
	my $Lpix ;
	my $x ;
	my $text ;
	my $rx = $sizeX - 20 ;
	my $ry = 20 ;
	
	$B = $right - $left ; 				# in degrees
	$B2 = $B * cos ($top/360*3.14*2) * 111.1 ;	# in km
	$text = "100m" ; $x = 0.1 ;			# default length ruler
	if ($B2 > 5) {$text = "500m" ; $x = 0.5 ; }	# enlarge ruler
	if ($B2 > 10) {$text = "1km" ; $x = 1 ; }
	if ($B2 > 50) {$text = "5km" ; $x = 5 ; }
	if ($B2 > 100) {$text = "10km" ; $x = 10 ; }
	$L = $x / (cos ($top/360*3.14*2) * 111.1 ) ;	# length ruler in km
	$Lpix = $L / $B * $sizeX ;			# length ruler in pixels

	push @svgOutputText, svgElementLine ($rx-$Lpix,$ry,$rx,$ry, $col, 1) ;
	push @svgOutputText, svgElementLine ($rx-$Lpix,$ry,$rx-$Lpix,$ry+10, $col, 1) ;
	push @svgOutputText, svgElementLine ($rx,$ry,$rx,$ry+10, $col, 1) ;
	push @svgOutputText, svgElementLine ($rx-$Lpix/2,$ry,$rx-$Lpix/2,$ry+5, $col, 1) ;
	push @svgOutputText, svgElementText ($rx-$Lpix, $ry+15, $text, 10, "sans-serif", $col) ;
}

sub drawGrid {
#
# draw grid on top of map. receives number of parts in x/lon direction
#
	my ($number, $color) = @_ ;
	my $part = $sizeX / $number ;
	my $numY = $sizeY / $part ;
	# vertical lines
	for (my $i = 1; $i <= $number; $i++) {
		drawWayPixGrid ($color, 1, 1, $i*$part, 0, $i*$part, $sizeY) ;
		drawTextPixGrid (($i-1)*$part+$part/2, 20, chr($i+64), $color, 20) ;
	}
	# hor. lines
	for (my $i = 1; $i <= $numY; $i++) {
		drawWayPixGrid ($color, 1, 1, 0, $i*$part, $sizeX, $i*$part) ;
		drawTextPixGrid (20, ($i-1)*$part+$part/2, $i, $color, 20) ;
	}
}



#####
# SVG
#####


sub writeSVG {
#
# writes svg elemets collected so far to file
#
	my ($fileName) = shift ;
	my $file ;
	open ($file, ">", $fileName) || die "can't open svg output file";
	print $file "<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"no\"?>\n" ;
	print $file "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\" >\n" ;
	print $file "<svg version=\"1.1\" baseProfile=\"full\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" width=\"$sizeX\" height=\"$sizeY\" >\n" ;
	print $file "<rect width=\"$sizeX\" height=\"$sizeY\" y=\"0\" x=\"0\" fill=\"#ffffff\" />\n" ;

	print $file "<defs>\n" ;
	foreach (@svgOutputDef) { print $file $_, "\n" ; }
	print $file "</defs>\n" ;

	print $file "<g id=\"Areas\">\n" ;
	foreach (@svgOutputAreas) { print $file $_, "\n" ; }
	print $file "</g>\n" ;

	print $file "<g id=\"WaysAndNodes\">\n" ;

	foreach my $layer (sort {$a <=> $b} (keys %svgOutputWaysNodes)) {
		foreach (@{$svgOutputWaysNodes{$layer}}) { print $file $_, "\n" ; }
	}
	print $file "</g>\n" ;

	print $file "<g id=\"Text\">\n" ;
	foreach (@svgOutputText) { print $file $_, "\n" ; }
	print $file "</g>\n" ;

	print $file "<g id=\"Icons\">\n" ;
	foreach (@svgOutputIcons) { print $file $_, "\n" ; }
	print $file "</g>\n" ;

	print $file "<g id=\"Labels\">\n" ;
	foreach (@svgOutputPathText) { print $file $_, "\n" ; }
	print $file "</g>\n" ;

	print $file "<g id=\"Grid\">\n" ;
	foreach (@svgOutputPixelGrid) { print $file $_, "\n" ; }
	print $file "</g>\n" ;

	print $file "<g id=\"Pixels\">\n" ;
	foreach (@svgOutputPixel) { print $file $_, "\n" ; }
	print $file "</g>\n" ;

	print $file "</svg>\n" ;
	close ($file) ;
}

sub svgElementText {
#
# creates string with svg element incl utf-8 encoding
#
	my ($x, $y, $text, $size, $font, $col) = @_ ; 
	my $svg = "<text x=\"" . $x . "\" y=\"" . $y . 
		"\" font-size=\"" . $size . 
		"\" font-family=\"" . $font . 
		"\" fill=\"" . $col . 
		"\">" . encode("iso-8859-1", decode("utf8", $text)) . "</text>" ;
	return $svg ;
}

sub svgElementCircleFilled {
#
# draws circle not filled
#
	my ($x, $y, $size, $col) = @_ ;
	my $svg = "<circle cx=\"" . $x . "\" cy=\"" . $y . "\" r=\"" . $size . "\" fill=\"" . $col  . "\" />" ;
	return $svg ;
}

sub svgElementCircle {
#
# draws filled circle / dot
#
	my ($x, $y, $radius, $size, $col) = @_ ;
	my $svg = "<circle cx=\"" . $x . "\" cy=\"" . $y . "\" r=\"" . $radius . "\" fill=\"none\" stroke=\"" . $col  . "\" stroke-width=\"2\" />" ;
	return $svg ;
}

sub svgElementLine {
#
# draws line between two points
#
	my ($x1, $y1, $x2, $y2, $col, $size) = @_ ;
	my $svg = "<polyline points=\"" . $x1 . "," . $y1 . " " . $x2 . "," . $y2 . "\" stroke=\"" . $col . "\" stroke-width=\"" . $size . "\"/>" ;
	return $svg ;
}


sub svgElementPolyline {
#
# draws way to svg
#
	my ($col, $size, $dash, @points) = @_ ;
	my $svg = "<polyline points=\"" ;
	my $i ;
	for ($i=0; $i<scalar(@points)-1; $i+=2) {
		$svg = $svg . $points[$i] . "," . $points[$i+1] . " " ;
	}
	if ($dash == 0) { 
		$svg = $svg . "\" stroke=\"" . $col . "\" stroke-width=\"" . $size . "\" stroke-linecap=\"" . $lineCap . "\" stroke-linejoin=\"" . $lineJoin . "\" fill=\"none\" />" ;
	}
	else {
		$svg = $svg . "\" stroke=\"" . $col . "\" stroke-width=\"" . $size . "\" stroke-linecap=\"" . $lineCap . "\" stroke-linejoin=\"" . $lineJoin . "\" stroke-dasharray=\"" . $dashStyle{$dash} . "\" fill=\"none\" />" ;
	}
	return $svg ;
}

sub svgElementPolylineBridge {
#
# draws way to svg
#
	my ($col, $size, $dash, @points) = @_ ;
	my $svg = "<polyline points=\"" ;
	my $i ;
	for ($i=0; $i<scalar(@points)-1; $i+=2) {
		$svg = $svg . $points[$i] . "," . $points[$i+1] . " " ;
	}
	if ($dash == 0) { 
		$svg = $svg . "\" stroke=\"" . $col . "\" stroke-width=\"" . $size . "\" fill=\"none\" />" ;
	}
	else {
		$svg = $svg . "\" stroke=\"" . $col . "\" stroke-width=\"" . $size . "\" stroke-dasharray=\"" . $dashStyle{$dash} . "\" fill=\"none\" />" ;
	}
	return $svg ;
}

sub svgElementPath {
#
# creates path element for later use with textPath
#
	my ($pathName, @points) = @_ ;
	my $svg = "<path id=\"" . $pathName . "\" d=\"M " ;
	my $i ;
	my $first = 1 ;
	for ($i=0; $i<scalar(@points); $i+=2) {
		if ($first) {
			$svg = $svg . $points[$i] . "," . $points[$i+1] . " " ;
			$first = 0 ;
		}
		else {
			$svg = $svg . "L " . $points[$i] . "," . $points[$i+1] . " " ;
		}
	}
	$svg = $svg . "\" />\n" ;
}

sub svgElementPathText {
#
# draws text to path element
#
	my ($col, $size, $font, $text, $pathName, $tSpan) = @_ ;
	my $svg = "<text font-family=\"" . $font . "\" " ;
	$svg = $svg . "font-size=\"" . $size . "\" " ;
	$svg = $svg . "fill=\"" . $col . "\" >\n" ;
	$svg = $svg . "<textPath xlink:href=\"#" . $pathName . "\" text-anchor=\"middle\" startOffset=\"50%\" >\n" ;
	$svg = $svg . "<tspan dy=\"" . $tSpan . "\" >" . encode("iso-8859-1", decode("utf8", $text)) . " </tspan>\n" ;
	$svg = $svg . "</textPath>\n</text>\n" ;
	return $svg ;
}

sub svgElementPolygonFilled {
#
# draws areas in svg, filled with color
#
	my ($col, @points) = @_ ;
	my $i ;
	my $svg = "<polygon fill-rule=\"evenodd\" fill=\"" . $col . "\" points=\"" ;
	for ($i=0; $i<scalar(@points); $i+=2) {
		$svg = $svg . $points[$i] . "," . $points[$i+1] . " " ;
	}
	$svg = $svg . "\" />" ;
	return $svg ;
}

sub svgElementMultiPolygonFilled {
#
# draws mp in svg, filled with color. accepts holes. receives ARRAY of ARRAY of coordinates
#
	my ($col, $ref) = @_ ;
	my @ways = @$ref ;
	my $i ;
	my $svg = "<path fill-rule=\"evenodd\" fill=\"" . $col . "\" d=\"" ;
	
	foreach my $way (@ways) {
		my @actual = @$way ;
		# print "svg - actual: @actual\n" ;
		for ($i=0; $i<scalar(@actual); $i+=2) {
			if ($i == 0) { $svg .= " M " ; } else { $svg .= " L " ; }
			$svg = $svg . $actual[$i] . "," . $actual[$i+1] ;
		}
		$svg .= " z" ;
		# print "svg - text = $svg\n" ; 
	}

	$svg = $svg . "\" />" ;
	# print "svg - text = $svg\n" ; 
	return $svg ;
}

sub createLabel {
#
# takes @tags and labelKey(s) from style file and creates labelTextTotal and array of labels for directory
# takes more keys in one string - using a separator. 
#
# § all listed keys will be searched for and values be concatenated
# # first of found keys will be used to select value
# "name§ref" will return all values if given
# "name#ref" will return name, if given. if no name is given, ref will be used. none given, no text
#
	my ($ref1, $styleLabelText) = @_ ;
	my @tags = @$ref1 ;
	my @keys ;
	my @labels = () ;
	my $labelTextTotal = "" ; 

	if (grep /!/, $styleLabelText) { # AND
		@keys = split ( /!/, $styleLabelText) ;
		# print "par found: $styleLabelText; @keys\n" ;
		for (my $i=0; $i<=$#keys; $i++) {
			foreach my $tag (@tags) {
				if ($tag->[0] eq $keys[$i]) {
					push @labels, $tag->[1] ;
				}
			}
		}
		$labelTextTotal = "" ;
		foreach my $label (@labels) { $labelTextTotal .= $label . " " ; }
	}
	else { # PRIO
		@keys = split ( /#/, $styleLabelText) ;
		my $i = 0 ; my $found = 0 ;
		while ( ($i<=$#keys) and ($found == 0) ) {
			foreach my $tag (@tags) {
				if ($tag->[0] eq $keys[$i]) {
					push @labels, $tag->[1] ;
					$labelTextTotal = $tag->[1] ;
					$found = 1 ;
				}
			}
			$i++ ;
		}		
	}
	return ( $labelTextTotal, \@labels) ;
}

sub center {
#
# calculate center of area by averageing lons/lats. could be smarter because result could be outside of area! TODO
#
	my @nodes = @_ ;
	my $x = 0 ;
	my $y = 0 ;
	my $num = 0 ;

	while (scalar @nodes > 0) { 
		my $y1 = pop @nodes ;
		my $x1 = pop @nodes ;
		$x += $x1 ;
		$y += $y1 ;
		$num++ ;
	}
	$x = $x / $num ;
	$y = $y / $num ;
	return ($x, $y) ;
}

sub printScale {
#
# print scale based on dpi and global variables left, right etc.
#
	my ($dpi, $color) = @_ ;

	my $dist = distance ($left, $bottom, $right, $bottom) ;
	# print "distance = $dist\n" ;
	my $inches = $sizeX / $dpi ;
	# print "inches = $inches\n" ;
	my $cm = $inches * 2.54 ;
	# print "cm = $cm\n" ;
	my $scale = int ( $dist / ($cm/100/1000)  ) ;
	$scale = int ($scale / 100) * 100 ;

	my $text = "1 : $scale ($dpi dpi)" ;
	
	drawTextPix ($sizeX-200, 50, $text, $color, 14, "sans-serif") ;
	# print "scale = $text\n\n" ;
}


sub fitsPaper {
#
# takes dpi and calculates on what paper size the map will fit. sizes are taken from global variables
#
	my ($dpi) = shift ;
	my @sizes = () ;
	my $width = $sizeX / $dpi * 2.54 ;
	my $height = $sizeY / $dpi * 2.54 ;
	my $paper = "" ;
	push @sizes, ["4A0", 168.2, 237.8] ;
	push @sizes, ["2A0", 118.9, 168.2] ;
	push @sizes, ["A0", 84.1, 118.9] ;
	push @sizes, ["A1", 59.4, 84.1] ;
	push @sizes, ["A2", 42, 59.4] ;
	push @sizes, ["A3", 29.7, 42] ;
	push @sizes, ["A4", 21, 29.7] ;
	push @sizes, ["A5", 14.8, 21] ;
	push @sizes, ["A6", 10.5, 14.8] ;
	push @sizes, ["none", 0, 0] ;

	foreach my $size (@sizes) {
		if ( ( ($width<=$size->[1]) and ($height<=$size->[2]) ) or ( ($width<=$size->[2]) and ($height<=$size->[1]) ) ) {
			$paper = $size->[0] ;
		}
	}

	return ($paper, $width, $height) ;
}



1 ;

#
# copy this useful function to your main program and uncomment, if needed
#
# sub nodes2Coordinates {
#
# transform list of nodeIds to list of lons/lats
#
#	my @nodes = @_ ;
#	my $i ;
#	my @result = () ;
#
#	#print "in @nodes\n" ;
#
#	for ($i=0; $i<=$#nodes; $i++) {
#		push @result, $lon{$nodes[$i]} ;
#		push @result, $lat{$nodes[$i]} ;
#	}
#	return @result ;
#}

