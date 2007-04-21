#!/usr/bin/perl

my $VERSION ="dafif2osm.pl Copyright (c) Jon Burgess
Initial Version (April, 2007) by Jon Burgess <jburgess777 AT googlemail.com>
Version 0.02
";

BEGIN {
    my $dir = $0;
    $dir =~s,[^/]+/[^/]+$,,;
    unshift(@INC,"$dir/perl");
    unshift(@INC,"../../perl_lib");
    unshift(@INC,"~/svn.openstreetmap.org/utils/perl_lib");
    unshift(@INC,"$ENV{HOME}/svn.openstreetmap.org/utils/perl_lib");
}


use strict;
use warnings;

use File::Basename;
use File::Copy;
use File::Path;
use Getopt::Long;
use HTTP::Request;
use IO::File;
use Pod::Usage;
use Geo::OSM::Planet;
use Geo::OSM::Write;
use Utils::Debug;
use Utils::LWP::Utils;
use Utils::File;
use Data::Dumper;
use XML::Parser;

my ($man,$help);

my $OSM_NODES    = {};
my $OSM_SEGMENTS = {};
my $OSM_WAYS     = {};
my $OSM_OBJ      = undef; 

# ID used for all nodes (negative and decrements to be compatible with JOSM etc)
my $ID = -1000;

########################################################################
# Convert DAFIFT ARPT/ARPT airport list to OSM format
########################################################################

my($dafift_arpt_file) = shift(@ARGV);
my($dafift_rwy_file) = shift(@ARGV);
my($output_file) = shift(@ARGV);

die "Usage: $0 " .
    "<dafift_arpt_file> <dafift_rwy_file> <output_file>\n"
    if !defined($dafift_arpt_file) || !defined($dafift_rwy_file) || !defined($output_file);

my( %CODES );
my( %AIRPORTS );
my( %Elevations );
my( %Latitudes );
my( %Longitudes );
my( %RUNWAYS );


&load_dafift_airports( $dafift_arpt_file);
&build_airports();
&load_dafift_runways( $dafift_rwy_file);
&build_runways();
&write_result( $output_file );

exit;


########################################################################
# Process DAFIFT data
########################################################################

sub load_dafift_airports() {
    my( $arpt_file ) = shift;

    my( $id, $rwy, $type );

    # load airport file so we can lookup ICAO from internal ID

    open( ARPT, "<$arpt_file" ) || die "Cannot open DAFIFT: $arpt_file\n";

    <ARPT>;                          # skip header line

    while ( <ARPT> ) {
        chomp;
        my(@F) = split(/\t/);

	$id = $F[0];
	# Just UK airports for now...
	#next unless ( $id =~ m/^UK/ );

        my($icao) = $F[3];
        if ( length($icao) < 3 ) {
            if ( length( $F[4] ) >= 3 ) {
                $icao = $F[4];
            } else {
                #$icao = "[none]";
		next;
            }
        }
        $CODES{$id} = $icao;
	$AIRPORTS{$icao} = $F[1];
        #$Elevations{$icao} = $F[11];
	$Latitudes{$icao} = $F[8];
	$Longitudes{$icao} = $F[10];

	#print "$F[1]\t$F[0]\t$icao\t$F[8]\t$F[10]\n" ;
    }
}


sub build_airports() {
	my $cnt = 0;
	for my $icao ( keys %{AIRPORTS} ) {
		my $id = $ID--;
		$OSM_OBJ = {};
		$OSM_OBJ->{id} = $id;
		$OSM_OBJ->{lat} = $Latitudes{$icao};
		$OSM_OBJ->{lon} = $Longitudes{$icao};
		#$OSM_OBJ->{alt} = $Elevations{$icao};
		$OSM_OBJ->{tag}->{'name'} = $AIRPORTS{$icao};
		$OSM_OBJ->{tag}->{'aeroway'} = 'terminal'; # Is this correct?
		$OSM_NODES->{$id} = $OSM_OBJ;
		$cnt++;
	}
	print "Loaded $cnt airports\n";
}

sub load_dafift_runways() {
    my( $rwy_file ) = shift;

    my( $arpt_id, $rwy_id, $rwy, $type );

    # load runway file

    open( RWY, "<$rwy_file" ) || die "Cannot open DAFIFT: $rwy_file\n";

    <RWY>;                          # skip header line

    while ( <RWY> ) {
        chomp;
        my(@F) = split(/\t/);

	$arpt_id = $F[0];
	$rwy_id  = $F[1];
	# Just UK airports for now...
	#next unless ( $arpt_id =~ m/^UK/ );

	if (!exists $CODES{$arpt_id}) {
		print "Missing airport code: $arpt_id\n";
		next;
	}

        my($icao) = $CODES{$arpt_id};
	$RUNWAYS{$icao}->{$rwy_id}->{HE}->{lat} = $F[10];
	$RUNWAYS{$icao}->{$rwy_id}->{HE}->{lon} = $F[12];
	$RUNWAYS{$icao}->{$rwy_id}->{LE}->{lat} = $F[27];
	$RUNWAYS{$icao}->{$rwy_id}->{LE}->{lon} = $F[29];

	#print "$F[1]\t$F[0]\t$icao\t$F[8]\t$F[10]\n" ;
    }
}

