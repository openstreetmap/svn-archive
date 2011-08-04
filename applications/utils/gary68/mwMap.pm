# 
# PERL mapweaver module by gary68
#
#
#
#
# Copyright (C) 2011, Gerhard Schwanz
#
# This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 3 of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>
#


package mwMap ; 

use strict ;
use warnings ;

use mwConfig ;
# use mwMisc ;
# use mwFile ;
# use mwLabel ;

use OSM::osm ;

use Geo::Proj4 ;


my $areaNum = 0 ;
my %areaDef = () ;

use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw (	initGraph
			drawCircle
			drawSquare
			drawTriangle
			drawDiamond
			drawRect
			drawText
			writeMap
			drawWay
			drawArea
			fitsPaper
			getScale
			createPath
			pathText
			placeIcon
			convert
			gridSquare
			getDimensions
			initOneways
			addOnewayArrows
			addAreaIcon
			createShield
			getMaxShieldSize
			getShieldSizes
			getShieldId
			addToLayer
			createLegendFile
		 ) ;


my @belowWays = ("background", "base", "area", "multi") ;

my @aboveWays = ( "arealabels", "wayLabels", "shields", "routes", "routeStops", "nodes", "icons", "text", "additional") ;

my @elements = ("scale", "ruler", "legend", "wns", "header", "footer", "rectangles", "title") ;

my %svgLayer = () ;
my %wayLayer = () ;

my $shieldPathId = 0 ;
my %createdShields = () ;
my %shieldXSize = () ;
my %shieldYSize = () ;

my $proj ;

my ($bottom, $left, $right, $top) ;
my ($sizeX, $sizeY) ;
my ($projLeft, $projBottom, $projRight, $projTop) ;
my ($projSizeX, $projSizeY) ;

sub initGraph {

	# function initializes the picture and projection

	my ($x, $l, $b, $r, $t) = @_ ;	

	# my $l0 = int($l) - 1 ;
	my $l0 = int(($r+$l) / 2 ) ;

	$proj = Geo::Proj4->new(
		proj => cv('projection'), 
		ellps => cv('ellipsoid'), 
		lon_0 => $l0 
		) or die "parameter error: ".Geo::Proj4->error. "\n"; 


	($projLeft, $projBottom) = $proj->forward($b, $l) ; # lat/lon!!!
	($projRight, $projTop) = $proj->forward($t, $r) ; # lat/lon!!!

	$projSizeX = $projRight - $projLeft ;
	$projSizeY = $projTop - $projBottom ;

	my $factor = $projSizeY / $projSizeX ;

	$sizeX = int ($x) ;
	$sizeY = int ($x * $factor) ;

	mwLabel::initQuadTrees ($sizeX, $sizeY) ;

	$top = $t ;
	$left = $l ;
	$right = $r ;
	$bottom = $b ;

	if ( ( cv('bgcolor') ne "none" ) and ( cv('bgcolor') ne "" ) ) {
		my $col = cv('bgcolor') ;
		my $svgText = "fill=\"$col\" " ;
		drawRect (0, 0, $sizeX, $sizeY, 0, $svgText, "background") ;
	}

	if ( cv('ruler') ne "0" ) {
		drawRuler() ;
	}

	if ( cv('scale') ne "0" ) {
		drawScale() ;
	}

	if ( cv('grid') != 0) {
		drawGrid() ;
	}
	if ( cv('coords') eq "1") {
		drawCoords() ;
	}
	if ( length cv('foot') > 0 ) {
		drawFoot() ;
	}
	if ( length cv('head') > 0 ) {
		drawHead() ;
	}

}

sub addToLayer {
	my ($layer, $text) = @_ ;

	if ( $layer =~ /^[\d\-\.]+$/) {
		push @{$wayLayer{$layer}}, $text ;
		# print "adding NUMERIC: $text\n" ;
	}
	else {
		push @{$svgLayer{$layer}}, $text ;
		# print "adding TEXTUAL: $text\n" ;
	}
}

sub drawWay {

	# accepts list of nodes (plus convert=1)  or list of x,y,x,y (convert=0) and draws way/polygon to layerNr if defined or to layerName

	my ($nodesRef, $convert, $svgString, $layerName, $layerNumber) = @_ ;
	my @points = () ;

	# convert? and expand.
	if ($convert) {
		my ($lonRef, $latRef, $tagRef) = mwFile::getNodePointers() ;
		foreach my $node (@$nodesRef) {
			my ($x, $y) = convert ( $$lonRef{$node}, $$latRef{$node}) ;
			push @points, $x, $y ;
		}
	}
	else {
		@points = @$nodesRef ;
	}

	my $refp = simplifyPoints (\@points) ;
	@points = @$refp ;


	my $svg = "<polyline points=\"" ;
	for (my$i=0; $i<scalar(@points)-1; $i+=2) {
		$svg = $svg . $points[$i] . "," . $points[$i+1] . " " ;
	}

	$svg = $svg . "\" $svgString />" ;

	if (defined $layerNumber) {
		push @{ $wayLayer{ $layerNumber } }, $svg ;
	}
	else {
		push @{ $svgLayer { $layerName } }, $svg ;
	}
}



