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


package mwNodes ; 

use strict ;
use warnings ;

use mwConfig ;
use mwFile ;
use mwRules ;
use mwMap ;
use mwMisc ;
use mwLabel ;

use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw ( 	processNodes 
			createPoiDirectory
		 ) ;


sub processNodes {

	print "drawing nodes...\n" ;

	my $lonRef; my $latRef; my $tagRef ;
	($lonRef, $latRef, $tagRef) = getNodePointers () ;

	foreach my $nodeId (keys %$lonRef) {
		my @tags = @{ $$tagRef{$nodeId} } ;
		my $tagsString = "" ;

		my $ruleRef = getNodeRule (\@tags) ;
		if (defined $ruleRef) {
			# foreach my $t (@tags) { $tagsString .= $t->[0] . "=" . $t->[1] . " " ; }
			# print "$nodeId $tagsString\n" ;
			# print "rule found\n" ;
			# foreach my $prop (keys %$ruleRef) {
			#	print "$prop=$$ruleRef{$prop}\n" ;
			# }
			# print "\n" ;

			# draw disc first !
			if (grep /yes/, $$ruleRef{'disc'})  {
				my $svgString = "" ;
				if ( $$ruleRef{'discsvgstring'} ne "" ) {
					$svgString = $$ruleRef{'discsvgstring'} ;
				}
				else {
					$svgString = "fill=\"$$ruleRef{'disccolor'}\" stroke=\"none\" fill-opacity=\"$$ruleRef{'discopacity'}\"" ;
				}
				drawCircle ($$lonRef{$nodeId}, $$latRef{$nodeId}, 1, $$ruleRef{'discradius'}, 1, $svgString, 'nodes') ;
			}

			if (grep /yes/, $$ruleRef{'circle'})  {
				my $svgString = "" ;
				if ( $$ruleRef{'circlesvgstring'} ne "" ) {
					$svgString = $$ruleRef{'circlesvgstring'} ;
				}
				else {
					$svgString = "fill=\"none\" stroke=\"$$ruleRef{'circlecolor'}\" stroke-width=\"$$ruleRef{'circlethickness'}\"" ;
				}
				drawCircle ($$lonRef{$nodeId}, $$latRef{$nodeId}, 1, $$ruleRef{'circleradius'}, 1, $svgString, 'nodes') ;
			}

			if ( ($$ruleRef{'size'} > 0) and ($$ruleRef{'icon'} eq "none") )  {
				my $svgString = "" ;
				if ( $$ruleRef{'svgstring'} ne "" ) {
					$svgString = $$ruleRef{'svgstring'} ;
				}
				else {
					$svgString = "fill=\"$$ruleRef{'color'}\"" ;
				}

				if ( $$ruleRef{'shape'} eq "circle") {
					drawCircle ($$lonRef{$nodeId}, $$latRef{$nodeId}, 1, $$ruleRef{'size'}, 0, $svgString, 'nodes') ;
				}
				elsif ( $$ruleRef{'shape'} eq "square") {
					drawSquare ($$lonRef{$nodeId}, $$latRef{$nodeId}, 1, $$ruleRef{'size'}, 0, $svgString, 'nodes') ;
				}
				elsif ( $$ruleRef{'shape'} eq "triangle") {
					drawTriangle ($$lonRef{$nodeId}, $$latRef{$nodeId}, 1, $$ruleRef{'size'}, 0, $svgString, 'nodes') ;
				}
				elsif ( $$ruleRef{'shape'} eq "diamond") {
					drawDiamond ($$lonRef{$nodeId}, $$latRef{$nodeId}, 1, $$ruleRef{'size'}, 0, $svgString, 'nodes') ;
				}
			}

			if ( ($$ruleRef{'label'} ne "none") or ($$ruleRef{'icon'} ne "none") ) {
				my ($labelText, $ref) = createLabel (\@tags, $$ruleRef{'label'}, $$lonRef{$nodeId}, $$latRef{$nodeId}) ;
				my $labelSize = $$ruleRef{'labelsize'} ;
				my $icon = $$ruleRef{'icon'} ;
				my $iconSize = $$ruleRef{'iconsize'} ;
				my $svgText = "font-size=\"$labelSize\"" ;

				placeLabelAndIcon($$lonRef{$nodeId}, $$latRef{$nodeId}, 0, $$ruleRef{'size'}, $labelText, $svgText, $icon, $iconSize, $iconSize, "nodes") ;
			}

			# fill poi directory

			my $thing0 = $$ruleRef{'keyvalue'} ;
			my ($thing) = ( $thing0 =~ /.+=(.+)/ ) ;

			my $dirName = getValue ("name", $$tagRef{$nodeId} ) ;
			if ( 	( cv('poi') eq "1" ) and 
				( defined $dirName ) and 
				( $$ruleRef{'direxclude'} eq "no") 
			) {
				$dirName .=  " ($thing)" ;
				if ( cv('grid') > 0) {
					my $sq = gridSquare($$lonRef{$nodeId}, $$latRef{$nodeId}, cv('grid')) ;
					if (defined $sq) {
						addToPoiHash ($dirName, $sq) ;
					}
				}
				else {
					# $poiHash{$dirName} = 1 ;
					addToPoiHash ($dirName, undef) ;
				}
			}
			

		} # defined ruleref
	}

}

# ------------------------------------------------------------------------------------

sub createPoiDirectory {
	my $poiName ;
	my $poiFile ;
	$poiName = cv ('out')  ;
	$poiName =~ s/\.svg/\_pois.txt/ ;
	setConfigValue("poiname", $poiName) ;
	print "creating poi file $poiName ...\n" ;
	open ($poiFile, ">", $poiName) or die ("can't open poi file $poiName\n") ;

	my $ref = getPoiHash() ;
	my %poiHash = %$ref ;

	if ( cv('grid') eq "0") {
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

1 ;


