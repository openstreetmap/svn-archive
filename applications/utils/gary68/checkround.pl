#
# roundabout check by gary68
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
use File::stat;
use Time::localtime;



my $program = "checkround.pl" ;
my $version = "1.0" ;
my $usage = $program . " <direction> file.osm out.htm out.gpx // direction = [L|R]" ;

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
my $osmName ;
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
print "\n\n" ;





######################
# skip all nodes first
######################
openOsmFile ($osmName) ;
print "INFO: pass1: skipping nodes...\n" ;
skipNodes () ;


######################
# identify roundabouts
######################
print "INFO: pass1: find roundabouts...\n" ;
($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {	
	$wayCount++ ;

	my $found = 0 ;
	my $reverse = 0 ;
	foreach $tag1 (@wayTags) {
		if ($tag1 eq "junction:roundabout") { $found = 1 ; }
		if ($tag1 eq "highway:mini_roundabout") { $found = 1 ; }
		if ($tag1 eq "oneway-1") { $reverse = 1 ; }
	}

	if ($found and (scalar @wayNodes > 2) ) { 
		$roundaboutCount++ ;
		push @roundabouts, $wayId ;
		$wayStart{$wayId} = $wayNodes[0] ; 
		$wayEnd{$wayId} = $wayNodes[-1] ; 
		@{$roundaboutTags{$wayId}} = @wayTags ;
		if ($reverse) { @wayNodes = reverse @wayNodes ; }
		@{$roundaboutNodes{$wayId}} = @wayNodes ;
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
print "INFO: number roundabouts: $roundaboutCount\n" ;



######################
# collect needed nodes
######################
print "INFO: pass2: collect needed nodes...\n" ;
foreach $wayId (@roundabouts) {
	push @neededNodes, @{$roundaboutNodes{$wayId}} ;
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

foreach $wayId (@roundabouts) {

		# angle (x1,y1,x2,y2)					> angle (N=0,E=90...)
		my $node0 = @{$roundaboutNodes{$wayId}}[0] ;
		my $node1 = @{$roundaboutNodes{$wayId}}[1] ; 
		my $node2 = @{$roundaboutNodes{$wayId}}[2] ; 
		
		my $angle1 = angle ($lon{$node0}, $lat{$node0}, $lon{$node1}, $lat{$node1}) ; 
		my $angle2 = angle ($lon{$node1}, $lat{$node1}, $lon{$node2}, $lat{$node2}) ; 
		my $angleDelta = $angle2 - $angle1 ;
		# print "$wayId $angle1 $angle2 $angleDelta\n" ;
		if ($angleDelta > 180) { $angleDelta = $angleDelta - 360 ; }
		if ($angleDelta < -180) { $angleDelta = $angleDelta + 360 ; }
		if ( 	( ($direction eq "L") and ($angleDelta > 0) ) or
			( ($direction eq "R") and ($angleDelta < 0)) ) {
			$onewayWrongCount ++ ;
			push @wrong, $wayId ;
		}
}
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
print $html "<p>", stringFileInfo ($osmName), "<br>\n" ;
print $html "number ways total: $wayCount<br>\n" ;
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

statistics ( ctime(stat($osmName)->mtime),  $program,  "roundabout", $osmName,  $roundaboutCount,  $i) ;

print "\nINFO: finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;

sub statistics {
	my ($date, $program, $def, $area, $total, $errors) = @_ ;
	my $statfile ; my ($statfileName) = "statistics.csv" ;

	if (grep /\.bz2/, $area) { $area =~ s/\.bz2// ; }
	if (grep /\.osm/, $area) { $area =~ s/\.osm// ; }
	my ($area2) = ($area =~ /.+\/([\w\-]+)$/ ) ;

	my ($def2) = "roundabouts" ;

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