sub drawText {

	my ($x, $y, $convert, $text, $svgString, $layerName) = @_ ;

	if ($convert) {
		($x, $y) = convert ($x, $y) ;
	}

	my $svg = "<text x=\"$x\" y=\"$y\" $svgString>" . $text . "</text>" ;

	push @{ $svgLayer { $layerName } }, $svg ;

}




sub drawCircle {

	# draws circle element to svgLayer given; if convertCoords then lon / lat is converted to x / y
	# circleradius either in pixel or in meters (convert=1)

	my ($x, $y, $convertCoords, $radius, $convertRadius, $format, $layerName) = @_ ;

	if ($convertCoords) {
		($x, $y) = convert ($x, $y) ;
	}
	if ($convertRadius) {
		$radius = $radius / (1000 * distance ($left, $bottom, $right, $bottom) ) * $sizeX ;
	}
	my $svg = "<circle cx=\"$x\" cy=\"$y\" r=\"$radius\" " ;
	$svg .= $format . " />" ;

	push @{ $svgLayer { $layerName } }, $svg ;
}

sub drawSquare {

	# draws square element to svgLayer given; if convertCoords then lon / lat is converted to x / y
	# square size either in pixel or in meters (convert=1)

	my ($x, $y, $convertCoords, $size, $convertSize, $format, $layerName) = @_ ;

	if ($convertCoords) {
		($x, $y) = convert ($x, $y) ;
	}
	if ($convertSize) {
		$size = $size / (1000 * distance ($left, $bottom, $right, $bottom) ) * $sizeX ;
	}

	my $x1 = $x - $size ;
	my $y1 = $y - $size ;
	my $dSize = 2 * $size ;

	my $svg = "<rect x=\"$x1\" y=\"$y1\" width=\"$dSize\" height=\"$dSize\" " ;
	$svg .= $format . " />" ;

	push @{ $svgLayer { $layerName } }, $svg ;
}

sub drawTriangle {

	# draws triangle element to svgLayer given; if convertCoords then lon / lat is converted to x / y
	# square size either in pixel or in meters (convert=1)

	my ($x, $y, $convertCoords, $size, $convertSize, $format, $layerName) = @_ ;

	if ($convertCoords) {
		($x, $y) = convert ($x, $y) ;
	}
	if ($convertSize) {
		$size = $size / (1000 * distance ($left, $bottom, $right, $bottom) ) * $sizeX ;
	}

	my $h = int ( sqrt ($size * $size / 2) ) ;

	my $x1 = $x ;
	my $y1 = $y - $size ;
	my $x2 = $x - $h ;
	my $y2 = $y + $h ;
	my $x3 = $x + $h ;
	my $y3 = $y + $h ;

	my $svg = "<polyline points=\"$x1,$y1 $x2,$y2 $x3,$y3 $x1,$y1\" " ;
	$svg .= $format . " />" ;

	push @{ $svgLayer { $layerName } }, $svg ;
}

sub drawDiamond {

	# draws diamond element to svgLayer given; if convertCoords then lon / lat is converted to x / y
	# square size either in pixel or in meters (convert=1)

	my ($x, $y, $convertCoords, $size, $convertSize, $format, $layerName) = @_ ;

	if ($convertCoords) {
		($x, $y) = convert ($x, $y) ;
	}
	if ($convertSize) {
		$size = $size / (1000 * distance ($left, $bottom, $right, $bottom) ) * $sizeX ;
	}

	my $x1 = $x - $size ; # left
	my $y1 = $y ;
	my $x2 = $x ; # top
	my $y2 = $y - $size ;
	my $x3 = $x + $size ; #right
	my $y3 = $y ;
	my $x4 = $x ; # bottom
	my $y4 = $y + $size ;

	my $svg = "<polyline points=\"$x1,$y1 $x2,$y2 $x3,$y3 $x4,$y4 $x1,$y1\" " ;
	$svg .= $format . " />" ;

	push @{ $svgLayer { $layerName } }, $svg ;
}

sub drawRect {

	# draws square element to svgLayer given; if convertCoords then lon / lat is converted to x / y
	# square size either in pixel or in meters (convert=1)

	my ($x1, $y1, $x2, $y2, $convertCoords, $format, $layerName) = @_ ;

	if ($convertCoords) {
		($x1, $y1) = convert ($x1, $y1) ;
		($x2, $y2) = convert ($x2, $y2) ;
	}

	my $sizeX = $x2 - $x1 ;
	my $sizeY = $y2 - $y1 ;

	my $svg = "<rect x=\"$x1\" y=\"$y1\" width=\"$sizeX\" height=\"$sizeY\" " ;
	$svg .= $format . " />" ;

	push @{ $svgLayer { $layerName } }, $svg ;
}


