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


package mwWayLabel ; 

use strict ;
use warnings ;

use mwConfig ;
use mwFile ;
use mwMisc ;
use mwMap ;
use mwLabel ;
use mwOccupy ;

use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw (	addToDirectory
		getDirectory
		addWayLabel
		preprocessWayLabels
		createWayLabels
		 ) ;

my %directory = () ;
my %wayLabels = () ;
my @labelCandidates = () ;
my %ruleRefs = () ;
my $pathNumber = 0 ;

my $numWayLabelsOmitted = 0 ;
my $wnsNumber = 1 ;
my @wns =() ;


# ------------------------------------------------------------------------

sub addToDirectory {
	my ($name, $square) = @_ ;
	if ( ! defined $square ) {
		$directory { $name } = 1 ;
	}
	else {
		$directory { $name } { $square } = 1 ;
	}
}

sub getDirectory {
	return \%directory ;
}

sub addWayLabel {
#
# collect all way label data before actual labeling
#
	my ($wayId, $name, $ruleRef) = @_ ;
	push @{ $wayLabels{$ruleRef}{$name} }, $wayId ;
	$ruleRefs{$ruleRef} = $ruleRef ;
	if ( cv ('debug') eq "1" ) {
		print "AWL: $wayId, $name, $ruleRef\n" ;
	}
}

