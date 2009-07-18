# 
#
# relationdiff.pl by gary68
#
# this program check for connections at start or end of a way. this is intended to check motorways, trunks, their links, primary, secondary and
# tertiary highways. it might not be too useful for i.e. highway=residential
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

use strict ;
use warnings ;

use OSM::osm 4.0 ;

my $program = "relationdiff.pl" ;
my $usage = $program . " file1.osm file2.osm out.htm" ;
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

my $relationId ;
my $relationUser ;
my @relationMembers ;
my @relationTags ;

my $relationCount = 0 ;
my $relationCount2 = 0 ;

my @neededWays = () ;
my @neededNodes = () ;

my %lon ; my %lat ;

my $osm1Name ; 
my $osm2Name ; 
my $htmlName ; my $html ;

my %relMem1 ;
my %relMem2 ;
my %relTags1 ;
my %relTags2 ;
my %relUser1 ;
my %relUser2 ;

my $time0 = time() ;

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

print "\n$program $version for files $osm1Name $osm2Name\n\n" ;


print "parsing relations file1...\n" ;
openOsmFile ($osm1Name) ;
skipNodes() ;
skipWays() ;


($relationId, $relationUser, $aRef1, $aRef2) = getRelation () ;
if ($relationId != -1) {
	@relationMembers = @$aRef1 ;
	@relationTags = @$aRef2 ;
}

while ($relationId != -1) {
	$relationCount++ ;	


	$relUser1{$relationId} = $relationUser ;
	@{$relMem1{$relationId}}= @relationMembers ;
	@{$relTags1{$relationId}}= @relationTags ;

	#next
	($relationId, $relationUser, $aRef1, $aRef2) = getRelation () ;
	if ($relationId != -1) {
		@relationMembers = @$aRef1 ;
		@relationTags = @$aRef2 ;
	}
}

closeOsmFile () ;

print "parsing relations file2...\n" ;
openOsmFile ($osm2Name) ;
skipNodes() ;
skipWays() ;


($relationId, $relationUser, $aRef1, $aRef2) = getRelation () ;
if ($relationId != -1) {
	@relationMembers = @$aRef1 ;
	@relationTags = @$aRef2 ;
}

while ($relationId != -1) {

	$relationCount2++ ;	
	$relUser2{$relationId} = $relationUser ;
	@{$relMem2{$relationId}}= @relationMembers ;
	@{$relTags2{$relationId}}= @relationTags ;

	#next
	($relationId, $relationUser, $aRef1, $aRef2) = getRelation () ;
	if ($relationId != -1) {
		@relationMembers = @$aRef1 ;
		@relationTags = @$aRef2 ;
	}
}

closeOsmFile () ;

print "comparison and write html...\n" ;

open ($html, ">", $htmlName) || die ("Can't open html output file") ;

printHTMLHeader ($html, "Relation Diff by Gary68") ;

print $html "<H1>Relation Diff by Gary68</H1>\n" ;
print $html "<p>Version ", $version, "</p>\n" ;
print $html "<H2>Statistics</H2>\n" ;
print $html "<p>", stringFileInfo ($osm1Name), "</p>\n" ;
print $html "<p>", stringFileInfo ($osm2Name), "</p>\n" ;

print $html "<H2>Info</H2>\n" ;
print $html "<p>Number relations file 1: $relationCount</p>\n" ;
print $html "<p>Number relations file 2: $relationCount2</p>\n" ;


print $html "<H2>New Relations</H2>\n" ;
my $line = 0 ;
printHTMLTableHead ($html) ;
printHTMLTableHeadings ($html, "Line", "RelationId", "Tags", "Members") ;

my $relId ;
foreach $relId (keys %relMem2) {
	if (! (defined $relMem1{$relId})) {
		$line++ ;
		printHTMLRowStart ($html) ;
		printHTMLCellLeft ($html, $line) ;
		printHTMLCellLeft ($html, historyLink ("relation", $relId)) ;
		my $tagsText = "" ; my $membersText = "" ; my $tag ; my @member ;
		if (scalar (@{$relTags2{$relId}}) > 0) {
			my $i ;
			for ($i=0; $i<scalar (@{$relTags2{$relId}}); $i++) {
				$tagsText = $tagsText . ${$relTags2{$relId}[$i]}[0] . " : " . ${$relTags2{$relId}[$i]}[1] . "<br>";
			}
		}

		if (scalar (@{$relMem2{$relId}}) > 0) {
			my $i ;
			for ($i=0; $i<scalar (@{$relMem2{$relId}}); $i++) {
				$membersText = $membersText . ${$relMem2{$relId}[$i]}[0] . " : " . historyLink (${$relMem2{$relId}[$i]}[0], ${$relMem2{$relId}[$i]}[1]) . " : " . ${$relMem2{$relId}[$i]}[2] . "<br>";
			}
		}

		printHTMLCellLeft ($html, $tagsText) ;
		printHTMLCellLeft ($html, $membersText) ;
		printHTMLRowEnd ($html) ;
	}
}

