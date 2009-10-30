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

use strict ;
use warnings ;

use OSM::osm ;

my $programName = "prouting.pl" ;
my $usage = $programName . " file.osm out.rou" ; 
my $version = "1.0" ;

# add all allowed pathes here
my %allowedPaths = () ;
$allowedPaths{"highway:motorway"} = 1 ;
$allowedPaths{"highway:motorway_link"} = 1 ;
$allowedPaths{"highway:trunk"} = 1 ;
$allowedPaths{"highway:trunk_link"} = 1 ;
$allowedPaths{"highway:primary"} = 1 ;
$allowedPaths{"highway:primary_link"} = 1 ;
$allowedPaths{"highway:secondary"} = 1 ;
$allowedPaths{"highway:secondary_link"} = 1 ;
$allowedPaths{"highway:tertiary"} = 1 ;
$allowedPaths{"highway:residential"} = 1 ;
$allowedPaths{"highway:service"} = 1 ;
# etc...

my %edge ;
my %wayCount = () ;

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
my $outFileName ;

my %lon ; my %lat ;

my $time0 ; my $time1 ;

# get parameter

$osmName = shift||'';
if (!$osmName)
{
	die (print $usage, "\n");
}

$outFileName = shift||'';
if (!$outFileName)
{
	die (print $usage, "\n");
}


print "\n$programName $version for file $osmName\n" ;

$time0 = time() ;

# get all node locations

print "get node information...\n" ;
openOsmFile ($osmName) ;

($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
if ($nodeId != -1) {
	@nodeTags = @$aRef1 ;
}

while ($nodeId != -1) {

	$lon{$nodeId} = $nodeLon ;	
	$lat{$nodeId} = $nodeLat ;	

	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
	if ($nodeId != -1) {
		@nodeTags = @$aRef1 ;
	}
}


print "get way information...\n" ;

($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {
	my $highway = "none" ;
	foreach my $key (@wayTags) {
		if (grep /^highway:/, $key) { $highway = $key ; }
	}	

	if ( (defined ($allowedPaths{$highway})) and (scalar (@wayNodes) > 1 ) ) {
		# set distances
		my $i ;
		for ($i=0; $i<$#wayNodes; $i++) {
			my $dist = distance ($lon{$wayNodes[$i]}, $lat{$wayNodes[$i]}, $lon{$wayNodes[$i+1]}, $lat{$wayNodes[$i+1]}) ;
			$edge{$wayNodes[$i]}{$wayNodes[$i+1]} = $dist ;
			$edge{$wayNodes[$i+1]}{$wayNodes[$i]} = $dist ;
		}

		# incr waycount per node (count > 0 --> crossing)
		foreach my $node (@wayNodes) {
			if (defined ($wayCount{$node})) {
				$wayCount{$node} ++ ;
			}
			else {
				$wayCount{$node} = 1 ;
			}
		}

	}	

	($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;

my $outFile ;
open ($outFile, ">", $outFileName) or die ("can't open output file!") ;
foreach my $from (keys %edge) {
	foreach my $to (keys %{$edge{$from}}) {
		print $outFile $from, " ", $to, " ", $edge{$from}{$to}, "\n" ;
	}
}
close ($outFile) ;


$time1 = time() ;
print "\n$programName finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;

