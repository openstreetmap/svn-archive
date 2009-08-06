#
# checkdouble by gary68
#
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
# 1.0
# - initial version
#
# 1.1
# - stat
#
# 1.2
# - stat 2
#





use strict ;
use warnings ;

use OSM::osm ;
use File::stat;
use Time::localtime;



my $program = "checkdouble.pl" ;
my $version = "1.2" ;
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
my $doubleCount = 0 ;
my $highwayCount = 0 ;

my $time0 = time() ; my $time1 ;
my $i ;
my $key ;
my $num ;
my $tag1 ; my $tag2 ;

my $html ;
my $gpx ;
my $osmName ;
my $htmlName ;
my $gpxName ;


my @double ;
my @neededNodes ;
my %neededNodesHash ;
my %lon ;
my %lat ;
my %wayStart ;
my %wayEnd ;
my %doubleWayTags ;
my %doubleWayNodes ;

my %sources ;

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





######################
# skip all nodes first
######################
openOsmFile ($osmName) ;
print "skipping nodes...\n" ;
skipNodes () ;


#######################
# identify double nodes
#######################
print "find double nodes in ways...\n" ;
($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {	
	my $sourceName = "unknown" ;
	$wayCount++ ;

	my $found = 0 ;
	foreach $tag1 (@wayTags) {
		if (grep /^highway:/, $tag1) { $found = 1 ; }
		if (grep /^created_by:/, $tag1) { $sourceName = $tag1 ; $sourceName =~ s/created_by:// ; }
	}

	if ($found) { 
		$highwayCount++ ;

		my $double = 0 ;
		if ($#wayNodes > 0) { 
			for ($i = 0; $i < $#wayNodes; $i++) {
				if ($wayNodes[$i] == $wayNodes[$i+1]) {
					$double = 1 ;
				}
			}
		}

		if ($double) {
			$doubleCount ++ ;
			push @double, $wayId ;
			$wayStart{$wayId} = $wayNodes[0] ; 
			$wayEnd{$wayId} = $wayNodes[-1] ; 
			@{$doubleWayTags{$wayId}} = @wayTags ;
			@{$doubleWayNodes{$wayId}} = @wayNodes ;
			$sources{$sourceName} += 1 ;
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

print "number total ways: $wayCount\n" ;
print "number highways: $highwayCount\n" ;
print "number ways with double nodes: $doubleCount\n" ;
print "sources:\n" ;
foreach (sort keys %sources) { print $_, " ", $sources{$_}, "\n" ; }


######################
# collect needed nodes
######################
print "collect needed nodes...\n" ;
foreach $wayId (@double) {
	push @neededNodes, $wayStart{$wayId} ;
	push @neededNodes, $wayEnd{$wayId} ;
}


######################
# get node information
######################
print "get node information...\n" ;
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
print "\nwrite HTML tables and gpx data...\n" ;

open ($html, ">", $htmlName) || die ("Can't open html output file") ;
open ($gpx, ">", $gpxName) || die ("Can't open gpx output file") ;

printHTMLHeader ($html, "$program by Gary68") ;
printGPXHeader ($gpx) ;

print $html "<H1>$program by Gary68</H1>\n" ;
print $html "<p>Version ", $version, "</p>\n" ;

print $html "<p>Check highways for double nodes</p>\n" ;


print $html "<H2>Statistics</H2>\n" ;
print $html "<p>", stringFileInfo ($osmName), "<br>\n" ;
print $html "number ways total: $wayCount<br>\n" ;
print $html "number highways: $highwayCount</p>\n" ;
print $html "number ways with double nodes: $doubleCount</p>\n" ;
print $html "<h3>Sources</h3>\n<p>" ;
print $html "<table border=\"1\">\n";
foreach (sort keys %sources) { print $html "<tr><td>$_</td><td align=\"right\">$sources{$_}</td></tr>\n" ; }
print $html "</table>" ;


print $html "<H2>Ways with double nodes</H2>\n" ;
print $html "<p>These ways have (at least) two nodes with the same ID following each other.</p>" ;
print $html "<table border=\"1\" width=\"100%\">\n";
print $html "<tr>\n" ;
print $html "<th>Line</th>\n" ;
print $html "<th>WayId</th>\n" ;
print $html "<th>Tags</th>\n" ;
print $html "<th>Nodes</th>\n" ;
print $html "<th>OSM</th>\n" ;
print $html "<th>OSB</th>\n" ;
print $html "<th>JOSM</th>\n" ;
print $html "</tr>\n" ;
$i = 0 ;
foreach $wayId (@double) {
	$i++ ;

	print $html "<tr>\n" ;
	print $html "<td>", $i , "</td>\n" ;
	print $html "<td>", historyLink ("way", $wayId) , "</td>\n" ;

	print $html "<td>" ;
	foreach (@{$doubleWayTags{$wayId}}) { print $html $_, " - " ; }
	print $html "</td>\n" ;

	print $html "<td>" ;
	foreach (@{$doubleWayNodes{$wayId}}) { print $html $_, " - " ; }
	print $html "</td>\n" ;


	print $html "<td>", osmLink ($lon{$wayStart{$wayId}}, $lat{$wayStart{$wayId}}, 16) , "</td>\n" ;
	print $html "<td>", osbLink ($lon{$wayStart{$wayId}}, $lat{$wayStart{$wayId}}, 16) , "</td>\n" ;
	print $html "<td>", josmLink ($lon{$wayStart{$wayId}}, $lat{$wayStart{$wayId}}, 0.01, $wayId), "</td>\n" ;

	print $html "</tr>\n" ;

	# GPX
	my $text = $wayId . " - way with double nodes" ;
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

statistics ( ctime(stat($osmName)->mtime),  $program,  "double", $osmName,  $highwayCount,  $i) ;

print "\nfinished after ", stringTimeSpent ($time1-$time0), "\n\n" ;

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
