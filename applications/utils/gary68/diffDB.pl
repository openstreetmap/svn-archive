
use strict ;
use warnings ;

use OSM::osm ;
use OSM::osmDB ;
use File::stat;
use Time::localtime;


my $program = "diffDB.pl" ;
my $version = "0.1 BETA" ;

my $usage = $program . " dbname file.osc" ;



my $time0 = time() ; my $time1 ;

my $oscName ;
my $dbName ;




###############
# get parameter
###############

$dbName = shift||'';
if (!$dbName)
{
	die (print $usage, "\n");
}

$oscName = shift||'';
if (!$oscName)
{
	die (print $usage, "\n");
}

print "\n$program $version for DB $dbName file $oscName\n\n" ;
print "\n\n" ;



applyDiffFile ($dbName, $oscName) ;


$time1 = time () ;

print "\nINFO: finished after ", stringTimeSpent ($time1-$time0), "\n\n" ; 
