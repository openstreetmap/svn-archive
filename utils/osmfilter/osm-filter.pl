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
#	- distance between trackpoints < 1 Km
#	- speed between Trackpoints    < 200 Km/h
#       - the track point is near (<20m) any OSM-segment
#
# Output is:
#   - OSM File for josm *.osm ( http://openstreetmap.org/)
#   - GPX File for josm *.gpx
#   - 00__collection.gpx, 00__collection.osm (one File with all good tracks)
#   - 00__filter_areas.gpx with al the area filters 
#
# Joerg Ostertag <openstreetmap@ostertag.name>
# TODO:
#   eliminate duplicate waypoints
#   area filter for waypoints
#   keep name of Tracks
#   eliminate duplicate Tracks
#   cut out part of tracks which cover the same road
#   make limits (max_speed, max_line_dist, ...) configurable
#   write more filters:
#      - eliminate duplicate tracksegments (driving a street up and down)
#      - elimiate trackpoints where the GPS was standing for a longer time at one point

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

my ($man,$help);
our $debug =0;
our $verbose =0;
our $no_mirror=0;
our $PROXY='';


my $osm_nodes        = {};
my $osm_nodes_duplicate   = {};
my $osm_ways          = {};
my $osm_stats         = {};
my $osm_obj           = undef; # OSM Object currently read

my $use_stdin         = 0;
my $use_stdout        = 0;
my $out_osm           = 0;
my $split_tracks      = 0;
my $use_area_limit    = 0;
my $use_reduce_filter = 0;
my $draw_filter_areas  = 0;
my $check_against_osm = 0;
my $filter_duplicate_tracepoints = 0;
my $do_all_filters    = 0;
my $generate_ways     = 0;

my $first_id=10000;
my $next_osm_node_number    = $first_id;
my $osm_segment_number = $first_id;
my $osm_way_number     = $first_id;

my $FILTER_FILE = "$ENV{'HOME'}/.josm/filter.xml";


##################################################################
package Utils;
##################################################################

# print the time elapsed since starting
# starting_time is the first argument
sub print_time($){
    my $start_time = shift;
    printf STDERR " in %.0f sec\n", time()-$start_time;
}


##################################################################
package Geometry;
##################################################################

use Math::Trig;


our ($VERSION, @ISA, @EXPORT, @EXPORT_OK, %EXPORT_TAGS);
@ISA         = qw(Exporter);
@EXPORT = qw( &min &max);

sub min($$){
    my $a = shift;
    my $b = shift;
    return $b if ! defined $a;
    return $a if ! defined $b;
    return $a<$b?$a:$b;
}
sub max($$){
    my $a = shift;
    my $b = shift;
    return $b if ! defined $a;
    return $a if ! defined $b;
    return $a>$b?$a:$b;
}

# ------------------------------------------------------------------
# Distance in Km between 2 geo points with lat/lon
# Wild estimation of Earth radius 40.000Km
# At the poles we are completely off, since we assume the 
# lat/lon both have 40000Km radius, which is completely wrong
# if you are not at the aequator
sub distance_point_point_Km($$) {
    my $p1 = shift;
    my $p2 = shift;
    no warnings 'deprecated';

    return 999999999 
	unless defined($p1) && defined($p2);

    my $lat1 = $p1->{'lat'};
    my $lon1 = $p1->{'lon'};
    my $lat2 = $p2->{'lat'};
    my $lon2 = $p2->{'lon'};

    return 999999999 
	unless defined($lat1) && defined($lon1);
    return 999999999 
	unless defined($lat2) && defined($lon2);

    
    # Distance
    my $delta_lat=$lat1-$lat2;
    my $delta_lon=$lon1-$lon2;
    return sqrt($delta_lat*$delta_lat+$delta_lon*$delta_lon)*40000/360;
   
}

# ------------------------------------------------------------------
# Distance between 2 geo points with lat/lon, 
# result: (delta_lat,delta_lon) in degrees
sub distance_degree_point_point($$) {
    my $p1 = shift;
    my $p2 = shift;

    return (999999999,999999999)
	unless defined($p1) && defined($p2);

    my $lat1 = $p1->{lat};
    my $lon1 = $p1->{lon};
    my $lat2 = $p2->{lat};
    my $lon2 = $p2->{lon};
    
    # Distance
    my $delta_lat=$lat1-$lat2;
    my $delta_lon=$lon1-$lon2;
    return $delta_lat,$delta_lon;
  
}

