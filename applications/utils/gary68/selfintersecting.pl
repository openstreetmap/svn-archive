#
#
# selfintersecting.pl
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


use strict ;
use warnings ;


use OSM::osm 4.9 ;

my $programName = "selfintersecting.pl" ;
my $usage = "selfintersecting.pl file.osm out.htm out.gpx" ; 
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

my @problems = () ;

my $wayCount = 0 ;
my $problems = 0 ;
my $APIcount = 0 ;
my $APIerrors = 0 ;

my $osmName ; 
my $gpxName ; 
my $htmName ; 

my %lon ; my %lat ;


my $time0 ; 

# get parameter

$osmName = shift||'';
if (!$osmName)
{
	die (print $usage, "\n");
}

$htmName = shift||'';
if (!$htmName)
{
	die (print $usage, "\n");
}

$gpxName = shift||'';
if (!$gpxName)
{
	die (print $usage, "\n");
}


print "\n$programName $version for file $osmName\n" ;

$time0 = time() ;

print "read node data...\n" ;
openOsmFile ($osmName) ;
($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
if ($nodeId != -1) {
	#@nodeTags = @$aRef1 ;
}

while ($nodeId != -1) {

	$lon{$nodeId} = $nodeLon ; 
	$lat{$nodeId} = $nodeLat ; 

	# next
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
	if ($nodeId != -1) {
		#@nodeTags = @$aRef1 ;
	}
}

print "done.\n" ;

print "parsing and checking ways...\n" ;

($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {
	if (scalar (@wayNodes) > 3) {
		$wayCount++ ;
		if ($wayCount % 1000000 == 0) { print "$wayCount ways processed...\n" ;}
		my ($tagText) = "" ;
		my ($highway) = 0 ;
		foreach my $t (@wayTags) { 
			$tagText = $tagText . $t->[0] . ":" . $t->[1] . "<br>" ; 
			if ($t->[0] eq "highway") { $highway = 1 ; }
		}

		$tagText = $tagText . "<br>Nodes: " . scalar (@wayNodes) . "<br><br>Nodes:  @wayNodes<br>" ;

		my %count = () ;
		foreach my $n (@wayNodes) { $count{$n}++ ; }
		my ($double) = 0 ; my $doubleError = 0 ;
		foreach my $n (keys %count) {
			if ($count{$n}>1) { $double++ ; }
		}
		if ($double > 1) { $doubleError = 1 ; }
		if ( ($double == 1) and ($wayNodes[0] != $wayNodes[-1]) ) { $doubleError = 1 ; }

		# check highway exception and possibly reset $doubleError
		my ($doubleNodes) = 0 ;
		foreach my $n (@wayNodes) {
			if ($count{$n} == 2) { $doubleNodes++ ; }
			if ($count{$n} > 2) { $doubleNodes = 100 ; } # at least one node used more than twice -> error
		}
		if ($doubleNodes == 2) {
			if ( ($count{$wayNodes[0]} == 2) and ($count{$wayNodes[-1]} != 2) ) { $doubleError = 0 ; }
			if ( ($count{$wayNodes[-1]} == 2) and ($count{$wayNodes[0]} != 2) ) { $doubleError = 0 ; }
		}
		if ( ($doubleNodes == 4) and ($count{$wayNodes[0]} == 2) and ($count{$wayNodes[-1]} == 2) ) {
			 $doubleError = 0 ; 
		}
	
		if ($doubleError) {
			$problems++ ;
			my $node ;
			foreach my $n (@wayNodes[1..$#wayNodes-1]) {
				if ($count{$n}>1)  { $node = $n ; }
			}
			push @problems, [$wayId, 0, 0, $node, "Node used twice", $lon{$node}, $lat{$node}, $tagText] ;
		}
		else { # check segments
			my $crossingFound = 0 ;
			my ($a, $b) ; 
			my ($cLon, $cLat, $seg1, $seg2) ;
			# print $wayId, " ", $#wayNodes, "\n" ;
			for ($a=0; $a<$#wayNodes-1; $a++) {
				for ($b=$a + 1; $b<$#wayNodes; $b++) {
					my ($x, $y) = crossing ($lon{$wayNodes[$a]}, 
									$lat{$wayNodes[$a]}, 
									$lon{$wayNodes[$a+1]}, 
									$lat{$wayNodes[$a+1]}, 
									$lon{$wayNodes[$b]}, 
									$lat{$wayNodes[$b]}, 
									$lon{$wayNodes[$b+1]}, 
									$lat{$wayNodes[$b+1]}) ;
					if (($x != 0) and ($y != 0)) {
						#print "\nFOUND $wayId $a $b $x $y\n" ;
						#print "number nodes: ", scalar (@wayNodes), "\n" ;
						#print "nodes list: @wayNodes\n" ;
						#print "$wayNodes[$a] $wayNodes[$a+1] $wayNodes[$b] $wayNodes[$b+1]\n" ;
						#print $lon{$wayNodes[$a]}, " ", $lat{$wayNodes[$a]}, " ", $lon{$wayNodes[$a+1]}, " ", $lat{$wayNodes[$a+1]}, "\n" ; 
						#print $lon{$wayNodes[$b]}, " ", $lat{$wayNodes[$b]}, " ", $lon{$wayNodes[$b+1]}, " ", $lat{$wayNodes[$b+1]}, "\n" ; 
						$crossingFound = 1 ;
						$seg1 = $a ; $seg2 = $b ; $cLon = $x ; $cLat = $y ;
					} # found
				} # for
			} # for


			if ($crossingFound == 1) {
				# check API way data
				print "request API data for way $wayId...\n" ;
				$APIcount++ ;
				my ($id, $u, @nds, @tags, $ndsRef, $tagRef) ;
				($id, $u, $ndsRef, $tagRef) = APIgetWay ($wayId) ;
				print "API request finished.\n" ;
				@nds = @$ndsRef ; @tags = @$tagRef ;
				if ($id == 0) { $APIerrors++ ; }
				if ( ( scalar @wayNodes != scalar @nds) and ($wayId == $id) ) { 
					$crossingFound = 0 ; 
					print "WARNING: way $wayId segment crossing but API node count mismatch. Ignoring this way.\n" ;
				}
				if ($crossingFound == 1) {
					$problems++ ;
					push @problems, [$wayId, $seg1, $seg2, 0, "<p>Intersection at<br>" . $cLon . "<br>" . $cLat . "</p>\n", $cLon, $cLat, $tagText] ;
				}
			}
		}
	}
	
	($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile() ;

print "done.\n" ;
print "$wayCount ways found.\n" ;
print "$problems problems found.\n" ;
print "$APIcount API calls.\n" ;
print "$APIerrors API errors.\n\n" ;


my $html ; my $gpx ;
open ($html, ">", $htmName) || die ("Can't open html output file") ;
open ($gpx, ">", $gpxName) || die ("Can't open gpx output file") ;


printHTMLiFrameHeader ($html, "Self intersecting way Check by Gary68") ;
printGPXHeader ($gpx) ;

print $html "<H1>Self intersecting way Check by Gary68</H1>\n" ;
print $html "<p>Version ", $version, "</p>\n" ;
print $html "<H2>Statistics</H2>\n" ;
print $html "<p>", stringFileInfo ($osmName), "<br>\n" ;
print $html "number ways total: $wayCount<br>\n" ;
print $html "number problems: $problems<br>\n" ;
print $html "</p>\n" ;


print $html "<H2>Data</H2>\n" ;
print $html "<table border=\"1\">\n";
print $html "<tr>\n" ;
print $html "<th>Line</th>\n" ;
print $html "<th>Way Id</th>\n" ;
print $html "<th>Way tags</th>\n" ;
print $html "<th>Segment 1</th>\n" ;
print $html "<th>Segment 2</th>\n" ;
print $html "<th>Node</th>\n" ;
print $html "<th>Text</th>\n" ;
print $html "<th>JOSM</th>\n" ;
print $html "<th>OSM</th>\n" ;
print $html "<th>OSB</th>\n" ;
print $html "</tr>\n" ;

my $line = 0 ;
foreach my $problem (@problems) {
	$line++ ;
	my ($lo) = $problem->[5] ;
	my ($la) = $problem->[6] ;
	
	print $html "<tr>\n" ;
	print $html "<td>", $line , "</td>\n" ;
	print $html "<td>", historyLink ("way", $problem->[0]), "</td>\n"  ;
	print $html "<td><p>" . $problem->[7] . "</p></td>\n" ;
	print $html "<td>", $problem->[1], "</td>\n" ;
	print $html "<td>", $problem->[2], "</td>\n" ;
	print $html "<td>", historyLink ("node", $problem->[3]), "</td>\n" ;
	print $html "<td>", $problem->[4], "</td>\n" ;
	print $html "<td>", josmLinkSelectWays ($lo, $la, 0.01, $problem->[0]), "</td>\n" ;
	print $html "<td>", osmLink ($lo, $la, 16) , "<br>\n" ;
	print $html "<td>", osbLink ($lo, $la, 16) , "<br>\n" ;
	print $html "</tr>\n" ;
	
	my ($text) = "Self intersecting way - " . $problem->[0] ;
	printGPXWaypoint ($gpx, $lo, $la, $text) ;
}

print $html "</table>\n" ;
print $html "<p>", stringTimeSpent (time()-$time0), "</p>\n" ;

printHTMLFoot ($html) ;
printGPXFoot ($gpx) ;

close ($html) ;
close ($gpx) ;


print "\n$programName finished after ", stringTimeSpent (time()-$time0), "\n\n" ;




