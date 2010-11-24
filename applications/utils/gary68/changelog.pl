


# changelog.pl


use strict ;
use warnings ;

use OSM::osm ;

my $version = 1.0 ;

my %users  ;
$users{"Oberförster"} = 1 ;
$users{"pfoten-weg"} = 1 ;
$users{"pfoten_weg_!_"} = 1 ;
$users{"greatmaster"} = 1 ;
$users{"BugBuster"} = 1 ;
$users{"leck_mich_alle_am_arsch"} = 1 ;
$users{"hasse_osm_korinthenkacker"} = 1 ;
$users{"Ich_hasse_doitsche_OSM-Korinthenkacker"} = 1 ;
$users{"Kraftfahrstraßen"} = 1 ;
$users{"asmtb"} = 1 ;
#$users{""} = 1 ;

my %lastNodeUser = () ;
my %lastWayUser = () ;

my $fileName1 ;
my $fileName2 ;
my $txtFileName ;


# read data from file
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
my $relationId ;
my $relationUser ;
my @relationTags ;
my @relationMembers ;

my %lon ; my %lat ;

my $txtFile ;
my $htmlFile ;

my %memNodeTags2 = () ;
my %memWayTags2 = () ;

my $editedNodes = 0 ;
my $editedWays = 0 ;
my $changedNodes = 0 ;
my $changedWays = 0 ;

my %changedNodesUser = () ;
my %changedWaysUser = () ;

my @ways = () ;
my %neededNodes = () ;
my $line = 0 ;

$fileName1 = shift ;
$fileName2 = shift ;
$txtFileName = shift ;


print "$fileName1\n" ;
print "$fileName2\n" ;
print "$txtFileName\n" ;

open ($txtFile, ">", $txtFileName) ;

my $htmlFileName = $txtFileName ;
$htmlFileName =~ s/.txt/.htm/ ;
open ($htmlFile, ">", $htmlFileName) ;
printHTMLiFrameHeader ($htmlFile, "changelog") ;
print $htmlFile "<H1>Changelog</H1>\n" ;
print $htmlFile "<table border=\"1\">\n";
print $htmlFile "<tr>\n" ;
print $htmlFile "<th>Line</th>\n" ;
print $htmlFile "<th>Object</th>\n" ;
print $htmlFile "<th>Id</th>\n" ;
print $htmlFile "<th>User</th>\n" ;
print $htmlFile "<th>JOSM</th>\n" ;
print $htmlFile "<th>Changes</th>\n" ;
print $htmlFile "</tr>\n" ;




print "reading osm file 2...\n" ;
print "  - nodes\n" ;

