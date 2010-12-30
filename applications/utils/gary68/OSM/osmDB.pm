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

# requires osmdb.ini file in program directory for credentials containing two information
# 
# user=NAME
# password=PASSWORD
#


# TODO
#
# db size
# 
#


package OSM::osmDB ;

my $DBuser = "" ;	# to be read from ini file
my $DBpassword = "" ;

my $tempDir = "/tmp" ;

use strict ;
use warnings ;

use DBI ;
use OSM::osm ;


use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

$VERSION = '1.0' ;

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw (		bulkLoad
	 		dbConnect
			dbDisconnect
			deleteDBNode
			deleteDBWay
			deleteDBRelation
			getDBNode
			getDBWay
			getDBWayNodesCoords
			getDBRelation
			getTag
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
	my ($aRef0, $aRef1) = @_ ;
	my %nodeProperties = %$aRef0 ;
	my @nodeTags = @$aRef1 ;

	my $id = $nodeProperties{'id'} ;
	my $lon = $nodeProperties{'lon'} ;
	my $lat = $nodeProperties{'lat'} ;
	my $user = $nodeProperties{'user'} ;
	my $version = $nodeProperties{'version'} ;
	my $timestamp = $nodeProperties{'timestamp'} ;
	my $uid = $nodeProperties{'uid'} ;
	my $changeset = $nodeProperties{'changeset'} ;

	$dbh->do("INSERT INTO nodes (id, lon, lat, user, version, timestamp, uid, changeset) VALUES ($id, $lon, $lat, '$user', $version, '$timestamp', $uid, $changeset)") ;

	my $nodeId = $nodeProperties{'id'} ;
	foreach my $t (@nodeTags) {
		my $k = $t->[0] ;
		my $v = $t->[1] ;
		if (length $k > $maxK) { $maxK = length $k ; }
		if (length $v > $maxV) { $maxV = length $v ; }
		$dbh->do("INSERT INTO nodetags (id, k, v) VALUES ($nodeId, '$k', '$v')") ;
	}
}

