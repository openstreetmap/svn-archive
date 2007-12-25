#!/usr/bin/perl -w
#
# Author: Sven Anders <sven@anders-hamburg.de>
# License GPL 2.0
#
my $version="0.4.1";
my $dbversion="0.2.5a / 2007-10-04 / http://opengeodb.sourceforge.net/";

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

$osmOGDB{"400100000"}="openGeoDB:is_in";
$osmOGDB{"400200000"}="openGeoDB:layer";
$osmOGDB{"400300000"}="openGeoDB:type";
$osmOGDB{"500300000"}="openGeoDB:postal_codes";
$osmOGDB{"500600000"}="openGeoDB:community_identification_number";
$osmOGDB{"500100002"}="openGeoDB:sort_name";
$osmOGDB{"500500000"}="openGeoDB:license_plate_code";
$osmOGDB{"500400000"}="openGeoDB:telephone_area_code";

$osmOGDB{"500100000"}="name";

my %is_in;

my $dbh = DBI->connect( 'dbi:mysql:'.$mysqldb, $mysqluser, $mysqlpw) ||
     die "Kann keine Verbindung zum MySQL-Server aufbauen: $DBI::errstr\n";

# Befehl fuer Ausfuehrung vorbereiten. Referenz auf Statement
# Handle Objekt wird zurueckgeliefert

my $sthRel=$dbh->prepare( 'select distinct text_val from geodb_textdata where text_type="400100000" ') ||
     die "Kann Statement nicht vorbereiten: $DBI::errstr\n";
my $sth = $dbh->prepare( 'SELECT distinct loc_id FROM geodb_textdata') ||
     die "Kann Statement nicht vorbereiten: $DBI::errstr\n";

my $where="";
if ($uselat==1) {
	$where.="where lat>=$minlat and lat<=$maxlat and lon>=$minlon and lon<=$maxlon";
}





my $sthNode = $dbh->prepare( 'SELECT distinct loc_id FROM geodb_coordinates '.$where ) ||
     die "Kann Statement nicht vorbereiten: $DBI::errstr\n";

if ($where ne "") {
    $where.=" and loc_id=?";
} else {
    $where="where loc_id=?";
}

my $sthCoord = $dbh->prepare( 'SELECT lon,lat,valid_since,date_type_since,valid_until,date_type_until FROM geodb_coordinates '.$where ) ||
     die "Kann Statement nicht vorbereiten: $DBI::errstr\n";


my $sthText = $dbh->prepare('select text_type,text_val,text_locale,is_native_lang,is_default_name from geodb_textdata where loc_id=?');

my $sthLoc = $dbh->prepare('select loc_type from geodb_locations where loc_id=?');
my $sthPop = $dbh->prepare('select int_val from geodb_intdata where loc_id=? and int_type=600700000');

my $sthParts = $dbh->prepare('select loc_id from geodb_textdata where text_type=400100000 and text_val=?');

# Vorbereitetes Statement (Abfrage) ausfuehren
$sthNode->execute ||
     die "Kann Abfrage nicht ausfuehren: $DBI::errstr\n";

$nodeid=-1;
while ( my @ergebnis = $sthNode->fetchrow_array() ){
    my $locid=$ergebnis[0];
    $NodelocIdHash{$locid}=$nodeid;
    $nodeid--;
}
$sthRel->execute ||
     die "Kann Abfrage nicht ausfuehren: $DBI::errstr\n";

while ( my @ergebnis = $sthRel->fetchrow_array() ){
    my $locid=$ergebnis[0];
    $RelationlocIdHash{$locid}=$nodeid;
    $nodeid--;
}



# Vorbereitetes Statement (Abfrage) ausfuehren
$sth->execute ||
     die "Kann Abfrage nicht ausfuehren: $DBI::errstr\n";


