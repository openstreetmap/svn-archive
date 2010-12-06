


# errors.pl


use strict ;
use warnings ;

use OSM::osm ;

my $version = 1.2 ;

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



open ($txtFile, ">", $txtFileName) ;




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
			writeFile ("way", $wayId, $wayUser, "UNCOMMON REF: $refUsed", $problems) ;
		}

#	}
	
	($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

print "problems found: $problems\n" ;

closeOsmFile () ;

close ($txtFile) ;


# -----------------------------------------------------

sub writeFile {
	my ($object, $id, $user, $comment, $line) = @_ ;
	print $txtFile "$object,$id,$user,$comment          ,$line\n" ;
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
