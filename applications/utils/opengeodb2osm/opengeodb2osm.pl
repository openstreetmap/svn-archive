#!/usr/bin/perl -w
#
# Author: Sven Anders <sven@anders-hamburg.de>
# License GPL 2.0
#
my $version="0.0.1";


use DBI;
my $progname="opengeodb2osm";
my %osmOGDB;
$uselat=0;
$minlat=53;
$maxlat=54;
$minlon=9;
$maxlon=11;

$mysqldb="opengeodb";
$mysqluser="root"; 

$mysqlpw="";

if (-f "opengeodb2osmSettings.pm") {
    require opengeodb2osmSettings;
}

$osmOGDB{"400100000"}="opengeodb:is_in";
$osmOGDB{"400200000"}="opengeodb:layer";
$osmOGDB{"400300000"}="opengeodb:Typ";
$osmOGDB{"500300000"}="opengeodb:postal_codes";
$osmOGDB{"500600000"}="opengeodb:Amtlicher-Gemeindeschl";
$osmOGDB{"500100002"}="opengeodb:sortName";
$osmOGDB{"500500000"}="opengeodb:CarCode";
$osmOGDB{"500400000"}="opengeodb:TelephoneCode";
$osmOGDB{"500100000"}="name";



my $dbh = DBI->connect( 'dbi:mysql:'.$mysqldb, $mysqluser, $mysqlpw) ||
     die "Kann keine Verbindung zum MySQL-Server aufbauen: $DBI::errstr\n";

# Befehl fuer Ausfuehrung vorbereiten. Referenz auf Statement
# Handle Objekt wird zurueckgeliefert
my $where="";
if ($uselat==1) {
	$where="where lat>=$minlat and lat<=$maxlat and lon>=$minlon and lon<=$maxlon";
}
my $sth = $dbh->prepare( 'SELECT loc_id,lon,lat,valid_since,date_type_since,valid_until,date_type_until FROM geodb_coordinates '.$where ) ||
     die "Kann Statement nicht vorbereiten: $DBI::errstr\n";

my $sthText = $dbh->prepare('select text_type,text_val from geodb_textdata where loc_id=?');
my $sthLoc = $dbh->prepare('select loc_type from geodb_locations where loc_id=?');
my $sthPop = $dbh->prepare('select int_val from geodb_intdata where loc_id=? and int_type=600700000');

# Vorbereitetes Statement (Abfrage) ausfuehren
$sth->execute ||
     die "Kann Abfrage nicht ausfuehren: $DBI::errstr\n";

$nodeid=-1;
while ( my @ergebnis = $sth->fetchrow_array() ){
   # Im Array @ergebnis steht nun ein Datensatz
    my $locid=$ergebnis[0];
    print '<node id="'.$nodeid.'" visible="true" lat="' . $ergebnis[2] ."\" lon=\"". $ergebnis[1]."\" >\n";
    print ' <tag k="openGeoDB:loc_id" v="'.$locid.'" />'."\n";
    $sthText->execute($locid);
    $sthLoc->execute($locid);
    $sthPop->execute($locid);
    my %textval;
    while ( my @texterg = $sthText->fetchrow_array() ){
	my $key=$texterg[0];
	if (defined $textval{"$texterg[0]"}) {
	    $textval{$texterg[0]}.=",".$texterg[1];
	} else {
	    $textval{$texterg[0]}=$texterg[1];
	}

    }
    for my $key ( keys %textval ) {
        my $value = $textval{$key};
	if (defined($osmOGDB{$key})) {
	    print ' <tag k="'.$osmOGDB{$key}.'" v="'.$value.'" />'."\n";
	} else {
	    print ' <tag k="opengeodb:'.$key.'" v="'.$value.'" />'."\n";
	}
    }
    my $population="";
    while ( my @poperg = $sthPop->fetchrow_array() ){
	$population=$poperg[0];
    }
    if ($population ne "") {
	print ' <tag k="population" v="'.$population.'" />'."\n";
    }
    my $place="";
    while ( my @locerg = $sthLoc->fetchrow_array() ){
	my $id=$locerg[0];
	if ($id == 100100000) {
	    $place="continent";
	} elsif ($id == 100200000) {
	    $place="country";
	} elsif ($id == 100300000) {
	    $place="state";
	} elsif ($id == 100400000) {
	    $place="county";
	} elsif ($id == 100500000) {
	    $place="region";
	} elsif ($id == 100600000) {
	    $place="opengeodb:political_structure";
	} elsif ($id == 100700000) {
	    $place="opengeodb:locality";
	} elsif ($id == 100800000) {
	    $place="opengeodb:postalCodeArea";
	}
	
    }
    if ($place =~ /^opengeodb:/) {
	my $typ=$textval{400300000};
	if (!defined($typ)) {
	} elsif ($typ=~/Stadtteil/) {
	    $place="suburb";
	} elsif ($typ=~/stadt/i) {
	    if ($population>100000) {
		$place="city";
	    } else {
		$place="town";
	    }
	} elsif ($typ=~/gemeinde/) {
	    $place="village";
	} elsif ($population eq "") {
	    # do nothing
	} elsif ($population>100000) {
	    $place="city";
	} elsif ($population>10000) {
	    $place="town";
	} elsif ($population<30) {
	    $place="hamlet";
	}
    }
    if ($place ne "") {
	print ' <tag k="place" v="'.$place.'" />'."\n";
    }
    print ' <tag k="created_by" v="'.$progname.$version.'" />'."\n";
    
    print "</node>\n";
    $nodeid--;
}

# Datenbank-Verbindung beenden
$dbh->disconnect;
