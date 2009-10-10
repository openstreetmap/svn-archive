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


my $program = "streetnames.pl" ;
my $version = "1.1" ;
my $usage = $program . " file.osm out.txt" ;

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
my %names = () ;
my $wayCount = 0 ;
my $highwayCount = 0 ;

my $time0 = time() ; my $time1 ;

my $osmName ;
my $outName ;



###############
# get parameter
###############
$osmName = shift||'';
if (!$osmName)
{
	die (print $usage, "\n");
}

$outName = shift||'';
if (!$outName)
{
	die (print $usage, "\n");
}

print "\n$program $version for file $osmName\n\n" ;
print "\n\n" ;





######################
# get node information
######################
print "skip node information...\n" ;
openOsmFile ($osmName) ;


skipNodes() ;

print "get way information...\n" ;

($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {	
	$wayCount++ ;
	if ($wayCount % 100000 == 0 ) { print "$wayCount ways processed...\n" ; } 
	my $highway = 0 ;
	my $name = "" ;
	foreach my $tag (@wayTags) {
		if ($tag->[0] eq "highway") { $highway = 1 ; }
		if ($tag->[0] eq "name") { $name = $tag->[1] ; }
	}

	if ($highway and ($name ne "") ) { 
		$highwayCount++ ;
		$names{$name} = 1 ;
	}

	# next way
	($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;
my $outFile ;
open ($outFile, ">", $outName) ;
foreach my $street (sort keys %names) {
	print $outFile $street, "\n" ;
}
close ($outFile) ;

print "$wayCount ways processed.\n" ;
print "$highwayCount highways processed.\n" ;
print scalar (keys %names), " different names found.\n" ;

$time1 = time () ;
print "\nINFO: finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;


