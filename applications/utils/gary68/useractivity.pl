#!/usr/bin/perl 
#
# useractivity.pl by gary68
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
# Version 2
# - map support added
# - way support added
#
# Version 2.1
# - separate files for time slots (privacy)
#
# Version 2.2
# - some bugs fixed
#

use strict ;
use warnings ;

use OSM::osm ;
use OSM::osmgraph ;
use File::stat ;
use Time::localtime ;
use Compress::Bzip2 ;

my $program = "useractivity.pl" ;
my $usage = $program . " file1.osm file2.osm out.htm Mode [numTopUsers] [picSize] (Mode = [N|P|D|S], picSize x in pixels)\n" ;
$usage .= "N = normal\nP = with picture\nPD = with detailed picture\nPS/PDS = also write SVG file\n" ;
my $version = "2.1" ;

my $topMax = 10 ;

my %lon1 ; my %lat1 ; my %lon2 ; my %lat2 ;
my $wayId1 ; my $wayVersion1 ; my $wayTimestamp1 ; my $wayUid1 ; my $wayUser1 ; my $wayChangeset1 ; my @wayNodes1 ; my @wayTags1 ;
my $wayId2 ; my $wayVersion2 ; my $wayTimestamp2 ; my $wayUid2 ; my $wayUser2 ; my $wayChangeset2 ; my @wayNodes2 ; my @wayTags2 ;

my $nodeId1 ; my $nodeUser1 ; my $nodeLat1 ; my $nodeLon1 ; my @nodeTags1 ; my $nodeVersion1 ; my $nodeTimestamp1 ; my $nodeUid1 ; my $nodeChangeset1 ;
my $nodeId2 ; my $nodeUser2 ; my $nodeLat2 ; my $nodeLon2 ; my @nodeTags2 ; my $nodeVersion2 ; my $nodeTimestamp2 ; my $nodeUid2 ; my $nodeChangeset2 ;
my $aRef1 ; my $aRef2 ;

my $time0 = time() ; 

my $mode = "" ; my $sizeX = 1024 ;
my $html ; my $htmlName ;
my $osm1Name ; my $osm2Name ;

my $file1 ; my $file2 ; my $bz1 ; my $bz2 ; my $isBz21 ; my $isBz22 ; my $line1 ; my $line2 ; my $bzerrno ;
my $file1Name ; my $file2Name ;

my %minLon ; my %minLat ; my %maxLon ; my %maxLat ; # per user
my $deletedNodes = 0 ; my $deletedNodesWithTags = 0 ;
my $deletedWays = 0 ; my $deletedWaysWithTags = 0 ;
my %nodesAdded ; my %nodesMovedNumber ; my %nodesMovedDistance ; my %nodesMovedMax ;
my %waysAdded ;
my %tagsAdded ; my %tagsDeleted ; my %tagsRenamed ; my %tagsReclassified ; my %tagsRereffed ;
my %tagsDeletedName ;
my %renames ; my %reclassifications ; my %rerefs ;
my @deletedNodesIds = () ; my %deletedNodesTags = () ;
my @deletedWaysIds = () ; my %deletedWaysTags = () ;
my %versionJumpsNodes = () ;
my %versionJumpsWays = () ;
my %opTime ;
my %neededNodes = () ;

my $reName ; my $reClass ; my $reRef ;
my $name1 ; my $name2 ; my $ref1 ; my $ref2 ; my $class1 ; my $class2 ;

my $objectProcessed = 0 ; # 0=node, 1=way

###############
# get parameter
###############
$osm1Name = shift||'';
if (!$osm1Name) { die (print $usage, "\n") ; }

$osm2Name = shift||'';
if (!$osm2Name) { die (print $usage, "\n") ; }

$htmlName = shift||'';
if (!$htmlName) { die (print $usage, "\n") ; }

$mode = shift||'';
if (!$mode) {	$mode = "N" ; }

$topMax = shift||'';
if (!$topMax) {	$topMax = 10 ; }

$sizeX = shift||'';
if (!$sizeX) {	$sizeX = 1024 ; }

print "\n$program $version for files:\n\n" ;
print stringFileInfo ($osm1Name), "\n"  ;
print stringFileInfo ($osm2Name), "\n\n"  ;

#------------------------------------------------------------------------------------
# Main
#------------------------------------------------------------------------------------

if (grep /P/, $mode) { initializeMap() ; }

openOsm1File ($osm1Name) ;
moveNodeFile1() ;

openOsm2File ($osm2Name) ;
moveNodeFile2() ;

processNodes() ;

moveWayFile1() ;
moveWayFile2() ;

processWays() ;

closeOsm1File () ;
closeOsm2File () ;

if ( ! (grep /P/, $mode) )  { 
	processNeededWayNodes() ; 
}

if (grep /P/, $mode) { print "paint areas and save map...\n" ; }
if (grep /P/, $mode) { paintAreas() ; }
if (grep /P/, $mode) { saveMap() ; }
if (grep /P/, $mode) { print "done.\n" ; }


output() ;

#-------
# FINISH
#-------

print "\n$program finished after ", stringTimeSpent (time()-$time0), "\n\n" ;


#------------------------------------------------------------------------------------
# Node procesing
#------------------------------------------------------------------------------------

