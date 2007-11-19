#!/usr/bin/perl
# Copyright (C) 2006,2007, and GNU GPL'd, by Joerg Ostertag
#
# This Script converts/filters GPS-Track-Data 
# Input is one of the folowing:
#   - Kismet-GPS File   *.gps 
#   - GpsDrive-Track    *.sav
#   - GPX File          *.gpx
#   - Garmin mps File   *.mps
#   - Garmin gdb File   *.gdb
#   - Netstumbler Files *.ns1
#
# Standard Filters:
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
# Joerg Ostertag <osm-track-filter.openstreetmap@ostertag.name>
# Anyone is welcome to test, find Bugs, add new features
# , correct existing bugs or/and improve this Code.
#
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
    my $dir = $0;
    $dir =~s,/[^/]+$,,;
    $dir =~s,/[^/]+$,,;
    $dir =~s,/[^/]+$,,;
    $dir =~s,/utils.*$,/utils/perl_lib,;
    unshift(@INC,"$dir");
    unshift(@INC,"../../perl_lib");
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
use Geo::OSM::SegmentList;

my ($man,$help);
#our $DEBUG =0;
#our $VERBOSE =0;
our $PROXY='';


our $osm_stats         = {};
our $osm_obj           = undef; # OSM Object currently read

our $use_stdin         = 0;
our $use_stdout        = 0;
our $out_osm           = undef;
our $fake_gpx_date     = undef;
our $write_gpx_wpt     = 0;
our $out_raw_gpx       = undef;
our $out_upload_gpx    = undef;
our $split_tracks      = undef;
our @filter_area_files = ();
our $draw_filter_areas = undef;
our $do_filter_reduce_pt = undef;
our $do_filter_clew   = undef;
our $do_filter_against_osm = undef;
our $do_filter_dup_trace_segments = undef;
our $do_all_filters    = 0;
our $generate_ways     = undef;

our $FILTER_FILE = "$ENV{'HOME'}/.josm/filter.xml";


##################################################################
package OSM;
##################################################################
use Storable;
use strict;
use warnings;
use Carp;


use Geo::GPX::File;
use Geo::Geometry;
use Geo::OSM::SegmentList;
use Geo::OSM::Tracks2OSM;
use Geo::OSM::Write;
use Geo::Tracks::GpsBabel;
use Geo::Tracks::Kismet;
use Geo::Tracks::NMEA;
use Geo::Tracks::TRK;
use Geo::Tracks::Netmonitor;
use Geo::Tracks::Tools;
use Utils::Debug;
use Utils::File;
use Utils::Math;


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
	#print STDERR "abs_angle: $angle_n_r2\n" if $DEBUG;

	# Distance between line of segment($segment)  to trackpoint $elem1
	my $dist = 1000*distance_line_point_Km($segment->[0],$segment->[1],
					       $segment->[2],$segment->[3],
					       $elem1->{lat},$elem1->{lon}
					       );
	$min_dist = $dist if $dist < $min_dist;
	next if $dist > $compare_dist; # in m
	printf STDERR "Elem is %3.1f m away from line\n",$dist
	    if $DEBUG >5;
	$skip_point++;
	last;
    }
    # printf STDERR "Min Dist: $min_dist Meter\n";
    return $skip_point;
}


# ------------------------------------------------------------------
# check if new trackpoints are on existing osm tracks
sub filter_against_osm($$$){
    my $tracks       = shift; # reference to tracks list
    my $segments_filename = shift;
    my $config       = shift;

    my $start_time=time();

    my $filename=$tracks->{filename};

    my $dist_osm_track = $config->{dist} || 40;

    my $bounds = GPS::get_bounding_box($tracks);
    printf STDERR "Track Bounds: ".Dumper(\$bounds) if $DEBUG>5;
    my $osm_segments = load_segment_list($segments_filename,$bounds);

    enrich_tracks($tracks);

    my $parsing_display_time=time();
    my ($track_count,$point_count) =   count_data($tracks);
    my $track_points_done=0;
    my $track_no=0;
    for my $track ( @{$tracks->{tracks}} ) {
	$track_no++;
	next if !$track;

	for my $track_pos ( 0 .. $#{@{$track}} ) {
	    my $elem = $track->[$track_pos];
	    $track_points_done++;
	    if ( ! is_segment_of_list_nearby($track,$track_pos,$osm_segments)){
		$elem->{good_point} = 1;
		$elem->{split_track} =0;
	    } else {
		$elem->{good_point} = 0;
		$elem->{split_track} =1;
	    }
	    if ( ( $VERBOSE || $DEBUG ) &&
		 ( time()-$parsing_display_time >10)
		 )  {
		$parsing_display_time= time();
		print STDERR "Filter against osm track $track_no($track_count) ".mem_usage();
		print STDERR time_estimate($start_time,$track_points_done,$point_count);
		print STDERR "\r";
	    }
	}
    }

    my $new_tracks = tracks_only_good_point($tracks);

    print_count_data($new_tracks,"after Filtering against existing OSM Data");
    print_time($start_time);
    return $new_tracks;
}

##################################################################
package GPSDrive;
##################################################################
use strict;
use warnings;

use Date::Parse;
use Data::Dumper;

