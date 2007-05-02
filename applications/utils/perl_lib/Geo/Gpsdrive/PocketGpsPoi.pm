# Import Speedtrap Data into geoinfo.poi
#
# $Log$
# Revision 1.1  2005/08/15 13:54:22  tweety
# move scripts/POI --> scripts/Geo/Gpsdrive to reflect final Structure and make debugging easier
#
# Revision 1.4  2005/08/09 01:08:30  tweety
# Twist and bend in the Makefiles to install the DataDirectory more apropriate
# move the perl Functions to Geo::Gpsdrive in /usr/share/perl5/Geo/Gpsdrive/POI
# adapt icons.txt loading according to these directories
#
# Revision 1.3  2005/04/13 19:58:30  tweety
# renew indentation to 4 spaces + tabstop=8
#
# Revision 1.2  2005/04/10 00:15:58  tweety
# changed primary language for poi-type generation to english
# added translation for POI-types
# added some icons classifications to poi-types
# added LOG: Entry for CVS to some *.pm Files
#

package Geo::Gpsdrive::PocketGpsPoi;

use strict;
use warnings;

use IO::File;
use LWP::Debug qw(- -conns -trace);
use LWP::UserAgent;
use Geo::Gpsdrive::DBFuncs;
use Geo::Gpsdrive::Utils;

#############################################################################
# Args: 
#    $filename : Filename to read 
# RETURNS:
#    $waypoints : Hash of read Waypoints
#############################################################################
sub read_speedstrap_wp($$){
    my $full_filename = shift;
    my $type = shift; # Type of Speedtrap / Photo

    print "Reading: $full_filename\n";

    my $fh = IO::File->new("<$full_filename");
    $fh or die ("read_speedtrap_wp: Cannot open $full_filename:$!\n");

    my $waypoints;
    my $lines_count_file =0;
    while ( my $line = $fh->getline() ) {
	$lines_count_file ++;
	$line =~ s/[\t\r\n\s]*$//g;;
	# print "line: '$line'\n";
	if ($line =~ m/^Longitude,Latitude,Name/ ) {
	} elsif ( $line =~ m/^$/ ) {
	} elsif ( $line =~ m/^\-?\d+/ ) {
	    my @values = split(/\,/,$line);
	    #print Dumper(\@values);
	    my $values;
	    $values->{lat} = $values[0];
	    $values->{lon} = $values[1];
	    $values->{Name} = $values[2];
	    $values->{Name} =~ s/^\"//;
	    $values->{Name} =~ s/\"$//;

	    ############################################
	    # Set Default Proximity for speedtraps to 500m
	    $values->{'Proximity'} ||= "500 m";

	    $values->{Symbol} = "SPEEDTRAP-$type";

	    ############################################
	    correct_lat_lon($values);

	    #print Dumper($values) if defined $values->{'Proximity'};
	    my $wp_name = $values->{'Name'};
	    $waypoints->{$wp_name} = $values;
	} else {
	    print "Unknown Line: '$line'\n";
	}
	
    }
    return $waypoints;
}

########################################################################################
# Get and Unpack POCKETGPS_DIR
# http://www.pocketgpspoi.com
########################################################################################
sub import_Data(){
    print "=============================================================================\n";
    print "=============================================================================\n";
    print "=============================================================================\n";
    print "Pocketgps Not working yet.\n";
    print "Skipping \n";
    print "=============================================================================\n";
    print "=============================================================================\n";
    print "=============================================================================\n";
}
if ( 0 ) {
    my $POCKETGPS_DIR = "$main::CONFIG_DIR/MIRROR/POCKETGPS";

    unless ( -d $POCKETGPS_DIR ) {
	print "Creating Directory $POCKETGPS_DIR\n";
	mkpath $POCKETGPS_DIR
	    or die "Cannot create Directory $POCKETGPS_DIR:$!\n";
    }

    # download
    my $mirror = mirror_file("http://www.pocketgpspoi.com/downloads/pocketgps_uk_sc.zip",
			     "$POCKETGPS_DIR/pocketgps_uk_sc.zip");

    print "Mirror: $mirror\n";

    if ( $mirror ) {
	# Unpack it 
	`(cd $POCKETGPS_DIR/; unzip -o pocketgps_uk_sc.zip)`;

	for my $file_name ( glob("$POCKETGPS_DIR/pocketgps_*.csv") ) {
	    my ( $type ) = ($file_name =~ m/pocketgps_(.*)\.csv/);
	    my $out_file_name = "/home/gpsdrive/way_pocketgps_$type.txt";
	    my $waypoints = read_speedstrap_wp($file_name,$type);
	    write_gpsdrive_waypoints($waypoints,$out_file_name);
	    write_mapsource_waypoints($waypoints,"way_pocketgps_$type.txt");
	}
    }
}


1;
