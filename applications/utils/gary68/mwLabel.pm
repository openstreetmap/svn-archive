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


package mwLabel ; 

use strict ;
use warnings ;

use mwConfig ;
use mwMap ;
# use mwMisc ;
use mwOccupy ;

my $labelPathId = 0 ;

my @lines = () ;

my $numIconsMoved = 0 ;
my $numLabels = 0 ;
my $numIcons = 0 ;
my $numLabelsOmitted = 0 ;
my $numLabelsMoved = 0 ;
my $numIconsOmitted = 0 ;

my %poiHash = () ;

use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw ( 
			placeLabelAndIcon
			addToPoiHash
			getPoiHash
		 ) ;




sub placeLabelAndIcon {
#
# intelligent icon and label placement alg.
#
	my ($lon, $lat, $offset, $thickness, $text, $svgText, $icon, $iconSizeX, $iconSizeY, $layer) = @_ ;

	if (cv('debug') eq "1") { print "PLAI: $lon, $lat, $offset, $thickness, $text, $svgText, $icon, $iconSizeX, $iconSizeY, $layer\n" ; }

	my ($x, $y) = mwMap::convert ($lon, $lat) ; # center !

	if ( ! coordsOut ($x, $y) ) {

		$y = $y + $offset ;

		my ($ref) = splitLabel ($text) ;
		my (@lines) = @$ref ;
		my $numLines = scalar @lines ;
		my $maxTextLenPix = 0 ;
		my $orientation = "" ;
		my $lineDist = cv ('linedist') ; ;
		my $tries = 0 ;
		my $allowIconMove = cv ('allowiconmove') ;

		my ($textSize) = ( $svgText =~ /font-size=\"(\d+)\"/ ) ;
		if ( ! defined $textSize ) { die ("ERROR: font size could not be determined from svg format string \"$svgText\"\n") ; }

		foreach my $line (@lines) {
			my $len = length ($line) * cv('ppc') / 10 * $textSize ; # in pixels
			if ($len > $maxTextLenPix) { $maxTextLenPix = $len ; }
		}
		my $spaceTextX = $maxTextLenPix ;
		my $spaceTextY = $numLines * ($lineDist+$textSize) ;


		if ($icon ne "none") {
			$numIcons++ ;
			# space for icon?
				my $sizeX1 = $iconSizeX ; if ($sizeX1 == 0) { $sizeX1 = 20 ; }
				my $sizeY1 = $iconSizeY ; if ($sizeY1 == 0) { $sizeY1 = 20 ; }
				my $iconX = $x - $sizeX1/2 ; # top left corner
				my $iconY = $y - $sizeY1/2 ; 

				my @shifts = (0) ;
				if ($allowIconMove eq "1") {
					@shifts = ( 0, -15, 15 ) ;
				}
				my $posFound = 0 ; my $posCount = 0 ;
				my ($iconAreaX1, $iconAreaY1, $iconAreaX2, $iconAreaY2) ;
				LABAB: foreach my $xShift (@shifts) {
					foreach my $yShift (@shifts) {
						$posCount++ ;
						if ( ( ! boxAreaOccupied ($iconX+$xShift, $iconY+$sizeY1+$yShift, $iconX+$sizeX1+$xShift, $iconY+$yShift) ) or ( cv('forcenodes') eq "1" )  ) {
							placeIcon ($iconX+$xShift, $iconY+$yShift, $icon, $sizeX1, $sizeY1, "nodes") ;
							$iconAreaX1 = $iconX+$xShift ;
							$iconAreaY1 = $iconY+$sizeY1+$yShift ;
							$iconAreaX2 = $iconX+$sizeX1+$xShift ;
							$iconAreaY2 = $iconY+$yShift ;

							$posFound = 1 ;
							if ($posCount > 1) { $numIconsMoved++ ; }
							$iconX = $iconX + $xShift ; # for later use with label
							$iconY = $iconY + $yShift ;
							last LABAB ;
						}
					}
				}
				if ($posFound == 1) {

					# label text?
					if ($text ne "") {
						$numLabels++ ;


						$sizeX1 += 1 ; $sizeY1 += 1 ;

						my ($x1, $x2, $y1, $y2) ;
						# $x, $y centered 
						# yes, check if space for label, choose position, draw
						# no, count omitted text

						my @positions = () ; my $positionFound = 0 ;
						# pos 1 centered below
						$x1 = $x - $spaceTextX/2 ; $x2 = $x + $spaceTextX/2 ; $y1 = $y + $sizeY1/2 + $spaceTextY ; $y2 = $y + $sizeY1/2 ; $orientation = "centered" ; 
						push @positions, [$x1, $x2, $y1, $y2, $orientation] ;

						# pos 2/3 to the right, bottom, top
						$x1 = $x + $sizeX1/2 ; $x2 = $x + $sizeX1/2 + $spaceTextX ; $y1 = $y + $sizeY1/2 ; $y2 = $y1 - $spaceTextY ; $orientation = "left" ; 
						push @positions, [$x1, $x2, $y1, $y2, $orientation] ;
						$x1 = $x + $sizeX1/2 ; $x2 = $x + $sizeX1/2 + $spaceTextX ; $y2 = $y - $sizeY1/2 ; $y1 = $y2 + $spaceTextY ; $orientation = "left" ; 
						push @positions, [$x1, $x2, $y1, $y2, $orientation] ;

						# pos 4 centered upon
						$x1 = $x - $spaceTextX/2 ; $x2 = $x + $spaceTextX/2 ; $y1 = $y - $sizeY1/2 ; $y2 = $y - $sizeY1/2 - $spaceTextY ; $orientation = "centered" ; 
						push @positions, [$x1, $x2, $y1, $y2, $orientation] ;

						# pos 5/6 to the right, below and upon
						$x1 = $x + $sizeX1/2 ; $x2 = $x + $sizeX1/2 + $spaceTextX ; $y2 = $y + $sizeY1/2 ; $y1 = $y2 + $spaceTextY ; $orientation = "left" ; 
						push @positions, [$x1, $x2, $y1, $y2, $orientation] ;
						$x1 = $x + $sizeX1/2 ; $x2 = $x + $sizeX1/2 + $spaceTextX ; $y1 = $y - $sizeY1/2 ; $y2 = $y1 - $spaceTextY ; $orientation = "left" ; 
						push @positions, [$x1, $x2, $y1, $y2, $orientation] ;

						# left normal, bottom, top
						$x1 = $x - $sizeX1/2 - $spaceTextX ; $x2 = $x - $sizeX1/2 ; $y1 = $y + $sizeY1/2 ; $y2 = $y1 - $spaceTextY ; $orientation = "right" ; 
						push @positions, [$x1, $x2, $y1, $y2, $orientation] ;
						$x1 = $x - $sizeX1/2 - $spaceTextX ; $x2 = $x - $sizeX1/2 ; $y2 = $y - $sizeY1/2 ; $y1 = $y2 + $spaceTextY ; $orientation = "right" ; 
						push @positions, [$x1, $x2, $y1, $y2, $orientation] ;

						# left corners, bottom, top
						$x1 = $x - $sizeX1/2 - $spaceTextX ; $x2 = $x - $sizeX1/2 ; $y2 = $y + $sizeY1/2 ; $y1 = $y2 + $spaceTextY ; $orientation = "right" ; 
						push @positions, [$x1, $x2, $y1, $y2, $orientation] ;
						$x1 = $x - $sizeX1/2 - $spaceTextX ; $x2 = $x - $sizeX1/2 ; $y1 = $y - $sizeY1/2 ; $y2 = $y1 - $spaceTextY ; $orientation = "right" ; 
						push @positions, [$x1, $x2, $y1, $y2, $orientation] ;


						$tries = 0 ;
						LABB: foreach my $pos (@positions) {
							$tries++ ;

							$positionFound = checkAndDrawText ($pos->[0], $pos->[1], $pos->[2], $pos->[3], $pos->[4], \@lines, $svgText, $layer) ;

							if ($positionFound == 1) {
								last LABB ;
							}
						}
						if ($positionFound == 0) { $numLabelsOmitted++ ; }
						if ($tries > 1) { $numLabelsMoved++ ; }
					} # label

					boxOccupyArea ($iconAreaX1, $iconAreaY1, $iconAreaX2, $iconAreaY2, 0, 2) ;
				} # pos found
				else {
					# no, count omitted
					$numIconsOmitted++ ;
				}
		}
		else { # only text
			my ($x1, $x2, $y1, $y2) ;
			# x1, x2, y1, y2
			# left, right, bottom, top		
			# choose space for text, draw
			# count omitted

			$numLabels++ ;
			my @positions = () ;
			$x1 = $x + $thickness ; $x2 = $x + $thickness + $spaceTextX ; $y1 = $y ; $y2 = $y - $spaceTextY ; $orientation = "left" ; 
			push @positions, [$x1, $x2, $y1, $y2, $orientation] ;
			$x1 = $x + $thickness ; $x2 = $x + $thickness + $spaceTextX ; $y1 = $y + $spaceTextY ; $y2 = $y ; $orientation = "left" ; 
			push @positions, [$x1, $x2, $y1, $y2, $orientation] ;

			$x1 = $x - ($thickness + $spaceTextX) ; $x2 = $x - $thickness ; $y1 = $y ; $y2 = $y - $spaceTextY ; $orientation = "right" ; 
			push @positions, [$x1, $x2, $y1, $y2, $orientation] ;
			$x1 = $x - ($thickness + $spaceTextX) ; $x2 = $x - $thickness ; $y1 = $y ; $y2 = $y - $spaceTextY ; $orientation = "right" ; 
			push @positions, [$x1, $x2, $y1, $y2, $orientation] ;

			$x1 = $x - $spaceTextX/2 ; $x2 = $x + $spaceTextX/2 ; $y1 = $y - $thickness ; $y2 = $y - ($thickness + $spaceTextY) ; $orientation = "centered" ; 
			push @positions, [$x1, $x2, $y1, $y2, $orientation] ;
			$x1 = $x - $spaceTextX/2 ; $x2 = $x + $spaceTextX/2 ; $y1 = $y + $thickness + $spaceTextY ; $y2 = $y + $thickness ; $orientation = "centered" ; 
			push @positions, [$x1, $x2, $y1, $y2, $orientation] ;

			my $positionFound = 0 ;
			$tries = 0 ;
			LABA: foreach my $pos (@positions) {
				$tries++ ;
				# print "$lines[0]   $pos->[0], $pos->[1], $pos->[2], $pos->[3], $pos->[4], $numLines\n" ;

				$positionFound = checkAndDrawText ($pos->[0], $pos->[1], $pos->[2], $pos->[3], $pos->[4], \@lines, $svgText, $layer) ;

				if ($positionFound == 1) {
					last LABA ;
				}
			}
			if ($positionFound == 0) { $numLabelsOmitted++ ; }
			if ($tries > 1) { $numLabelsMoved++ ; }
		}
	}
}


sub checkAndDrawText {
#
# checks if area available and if so draws text
#
	my ($x1, $x2, $y1, $y2, $orientation, $refLines, $svgText, $layer) = @_ ;

	if (cv('debug') eq "1") { print "CADT: $x1, $x2, $y1, $y2, $orientation, $refLines, $svgText, $layer\n" ; }

	my @lines = @$refLines ;
	my $numLines = scalar @lines ;
	my $lineDist = cv ('linedist') ;

	my ($size) = ( $svgText =~ /font-size=\"(\d+)\"/ ) ;
	if ( ! defined $size ) { die ("ERROR: font size could not be determined from svg format string \"$svgText\"\n") ; }


	# WATCH for variable sequence!
	if ( 
	( ! boxAreaOccupied ($x1, $y1, $x2, $y2) ) or 
	( cv('forcenodes') eq "1" ) 
	)  {

		for (my $i=0; $i<=$#lines; $i++) {

			my @points = ($x1, $y2+($i+1)*($size+$lineDist), $x2, $y2+($i+1)*($size+$lineDist)) ;
			my $pathName = "LabelPath" . $labelPathId ; 
			$labelPathId++ ;
			createPath ($pathName, \@points, "definitions") ;

			if ($orientation eq "centered") {
				pathText ($svgText, $lines[$i], $pathName, 0, "middle", 50, $layer)
			}
			if ($orientation eq "left") {
				pathText ($svgText, $lines[$i], $pathName, 0, "start", 0, $layer)
			}
			if ($orientation eq "right") {
				pathText ($svgText, $lines[$i], $pathName, 0, "end", 100, $layer)
			}
		}

		boxOccupyArea ($x1, $y1, $x2, $y2, 0, 2) ;
		
		return (1) ;
	}
	else {
		return 0 ;
	}
}

sub splitLabel {
#
# split label text at space locations and then merge new parts if new part will be smaller than XX chars
#
	my $text = shift ;
	my @lines = split / /, $text ;
	my $merged = 1 ;
	while ($merged) {
		$merged = 0 ;
		LAB2: for (my $i=0; $i<$#lines; $i++) {
			if (length ($lines[$i] . " " . $lines[$i+1]) <= cv ('maxcharperline') ) {	
				$lines[$i] = $lines[$i] . " " . $lines[$i+1] ;
				splice (@lines, $i+1, 1) ;
				$merged = 1 ;
				last LAB2 ;
			}
		}
	}
	return (\@lines) ;
}




# ------------------------------------------------------------

sub addToPoiHash {
	my ($name, $sq) = @_ ;
	if (defined $sq) {
		$poiHash{$name}{$sq} = 1 ;
	}
	else {
		$poiHash{$name} = 1 ;
	}
}


sub getPoiHash {
	return \%poiHash ;
}


1 ;