# ------------------------------------------------------------------
# Angle from North
# East is  +0 ..   180
# West is  -0 .. - 180
sub angle_north($$){
    my $p1 = shift;
    my $p2 = shift;

    my $lat1 = $p1->{lat};
    my $lon1 = $p1->{lon};
    my $lat2 = $p2->{lat};
    my $lon2 = $p2->{lon};
    
    # Distance
    my $delta_lat=$lat1-$lat2;
    my $delta_lon=$lon1-$lon2;

    # Angle
    my $angle = - rad2deg(atan2($delta_lat,$delta_lon));
    return $angle;
}

# ------------------------------------------------------------------
# Minimal Distance between line and point in degrees
sub distance_line_point_Km($$$$$$) {
    return distance_line_point(@_)*40000/360;
}
# ------------------------------------------------------------------
# Minimal Distance between line and point in degrees
sub distance_line_point($$$$$$) {
    my $x1 = shift;
    my $y1 = shift;
    my $x2 = shift;
    my $y2 = shift;

    my $xp = shift;
    my $yp = shift;


    printf STDERR "distance_line_point(%f,%f, %f,%f,   %f,%f)\n", $x1, $y1, $x2, $y2,  $xp, $yp
	if ( $debug >10 ) ;

    my $dx1p = $x1 - $xp;
    my $dx21 = $x2 - $x1;
    my $dy1p = $y1 - $yp;
    my $dy21 = $y2 - $y1;
    my $frac = $dx21 * $dx21 + $dy21 * $dy21;

    if ( $frac == 0 ) {
	return(sqrt(($x1-$xp)*($x1-$xp) + ($y1-$yp)*($y1-$yp)));
    }

    my $lambda = -($dx1p * $dx21 + $dy1p * $dy21) / $frac;
    printf STDERR "distance_line_point(): lambda_1: %f\n",$lambda
	if ( $debug > 10 );

    $lambda = Geometry::min(Geometry::max($lambda,0.0),1.0);
    
    printf STDERR "distance_line_point(): lambda: %f\n",$lambda
	if ( $debug > 10 ) ;

    my $xsep = $dx1p + $lambda * $dx21;
    my $ysep = $dy1p + $lambda * $dy21;
    return sqrt($xsep * $xsep + $ysep * $ysep);
}

##################################################################
package File;
##################################################################

use IO::File;

# -----------------------------------------------------------------------------
# Open Data File in predefined Directories
sub data_open($){
    my $filename = shift;
    my $fh;

    # If it's already an open File
    if ( ref($filename) =~ m/IO::File/ ) {
	return $filename;
    }

    if ( $filename eq "-" ) {
	$fh = IO::File->new('<&STDIN');
	$fh or  die("cannot open $filename: $!");
	return $fh;
    } else {
	my $size = (-s $filename)||0;
	if ( $size < 270 ) {
	    warn "cannot Open $filename ($size) Bytes is too small)\n"
		if $verbose || $debug;
	    return undef;
	}
    }

    printf STDERR "Opening $filename\n" if $debug;
    if ( $filename =~ m/\.gz$/ ) {
	$fh = IO::File->new("gzip -dc $filename|")
	    or die("cannot open $filename: $!");
    } elsif ( $filename =~ m/\.bz2$/ ) {
	    $fh = IO::File->new("bzip2 -dc $filename|")
		or die("cannot open $filename: $!");
	} else {
	    $fh = IO::File->new("$filename",'r')
		or die("cannot open $filename: $!");
	}
    return $fh;
}

##################################################################
package OSM;
##################################################################
use Storable;

