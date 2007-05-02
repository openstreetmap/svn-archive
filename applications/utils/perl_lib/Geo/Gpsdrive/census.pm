# Read Data from US Census Bureau and import into geoinfo DB
#
# $Log$
# Revision 1.1  2005/08/15 13:54:22  tweety
# move scripts/POI --> scripts/Geo/Gpsdrive to reflect final Structure and make debugging easier
#
# Revision 1.3  2005/08/09 01:08:30  tweety
# Twist and bend in the Makefiles to install the DataDirectory more apropriate
# move the perl Functions to Geo::Gpsdrive in /usr/share/perl5/Geo/Gpsdrive/POI
# adapt icons.txt loading according to these directories
#
# Revision 1.2  2005/04/10 00:15:58  tweety
# changed primary language for poi-type generation to english
# added translation for POI-types
# added some icons classifications to poi-types
# added LOG: Entry for CVS to some *.pm Files
#

package Geo::Gpsdrive::census;

use strict;
use warnings;

use IO::File;
use Geo::Gpsdrive::DBFuncs;
use Geo::Gpsdrive::Utils;

########################################################################################
# Get and Unpack Census Data
# http://www.census.gov/geo/cob/bdy/
########################################################################################
sub import_Data() {
    print "=============================================================================\n";
    print "=============================================================================\n";
    print "=============================================================================\n";
    print "Census Download Not working yet.\n";
    print "Skipping \n";
    print "=============================================================================\n";
    print "=============================================================================\n";
    print "=============================================================================\n";
}
if ( 0 ) {
    my $CENSUS_DIR = "$main::CONFIG_DIR/MIRROR/CENSUS";

    unless ( -d $CENSUS_DIR ) {
	print "Creating Directory $CENSUS_DIR\n";
	mkpath $CENSUS_DIR
	    or die "Cannot create Directory $CENSUS_DIR:$!\n";
    }

    `wget --mirror --directory-prefix= --no-host-directories --include-directories=geo/cob/bdy --force-directories --level=2 http://www.census.gov/geo/cob/bdy/ --accept=zip,html -nv -D$CENSUS_DIR`;

    # download
    for my $state ( qw(  aia/     an/      bg/      cc/      cd/      co/     
			 cs/      dv/      econ/    ir/      ma/      mcd/    
			 na/      ne/      ou/      pl/      pu/      rg/     
			 sb/      se/      sl/      sn/      ss/      st/     
			 su/      tb/      tr/      ts/      tt/      tz/     
			 ua/      vt/      zt/     
			 90_data/
			 scripts/
			 )) {
#	for my $type ( qw( 00ascii/	     00e00/	     00shp/   ) ) {
	my $mirror = mirror_file("http://www.census.gov/geo/cob/bdy/".
				 "$state/${state}00ascii/${state}99_d00ascii.zip",
				 "$CENSUS_DIR/${state}99_d00ascii.zip");
	
	print "Mirror: $mirror\n";
	if ( $mirror ) {
	    # Unpack it 
	    `(cd $CENSUS_DIR/; unzip -o xy.zip)`;

	    for my $file_name ( glob("$CENSUS_DIR/pocketgps_*.csv") ) {
		my ( $type ) = ($file_name =~ m/pocketgps_(.*)\.csv/);
		my $out_file_name = "/home/gpsdrive/way_pocketgps_$type.txt";
		my $waypoints = read_speedstrap_wp($file_name,$type);
		write_gpsdrive_waypoints($waypoints,$out_file_name);
		write_mapsource_waypoints($waypoints,"way_pocketgps_$type.txt");
	    }
	}
    }	
}



1;
