#
# onewaycheck by gary68
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

use OSM::osm ;
use File::stat;
use Time::localtime;

my $programName = "onewaycheck" ; 
my $usage = "onewaycheck.pl file.osm out.htm" ;
my $version = "1.1" ;

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
my $wayCount = 0 ;

my $oneway ;
my $reverse ;
my $problems = 0 ;
my $numCritical = 0 ;

my %to = () ;
my %from = () ;
my %wayNodeNumber = () ;
my %nodeWays = () ;
my %neededWayNodes = () ;

my %criticalNodes = () ;
my %nodesLon ;
my %nodesLat ;

my $time0 = time() ;
my $time1 ;
my $i ;
my $key ;
my $num ;
my $oneways = 0 ;

my $APIcount = 0 ;
my $APIerrors = 0 ;
my $APIrejections = 0 ;

my $html ;
my $gpx ;
my $osmName ;
my $htmlName ;


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

my $gpxName = $htmlName ;
$gpxName =~ s/htm/gpx/ ;


print "\n$programName $version for file $osmName\n" ;

#####################
# open and init files
#####################

openOsmFile ($osmName) ;


######################
# skip all nodes first
######################
print $programName, " ", $osmName, " pass0: skipping nodes...\n" ;
skipNodes () ;


#######################################
# 
#######################################
print $programName, " ", $osmName, " pass0: processing ways...\n" ;
($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}

