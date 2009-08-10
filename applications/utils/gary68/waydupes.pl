#
#
# version 1.1
# - added tags in output
#
#

use strict ;
use warnings ;

use OSM::osm 4.0 ;

my $programName = "waydupes.pl" ;
my $usage = "waydupes.pl file.osm out.htm out.gpx" ; 
my $version = "1.1" ;

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
my %numberNodesHash ;
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
	
	my $highway = 0 ;
	foreach my $tag (@wayTags) {
		# if ($tag->[0] eq "highway") { $highway = 1 ; $highwayCount++ ; }
		if ( ($tag->[0] eq "highway") and ($tag->[1] eq "residential") ){ $highway = 1 ; $highwayCount++ ; }
	}

	if ($highway == 1) {
		push @{$numberNodesHash{scalar(@wayNodes)}}, $wayId ;
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
print "$highwayCount highways found.\n" ;

print "comparing ways...\n" ;


my $comparisons = 0 ;
foreach my $number (keys %numberNodesHash) {
	# print "checking ways with length $number nodes.\n" ;
	foreach my $way1 (@{$numberNodesHash{$number}}) {
		foreach my $way2 (@{$numberNodesHash{$number}}) {
			if ($way1 < $way2) {
				$comparisons++ ;
				if (($comparisons % 10000000) == 0 ) { print "$comparisons comparisons done...\n" ; }
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

