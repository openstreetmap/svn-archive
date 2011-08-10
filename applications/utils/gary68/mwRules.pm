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


package mwRules ; 

use strict ;
use warnings ;

use mwConfig ;
use mwMap ;
use mwMisc ;

use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw ( 	readRules
			getNodeRule
			printNodeRules
			getWayRule
			printWayRules
			getAreaRule
			printAreaRules
			printValidObjectProperties
			getRouteColors
			getRouteRule
			printRouteRules
			adaptRuleSizes
			createLegend
		 ) ;

my @validNodeProperties = (	
			["keyValue","key and value like [amenity=hospital]"],
			["color","color of node i.e. [black]"],
			["size","size of node i.e. [50]"],
			["shape","shape of node [circle|disc|triangle|diamond|rectangle]"],
			["svgString","svg format of shape [valid svg string]"],
			["circle","add a circle to the node [yes|no]"],
			["circleColor","color of the circle i.e. [blue]"],
			["circleRadius","circle radius in meters i.e. [1000]"],
			["circleThickness","thickness of the circle i.e. [5]"],
			["circleSVGString","format of the circle []"],
			["disc","add a disc to the node [yes|no]"],
			["discColor","color of the disc i.e. [green]"],
			["discOpacity","opacity of the disc [0..100]"],
			["discRadius","radius of disc in meters i.e. [5000]"],
			["discSVGString","format of the disc []"],
			["label","label for the node like [name|ref]"],
			["labelColor","color for label text i.e. [white]"],
			["labelSize","size of label text i.e. [20]"],
			["labelFont","font for label"],
			["labelFontFamily","font family for label"],
			["labelOffset","distance of label to node i.e. [10]"],
			["labelBold","bold font for label"],
			["labelItalic","italic font for label"],
			["labelHalo","halo for label, width in pixels"],
			["labelHaloColor","color for halo"],
			["labelTransform","perl code for label name transformation"],
			["legend","is this object to be listed in map legend? [yes|no]"],
			["legendLabel","label text of object in legend i.e. [city]"],
			["icon","icon to use for node, overrides shape i.e. [icondir/icon.svg]"],
			["iconSize","size of the icon i.e. [40]"],
			["shieldName","NOT YET IMPLEMENTED"],
			["shieldSize","NOT YET IMPLEMENTED"],
			["shieldLabel","NOT YET IMPLEMENTED"],
			["fromScale","rule will only applied if scale is bigger than fromScale i.e. [5000]"],
			["toScale","rule will only applied if scale is lower than fromScale i.e. [25000]"],
			["direxclude","should these objects be excluded from directory? [yes|no]"]
					) ;

my @validWayProperties =  (	
			["keyValue","key and value like [highway=residential]"],
			["color","color for the way i.e. [gray]"],
			["size","size of the way i.e. [15]"],
			["dash","svg dash array for the way i.e. [20,20]; old mapgen values are also possible"],
			["dashCap","linecap shape for dashes like [butt|round|square]"],
			["borderColor","color of the border of the way i.e. [black]"],
			["borderSize","thickness os the border i.e. [2]"],
			["label","label to be used i.e. [name|ref]"],
			["labelColor","color of label text i.e. [blue]"],
			["labelSize","size of the label i.e. [20]"],
			["labelFont","font for label"],
			["labelFontFamily","font family for label"],
			["labelOffset","distance of label to middle of way i.e. [5]"],
			["labelBold","bold font for label"],
			["labelItalic","italic font for label"],
			["labelHalo","halo for label, width in pixels"],
			["labelHaloColor","color for halo"],
			["labelTransform","perl code for label name transformation"],
			["legend","is this object to be listed in map legend? [yes|no]"],
			["legendLabel","label text of object in legend i.e. [Highway]"],

			["svgStringBottom","format of lower way part (i.e. border) []"],
			["svgStringTop","format of upper way part []"],
			["bottomBorder","NOT YET IMPLEMENTED"],
			
			["fromScale","rule will only applied if scale is bigger than fromScale i.e. [5000]"],
			["toScale","rule will only applied if scale is lower than fromScale i.e. [25000]"],

			["direxclude","should these objects be excluded from directory? [yes|no]"]
					) ;

my @validAreaProperties = (	
			["keyValue","key and value of object i.e. [amenity=parking]"],
			["color","color of area i.e. [lightgrey]"],
			["icon","icon for fill pattern to be used i.e. [icondir/parking.svg]"],
			["label", "label text to be rendered i.e. [name]"] ,
			["labelFont","font for label"],
			["labelFontFamily","font family for label"],
			["labelColor", "color of label i.e. [green]"] ,
			["labelSize", "size of label text i.e. [20]"] ,
			["labelBold","bold font for label"],
			["labelItalic","italic font for label"],
			["labelHalo","halo for label, width in pixels"],
			["labelHaloColor","color for halo"],
			["labelTransform","perl code for label name transformation"],
			["base","should this object be drawn underneath other objects? (applies for landuse residential i.e.) [yes|no]"],
			["svgString","format of area []"],
			["legend","is this object to be listed in map legend? [yes|no]"],
			["legendLabel","label text of object in legend i.e. [Parking]"],
			["fromScale","rule will only applied if scale is bigger than fromScale i.e. [5000]"],
			["toScale","rule will only applied if scale is lower than fromScale i.e. [25000]"]
					) ;


