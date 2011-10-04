# 
# PERL mapweaver by gary68
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



# 0.03 20110614 -help
# 0.03 20110614 square for nodes
# 0.03 print prg name and version
# 0.03 ruler
# 0.04 ruler positions; ruler background; disc opacity correction; -debug; -verbose
# 0.04 scale, colors and positions; header/footer
# 0.04 triangle and diamond for nodes; labels and icons for nodes
# 0.05 categories for config values
# 0.06 drawArea; area rules; extended help, added valid object properties
# 0.07 way labels; minsizearea implemented;
# 0.08 added coastlines; problems with completeObjects! use option -cie
# 0.09 oneways
# 0.10 pagenumbers; rectangles; comments and empty lines in rule file; config in rule file
# 0.10 coast lines fixed; auto bridge implemented
# 0.11 area icons / patterns added; time; street directory; poi directory; pdf directoriy
# 0.12 way shields
# 0.13 routes, not yet working...
# 0.14 route work
# 0.15 routes working now - finetuning needed; bgbolor implemented; multipolygons
# 0.16 size check for multipolygon areas; scale rule sizes (x:y)
# 0.17 -forcenodes; projection in footer
# 0.18 direxclude options and rule properties
# 0.19 pagenumber bug solved
# 0.20 legend
# 0.21 legend in separate file
# 0.22 help texts for object properties in rule file
# 0.23 latex string sanitize
# 0.24 labels for areas
# 0.25 labels for multipolygons
# 0.26 fix directory bugs
# 0.27 way name substitution, if name is too long for way. incl. legend for map
# 0.28 oceancolor bug fixed
# 0.29 fonts/families
# 0.30 -wns=5 now possible; way name substitutions in separate file
# 0.31 getXXXrule bug fixed; wnsunique
# 0.32 -targetSize
# 0.33 -onewayautosize
# 0.34 pbf support; halo; label transform; bold print of labels
# 0.35 svg text creation bug fixed
# 0.36 font size error wns corrected; box occupy; new place management
# 0.37 -dirprg program to create directory; gpx support
# 0.38 -gpxcolor; -gpxsize
# 0.39 parameter bug dirprg fixed; sanitize bug fixed 
# 0.40 

# TODO
# -different tempfilenames

my $version = "0.39" ;
my $programName = "mapweaver" ;

use strict ;
use warnings ;

use OSM::osm ;
use mwConfig ;
use mwMap ;
use mwRules ;
use mwFile ;
use mwNodes ;
use mwWays ;
use mwRelations ;
use mwMulti ;
use mwMisc ;
use mwOccupy ;
use mwGPX ;

my $time0 = time() ; 


print "\n$programName $version by gary68\n\n" ;

initConfig() ;

getProgramOptions() ;

readConfigFile( cv('ini') ) ;

if ( cv('help') eq "1" ) {
	printConfigDescriptions() ;
	printValidObjectProperties() ;
	die ("quit after help output\n") ;
}

if ( cv('verbose') eq "1" ) {
	printConfig() ;
}

readRules() ;

if ( cv('debug') eq "1" ) {
	printNodeRules() ;
	printWayRules() ;
	printAreaRules() ;
	printRouteRules() ;
}

readFile() ;

my $renderTime0 = time() ;

adaptRuleSizes() ;

if ( cv('multionly') eq "0" ) {

	processNodes() ;

	if ( cv('poi') eq "1") {
		createPoiDirectory() ;
	}

	initOneways() ;
	processWays() ;

	if ( cv('dir') eq "1") {
		createDirectory() ;
	}

	if ( cv('dirpdf') eq "1") {
		createDirPdf() ;
	}

	processRoutes() ;

} # multionly

processMultipolygons() ;


if ( cv('legend') ne "0" ) { createLegend() ; }

if ( cv('pagenumbers') ne "" ) { processPageNumbers() ; }
if ( cv('rectangles') ne "" ) { processRectangles() ; }

if ( cv ('test') eq "1") {
	boxDrawOccupiedAreas() ;
}

if ( cv ('gpx') ne "") {
	processGPXFile() ;
}


writeMap() ;

my $renderTime1 = time() ;


my ($paper, $x, $y) = fitsPaper () ; $x = int ($x*10) / 10 ; $y = int ($y*10) / 10 ;
print "map ($x cm x $y cm) fits paper $paper\n\n" ;

my $time1 = time() ;
print "\nrender time (excluding all file operations) ", stringTimeSpent ($renderTime1-$renderTime0), "\n" ;
print "\n$programName finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;