sub processNodes {
	print "processing nodes...\n" ;
	while ( ($nodeId1 > -1) or ($nodeId2 > -1) ) {
		# print "while $nodeId1     $nodeId2\n" ;
		if ($nodeId1 == -1) {
			# get rest from file 2, new nodes
			while ( $nodeId2 != -1 ) {
				# print "$nodeId1     $nodeId2   2-NEW\n" ;
				$nodesAdded{$nodeUser2}++ ;
				addOperationTime ($nodeUser2, $nodeTimestamp2) ;
				if (grep /P/, $mode) { paintNewNode() ; }
				userArea ($nodeUser2, $nodeLon2, $nodeLat2) ;
				moveNodeFile2() ;
			}
		}

		if ($nodeId2 == -1) {
			# get rest from file 1, deleted nodes
			while ( $nodeId1 != -1 ) {
				$deletedNodes++ ;
				if (scalar @nodeTags1 > 0) { 
					$deletedNodesWithTags++ ; 
					if (grep /P/, $mode) { paintDeletedNodeWithTag() ; }
					push @deletedNodesIds, $nodeId1 ;
					@{$deletedNodesTags{$nodeId1}} = @nodeTags1 ;
				}
				else {
					if (grep /P/, $mode) { paintDeletedNode() ; }
				}
				moveNodeFile1() ;
			}
		}

		if ( ($nodeId1 == $nodeId2) and ($nodeId1 != -1) ) {
			# print "equal $nodeId1     $nodeId2\n" ;

			# place?
			if (grep /P/, $mode) {
				my $place = 0 ; my $placeName = "" ; my $placeNameGiven = 0 ;
				foreach my $t (@nodeTags1) {
					if ($t->[0] eq "place") { $place = 1 ; }
					if ($t->[0] eq "name") { $placeNameGiven = 1 ; $placeName = $t->[1] ; }
				}
				if ( ($place == 1) and ($placeNameGiven == 1) ) { paintPlace ($nodeLon1, $nodeLat1, $placeName) ; } 
			}

			# position...
			if ( ($nodeLon1 != $nodeLon2) or ($nodeLat1 != $nodeLat2) ) {
				$nodesMovedNumber{$nodeUser2}++ ;
				addOperationTime ($nodeUser2, $nodeTimestamp2) ;
				if (grep /P/, $mode) { paintMovedNode() ; }
				my ($d) = distance ($nodeLon1, $nodeLat1, $nodeLon2, $nodeLat2) ;
				$nodesMovedDistance{$nodeUser2} += $d ;
				if (defined $nodesMovedMax{$nodeUser2}) {
					if ($nodesMovedMax{$nodeUser2} < $d) { $nodesMovedMax{$nodeUser2} = $d ; }
				}
				else {
					$nodesMovedMax{$nodeUser2} = $d ;
				}
				userArea ($nodeUser2, $nodeLon1, $nodeLat1) ;
				userArea ($nodeUser2, $nodeLon2, $nodeLat2) ;
			} # moved

			if ($nodeVersion2 - $nodeVersion1 > 2) { 
				push @{$versionJumpsNodes{$nodeUser2}}, [$nodeId2, $nodeVersion2 - $nodeVersion1] ;
			}

			# process tags
			my ($added, $deleted, $renamed, $reclassified) = compareTags (\@nodeTags1, \@nodeTags2) ; 
			if ($added) { $tagsAdded{$nodeUser2} += $added ; }
			if ($deleted) { $tagsDeleted{$nodeUser2} += $deleted ; }
			if ($renamed) { 
				$tagsRenamed{$nodeUser2} += $renamed ; 
				if (grep /P/, $mode) { paintRenamedNode() ; }
			}
			if ($added or $deleted or $renamed or $reclassified) {
				addOperationTime ($nodeUser2, $nodeTimestamp2) ;
			}
			moveNodeFile1() ;
			moveNodeFile2() ;
		}

		if ( ($nodeId1 > $nodeId2) and ($nodeId2 != -1) ) {
			# print "1 > 2 $nodeId1     $nodeId2   2-NEW\n" ;
			# id 2 not found in file 1, nodeId2 new
			$nodesAdded{$nodeUser2}++ ;
			addOperationTime ($nodeUser2, $nodeTimestamp2) ;
			if (grep /P/, $mode) { paintNewNode() ; }
			userArea ($nodeUser2, $nodeLon2, $nodeLat2) ;
			# move file2 until id2>=id1
			while ( ($nodeId2 < $nodeId1) and ($nodeId2 != -1) ) {
				moveNodeFile2() ;
			}
		}

		if ( ($nodeId1 < $nodeId2) and ($nodeId1 != -1) ) {
			# print "1 < 2 $nodeId1     $nodeId2   1-DELETED\n" ;
			# id 1 not found in file 2, nodeId1 deleted
			$deletedNodes++ ;
			if (scalar @nodeTags1 > 0) { 
				$deletedNodesWithTags++ ; 
				if (grep /P/, $mode) { paintDeletedNodeWithTag() ; }
				push @deletedNodesIds, $nodeId1 ;
				@{$deletedNodesTags{$nodeId1}} = @nodeTags1 ;
			}
			else {
				if (grep /P/, $mode) { paintDeletedNode() ; }
			}
			# move file1 until id1>=id2
			while ( ($nodeId1 < $nodeId2) and ($nodeId1 != -1) ) {
				moveNodeFile1() ;
			}
		}
	}
	print "finished.\n" ;
}

#------------------------------------------------------------------------------------
# Way procesing
#------------------------------------------------------------------------------------

sub processWays {
	$objectProcessed = 1 ;
	print "processing ways...\n" ;
	while ( ($wayId1 > -1) or ($wayId2 > -1) ) {
		if ($wayId1 == -1) {
			# get rest from file 2, new ways
			while ( $wayId2 != -1 ) {
				# print "$wayId1     $wayId2   2-NEW\n" ;
				$waysAdded{$wayUser2}++ ;
				addOperationTime ($wayUser2, $wayTimestamp2) ;
				if (scalar @wayNodes2 >= 2) {
					#userArea ($wayUser2, $lon2{$wayNodes2[0]}, $lat2{$wayNodes2[0]}) ;
					#userArea ($wayUser2, $lon2{$wayNodes2[-1]}, $lat2{$wayNodes2[-1]}) ;
					userAreaWay ($wayUser2, $wayNodes2[0]) ;
					userAreaWay ($wayUser2, $wayNodes2[-1]) ;
					if (grep /P/, $mode) { paintNewWay() ; }
				}
				#next
				moveWayFile2() ;
			}
		}

		if ($wayId2 == -1) {
			# get rest from file 1, deleted ways
			while ( $wayId1 != -1 ) {
				$deletedWays++ ;
				if (scalar @wayTags1 > 0) { 
					$deletedWaysWithTags++ ; 
					if (scalar @wayNodes1 >= 2) {
						if (grep /P/, $mode) { paintDeletedWayWithTag() ; }
					}
					push @deletedWaysIds, $wayId1 ;
					@{$deletedWaysTags{$wayId1}} = @wayTags1 ;
				}
				else {
					if (scalar @wayNodes1 >= 2) {
						if (grep /P/, $mode) { paintDeletedWay() ; }
					}
				}
				#next
				moveWayFile1() ;
			}
		}

		if ( ($wayId1 == $wayId2) and ($wayId1 != -1) ) {
			# print "equal $wayId1     $wayId2\n" ;
			# TODO position
			# TODO nodes number

			if ($wayVersion2 - $wayVersion1 > 2) { 
				push @{$versionJumpsWays{$wayUser2}}, [$wayId2, $wayVersion2 - $wayVersion1] ;
			}

			# process tags
			my ($added, $deleted, $renamed, $reclassified, $rereffed) = compareTags (\@wayTags1, \@wayTags2) ; 
			if ($added) { $tagsAdded{$wayUser2} += $added ; }
			if ($deleted) { $tagsDeleted{$wayUser2} += $deleted ; }
			if ($renamed) { 
				$tagsRenamed{$wayUser2} += $renamed ;  
				# TODO draw
			}
			if ($reclassified) { 
				$tagsReclassified{$wayUser2} += $reclassified ; 
				if (scalar @wayNodes1 >= 2) {
					if (grep /P/, $mode) { paintReclassifiedWay() ; }
				}
			} 
			if ($rereffed) { 
				$tagsRereffed{$wayUser2} += $rereffed ; 
				if (scalar @wayNodes1 >= 2) {
					if (grep /P/, $mode) { paintRereffedWay() ; }
				}
			} 
			if ($added or $deleted or $renamed or $reclassified or $rereffed) { 
				addOperationTime ($wayUser2, $wayTimestamp2) ; 
				if ( (scalar @wayNodes1 >= 2) and (scalar @wayNodes2 >= 2) ) {
					# userArea ($wayUser2, $lon2{$wayNodes2[0]}, $lat2{$wayNodes2[0]}) ;
					# userArea ($wayUser2, $lon2{$wayNodes2[-1]}, $lat2{$wayNodes2[-1]}) ;
					userAreaWay ($wayUser2, $wayNodes2[0]) ;
					userAreaWay ($wayUser2, $wayNodes2[-1]) ;
				}
			}
			moveWayFile1() ;
			moveWayFile2() ;
		}

		if ( ($wayId1 > $wayId2) and ($wayId2 != -1) ) {
			# print "1 > 2 $wayId1     $wayId2   2-NEW\n" ;
			# id 2 not found in file 1, wayId2 new
			$waysAdded{$wayUser2}++ ;
			addOperationTime ($wayUser2, $wayTimestamp2) ; 
			if (scalar @wayNodes2 >= 2) {
				# userArea ($wayUser2, $lon2{$wayNodes2[0]}, $lat2{$wayNodes2[0]}) ;
				# userArea ($wayUser2, $lon2{$wayNodes2[-1]}, $lat2{$wayNodes2[-1]}) ;
				userAreaWay ($wayUser2, $wayNodes2[0]) ;
				userAreaWay ($wayUser2, $wayNodes2[-1]) ;
				if (grep /P/, $mode) { paintNewWay() ; }
			}
			# move file2 until id2>=id1
			while ( ($wayId2 < $wayId1) and ($wayId2 != -1) ) {
				moveWayFile2() ;
			}
		}

		if ( ($wayId1 < $wayId2) and ($wayId1 != -1) ) {
			# print "1 < 2 $wayId1     $wayId2   1-DELETED\n" ;
			# id 1 not found in file 2, wayId1 deleted
			$deletedWays++ ;
			if (scalar @wayTags1 > 0) { 
				$deletedWaysWithTags++ ; 
				if (scalar @wayNodes1 >= 2) {
					if (grep /P/, $mode) { paintDeletedWayWithTag() ; }
				}
				push @deletedWaysIds, $wayId1 ;
				@{$deletedWaysTags{$wayId1}} = @wayTags1 ;
			}
			else {
				if (scalar @wayNodes1 >= 2) {
					if (grep /P/, $mode) { paintDeletedWay() ; }
				}
			}
			# move file1 until id1>=id2
			while ( ($wayId1 < $wayId2) and ($wayId1 != -1) ) {
				moveWayFile1() ;
			}
		}
	}
	print "finished.\n" ;
}