my @validRouteProperties =  (	
			["type","type of route like [bus|hiking]"],
			["color","color of route like [red]"],
			["size","size of route i.e. [10]"],
			["dash","svg dash array style like [20,20]"],
			["linecap","linecap style [butt|round|square]"],
			["opacity","opacity of the route [0..100]"],
			["label","label to be used like [ref]"],
			["labelFont","font for label"],
			["labelFontFamily","font family for label"],
			["labelSize","size of the label i.e. [15]"],
			["nodeSize","size of nodes belonging to route i.e. [20]"],
			["fromScale","rule will only applied if scale is bigger than fromScale i.e. [5000]"],
			["toScale","rule will only applied if scale is lower than fromScale i.e. [25000]"]
					) ;


my %nodeRules = () ;
my %areaRules = () ;
my %wayRules = () ;
my %routeRules = () ;
my $nodeNr = 0 ;
my $areaNr = 0 ;
my $wayNr = 0 ;
my $routeNr = 0 ;

my $line ;
my $ruleFile ;

# ---------------------------------------------------------------------------------------

sub printValidObjectProperties {

	print "\nValid Object Properties\n" ;

	print "\nNodes\n-----\n" ;
	foreach my $p (sort {$a->[0] cmp $b->[0]} @validNodeProperties) {
		printf "%-20s %s\n", $p->[0], $p->[1] ;
	}
	print "\nWays\n----\n" ;
	foreach my $p (sort {$a->[0] cmp $b->[0]} @validWayProperties) {
		printf "%-20s %s\n", $p->[0], $p->[1] ;
	}
	print "\nAreas\n-----\n" ;
	foreach my $p (sort {$a->[0] cmp $b->[0]} @validAreaProperties) {
		printf "%-20s %s\n", $p->[0], $p->[1] ;
	}
	print "\nRoutes\n-----\n" ;
	foreach my $p (sort {$a->[0] cmp $b->[0]} @validRouteProperties) {
		printf "%-20s %s\n", $p->[0], $p->[1] ;
	}
	print "\n" ;
}


# ---------------------------------------------------------------------------------------


