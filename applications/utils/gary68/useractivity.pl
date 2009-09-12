# 
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

use strict ;
use warnings ;

use OSM::osm ;
use File::stat;
use Time::localtime;

my $program = "useractivity.pl" ;
my $usage = $program . " file1.osm file2.osm out.htm [numTopUsers]" ;
my $version = "1.0 BETA" ;

my $topMax = 10 ;

my $wayId1 ; my $wayUser1 ; my @wayNodes1 ; my @wayTags1 ;
my $wayId2 ; my $wayUser2 ; my @wayNodes2 ; my @wayTags2 ;

my $nodeId1 ; my $nodeUser1 ; my $nodeLat1 ; my $nodeLon1 ; my @nodeTags1 ; my $nodeVersion1 ; my $nodeTimestamp1 ; my $nodeUid1 ; my $nodeChangeset1 ;
my $nodeId2 ; my $nodeUser2 ; my $nodeLat2 ; my $nodeLon2 ; my @nodeTags2 ; my $nodeVersion2 ; my $nodeTimestamp2 ; my $nodeUid2 ; my $nodeChangeset2 ;
my $aRef1 ; my $aRef2 ;

my $time0 = time() ; 

my $html ; my $htmlName ;
my $osm1Name ; my $osm2Name ;

my $file1 ; my $file2 ; my $bz1 ; my $bz2 ; my $isBz21 ; my $isBz22 ; my $line1 ; my $line2 ; my $bzerrno ;
my $file1Name ; my $file2Name ;

my %minLon ; my %minLat ; my %maxLon ; my %maxLat ; # per user
my $deletedNodes = 0 ; my $deletedNodesWithTags = 0 ;
my %nodesAdded ; my %nodesMovedNumber ; my %nodesMovedDistance ; my %nodesMovedMax ;
my %tagsAdded ; my %tagsDeleted ; my %tagsRenamed ; my %tagsReclassified ;
my %tagsDeletedName ;
my %renames ; 
my @deletedNodesIds = () ; my %deletedNodesTags = () ;
my %versionJumpsNodes = () ;

###############
# get parameter
###############
$osm1Name = shift||'';
if (!$osm1Name)
{
	die (print $usage, "\n");
}

$osm2Name = shift||'';
if (!$osm2Name)
{
	die (print $usage, "\n");
}

$htmlName = shift||'';
if (!$htmlName)
{
	die (print $usage, "\n");
}

$topMax = shift||'';
if (!$topMax)
{
	$topMax = 10 ;
}

print "\n$program $version for files:\n\n" ;
print stringFileInfo ($osm1Name), "\n"  ;
print stringFileInfo ($osm2Name), "\n\n"  ;

# test ways 1
# expand way fields
# test ways
# copy ways file 2
# test ways file 2


#------------------------------------------------------------------------------------
# Main
#------------------------------------------------------------------------------------

openOsm1File ($osm1Name) ;
($nodeId1, $nodeVersion1, $nodeTimestamp1, $nodeUid1, $nodeUser1, $nodeChangeset1, $nodeLat1, $nodeLon1, $aRef1) = getNode1 () ;
if ($nodeId1 != -1) {
	@nodeTags1 = @$aRef1 ;
}
openOsm2File ($osm2Name) ;
($nodeId2, $nodeVersion2, $nodeTimestamp2, $nodeUid2, $nodeUser2, $nodeChangeset2, $nodeLat2, $nodeLon2, $aRef1) = getNodeFile2 () ;
if ($nodeId2 != -1) {
	@nodeTags2 = @$aRef1 ;
}
processNodes() ;

# TODO get first ways
processWays() ;