printHTMLTableFoot ($html) ;

print $html "<H2>Deleted Relations</H2>\n" ;
$line = 0 ;
printHTMLTableHead ($html) ;
printHTMLTableHeadings ($html, "Line", "RelationId", "Tags", "Members") ;

foreach $relId (keys %relMem1) {
	if (! (defined $relMem2{$relId})) {
		$line++ ;
		printHTMLRowStart ($html) ;
		printHTMLCellLeft ($html, $line) ;
		printHTMLCellLeft ($html, historyLink ("relation", $relId)) ;
		my $tagsText = "" ; my $membersText = "" ; my $tag ; my @member ;
		if (scalar (@{$relTags1{$relId}}) > 0) {
			my $i ;
			for ($i=0; $i<scalar (@{$relTags1{$relId}}); $i++) {
				$tagsText = $tagsText . ${$relTags1{$relId}[$i]}[0] . " : " . ${$relTags1{$relId}[$i]}[1] . "<br>";
			}
		}

		if (scalar (@{$relMem1{$relId}}) > 0) {
			my $i ;
			for ($i=0; $i<scalar (@{$relMem1{$relId}}); $i++) {
				$membersText = $membersText . ${$relMem1{$relId}[$i]}[0] . " : " . historyLink (${$relMem1{$relId}[$i]}[0], ${$relMem1{$relId}[$i]}[1]) . " : " . ${$relMem1{$relId}[$i]}[2] . "<br>";
			}
		}

		printHTMLCellLeft ($html, $tagsText) ;
		printHTMLCellLeft ($html, $membersText) ;
		printHTMLRowEnd ($html) ;
	}
}

printHTMLTableFoot ($html) ;


print $html "<H2>Changed Relations</H2>\n" ;
$line = 0 ;
#printHTMLTableHead ($html) ;
print $html "<table width=\"100%\" border=\"1\">\n" ;
#print $html "<colgroup>\n" ;
#print $html "<col width=\"5%\">\n" ;
#print $html "<col width=\"10%\">\n" ;
#print $html "<col width=\"25%\">\n" ;
#print $html "<col width=\"15%\">\n" ;
#print $html "<col width=\"15%\">\n" ;
#print $html "<col width=\"15%\">\n" ;
#print $html "<col width=\"15%\">\n" ;
#print $html "</colgroup>\n" ;
#printHTMLTableHeadings ($html, "Line", "RelationId", "Tags (old)", "Deleted Tags", "Added Tags", "Members", "Deleted Members", "Added Members") ;
#printHTMLTableHeadings ($html, "Line", "RelationId", "Tags (old)", "Deleted Tags", "Added Tags", "Deleted Members", "Added Members") ;
printHTMLTableHeadings ($html, "Line", "RelationId", "Tags (old)", "Changes") ;
#print $html "<tr>\n" ; 
#print $html "<th width=\"5%\">Line</th>\n" ;
#print $html "<th width=\"10%\">RelationId</th>\n" ; 
#print $html "<th width=\"25%\">Tags (old)</th>\n" ; 
#print $html "<th width=\"15%\">Deleted Tags</th>\n" ; 
#print $html "<th width=\"15%\">Added Tags</th>\n" ; 
#print $html "<th width=\"15%\">Deleted Members</th>\n" ; 
#print $html "<th width=\"15%\">Added members</th>\n" ; 
#print $html "</tr>\n" ; 