sub readRules {

	my $fileName = cv('style') ;
	my $nrr = 0 ; my $wrr = 0 ; my $arr = 0 ; my $rrr = 0 ; my $crr = 0 ;

	print "reading rule file $fileName\n" ;

	my %vnp = () ;
	foreach my $p ( @validNodeProperties ) { $vnp{ lc ( $p->[0] ) } = 1 ; }

	my %vwp = () ;
	foreach my $p ( @validWayProperties ) { $vwp{ lc ( $p->[0] ) } = 1 ; }

	my %vap = () ;
	foreach my $p ( @validAreaProperties ) { $vap{ lc ( $p->[0] ) } = 1 ; }

	my %vrp = () ;
	foreach my $p ( @validRouteProperties ) { $vrp{ lc ( $p->[0] ) } = 1 ; }

	openRuleFile($fileName) ;
	while (defined $line) {
		if ( grep /^rule node/i, $line ) {
			$nodeNr++ ;
			$nrr++ ;
			getRuleLine() ;

			# set defaults first
			$nodeRules{ $nodeNr }{ 'size' } = cv( 'ruleDefaultNodeSize' ) ;
			$nodeRules{ $nodeNr }{ 'color' } = cv( 'ruleDefaultNodeColor' ) ;
			$nodeRules{ $nodeNr }{ 'shape' } = cv( 'ruleDefaultNodeShape' ) ;

			$nodeRules{ $nodeNr }{ 'label' } = cv( 'ruleDefaultNodeLabel' ) ;
			$nodeRules{ $nodeNr }{ 'labelfont' } = cv( 'ruleDefaultNodeLabelFont' ) ;
			$nodeRules{ $nodeNr }{ 'labelfontfamily' } = cv( 'ruleDefaultNodeLabelFontFamily' ) ;
			$nodeRules{ $nodeNr }{ 'labelsize' } = cv( 'ruleDefaultNodeLabelSize' ) ;
			$nodeRules{ $nodeNr }{ 'labelitalic' } = "no" ;
			$nodeRules{ $nodeNr }{ 'labelbold' } = "no" ;
			$nodeRules{ $nodeNr }{ 'labelhalo' } = 0 ;
			$nodeRules{ $nodeNr }{ 'labelhalocolor' } = "white" ;
			$nodeRules{ $nodeNr }{ 'labeltransform' } = "" ;
			$nodeRules{ $nodeNr }{ 'icon' } = "none" ;
			$nodeRules{ $nodeNr }{ 'iconsize' } = cv( 'ruleDefaultNodeIconSize' ) ;
			$nodeRules{ $nodeNr }{ 'legend' } = "no" ;
			$nodeRules{ $nodeNr }{ 'shieldname' } = "none" ;
			$nodeRules{ $nodeNr }{ 'svgstring' } = "" ;
			$nodeRules{ $nodeNr }{ 'legend' } = "no" ;
			$nodeRules{ $nodeNr }{ 'legendlabel' } = "" ;

			$nodeRules{ $nodeNr }{ 'circle' } = 'no' ;
			$nodeRules{ $nodeNr }{ 'circlecolor' } = 'black' ;
			$nodeRules{ $nodeNr }{ 'circleradius' } = 1000 ;
			$nodeRules{ $nodeNr }{ 'circlethickness' } = 10 ;
			$nodeRules{ $nodeNr }{ 'circlesvgstring' } = "" ;

			$nodeRules{ $nodeNr }{ 'disc' } = 'no' ;
			$nodeRules{ $nodeNr }{ 'disccolor' } = 'red' ;
			$nodeRules{ $nodeNr }{ 'discopacity' } = 50 ;
			$nodeRules{ $nodeNr }{ 'discradius' } = 1000 ;
			$nodeRules{ $nodeNr }{ 'discradius' } = 1000 ;
			$nodeRules{ $nodeNr }{ 'discsvgstring' } = '' ;

			$nodeRules{ $nodeNr }{ 'fromscale' } = cv ('ruledefaultnodefromscale') ;
			$nodeRules{ $nodeNr }{ 'toscale' } =  cv ('ruledefaultnodetoscale') ;

			$nodeRules{ $nodeNr }{ 'direxclude' } = cv('direxcludedefault') ;

			while ( ( defined $line) and ( ! grep /^rule/i, $line) ) {
				my ($k, $v) = ( $line =~ /(.+?)=(.+)/ ) ;
				if ( ( ! defined $k ) or ( ! defined $v ) ) {
					print "WARNING: could not parse rule line: $line" ;
				}
				else {
					$k = lc ( $k ) ;
					$nodeRules{ $nodeNr }{ $k } = $v ;
					if ( ! defined $vnp{$k} ) { print "WARNING: $k is not a valid node property!\n" ; }
				}
				getRuleLine() ;
			}
			if ( ! defined $nodeRules{ $nodeNr }{ 'keyvalue' } ) { die "ERROR: rule without keyValue detected!\n" ; }

		} # node

		elsif ( grep /^rule way/i, $line ) {

			$wayNr++ ;
			$wrr++ ;
			getRuleLine() ;

			# set defaults first
			$wayRules{ $wayNr }{ 'label' } = cv( 'ruleDefaultWayLabel' ) ;
			$wayRules{ $wayNr }{ 'labelfont' } = cv( 'ruleDefaultWayLabelFont' ) ;
			$wayRules{ $wayNr }{ 'labelfontfamily' } = cv( 'ruleDefaultWayLabelFontFamily' ) ;
			$wayRules{ $wayNr }{ 'labelsize' } = cv( 'ruleDefaultWayLabelSize' ) ;
			$wayRules{ $wayNr }{ 'labelcolor' } = cv( 'ruleDefaultWayLabelColor' ) ;
			$wayRules{ $wayNr }{ 'labelfont' } = cv( 'ruleDefaultWayLabelFont' ) ;
			$wayRules{ $wayNr }{ 'labeloffset' } = cv( 'ruleDefaultWayLabelOffset' ) ;
			$wayRules{ $wayNr }{ 'labelitalic' } = "no" ;
			$wayRules{ $wayNr }{ 'labelbold' } = "no" ;
			$wayRules{ $wayNr }{ 'labelhalo' } = 0 ;
			$wayRules{ $wayNr }{ 'labelhalocolor' } = "white" ;
			$wayRules{ $wayNr }{ 'labeltransform' } = "" ;
			$wayRules{ $wayNr }{ 'legend' } = "no" ;
			$wayRules{ $wayNr }{ 'legendlabel' } = "" ;
			$wayRules{ $wayNr }{ 'color' } = cv( 'ruleDefaultWayColor' ) ;
			$wayRules{ $wayNr }{ 'size' } = cv( 'ruleDefaultWaySize' ) ;
			$wayRules{ $wayNr }{ 'bordercolor' } = cv( 'ruleDefaultWayBorderColor' ) ;
			$wayRules{ $wayNr }{ 'bordersize' } = cv( 'ruleDefaultWayBorderSize' ) ;
			$wayRules{ $wayNr }{ 'dash' } = cv( 'ruleDefaultWayDash' ) ;
			$wayRules{ $wayNr }{ 'dashcap' } = cv( 'ruleDefaultWayDashCap' ) ;

			$wayRules{ $wayNr }{ 'svgstringtop' } = "" ;
			$wayRules{ $wayNr }{ 'svgstringbottom' } = "" ;

			$wayRules{ $wayNr }{ 'fromscale' } = cv ('ruledefaultwayfromscale') ;
			$wayRules{ $wayNr }{ 'toscale' } =  cv ('ruledefaultwaytoscale') ;

			$wayRules{ $wayNr }{ 'direxclude' } = cv('direxcludedefault') ;

			while ( ( defined $line) and ( ! grep /^rule/i, $line) ) {
				my ($k, $v) = ( $line =~ /(.+?)=(.+)/ ) ;
				if ( ( ! defined $k ) or ( ! defined $v ) ) {
					print "WARNING: could not parse rule line: $line" ;
				}
				else {
					$k = lc ( $k ) ;
					$wayRules{ $wayNr }{ $k } = $v ;
					if ( ! defined $vwp{$k} ) { print "WARNING: $k is not a valid way property!\n" ; }
				}
				getRuleLine() ;
			}
			if ( ! defined $wayRules{ $wayNr }{ 'keyvalue' } ) { die "ERROR: rule without keyValue detected!\n" ; }

		} # way

		elsif ( grep /^rule area/i, $line ) {
			$areaNr++ ;
			$arr++ ;
			getRuleLine() ;

			# set defaults first
			$areaRules{ $areaNr }{ 'label' } = "none" ;
			$areaRules{ $areaNr }{ 'labelfont' } = cv( 'ruleDefaultAreaLabelFont' ) ;
			$areaRules{ $areaNr }{ 'labelfontfamily' } = cv( 'ruleDefaultAreaLabelFontFamily' ) ;
			$areaRules{ $areaNr }{ 'labelcolor' } = "black" ;
			$areaRules{ $areaNr }{ 'labelsize' } = 30 ;
			$areaRules{ $areaNr }{ 'labelitalic' } = "no" ;
			$areaRules{ $areaNr }{ 'labelbold' } = "no" ;
			$areaRules{ $areaNr }{ 'labelhalo' } = 0 ;
			$areaRules{ $areaNr }{ 'labelhalocolor' } = "white" ;
			$areaRules{ $areaNr }{ 'labeltransform' } = "" ;
			$areaRules{ $areaNr }{ 'color' } = cv( 'ruleDefaultAreaColor') ;
			$areaRules{ $areaNr }{ 'icon' } = "none" ;
			$areaRules{ $areaNr }{ 'base' } = "no"  ;
			$areaRules{ $areaNr }{ 'svgstring' } = ""  ;
			$areaRules{ $areaNr }{ 'minsize' } = cv ('ruledefaultareaminsize')  ;
			$areaRules{ $areaNr }{ 'legend' } = "no" ;
			$areaRules{ $areaNr }{ 'legendlabel' } = "" ;
			$areaRules{ $areaNr }{ 'fromscale' } = cv ('ruledefaultareafromscale') ;
			$areaRules{ $areaNr }{ 'toscale' } =  cv ('ruledefaultareatoscale') ;

			while ( ( defined $line) and ( ! grep /^rule/i, $line) ) {
				my ($k, $v) = ( $line =~ /(.+?)=(.+)/ ) ;
				if ( ( ! defined $k ) or ( ! defined $v ) ) {
					print "WARNING: could not parse rule line: $line" ;
				}
				else {
					$k = lc ( $k ) ;
					$areaRules{ $areaNr }{ $k } = $v ;
					if ( ! defined $vap{$k} ) { print "WARNING: $k is not a valid area property!\n" ; }

					if ($k eq "icon") { mwMap::addAreaIcon ($v) ; }
				}
				getRuleLine() ;
			}
			if ( ! defined $areaRules{ $areaNr }{ 'keyvalue' } ) { die "ERROR: rule without keyValue detected!\n" ; }

		} # area

		elsif ( grep /^rule route/i, $line ) {
			$routeNr++ ;
			$rrr++ ;
			getRuleLine() ;

			# set defaults first
			$routeRules{ $routeNr }{ 'color' } = cv( 'ruleDefaultRouteColor' ) ;
			$routeRules{ $routeNr }{ 'size' } = cv( 'ruleDefaultRouteSize' ) ;
			$routeRules{ $routeNr }{ 'dash' } = cv( 'ruleDefaultRouteDash' ) ;
			$routeRules{ $routeNr }{ 'linecap' } = cv( 'ruleDefaultRouteLinecap' ) ;
			$routeRules{ $routeNr }{ 'opacity' } = cv( 'ruleDefaultRouteOpacity' ) ;
			$routeRules{ $routeNr }{ 'label' } = cv( 'ruleDefaultRouteLabel' ) ;
			# $routeRules{ $routeNr }{ 'labelfont' } = cv( 'ruleDefaultRouteLabelFont' ) ;
			# $routeRules{ $routeNr }{ 'labelfontfamily' } = cv( 'ruleDefaultRouteLabelFontFamily' ) ;
			# $routeRules{ $routeNr }{ 'labelsize' } = cv( 'ruleDefaultRouteLabelSize' ) ;
			$routeRules{ $routeNr }{ 'nodesize' } = cv( 'ruleDefaultRouteNodeSize' ) ;
			$routeRules{ $routeNr }{ 'fromscale' } = cv( 'ruleDefaultRouteFromScale' ) ;
			$routeRules{ $routeNr }{ 'toscale' } = cv( 'ruleDefaultRouteToScale' ) ;

			while ( ( defined $line) and ( ! grep /^rule/i, $line) ) {
				my ($k, $v) = ( $line =~ /(.+?)=(.+)/ ) ;
				if ( ( ! defined $k ) or ( ! defined $v ) ) {
					print "WARNING: could not parse rule line: $line" ;
				}
				else {
					$k = lc ( $k ) ;
					$routeRules{ $routeNr }{ $k } = $v ;
					if ( ! defined $vrp{$k} ) { print "WARNING: $k is not a valid route property!\n" ; }
				}
				getRuleLine() ;
			}
			if ( ! defined $routeRules{ $routeNr }{ 'type' } ) { die "ERROR: route rule without type detected!\n" ; }

		} # route

		elsif ( grep /^rule config/i, $line ) {
			$crr++ ;
			my ($key, $value) = ( $line =~ /^rule config\s+(.+)=(.+)/i ) ;
			if ( (defined $key) and (defined $value) ) {
				setConfigValue ($key, $value) ;
				if ( cv('debug') eq "1" ) {
					print "RULES: config changed $key=$value\n" ;
				}
			}
			getRuleLine() ;
		} # config

		else {
			getRuleLine() ;
		}

	}


	close ($ruleFile) ;

	print "rules read: $nrr nodes, $wrr ways, $arr areas, $rrr routes and $crr configs\n\n" ;

}

