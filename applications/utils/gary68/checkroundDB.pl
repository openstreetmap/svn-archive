#
# roundaboutDB check by gary68
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
# 1.0
#



use strict ;
use warnings ;

use OSM::osm ;
use OSM::osmDB ;
use File::stat;
use Time::localtime;



my $program = "checkroundDB.pl" ;
my $version = "1.0" ;
my $usage = $program . " <direction> database out.htm out.gpx // direction = [L|R]" ;

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
my $roundaboutCount = 0 ;
my $onewayCount = 0 ;
my $onewayWrongCount = 0 ;

my $time0 = time() ; my $time1 ;
my $i ;
my $key ;
my $num ;
my $tag1 ; my $tag2 ;
my $direction = "" ;

my $html ;
my $gpx ;
my $dbName ;
my $htmlName ;
my $gpxName ;


my @roundabouts ;
my @wrong ;
my @neededNodes ;
my %neededNodesHash ;
my %lon ;
my %lat ;
my %wayStart ;
my %wayEnd ;
my %roundaboutTags ;
my %roundaboutNodes ;


###############
# get parameter
###############
$direction = shift||'';
if (!$direction)
{
	$direction = "L" ;
}

$dbName = shift||'';
if (!$dbName)
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

print "\n$program $version for DB $dbName\n\n" ;
print "\n\n" ;




dbConnect ($dbName) ;


my @checks = (["junction","roundabout"],["highway","mini_roundabout"]) ;

foreach my $c (@checks) {

	loopInitWays($c->[0], $c->[1]) ;
	$wayId = loopGetNextWay() ;
	while (defined $wayId) {

		my ($ref0, $ref1, $ref2) = getDBWay ($wayId) ;
		my %wayProperties = %$ref0 ;
		my @wayNodes = @$ref1 ;
		my @wayTags = @$ref2 ;

		my $reverse = 0 ;
		foreach my $tag (@wayTags) {
			if ( ($tag->[0] eq "oneway") and ($tag->[1] eq "-1") ) { $reverse = 1 ; }
		}

		if (scalar @wayNodes > 2) { 
			$roundaboutCount++ ;

			if ($reverse) { @wayNodes = reverse @wayNodes ; }
			my $wayStart = $wayNodes[0] ; 
			my $wayEnd = $wayNodes[-1] ; 

			my %lonLocal = () ;
			my %latLocal = () ;

			foreach my $n (@wayNodes) {
				my ($ref0, $ref1) = getDBNode ($n) ;
				my %properties = %$ref0 ;
				$lonLocal{$n} = $properties{"lon"} ;
				$latLocal{$n} = $properties{"lat"} ;
			}


			# angle (x1,y1,x2,y2)					> angle (N=0,E=90...)
			my $node0 = $wayNodes[0] ;
			my $node1 = $wayNodes[1] ;
			my $node2 = $wayNodes[2] ;
		
			my $angle1 = angle ($lonLocal{$node0}, $latLocal{$node0}, $lonLocal{$node1}, $latLocal{$node1}) ; 
			my $angle2 = angle ($lonLocal{$node1}, $latLocal{$node1}, $lonLocal{$node2}, $latLocal{$node2}) ; 
			my $angleDelta = $angle2 - $angle1 ;
			# print "$wayId $angle1 $angle2 $angleDelta\n" ;
			if ($angleDelta > 180) { $angleDelta = $angleDelta - 360 ; }
			if ($angleDelta < -180) { $angleDelta = $angleDelta + 360 ; }
			if ( 	( ($direction eq "L") and ($angleDelta > 0) ) or
				( ($direction eq "R") and ($angleDelta < 0)) ) {
				$onewayWrongCount ++ ;
				push @wrong, $wayId ;

				@{$roundaboutTags{$wayId}} = @wayTags ;
				@{$roundaboutNodes{$wayId}} = @wayNodes ;
				$lon{$node0} = $lonLocal{$node0} ;
				$lat{$node0} = $latLocal{$node0} ;
				$wayStart{$wayId} = $node0 ;
			}
		} # if valid way

		$wayId = loopGetNextWay() ;
	} # while

} #foreach


dbDisconnect () ;



print "INFO: number roundabouts: $roundaboutCount\n" ;
print "INFO: number wrong roundabouts: $onewayWrongCount\n" ;

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

print $html "<H2>Statistics</H2>\n" ;
print $html "number roundabouts: $roundaboutCount</p>\n" ;
print $html "number wrong roundabouts: $onewayWrongCount</p>\n" ;


print $html "<H2>Wrong roundabouts</H2>\n" ;
print $html "<p>These roundabouts have the wrong direction.</p>" ;
print $html "<table border=\"1\" width=\"100%\">\n";
print $html "<tr>\n" ;
print $html "<th>Line</th>\n" ;
print $html "<th>WayId</th>\n" ;
print $html "<th>Tags</th>\n" ;
print $html "<th>Nodes</th>\n" ;
print $html "<th>OSM start</th>\n" ;
print $html "<th>OSB start</th>\n" ;
print $html "<th>JOSM start</th>\n" ;
print $html "</tr>\n" ;
$i = 0 ;
foreach $wayId (@wrong) {
	$i++ ;

	print $html "<tr>\n" ;
	print $html "<td>", $i , "</td>\n" ;
	print $html "<td>", historyLink ("way", $wayId) , "</td>\n" ;

	print $html "<td>" ;
	foreach (@{$roundaboutTags{$wayId}}) { print $html $_, " - " ; }
	print $html "</td>\n" ;

	print $html "<td>" ;
	foreach (@{$roundaboutNodes{$wayId}}) { print $html $_, " - " ; }
	print $html "</td>\n" ;

	print $html "<td>", osmLink ($lon{$wayStart{$wayId}}, $lat{$wayStart{$wayId}}, 16) , "</td>\n" ;
	print $html "<td>", osbLink ($lon{$wayStart{$wayId}}, $lat{$wayStart{$wayId}}, 16) , "</td>\n" ;
	print $html "<td>", josmLink ($lon{$wayStart{$wayId}}, $lat{$wayStart{$wayId}}, 0.005, $wayId), "</td>\n" ;

	print $html "</tr>\n" ;

	# GPX
	my $text = $wayId . " - roundabout with wrong direction" ;
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

$time1 = time () ;

print "\nINFO: finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;

