# 
#
# checkcross.pl by gary68
#
# this program checks an osm file for crossing ways which don't share a common node at the intersection and are on the same layer
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
# Version 1.0
#
# Version 1.1
# - don't consider short oneways (false positives in large intersections)
#
# Version 1.2
# - stat
#
# Version 1.3
# - stat 2
#
# Version 1.4
# - select both ways in josm
#
# Version 1.5
# - get bugs implemented
#
# Version 1.6
# - get bugs NEW OSB additionally implemented
# - gpx file with open bugs for germany can be obtained here: http://openstreetbugs.schokokeks.org/api/0.1/getGPX?b=47.4&t=55.0&l=5.9&r=15.0&limit=100000&open=yes
# - added map compare link
#

use strict ;
use warnings ;

use List::Util qw[min max] ;
use OSM::osm 4.1 ;
use File::stat;
use Time::localtime;
use LWP::Simple;

my $program = "checkcross.pl" ;
my $usage = $program . " [N|B] def.xml file.osm out.htm out.gpx (mode N = normal or B = also get openstreetbugs" ;
my $version = "1.6 BETA (004)" ;
my $mode = "N" ;

my $gpxFileName = "../../web/osm/qa/bugs/OpenStreetBugsOpen.gpx" ;
my $threshold = 0.005 ; # in degrees, ~500m
my $bugsMaxDist = 0.05 ; # in km
my $bugsDownDist = 0.02 ; # in deg
my $minLength = 100 ; # min length of way to be considered in result list (in meters)

my (%gpxLon, %gpxLat, %gpxId, %gpxClosed, %gpxDesc) ;



my $wayId ; my $wayId1 ; my $wayId2 ;
my $wayUser ; my @wayNodes ; my @wayTags ;
my $nodeId ; my $nodeId2 ;
my $nodeUser ; my $nodeLat ; my $nodeLon ; my @nodeTags ;
my $aRef1 ; my $aRef2 ;
my $wayCount = 0 ;
my $againstCount = 0 ;
my $checkWayCount = 0 ;
my $againstWayCount = 0 ;
my $invalidWays ;

my @check ;
my @against ;
my @checkWays ;
my @againstWays ;

my $time0 = time() ; my $time1 ; my $timeA ;
my $i ;
my $key ;
my $num ;
my $tag1 ; my $tag2 ;
my $progress ;
my $potential ;
my $checksDone ;

my $html ;
my $def ;
my $gpx ;
my $osmName ;
my $htmlName ;
my $defName ;
my $gpxName ;

my %wayNodesHash ;
my @neededNodes ;
my %lon ; my %lat ;
my %xMax ; my %xMin ; my %yMax ; my %yMin ; 
my %layer ;
my %wayCategory ;
my %wayHash ;
my %length ;
my %oneway ;

my $crossings = 0 ;
my %crossingsHash ;

###############
# get parameter
###############
$mode = shift||'';
if (!$mode)
{
	$mode = "N" ;
}

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
	$gpxName = $htmlName ;
	$gpxName =~ s/htm/gpx/ ;
}

print "\n$program $version for file $osmName\n\n" ;


##################
# read definitions
##################

print "read definitions file $defName...\n" ;
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




print "Check ways: " ;
foreach (@check) { print $_, " " ;} print "\n" ;
print "Against: " ;
foreach (@against) { print $_, " " ;} print "\n\n" ;


