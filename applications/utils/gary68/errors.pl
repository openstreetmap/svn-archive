


# errors.pl


use strict ;
use warnings ;

use OSM::osm ;

my $version = 1.3 ;

my @allowedREF = qw (motorway motorway_link trunk trunk_link primary primary_link secondary secondary_link tertiary unclassified) ;
my %allowedREFHash = () ;
foreach my $a (@allowedREF) { $allowedREFHash{$a} = 1 ; }

my %users  ;


#$users{""} = 1 ;


my $fileName ;
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

my $txtFile ;

my $line = 0 ;
my $problems = 0 ;

$fileName = shift ;
$txtFileName = shift ;

my $html ;
my $htmlName ;


open ($txtFile, ">", $txtFileName) or die ("ERROR: couldn't open outfile $txtFileName\n") ;

$htmlName = $txtFileName ;
$htmlName =~ s/\.txt/\.htm/i ;

open ($html, ">", $htmlName)  or die ("ERROR: couldn't open outfile $htmlName\n");
printHTMLiFrameHeader ($html, "errors by Gary68") ;

print $html "<H1>Errors by Gary68</H1>\n" ;
print $html "<p>Version ", $version, "</p>\n" ;
print $html "<H2>Statistics</H2>\n" ;
print $html "<p>", stringFileInfo ($fileName), "</p>\n" ;

print $html "<H2>Data</H2>\n" ;
print $html "<table border=\"1\">\n";
print $html "<tr>\n" ;
print $html "<th>Line</th>\n" ;
print $html "<th>Object</th>\n" ;
print $html "<th>OSM link</th>\n" ;
print $html "<th>History link</th>\n" ;
print $html "<th>User</th>\n" ;
print $html "<th>Comment</th>\n" ;
print $html "</tr>\n" ;


print "reading osm file...\n" ;
print "  - nodes\n" ;

openOsmFile ($fileName) ;

skipNodes() ;



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

#	if (defined $users{$wayUser}) {

		my $ref ; my $refPresent = 0 ; my $refUsed = "" ; my $highway = 0 ; my $invalidRefPresent = 0 ;
		foreach my $t (@wayTags) {
			if ( ($t->[0] eq "highway") and (defined $allowedREFHash{$t->[1]}) ) { $highway = 1 ; }
			if ($t->[0] eq "ref") {
				$refPresent = 1 ;
				$refUsed = $t->[1] ;
				$refUsed =~ s/neu//ig ;
				$refUsed =~ s/alt//ig ;
				my @refs ;
				if (grep /;/, $refUsed) { 
					@refs = split /;/, $refUsed ; 
				}
				else {
					if (grep /,/, $refUsed) { @refs = split /,/, $refUsed ; }
					else {
						if (grep /\//, $refUsed) { @refs = split /\//, $refUsed ; }
						else {
							@refs = ($refUsed) ;
						}

					}
				}
				foreach my $r (@refs) {
					($ref) = ( $r =~ /^(\s*[a-zäöüÄÖÜ]+\s*[0-9]+\s*[a-z]?\s*)$/i ) ;
					if (!defined $ref) { $invalidRefPresent = 1 ; }
				}
			}
		}
		if ($refPresent and ($invalidRefPresent) and $highway) {
			$problems++ ;
			writeFiles ("way", $wayId, $wayUser, "UNCOMMON REF: $refUsed", $problems) ;
		}

#	}
	
	($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;

print $html "</table>\n" ;


print "problems found: $problems\n" ;

print $html "<P>problems found: $problems</P>\n" ;


printHTMLFoot ($html) ;


close ($txtFile) ;
close ($html) ;


# -----------------------------------------------------

sub writeFiles {
	my ($object, $id, $user, $comment, $line) = @_ ;
	# print $txtFile "$object,$id,$user,$comment,$line\n" ;

	my $text = sprintf "%s,%d,%s," , $object,$id,$user ;
	# printf $txtFile "%s,%d,%s, %-80s %-5d\n", $object,$id,$user,$comment,$line ;
	printf $txtFile "%-50s %-80s %-5d\n", $text, $comment,$line ;

	print $html "<tr>\n" ;
	print $html "<td>$line</td>\n" ;
	print $html "<td>$object</td>\n" ;
	print $html "<td>", objectLink($object, $id) , "</td>\n" ;
	print $html "<td>", historyLink ($object, $id), "</td>\n" ;
	print $html "<td>$user</td>\n" ;
	print $html "<td>$comment</td>\n" ;
	print $html "</tr>\n" ;

}

# ----------------------------------------------------------------

sub objectLink {
	my $obj = shift ;
	my $id = shift ;

	my $link = "<A HREF=\"http://www.openstreetmap.org/?$obj=$id\">$id</A>" ;

	return $link ;
}
