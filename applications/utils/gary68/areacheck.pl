#
# areacheck by gary68
#
#
#
#
# Copyright (C) 2008, 2009, Gerhard Schwanz
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
# 1.0 B 003
# - added lots of tags
#
# 1.0 B 004
#
# 1.0 B 005
# - hash get node info
# - additional gpx output
#
# 1.1
# - add more links to HTML
#
# 3.0
# - online api support
#


use strict ;
use warnings ;

use OSM::osm 4.9 ;

my @areas = qw (area:yes waterway:riverbank aeroway:terminal aeroway:apron man_made:surveillance building:yes leisure:park leisure:playground 
	amenity:bus_station amenity:college 
	amenity:ferry_terminal amenity:hospital amenity:parking amenity:school amenity:university tourism:attraction tourism:zoo tourism:museum
	landuse:forest landuse:residential landuse:industrial landuse:cemetery natural:glacier natural:wood natural:water ) ;

#my @areas = qw (area:yes waterway:riverbank waterway:dock railway:turntable landuse:railway aeroway:terminal aeroway:apron 
#	aerialway:station power:station power:sub_station man_made:reservoir_covered man_made:surveillance 
#	man_made:wastewater_plant man_made:watermill man_made:water_works building:yes 
#	leisure:golf_course leisure:sports_center leisure:stadium leisure:track leisure:pitch leisure:water_park leisure:marina
#	leisure:fishing leisure:nature_reserve leisure:park leisure:playground leisure:garden leisure:common 
#	amenity:bicycle_parking amenity:bus_station amenity:car_rental amenity:car_sharing amenity:college 
#	amenity:ferry_terminal amenity:fountain amenity:hospital amenity:kindergarten amenity:parking amenity:place_of_worship
#	amenity:public_building amenity:school amenity:taxi amenity:townhall amenity:university amenity:verterinary
#	shop:kiosk shop:supermarket tourism:chalet tourism:camp_site tourism:caravan_site tourism:picnic_site tourism:theme_park
#	tourism:attraction tourism:zoo tourism:museum historic:archeological_site historic:ruins historic:battlefield historic:wreck
#	landuse:farm landuse:farm_yard landuse:quarry landuse:landfill landuse:basin landuse:reservoir landuse:forest
#	landuse:allotments landuse:residential landuse:retail landuse:commercial landuse:industrial landuse:brownfield landuse:greenfield
#	landuse:railway landuse:construction landuse:military landuse:cemetery landuse:meadow landuse:village_green 
#	landuse:recreation_ground military:airfield military:barracks military:danger_area military:range military:naval_base
#	natural:glacier natural:scree natural:scrub natural:fell natural:heath natural:wood natural:marsh natural:wetland
#	natural:water natural:mud natural:beach natural:bay natural:land natural:cave_entrance 
#	boundary:administrative boundary:civil boundary:political boundary:national_park
#	place:region place:county place:city place:town place:village place:hamlet place:suburb place:locality place:island) ;


my $program = "areacheck.pl" ;
my $version = "3.0" ;
my $usage = $program . " file.osm out.htm out.gpx" ;

my $wayId ;
my $wayId2 ;
my $wayUser ;
my @wayNodes ;
my @wayTags ;
my $nodeId ;
my $nodeId2 ;
my $nodeUser ;
my $nodeLat ;
my $nodeLon ;
my @nodeTags ;
my $aRef1 ;
my $aRef2 ;
my $wayCount = 0 ;
my $areaCount = 0 ;
my $areaOpenCount = 0 ;

my $time0 = time() ; my $time1 ;
my $i ;
my $key ;
my $num ;
my $tag1 ; my $tag2 ;

my $APIcount = 0 ;
my $APIerrors = 0 ;
my $APIrejected = 0 ;

my $html ;
my $gpx ;
my $osmName ;
my $htmlName ;
my $gpxName ;


my @open ;
my @neededNodes ;
my %neededNodesHash ;
my %lon ;
my %lat ;
my %wayStart ;
my %wayEnd ;
my %openWayTags ;
my %openWayNodes ;


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

$gpxName = shift||'';
if (!$gpxName)
{
	die (print $usage, "\n");
}

print "\n$program $version for file $osmName\n\n" ;
foreach (@areas) {
	print $_, " " ;
}
print "\n\n" ;





######################
# skip all nodes first
######################
openOsmFile ($osmName) ;
print "INFO: pass1: skipping nodes...\n" ;
skipNodes () ;