sub createPath {
#
# creates path element for later use with textPath
#
	my ($pathName, $refp, $layer) = @_ ;

	my $refp2 = simplifyPoints ($refp) ;
	my @points = @$refp2 ;

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

	push @{ $svgLayer{ $layer } }, $svg ;
}


sub pathText {
#
# draws text to path element; alignment: start, middle, end
#
	my ($svgText, $text, $pathName, $tSpan, $alignment, $offset, $layer) = @_ ;

	my $svg = "<text $svgText >\n" ;
	$svg = $svg . "<textPath xlink:href=\"#" . $pathName . "\" text-anchor=\"" . $alignment . "\" startOffset=\"" . $offset . "%\" >\n" ;
	$svg = $svg . "<tspan dy=\"" . $tSpan . "\" >" . $text . " </tspan>\n" ;
	$svg = $svg . "</textPath>\n</text>\n" ;

	push @{ $svgLayer{ $layer } }, $svg ;
}


sub placeIcon {
#
# create SVG text for icons
#
	my ($x, $y, $icon, $sizeX, $sizeY, $layer) = @_ ;
	my ($out) = "<image x=\"" . $x . "\"" ;
	$out .= " y=\"" . $y . "\"" ;
	if ($sizeX > 0) { $out .= " width=\"" . $sizeX . "\"" ; }
	if ($sizeY > 0) { $out .= " height=\"" . $sizeY . "\"" ; }
	$out .= " xlink:href=\"" . $icon . "\" />" ;

	push @{ $svgLayer{ $layer } }, $out ;
}


sub drawArea {
#
# draws mp in svg ARRAY of ARRAY of nodes/coordinates
#
	my ($svgText, $icon, $ref, $convert, $layer) = @_ ;
	my @ways = @$ref ;
	my $i ;
	my $svg = "" ;
	my @newArray = () ;

	# TODO loop converts original data !!!

	if ($convert) {
		my ($lonRef, $latRef, $tagRef) = mwFile::getNodePointers () ;
		foreach my $aRef (@ways) {
			my @way = @$aRef ;
			my @newCoords = () ;
			foreach my $n (@way) {
				my ($x, $y) = convert ($$lonRef{$n}, $$latRef{$n}) ;
				push @newCoords, $x, $y ;
			}
			push @newArray , [@newCoords] ;
		}
		@ways = @newArray ;
	}

	if (defined $areaDef{$icon}) {
		$svg = "<path $svgText fill-rule=\"evenodd\" style=\"fill:url(" . $areaDef{$icon} . ")\" d=\"" ;
	}
	else {
		$svg = "<path $svgText fill-rule=\"evenodd\" d=\"" ;
	}
	
	foreach my $way (@ways) {
		my @actual = @$way ;
		for ($i=0; $i<scalar(@actual); $i+=2) {
			if ($i == 0) { $svg .= " M " ; } else { $svg .= " L " ; }
			$svg = $svg . $actual[$i] . " " . $actual[$i+1] ;
		}
		$svg .= " z" ;
	}

	$svg = $svg . "\" />" ;

	push @{ $svgLayer{ $layer } }, $svg ;
}



# ---------------------------------------------------------------------

sub writeMap {

	my $fileName = cv ('out')  ;

	open (my $file, ">", $fileName) || die "can't open svg output file $fileName\n";

	print $file "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n" ;
	print $file "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\" >\n" ;

	my $w = $sizeX / 300 * 2.54 ; # cm
	my $h = $sizeY / 300 * 2.54 ;

	my ($svg) = "<svg version=\"1.1\" baseProfile=\"full\" xmlns=\"http://www.w3.org/2000/svg\" " ;
	$svg .= "xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" " ;
	$svg .= "width=\"$w" . "cm\" height=\"$h" . "cm\" viewBox=\"0 0 $sizeX $sizeY\">\n" ;
	print $file $svg ;

	# definitions
	if ( defined @{$svgLayer{'definitions'}} ) {
		print $file "<defs>\n" ;
		foreach ( @{$svgLayer{'definitions'}} ) { print $file $_, "\n" ; }
		print $file "</defs>\n" ;
	}

	# below ways
	foreach my $layer (@belowWays) {
		if ( defined @{$svgLayer{$layer}} ) {
			print $file "<g id=\"$layer\">\n" ;
			foreach ( @{$svgLayer{$layer}} ) { print $file $_, "\n" ; }
			print $file "</g>\n" ;
		}
	}

	# ways
	foreach my $layer (sort {$a <=> $b} keys %wayLayer) {
		if ( defined @{$wayLayer{$layer}} ) {
			print $file "<g id=\"way$layer\">\n" ;
			foreach ( @{$wayLayer{$layer}} ) { print $file $_, "\n" ; }
			print $file "</g>\n" ;
		}
	}


	# above of ways
	foreach my $layer (@aboveWays) {
		if ( defined @{$svgLayer{$layer}} ) {
			print $file "<g id=\"$layer\">\n" ;
			foreach ( @{$svgLayer{$layer}} ) { print $file $_, "\n" ; }
			print $file "</g>\n" ;
		}
	}


	foreach my $layer (@elements) {
		if (defined @{$svgLayer{$layer}}) {
			print $file "<g id=\"$layer\">\n" ;
			foreach ( @{$svgLayer{$layer}} ) { print $file $_, "\n" ; }
			print $file "</g>\n" ;
		}
	}


	print $file "</svg>\n" ;

	close ($file) ;

	if (cv('pdf') eq "1") {
		my ($pdfName) = $fileName ;
		$pdfName =~ s/\.svg/\.pdf/ ;
		print "creating pdf file $pdfName ...\n" ;
		`inkscape -A $pdfName $fileName` ;
	}

	if (cv('png') eq "1") {
		my ($pngName) = $fileName ;
		$pngName =~ s/\.svg/\.png/ ;
		my $dpi = cv('pngdpi') ;
		print "creating png file $pngName ($dpi dpi)...\n" ;
		`inkscape --export-dpi=$dpi -e $pngName $fileName` ;
	}



}