closeOsm1File () ;
closeOsm2File () ;

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
				userArea ($nodeUser2, $nodeLon2, $nodeLat2) ;
				($nodeId2, $nodeVersion2, $nodeTimestamp2, $nodeUid2, $nodeUser2, $nodeChangeset2, $nodeLat2, $nodeLon2, $aRef1) = getNodeFile2 () ;
				if ($nodeId2 != -1) {
					@nodeTags2 = @$aRef1 ;
				}
			}
		}

		if ($nodeId2 == -1) {
			# get rest from file 1, deleted nodes
			while ( $nodeId1 != -1 ) {
				$deletedNodes++ ;
				if (scalar @nodeTags1 > 0) { 
					$deletedNodesWithTags++ ; 
					push @deletedNodesIds, $nodeId1 ;
					@{$deletedNodesTags{$nodeId1}} = @nodeTags1 ;
				}
				($nodeId1, $nodeVersion1, $nodeTimestamp1, $nodeUid1, $nodeUser1, $nodeChangeset1, $nodeLat1, $nodeLon1, $aRef1) = getNode1 () ;
				if ($nodeId1 != -1) {
					@nodeTags1 = @$aRef1 ;
				}
			}
		}

		if ( ($nodeId1 == $nodeId2) and ($nodeId1 != -1) ) {
			# print "equal $nodeId1     $nodeId2\n" ;
			# position...
			if ( ($nodeLon1 != $nodeLon2) or ($nodeLat1 != $nodeLat2) ) {
				$nodesMovedNumber{$nodeUser2}++ ;
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
			if ($renamed) { $tagsRenamed{$nodeUser2} += $renamed ; }
			# 2x next
			($nodeId2, $nodeVersion2, $nodeTimestamp2, $nodeUid2, $nodeUser2, $nodeChangeset2, $nodeLat2, $nodeLon2, $aRef1) = getNodeFile2 () ;
			if ($nodeId2 != -1) {
				@nodeTags2 = @$aRef1 ;
			}
			($nodeId1, $nodeVersion1, $nodeTimestamp1, $nodeUid1, $nodeUser1, $nodeChangeset1, $nodeLat1, $nodeLon1, $aRef1) = getNode1 () ;
			if ($nodeId1 != -1) {
				@nodeTags1 = @$aRef1 ;
			}
		}

		if ( ($nodeId1 > $nodeId2) and ($nodeId2 != -1) ) {
			# print "1 > 2 $nodeId1     $nodeId2   2-NEW\n" ;
			# id 2 not found in file 1, nodeId2 new
			$nodesAdded{$nodeUser2}++ ;
			userArea ($nodeUser2, $nodeLon2, $nodeLat2) ;
			# move file2 until id2>=id1
			while ( ($nodeId2 < $nodeId1) and ($nodeId2 != -1) ) {
				($nodeId2, $nodeVersion2, $nodeTimestamp2, $nodeUid2, $nodeUser2, $nodeChangeset2, $nodeLat2, $nodeLon2, $aRef1) = getNodeFile2 () ;
				if ($nodeId2 != -1) {
					@nodeTags2 = @$aRef1 ;
				}
			}
		}

		if ( ($nodeId1 < $nodeId2) and ($nodeId1 != -1) ) {
			# print "1 < 2 $nodeId1     $nodeId2   1-DELETED\n" ;
			# id 1 not found in file 2, nodeId1 deleted
			$deletedNodes++ ;
			if (scalar @nodeTags1 > 0) { 
				$deletedNodesWithTags++ ; 
				push @deletedNodesIds, $nodeId1 ;
				@{$deletedNodesTags{$nodeId1}} = @nodeTags1 ;
			}


			# move file1 until id1>=id2
			while ( ($nodeId1 < $nodeId2) and ($nodeId1 != -1) ) {
				($nodeId1, $nodeVersion1, $nodeTimestamp1, $nodeUid1, $nodeUser1, $nodeChangeset1, $nodeLat1, $nodeLon1, $aRef1) = getNode1 () ;
				if ($nodeId1 != -1) {
					@nodeTags1 = @$aRef1 ;
				}
			}
		}
	}
	print "finished.\n" ;
}

#------------------------------------------------------------------------------------
# Way procesing
#------------------------------------------------------------------------------------

sub processWays {

}

#------------------------------------------------------------------------------------
# Output functions
#------------------------------------------------------------------------------------

