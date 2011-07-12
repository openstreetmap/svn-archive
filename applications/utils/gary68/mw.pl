

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

my $version = "0.14" ;
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
use mwMisc ;

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

if ( cv('pagenumbers') ne "" ) { processPageNumbers() ; }
if ( cv('rectangles') ne "" ) { processRectangles() ; }

writeMap() ;

my ($paper, $x, $y) = fitsPaper () ; $x = int ($x*10) / 10 ; $y = int ($y*10) / 10 ;
print "map ($x cm x $y cm) fits paper $paper\n\n" ;

my $time1 = time() ;
print "\n$programName finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;