sub storeDBWay {
	my ($aRef0, $aRef1, $aRef2) = @_ ;
	my %wayProperties = %$aRef0 ;
	my @wayNodes = @$aRef1 ;
	my @wayTags = @$aRef2 ;

	my $id = $wayProperties{'id'} ;
	my $user = $wayProperties{'user'} ;
	my $version = $wayProperties{'version'} ;
	my $timestamp = $wayProperties{'timestamp'} ;
	my $uid = $wayProperties{'uid'} ;
	my $changeset = $wayProperties{'changeset'} ;

	$dbh->do("INSERT INTO ways (id, user, version, timestamp, uid, changeset) VALUES ($id, '$user', $version, '$timestamp', $uid, $changeset)") ;

	my $wayId = $wayProperties{'id'} ;
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
	my ($aRef0, $aRef1, $aRef2) = @_ ;
	my %relationProperties = %$aRef0 ;
	my @relationMembers = @$aRef1 ;
	my @relationTags = @$aRef2 ;

	my $id = $relationProperties{'id'} ;
	my $user = $relationProperties{'user'} ;
	my $version = $relationProperties{'version'} ;
	my $timestamp = $relationProperties{'timestamp'} ;
	my $uid = $relationProperties{'uid'} ;
	my $changeset = $relationProperties{'changeset'} ;
	$dbh->do("INSERT INTO relations (id, user, version, timestamp, uid, changeset) VALUES ($id, '$user', $version, '$timestamp', $uid, $changeset)") ;

	my $relationId = $relationProperties{'id'} ;
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


sub getTag {
	my ($type, $id, $key) = @_ ;
	my $tag = undef ;

	my $tableName = "" ;
	if ($type eq "node") { $tableName = "nodetags" ; }
	if ($type eq "way") { $tableName = "waytags" ; }
	if ($type eq "relation") { $tableName = "relationtags" ; }

	if ($tableName ne "") {
		my $sth = $dbh->prepare("SELECT v FROM $tableName WHERE id = $id AND k='$key'") or die "Couldn't prepare statement: " . $dbh->errstr ;
		my @data ;
	 	$sth->execute() or die "Couldn't execute statement: " . $sth->errstr ;
		while (@data = $sth->fetchrow_array()) {
			$tag = $data[0] ;
		}
		$sth->finish ;
	}
	return $tag ;	
}

sub getDBWayNodesCoords {
	my $wayId = shift ;
	my %lon = () ;
	my %lat = () ;

	my $sth = $dbh->prepare("SELECT nodes.id,lon,lat FROM waynodes, nodes WHERE waynodes.id=$wayId AND waynodes.nodeid=nodes.id") or die "Couldn't prepare statement: " . $dbh->errstr ;
	my @data ;
 	$sth->execute() or die "Couldn't execute statement: " . $sth->errstr ;
	while (@data = $sth->fetchrow_array()) {		my $id = $data[0] ;
		my $lo = $data[1] ;
		my $la = $data[2] ;
		$lon{$id} = $lo ;
		$lat{$id} = $la ;
	}

	return (\%lon, \%lat) ;
}

sub getDBNode {
	my $id = shift ;

	my $user = undef ;
	my $lon = undef ;
	my $lat = undef ;
	my $version = "0" ;
	my $timestamp = "" ;
	my $uid = 0 ;
	my $changeset = 0 ;
	my @nodeTags = () ;
	my $refTags = undef ;
	my %properties = () ;
	my $refProperties = undef ;

	my $sth = $dbh->prepare("SELECT * FROM nodes WHERE id = $id") or die "Couldn't prepare statement: " . $dbh->errstr ;
	my @data ;
 	$sth->execute() or die "Couldn't execute statement: " . $sth->errstr ;
	while (@data = $sth->fetchrow_array()) {
		$lon = $data[1] ;
		$lat = $data[2] ;
		$user = $data[3] ;
		$version = $data[4] ;
		$timestamp = $data[5] ;
		$uid = $data[6] ;
		$changeset = $data[7] ;
		$properties{"user"} = $user ;
		$properties{"lon"} = $lon ;
		$properties{"lat"} = $lat ;
		$properties{"version"} = $version ;
		$properties{"timestamp"} = $timestamp ;
		$properties{"uid"} = $uid ;
		$properties{"changeset"} = $changeset ;
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
	my $version = "0" ;
	my $timestamp = "" ;
	my $uid = 0 ;
	my $changeset = 0 ;
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
		$version = $data[2] ;
		$timestamp = $data[3] ;
		$uid = $data[4] ;
		$changeset = $data[5] ;
		$properties{"user"} = $user ;
		$properties{"version"} = $version ;
		$properties{"timestamp"} = $timestamp ;
		$properties{"uid"} = $uid ;
		$properties{"changeset"} = $changeset ;
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
	my $version = "0" ;
	my $timestamp = "" ;
	my $uid = 0 ;
	my $changeset = 0 ;
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
		$version = $data[2] ;
		$timestamp = $data[3] ;
		$uid = $data[4] ;
		$changeset = $data[5] ;
		$properties{"user"} = $user ;
		$properties{"version"} = $version ;
		$properties{"timestamp"} = $timestamp ;
		$properties{"uid"} = $uid ;
		$properties{"changeset"} = $changeset ;
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
	my $DBname = shift ;

	readINI() ;

	$dbh = DBI->connect("DBI:mysql:$DBname", $DBuser, $DBpassword) or die ("error connecting DB: $DBI::errstr\n") ;
	print STDERR "successfully connected to DB $DBname\n" ;
}


sub dbDisconnect {
	$dbh->disconnect() ;
	print STDERR "DB disconnected\n" ;
}



# ----------------------------------------------------------------------------

sub initTableNodes {
	$dbh->do("DROP TABLE nodes") ;	
	$dbh->do("create table nodes ( id BIGINT, lon DOUBLE, lat DOUBLE, user VARCHAR(50), version INTEGER, timestamp VARCHAR(20), uid INTEGER, changeset INTEGER )") ;	
	$dbh->do("CREATE UNIQUE INDEX i_nodeids ON nodes (id)") ;	

	$dbh->do("DROP TABLE nodetags") ;	
	$dbh->do("create table nodetags (id BIGINT, k VARCHAR(50), v VARCHAR(256))") ;	
	$dbh->do("CREATE INDEX i_nodeids2 ON nodetags (id)") ;	
	$dbh->do("CREATE INDEX i_nodekeys ON nodetags (k(12))") ;	
	$dbh->do("CREATE INDEX i_nodevalues ON nodetags (v(12))") ;	
}

sub initTableWays {
	$dbh->do("DROP TABLE ways") ;	
	$dbh->do("create table ways (id BIGINT, user VARCHAR(50), version INTEGER, timestamp VARCHAR(20), uid INTEGER, changeset INTEGER)") ;	
	$dbh->do("CREATE UNIQUE INDEX i_wayids ON ways (id)") ;	

	$dbh->do("DROP TABLE waytags") ;	
	$dbh->do("create table waytags (id BIGINT, k VARCHAR(50), v VARCHAR(256))") ;	
	$dbh->do("CREATE INDEX i_wayids2 ON waytags (id)") ;	
	$dbh->do("CREATE INDEX i_waykeys ON waytags (k(12))") ;	
	$dbh->do("CREATE INDEX i_wayvalues ON waytags (v(12))") ;	

	$dbh->do("DROP TABLE waynodes") ;	
	$dbh->do("create table waynodes (id BIGINT, s INT, nodeid BIGINT)") ;	
	$dbh->do("CREATE INDEX i_wayids3 ON waynodes (id)") ;	
}

sub initTableRelations {
	$dbh->do("DROP TABLE relations") ;	
	$dbh->do("create table relations (id BIGINT, user VARCHAR(50), version INTEGER, timestamp VARCHAR(20), uid INTEGER, changeset INTEGER)") ;	
	$dbh->do("CREATE UNIQUE INDEX i_relationids ON relations (id)") ;	

	$dbh->do("DROP TABLE relationtags") ;	
	$dbh->do("create table relationtags (id BIGINT, k VARCHAR(50), v VARCHAR(256))") ;	
	$dbh->do("CREATE INDEX i_relationids2 ON relationtags (id)") ;	
	$dbh->do("CREATE INDEX i_relationkeys ON relationtags (k(12))") ;	
	$dbh->do("CREATE INDEX i_relationvalues ON relationtags (v(12))") ;	

	$dbh->do("DROP TABLE relationmembers") ;	
	$dbh->do("create table relationmembers (id BIGINT, s INT, type VARCHAR(20), memberid BIGINT, role VARCHAR(20))") ;	
	$dbh->do("CREATE INDEX i_relationids3 ON relationmembers (id)") ;	
}

# --------------------------------------------------------------------------

sub readINI {
	my $file ;
	open ($file, "<", "osmdb.ini") or die ("ERROR: could not open ./osmdb.ini\n") ;
	my $line ;
	while ($line = <$file>) {
		my ($k, $v) = ( $line =~ /(.+)=(.+)/ ) ;
		if ((defined $k) and (defined $v)) {
			if ($k eq "password") { $DBpassword = $v ; } 
			if ($k eq "user") { $DBuser = $v ; } 
		}
	}
	close ($file) ;
}

# --------------------------------------------------------------------------

sub bulkLoad {
	my ($file, $dbName) = @_ ;
	my $nodeCount = 0 ;
	my $wayCount = 0 ;
	my $relationCount = 0 ;

	my $wayId ;
	my $wayId2 ;
	my $wayUser ;
	my @wayNodes ;
	my @wayTags ;
	my $nodeId ;
	my $nodeUser ;
	my $nodeLat ;
	my $nodeLon ;
	my @nodeTags ;
	my $relationId ;
	my $relationUser ;
	my @relationTags ;
	my @relationMembers ;
	my %nodeProperties ;
	my %wayProperties ;
	my %relationProperties ;
	my $aRef0 ;
	my $aRef1 ;
	my $aRef2 ;

	OSM::osm::openOsmFile ($file) ;

	print STDERR "INFO: processing nodes...\n" ;
	print STDERR "reading nodes...\n" ;

	open (my $nodesFile, ">", $tempDir . "/nodes.txt") ;
	open (my $nodetagsFile, ">", $tempDir . "/nodetags.txt") ;

	($aRef0, $aRef1) = OSM::osm::getNode3 () ;
	if (defined $aRef0) {
		@nodeTags = @$aRef1 ;
		%nodeProperties = %$aRef0 ;
	}

	while (defined $aRef0) {
		$nodeCount++ ;

		print $nodesFile "$nodeProperties{'id'}\t$nodeProperties{'lon'}\t$nodeProperties{'lat'}\t" ;
		print $nodesFile "$nodeProperties{'user'}\t$nodeProperties{'version'}\t$nodeProperties{'timestamp'}\t" ;
		print $nodesFile "$nodeProperties{'uid'}\t$nodeProperties{'changeset'}\n" ;

		my $nodeId = $nodeProperties{'id'} ;
		foreach my $t (@nodeTags) {
			my $k = $t->[0] ;
			my $v = $t->[1] ;
			print $nodetagsFile "$nodeId\t$k\t$v\n" ;
		}

		# next
		($aRef0, $aRef1) = OSM::osm::getNode3 () ;
		if (defined $aRef0) {
			@nodeTags = @$aRef1 ;
			%nodeProperties = %$aRef0 ;
		}
	}

	close ($nodesFile) ;
	close ($nodetagsFile) ;

	dbConnect ($dbName) ;
	initTableNodes() ;
	print STDERR "load nodes data...\n" ;
	$dbh->do("LOAD DATA LOCAL INFILE '$tempDir/nodes.txt' INTO TABLE nodes") ;
	print STDERR "load nodetags data...\n" ;
	$dbh->do("LOAD DATA LOCAL INFILE '$tempDir/nodetags.txt' INTO TABLE nodetags") ;
	dbDisconnect ($dbName) ;

	`rm $tempDir/nodes.txt` ;
	`rm $tempDir/nodetags.txt` ;

	print STDERR "INFO: $nodeCount nodes processed.\n" ;

	# -----------

	print STDERR "INFO: processing ways...\n" ;
	print STDERR "reading ways...\n" ;

	open (my $waysFile, ">", $tempDir . "/ways.txt") ;
	open (my $waytagsFile, ">", $tempDir . "/waytags.txt") ;
	open (my $waynodesFile, ">", $tempDir . "/waynodes.txt") ;

	($aRef0, $aRef1, $aRef2) = OSM::osm::getWay3 () ;
	if (defined $aRef0) {
		%wayProperties = %$aRef0 ;
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
	while (defined $aRef0) {	
		$wayCount++ ;

		print $waysFile "$wayProperties{'id'}\t" ;
		print $waysFile "$wayProperties{'user'}\t$wayProperties{'version'}\t$wayProperties{'timestamp'}\t" ;
		print $waysFile "$wayProperties{'uid'}\t$wayProperties{'changeset'}\n" ;

		my $wayId = $wayProperties{'id'} ;
		foreach my $t (@wayTags) {
			my $k = $t->[0] ;
			my $v = $t->[1] ;
			print $waytagsFile "$wayId\t$k\t$v\n" ;
		}

		my $i = 0 ;
		foreach my $n (@wayNodes) {
			print $waynodesFile "$wayId\t$i\t$n\n" ;
			$i++ ;
		}


		# next way
		($aRef0, $aRef1, $aRef2) = OSM::osm::getWay3 () ;
		if (defined $aRef0) {
			%wayProperties = %$aRef0 ;
			@wayNodes = @$aRef1 ;
			@wayTags = @$aRef2 ;
		}
	}

	close ($waysFile) ;
	close ($waytagsFile) ;
	close ($waynodesFile) ;

	dbConnect ($dbName) ;
	initTableWays() ;
	print STDERR "load ways data...\n" ;
	$dbh->do("LOAD DATA LOCAL INFILE '$tempDir/ways.txt' INTO TABLE ways") ;
	print STDERR "load waytags data...\n" ;
	$dbh->do("LOAD DATA LOCAL INFILE '$tempDir/waytags.txt' INTO TABLE waytags") ;
	print STDERR "load waynodes data...\n" ;
	$dbh->do("LOAD DATA LOCAL INFILE '$tempDir/waynodes.txt' INTO TABLE waynodes") ;
	dbDisconnect ($dbName) ;

	`rm $tempDir/ways.txt` ;
	`rm $tempDir/waytags.txt` ;
	`rm $tempDir/waynodes.txt` ;

	print STDERR "INFO: $wayCount ways processed.\n" ;

	# -----------

	print STDERR "INFO: processing relations...\n" ;
	print STDERR "reading relations...\n" ;

	open (my $relationsFile, ">", $tempDir . "/relations.txt") ;
	open (my $relationtagsFile, ">", $tempDir . "/relationtags.txt") ;
	open (my $relationmembersFile, ">", $tempDir . "/relationmembers.txt") ;

	($aRef0, $aRef1, $aRef2) = OSM::osm::getRelation3 () ;
	if (defined $aRef0) {
		%relationProperties = %$aRef0 ;
		@relationMembers = @$aRef1 ;
		@relationTags = @$aRef2 ;
	}

	while (defined $aRef0) {
		$relationCount++ ;
	
		print $relationsFile "$relationProperties{'id'}\t" ;
		print $relationsFile "$relationProperties{'user'}\t$relationProperties{'version'}\t$relationProperties{'timestamp'}\t" ;
		print $relationsFile "$relationProperties{'uid'}\t$relationProperties{'changeset'}\n" ;

		my $relationId = $relationProperties{'id'} ;
		foreach my $t (@relationTags) {
			my $k = $t->[0] ;
			my $v = $t->[1] ;
			print $relationtagsFile "$relationId\t$k\t$v\n" ;
		}

		my $n = 0 ;
		foreach my $m (@relationMembers) {
			my $t = $m->[0] ;
			my $i = $m->[1] ;
			my $r = $m->[2] ;
			print $relationmembersFile "$relationId\t$n\t$t\t$i\t$r\n" ;
			$n++ ;
		}

		#next
		($aRef0, $aRef1, $aRef2) = OSM::osm::getRelation3 () ;
		if (defined $aRef0) {
			%relationProperties = %$aRef0 ;
			@relationMembers = @$aRef1 ;
			@relationTags = @$aRef2 ;
		}
	}

	close ($relationsFile) ;
	close ($relationtagsFile) ;
	close ($relationmembersFile) ;

	dbConnect ($dbName) ;
	initTableRelations() ;
	print STDERR "load relations data...\n" ;
	$dbh->do("LOAD DATA LOCAL INFILE '$tempDir/relations.txt' INTO TABLE relations") ;
	print STDERR "load relationtags data...\n" ;
	$dbh->do("LOAD DATA LOCAL INFILE '$tempDir/relationtags.txt' INTO TABLE relationtags") ;
	print STDERR "load relationmembers data...\n" ;
	$dbh->do("LOAD DATA LOCAL INFILE '$tempDir/relationmembers.txt' INTO TABLE relationmembers") ;
	dbDisconnect ($dbName) ;

	`rm $tempDir/relations.txt` ;
	`rm $tempDir/relationtags.txt` ;
	`rm $tempDir/relationmembers.txt` ;

	print STDERR "INFO: $relationCount relations processed.\n" ;

	OSM::osm::closeOsmFile () ;

}

1 ;