openOsmFile ($fileName2) ;
($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
if ($nodeId != -1) {
	@nodeTags = @$aRef1 ;
}
while ($nodeId != -1) {

	if (! defined $nodeUser) {
		print "user undefined\n" ;
		$nodeUser = "unknown" ;
	}

	if ($nodeUser eq "") {
		print "user empty\n" ;
		$nodeUser = "unknown" ;
	}

	if (defined $users{$nodeUser}) {
		$lon{$nodeId} = $nodeLon ; $lat{$nodeId} = $nodeLat ;	
		@{$memNodeTags2{$nodeId}} = @nodeTags ;
		$lastNodeUser{$nodeId} = $nodeUser ;
		$editedNodes++ ;
	}

	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
	if ($nodeId != -1) {
		@nodeTags = @$aRef1 ;
	}
}

print "nodes last edited by users: $editedNodes\n" ;

print "  - ways\n" ;

($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {

	if (! defined $wayUser) {
		print "user undefined\n" ;
		$wayUser = "unknown" ;
	}

	if ($wayUser eq "") {
		print "user empty\n" ;
		$wayUser = "unknown" ;
	}

	if (defined $users{$wayUser}) {
		@{$memWayTags2{$wayId}} = @wayTags ;
		$lastWayUser{$wayId} = $wayUser ;
		$editedWays++
	}
	
	($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;

print "ways last edited by defined users: $editedWays\n" ;


print "reading osm file 1...\n" ;
print "  - nodes\n" ;

openOsmFile ($fileName1) ;
($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
if ($nodeId != -1) {
	@nodeTags = @$aRef1 ;
}
while ($nodeId != -1) {

	if (! defined $nodeUser) {
		print "user undefined\n" ;
		$nodeUser = "unknown" ;
	}

	if ($nodeUser eq "") {
		print "user empty\n" ;
		$nodeUser = "unknown" ;
	}

	if (defined $lon{$nodeId}) {
		if ($nodeUser ne $lastNodeUser{$nodeId}) {
			my ($result, $changes) = compareTags (\@nodeTags, $memNodeTags2{$nodeId}) ;
			if ($result) {
				$line++ ;
				writeFile ("node", $nodeId, $lastNodeUser{$nodeId}, $changes, $line) ;
				printHTMLLine ("node", $nodeId, $lastNodeUser{$nodeId}, $changes, $nodeLon, $nodeLat, $line) ;
				$changedNodes++ ;
				$changedNodesUser{$lastNodeUser{$nodeId}}++ ;
			}
		}
	}

	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
	if ($nodeId != -1) {
		@nodeTags = @$aRef1 ;
	}
}

print "nodes CHANGED by users: $changedNodes\n" ;

print "  - ways\n" ;
($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {

	if (! defined $wayUser) {
		print "user undefined\n" ;
		$wayUser = "unknown" ;
	}

	if ($wayUser eq "") {
		print "user empty\n" ;
		$wayUser = "unknown" ;
	}

	if (defined $memWayTags2{$wayId}) {
		if ($wayUser ne $lastWayUser{$wayId}) {
			my ($result, $changes) = compareTags (\@wayTags, $memWayTags2{$wayId}) ;
			if ($result) {
				$line++ ;
				writeFile ("way", $wayId, $lastWayUser{$wayId}, $changes, $line) ;
				push @ways, [$wayId, $lastWayUser{$wayId}, $changes, $wayNodes[0], $line] ;
				$neededNodes{$wayNodes[0]} = 1 ;
				$changedWays++ ;
				$changedWaysUser{$lastWayUser{$wayId}}++ ;
			}
		}
	}
	
	($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;

print "ways CHANGED by users: $changedWays\n" ;

print "get needed nodes from file 2...\n" ;

openOsmFile ($fileName2) ;
($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
if ($nodeId != -1) {
	@nodeTags = @$aRef1 ;
}
while ($nodeId != -1) {

	if (defined $neededNodes{$nodeId}) {
		$lon{$nodeId} = $nodeLon ; $lat{$nodeId} = $nodeLat ;	
	}

	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
	if ($nodeId != -1) {
		@nodeTags = @$aRef1 ;
	}
}

closeOsmFile () ;

foreach my $way (@ways) {
	my $wayId = $way->[0] ;	
	my $wayUser = $way->[1] ;	
	my $changes = $way->[2] ;	
	my $node = $way->[3] ;	
	my $line = $way->[4] ;	
	printHTMLLine ("way", $wayId, $wayUser, $changes, $lon{$node}, $lat{$node}, $line) ;
}



print $htmlFile "</table>\n" ;

printHTMLFoot ($htmlFile) ;
close ($htmlFile) ;

close ($txtFile) ;

print "\nChanged nodes per User:\n" ;
foreach my $u (sort keys %changedNodesUser) {
	printf "%-25s %6d\n", $u, $changedNodesUser{$u} ;
}

print "\nChanged ways per User:\n" ;
foreach my $u (sort keys %changedWaysUser) {
	printf "%-25s %6d\n", $u, $changedWaysUser{$u} ;
}


print "\ndone.\n" ;


# -----------------------------------------------------

sub writeFile {
	my ($object, $id, $user, $comment) = @_ ;
	print $txtFile "$object,$id,$user,$comment,$line\n" ;
}

sub compareTags {
	my ($ref1, $ref2) = @_ ;
	my @tags1 = @$ref1 ;
	my @tags2 = @$ref2 ;
	my %t1 = () ; my %t2 = () ;

	my $changed = 0 ;
	my $changes = "" ;

	foreach my $tag (@tags1) { $t1{$tag->[0]} = $tag->[1] ; }
	foreach my $tag (@tags2) { $t2{$tag->[0]} = $tag->[1] ; }

	foreach my $t (keys %t1) {
		if (!defined $t2{$t}) {
			# deleted tag
			$changes .= "DELETED TAG $t:$t1{$t} " ;
			$changed = 1 ;
		}
		else {
			if ($t1{$t} ne $t2{$t}) {
				# changed
				$changes .= "CHANGED TAG $t: $t1{$t} -> $t2{$t} " ;
				$changed = 1 ;
			}
		}
	}
	
	foreach my $t (keys %t2) {
		if (!defined $t1{$t}) {
			# new tag
			$changes .= "NEW TAG $t:$t2{$t} " ;
			$changed = 1 ;
		}
	}

	return ($changed, $changes) ;
}


sub printHTMLLine {
	my ($object, $id, $user, $changes, $lon, $lat, $line) = @_ ;
	print $htmlFile "<tr>\n" ;
	print $htmlFile "<td>$line</th>\n" ;
	print $htmlFile "<td>$object</th>\n" ;
	print $htmlFile "<td>", historyLink ($object, $id) , "</td>\n" ;
	print $htmlFile "<td>$user</th>\n" ;
	if ($object eq "way") {
		print $htmlFile "<td>", josmLink ($lon, $lat, 0.001, $id), "</td>\n" ;
	}
	if ($object eq "node") {
		print $htmlFile "<td>", josmLinkSelectNode ($lon, $lat, 0.001, $id), "</td>\n" ;
	}
	print $htmlFile "<td>$changes</th>\n" ;
	print $htmlFile "</tr>\n" ;
}
