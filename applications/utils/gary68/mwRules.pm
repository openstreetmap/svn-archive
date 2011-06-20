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

use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw ( 	readRules
			getNodeRule
			printNodeRules
			getWayRule
			printWayRules
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
					borderColor
					borderSize
					label
					labelSize
					labelFont
					labelOffset

					svgStringBottom
					svgStringTop
					bottomBorder
					
					fromScale
					toScale
					) ;


my %nodeRules = () ;
my %areaRules = () ;
my %wayRules = () ;
my $nodeNr = 0 ;
my $areaNr = 0 ;
my $wayNr = 0 ;

sub readRules {

	my $fileName = cv('style') ;
	my $nrr = 0 ; my $wrr = 0 ; my $rrr = 0 ; my $crr = 0 ;

	print "reading rule file $fileName\n" ;

	my %vnp = () ;
	foreach my $p ( @validNodeProperties ) { $vnp{ lc ( $p ) } = 1 ; }

	my %vwp = () ;
	foreach my $p ( @validWayProperties ) { $vwp{ lc ( $p ) } = 1 ; }

	open (my $file, "<", $fileName) or die ("ERROR: could not open rule file $fileName\n") ;
	my $line = "" ;
	$line = <$file>	;
	while (defined $line) {
		$line =~ s/\r//g ; # remove dos/win char at line end
		if ( grep /^rule node/i, $line ) {
			$nodeNr++ ;
			$nrr++ ;
			$line = <$file> ;

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
				$line = <$file> ;
			}
			if ( ! defined $nodeRules{ $nodeNr }{ 'keyvalue' } ) { die "ERROR: rule without keyValue detected!\n" ; }

		} # node

		elsif ( grep /^rule way/i, $line ) {

			$wayNr++ ;
			$wrr++ ;
			$line = <$file> ;

			# set defaults first
			$wayRules{ $wayNr }{ 'color' } = cv( 'ruleDefaultWayColor' ) ;
			$wayRules{ $wayNr }{ 'size' } = cv( 'ruleDefaultWaySize' ) ;
			$wayRules{ $wayNr }{ 'bordercolor' } = cv( 'ruleDefaultWayBorderColor' ) ;
			$wayRules{ $wayNr }{ 'bordersize' } = cv( 'ruleDefaultWayBorderSize' ) ;

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
					if ( ! defined $vwp{$k} ) { print "WARNING: $k is not a valid node property!\n" ; }
				}
				$line = <$file> ;
			}
			if ( ! defined $wayRules{ $wayNr }{ 'keyvalue' } ) { die "ERROR: rule without keyValue detected!\n" ; }

		} # way

		elsif ( grep /^rule area/i, $line ) {
		} # area

		elsif ( grep /^rule config/i, $line ) {
		} # area

		else {
			$line = <$file> ;
		}

	}


	close ($file) ;

	print "rules read: $nrr nodes, $wrr ways, $rrr routes and $crr configs\n\n" ;

}

sub getNodeRule {

	# takes tagref and returns hashref to rule properties

	my $tagRef = shift ;

	my $scale = cv ('scale') ;
	if ( cv('rulescaleset') != 0 ) { $scale = cv('rulescaleset') ; }

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

	my $scale = cv ('scale') ;
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



1 ;