sub LoadOSM($)
{
  my $filename = shift;

  printf STDERR "Reading OSM File: $filename\n"
      if $debug || $verbose;
  my $start_time=time();

  my $segments;

  if ( -s "$filename.storable") {
      # later we should compare if the file also is newer than the source
      $filename .= ".storable";
      $segments = Storable::retrieve($filename);
  } else {
      my $fh = File::data_open($filename);

      die "Cannot open $filename in LoadOSM"  unless $fh;

      while ( my $line = $fh ->getline() ) {
	  my @segment;
	  my $dummy;
	  ($segment[0],$segment[1],$segment[2],$segment[3],$dummy) = split(/,/,$line,5);
	  $segment[4] = Geometry::angle_north(
					  { lat => $segment[0] , lon => $segment[1] },
					  { lat => $segment[2] , lon => $segment[3] });
	  $segment[5] = $dummy if $debug;
	  push (@{$segments},\@segment);
      }
      $fh->close();
      Storable::store($segments   ,"$filename.storable");
  }

  if ( $verbose) {
      printf STDERR "Read and parsed $filename";
      Utils::print_time($start_time);
  }

  return($segments);
}


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
    if ( $verbose || $debug) {
	printf STDERR "Reduced OSM Data to $count( $all_count) OSM-Segments ";
	Utils::print_time($start_time);
    }

    return $osm_segments;
}

# ------------------------------------------------------------------
# check if new trackpoints are on existing osm tracks
sub check_against_osm($$$){
    my $tracks       = shift; # reference to tracks list
    my $all_osm_segments = shift;
    my $config       = shift;

    my $dist_osm_track = $config->{dist} || 20;
    my $max_angle = 30;
    my $start_time=time();

    my $bounds = GPS::get_bounding_box($tracks);
    #printf STDERR "Track Bounds: ".Dumper(\$bounds);
    my $osm_segments = reduce_osm_segments($all_osm_segments,$bounds);

    my $new_tracks={tracks=>[],wpt=>[]};

    # Keep WPT
    for my $wpt ( @{$tracks->{wpt}} ) {
	next unless $wpt;
	push(@{$new_tracks->{wpt}},$wpt);
    }

    my $all_points = 0;
    my $new_points = 0;
    for my $track ( @{$tracks->{tracks}} ) {
	next if !$track;
	my $new_track=[];
	my $last_elem;
	for my $elem ( @{$track} ) {
	    $all_points++;
	    my $skip_point=0;
	    my $min_dist = 40000;
	    my $pdop = $elem->{pdop};
	    my $compare_dist=$dist_osm_track;
	    if ( defined ( $pdop ) &&  ($pdop >0) ) {
		$compare_dist= $pdop;
		$compare_dist=10 if $compare_dist <10;
	    }

	    my $angle=-999999;
	    if ( $last_elem ) {
		$angle = Geometry::angle_north($last_elem,$elem);
	    }

	    for my $segment ( @{$osm_segments} ) {
		# The lines have to be fairly parallel
		next unless abs ($angle - $segment->[4]) < $max_angle;

		# Distance between line of segment($segment)  to trackpoint $elem
		my $dist = 1000*Geometry::distance_line_point_Km($segment->[0],$segment->[1],
							    $segment->[2],$segment->[3],
							    $elem->{lat},$elem->{lon}
							 );
		$min_dist = $dist if $dist < $min_dist;
		next if $dist > $compare_dist; # in m
		printf STDERR "Elem is %3.1f m away from line\n",$dist
		    if $debug >5;
		$skip_point++;
		last;
	    }
	    # printf STDERR "Min Dist: $min_dist Meter\n";
	    
	    if ( $skip_point ) {
		my $num_elem=scalar(@{$new_track});
		if ( $num_elem >4 ) {
		    push(@{$new_tracks->{tracks}},$new_track);
		}
		$new_track=[];
	    } else {
		push(@{$new_track},$elem);
		$new_points++;
	    }
	    $last_elem=$elem;
	}
	push(@{$new_tracks->{tracks}},$new_track);
    }

    if ( $debug || $verbose) {
	printf STDERR "Reduced  $new_points($all_points) Points comparing to OSM.txt";
	Utils::print_time($start_time);
    }

    return $new_tracks;
}


##################################################################
package Kismet;
##################################################################
use Date::Parse;
use Data::Dumper;
# -----------------------------------------------------------------------------
# Read GPS Data from Kismet File
sub read_gps_file($) { 
    my $filename = shift;

    my $start_time=time();

    printf STDERR ("Reading $filename\n") if $verbose || $debug;
    printf STDERR "$filename:	".(-s $filename)." Bytes\n" if $debug;

    print STDERR "Parsing file: $filename\n" if $debug;
    my $p = XML::Parser->new( Style => 'Objects' ,
			      );
    
    my $fh = File::data_open($filename);
    return {tracks=>[]} unless $fh;
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
	return {tracks=>[]};
    }
    if ( $debug ) {
	printf STDERR "Read and parsed $filename";
	Utils::print_time($start_time);
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

    return {tracks=>[$track]};
}


