# 
#
# checkrelation.pl by gary68
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
#
# format boder file (defining the border polygon of the checked area) - same format osmosis accepts for cut polygons
#
# name
# 1
# lon lat
# lon lat
# ...
# END
# 
# version 1.2
# - analyzerLink definition removed from file. is defined in osm.pm
#
# version 1.3
# - double way in relation check added. according output added to html
#


use strict ;
use warnings ;

use File::stat ;
use Time::localtime ; 
#use String::Scanf ;

use OSM::osm 4.0 ;
use OSM::osmgraph 2.1 ;

my $program = "checkrelation.pl" ;
my $usage = $program . " <mode> file.osm baseDir baseName [borderName]\nmode=[M|Re|Ro|B|P]\nM=multipolygon, Re=restriction, Ro=route, B=boundary P=picture" ;
my $version = "1.3" ;

my @restrictions = qw (no_right_turn no_left_turn no_u_turn no_straight_on only_right_turn only_left_turn only_straight_on) ;
my @typesChecked = qw (restriction multipolygon boundary route) ; 

my $buffer = 0.15 ;
my $picSize = 1024 ;
my $borderThreshold = 1 ;

my %typehash ;

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
my $placeCount = 0 ;

my $relationCount = 0 ;
my $checkedRelationCount = 0 ;
my $members = 0 ;
my @member;
my $wayCount = 0 ; my $invalidWayCount = 0 ; my %invalidWays ;
my $nodeCount = 0 ;
my $problems = 0 ;

my @neededWays = () ;
my @neededNodes = () ;

my %lon ; my %lat ;
my %lineMax = () ; my %lineMin = () ;
my %wayNodesHash ;
my %placeName ;

my @borderWay = () ;

my $mode ;

my $baseDirName ;
my $baseName ;
my $osmName ; 
my $borderFileName = "" ; 
my $htmlName ; my $html ;
my $gpxName ; my $gpx ;

my $time0 = time() ;
my $totalBorderCheckTime = 0 ;
my $maxBorderCheckTime = 0 ;
my $totalSegmentsCheckTime = 0 ;
my $maxSegmentsCheckTime = 0 ;


$mode = shift||'';
if (!$mode)
{
	$mode = "MRoReB" ; # all
}

$osmName = shift||'';
if (!$osmName)
{
	die (print $usage, "\n");
}

$baseDirName = shift||'';
if (!$baseDirName)
{
	die (print $usage, "\n");
}

$baseName = shift||'';
if (!$baseName)
{
	die (print $usage, "\n");
}

$borderFileName = shift||'';
if (!$borderFileName)
{
	$borderFileName = "" ;
}

$htmlName = $baseDirName . "/" . $baseName . ".htm" ;
$gpxName = $baseDirName . "/" . $baseName . ".gpx" ;

print "\n$program $version \nfor file $osmName\nmode = $mode\nborder threshold = $borderThreshold km\n\n" ;

if ($borderFileName ne "") {
	readBorder ($borderFileName) ;
}

print "parsing relations...\n" ;
openOsmFile ($osmName) ;
print "- skipping nodes...\n" ;
skipNodes() ;
print "- skipping ways...\n" ;
skipWays() ;
print "- checking...\n" ;


($relationId, $relationUser, $aRef1, $aRef2) = getRelation () ;
if ($relationId != -1) {
	@relationMembers = @$aRef1 ;
	@relationTags = @$aRef2 ;
}

while ($relationId != -1) {
	$relationCount++ ;	
	$members += scalar (@relationMembers) ;

	my $i ;
	for ($i=0; $i<scalar (@relationMembers); $i++) {
		#print "${$relationMembers[$i]}[0] ${$relationMembers[$i]}[1] ${$relationMembers[$i]}[2]\n" ; # type, id, role
		if (${$relationMembers[$i]}[0] eq "way") { push @neededWays, ${$relationMembers[$i]}[1] ; }
		if (${$relationMembers[$i]}[0] eq "node") { push @neededNodes, ${$relationMembers[$i]}[1] ; }
	}

	if (scalar (@relationTags) > 0) {
		for ($i=0; $i<scalar (@relationTags); $i++) {
			#print "${$relationTags[$i]}[0] = ${$relationTags[$i]}[1]\n" ; 
			if ( ${$relationTags[$i]}[0] eq "type") { $typehash{   ${$relationTags[$i]}[1]     } = 1 ; }
		}
	}

	#next
	($relationId, $relationUser, $aRef1, $aRef2) = getRelation () ;
	if ($relationId != -1) {
		@relationMembers = @$aRef1 ;
		@relationTags = @$aRef2 ;
	}
}

closeOsmFile () ;

# parse ways for nodes
print "parsing ways...\n" ;
openOsmFile ($osmName) ;
print "- skipping nodes...\n" ;
skipNodes() ;

@neededWays = sort { $a <=> $b } @neededWays ;