use Geo::GPX::File;
use Geo::Geometry;
use Geo::OSM::SegmentList;
use Geo::OSM::Tracks2OSM;
use Geo::OSM::Write;
use Geo::Tracks::GpsBabel;
use Geo::Tracks::Kismet;
use Geo::Tracks::NMEA;
use Geo::Tracks::TRK;
use Geo::Tracks::Netmonitor;
use Geo::Tracks::Tools;
use Utils::Debug;
use Utils::File;
use Utils::Math;

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

    printf STDERR ("Reading $filename\n") if $VERBOSE || $DEBUG;
    printf STDERR "$filename:	".(-s $filename)." Bytes\n" if $DEBUG;

    my $fh = data_open($filename);
    return $new_tracks  unless $fh;

    my $new_track = [];
    while ( my $line = $fh->getline() ) {
	chomp $line;
	#printf STDERR "$line\n";
	$line =~ s/^\s*//;
	#my ($lat,$lon,$alt,$time) = ($line =~ m/\s*([\+\-\d\.]+)\s+([\+\-\d\.]+)\s+([\+\-\d\.]+)\s+(.*)/);
	my ($lat,$lon,$alt,$time) = split(/\s+/,$line,4);
	printf STDERR "(lat: $lat,lon: $lon, alt:$alt, time: $time)\n" if $DEBUG>1;
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
    if ( $VERBOSE >1 ) {
	printf STDERR "Read and parsed $filename";
	print_time($start_time);
    }

    return $new_tracks;
}


my $GPSDRIVE_CONFIG_DIR          = "$ENV{'HOME'}/.gpsdrive";
my $GPSDRIVE_WAYPT_FILE          = "$GPSDRIVE_CONFIG_DIR/way.txt";
######################################################################
# read a GpsDrive-Waypoint from the .gpsdrive/way.txt File
my $waypoints={};
sub get_waypoint($) {
    my $waypoint_name = shift;
    
    # Look it up if it's cached?
    if( defined ( $waypoints->{$waypoint_name} )){
	return @{$waypoints->{$waypoint_name}};
    }
    # If they give just a filename, we should assume they meant the CONFIG_DIR
    $GPSDRIVE_WAYPT_FILE = "$GPSDRIVE_CONFIG_DIR/$GPSDRIVE_WAYPT_FILE" unless ($GPSDRIVE_WAYPT_FILE =~ /\//);
    
    open(WAYPT,"$GPSDRIVE_WAYPT_FILE") || die "ERROR: get_waypoint Can't open: $GPSDRIVE_WAYPT_FILE: $!\n";
    my ($name,$lat,$lon, $typ,$wlan, $action, $sqlnr, $proximity);
    while (<WAYPT>) {
	chomp;
	next unless (/$waypoint_name/);
	($name,$lat,$lon, $typ, $wlan, $action, $sqlnr, $proximity) = split(/\s+/);
    }
    close(WAYPT);
    unless (($lat) && ($lon)) {
	printf STDERR "Unable to find waypoint '$waypoint_name' in '$GPSDRIVE_WAYPT_FILE'\n";
	exit;
    }
    $waypoints->{$waypoint_name} = [$lat,$lon];
    return ($lat,$lon,$proximity/1000);
} #End get_waypoint

##################################################################
package GPS;
##################################################################
use strict;
use warnings;

use Date::Parse;
use Data::Dumper;
use Math::Trig;
use Carp;

use Geo::GPX::File;
use Geo::Geometry;
use Geo::OSM::SegmentList;
use Geo::OSM::Tracks2OSM;
use Geo::OSM::Write;
use Geo::Tracks::GpsBabel;
use Geo::Tracks::Kismet;
use Geo::Tracks::NMEA;
use Geo::Tracks::TRK;
use Geo::Tracks::Tools;
use Utils::Debug;
use Utils::File;
use Utils::Math;

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

    printf STDERR "Reading Filter File $filename\n" if $VERBOSE || $DEBUG;
    printf STDERR "$filename:	".(-s $filename)." Bytes\n" if $DEBUG;

    print STDERR "Parsing file: $filename\n" if $DEBUG;
    my $p = XML::Parser->new( Style => 'Objects' ,
			      );
    
    my $fh = data_open($filename);
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
    if ( $DEBUG ) {
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
	if ( $DEBUG || $VERBOSE );

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
	push ( @filter_area_files, $GPSDRIVE_WAYPT_FILE) if -s $GPSDRIVE_WAYPT_FILE;
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
	if ( $DEBUG >30 ) {
	    print "internal__filter_area_list:".Dumper(\$internal__filter_area_list);
	}
    }
    print STDERR Dumper(\$internal__filter_area_list) if $DEBUG>2 || $VERBOSE>5;
}

