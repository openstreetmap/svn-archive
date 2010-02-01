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
# drawArea ($color, @nodes) - real world
# drawAreaPix ($color, @nodes) - pixels
# drawHead ($text, $color, $size) / size (1..5) 
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
# initGraph ($sizeX, $left, $bottom, $right, $top) / real world coordinates, sizeX in pixels, Y automatic
# labelWay ($col, $size, $font, $text, $tSpan, @nodes) / size can be 0..5 (or bigger...) / $tSpan = offset to line/way
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
# font size (1..5). 1 = smallest, 5 = giant
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


use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

$VERSION = '0.04' ;

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw ( 	drawArea 
			drawAreaPix 
			drawCircleRadius 
			drawCircleRadiusText 
			drawHead 
			drawFoot 
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
			initGraph 
			labelWay 
			writeSVG ) ;

#
# constants
#

my %colorHash ;

@{$colorHash{"black"}} = (0, 0, 0) ;
@{$colorHash{"darkgray"}} = (79,79,79) ;
@{$colorHash{"gray"}} = (145, 145, 145) ;
@{$colorHash{"lightgray"}} = (207, 207, 207) ;
@{$colorHash{"white"}} = (255, 255, 255) ;

@{$colorHash{"red"}} = (255, 0, 0) ;
@{$colorHash{"orange"}} = (255, 165, 0) ;
@{$colorHash{"darkorange"}} = (255, 140, 0) ;
@{$colorHash{"tomato"}} = (255, 140, 0) ;
@{$colorHash{"yellow"}} = (255, 255, 0) ;

@{$colorHash{"blue"}} = (0, 0, 255) ;
@{$colorHash{"lightblue"}} = (135, 206, 235) ;
@{$colorHash{"pink"}} = (255, 105, 180) ;
@{$colorHash{"green"}} = (0, 255, 0) ;
@{$colorHash{"darkgreen"}} = (105, 139, 105) ;
@{$colorHash{"lightgreen"}} = (0, 255, 127) ;
@{$colorHash{"brown"}} = (139, 69, 19) ;
@{$colorHash{"lightbrown"}} = (244, 164, 96) ;

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
my %color ;

my ($top, $bottom, $left, $right) ; # min and max real world coordinates
my ($sizeX, $sizeY) ; # pic size in pixels

my %svgOutputWaysNodes = () ;
my @svgOutputAreas = () ;
my @svgOutputText = () ;
my @svgOutputPixel = () ;
my @svgOutputDef = () ;
my @svgOutputPathText = () ;
my $pathNumber = 0 ;
my $svgBaseFontSize = 10 ;


sub initGraph {
#
# function initializes the picture, the colors and the background (white)
#
	my ($x, $l, $b, $r, $t) = @_ ;	
	
	$sizeX = $x ;
	$sizeY = $x * ($t - $b) / ($r - $l) / cos ($t/360*3.14*2) ;
	$top = $t ;
	$left = $l ;
	$right = $r ;
	$bottom = $b ;
}

sub convert {
#
# converts real world coordinates to system graph pixel coordinates
#
	my ($x, $y) = @_ ;

	my ($x1) = int( ($x - $left) / ($right - $left) * $sizeX ) ;
	my ($y1) = $sizeY - int( ($y - $bottom) / ($top - $bottom) * $sizeY ) ;

	return ($x1, $y1) ;
}

sub drawHead {
#
# draws text on top left corner of the picture
#
	my ($text, $col, $size) = @_ ;
	push @svgOutputText, svgElementText (20, 20, $text, $size, $col, "") ;
}

sub drawFoot {
#
# draws text on bottom left corner of the picture, below legend
#
	my ($text, $col, $size) = @_ ;
	push @svgOutputText, svgElementText (20, ($sizeY-20), $text, $size, $col, "") ;
}


sub drawTextPos {
#
# draws text at given real world coordinates. however an offset can be given for not to interfere with node dot i.e.
#
	my ($lon, $lat, $offX, $offY, $text, $col, $size) = @_ ;
	my ($x1, $y1) = convert ($lon, $lat) ;
	$x1 = $x1 + $offX ;
	$y1 = $y1 - $offY ;
	push @svgOutputText, svgElementText ($x1, $y1, $text, $size, $col, "") ;
}