#####################
# identify open areas
#####################
print "INFO: pass1: find open areas...\n" ;
($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {	
	$wayCount++ ;

	my $found = 0 ;
	foreach $tag1 (@wayTags) {
		foreach $tag2 (@areas) {
			if ($tag1 eq $tag2) { $found = 1 ; }
		}
	}

	if ($found) { 
		$areaCount++ ;
		if ($wayNodes[0] != $wayNodes[-1]) {
			# check API way data
			#print "request API data for way $wayId...\n" ;
			sleep (1) ; # don't stress API
			$APIcount++ ;
			my ($id, $u, @nds, @tags, $ndsRef, $tagRef) ;
			($id, $u, $ndsRef, $tagRef) = APIgetWay ($wayId) ;
			#print "API request finished.\n" ;
			@nds = @$ndsRef ; 
			@tags = @$tagRef ;

			if ($id == 0) { $APIerrors++ ; }

			#print "nodes: @nds\n" ;
			#print "WayId=$wayId Id=$id wayNodes:", scalar (@wayNodes), " nds:", scalar (@nds), "\n" ;

			if ( ( scalar (@wayNodes) != scalar (@nds)) and ($wayId == $id) ) { 
				print "WARNING: way $wayId has different node count in API call. Ignoring...\n" ;
				$APIrejected++ ;
			}
			else {
				$areaOpenCount ++ ;
				push @open, $wayId ;
				$wayStart{$wayId} = $wayNodes[0] ; 
				$wayEnd{$wayId} = $wayNodes[-1] ; 
				@{$openWayTags{$wayId}} = @wayTags ;
				@{$openWayNodes{$wayId}} = @wayNodes ;
			}
		}
	}

	# next way
	($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;

print "INFO: number total ways: $wayCount\n" ;
print "INFO: number areas: $areaCount\n" ;
print "INFO: number open areas: $areaOpenCount\n" ;
print "INFO: API calls: $APIcount\n" ;
print "INFO: API errors: $APIerrors\n" ;
print "INFO: API rejections: $APIrejected\n" ;



######################
# collect needed nodes
######################
print "INFO: pass2: collect needed nodes...\n" ;
foreach $wayId (@open) {
	push @neededNodes, $wayStart{$wayId} ;
	push @neededNodes, $wayEnd{$wayId} ;
}


######################
# get node information
######################
print "INFO: pass2: get node information...\n" ;
openOsmFile ($osmName) ;

foreach (@neededNodes) { $neededNodesHash{$_} = 1 ; }

($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
if ($nodeId != -1) {
	#@nodeTags = @$aRef1 ;
}

while ($nodeId != -1) {

	if (exists ($neededNodesHash{$nodeId}) ) { 
		$lon{$nodeId} = $nodeLon ; $lat{$nodeId} = $nodeLat
	}

	# next
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
	if ($nodeId != -1) {
		#@nodeTags = @$aRef1 ;
	}
}

closeOsmFile () ;

$time1 = time () ;


######################
# PRINT HTML/GPX INFOS
######################
print "\nINFO: write HTML tables...\n" ;

open ($html, ">", $htmlName) || die ("Can't open html output file") ;
open ($gpx, ">", $gpxName) || die ("Can't open gpx output file") ;

printHTMLHeader ($html, "$program by Gary68") ;
printGPXHeader ($gpx) ;

print $html "<H1>$program by Gary68</H1>\n" ;
print $html "<p>Version ", $version, "</p>\n" ;

print $html "<p>Check ways with following tags:</p>\n" ;
print $html "<p>" ;
foreach (@areas) {
	print $html $_, " " ;
}
print $html "</p>" ;



print $html "<H2>Statistics</H2>\n" ;
print $html "<p>", stringFileInfo ($osmName), "<br>\n" ;
print $html "number ways total: $wayCount<br>\n" ;
print $html "number areas: $areaCount</p>\n" ;
print $html "number open areas: $areaOpenCount</p>\n" ;
print $html "number API errors: $APIerrors</p>\n" ;


print $html "<H2>Open Areas</H2>\n" ;
print $html "<p>These ways have to be closed areas according to map features but the first node is not the same as the last. So area is probably not closed or not properly so. It is possible that parts of the way are drawn doubly (thus closing the area in a way).</p>" ;
print $html "<table border=\"1\" width=\"100%\">\n";
print $html "<tr>\n" ;
print $html "<th>Line</th>\n" ;
print $html "<th>WayId</th>\n" ;
print $html "<th>Tags</th>\n" ;
print $html "<th>Nodes</th>\n" ;
print $html "<th>Distance start/end</th>\n" ;
print $html "<th>start/end node id</th>\n" ;
print $html "<th>OSM start/end</th>\n" ;
print $html "<th>OSB start/end</th>\n" ;
print $html "<th>JOSM start/end</th>\n" ;
print $html "</tr>\n" ;
$i = 0 ;
foreach $wayId (@open) {
	$i++ ;

	print $html "<tr>\n" ;
	print $html "<td>", $i , "</td>\n" ;
	print $html "<td>", historyLink ("way", $wayId) , "</td>\n" ;

	print $html "<td>" ;
	foreach (@{$openWayTags{$wayId}}) { print $html $_, " - " ; }
	print $html "</td>\n" ;

	print $html "<td>" ;
	foreach (@{$openWayNodes{$wayId}}) { print $html $_, " - " ; }
	print $html "</td>\n" ;

	my $dist = distance ($lon{$wayStart{$wayId}},$lat{$wayStart{$wayId}},$lon{$wayEnd{$wayId}},$lat{$wayEnd{$wayId}}) * 1000 ;
	printf $html "<td>%.0f m</td>\n", $dist ;

	print $html "<td>", $wayStart{$wayId}, " / ", $wayEnd{$wayId}, "</td>\n" ;
	print $html "<td>", osmLink ($lon{$wayStart{$wayId}}, $lat{$wayStart{$wayId}}, 16) , " ", osmLink ($lon{$wayEnd{$wayId}}, $lat{$wayEnd{$wayId}}, 16), "</td>\n" ;
	print $html "<td>", osbLink ($lon{$wayStart{$wayId}}, $lat{$wayStart{$wayId}}, 16) , " ", osbLink ($lon{$wayEnd{$wayId}}, $lat{$wayEnd{$wayId}}, 16), "</td>\n" ;
	print $html "<td>", josmLink ($lon{$wayStart{$wayId}}, $lat{$wayStart{$wayId}}, 0.01, $wayId), " ", josmLink ($lon{$wayEnd{$wayId}}, $lat{$wayEnd{$wayId}}, 0.01, $wayId), "</td>\n" ;

	print $html "</tr>\n" ;

	# GPX
	my $text = $wayId . " - area way not closed or doubly drawn segments" ;
	printGPXWaypoint ($gpx, $lon{$wayStart{$wayId}}, $lat{$wayStart{$wayId}}, $text) ;
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

print "\nINFO: finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;