##################################################################
package Gpsbabel;
##################################################################
use IO::File;


# -----------------------------------------------------------------------------
# Read GPS Data with the help of gpsbabel converting a file to a GPX-File
sub read_file($$) { 
    my $filename     = shift;
    my $gpsbabel_type = shift;

    printf STDERR ("Reading $filename\n") if $verbose || $debug;
    printf STDERR "$filename:	".(-s $filename)." Bytes\n" if $debug;

    my $gpsbabel_call="gpsbabel  -i $gpsbabel_type -f '$filename' -o gpx -F - ";
    printf STDERR "calling gpsbabel:\n$gpsbabel_call\n " if $debug>3;
    my $fh = IO::File->new("$gpsbabel_call |");
    if ( !$fh )  {
	warn "Cannot Convert $filename as Type $gpsbabel_type\n";
	return {tracks=>[]};
    }
    GPX::read_gpx_file($fh);
}

##################################################################
package NMEA;
##################################################################
use IO::File;
use Date::Parse;
use Data::Dumper;
use Date::Parse;
use Date::Manip;

# -----------------------------------------------------------------------------
# Read GPS Data from NMEA - File
sub read_file($) { 
    my $filename = shift;

    my $start_time=time();

    my $new_tracks={tracks=>[],wpt=>[]};
    printf STDERR ("Reading $filename\n") if $verbose || $debug;
    printf STDERR "$filename:	".(-s $filename)." Bytes\n" if $debug;

    my $fh = File::data_open($filename);
    return {tracks=>[],wpt=>[]} unless $fh;
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
    if ( $verbose) {
	printf STDERR "Read and parsed $filename";
	Utils::print_time($start_time);
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

# -----------------------------------------------------------------------------
# Read GPS Data from GPX - File
sub read_gpx_file($) { 
    my $filename = shift;

    my $start_time=time();
    my $fh;

    $fh = File::data_open($filename);
    if ( ! ref($filename) =~ m/IO::File/ ) {
	print STDERR "Parsing file: $filename\n" if $debug;
    }
    return {tracks=>[]} unless $fh;

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
	return {tracks=>[]} ;
    }
    if ( $verbose) {
	printf STDERR "Read and parsed $filename";
	Utils::print_time($start_time);
    }

    my $new_tracks={tracks=>[],wpt=>[]};
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
				sym
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

    my $track_id=0;
    my $point_count=0;
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
	    if( defined $value ) {
		print $fh "     <$type>$value</$type>\n";
	    }
	};
	print $fh " </wpt>\n";
    }
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
		my $time = strftime("%FT%H:%M:%SZ", localtime($time_sec));
		#UnixDate("epoch ".$time_sec,"%m/%d/%Y %H:%M:%S");
		$time .= ".$time_usec" if $time_usec;
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

    printf STDERR "$filename: Wrote GPX File ($track_id Tracks with $point_count Points)";
    Utils::print_time($start_time);
}


##################################################################
package GPSDrive;
##################################################################
use Date::Parse;
use Data::Dumper;

