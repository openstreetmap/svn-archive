# 
#
# checkconn.pl by gary68
#
# this program check for connections at start or end of a way. this is intended to check motorways, trunks, their links, primary, secondary and
# tertiary highways. it might not be too useful for i.e. highway=residential
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
# example definition file:
# (IMPORTANT: don't enter a tag in both sections!)
#
#<XML>
#  <k="check" v="highway:motorway">
#  <k="check" v="highway:motorway_link">
#  <k="check" v="highway:trunk">
#  <k="check" v="highway:trunk_link">
#  <k="against" v="highway:primary">
#  <k="against" v="highway:primary_link">
#  <k="against" v="highway:secondary">
#  <k="against" v="highway:tertiary">
#  <k="against" v="junction:roundabout">
#</XML>
#
# Version 2.0
# 001 
# - new improved algorythm
# 002
# - gpx format support
# - needed nodes sorted removed
#
# Version 2.1
# - add stat output
#
# Version 2.2
# - add stat output 2
#
# Version 3.0
# - add boundary file support
#

use strict ;
use warnings ;

use OSM::osm ;
use File::stat;
use Time::localtime;

my $program = "checkconn.pl" ;
my $usage = $program . " def.xml file.osm out.htm out.gpx boundary.poly" ;
my $version = "3.0" ;

my $borderThreshold = 2 ; # in km

my $wayId ; my $wayId2 ;
my $wayUser ; my @wayNodes ; my @wayTags ;
my $nodeId ; my $nodeId2 ;
my $nodeUser ; my $nodeLat ; my $nodeLon ; my @nodeTags ;
my $aRef1 ; my $aRef2 ;
my $wayCount = 0 ;
my $againstCount = 0 ;
my $checkWayCount = 0 ;
my $againstWayCount = 0 ;
my $checkedWays = 0 ;
my $invalidWays ;
my $borderCollisions = 0 ;

my @check ;
my @against ;
my @borderWay = () ;

my $time0 = time() ; my $time1 ; my $timeA ;
my $i ;
my $key ;
my $num ;
my $tag1 ; my $tag2 ;
my $progress ;

my $html ;
my $def ;
my $gpx ;
my $osmName ;
my $htmlName ;
my $defName ;
my $gpxName ;
my $borderFileName = "" ;

my @wayStat = () ; # 0= fully connected; 3= unconnected; 2= end unconnected; 1= start unconnected
my @cat1 ; my %cat1hash ;
my @allWayNodes ;
my @allCat1Nodes ;
my %cat1Connected ;
my @cat1Nodes ; my @cat12Nodes ;

my @neededNodes ;
my %lon ; my %lat ;
my %wayStart ; my %wayEnd ; my %wayStat ;

my $maxBorderCheckTime = 0 ;
my $totalBorderCheckTime = 0 ;

###############
# get parameter
###############
$defName = shift||'';
if (!$defName)
{
	die (print $usage, "\n");
}

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

$borderFileName = shift||'';
if (!$borderFileName)
{
	$borderFileName = "" ;
}

print "\n$program $version for file $osmName\n\n" ;


if ($borderFileName ne "") {
	readBorder ($borderFileName) ;
}


##################
# read definitions
##################

print "read definitions $defName...\n" ;
open ($def, , "<", $defName) or die "definition file $defName not found" ;

