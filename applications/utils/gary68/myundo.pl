
use strict ;
use warnings ;

use Changeset ;
use Undo ;


# my $online = "no" ;
my $online = "YES" ;
my $undoFileName = "ofGer20101123.txt" ;
my $comment = "corrections" ;



my $delay = 1 ;

my $cs ;

if ($online eq "YES") {
	print "\nWORKING ONLINE - SURE?\n\n" ;
	sleep (5) ;


	$cs = Changeset::create() ;

	if (!defined $cs) {
		die "ERROR: changeset id could not be obtained\n" ;
	}
	else {
		print "INFO: changeset id = $cs\n" ;
	}

}



my $changeCount = 0 ;
my $successfulChanges = 0 ;

my $object ;
my $id ;
my $user ;

my $file ;

my $fileResult = open ($file, "<" , $undoFileName) ;
my $line ;
if ($fileResult) {
	print "INFO: undo file opened\n" ;

	while ($line = <$file>) {

		chomp ($line) ;		

		my @a = split (/,/, $line) ;
		$object = $a[0] ;
		$id = $a[1] ;
		$user = $a[2] ;

		if ((defined $object) and (defined $id) and (defined $user)) {
			print "INFO: undoing $object $id *$user*\n" ;
			$changeCount++ ;
			if ($online eq "YES") {
				my $oldCS ; undef $oldCS ;
				my $undoResult = Undo::undo ($object, $id, $user, $oldCS, $cs) ;
				if (defined $undoResult){
					$successfulChanges++ ;
					if ($undoResult == 0) { print "INFO: no action necessary\n" ; }
					if ($undoResult == 1) { print "INFO: success\n" ; }
				}	
				else {
					print "ERROR: undo could not be performed\n" ;
				}
				sleep ($delay) ;
			}
		}
	}
}
else {
	print "ERROR: undo file COULD NOT BE OPENED\n" ;
}


if ($online eq "YES") {
	my $result = Changeset::close($cs, $comment) ;
	if ( (defined $result) and ($result == 1) ) {
		print "INFO: changeset closed\n" ;
	}
	else {
		print "ERROR: changeset could not be closed properly!!!\n" ;
	}
}

print "\n$changeCount changes read from file.\n" ;
print "$successfulChanges changes actually done.\n\n" ;