($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {	
	my $needed = 0 ;
	$needed = binSearch ($wayId, \@neededWays ) ;
	if (scalar (@wayNodes) >= 2) {
		if ($needed >= 0) {
			$wayCount++ ;
			@{$wayNodesHash{$wayId}} = @wayNodes ;
			push @neededNodes, @wayNodes ;
		}
	}
	else {
		#print "invalid way (one node only): ", $wayId, "\n" ;
		$invalidWayCount++ ;
		$invalidWays{$wayId} = 1 ;
	}

	# next way
	($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;


# parse nodes for position and places
print "parsing nodes...\n" ;
openOsmFile ($osmName) ;

@neededNodes = sort { $a <=> $b } @neededNodes ;

($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
if ($nodeId != -1) {
	@nodeTags = @$aRef1 ;
}

while ($nodeId != -1) {
	my $needed = 0 ;

	my $Name = "" ; my $place = 0 ; my $tag ;
	foreach $tag (@nodeTags) {
		if ( (grep /^place:city/, $tag) and ( ($tag =~ s/://g ) == 1 ) ) { $place = 1 ; }
		if (grep /^place:town/, $tag) { $place = 1 ; }
		if (grep /^place_name:/, $tag) { $tag =~ s/^place_name:// ; $Name = $tag ; }
		my $tag2 = $tag ;
		if ( (grep /^name:/, $tag) and ( ($tag2 =~ s/://g ) == 1 ) ) { $tag =~ s/^name:// ; $Name = $tag ; }
	}
	if ( ($place == 1) and ($Name ne "") ) { $placeName{$nodeId} = $Name ; $placeCount++ ; }

	$needed = binSearch ($nodeId, \@neededNodes ) ;
	if ( ($needed >= 0) or ($place == 1) ) { $nodeCount++ ; $lon{$nodeId} = $nodeLon ; $lat{$nodeId} = $nodeLat }

	# lineExtr
	my $latKey = int ($nodeLat*100) / 100 ;
	if (defined $lineMax{$latKey}) {
		if ($nodeLon > $lineMax{$latKey}) {
			$lineMax{$latKey} = $nodeLon ;
		}
		if ($nodeLon < $lineMin{$latKey}) {
			$lineMin{$latKey} = $nodeLon ;
		}
	}
	else {
		$lineMax{$latKey} = $nodeLon ;
		$lineMin{$latKey} = $nodeLon ;
	}

	# next
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
	if ($nodeId != -1) {
		@nodeTags = @$aRef1 ;
	}
}

open ($html, ">", $htmlName) || die ("Can't open html output file") ;
open ($gpx, ">", $gpxName) || die ("Can't open gpx output file") ;

my $line = 0 ;
printHTMLHeader ($html, "Relation Check by Gary68") ;
printGPXHeader ($gpx) ;

print $html "<H1>Relation Check by Gary68</H1>\n" ;
print $html "<p>Version ", $version, "</p>\n" ;
print $html "<H2>Info</H2>\n" ;
print $html "<p>", stringFileInfo ($osmName), "<br>\n" ;
print $html "<p>Mode: $mode<br>\n" ;

print $html "<H2>Info</H2>\n" ;
print $html "<p>Checked relation types: @typesChecked</p>\n" ;
print $html "<p>Valid restrictions: @restrictions</p>\n" ;
print $html "<p>Border threshold: $borderThreshold km</p>\n" ;


print "parsing relations 2...\n" ;
print "- skipping ways...\n" ;
skipWays() ;
print "- checking...\n" ;

print "\nnumber relations found: $relationCount\n" ;

printHTMLTableHead ($html) ;
printHTMLTableHeadings ($html, "Line", "RelationId", "Tags", "Issues", "Links") ;

($relationId, $relationUser, $aRef1, $aRef2) = getRelation () ;
if ($relationId != -1) {
	@relationMembers = @$aRef1 ;
	@relationTags = @$aRef2 ;
}

my $work = 0 ;
while ($relationId != -1) {
	my $i ;
	my $type = "" ; my $tagText = "" ; my $double = 0 ;
	my $from = 0 ; my $via = 0  ; my $to = 0  ; my $restrictionType = "" ; my $viaNode = 0 ; my $viaWay = 0 ;
	my @openEnds ;

	$work++ ;
	# print "checking relation $relationId\n" ;
	if (($work % 1000) == 0) { print "...$work relations checked.\n" ; }

	if (scalar (@relationTags) > 0) {
		for ($i=0; $i<scalar (@relationTags); $i++) {
			if ( ${$relationTags[$i]}[0] eq "type") { $type = ${$relationTags[$i]}[1] ; }
			if ( ${$relationTags[$i]}[0] eq "restriction") { $restrictionType= ${$relationTags[$i]}[1] ; }
			if (grep /^type/, ${$relationTags[$i]}[0]) {
				$tagText = $tagText . "<strong>" . ${$relationTags[$i]}[0] . " : " . ${$relationTags[$i]}[1] . "</strong><br>\n" ;
			}
			else {
				$tagText = $tagText . ${$relationTags[$i]}[0] . " : " . ${$relationTags[$i]}[1] . "<br>\n" ;
			}
		}
	}

	my %count = () ;
	my @doubleWays = () ;
	foreach my $member (@relationMembers) {
		if ($member->[0] eq "way") {
			if (defined ($count{$member->[1]})) {
				$count{$member->[1]}++ ;
			}
			else {
				$count{$member->[1]} = 1 ;
			}
		}
	}

	foreach my $way (keys %count) {
		if ( $count{$way} > 1 ) {
			print "ERROR: relation $relationId contains way $way at least TWICE\n" ;
			$double = 1 ;
			push @doubleWays, $way ;
		}
	}

	##############
	# RESTRICTIONS
	##############

	if ( ($type eq "restriction") and (grep /Re/, $mode) ) {
		$checkedRelationCount++ ;
		for ($i=0; $i<scalar (@relationMembers); $i++) {
			#print "${$relationMembers[$i]}[0] ${$relationMembers[$i]}[1] ${$relationMembers[$i]}[2]\n" ; # type, id, role
			if ( (${$relationMembers[$i]}[0] eq "node") and (${$relationMembers[$i]}[2] eq "via") ) { $via++ ; $viaNode = ${$relationMembers[$i]}[1] ; }
			if ( (${$relationMembers[$i]}[0] eq "way") and (${$relationMembers[$i]}[2] eq "via") ) { $via++ ; $viaWay = ${$relationMembers[$i]}[1] ; }
			if ( (${$relationMembers[$i]}[0] eq "way") and (${$relationMembers[$i]}[2] eq "from") ) { $from++ ; }
			if ( (${$relationMembers[$i]}[0] eq "way") and (${$relationMembers[$i]}[2] eq "to") ) { $to++ ; }
		}
		my $validRestriction = 0 ;
		foreach (@restrictions) {
			if ($_ eq $restrictionType) { $validRestriction = 1 ; }
		}
		if ( (!$validRestriction) or ($via != 1) or ($from != 1) or ($from != 1) ) {
			$problems++ ;
			$line++ ;
			my $problemText = "" ;
			if (!$validRestriction) { $problemText = $problemText . "<strong>invalid restriction string: $restrictionType</strong> * " ; }
			if ($via == 0) { $problemText = $problemText . "<strong>no \"via\" specified</strong> * " ; }
			if ($via > 1) { $problemText = $problemText . "<strong>more than one \"via\" specified</strong> * " ; }
			if ($from !=1) { $problemText = $problemText . "<strong>number \"from\" ways != 1</strong> * " ; }
			if ($to !=1) { $problemText = $problemText . "<strong>number \"to\" ways != 1</strong> * " ; }
			printHTMLRowStart ($html) ;
			printHTMLCellLeft ($html, $line ) ;
			printHTMLCellLeft ($html, historyLink ("relation", $relationId) . "(OSM)<br>" . analyzerLink ($relationId) ) ;
			printHTMLCellLeft ($html, $tagText ) ;
			printHTMLCellLeft ($html, $problemText ) ;
			if ($viaNode != 0) {
				my $temp = "\"via\" node " . historyLink("node", $viaNode) . " in " ;
				$temp = $temp . josmLinkSelectNode ($lon{$viaNode}, $lat{$viaNode}, 0.003, $viaNode) . " * " ;
				$temp = $temp . osmLink ($lon{$viaNode}, $lat{$viaNode}, 16) . " * " ;
				$temp = $temp . osbLink ($lon{$viaNode}, $lat{$viaNode}, 16) ;
				printGPXWaypoint ($gpx, $lon{$viaNode}, $lat{$viaNode}, "restriction relation with problem(s)") ;
				printHTMLCellLeft ($html, $temp) ;
			}
			else {
				printHTMLCellLeft ($html, "") ;
			}
			printHTMLRowEnd ($html) ;
		}
	} # restriction


	##############
	# MULTIPOLYGON
	##############
	if ( ($type eq "multipolygon")  and (grep /M/, $mode) ) {
		$checkedRelationCount++ ;
		my $text = "" ; my $textInner = "" ; my $textOuter = "" ; my $textBoundary = "" ;
		my $inner = 0 ; my $outer = 0 ; my @innerWays = () ; my @outerWays = () ; my @noRoleWays = () ;
		my $check = 1 ; # 0 = contains invalid ways ;
		my $firstWay = 0 ;

		# parse members
		for ($i=0; $i<scalar (@relationMembers); $i++) {

			if ( (${$relationMembers[$i]}[0] eq "way") and (${$relationMembers[$i]}[2] eq "inner") ) { 
				if (defined ($invalidWays{${$relationMembers[$i]}[1]})) { $check = 0 ; }
				$inner++ ; push @innerWays, ${$relationMembers[$i]}[1] ; 
				if ($firstWay == 0) { $firstWay = ${$relationMembers[$i]}[1] ;}
			}
			if ( (${$relationMembers[$i]}[0] eq "way") and (${$relationMembers[$i]}[2] eq "outer") ) { 
				if (defined ($invalidWays{${$relationMembers[$i]}[1]})) { $check = 0 ; }
				$outer++ ; push @outerWays, ${$relationMembers[$i]}[1] ; 
				if ($firstWay == 0) { $firstWay = ${$relationMembers[$i]}[1] ;}
			}
			if ( (${$relationMembers[$i]}[0] eq "way") and (${$relationMembers[$i]}[2] eq "none") ) { 
				if ($firstWay == 0) { $firstWay = ${$relationMembers[$i]}[1] ;}
				push @noRoleWays, ${$relationMembers[$i]}[1] ;
				$text = "<strong>at least one way given without role</strong><br>" ;
			}
		}

		if ( ($check) and ($double == 0) ) {
			my $openTextInner = "" ; my $openTextOuter = "" ; my $noOuterText = "" ; 
	
			# CHECK OPEN WAYS/SEGMENTS
			my $segCount ; my $segOpenCount ; my @openEndsList = () ;
	
			if (scalar @innerWays > 0) {
				($segCount, $segOpenCount, @openEnds) = checkSegments2 (@innerWays) ;
				$textInner = "#inner segs: " . $segCount . " #open segs: " . $segOpenCount . "<br>" ;
				if ($segOpenCount != 0) {
					$text = $text . "<strong>at least one open inner segment</strong><br>\n" ;
					#$openTextInner = "<strong>JOSM links open ends inner ways:</strong><br> " . listEnds (@openEndsList) ;
					$openTextInner = "<strong>JOSM links open ends inner ways:</strong><br> " . listEnds (@openEnds) ;
					push @openEndsList, @openEnds ;
				}
			}
	
			if (scalar @outerWays > 0) {
				($segCount, $segOpenCount, @openEnds) = checkSegments2 (@outerWays) ;
				$textOuter = "#outer segs: " . $segCount . " #open segs: " . $segOpenCount . "<br>" ;
				if ($segOpenCount > 0) {
					$text = $text . "<strong>at least one open outer segment</strong><br>\n" ;
					#$openTextOuter = "<strong>JOSM links open ends outer ways:</strong><br> " . listEnds (@openEndsList) ;
					$openTextOuter = "<strong>JOSM links open ends outer ways:</strong><br> " . listEnds (@openEnds) ;
					push @openEndsList, @openEnds ;
				}
			}
			else {
				$text = $text . "<strong>no outer way</strong><br>\n" ;
				if ($firstWay != 0) {
					$noOuterText = "Link to first given way: " . 
						josmLinkSelectWay ($lon{$wayNodesHash{$firstWay}[0]}, $lat{$wayNodesHash{$firstWay}[0]}, 0.003, $firstWay) ;
				}
			}
			if (($text ne "") and (minDistToBorderOK (@openEndsList) )  ) {
				$line++ ;
				$problems++ ;
				#print "relation: $relationId distance: ", minDistToBorder(@innerWays, @outerWays), "\n" ;
				printHTMLRowStart ($html) ;
				printHTMLCellLeft ($html, $line ) ;
				if (grep /P/, $mode) {
					printHTMLCellLeft ($html, historyLink ("relation", $relationId) . "(OSM)<br>" . analyzerLink ($relationId) . "<br>" . linkLocal ($relationId) ) ;
				}
				else {
					printHTMLCellLeft ($html, historyLink ("relation", $relationId) . "(OSM)<br>" . analyzerLink ($relationId) ) ;
				}
				printHTMLCellLeft ($html, $tagText ) ;
				printHTMLCellLeft ($html, $textOuter . $textInner . $text ) ;
				printHTMLCellLeft ($html, $openTextInner . "<br>" . $openTextOuter  . "<br>" . $noOuterText) ;
				printHTMLRowEnd ($html) ;

				my $node ;
				foreach $node (@openEndsList) {
					printGPXWaypoint ($gpx, $lon{$node}, $lat{$node}, "open end from multypolygon relation id=" . $relationId ) ;
				}

				if (grep /P/, $mode) {
					my @initWays ; 
					push @initWays, @innerWays, @outerWays, @noRoleWays ;
					if (scalar @initWays > 0) {
						my ($lonMin, $latMin, $lonMax, $latMax) = calcRange (@initWays) ;
						my $way ; my $node ;
						initGraph ($picSize, $lonMin, $latMin, $lonMax, $latMax) ;
						drawPlaces() ;
						drawBorder2 (@borderWay) ;
						foreach $way (@innerWays) {
							drawWay ("blue", 2, nodes2Coordinates (@{$wayNodesHash{$way}}) ) ;
						}
						foreach $way (@outerWays) {
							drawWay ("black", 2, nodes2Coordinates (@{$wayNodesHash{$way}}) ) ;
						}
						foreach $way (@noRoleWays) {
							drawWay ("gray", 2, nodes2Coordinates (@{$wayNodesHash{$way}}) ) ;
						}
						foreach $node (@openEndsList) {
							drawNodeCircle ($lon{$node}, $lat{$node}, "red", 7) ; # / size (1..5)
							drawTextPos ($lon{$node}, $lat{$node}, 3, 3, $node, "red", 2)
						}
						drawHead ($program . " ". $version . " by Gary68 for Id: " . $relationId . ", " . $type, "black", 3) ;
						drawFoot ("data by openstreetmap.org" . " " . $osmName . " " .ctime(stat($osmName)->mtime), "gray", 3) ;
						drawLegend (3, "Border of file", "green", "Open end", "red", "Inner way", "blue", "No role (defaults to outer)", "gray", "Outer way", "black") ;
						drawRuler ("black") ;
						writeGraph ($baseDirName . "/" . $baseName . $relationId . ".png") ;
					}
				}
			}
		}
		else {
			if ($double == 1) {
				$line++ ;
				$problems++ ;
				printHTMLRowStart ($html) ;
				printHTMLCellLeft ($html, $line ) ;
				printHTMLCellLeft ($html, historyLink ("relation", $relationId) . "(OSM)<br>" . analyzerLink ($relationId) ) ;
				printHTMLCellLeft ($html, $tagText ) ;
				printHTMLCellLeft ($html, "Relation contains ways twice: @doubleWays\n" ) ;
				printHTMLCellLeft ($html, "" ) ;
				printHTMLRowEnd ($html) ;
			}
		}
	} # multipolygon


	##########
	# BOUNDARY
	##########
	if ( ($type eq "boundary") and (grep /B/, $mode) ) {
		$checkedRelationCount++ ;
		my $text = "" ;
		my $textInner = "" ; my $textOuter = "" ; my $textBoundary = "" ;
		my $inner = 0 ; my $outer = 0 ; my $boundary = 0 ; my @innerWays = () ; my @outerWays = () ; my @boundaryWays = () ; 
		my @openEndsList = () ;
		my $check = 1 ; # 0 = contains invalid ways ;

		# parse members
		for ($i=0; $i<scalar (@relationMembers); $i++) {
			if ( (${$relationMembers[$i]}[0] eq "way") and ((${$relationMembers[$i]}[2] eq "inner") or (${$relationMembers[$i]}[2] eq "enclave") ) ) { 
				if (defined ($invalidWays{${$relationMembers[$i]}[1]})) { $check = 0 ; }
				$inner++ ; push @innerWays, ${$relationMembers[$i]}[1] ; 
			}
			if ( (${$relationMembers[$i]}[0] eq "way") and ((${$relationMembers[$i]}[2] eq "outer") or (${$relationMembers[$i]}[2] eq "exclave") ) ) { 
				if (defined ($invalidWays{${$relationMembers[$i]}[1]})) { $check = 0 ; }
				$outer++ ; push @outerWays, ${$relationMembers[$i]}[1] ; 
			}
			if ( (${$relationMembers[$i]}[0] eq "way") and (${$relationMembers[$i]}[2] eq "none") ) { 
				if (defined ($invalidWays{${$relationMembers[$i]}[1]})) { $check = 0 ; }
				$boundary++ ; push @boundaryWays, ${$relationMembers[$i]}[1] ; 
			}

			# RELATION?
			if  (${$relationMembers[$i]}[0] eq "relation") { 
				$check = 0 ; # TODO
			}
		}

		if ( ($check) and ($double == 0) ) {
			my $segCount ; my $segOpenCount ;
			my $openTextInner = "" ; my $openTextOuter = "" ; my $openTextBoundary = "" ;

			#boundary
			if (scalar @boundaryWays > 0) {
				($segCount, $segOpenCount, @openEnds) = checkSegments2 (@boundaryWays) ;
				$textBoundary = "#boundary segs: " . $segCount . " #open segs: " . $segOpenCount . "<br>" ;
				if ($segOpenCount != 0) {
					$text = "<strong>at least one open boundary segment</strong><br>\n" ;
					$openTextBoundary = "<strong>JOSM links open ends boundary ways:</strong><br> " . listEnds (@openEnds) ;
					push @openEndsList, @openEnds ;
				}
			}
			else {
				$text = $text . "<strong>no boundary ways</strong><br>" ;
			}

			#enclave
			if (scalar @innerWays > 0) {
				($segCount, $segOpenCount, @openEnds) = checkSegments2 (@innerWays) ;
				$textInner = "#inner segs: " . $segCount . " #open segs: " . $segOpenCount . "<br>" ;
				if ($segOpenCount != 0) {
					$text = "<strong>at least one open inner (enclave) segment</strong><br>\n" ;
					$openTextInner = "<strong>JOSM links open ends inner/enclave ways:</strong><br> " . listEnds (@openEnds) ;
					push @openEndsList, @openEnds ;
				}
			}

			#exclave
			if (scalar @outerWays > 0) {
				($segCount, $segOpenCount, @openEnds) = checkSegments2 (@outerWays) ;
				$textOuter = "#outer segs: " . $segCount . " #open segs: " . $segOpenCount . "<br>" ;
				if ($segOpenCount != 0) {
					$text = "<strong>at least one open outer (exclave) segment</strong><br>\n" ;
					$openTextOuter = "<strong>JOSM links open ends outer/exclave ways:</strong><br> " . listEnds (@openEnds) ;
					push @openEndsList, @openEnds ;
				}
			}
			if (($text ne "") and ( minDistToBorderOK (@openEndsList) ) ) {
				$line++ ;
				$problems++ ;
				printHTMLRowStart ($html) ;
				printHTMLCellLeft ($html, $line ) ;
				if (grep /P/, $mode) {
					printHTMLCellLeft ($html, historyLink ("relation", $relationId) . "(OSM)<br>" . analyzerLink ($relationId) . "<br>" . linkLocal ($relationId) ) ;
				}
				else {
					printHTMLCellLeft ($html, historyLink ("relation", $relationId) . "(OSM)<br>" . analyzerLink ($relationId) ) ;
				}
				printHTMLCellLeft ($html, $tagText ) ;
				printHTMLCellLeft ($html, $textBoundary . $textOuter . $textInner . $text ) ;
				printHTMLCellLeft ($html, $openTextBoundary . "<br>" . $openTextInner . "<br>" . $openTextOuter ) ;
				printHTMLRowEnd ($html) ;

				my $node ;
				foreach $node (@openEndsList) {
					printGPXWaypoint ($gpx, $lon{$node}, $lat{$node}, "open end from boundary relation id=" . $relationId ) ;
				}

				if (grep /P/, $mode) {
					my @initWays ; 
					push @initWays, @innerWays, @outerWays, @boundaryWays ;
					if (scalar @initWays > 0) {
						my ($lonMin, $latMin, $lonMax, $latMax) = calcRange (@initWays) ;
						my $way ; my $node ;
						initGraph ($picSize, $lonMin, $latMin, $lonMax, $latMax) ;
						drawPlaces() ;
						drawBorder2 (@borderWay) ;
						foreach $way (@innerWays) {
							drawWay ("blue", 2, nodes2Coordinates (@{$wayNodesHash{$way}}) ) ;
						}
						foreach $way (@outerWays) {
							drawWay ("red", 2, nodes2Coordinates (@{$wayNodesHash{$way}}) ) ;
						}
						foreach $way (@boundaryWays) {
							drawWay ("black", 2, nodes2Coordinates (@{$wayNodesHash{$way}}) ) ;
						}
						foreach $node (@openEndsList) {
							drawNodeCircle ($lon{$node}, $lat{$node}, "red", 7) ; # / size (1..5)
							drawTextPos ($lon{$node}, $lat{$node}, 3, 3, $node, "red", 2)
						}
						drawHead ($program . " ". $version . " by Gary68 for Id: " . $relationId . ", " . $type, "black", 3) ;
						drawFoot ("data by openstreetmap.org" . " " . $osmName . " " .ctime(stat($osmName)->mtime), "gray", 3) ;
						drawLegend (3, "Border of file", "green", "Open end", "red", "Inner way", "blue", "Outer way", "red", "Boundary way", "black") ;
						drawRuler ("black") ;
						writeGraph ($baseDirName . "/" . $baseName . $relationId . ".png") ;
					}
				}

			}
		}
		else {
			if ($double == 1) {
				$line++ ;
				$problems++ ;
				printHTMLRowStart ($html) ;
				printHTMLCellLeft ($html, $line ) ;
				printHTMLCellLeft ($html, historyLink ("relation", $relationId) . "(OSM)<br>" . analyzerLink ($relationId) ) ;
				printHTMLCellLeft ($html, $tagText ) ;
				printHTMLCellLeft ($html, "Relation contains ways twice: @doubleWays\n" ) ;
				printHTMLCellLeft ($html, "" ) ;
				printHTMLRowEnd ($html) ;
			}
		}
	} # boundary 

	#######
	# ROUTE
	#######
	if ( ($type eq "route") and (grep /Ro/, $mode) ) {
		$checkedRelationCount++ ;
		my $text = "" ;
		my $textForward = "" ; my $textBackward = "" ;
		my $route = 0 ; my @forwardWays = () ; my @backwardWays = () ; 
		my $openTextForward = "" ; my $openTextBackward = "" ; 
		my @normalWaysDraw = () ; my @forwardWaysDraw = () ; my @backwardWaysDraw = () ; my @openEndsList = () ; my @otherWaysDraw = () ;
		my $check = 1 ; # 0 = contains invalid ways ;

		# parse members
		for ($i=0; $i<scalar (@relationMembers); $i++) {
			my ($role) = ${$relationMembers[$i]}[2] ;
			#print "${$relationMembers[$i]}[0] ${$relationMembers[$i]}[1] ROLE: ${$relationMembers[$i]}[2]\n" ; # type, id, role
			if ( (${$relationMembers[$i]}[0] eq "way") and (${$relationMembers[$i]}[2] eq "none") ) { 
				if (defined ($invalidWays{${$relationMembers[$i]}[1]})) { $check = 0 ; }
				$route++ ; 
				push @forwardWays, ${$relationMembers[$i]}[1] ; 
				push @backwardWays, ${$relationMembers[$i]}[1] ; 
				push @normalWaysDraw, ${$relationMembers[$i]}[1] ;
			}
			if ( (${$relationMembers[$i]}[0] eq "way") and (${$relationMembers[$i]}[2] eq "forward") ) { 
				if (defined ($invalidWays{${$relationMembers[$i]}[1]})) { $check = 0 ; }
				$route++ ; 
				push @forwardWays, ${$relationMembers[$i]}[1] ; 
				push @forwardWaysDraw, ${$relationMembers[$i]}[1] ; 
			}
			if ( (${$relationMembers[$i]}[0] eq "way") and (${$relationMembers[$i]}[2] eq "backward") ) { 
				if (defined ($invalidWays{${$relationMembers[$i]}[1]})) { $check = 0 ; }
				$route++ ; 
				push @backwardWays, ${$relationMembers[$i]}[1] ; 
				push @backwardWaysDraw, ${$relationMembers[$i]}[1] ; 
			}
			if ( (${$relationMembers[$i]}[0] eq "way") and 
				( ($role eq "shortcut") or ($role eq "variation") or ($role eq "excursion") ) ) { 
				if (defined ($invalidWays{${$relationMembers[$i]}[1]})) { $check = 0 ; }
				$route++ ; 
				push @otherWaysDraw, ${$relationMembers[$i]}[1] ; 
			}
		}

		if ( ($check) and ($double == 0) ) {
			my $segCount ; my $segOpenCount ;

			# forward
			if (scalar (@forwardWays) > 0) {
				($segCount, $segOpenCount, @openEnds) = checkSegments2 (@forwardWays) ;
				$textForward = "#fw segs: " . $segCount . "<br>" ;
				if ($segCount > 1) {
					$text = $text . "<strong>forward route segmented</strong><br>\n" ;
					$openTextForward = "<strong>JOSM links open ends forward ways:</strong><br> " . listEnds (@openEnds) ;
					push @openEndsList, @openEnds ;
				}
			}
			else {
				$text = $text . "<strong>no forward ways</strong><br>" ;
			}

			# backward
			if (scalar (@backwardWays) > 0) {
				($segCount, $segOpenCount, @openEnds) = checkSegments2 (@backwardWays) ;
				$textBackward = "#bw segs: " . $segCount . "<br>" ;
				if ($segCount > 1) {
					$text = $text . "<strong>backward route segmented</strong><br>\n" ;
					$openTextBackward = "<strong>JOSM links open ends backward ways:</strong><br> " . listEnds (@openEnds) ;
					push @openEndsList, @openEnds ;
				}
			}
			else {
				$text = $text . "<strong>no backward ways</strong><br>" ;
			}

			if (($text ne "") and ( minDistToBorderOK (@openEndsList) ) ) {
				$line++ ;
				$problems++ ;

				printHTMLRowStart ($html) ;
				printHTMLCellLeft ($html, $line ) ;
				if (grep /P/, $mode) {
					printHTMLCellLeft ($html, historyLink ("relation", $relationId) . "(OSM)<br>" . analyzerLink ($relationId) . "<br>" . linkLocal ($relationId) ) ;
				}
				else {
					printHTMLCellLeft ($html, historyLink ("relation", $relationId) . "(OSM)<br>" . analyzerLink ($relationId) ) ;
				}
				printHTMLCellLeft ($html, $tagText ) ;
				printHTMLCellLeft ($html, $textForward . $textBackward . $text ) ;
				printHTMLCellLeft ($html, $openTextForward . "<br>" . $openTextBackward ) ;
				printHTMLRowEnd ($html) ;

				if (grep /P/, $mode) {
					my @initWays ; 
					push @initWays, @forwardWaysDraw, @backwardWaysDraw, @normalWaysDraw, @otherWaysDraw ;
					if (scalar @initWays > 0) {
						my ($lonMin, $latMin, $lonMax, $latMax) = calcRange (@initWays) ;
						my $way ; my $node ;
						initGraph ($picSize, $lonMin, $latMin, $lonMax, $latMax) ;
						drawPlaces() ;
						drawBorder2 (@borderWay) ;
						foreach $way (@normalWaysDraw) {
							drawWay ("black", 2, nodes2Coordinates (@{$wayNodesHash{$way}}) ) ;
						}
						foreach $way (@forwardWaysDraw) {
							drawWay ("blue", 2, nodes2Coordinates (@{$wayNodesHash{$way}}) ) ;
						}
						foreach $way (@backwardWaysDraw) {
							drawWay ("red", 2, nodes2Coordinates (@{$wayNodesHash{$way}}) ) ;
						}
						foreach $way (@otherWaysDraw) {
							drawWay ("gray", 2, nodes2Coordinates (@{$wayNodesHash{$way}}) ) ;
						}
						foreach $node (@openEndsList) {
							drawNodeCircle ($lon{$node}, $lat{$node}, "red", 7) ; # / size (1..5)
							drawTextPos ($lon{$node}, $lat{$node}, 3, 3, $node, "red", 2)
						}
						drawHead ($program . " ". $version . " by Gary68 for Id: " . $relationId . ", " . $type, "black", 3) ;
						drawFoot ("data by openstreetmap.org" . " " . $osmName . " " .ctime(stat($osmName)->mtime), "gray", 3) ;
						drawLegend (3, "Border of file", "green", "Open end", "red", "Other way", "gray", "Forward way", "blue", "Backward way", "red", "Normal way", "black") ;
						drawRuler ("black") ;
						writeGraph ($baseDirName . "/" . $baseName . $relationId . ".png") ;
					}
				}

			}
		}
		else {
			if ($double == 1) {
				$line++ ;
				$problems++ ;
				printHTMLRowStart ($html) ;
				printHTMLCellLeft ($html, $line ) ;
				printHTMLCellLeft ($html, historyLink ("relation", $relationId) . "(OSM)<br>" . analyzerLink ($relationId) ) ;
				printHTMLCellLeft ($html, $tagText ) ;
				printHTMLCellLeft ($html, "Relation contains ways twice: @doubleWays\n" ) ;
				printHTMLCellLeft ($html, "" ) ;
				printHTMLRowEnd ($html) ;
			}
		}
	} # route

	#next
	($relationId, $relationUser, $aRef1, $aRef2) = getRelation () ;
	if ($relationId != -1) {
		@relationMembers = @$aRef1 ;
		@relationTags = @$aRef2 ;
	}
}

closeOsmFile () ;

print "\nTYPES FOUND\n" ;
foreach (sort keys %typehash) { print "- ", $_, "\n" ; }
print "\n" ;

print "STATISTICS\n" ;
print "number problems $problems\n" ;
print "number relations $relationCount\n" ;
print "number checked relations $checkedRelationCount\n" ;
print "number members $members\n" ;
print "number member ways $wayCount\n" ;
print "number member ways invalid $invalidWayCount\n" ;
print "number related nodes $nodeCount\n" ;
print "number places $placeCount\n" ;
print "total segments check time $totalSegmentsCheckTime\n" ;
print "max segments check time $maxSegmentsCheckTime\n" ;
print "total border check time $totalBorderCheckTime\n" ;
print "max border check time $maxBorderCheckTime\n" ;



printHTMLTableFoot ($html) ;


print $html "<H2>Stats and counts</H2>\n" ;

print $html "<H3>TYPES FOUND</H3>\n" ;
print $html "<p>" ;
foreach (sort keys %typehash) { print $html "- ", $_, "<br>\n" ; }
print $html "</p>\n" ;

print $html "<H3>STATISTICS</H3>\n" ;
print $html "<p>number problems $problems<br>\n" ;
print $html "number relations $relationCount<br>\n" ;
print $html "number checked relations $checkedRelationCount<br>\n" ;
print $html "number members $members<br>\n" ;
print $html "number member ways $wayCount<br>\n" ;
print $html "number member ways invalid $invalidWayCount<br>\n" ;
print $html "number related nodes $nodeCount<br></p>\n" ;
print $html "number places $placeCount<br></p>\n" ;
print $html  "<p>total segments check time $totalSegmentsCheckTime<br>\n" ;
print $html  "max segments check time $maxSegmentsCheckTime<br>\n" ;print $html  "total border check time $totalBorderCheckTime<br>\n" ;
print $html  "max border check time $maxBorderCheckTime</p>\n" ;

my $time1 = time() ;

print $html "<p>", stringTimeSpent ($time1-$time0), "</p>\n" ;
printHTMLFoot ($html) ;
printGPXFoot ($gpx) ;

close ($html) ;
close ($gpx) ;

statistics ( ctime(stat($osmName)->mtime),  $program,  $baseName, $osmName,  $checkedRelationCount,  $problems) ;

print "\n$program finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;

sub listEnds {
	my (@ends) = @_ ;
	my $text = "" ;
	my $node ;
	if (scalar (@ends) > 0) {
		foreach $node (@ends) {
			$text = $text . "(" . historyLink ("node", $node) . " " . josmLinkSelectNode ($lon{$node}, $lat{$node}, 0.003, $node) . ") " ;
		}
	}
	return $text ;
}


sub calcRange {
	my (@ways) = @_ ;
	my $lonMin = 999 ;
	my $latMin = 999 ;
	my $lonMax = -999 ; 
	my $latMax = -999 ; 
	my $way ; my $node ;
	#print "ways: @ways\n" ;
	foreach $way (@ways) {
		#print "  way: $way\n" ;
		foreach $node (@{$wayNodesHash{$way}}) {
			#print "    node: $node\n" ;
			if ($lon{$node} > $lonMax) { $lonMax = $lon{$node} ; }
			if ($lat{$node} > $latMax) { $latMax = $lat{$node} ; }
			if ($lon{$node} < $lonMin) { $lonMin = $lon{$node} ; }
			if ($lat{$node} < $latMin) { $latMin = $lat{$node} ; }
		}
	}
	$lonMin = $lonMin - ($buffer * ($lonMax - $lonMin)) ;
	$latMin = $latMin - ($buffer * ($latMax - $latMin)) ;
	$lonMax = $lonMax + ($buffer * ($lonMax - $lonMin)) ;
	$latMax = $latMax + ($buffer * ($latMax - $latMin)) ;
	return ($lonMin, $latMin, $lonMax, $latMax) ;
}

sub nodes2Coordinates {
# transform list of nodeIds to list of lons/lats

	my @nodes = @_ ;
	my $i ;
	my @result = () ;

	for ($i=0; $i<=$#nodes; $i++) {
		push @result, $lon{$nodes[$i]} ;
		push @result, $lat{$nodes[$i]} ;
	}
	return @result ;
}

sub linkLocal {
	my ($id) = shift ;
	my $result = "<A HREF=\"./" . $baseName . $id . ".png\">Picture</A>" ;
	return $result ;
}

sub drawPlaces {
	my $place ; my $count = 0 ;
	foreach $place (keys %placeName) {
		drawNodeDot ($lon{$place}, $lat{$place}, "black", 2) ;
		drawTextPos ($lon{$place}, $lat{$place}, 0, 0, $placeName{$place}, "black", 2) ;
	}
}

sub drawBorder2 {
	my (@way) = @_ ;
	drawWay ("green", 2, nodes2Coordinates (@way) ) ;
}

sub statistics {
	my ($date, $program, $def, $area, $total, $errors) = @_ ;
	my $statfile ; my ($statfileName) = "statistics.csv" ;

	if (grep /\.bz2/, $area) { $area =~ s/\.bz2// ; }
	if (grep /\.osm/, $area) { $area =~ s/\.osm// ; }
	my ($area2) = ($area =~ /.+\/([\w\-]+)$/ ) ;
	if (! defined ($area2) ) { $area2 = "unknown" ; }

	my ($def2) = $baseName ;

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

sub checkSegments2 {
	my (@ways) = @_ ;
	my $way ; my $node ;
	my @openEnds = () ;
	my $segments = 0 ; my $openSegments = 0 ;
	my $found = 1 ;
	my $way1 ; my $way2 ;
	my $endNodeWay2 ;	my $startNodeWay2 ;
	my %starts = () ; my %ends = () ;
	my %wayStart = () ; my %wayEnd = () ;

	my $time1  = time() ;

	#init
	foreach $way (@ways) {
		push @{$starts{$wayNodesHash{$way}[0]}}, $way ;
		push @{$ends{$wayNodesHash{$way}[-1]}}, $way ;
		$wayStart{$way} = $wayNodesHash{$way}[0] ;
		$wayEnd{$way} = $wayNodesHash{$way}[-1] ;
	}

	while ($found == 1) {
		$found = 0 ;

		# check start/start
		loop1:
		foreach $node (keys %starts) {

			# if node with more than 1 connecting way...
			if (scalar (@{$starts{$node}}) > 1) {
				$way1 = ${$starts{$node}}[0] ; $way2 = ${$starts{$node}}[1] ;
				#print "merge start/start $way1 and $way2 at node $node\n" ;

				$endNodeWay2 = $wayEnd{$way2} ;
				#print "end node way2 = $endNodeWay2\n" ;

				# way1 gets new start: end way2
				push @{$starts{ $endNodeWay2 }}, $way1 ;
				$wayStart{$way1} = $endNodeWay2 ;

				# remove end way2
				if (scalar (@{$ends{$endNodeWay2}}) == 1) {
					delete $ends{$endNodeWay2} ;
					#print "$endNodeWay2 removed from end hash\n" ;
				}
				else {
					@{$ends{$endNodeWay2}} = removeElement ($way2, @{$ends{$endNodeWay2}}) ;
					#print "way $way2 removed from node $endNodeWay2 from end hash\n" ;
				}
				
				# remove way2
				delete $wayEnd{$way2} ;
				delete $wayStart{$way2} ;

				# remove connecting starts
				if (scalar @{$starts{$node}} == 2) {
					delete $starts{$node} ;
					#print "$node removed from start hash\n" ;
				}
				else {
					@{$starts{$node}} = @{$starts{$node}}[2..$#{$starts{$node}}] ;
					#print "first two elements removed from start hash node = $node\n" ;
				}
				#print "\n" ;
				$found = 1 ; 
				last loop1 ;
			}
		}

		# check end/end
		if (!$found) {
			loop2:
			foreach $node (keys %ends) {

				# if node with more than 1 connecting way...
				if (scalar @{$ends{$node}} > 1) {
					$way1 = ${$ends{$node}}[0] ; $way2 = ${$ends{$node}}[1] ;
					#print "merge end/end $way1 and $way2 at node $node\n" ;
	
					$startNodeWay2 = $wayStart{$way2} ;
					#print "start node way2 = $startNodeWay2\n" ;
	
					# way1 gets new end: start way2
					push @{$ends{ $startNodeWay2 }}, $way1 ;
					$wayEnd{$way1} = $startNodeWay2 ;
	
					# remove start way2
					if (scalar (@{$starts{$startNodeWay2}}) == 1) {
						delete $starts{$startNodeWay2} ;
						#print "$startNodeWay2 removed from start hash\n" ;
					}
					else {
						@{$starts{$startNodeWay2}} = removeElement ($way2, @{$starts{$startNodeWay2}}) ;
						#print "way $way2 removed from node $startNodeWay2 from start hash\n" ;
					}
				
					# remove way2
					delete $wayEnd{$way2} ;
					delete $wayStart{$way2} ;

					# remove connecting ends
					if (scalar @{$ends{$node}} == 2) {
						delete $ends{$node} ;
						#print "$node removed from end hash\n" ;
					}
					else {
						@{$ends{$node}} = @{$ends{$node}}[2..$#{$ends{$node}}] ;
						#print "first two elements removed from end hash node = $node\n" ;
					}
					#print "\n" ;
					$found = 1 ; 
					last loop2 ;
				}
			}
		}


		# check start/end
		if (!$found) {
			my $wayFound = 0 ;
			loop3:
			foreach $node (keys %starts) {
				if (exists ($ends{$node})) {
					#look for different! ways
					my (@startingWays) = @{$starts{$node}} ;
					my (@endingWays) = @{$ends{$node}} ;
					my $w1 ; my $w2 ;
					loop4:
					foreach $w1 (@startingWays) {
						foreach $w2 (@endingWays) {
							if ($w1 != $w2) {
								$wayFound = 1 ;
								$way1 = $w1 ; 
								$way2 = $w2 ; # merge w1 and w2
								#print "start/end: merge ways $way1 and $way2 connected at node $node\n" ;
								last loop4 ;
							}
						}
					} # look for ways
					if ($wayFound) {
						#print "way $way1 start $wayStart{$way1} end $wayEnd{$way1}\n" ;
						#print "way $way2 start $wayStart{$way2} end $wayEnd{$way2}\n" ;

						# way1 gets new start: start way2
						$wayStart{$way1} = $wayStart{$way2} ;
						my ($way2StartNode) = $wayStart{$way2} ;

						push @{$starts{$way2StartNode}}, $way1 ;
						#print "way $way1 added to starts for node $way2StartNode\n" ;

						# remove start way1
						if (scalar (@{$starts{$node}}) == 1) {
							delete $starts{$node} ;
							#print "$way1 removed from start hash for node $node\n" ;
						}
						else {
							@{$starts{$node}} = removeElement ($way1, @{$starts{$node}}) ;
							#print "$way1 removed from start hash for node $node\n" ;
						}

						#remove end way2
						if (scalar (@{$ends{$node}}) == 1) {
							delete $ends{$node} ;
							#print "$way2 removed from end hash for node $node\n" ;
						}
						else {
							@{$ends{$node}} = removeElement ($way2, @{$ends{$node}}) ;
							#print "$way2 removed from end hash for node $node\n" ;
						}
						#remove start way2
						if (scalar (@{$starts{$way2StartNode}}) == 1) {
							delete $starts{$way2StartNode} ;
							#print "$way2 removed from start hash for node $way2StartNode\n" ;
						}
						else {
							@{$starts{$way2StartNode}} = removeElement ($way2, @{$starts{$way2StartNode}}) ;
							#print "$way2 removed from start hash for node $way2StartNode\n" ;
						}

						# remove way2
						delete $wayEnd{$way2} ;
						delete $wayStart{$way2} ;
						#print "way $way2 removed from waystart and wayend hashes\n" ;

						#print "\n" ;
						$found = 1 ; 
						last loop3 ;
					}
				}
			}
		}
	}

	# evaluation


	#print "\nSUB RESULT\n" ;
	foreach $way (keys %wayStart) {
		#print "way $way start $wayStart{$way} end $wayEnd{$way}\n" ;
		if ($wayStart{$way} != $wayEnd{$way}) {
			$openSegments++ ;
			#print "   open!\n" ;
			push @openEnds, $wayStart{$way}, $wayEnd{$way} ;
		}
	}
	#print "SUB RESULT END\n" ;

	#print "check segments took ", time() - $time1, "seconds\n" ;
	$totalSegmentsCheckTime += time() - $time1 ;
	if ( (time () - $time1) > $maxSegmentsCheckTime ) {
		$maxSegmentsCheckTime = time () - $time1 ;
		print "max segment check now $maxSegmentsCheckTime secs\n" ;
	}

	return (scalar (keys %wayStart), $openSegments, @openEnds) ;
}

sub removeElement {
	my ($element, @array) = @_ ;
	my @arrayNew = () ;
	my $pos = -1 ; my $i ;
	for ($i=0; $i<=$#array; $i++) { if ($array[$i] == $element) { $pos = $i ; } }
	if ($pos != -1) {
		if ($pos == 0) {
			@arrayNew = @array[1..$#array] ;
		}
		if ($pos == $#array) {
			@arrayNew = @array[0..$#array-1] ;
		}
		if ( ($pos > 0) and ($pos < $#array) ) {
			@arrayNew = @array[0..$pos-1, $pos+1..$#array] ;
		}
	}
	return @arrayNew ;
}