while ( my @ergebnis = $sth->fetchrow_array() ){
   # Im Array @ergebnis steht nun ein Datensatz
    my $locid=$ergebnis[0];

    my $lat;
    my $lon;

    
    $tag=' <tag k="openGeoDB:loc_id" v="'.$locid.'" />'."\n";
    $sthCoord->execute($locid);
    while ( my @corderg = $sthCoord->fetchrow_array() ){
	$lat=$corderg[1];
	$lon=$corderg[0];
    }
    $sthText->execute($locid);
    $sthLoc->execute($locid);
    $sthPop->execute($locid);

    my %textval;
    while ( my @texterg = $sthText->fetchrow_array() ){
	my $key=$texterg[0];
	my $value=$texterg[1];
	utf8::encode($value);
	$value=~s/&/&amp;/g;
	$value=~s/\"/&quot;/g;
	my $locale=$texterg[2];
	my $nativelang=$texterg[3];
	my $defaultname=$texterg[4];
	if (defined($osmOGDB{$key})) {
	    $key=$osmOGDB{$key};
	} else {
	    $key='openGeoDB:'.$key;
	}
	if (defined($defaultname) and ($defaultname eq "0")) {
	    $key.=":".$locale;
	}
	if (defined $textval{"$key"}) {
	    $textval{$key}.=",".$value;
	} else {
	    $textval{$key}=$value;
	}

    }
    for my $key ( keys %textval ) {
        my $value = $textval{$key};
	$tag.=' <tag k="'.$key.'" v="'.$value.'" />'."\n";
	
    }
    if (defined($textval{"openGeoDB:is_in"})) {
#	print "ISIN: ".$textval{"openGeoDB:is_in"}."\n";
	my $isinval="";
	if (defined($is_in{$textval{"openGeoDB:is_in"}})) {
	    $isinval=$is_in{$textval{"openGeoDB:is_in"}};
	} else {
	    my $bla=$textval{"openGeoDB:is_in"};
	    my $ibla=$bla;
	    while ($bla > -1) {
		$sthText->execute($bla);
		$bla=-1;
		while ( my @texterg= $sthText->fetchrow_array() ) {
		    my $key=$texterg[0];
		    my $value=$texterg[1];
		    utf8::encode($value);
		    my $locale=$texterg[2];
		    my $nativelang=$texterg[3];
		    my $defaultname=$texterg[4];
		    if (defined($osmOGDB{$key})) {
			$key=$osmOGDB{$key};
		    } else {
			$key='openGeoDB:'.$key;
		    }
		    if (defined($defaultname) and ($defaultname eq "0")) {
			$key.=":".$locale;
		    }
		    if ($key eq "openGeoDB:is_in") {
			$bla=$value;
		    }
		    if ($key eq "name") {
			if ($isinval ne "") {
			    $isinval="$value,$isinval";
			} else {
			    $isinval=$value;
			}
		    }
		}
	    }
	    $is_in{$ibla}=$isinval;
	}
	if (defined($isinval)) {
	    $tag.=' <tag k="is_in" v="'. $isinval.'" />'."\n";
	} else {
	    die($locid);
	}
	if (defined($textval{'name'})) {
	    $is_in{$locid}=$isinval.",".$textval{'name'};
	}

    
    } else {
	if (defined($textval{'name'})) {
	    $is_in{$locid}=$textval{'name'};
	}
    }

    my $population="";
    while ( my @poperg = $sthPop->fetchrow_array() ){
	$population=$poperg[0];
    }
    if ($population ne "") {
	$tag.=' <tag k="population" v="'.$population.'" />'."\n";
	$tag.=' <tag k="openGeoDB:population" v="'.$population.'" />'."\n";
    }
    my $place="";
    my $geodbPlace="";
    # FIXME $place aus OSM holen
    if ($place eq "") {
	while ( my @locerg = $sthLoc->fetchrow_array() ){
	    my $id=$locerg[0];
	    if ($id == 100100000) {
		$place="continent";
		$geodbPlace="continent";
	    } elsif ($id == 100200000) {
		$place="country";
		$geodbPlace="country";
	    } elsif ($id == 100300000) {
		$place="state";
		$geodbPlace="state";
	    } elsif ($id == 100400000) {
		$place="county";
		$geodbPlace="county";
	    } elsif ($id == 100500000) {
		$place="region";
		$geodbPlace="region";
	    } elsif ($id == 100600000) {
		$geodbPlace="political_structure";
	    } elsif ($id == 100700000) {
		$geodbPlace="locality";
	    } elsif ($id == 100800000) {
		$geodbPlace="postal_code_area";
	    } else {
		$geodbPlace=$id;
	    }
	
	}
    }
    if ($geodbPlace ne "") {
	$tag.=' <tag k="openGeoDB:location" v="'.$geodbPlace.'" />'."\n";    
    }

    if ($place eq "") {

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
    } else {
	$tag.=' <tag k="place" v="FIXME" />'."\n";
    }

    
    $tag.=' <tag k="created_by" v="'.$progname.$version.'" />'."\n";
    $tag.=' <tag k="openGeoDB:version" v="'.$dbversion.'" />'."\n";
    $tag.=' <tag k="openGeoDB:auto_update" v="population,is_in" />'."\n";

    if (defined($lat)) {
	print '<node id="'.$NodelocIdHash{$locid}.'" visible="true" lat="'." $lat\" lon=\"$lon\" >\n$tag</node>\n";
    }
    $found=0;
    $sthParts->execute($locid);
    while ( my @partserg = $sthParts->fetchrow_array() ){
	if ((defined($NodelocIdHash{$partserg[0]})) or (defined($RelationlocIdHash{$partserg[0]}))) {
	    if ($found==0) {
		defined($RelationlocIdHash{$locid}) or die("RelationLocId $locid");
		print "<relation id=\"".$RelationlocIdHash{$locid}."\" visible='true'>\n";
		$found++;
		print $tag;
		if (defined($NodelocIdHash{$locid})) {
		    print " <member type='node' ref=\"$NodelocIdHash{$locid}\" role='this' />\n";
		}

	    }
	    if (defined($NodelocIdHash{$partserg[0]})) {
		print " <member type='node' ref=\"$NodelocIdHash{$partserg[0]}\" role='child' />\n";
	    }
	    if (defined($RelationlocIdHash{$partserg[0]})) {
		print " <member type='relation' ref=\"$RelationlocIdHash{$partserg[0]}\" role='child' />\n";
	    }
	}
    }
    if ($found>0) {
	print "</relation>\n";
    }
}

# Datenbank-Verbindung beenden
$dbh->disconnect;
print "</osm>\n";