# -----------------------------------------------------------------------------
# Read GPSDrive Track Data
sub read_gpsdrive_track_file($) { 
    my $filename = shift;

    my $start_time=time();

    my $new_tracks = { tracks=>[],wpt=>[] };
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
    if ( $verbose) {
	printf STDERR "Read and parsed $filename";
	Utils::print_time($start_time);
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

# ------------------------------------------------------------------
# Check if the point is in the area to currently evaluate
my $areas_allowed_squares = 
    [    # Block a circle of <proximity> Km arround each point
#     { lat =>  48.175921 	,lon => 11.754312  ,proximity => .030 , block => 1 },
#     { lat =>  48.175710 	,lon => 11.754400  ,proximity => .030 , block => 1 },
#     { lat =>  48.175681 	,lon => 11.7547203 ,proximity => .030 , block => 1 },
#     { lat =>  48.175527 	,lon => 11.7586399 ,proximity => .030 , block => 1 },
#     { lat =>  48.1750 	,lon => 11.7536    ,proximity => .10  , block => 1 },
	 
	 # Allow Rules for square size areas
	 # min_lat,min_lon max_lat,max_lon,     (y,x)
	 #[ 48.0  , 11.6  , 48.4    , 12.0    ], # München
	 #[ 48.10  , 11.75  , 49.0    , 14.0    ], # Münchner-Osten-
	 #[ -90.0  , -180  , 90.0    , 180   ], # World
	 
	 # The rest of the World is blocked by default
	 
	 ];	


# -------------------------------------------------
# Read XML Filter File from josm directory
sub read_filter_areas_xml($){
    my $filename = shift;

    my $start_time=time();

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
	return {tracks=>[]};
    }
    if ( $debug ) {
	printf STDERR "Read and parsed $filename";
	Utils::print_time($start_time);
    }
    for my $elem  ( @{$content->[0]->{Kids}} ) {
	next; #Since this is only a stub for now
	my $name = '';
	my $block=1;
	push( @{$areas_allowed_squares},
	  { wp => $name, block => $block }
	      );
    }
}

# -------------------------------------------------
# Add all gpsdrive waypoints from this File as a filter 
# where deny specifies that its a block filter
sub read_filter_areas($){
    my $filename = shift;

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
	push( @{$areas_allowed_squares},
	  { wp => $name, block => $block }
	      );
    }
    close(WAYPT);
}

# -------------------------------------------------
sub check_allowed_area($){
    my $elem = shift;
    
    return 1 unless $use_area_limit;
    
    for my $area ( @{$areas_allowed_squares} ) {
	if (ref($area) eq "HASH" ) {	    
	    if ( defined ( $area->{wp} ) ) { # Get from GPSDrive ~/.gpsdrive/way.txt Waypoints
		my $proximity;
		($area->{lat},$area->{lon},$proximity) = GPSDrive::get_waypoint($area->{wp});
		$area->{proximity} ||= $proximity;
		$area->{proximity} ||= 10;
	    }
	    
	    if ( Geometry::distance_point_point_Km($area,$elem) < $area->{proximity} ) {
		printf STDERR "check_allowed_area(".$elem->{lat}.",".$elem->{lon}.
		    ") -> WP: $area->{wp} : block: $area->{block}\n"
		    if $debug >30;
		return ! $area->{block};
	    }
	} else {
	    my ($min_lat,$min_lon, $max_lat,$max_lon ) = @{$area};
	    if ( $min_lat <= $elem->{lat} &&	 $max_lat >= $elem->{lat} &&
		 $min_lon <= $elem->{lon} &&	 $max_lon >= $elem->{lon} ) {
		return 1;
	    }
	}
    }
    return 0;
}