# -------------------------------------------------
# Check given Element against all defined area-filters
sub check_allowed_area($$){
    my $elem = shift;
    my $filter_area_list = shift;
    
    return 1 unless @filter_area_files;
    if ( ref($filter_area_list) ne "ARRAY" ) {
	confess("check_allowed_area(): Filter list must be an array");
    };
    unless ( defined($elem->{lat}) && defined($elem->{lon}) ) {
	confess("check_allowed_area(): Unknown Type of Element to check:".Dumper(\$elem));
    };
    
    # print "check_allowed_area():".Dumper(\$elem).Dumper(\$internal__filter_area_list);
    for my $area ( @{$filter_area_list} ) {
	if (ref($area) eq "HASH" ) {
	    #print "check_allowed_area(HASH):".Dumper(\$elem).Dumper(\$area);
	    if ( defined ( $area->{wp} ) ) { # Get from GPSDrive ~/.gpsdrive/way.txt Waypoints
		my $proximity;
		($area->{lat},$area->{lon},$proximity) = GPSDrive::get_waypoint($area->{wp});
		$area->{proximity} ||= $proximity;
		$area->{proximity} ||= 10;
	    }
	    
	    if ( distance_point_point_Km($area,$elem) < $area->{proximity} ) {
		printf STDERR "check_allowed_area(".$elem->{lat}.",".$elem->{lon}.
		    ") -> WP: $area->{wp} : block: $area->{block}\n"
		    if $DEBUG > 30;
		return ! $area->{block};
	    }
	} else {
	    #print "check_allowed_area(ARRAY):".Dumper(\$elem).Dumper(\$area);
	    my ($min_lat,$min_lon, $max_lat,$max_lon ) = @{$area};
	    if ( $min_lat <= $elem->{lat} &&	 $max_lat >= $elem->{lat} &&
		 $min_lon <= $elem->{lon} &&	 $max_lon >= $elem->{lon} ) {
		if ( $DEBUG >30) {
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
# Add things like speed, time, .. to the GPS Data
# and the split the tracks if we think it's necessary
sub split_tracks($$){
    my $tracks      = shift; # reference to tracks list
    my $config      = shift;

    my $start_time=time();

    my $filename=$tracks->{filename};

    my $max_allowed_speed = $config->{max_speed} || 200; # 200 Km/h
    my $max_allowed_dist  = $config->{max_dist}  || 500; # 0.5 Km
    my $max_allowed_time  = $config->{max_time}  || 60;  # 1 Minute

    my $track_number=0;
    enrich_tracks($tracks);
    if ( $DEBUG>3) {
	my ($track_count,$point_count) = count_data($tracks);
	print_count_data($tracks,"before splitting");
    }
    for my $track ( @{$tracks->{tracks}} ) {
	my $max_pos = $#{@{$track}};
	for  ( my $track_pos=0; $track_pos <= $max_pos;$track_pos++ ) {
	    my $elem = $track->[$track_pos];
	    unless ( defined($elem->{lat}) && defined($elem->{lon})){
		$elem->{good_point}=0;
		print STDERR "bad point ($track_pos)" if $DEBUG>3;
		next;
	    }
	    if ( $elem->{fix} && $elem->{fix} eq "none" ) {
		$elem->{good_point}=0;
		print STDERR "x ($track_pos)" if $DEBUG>2;
		next;
	    };		
	    next if $track_pos >= $max_pos;

	    $elem->{time} = 0 unless defined $elem->{time};

	    if ( defined($elem->{time_diff}) &&
		 ( $elem->{time_diff} > $max_allowed_time ) ) { # ---- Check for Track Split: time diff
		$elem->{split_track} =1;
		print STDERR "split($track_pos)\n " if $DEBUG>3;
	    }

	    my $dist  = $elem->{dist};   # in Km
	    if ( $dist > $max_allowed_dist) {             # ---- Check for Track Split: xx Km
		$elem->{split_track} =1;
	    }
	    
	    # --------- Speed
	    my $speed = track_point_speed($track,$track_pos);
	    if ( $speed && $speed > $max_allowed_speed) { # ---- Check for Track Split: 200 Km/h
		$elem->{good_point} =0;
	    }
	}
    }

    my $new_tracks = tracks_only_good_point($tracks);

    print_count_data($new_tracks,"after splitting");
    print_time($start_time);
    return $new_tracks;
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
    if ( $DEBUG>10 || ($used_time >5 )) {
	printf STDERR "Bounds are ($lat_min,$lon_min) ($lat_max,$lon_max)";
	print_time($start_time);
    }

    return { lat_min => $lat_min,lon_min => $lon_min,
	     lat_max => $lat_max,lon_max => $lon_max };
}

# ------------------------------------------------------------------
# Filter tracks with points
# check_allowed_area($elem) tells if this element is added or not
sub filter_track_by_area($){
    my $tracks      = shift; # reference to tracks list

    return unless @filter_area_files;

    my $start_time=time();

    for my $track ( @{$tracks->{tracks}} ) {
	for my $track_pos ( 0 .. $#{@{$track}} ) {
	    my $elem = $track->[$track_pos];
	    $elem->{good_point} = check_allowed_area($elem,$internal__filter_area_list);
	    $elem->{split_track} =! $elem->{good_point};
	}
    }

    my $new_tracks = tracks_only_good_point($tracks);

    print_count_data($new_tracks,"after Filtering Areas");
    print_time($start_time); 
    return $new_tracks;
}


# ------------------------------------------------------------------
# check if a the next n points are combinable and 
# can be replaceable by one segment
sub is_gps_combineable($$){
    my $track    = shift;
    my $track_pos = shift;

    my $count_combine =0;

    my $sum_dist=0;
    my $sum_angle=0;

    my $elem0 = $track->[$track_pos];
    my $dist_over=0;
    my $pos_max = $#{@{$track}};
    for my $track_pos_test ( $track_pos .. $pos_max ) {
	my $elem2 = $track->[$track_pos_test];
	$sum_dist = $sum_dist + $elem2->{dist};
	if ( $sum_dist > 500 ) { # max 500 m distanz
	    printf STDERR "Elements have $sum_dist m Distance, which would be too much\n"
		if $DEBUG >10;
	    last;
	}
	if ( abs ($elem2->{angle}) > 20 ) {
	    printf STDERR "Element $track_pos_test has $elem2->{angle} ° to next elem, which would be too much\n"
		if $DEBUG >10;
	    last;
	}

	$sum_angle +=abs($elem2->{angle});
	if ( $sum_angle > 20 ) {
	    printf STDERR "Elements 0 .. $track_pos_test has $sum_angle ° in summ, which would be too much\n"
		if $DEBUG >10;
	    last;
	}

	$dist_over=0;
	for my $track_pos_test1 ( $track_pos+1 .. $track_pos_test) {
	    my $elem1 = $track->[$track_pos_test1];
	    # Distance between line of line(p0 and p2) to p1 20060810_182203.nmea
	    my $dist = distance_line_point_Km($elem0->{lat},$elem0->{lon},
					      $elem2->{lat},$elem2->{lon},
					      $elem1->{lat},$elem1->{lon}
					      );
	    $dist_over =  1 if $dist > 0.05; # 5 meter
	    printf STDERR "Elem  $track_pos_test1 is $dist m away from line\n"
		if $DEBUG >10;
	    last if $dist_over;
	}
	last if $dist_over;
        $count_combine++;
    }

    return 0 unless $count_combine;
    #print "pos: $track_pos($pos_max) : combine $count_combine Dist: $sum_dist\n";

    return $count_combine;
}

# ------------------------------------------------------------------
# Filter tracks with points
# delete points which are 
# inside a straight line of the point before and after
sub filter_data_reduce_points($){
    my $tracks      = shift; # reference to tracks list

    return unless $do_filter_reduce_pt;

    my $start_time=time();

    enrich_tracks($tracks);
    for my $track ( @{$tracks->{tracks}} ) {
	my $last_track_elem= $#{@{$track}};
	for  ( my $track_pos=0; $track_pos <= $last_track_elem;$track_pos++ ) {
	    my $count_combinable = is_gps_combineable($track,$track_pos);
	    if ( $track_pos+1+$count_combinable >= $last_track_elem ) {
		# Do not remove last point in sub-track
		$count_combinable =  $last_track_elem-$track_pos-2;
	    };
	    if ( $count_combinable>0) {
		set_number_bad_points($track,$track_pos+1,$count_combinable);
		$track_pos+=$count_combinable;
	    }
	}
    }
    
    my $new_tracks = tracks_only_good_point($tracks);

    print_count_data($new_tracks,"after Data Reduce Points ");
    print_time($start_time);
    return $new_tracks;
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
			if $VERBOSE >5 || $DEBUG;
		    next;
		}
		push(@{$wpt_by_name{$name}}, $elem);
	    } else {
		$wpt_by_name{$name} = [ $elem ];
	    }
	    push(@new_wpt,$elem);
	} else {
	    warn "unnamed Waypoint".Dumper(\$elem)."\n" if $VERBOSE || $DEBUG;
	}
    }

    $tracks->{wpt}=\@new_wpt;
}



# Adds a segment to the segment list by providing 
# a track and the position where to get the segment from
sub add_trackpoint2segmentlist($$$){
    my $segment_list = shift;
    my $track = shift;
    my $track_pos = shift;

    my $elem0 = $track->[$track_pos];
    my $elem1 = $track->[$track_pos+1];

    my @segment;
    ($segment[0],$segment[1],$segment[2],$segment[3]) =
	($elem0->{lat},$elem0->{lon},$elem1->{lat},$elem1->{lon});
    $segment[4] = angle_north_relative(
				   { lat => $segment[0] , lon => $segment[1] },
				   { lat => $segment[2] , lon => $segment[3] });
    push (@{$segment_list},\@segment);
}

# ------------------------------------------------------------------
# check if new trackpoints are ner already existing ones
sub filter_dup_trace_segments($$){
    my $tracks       = shift; # reference to tracks list
    my $config       = shift;

    my $dist_old2track = $config->{dist} || 20;
    my $start_time=time();

    my $filename=$tracks->{filename};

    my $bounds = GPS::get_bounding_box($tracks);

    my $count_points = 0;
    my $new_points = 0;
    my $last_compare_dist=0;

    enrich_tracks($tracks);
    my $parsing_display_time=time();
    my ($track_count,$point_count) =   count_data($tracks);
    my $track_points_done=0;

    my $segment_list=[];
    for my $track_no ( 0 .. $#{@{$tracks->{tracks}}} ) {
	my $track = $tracks->{tracks}->[$track_no];
	next if !$track;
	my $sliding_track_pos=0;
	my $pos_max= $#{@{$track}};
	for my $track_pos ( 1 .. $pos_max ) {
	    my $elem = $track->[$track_pos];
	    $elem->{good_point} =
		! OSM::is_segment_of_list_nearby($track,$track_pos,$segment_list);
	    $elem->{split_track} =! $elem->{good_point};

	    my $track_angle=0;
	    while ( ( abs($track_angle = track_part_angle($track,$sliding_track_pos,$track_pos-1)) > 140 ) 
		    && ($sliding_track_pos<$track_pos-5)
		    && track_part_distance($track,$sliding_track_pos,$track_pos-1)>100) {
		add_trackpoint2segmentlist($segment_list,$track,$sliding_track_pos);
		$sliding_track_pos ++;
		print STDERR "Track: $track_no ".
		    "sliding_track_pos: $sliding_track_pos/".($sliding_track_pos-$track_pos).
		    " ($track_pos,$pos_max)	track_angle: $track_angle lets me assume a turn\n" 
		    if $DEBUG > 5;
	    }
	    if ( ( $track_pos - $sliding_track_pos ) > 140 ) {
		#add_trackpoint2segmentlist($segment_list,$track,$sliding_track_pos);
		#$sliding_track_pos ++;

		# TODO: Immer nur dann kopieren, wenn der 
		# winkel sich um mehr als 160 Grad geändert hat
	    }
#	    print "Track: $track_no sliding_track_pos: $sliding_track_pos ($pos_max)	track_angle: $track_angle\n";
	    #print Dumper(\@sliding_track_list);
	    $track_points_done++;
	    if ( ( $VERBOSE || $DEBUG ) &&
		 ( time()-$parsing_display_time >.9)
		 )  {
		$parsing_display_time= time();
		print STDERR "Filter dup Trackseg ".mem_usage();
		print STDERR time_estimate($start_time,$track_points_done,$point_count);
		print STDERR "\r";
	    }
	}
	while ( $sliding_track_pos < $pos_max ) {
	    add_trackpoint2segmentlist($segment_list,$track,$sliding_track_pos);
	    $sliding_track_pos ++;
	}

    }

    my $new_tracks = tracks_only_good_point($tracks);

    print_count_data($new_tracks,"after Filtering my own Tracks.");
    print_time($start_time);
    
    return $new_tracks;
}



# ------------------------------------------------------------------
# check if distance between two  trackpoints is 0 Km
sub filter_null_dist($){
    my $tracks       = shift; # reference to tracks list

    my $start_time=time();

    enrich_tracks($tracks);
    for my $track ( @{$tracks->{tracks}} ) {
	next if !$track;
	
	for  ( my $track_pos=1; $track_pos <= $#{@{$track}};$track_pos++ ) {
	    my $elem0  =$track->[$track_pos-1];
	    my $elem1  =$track->[$track_pos];
	    next if $elem0->{lat} !=  $elem1->{lat};
	    next if $elem0->{lon} !=  $elem1->{lon};
	    $elem1->{good_point} =0;
	}

    }

    my $new_tracks = tracks_only_good_point($tracks);
    print_count_data($new_tracks,"after Filtering null distance");
    print_time($start_time);
    return $new_tracks;
}




# ------------------------------------------------------------------
# scan over the elements and return the number of elements which 
# are inside a boundingbox with a specified length
sub find_max_points_in_bbox_for_track($$$){
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
    #print STDERR "Bbox($count)\n" if $DEBUG>1;
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
    # We work with a bounding circle too
    # we just have to return a number of segments which belong to the clew
    # This reduces the cost for this action dramatically
    my $bbox=find_max_points_in_bbox_for_track($track,$track_pos,0.050);

    my $pos_max = $#{@{$track}};
    for my $track_pos_test ( $track_pos .. min($track_pos+$bbox,$pos_max) ) {
	#print "track_pos_test($track_pos): $track_pos_test\n";
	my $elem0 = $track->[$track_pos_test];
	my $elem1 = $track->[$track_pos_test+1];
	last if	$elem0->{dist} > $elem0->{compare_dist};
	last if abs ($elem0->{angle}) < 20;
	last if abs ($elem0->{angle})+abs($elem1->{angle})<70;
	last if abs ($elem0->{angle})<70 && abs($elem1->{angle})<70;
	$count_clew++;
    }

    return 0 if $count_clew < 5;
    #print "pos: $track_pos($pos_max) : clew $count_clew bbox: $bbox\n";
    return $count_clew;
}

# ------------------------------------------------------------------
# check if new trackpoints are on existing duplicate to other gpx tracks
sub filter_gps_clew($$){
    my $tracks       = shift; # reference to tracks list
    my $config       = shift;

    my $start_time=time();

    my $dist_osm_track = $config->{dist} || 40;

    enrich_tracks($tracks);
    my $parsing_display_time=time();
    my ($track_count,$point_count) =   count_data($tracks);
    my $track_points_done=0;
    for my $track ( @{$tracks->{tracks}} ) {
	next if !$track;
	
	for  ( my $track_pos=0; $track_pos <= $#{@{$track}};$track_pos++ ) {
	    my $count_clew = is_gps_clew($track,$track_pos);
	    set_number_bad_points($track,$track_pos,$count_clew);
	    $track_points_done++;
	    if ( ( $VERBOSE || $DEBUG ) &&
		 ( time()-$parsing_display_time >.9)
		 )  {
		$parsing_display_time= time();
		print STDERR "Filter clew ".mem_usage();
		print STDERR time_estimate($start_time,$track_points_done,$point_count);
		print STDERR "\r";
	    }
	}
	for  ( my $track_pos=0; $track_pos <= $#{@{$track}};$track_pos++ ) {
	    my $elem = $track->[$track_pos];
	    $elem->{split_track} =! $elem->{good_point};
	}
    }

    #printf STDERR "Good Points: %d\n",count_good_point($tracks);

    my $new_tracks = tracks_only_good_point($tracks);

    print_count_data($new_tracks,"after Filtering Clews");
    print_time($start_time);
    return $new_tracks;
}



# ------------------------------------------------------------------



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

use strict;
use warnings;

use Geo::GPX::File;
use Geo::Geometry;
use Geo::OSM::SegmentList;
use Geo::OSM::Tracks2OSM;
use Geo::OSM::Write;
use Geo::Tracks::GpsBabel;
use Geo::Tracks::Kismet;
use Geo::Tracks::NMEA;
use Geo::Tracks::TRK;
use Geo::Tracks::Netmonitor;
use Geo::Tracks::Tools;
use Utils::Debug;
use Utils::File;
use Utils::Math;

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

    if ( $use_stdin ) {
	push(@ARGV,"-");
    }
    if ( @ARGV < 1){
	printf STDERR "Need Filename(s) to convert\n";
	printf STDERR "use: osmfilter.pl -h for more help\n";
	exit 1;
    }

    GPS::read_all_filter_areas(@filter_area_files);

    
    my $osm_segments;
    if ( $do_filter_against_osm ) {
#	$osm_segments = load_segment_list($do_filter_against_osm);
    }
    

    
    my $count_files_converted=0;
    while ( my $filename = shift @ARGV ) {
	my $new_tracks;
	if ( $filename =~ m/\*/ ){
	    printf STDERR "$filename: Skipping for read. Filename has wildcard.\n\n";
	    next;
	};
	if ( ( $filename =~ m/-raw(|-osm|-pre-osm|-pre-clew)\.gpx$/ ) ||
	     ( $filename =~ m/-converted\.gpx(\.gz|\.bz2)?$/ ) ||
	     ( $filename =~ m/-combination.*\.gpx(\.gz|\.bz2)?$/ ) ||
	     ( $filename =~ m/00_combination\.gpx(\.gz|\.bz2)?$/ ) ||
	     ( $filename =~ m/00_filter_areas\.gpx(\.gz|\.bz2)?$/ )  
	     ){
	    printf STDERR "$filename: Skipping for read. These are my own files.\n\n";
	    next;
	}

	if ( ! -s $filename ) {
	    printf STDERR "$filename: Skipping for read. Cannot be found.\n\n";
	    next;
	    
	}

	my ( $extention ) = ( $filename =~ m/\.([^\.]+)(\.gz|\.bz2)?$/ );
	printf STDERR "$filename has extention '$extention'\n" if $DEBUG>1;
	if ( $filename eq '-' ) {
	    $new_tracks = read_gpx_file($filename);
	} elsif ( $filename =~ m/^gpsbabel:(\S+):(\S+)$/ ) {
	    my ($type,$name) = ($1,$2);
	    $new_tracks = read_track_GpsBabel($name,$type);    
	} elsif ( $extention eq "gps" ) {
	    $new_tracks = read_kismet_file($filename);
	} elsif ( $extention eq "gpx" ) {
	    $new_tracks = read_gpx_file($filename);
	} elsif ( $extention eq "mps" ) {
	    $new_tracks = read_track_GpsBabel($filename,"mapsource");
	} elsif ( $extention eq "gdb" ) {
	    $new_tracks = read_track_GpsBabel($filename,"gdb");
	} elsif ( $extention eq "ns1" ) {
	    $new_tracks = read_track_GpsBabel($filename,"netstumbler");
	} elsif ( $extention eq "nmea" ) {
	    $new_tracks = read_track_NMEA($filename);
	} elsif ( $extention eq "trk" ) {
	    $new_tracks = read_track_TRK($filename); # Aldi Tevion Navigation Unit
	} elsif ( $extention eq "TXT" ) { # This is the NAVI-GPS extention
	    $new_tracks = read_track_NMEA($filename);
	} elsif ( $extention eq "sav" ) {
	    $new_tracks = GPSDrive::read_gpsdrive_track_file($filename);
	} elsif ( $extention eq "log" ) {
	    $new_tracks = read_track_Netmonitor($filename); # "Netmonitor" www.nobbi.com
	} else {
	    warn "$filename: !!! Skipping because of unknown Filetype ($extention) for reading\n";
	    next;
	}
	my ($track_read_count,$point_read_count) =   count_data($new_tracks);
	if ( $VERBOSE || $DEBUG) {
	    my $comment = "read";
	    printf STDERR "%-35s: %5d Points in %d Tracks $comment",$filename,$point_read_count,$track_read_count;
	    print  STDERR "\n";
	}

	if ( $out_raw_gpx ){
	    my $new_gpx_file = "$filename-raw.gpx";
	    $new_gpx_file =~s/.gpx-raw.gpx/-raw.gpx/;
	    Geo::GPX::File::write_gpx_file($new_tracks,$new_gpx_file);

	    add_tracks($all_raw_tracks,$new_tracks);
	};
		
	$new_tracks = GPS::filter_null_dist( $new_tracks );

	if ( @filter_area_files ) {
	    $new_tracks = GPS::filter_track_by_area($new_tracks);
	    debug_write_track($new_tracks,"-post-filter_track_by_area");
	}

	if ( $split_tracks ) {
	    debug_write_track($new_tracks,"-pre-split_tracks-max_speed_200");
	    $new_tracks = GPS::split_tracks($new_tracks,
					{ max_speed => 200, # 200   Km/h
					  max_dist  => 500, #   0.5 Km
					  max_time  => 60,  #  60   sec
					});
	    debug_write_track($new_tracks,"-post-split_tracks-max_speed_200");
	}

	if ( $do_filter_clew ) {
	    debug_write_track($new_tracks,"-pre-filter_gps_clew");
	    $new_tracks = GPS::filter_gps_clew( $new_tracks,
					      { dist => 30 });
	    debug_write_track($new_tracks,"-post-filter_gps_clew");
	};

	if ( $do_filter_dup_trace_segments ) {
	    debug_write_track($new_tracks,"-pre-filter_dup_trace_segments");
	    $new_tracks = GPS::filter_dup_trace_segments( $new_tracks,{});
	    debug_write_track($new_tracks,"-post-filter_dup_trace_segments");
	};

	if ( $do_filter_against_osm ) {
	    debug_write_track($new_tracks,"-pre-filter_against_osm");
	    $new_tracks = OSM::filter_against_osm( $new_tracks,$do_filter_against_osm,
					      { dist => 30 });
	    debug_write_track($new_tracks,"-post-filter_against_osm");
	};

	if ( $split_tracks ) {
	    debug_write_track($new_tracks,"-pre-split_tracks-max_speed_200b");
	    $new_tracks = GPS::split_tracks($new_tracks,
					{ max_speed => 200, # 200   Km/h
					  max_dist  => 500, #   0.5 Km
					  max_time  => 60,  #  60   sec
					});
	    debug_write_track($new_tracks,"-post-split_tracks-max_speed_200b");
	}

	if ( $do_filter_reduce_pt ) {
	    debug_write_track($new_tracks,"-pre-filter_data_reduce_points");
	    $new_tracks = GPS::filter_data_reduce_points($new_tracks);
	    debug_write_track($new_tracks,"-post-filter_data_reduce_points");
	}

	my ($track_count,$point_count);
	($track_count,$point_count) =   count_data($new_tracks);
	$count_files_converted ++ if $point_count && $track_count;

	if ( $track_count > 0 ) {
	    my $new_gpx_file = "$filename-converted.gpx";
	    $new_gpx_file =~s/.gpx-converted.gpx/-converted.gpx/;
	    write_gpx_file($new_tracks,$new_gpx_file)
		if $single_file;
	    
	    if ( $out_osm ){
		my $new_osm_file = "$filename.osm";
		my $osm = tracks2osm($new_tracks);
		write_osm_file($new_osm_file,$osm)
		}
	} else {
	    printf STDERR "%-35s:No resulting Tracks so nothing is written\n",$filename;
	}

	add_tracks($all_tracks,$new_tracks);
	if ( $point_count && $track_count ) {
	    printf STDERR "%-35s:	",$filename;
	    printf STDERR "%5d(%5d) Points in %3d(%3d) Tracks added\n",
	    $point_count,$point_read_count,$track_count,$track_read_count;

	}
	if ( $VERBOSE || $DEBUG ) {
	    printf STDERR "\n";
	}
    }

    GPS::filter_duplicate_wpt($all_tracks);
    

    if ( $count_files_converted ) {
	    print_count_data($all_tracks,"after complete processing");
	    print_time($start_time);
	    
	    if (  $out_osm ) {
		my $osm = tracks2osm($all_tracks);
		write_osm_file("00_combination.osm",$osm);
	    }
	    
	    if ( $use_stdout ) {
		write_gpx_file($all_tracks,'-');
	    } else {
		write_gpx_file($all_tracks,"00_combination.gpx");
		if ( $out_raw_gpx ){
		    write_gpx_file($all_raw_tracks,"00_combination-raw.gpx");
		}
		if ( $out_upload_gpx ){
		    my $mem1= $out_upload_gpx;
		    my $mem2 = $fake_gpx_date;
		    $out_upload_gpx=0;
		    $fake_gpx_date=1;
		    write_gpx_file($all_raw_tracks,"00_combination-upload.gpx");
		    $out_upload_gpx=$mem1;
		    $fake_gpx_date=$mem2;
		}
	    };
	    
	} else {
	    if ( $VERBOSE || $DEBUG ) {
		print STDERR "No files Converted; so nothing written\n";
	    }
	}
    
    if ( $draw_filter_areas ) {
	my $filter_areas = GPS::draw_filter_areas();
	write_gpx_file($filter_areas,"00_filter_areas.gpx");
    }

    if ( $VERBOSE) {
	printf STDERR "Converting $count_files_converted Files";
	print_time($start_time);
    }
}

