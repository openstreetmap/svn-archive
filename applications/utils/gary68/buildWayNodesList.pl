#
#
# buildWayNodesList.pl
#
# program takes osm file (preferably a whole planet) and builds a file that contains just lines of wayIds and the number of nodes contained in that way.
# this information can be used by programs that use only portions of the planet file to determine whether a way was cut of by using bounding boxes 
# or polygons.
#
#
#
# Copyright (C) 2009, Gerhard Schwanz
#
# This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 3 of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>
#
#


use strict ;
use warnings ;

use OSM::osm 5.0 ;

my $usage = "perl buildWayNodesList.pl file.osm out.txt" ;
my $version = "1.0" ;

my $wayId ;
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

my $osmName ; 
my $outName ;
my $outFile ;

my $time0 ; 

# get parameter

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

$time0 = time() ;

open ($outFile, ">", $outName) or die ("can't open output file")  ;
print $outFile "# waynodes file for osm file $osmName\n" ;

print "skipping nodes...\n" ;
openOsmFile ($osmName) ;
skipNodes() ;
print "done.\n" ;

print "parsing and checking ways...\n" ;
($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {
	
	my ($number) = scalar @wayNodes ;

	print $outFile "$wayId $number\n" ;
	
	($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}
closeOsmFile() ;
print "done.\n" ;
close ($outFile) ;

print "\nfinished after ", stringTimeSpent (time()-$time0), "\n\n" ;




