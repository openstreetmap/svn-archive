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
		 ) ;

my $areasOmitted = 0 ;
my $areasDrawn = 0 ;

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

			if ( ( $$ruleRef{'svgstringtop'} ne "" ) or ( $$ruleRef{'svgstringbottom'} ne "" ) ) {
				# TODO individual (NEEDS sizes anyway!!! for layers... or automatic?)
			}
			else {

				# top (actual way)
				my $size = $$ruleRef{'size'} ;
				my $color = $$ruleRef{'color'} ;
				my $svgString = "stroke=\"$color\" stroke-width=\"$size\" stroke-linecap=\"round\" fill=\"none\" stroke-linejoin=\"round\"" ;
				drawWay ( \@nodes, 1, $svgString, undef, $layer + $size/100 ) ;

				# bottom (border)
				$size = 2 * $$ruleRef{'bordersize'} + $$ruleRef{'size'} ;
				$color = $$ruleRef{'bordercolor'} ;
				$svgString = "stroke=\"$color\" stroke-width=\"$size\" stroke-linecap=\"round\" fill=\"none\" stroke-linejoin=\"round\"" ;
				drawWay ( \@nodes, 1, $svgString, undef, $layer -0.3 + $size/100 ) ;
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
				}
				else {
					($name, $ref1) = createLabel (\@tags, $$ruleRef{'label'}, 0, 0) ;
					@names = @$ref1 ;
				}

				if ( ( cv('nolabel') eq "1") and ($name eq "") ) { $name = "NO LABEL" ; }

				if ($name ne "") { 
					addWayLabel ($wayId, $name, $ruleRef) ; 
				}
				if ( cv('dir') eq "1") {
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

			if ($svgString eq "") {
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
			}
			else {
				$areasOmitted++ ;
			}

		} # Area
	}

	print "$areasDrawn areas drawn, $areasOmitted omitted because they are too small\n" ;

	my $cw = scalar @coastWays ;
	if ( cv('verbose')) { print "$cw coast line ways found.\n" ; }
 
	preprocessWayLabels() ;
	createWayLabels() ;

	if ($cw > 0) {
		processCoastLines (\@coastWays) ;
	}
}

1 ;


