# 
#
# checkoverlay.pl by gary68
#
#
#
# Copyright (C) 2010, Gerhard Schwanz
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
# Version 1.0
#
#

use strict ;
use warnings ;

use OSM::osm 5.1 ;
use File::stat;
use Time::localtime;


my $program = "checkoverlay.pl" ;
my $usage = $program . " file.osm out.htm" ;
my $version = "1.0" ;

my $wayId ; 
my $wayUser ; my @wayNodes ; my @wayTags ;
my $nodeId ; 
my $nodeUser ; my $nodeLat ; my $nodeLon ; my @nodeTags ;
my $aRef1 ; my $aRef2 ;

my %startNodes = () ;
my %endNodes = () ;
my @result = () ;
my %segments = () ;
my %neededNodes = () ;
my $count= 0 ;

my $time0 = time() ; my $time1 ; my $timeA ;

my $html ;
my $gpx ;
my $osmName ;
my $htmlName ;
my $gpxName ;

my %lon ; my %lat ;
my %motorwayNodes = () ;

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

$gpxName = $htmlName ;
$gpxName =~ s/htm/gpx/ ;

print "\n$program $version for file $osmName\n\n" ;




######################
# skip all nodes first
######################
openOsmFile ($osmName) ;
print "pass1: skipping nodes...\n" ;
skipNodes () ;


print "pass1...\n" ;
($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {	

	my $mw = 0 ;
	foreach my $tag (@wayTags) {
		if ( ($tag->[0] eq "highway") and (scalar @wayNodes > 1) and ($wayNodes[0] != $wayNodes[-1]) ) {
			for (my $i=0; $i<$#wayNodes; $i++) {
				my $start = $wayNodes[$i] ;
				my $end = $wayNodes[$i+1] ;
				$segments{$count} = [$start, $end, $wayId] ;
				push @{$startNodes{$start}}, $count ; 
				push @{$endNodes{$end}}, $count ; 
				$count++ ;
			}
		}
	} 


	# next way
	($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;


foreach my $segment (keys %segments) {
	my $start = $segments{$segment}->[0] ;
	my $end = $segments{$segment}->[1] ;
	my $way = $segments{$segment}->[2] ;
	if (scalar @{$startNodes{$start}} > 1) {
		my @here = @{$startNodes{$start}} ;
		foreach my $segHere (@here) { # segments starting here
			if ( ($segments{$segHere}->[1] == $end) and ($segments{$segHere}->[2] != $way) ) {
				push @result, [$way, $segments{$segHere}->[2], $start, $end] ;
				$neededNodes{$start} = 1 ;
				$neededNodes{$end} = 1 ;
				# print "$way, $segments{$segHere}->[2], $start, $end\n" ;
			} 
		}
		if (defined @{$startNodes{$end}}) {
			@here = @{$startNodes{$end}} ;
			foreach my $segHere (@here) {
				if ( ($segments{$segHere}->[1] == $start) and ($segments{$segHere}->[2] != $way) ) {
					push @result, [$way, $segments{$segHere}->[2], $start, $end] ;
					$neededNodes{$start} = 1 ;
					$neededNodes{$end} = 1 ;
					# print "$way, $segments{$segHere}->[2], $start, $end\n" ;
				} 
			}
		}
	}
}

# print scalar @result, " problems found\n" ;


######################
# get node information
######################
print "pass2: get node information...\n" ;

openOsmFile ($osmName) ;

($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
if ($nodeId != -1) {
	#@nodeTags = @$aRef1 ;
}

while ($nodeId != -1) {

	if (defined $neededNodes{$nodeId}) {
		$lon{$nodeId} = $nodeLon ;
		$lat{$nodeId} = $nodeLat ;
	}

	# next
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
	if ($nodeId != -1) {
		#@nodeTags = @$aRef1 ;
	}
}

closeOsmFile () ;






$time1 = time () ;


##################
# PRINT HTML INFOS
##################
open ($html, ">", $htmlName) || die ("Can't open html output file") ;
open ($gpx, ">", $gpxName) || die ("Can't open gpx output file") ;


printHTMLiFrameHeader ($html, "Overlay Check by Gary68") ;
printGPXHeader ($gpx) ;

print $html "<H1>Overlay Check by Gary68</H1>\n" ;
print $html "<p>Version ", $version, "</p>\n" ;
print $html "<H2>Statistics</H2>\n" ;
print $html "<p>", stringFileInfo ($osmName), "<br>\n" ;

print $html "<H2>Overlays found</H2>\n" ;
print $html "<p>At the given two ways share the exact same nodes." ;
print $html "<table border=\"1\">\n";
print $html "<tr>\n" ;
print $html "<th>Line</th>\n" ;
print $html "<th>way1 Id</th>\n" ;
print $html "<th>way2 Id</th>\n" ;
print $html "<th>Links</th>\n" ;
print $html "<th>JOSM Ways</th>\n" ;
print $html "<th>JOSM Nodes</th>\n" ;
print $html "<th>Pic</th>\n" ;
print $html "</tr>\n" ;

my $i = 0 ;

foreach my $res (@result) {

	my $lo = ($lon{$res->[2]} + $lon{$res->[3]}) / 2 ;
	my $la = ($lat{$res->[2]} + $lat{$res->[3]}) / 2 ;

	$i++ ;
	# HTML
	print $html "<tr>\n" ;
	print $html "<td>", $i , "</td>\n" ;
	print $html "<td>", historyLink ("way", $res->[0]), "</td>\n" ;
	print $html "<td>", historyLink ("way", $res->[1]), "</td>\n" ;
	print $html "<td>", osmLink ($lo, $la, 16) , "<br>\n" ;
	print $html osbLink ($lo, $la, 16) , "<br>\n" ;
	print $html mapCompareLink ($lo, $la, 16) , "</td>\n" ;
	print $html "<td>", josmLinkSelectWays ($lo, $la, 0.002, $res->[0], $res->[1]), "</td>\n" ;
	print $html "<td>", josmLinkSelectNodes ($lo, $la, 0.002, $res->[2], $res->[3]), "</td>\n" ;
	print $html "<td>", picLinkOsmarender ($lo, $la, 16), "</td>\n" ;
	print $html "</tr>\n" ;

	# GPX
	my $text = "ChkOverlay - " . $res->[0] . " / " . $res->[1]  . " ways do overlay" ;
	printGPXWaypoint ($gpx, $lo, $la, $text) ;
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


print "\n$program finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;

print "$i lines total\n\n" ;


