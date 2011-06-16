

# 0.03 20110614 -help
# 0.03 20110614 square for nodes
# 0.03 print prg name and version
# 0.03 ruler
# 0.04 ruler positions; ruler background; disc opacity correction; -debug; -verbose
# 0.04 scale, colors and positions; header/footer
# 0.04 triangle and diamond for nodes; labels and icons for nodes
# 

my $version = "0.04" ;

use strict ;
use warnings ;

# use OSM::osm.pm ;
use mwConfig ;
use mwMap ;
use mwRules ;
use mwFile ;
use mwNodes ;
use mwWays ;
use mwMisc ;

print "\nmapweaver version $version by gary68\n\n" ;

initConfig() ;

getProgramOptions() ;

readConfigFile( cv('ini') ) ;

if ( cv('help') eq "1" ) {
	printConfigDescriptions() ;
	die ("quit after help output\n") ;
}

if ( cv('verbose') eq "1" ) {
	printConfig() ;
}

readRules() ;

readFile() ;

processNodes() ;
processWays() ;

writeMap() ;

my ($paper, $x, $y) = fitsPaper () ; $x = int ($x*10) / 10 ; $y = int ($y*10) / 10 ;
print "map ($x cm x $y cm) fits paper $paper\n" ;