# -----------------------------------------------------------------------------------

sub drawGrid {
#
# draw grid on top of map. receives number of parts in x/lon direction
#

	my $number = cv ('grid') ;
	my $color = cv ('gridcolor') ;

	my $part = $sizeX / $number ;
	my $numY = $sizeY / $part ;

	my $svgStringLine="stroke=\"$color\" stroke-width=\"5\" stroke-dasharray=\"30,30\"" ;

	my $svgStringText="font-family=\"sans-serif\" font-size=\"60\" fill=\"$color\"" ;

	# vertical lines
	for (my $i = 1; $i <= $number; $i++) {
		my @coords = ($i*$part, 0, $i*$part, $sizeY) ;
		drawWay (\@coords, 0, $svgStringLine, "additional", undef) ;
		drawText ( ($i-1)*$part+$part/2, 160, 0, chr($i+64), $svgStringText, "additional") ;

	}

	# hor. lines
	for (my $i = 1; $i <= $numY; $i++) {
		my @coords = (0, $i*$part, $sizeX, $i*$part) ;
		drawWay (\@coords, 0, $svgStringLine, "additional", undef) ;
		drawText ( 20, ($i-1)*$part+$part/2, 0, $i, $svgStringText, "additional") ;

	}
}

sub drawCoords {
#
# draws coordinates grid on map
#
	my $exp = cv('coordsexp') ; 
	my $color = cv ('coordscolor');
	my $step = 10 ** $exp ;

	# vert. lines
	my $start = int ($left / $step) + 1 ;
	my $actual = $start * $step ;

	my $svgStringLine="stroke=\"$color\" stroke-width=\"3\"" ;
	my $svgStringText="font-family=\"sans-serif\" font-size=\"30\" fill=\"$color\"" ;

	while ($actual < $right) {
		my ($x1, $y1) = convert ($actual, 0) ;

		drawText ( $x1+10, $sizeY-50, 0, $actual, $svgStringText, "additional") ;

		my @coords = ($x1, 0, $x1, $sizeY) ;
		drawWay (\@coords, 0, $svgStringLine, "additional", undef) ;

		$actual += $step ;
	}

	# hor lines
	$start = int ($bottom / $step) + 1 ;
	$actual = $start * $step ;
	while ($actual < $top) {
		# print "actualY: $actual\n" ;
		my ($x1, $y1) = convert (0, $actual) ;

		drawText ( $sizeX-180, $y1+30, 0, $actual, $svgStringText, "additional") ;

		my @coords = (0, $y1, $sizeX, $y1) ;
		drawWay (\@coords, 0, $svgStringLine, "additional", undef) ;

		$actual += $step ;
	}
}




# -----------------------------------------------------------------------------------

sub convert {

	# converts real world coordinates to system graph pixel coordinates

	my ($x, $y) = @_ ;

	my ($x1, $y1) = $proj->forward($y, $x) ; # lat/lon!!!

	my $x2 = int ( ($x1 - $projLeft) / ($projRight - $projLeft) * $sizeX ) ;
	my $y2 = $sizeY - int ( ($y1 - $projBottom) / ($projTop - $projBottom) * $sizeY ) ;

	return ($x2, $y2) ;
}

sub simplifyPoints {
	my $ref = shift ;
	my @points = @$ref ;
	my @newPoints ;
	my $maxIndex = $#points ;

	if (scalar @points > 4) {
		# push first
		push @newPoints, $points[0], $points[1] ;

		# push other
		for (my $i=2; $i <= $maxIndex; $i+=2) {
			# $simplifyTotal++ ;
			if ( ($points[$i]==$points[$i-2]) and ($points[$i+1]==$points[$i-1]) ) {
				# same
				# $simplified++ ;
			}
			else {
				push @newPoints, $points[$i], $points[$i+1] ;
			}
		}
		return (\@newPoints) ;
	}
	else {
		return ($ref) ;
	}

}