# ------------------------------------------------------------------

# Set defaults and get options from command line
Getopt::Long::Configure('no_ignore_case');
GetOptions ( 
	     'debug:+'              => \$DEBUG,      
	     'd:+'                  => \$DEBUG,      
	     'verbose:+'            => \$VERBOSE,
	     'v:+'                  => \$VERBOSE,
	     'MAN'                  => \$man, 
	     'man'                  => \$man, 
	     'h|help|x'             => \$help, 

	     'stdin'                => \$use_stdin,
	     'stdout'               => \$use_stdout,
	     'proxy=s'              => \$PROXY,

	     'out-osm!'             => \$out_osm,
	     'out-raw-gpx!'         => \$out_raw_gpx,
	     'out-upload-gpx!'      => \$out_upload_gpx,
	     'split-tracks!'        => \$split_tracks,
	     'filter-against-osm:s' => \$do_filter_against_osm,
	     'osm:s'                => \$do_filter_against_osm,
             'filter-dup-seg!'      => \$do_filter_dup_trace_segments,
	     'filter-clew!'         => \$do_filter_clew,
	     'filter-reduce!'       => \$do_filter_reduce_pt,
	     'filter-area:s@'       => \@filter_area_files,
	     'generate_ways!'       => \$generate_ways,
	     'filter-all'           => \$do_all_filters,
	     'fake-gpx-date!'       => \$fake_gpx_date,
	     'write-gpx-wpt!'       => \$write_gpx_wpt,
	     'draw_filter_areas!'   => \$draw_filter_areas,
	     )
    or pod2usage(1);

