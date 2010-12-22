#
#
#
#
#
# Copyright (C) 2010, Gerhard Schwanz
#
# This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 3 of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>
#


# TODO
#
# db size
# 
#


package OSM::osmDB ;

my $DBname = "test" ;
my $DBuser = "root" ;
my $DBpassword = "7636" ;


use strict ;
use warnings ;

use DBI ;



use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

$VERSION = '1.0' ;

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw ( 	dbConnect
			dbDisconnect
			deleteDBNode
			deleteDBWay
			deleteDBRelation
			getDBNode
			getDBWay
			getDBRelation
			initTableNodes
			initTableWays
			initTableRelations
			loopInitNodes
			loopGetNextNode
			loopInitRelations
			loopGetNextRelation
			loopInitWays
			loopGetNextWay
			printMaxValues
			storeDBNode			storeDBWay			storeDBRelation			) ;


my $dbh ;
my $sthLoopNodes ;
my $sthLoopWays ;
my $sthLoopRelations ;

my $maxK = 0 ;
my $maxV = 0 ;

# ----------------------------------------------------------------------------

sub deleteDBNode {
	my $id = shift ;
	$dbh->do("DELETE FROM nodes WHERE id = $id") ;
	$dbh->do("DELETE FROM nodetags WHERE id = $id") ;
}

sub deleteDBWay {
	my $id = shift ;
	$dbh->do("DELETE FROM ways WHERE id = $id") ;
	$dbh->do("DELETE FROM waytags WHERE id = $id") ;
	$dbh->do("DELETE FROM waynodes WHERE id = $id") ;
}

sub deleteDBRelation {
	my $id = shift ;
	$dbh->do("DELETE FROM relations WHERE id = $id") ;
	$dbh->do("DELETE FROM relationtags WHERE id = $id") ;
	$dbh->do("DELETE FROM relationmembers WHERE id = $id") ;
}

# ----------------------------------------------------------------------------