sub drawRuler {
#
# draws ruler
#
	my $col = cv('rulercolor') ;	

	my $B ; my $B2 ;
	my $L ; my $Lpix ;
	my $x ;
	my $text ;

	my $lineThickness = 8 ; # at 300dpi
	my $textSize = 40 ; # at 300 dpi
	my $textDist = 60 ; # at 300 dpi
	my $lineLen = 40 ; # at 300 dpi

	my $xOffset = 2 * $lineThickness ;
	my $yOffset = 2 * $lineThickness ;
		
	$B = $right - $left ; 				# in degrees
	$B2 = $B * cos ($top/360*3.14*2) * 111.1 ;	# in km
	$text = "50m" ; $x = 0.05 ;			# default length ruler

	if ($B2 > 0.5) {$text = "100 m" ; $x = 0.1 ; }	# enlarge ruler
	if ($B2 > 1) {$text = "500 m" ; $x = 0.5 ; }	# enlarge ruler
	if ($B2 > 5) {$text = "1 km" ; $x = 1 ; }
	if ($B2 > 10) {$text = "5 km" ; $x = 5 ; }
	if ($B2 > 50) {$text = "10 km" ; $x = 10 ; }
	$L = $x / (cos ($top/360*3.14*2) * 111.1 ) ;	# length ruler in km
	$Lpix = $L / $B * $sizeX ;			# length ruler in pixels

	my $rSizeX = int ($Lpix + 2 * $xOffset) ;
	my $rSizeY = int ($lineLen + $textSize + 3 * $yOffset) ;
	addToLayer ("definitions", "<g id=\"rulerdef\" width=\"$rSizeX\" height=\"$rSizeY\" >") ;

	if ( cv('rulerbackground') ne "none" ) {
		my $color = cv ('rulerbackground') ;
		my $svgString = "fill=\"$color\"" ;
		drawRect (0, 0, $rSizeX, $rSizeY, 0, $svgString, "definitions") ;
	}

	my $svgString = "stroke=\"$col\" stroke-width=\"$lineThickness\" stroke-linecap=\"round\" " ;

	my @coords = ($xOffset, $yOffset, $xOffset+$Lpix, $yOffset) ;
	drawWay (\@coords, 0, $svgString, "definitions", undef) ;

	@coords = ($xOffset, $yOffset, $xOffset, $yOffset+$lineLen) ;
	drawWay (\@coords, 0, $svgString, "definitions", undef) ;

	@coords = ($xOffset+$Lpix, $yOffset, $xOffset+$Lpix, $yOffset+$lineLen) ;
	drawWay (\@coords, 0, $svgString, "definitions", undef) ;

	@coords = ($xOffset+$Lpix/2, $yOffset, $xOffset+$Lpix/2, $yOffset+$lineLen/2) ;
	drawWay (\@coords, 0, $svgString, "definitions", undef) ;

	$svgString = "fill=\"$col\" stroke=\"$col\" font-size=\"45\" " ;
	my $scale= getScale() ;
	$text .= "(1:$scale)" ;
	drawText ($xOffset, $yOffset+$textDist+30, 0, $text, $svgString, "definitions") ;

	addToLayer ("definitions", "</g>") ;

	my $posX = 40 ; my $posY = 40 ;

	if ( cv('ruler') eq "2") {
		$posX = $sizeX - 40 - $rSizeX ;
		$posY = 40 ;
	}

	if ( cv('ruler') eq "3") {
		$posX = 40 ;
		$posY = $sizeY - 40 - $rSizeY ;
	}

	if ( cv('ruler') eq "4") {
		$posX = $sizeX - 40 - $rSizeX ;
		$posY = $sizeY - 40 - $rSizeY ;
	}

	addToLayer ("ruler", "<use x=\"$posX\" y=\"$posY\" xlink:href=\"#rulerdef\" />") ;
}

sub drawScale {
#
# draws scale value
#
	my $col = cv('scalecolor') ;	

	my $xOffset = 20 ;
	my $yOffset = 20 ;
	my $fontSize = 70 ;		
	my $borderDist = 60 ;

	my $rSizeX = int (350 + 2 * $xOffset) ;
	my $rSizeY = int ($fontSize + 2 * $yOffset) ;
	addToLayer ("definitions", "<g id=\"scaledef\" width=\"$rSizeX\" height=\"$rSizeY\" >") ;

	if ( cv('scalebackground') ne "none" ) {
		my $color = cv ('scalebackground') ;
		my $svgString = "fill=\"$color\"" ;
		drawRect (0, 0, $rSizeX, $rSizeY, 0, $svgString, "definitions") ;
	}

	my $scale= getScale() ;
	my $svgString = "fill=\"$col\" stroke=\"$col\" font-size=\"$fontSize\" " ;
	drawText ($xOffset, $fontSize + $yOffset, 0, "1:$scale", $svgString, "definitions") ;

	addToLayer ("definitions", "</g>") ;

	my $posX = $borderDist ; my $posY = $borderDist ;

	if ( cv('scale') eq "2") {
		$posX = $sizeX - $borderDist - $rSizeX ;
		$posY = $borderDist ;
	}

	if ( cv('scale') eq "3") {
		$posX = $borderDist ;
		$posY = $sizeY - $borderDist - $rSizeY ;
	}

	if ( cv('scale') eq "4") {
		$posX = $sizeX - $borderDist - $rSizeX ;
		$posY = $sizeY - $borderDist - $rSizeY ;
	}

	addToLayer ("scale", "<use x=\"$posX\" y=\"$posY\" xlink:href=\"#scaledef\" />") ;
}

