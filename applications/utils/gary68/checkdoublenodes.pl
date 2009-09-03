# 
#
#
#
# Copyright (C) 2008, Gerhard Schwanz
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

my $maxDist = 0.0001 ; # in kilometers

use strict ;
use warnings ;

use OSM::osm 5.2 ;
use File::stat;
use Time::localtime;

my $program = "checkdouble.pl" ;
my $usage = $program . " file.osm out.htm out.gpx" ;
my $version = "1.0 BETA" ;

my $nodeId ; my $nodeId2 ;
my $nodeUser ; my $nodeLat ; my $nodeLon ; my @nodeTags ;
my $aRef1 ; my $aRef2 ;

my $time0 = time() ; my $time1 ; my $timeA ;

my $html ;
my $gpx ;
my $osmName ;
my $htmlName ;
my $gpxName ;

my %lon ; my %lat ;
my %nodeTagsHash = () ;
my %nodesHash = () ;

my %neededNodes = () ;
my @dupes = () ;
my $nodeCount = 0 ;
my $numberDupes = 0 ;

###############
# get parameter
###############

$osmName = shift||'';
if (!$osmName)
{
	die (print $usage, "\n");
}

$htmlName = shift||'';
if (!$htmlName)
{
	die (print $usage, "\n");
}

$gpxName = shift||'';
if (!$gpxName)
{
	die (print $usage, "\n");
}

print "\n$program $version for file $osmName\n\n" ;








######################
# get node information
######################
print "pass1: get node positions...\n" ;
openOsmFile ($osmName) ;

($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
if ($nodeId != -1) {
	#@nodeTags = @$aRef1 ;
}

while ($nodeId != -1) {
	$nodeCount++ ;

	my ($hashValue) = int ($nodeLon * 1000) * 1000000 + int ($nodeLat * 1000) ;
	push @{$nodesHash{$hashValue}}, $nodeId ;

	$lon{$nodeId} = $nodeLon ;
	$lat{$nodeId} = $nodeLat ;

	# next
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
	if ($nodeId != -1) {
		#@nodeTags = @$aRef1 ;
	}
}
closeOsmFile () ;

my ($total) = scalar (keys %nodesHash) ;
my $progress = 0 ;
foreach my $key (keys %nodesHash) {

	#print "$key: @{$nodesHash{$key}}\n" ;

	$progress++ ;
	if ($progress % 10000 == 0) {
		my ($done) = int ($progress / $total * 100) ;
		print "$done percent done...\n" ; 
	}

	foreach my $n1 (@{$nodesHash{$key}}) {
		foreach my $n2 (@{$nodesHash{$key}}) {
			if ($n1 < $n2) {
				if ( ($lon{$n1} == $lon{$n2}) and ($lat{$n1} == $lat{$n2}) ) {
					$numberDupes++ ;
					push @dupes, [$n1, $n2, "exact same position", 0] ;
					$neededNodes{$n1} = 1 ;
					$neededNodes{$n2} = 1 ;
				}
				else {
					my ($dist) = distance ($lon{$n1}, $lat{$n1}, $lon{$n2}, $lat{$n2}) ;
					if ( $dist < $maxDist) {
						$numberDupes++ ;
						push @dupes, [$n1, $n2, "under threshold distance", int($dist*100000)/100] ;
						$neededNodes{$n1} = 1 ;
						$neededNodes{$n2} = 1 ;
					}
				}
			}
		}
	}
}


######################
# get node information
######################
print "pass2: get node tags...\n" ;
openOsmFile ($osmName) ;

($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
if ($nodeId != -1) {
	@nodeTags = @$aRef1 ;
}

while ($nodeId != -1) {
	
	if (defined $neededNodes{$nodeId}) {
		@{$nodeTagsHash{$nodeId}} = @nodeTags ;
	}

	# next
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
	if ($nodeId != -1) {
		@nodeTags = @$aRef1 ;
	}
}
closeOsmFile () ;




$time1 = time () ;


##################
# PRINT HTML INFOS
##################
print "\nwrite HTML tables and GPX file...\n" ;

open ($html, ">", $htmlName) || die ("Can't open html output file") ;
open ($gpx, ">", $gpxName) || die ("Can't open gpx output file") ;


printHTMLiFrameHeader ($html, "Double Node Check by Gary68") ;
printGPXHeader ($gpx) ;

print $html "<H1>Double Node Check by Gary68</H1>\n" ;
print $html "<p>Version ", $version, "</p>\n" ;
print $html "<H2>Statistics</H2>\n" ;
print $html "<p>", stringFileInfo ($osmName), "<br>\n" ;
print $html "number nodes total: $nodeCount<br>\n" ;
print $html "number dupes $numberDupes<br>\n" ;



print $html "<H2>Data</H2>\n" ;
print $html "<table border=\"1\">\n";
print $html "<tr>\n" ;
print $html "<th>Line</th>\n" ;
print $html "<th>Problem</th>\n" ;
print $html "<th>Node 1</th>\n" ;
print $html "<th>Node 2</th>\n" ;
print $html "<th>Distance (m)</th>\n" ;
print $html "<th>JOSM</th>\n" ;
print $html "<th>OSM</th>\n" ;
print $html "<th>OSB</th>\n" ;
print $html "</tr>\n" ;

my $i = 0 ;
foreach my $d (@dupes) {
	$i++ ;
	# HTML
	print $html "<tr>\n" ;
	print $html "<td>", $i , "</td>\n" ;
	print $html "<td>", $d->[2] , "</td>\n" ;
	print $html "<td>", historyLink("node", $d->[0]) , "<br>\n" ;
	if ( scalar (@{$nodeTagsHash{$d->[0]}}) > 0 ) { 
		foreach my $t (@{$nodeTagsHash{$d->[0]}}) { 
			print $html $t->[0] . " : " . $t->[1] . "<br>\n" ; 
		} 
	}
	else {
		print $html "no tags<br>\n" ;
	}
	print $html "</td>\n" ;
	print $html "<td>", historyLink("node", $d->[1]), "<br>\n" ;
	if (scalar (@{$nodeTagsHash{$d->[1]}}) > 0) { 
		foreach my $t (@{$nodeTagsHash{$d->[1]}}) { 
			print $html "$t->[0] : $t->[1]<br>\n" ; 
		}
	}
	else {
		print $html "no tags<br>\n" ;
	}
	print $html "</td>\n" ;
	print $html "<td>", $d->[3] , "</td>\n" ;
	print $html "<td>", josmLinkSelectNodes ($lon{$d->[0]}, $lat{$d->[0]}, 0.001, $d->[0], $d->[1]), "</td>\n" ;
	print $html "<td>", osmLink ($lon{$d->[0]}, $lat{$d->[0]}, 17) , "<br>\n" ;
	print $html "<td>", osbLink ($lon{$d->[0]}, $lat{$d->[0]}, 17) , "<br>\n" ;
	print $html "</tr>\n" ;
	
	# GPX
	my ($text) = "node dupes " . $d->[0] . " " . $d->[1] . " - " . $d->[2] ;
	printGPXWaypoint ($gpx, $lon{$d->[0]}, $lat{$d->[0]}, $text) ;
}
print $html "</table>\n" ;
print $html "<p>$i lines total</p>\n" ;



########
# FINISH
########

print $html "<p>", stringTimeSpent ($time1-$time0), "</p>\n" ;
printHTMLFoot ($html) ;
printGPXFoot ($gpx) ;

close ($html) ;
close ($gpx) ;

print "$total different hash values.\n" ;
print "$numberDupes node dupes found.\n" ;
print "\n$program finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;