sub storeDBNode {
	my ($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = @_ ;
	my @nodeTags = @$aRef1 ;

	$dbh->do("INSERT INTO nodes (id, lon, lat, user) VALUES ($nodeId, $nodeLon, $nodeLat, '$nodeUser')") ;

	foreach my $t (@nodeTags) {
		my $k = $t->[0] ;
		my $v = $t->[1] ;
		if (length $k > $maxK) { $maxK = length $k ; }
		if (length $v > $maxV) { $maxV = length $v ; }
		$dbh->do("INSERT INTO nodetags (id, k, v) VALUES ($nodeId, '$k', '$v')") ;
	}
}

sub storeDBWay {
	my ($wayId, $wayUser, $aRef1, $aRef2) = @_ ;
	my @wayNodes = @$aRef1 ;
	my @wayTags = @$aRef2 ;

	$dbh->do("INSERT INTO ways (id, user) VALUES ($wayId, '$wayUser')") ;

	foreach my $t (@wayTags) {
		my $k = $t->[0] ;
		my $v = $t->[1] ;
		if (length $k > $maxK) { $maxK = length $k ; }
		if (length $v > $maxV) { $maxV = length $v ; }
		$dbh->do("INSERT INTO waytags (id, k, v) VALUES ($wayId, '$k', '$v')") ;
	}

	my $i = 0 ;
	foreach my $n (@wayNodes) {
		$dbh->do("INSERT INTO waynodes (id, s, nodeid) VALUES ($wayId, $i, $n)") ;
		$i++ ;
	}
}


sub storeDBRelation {
	my ($relationId, $relationUser, $aRef1, $aRef2) = @_ ;
	my @relationMembers = @$aRef1 ;
	my @relationTags = @$aRef2 ;

	$dbh->do("INSERT INTO relations (id, user) VALUES ($relationId, '$relationUser')") ;

	foreach my $t (@relationTags) {
		my $k = $t->[0] ;
		my $v = $t->[1] ;
		if (length $k > $maxK) { $maxK = length $k ; }
		if (length $v > $maxV) { $maxV = length $v ; }
		$dbh->do("INSERT INTO relationtags (id, k, v) VALUES ($relationId, '$k', '$v')") ;
	}

	my $i = 0 ;
	foreach my $m (@relationMembers) {
		my $type = $m->[0] ;
		my $mid = $m->[1] ;
		my $role = $m->[2] ;
		$dbh->do("INSERT INTO relationmembers (id, s, type, memberid, role) VALUES ($relationId, $i, '$type', $mid, '$role')") ;
		$i++ ;
	}
}

sub printMaxValues {
	print "\nmax key length = $maxK\n" ;
	print "max val length = $maxV\n\n" ;
}


# ----------------------------------------------------------------------------

sub loopInitNodes {
	my ($k, $v) = @_ ;
	my $kq = "" ;
	my $vq = "" ;
	my $and = "" ;

	if (defined $k) { $kq = " k = '$k'" ; }
	if (defined $v) { $vq = " v = '$v'" ; }

	if ( (defined $k) and (defined $v) ) {
		$and = " AND " ;
	}

	if ( (! defined $k) and (! defined $v) ) {
		$sthLoopNodes = $dbh->prepare("SELECT id FROM nodes ORDER BY id") or die "Couldn't prepare statement: " . $dbh->errstr ;
	}
	else {
		my $q = "SELECT id from nodetags WHERE $kq $and $vq ORDER BY id" ;
		$sthLoopNodes = $dbh->prepare("$q") or die "Couldn't prepare statement: " . $dbh->errstr ;
	}

 	$sthLoopNodes->execute() or die "Couldn't execute statement: " . $sthLoopNodes->errstr ;
}

sub loopGetNextNode {
	my $id = undef ;
	my @data ;
	if (@data = $sthLoopNodes->fetchrow_array()) {		$id = $data[0] ;
	}
	else {
		$sthLoopNodes->finish ;
		$id = undef ;
	}
	return $id ;
}

sub loopInitWays {
	my ($k, $v) = @_ ;
	my $kq = "" ;
	my $vq = "" ;
	my $and = "" ;

	if (defined $k) { $kq = " k = '$k'" ; }
	if (defined $v) { $vq = " v = '$v'" ; }

	if ( (defined $k) and (defined $v) ) {
		$and = " AND " ;
	}

	if ( (! defined $k) and (! defined $v) ) {
		$sthLoopWays = $dbh->prepare("SELECT id FROM ways ORDER BY id") or die "Couldn't prepare statement: " . $dbh->errstr ;
	}
	else {
		my $q = "SELECT id from waytags WHERE $kq $and $vq ORDER BY id" ;
		$sthLoopWays = $dbh->prepare("$q") or die "Couldn't prepare statement: " . $dbh->errstr ;
	}

 	$sthLoopWays->execute() or die "Couldn't execute statement: " . $sthLoopWays->errstr ;
}

sub loopGetNextWay {
	my $id = undef ;
	my @data ;
	if (@data = $sthLoopWays->fetchrow_array()) {		$id = $data[0] ;
	}
	else {
		$sthLoopWays->finish ;
		$id = undef ;
	}
	return $id ;
}

sub loopInitRelations {
	my ($k, $v) = @_ ;
	my $kq = "" ;
	my $vq = "" ;
	my $and = "" ;

	if (defined $k) { $kq = " k = '$k'" ; }
	if (defined $v) { $vq = " v = '$v'" ; }

	if ( (defined $k) and (defined $v) ) {
		$and = " AND " ;
	}

	if ( (! defined $k) and (! defined $v) ) {
		$sthLoopRelations = $dbh->prepare("SELECT id FROM relations ORDER BY id") or die "Couldn't prepare statement: " . $dbh->errstr ;
	}
	else {
		my $q = "SELECT id from relationtags WHERE $kq $and $vq ORDER BY id" ;
		$sthLoopRelations = $dbh->prepare("$q") or die "Couldn't prepare statement: " . $dbh->errstr ;
	}

 	$sthLoopRelations->execute() or die "Couldn't execute statement: " . $sthLoopRelations->errstr ;
}

sub loopGetNextRelation {
	my $id = undef ;
	my @data ;
	if (@data = $sthLoopRelations->fetchrow_array()) {		$id = $data[0] ;
	}
	else {
		$sthLoopRelations->finish ;
		$id = undef ;
	}
	return $id ;
}


# ----------------------------------------------------------------------------

sub getDBNode {
	my $id = shift ;

	my $user = undef ;
	my $lon = undef ;
	my $lat = undef ;
	my @nodeTags = () ;
	my $refTags = undef ;
	my %properties = () ;
	my $refProperties = undef ;

	my $sth = $dbh->prepare("SELECT * FROM nodes WHERE id = $id") or die "Couldn't prepare statement: " . $dbh->errstr ;
	my @data ;
 	$sth->execute() or die "Couldn't execute statement: " . $sth->errstr ;
	while (@data = $sth->fetchrow_array()) {
		$user = $data[3] ;
		$lon = $data[1] ;
		$lat = $data[2] ;
		$properties{"user"} = $user ;
		$properties{"lon"} = $lon ;
		$properties{"lat"} = $lat ;
	}

	if ($sth->rows == 0) {
		print STDERR "ERROR: node $id not found in DB.\n\n" ;
	}
	$sth->finish ;

	my $sth2 = $dbh->prepare("SELECT * FROM nodetags WHERE id = $id") or die "Couldn't prepare statement: " . $dbh->errstr ;
	my @tagdata;
	$sth2->execute() or die "Couldn't execute statement: " . $sth->errstr ;
	my @data2 ;
	while (@data2 = $sth2->fetchrow_array()) {
		my $k = $data2[1] ;
		my $v = $data2[2] ;
		push @nodeTags, [$k, $v] ;
	}
	$sth2->finish ;

	$refTags = \@nodeTags ;
	$refProperties = \%properties ;

	return ($refProperties, $refTags) ;
}

sub getDBWay {
	my $id = shift ;

	my $user = undef ;
	my @wayTags = () ;
	my @wayNodes = () ;
	my %properties = () ;
	my $refNodes = undef ;
	my $refTags = undef ;
	my $refProperties = undef ;

	my $sth = $dbh->prepare("SELECT * FROM ways WHERE id = $id") or die "Couldn't prepare statement: " . $dbh->errstr ;
	my @data ;
 	$sth->execute() or die "Couldn't execute statement: " . $sth->errstr ;
	while (@data = $sth->fetchrow_array()) {		$user = $data[1] ;
		$properties{"user"} = $user ;
	}

	if ($sth->rows == 0) {
		print STDERR "ERROR: node $id not found in DB.\n\n" ;
	}
	$sth->finish ;

	my $sth2 = $dbh->prepare("SELECT * FROM waytags WHERE id = $id") or die "Couldn't prepare statement: " . $dbh->errstr ;
	$sth2->execute() or die "Couldn't execute statement: " . $sth->errstr ;
	my @data2 ;
	while (@data2 = $sth2->fetchrow_array()) {
		my $k = $data2[1] ;
		my $v = $data2[2] ;
		push @wayTags, [$k, $v] ;
	}
	$sth2->finish ;

	my $sth3 = $dbh->prepare("SELECT * FROM waynodes WHERE id = $id ORDER BY s") or die "Couldn't prepare statement: " . $dbh->errstr ;
	$sth3->execute() or die "Couldn't execute statement: " . $sth->errstr ;
	my @data3 ;
	while (@data3 = $sth3->fetchrow_array()) {
		my $n = $data3[2] ;
		push @wayNodes, $n ;
	}
	$sth3->finish ;

	$refTags = \@wayTags ;
	$refNodes = \@wayNodes ;
	$refProperties = \%properties ;
	return ($refProperties, $refNodes, $refTags) ;
}

sub getDBRelation {
	my $id = shift ;

	my $user = undef ;
	my @relationTags = () ;
	my @relationMembers = () ;
	my %properties = ()  ;
	my $refMembers = undef ;
	my $refTags = undef ;
	my $refProperties = undef ;

	my $sth = $dbh->prepare("SELECT * FROM relations WHERE id = $id") or die "Couldn't prepare statement: " . $dbh->errstr ;
	my @data ;
 	$sth->execute() or die "Couldn't execute statement: " . $sth->errstr ;
	while (@data = $sth->fetchrow_array()) {		$user = $data[1] ;
		$properties{"user"} = $user ;
	}

	if ($sth->rows == 0) {
		print STDERR "ERROR: node $id not found in DB.\n\n" ;
	}
	$sth->finish ;

	my $sth2 = $dbh->prepare("SELECT * FROM relationtags WHERE id = $id") or die "Couldn't prepare statement: " . $dbh->errstr ;
	$sth2->execute() or die "Couldn't execute statement: " . $sth->errstr ;
	my @data2 ;
	while (@data2 = $sth2->fetchrow_array()) {
		my $k = $data2[1] ;
		my $v = $data2[2] ;
		push @relationTags, [$k, $v] ;
	}
	$sth2->finish ;

	my $sth3 = $dbh->prepare("SELECT * FROM relationmembers WHERE id = $id ORDER BY s") or die "Couldn't prepare statement: " . $dbh->errstr ;
	$sth3->execute() or die "Couldn't execute statement: " . $sth->errstr ;
	my @data3 ;
	while (@data3 = $sth3->fetchrow_array()) {
		my $type = $data3[2] ;
		my $memId = $data3[3] ;
		my $role = $data3[4] ;
		push @relationMembers, [$type, $memId, $role] ;
	}
	$sth3->finish ;

	$refTags = \@relationTags ;
	$refMembers = \@relationMembers ;
	$refProperties = \%properties ;
	return ($refProperties, $refMembers, $refTags) ;
}

# ----------------------------------------------------------------------------

sub dbConnect {
	$dbh = DBI->connect('DBI:mysql:test', 'root', '7636') or die ("error connecting DB: $DBI::errstr\n") ;
	print STDERR "successfully connected to DB $DBname\n" ;
}


sub dbDisconnect {
	$dbh->disconnect() ;
	print STDERR "DB $DBname disconnected\n" ;
}



# ----------------------------------------------------------------------------

sub initTableNodes {
	$dbh->do("DROP TABLE nodes") ;	
	$dbh->do("create table nodes (id BIGINT, lon DOUBLE, lat DOUBLE, user VARCHAR(50) )") ;	
	$dbh->do("CREATE UNIQUE INDEX i_nodeids ON nodes (id)") ;	

	$dbh->do("DROP TABLE nodetags") ;	
	$dbh->do("create table nodetags (id BIGINT, k VARCHAR(50), v VARCHAR(256))") ;	
	$dbh->do("CREATE INDEX i_nodeids2 ON nodetags (id)") ;	
}

sub initTableWays {
	$dbh->do("DROP TABLE ways") ;	
	$dbh->do("create table ways (id BIGINT, user VARCHAR(50))") ;	
	$dbh->do("CREATE UNIQUE INDEX i_wayids ON ways (id)") ;	

	$dbh->do("DROP TABLE waytags") ;	
	$dbh->do("create table waytags (id BIGINT, k VARCHAR(50), v VARCHAR(256))") ;	
	$dbh->do("CREATE INDEX i_wayids2 ON waytags (id)") ;	

	$dbh->do("DROP TABLE waynodes") ;	
	$dbh->do("create table waynodes (id BIGINT, s INT, nodeid BIGINT)") ;	
	$dbh->do("CREATE INDEX i_wayids3 ON waynodes (id)") ;	
}

sub initTableRelations {
	$dbh->do("DROP TABLE relations") ;	
	$dbh->do("create table relations (id BIGINT, user VARCHAR(50))") ;	
	$dbh->do("CREATE UNIQUE INDEX i_relationids ON relations (id)") ;	

	$dbh->do("DROP TABLE relationtags") ;	
	$dbh->do("create table relationtags (id BIGINT, k VARCHAR(50), v VARCHAR(256))") ;	
	$dbh->do("CREATE INDEX i_relationids2 ON relationtags (id)") ;	

	$dbh->do("DROP TABLE relationmembers") ;	
	$dbh->do("create table relationmembers (id BIGINT, s INT, type VARCHAR(20), memberid BIGINT, role VARCHAR(20))") ;	
	$dbh->do("CREATE INDEX i_relationids3 ON relationmembers (id)") ;	
}
1 ;