sub getNodeRule {

	# takes tagref and returns hashref to rule properties

	my $tagRef = shift ;

	my $scale = getScale() ;
	if ( cv('rulescaleset') != 0 ) { $scale = cv('rulescaleset') ; }
	# print "GNR: scale: $scale\n" ;

	my $ruleFound ; undef $ruleFound ;

	# print "\n" ;

	RUL2: foreach my $rule ( sort { $a <=> $b } keys %nodeRules) {
		# print "rule $rule\n" ;
		if ( ( $nodeRules{$rule}{'fromscale'} <= $scale) and ( $nodeRules{$rule}{'toscale'} >= $scale) ) {

			my @kvs = split /;/, $nodeRules{$rule}{'keyvalue'} ;
			my $allValid = 1 ;
			RUL1: foreach my $kv1 ( @kvs ) { # for each needed
				my ($k, $v) = ( $kv1 =~ /(.+)=(.+)/ ) ;
				# print "  looking for $k=$v\n" ;
				my $found = 0 ;
				RUL3: foreach my $tag ( @$tagRef) {
					# print "    actual kvs: $tag->[0]=$tag->[1]\n" ;
					if ( ( $tag->[0] eq $k) and ( ( $tag->[1] eq $v) or ( $v eq "*") ) ) {
						$found = 1 ;
						# print "    FOUND\n" ;
						last RUL3 ;
					}
				} # tags
				if ( ! $found ) { 
					$allValid = 0 ;
					last RUL1 ; 
				}
			} # kv1

			if ( $allValid ) {
				# print "ALL VALID\n" ;
				# return the first rule found
				$ruleFound = \%{ $nodeRules{ $rule } } ;
				last RUL2 ;
			}

		} # scale

	} # all rules

	return ($ruleFound) ;

}