sub drawFoot {
#
# draws footer
#
	my $col = cv('footcolor') ;	
	my $text = cv('foot') ;	
	my $len = length $text ;

	my $xOffset = 20 ;
	my $yOffset = 20 ;
	my $fontSize = cv('footsize') ;		
	my $borderDistX = 60 ;
	my $borderDistY = $fontSize + 50 ;

	my $rSizeX = int ($len*cv('ppc')/10*$fontSize + 2 * $xOffset) ;
	my $rSizeY = int ($fontSize + 2 * $yOffset) ;
	addToLayer ("definitions", "<g id=\"footdef\" width=\"$rSizeX\" height=\"$rSizeY\" >") ;

	if ( cv('footbackground') ne "none" ) {
		my $color = cv ('footbackground') ;
		my $svgString = "fill=\"$color\"" ;
		drawRect (0, 0, $rSizeX, $rSizeY, 0, $svgString, "definitions") ;
	}

	my $svgString = "fill=\"$col\" stroke=\"$col\" font-size=\"$fontSize\" " ;
	drawText ($xOffset, $fontSize + $yOffset, 0, $text, $svgString, "definitions") ;

	addToLayer ("definitions", "</g>") ;

	my $posX = $borderDistX ; my $posY = $sizeY - $borderDistY ;

	addToLayer ("footer", "<use x=\"$posX\" y=\"$posY\" xlink:href=\"#footdef\" />") ;
}

sub drawHead {
#
# draws header
#
	my $col = cv('headcolor') ;	
	my $text = cv('head') ;	
	my $len = length $text ;

	my $xOffset = 20 ;
	my $yOffset = 20 ;
	my $fontSize = cv('headsize') ;		
	my $borderDistX = 60 ;
	my $borderDistY = 60 ;

	my $rSizeX = int ($len*cv('ppc')/10*$fontSize + 2 * $xOffset) ;
	my $rSizeY = int ($fontSize + 2 * $yOffset) ;
	addToLayer ("definitions", "<g id=\"headdef\" width=\"$rSizeX\" height=\"$rSizeY\" >") ;

	if ( cv('headbackground') ne "none" ) {
		my $color = cv ('headbackground') ;
		my $svgString = "fill=\"$color\"" ;
		drawRect (0, 0, $rSizeX, $rSizeY, 0, $svgString, "definitions") ;
	}

	my $svgString = "fill=\"$col\" stroke=\"$col\" font-size=\"$fontSize\" " ;
	drawText ($xOffset, $fontSize + $yOffset, 0, $text, $svgString, "definitions") ;

	addToLayer ("definitions", "</g>") ;

	my $posX = $borderDistX ; my $posY = $borderDistY ;

	addToLayer ("header", "<use x=\"$posX\" y=\"$posY\" xlink:href=\"#headdef\" />") ;
}


sub fitsPaper {
#
# calculates on what paper size the map will fit. sizes are taken from global variables
#

	my $width = $sizeX / 300 * 2.54 ;
	my $height = $sizeY / 300 * 2.54 ;
	my $paper = "" ;

	my @sizes = () ;
	push @sizes, ["4A0", 168.2, 237.8] ;
	push @sizes, ["2A0", 118.9, 168.2] ;
	push @sizes, ["A0", 84.1, 118.9] ;
	push @sizes, ["A1", 59.4, 84.1] ;
	push @sizes, ["A2", 42, 59.4] ;
	push @sizes, ["A3", 29.7, 42] ;
	push @sizes, ["A4", 21, 29.7] ;
	push @sizes, ["A5", 14.8, 21] ;
	push @sizes, ["A6", 10.5, 14.8] ;
	push @sizes, ["A7", 7.4, 10.5] ;
	push @sizes, ["none", 0, 0] ;

	foreach my $size (@sizes) {
		if ( ( ($width<=$size->[1]) and ($height<=$size->[2]) ) or ( ($width<=$size->[2]) and ($height<=$size->[1]) ) ) {
			$paper = $size->[0] ;
		}
	}

	return ($paper, $width, $height) ;
}

