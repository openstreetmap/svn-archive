# Database Defaults for poi/streets Table for poi.pl
#
# $Log$
# Revision 1.2  2006/04/20 22:41:05  tweety
# make database name variable
# import osm POI too
# add colog_bg, width, width_bg to db layout
#
# Revision 1.1  2006/02/01 18:08:01  tweety
# 2 new features by  Stefan Wolf
#

package Geo::Gpsdrive::gettraffic;

use strict;
use warnings;

use DBI;
use Geo::Gpsdrive::DBFuncs;
use Geo::Gpsdrive::Utils;

use WWW::Mechanize;

sub gettraffic(){
    my $url ="http://gpsdrive.blue-stripes.de/verkehr.php";
    my $mech= WWW::Mechanize->new();
    $mech->agent_alias( 'Windows IE 6' );
    $mech->get($url);
    my $source =$mech->content();			#open website $url which create the verkehr.bz2
    $url ="http://gpsdrive.blue-stripes.de/verkehr.bz2";
    $mech->get($url);
    $source = $mech->content();				#download verker.bz2
    my $datei = "$ENV{'HOME'}/.gpsdrive/traffic.bz2";	#this shoud be better with Compress::Bzip2
    open(FILE,">$datei")
	|| die "Error: File not found\n";
    print FILE $source;
    close(FILE);

    # Entpacken und einlesen
    system("bzip2 -f -d $datei");
    $datei = "$ENV{'HOME'}/.gpsdrive/traffic";
    open(FILE,"<$datei")
	|| die "Error: File not found";	
    my @file = <FILE>;
    close(FILE);

    system("rm -f $datei");
    my $x =0;
    my @data;
    my $dbh = DBI->connect( "dbi:mysql:$main::GPSDRIVE_DB_NAME", $main::db_user, $main::db_password ) 
	|| die "Kann keine Verbindung zum MySQL-Server aufbauen: $DBI::errstr\n";

    #clear table
    my $query = "delete from traffic";
    $dbh->prepare($query)->execute;

    for my $line ( @file ){
	chomp($line);
	@data = split(";",$line);
	debug("insert $line");
	$query = "insert into traffic(status,street,descshort,desclong,future,time)".
	    "values('$data[0]','$data[1]','$data[2]','$data[3]','$data[4]','$data[5]')";
	$dbh->prepare($query)->execute;
    }
    return 1;
}
sub showtraffic(){
    #nur Autobahnen und Bundesstraßen raussuchen
    my $query = "select status,street,descshort,desclong from traffic where street like 'A%' or street like 'B%'";
    
    my $dbh = DBI->connect( "dbi:mysql:$main::GPSDRIVE_DB_NAME", $main::db_user, $main::db_password ) 
	|| die "Kann keine Verbindung zum MySQL-Server aufbauen: $DBI::errstr\n";
    my $sth = $dbh->prepare( $query );
    $sth->execute();
    my( $status, $street, $descshort, $desclong );
    $sth->bind_columns( undef, \$status, \$street, \$descshort, \$desclong );

    while( $sth->fetch() ) {
	if (!$status    ){$status = "Unknown"}
	elsif ($status == 1){$status = "Stau"}
	elsif ($status == 2){$status = "Bauarbeiten"}
	elsif ($status == 3){$status = "Gesperrt"}
	elsif ($status == 4){$status = "Achtung"}
	elsif ($status == 5){$status = "Aufgehoben"}
	elsif ($status == 6){$status = "Unbekannt"};
	printf "%-5s %-12s %s\n\t\t\t%s\n"
	    ,$street,$status,$descshort,$desclong;
    }
}
1;