#------------------------------------------------------------------------------------
# Output functions
#------------------------------------------------------------------------------------

sub output {
	print "outputting html...\n" ;
	my @a ; my @list ;
	open ($html, ">", $htmlName) or die ("can't open html output file") ;
	printHTMLHeader ($html, "UserActvity by Gary68") ;
	print $html "<H1>UserActvity by gary68</H1>" ;
	print $html "<p>", stringFileInfo ($osm1Name), "</p>\n"  ;
	print $html "<p>", stringFileInfo ($osm2Name), "</p>\n"  ;
	print $html "<p>A deleted tag can result out of a change of a tag. The same is true for the addition of a tag. Real changes are not counted.</p>" ;
	print $html "<H1>Results</H1>" ;
	print $html "<p>DELETED NODES: $deletedNodes</p>\n" ;
	print $html "<p>DELETED NODES WITH TAGS: $deletedNodesWithTags (details see further down)</p>\n" ;
	
	@a = () ;
	foreach my $e (keys %nodesMovedNumber) { push @a, [$e, $nodesMovedNumber{$e}] ; }
	printTop ("TOP moved nodes number", 0, @a) ;
	@a = () ;
	foreach my $e (keys %nodesMovedDistance) { push @a, [$e, $nodesMovedDistance{$e}] ; }
	printTop ("TOP moved nodes total distance (km)", 1, @a) ;
	@a = () ;
	foreach my $e (keys %nodesMovedDistance) { push @a, [$e, $nodesMovedDistance{$e}/$nodesMovedNumber{$e}] ; }
	printTop ("TOP moved nodes average distance (km)", 1, @a) ;
	@a = () ;
	foreach my $e (keys %nodesMovedMax) { push @a, [$e, $nodesMovedMax{$e}] ; }
	printTop ("TOP moved nodes maximum distance (km)", 1, @a) ;

	@a = () ;
	foreach my $u (keys %maxLon) {
		# print "calc area for user: $u\n" ;
		push @a, [$u, distance ($minLon{$u}, $minLat{$u}, $minLon{$u}, $maxLat{$u}) * distance ($minLon{$u}, $minLat{$u}, $maxLon{$u}, $minLat{$u})] ;
	}
	printTop ("TOP work areas (km²)", 1, @a) ;

	@a = () ;
	foreach my $e (keys %tagsAdded) { push @a, [$e, $tagsAdded{$e}] ; }
	printTop ("TOP tags added", 0, @a) ;

	@a = () ;
	foreach my $e (keys %tagsRenamed) { push @a, [$e, $tagsRenamed{$e}] ; }
	printTop ("TOP objects renamed", 0, @a) ;

	@list = @a ; @list = reverse (sort {$a->[1]<=>$b->[1]} @list) ;
	# if (scalar @list > $topMax) { @list = @list[0..$topMax-1] ; }
	print $html "<h2>Renames by ALL users</h2>\n" ;
	printHTMLTableHead ($html) ;
	printHTMLTableHeadings ($html, "User", "Tag", "Number") ; 
	foreach my $e (@list) {
		foreach my $t (keys %{$renames{$e->[0]}}) {
			# printf $html "%-25s %-80s %5i<br>\n" , $e->[0], $t, $renames{$e->[0]}{$t} ;
			printHTMLRowStart ($html) ;
			printHTMLCellLeft ($html, userLink ($e->[0])) ;			
			printHTMLCellLeft ($html, $t) ;			
			printHTMLCellRight ($html, $renames{$e->[0]}{$t}) ;			
			printHTMLRowEnd ($html) ;
		}
	}
	printHTMLTableFoot ($html) ;

	@a = () ;
	foreach my $e (keys %tagsRereffed) { push @a, [$e, $tagsRereffed{$e}] ; }
	printTop ("TOP objects rereffed", 0, @a) ;

	@list = @a ; @list = reverse (sort {$a->[1]<=>$b->[1]} @list) ;
	# if (scalar @list > $topMax) { @list = @list[0..$topMax-1] ; }
	print $html "<h2>Rerefs by ALL users</h2>\n" ;
	printHTMLTableHead ($html) ;
	printHTMLTableHeadings ($html, "User", "Tag", "Number") ; 
	foreach my $e (@list) {
		foreach my $t (keys %{$rerefs{$e->[0]}}) {
			# printf $html "%-25s %-80s %5i<br>\n" , $e->[0], $t, $rerefs{$e->[0]}{$t} ;
			printHTMLRowStart ($html) ;
			printHTMLCellLeft ($html, userLink ($e->[0])) ;			
			printHTMLCellLeft ($html, $t) ;			
			printHTMLCellRight ($html, $rerefs{$e->[0]}{$t}) ;			
			printHTMLRowEnd ($html) ;
		}
	}
	printHTMLTableFoot ($html) ;

	@a = () ;
	foreach my $e (keys %tagsReclassified) { push @a, [$e, $tagsReclassified{$e}] ; }
	printTop ("TOP ways reclassified", 0, @a) ;

	@list = @a ; @list = reverse (sort {$a->[1]<=>$b->[1]} @list) ;
	# if (scalar @list > $topMax) { @list = @list[0..$topMax-1] ; }
	print $html "<h2>Reclassifications by ALL users</h2>\n" ;
	printHTMLTableHead ($html) ;
	printHTMLTableHeadings ($html, "User", "Tag", "Number") ; 
	foreach my $e (@list) {
		foreach my $t (keys %{$reclassifications{$e->[0]}}) {
			# printf $html "%-25s %-80s %5i<br>\n" , $e->[0], $t, $reclassifications{$e->[0]}{$t} ;
			printHTMLRowStart ($html) ;
			printHTMLCellLeft ($html, userLink ($e->[0])) ;			
			printHTMLCellLeft ($html, $t) ;			
			printHTMLCellRight ($html, $reclassifications{$e->[0]}{$t}) ;			
			printHTMLRowEnd ($html) ;
		}
	}
	printHTMLTableFoot ($html) ;

#	print $html "<h2>Version jumps nodes by ALL users</h2>\n" ;
#	printHTMLTableHead ($html) ;
#	printHTMLTableHeadings ($html, "User", "Nodes") ; 
#	foreach my $u (keys %versionJumpsNodes) {
#		printHTMLRowStart ($html) ;
#		printHTMLCellLeft ($html, userLink ($u)) ;			
#		my $jumps = "" ;
#		foreach my $v (@{$versionJumpsNodes{$u}}) {
#			$jumps = $jumps . historyLink ("node", $v->[0]) . " (" . $v->[1] . ") " ;
#		}
#		printHTMLCellLeft ($html, $jumps) ;			
#		printHTMLRowEnd ($html) ;
#	}
#	printHTMLTableFoot ($html) ;

#	print $html "<h2>Version jumps ways by ALL users</h2>\n" ;
#	printHTMLTableHead ($html) ;
#	printHTMLTableHeadings ($html, "User", "Ways") ; 
#	foreach my $u (keys %versionJumpsWays) {
#		printHTMLRowStart ($html) ;
#		printHTMLCellLeft ($html, userLink ($u)) ;			
#		my $jumps = "" ;
#		foreach my $v (@{$versionJumpsWays{$u}}) {
#			$jumps = $jumps . historyLink ("node", $v->[0]) . " (" . $v->[1] . ") " ;
#		}
#		printHTMLCellLeft ($html, $jumps) ;			
#		printHTMLRowEnd ($html) ;
#	}
#	printHTMLTableFoot ($html) ;

	@a = () ;
	foreach my $e (keys %tagsDeleted) { push @a, [$e, $tagsDeleted{$e}] ; }
	printTop ("TOP tags deleted", 0, @a) ;

	@list = @a ; @list = reverse (sort {$a->[1]<=>$b->[1]} @list) ;
	if (scalar @list > $topMax) { @list = @list[0..$topMax-1] ; }
	print $html "<h2>Tags removed by TOP users</h2>\n" ;
	printHTMLTableHead ($html) ;
	printHTMLTableHeadings ($html, "User", "Removed", "Number") ; 
	foreach my $e (@list) {
		foreach my $t (keys %{$tagsDeletedName{$e->[0]}}) {
			# printf $html "%-25s %-50s %5i<br>\n" , $e->[0], $t, $tagsDeletedName{$e->[0]}{$t} ;
			printHTMLRowStart ($html) ;
			printHTMLCellLeft ($html, userLink ($e->[0])) ;			
			printHTMLCellLeft ($html, $t) ;			
			printHTMLCellRight ($html, $tagsDeletedName{$e->[0]}{$t}) ;			
			printHTMLRowEnd ($html) ;
		}
	}
	printHTMLTableFoot ($html) ;

	print $html "<h2>ALL deleted Nodes with tags (details)</h2>\n" ;
	printHTMLTableHead ($html) ;
	printHTMLTableHeadings ($html, "NodeId", "Tags") ; 
	foreach my $n (@deletedNodesIds) {
		printHTMLRowStart ($html) ;
		printHTMLCellLeft ($html, historyLink ("node", $n) ) ;			
		my $tagText = "" ;
		foreach my $t (@{$deletedNodesTags{$n}}) { $tagText = $tagText . $t->[0] . ":" . $t->[1] . "<br>\n" ; }
		printHTMLCellLeft ($html, $tagText) ;	
		printHTMLRowEnd ($html) ;
	}
	printHTMLTableFoot ($html) ;

	print $html "<h2>ALL deleted ways with tags (details)</h2>\n" ;
	printHTMLTableHead ($html) ;
	printHTMLTableHeadings ($html, "WayId", "Tags") ; 
	foreach my $n (@deletedWaysIds) {
		printHTMLRowStart ($html) ;
		printHTMLCellLeft ($html, historyLink ("way", $n) ) ;			
		my $tagText = "" ;
		foreach my $t (@{$deletedWaysTags{$n}}) { $tagText = $tagText . $t->[0] . ":" . $t->[1] . "<br>\n" ; }
		printHTMLCellLeft ($html, $tagText) ;	
		printHTMLRowEnd ($html) ;
	}
	printHTMLTableFoot ($html) ;

	print $html "<p>$program finished after ", stringTimeSpent (time()-$time0), "</p>\n" ;
	printHTMLFoot ($html) ;
	close ($html) ;

	my ($html2Name) = $htmlName ;
	$html2Name =~ s/.htm/.time.htm/ ;
	open ($html, ">", $html2Name) or die ("can't open html2 output file") ;
	printHTMLHeader ($html, "UserActvity (TIME) by Gary68") ;
	print $html "<H1>UserActvity (TIME) by gary68</H1>" ;
	print $html "<p>", stringFileInfo ($osm1Name), "</p>\n"  ;
	print $html "<p>", stringFileInfo ($osm2Name), "</p>\n"  ;

	@a = () ;
	foreach my $u (keys %opTime) { push @a, [$u, numOpHours ($u)] ; }
	printTop ("TOP operation hour slots", 0, @a) ;

	@list = reverse (sort {$a->[1]<=>$b->[1]} @a) ;
	if (scalar @list > $topMax) { @list = @list[0..$topMax-1] ; }
	print $html "<h2>Operation time slots TOP users</h2>\n" ;
	printHTMLTableHead ($html) ;
	printHTMLTableHeadings ($html, "User", "00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23") ; 
	foreach my $e (@list) {
		printHTMLRowStart ($html) ;
		printHTMLCellLeft ($html, userLink ($e->[0])) ;			
		my $max = 0 ;
		foreach my $h ("00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23") {
			if ( (defined $opTime{$e->[0]}{$h}) and ($opTime{$e->[0]}{$h} > $max) )  { $max = $opTime{$e->[0]}{$h} ; } 
		}
		# print "$e->[0] $max\n" ;
		my $value = 0 ;
		foreach my $h ("00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23") {
			if (defined $opTime{$e->[0]}{$h}) { $value = $opTime{$e->[0]}{$h} ; } else { $value = 0 ; }
			my ($colorValue) = 255 - ( int ($value / $max * 255) / 2) ; my ($colorString) = sprintf "%02x", $colorValue ;
			# print "$value $colorValue $colorString\n" ;
			my ($htmlString) = "<td align=\"right\" bgcolor=\"#" . $colorString . $colorString . $colorString . "\">" . $value . "</td>" ;
			print $html $htmlString ;
		}
		printHTMLRowEnd ($html) ;
	}
	printHTMLTableFoot ($html) ;


	printHTMLFoot ($html) ;
	close ($html) ;


	print "done.\n" ;
}

