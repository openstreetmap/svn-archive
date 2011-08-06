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


package mwWays ; 

use strict ;
use warnings ;

use OSM::osm 8.3 ;

use mwConfig ;
use mwFile ;
use mwRules ;
use mwMap ;
use mwMisc ;
use mwWayLabel ;
use mwCoastLines ;

use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw ( 	processWays 
		getCoastWays
		createDirectory
		 ) ;

my $areasOmitted = 0 ;
my $areasDrawn = 0 ;

my $areaLabels = 0 ;
my $areaLabelsOmitted = 0 ;

my @coastWays = () ;

sub processWays {

	print "drawing ways/areas...\n" ;

	my $nodesRef; my $tagRef ;
	($nodesRef, $tagRef) = getWayPointers () ;
	my ($lonRef, $latRef, $nodeTagRef) = getNodePointers() ;

	foreach my $wayId (keys %$nodesRef) {
		my @tags = @{ $$tagRef{$wayId} } ;
		my $tagsString = "" ;

		# coast
		my $v = getValue ("natural", \@tags) ;
		if ( (defined $v) and ($v eq "coastline") ) {
			push @coastWays, $wayId ;
		} 

		# WAYS

		my $ruleRef = getWayRule (\@tags) ;
		if (defined $ruleRef) {
			my @nodes = @{ $$nodesRef{ $wayId } } ;

			my $layer = getValue ("layer", $$tagRef{$wayId}) ;
			if ( ! defined $layer ) { $layer = 0 ; }

			# TODO check for numeric!!!

			my $direction = 0 ;
			my $ow = getValue("oneway", $$tagRef{$wayId}) ;
			if (defined $ow) {
				if (($ow eq "yes") or ($ow eq "true") or ($ow eq "1")) { $direction = 1 ; }
				if ($ow eq "-1") { $direction = -1 ; }
			}

			my $bridge = getValue("bridge", $$tagRef{$wayId}) ;
			if (defined $bridge) {
				if (($bridge eq "yes") or ($bridge eq "true")) { $bridge = 1 ; } else { $bridge = 0 ; }
			}
			else { $bridge = 0 ; }

			my $tunnel = getValue("tunnel", $$tagRef{$wayId}) ;
			if (defined $tunnel) {
				if (($tunnel eq "yes") or ($tunnel eq "true")) { $tunnel = 1 ; } else { $tunnel = 0 ; }
			}
			else { $tunnel = 0 ; }

			my ($svg1, $layer1, $svg2, $layer2) = createWayParameters ($ruleRef, $layer, $bridge, $tunnel) ;

			drawWay ( \@nodes, 1, $svg1, undef, $layer1 ) ;
			if ($svg2 ne "") {
				drawWay ( \@nodes, 1, $svg2, undef, $layer2 ) ;
			}

			my $size = $$ruleRef{'size'} ;
			if ( ( cv('oneways') eq "1" ) and ($direction != 0) ) {
				addOnewayArrows (\@nodes, $direction, $size, $layer) ;
			}


			# LABEL WAY

			if ($$ruleRef{'label'} ne "none") {

				my $name = "" ; my $ref1 ; my @names ;

				if (grep /shield/i, $$ruleRef{'label'} ) {
					($name, $ref1) = createLabel (\@tags, "ref",0, 0) ;
					my $ref = $name ;

					if (grep /;/, $ref) {
						my @a = split /;/, $ref ;
						$ref = $a[0] ; 
					}

					if ($ref ne "") {
						@names = ($ref) ;
						$name = $$ruleRef{'label'} . ":$ref" ;
						# print "DRAW WAY: name set to $name\n" ;
					}
					else {
						@names = () ;
						$name = "" ;
					}

					# print "WAY: name for shield >$name<\n" ;
				}
				else {
					($name, $ref1) = createLabel (\@tags, $$ruleRef{'label'}, 0, 0) ;
					@names = @$ref1 ;
				}

				if ( ( cv('nolabel') eq "1") and ($name eq "") ) { $name = "NO LABEL" ; }

				if ($name ne "") { 
					addWayLabel ($wayId, $name, $ruleRef) ; 
				}
				if ( ( cv('dir') eq "1") and ( $$ruleRef{'direxclude'} eq "no") ) {
					if ( cv('grid') > 0) {
						foreach my $node ( @nodes ) {
							foreach my $name (@names) {
								my $sq = gridSquare($$lonRef{$node}, $$latRef{$node}, cv('grid') ) ;
								if (defined $sq) {
									addToDirectory($name, $sq) ;
								}
							}
						}
					}
					else {
						foreach my $name (@names) {
							addToDirectory ($name, undef) ;
						}
					}
				}
			}  # label

		}

		# AREAS

		$ruleRef = getAreaRule (\@tags) ;
		if (defined $ruleRef) {
			my $color = $$ruleRef{'color'} ;
			my $icon = $$ruleRef{'icon'} ;
			my $base = $$ruleRef{'base'} ;
			my $svgString = $$ruleRef{'svgstring'} ;
			my @nodes = @{ $$nodesRef{ $wayId } } ;
			my $size = areaSize (\@nodes) ;
			my @ways = [@nodes] ;

			if ( ($svgString eq "") and ($icon eq "none") ) {
				$svgString = "fill=\"$color\" " ;
			}

			if ($size > cv('minareasize') ) {
				if ($base eq "yes") {
					drawArea ($svgString, $icon, \@ways, 1, "base") ;
				}
				else {
					drawArea ($svgString, $icon, \@ways, 1, "area") ;
				}
				$areasDrawn++ ;


				# DRAW label
				if ( $$ruleRef{'label'} ne "none" )  {
					$areaLabels++ ;
					if ($size > cv('minarealabelsize') ) {
						# text
						my ($name, $ref1) = createLabel (\@tags, $$ruleRef{'label'},0, 0) ;
					
						# pos
						my ($lon, $lat) = areaCenter ( $$nodesRef{$wayId} ) ;

						# draw
						my $labelFont = $$ruleRef{'labelfont'} ;
						my $labelFontFamily = $$ruleRef{'labelfontfamily'} ;
						my $labelSize = $$ruleRef{'labelsize'} ;
						my $color = $$ruleRef{'labelcolor'} ;

						my $svgText = createTextSVG ( $labelFontFamily, $labelFont, $labelSize, $color, undef, undef) ;  

						mwLabel::placeLabelAndIcon ($lon, $lat, 0, 0, $name, $svgText, "none", 0, 0, "arealabels") ;
					}
					else {
						$areaLabelsOmitted++ ;
					}
				}


			}
			else {
				$areasOmitted++ ;
			}

		} # Area
	}

	print "$areasDrawn areas drawn, $areasOmitted omitted because they are too small\n" ;
	print "$areaLabels area labels total, $areaLabelsOmitted omitted because belonging areas were too small\n" ;

	my $cw = scalar @coastWays ;
	if ( cv('verbose')) { print "$cw coast line ways found.\n" ; }
 
	preprocessWayLabels() ;
	createWayLabels() ;

	if ($cw > 0) {
		processCoastLines (\@coastWays) ;
	}
}