sub printNodeRules {
	foreach my $n ( sort { $a <=> $b }  keys %nodeRules) {
		print "node rule $n\n" ;
		foreach my $v (sort keys %{$nodeRules{$n}}) {
			print "  $v=$nodeRules{$n}{$v}\n" ;
		} 
		print "\n" ;
	}
}

# ---------------------------------------------------------------------------------------



sub getWayRule {

	# takes tagref and returns hashref to rule properties

	my $tagRef = shift ;

	my $scale = getScale() ;
	if ( cv('rulescaleset') != 0 ) { $scale = cv('rulescaleset') ; }

	my $ruleFound ; undef $ruleFound ;

	RUL5: foreach my $rule ( sort { $a <=> $b } keys %wayRules) {
		# print "rule $rule\n" ;
		if ( ( $wayRules{$rule}{'fromscale'} <= $scale) and ( $wayRules{$rule}{'toscale'} >= $scale) ) {

			my @kvs = split /;/, $wayRules{$rule}{'keyvalue'} ;
			my $allValid = 1 ;
			RUL4: foreach my $kv1 ( @kvs ) { # for each needed
				my ($k, $v) = ( $kv1 =~ /(.+)=(.+)/ ) ;
				# print "  looking for $k=$v\n" ;
				my $found = 0 ;
				RUL6: foreach my $tag ( @$tagRef) {
					# print "    actual kvs: $tag->[0]=$tag->[1]\n" ;
					if ( ( $tag->[0] eq $k) and ( ( $tag->[1] eq $v) or ( $v eq "*") ) ) {
						$found = 1 ;
						# print "    FOUND\n" ;
						last RUL6 ;
					}
				} # tags
				if ( ! $found ) { 
					$allValid = 0 ;
					last RUL4 ; 
				}
			} # kv1

			if ( $allValid ) {
				# print "ALL VALID\n" ;
				# return the first rule found
				$ruleFound = \%{ $wayRules{ $rule } } ;
				last RUL5 ;
			}

		} # scale

	} # all rules

	return ($ruleFound) ;

}