sub printTop {
	my ($heading, $decimal, @list) = @_ ;
	print $html "<h2>$heading</h2>\n" ;
	printHTMLTableHead ($html) ;
	printHTMLTableHeadings ($html, "User", "Data") ; 
	@list = reverse (sort {$a->[1]<=>$b->[1]} @list) ;
	if (scalar @list > $topMax) { @list = @list[0..$topMax-1] ; }
	foreach my $e (@list) {
		printHTMLRowStart ($html) ;
		my $s ;
		if ($decimal) { 
			$s = sprintf "%8.3f", $e->[1] ;
		}
		else {
			$s = sprintf "%8i", $e->[1] ;
		}
		printHTMLCellLeft ($html, userLink ($e->[0]) ) ;			
		printHTMLCellRight ($html, $s) ;			
		printHTMLRowEnd ($html) ;
	} 
	printHTMLTableFoot ($html) ;
}


#------------------------------------------------------------------------------------
# some functions
#------------------------------------------------------------------------------------

sub addOperationTime {
	my ($user, $timestamp) = @_ ;
	# timestamp="2008-04-13T13:23:55+01:00"
	my ($hour) = substr ($timestamp, 11, 2) ;
	$opTime{$user}{$hour}++ ;
}

sub numOpHours {
	my ($user) = shift ;
	my $hours = 0 ;
	foreach my $hour ("00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23") {
		if (defined $opTime{$user}{$hour}) { $hours++ ; }
	}
	return ($hours) ;
}