sub drawTextPix {
#
# draws text at pixel position
#
	my ($x1, $y1, $text, $col, $size) = @_ ;

	push @svgOutputPixel, svgElementText ($x1, $y1+9, $text, $size, $col, "") ;
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

sub drawNodeCircle {
#
# draws node as a circle at given real world coordinates
#
	my ($lon, $lat, $col, $size) = @_ ;
	my ($x1, $y1) = convert ($lon, $lat) ;
	push @{$svgOutputWaysNodes{0}}, svgElementCircle ($x1, $y1, $size, 2, $col) ;
}

sub drawNodeCirclePix {
#
# draws node as a circle at given real world coordinates
#
	my ($x1, $y1, $col, $size) = @_ ;
	push @{$svgOutputWaysNodes{0}}, svgElementCircle ($x1, $y1, $size, 2, $col) ;
}

sub drawCircleRadius {
#
# draws circle at real world coordinates with radius in meters
#
	my ($lon, $lat, $radius, $size, $col) = @_ ;
	my $radX ; my $radY ;
	my ($x1, $y1) = convert ($lon, $lat) ;

	$radX = ($radius/1000) / (($right - $left) * 111.1) / cos ($top/360*3.14*2) * $sizeX ;
	$radY = $radX ;
	push @{$svgOutputWaysNodes{0}}, svgElementCircle ($x1, $y1, $radX, $size, $col) ;
}

sub drawCircleRadiusText {
#
# draws circle at real world coordinates with radius in meters
#
	my ($lon, $lat, $radius, $size, $col, $text) = @_ ;
	my $radX ; my $radY ;
	my ($x1, $y1) = convert ($lon, $lat) ;

	$radX = ($radius/1000) / (($right - $left) * 111.1) / cos ($top/360*3.14*2) * $sizeX ;
	$radY = $radX ;
	if ($size > 4 ) { $size = 4 ; }
	push @{$svgOutputWaysNodes{0}}, svgElementCircle ($x1, $y1, $radX, $size, $col) ;
	push @svgOutputText, svgElementText ($x1, $y1+$radY+10, $text, $size, $col, "") ;
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
	push @svgOutputText, svgElementText ($rx-$Lpix, $ry+15, $text, 2, $col, "") ;
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

	print $file "<g id=\"Labels\">\n" ;
	foreach (@svgOutputPathText) { print $file $_, "\n" ; }
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
# TODO support different fonts
#
	my ($x, $y, $text, $size, $col, $font) = @_ ; 
	my $fontSize = 12 + ($size - 1) * 4 ;
	my $svg = "<text x=\"" . $x . "\" y=\"" . $y . "\" font-size=\"" . $fontSize . "\" fill=\"#" . colorToHex(@{$colorHash{$col}}) . "\">" . encode("iso-8859-1", decode("utf8", $text)) . "</text>" ;
	return $svg ;
}

sub svgElementCircleFilled {
#
# draws circle not filled
#
	my ($x, $y, $size, $col) = @_ ;
	my $svg = "<circle cx=\"" . $x . "\" cy=\"" . $y . "\" r=\"" . $size . "\" fill=\"#" . colorToHex(@{$colorHash{$col}})  . "\" />" ;
	return $svg ;
}

sub svgElementCircle {
#
# draws filled circle / dot
#
	my ($x, $y, $radius, $size, $col) = @_ ;
	my $svg = "<circle cx=\"" . $x . "\" cy=\"" . $y . "\" r=\"" . $radius . "\" fill=\"none\" stroke=\"#" . colorToHex(@{$colorHash{$col}})  . "\" stroke-width=\"2\" />" ;
	return $svg ;
}

sub svgElementLine {
#
# draws line between two points
#
	my ($x1, $y1, $x2, $y2, $col, $size) = @_ ;
	my $svg = "<polyline points=\"" . $x1 . "," . $y1 . " " . $x2 . "," . $y2 . "\" stroke=\"#" . colorToHex(@{$colorHash{$col}}) . "\" stroke-width=\"" . $size . "\"/>" ;
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
		$svg = $svg . "\" stroke=\"#" . colorToHex(@{$colorHash{$col}}) . "\" stroke-width=\"" . $size . "\" stroke-linecap=\"" . $lineCap . "\" stroke-linejoin=\"" . $lineJoin . "\" fill=\"none\" />" ;
	}
	else {
		$svg = $svg . "\" stroke=\"#" . colorToHex(@{$colorHash{$col}}) . "\" stroke-width=\"" . $size . "\" stroke-linecap=\"" . $lineCap . "\" stroke-linejoin=\"" . $lineJoin . "\" stroke-dasharray=\"" . $dashStyle{$dash} . "\" fill=\"none\" />" ;
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
		$svg = $svg . "\" stroke=\"#" . colorToHex(@{$colorHash{$col}}) . "\" stroke-width=\"" . $size . "\" fill=\"none\" />" ;
	}
	else {
		$svg = $svg . "\" stroke=\"#" . colorToHex(@{$colorHash{$col}}) . "\" stroke-width=\"" . $size . "\" stroke-dasharray=\"" . $dashStyle{$dash} . "\" fill=\"none\" />" ;
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
	my $fontSize = 12 + ($size - 1) * 4 ;
	my $svg = "<text font-family=\"" . $font . "\" " ;
	$svg = $svg . "font-size=\"" . $fontSize . "\" " ;
	$svg = $svg . "fill=\"#" . colorToHex(@{$colorHash{$col}}) . "\" >\n" ;
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
	my $svg = "<polygon fill=\"#" . colorToHex(@{$colorHash{$col}}) . "\" points=\"" ;
	for ($i=0; $i<scalar(@points); $i+=2) {
		$svg = $svg . $points[$i] . "," . $points[$i+1] . " " ;
	}
	$svg = $svg . "\" />" ;
	return $svg ;
}

sub colorToHex {
#
# converts array of integers (rgb) to hex string without hash # (internaly used)
#
	my @arr = @_ ;
	my $string = "" ; 
	$string = sprintf "%02x", $arr[0] ;
	$string = $string . sprintf "%02x", $arr[1] ;
	$string = $string . sprintf "%02x", $arr[2] ;
	return $string ;
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

