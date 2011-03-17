# 
#
# checklayer.pl by gary68
#
#
#
# Copyright (C) 2011, Gerhard Schwanz
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


my $program = "checklayer.pl" ;
my $usage = $program . " file.osm out.htm" ;
my $version = "1.0" ;
my @types = qw (motorway motorway_link trunk trunk_link primary primary_link secondary secondary_link tertiary road unclassified residential) ;

my $wayId ; 
my $wayUser ; my @wayNodes ; my @wayTags ;
my $nodeId ; 
my $nodeUser ; my $nodeLat ; my $nodeLon ; my @nodeTags ;
my $aRef1 ; my $aRef2 ;

my %layers = () ;
my %problems = () ;

my $time0 = time() ; my $time1 ; my $timeA ;

my $html ;
my $gpx ;
my $osmName ;
my $htmlName ;
my $gpxName ;

my %lon ; my %lat ;

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


print "pass1: reading ways...\n" ;
($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {	

	my $use = 0 ;
	my $layer = 0 ;
	foreach my $tag (@wayTags) {
		if ($tag->[0] eq "highway") {
			foreach my $type (@types) {
				if ($type eq $tag->[1]) {
					$use = 1 ;
				}
			}
		}
		if ($tag->[0] eq "layer") {
			$layer = $tag->[1] ;
		}
	} 

	if ($layer =~ /^([+-]?\d+)$/) {
	}
	else {
		print "way $wayId: layer $layer not numeric. will be set to 0.\n" ;
		$layer = 0 ;
	}

	if ( ($use) and (scalar @wayNodes > 2) ) {
		for (my $i = 1; $i < $#wayNodes; $i++) {
			if (defined $layers { $wayNodes[$i] } ) {
				if ( $layers { $wayNodes[$i] } != $layer) {
					$problems { $wayNodes[$i] } = 1 ;
				}
			}
			else {
				$layers { $wayNodes[$i] } = $layer ;
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


print scalar keys %problems, " problems found.\n" ;

######################
# get node information
######################

if (scalar keys %problems > 0) {
	print "pass2: get node information...\n" ;
	openOsmFile ($osmName) ;

	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
	if ($nodeId != -1) {
		#@nodeTags = @$aRef1 ;
	}

	while ($nodeId != -1) {

		if (defined $problems{$nodeId}) {
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

}




$time1 = time () ;


##################
# PRINT HTML INFOS
##################
open ($html, ">", $htmlName) || die ("Can't open html output file") ;
open ($gpx, ">", $gpxName) || die ("Can't open gpx output file") ;


printHTMLiFrameHeader ($html, "Layer Check by Gary68") ;
printGPXHeader ($gpx) ;

print $html "<H1>Layer Check by Gary68</H1>\n" ;
print $html "<p>Version ", $version, "</p>\n" ;
print $html "<H2>Statistics</H2>\n" ;
print $html "<p>", stringFileInfo ($osmName), "<br>\n" ;

print $html "<H2>Layer problems found</H2>\n" ;
print $html "<p>At the given location two ways (with different layer value) do intersect WITH a common node." ;
print $html "<table border=\"1\">\n";
print $html "<tr>\n" ;
print $html "<th>Line</th>\n" ;
print $html "<th>NodeId</th>\n" ;
print $html "<th>Links</th>\n" ;
print $html "<th>JOSM</th>\n" ;
print $html "<th>Pic</th>\n" ;
print $html "</tr>\n" ;

my $i = 0 ;

foreach my $node (keys %problems) {
	$i++ ;
	# HTML
	print $html "<tr>\n" ;
	print $html "<td>", $i , "</td>\n" ;
	print $html "<td>", historyLink ("node", $node), "</td>\n" ;
	print $html "<td>", osmLink ($lon{$node}, $lat{$node}, 16) , "<br>\n" ;
	print $html osbLink ($lon{$node}, $lat{$node}, 16) , "<br>\n" ;
	print $html mapCompareLink ($lon{$node}, $lat{$node}, 16) , "</td>\n" ;
	print $html "<td>", josmLinkSelectNode ($lon{$node}, $lat{$node}, 0.01, $node), "</td>\n" ;
	print $html "<td>", picLinkOsmarender ($lon{$node}, $lat{$node}, 16), "</td>\n" ;
	print $html "</tr>\n" ;

	# GPX
	my $text = "Layer Check - " . $node  . " ways w. different layer share same node" ;
	printGPXWaypoint ($gpx, $lon{$node}, $lat{$node}, $text) ;
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