sub userAreaWay {
	my ($u, $n) = @_ ;
	if (grep /P/, $mode) {
		userArea ($u, $lon2{$n}, $lat2{$n}) ;
	}
	else {
		$neededNodes{$n} = $u ;
	}
}

sub processNeededWayNodes {
	print "get needed nodes for touched ways...\n" ;
	openOsm2File ($osm2Name) ;
	moveNodeFile2() ;
	while ( $nodeId2 != -1 ) {
		if (defined $neededNodes{$nodeId2}) {
			userArea ($neededNodes{$nodeId2}, $nodeLon2, $nodeLat2) ;
		}
		moveNodeFile2() ;
	}
	closeOsm2File () ;
	print "done.\n" ;
}

sub userArea {
	my ($u, $lon, $lat) = @_ ;

	if ( (!defined $lon) or (! defined $lat) ) {
		print "userArea ERROR user $u nodes $nodeId1 $nodeId2 ways $wayId1 $wayId2\n" ;
	}

	if (! defined $maxLon{$u}) {
		$minLon{$u} = $lon ;
		$minLat{$u} = $lat ;
		$maxLon{$u} = $lon ;
		$maxLat{$u} = $lat ;
	}
	else {
		if ($lon > $maxLon{$u}) { $maxLon{$u} = $lon ; }
		if ($lon < $minLon{$u}) { $minLon{$u} = $lon ; }
		if ($lat > $maxLat{$u}) { $maxLat{$u} = $lat ; }
		if ($lat < $minLat{$u}) { $minLat{$u} = $lat ; }
	}
}

sub compareTags {
	my ($aRef1, $aRef2) = @_ ;
	my $added = 0 ; my $deleted = 0 ; my $renamed = 0 ; my $reclassified = 0 ; my $rereffed = 0 ;
	my (@tags1) = @$aRef1 ; my (@tags2) = @$aRef2 ;

	$reName = 0 ; $reClass = 0 ; $reRef = 0 ;

	# RENAMED?
	my $nameGiven = 0 ;
	my $nameOld = "" ; 
	my $nameNew = "" ;
	foreach my $t (@tags1) { 
		if ($t->[0] eq "name") { $nameGiven = 1 ; $nameOld = $t->[1] ; }
	}
	foreach my $t (@tags2) { 
		if ($t->[0] eq "name") { $nameNew = $t->[1] ; }
	}
	if ( ($nameGiven == 1) and ($nameNew ne $nameOld) ) { 
		$renamed = 1 ; 
		$renames{$nodeUser2}{$nameOld . " > " . $nameNew} ++ ;
		$reName = 1 ; $name1 = $nameOld ; $name2 = $nameNew ;
	}
	
	if ($objectProcessed == 1 ) {
		# REREF?
		my $refGiven = 0 ;
		my $refOld = "" ; 
		my $refNew = "" ;
		foreach my $t (@tags1) { 
			if ($t->[0] eq "ref") { $refGiven = 1 ; $refOld = $t->[1] ; }
		}
		foreach my $t (@tags2) { 
			if ($t->[0] eq "ref") { $refNew = $t->[1] ; }
		}
		if ( ($refGiven == 1) and ($refNew ne $refOld) ) { 
			$rereffed = 1 ; 
			$rerefs{$wayUser2}{$refOld . " > " . $refNew} ++ ;
			$reRef = 1 ; $ref1 = $refOld ; $ref2 = $refNew ;
		}
		# RECLASSIFIED?
		my $highwayGiven = 0 ;
		my $highwayOld = "" ;
		my $highwayNew = "" ;
		foreach my $t (@tags1) { 
			if ($t->[0] eq "highway") { $highwayGiven = 1 ; $highwayOld = $t->[1] ; }
		}
		foreach my $t (@tags2) { 
			if ($t->[0] eq "highway") { $highwayNew = $t->[1] ; }
		}
		if ( ($highwayGiven == 1) and ($highwayNew ne $highwayOld) ) { 
			$reclassified = 1 ; 
			$reclassifications{$wayUser2}{$highwayOld . " > " . $highwayNew} ++ ;
			$reClass = 1 ; $class1 = $highwayOld ; $class2 = $highwayNew ;
		}
	} # objectProcessed

	# ADDED?
	foreach my $t2 (@tags2) {
		my $found = 0 ;
		foreach my $t1 (@tags1) {
			if ( ($t1->[0] eq $t2->[0]) and ($t1->[1] eq $t2->[1]) ) { $found = 1 ; }
		}
		if ($found == 0) { $added++ ; }
	}

	# DELETED?
	foreach my $t1 (@tags1) {
		my $found = 0 ;
		foreach my $t2 (@tags2) {
			if ( ($t1->[0] eq $t2->[0]) and ($t1->[1] eq $t2->[1]) ) { $found = 1 ; }
		}
		# if ($found == 0) { 
		if ( ($found == 0) and ($t1->[0] ne "created_by") ) { 
			$deleted++ ; 
			$tagsDeletedName{$nodeUser2}{$t1->[0].":".$t1->[1]}++ ;
		}
	}

	return ($added, $deleted, $renamed, $reclassified, $rereffed) ;
} # compareTags