sub printWayRules {
	foreach my $n ( sort { $a <=> $b }  keys %wayRules) {
		print "way rule $n\n" ;
		foreach my $v (sort keys %{$wayRules{$n}}) {
			print "  $v=$wayRules{$n}{$v}\n" ;
		} 
		print "\n" ;
	}
}


# ---------------------------------------------------------------------------------------



sub getAreaRule {

	# takes tagref and returns hashref to rule properties

	my $tagRef = shift ;

	my $scale = getScale() ;
	if ( cv('rulescaleset') != 0 ) { $scale = cv('rulescaleset') ; }

	my $ruleFound ; undef $ruleFound ;

	RUL8: foreach my $rule ( sort { $a <=> $b } keys %areaRules) {
		# print "rule $rule\n" ;
		if ( ( $areaRules{$rule}{'fromscale'} <= $scale) and ( $areaRules{$rule}{'toscale'} >= $scale) ) {

			my @kvs = split /;/, $areaRules{$rule}{'keyvalue'} ;
			my $allValid = 1 ;
			RUL7: foreach my $kv1 ( @kvs ) { # for each needed
				my ($k, $v) = ( $kv1 =~ /(.+)=(.+)/ ) ;
				# print "  looking for $k=$v\n" ;
				my $found = 0 ;
				RUL9: foreach my $tag ( @$tagRef) {
					# print "    actual kvs: $tag->[0]=$tag->[1]\n" ;
					if ( ( $tag->[0] eq $k) and ( ( $tag->[1] eq $v) or ( $v eq "*") ) ) {
						$found = 1 ;
						# print "    FOUND\n" ;
						last RUL9 ;
					}
				} # tags
				if ( ! $found ) { 
					$allValid = 0 ;
					last RUL7 ; 
				}
			} # kv1

			if ( $allValid ) {
				# print "ALL VALID\n" ;
				# return the first rule found
				$ruleFound = \%{ $areaRules{ $rule } } ;
				last RUL8 ;
			}

		} # scale

	} # all rules

	return ($ruleFound) ;

}


sub printAreaRules {
	foreach my $n ( sort { $a <=> $b }  keys %areaRules) {
		print "area rule $n\n" ;
		foreach my $v (sort keys %{$areaRules{$n}}) {
			print "  $v=$areaRules{$n}{$v}\n" ;
		} 
		print "\n" ;
	}
}

# --------------------------------------------------------------------------------

sub getRouteRule {
	my $tagRef = shift ;

	my $scale = getScale() ;
	if ( cv('rulescaleset') != 0 ) { $scale = cv('rulescaleset') ; }

	my $ruleFound ; undef $ruleFound ;

	my $type = getValue ("route", $tagRef) ;

	if (defined $type) {
		# print "      GRR: $type \n" ;
		RULA: foreach my $r ( sort { $a <=> $b }  keys %routeRules) {
			# print "        GRR: $routeRules{$r}{'type'}\n" ;
			if ($routeRules{$r}{'type'} eq $type) {
				if ( ( $routeRules{$r}{'fromscale'} <= $scale) and ( $routeRules{$r}{'toscale'} >= $scale) ) {
					$ruleFound = \%{ $routeRules{ $r } } ;
					last RULA ;
				}
			}
		}

	}

	return $ruleFound ;
}

sub getRouteColors {
	my %routeColors = () ;
	foreach my $n (keys %routeRules) {
		my $type = $routeRules{$n}{'type'} ;
		my $color = $routeRules{$n}{'color'} ;
		@{$routeColors{$type}} = split ( /;/, $color ) ;
	}
	return \%routeColors ;
}