while (my $line = <$def>) {
	#print "read line: ", $line, "\n" ;
	my ($k)   = ($line =~ /^\s*<k=[\'\"]([:\w\s\d]+)[\'\"]/); # get key
	my ($v) = ($line =~ /^.+v=[\'\"]([:\w\s\d]+)[\'\"]/);       # get value
	
	if ($k and defined ($v)) {
		#print "key: ", $k, "\n" ;
		#print "val: ", $v, "\n" ;

		if ($k eq "check") {
			push @check, $v ;
		}
		if ($k eq "against") {
			push @against, $v ;
		}
	}
}

close ($def) ;


# TODO: remove check from against, if specified!


print "Check ways: " ;
foreach (@check) { print $_, " " ;} print "\n" ;
print "Against: " ;
foreach (@against) { print $_, " " ;} print "\n\n" ;



######################
# skip all nodes first
######################
openOsmFile ($osmName) ;
print "pass1: skipping nodes...\n" ;
skipNodes () ;


#############################
# identify check/against ways
#############################
print "pass1: identify check ways...\n" ;
($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {	
	$wayCount++ ;
	if (scalar (@wayNodes) >= 2) {

		my $found = 0 ;
		my $round = 0 ;
		foreach $tag1 (@wayTags) {
			if ($tag1 eq "junction:roundabout") { $round = 1 ; }
			foreach $tag2 (@check) {
				if ($tag1 eq $tag2) { $found = 1 ; }
			}
		}
		if (($found) and ($round == 0)) { 
			push @cat1, $wayId ; 
			$checkWayCount++ ; 
			$wayStart{$wayId} = $wayNodes[0] ; 
			$wayEnd{$wayId} = $wayNodes[-1] ; 
			$wayStat{$wayId} = 3 ;
			push @allWayNodes, @wayNodes ;
			push @allCat1Nodes, ($wayNodes[0], $wayNodes[-1]) ;
		}

		$found = 0 ;
		foreach $tag1 (@wayTags) {
			foreach $tag2 (@against) {
				if ($tag1 eq $tag2) { $found = 1 ; }
			}
		}
		if ($found) {
			$againstWayCount++ ;
			push @allWayNodes, @wayNodes ;
		}
	}
	else {
		#print "invalid way (one node only): ", $wayId, "\n" ;
		$invalidWays++ ;
	}

	# next way
	($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;

print "number total ways: $wayCount\n" ;
print "number invalid ways (1 node only): $invalidWays\n" ;
print "number check ways: $checkWayCount\n" ;
print "number against ways: $againstWayCount\n" ;

$" = " " ;
#print "Cat1 ways: @cat1\n" ;
#print "Cat1 nodes: @allCat1Nodes\n" ;
#print "All way nodes: @allWayNodes\n" ;



###############################
# pass 2; check for connections
###############################
print "check for connections...\n" ;

$progress = 0 ;
$timeA = time() ;

# init cat1Connected
foreach (@allCat1Nodes) { $cat1Connected{$_} = 0 ; }

# sort cat 1
print "sort cat1 nodes...\n" ;
@allCat1Nodes = sort {$a <=> $b} @allCat1Nodes ;

#print "Cat1 nodes sorted: @allCat1Nodes\n" ;


# find doubles in allCat1Nodes and mark as connected in @cat1Connected
# remove doubles completely, create @cat12Nodes

print "find doubles in cat1 nodes...\n" ;

my $actualId = $allCat1Nodes[0] ;
my $actualNumber = 1 ;
my $actualIndex = 0 ;
while ($actualIndex < $#allCat1Nodes) {
	$actualIndex++ ;
	if ($allCat1Nodes[$actualIndex] == $actualId) { 
		$actualNumber++ ;
	}
	else
	{
		if ($actualNumber > 1) {
			$cat1Connected{$actualId} = 1 ;
			#print "cat1/cat1 connection found on node id = $actualId\n" ;
		}
		else {
			push @cat12Nodes, $actualId ;
		}
		$actualNumber = 1 ;
		$actualId = $allCat1Nodes[$actualIndex] ;
	}
}
if ($actualNumber > 1) {
	$cat1Connected{$actualId} = 1 ;
	#print "cat1/cat1 connection found on node id = $actualId\n" ;
}
else {
	push @cat12Nodes, $actualId ;
}

print "sort cat1-2 way nodes...\n" ;
@cat12Nodes = sort {$a <=> $b} @cat12Nodes ;

#print "Cat12 nodes sorted: @cat12Nodes\n" ;


# sort allWayNodes
print "sort all way nodes...\n" ;
@allWayNodes = sort {$a <=> $b} @allWayNodes ;

#print "All way nodes sorted: @allWayNodes\n" ;



# init loop allWayNodes (index)
# loop through cat12Nodes ascending
	# inc indexAll until >= cat12NodeId
	# if == then check for number occurrences
	# if num occurrences > 1 then mark cat12NodeId as connected
#

print "big loop running...\n" ;

my $cat1Index = 0 ;
my $allIndex = 0 ;
my $actualCat1Id = $cat12Nodes[0] ;
my $actualAllId = $allWayNodes[0] ;

while ($cat1Index <= $#cat12Nodes) {
	while ( ($allWayNodes[$allIndex] < $cat12Nodes[$cat1Index]) and ($allIndex < $#allWayNodes) ) {$allIndex++}
	if ($allWayNodes[$allIndex] == $cat12Nodes[$cat1Index]) {
		if ( ($allIndex < $#allWayNodes) and ($allWayNodes[$allIndex+1] == $cat12Nodes[$cat1Index]) ) {
			$cat1Connected{$cat12Nodes[$cat1Index]} = 1 ;
			#print "cat12 node found > 1x in allWayNodes id =", $cat12Nodes[$cat1Index], "\n" ;
		}
	}
	$cat1Index++ ;
}


# check all starts and end for connection
# waystat filled

foreach $wayId (@cat1) {
	# check start
	if ($cat1Connected{$wayStart{$wayId}} == 1) { 
		if ($wayStat{$wayId} == 1) { $wayStat{$wayId} = 0 ; }
		if ($wayStat{$wayId} == 3) { $wayStat{$wayId} = 2 ; }
	} 
	
	# check end
	if ($cat1Connected{$wayEnd{$wayId}} == 1) { 
		if ($wayStat{$wayId} == 2) { $wayStat{$wayId} = 0 ; }
		if ($wayStat{$wayId} == 3) { $wayStat{$wayId} = 1 ; }
	} 
}




#print "status\n" ;
#foreach (@cat1) { print "$_ $wayStat{$_}\n" ; }
#print "\n" ;


######################
# collect needed nodes
######################
print "collect needed nodes...\n" ;
foreach $wayId (@cat1) {
	if (($wayStat{$wayId} && 1) == 1) {
		push @neededNodes, $wayStart{$wayId} ;
	}
	if (($wayStat{$wayId} && 2) == 2) {
		push @neededNodes, $wayEnd{$wayId} ;
	}
}


######################
# get node information
######################
print "pass2: get node information...\n" ;
openOsmFile ($osmName) ;

@neededNodes = sort { $a <=> $b } @neededNodes ;

($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
if ($nodeId != -1) {
	#@nodeTags = @$aRef1 ;
}

while ($nodeId != -1) {
	my $needed = 0 ;

	$needed = binSearch ($nodeId, \@neededNodes ) ;

	if ($needed >= 0) { $lon{$nodeId} = $nodeLon ; $lat{$nodeId} = $nodeLat }

	# next
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
	if ($nodeId != -1) {
		#@nodeTags = @$aRef1 ;
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


printHTMLHeader ($html, "Connection Check by Gary68") ;
printGPXHeader ($gpx) ;

print $html "<H1>Connection Check by Gary68</H1>\n" ;
print $html "<p>Version ", $version, "</p>\n" ;
print $html "<H2>Statistics</H2>\n" ;
print $html "<p>", stringFileInfo ($osmName), "<br>\n" ;
print $html "number ways total: $wayCount<br>\n" ;
print $html "number invalid ways (1 node only): $invalidWays<br>\n" ;
print $html "number check ways: $checkWayCount<br>\n" ;
print $html "number against ways: $againstWayCount</p>\n" ;
print $html "<p>border file: $borderFileName<br>\n" ;
print $html "min border distance: $borderThreshold</p>\n" ;


print $html "<p>Check ways: " ;
foreach (@check) { print $html $_, " " ;} print $html "</p>\n" ;
print $html "<p>Against: " ;
foreach (@against) { print $html $_, " " ;} print $html "</p>\n" ;


print $html "<H2>Unconnected Start/End</H2>\n" ;
print $html "<p>These ways are either unconnected at start or end (or both). " ;
print $html " Please be aware that most osm files are excerpts of some sort with cut ways at their limits. " ;
print $html " This causes false positives! In case of countries or other entities with boundaries they can " ;
print $html " easily be spotted in maps or JOSM because there are borders in the vincinity.</p>" ;
print $html "<table border=\"1\">\n";
print $html "<tr>\n" ;
print $html "<th>Line</th>\n" ;
print $html "<th>WayId</th>\n" ;
print $html "<th>Unconnected</th>\n" ;
print $html "<th>OSM/OSB Start</th>\n" ;
print $html "<th>JOSM Start</th>\n" ;
print $html "<th>OSM/OSB End</th>\n" ;
print $html "<th>JOSM End</th>\n" ;
print $html "<th>Pic Start</th>\n" ;
print $html "<th>Pic End</th>\n" ;
print $html "</tr>\n" ;
$i = 0 ;
foreach $wayId (@cat1) {

	# border check
	if ( ($wayStat{$wayId} == 1) or ($wayStat{$wayId} == 3) ) {
		if ( ! minDistToBorderOK ($wayStart{$wayId})   ) {
			$wayStat{$wayId} = $wayStat{$wayId} - 1 ;
			#print "dropped start node of way $wayId because low dist to border node\n" ;
			$borderCollisions++ ;
		}
	}
	if ( ($wayStat{$wayId} == 2) or ($wayStat{$wayId} == 3) ) {
		if ( ! minDistToBorderOK ($wayEnd{$wayId})   ) {
			$wayStat{$wayId} = $wayStat{$wayId} - 2 ;
			#print "dropped end node of way $wayId because low dist to border node\n" ;
			$borderCollisions++ ;
		}
	}


	if ($wayStat{$wayId} > 0) {
		$i++ ;

		my $status ;
		if ($wayStat{$wayId} == 1) { $status = "start" ; } 
		if ($wayStat{$wayId} == 2) { $status = "end" ; } 
		if ($wayStat{$wayId} == 3) { $status = "start/end" ; } 

		# HTML
		print $html "<tr>\n" ;
		print $html "<td>", $i , "</td>\n" ;
		print $html "<td>", historyLink ("way", $wayId) , "</td>\n" ;
		print $html "<td>", $status , "</td>\n" ;

		print $html "<td>start ", osmLink ($lon{$wayStart{$wayId}}, $lat{$wayStart{$wayId}}, 16) , "<br>\n" ;
		print $html "start ", osbLink ($lon{$wayStart{$wayId}}, $lat{$wayStart{$wayId}}, 16) , "</td>\n" ;
		print $html "<td>start ", josmLink ($lon{$wayStart{$wayId}}, $lat{$wayStart{$wayId}}, 0.01, $wayId), "</td>\n" ;

		print $html "<td>end ", osmLink ($lon{$wayEnd{$wayId}}, $lat{$wayEnd{$wayId}}, 16) , "<br>\n" ;
		print $html "end ", osbLink ($lon{$wayEnd{$wayId}}, $lat{$wayEnd{$wayId}}, 16) , "</td>\n" ;
		print $html "<td>end ", josmLink ($lon{$wayEnd{$wayId}}, $lat{$wayEnd{$wayId}}, 0.01, $wayId), "</td>\n" ;

		print $html "<td>", picLinkOsmarender ($lon{$wayStart{$wayId}}, $lat{$wayStart{$wayId}}, 16), "</td>\n" ;
		print $html "<td>", picLinkOsmarender ($lon{$wayEnd{$wayId}}, $lat{$wayEnd{$wayId}}, 16), "</td>\n" ;
		print $html "</tr>\n" ;

		# GPX
		if (($wayStat{$wayId} == 1) or ($wayStat{$wayId} == 3) ) { 
			my ($text) = "ChkCon - " . $defName . " - way start unconnected" ;
			printGPXWaypoint ($gpx, $lon{$wayStart{$wayId}}, $lat{$wayStart{$wayId}}, $text) ;
		} 
		if (($wayStat{$wayId} == 2) or ($wayStat{$wayId} == 3) ) { 
			my ($text) = "ChkCon - " . $defName . " - way end unconnected" ;
			printGPXWaypoint ($gpx, $lon{$wayEnd{$wayId}}, $lat{$wayEnd{$wayId}}, $text) ;
		} 
	}
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

statistics ( ctime(stat($osmName)->mtime),  $program,  $defName, $osmName,  $checkWayCount,  $i) ;

print "\nborder collisions found: $borderCollisions\n" ;
print "\n$program finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;


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



sub readBorder {
	my ($borderFileName) = shift ;
	my $borderFile ;
	my $line ;
	my $id = 0 ;
	my $dist ;
	my $lastLon = 0 ; my $lastLat = 0 ; my $maxDist = 0 ;
	
	open ($borderFile, "<", $borderFileName) || die ("couldn't open border file");
	print "parsing border file...\n" ;	
	$line = <$borderFile> ;
	$line = <$borderFile> ;
	$line = <$borderFile> ;
	while (! (grep /END/, $line) ) {
		$id-- ; # negative ids for border nodes
		#($lo, $la) = sscanf ("%g %g", $line) ;
		#print "line: $line\n" ;
		my ($lo, $la)   = ($line =~ /^\s*([\-\+\d\.Ee]+)\s+([\-\+\d\.Ee]+)+/ ) ;	
		if (!defined ($lo))  { print "id: $id line: $line\n" ; }
		$lon{$id} = $lo ; $lat{$id} = $la ;
		if ($lastLon == 0) {
			$lastLon = $lo ;
			$lastLat = $la ;
		}
		push @borderWay, $id ;
		$line = <$borderFile> ;
		$dist = distance ($lo, $la, $lastLon, $lastLat) ;
		if ($dist > $maxDist) { $maxDist = $dist ; }
		#printf "%3d \n", distance ($lo, $la, $lastLon, $lastLat) ;
		$lastLon = $lo ;
		$lastLat = $la ;
	}
	close ($borderFile) ;
	print $id*(-1), " border nodes read.\nmax distance between border nodes: $maxDist\n\n" ;
}

sub minDistToBorderOK {
	my (@nodes) = @_ ;
	my $way ; my $node ; my $borderNode ;
	my $ok = 1 ;
	#print "checking distance...\n" ;

	my ($startTime) = time() ;
	loopA: 
	foreach $node (@nodes) {
		foreach $borderNode (@borderWay) {
			my ($dist) = distance ($lon{$borderNode}, $lat{$borderNode}, $lon{$node}, $lat{$node}) ;
			if ($dist < $borderThreshold) { 
				$ok = 0 ; 
				last loopA ;  
			}
		}
	}

	my ($secs) = time() - $startTime ;
	#print "done extensive border check in $secs seconds...\n" ;
	$totalBorderCheckTime += $secs ;
	if ( $secs > $maxBorderCheckTime ) {
		$maxBorderCheckTime = $secs ;
		print "max border check now $maxBorderCheckTime secs\n" ;
	}

	return $ok ;
}