sub userLink {
	my ($user) = shift ;
	return "<a href=\"http://www.openstreetmap.org/user/" . $user . "\">" . $user . "</a>" ;
}



#------------------------------------------------------------------------------------
# Map functions
#------------------------------------------------------------------------------------

sub paintNewWay {
	drawWay ("black", 2, nodes2Coordinates2 (@wayNodes2) ) ;
	if (grep /D/, $mode) { drawTextPos ($lon2{$wayNodes2[0]}, $lat2{$wayNodes2[0]}, 0, 0, $wayUser2, "black", 2) ; }
}

sub paintDeletedWay {
	drawWay ("red", 2, nodes2Coordinates1 (@wayNodes1) ) ;
}

sub paintDeletedWayWithTag {
	drawWay ("red", 3, nodes2Coordinates1 (@wayNodes1) ) ;
}

sub paintRenamedWay {
	drawWay ("orange", 3, nodes2Coordinates1 (@wayNodes1) ) ;
	if (grep /D/, $mode) { 
		drawTextPos ($lon1{$wayNodes1[0]}, $lat1{$wayNodes1[0]}, 0, 0, $wayUser2, "orange", 2) ; 
		drawTextPos ($lon1{$wayNodes1[0]}, $lat1{$wayNodes1[0]}, 0, -8, $name1 . "->" . $name2, "black", 2) ; 
	}
}

sub paintRereffedWay {
	drawWay ("orange", 3, nodes2Coordinates1 (@wayNodes1) ) ;
	if (grep /D/, $mode) { 
		drawTextPos ($lon1{$wayNodes1[0]}, $lat1{$wayNodes1[0]}, 0, 0, $wayUser2, "orange", 2) ; 
		drawTextPos ($lon1{$wayNodes1[0]}, $lat1{$wayNodes1[0]}, 0, -8, $ref1 . "->" . $ref2, "black", 2) ; 
	}
}

sub paintReclassifiedWay {
	drawWay ("pink", 3, nodes2Coordinates1 (@wayNodes1) ) ;
	if (grep /D/, $mode) { 
		drawTextPos ($lon1{$wayNodes1[0]}, $lat1{$wayNodes1[0]}, 0, 0, $wayUser2, "pink", 2) ; 
		drawTextPos ($lon1{$wayNodes1[0]}, $lat1{$wayNodes1[0]}, 0, -8, $class1 . "->" . $class2, "black", 2) ; 
	}
}

sub paintMovedWay {
}


sub paintNewNode {
	drawNodeDot ($nodeLon2, $nodeLat2, "black", 4) ;
	if (grep /D/, $mode) { drawTextPos ($nodeLon2, $nodeLat2, 0, 0, $nodeUser2, "black", 2) ; }
}

sub paintDeletedNode {
	drawNodeDot ($nodeLon1, $nodeLat1, "red", 4) ;
}

sub paintDeletedNodeWithTag {
	drawNodeDot ($nodeLon1, $nodeLat1, "red", 5) ;
}

sub paintRenamedNode {
	drawNodeDot ($nodeLon1, $nodeLat1, "orange", 4) ;
	if (grep /D/, $mode) { 
		drawTextPos ($nodeLon1, $nodeLat1, 0, 0, $nodeUser2, "orange", 2) ; 
		drawTextPos ($nodeLon1, $nodeLat1, 0, -8, $name1 . "->" . $name2, "black", 2) ; 
	}
}

sub paintMovedNode {
	# blue
	drawNodeDot ($nodeLon1, $nodeLat1, "lightblue", 4) ;
	drawWay ("lightblue", 1, ($nodeLon1, $nodeLat1, $nodeLon2, $nodeLat2) ) ;
	drawNodeDot ($nodeLon2, $nodeLat2, "blue", 4) ;
	if (grep /D/, $mode) { drawTextPos ($nodeLon1, $nodeLat1, 0, 0, $nodeUser2, "blue", 2) ; }
}

sub paintAreas {
	foreach my $user (keys %minLon) {
		drawWay ("tomato", 1, $minLon{$user}, $minLat{$user}, $minLon{$user}, $maxLat{$user}, $maxLon{$user}, $maxLat{$user}, $maxLon{$user}, $minLat{$user}, $minLon{$user}, $minLat{$user} ) ;
		drawTextPos ($minLon{$user}, $minLat{$user}, 0, 0, $user, "black", 2) ;
	}
}

sub paintPlace {
	my ($lon, $lat, $name) = @_ ;
	drawTextPos ($lon, $lat, 0, 0, $name, "black", 4) ;	
	drawNodeDot ($lon, $lat, "black", 2) ;
}

sub initializeMap {
	print "initializing map...\n" ;
	print "- parsing nodes file1...\n" ;
	my $lonMax = -999 ; my $lonMin = 999 ; my $latMax = -999 ; my $latMin = 999 ;
	openOsm1File ($osm1Name) ;
	moveNodeFile1() ;	

	# get all node information from file 1		
	while ($nodeId1 != -1) {
		$lon1{$nodeId1} = $nodeLon1 ; $lat1{$nodeId1} = $nodeLat1 ;
		if ($nodeLon1 > $lonMax) { $lonMax = $nodeLon1 ; }
		if ($nodeLat1 > $latMax) { $latMax = $nodeLat1 ; }
		if ($nodeLon1 < $lonMin) { $lonMin = $nodeLon1 ; }
		if ($nodeLat1 < $latMin) { $latMin = $nodeLat1 ; }
		# next
		moveNodeFile1() ;	
	}
	initGraph ($sizeX, $lonMin, $latMin, $lonMax, $latMax) ;
	if (grep /S/, $mode) {
		enableSVG() ;
	}

	# ways
	print "- parsing ways file1...\n" ;
	moveWayFile1() ;
	while ($wayId1 != -1) {
		drawWay ("lightgray", 1, nodes2Coordinates1 (@wayNodes1) ) ;
		moveWayFile1() ;
	}
	closeOsm1File() ;

	print "- parsing nodes file2...\n" ;
	openOsm2File ($osm2Name) ;
	moveNodeFile2() ;

	# get all node information from file 2
	while ($nodeId2 != -1) {
		$lon2{$nodeId2} = $nodeLon2 ; $lat2{$nodeId2} = $nodeLat2 ;
		# next
		moveNodeFile2() ;
	}
	closeOsm2File() ;

	print "done.\n" ;
}