if ( $do_all_filters ) {
    $out_osm               = 1 unless defined $out_osm;
    $out_raw_gpx           = 1 unless defined $out_raw_gpx;
    $out_upload_gpx        = 1 unless defined $out_upload_gpx;
    $split_tracks          = 1 unless defined $split_tracks;
    $do_filter_reduce_pt   = 1 unless defined $do_filter_reduce_pt;
    $do_filter_clew        = 1 unless defined $do_filter_clew;
    $do_filter_against_osm = 1 unless defined $do_filter_against_osm;
    $do_filter_dup_trace_segments =1 unless defined $do_filter_dup_trace_segments;
    @filter_area_files || push(@filter_area_files,"");
}

$fake_gpx_date ||=0;

pod2usage(1) if $help;
pod2usage(-verbose=>2) if $man;

convert_Data();

print "\n";

##################################################################
# Usage/manual

__END__

=head1 NAME

B<osmfilter.pl> Version 0.05

=head1 DESCRIPTION

B<osmtrackfilter.pl> is a program to convert and filter Track Files 
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

The Idea behind the osm-track-filter is:
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
 - Then osm-track-filter then enriches the Data for internal use by adding:
	- speed of each segment (if necessary)
        - distance to last point 
        - angle to last segment (Which would represent steering wheel angle)
 - Then osm-track-filter is splitting the tracks if necessary.
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
   osm-track-filter tries to determin continuous ways by looking 
   at the angle to the last segment, the speed and distance.

 - This is now done for all input Files. So you can also use 
   multiple input files as source and combine them to ine large 
   output File.

 - After this all now existing data iw written to a gpx file.
 - If you add the option  --out-osm. osm-track-filter tries 
   to generate an *.osm file out of this Data.