# ----------------------------------------------------------------------------

sub createWayParameters {
	my ($ruleRef, $layer, $bridge, $tunnel) = @_ ;

	my $svg1 = "" ; my $layer1 = 0 ;
	my $svg2 = "" ; my $layer2 = 0 ;

	my %dashDefinition = () ;
	@{$dashDefinition {1} } = ("round", "20,20") ;
	@{$dashDefinition {2} } = ("round", "44,20") ;
	@{$dashDefinition {3} } = ("round", "28,20") ;
	@{$dashDefinition {4} } = ("round", "12,20") ;

	@{$dashDefinition {10} } = ("round", "8,8") ;
	@{$dashDefinition {11} } = ("round", "16,16") ;
	@{$dashDefinition {12} } = ("round", "24,24") ;
	@{$dashDefinition {13} } = ("round", "32,32") ;
	@{$dashDefinition {14} } = ("round", "40,40") ;

	@{$dashDefinition {20} } = ("round", "0,8,0,16") ;
	@{$dashDefinition {21} } = ("round", "0,16,0,32") ;
	@{$dashDefinition {22} } = ("round", "0,24,0,48") ;
	@{$dashDefinition {23} } = ("round", "0,32,0,48") ;

	@{$dashDefinition {30} } = ("butt", "4,4") ;
	@{$dashDefinition {31} } = ("butt", "8,8") ;
	@{$dashDefinition {32} } = ("butt", "12,12") ;
	@{$dashDefinition {33} } = ("butt", "4,12") ;
	@{$dashDefinition {34} } = ("butt", "4,20") ;
	@{$dashDefinition {35} } = ("butt", "8,20") ;

	if ( cv ('autobridge') eq "0" ) {
		$layer = 0 ;
	}

	if ( ( $$ruleRef{'svgstringtop'} ne "" ) or ( $$ruleRef{'svgstringbottom'} ne "" ) ) {

		$svg1 = $$ruleRef{'svgstringtop'} ;
		$svg2 = $$ruleRef{'svgstringbottom'} ;

		# TODO layer
		$layer1 = $layer ;
		$layer2 = $layer ;

	}
	else {

		my $size = $$ruleRef{'size'} ;
		my $color = $$ruleRef{'color'} ;

		my $lc = "round" ;
		my $lj = "round" ;

		my $dash = "" ;
		if ( $$ruleRef{'dash'} ne "" ) {
			if ( ! grep /,/, $$ruleRef{'dash'}) {
				my @ds = @{$dashDefinition{ $$ruleRef{'dash'} } } ;
				$lc = $ds[0] ;
				my $style = $ds[1] ;
				$dash = "stroke-dasharray=\"$style\" " ;
			}
			else {
				$lc = $$ruleRef{'dashcap'} ;
				my $style = $$ruleRef{'dash'} ;
				$dash = "stroke-dasharray=\"$style\"" ;
			}
		}

		# top (actual way)
		$svg1 = "stroke=\"$color\" stroke-width=\"$size\" stroke-linecap=\"$lc\" fill=\"none\" stroke-linejoin=\"$lj\" " . $dash ;
		$layer1 = $layer + $size / 100 ;

		my $bs = $$ruleRef{'bordersize'} ;
		$lc = "round" ;
		$dash = "" ;

		if ( cv ('autobridge') eq "1" ) {
			# TODO bridge/tunnel
			if ( $bridge == 1) {
				$lc = "butt" ;
				$bs += 3 ; # TODO config value
			}
			elsif ( $tunnel == 1) {
				$lc = "butt" ;
				$dash = "stroke-dasharray=\"10,10\" " ;
				$bs += 3 ; 
			}
		}

		# bottom (border)
		if ( $bs > 0 ) {
			$size = 2 * $bs + $$ruleRef{'size'} ;
			$color = $$ruleRef{'bordercolor'} ;
			$svg2 = "stroke=\"$color\" stroke-width=\"$size\" stroke-linecap=\"$lc\" fill=\"none\" stroke-linejoin=\"$lj\" " . $dash ;
			$layer2 = $layer - 0.3 + $size / 100 ;
		}
		else {
			$svg2 = "" ;
			$layer2 = 0 ;
		}

	}

	return ($svg1, $layer1, $svg2, $layer2) ;
}

# ---------------------------------------------------------------------------------

sub createDirectory {
	my $directoryName ;
	my $dirFile ;
	$directoryName = cv ('out') ;
	$directoryName =~ s/\.svg/\_streets.txt/ ;
	setConfigValue("directoryname", $directoryName) ;
	print "creating dir file $directoryName ...\n" ;
	open ($dirFile, ">", $directoryName) or die ("can't open dir file $directoryName\n") ;

	my $ref = getDirectory() ;
	my %directory = %$ref ;

	if ( cv('grid') eq "0") {
		foreach my $street (sort keys %directory) {
			$street = replaceHTMLCode ( $street ) ;
			print $dirFile "$street\n" ;
		}
	}
	else {
		foreach my $street (sort keys %directory) {
			$street = replaceHTMLCode ( $street ) ;
			print $dirFile "$street\t" ;
			foreach my $square (sort keys %{$directory{$street}}) {
				print $dirFile "$square " ;
			}
			print $dirFile "\n" ;
		}
	}
	close ($dirFile) ;
}

1 ;