sub saveMap {
	print "saving map...\n" ;
	drawHead ($program . " ". $version . " by Gary68", "black", 3) ;
	drawFoot ("data by openstreetmap.org" . " " . $osm1Name . " " .ctime(stat($osm1Name)->mtime) . $osm2Name . " " .ctime(stat($osm2Name)->mtime), "black", 3) ;
	drawLegend (3, "New", "black", "Deleted", "red", "Moved", "blue", "Renamed", "orange", "Reclassified", "pink", "User Area", "tomato") ;
	drawRuler ("black") ;
	my ($pngName) = $htmlName ;
	$pngName =~ s/.htm/.png/ ;
	writeGraph ($pngName) ; 
	if (grep /S/, $mode) {
		my ($svgName) = $htmlName ;
		$svgName =~ s/.htm/.svg/ ;
		writeSVG ($svgName) ; 
	}
	print "done.\n" ;
}

sub nodes2Coordinates1 {
	my @nodes = @_ ;
	my $i ;
	my @result = () ;
	for ($i=0; $i<=$#nodes; $i++) {
		push @result, $lon1{$nodes[$i]} ;
		push @result, $lat1{$nodes[$i]} ;
	}
	return @result ;
}

sub nodes2Coordinates2 {
	my @nodes = @_ ;
	my $i ;
	my @result = () ;
	for ($i=0; $i<=$#nodes; $i++) {
		push @result, $lon2{$nodes[$i]} ;
		push @result, $lat2{$nodes[$i]} ;
	}
	return @result ;
}



#------------------------------------------------------------------------------------
# Basic object operations
#------------------------------------------------------------------------------------

