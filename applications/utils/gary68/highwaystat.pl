#
#
#
#
#
# Copyright (C) 2008, 2009, Gerhard Schwanz
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
use File::stat;
use Time::localtime;


my $program = "highwaystat.pl" ;
my $version = "1.1" ;
my $usage = $program . " file.osm" ;

my $wayId ;
my $wayId2 ;
my $wayUser ;
my @wayNodes ;
my @wayTags ;
my $nodeId ;
my $nodeId2 ;
my $nodeUser ;
my $nodeLat ;
my $nodeLon ;
my @nodeTags ;
my $aRef1 ;
my $aRef2 ;
my $wayCount = 0 ;
my $areaCount = 0 ;
my $areaOpenCount = 0 ;

my $time0 = time() ; my $time1 ;
my $i ;
my $key ;
my $num ;
my $tag1 ; my $tag2 ;

my $osmName ;


my %lon ;
my %lat ;
my %length ;

###############
# get parameter
###############
$osmName = shift||'';
if (!$osmName)
{
	die (print $usage, "\n");
}

print "\n$program $version for file $osmName\n\n" ;
print "\n\n" ;





######################
# get node information
######################
print "get node information...\n" ;
openOsmFile ($osmName) ;


($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
if ($nodeId != -1) {
	#@nodeTags = @$aRef1 ;
}

while ($nodeId != -1) {

	$lon{$nodeId} = $nodeLon ;
	$lat{$nodeId} = $nodeLat ;

	# next
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
	if ($nodeId != -1) {
		#@nodeTags = @$aRef1 ;
	}
}


print "get way information...\n" ;

($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {	
	$wayCount++ ;

	my $found = 0 ;
	my $type = "" ;
	foreach $tag1 (@wayTags) {
		if ($tag1->[0] eq "highway") { $found = 1 ; $type = $tag1->[1] ; }
	}

	if ($found and (scalar @wayNodes>1) ) { 
		my $i ; my $Length = 0 ;
		for ($i = 0; $i<$#wayNodes; $i++) {
			$Length += distance ($lon{$wayNodes[$i]}, $lat{$wayNodes[$i]}, $lon{$wayNodes[$i+1]}, $lat{$wayNodes[$i+1]}) ;
		}
		if (defined $length{$type}) {
			$length{$type} += $Length ;
		}
		else {
			$length{$type} = $Length ;
		}
	}

	# next way
	($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;



print "\n" ;

my $wayType ;
foreach $wayType (sort keys %length) {
	printf "%-30s %8.2f km\n", $wayType, $length{$wayType} ;
}


$time1 = time () ;



print "\nINFO: finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;


