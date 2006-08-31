#!/usr/bin/perl
# This Script converts/filters GPS-Track-Data 
# Input is one of the folowing:
#   - Kismet-GPS File   *.gps 
#   - GpsDrive-Track    *.sav
#   - GPX File          *.gpx
#   - Garmin mps File   *.mps
#   - Garmin gdb File   *.gdb
#   - Netstumbler Files *.ns1
#
# Standars Filters:
#	- are points are inside [ -90.0  , -180  , 90.0    , 180   ], # World
#	- minimum good points          > 5 Points/Track
#	- distance between trackpoints < 2 Km
#	- speed between Trackpoints    < 200 Km/h
#       - the track point is near (<20m) any OSM-segment
#
# Output is:
#   - OSM File for josm *.osm ( http://openstreetmap.org/)
#   - GPX File for josm *.gpx
#   - 00_collection.gpx, 00_collection.osm (one File with all good tracks)
#   - 00_filter_areas.gpx with al the area filters 
#
# Joerg Ostertag <osm-filter.openstreetmap@ostertag.name>
# TODO:
#   - eliminate duplicate waypoints
#   - area filter for waypoints
#   - eliminate duplicate Tracks
#   - cut out part of tracks which cover the same road
#   - make limits (max_speed, max_line_dist, ...) configurable
#   - add config file 
#   - write more filters:
#      - eliminate duplicate tracksegments (driving a street up and down)
#      - elimiate trackpoints where the GPS was standing for a longer time at one point
#      - Filter to eliminate all waypoints

BEGIN {
    unshift(@INC,"../perl");
    unshift(@INC,"~/svn.openstreetmap.org/utils/perl");
    unshift(@INC,"$ENV{HOME}/svn.openstreetmap.org/utils/perl");
}


use strict;
use warnings;

use Data::Dumper;
use File::Basename;
use File::Copy;
use File::Path;
use Getopt::Long;
use HTTP::Request;
use IO::File;
use Pod::Usage;
use XML::Parser;

use Geo::Geometry;
use Utils::File;
use Utils::Math;
use Utils::Timing;
use Geo::OSM::SegmentList;

my ($man,$help);
our $debug =0;
our $verbose =0;
our $PROXY='';


my $osm_nodes        = {};
my $osm_nodes_duplicate   = {};
my $osm_ways          = {};
my $osm_stats         = {};
my $osm_obj           = undef; # OSM Object currently read

my $use_stdin         = 0;
my $use_stdout        = 0;
my $out_osm           = 0;
my $fake_gpx_date     = 0;
my $write_gpx_wpt     = 0;
my $out_raw_gpx       = 0;
my $split_tracks      = 0;
my @filter_area_files = ();
my $draw_filter_areas = 0;
my $use_reduce_filter = 0;
my $do_filter_clew   = 0;
my $do_check_against_osm = undef;
my $filter_duplicate_tracepoints = 0;
my $do_all_filters    = 0;
my $generate_ways     = 0;

my $first_id=10000;
my $next_osm_node_number    = $first_id;
my $osm_segment_number = $first_id;
my $osm_way_number     = $first_id;

my $FILTER_FILE = "$ENV{'HOME'}/.josm/filter.xml";


##################################################################
package OSM;
##################################################################
use Storable;

use Geo::Geometry;
use Utils::File;
use Utils::Math;
use Utils::Timing;

use Geo::Geometry;
use Utils::File;
use Utils::Math;
use Utils::Timing;

###########################################

my $read_osm_nodes;
my $read_osm_segments;

sub node_ {
    $osm_obj = undef;
}
sub node {
    my($p, $tag, %attrs) = @_;  
    my $id = delete $attrs{id};
    $osm_obj = {};
    $osm_obj->{id} = $id;

    $osm_obj->{lat} = delete $attrs{lat};
    $osm_obj->{lon} = delete $attrs{lon};

    delete $attrs{timestamp};
    delete $attrs{action};

    if ( keys %attrs ) {
	warn "node $id has extra attrs: ".Dumper(\%attrs);
    }

    $read_osm_nodes->{$id} = $osm_obj;
}

# --------------------------------------------
sub segment_ {
    $osm_obj = undef;
}
sub segment {
    my($p, $tag, %attrs) = @_;  
    my $id = delete $attrs{id};
    $osm_obj = {};
    $osm_obj->{id} = $id;

    $osm_obj->{from} = delete $attrs{from};
    $osm_obj->{to}   = delete $attrs{to};

    if ( keys %attrs ) {
	warn "segment $id has extra attrs: ".Dumper(\%attrs);
    }
    my @segment;
    my $dummy;
    my $node1 = $read_osm_nodes->{$osm_obj->{from}};
    my $node2 = $read_osm_nodes->{$osm_obj->{to}};
    ($segment[0],$segment[1],$segment[2],$segment[3]) =
	($node1->{lat},$node1->{lon},$node2->{lat},$node2->{lon});
    $segment[4] = angle_north_relative(
				    { lat => $segment[0] , lon => $segment[1] },
				    { lat => $segment[2] , lon => $segment[3] });
    #$segment[5] = $attrs{name} if $debug;
    push (@{$read_osm_segments},\@segment);
}
# --------------------------------------------
sub tag {
    my($p, $tag, %attrs) = @_;  
    #print "Tag - $tag: ".Dumper(\%attrs);
    my $k = delete $attrs{k};
    my $v = delete $attrs{v};

    return if $k eq "created_by";

    if ( keys %attrs ) {
	print "Unknown Tag value for ".Dumper($osm_obj)."Tags:".Dumper(\%attrs);
    }
    
    my $id = $osm_obj->{id};
    if ( defined( $osm_obj->{tag}->{$k} ) &&
	 $osm_obj->{tag}->{$k} ne $v
	 ) {
	if ( $debug >1 ) {
	    printf STDERR "Tag %8s already exists for obj tag '$osm_obj->{tag}->{$k}' ne '$v'\n",$k ;
	}
    }
    $osm_obj->{tag}->{$k} = $v;
    if ( $k eq "alt" ) {
	$osm_obj->{alt} = $v;
    }	    
}

# --------------------------------------------
sub read_osm_file($) { # Insert Segments from osm File
    my $file_name = shift;

    print("Reading OSM Segment from File $file_name\n") if $verbose || $debug;
    print "$file_name:	".(-s $file_name)." Bytes\n" if $debug;

    print STDERR "Parsing file: $file_name\n" if $debug;
    my $p = XML::Parser->new( Style => 'Subs' ,
			      ErrorContext => 10,
			      );
    
    my $fh = File::data_open($file_name);
    my $content = $p->parse($fh);
    if (not $p) {
	print STDERR "WARNING: Could not parse osm data from $file_name\n";
	return;
    }
    return($read_osm_segments);
}


# ------------------------------------------------------------------
# reduce osm Segments to only those insiide the bounding box
# This make comparison faster
sub reduce_osm_segments($$) {
    my $all_osm_segments = shift;
    my $bounds = shift;

    my $start_time=time();

    #printf STDERR "reduce_osm_segments(".Dumper(\$bounds).")\n" if $debug;

    my $osm_segments = [];
    my $count=0;
    my $all_count=0;
    for my $segment ( @{$all_osm_segments} ) {
	$all_count++;
	next unless $segment->[0] >= $bounds->{lat_min};
	next unless $segment->[0] <= $bounds->{lat_max};
	next unless $segment->[1] >= $bounds->{lon_min};
	next unless $segment->[1] <= $bounds->{lon_max};
	next unless $segment->[2] >= $bounds->{lat_min};
	next unless $segment->[2] <= $bounds->{lat_max};
	next unless $segment->[3] >= $bounds->{lon_min};
	next unless $segment->[3] <= $bounds->{lon_max};
	$count++;
	push(@{$osm_segments},$segment);
    }
    if ( $verbose > 3 || $debug > 3 ) {
	printf STDERR "		Reduced OSM Data to $count( $all_count) OSM-Segments ";
	print_time($start_time);
    }

    return $osm_segments;
}
# ------------------------------------------------------------------
# check if a nearby segment would fit
sub is_segment_of_list_nearby($$$){
    my $track        = shift; # Track to check
    my $track_pos    = shift; # Track position to check
    my $osm_segments = shift; # Segments to compare with 
    #                           List of \[$lat1,$lon1,$lat2,$lon2,$angle_north_relative]

    my $max_angle = 30;

    my $elem0=$track->[$track_pos-1];
    my $elem1=$track->[$track_pos];
    my $elem2=$track->[$track_pos+1];
    my $skip_point=0;
    my $min_dist = 40000;
    my $compare_dist = $elem1->{compare_dist};

    for my $segment_num ( 0 .. $#{@{$osm_segments}} ) {
	my $segment = $osm_segments->[$segment_num];
	# The line from or to the element has to be fairly parallel
	next unless
	    ( abs ($elem0->{angle_n_r} - $segment->[4])  < $max_angle) ||
	    ( abs ($elem1->{angle_n_r} - $segment->[4])  < $max_angle);
	#print STDERR "abs_angle: $angle_n_r2\n" if $debug;

	# Distance between line of segment($segment)  to trackpoint $elem1
	my $dist = 1000*distance_line_point_Km($segment->[0],$segment->[1],
							 $segment->[2],$segment->[3],
							 $elem1->{lat},$elem1->{lon}
							 );
	$min_dist = $dist if $dist < $min_dist;
	next if $dist > $compare_dist; # in m
	printf STDERR "Elem is %3.1f m away from line\n",$dist
	    if $debug >5;
	$skip_point++;
	last;
    }
    # printf STDERR "Min Dist: $min_dist Meter\n";
    return $skip_point;
}