sub printRouteRules {
	foreach my $n ( sort { $a <=> $b }  keys %routeRules) {
		print "route rule $n\n" ;
		foreach my $v (sort keys %{$routeRules{$n}}) {
			print "  $v=$routeRules{$n}{$v}\n" ;
		} 
		print "\n" ;
	}
}

# --------------------------------------------------------------------------------


sub openRuleFile {
	my $fileName = shift ;
	open ($ruleFile, "<", $fileName) or die ("ERROR: could not open rule file $fileName\n") ;
	getRuleLine() ;
}

sub getRuleLine {
	$line = <$ruleFile> ;
	if (defined $line) {	
		$line =~ s/\r//g ; # remove dos/win char at line end
	}
	while ( (defined $line) and ( (length $line < 2) or ( grep /^comment/i, $line) or ( grep /^\#/i, $line) ) ) {
		$line = <$ruleFile> ;
	}
	return $line ;
}

sub adaptRuleSizes {
	foreach my $r ( keys %nodeRules ) {
		foreach my $p ( qw (iconSize labelOffset labelSize shieldSize size) ) {
			if ( defined $nodeRules{ $r }{ $p } ) {
				if ( grep /:/, $nodeRules{ $r }{ $p } ) {
					my $old = $nodeRules{ $r }{ $p } ;
					my $new = scaleSize ($nodeRules{ $r }{ $p }, $nodeRules{ $r }{ 'fromscale' }, $nodeRules{ $r }{ 'toscale' }) ;
					$nodeRules{ $r }{ $p } = $new ;
					if ( cv('debug') eq "1" ) {
						print "RULES/scale/node: $old -> $new\n" ;
					}
				}
			}
		}
	}
	foreach my $r ( keys %wayRules ) {
		foreach my $p ( qw (bordersize labelsize labeloffset size ) ) {
			if ( defined $wayRules{ $r }{ $p } ) {
				if ( grep /:/, $wayRules{ $r }{ $p } ) {
					my $kv = $wayRules{ $r }{ 'keyvalue' } ;
					my $old = $wayRules{ $r }{ $p } ;
					my $new = 0 ;
					$new = scaleSize ($wayRules{ $r }{ $p }, $wayRules{ $r }{ 'fromscale' }, $wayRules{ $r }{ 'toscale' }) ;
					$wayRules{ $r }{ $p } = $new ;
					if ( cv('debug') eq "1" ) {
						print "RULES/scale/way: $kv $p $old to $new\n" ;
					}
				}
			}
		}
	}
}

sub scaleSize {
	my ($str, $fromScale, $toScale) = @_ ;
	my @tmp = split /:/, $str ;
	my $lower = $tmp[0] ;
	my $upper = $tmp[1] ;
	my $newSize = 0 ;

	my $scale = getScale() ;
	if ( cv('rulescaleset') ne "0" ) { $scale = cv('rulescaleset') } ;

	if ( $scale < $fromScale) {
		$newSize = $upper ;
	}
	elsif ( $scale > $toScale ) {
		$newSize = $lower ;
	}
	else {
		my $percent = ( $scale - $fromScale ) / ($toScale - $fromScale) ;
		$newSize = $upper - $percent * ($upper - $lower) ;
	}
	$newSize = int ( $newSize * 10 ) / 10 ;
	return $newSize ;
}

sub createLegend {

	# TODO Auto size

	my $nx = 80 ;
	my $ny = 80 ;
	my $ey = 1.5 * $ny ;
	my $sx = 700 ;
	my $tx = 200 ;
	my $ty = $ey / 2 ;
	my $fs = 40 ;
	my $actualLine = 0 ;

	my $preCount = 0 ;
	foreach my $n (keys %nodeRules) {
		if ( $nodeRules{$n}{"legend"} eq "yes" ) { $preCount++ ; }
	}
	foreach my $n (keys %wayRules) {
		if ( $wayRules{$n}{"legend"} eq "yes" ) { $preCount++ ; }
	}
	foreach my $n (keys %areaRules) {
		if ( $areaRules{$n}{"legend"} eq "yes" ) { $preCount++ ; }
	}
	if ( cv('debug') eq "1" ) { print "LEGEND: $preCount elements found\n" ; }

	my $sy = $preCount * $ey ;
	addToLayer ("definitions", "<g id=\"legenddef\" width=\"$sx\" height=\"$sy\" >") ;

	my $color = "white" ;
	my $svgString = "fill=\"$color\"" ;
	drawRect (0, 0, $sx, $sy, 0, $svgString, "definitions") ;

	foreach my $n (keys %nodeRules) {
		if ( $nodeRules{$n}{"legend"} eq "yes" ) {
			my $x = $nx ;
			my $y = $actualLine * $ey + $ny ;
			
			if ( ($nodeRules{$n}{'size'} > 0) and ($nodeRules{$n}{'icon'} eq "none") )  {
				my $svgString = "" ;
				if ( $nodeRules{$n}{'svgstring'} ne "" ) {
					$svgString = $nodeRules{$n}{'svgstring'} ;
				}
				else {
					$svgString = "fill=\"$nodeRules{$n}{'color'}\"" ;
				}

				if ( $nodeRules{$n}{'shape'} eq "circle") {
					drawCircle ($x, $y, 0, $nodeRules{$n}{'size'}, 0, $svgString, 'definitions') ;
				}
				elsif ( $nodeRules{$n}{'shape'} eq "square") {
					drawSquare ($x, $y, 0, $nodeRules{$n}{'size'}, 0, $svgString, 'definitions') ;
				}
				elsif ( $nodeRules{$n}{'shape'} eq "triangle") {
					drawTriangle ($x, $y, 0, $nodeRules{$n}{'size'}, 0, $svgString, 'definitions') ;
				}
				elsif ( $nodeRules{$n}{'shape'} eq "diamond") {
					drawDiamond ($x, $y, 0, $nodeRules{$n}{'size'}, 0, $svgString, 'definitions') ;
				}

				my $textSvgString = createTextSVG ( cv('elementFontFamily'), cv('elementFont'), $fs, "black", undef, undef ) ;
				drawText ($tx, ($actualLine+0.5) * $ey + $fs/2, 0, $nodeRules{$n}{'legendlabel'}, $textSvgString, "definitions") ;
			}
			else {
				# TODO icon
			}
		$actualLine ++ ;
		}
	}

	foreach my $w (keys %wayRules) {
		if ( $wayRules{$w}{"legend"} eq "yes" ) {
			my ($x1, $x2) ;
			$x1 = 0.5 * $nx ;
			$x2 = 1.5 * $nx ;
			my $y = $actualLine * $ey + $ny ;
			my ($svg1, $layer1, $svg2, $layer2) = mwWays::createWayParameters ($wayRules{$w}, 0, 0, 0) ;
			my @coords = ($x1, $y, $x2, $y) ;
			if ($svg2 ne "") {
				drawWay ( \@coords, 0, $svg2, "definitions", undef ) ;
			}
			drawWay ( \@coords, 0, $svg1, "definitions", undef ) ;

			my $textSvgString = createTextSVG ( cv('elementFontFamily'), cv('elementFont'), $fs, "black", undef, undef ) ;
			drawText ($tx, ($actualLine+0.5)*$ey + $fs/2, 0, $wayRules{$w}{'legendlabel'}, $textSvgString, "definitions") ;

			$actualLine++ ;

		}
	}

	foreach my $a (keys %areaRules) {
		if ( $areaRules{$a}{"legend"} eq "yes" ) {
			my ($x1, $x2) ;
			my ($y1, $y2) ;
			$x1 = 0.7 * $nx ;
			$x2 = 1.3 * $nx ;
			$y1 = $actualLine * $ey + 0.7 * $ny ;
			$y2 = $actualLine * $ey + 1.3 * $ny ;

			my $color = $areaRules{$a}{'color'} ;
			my $icon = $areaRules{$a}{'icon'} ;
			my $base = $areaRules{$a}{'base'} ;
			my $svgString = $areaRules{$a}{'svgstring'} ;

			if ( ($svgString eq "") and ($icon eq "none") ) {
				$svgString = "fill=\"$color\" " ;
			}

			my @coords = ([$x1, $y1, $x2, $y1, $x2, $y2, $x1, $y2, $x1, $y1]) ;
			drawArea ($svgString, $icon, \@coords, 0, "definitions") ;

			my $textSvgString = createTextSVG ( cv('elementFontFamily'), cv('elementFont'), $fs, "black", undef, undef ) ;
			drawText ($tx, ($actualLine+0.5)*$ey + $fs/2, 0, $areaRules{$a}{'legendlabel'}, $textSvgString, "definitions") ;
			$actualLine++ ;
		}
	}


	addToLayer ("definitions", "</g>") ;

	my $posX = 0 ;
	my $posY = 0 ;

	my ($sizeX, $sizeY) = getDimensions() ;

	if ( cv('legend') eq "2") {
		$posX = $sizeX - $sx ;
		$posY = 0 ;
	}

	if ( cv('legend') eq "3") {
		$posX = 0 ;
		$posY = $sizeY - $sy ;
	}

	if ( cv('legend') eq "4") {
		$posX = $sizeX - $sx ;
		$posY = $sizeY - $sy ;
	}

	if ( (cv('legend') >=1) and (cv('legend')<=4) ) {
		addToLayer ("legend", "<use x=\"$posX\" y=\"$posY\" xlink:href=\"#legenddef\" />") ;
	}
	elsif (cv('legend') == 5) {
		# separate file
		createLegendFile($sx, $sy, "_legend", "#legenddef") ;
	}

}

1 ;