# Return a tracklist whith a track for each chek_area
sub draw_filter_areas(){
    return [] unless $draw_filter_areas;
    my $new_tracks={tracks=>[]};
    for my $area ( @{$areas_allowed_squares} ) {
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
# Add things like speed, time, .. to the GPS Data
# and the split the tracks if we think it's necessary
sub split_tracks($$$){
    my $tracks      = shift; # reference to tracks list
    my $comment     = shift;
    my $config      = shift;

    my $max_speed = $config->{max_speed} || 200;
    my $max_dist  = $config->{max_dist}  || 11;
    my $max_time  = $config->{max_time}  || 600;

    my $new_tracks= { tracks => [],wpt=>[] };

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
	my $new_track = [];
	for my $elem ( @{$track} ) {
	    unless ( defined($elem->{lat}) && defined($elem->{lon})){
		$deleted_points++;
		next;
	    }

	    $elem->{time} = 0 unless defined $elem->{time};

	    if (  $prev_elem ) {
		my $dist  = Geometry::distance_point_point_Km($prev_elem,$elem);
		my $angle = Geometry::angle_north($prev_elem,$elem);
		my ($d_lat,$d_lon) = Geometry::distance_degree_point_point($prev_elem,$elem);
		$elem->{dist}=$dist;   # in Km
		$elem->{angle}=$angle; # in Degre
		if ($debug)  {
		    #$elem->{d_lat}=sprintf("%9f",$d_lat*100000);
		    #$elem->{d_lon}=sprintf("%9f",$d_lon*100000);
		}
		if ($dist) {
		    $min_dist=Geometry::min($min_dist,$dist);
		    $max_dist=Geometry::max($max_dist,$dist);
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

		if ( $elem->{time_diff} > $max_time) { # ---- Check for Track Split: time diff
		    $split_track .= " Delta Time: $elem->{time_diff} sec. ";
		}

		if ( $elem->{dist} > $max_dist) {             # ---- Check for Track Split: 11 Km
		    $split_track .= sprintf(" Dist: %.3f Km ",$elem->{dist});
		}

		if ( $elem->{speed} && $elem->{speed} > $max_speed) { # ---- Check for Track Split: 200 Km/h
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
	
	if ( $debug>1 || $verbose >1 ) {
	    printf STDERR "Enrich Data: Track $track_number from $comment\n";
	    printf STDERR "	Distance: %8.2f m .. %8.2f Km \n", $min_dist*1000,$max_dist;
	    printf STDERR "	Elements: ".(scalar(@{$track}))."\n",
	}
    }
    if ( $debug || $verbose >1) {
	printf STDERR "Enrich Data: $comment:\n";
    };
    if ( $debug ) {
	printf STDERR "	Deleted Points: $deleted_points\n"
    }
    #printf STDERR Dumper(\$new_tracks);
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
	Utils::print_time($start_time);
    }

    return ( $count_tracks,$count_points);
}

# ------------------------------------------------------------------
# Print Number of points/tracks with a comment
sub print_count_data($$){
    my $tracks      = shift; # reference to tracks list
    my $comment = shift;

    my ($track_count,$point_count) = GPS::count_data($tracks);
    if ( $verbose || $debug) {
	printf STDERR "$comment %5d Points in %d Tracks\n",$point_count,$track_count;
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
	Utils::print_time($start_time);
    }

    return { lat_min => $lat_min,lon_min => $lon_min,
	     lat_max => $lat_max,lon_max => $lon_max };
}

# ------------------------------------------------------------------
# Filter tracks with points
# check_allowed_area($elem) tells if this element is added or not
sub filter_data_by_area($){
    my $tracks      = shift; # reference to tracks list

    return unless $use_area_limit;

    my $start_time=time();

    my $new_tracks= { tracks => [],wpt=>[] };

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
    if ( $verbose >1) {
	printf STDERR "Filter by Area: Good Tracks: $good_tracks, Points: $good_points";
	Utils::print_time($start_time); 
    }
    printf STDERR "deleted_points:$deleted_points \n"	
	if $debug>10;

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
    my $new_tracks= { tracks => [],wpt=>[] };

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
	    my $dist_0_2 = Geometry::distance_point_point_Km($elem0,$elem2);

	    # Max Distance between 2 points in Track
	    if ( $dist_0_2 > .5 ) { # max .5 km distanz
		printf STDERR "Elem0 und Elem2 have $dist_0_2 Km Distance, which would be too much\n"
		    if $debug >10;
	    } else {
		# Distance between line of line(p0 and p2) to p1 
		my $dist = Geometry::distance_line_point_Km($elem0->{lat},$elem0->{lon},
							 $elem2->{lat},$elem2->{lon},
							 $elem1->{lat},$elem1->{lon}
							 );
		$skip_point =  1 if $dist < 0.001; # 1 meter
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
    if ( $verbose > 1 ) {
	printf STDERR "Filter to reduce number of points: ".
	    "Good Tracks: $good_tracks, GoodPoints: $good_points, deleted_points:$deleted_points ";
	Utils::print_time($start_time);
    }
    return $new_tracks;
}

# ------------------------------------------------------------------
# add a list of tracks to another 
sub add_tracks($$){
    my $dst_tracks      = shift; # reference to tracks list
    my $src_tracks      = shift; # reference to tracks list

    $dst_tracks ||= { tracks => [],wpt=>[] };
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

    my $new_tracks={tracks=>[],wpt=>[]};

    # Keep WPT
    for my $wpt ( @{$tracks->{wpt}} ) {
	next unless $wpt;
	push(@{$new_tracks->{wpt}},$wpt);
    }

    my $count_points = 0;
    my $new_points = 0;
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

	    for my $segment ( @{$checked_track_segments} ) {
		#printf STDERR Dumper(\$segment);
		# Distance between line of segment($segment)  to trackpoint $elem
		my $dist = 1000*Geometry::distance_line_point_Km($segment->[0],$segment->[1],
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
		if ( defined($last_elem) && defined($last_elem->{lat}) && defined($last_elem->{lon})) {
		    push(@{$checked_track_segments},
			 [$last_elem->{lat},$last_elem->{lon},$elem->{lat},$elem->{lon}]);
		}
	    }
	    $last_elem=$elem;
	}
	push(@{$new_tracks->{tracks}},$new_track);
    }

    if ( $debug || $verbose) {
	printf STDERR "Found $count_points Points already in other Tracks. This leaves $new_points ";
	Utils::print_time($start_time);
    }

    return $new_tracks;
}



# ------------------------------------------------------------------



##################################################################
package OSM;
##################################################################

use Data::Dumper;

#no warnings 'deprecated';

# ------------------------------------------------------------------
sub Tracks2osm($$){
    my $tracks = shift;
    my $reference = shift;

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
		$dist = Geometry::distance_point_point_Km($osm_nodes->{$node_from},$elem);
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
		    $angle = Geometry::angle_north($osm_nodes->{$node_from},$osm_nodes->{$node_to});
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
	Utils::print_time($start_time);
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

    my $filename    = "/home/kismet/log/gps-Tweety/gps-14.5.2004.txt-ACTIVE_LOG_015.gps";
    my $all_tracks  = { tracks => []};
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

    GPS::read_filter_areas($WAYPT_FILE);
    GPS::read_filter_areas_xml($FILTER_FILE);

    my $osm_segments;
    if ( $check_against_osm ) {
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
	$osm_segments = OSM::LoadOSM($osm_filename);
	};
    

   
    my $count=0;
    while ( $filename = shift @ARGV ) {
	my $new_tracks;
	if ( $filename =~ m/-converted.gpx$/ ) {
	    printf STDERR "$filename: Skipping for read. These are my own files.\n";
	    next;
	}
	if ( $filename =~ m/00__combination.gpx$/ ) {
	    printf STDERR "$filename: Skipping for read. These are my own files.\n";
	    next;
	}
	if ( $filename =~ m/00__filter_areas.gpx$/ ) {
	    printf STDERR "$filename: Skipping for read. These are my own files.\n";
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
	    printf STDERR "$filename: Read %5d Points in %d Tracks\n",$point_read_count,$track_read_count;
	}

	if ( $use_area_limit ) {
	    $new_tracks = GPS::filter_data_by_area($new_tracks);
	    GPS::print_count_data($new_tracks,"$filename: Area Filter to ");
	}

	
	if ( $filter_duplicate_tracepoints ) {
	    $new_tracks = GPS::filter_duplicate_tracepoints( $new_tracks,[],
					      { dist => 20 });
	};

	if ( $check_against_osm ) {
	    $new_tracks = OSM::check_against_osm( $new_tracks,$osm_segments,
					      { dist => 20 });
	};

	if ( $split_tracks ) {
	    $new_tracks = GPS::split_tracks($new_tracks,$filename,
					{ max_speed => 200 });
	    GPS::print_count_data($new_tracks,"$filename: enriching/splitting to" );
	}

	if ( $use_reduce_filter ) {
	    $new_tracks = GPS::filter_data_reduce_points($new_tracks);
	    GPS::print_count_data($new_tracks,"$filename: Data Reduce to ");
	}


	$count ++ if $point_count && $track_count;


	($track_count,$point_count) =   GPS::count_data($new_tracks);
	if ( $track_count > 0 ) {
	    my $new_gpx_file = "$filename-converted.gpx";
	    $new_gpx_file =~s/.gpx-converted.gpx/-converted.gpx/;
	    GPX::write_gpx_file($new_tracks,$new_gpx_file)
		if $single_file;
	    
	    if ( $out_osm ){
		my $new_osm_file = "$filename.osm";
		my $points = OSM::Tracks2osm($new_tracks,$filename);
		OSM::write_osm_file($new_osm_file,$points)
		}
	}

	GPS::add_tracks($all_tracks,$new_tracks);
	if ( $point_count && $track_count ) {
	    printf STDERR "Added:  %5d(%5d) Points in %3d(%3d) Tracks for %s\n",
	    $point_count,$point_read_count,$track_count,$track_read_count,$filename;

	}
	if ( $verbose ) {
	    printf STDERR "\n";
	}
	if ( $debug) {
	    printf STDERR "\n";
	}
    }

    GPS::filter_duplicate_wpt($all_tracks);


    GPS::print_count_data($all_tracks,"Result:  after complete processing ");

    ($track_count,$point_count) =   GPS::count_data($all_tracks);
    if ($track_count && $point_count) {
	if (  $out_osm) {
	    my $points = OSM::Tracks2osm($all_tracks,$filename);
	    OSM::write_osm_file("00__combination.osm",$points);
	}
	
	if ( $use_stdout ) {
	    GPX::write_gpx_file($all_tracks,'-');
	    } else {
		GPX::write_gpx_file($all_tracks,"00__combination.gpx");
		};
    }

    if ( $draw_filter_areas ) {
	my $filter_areas = GPS::draw_filter_areas();
	GPX::write_gpx_file($filter_areas,"00__filter_areas.gpx");
     }

    if ( $verbose) {
	printf STDERR "Converting $count Files";
	Utils::print_time($start_time);
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
	     'no-mirror'           => \$no_mirror,
	     'out-osm'             => \$out_osm,
	     'stdin'               => \$use_stdin,
	     'stdout'              => \$use_stdout,
	     'split-tracks'        => \$split_tracks,
	     'limit-area'          => \$use_area_limit,
	     'draw_filter_areas'    => \$draw_filter_areas,
	     'check_against_osm'   => \$check_against_osm,
             'filter_duplicate_tracepoints' => \$filter_duplicate_tracepoints,
	     'use_reduce_filter'   => \$use_reduce_filter,
	     'filter-all'          => \$do_all_filters,
	     'generate_ways'       => \$generate_ways,
	     'proxy=s'             => \$PROXY,
	     'MAN'                 => \$man, 
	     'man'                 => \$man, 
	     'h|help|x'            => \$help, 
	     )
    or pod2usage(1);

if ( $do_all_filters ) {
    $out_osm           = 1;
    $split_tracks      = 1;
    $use_reduce_filter = 1;
    $check_against_osm = 1;
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
      - Distance between 2 point is too high ( >1 Km for now)
   Then each Track with less than 3 points is discarded.
 - After that the ammount of Datapoints is reduced. This is done by
   looking at three trackpoints in a row. For now I calculate the
   distance between the line of point 1 and 3 to the point in the 
   middle. If this distance is small enough (currently 1 meter) the 
   middle point is dropped, because it doesn't really improve the track.
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

=item B<--out-osm>

*.osm files will only be generated if this option is set.

There is still a Bug/"Design Flaw" so all single .osm Files might 
always be a collection of all previous read Files.

There will also be written a file named
 ./00__combination.osm


=item B<--split-tracks>

Split tracks it they have gaps of more than 
 - 10 Minutes
 - 11Km
 - 200Km/h

=item B<--check_against_osm>

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

=item B<--filter_duplicate_tracepoints>

This Filter checks for points near an already existing Trackline. 
If so it is removed

Currently it's only a stub, so anyone can sit down and programm the compare routine.

=item B<--filter-all>

Switch on all of the above filters


=item B<--limit-area>

use the area limits coded in the source

By default the File ~/.gpsdrive/way.txt is also read and all 
waypoints starting with 
  filter.
are added as filter areas.
    filter.deny will be filtered out
    filter.allo will be left in the resulting files

If you want to define squares you have to define them for now 
in the Source at the definition of
  $areas_allowed_squares = 
AND: they are not tested :-(

=item B<--draw_filter_areas>

draw the filter_areas into the file 00__filter_areas.gpx file 
by creating a track with the border of each filter_area 

=item B<--generate_ways>

Try to generate ways inside the OSM structure. 
Still only testing


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
 ./00__combination.gpx

=item B<--stdin>

use stdin to read GPX track file

=item B<--stdout>

use stdout to write filtered  tracks as GPX

=back