if ($mode eq "B") {
	readGPXFile ($gpxFileName) ;
}


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
		my $layerTemp = "0" ; my $onewayTemp = 0 ;
		# check tags ONLY ONCE
		foreach $tag1 (@wayTags) {
			if (grep (/layer/, $tag1)) { $layerTemp = $tag1 ; $layerTemp =~ s/layer:// ; }
			if ( ($tag1 eq "oneway:yes") or ($tag1 eq "oneway:1") or ($tag1 eq "oneway:-1") or ($tag1 eq "oneway:true") ) {
				$onewayTemp = 1 ;
			}
			foreach $tag2 (@against) {
				if ($tag1 eq $tag2) { $found = 1 ; }
			}
		}
		if ($found) {
			$againstWayCount++ ;
			push @againstWays, $wayId ;
			@{$wayNodesHash{$wayId}} = @wayNodes ;
			push @neededNodes, @wayNodes ;
			$layer{$wayId} = $layerTemp ;
			$oneway{$wayId} = $onewayTemp ;
			$wayCategory{$wayId} = 2 ;
		}

		$found = 0 ; 
		foreach $tag1 (@wayTags) {
			# if (grep (/layer/, $tag1)) { $layerTemp = $tag1 ; }
			foreach $tag2 (@check) {
				if ($tag1 eq $tag2) { $found = 1 ; }
			}
		}
		if ($found)  { 
			push @checkWays, $wayId ; 
			$checkWayCount++ ;
			@{$wayNodesHash{$wayId}} = @wayNodes ;
			push @neededNodes, @wayNodes ;
			$layer{$wayId} = $layerTemp ;
			$oneway{$wayId} = $onewayTemp ;
			$wayCategory{$wayId} = 1 ;
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

#$" = " " ;
#print "Cat1 ways: @cat1\n" ;
#print "Cat1 nodes: @allCat1Nodes\n" ;
#print "All way nodes: @allWayNodes\n" ;




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

	if ($needed >= 0) { $lon{$nodeId} = $nodeLon ; $lat{$nodeId} = $nodeLat ; }

	# next
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
	if ($nodeId != -1) {
		#@nodeTags = @$aRef1 ;
	}
}

closeOsmFile () ;


##############
# calc lengths
##############

my $lengthTemp = 0 ;
foreach $wayId (@checkWays) {
	$lengthTemp = 0 ;
	for ($i = 0; $i < scalar (@{$wayNodesHash{$wayId}}) - 1 ; $i++) {
		$lengthTemp += distance ($lon{$wayNodesHash{$wayId}[$i]}, $lat{$wayNodesHash{$wayId}[$i]}, 
			$lon{$wayNodesHash{$wayId}[$i+1]}, $lat{$wayNodesHash{$wayId}[$i+1]}) ;
	}
	$length{$wayId} = $lengthTemp ;
}

$lengthTemp = 0 ;
foreach $wayId (@againstWays) {
	$lengthTemp = 0 ;
	for ($i = 0; $i < scalar (@{$wayNodesHash{$wayId}}) - 1 ; $i++) {
		$lengthTemp += distance ($lon{$wayNodesHash{$wayId}[$i]}, $lat{$wayNodesHash{$wayId}[$i]}, 
			$lon{$wayNodesHash{$wayId}[$i+1]}, $lat{$wayNodesHash{$wayId}[$i+1]}) ;
	}
	$length{$wayId} = $lengthTemp ;
}



##########################
# init areas for chechWays
##########################
print "init areas for checkways...\n" ;
foreach $wayId (@checkWays) {
	$xMax{$wayId} =  max ($lon{$wayNodesHash{$wayId}[0]}, $lon{$wayNodesHash{$wayId}[-1]}) + $threshold ;
	$xMin{$wayId} =  min ($lon{$wayNodesHash{$wayId}[0]}, $lon{$wayNodesHash{$wayId}[-1]}) - $threshold ;
	$yMax{$wayId} =  max ($lat{$wayNodesHash{$wayId}[0]}, $lat{$wayNodesHash{$wayId}[-1]}) + $threshold ;
	$yMin{$wayId} =  min ($lat{$wayNodesHash{$wayId}[0]}, $lat{$wayNodesHash{$wayId}[-1]}) - $threshold ;
}

###############
# init way hash
###############
foreach $wayId (@checkWays) {
	my $hashValue = hashValue ($lon{$wayNodesHash{$wayId}[0]}, $lat{$wayNodesHash{$wayId}[0]}) ;
	push (@{$wayHash {$hashValue}}, $wayId) ;
}


###############################
# check for crossings
###############################
print "check for crossings...\n" ;

$progress = 0 ;
$timeA = time() ;

push @againstWays, @checkWays ;
my $total = scalar (@againstWays) ;

$potential = $total * scalar (@checkWays) ;

foreach $wayId1 (@againstWays) {
	$progress++ ;
	if ( ($progress % 1000) == 0 ) {
		printProgress ($program, $osmName, $timeA, $total, $progress) ;
	}

	# create temp array according to hash
	my @temp = () ;
	my $lo ; my $la ;
	for ($lo=$lon{$wayNodesHash{$wayId1}[0]}-0.1; $lo<=$lon{$wayNodesHash{$wayId1}[0]}+0.1; $lo=$lo+0.1) {
		for ($la=$lat{$wayNodesHash{$wayId1}[0]}-0.1; $la<=$lat{$wayNodesHash{$wayId1}[0]}+0.1; $la=$la+0.1) {
			if ( defined @{$wayHash{hashValue($lo,$la)}} ) {
				push @temp, @{$wayHash{hashValue($lo,$la)}} ;
			}
		}
	}

	my $aXMax = max ($lon{$wayNodesHash{$wayId1}[0]}, $lon{$wayNodesHash{$wayId1}[-1]}) ;
	my $aXMin = min ($lon{$wayNodesHash{$wayId1}[0]}, $lon{$wayNodesHash{$wayId1}[-1]}) ;
	my $aYMax = max ($lat{$wayNodesHash{$wayId1}[0]}, $lat{$wayNodesHash{$wayId1}[-1]}) ;
	my $aYMin = min ($lat{$wayNodesHash{$wayId1}[0]}, $lat{$wayNodesHash{$wayId1}[-1]}) ;

	foreach $wayId2 (@temp) {
		if ( $layer{$wayId1} ne $layer{$wayId2} ) {
			# don't do anything, ways on different layer
		}
		else { # ways on same layer
			# check for overlapping "way areas"

			if (checkOverlap ($aXMin, $aYMin, $aXMax, $aYMax, $xMin{$wayId2}, $yMin{$wayId2}, $xMax{$wayId2}, $yMax{$wayId2})) {
				if ( ($wayCategory{$wayId1} == $wayCategory{$wayId2}) and ($wayId1 <= $wayId2) ) {
					# don't do anything because cat1/cat1 only if id1>id2
				}
				else {
					my $a ; my $b ;
					$checksDone++ ;
					for ($a=0; $a<$#{$wayNodesHash{$wayId1}}; $a++) {
						for ($b=0; $b<$#{$wayNodesHash{$wayId2}}; $b++) {
							my ($x, $y) = crossing ($lon{$wayNodesHash{$wayId1}[$a]}, 
									$lat{$wayNodesHash{$wayId1}[$a]}, 
									$lon{$wayNodesHash{$wayId1}[$a+1]}, 
									$lat{$wayNodesHash{$wayId1}[$a+1]}, 
									$lon{$wayNodesHash{$wayId2}[$b]}, 
									$lat{$wayNodesHash{$wayId2}[$b]}, 
									$lon{$wayNodesHash{$wayId2}[$b+1]}, 
									$lat{$wayNodesHash{$wayId2}[$b+1]}) ;
							if (($x != 0) and ($y != 0)) {
								$crossings++ ;
								@{$crossingsHash{$crossings}} = ($x, $y, $wayId1, $wayId2) ;
								#print "crossing: $x, $y, $wayId1, $wayId2\n" ;
							} # found
						} # for
					} # for
				} # categories
			} # overlap
		} 
	}
}

print "potential checks: $potential\n" ;
print "checks actually done: $checksDone\n" ;
my $percent = $checksDone / $potential * 100 ;
printf "work: %2.3f percent\n", $percent ;
print "crossings found: $crossings\n" ;

$time1 = time () ;


##################
# PRINT HTML INFOS
##################
print "\nwrite HTML tables and GPX file, get bugs if specified...\n" ;

open ($html, ">", $htmlName) || die ("Can't open html output file") ;
open ($gpx, ">", $gpxName) || die ("Can't open gpx output file") ;


printHTMLHeader ($html, "Crossings Check by Gary68") ;
printGPXHeader ($gpx) ;

print $html "<H1>Crossing Check by Gary68</H1>\n" ;
print $html "<p>Version ", $version, "</p>\n" ;
print $html "<p>Mode ", $mode, "</p>\n" ;
print $html "<H2>Statistics</H2>\n" ;
print $html "<p>", stringFileInfo ($osmName), "<br>\n" ;
print $html "number ways total: $wayCount<br>\n" ;
print $html "number invalid ways (1 node only): $invalidWays<br>\n" ;
print $html "number check ways: $checkWayCount<br>\n" ;
print $html "number against ways: $againstWayCount</p>\n" ;

print $html "<p>Check ways: " ;
foreach (@check) { print $html $_, " " ;} print $html "</p>\n" ;
print $html "<p>Against: " ;
foreach (@against) { print $html $_, " " ;} print $html "</p>\n" ;


print $html "<H2>Crossings found where layer is the same</H2>\n" ;
print $html "<p>At the given location two ways intersect without a common node and on the same layer." ;
print $html "<table border=\"1\">\n";
print $html "<tr>\n" ;
print $html "<th>Line</th>\n" ;
print $html "<th>WayId1</th>\n" ;
print $html "<th>WayId2</th>\n" ;
print $html "<th>Links</th>\n" ;
print $html "<th>JOSM</th>\n" ;
print $html "<th>Pic</th>\n" ;
print $html "<th>Bugs found</th>\n" ;
print $html "</tr>\n" ;
$i = 0 ;
foreach $key (keys %crossingsHash) {
	my ($x, $y, $id1, $id2) = @{$crossingsHash{$key}} ;

	my $len1 = int ( $length{$id1} * 1000) ;
	my $len2 = int ( $length{$id2} * 1000) ;

	if ( ( ($len1 < $minLength) and ($oneway{$id1} == 1) ) or 
		( ($len2 < $minLength) and ($oneway{$id2} == 1) ) ) {
		# do nothing
	}
	else {
		$i++ ;
		# HTML
		print $html "<tr>\n" ;
		print $html "<td>", $i , "</td>\n" ;
		print $html "<td>", historyLink ("way", $id1) , " (oneway=$oneway{$id1}; $len1 m)</td>\n" ;
		print $html "<td>", historyLink ("way", $id2) , " (oneway=$oneway{$id2}; $len2 m)</td>\n" ;
		print $html "<td>", osmLink ($x, $y, 16) , "<br>\n" ;
		print $html osbLink ($x, $y, 16) , "<br>\n" ;
		print $html mapCompareLink ($x, $y, 16) , "</td>\n" ;
		print $html "<td>", josmLinkSelectWays ($x, $y, 0.01, $id1, $id2), "</td>\n" ;
		print $html "<td>", picLinkOsmarender ($x, $y, 16), "</td>\n" ;
		if ($mode eq "B") {
			print "get bugs for line $i...\n" ;
			print $html "<td><h3>Old OSB</h3>", getBugs ($x, $y, $bugsDownDist, $bugsMaxDist), "<br>\n" ;
			print $html "<h3>NEW OSB</h3>", getGPXWaypoints ($x, $y, $bugsMaxDist), "</td>\n"
		}
		else {
			print $html "<td>bugs not enabled</td>\n" ;
		}
		print $html "</tr>\n" ;

		# GPX
		my $text = "ChkCross - " . $id1 . "/" . $id2 . " level way crossing without common node" ;
		printGPXWaypoint ($gpx, $x, $y, $text) ;
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



sub readGPXFile {
	my ($gpxFileName) = shift ;
	my $gpxFile ;
	my $line ;
	my $i = 0 ;
	my $o = 0 ;
	my $c = 0 ;

	open ($gpxFile, "<", $gpxFileName) or die ("can't open gpx file") ;
	$line = <$gpxFile> ;
	while ( ! (grep /<wpt /, $line) ) {
		$line = <$gpxFile> ;	
	}

	while (grep /<wpt /, $line) {
		$i++ ;	
		my ($desc)   = ($line =~ /^.*<desc>(.+)<\/desc>/);	
		my ($closed)   = ($line =~ /^.*<closed>(.+)<\/closed>/);	
		my ($name)   = ($line =~ /^.*<id>(.*)<\/id>/);	
		my ($lon) = ($line =~ /^.+lon=[\'\"]([-\d,\.]+)[\'\"]/) ;
		my ($lat) = ($line =~ /^.+lat=[\'\"]([-\d,\.]+)[\'\"]/) ;

		if (!(defined $closed)) { $closed = 0 ; } 		
		if (!(defined $name)) { $name = "noId" ; } 		

		$desc =~ s/<hr \/>/:::/g ;

		$gpxLon{$i} = $lon ;
		$gpxLat{$i} = $lat ;
		$gpxId{$i} = $name ;
		$gpxDesc{$i} = $desc ;
		$gpxClosed{$i} = $closed ;
		# print "$name, $closed, $lon, $lat, $desc\n" ;

		$line = <$gpxFile> ;	
	}
	close ($gpxFile) ;
	print "$i waypoints read from gpx file\n\n" ;
}


sub getGPXWaypoints {
	my ($lon, $lat, $bugsMaxDist) = @_ ;

	my $result = "<p>\n" ;

	my $key ;
	my $j = 0 ;
	foreach $key (keys %gpxLon) {
		my $dist = distance ($lon, $lat, $gpxLon{$key}, $gpxLat{$key}) ;
		if ( $dist < $bugsMaxDist ) {
			$j++ ;
			my ($d) = int (distance ($lon, $lat, $gpxLon{$key}, $gpxLat{$key}) * 1000)  ;
			$result = $result . "id=" . $gpxId{$key} . " dist=" . $d . "m " . $gpxClosed{$key} . " " . $gpxDesc{$key} . "<br>\n" ;
		}
	}
	# print "$j found inside distance\n" ;
	$result = $result . "</p>\n" ;
	return $result ;
}

