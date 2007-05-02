# Read waypoints from Mapsource Data exported as txt
# and import into geoinfo.poi
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
# Revision 1.2  2005/04/10 20:47:49  tweety
# added src/speech_out.h
# update configure and po Files
#

package Geo::Gpsdrive::mapsource;

use strict;
use warnings;

use IO::File;
use Geo::Gpsdrive::DBFuncs;
use Geo::Gpsdrive::Utils;

#############################################################################
# Args: 
#    $filename : Filename to read 
# RETURNS:
#    $waypoints : Has of read Waypoints
#############################################################################
sub read_mapsource_waypoints($){
    my $full_filename = shift;

    my $waypoints={};

    print "Reading: $full_filename\n";

    my $fh = IO::File->new("<$full_filename");
    $fh or die ("read_mapsource_waypoints: Cannot open $full_filename:$!\n");
    my @columns;

    my $lines_count_file =0;
    while ( my $line = $fh->getline() ) {
	$lines_count_file ++;
	$line =~ s/[\t\r\n\s]*$//g;;
	# print "line: '$line'\n";
	if ($line =~ m/^Grid\s+Breite\/L.nge / ) {
	} elsif ( $line =~ m/^$/ ) {
	} elsif ( $line =~ m/^Datum\s+WGS 84/ ) {
	} elsif ( $line =~ m/^Header/ ) {
	    @columns = split(/\s+/,$line);
	    #print Dumper(\@columns);
	} elsif ( $line =~ m/^Waypoint\s+/ ) {
	    die "Spalten nicht definiert" unless @columns;
	    
#	    print "WP: $line\n";
	    my @values = split(/\t/,$line);
	    #print Dumper(\@values);
	    my $values;
	    for my $i ( 0 .. scalar @columns -1 ) {
		$values->{$columns[$i]} = $values[$i];
	    }

	    ############################################
	    ############################################
	    # Set Default Proximity to 800m
	    $values->{'Proximity'} ||= "800 m";


	    $values->{Symbol} ||= "";

	    ( $values->{lat},$values->{lon}) = split(/\s+/,$values->{'Position'});

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

sub Geo::Gpsdrive::mapsource::import_DB
{
    my $waypoints = read_mapsource_waypoints($do_mapsource_points);
    db_add_waypoints($waypoints);
}

1;
