#
#
#
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
#



use strict ;
use warnings ;

use OSM::osm ;
use OSM::osmDB ;

use DBI ;

my $program = "populateDB.pl" ;
my $version = "1.0 BETA" ;
my $usage = $program . " file.osm DBname" ;

my $wayId ;
my $wayId2 ;
my $wayUser ;
my @wayNodes ;
my @wayTags ;
my $nodeId ;
my $nodeUser ;
my $nodeLat ;
my $nodeLon ;
my @nodeTags ;
my $aRef1 ;
my $aRef2 ;
my $relationId ;
my $relationUser ;
my @relationTags ;
my @relationMembers ;


my $wayCount = 0 ;
my $nodeCount = 0 ;
my $relationCount = 0 ;

my $time0 = time() ; my $time1 ; my $time2 ;

my $osmName ;
my $dbName ;

my $maxK = 0 ;
my $maxV = 0 ;

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

print "\n$program $version for file $osmName DB:$ dbName\n\n" ;
print "\n\n" ;





dbConnect($dbName) ;
print "DB $dbName connected\n" ;

initTableNodes() ;
initTableWays() ;
initTableRelations() ;



######################
# get node information
######################
print "\nget node information...\n" ;
openOsmFile ($osmName) ;


($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
while ($nodeId != -1) {
	$nodeCount++ ;
	if ($nodeCount % 10000 == 0) { print "node $nodeCount\n" ; }

	storeDBNode ($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) ;

	# next
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
}


$time1 = time () ;
print "\nINFO: nodes finished after ", stringTimeSpent ($time1-$time0), "\n" ;

my $nps = $nodeCount / ($time1-$time0) ;
my $nph = int ($nps * 3600) ;
printf "INFO: %10d nodes/h\n", $nph ;


print "\nget way information...\n" ;

($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
while ($wayId != -1) {	
	$wayCount++ ;

	if ($wayCount % 10000 == 0) { print "way $wayCount\n" ; }

	storeDBWay ($wayId, $wayUser, $aRef1, $aRef2) ;

	# next way
	($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
}


$time2 = time () ;
print "\nINFO: ways finished after ", stringTimeSpent ($time2-$time1), "\n" ;

my $wps = $wayCount / ($time2-$time1) ;
my $wph = int ($wps * 3600) ;
printf "INFO: %10d nodes/h\n", $nph ;
printf "INFO: %10d ways/h\n", $wph ;

print "\nget relation information...\n" ;


($relationId, $relationUser, $aRef1, $aRef2) = getRelation () ;

while ($relationId != -1) {
	$relationCount++ ;	

	if ($relationCount % 10000 == 0) { print "rel: $relationCount\n" ; }

	storeDBRelation ($relationId, $relationUser, $aRef1, $aRef2) ;

	#next
	($relationId, $relationUser, $aRef1, $aRef2) = getRelation () ;
}

closeOsmFile () ;



dbDisconnect() ;
print "DB disconnected\n" ;

my $time3 = time () ;
print "\nINFO: relations finished after ", stringTimeSpent ($time3-$time2), "\n" ;

my $rps = $relationCount / ($time3-$time2) ;
my $rph = int ($rps * 3600) ;
printf "INFO: %10d nodes/h\n", $nph ;
printf "INFO: %10d ways/h\n", $wph ;printf "INFO: %10d relations/h\n", $rph ;


printMaxValues() ;


print "\nINFO: finished after ", stringTimeSpent ($time3-$time0), "\n\n" ;