sub preprocessWayLabels {
#
# preprocess way labels collected so far
# combine ways with same rule and name
# split ways where direction in longitude changes so labels will be readable later
# store result in @labelCandidates
#

	my ($lonRef, $latRef) = getNodePointers() ;
	my ($memWayNodesRef, $memWayTagsRef) = getWayPointers() ;

	foreach my $rule (keys %wayLabels) {
		my $ruleRef = $ruleRefs{ $rule } ;
		# print "PPWL: ruleNum $rule\n" ;
		foreach my $name (keys %{$wayLabels{$rule}}) {
			my (@ways) = @{$wayLabels{$rule}{$name}} ;
			# print "PPWL:    processing name $name, " . scalar (@ways) . " ways\n" ;
			my ($waysRef, $nodesRef) = buildRings (\@ways, 0) ;
			my @segments = @$nodesRef ;
			# print "PPWL:    processing name $name, " . scalar (@segments) . " segments\n" ;

			if ( ! grep /shield/i, $name) {

				my @newSegments = () ;
				foreach my $segment (@segments) {
					my @actual = @$segment ;
					# print "PPWL: Actual segment @actual\n" ;
					my $found = 1 ;
					while ($found) {
						$found = 0 ; my $sp = 0 ;
						# look for splitting point
						LABSP: for (my $i=1; $i<$#actual; $i++) {
							if ( (($$lonRef{$actual[$i-1]} > $$lonRef{$actual[$i]}) and ($$lonRef{$actual[$i+1]} > $$lonRef{$actual[$i]})) or 
								(($$lonRef{$actual[$i-1]} < $$lonRef{$actual[$i]}) and ($$lonRef{$actual[$i+1]} < $$lonRef{$actual[$i]})) ) {
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

			}

			foreach my $segment (@segments) {
				my (@wayNodes) = @$segment ;
				my @points = () ;

				if ($$lonRef{$wayNodes[0]} > $$lonRef{$wayNodes[-1]}) {
					if ( ( ! grep /motorway/, $$ruleRef{'keyvalue'}) and ( ! grep /trunk/, $$ruleRef{'keyvalue'} ) ) {
						@wayNodes = reverse @wayNodes ;
					}
				}

				foreach my $node (@wayNodes) {
					push @points, convert ($$lonRef{$node}, $$latRef{$node}) ;
				}
				# print "PPWL:      segment @wayNodes\n" ;
				# print "PPWL:      segment @points\n" ;

				my ($segmentLengthPixels) = 0 ; 


				for (my $i=0; $i<$#wayNodes; $i++) {
					my ($x1, $y1) = convert ($$lonRef{$wayNodes[$i]}, $$latRef{$wayNodes[$i]}) ;
					my ($x2, $y2) = convert ($$lonRef{$wayNodes[$i+1]}, $$latRef{$wayNodes[$i+1]}) ;
					$segmentLengthPixels += sqrt ( ($x2-$x1)**2 + ($y2-$y1)**2 ) ;
				}
				# print "$rule, $wayIndexLabelSize\n" ;

				my $labelLengthPixels = 0 ;

				if (grep /shield/i, $$ruleRef{'label'} ) {
					$labelLengthPixels = $$ruleRef{'labelsize'} ;
					# print "PPWL: len = $labelLengthPixels\n" ;
				}
				else {
					$labelLengthPixels = length ($name) * cv('ppc') / 10 * $$ruleRef{'labelsize'} ;
				}

				# print "\nPPWL:        name $name - ppc $ppc - size $ruleArray[$wayIndexLabelSize]\n" ;
				# print "PPWL:        wayLen $segmentLengthPixels\n" ;
				# print "PPWL:        labLen $labelLengthPixels\n" ;

				push @labelCandidates, [$rule, $name, $segmentLengthPixels, $labelLengthPixels, [@points]] ;
				if ( cv('debug') eq "1") {
					print "PLC: $rule, $name, $segmentLengthPixels, $labelLengthPixels\n" ;
				}
			}
		}
	}
}

sub subWay {
#
# takes coordinates and label information and creates new way/path
# also calculates total angles / bends
#
	my ($ref, $labLen, $alignment, $position) = @_ ;
	my @coordinates = @$ref ;
	my @points ;
	my @dists ;
	my @angles = () ;

	for (my $i=0; $i < $#coordinates; $i+=2) {
		push @points, [$coordinates[$i],$coordinates[$i+1]] ;
	}

	$dists[0] = 0 ;
	my $dist = 0 ;
	if (scalar @points > 1) {
		for (my $i=1;$i<=$#points; $i++) {
			$dist = $dist + sqrt ( ($points[$i-1]->[0]-$points[$i]->[0])**2 + ($points[$i-1]->[1]-$points[$i]->[1])**2 ) ;
			$dists[$i] = $dist ;
		}			
	}

	# calc angles at nodes
	if (scalar @points > 2) {
		for (my $i=1;$i<$#points; $i++) {
			$angles[$i] = angleMapgen ($points[$i-1]->[0], $points[$i-1]->[1], $points[$i]->[0], $points[$i]->[1], $points[$i]->[0], $points[$i]->[1], $points[$i+1]->[0], $points[$i+1]->[1]) ;
		}			
	}

	my $wayLength = $dist ;
	my $refPoint = $wayLength / 100 * $position ;
	my $labelStart ; my $labelEnd ;
	if ($alignment eq "start") { # left
		$labelStart = $refPoint ;
		$labelEnd = $labelStart + $labLen ;
	}
	if ($alignment eq "end") { # right
		$labelEnd = $refPoint ;
		$labelStart = $labelEnd - $labLen ;
	}
	if ($alignment eq "middle") { # center
		$labelEnd = $refPoint + $labLen / 2 ;
		$labelStart = $refPoint - $labLen / 2 ;
	}

	# find start and end segments
	my $startSeg ; my $endSeg ;
	for (my $i=0; $i<$#points; $i++) {
		if ( ($dists[$i]<=$labelStart) and ($dists[$i+1]>=$labelStart) ) { $startSeg = $i ; }
		if ( ($dists[$i]<=$labelEnd) and ($dists[$i+1]>=$labelEnd) ) { $endSeg = $i ; }
	}

	my @finalWay = () ;
	my $finalAngle = 0 ;
	my ($sx, $sy) = triangleNode ($coordinates[$startSeg*2], $coordinates[$startSeg*2+1], $coordinates[$startSeg*2+2], $coordinates[$startSeg*2+3], $labelStart-$dists[$startSeg], 0) ;
	push @finalWay, $sx, $sy ;

	if ($startSeg != $endSeg) {
		for (my $i=$startSeg+1; $i<=$endSeg; $i++) { 
			push @finalWay, $coordinates[$i*2], $coordinates[$i*2+1] ; 
			$finalAngle += abs ($angles[$i]) ;
		}
	}

	my ($ex, $ey) = triangleNode ($coordinates[$endSeg*2], $coordinates[$endSeg*2+1], $coordinates[$endSeg*2+2], $coordinates[$endSeg*2+3], $labelEnd-$dists[$endSeg], 0) ;
	push @finalWay, $ex, $ey ;
	
	return (\@finalWay, $finalAngle) ;	
}

sub createWayLabels {
#
# finally take all way label candidates and try to label them
#

	my %wnsUnique = () ;
	print "placing way labels...\n" ;

	my %notDrawnLabels = () ;
	my %drawnLabels = () ;

	# calc ratio to label ways first where label just fits
	# these will be drawn first
	foreach my $candidate (@labelCandidates) {
		my $wLen = $candidate->[2] ;
		my $lLen = $candidate->[3] ;
		if ($wLen == 0) { $wLen = 1 ; }
		if ($lLen == 0) { $lLen = 1 ; }
		$candidate->[5] = $lLen / $wLen ;
	}
	@labelCandidates = sort { $b->[5] <=> $a->[5] } @labelCandidates ;

	foreach my $candidate (@labelCandidates) {
		my $ruleRef = $ruleRefs{ $candidate->[0] } ;
		my $name = $candidate->[1] ;
		my $wLen = $candidate->[2] ;
		my $lLen = $candidate->[3] ;
		my @points = @{$candidate->[4]} ;

		my $toLabel = 1 ;
		if ( ( cv('declutter') eq "1") and ($points[0] > $points[-2]) and 
			( ( grep /motorway/i, $$ruleRef{'keyvalue'}) or (grep /trunk/i, $$ruleRef{'keyvalue'}) ) ) {
			$toLabel = 0 ;
		}


		# wns?
		if ( ($lLen > $wLen * 0.95) and ( cv('wns') > 0 ) ) {
			if ( ( $toLabel != 0 ) and ( ! grep /shield/i, $name) and ( wayVisible( \@points ) ) ) {
				if ( ! defined $wnsUnique{$name} ) {
					my $oldName = $name ;
					$wnsUnique{$name} = 1 ;
					push @wns, [ $wnsNumber, $name] ;
					$name = $wnsNumber ;
					$lLen = cv('ppc') / 10 * $$ruleRef{'labelsize'} * length ($name) ;
					# print "WNS: $oldName - $name\n" ;
					$wnsNumber++ ;
				}
			}
		}


		if ( ($lLen > $wLen*0.95) or ($toLabel == 0) ) {
			# label too long
			$numWayLabelsOmitted++ ;
			$notDrawnLabels { $name } = 1 ;

		}
		else {

			if (grep /shield/i, $name) {

				createShield ($name, $$ruleRef{'labelsize'} ) ;

				my $shieldMaxSize = getMaxShieldSize ($name) ;

				my $numShields = int ($wLen / ($shieldMaxSize * 12) ) ;
				# if ($numShields > 4) { $numShields = 4 ; } 

				if ($numShields > 0) {
					my $step = $wLen / ($numShields + 1) ;
					my $position = $step ; 
					while ($position < $wLen) {
						my ($x, $y) = getPointOfWay (\@points, $position) ;
						# print "XY: $x, $y\n" ;

						# place shield if not occupied
			
						my ($ssx, $ssy) = getShieldSizes($name) ;

						my $x2 = int ($x - $ssx / 2) ;
						my $y2 = int ($y - $ssy / 2) ;

						# print "AREA: $x2, $y2, $x2+$lLen, $y2+$lLen\n" ;

						if ( ! mwLabel::boxAreaOccupied ($x2, $y2+$ssy, $x2+$ssx, $y2) ) {

							my $id = getShieldId ($name) ;
							addToLayer ("shields", "<use xlink:href=\"#$id\" x=\"$x2\" y=\"$y2\" />") ;

							mwLabel::boxOccupyArea ($x2, $y2+$ssy, $x2+$ssx, $y2, 0, 3) ;
						}

						$position += $step ;
					}
				}

			} # shield

			else { 

				# print "$wLen - $name - $lLen\n" ;
				my $numLabels = int ($wLen / (4 * $lLen)) ;
				if ($numLabels < 1) { $numLabels = 1 ; }
				if ($numLabels > 4) { $numLabels = 4 ; }

				if ($numLabels == 1) {
					# print "LA: $name *1*\n" ;
					my $spare = 0.95 * $wLen - $lLen ;
					my $sparePercentHalf = $spare / ($wLen*0.95) *100 / 2 ;
					my $startOffset = 50 - $sparePercentHalf ;
					my $endOffset = 50 + $sparePercentHalf ;
					# five possible positions per way
					my $step = ($endOffset - $startOffset) / 5 ;
					my @positions = () ;
					my $actual = $startOffset ;
					my $size = $$ruleRef{'labelsize'} ;
					while ($actual <= $endOffset) {
						my ($ref, $angle) = subWay (\@points, $lLen, "middle", $actual) ;
						my @way = @$ref ;
						# my ($col) = lineCrossings (\@way) ;
						my ($col) = boxLinesOccupied (\@way, $size/2) ;
						# calc quality of position. distance from middle and bend angles
						my $quality = $angle + abs (50 - $actual) ;
						if ($col == 0) { push @positions, ["middle", $actual, $quality] ; }
						$actual += $step ;
					}
					if (scalar @positions > 0) {
						$drawnLabels { $name } = 1 ;
						# sort by quality and take best one
						@positions = sort {$a->[2] <=> $b->[2]} @positions ;
						my ($pos) = shift @positions ;
						my ($ref, $angle) = subWay (\@points, $lLen, $pos->[0], $pos->[1]) ;
						my @finalWay = @$ref ;

						my $pathName = "Path" . $pathNumber ; $pathNumber++ ;
						createPath ($pathName, \@finalWay, "definitions") ;

						my $size = $$ruleRef{'labelsize'} ;
						my $color = $$ruleRef{'labelcolor'} ;
						my $font = $$ruleRef{'labelfont'} ;
						my $fontFamily = $$ruleRef{'labelfontfamily'} ;
						my $labelBold = $$ruleRef{'labelbold'} ;
						my $labelItalic = $$ruleRef{'labelitalic'} ;
						my $labelHalo = $$ruleRef{'labelhalo'} ;
						my $labelHaloColor = $$ruleRef{'labelhalocolor'} ;

						my $svgText = createTextSVG ( $fontFamily, $font, $labelBold, $labelItalic, $size, $color, $labelHalo, $labelHaloColor) ;  
						# pathText ($svgText, $name, $pathName, $$ruleRef{'labeloffset'}, $pos->[0], $pos->[1], "text") ;
						pathText ($svgText, $name, $pathName, $$ruleRef{'labeloffset'}, $pos->[0], 50, "text") ;

						boxOccupyLines (\@finalWay, $size/2, 3) ;
					}
					else {
						$numWayLabelsOmitted++ ;
					}
				}
				else { # more than one label
					# print "LA: $name *X*\n" ;
					my $labelDrawn = 0 ;
					my $interval = int (100 / ($numLabels + 1)) ;
					my @positions = () ;
					for (my $i=1; $i<=$numLabels; $i++) {
						push @positions, $i * $interval ;
					}
			
					foreach my $position (@positions) {
						my ($refFinal, $angle) = subWay (\@points, $lLen, "middle", $position) ;
						my (@finalWay) = @$refFinal ;
						# my ($collision) = lineCrossings (\@finalWay) ;

						my $size = $$ruleRef{'labelsize'} ;
						my ($collision) = boxLinesOccupied (\@finalWay, $size/2 ) ;

						if ($collision == 0) {
							$labelDrawn = 1 ;
							$drawnLabels { $name } = 1 ;
							my $pathName = "Path" . $pathNumber ; $pathNumber++ ;

							# createPath ($pathName, \@points, "definitions") ;
							createPath ($pathName, \@finalWay, "definitions") ;

							my $size = $$ruleRef{'labelsize'} ;
							my $color = $$ruleRef{'labelcolor'} ;
							my $font = $$ruleRef{'labelfont'} ;
							my $fontFamily = $$ruleRef{'labelfontfamily'} ;
							my $labelBold = $$ruleRef{'labelbold'} ;
							my $labelItalic = $$ruleRef{'labelitalic'} ;
							my $labelHalo = $$ruleRef{'labelhalo'} ;
							my $labelHaloColor = $$ruleRef{'labelhalocolor'} ;

							my $svgText = createTextSVG ( $fontFamily, $font, $labelBold, $labelItalic, $size, $color, $labelHalo, $labelHaloColor) ;  
							pathText ($svgText, $name, $pathName, $$ruleRef{'labeloffset'}, "middle", 50, "text") ;

							boxOccupyLines (\@finalWay, $size/2, 3) ;



						}
						else {
							# print "INFO: $name labeled less often than desired.\n" ;
						}
					}
					if ($labelDrawn == 0) {
						$notDrawnLabels { $name } = 1 ;
					}
				}
			}
		}
	}
	my $labelFileName = cv('out') ;
	$labelFileName =~ s/\.svg/_NotDrawnLabels.txt/ ;
	my $labelFile ;
	open ($labelFile, ">", $labelFileName) or die ("couldn't open label file $labelFileName") ;
	print $labelFile "Not drawn labels\n\n" ;
	foreach my $labelName (sort keys %notDrawnLabels) {
		if (!defined $drawnLabels { $labelName } ) {
			print $labelFile "$labelName\n" ;
		}
	}
	close ($labelFile) ;


	# way name substitutes legend?

	if ( cv('wns') > 0 ) {
		createWNSLegend() ;
	}

}

# ------------------------------------------------------------

sub createWNSLegend {
	my $size = cv('wnssize') ;	
	my $color = cv('wnscolor') ;

	# TODO max len auto size
	my $maxLen = 0 ;
	foreach my $e ( @wns ) {
		if ( length $e->[1] > $maxLen ) { $maxLen = length $e->[1] ; }
	}

	my $sy = 2 * $size ;
	my $sx = (4 + $maxLen) * $size / 10 * cv('ppc') ;
	my $tx = 4 * $size / 10 * cv('ppc') ;
	my $nx = 1 * $size / 10 * cv('ppc') ;
	my $ty = 1.5 * $size ;

	my $sizeX = $sx ;
	my $sizeY = $sy * scalar @wns ;

	# defs

	my $actualLine = 0 ;

	addToLayer ("definitions", "<g id=\"wnsdef\" width=\"$sizeX\" height=\"$sizeY\" >") ;

	# bg
	my $bg = cv('wnsbgcolor') ;
	my $svgString = "fill=\"$bg\"" ;
	drawRect (0, 0, $sizeX, $sizeY, 0, $svgString, "definitions") ;

	$svgString = createTextSVG ( cv('elementFontFamily'), cv('elementFont'), undef, undef, cv('wnssize'), cv('wnscolor'), undef, undef) ;
	foreach my $e ( @wns ) {
		my $y = $actualLine * $sy + $ty ;
		drawText ($nx, $y, 0, $e->[0], $svgString, "definitions") ;
		drawText ($tx, $y, 0, $e->[1], $svgString, "definitions") ;
		
		$actualLine++ ;
	}

	addToLayer ("definitions", "</g>") ;

	my $posX = 0 ;
	my $posY = 0 ;

	# reset some variables
	($sizeX, $sizeY) = getDimensions() ;
	$sy = $sy * scalar @wns ;

	if ( cv('wns') eq "2") {
		$posX = $sizeX - $sx ;
		$posY = 0 ;
	}

	if ( cv('wns') eq "3") {
		$posX = 0 ;
		$posY = $sizeY - $sy ;
	}

	if ( cv('wns') eq "4") {
		$posX = $sizeX - $sx ;
		$posY = $sizeY - $sy ;
	}

	if ( ( cv('wns') >=1 ) and ( cv('wns') <= 4 ) ) {
		addToLayer ("wns", "<use x=\"$posX\" y=\"$posY\" xlink:href=\"#wnsdef\" />") ;
	}

	if ( cv('wns') eq "5") {
		createLegendFile ($sx, $sy, "_wns", "#wnsdef") ;
	}
}

1 ;


