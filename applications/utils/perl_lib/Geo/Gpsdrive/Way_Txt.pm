# Database Defaults for poi/streets Table for poi.pl
#
# $Log$
# Revision 1.2  2005/10/11 08:28:35  tweety
# gpsdrive:
# - add Tracks(MySql) displaying
# - reindent files modified
# - Fix setting of Color for Grid
# - poi Text is different in size depending on Number of POIs shown on
#   screen
#
# geoinfo:
#  - get Proxy settings from Environment
#  - create tracks Table in Database and fill it
#    this separates Street Data from Track Data
#  - make geoinfo.pl download also Opengeodb Version 2
#  - add some poi-types
#  - Split off Filling DB with example Data
#  - extract some more Funtionality to Procedures
#  - Add some Example POI for Kirchheim(Munich) Area
#  - Adjust some Output for what is done at the moment
#  - Add more delayed index generations 'disable/enable key'
#  - If LANG=*de_DE* then only impert europe with --all option
#  - WDB will import more than one country if you wish
#  - add more things to be done with the --all option
#
# Revision 1.1  2005/08/15 13:54:22  tweety
# move scripts/POI --> scripts/Geo/Gpsdrive to reflect final Structure and make debugging easier
#
# Revision 1.3  2005/08/09 01:08:30  tweety
# Twist and bend in the Makefiles to install the DataDirectory more apropriate
# move the perl Functions to Geo::Gpsdrive in /usr/share/perl5/Geo/Gpsdrive/POI
# adapt icons.txt loading according to these directories
#
# Revision 1.2  2005/05/14 21:21:23  tweety
# Update Index createion
# Update default Streets
# Eliminate some undefined Value
#
# Revision 1.1  2005/05/09 19:35:12  tweety
# Split Default Values into seperate File
# Add new Icon
#

package Geo::Gpsdrive::Way_Txt;

use strict;
use warnings;

use POSIX qw(strftime);
use Time::Local;
use DBI;
use Geo::Gpsdrive::Utils;
use Data::Dumper;
use IO::File;
use Geo::Gpsdrive::DBFuncs;

$|= 1;                          # Autoflush

BEGIN {
    use Exporter   ();
    our ($VERSION, @ISA, @EXPORT, @EXPORT_OK, %EXPORT_TAGS);

    # set the version for version checking
    $VERSION     = 1.00;
    # if using RCS/CVS, this may be preferred
    # $VERSION = sprintf "%d.%03d", q$Revision: 1190 $ =~ /(\d+)/g;

    @ISA         = qw(Exporter);
    @EXPORT = qw( );
    %EXPORT_TAGS = ( );
    @EXPORT_OK   = qw();

}
#our @EXPORT_OK;


# *****************************************************************************
sub import_Data(){
    
    print "\nimport Way.txt File(s)\n";
    Geo::Gpsdrive::DBFuncs::disable_keys('streets');


    # Import Points from way*.txt into DB

    my $type2poi_type = { 
	AIRPORT    => "transport.public.airport" ,
	BURGERKING => "food.restaurant.fastfood.burger king" ,
	CAFE       => "food.cafe" ,
	GASSTATION => "transport.car.gas station" ,
	GEOCACHE   => "recreation.geocache" ,
	GOLF       => "recreation.sports.golf place" ,
	HOTEL      => "accommodation" ,
	MCDONALDS  => "food.restaurant.fastfood.mc donalds" ,
	MONU       => "recreation.landmark" ,
	NIGHTCLUB  => "recreation.night club" ,
	SHOP       => "shopping" ,
	SPEEDTRAP  => "transport.traffic.speedtrap" ,
	WLAN       => "w-lan.open" ,
	'WLAN-WEP' => "w-lan.wep" ,
	'Automatic_key' => "unknown" ,
	'Reiten'   => 'recreation.sports.horse.riding',
	'FRITZ'    => 'custom.friends.home',
    };

    for my $k ( keys %{$type2poi_type} ) {
	if ( ! poi_type_name2id($type2poi_type->{$k}) ) {
	    print "Undefined Type for WP-translation $k => $type2poi_type->{$k}\n";
	} else {
	    #print " Type $k => $type2poi_type->{$k} => ".poi_type_name2id($type2poi_type->{$k})."\n";
	}
    }

    for my $file_name ( glob('~/.gpsdrive/way*.txt') ) {
	my ($file_type ) = ( $file_name =~ m/way[-_]?(.*)\.txt$/);
	my $default_poi_type = 'unknown';
	$default_poi_type = $file_type if poi_type_name2id($file_type);
	$default_poi_type = $type2poi_type->{$file_type} if poi_type_name2id($type2poi_type->{$file_type});
	print "Default Type: $default_poi_type\n" if $default_poi_type ne "unknown";
	next if $file_name =~ m,/way-SQLRESULT.txt$,;
	print "Reading $file_name\n";

	my $source_id = source_name2id("import way.txt");

	my $fh = IO::File->new($file_name);
	my $count =0;
	while ( my $line = $fh->getline() ) {
	    # Columns: (wayp + i)->name, slat, slong, typ, wlan, action, sqlnr, proximity
	    my @columns = ('')x10;
	    @columns = split(/\t/,$line);
	    unless (  $columns[0] && $columns[1] && $columns[2]){
		@columns = split(/\s+/,$line);
	    };
	    next unless $columns[0] && $columns[1] && $columns[2];
	    $columns[0] =~ s/'/\\'/g;
	    my $type = "unknown";
	    $type =  $columns[3];
	    $type ||= $default_poi_type;
	    $type = $default_poi_type if $type eq"-";
	    my $wep = 0;
	    $wep ='1' if $type eq "WLAN-WEP";
	    $type = "WLAN" if $columns[0] =~ m/\d\d\:\d\d\:\d\d\:\d\d\:\d\d\:\d\d/;

	    my $poi_type_id = poi_type_name2id($type2poi_type->{"$type"});
	    $poi_type_id ||= poi_type_name2id("$type");
	    $poi_type_id || printf "Unknown Waypoint Type '$type' in $line\n";
	    $poi_type_id ||= poi_type_name2id("import way.txt");

	    for my $t ( qw(waypoints poi)) {
		my $wp = { "waypoints.wep"      => 0 ,
			   "waypoints.nettype"  => '',
			   "waypoints.type"     => 'import_way.txt',
			   "poi.poi_type_id"    => $poi_type_id,
			   "poi.source_id"      => $source_id,
			   "poi.scale_min"      => 1,
			   "poi.scale_max"      => 100000,
			   "poi.last_modified"  => localtime(time()),
			   "$t.name"            => $columns[0],
			   "$t.lat"             => $columns[1],
			   "$t.lon"             => $columns[2],
			   "$t.proximity"       => $columns[7],
			   "$t.type"            => $type,
			   "$t.wep"             => $wep,
		       };
		
		Geo::Gpsdrive::DBFuncs::db_exec("DELETE FROM $t ".
			"WHERE $t.name = '$columns[0]' ".
			"AND   $t.lat  = '$columns[1]' ".
			"AND   $t.lon  = '$columns[2]' ");
		
		Geo::Gpsdrive::DBFuncs::insert_hash($t,	$wp );
	    }
	    $count++;
	}
	$fh->close();
	print "Inserted $count Entries from $file_name\n";
    }

    Geo::Gpsdrive::DBFuncs::enable_keys('streets');
    print "import Way.txt File(s) FINISHED\n";
}





1;