sub getScale {
#
# calcs scale of map
#
	my ($dpi) = 300 ;

	my $dist = distance ($left, $bottom, $right, $bottom) ;
	my $inches = $sizeX / $dpi ;
	my $cm = $inches * 2.54 ;
	my $scale = int ( $dist / ($cm/100/1000)  ) ;
	$scale = int ($scale / 100) * 100 ;

	return ($scale) ;
}

sub gridSquare {
#
# returns grid square of given coordinates for directories
#
	my ($lon, $lat) = @_ ;

	my $parts = cv('grid') ;

	my ($x, $y) = convert ($lon, $lat) ;
	my $xi = int ($x / ($sizeX / $parts)) + 1 ;
	my $yi = int ($y / ($sizeX / $parts)) + 1 ;
	if ( ($x >= 0) and ($x <= $sizeX) and ($y >= 0) and ($y <= $sizeY) ) {
		return (chr($xi+64) . $yi) ;
	}
	else {
		return undef ;
	}
}


sub getDimensions {
	return ($sizeX, $sizeY) ;
}

# ----------------------------------------------------------------------

sub initOneways {
#
# write marker defs to svg 
#
	my $color = cv('onewaycolor') ;
	my $markerSize = cv('onewaysize') ;

	my @svgOutputDef = () ;
	push @svgOutputDef, "<marker id=\"Arrow1\"" ;
	push @svgOutputDef, "viewBox=\"0 0 10 10\" refX=\"5\" refY=\"5\"" ;
	push @svgOutputDef, "markerUnits=\"strokeWidth\"" ;
	push @svgOutputDef, "markerWidth=\"" . $markerSize . "\" markerHeight=\"" . $markerSize . "\"" ;
	push @svgOutputDef, "orient=\"auto\">" ;
	push @svgOutputDef, "<path d=\"M 0 4 L 6 4 L 6 2 L 10 5 L 6 8 L 6 6 L 0 6 Z\" fill=\"" . $color .  "\" />" ;
	push @svgOutputDef, "</marker>" ;

	foreach my $line (@svgOutputDef) {
		addToLayer ("definitions", $line) ;
	}
}

sub addOnewayArrows {
#
# adds oneway arrows to new pathes
#
	my ($wayNodesRef, $direction, $thickness, $layer) = @_ ;
	my @wayNodes = @$wayNodesRef ;
	my $minDist = cv('onewaysize') * 1.5 ;
	my ($lonRef, $latRef) = mwFile::getNodePointers() ;

	if ($direction == -1) { @wayNodes = reverse @wayNodes ; }

	# create new pathes with new nodes
	for (my $i=0; $i<scalar(@wayNodes)-1;$i++) {
		my ($x1, $y1) = convert ($$lonRef{$wayNodes[$i]}, $$latRef{$wayNodes[$i]}) ;
		my ($x2, $y2) = convert ($$lonRef{$wayNodes[$i+1]}, $$latRef{$wayNodes[$i+1]}) ;
		my $xn = ($x2+$x1) / 2 ;
		my $yn = ($y2+$y1) / 2 ;
		if (sqrt (($x2-$x1)**2+($y2-$y1)**2) > $minDist) {
			# create path
			# use path
			my $svg = "<path d=\"M $x1 $y1 L $xn $yn L $x2 $y2\" fill=\"none\" marker-mid=\"url(#Arrow1)\" />" ;
			
			addToLayer ($layer+$thickness/100, $svg) ;
		}
	}
}

# ----------------------------------------------------------------------------

sub addAreaIcon {
#
# initial collection of area icons 
#
	my $fileNameOriginal = shift ;
	# print "AREA: $fileNameOriginal\n" ;
	my $result = open (my $file, "<", $fileNameOriginal) ;
	close ($file) ;
	if ($result) {
		my ($x, $y) ;
		if (grep /.svg/, $fileNameOriginal) {
			($x, $y) = mwMisc::sizeSVG ($fileNameOriginal) ;
			if ( ($x == 0) or ($y == 0) ) { 
				$x = 32 ; $y = 32 ; 
				print "WARNING: size of file $fileNameOriginal could not be determined. Set to 32px x 32px\n" ;
			} 
		}

		if (grep /.png/, $fileNameOriginal) {
			($x, $y) = mwMisc::sizePNG ($fileNameOriginal) ;
		}

		if (!defined $areaDef{$fileNameOriginal}) {

			my $x1 = $x ; # scale area icons 
			my $y1 = $y ;
			my $fx = $x1 / $x ;
			my $fy = $y1 / $y ;
			
			# add defs to svg output
			my $defName = "A" . $areaNum ;
			# print "INFO area icon $fileNameOriginal, $defName, $x, $y --- $x1, $y1 --- $fx, $fy --- processed.\n" ;
			$areaNum++ ;

			my $svgElement = "<pattern id=\"" . $defName . "\" width=\"" . $x . "\" height=\"" . $y . "\" " ;
			$svgElement .= "patternTransform=\"translate(0,0) scale(" . $fx . "," . $fy . ")\" \n" ;
			$svgElement .= "patternUnits=\"userSpaceOnUse\">\n" ;
			$svgElement .= "  <image xlink:href=\"" . $fileNameOriginal . "\"/>\n" ;
			$svgElement .= "</pattern>\n" ;

			addToLayer ("definitions", $svgElement) ;

			$defName = "#" . $defName ;
			$areaDef{$fileNameOriginal} = $defName ;
		}
	}
	else {
		print "WARNING: area icon $fileNameOriginal not found!\n" ;
	}
}