=head1 SYNOPSIS

B<Common usages:>

osmtrackfilter.pl [--man] [-d] [-v] [-h][--out-osm] [--limit-area] <File1.gps> [<File2.sav>,<File2.ns1>,...]

!!!Please be carefull this is still a beta Version. 
   Make Backups of your valuable source Files
   and never upload the created data without first checking it!!!

=head1 OPTIONS

=over 2

=item B<--man> Complete documentation

This shows the Complete documentation

=item B<--out-raw-gpx>

Write raw converted output to filename-raw.gpx

=item B<--out-upload-gpx>

Write raw converted output to filename-upload.gpx. 
Without timestamps and without Waypoints.

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

=item B<--filter-against-osm> |  B<--osm>

This loads the osm.csv and checks if the 
track-points already exist as an osm-segment.
It checks if any of the points are near (<20m) any  
of the osm-segments. 
And the OSM Segment and the track segments have an 
angle of less than 30 Degrees.
If so they will be dropped.

The file osm.csv is the segments filealso used by osm-pdf-atlas.
It's containing all osm-segments from planet.osm in a rather simple way.
The probably easiest way to create it is to have the debian package 
openstreetmap-utils installed and then call something like:
   osm2csv --area=germany

If you provide a filename for the --osm option, this file is read instead
of the osm.csv file. 
The Filename provided can be a csv-file or a standard osm-file.

you can use --osm=0 in combination with --filter-all to not let osm-track-filter 
check against osm Data.

=item B<--filter-dup-seg>

This Filter checks for points near an already existing Trackline. 
If so it is removed

Currently it's only a stub, so anyone can sit down and programm the compare routine.

=item B<--filter-reduce>

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

=item B<--filter-clew>

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
Default is on.

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
   - Tevion Tracks     *.trk
   - Netmonitor Tracks *.log
   - via gpsbabel gpsbabel:<type>:*

For each File read a File *-converted.gpx will be written
All input filenames ending with -converted.gpx will be skiped.

To read all Files in a specified directory at once do the following:

 find <kismet_dir>/log -name "*.gps" | xargs ./osmtrackfilter.pl

If you define multiple Files a summary File will automagically be written:
 ./00_combination.gpx

=item B<--stdin>

use stdin to read GPX track file

=item B<--stdout>

use stdout to write filtered  tracks as GPX

=back

=head1 COPYRIGHT

Copyright (C) 2006,2007, and GNU GPL'd, by Joerg Ostertag

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

=head1 AUTHOR

Jörg Ostertag (osmfilter-for-openstreetmap@ostertag.name)

=head1 SEE ALSO

http://www.openstreetmap.org/

=cut