sub getNode1 {
	my ($id, $version, $timestamp, $uid, $user, $changeset, $lat, $lon) ;
	my @gTags = () ;
	if($line1 =~ /^\s*\<node/) {
		if (! grep /changeset/, $line1) { $line1 =~ s/lat=/changeset=\"0\" lat=/ ; }
		if ( (grep /user=/, $line1) and (grep /uid=/, $line1) ) {
			($id, $version, $timestamp, $uid, $user, $changeset, $lat, $lon) = 
			($line1 =~ /^\s*\<node id=[\'\"](\d+)[\'\"].+version=[\'\"](\d+)[\'\"].+timestamp=[\'\"](.+)[\'\"].+uid=[\'\"](.+)[\'\"].+user=[\'\"](.+)[\'\"].+changeset=[\'\"](\d+)[\'\"].+lat=[\'\"](.+)[\'\"].+lon=[\'\"](.+)[\'\"]/) ;
		}
		else {
			($id, $version, $timestamp, $changeset, $lat, $lon) = 
			($line1 =~ /^\s*\<node id=[\'\"](\d+)[\'\"].+version=[\'\"](\d+)[\'\"].+timestamp=[\'\"](.+)[\'\"].+changeset=[\'\"](\d+)[\'\"].+lat=[\'\"](.+)[\'\"].+lon=[\'\"](.+)[\'\"]/) ;
			$user = "unknown" ; $uid = 0 ;
		}
		if (!defined $user) { $user = "unknown" ; }
		if (!defined $uid) { $uid = 0 ; }

		if (!$id or (! (defined ($lat))) or ( ! (defined ($lon))) ) {
			print "WARNING reading osm file1, line follows (expecting id, lon, lat and user for node):\n", $line1, "\n" ; 
		}
		else {
			if ( (grep (/">/, $line1)) or (grep (/'>/, $line1)) ) {                  # more lines, get tags
				nextLine1() ;
				while (!grep(/<\/node>/, $line1)) {
					my ($k, $v) = ($line1 =~ /^\s*\<tag k=[\'\"](.+)[\'\"]\s*v=[\'\"](.+)[\'\"]/) ;
					if ( (defined ($k)) and (defined ($v)) ) {
						my $tag = [$k, $v] ; push @gTags, $tag ;
					}
					else { 
						#print "WARNING tag not recognized file1: ", $line1, "\n" ; 
					}
					nextLine1() ;
				}
				nextLine1() ;
			}
			else {
				nextLine1() ;
			}
		}
	}
	else {
		return (-1, -1, -1, -1, -1) ; 
	} # node
	return ($id, $version, $timestamp, $uid, $user, $changeset, $lat, $lon, \@gTags) ; # in main @array = @$ref
} # getNode1

sub getNodeFile2 {
	my ($id, $version, $timestamp, $uid, $user, $changeset, $lat, $lon) ;
	my @gTags = () ;
	if($line2 =~ /^\s*\<node/) {
		if (! grep /changeset/, $line2) { $line2 =~ s/lat=/changeset=\"0\" lat=/ ; }
		if ( (grep /user=/, $line2) and (grep /uid=/, $line2) ) {
			($id, $version, $timestamp, $uid, $user, $changeset, $lat, $lon) = 
			($line2 =~ /^\s*\<node id=[\'\"](\d+)[\'\"].+version=[\'\"](\d+)[\'\"].+timestamp=[\'\"](.+)[\'\"].+uid=[\'\"](.+)[\'\"].+user=[\'\"](.+)[\'\"].+changeset=[\'\"](\d+)[\'\"].+lat=[\'\"](.+)[\'\"].+lon=[\'\"](.+)[\'\"]/) ;
		}
		else {
			($id, $version, $timestamp, $changeset, $lat, $lon) = 
			($line2 =~ /^\s*\<node id=[\'\"](\d+)[\'\"].+version=[\'\"](\d+)[\'\"].+timestamp=[\'\"](.+)[\'\"].+changeset=[\'\"](\d+)[\'\"].+lat=[\'\"](.+)[\'\"].+lon=[\'\"](.+)[\'\"]/) ;
			$user = "unknown" ; $uid = 0 ;
		}
		if (!defined $user) { $user = "unknown" ; }
		if (!defined $uid) { $uid = 0 ; }

		if (!$id or (! (defined ($lat))) or ( ! (defined ($lon))) ) {
			print "WARNING reading osm file 2, line follows (expecting id, lon, lat and user for node):\n", $line2, "\n" ; 
		}
		else {
			if ( (grep (/">/, $line2)) or (grep (/'>/, $line2)) ) {                  # more lines, get tags
				nextLine2() ;
				while (!grep(/<\/node>/, $line2)) {
					my ($k, $v) = ($line2 =~ /^\s*\<tag k=[\'\"](.+)[\'\"]\s*v=[\'\"](.+)[\'\"]/) ;
					if ( (defined ($k)) and (defined ($v)) ) {
						my $tag = [$k, $v] ; push @gTags, $tag ;
					}
					else { 
						#print "WARNING tag not recognized file 2: ", $line2, "\n" ; 
					}
					nextLine2() ;
				}
				nextLine2() ;
			}
			else {
				nextLine2() ;
			}
		}
	}
	else {
		return (-1, -1, -1, -1, -1) ; 
	} # node
	return ($id, $version, $timestamp, $uid, $user, $changeset, $lat, $lon, \@gTags) ; # in main @array = @$ref
} # getNodeFile2



sub getWay1 {
	my $id ; my $u ; my @tags ; my @nodes ; my $version ; my $timestamp ; my $uid ; my $changeset ;
	if($line1 =~ /^\s*\<way/) {

		if (! grep /changeset/, $line1) { $line1 =~ s/\">/\" changeset=\"0\">/ ; }
		if ( (grep /user=/, $line1) and (grep /uid=/, $line1) ) {
			($id, $version, $timestamp, $uid, $u, $changeset) = ($line1 =~ /^\s*\<way id=[\'\"](\d+)[\'\"].+version=[\'\"](\d+)[\'\"].+timestamp=[\'\"](.+)[\'\"].+uid=[\'\"](\d+)[\'\"].+user=[\'\"](.+)[\'\"].+changeset=[\'\"](\d+)[\'\"]>/) ;
		}
		else {
			($id, $version, $timestamp, $changeset) = ($line1 =~ /^\s*\<way id=[\'\"](\d+)[\'\"].+version=[\'\"](\d+)[\'\"].+timestamp=[\'\"](.+)[\'\"].+changeset=[\'\"](\d+)[\'\"]>/) ;
			$uid = 0 ; $u = "unknown" ;
		}

		if (!$u) { $u = "unknown" ; }
		if (!$id) { print "ERROR reading osm file1, line follows (expecting way id):\n", $line1, "\n" ; }
		unless ($id) { next; }
		nextLine1() ;
		while (not($line1 =~ /\/way>/)) { # more way data
			my ($node) = ($line1 =~ /^\s*\<nd ref=[\'\"](\d+)[\'\"]/); # get node id
			my ($k, $v) = ($line1 =~ /^\s*\<tag k=[\'\"](.+)[\'\"]\s*v=[\'\"](.+)[\'\"]/) ;
			if ($node) {
				push @nodes, $node ;
			}
			if ($k and defined($v)) { my $tag = [$k, $v] ; push @tags, $tag ; }
			nextLine1() ;
		}
		nextLine1() ;
	}
	else {
		return (-1, -1, -1, -1, -1, -1, -1, -1) ;
	}
	return ($id, $version, $timestamp, $uid, $u, $changeset, \@nodes, \@tags) ;
} # getWay1

sub getWayFile2 {
	my $id ; my $u ; my @tags ; my @nodes ; my $version ; my $timestamp ; my $uid ; my $changeset ;
	if($line2 =~ /^\s*\<way/) {

		if (! grep /changeset/, $line2) { $line2 =~ s/\">/\" changeset=\"0\">/ ; }
		if ( (grep /user=/, $line2) and (grep /uid=/, $line2) ) {
			($id, $version, $timestamp, $uid, $u, $changeset) = ($line2 =~ /^\s*\<way id=[\'\"](\d+)[\'\"].+version=[\'\"](\d+)[\'\"].+timestamp=[\'\"](.+)[\'\"].+uid=[\'\"](\d+)[\'\"].+user=[\'\"](.+)[\'\"].+changeset=[\'\"](\d+)[\'\"]>/) ;
		}
		else {
			($id, $version, $timestamp, $changeset) = ($line2 =~ /^\s*\<way id=[\'\"](\d+)[\'\"].+version=[\'\"](\d+)[\'\"].+timestamp=[\'\"](.+)[\'\"].+changeset=[\'\"](\d+)[\'\"]>/) ;
			$uid = 0 ; $u = "unknown" ;
		}
		if (!$u) { $u = "unknown" ; }
		if (!$id) { print "ERROR reading osm file2, line follows (expecting way id):\n", $line1, "\n" ; }
		unless ($id) { next; }
		nextLine2() ;
		while (not($line2 =~ /\/way>/)) { # more way data
			my ($node) = ($line2 =~ /^\s*\<nd ref=[\'\"](\d+)[\'\"]/); # get node id
			my ($k, $v) = ($line2 =~ /^\s*\<tag k=[\'\"](.+)[\'\"]\s*v=[\'\"](.+)[\'\"]/) ;
			if ($node) {
				push @nodes, $node ;
			}
			if ($k and defined($v)) { my $tag = [$k, $v] ; push @tags, $tag ; }
			nextLine2() ;
		}
		nextLine2() ;
	}
	else {
		return (-1, -1, -1, -1, -1, -1, -1, -1) ;
	}
	return ($id, $version, $timestamp, $uid, $u, $changeset, \@nodes, \@tags) ;
} # getWayFile2


sub moveNodeFile1 {
	($nodeId1, $nodeVersion1, $nodeTimestamp1, $nodeUid1, $nodeUser1, $nodeChangeset1, $nodeLat1, $nodeLon1, $aRef1) = getNode1 () ;
	if ($nodeId1 != -1) {
		@nodeTags1 = @$aRef1 ;
	}
}

sub moveNodeFile2 {
	($nodeId2, $nodeVersion2, $nodeTimestamp2, $nodeUid2, $nodeUser2, $nodeChangeset2, $nodeLat2, $nodeLon2, $aRef1) = getNodeFile2 () ;
	if ($nodeId2 != -1) {
		@nodeTags2 = @$aRef1 ;
	}
}

sub moveWayFile1 {
	($wayId1, $wayVersion1, $wayTimestamp1, $wayUid1, $wayUser1, $wayChangeset1, $aRef1, $aRef2) = getWay1() ;
	if ($wayId1 != -1) {
		@wayNodes1 = @$aRef1 ;
		@wayTags1 = @$aRef2 ;
	}
}

sub moveWayFile2 {
	($wayId2, $wayVersion2, $wayTimestamp2, $wayUid2, $wayUser2, $wayChangeset2, $aRef1, $aRef2) = getWayFile2() ;
	if ($wayId2 != -1) {
		@wayNodes2 = @$aRef1 ;
		@wayTags2 = @$aRef2 ;
	}
}


#------------------------------------------------------------------------------------
# Basic file operations
#------------------------------------------------------------------------------------

sub openOsm1File {
	$file1Name = shift ;
	if (grep /.bz2/, $file1Name) { $isBz21 = 1 ; } else { $isBz21 = 0 ; }
	if ($isBz21) {
		$bz1 = bzopen($file1Name, "rb") or die "Cannot open $file1Name: $bzerrno\n" ;
	}
	else {
		open ($file1, "<", $file1Name) || die "can't open osm file1" ;
	}
	nextLine1() ;		
	while ( ! (grep /\<node/, $line1) ) {
		nextLine1() ;
	}
	return 1 ;
}

sub closeOsm1File {
	if ($isBz21) { $bz1->bzclose() ; }
	else { close ($file1) ; }
}

sub nextLine1 {
	if ($isBz21) { $bz1->bzreadline($line1) ; }
	else { $line1 = <$file1> ; }
}

sub openOsm2File {
	$file2Name = shift ;
	if (grep /.bz2/, $file2Name) { $isBz22 = 1 ; } else { $isBz22 = 0 ; }
	if ($isBz22) {
		$bz2 = bzopen($file2Name, "rb") or die "Cannot open $file2Name: $bzerrno\n" ;
	}
	else {
		open ($file2, "<", $file2Name) || die "can't open osm file2" ;
	}
	nextLine2() ;		
	while ( ! (grep /\<node/, $line2) ) {
		nextLine2() ;
	}
	return 1 ;
}

sub closeOsm2File {
	if ($isBz22) { $bz2->bzclose() ; }
	else { close ($file2) ; }
}

sub nextLine2 {
	if ($isBz22) { $bz2->bzreadline($line2) ; }
	else { $line2 = <$file2> ; }
}

