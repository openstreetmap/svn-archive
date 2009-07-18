#
# motorwaycheck by gary68
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
# Version 1.1 / 1.2
# - stat
#

use strict ;
use warnings ;

use OSM::osm ;
use File::stat;
use Time::localtime;

my $programName = "motorwaycheck" ; 
my $usage = "motorwaycheck.pl file.osm out.htm" ;
my $version = "1.2" ;

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

my $motorway ;
my $onewayTag ;
my $onewayTagWrong ;
my $reverse ;
my $problems = 0 ;
my $motorways = 0 ;
my $numCritical = 0 ;

my @onewayProblems ;
my %endNodeWays ;
my @criticalNodes = () ;
my %nodesLon ;
my %nodesLat ;

my $time0 = time() ;
my $time1 ;
my $i ;
my $key ;
my $num ;

my $html ;
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

print "\n$programName $version for file $osmName\n" ;

#####################
# open and init files
#####################

openOsmFile ($osmName) ;

open ($html, ">", $htmlName) || die ("Can't open html output file") ;
printHTMLHeader ($html, "MotorwayCheck by Gary68") ;
print $html "<H1>MotorwayCheck by Gary68</H1>\n" ;
print $html "<p>Version ", $version, "</p>\n" ;
print $html "<H2>Statistics</H2>\n" ;
print $html "<p>", stringFileInfo ($osmName), "</p>\n" ;


######################
# skip all nodes first
######################
print $programName, " ", $osmName, " pass1: skipping nodes...\n" ;
skipNodes () ;


#######################################
# check for missing/invalid oneway tags
#######################################
print $programName, " ", $osmName, " pass1: processing ways...\n" ;
$motorway = 0 ;
$onewayTag = 0 ;
$onewayTagWrong = 0 ;
$reverse = 0 ;
($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}