sub add_runway_node($$) {
	my $id = $ID--;
	$OSM_OBJ = {};
	$OSM_OBJ->{id} = $id;
	$OSM_OBJ->{lat} = shift;
	$OSM_OBJ->{lon} = shift;
	$OSM_NODES->{$id} = $OSM_OBJ;
	return $id;
}

sub add_runway_segment($$) {
	my $seg_id = $ID--;
	$OSM_OBJ = {};
	$OSM_OBJ->{id} = $seg_id;
	$OSM_OBJ->{from} = shift;
	$OSM_OBJ->{to} = shift;
	$OSM_SEGMENTS->{$seg_id} = $OSM_OBJ;
	return $seg_id;
}

sub add_runway_way($) {
	my $way_id = $ID--;
	$OSM_OBJ = {};
	$OSM_OBJ->{id} = $way_id;
    	push(@{$OSM_OBJ->{seg}},shift);
	$OSM_OBJ->{tag}->{'aeroway'} = 'runway'; 
	$OSM_WAYS->{$way_id} = $OSM_OBJ;
	return $way_id;
}

sub build_runways() {
	my $cnt;
	for my $icao ( keys %{AIRPORTS} ) {
		for my $rwy_id (keys %{$RUNWAYS{$icao}}) {
			my $le_lat = $RUNWAYS{$icao}->{$rwy_id}->{LE}->{lat};
			my $le_lon = $RUNWAYS{$icao}->{$rwy_id}->{LE}->{lon};
			my $he_lat = $RUNWAYS{$icao}->{$rwy_id}->{HE}->{lat};
			my $he_lon = $RUNWAYS{$icao}->{$rwy_id}->{HE}->{lon};

			next unless ($le_lat && $le_lon && $he_lat && $he_lon);		

			my $le_id = add_runway_node($le_lat, $le_lon);
			my $he_id = add_runway_node($he_lat, $he_lon);
						    
			my $seg_id = add_runway_segment($he_id, $le_id);
			my $way_id = add_runway_way($seg_id);
			$cnt++;
		}
	}
	print "Loaded $cnt runways\n";
}



########################################################################
# Write out the accumulated combined result
########################################################################

sub write_result() {
	my( $output ) = shift;
	
	my $OSM = {};
	$OSM->{tool}     = 'dafif2osm.pl';
	$OSM->{nodes}    = $OSM_NODES;
	$OSM->{segments} = $OSM_SEGMENTS;
	$OSM->{ways}     = $OSM_WAYS;
	
	# Make sure we can create the output file before we start processing data
	open(OUTFILE, ">$output") or die "Can’t write to $output: $!";
	close OUTFILE;
	
	write_osm_file($output, $OSM);
}




# convert a lon/lat coordinate in various formats to signed decimal

sub make_dcoord() {
    my($coord) = shift;
    my( $dir, $deg, $min, $sec );
    my( $value ) = 0.0;

    $coord = &strip_ws( $coord );

    if ( $coord =~ m/^[WE]/ ) {
        ( $dir, $deg, $min, $sec )
            = $coord =~ m/^([EW])(\d\d\d)(\d\d)(\d\d\d\d)/;
        $value = $deg + $min/60.0 + ($sec/100)/3600.0;
        if ( $dir eq "W" ) {
            $value = -$value;
        }
    } elsif ( $coord =~ m/^[NS]/ ) {
        ( $dir, $deg, $min, $sec )
            = $coord =~ m/^([NS])(\d\d)(\d\d)(\d\d\d\d)/;
        $value = $deg + $min/60.0 + ($sec/100)/3600.0;
        if ( $dir eq "S" ) {
            $value = -$value;
        }
    } elsif ( $coord =~ m/[EW]$/ ) {
        ($value, $dir) = $coord =~ m/([\d\s\.]+)([EW])/;
        if ( $dir eq "W" ) {
            $value = -$value;
        }
    } elsif ( $coord =~ m/[NS]$/ ) {
        ($value, $dir) = $coord =~ m/([\d\s\.]+)([NS])/;
        if ( $dir eq "S" ) {
            $value = -$value;
        }
    }
    # print "$dir $deg:$min:$sec = $value\n";
    return $value;
}


# strip white space off front and back of string

sub strip_ws() {
    my( $string ) = shift;
    $string =~ s/^\s+//;
    $string =~ s/\s+$//;
    return $string;
}


