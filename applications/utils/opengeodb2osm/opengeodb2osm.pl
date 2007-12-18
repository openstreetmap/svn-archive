#!/usr/bin/perl -w
#
# Author: Sven Anders <sven@anders-hamburg.de>
# License GPL 2.0
#
my $version="0.0.2";

use utf8;
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

print "<?xml version='1.0' encoding='UTF-8'?>
<osm version='0.5' generator='$progname'>\n";
if (-f "opengeodb2osmSettings.pm") {
    require opengeodb2osmSettings;
}

$osmOGDB{"400100000"}="opengeodb:is_in";
$osmOGDB{"400200000"}="opengeodb:layer";
$osmOGDB{"400300000"}="opengeodb:typ";
$osmOGDB{"500300000"}="opengeodb:postal_codes";
$osmOGDB{"500600000"}="opengeodb:community_identification_number";
$osmOGDB{"500100002"}="opengeodb:sort_name";
$osmOGDB{"500500000"}="opengeodb:car_code";
$osmOGDB{"500400000"}="opengeodb:telephone_area_code";
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

my $sthText = $dbh->prepare('select text_type,text_val,text_locale,is_native_lang,is_default_name from geodb_textdata where loc_id=?');
my $sthLoc = $dbh->prepare('select loc_type from geodb_locations where loc_id=?');
my $sthPop = $dbh->prepare('select int_val from geodb_intdata where loc_id=? and int_type=600700000');

my $sthParts = $dbh->prepare('select loc_id from geodb_textdata where text_type=400100000 and text_val=?');

# Vorbereitetes Statement (Abfrage) ausfuehren
$sth->execute ||
     die "Kann Abfrage nicht ausfuehren: $DBI::errstr\n";

$nodeid=-1;
while ( my @ergebnis = $sth->fetchrow_array() ){
    my $locid=$ergebnis[0];
    $locIdHash{$locid}=$nodeid;
    $nodeid--;
}


# Vorbereitetes Statement (Abfrage) ausfuehren
$sth->execute ||
     die "Kann Abfrage nicht ausfuehren: $DBI::errstr\n";


while ( my @ergebnis = $sth->fetchrow_array() ){
   # Im Array @ergebnis steht nun ein Datensatz
    my $locid=$ergebnis[0];
    print '<node id="'.$locIdHash{$locid}.'" visible="true" lat="' . $ergebnis[2] ."\" lon=\"". $ergebnis[1]."\" >\n";
    
    $tag=' <tag k="openGeoDB:loc_id" v="'.$locid.'" />'."\n";
    $sthText->execute($locid);
    $sthLoc->execute($locid);
    $sthPop->execute($locid);

    my %textval;
    while ( my @texterg = $sthText->fetchrow_array() ){
	my $key=$texterg[0];
	utf8::encode($texterg[1]);
	my $locale=$texterg[2];
	my $nativelang=$texterg[3];
	my $defaultname=$texterg[4];
	if (defined($osmOGDB{$key})) {
	    $key=$osmOGDB{$key};
	} else {
	    $key='opengeodb:'.$key;
	}
	if (defined($defaultname) and ($defaultname eq "0")) {
	    $key.=":".$locale;
	}
	if (defined $textval{"$key"}) {
	    $textval{$key}.=",".$texterg[1];
	} else {
	    $textval{$key}=$texterg[1];
	}

    }
    for my $key ( keys %textval ) {
        my $value = $textval{$key};
	$tag.=' <tag k="'.$key.'" v="'.$value.'" />'."\n";
	
    }
    my $population="";
    while ( my @poperg = $sthPop->fetchrow_array() ){
	$population=$poperg[0];
    }
    if ($population ne "") {
	$tag.=' <tag k="population" v="'.$population.'" />'."\n";
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

	my $typ=$textval{$osmOGDB{"400300000"}};
	if (!defined($typ)) {
	} elsif ($typ=~/Stadtteil/) {
	    $place="suburb";
	} elsif ($typ=~/stadt/i) {
	    if ($population eq "") {
		$place="town";
	    } elsif ($population>100000) {
		$place="city";
	    } else {
		$place="town";
	    }
	} elsif ($typ=~/[Gg]emeinde/i) {
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
	$tag.=' <tag k="place" v="'.$place.'" />'."\n";
    }
    $tag.=' <tag k="created_by" v="'.$progname.$version.'" />'."\n";
  
    print "$tag</node>\n";

    $found=0;
    $sthParts->execute($locid);
    while ( my @partserg = $sthParts->fetchrow_array() ){
	if (defined($locIdHash{$partserg[0]})) {
	    if ($found==0) {
		$nodeid--;
		print "<relation id=\"".$nodeid."\" visible='true'>\n";
		$found++;
		print $tag;
		print " <member type='node' ref=\"$locIdHash{$locid}\" role='this' />\n";
	    }
	    print " <member type='node' ref=\"$locIdHash{$partserg[0]}\" role='child' />\n";
	}
    }
    if ($found>0) {
	print "</relation>\n";
    }
}

# Datenbank-Verbindung beenden
$dbh->disconnect;
print "</osm>\n";