while ($wayId != -1) {
	$wayCount++ ;

	if (scalar (@wayNodes) >= 2) {

		foreach (@wayTags) {
			if ($_ eq "highway:motorway") { $motorway = 1 ; }
			if ($_ eq "oneway:true") { $onewayTag = 1 ; }
			if ($_ eq "oneway:false") { $onewayTag = 1 ; }
			if ($_ eq "oneway:yes") { $onewayTag = 1 ; }
			if ($_ eq "oneway:no") { $onewayTag = 1 ; }
			if ($_ eq "oneway:1") { $onewayTag = 1 ; }
			if ($_ eq "oneway:-1") { $onewayTag = 1 ; $reverse = 1 ; }
			if ( (grep /oneway/, $_) and ($onewayTag == 0) ) { $onewayTagWrong = 1 ; }
		}
		if ($motorway == 1) { $motorways++ ;} 

		if (($motorway == 1) and ($onewayTagWrong == 1)) {
			$problems++ ;
			push @onewayProblems, $wayId ;
			#print $wayId, "\n" ;
		}

		if ($motorway == 1) {
			my $start = $wayNodes[0] ;
			my $end = $wayNodes[-1] ;
			if ($reverse == 1) { $end = $start ; }
			push @{$endNodeWays{$end}}, $wayId ;
		}
	}
	else {
		#print "invalid way (one node only): ", $wayId, "\n" ;
	}

	$motorway = 0 ;
	$onewayTag = 0 ;
	$onewayTagWrong = 0 ;
	$reverse = 0 ;
	($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
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
@criticalNodes = () ;
foreach $key (keys %endNodeWays) {
	my ($num) = scalar (@{$endNodeWays{$key}}) ;
	#print $key, " ", $num, " " ;
	if ($num > 1) {
		push @criticalNodes, $key ;
		#print "node pushed" ;
	}
	#print "\n" ;
}

#foreach (@criticalNodes) {
#	print "critical $_\n" ;
#}


######################
# get node information
######################
print $programName, " ", $osmName, " pass2: get node information...\n" ;
openOsmFile ($osmName) ;

($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
if ($nodeId != -1) {
	#@nodeTags = @$aRef1 ;
}

while ($nodeId != -1) {
	#print "test nodeID $nodeId\n" ;
	foreach (@criticalNodes) {
		if ($_ == $nodeId) {
			$nodesLon{$nodeId} = $nodeLon ;
			$nodesLat{$nodeId} = $nodeLat ;
			#print "node info read $nodeLon $nodeLat\n" ;
		}
	}

	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
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
print $programName, " ", $osmName, " number motorways: $motorways\n" ;
print $programName, " ", $osmName, " oneway tag invalid problems: $problems\n" ;
$numCritical = scalar (@criticalNodes) ;
print $programName, " ", $osmName, " critical nodes: ", $numCritical, "\n\n" ;

print $programName, " ", $osmName, " write HTML tables...\n" ;


print $html "<p>number ways: $wayCount<br>\n" ;
print $html "number motorways: $motorways<br>\n" ;
print $html "oneway tag invalid: $problems<br>" ;
$numCritical = scalar (@criticalNodes) ;
print $html "critical nodes: ", $numCritical, "</p>\n" ;


#######################
# PRINT HTML INFOS TAGS
#######################
print $html "<H2>Oneway tag invalid according to Map Features</H2>\n" ;
print $html "<table border=\"1\">\n";
print $html "<tr>\n" ;
print $html "<th>Line</th>\n" ;
print $html "<th>WayId</th>\n" ;
#print $html "<th>OSM</th>\n" ;
#print $html "<th>OSB</th>\n" ;
#print $html "<th>JOSM</th>\n" ;
print $html "</tr>\n" ;
$i = 0 ;
foreach $wayId (@onewayProblems) {
	$i++ ;
	print $html "<tr>\n" ;
	print $html "<td>", $i , "</td>\n" ;
	print $html "<td>", historyLink ("way", $wayId) , "</td>\n" ;
	print $html "</tr>\n" ;
}
print $html "</table>\n" ;
print $html "<p>$i lines total</p>\n" ;


########################
# PRINT HTML INFOS NODES
########################
print $html "<H2>Critical Nodes</H2>\n" ;
print $html "<p>At these nodes more than one motorway has an end node. Maybe wrong oneway tags, direction wrong or highway doubly mapped!</p>" ;
print $html "<table border=\"1\">\n";
print $html "<tr>\n" ;
print $html "<th>Line</th>\n" ;
print $html "<th>NodeId</th>\n" ;
print $html "<th>Ways</th>\n" ;
print $html "<th>OSM</th>\n" ;
print $html "<th>OSB</th>\n" ;
print $html "<th>JOSM</th>\n" ;
print $html "<th>Mapnik</th>\n" ;
print $html "<th>Osmarender</th>\n" ;
print $html "</tr>\n" ;
$i = 0 ;
#print @criticalNodes, "\n" ;
foreach $nodeId (@criticalNodes) {
	$i++ ;
	print $html "<tr>\n" ;
	print $html "<td>", $i , "</td>\n" ;
	print $html "<td>", historyLink ("node", $nodeId) , "</td>\n" ;

	print $html "<td>" ;
	foreach (@{$endNodeWays{$nodeId}}) {
		print $html historyLink ("way", $_), " " ;
	}
	print $html "</td>\n" ;

	print $html "<td>", osmLink ($nodesLon{$nodeId}, $nodesLat{$nodeId}, 16) , "</td>\n" ;
	print $html "<td>", osbLink ($nodesLon{$nodeId}, $nodesLat{$nodeId}, 16) , "</td>\n" ;
	print $html "<td>", josmLink ($nodesLon{$nodeId}, $nodesLat{$nodeId}, 0.01, ${$endNodeWays{$nodeId}}[0]), "</td>\n" ;
	print $html "<td>", picLinkMapnik ($nodesLon{$nodeId}, $nodesLat{$nodeId}, 16), "</td>\n" ;
	print $html "<td>", picLinkOsmarender ($nodesLon{$nodeId}, $nodesLat{$nodeId}, 16), "</td>\n" ;
	print $html "</tr>\n" ;
}
print $html "</table>\n" ;
print $html "<p>$i lines total</p>\n" ;




print $html "<p>", stringTimeSpent ($time1-$time0), "</p>\n" ;


########
# FINISH
########

printHTMLFoot ($html) ;
close ($html) ;

statistics ( ctime(stat($osmName)->mtime),  $programName,  "motorway", $osmName,  $motorways,  $i) ;

print $programName, " ", $osmName, " FINISHED after ", stringTimeSpent ($time1-$time0), "\n\n" ;
sub statistics {
	my ($date, $program, $def, $area, $total, $errors) = @_ ;
	my $statfile ; my ($statfileName) = "statistics.csv" ;

	if (grep /\.bz2/, $area) { $area =~ s/\.bz2// ; }
	if (grep /\.osm/, $area) { $area =~ s/\.osm// ; }
	my ($area2) = ($area =~ /.+\/([\w\-]+)$/ ) ;

	if (grep /\.xml/, $def) { $def =~ s/\.xml// ; }
	my ($def2) = ($def =~ /([\w\d\_]+)$/ ) ;

	my ($success) = open ($statfile, "<", $statfileName) ;

	if ($success) {
		print "statfile found. writing stats...\n" ;
		close $statfile ;
		open $statfile, ">>", $statfileName ;
		printf $statfile "%02d.%02d.%4d;", localtime->mday(), localtime->mon()+1, localtime->year() + 1900 ;
		printf $statfile "%02d/%02d/%4d;", localtime->mon()+1, localtime->mday(), localtime->year() + 1900 ;
		print $statfile $date, ";" ;
		print $statfile $program, ";" ;
		print $statfile $def2, ";" ;
		print $statfile $area2, ";" ;
		print $statfile $total, ";" ;
		print $statfile $errors ;
		print $statfile "\n" ;
		close $statfile ;
	}
	return ;
}
