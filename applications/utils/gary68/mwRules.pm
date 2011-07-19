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
		 ) ;

my @validNodeProperties = qw (	keyValue
						color
						size
						shape
						svgString
						circle
						circleColor
						circleRadius
						circleThickness
						circleSVGString
						disc
						discColor
						discOpacity
						discRadius
						discSVGString
						label
						labelColor
						labelSize
						labelFont
						labelOffset
						legend
						legendLabel
						icon
						iconSize
						shieldName
						shieldSize
						shieldLabel
						fromScale
						toScale
					) ;

my @validWayProperties = qw (	keyValue
					color
					size
					dash
					dashCap
					borderColor
					borderSize
					label
					labelColor
					labelSize
					labelFont
					labelOffset

					svgStringBottom
					svgStringTop
					bottomBorder
					
					fromScale
					toScale
					) ;

my @validAreaProperties = qw (	keyValue
						color
						icon
						base
						svgString
						fromScale
						toScale
					) ;


my @validRouteProperties = qw (	type
						color
						size
						dash
						linecap
						opacity
						label
						labelSize
						nodeSize
						fromScale
						toScale
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
	foreach my $p (sort @validNodeProperties) {
		print "$p\n" ;
	}
	print "\nWays\n----\n" ;
	foreach my $p (sort @validWayProperties) {
		print "$p\n" ;
	}
	print "\nAreas\n-----\n" ;
	foreach my $p (sort @validAreaProperties) {
		print "$p\n" ;
	}
	print "\nRoutes\n-----\n" ;
	foreach my $p (sort @validRouteProperties) {
		print "$p\n" ;
	}
	print "\n" ;
}


# ---------------------------------------------------------------------------------------


sub readRules {

	my $fileName = cv('style') ;
	my $nrr = 0 ; my $wrr = 0 ; my $arr = 0 ; my $rrr = 0 ; my $crr = 0 ;

	print "reading rule file $fileName\n" ;

	my %vnp = () ;
	foreach my $p ( @validNodeProperties ) { $vnp{ lc ( $p ) } = 1 ; }

	my %vwp = () ;
	foreach my $p ( @validWayProperties ) { $vwp{ lc ( $p ) } = 1 ; }

	my %vap = () ;
	foreach my $p ( @validAreaProperties ) { $vap{ lc ( $p ) } = 1 ; }

	my %vrp = () ;
	foreach my $p ( @validRouteProperties ) { $vrp{ lc ( $p ) } = 1 ; }

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
			$nodeRules{ $nodeNr }{ 'labelsize' } = cv( 'ruleDefaultNodeLabelSize' ) ;
			$nodeRules{ $nodeNr }{ 'icon' } = "none" ;
			$nodeRules{ $nodeNr }{ 'iconsize' } = cv( 'ruleDefaultNodeIconSize' ) ;
			$nodeRules{ $nodeNr }{ 'legend' } = "no" ;
			$nodeRules{ $nodeNr }{ 'shieldname' } = "none" ;
			$nodeRules{ $nodeNr }{ 'svgstring' } = "" ;

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
			$wayRules{ $wayNr }{ 'labelsize' } = cv( 'ruleDefaultWayLabelSize' ) ;
			$wayRules{ $wayNr }{ 'labelcolor' } = cv( 'ruleDefaultWayLabelColor' ) ;
			$wayRules{ $wayNr }{ 'labelfont' } = cv( 'ruleDefaultWayLabelFont' ) ;
			$wayRules{ $wayNr }{ 'labeloffset' } = cv( 'ruleDefaultWayLabelOffset' ) ;
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
			$areaRules{ $areaNr }{ 'color' } = cv( 'ruleDefaultAreaColor' ) ;
			$areaRules{ $areaNr }{ 'icon' } = "none" ;
			$areaRules{ $areaNr }{ 'base' } = "no"  ;
			$areaRules{ $areaNr }{ 'svgstring' } = ""  ;
			$areaRules{ $areaNr }{ 'minsize' } = cv ('ruledefaultareaminsize')  ;
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
			$routeRules{ $routeNr }{ 'labelsize' } = cv( 'ruleDefaultRouteLabelSize' ) ;
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

	RUL2: foreach my $rule (keys %nodeRules) {
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
	foreach my $n (sort keys %nodeRules) {
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

	RUL5: foreach my $rule (keys %wayRules) {
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
	foreach my $n (sort keys %wayRules) {
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

	RUL8: foreach my $rule (keys %areaRules) {
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
	foreach my $n (sort keys %areaRules) {
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
		RULA: foreach my $r (sort keys %routeRules) {
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
	foreach my $n (sort keys %routeRules) {
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

1 ;
