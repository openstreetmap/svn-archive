
use strict ;
use warnings ;

use OSM::osm ;
use OSM::osmDB ;
use File::stat;
use Time::localtime;


my $program = "bulkDB.pl" ;
my $version = "1.0" ;

my $usage = $program . " file.osm dbname" ;



my $time0 = time() ; my $time1 ;

my $osmName ;
my $dbName ;




###############
# get parameter
###############

$osmName = shift||'';
if (!$osmName)
{
	die (print $usage, "\n");
}

$dbName = shift||'';
if (!$dbName)
{
	die (print $usage, "\n");
}

print "\n$program $version for file $osmName DB $dbName\n\n" ;
print "\n\n" ;



bulkLoad ($osmName, $dbName) ;


$time1 = time () ;

print "\nINFO: finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;