while ($wayId != -1) {
	if (scalar (@wayNodes) >= 2) {
		$oneway = 0 ; my $highway = 0 ;
		foreach (@wayTags) {
			if ($_->[0] eq "highway") { $highway = 1 ; } 
			if ($_->[0] eq "oneway") { $oneway = 1 ; }
		}
		if (($highway == 1) and ($oneway == 1) ) { 
			$oneways++ ;
			foreach my $node (@wayNodes) {
				$neededWayNodes{$node} = 1 ;
			}
		}
	}
	else {
		#print "invalid way (one node only): ", $wayId, "\n" ;
	}

	($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;

print "$oneways oneways found.\n" ;

openOsmFile ($osmName) ;


######################
# skip all nodes first
######################
print $programName, " ", $osmName, " pass1: skipping nodes...\n" ;
skipNodes () ;


#######################################
# 
#######################################
print $programName, " ", $osmName, " pass1: processing ways...\n" ;
$oneway = 0 ;
$reverse = 0 ;
($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}

while ($wayId != -1) {
	$wayCount++ ;

	if (scalar (@wayNodes) >= 2) {
		$oneway = 0 ; $reverse = 0 ; my $highway = 0 ;
		foreach (@wayTags) {
			if ($_->[0] eq "highway") { $highway = 1 ; } 
			if ($_->[0] eq "amenity" and $_->[1] eq "parking" ) { $highway = 1 ; } 
			if ($_->[0] eq "oneway" and $_->[1] eq "true") { $oneway = 1 ; }
			if ($_->[0] eq "oneway" and $_->[1] eq "false") { $oneway = 0 ; }
			if ($_->[0] eq "oneway" and $_->[1] eq "yes") { $oneway = 1 ; }
			if ($_->[0] eq "oneway" and $_->[1] eq "no") { $oneway = 0 ; }
			if ($_->[0] eq "oneway" and $_->[1] eq "1") { $oneway = 1 ; }
			if ($_->[0] eq "oneway" and $_->[1] eq "-1") { $oneway = 1 ; $reverse = 1 ; }
		}
		if ($highway) {
			if ( ($oneway) and ($reverse)) { @wayNodes = reverse @wayNodes ; } 
	
			for (my $i = 0; $i <= $#wayNodes; $i++) {
				if (defined $neededWayNodes{$wayNodes[$i]}) {
					$to{$wayNodes[$i]} ++ ;
					$from{$wayNodes[$i]} ++ ;
					push @{$nodeWays{$wayNodes[$i]}}, $wayId ;
					$wayNodeNumber{$wayId} = scalar (@wayNodes) ;
					if ( ($i == 0) and ($oneway) ) {
						$to{$wayNodes[$i]} -- ;
					}
					if ( ($i == $#wayNodes) and ($oneway) ) {
						$from{$wayNodes[$i]} -- ;
					}
				}
			}		
		}
	}
	else {
		#print "invalid way (one node only): ", $wayId, "\n" ;
	}

	($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;

#####################
# find critical nodes
#####################
print $programName, " ", $osmName, " pass1: find critical nodes...\n" ;
%criticalNodes = () ;

foreach my $key (keys %to) {
	my $allWaysOK = 1 ;
	if (($to{$key} > 0) and ($from{$key} == 0)) {
		foreach my $wayId (@{$nodeWays{$key}}) {
			my $APIok = 1 ;
			# print "request API data for way $wayId...\n" ;
			$APIcount++ ;
			sleep (1) ; # don't stress API
			my ($id, $u, @nds, @tags, $ndsRef, $tagRef) ;
			($id, $u, $ndsRef, $tagRef) = APIgetWay ($wayId) ;
			# print "API request finished.\n" ;
			@nds = @$ndsRef ; @tags = @$tagRef ;
			if ($id == 0) { $APIerrors++ ; }
			if ( ( $wayNodeNumber{$wayId} != scalar @nds) and ($wayId == $id) ) { 
				$APIok = 0 ;
				$APIrejections++ ;
				# print "WARNING: $key ignored because way node count of osm file not equal to API node count\n" ;
			}
			if ($APIok == 0) { $allWaysOK = 0 ; }
		}

		if ($allWaysOK == 1) { 
			$criticalNodes{$key} = 1 ;
		}
	}
}


######################
# get node information
######################
print $programName, " ", $osmName, " pass2: get node information...\n" ;
openOsmFile ($osmName) ;

($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
if ($nodeId != -1) {
	#@nodeTags = @$aRef1 ;
}

while ($nodeId != -1) {
	#print "test nodeID $nodeId\n" ;
	if (defined $criticalNodes{$nodeId}) {
		$nodesLon{$nodeId} = $nodeLon ;
		$nodesLat{$nodeId} = $nodeLat ;
		#print "node info read $nodeLon $nodeLat\n" ;
	}

	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
	if ($nodeId != -1) {
		#@nodeTags = @$aRef1 ;
	}
}

closeOsmFile () ;
print $programName, " ", $osmName, " pass2: nodes read...\n" ;




$time1 = time () ;


#######################
# print info to console
#######################

print "\n", $programName, " ", $osmName, " number ways: $wayCount\n" ;
$numCritical = scalar (keys %criticalNodes) ;
print $programName, " ", $osmName, " critical nodes: ", $numCritical, "\n\n" ;
print $programName, " ", $osmName, " write HTML tables...\n" ;




##############################
# PRINT HTML / GPX INFOS NODES
##############################
open ($gpx, ">", $gpxName) || die ("Can't open gpx output file") ;
printGPXHeader ($gpx) ;


open ($html, ">", $htmlName) || die ("Can't open html output file") ;
printHTMLHeader ($html, "OnewayCheck by Gary68") ;
print $html "<H1>OnewayCheck by Gary68</H1>\n" ;
print $html "<p>Version ", $version, "</p>\n" ;
print $html "<H2>Statistics</H2>\n" ;
print $html "<p>", stringFileInfo ($osmName), "</p>\n" ;
print $html "<p>number ways: $wayCount<br>\n" ;
$numCritical = scalar (keys %criticalNodes) ;
print $html "critical nodes: ", $numCritical, "</p>\n" ;
print $html "<p>$APIcount API calls</p>" ;
print $html "<p>$APIerrors API errors</p>" ;
print $html "<p>$APIrejections API rejections</p>" ;

print $html "<H2>Critical Nodes</H2>\n" ;
print $html "<table border=\"1\">\n";
print $html "<tr>\n" ;
print $html "<th>Line</th>\n" ;
print $html "<th>NodeId</th>\n" ;
# print $html "<th>Ways</th>\n" ;
print $html "<th>OSM</th>\n" ;
print $html "<th>OSB</th>\n" ;
print $html "<th>JOSM</th>\n" ;
print $html "<th>Mapnik</th>\n" ;
print $html "<th>Osmarender</th>\n" ;
print $html "</tr>\n" ;
$i = 0 ;
#print @criticalNodes, "\n" ;
foreach $nodeId (keys %criticalNodes) {
	$i++ ;
	print $html "<tr>\n" ;
	print $html "<td>", $i , "</td>\n" ;
	print $html "<td>", historyLink ("node", $nodeId) , "</td>\n" ;

#	print $html "<td>" ;
#	foreach (@{$endNodeWays{$nodeId}}) {
#		print $html historyLink ("way", $_), " " ;
#	}
#	print $html "</td>\n" ;

	print $html "<td>", osmLink ($nodesLon{$nodeId}, $nodesLat{$nodeId}, 16) , "</td>\n" ;
	print $html "<td>", osbLink ($nodesLon{$nodeId}, $nodesLat{$nodeId}, 16) , "</td>\n" ;
	print $html "<td>", josmLinkSelectNode ($nodesLon{$nodeId}, $nodesLat{$nodeId}, 0.01, $nodeId), "</td>\n" ;
	print $html "<td>", picLinkMapnik ($nodesLon{$nodeId}, $nodesLat{$nodeId}, 16), "</td>\n" ;
	print $html "<td>", picLinkOsmarender ($nodesLon{$nodeId}, $nodesLat{$nodeId}, 16), "</td>\n" ;
	print $html "</tr>\n" ;

	# GPX
	my $text = "OnewayCheck - " . $nodeId . " problem with oneway road" ;
	printGPXWaypoint ($gpx, $nodesLon{$nodeId}, $nodesLat{$nodeId}, $text) ;

}
print $html "</table>\n" ;
print $html "<p>$i lines total</p>\n" ;




print $html "<p>", stringTimeSpent ($time1-$time0), "</p>\n" ;


########
# FINISH
########

printHTMLFoot ($html) ;
close ($html) ;

printGPXFoot ($gpx) ;
close ($gpx) ;

print "\n$APIcount API calls\n" ;
print "$APIerrors API errors\n" ;
print "$APIrejections API rejections\n" ;

print $programName, " ", $osmName, " FINISHED after ", stringTimeSpent ($time1-$time0), "\n\n" ;