sub output {
	open ($html, ">", $htmlName) or die ("can't open html output file") ;
	printHTMLHeader ($html, "UserActvity by Gary68") ;
	print $html "<H1>UserActvity by gary68</H1>" ;
	print $html "<p>", stringFileInfo ($osm1Name), "</p>\n"  ;
	print $html "<p>", stringFileInfo ($osm2Name), "</p>\n"  ;
	print $html "<H1>Results</H1>" ;
	print $html "<p>DELETED NODES: $deletedNodes</p>\n" ;
	print $html "<p>DELETED NODES WITH TAGS: $deletedNodesWithTags (details see further down)</p>\n" ;

	my @a = () ;
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
		# print "USER: $u\n" ;
		push @a, [$u, distance ($minLon{$u}, $minLat{$u}, $minLon{$u}, $maxLat{$u}) * distance ($minLon{$u}, $minLat{$u}, $maxLon{$u}, $minLat{$u})] ;
	}
	printTop ("TOP work areas (km²)", 1, @a) ;

	@a = () ;
	foreach my $e (keys %tagsAdded) { push @a, [$e, $tagsAdded{$e}] ; }
	printTop ("TOP tags added", 0, @a) ;

	@a = () ;
	foreach my $e (keys %tagsRenamed) { push @a, [$e, $tagsRenamed{$e}] ; }
	printTop ("TOP objects renamed", 0, @a) ;

	my (@list) = @a ; @list = reverse (sort {$a->[1]<=>$b->[1]} @list) ;
	# if (scalar @list > $topMax) { @list = @list[0..$topMax-1] ; }
	print $html "<h2>Renames by ALL users</h2>\n" ;
	printHTMLTableHead ($html) ;
	printHTMLTableHeadings ($html, "User", "Tag", "Number") ; 
	foreach my $e (@list) {
		foreach my $t (keys %{$renames{$e->[0]}}) {
			# printf $html "%-25s %-80s %5i<br>\n" , $e->[0], $t, $renames{$e->[0]}{$t} ;
			printHTMLRowStart ($html) ;
			printHTMLCellLeft ($html, $e->[0]) ;			
			printHTMLCellLeft ($html, $t) ;			
			printHTMLCellRight ($html, $renames{$e->[0]}{$t}) ;			
			printHTMLRowEnd ($html) ;
		}
	}
	printHTMLTableFoot ($html) ;

	print $html "<h2>Version jumps nodes by ALL users</h2>\n" ;
	printHTMLTableHead ($html) ;
	printHTMLTableHeadings ($html, "User", "Nodes") ; 
	foreach my $u (keys %versionJumpsNodes) {
		printHTMLRowStart ($html) ;
		printHTMLCellLeft ($html, $u) ;			
		my $jumps = "" ;
		foreach my $v (@{$versionJumpsNodes{$u}}) {
			$jumps = $jumps . historyLink ("node", $v->[0]) . " (" . $v->[1] . ") " ;
		}
		printHTMLCellLeft ($html, $jumps) ;			
		printHTMLRowEnd ($html) ;
	}
	printHTMLTableFoot ($html) ;

	

	@a = () ;
	foreach my $e (keys %tagsDeleted) { push @a, [$e, $tagsDeleted{$e}] ; }
	printTop ("TOP tags deleted", 0, @a) ;

	@list = @a ; @list = reverse (sort {$a->[1]<=>$b->[1]} @list) ;
	if (scalar @list > $topMax) { @list = @list[0..$topMax-1] ; }
	print $html "<h2>Tags removed by TOP users</h2>\n" ;
	printHTMLTableHead ($html) ;
	printHTMLTableHeadings ($html, "User", "Rename", "Number") ; 
	foreach my $e (@list) {
		foreach my $t (keys %{$tagsDeletedName{$e->[0]}}) {
			# printf $html "%-25s %-50s %5i<br>\n" , $e->[0], $t, $tagsDeletedName{$e->[0]}{$t} ;
			printHTMLRowStart ($html) ;
			printHTMLCellLeft ($html, $e->[0]) ;			
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


	printHTMLFoot ($html) ;
	print $html "<p>$program finished after ", stringTimeSpent (time()-$time0), "</p>\n" ;
	close ($html) ;


	#@a = () ;
	#foreach my $e (keys %tagsReclassified) { push @a, [$e, $tagsReclassified{$e}] ; }
	#printTop ("Highways reclassified", @a) ;

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
		printHTMLCellLeft ($html, $e->[0]) ;			
		printHTMLCellRight ($html, $s) ;			
		printHTMLRowEnd ($html) ;
	} 
	printHTMLTableFoot ($html) ;
}


#------------------------------------------------------------------------------------
# some functions
#------------------------------------------------------------------------------------

sub userArea {
	my ($u, $lon, $lat) = @_ ;
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
	my $added = 0 ; my $deleted = 0 ; my $renamed = 0 ; my $reclassified = 0 ;
	my (@tags1) = @$aRef1 ; my (@tags2) = @$aRef2 ;

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
		$renames{$nodeUser2}{$nameOld . " -> " . $nameNew} ++ ;
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
	if ( ($highwayGiven == 1) and ($highwayNew ne $highwayOld) ) { $reclassified = 1 ; }

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

	return ($added, $deleted, $renamed, $reclassified) ;
} # compareTags

#------------------------------------------------------------------------------------
# Basic object operations
#------------------------------------------------------------------------------------

sub getNode1 {
	my ($id, $version, $timestamp, $uid, $user, $changeset, $lat, $lon) ;
	my @gTags = () ;
	if($line1 =~ /^\s*\<node/) {
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
			print "WARNING reading osm file, line follows (expecting id, lon, lat and user for node):\n", $line1, "\n" ; 
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
						#print "WARNING tag not recognized: ", $line1, "\n" ; 
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



# TODO copy 1->2
sub getWay1 {
	my $gId ; my $gU ; my @gTags ; my @gNodes ;
	if($line1 =~ /^\s*\<way/) {
		my ($id)   = ($line1 =~ /^\s*\<way id=[\'\"](\d+)[\'\"]/); # get way id
		my ($u) = ($line1 =~ /^.+user=[\'\"](.*)[\'\"]/);       # get value // REGEX???
		if (!$u) { $u = "unknown" ; }
		if (!$id) { print "ERROR reading osm file, line follows (expecting way id):\n", $line1, "\n" ; }
		unless ($id) { next; }
		nextLine1() ;
		while (not($line1 =~ /\/way>/)) { # more way data
			my ($node) = ($line1 =~ /^\s*\<nd ref=[\'\"](\d+)[\'\"]/); # get node id
			my ($k, $v) = ($line1 =~ /^\s*\<tag k=[\'\"](.+)[\'\"]\s*v=[\'\"](.+)[\'\"]/) ;
			if ($node) {
				push @gNodes, $node ;
			}
			if ($k and defined($v)) { my $tag = [$k, $v] ; push @gTags, $tag ; }
			nextLine1() ;
		}
		nextLine1() ;
		$gId = $id ; $gU = $u ;
	}
	else {
		return (-1, -1, -1, -1) ;
	}
	return ($gId, $gU, \@gNodes, \@gTags) ;
} # getWay1




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