# ----------------------------------------------------------------------------

sub createShield {
	my ($name, $targetSize) = @_ ;
	my @a = split /:/, $name ;
	my $shieldFileName = $a[1] ;
	my $shieldText = $a[2] ;

	if (! defined $createdShields{$name}) {
		open (my $file, "<", $shieldFileName) or die ("ERROR: shield definition $shieldFileName not found.\n") ;
		my @defText = <$file> ;
		close ($file) ;

		# get size
		# calc scaling
		my $sizeX = 0 ;
		my $sizeY = 0 ;
		foreach my $line (@defText) {
			if (grep /<svg/, $line) {
				($sizeY) = ( $line =~ /height=\"(\d+)px\"/ ) ;
				($sizeX) = ( $line =~ /width=\"(\d+)px\"/ ) ;
				if ( (!defined $sizeX) or (!defined $sizeY) ) {
					die "ERROR: size of shield in $shieldFileName could not be determined.\n" ;
				}
			}
		}
		if ( ($sizeX == 0) or ($sizeY == 0) ) {
			die "ERROR: initial size of shield $shieldFileName could not be determined.\n" ;
		}

		my $scaleFactor = $targetSize / $sizeY ;

		$shieldXSize{ $name } = int ($sizeX * $scaleFactor) ;
		$shieldYSize{ $name } = int ($sizeY * $scaleFactor) ;

		$shieldPathId++ ;
		my $shieldPathName = "ShieldPath" . $shieldPathId ;
		my $shieldGroupName = "ShieldGroup" . $shieldPathId ;

		foreach my $line (@defText) {
			$line =~ s/REPLACEID/$shieldGroupName/ ;
			$line =~ s/REPLACESCALE/$scaleFactor/g ;
			$line =~ s/REPLACEPATH/$shieldPathName/ ;
			$line =~ s/REPLACELABEL/$shieldText/ ;
		}

		foreach my $line (@defText) {
			addToLayer ("definitions", $line) ;
		}

		$createdShields{$name} = $shieldGroupName ;
	}
}

sub getMaxShieldSize {
	my $name = shift ;
	my $max = $shieldXSize{$name} ;
	if ( $shieldYSize{$name} > $max) { $max = $shieldYSize{$name} ; }
	return $max ;
}

sub getShieldSizes {
	my $name = shift ;
	my $x = $shieldXSize{$name} ;
	my $y = $shieldYSize{$name} ;
	return $x, $y ;
}

sub getShieldId {
	my $name = shift ;
	return $createdShields{$name} ;
}

# --------------------------------------------------------------------

sub createLegendFile {
	my ($x, $y) = @_ ;

	my $svgName = cv('out') ;
	$svgName =~ s/\.svg/\_legend\.svg/i ;	
	my $pngName = $svgName ;
	$pngName =~ s/\.svg/\.png/i ;
	my $pdfName = $svgName ;
	$pdfName =~ s/\.svg/\.pdf/i ;


	open (my $file, ">", $svgName) || die "can't open legend svg output file $svgName\n";

	print $file "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n" ;
	print $file "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\" >\n" ;

	my $w = $x / 300 * 2.54 ; # cm
	my $h = $y / 300 * 2.54 ;

	my ($svg) = "<svg version=\"1.1\" baseProfile=\"full\" xmlns=\"http://www.w3.org/2000/svg\" " ;
	$svg .= "xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" " ;
	$svg .= "width=\"$w" . "cm\" height=\"$h" . "cm\" viewBox=\"0 0 $x $y\">\n" ;
	print $file $svg ;

	print $file "<defs>\n" ;
	foreach ( @{$svgLayer{'definitions'}} ) { print $file $_, "\n" ; }
	print $file "</defs>\n" ;

	print $file "<use x=\"0\" y=\"0\" xlink:href=\"#legenddef\" />\n" ;
	print $file "</svg>\n" ;
	close $file ;

	if (cv('pdf') eq "1") {
		print "creating pdf file $pdfName ...\n" ;
		`inkscape -A $pdfName $svgName` ;
	}

	if (cv('png') eq "1") {
		my $dpi = cv('pngdpi') ;
		print "creating png file $pngName ($dpi dpi)...\n" ;
		`inkscape --export-dpi=$dpi -e $pngName $svgName` ;
	}
}

1 ;