foreach $relId (keys %relMem1) {
	if (defined $relMem2{$relId}) {

		my $relChanged = 0 ;

		my $deletedTags = "" ;
		if (scalar (@{$relTags1{$relId}}) > 0) {
			my $i ;
			for ($i=0; $i<scalar (@{$relTags1{$relId}}); $i++) {
				my $found = 0 ;
				if (scalar (@{$relTags2{$relId}}) > 0) {
					my $j ;
					for ($j=0; $j<scalar (@{$relTags2{$relId}}); $j++) {
						if ( (${$relTags1{$relId}[$i]}[0] eq ${$relTags2{$relId}[$j]}[0]) and
							(${$relTags1{$relId}[$i]}[1] eq ${$relTags2{$relId}[$j]}[1]) ) { $found = 1 ; }
					}
				}
				if ( ($found == 0) and (! (grep /created_by/, ${$relTags1{$relId}[$i]}[0])) ) {
					$deletedTags = $deletedTags . ${$relTags1{$relId}[$i]}[0] . " : " . ${$relTags1{$relId}[$i]}[1] . "<br>";
					$relChanged = 1 ;
				}
			}
		}

		my $addedTags = "" ;
		if (scalar (@{$relTags2{$relId}}) > 0) {
			my $i ;
			for ($i=0; $i<scalar (@{$relTags2{$relId}}); $i++) {
				my $found = 0 ;
				if (scalar (@{$relTags1{$relId}}) > 0) {
					my $j ;
					for ($j=0; $j<scalar (@{$relTags1{$relId}}); $j++) {
						if ( (${$relTags2{$relId}[$i]}[0] eq ${$relTags1{$relId}[$j]}[0]) and
							(${$relTags2{$relId}[$i]}[1] eq ${$relTags1{$relId}[$j]}[1]) ) { $found = 1 ; }
					}
				}
				if ( ($found == 0) and (! (grep /created_by/, ${$relTags2{$relId}[$i]}[0]) ) ) {
					$addedTags = $addedTags . ${$relTags2{$relId}[$i]}[0] . " : " . ${$relTags2{$relId}[$i]}[1] . "<br>";
					$relChanged = 1 ;
				}
			}
		}
		

		# check members
		# check 1 in 2. not, then 1 deleted
		my $deletedMembers = "" ;
		if (scalar (@{$relMem1{$relId}}) > 0) {
			my $i ;
			for ($i=0; $i<scalar (@{$relMem1{$relId}}); $i++) {
				my $found = 0 ;
				if (scalar (@{$relMem2{$relId}}) > 0) {
					my $j ;
					for ($j=0; $j<scalar (@{$relMem2{$relId}}); $j++) {
						if ( (${$relMem1{$relId}[$i]}[0] eq ${$relMem2{$relId}[$j]}[0]) and
							(${$relMem1{$relId}[$i]}[1] eq ${$relMem2{$relId}[$j]}[1]) and
							(${$relMem1{$relId}[$i]}[2] eq ${$relMem2{$relId}[$j]}[2]) ) { $found = 1 ; }
					}
				}
				if ( ($found == 0) ) {
					$deletedMembers = $deletedMembers . ${$relMem1{$relId}[$i]}[0] . " : " . historyLink (${$relMem1{$relId}[$i]}[0], ${$relMem1{$relId}[$i]}[1])  . " : " . ${$relMem1{$relId}[$i]}[2]. "<br>";
					$relChanged = 1 ;
				}
			}
		}



		# check 2 in 1. not, then 2 added
		my $addedMembers = "" ;
		if (scalar (@{$relMem2{$relId}}) > 0) {
			my $i ;
			for ($i=0; $i<scalar (@{$relMem2{$relId}}); $i++) {
				my $found = 0 ;
				if (scalar (@{$relMem1{$relId}}) > 0) {
					my $j ;
					for ($j=0; $j<scalar (@{$relMem1{$relId}}); $j++) {
						if ( (${$relMem2{$relId}[$i]}[0] eq ${$relMem1{$relId}[$j]}[0]) and
							(${$relMem2{$relId}[$i]}[1] eq ${$relMem1{$relId}[$j]}[1]) and
							(${$relMem2{$relId}[$i]}[2] eq ${$relMem1{$relId}[$j]}[2]) ) { $found = 1 ; }
					}
				}
				if ( ($found == 0) ) {
					$addedMembers = $addedMembers . ${$relMem2{$relId}[$i]}[0] . " : " . historyLink (${$relMem2{$relId}[$i]}[0], ${$relMem2{$relId}[$i]}[1])  . " : " . ${$relMem2{$relId}[$i]}[2]. "<br>";
					$relChanged = 1 ;
				}
			}
		}

		my $tagsText = "" ; 
		if (scalar (@{$relTags1{$relId}}) > 0) {
			my $i ;
			for ($i=0; $i<scalar (@{$relTags1{$relId}}); $i++) {
				$tagsText = $tagsText . ${$relTags1{$relId}[$i]}[0] . " : " . ${$relTags1{$relId}[$i]}[1] . "<br>";
			}
		}

		my $membersText = "" ;
		if (scalar (@{$relMem1{$relId}}) > 0) {
			my $i ;
			for ($i=0; $i<scalar (@{$relMem1{$relId}}); $i++) {
				$membersText = $membersText . ${$relMem1{$relId}[$i]}[0] . " : " . historyLink (${$relMem1{$relId}[$i]}[0], ${$relMem1{$relId}[$i]}[1]) . " : " . ${$relMem1{$relId}[$i]}[2] . "<br>";
			}
		}


		if ($relChanged) {
			$line++ ;
			printHTMLRowStart ($html) ;


			printHTMLCellLeft ($html, $line) ;
			printHTMLCellLeft ($html, historyLink ("relation", $relId)) ;
			printHTMLCellLeft ($html, $tagsText) ;
			

			my $changes = "<strong>Deleted Tags</strong><br>" . $deletedTags ;
			$changes = $changes . "<br><br><strong>Added Tags</strong><br>" . $addedTags ;
			$changes = $changes . "<br><br><strong>Deleted Members</strong><br>" . $deletedMembers ;
			$changes = $changes . "<br><br><strong>Added Members</strong><br>" . $addedMembers ;
			printHTMLCellLeft ($html, $changes) ;

			printHTMLRowEnd ($html) ;
		}
	}
}

printHTMLTableFoot ($html) ;





my $time1 = time() ;

print $html "<p>", stringTimeSpent ($time1-$time0), "</p>\n" ;
printHTMLFoot ($html) ;

close ($html) ;

print "\n$program finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;