# ------------------------------------------------------------------
# check if new trackpoints are on existing osm tracks
sub check_against_osm($$$){
    my $tracks       = shift; # reference to tracks list
    my $all_osm_segments = shift;
    my $config       = shift;

    my $filename=$tracks->{filename};
    if ( $out_raw_gpx && $debug >3 ){
	my $new_gpx_file = "$filename-raw-pre-osm.gpx";
	$new_gpx_file =~s/.gpx-raw-pre-osm.gpx/-raw-pre-osm.gpx/;
	GPX::write_gpx_file($tracks,$new_gpx_file);
    };

    my $dist_osm_track = $config->{dist} || 40;
    my $start_time=time();

    my $bounds = GPS::get_bounding_box($tracks);
    #printf STDERR "Track Bounds: ".Dumper(\$bounds);
    my $osm_segments = reduce_osm_segments($all_osm_segments,$bounds);

    my $new_tracks={ filename => $tracks->{filename},
		     tracks => [],
		     wpt => [],
		     };

    # Keep WPT
    for my $wpt ( @{$tracks->{wpt}} ) {
	next unless $wpt;
	push(@{$new_tracks->{wpt}},$wpt);
    }

    my $all_points = 0;
    my $new_points = 0;
    my $track_count=0;
    my $skiped_points=0;
    for my $track ( @{$tracks->{tracks}} ) {
	$track_count++;
	next if !$track;
	my $new_track=[];

	GPS::enrich_single_track($track);

	for my $track_pos ( 0 .. $#{@{$track}} ) {
	    $all_points++;

	    #print STDERR "Track: $track_count Element: $track_pos\n";
	    $track->[$track_pos]->{skip_point} =
		is_segment_of_list_nearby($track,$track_pos,$osm_segments);

	}

	# Copy only those with skip_point set to 1
	for my $track_pos ( 0 .. $#{@{$track}} ) {
	    my $elem0=$track->[$track_pos-1];
	    my $elem1=$track->[$track_pos];
	    my $elem2=$track->[$track_pos+1];
	    my $skip_point = $elem1->{skip_point};
	    # This should only skip the point if the one before and after are skiped too
	    # But currentls it's not working yet
	    $skip_point=0 if ( $track_pos > 0             ) && ( ! $elem0->{skip_point} );
	    $skip_point=0 if ( $track_pos < $#{@{$track}} ) && ( ! $elem2->{skip_point} );

	    if ( $skip_point ) {
		my $num_elem=scalar(@{$new_track});
		if ( $num_elem >2 ) {
		    push(@{$new_tracks->{tracks}},$new_track);
		}
		$new_track=[];
		$skiped_points++;
	    } else {
		push(@{$new_track},$elem1);
		$new_points++;
	    }
	}
	push(@{$new_tracks->{tracks}},$new_track);
    }

    if ( $debug || $verbose >1) {
	printf STDERR "		Eliminated  $skiped_points ($all_points) Points comparing to ".
	    (scalar @{$osm_segments})." OSM Segments\n";
    }

    GPS::print_count_data($new_tracks,"after filtering against existing OSM Data");
    print_time($start_time);
    return $new_tracks;
}


##################################################################
package Kismet;
##################################################################
use Date::Parse;
use Data::Dumper;

use Geo::Geometry;
use Utils::File;
use Utils::Math;
use Utils::Timing;

# -----------------------------------------------------------------------------
# Read GPS Data from Kismet File
sub read_gps_file($) { 
    my $filename = shift;

    my $start_time=time();

    my $data = {
	filename => $filename,
	tracks => [],
	wpt => [],
	};

    printf STDERR ("Reading $filename\n") if $verbose>1 || $debug;
    printf STDERR "$filename:	".(-s $filename)." Bytes\n" if $debug;

    print STDERR "Parsing file: $filename\n" if $debug;
    my $p = XML::Parser->new( Style => 'Objects' ,
			      );
    
    my $fh = File::data_open($filename);
    return $data unless $fh;
    my $content = [{Kids => []}];
    eval {
	$content = $p->parse($fh);
    };
    if ( $@ ) {
	warn "$@Error while parsing\n $filename\n";
	printf STDERR Dumper(\$content);
	#return $content->[0]->{Kids};
    }
    if (not $p) {
	print STDERR "WARNING: Could not parse osm data\n";
	return $data;
    }
    if ( $debug ) {
	printf STDERR "Read and parsed $filename";
	print_time($start_time);
    }
    my $track=[];
    for my $elem  ( @{$content->[0]->{Kids}} ) {
	next unless ref($elem) eq "Kismet::gps-point";
	next unless $elem->{'bssid'} eq 'GP:SD:TR:AC:KL:OG';
	delete $elem->{Kids};
	if ( defined($elem->{"time-sec"}) && defined($elem->{"time-usec"}) ) {
	    $elem->{time} = $elem->{"time-sec"}+($elem->{"time-usec"}/1000000);
	    #printf STDERR "$elem->{time} = $elem->{'time-sec'}  $elem->{'time-usec'}\n";
	}
	delete $elem->{'time-sec'};
	delete $elem->{'time-usec'};
	delete $elem->{'bssid'};
	delete $elem->{'signal'};
	delete $elem->{'quality'};
	delete $elem->{'noise'};
	$elem->{'speed'} = delete($elem->{'spd'}) * 1;
	if ( $debug > 10 ) {
	    printf STDERR "read element: ".Dumper(\$elem);
	}
	push(@{$track},$elem);
    };
    
#    printf STDERR Dumper(\$track);

    $data->{tracks}=[$track];
    return $data;
}


##################################################################
package Gpsbabel;
##################################################################
use IO::File;

use Geo::Geometry;
use Utils::File;
use Utils::Math;
use Utils::Timing;


# -----------------------------------------------------------------------------
# Read GPS Data with the help of gpsbabel converting a file to a GPX-File
sub read_file($$) { 
    my $filename     = shift;
    my $gpsbabel_type = shift;

    my $data = {
	filename => $filename,
	tracks => [],
	wpt => [],
	};

    printf STDERR ("Reading $filename\n") if $verbose>1 || $debug;
    printf STDERR "$filename:	".(-s $filename)." Bytes\n" if $debug;

    my $gpsbabel_call="gpsbabel  -i $gpsbabel_type -f '$filename' -o gpx -F - ";
    printf STDERR "calling gpsbabel:\n$gpsbabel_call\n " if $debug>3;
    my $fh = IO::File->new("$gpsbabel_call |");
    if ( !$fh )  {
	warn "Cannot Convert $filename as Type $gpsbabel_type\n";
	return $data;
    }
    $data = GPX::read_gpx_file($fh);
    $data->{filename} = $filename;
    return $data;
}

##################################################################
package NMEA;
##################################################################
use IO::File;
use Date::Parse;
use Data::Dumper;
use Date::Parse;
use Date::Manip;

use Geo::Geometry;
use Utils::File;
use Utils::Math;
use Utils::Timing;

# -----------------------------------------------------------------------------
# Read GPS Data from NMEA - File
sub read_file($) { 
    my $filename = shift;

    my $start_time=time();

    my $new_tracks={ 
	filename => $filename,
	tracks => [],
	wpt => []
	};
    printf STDERR ("Reading $filename\n") if $verbose || $debug;
    printf STDERR "$filename:	".(-s $filename)." Bytes\n" if $debug;

    my $fh = File::data_open($filename);
    return $new_tracks unless $fh;
    my $elem ={};
    my $last_date='';
    my $last_time=0;
    my $new_track=[];
    my ($sat,$pdop,$hdop,$vdop,$sat_count);
    my $sat_time = 0;
    my $dop_time = 0;
    while ( my $line = $fh->getline() ) {
	my ($dummy,$type,$time,$status,$lat,$lat_v,$lon,$lon_v,$speed,$alt);
	my ($date,$mag_variation,$checksumm,$quality,$alt_unit);
	$alt=0;
	chomp $line;
	$line =~ s/\*(\S+)\s*$//;
	$checksumm=$1;
	($type,$line) = split( /,/,$line,2);
	$type =~ s/^\s*\$GP//;
	printf STDERR "type $type, line: $line, checksumm:$checksumm\n"
	    if $debug>4;
	if ( $type eq "GGA" ) {
	    # GGA - Global Positioning System Fix Data
	    # Time, Position and fix related data fora GPS receiver.
	    #        1         2       3 4        5 6 7  8   9  10 |  12 13  14   15
	    #        |         |       | |        | | |  |   |   | |   | |   |    |
	    # $--GGA,hhmmss.ss,llll.ll,a,yyyyy.yy,a,x,xx,x.x,x.x,M,x.x,M,x.x,xxxx*hh<CR><LF>
	    #  1) Universal Time Coordinated (UTC)
	    #  2) Latitude
	    #  3) N or S (North or South)
	    #  4) Longitude
	    #  5) E or W (East or West)
	    #  6) GPS Quality Indicator, 0 - fix not available, 1 - GPS fix, 2 - Differential GPS fix
	    #  7) Number of satellites in view, 00 - 12
	    #  8) Horizontal Dilution of precision
	    #  9) Antenna Altitude above/below mean-sea-level (geoid) 
	    # 10) Units of antenna altitude, meters
	    # 11) Geoidal separation, the difference between the WGS-84 earth
	    #     ellipsoid and mean-sea-level (geoid), "-" means mean-sea-level below ellipsoid
	    # 12) Units of geoidal separation, meters
	    # 13) Age of differential GPS data, time in seconds since last SC104
	    #     type 1 or 9 update, null field when DGPS is not used
	    # 14) Differential reference station ID, 0000-1023
	    # 15) Checksum
	    ($time,$lat,$lat_v,$lon,$lon_v,$quality,$dummy,$dummy,$alt,$alt_unit,
	     $dummy,$dummy,$dummy)
		= split(/,/,$line);	    
	    #printf STDERR "(,$time,$status, la: $lat,$lat_v, lo: $lon,$lon_v, Q: $quality,,, Alt: $alt,$alt_unit,,,,)\n";

	} elsif ( $type eq "RMC" ) {
	    # RMC - Recommended Minimum Navigation Information
	    #        1         2 3       4 5        6 7   8   9    10  11|
	    #        |         | |       | |        | |   |   |    |   | |
	    # $--RMC,hhmmss.ss,A,llll.ll,a,yyyyy.yy,a,x.x,x.x,xxxx,x.x,a*hh<CR><LF>
	    #  1) UTC Time
	    #  2) Status, V = Navigation receiver warning
	    #  3) Latitude
	    #  4) N or S
	    #  5) Longitude
	    #  6) E or W
	    #  7) Speed over ground, knots
	    #  8) Track made good, degrees true
	    #  9) Date, ddmmyy
	    # 10) Magnetic Variation, degrees
	    # 11) E or W
	    # 12) Checksum
	    ($time,$status,$lat,$lat_v,$lon,$lon_v,$speed,$dummy,$date,$mag_variation)
		= split(/,/,$line);    
	} elsif ( $type eq "GSA" ) {
	    # GSA - GPS DOP and active satellites
	    #        1 2 3                        14 15  16  17  18
	    #        | | |                         |  |   |   |   |
	    # $--GSA,a,a,x,x,x,x,x,x,x,x,x,x,x,x,x,x,x.x,x.x,x.x*hh<CR><LF>
	    # Field Number: 
	    #  1) Selection mode
	    #  2) Mode
	    #  3) ID of 1st satellite used for fix
	    #  ...
	    #  14) ID of 12th satellite used for fix
	    #  15) PDOP in meters
	    #  16) HDOP in meters
	    #  17) VDOP in meters
	    #  18) checksum 
	    ($dummy,$dummy,$dummy,$dummy,$dummy,$dummy,$dummy,$dummy,$dummy,$dummy,
	     $dummy,$dummy,$dummy,$dummy,$pdop,$hdop,$vdop)
		= split(/,/,$line);
	    $hdop = undef unless $hdop =~ m/[\d\-\+]+/;
	    $vdop = undef unless $vdop =~ m/[\d\-\+]+/;
	    $pdop = undef unless $pdop =~ m/[\d\-\+]+/;
	    $dop_time=$last_time;
	    next;
	} elsif ( $type eq "GSV" ) {
	    # GSV - Satellites in view
	    #
	    #        1 2 3 4 5 6 7     n
	    #        | | | | | | |     |
	    # $--GSV,x,x,x,x,x,x,x,...*hh<CR><LF>
	    # Field Number: 
	    #  1) total number of messages
	    #  2) message number
	    #  3) satellites in view
	    #  4) satellite number
	    #  5) elevation in degrees
	    #  6) azimuth in degrees to true
	    #  7) SNR in dB
	    #  more satellite infos like 4)-7)
	    #  n) checksum
	    my ($msg_anz,$msg_no,$rest);
	    ($msg_anz,$msg_no,$sat_count,$rest) = split(/,/,$line,4);
	    $msg_anz = 20 if $msg_anz>20;
	    $sat={} if $msg_no == 1;
	    #printf STDERR "# of Messages: $msg_anz; rest: '$rest'\n";
	    while ( defined $rest && $rest =~ m/,/) {
		#printf STDERR "# $count: $rest\n";
		my ($sat_no,$sat_ele,$sat_azi,$sat_snr);
		($sat_no,$sat_ele,$sat_azi,$sat_snr,$rest) = split(/,/,$rest,5);
		#printf STDERR "($sat_no,$sat_ele,$sat_azi,$sat_snr)\n";
		last unless defined ($sat_no) && defined($sat_ele) && defined($sat_azi) && defined($sat_snr);
		$sat->{$sat_no}->{ele} = $sat_ele;
		$sat->{$sat_no}->{azi} = $sat_azi;
		$sat->{$sat_no}->{snr} = $sat_snr;
	    }
	    $sat_time = $last_time;
	    #printf STDERR Dumper(\$sat);
	    next;
	} else {
	    printf STDERR "Ignore Line $type: $line\n"
		if $debug>6;
	    next;
	};
	next unless ($lat ne "" )&& ($lon ne "");
	next if  ($lat eq "0000.0000" ) && ($lon eq "00000.0000");
	if ( $lat =~ m/(\d\d)(\d\d.\d+)/) {
	    $lat = $1 + $2/60;
	} else {
	    printf STDERR "Error in lat: $lat\n$line\n";
	}
	if ($lon =~ m/(\d+)(\d\d\.\d+)/){
	    $lon = $1 + $2/60;
	} else {
	    printf STDERR "Error in lon: $lon\n$line\n";
	}
	$lat = -$lat if $lat_v eq "S";
	$lon = -$lon if $lon_v eq "W";
	printf STDERR "type $type (time:$time	lat:$lat	lon:$lon	alt:$alt	speed:$speed)\n" 
	    if $debug>5;
	if ( ( abs($lat) < 0.001 ) && 
	     ( abs($lat) < 0.001 ) ) {
	    printf STDERR "too near to (0/0) : type $type (time:$time	lat:$lat	lon:$lon	alt:$alt	speed:$speed)\n";
	    next;
	};

	$time =~ s/^(..)(..)(..)/$1:$2:$3/;
	if ( defined($date)) {
	    $date =~ s/^(..)(..)(..)/20$3-$2-$1/;
	} else {
	    $date = $last_date;
	}
	$last_date=$date;
	$time = str2time("$date ${time}");
	$last_time = $time if $time;

	$elem->{lat} = $lat;
	$elem->{lon} = $lon;
	$elem->{alt} = $alt if defined $alt;
	$elem->{time} = $time if defined $time;
	$time ||=0;

	my $dop_time_diff = $time - $dop_time;
	#printf STDERR "time diff: %f\n ", $dop_time_diff;
	if ( $dop_time_diff < 10
	     && defined($hdop) 
	     && defined($vdop)
	     && defined($pdop)
	     ) {
	    $elem->{pdop} = $pdop if $pdop;
	    $elem->{hdop} = $hdop if $hdop;
	    $elem->{vdop} = $vdop if $vdop;
	    if ( $hdop > 30 or 
		 $vdop > 30 or 
		 $pdop > 30  ) {
		#printf STDERR Dumper(\$elem);
		next;
	    }
	}
	
	my $sat_time_diff = $time - $sat_time;
	#printf STDERR "time diff: %f\n ", $sat_time_diff;
	if ( $sat_time_diff < 10 ) {
	    $elem->{sat}  = $sat_count;
	    for my $sat_no ( keys %{$sat} ) {
		$elem->{"sat_${sat_no}_ele"} = $sat->{$sat_no}->{ele};
		$elem->{"sat_${sat_no}_azi"} = $sat->{$sat_no}->{azi};
		$elem->{"sat_${sat_no}_snr"} = $sat->{$sat_no}->{snr};
	    }
	}
	# More interesting Info might be:
	# <course>52.000000</course>
	# <ele>0.000000</ele>
	# <fix>2d</fix>
	# <fix>3d</fix>
	# <sat>4</sat>
	# <speed>0.000000</speed>
	# <time>2035-12-03T05:42:23Z</time>
	# <trkpt lat="48.177040000" lon="11.759786667">
	
	bless($elem,"NMEA::gps-point");
    	push(@{$new_track},$elem);
	$elem ={};
    }
    push(@{$new_tracks->{tracks}},$new_track);
    if ( $verbose >1 ) {
	printf STDERR "Read and parsed $filename";
	print_time($start_time);
    }

    return $new_tracks;
}

##################################################################
package GPX;
##################################################################
use Date::Parse;
use Data::Dumper;
use Date::Parse;
use Date::Manip;
use POSIX qw(strftime);

use Geo::Geometry;
use Utils::File;
use Utils::Math;
use Utils::Timing;

# -----------------------------------------------------------------------------
# Read GPS Data from GPX - File
sub read_gpx_file($;$) { 
    my $filename      = shift;
    my $real_filename = shift || $filename;

    my $start_time=time();
    my $fh;

    my $new_tracks={
	filename => $real_filename,
	tracks => [],
	wpt => []
	};

    $fh = File::data_open($filename);
    if ( ! ref($filename) =~ m/IO::File/ ) {
	print STDERR "Parsing file: $filename\n" if $debug;
    }
    return $new_tracks unless $fh;

    my $p = XML::Parser->new( Style => 'Objects' ,
			      );
    
    my $content = [{Kids => []}];
    eval {
	$content = $p->parse($fh);
    };
    if ( $@ ) {
	warn "$@Error while parsing\n $filename\n";
	#print Dumper(\$content);
	#return $content->[0]->{Kids};
    }
    if ( $content && (scalar(@{$content})>1) ) {
	die "More than one top level Section was read in $filename\n";
    }
    if (not $p) {
	print STDERR "WARNING: Could not parse osm data\n";
	return $new_tracks;
    }
    if ( $verbose >1 ) {
	printf STDERR "Read and parsed $filename";
	print_time($start_time);
    }

    #print Dumper(keys %{$content});
    #print Dumper(\$content);
    $content = $content->[0];
    $content = $content->{Kids};



    # Extract Waypoints
    for my $elem ( @{$content} ) {
	next unless ref($elem) eq "GPX::wpt";
	my $wpt_elem = $elem->{Kids};
	my $new_wpt={};
	$new_wpt->{lat} = $elem->{lat};
	$new_wpt->{lon} = $elem->{lon};
	for my $elem ( @{$wpt_elem} ) {
	    my $found=0;
	    for my $type ( qw ( name ele
				cmt desc
				sym pdop
				course  fix hdop sat speed time )) {
		if ( ref($elem) eq "GPX::$type" ){
		    $new_wpt->{$type} = $elem->{Kids}->[0]->{Text};
		    $found++;
		}
	    }
	    if ( $found ){
	    } elsif (ref($elem) eq 'GPX::Characters') {
	    } else {
		printf STDERR "unknown tag in Waypoint:".Dumper(\$elem);
	    }
	}
	#printf STDERR Dumper(\$new_wpt);
	push(@{$new_tracks->{wpt}},$new_wpt);
    }
    
    # Extract Tracks
    for my $elem ( @{$content} ) {
	next unless ref($elem) eq "GPX::trk";
	#	    GPX::trkseg
	$elem = $elem->{Kids};
	#printf STDERR Dumper(\$elem);
	my $new_track=[];
	for my $trk_elem ( @{$elem} ) {
	    next unless ref($trk_elem) eq "GPX::trkseg";
	    $trk_elem = $trk_elem->{Kids};
	    #printf STDERR Dumper(\$trk_elem);
	    for my $trk_pt ( @{$trk_elem} ) {
		next unless ref($trk_pt) eq "GPX::trkpt";
		#printf STDERR "Track Point:".Dumper(\$trk_pt);
		for my $trk_pt_kid ( @{$trk_pt->{Kids}} ) {
		    next if ref($trk_pt_kid) eq "GPX::Characters";
		    #printf STDERR "Track Point Kid:".Dumper(\$trk_pt_kid);
		    my $ref = ref($trk_pt_kid);
		    my ( $type ) = ($ref =~ m/GPX::(.*)/ );
		    $trk_pt->{$type} = $trk_pt_kid->{Kids}->[0]->{Text};
		}
		my $trk_time = $trk_pt->{time};
		if ( defined $trk_time ) {
		    #printf STDERR "trk_time $trk_time\n";
		    my $time = str2time( $trk_time);
		    my $ltime = localtime($time);
		    my ($year,$month) = split(/-/,$trk_time);
		    if ( $year < 1970 ) {
			warn "Ignoring Dataset because of Strange Date $trk_time ($ltime) in GPX File\n";
			next;
		    };
		    if ( $debug >= 11 ) {
			printf STDERR "time: $ltime  ".$trk_pt->{time}."\n\n";
		    }
		    $trk_pt->{time_string} = $trk_pt->{time};
		    $trk_pt->{time} = $time;
		}

		delete $trk_pt->{Kids};
		#printf STDERR "Final Track Point:".Dumper(\$trk_pt);
		push(@{$new_track},$trk_pt);
	    }
	}
	push(@{$new_tracks->{tracks}},$new_track);
    }

    #printf STDERR Dumper(\$new_tracks);
    return $new_tracks;
}

#------------------------------------------------------------------
sub write_gpx_file($$) { # Write an gpx File
    my $tracks = shift;
    my $filename = shift;

    my $start_time=time();

    printf STDERR ("Writing GPS File $filename\n") if $verbose >1 || $debug >1;

    my $fh;
    if ( $filename eq '-' ) {
	$fh = IO::File->new(">&STDOUT");
    } else {
	$fh = IO::File->new(">$filename");
    }
    print $fh "<?xml version=\"1.0\"?>\n";
    print $fh "<gpx \n";
    print $fh "    version=\"1.0\"\n";
    print $fh "    creator=\"osm-filter Converter\"\n";
    print $fh "    xmlns=\"http://www.ostertag.name\"\n";
    print $fh "    >\n";
    # <bounds minlat="47.855922617" minlon ="8.440864999" maxlat="48.424462667" maxlon="12.829756737" />
    # <time>2006-07-11T08:01:39Z</time>

    my $point_count=0;

    # write Waypoints
    if ( $write_gpx_wpt ) {
	for my $wpt ( @{$tracks->{wpt}} ) {
	    my $lat  = $wpt->{lat};
	    my $lon  = $wpt->{lon};
	    print $fh " <wpt lat=\"$lat\" lon=\"$lon\">\n";
	    #print $fh "     <name>$wpt->{name}</name>\n";
	    for my $type ( qw ( name ele
				cmt desc
				sym
				course  fix hdop sat speed time )) {
		my $value = $wpt->{$type};
		next if $fake_gpx_date && ($type eq "time");
		if( defined $value ) {
		    print $fh "     <$type>$value</$type>\n";
		}
	    };
	    print $fh " </wpt>\n";
	}
    }

    # write tracks
    my $fake_time=0;
    my $track_id=0;
    for my $track ( @{$tracks->{tracks}} ) {
	$track_id++;
	print $fh "\n";
	print $fh "<trk>\n";
	print $fh "   <name>$filename $track_id</name>\n";
	print $fh "   <number>$track_id</number>\n";
	print $fh "    <trkseg>\n";

	for my $elem ( @{$track} ) {
	    $point_count++;
	    my $lat  = $elem->{lat};
	    my $lon  = $elem->{lon};
	    if ( abs($lat) >90 || abs($lon) >180 ) {
		warn "write_gpx_track: Element ($lat/$lon) out of bound\n";
		next;
	    };
	    print $fh "     <trkpt lat=\"$lat\" lon=\"$lon\">\n";
	    if( defined $elem->{ele} ) {
		print $fh "       <ele>$elem->{ele}</ele>\n";
	    };
	    # --- time
	    if ( defined ( $elem->{time} ) ) {
		#print Dumper(\$elem);

		##################
		my ($time_sec,$time_usec)=( $elem->{time} =~ m/(\d+)(\.\d*)?/);
		if ( defined($time_usec) ) {
		    $time_usec =~ s/^\.//;
		}
		if ( $time_sec && $time_sec < 3600*30 ) {
		    $time_usec =~s/^\.//;
		    print "---------------- time_sec: $time_sec\n";
		}
		if ( $fake_gpx_date ) {
		    $fake_time += rand(10);
		    $time_sec = $fake_time;
		}
		my $time = strftime("%FT%H:%M:%SZ", localtime($time_sec));
		#UnixDate("epoch ".$time_sec,"%m/%d/%Y %H:%M:%S");
		$time .= ".$time_usec" if $time_usec && ! $fake_gpx_date;
		if ( $debug >20) {
		    printf STDERR "elem-time: $elem->{time} UnixDate: $time\n";
		}
		print $fh "       <time>".$time."</time>\n";
	    }
	    # --- other attributes
	    for my $type ( qw ( name ele
				cmt course  
				fix pdop hdop vdop sat
				speed  )) {
		next if $fake_gpx_date && ($type eq "time");
		my $value = $elem->{$type};
		if( defined $value ) {
		    print $fh "       <$type>$value</$type>\n";
		}
	    };
	    print $fh "     </trkpt>\n";
	}
	print $fh "    </trkseg>\n";
	print $fh "</trk>\n\n";
	
    }

    print $fh "</gpx>\n";
    $fh->close();

    printf STDERR "$filename:	 %5d Points in %d Tracks Wrote to GPX File",$point_count,$track_id;
    print_time($start_time);
}


##################################################################
package GPSDrive;
##################################################################
use Date::Parse;
use Data::Dumper;

use Geo::Geometry;
use Utils::File;
use Utils::Math;
use Utils::Timing;

# -----------------------------------------------------------------------------
# Read GPSDrive Track Data
sub read_gpsdrive_track_file($) { 
    my $filename = shift;

    my $start_time=time();

    my $new_tracks={
	filename => $filename,
	tracks => [],
	wpt => []
	};

    printf STDERR ("Reading $filename\n") if $verbose || $debug;
    printf STDERR "$filename:	".(-s $filename)." Bytes\n" if $debug;

    my $fh = File::data_open($filename);
    return $new_tracks  unless $fh;

    my $new_track = [];
    while ( my $line = $fh->getline() ) {
	chomp $line;
	#printf STDERR "$line\n";
	$line =~ s/^\s*//;
	#my ($lat,$lon,$alt,$time) = ($line =~ m/\s*([\+\-\d\.]+)\s+([\+\-\d\.]+)\s+([\+\-\d\.]+)\s+(.*)/);
	my ($lat,$lon,$alt,$time) = split(/\s+/,$line,4);
	printf STDERR "(lat: $lat,lon: $lon, alt:$alt, time: $time)\n" if $debug>1;
	if ( ($lat>1000) || ( $lon>1000 )) { # new track
	    if ( scalar(@{$new_track}) >0 ) {
		push(@{$new_tracks->{tracks}},$new_track);
	    }
	    $new_track = [];
	    next;
	}

	my $elem = {
	    lat => $lat, 
	    lon => $lon, 
	    alt => $alt, 
	    time => str2time($time),
	};
	push(@{$new_track},$elem);
	bless($elem,"GPSDrive::gps-point");
    }
    push(@{$new_tracks->{tracks}},$new_track);
    if ( $verbose >1 ) {
	printf STDERR "Read and parsed $filename";
	print_time($start_time);
    }

    return $new_tracks;
}


my $CONFIG_DIR          = "$ENV{'HOME'}/.gpsdrive"; # Should we allow config of this?
my $WAYPT_FILE          = "$CONFIG_DIR/way.txt";
######################################################################
my $waypoints={};
sub get_waypoint($) {
    my $waypoint_name = shift;
    
    # Lok it up if it's cached?
    if( defined ( $waypoints->{$waypoint_name} )){
	return @{$waypoints->{$waypoint_name}};
    }
    # If they give just a filename, we should assume they meant the CONFIG_DIR
    $WAYPT_FILE = "$CONFIG_DIR/$WAYPT_FILE" unless ($WAYPT_FILE =~ /\//);
    
    open(WAYPT,"$WAYPT_FILE") || die "ERROR: get_waypoint Can't open: $WAYPT_FILE: $!\n";
    my ($name,$lat,$lon, $typ,$wlan, $action, $sqlnr, $proximity);
    while (<WAYPT>) {
	chomp;
	next unless (/$waypoint_name/);
	($name,$lat,$lon, $typ, $wlan, $action, $sqlnr, $proximity) = split(/\s+/);
    }
    close(WAYPT);
    unless (($lat) && ($lon)) {
	printf STDERR "Unable to find waypoint '$waypoint_name' in '$WAYPT_FILE'\n";
	exit;
    }
    $waypoints->{$waypoint_name} = [$lat,$lon];
    return ($lat,$lon,$proximity/1000);
} #End get_waypoint

##################################################################
package GPS;
##################################################################
use Date::Parse;
use Data::Dumper;
use Math::Trig;

use Geo::Geometry;
use Utils::File;
use Utils::Math;
use Utils::Timing;

# ------------------------------------------------------------------
# Check if the point is in the area to currently evaluate
my $internal__filter_area_list;

# -------------------------------------------------
# Add these filters from source
sub add_internal_filter_areas(){
    push( @{$internal__filter_area_list},
	  (    
	       # Block a circle of <proximity> Km arround each point
	       #     { lat =>  48.175921 	,lon => 11.754312  ,proximity => .030 , block => 1 },
	       
	       # Allow Rules for square size areas
	       # Warning square areas are not tested yet

	       # min_lat,min_lon max_lat,max_lon,     (y,x)
	       #[ 48.0  , 11.6  , 48.4    , 12.0    ], # München
	       #[ 48.10  , 11.75  , 49.0    , 14.0    ], # Münchner-Osten-

	       # The rest of the World is blocked by default
	       [ -90.0  , -180  , 90.0    , 180   ], # World Allow 
	       
	       ));
}


# -------------------------------------------------
# Read XML Filter File from josm directory
sub read_filter_areas_xml($){
    my $filename = shift;

    my $start_time=time();

    my $new_tracks={
	filename => $filename,
	tracks => [],
	wpt => []
	};

    printf STDERR "Reading Filter File $filename\n" if $verbose || $debug;
    printf STDERR "$filename:	".(-s $filename)." Bytes\n" if $debug;

    print STDERR "Parsing file: $filename\n" if $debug;
    my $p = XML::Parser->new( Style => 'Objects' ,
			      );
    
    my $fh = File::data_open($filename);
    if ( ! $fh ) {
	warn "Could not open Filter File $filename\n";
	return;
    }
    my $content = [{Kids => []}];
    eval {
	$content = $p->parse($fh);
    };
    if ( $@ ) {
	warn "$@Error while parsing\n $filename\n";
	printf STDERR Dumper(\$content);
    }
    if (not $p) {
	print STDERR "WARNING: Could not parse filter data\n";
	return $new_tracks;
    }
    if ( $debug ) {
	printf STDERR "Read and parsed $filename";
	print_time($start_time);
    }
    for my $elem  ( @{$content->[0]->{Kids}} ) {
	next unless ref($elem) eq "GPS::node";
	for my $tag ( @{$elem->{Kids}} ) {
	    next unless defined $tag->{v};
	    $elem->{$tag->{k}}=$tag->{v};
	};
	delete $elem->{Kids};
	if ( defined $elem->{'of:active'} && !  $elem->{'of:active'} ) {
	    next;
	}
	my $block =  $elem->{'of:block'};
	$block = 1 unless defined $block;
	my $lat =  $elem->{lat};
	my $lon =  $elem->{lon};
	my $proximity = $elem->{'of:radius'}||10000;
	push( @{$internal__filter_area_list},
	  { lat => $lat,
	    lon => $lon,
	    proximity => $proximity,
	    block => $block, 
	}
	      );
    }
}

# -------------------------------------------------
# Add all gpsdrive waypoints from this File as a filter 
# where deny specifies that its a block filter
sub read_filter_areas($){
    my $filename = shift;

    printf STDERR "Read GpsDrive Filter File $filename\n"
	if ( $debug || $verbose );

    unless ( -s $filename ) {
	printf STDERR "Filter File $filename not found\n";
	return;
    };

    open(WAYPT,"$filename") || die "ERROR: get_waypoint Can't open: $filename: $!\n";
    my ($name,$lat,$lon, $typ,$wlan, $action, $sqlnr, $proximity);
    while (my $line = <WAYPT>) {
	chomp($line);
	($name,$lat,$lon, $typ, $wlan, $action, $sqlnr, $proximity) = split(/\s+/,$line);
	my $block=0;
	next unless $name;
	next unless $typ;
	next unless $typ =~ m/^filter\./;
	if ( $typ =~ m/deny/ ) {
	    $block = 1;
	} elsif ( $typ =~ m/allow/) {
	    $block = 0;
	} elsif ( $typ =~ m/none/) {
	    next;
	} else {
	    warn "WARNING !!! unknown Filter type $typ for WP $name\n";
	};
	next unless $name;
	push( @{$internal__filter_area_list},
	  { wp => $name, block => $block }
	      );
    }
    close(WAYPT);
}

# -------------------------------------------------
# Read in all specified Filter Areas
sub read_all_filter_areas(@){
    my @filter_area_files=@_;
    return unless  @filter_area_files;

    if ( $filter_area_files[0] eq ''  ) {
	shift ( @filter_area_files);
	push ( @filter_area_files, $WAYPT_FILE) if -s $WAYPT_FILE;
	push ( @filter_area_files, $FILTER_FILE) if -s $FILTER_FILE;
	push ( @filter_area_files, 'internal');
    };

    for my $file ( @filter_area_files ) {
	if ( $file =~ m/way[^\/]\.txt$/ ) {
	    read_filter_area();
	} elsif ( $file =~ m/\.xml$/ ) {
	    read_filter_areas_xml($file);
	} elsif ( $file =~ m/^internal$/ ) {
	    add_internal_filter_areas();
	}
	if ( $debug >30 ) {
	    print "internal__filter_area_list:".Dumper(\$internal__filter_area_list);
	}
    }
}

# -------------------------------------------------
# Check given Element against all defined area-filters
sub check_allowed_area($){
    my $elem = shift;
    
    return 1 unless @filter_area_files;
    
    for my $area ( @{$internal__filter_area_list} ) {
	if (ref($area) eq "HASH" ) {	    
	    if ( defined ( $area->{wp} ) ) { # Get from GPSDrive ~/.gpsdrive/way.txt Waypoints
		my $proximity;
		($area->{lat},$area->{lon},$proximity) = GPSDrive::get_waypoint($area->{wp});
		$area->{proximity} ||= $proximity;
		$area->{proximity} ||= 10;
	    }
	    
	    if ( distance_point_point_Km($area,$elem) < $area->{proximity} ) {
		printf STDERR "check_allowed_area(".$elem->{lat}.",".$elem->{lon}.
		    ") -> WP: $area->{wp} : block: $area->{block}\n"
		    if $debug > 30;
		return ! $area->{block};
	    }
	} else {
	    my ($min_lat,$min_lon, $max_lat,$max_lon ) = @{$area};
	    if ( $min_lat <= $elem->{lat} &&	 $max_lat >= $elem->{lat} &&
		 $min_lon <= $elem->{lon} &&	 $max_lon >= $elem->{lon} ) {
		if ( $debug >30) {
		    printf STDERR "Allow Square\n";
		}
		return 1;
	    }
	}
    }
    return 1;
}

# --------------------------------------------
# Return a tracklist whith a track for each area_filter
sub draw_filter_areas(){
    return [] unless $draw_filter_areas;
    my $new_tracks={
	filename => 'draw_filter_areas',
	tracks => [],
	wpt => []
	};
    for my $area ( @{$internal__filter_area_list} ) {
	my $new_track = [];
	if (ref($area) eq "HASH" ) {	    
	    if ( defined ( $area->{wp} ) ) { # Get from GPSDrive way.txt Waypoints
		my $proximity;
		($area->{lat},$area->{lon},$proximity) = GPSDrive::get_waypoint($area->{wp});

		$area->{proximity} ||= $proximity;
	    }
	    $area->{proximity} ||= 10000;

	    unless ( defined $area->{lat} && defined $area->{lon}) {
		warn "Waypoint $area->{wp} not found\n";
	    }
	    
	    my ($lat,$lon,$r) = ($area->{lat},$area->{lon},$area->{proximity}*360/40000);
	    for my $angel ( 0 .. 360 ) {
		my $elem;
		$elem->{lat} = $lat+sin($angel*2*pi/360)*$r;
		$elem->{lon} = $lon+cos($angel*2*pi/360)*$r;
		next unless ($elem->{lat} > -90) &&  ($elem->{lat} < 90);
		next unless ($elem->{lon} > -180) &&  ($elem->{lon} < 180);
		push(@{$new_track},$elem);
		if ( ! ( $angel % 10 ) ) {
		    my $dir = 1.3;
		    $dir = 0.7 if $area->{block};
		    my $lat1 = $lat+sin($angel*2*pi/360)*$r*$dir;
		    my $lon1 = $lon+cos($angel*2*pi/360)*$r*$dir;
		    next unless ($lat1 > -90) &&  ($lat1 < 90);
		    next unless ($lon1 > -180) &&  ($lon1 < 180);

		    push(@{$new_track},{lat=> $lat1,lon=>$lon1});
		    push(@{$new_track},$elem);	    
		}
	    }
	} else {
	    my ($min_lat,$min_lon, $max_lat,$max_lon ) = @{$area};
	    my $elem;
	    $elem->{lat} = $min_lat;
	    $elem->{lon} = $min_lon;    push(@{$new_track},$elem);
	    $elem->{lat} = $max_lat;    push(@{$new_track},$elem);
	    $elem->{lon} = $max_lon;    push(@{$new_track},$elem);
	    $elem->{lat} = $min_lat;    push(@{$new_track},$elem);
	    $elem->{lon} = $min_lon;    push(@{$new_track},$elem);
	}
	push(@{$new_tracks->{tracks}},$new_track);
    }
    return $new_tracks;
}


# ------------------------------------------------------------------
# Enrich Track Data by adding:;
#    Distance to next point
#    angle_n: Angle to next point compared to north
#    angle_n_r: Angle to next point compared to north ignoring direction
#    angle: Angle between previous segment and following segment
#    compare_dist: is pdop or any other usefull distance in
#                  meter we can later use for distance comparison
sub enrich_single_track($){
    my $track = shift;
    my $last_track_point = $#{@{$track}};
    my $compare_dist=30;
    for my $track_pos ( 0 ..  $last_track_point) {
	my $elem0=$track->[$track_pos-1];
	my $elem1=$track->[$track_pos];
	my $elem2=$track->[$track_pos+1];

	my $pdop = $elem1->{pdop};
	if ( defined ( $pdop ) &&  ($pdop >0) ) {
	    $compare_dist= $pdop;
	}
	$compare_dist=10 if $compare_dist <10;
	$elem1->{compare_dist} = $compare_dist;
	
	if ( $track_pos < $last_track_point ) {
	    $elem1->{angle_n}   = angle_north($elem1,$elem2);
	    $elem1->{angle_n_r} = angle_north_relative($elem1,$elem2);
	} else {
	    $elem1->{angle_n}   = -999999;
	    $elem1->{angle_n_r} = -999999;
	}
	if ( ($track_pos > 0) &&
	     ( $track_pos < $last_track_point ) ) {
	    $elem1->{angle} = 
		angle_north($elem0,$elem1)  -
		angle_north($elem1,$elem2);
	} else {
	    $elem1->{angle} =0;
	}
	# Distance between line of segment($segment)  to trackpoint $elem1
	$elem1->{dist} = 1000*distance_point_point_Km($elem1,$elem2);
    }
}


# ------------------------------------------------------------------
# Add things like speed, time, .. to the GPS Data
# and the split the tracks if we think it's necessary
sub split_tracks($$){
    my $tracks      = shift; # reference to tracks list
    my $config      = shift;

    my $start_time=time();

    my $filename     = $tracks->{filename};

    my $max_allowed_speed = $config->{max_speed} || 200;
    my $max_allowed_dist  = $config->{max_dist}  || 2;
    my $max_allowed_time  = $config->{max_time}  || 60;

    my $new_tracks={
	filename => $tracks->{filename},
	tracks => [],
	wpt => []
	};

    # Keep WPT
    for my $elem ( @{$tracks->{wpt}} ) {
	next unless $elem;
	push(@{$new_tracks->{wpt}},$elem);
    }

    my $deleted_points=0;

    my $track_number=0;
    for my $track ( @{$tracks->{tracks}} ) {
	my $prev_elem=0;
	my $min_dist=999999999999999;
	my $max_dist=0;
	$track_number++;
	next if !$track;
	GPS::enrich_single_track($track);
	my $new_track = [];
	for my $elem ( @{$track} ) {
	    unless ( defined($elem->{lat}) && defined($elem->{lon})){
		$deleted_points++;
		next;
	    }
	    if ( $elem->{fix} && $elem->{fix} eq "none" ) {
		$deleted_points++;
		next;
	    };		

	    $elem->{time} = 0 unless defined $elem->{time};

	    if (  $prev_elem ) {
		my $dist  = distance_point_point_Km($prev_elem,$elem);
		my $angle = angle_north($prev_elem,$elem);
		my ($d_lat,$d_lon) = distance_degree_point_point($prev_elem,$elem);
		$elem->{dist}=$dist;   # in Km
		$elem->{angle}=$angle; # in Degre
		if ($debug)  {
		    #$elem->{d_lat}=sprintf("%9f",$d_lat*100000);
		    #$elem->{d_lon}=sprintf("%9f",$d_lon*100000);
		}
		if ($dist) {
		    $min_dist=min($min_dist,$dist);
		    $max_dist=max($max_dist,$dist);
		}
		$elem->{time_diff} = $elem->{time} - $prev_elem->{time};
		# --------- Speed
		my $new_speed = 0;
		if ( $elem->{time_diff} ) {
		    $new_speed = $dist/$elem->{time_diff}*3600;
		    if ( defined ( $elem->{speed}) && $new_speed && $elem->{speed} ) {
			my $delta_speed = $elem->{speed} - $new_speed;
			if ( $debug && $delta_speed > 1 ) {
			    printf STDERR "Speed diff: old:$elem->{speed} - calc:$new_speed =  $delta_speed\n";
			}
		    } else {
			$elem->{speed} = sprintf("%5.4f",$new_speed);
		    }
		}

		my $split_track='';

		if ( $elem->{time_diff} > $max_allowed_time) { # ---- Check for Track Split: time diff
		    $split_track .= " Delta Time: $elem->{time_diff} sec. ";
		}

		if ( $elem->{dist} > $max_allowed_dist) {             # ---- Check for Track Split: 1 Km
		    $split_track .= sprintf(" Dist: %.3f Km ",$elem->{dist});
		}

		if ( $elem->{speed} && $elem->{speed} > $max_allowed_speed) { # ---- Check for Track Split: 200 Km/h
		    $split_track .= sprintf(" Speed: %.1f Km/h ",$elem->{speed});
		    if ( $debug >10) {
			printf STDERR "prev:".Dumper(\$prev_elem);
			printf STDERR "".Dumper(\$elem);
		    }
		}

		if (  $split_track ne '' ) {
		    my $num_elem=scalar(@{$new_track});
		    if ( $num_elem  > 1) {
			push(@{$new_tracks->{tracks}},$new_track);
			printf STDERR "--------------- Splitting" if $debug;
		    } else {
			printf STDERR "--------------- Dropping" if $debug;
			$deleted_points+=$num_elem;
		    }
		    if ( $debug ) {
			printf STDERR "\tTrack Part (%4d Points)\t$split_track\n",$num_elem;
		    }
		    $new_track=[];
		}
	    }
	    push(@{$new_track},$elem);
	    $prev_elem=$elem;
	}


	my $num_elm_in_track = scalar(@{$new_track})||0;
	if ( $num_elm_in_track > 3 ) {
	    push(@{$new_tracks->{tracks}},$new_track);
	} else {
	    $deleted_points += $num_elm_in_track;
	}
	
	if ( $debug>2 || $verbose >4 ) {
	    printf STDERR "Split Track $track_number from $filename\n";
	    printf STDERR "	Distance: %8.2f m .. %8.2f Km \n", $min_dist*1000,$max_dist;
	    printf STDERR "	Elements: ".(scalar(@{$track}))."\n",
	}
    }
    if ( $debug ) {
	printf STDERR "		Deleted Points: $deleted_points (because Elem/Track < 3 )\n"
	}
    #printf STDERR Dumper(\$new_tracks);
    GPS::print_count_data($new_tracks,"after enriching/splitting");
    print_time($start_time);
    return $new_tracks;
}

# ------------------------------------------------------------------
# count tracks and points
sub count_data($){
    my $tracks      = shift; # reference to tracks list

    my $start_time=time();

    my $count_tracks=0;
    my $count_points=0;

    for my $track ( @{$tracks->{tracks}} ) {
	next if !$track;
	for my $elem ( @{$track} ) {
	    $count_points++;
	}
	$count_tracks++;
    }

    my $used_time = time()-$start_time;
    if ( $debug>10 || ($used_time >5 )) {
	printf STDERR "Counted ( $count_tracks Tracks,$count_points Points)";
	print_time($start_time);
    }

    return ( $count_tracks,$count_points);
}

# ------------------------------------------------------------------
# Print Number of points/tracks with a comment
sub print_count_data($$){
#    my $filename = shift;
    my $tracks   = shift; # reference to tracks list
    my $comment  = shift;

    my $filename =     $tracks->{filename};


    my ($track_count,$point_count) = GPS::count_data($tracks);
    if ( $verbose || $debug) {
	printf STDERR "$filename:	%5d Points in %d Tracks $comment",$point_count,$track_count;
    }
}

# ------------------------------------------------------------------
# get bounding Box for Data
sub get_bounding_box($){
    my $tracks      = shift; # reference to tracks list

    my $start_time=time();

    my $lat_min =  90;
    my $lat_max = -90;
    my $lon_min =  180;
    my $lon_max = -180;

    for my $track ( @{$tracks->{tracks}} ) {
	next if !$track;
	for my $elem ( @{$track} ) {
	    $lat_min  = $elem->{lat}	    if $lat_min > $elem->{lat};
	    $lat_max  = $elem->{lat}	    if $lat_max < $elem->{lat};

	    $lon_min  = $elem->{lon}	    if $lon_min > $elem->{lon};
	    $lon_max  = $elem->{lon}	    if $lon_max < $elem->{lon};
	}
    }

    my $used_time = time()-$start_time;
    if ( $debug>10 || ($used_time >5 )) {
	printf STDERR "Bounds are ($lat_min,$lon_min) ($lat_max,$lon_max)";
	print_time($start_time);
    }

    return { lat_min => $lat_min,lon_min => $lon_min,
	     lat_max => $lat_max,lon_max => $lon_max };
}

# ------------------------------------------------------------------
# Filter tracks with points
# check_allowed_area($elem) tells if this element is added or not
sub filter_data_by_area($){
    my $tracks      = shift; # reference to tracks list

    return unless @filter_area_files;

    my $start_time=time();

    my $new_tracks={
	filename => $tracks->{filename},
	tracks => [],
	wpt => []
	};

    # Keep WPT
    for my $elem ( @{$tracks->{wpt}} ) {
	next unless $elem;
	# TODO: if (filter)
	push(@{$new_tracks->{wpt}},$elem);
    }

    my $good_points=0;
    my $deleted_points=0;
    my $good_tracks=0;
    for my $track ( @{$tracks->{tracks}} ) {
	my $new_track = [];
	for my $elem ( @{$track} ) {

	    my $skip_point =  ! check_allowed_area($elem);

	    if ( $skip_point ) {
		my $num_elem=scalar(@{$new_track});
		if ( $num_elem ) {
		    push(@{$new_tracks->{tracks}},$new_track);
		    $good_tracks++;
		}
		$new_track=[];
		$deleted_points++
		} else {
		    push(@{$new_track},$elem);
		    $good_points++;
		}
	}
	my $num_elem=scalar(@{$new_track});
	if ( $num_elem ) {
	    push(@{$new_tracks->{tracks}},$new_track);
	    $good_tracks++;
	}
    }
    printf STDERR "deleted_points:$deleted_points \n"	
	if $debug>10;

    GPS::print_count_data($new_tracks,"after Area Filters");
    print_time($start_time); 
    return $new_tracks;
}


# ------------------------------------------------------------------
# Filter tracks with points
# delete points which are 
# inside a straight line of the point before and after
sub filter_data_reduce_points($){
    my $tracks      = shift; # reference to tracks list

    return unless $use_reduce_filter;

    my $start_time=time();
    my $new_tracks={
	filename => $tracks->{filename},
	tracks => [],
	wpt => []
	};

    # Keep WPT
    for my $elem ( @{$tracks->{wpt}} ) {
	next unless $elem;
	push(@{$new_tracks->{wpt}},$elem);
    }

    my $good_points=0;
    my $deleted_points=0;
    my $good_tracks=0;
    for my $track ( @{$tracks->{tracks}} ) {
	my $new_track = [];
	my $last_angle         = 999999999;
	my $last_elem = undef;
	push(@{$new_track},$track->[0]);
	my $size = scalar(@{$track});
	for my $i ( 2 .. $size ) {
	    my $skip_point =  0;
	    my $elem0 = $new_track->[-1];
	    my $elem1 = $track->[$i-1];
	    my $elem2 = $track->[$i];
	    my $dist_0_2 = distance_point_point_Km($elem0,$elem2);

	    # Max Distance between 2 points in Track
	    if ( $dist_0_2 > .5 ) { # max .5 km distanz
		printf STDERR "Elem0 und Elem2 have $dist_0_2 Km Distance, which would be too much\n"
		    if $debug >10;
	    } else {
		# Distance between line of line(p0 and p2) to p1 
		my $dist = distance_line_point_Km($elem0->{lat},$elem0->{lon},
							    $elem2->{lat},$elem2->{lon},
							    $elem1->{lat},$elem1->{lon}
							    );
		$skip_point =  1 if $dist < 0.004; # 4 meter
		printf STDERR "Elem $i is $dist m away from line\n"
		    if $debug >10;
	    }

	    if ( $skip_point ) {
		$deleted_points++;
		printf STDERR "Delete Element $i\n"
		    if $debug >10;
	    } else {
		push(@{$new_track},$elem1);
		$good_points++;
	    }
	}
	push(@{$new_track},$track->[-1]);

	my $num_elem=scalar(@{$new_track});
	if ( $num_elem ) {
	    push(@{$new_tracks->{tracks}},$new_track);
	    $good_tracks++;
	}
    }
    GPS::print_count_data($new_tracks,"after Data Reduce Points deleted_points:$deleted_points ");
    print_time($start_time);
    return $new_tracks;
}

# ------------------------------------------------------------------
# add a list of tracks to another 
sub add_tracks($$){
    my $dst_tracks      = shift; # reference to tracks list
    my $src_tracks      = shift; # reference to tracks list

    $dst_tracks ||= { filename => '',
		      tracks => [],
		      wpt => [],
		      };
    $dst_tracks->{filename} .=",$src_tracks->{filename}";
    for my $elem ( @{$src_tracks->{wpt}} ) {
	next unless $elem;
	push(@{$dst_tracks->{wpt}},$elem);
    }
    for my $elem ( @{$src_tracks->{tracks}} ) {
	next unless $elem;
	push(@{$dst_tracks->{tracks}},$elem);
    }
}


# ------------------------------------------------------------------
# remove Waypoints which are duplicate
sub filter_duplicate_wpt($){
    my $tracks      = shift; # reference to tracks list

    my @new_wpt;
    my %wpt_by_name;
    sub compare_wpt($$){
	my $wpt1 = shift;
	my $wpt2 = shift;
	# later we do a full compare
	for my $type ( qw ( name lat lon ele )) {
	    return 0
		if $wpt1->{$type} && $wpt2->{$type} && $wpt1->{$type} ne $wpt2->{$type} ;
	}
	return 1;
    }

    for my $elem ( @{$tracks->{wpt}} ) {
	my $name = $elem->{name};
	#printf STDERR Dumper(\$elem);
	if ( defined($name) && $name ) {
	    if ( defined $wpt_by_name{$name}) {
		my $found =0;
		for my $wpt1( @{$wpt_by_name{$name}} ) {
		    if ( compare_wpt( $elem,$wpt1 ) ) {
			$found=1;
			last;
		    }
		}
		if ( $found ) {
		    printf STDERR "wpt($name) is duplicate ignoring\n"
			if $verbose >5 || $debug;
		    next;
		}
		push(@{$wpt_by_name{$name}}, $elem);
	    } else {
		$wpt_by_name{$name} = [ $elem ];
	    }
	    push(@new_wpt,$elem);
	} else {
	    warn "unnamed Waypoint".Dumper(\$elem)."\n" if $verbose || $debug;
	}
    }

    $tracks->{wpt}=\@new_wpt;
}


# ------------------------------------------------------------------
# check if new trackpoints are ner already existing ones

sub filter_duplicate_tracepoints($$$){
    my $tracks       = shift; # reference to tracks list
    my $checked_track_segments = shift;
    my $config       = shift;

    my $dist_old2track = $config->{dist} || 20;
    my $start_time=time();

    my $bounds = GPS::get_bounding_box($tracks);
    #printf STDERR "Track Bounds: ".Dumper(\$bounds);

    my $new_tracks={
	filename => $tracks->{filename},
	tracks => [],
	wpt => []
	};

    # Keep WPT
    for my $wpt ( @{$tracks->{wpt}} ) {
	next unless $wpt;
	push(@{$new_tracks->{wpt}},$wpt);
    }

    my $count_points = 0;
    my $new_points = 0;
    my $last_compare_dist=0;
    for my $track ( @{$tracks->{tracks}} ) {
	next if !$track;
	my $new_track=[];
	my $last_elem={};
	for my $elem ( @{$track} ) {
	    my $skip_point=0;
	    my $min_dist = 40000;
	    my $pdop = $elem->{pdop};
	    my $compare_dist=$dist_old2track;
	    if ( defined ( $pdop ) &&  ($pdop >0) ) {
		$compare_dist= $pdop;
	    }

	    if ( $last_compare_dist != $compare_dist ) {
		print STDERR "compare_dist: $compare_dist\n" 
		    if ($verbose > 1) || ($debug > 1 );
		$last_compare_dist = $compare_dist;
	    };

	    for my $segment ( @{$checked_track_segments} ) {
		#printf STDERR Dumper(\$segment);
		# Distance between line of segment($segment)  to trackpoint $elem
		my $dist = 1000*distance_line_point_Km($segment->[0],$segment->[1],
								 $segment->[2],$segment->[3],
								 $elem->{lat},$elem->{lon}
								 );
		$min_dist = $dist if $dist < $min_dist;
		next if $dist > $compare_dist; # in m
		printf STDERR "Elem is %3.1f m away from other Track line\n",$dist
		    if $debug >1;
		$count_points++;
		$skip_point++;
		last;
	    }
	    # printf STDERR "Min Dist: $min_dist Meter\n";

	    ################################
	    # Since this is currently only a dumpy all points are taken
	    #$skip_point=0;

	    
	    if ( $skip_point ) {
		my $num_elem=scalar(@{$new_track});
		if ( $num_elem >4 ) {
		    push(@{$new_tracks->{tracks}},$new_track);
		}
		$new_track=[];
		$count_points++;
	    } else {
		push(@{$new_track},$elem);
		$new_points++;
		if ( defined($last_elem) 
		     && defined($last_elem->{lat}) 
		     && defined($last_elem->{lon})) {
		    push(@{$checked_track_segments},
			 [$last_elem->{lat},$last_elem->{lon},$elem->{lat},$elem->{lon}]);
		}
	    }
	    $last_elem=$elem;
	}
	push(@{$new_tracks->{tracks}},$new_track);
    }

    if ( $debug || $verbose) {
	printf STDERR "Found $count_points Points already in other Tracks.".
	    "This leaves $new_points ";
	print_time($start_time);
    }

    return $new_tracks;
}




# ------------------------------------------------------------------
# scan over the elements and return the number of elements which 
# are inside a boundingbox with a specified length
sub find_points_in_bounding_box_for_track($$$){
    my $track     = shift; # reference to tracks list
    my $track_pos = shift; # First Trackpoint to look at
    my $max_dist  = shift; # Height/width in km for bounding box

    my $lat_min =  90;
    my $lat_max = -90;
    my $lon_min =  180;
    my $lon_max = -180;

    my $count=0;

    my $last_track_point = $#{@{$track}};
    for my $track_pos_test ( $track_pos .. $#{@{$track}} ) {
	my $elem  =$track->[$track_pos_test];
	$lat_min  = $elem->{lat}	    if $lat_min > $elem->{lat};
	$lat_max  = $elem->{lat}	    if $lat_max < $elem->{lat};
	
	$lon_min  = $elem->{lon}	    if $lon_min > $elem->{lon};
	$lon_max  = $elem->{lon}	    if $lon_max < $elem->{lon};
	
	my $dist = distance_point_point_Km(
						 { lat => $lat_min,lon => $lon_min},
						 { lat => $lat_max,lon => $lon_max} );
	last if $dist>$max_dist;
	$count++;
    }
    #print STDERR "Bbox($count)\n" if $debug>1;
    return $count;
}

# ------------------------------------------------------------------
# check if a the next n points are a GPS inacuracy clew
sub is_gps_clew($$){
    my $track        = shift;
    my $track_pos    = shift;

    my $max_angle = 90;
    my $skip_point=0;

    my $count_clew=0;
    # We work with a boundingbox too
    # we just have to return a number of segments which belong to the clew
    # This reduces the cost for this action dramatically
    my $bbox=find_points_in_bounding_box_for_track($track,$track_pos,0.040);
    for my $track_pos_test ( $track_pos .. min($track_pos+$bbox,$#{@{$track}}) ) {
	#print "track_pos_test($track_pos): $track_pos_test\n";
	my $elem0 = $track->[$track_pos_test];
	my $elem1 = $track->[$track_pos_test+1];
	last if	$elem0->{dist} > $elem0->{compare_dist};
	next if abs ($elem0->{angle}) < 20;
	last if abs ($elem0->{angle})+abs($elem1->{angle})<40;
	$count_clew++;
    }

    #print "$count_clew\n" if $count_clew>1;
    return $count_clew if $count_clew>7;
    return 0 ;
}

# ------------------------------------------------------------------
# check if new trackpoints are on existing duplicate to other gpx tracks
sub filter_gps_clew($$){
    my $tracks       = shift; # reference to tracks list
    my $config       = shift;

    if ( $out_raw_gpx && $debug >3 ){
	my $filename=$tracks->{filename};
	my $new_gpx_file = "$filename-raw-pre-clew.gpx";
	$new_gpx_file =~s/.gpx-raw-pre-clew.gpx/-raw-pre-clew.gpx/;
	GPX::write_gpx_file($tracks,$new_gpx_file);
    };

    my $dist_osm_track = $config->{dist} || 40;
    my $start_time=time();

    my $new_tracks={
	filename => $tracks->{filename},
	tracks => [],
	wpt => []
	};

    # Keep WPT
    for my $wpt ( @{$tracks->{wpt}} ) {
	next unless $wpt;
	push(@{$new_tracks->{wpt}},$wpt);
    }

    my $all_points = 0;
    my $track_count=0;
    my $skiped_points=0;
    for my $track ( @{$tracks->{tracks}} ) {
	$track_count++;
	next if !$track;

	GPS::enrich_single_track($track);

	for  ( my $track_pos=0; $track_pos <= $#{@{$track}};$track_pos++ ) {
	    my $elem=$track->[$track_pos];
	    my $count_clew = is_gps_clew($track,$track_pos);
	    for ( 1 .. $count_clew){
		$track_pos++;
		last if $track_pos > $#{@{$track}};
		$track->[$track_pos]->{skip_point}= 1;
	    }
	}

	# Copy only those with skip_point set to 0
	my $new_track=[];
	for my $track_pos ( 0 .. $#{@{$track}} ) {
	    $all_points ++;
	    my $elem0=$track->[$track_pos-1];
	    my $elem1=$track->[$track_pos];
	    my $elem2=$track->[$track_pos+1];
	    my $skip_point = $elem1->{skip_point};
	    # This should only skip the point if the one before and after are skiped too
	    # But currentls it's not working yet
	    $skip_point=0 if ( $track_pos > 0             ) && ( ! $elem0->{skip_point} );
	    $skip_point=0 if ( $track_pos < $#{@{$track}} ) && ( ! $elem2->{skip_point} );

	    if ( $skip_point ) {
		my $num_elem=scalar(@{$new_track});
		if ( $num_elem >2 ) {
		    push(@{$new_tracks->{tracks}},$new_track);
		}
		$new_track=[];
		$skiped_points++;
	    } else {
		push(@{$new_track},$elem1);
	    }	}
	push(@{$new_tracks->{tracks}},$new_track);
    }

    if ( $debug || $verbose >1) {
	printf STDERR "		Eliminated $skiped_points ($all_points) Points looking for clews\n";
    }

    GPS::print_count_data($new_tracks,"after Clew Filtering");
    print_time($start_time);
    return $new_tracks;
}



# ------------------------------------------------------------------



##################################################################
package OSM;
##################################################################

use Data::Dumper;

use Geo::Geometry;
use Utils::File;
use Utils::Math;
use Utils::Timing;

#no warnings 'deprecated';

# ------------------------------------------------------------------
sub Tracks2osm($){
    my $tracks = shift;

    my $reference = $tracks->{filename};
    my $osm_segments     = {};
    my $osm_segments_duplicate ={};

    my $last_angle         = 999999999;
    my $angle;
    my $way={};
    my $angle_to_last;

    my $node_to   = 0;
    my $node_from = 0;

    my $element_count=0;
    my $count_valid_points=0;

    for my $track ( @{$tracks->{tracks}} ) {
	for my $elem ( @{$track} ) {
	    my $skip_point=0;
	    my $seg_id=0;
	    my $dist=999999999;

	    printf STDERR "lat or lon undefined : $elem->{lat},$elem->{lon} ".Dumper(\$elem)."\n" 
		unless  defined($elem->{lat}) && defined($elem->{lon}) ;


	    $skip_point =  ! GPS::check_allowed_area($elem);

	    #printf STDERR Dumper(\$elem)."\n" if $debug;
	    my $pos = "$elem->{lat},$elem->{lon}";
	    $next_osm_node_number++;
	    if ( 0 && $osm_nodes_duplicate->{$pos} ) {
		$node_to   = $osm_nodes_duplicate->{$pos};
		printf STDERR "Node would $next_osm_node_number pos:$pos already exists as $node_to\n"
		    if $verbose || $debug;
	    } else {
		if ( ! $skip_point ) {
		    $osm_nodes->{$next_osm_node_number}=$elem;
		    $osm_nodes_duplicate->{$pos} = $next_osm_node_number;
		}
		$node_to   = $next_osm_node_number;
	    }
	    
	    next unless $element_count++; # Beim ersten keine Segment bearbeitung

	    # -------------------------------------------- Create Segments
	    if ( $node_from && $node_to && ! $skip_point) {
		$dist = distance_point_point_Km($osm_nodes->{$node_from},$elem);
	    }
	    
	    if (  ! $skip_point ) {
		if ( $osm_nodes->{$node_from}->{dist} && 
		     $osm_nodes->{$node_from}->{dist} != $dist &&
		     $dist ) {
#	    $osm_nodes->{$node_from}->{dist} .= ", $dist";
		    $osm_nodes->{$node_from}->{dist} = $dist;
		} else {
		    $osm_nodes->{$node_from}->{dist} = $dist;
		}

		if ( $dist < .5 && 
		     ( ! $elem->{angle_to_last} || $elem->{angle_to_last} < 90 )
		     ){
		    $seg_id = $osm_segment_number++;
		    my $from_to = "$node_from,$node_to";
		    if ( $osm_segments_duplicate->{$from_to} ) {
			$seg_id = $osm_segments_duplicate->{$from_to};
			printf STDERR "Duplicate segment $osm_segment_number --> $seg_id\n";
		    } else {
			$osm_segments->{$seg_id} = { from => $node_from,
						     to   => $node_to
						     };
			if ( $debug >20 ) {
			    $osm_segments->{$seg_id}->{tag} ={ distance => $dist,
							       distance_meter => $dist*1000,
							       reference => $reference,
							       from_to => "$node_from $node_to",
							   };
			}
		    };
		    # Angle
		    $angle = angle_north($osm_nodes->{$node_from},$osm_nodes->{$node_to});
		}
	    }

	    if ( $seg_id &&	 ! $skip_point  ) {
		if ( $debug) {
		    #$osm_segments->{$seg_id}->{tag}->{time_diff} = $elem->{time_diff};
		    if ( defined ( $elem->{speed} ) ) {
			$osm_segments->{$seg_id}->{tag}->{speed} = $elem->{speed};
		    }
		    $osm_segments->{$seg_id}->{tag}->{angle} = $angle;
		    #$osm_segments->{$seg_id}->{tag}->{d_lat} = $elem->{d_lat};
		    #$osm_segments->{$seg_id}->{tag}->{d_lon} = $elem->{d_lon};
		}
		if ( defined ( $last_angle )) {
		    $angle_to_last = $angle - $last_angle;
		    $angle_to_last = - ( 360 - $angle_to_last) if $angle_to_last > 180;
		    if ( $debug) {
			#$osm_segments->{$seg_id}->{tag}->{angle_to_last} = $angle_to_last;
		    }
		} else {
		    $angle_to_last=0;
		}
	    } 
	    
	    if ( $skip_point  
		 || ! $seg_id      		 # Wir haben ein neues Segment
		 || abs($angle_to_last) > 25 # unter x Grad Lenkeinschlag
		 || $dist > .4  		 # unter x Km Distanz
		 ) {
		if ( defined($way->{seg}) 
		     && ( @{$way->{seg}} > 4)
		     ) {
		    $osm_way_number++;
		    $way->{reference} = $reference;
		    $osm_ways->{$osm_way_number} = $way;
		}
		$way={};
	    }
	    if ( ! $skip_point  ){
		push(@{$way->{seg}},$seg_id);
		$count_valid_points++;
	    }
	    $node_from=$node_to;
	    $last_angle = $angle;
	}
    }
    return $osm_segments;
}

# ------------------------------------------------------------------
sub tags2osm($){
    my $obj = shift;
    
    my $erg = '';
    for my $k ( keys %{$obj->{tag}} ) {
	my $v = $obj->{tag}{$k};
	if ( ! defined $v ) {
	    warn "incomplete Object: ".Dumper($obj);
	}
	#next unless defined $v;
	$erg .= "	 <tag k=\"$k\" v=\"$v\"/>\n";
    }
    return $erg;
}

sub write_osm_file($$) { # Write an osm File
    my $filename = shift;
    my $osm_segments = shift;

    my $start_time=time();

    printf STDERR ("Writing OSM File $filename\n") if $verbose >1 || $debug>1;

    my $fh = IO::File->new(">$filename");
    print $fh "<?xml version=\"1.0\"?>\n";
    print $fh "<osm version=\"0.3\" generator=\"OpenStreetMap Tracks2osm Converter\">\n";
    
    # --- Nodes
    for my $node_id (  sort keys %{$osm_nodes} ) {
	next unless $node_id;
	my $node = $osm_nodes->{$node_id};
	my $lat = $osm_nodes->{$node_id}->{lat};
	my $lon = $osm_nodes->{$node_id}->{lon};
	next unless defined($lat) && defined($lon);
	print $fh "	<node id=\"-$node_id\" ";
	print $fh " lat=\"$lat\" ";
	print $fh " lon=\"$lon\" ";
	print $fh ">\t";
	print $fh tags2osm($node);
	#print $fh "	 <tag k=\"alt\" v=\"$node->{alt}\"/>\t";
	print $fh "	</node>\n";

    }

    # --- Segments
    for my $seg_id (  sort keys %{$osm_segments} ) {
	next unless $seg_id;
	my $segment = $osm_segments->{$seg_id};
	my $node_from = $segment->{from};
	my $node_to   = $segment->{to};
	print $fh "	<segment id=\"-$seg_id\" ";
	print $fh " from=\"-$node_from\" ";
	print $fh " to=\"-$node_to\" ";
	my $sep="\n";
	#$sep="\t";
	print $fh ">$sep";
	print $fh tags2osm($segment);
	print $fh "	</segment>\n";

    }

    # --- Ways
    if ( $generate_ways ) {
	for my $way_id ( keys %{$osm_ways} ) {
	    next unless $way_id;
	    my $way = $osm_ways->{$way_id};
	    print $fh "	<way id=\"-$way_id\">\n";
	    print $fh tags2osm($way);
	    
	    for my $seg_id ( @{$way->{seg}} ) {
		next unless $seg_id;
		print $fh "	 <seg id=\"-$seg_id\"/>\n";
	    }
	    print $fh "	</way>\n";
	}
    }

    print $fh "</osm>\n";
    $fh->close();

    if ( $verbose) {
	printf STDERR "$filename: Wrote OSM File";
	print_time($start_time);
    }

}


###########################################



########################################################################################
########################################################################################
########################################################################################
#
#                     Main
#
########################################################################################
########################################################################################
########################################################################################
package main;

# *****************************************************************************
sub convert_Data(){
    my $all_tracks={
	filename => '',
	tracks => [],
	wpt => []
	};
    my $all_raw_tracks={
	filename => '',
	tracks => [],
	wpt => []
	};
    my $single_file = ( @ARGV ==1 );
    my $start_time  = time();

    my ($track_count,$point_count);

    if ( $use_stdin ) {
	push(@ARGV,"-");
    }
    if ( @ARGV < 1){
	printf STDERR "Need Filename(s) to convert\n";
	printf STDERR "use: osm-filter.pl -h for more help\n";
	exit 1;
    }

    GPS::read_all_filter_areas(@filter_area_files);

    
    my $osm_segments;
    if ( $do_check_against_osm ) {
	if ( -s $do_check_against_osm ) {
	    if (  $do_check_against_osm =~ m/\.csv/ ) {
		$osm_segments = Geo::OSM::SegmentList::LoadOSM_segment_csv($do_check_against_osm);
	    } elsif ( $do_check_against_osm =~ m/\.osm/ ) {
		$osm_segments = OSM::read_osm_file($do_check_against_osm);
	    } else {
		die "Unknown Datatype for $do_check_against_osm\n";
	    }
	    #print Dumper(\$osm_segments ) if $debug;
	} else {

	    # later we search in:
	    #  ~/.osm/data/planet.osm.csv
	    # /var/data/osm/planet.osm.csv

	    my @path=qw( ./
			 ~/openstreetmap.org/svn.openstreetmap.org/utils/osm-pdf-atlas/Data/
			 ~/svn.openstreetmap.org/utils/osm-pdf-atlas/Data/
			 ~/.gpsdrive/MIRROR/osm/);
	    my $osm_filename;
	    my $home = $ENV{HOME}|| '~/';
	    for my $path ( @path ) {

		$osm_filename = "${path}osm.txt";
		$osm_filename =~ s,\~/,$home/,;
		printf STDERR "check $osm_filename for loading\n" if $debug;
		
		last if -s $osm_filename;
		$osm_filename='';
	    }
	    $osm_segments = Geo::OSM::SegmentList::LoadOSM_segment_csv($osm_filename);
	};
    }
    

    
    my $count_files_converted=0;
    while ( my $filename = shift @ARGV ) {
	my $new_tracks;
	if ( ( $filename =~ m/-raw(|-osm|-pre-osm|-pre-clew)\.gpx$/ ) ||
	     ( $filename =~ m/-converted\.gpx$/ ) ||
	     ( $filename =~ m/00_combination.gpx$/ ) ||
	     ( $filename =~ m/00_filter_areas.gpx$/ )  
	     ){
	    printf STDERR "$filename: Skipping for read. These are my own files.\n\n";
	    next;
	}

	my ( $extention ) = ( $filename =~ m/\.([^\.]+)(\.gz|\.bz2)?$/ );
	printf STDERR "$filename has extention '$extention'\n" if $debug>1;
	if ( $filename eq '-' ) {
	    $new_tracks = GPX::read_gpx_file($filename);
	} elsif ( $filename =~ m/^gpsbabel:(\S+):(\S+)$/ ) {
	    my ($type,$name) = ($1,$2);
	    $new_tracks = Gpsbabel::read_file($name,$type);    
	} elsif ( $extention eq "gps" ) {
	    $new_tracks = Kismet::read_gps_file($filename);
	} elsif ( $extention eq "gpx" ) {
	    $new_tracks = GPX::read_gpx_file($filename);
	} elsif ( $extention eq "mps" ) {
	    $new_tracks = Gpsbabel::read_file($filename,"mapsource");
	} elsif ( $extention eq "gdb" ) {
	    $new_tracks = Gpsbabel::read_file($filename,"gdb");
	} elsif ( $extention eq "ns1" ) {
	    $new_tracks = Gpsbabel::read_file($filename,"netstumbler");
	} elsif ( $extention eq "nmea" ) {
	    $new_tracks = NMEA::read_file($filename);
	} elsif ( $extention eq "sav" ) {
	    $new_tracks = GPSDrive::read_gpsdrive_track_file($filename);
	} else {
	    warn "$filename: !!! Skipping because of unknown Filetype ($extention) for reading\n";
	    next;
	}
	my ($track_read_count,$point_read_count) =   GPS::count_data($new_tracks);
	if ( $verbose || $debug) {
	    printf STDERR "$filename: %5d Points in %d Tracks read\n",$point_read_count,$track_read_count;
	}

	if ( $out_raw_gpx ){
	    GPS::add_tracks($all_raw_tracks,$new_tracks);

	    my $new_gpx_file = "$filename-raw.gpx";
	    $new_gpx_file =~s/.gpx-raw.gpx/-raw.gpx/;
	    GPX::write_gpx_file($new_tracks,$new_gpx_file);
	};
		

	if ( $do_filter_clew ) {
	    $new_tracks = GPS::filter_gps_clew( $new_tracks,
					      { dist => 30 });
	};

	if ( @filter_area_files ) {
	    $new_tracks = GPS::filter_data_by_area($new_tracks);
	}

	if ( $filter_duplicate_tracepoints ) {
	    $new_tracks = GPS::filter_duplicate_tracepoints( $new_tracks,[],
							 { dist => 20 });
	};

	if ( $do_check_against_osm ) {
	    $new_tracks = OSM::check_against_osm( $new_tracks,$osm_segments,
					      { dist => 30 });
	};

	if ( $split_tracks ) {
	    $new_tracks = GPS::split_tracks($new_tracks,
					{ max_speed => 200 });
	}

	if ( $use_reduce_filter ) {
	    $new_tracks = GPS::filter_data_reduce_points($new_tracks);
	}


	$count_files_converted ++ if $point_count && $track_count;


	($track_count,$point_count) =   GPS::count_data($new_tracks);
	if ( $track_count > 0 ) {
	    my $new_gpx_file = "$filename-converted.gpx";
	    $new_gpx_file =~s/.gpx-converted.gpx/-converted.gpx/;
	    GPX::write_gpx_file($new_tracks,$new_gpx_file)
		if $single_file;
	    
	    if ( $out_osm ){
		my $new_osm_file = "$filename.osm";
		my $points = OSM::Tracks2osm($new_tracks);
		OSM::write_osm_file($new_osm_file,$points)
		}
	}

	GPS::add_tracks($all_tracks,$new_tracks);
	if ( $point_count && $track_count ) {
	    printf STDERR "$filename: %5d(%5d) Points in %3d(%3d) Tracks added\n",
	    $point_count,$point_read_count,$track_count,$track_read_count;

	}
	if ( $verbose ) {
	    printf STDERR "\n";
	}
	if ( $debug) {
	    printf STDERR "\n";
	}
    }

    if ( $count_files_converted ) {
	GPS::filter_duplicate_wpt($all_tracks);


	    GPS::print_count_data($all_tracks,"after complete processing");
	    print_time($start_time);

	    ($track_count,$point_count) =   GPS::count_data($all_tracks);
	    if (  $out_osm ) {
		my $points = OSM::Tracks2osm($all_tracks);
		OSM::write_osm_file("00_combination.osm",$points);
	    }
	    
	    if ( $use_stdout ) {
		GPX::write_gpx_file($all_tracks,'-');
		} else {
		    GPX::write_gpx_file($all_tracks,"00_combination.gpx");
			if ( $out_raw_gpx ){
			    GPX::write_gpx_file($all_raw_tracks,"00_combination-raw.gpx");
			    }
		    };
	    
	    if ( $draw_filter_areas ) {
		my $filter_areas = GPS::draw_filter_areas();
		GPX::write_gpx_file($filter_areas,"00_filter_areas.gpx");
	    }
	}
    if ( $verbose) {
	printf STDERR "Converting $count_files_converted Files";
	print_time($start_time);
    }
}

# ------------------------------------------------------------------

# Set defaults and get options from command line
Getopt::Long::Configure('no_ignore_case');
GetOptions ( 
	     'debug+'               => \$debug,      
	     'd+'                  => \$debug,      
	     'verbose+'            => \$verbose,
	     'v+'                  => \$verbose,
	     'MAN'                 => \$man, 
	     'man'                 => \$man, 
	     'h|help|x'            => \$help, 

	     'stdin'               => \$use_stdin,
	     'stdout'              => \$use_stdout,
	     'proxy=s'             => \$PROXY,

	     'out-osm'             => \$out_osm,
	     'out-raw-gpx'         => \$out_raw_gpx,
	     'split-tracks'        => \$split_tracks,
	     'check_against_osm:s' => \$do_check_against_osm,
	     'osm:s'               => \$do_check_against_osm,
             'filter_duplicate_tracepoints' => \$filter_duplicate_tracepoints,
	     'filter-clew'     => \$do_filter_clew,
	     'use_reduce_filter'   => \$use_reduce_filter,
	     'filter-area:s@'      => \@filter_area_files,
	     'generate_ways'       => \$generate_ways,
	     'filter-all'          => \$do_all_filters,
	     'fake-gpx-date'       => \$fake_gpx_date,
	     'write-gpx-wpt'       => \$write_gpx_wpt,
	     'draw_filter_areas'   => \$draw_filter_areas,
	     )
    or pod2usage(1);

if ( $do_all_filters ) {
    $out_osm            ||= 1;
    $out_raw_gpx        ||= 1;
    $split_tracks       ||= 1;
    $use_reduce_filter  ||= 1;
    $do_filter_clew     ||= 1;
    $do_check_against_osm = 1 unless defined $do_check_against_osm;
    @filter_area_files || push(@filter_area_files,"");
#    $filter_duplicate_tracepoints=1;
}

pod2usage(1) if $help;
pod2usage(-verbose=>2) if $man;

convert_Data();

##################################################################
# Usage/manual

__END__

=head1 NAME

B<osm-filter.pl> Version 0.01

=head1 DESCRIPTION

B<osm-filter.pl> is a program to convert and filter Track Files 
to a *.gpx and *.osm File. This File then can then be loaded into josm,
corrected and then uploaded to OSM.

This Programm is completely experimental, but some Data 
can already be retrieved with it.
Since the script is still in Alpha Stage, please make backups 
of your source gpx/kismet,... Files.

So: Have Fun, improve it and send me lots of new fixes/patches/features 
    and new filters :-))


This description is still preleminary, since the script is 
still in the stage of development, but for me it was already 
very usefull. Any hints/suggestions/patches are welcome.
Most of the threasholds are currently hardcoded in the source. 
But since it's a perl script it shouldn't be too much effort 
to change them. If you think we need some of these in a config-file 
i can try to add a config file to the script too.

The Idea behind the osm-filter is:
 - reading some different input formats. 
       Input is currently one of the folowing:
         - Kismet-GPS File   *.gps 
         - GpsDrive-Track    *.sav
         - GPX File          *.gpx
         - Garmin mps File   *.mps
         - Garmin gdb File   *.gdb
 - Then the Data is optionally filtered by area filters.
   For this you can define squares- and circle- shaped areas
   where the data is accepted or dropped.
   For now the areas can only be defined in the Source, 
   but this will change in the future.
   This can be for example be used to:
     - eliminate your home position where your gps always 
       walk arround in circles.
     - eliminate areas already mapped completely
     - limit your editing/uploading to a special area
   The areas are currently defined like
     { lat =>  48.1111 	,lon => 11.7111    ,proximity => .10  , block => 1 },
     { wp => "MyHome"  	,proximity => 6, block => 1 },
   Where MyHome is a GPSDrive waypoint taken from ~/.gpsdrive/way.txt
    proximity is defaulted by the proximity in the way.txt File (column 8)
    block is defaulted with 0
    If you want to see the filter areas in the resulting gpx file you can 
    use the option    --draw_filter_areas. This will draw in the check areas 
    as seperate tracks.
 - Then osm-filter then enriches the Data for internal use by adding:
	- speed of each segment (if necessary)
        - distance to last point 
        - angle to last segment (Which would represent steering wheel angle)
 - Then osm-filter is splitting the tracks if necessary.
   This is needed if you have for example gathered Tracks 
   with a Garmin handheld. There the Tracks get combined 
   even if you had no reception or switched of the unit inbetween.
   The decission to split the tracks is done by checking if:
      - time between points is too high ( > 60 seconds for now )
      - Speed is too high ( > 200 Km/h for now )
      - Distance between 2 point is too high ( >2 Km for now)
   Then each Track with less than 3 points is discarded.
 - if you use the option 
       --generate_ways
   osm-filter tries to determin continuous ways by looking 
   at the angle to the last segment, the speed and distance.

 - This is now done for all input Files. So you can also use 
   multiple input files as source and combine them to ine large 
   output File.

 - After this all now existing data iw written to a gpx file.
 - If you add the option  --out-osm. osm-filter tries 
   to generate an *.osm file out of this Data.

=head1 SYNOPSIS

B<Common usages:>

osm-filter.pl [--man] [-d] [-v] [-h][--out-osm] [--limit-area] <File1.gps> [<File2.sav>,<File2.ns1>,...]

!!!Please be carefull this is still a betta Version. Make Backups of your valuable source Files!!!

=head1 OPTIONS

=over 2

=item B<--man> Complete documentation

This shows the Complete documentation

=item B<--out-raw-gpx>

Print raw converted output to filename-raw.gpx

=item B<--out-osm>

*.osm files will only be generated if this option is set.

There is still a Bug/"Design Flaw" so all single .osm Files might 
always be a collection of all previous read Files.

There will also be written a file named
 ./00_combination.osm


=item B<--split-tracks>

Split tracks it they have gaps of more than 
 - 1 Minute
 - 1 Km
 - 200 Km/h

=item B<--check_against_osm> |  B<--osm>

This loads the osm.txt and checks if the 
track-points already exist as an osm-segment.
It checks if any of the points are near (<20m) any  
of the osm-segments. 
And the OSM Segment and the track segments have an 
angle of less than 30 Degrees.
If so they will be dropped.

The file osm.txt is the segments file used by osm-pdf-atlas.
It's containing all osm-segments from planet.osm in a rather simple way.
The probably easiest way to create it is to go to the 
directory
   svn.openstreetmap.org/utils/osm-pdf-atlas
and once call "create.sh".

If you provide a filename this file is read instead of the osm.txt file

you can use --osm=0 in combination with --filter-all to not let osm-filter 
check against osm Data.

=item B<--filter_duplicate_tracepoints>

This Filter checks for points near an already existing Trackline. 
If so it is removed

Currently it's only a stub, so anyone can sit down and programm the compare routine.

=item B<--use_reduce_filter>

The ammount of Datapoints is reduced. 
This is done by looking at three trackpoints in a row. For now I calculate the
distance between the line of point 1 and 3 to the point in the 
middle. If this distance is small enough (currently 4 meter) the 
middle point is dropped, because it doesn't really improve the track.


=item B<--limit-area>

Use the area filters

By default the Files ~/.gpsdrive/way.txt is read
and all waypoints starting with filter.
are added as filter areas.
    filter.deny will be filtered out
    filter.allow will be left in the resulting files
    filter.none nothing will be done here
after this by default the file  ~/josm/filter.xml is read.
An example would look lie:
<?xml version="1.0"?>
<osm>
  <node id="-10001"  lat="48.411430"  lon="9.492400" >     
    <tag k="name" v="de_Dottingen"/>
    <tag k="of:radius" v="2000" />
    <tag k="of:block" v="1" />
    <tag k="of:active" v="1" />
  </node>
  <node id="-10002"  lat="48.138004"  lon="11.557109" >
    <tag k="name" v="de_Muenchen"/>
    <tag k="of:radius" v="5000" />
    <tag k="of:block" v="1" />
    <tag k="of:active" v="0" />
  </node>
</osm>

If you want to define squares you have to define them for now 
in the Source at the definition of
  $internal__filter_area_list = 
AND: they are not tested :-(

The default area-filter rule is allow the rest.

=irem B<--filter-clew>

Filter out these little nasty gps accuracies if you are standing still

=item B<--filter-all>

Switch on all of the above filters

=item B<--draw_filter_areas>

Draw the filter_areas into the file 00_filter_areas.gpx file 
by creating a track with the border of each filter_area 

=item B<--generate_ways>

Try to generate ways inside the OSM structure. 
Still only testing


=item B<--fake-gpx-date>

This eliminates the date for while writing gpx data.

=item B<--write-gpx-wpt>

Only if this option is set, Waypoints are written to any of the gpx Files.

=item <File1.gps> [<File2.gps>,...]

 The Files to read and proccess

 Input is one of the folowing:
   - Kismet-GPS File   *.gps 
   - GpsDrive-Track    *.sav
   - GPX File          *.gpx
   - Garmin mps File   *.mps
   - Garmin gdb File   *.gdb
   - Netstumbler Files *.ns1
   - NMEA              *.nmea
   - via gpsbabel gpsbabel:<type>:*

For each File read a File *-converted.gpx will be written
All input filenames ending with -converted.gpx will be skiped.

To read all Files in a specified directory at once do the following:

 find <kismet_dir>/log -name "*.gps" | xargs ./osm-filter.pl

If you define multiple Files a summary File will automagically be written:
 ./00_combination.gpx

=item B<--stdin>

use stdin to read GPX track file

=item B<--stdout>

use stdout to write filtered  tracks as GPX

=back
