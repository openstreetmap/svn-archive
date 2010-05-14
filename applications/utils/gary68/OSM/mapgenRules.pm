# 
# PERL mapgenRules module by gary68
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


package OSM::mapgenRules ; #  

use strict ;
use warnings ;

use List::Util qw[min max] ;
use OSM::osm ;
use OSM::mapgen 1.05 ;

use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

$VERSION = '1.05' ;

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw ( readRules printRules ) ;

#
# constants
#

#
# variables
#
my @nodes = () ;
my @ways = () ;
my @routes = () ;

sub readRules {
	my $csvName = shift ;
	# READ STYLE File
	print "read style file and preprocess tile icons for areas...\n" ;
	open (my $csvFile, "<", $csvName) or die ("ERROR: style file not found.") ;
	my $line = <$csvFile> ; # omit SECTION

	# READ NODE RULES
	$line = <$csvFile> ;
	while (! grep /^\"SECTION/, $line) {
		if (! grep /^\"COMMENT/i, $line) {
			my ($key, $value, $color, $thickness, $label, $labelColor, $labelSize, $labelFont, $labelOffset, $legend, $icon, $iconSize, $fromScale, $toScale) = ($line =~ /\"(.+)\" \"(.+)\" \"(.+)\" (\d+) \"(.+)\" \"(.+)\" (\d+) \"(.+)\" (\d+) (\d) \"(.+)\" (\d+) (\d+) (\d+)/ ) ;
			# print "N $key, $value, $color, $thickness, $label, $labelColor, $labelSize, $labelFont, $labelOffset, $legend, $icon, $iconSize, $fromScale, $toScale\n" ; 
			push @nodes, [$key, $value, $color, $thickness, $label, $labelColor, $labelSize, $labelFont, $labelOffset, $legend, $icon, $iconSize, $fromScale, $toScale] ;
		}
		$line = <$csvFile> ;
	}

	# READ WAY RULES
	$line = <$csvFile> ; # omit SECTION
	while ( (! grep /^\"SECTION/, $line) and (defined $line) ) {
		if (! grep /^\"COMMENT/i, $line) {
			# print "way line: $line\n" ;
			my ($key, $value, $color, $thickness, $dash, $borderColor, $borderSize, $fill, $label, $labelColor, $labelSize, $labelFont, $labelOffset, $legend, $baseLayer, $areaIcon, $fromScale, $toScale) = 
			($line =~ /\"(.+)\" \"(.+)\" \"(.+)\" (\d+) (\d+) \"(.+)\" (\d+) (\d+) \"(.+)\" \"(.+)\" (\d+) \"(.+)\" ([\d\-]+) (\d) (\d) \"(.+)\" (\d+) (\d+)/ ) ;
			# print "W $key, $value, $color, $thickness, $dash, $borderColor, $borderSize, $fill, $label, $labelColor, $labelSize, $labelFont, $labelOffset, $legend, $baseLayer, $areaIcon, $fromScale, $toScale\n" ; 
			push @ways, [$key, $value, $color, $thickness, $dash, $borderColor, $borderSize, $fill, $label, $labelColor, $labelSize, $labelFont, $labelOffset, $legend, $baseLayer, $areaIcon, $fromScale, $toScale] ;
			if (($areaIcon ne "") and ($areaIcon ne "none")) { addAreaIcon ($areaIcon) ; }
		}
		$line = <$csvFile> ;
	}

	# READ ROUTE RULES	#print "ROUTE LINE: $line\n" ;
	$line = <$csvFile> ; # omit SECTION
	#print "ROUTE LINE: $line\n" ;
	while ( (! grep /^\"SECTION/, $line) and (defined $line) ) {
		if (! grep /^\"COMMENT/i, $line) {
			#print "ROUTE LINE: $line\n" ;
			my ($route, $color, $thickness, $dash, $opacity, $label, $nodeThickness, $fromScale, $toScale) = ($line =~ /\"(.+)\" \"(.+)\" (\d+) (\d+) (\d+) \"(.+)\" (\d+) (\d+) (\d+)/ ) ;
			$opacity = $opacity / 100 ;
			push @routes, [$route, $color, $thickness, $dash, $opacity, $label, $nodeThickness, $fromScale, $toScale] ;		}
		$line = <$csvFile> ;	}
	close ($csvFile) ;

	foreach my $node (@nodes) {
		$node->[3] = scalePoints ($node->[3]) ;
		$node->[6] = scalePoints ($node->[6]) ;
		$node->[8] = scalePoints ($node->[8]) ;
		$node->[11] = scalePoints ($node->[11]) ;
	}

	foreach my $way (@ways) {
		$way->[3] = scalePoints ($way->[3]) ;
		$way->[6] = scalePoints ($way->[6]) ;
		$way->[10] = scalePoints ($way->[10]) ;
		$way->[12] = scalePoints ($way->[12]) ;
	}

	foreach my $route (@routes) {
		$route->[2] = scalePoints ($route->[2]) ;
		$route->[6] = scalePoints ($route->[6]) ;
	}

	return (\@nodes, \@ways, \@routes) ;
}


sub printRules {
	print "WAYS/AREAS\n" ;
	foreach my $way (@ways) {
		printf "%-20s %-20s %-10s %-6s %-6s %-10s %-6s %-6s %-10s %-10s %-10s %-10s %-6s %-6s %-6s %-20s %-10s %-10s\n", $way->[0], $way->[1], $way->[2], $way->[3], $way->[4], $way->[5], $way->[6], $way->[7], $way->[8], $way->[9], $way->[10], $way->[11], $way->[12], $way->[13], $way->[14], $way->[15], $way->[16], $way->[17] ;
	}
	print "\n" ;
	print "NODES\n" ;	foreach my $node (@nodes) {		printf "%-20s %-20s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-20s %6s %-10s %-10s\n", $node->[0], $node->[1], $node->[2], $node->[3], $node->[4], $node->[5], $node->[6], $node->[7], $node->[8], $node->[9], $node->[10], $node->[11], $node->[12], $node->[13] ;	}
	print "\n" ;

	print "ROUTES\n" ;	foreach my $route (@routes) {		printf "%-20s %-20s %-10s %-10s %-10s %-10s %-10s %-10s %-10s\n", $route->[0], $route->[1], $route->[2], $route->[3], $route->[4], $route->[5], $route->[6], $route->[7], $route->[8] ;	}	print "\n" ;
}


1 ;


