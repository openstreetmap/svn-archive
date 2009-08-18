#
#
# version 1.1
# - added tags in output
# version 1.2
# - check only ways with more than one node...
# version 1.3
# - speed improvement
# version 2.0
# - new algorhithm
#
# version 2.1
# - added check output
#


my @test = (	["highway", "residential"],
		 ["highway", "service"],
		 ["highway", "roundabout"],
		 ["highway", "motorway"],
		 ["highway", "motorway_link"],
		 ["highway", "trunk"],
		 ["highway", "trunk_link"],
		 ["highway", "primary"],
		 ["highway", "primary_link"],
		 ["highway", "secondary"],
		 ["highway", "secondary_link"],
		 ["highway", "tertiary"],
		 ["highway", "unclassified"],
		 ["highway", "cycleway"],
		 ["highway", "footway"],
		 ["highway", "track"]) ;

use strict ;
use warnings ;

use OSM::osm 4.0 ;

my $programName = "waydupes.pl" ;
my $usage = "waydupes.pl file.osm out.htm out.gpx" ; 
my $version = "2.1" ;

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

my %wayNodesHash ;
my %wayTagsHash ;
my %startNodeHash ;
my %neededNodes = () ;
my @problems = () ;

my $highwayCount = 0 ;
my $dupes = 0 ;

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

print "\ncheck ways:\n" ;
foreach my $w (@test) { print $w->[0], " ", $w->[1], "\n" ; } 
print "\n\n" ;

$time0 = time() ;

openOsmFile ($osmName) ;
print "skipping nodes...\n" ;
skipNodes() ;

print "parsing ways...\n" ;

($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {
	
	my $toTest = 0 ;
	foreach my $tag (@wayTags) {
		foreach my $t (@test) {
			if ( ($tag->[0] eq $t->[0]) and ($tag->[1] eq $t->[1]) ){ $toTest = 1 ; $highwayCount++ ; }
		}
	}

	if ( ($toTest == 1) and (scalar @wayNodes > 1) ) {
		push @{$startNodeHash{$wayNodes[0]}}, $wayId ;
		@{$wayTagsHash{$wayId}} = @wayTags ;
		@{$wayNodesHash{$wayId}} = @wayNodes ;
	}
	
	($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile() ;

print "done.\n" ;
print "$highwayCount ways found.\n" ;

print "comparing ways...\n" ;


my $comparisons = 0 ;
foreach my $startNode (keys %startNodeHash) {
	if ( scalar (@{$startNodeHash{$startNode}}) > 1 ) {
		foreach my $way1 (@{$startNodeHash{$startNode}}) {
			foreach my $way2 (@{$startNodeHash{$startNode}}) {
				if ($way1 < $way2) {
					$comparisons++ ;
					if (($comparisons % 10000000) == 0 ) { print "$comparisons detailed comparisons done...\n" ; }
					my %count = () ;
					my $different = 0 ;
					foreach my $item (@{$wayNodesHash{$way1}}, @{$wayNodesHash{$way2}}) { $count{$item}++ ; }
					foreach my $item (keys %count) {
						if ($count{$item} != 2) { $different = 1 ; }
					}
					if ($different == 0) {
						# print "$way1 and $way2 are dupes\n" ; 
						$dupes++ ;
						$neededNodes{$wayNodesHash{$way1}[0]} = 1 ;
						push @problems, [$way1, $way2] ;
					}
				}
			}
		}
	}
}




print "$dupes dupes found.\n\n" ;



print "read node data...\n" ;
openOsmFile ($osmName) ;
($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
if ($nodeId != -1) {
	#@nodeTags = @$aRef1 ;
}

while ($nodeId != -1) {

	if (defined ($neededNodes{$nodeId}) ) { $lon{$nodeId} = $nodeLon ; $lat{$nodeId} = $nodeLat ; }

	# next
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
	if ($nodeId != -1) {
		#@nodeTags = @$aRef1 ;
	}
}

closeOsmFile() ;
print "done.\n" ;






my $html ; my $gpx ;
open ($html, ">", $htmName) || die ("Can't open html output file") ;
open ($gpx, ">", $gpxName) || die ("Can't open gpx output file") ;


printHTMLHeader ($html, "Dupe Way Check by Gary68") ;
printGPXHeader ($gpx) ;

print $html "<H1>Dupe Way Check by Gary68</H1>\n" ;
print $html "<p>Version ", $version, "</p>\n" ;

print $html "<H2>Check ways</H2>\n<p>" ;foreach my $w (@test) { print $html $w->[0], " ", $w->[1], "<br>\n" ; } 
print $html "</p>\n" ;

print $html "<H2>Statistics</H2>\n" ;
print $html "<p>", stringFileInfo ($osmName), "<br>\n" ;
print $html "number ways total: $highwayCount<br>\n" ;
print $html "number dupes: $dupes<br>\n" ;
print $html "</p>\n" ;


print $html "<H2>Data</H2>\n" ;
print $html "<table border=\"1\">\n";
print $html "<tr>\n" ;
print $html "<th>Line</th>\n" ;
print $html "<th>Way1 Id</th>\n" ;
print $html "<th>Way2 Id</th>\n" ;
print $html "<th>OSM</th>\n" ;
print $html "<th>OSB</th>\n" ;
print $html "<th>JOSM</th>\n" ;
print $html "</tr>\n" ;

my $line = 0 ;
foreach my $problem (@problems) {
	$line++ ;
	my ($lo) = $lon{$wayNodesHash{$problem->[0]}[0]} ;
	my ($la) = $lat{$wayNodesHash{$problem->[0]}[0]} ;
	#print "ways are dupes: $problem->[0] $problem->[1], $lo, $la\n" ;
	
	print $html "<tr>\n" ;
	print $html "<td>", $line , "</td>\n" ;
	print $html "<td>", historyLink ("way", $problem->[0]), "\n"  ;
	foreach my $tag (@{$wayTagsHash{$problem->[0]}}) { print $html "<br>\n", $tag->[0], ":", $tag->[1] ; }
	print $html "</td>\n" ;

	print $html "<td>", historyLink ("way", $problem->[1]) ;
	foreach my $tag (@{$wayTagsHash{$problem->[1]}}) { print $html "<br>\n", $tag->[0], ":", $tag->[1] ; }
	print $html "</td>\n" ;

	print $html "<td>", osmLink ($lo, $la, 16) , "<br>\n" ;
	print $html "<td>", osbLink ($lo, $la, 16) , "<br>\n" ;
	print $html "<td>", josmLinkSelectWays ($lo, $la, 0.01, $problem->[0], $problem->[1]), "</td>\n" ;
	print $html "</tr>\n" ;
	
	my ($text) = "Waydupes - " . $problem->[0] . " and " . $problem->[1] ;
	printGPXWaypoint ($gpx, $lo, $la, $text) ;
}

print $html "</table>\n" ;
print $html "<p>", stringTimeSpent (time()-$time0), "</p>\n" ;

printHTMLFoot ($html) ;
printGPXFoot ($gpx) ;

close ($html) ;
close ($gpx) ;


print "\n$programName finished after ", stringTimeSpent (time()-$time0), "\n\n" ;

