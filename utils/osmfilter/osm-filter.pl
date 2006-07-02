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
# Output is:
#   - OSM File for josm *.osm ( http://openstreetmap.org/)
#   - GPX File for josm *.gpx
#   - _collection.gpx, _collection.osm (one File wit all good tracks)
#
# Joerg Ostertag <openstreetmap@ostertag.name>

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
use Storable ();
use XML::Parser;

my ($man,$help);
our $debug =0;
our $verbose =0;
our $no_mirror=0;
our $PROXY='';


my $osm_nodes        = {};
my $osm_nodes_duplicate   = {};
my $osm_segments     = {};
my $osm_segments_duplicate ={};
my $osm_ways          = {};
my $osm_stats         = {};
my $osm_obj           = undef; # OSM Object currently read
my $out_osm           = 0;
my $use_area_limit    = 0;
my $use_reduce_filter = 0;
my $draw_check_areas  = 0;
my $generate_ways =0;

my $first_id=10000;
my $next_osm_node_number    = $first_id;
my $osm_segment_number = $first_id;
my $osm_way_number     = $first_id;



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


    printf "distance_line_point(%f,%f, %f,%f,   %f,%f)\n", $x1, $y1, $x2, $y2,  $xp, $yp
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
    printf "distance_line_point(): lambda_1: %f\n",$lambda
	if ( $debug > 10 );

    $lambda = Geometry::min(Geometry::max($lambda,0.0),1.0);
    
    printf "distance_line_point(): lambda: %f\n",$lambda
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
    my $file_name = shift;

    my $file_with_path="$file_name";
    my $size = (-s $file_with_path)||0;
    if ( $size < 270 ) {
	warn "cannot Open $file_name ($size) Bytes is too small)\n"
	    if $verbose || $debug;
	return 0;
    }

    print "Opening $file_with_path" if $debug;
    my $fh;
    if ( $file_with_path =~ m/\.gz$/ ) {
	$fh = IO::File->new("gzip -dc $file_with_path|")
	    or die("cannot open $file_with_path: $!");
    } elsif ( $file_with_path =~ m/\.bz2$/ ) {
	    $fh = IO::File->new("bzip2 -dc $file_with_path|")
		or die("cannot open $file_with_path: $!");
	} else {
	    $fh = IO::File->new("<$file_with_path")
		or die("cannot open $file_with_path: $!");
	}
    return $fh;
}

##################################################################
package Kismet;
##################################################################
use Date::Parse;
use Data::Dumper;
# -----------------------------------------------------------------------------
# Read GPS Data from Kismet File
sub read_gps_file($) { 
    my $file_name = shift;

    my $start_time=time();

    print("Reading $file_name\n") if $verbose || $debug;
    print "$file_name:	".(-s $file_name)." Bytes\n" if $debug;

    print STDERR "Parsing file: $file_name\n" if $debug;
    my $p = XML::Parser->new( Style => 'Objects' ,
			      );
    
    my $fh = File::data_open($file_name);
    return unless $fh;
    my $content = [{Kids => []}];
    eval {
	$content = $p->parse($fh);
    };
    if ( $@ ) {
	warn "$@Error while parsing\n $file_name\n";
	#print Dumper(\$content);
	#return $content->[0]->{Kids};
    }
    if (not $p) {
	print STDERR "WARNING: Could not parse osm data\n";
	return;
    }
    if ( $debug ) {
	printf "Read and parsed $file_name in %.0f sec\n",time()-$start_time;
    }
    my $track=[];
    for my $elem  ( @{$content->[0]->{Kids}} ) {
	next unless ref($elem) eq "Kismet::gps-point";
	next unless $elem->{'bssid'} eq 'GP:SD:TR:AC:KL:OG';
	delete $elem->{Kids};
	if ( defined($elem->{"time-sec"}) && defined($elem->{"time-usec"}) ) {
	    $elem->{time} = $elem->{"time-sec"}.".".$elem->{"time-usec"};
	}
	delete $elem->{'time-sec'};
	delete $elem->{'time-usec'};
	delete $elem->{'bssid'};
	delete $elem->{'signal'};
	delete $elem->{'quality'};
	delete $elem->{'noise'};
	$elem->{'speed'} = delete($elem->{'spd'}) * 1;
	if ( $debug > 10 ) {
	    print Dumper(\$elem);
	}
	push(@{$track},$elem);
    };
    
#    print Dumper(\$track);

    return [$track];
}


##################################################################
package Gpsbabel;
##################################################################
use IO::File;


# -----------------------------------------------------------------------------
# Read GPS Data from GPX - File
sub read_file($$) { 
    my $file_name     = shift;
    my $gpsbabel_type = shift;

    print("Reading $file_name\n") if $verbose || $debug;
    print "$file_name:	".(-s $file_name)." Bytes\n" if $debug;

    my $fh = IO::File->new("gpsbabel  -i $gpsbabel_type -f \"$file_name\" -o gpx -F - |");
    if ( !$fh )  {
	warn "Cannot Convert $file_name as Type $gpsbabel_type\n";
	return [];
    }
    GPX::read_gpx_file($fh);
}

##################################################################
package GPX;
##################################################################
use Date::Parse;
use Data::Dumper;
use Date::Parse;
use Date::Manip;

# -----------------------------------------------------------------------------
# Read GPS Data from GPX - File
sub read_gpx_file($) { 
    my $file_name = shift;

    my $start_time=time();
    my $fh;

    if ( ref($file_name) =~ m/IO::File/ ) {
	$fh = $file_name;
    } else {
	print("Reading $file_name\n") if $verbose || $debug;
	my $size = (-s $file_name) || 0;
	print "$file_name:	$size Bytes\n" if $debug;

	print STDERR "Parsing file: $file_name\n" if $debug;
	$fh = File::data_open($file_name);
    }
    return unless $fh;

    my $p = XML::Parser->new( Style => 'Objects' ,
			      );
    
    my $content = [{Kids => []}];
    eval {
	$content = $p->parse($fh);
    };
    if ( $@ ) {
	warn "$@Error while parsing\n $file_name\n";
	#print Dumper(\$content);
	#return $content->[0]->{Kids};
    }
    if (not $p) {
	print STDERR "WARNING: Could not parse osm data\n";
	return;
    }
    if ( $verbose) {
	printf "Read and parsed $file_name in %.0f sec\n",time()-$start_time;
    }
    $content = $content->[0]->{Kids};

    my $new_tracks=[];
    for my $elem ( @{$content} ) {
	next unless ref($elem) eq "GPX::trk";
	#	    GPX::trkseg
	$elem = $elem->{Kids};
	#print Dumper(\$elem);
	my $new_track=[];
	for my $trk_elem ( @{$elem} ) {
	    next unless ref($trk_elem) eq "GPX::trkseg";
	    $trk_elem = $trk_elem->{Kids};
	    #print Dumper(\$trk_elem);
	    for my $trk_pt ( @{$trk_elem} ) {
		next unless ref($trk_pt) eq "GPX::trkpt";
		#print "Track Point:".Dumper(\$trk_pt);
		for my $trk_pt_kid ( @{$trk_pt->{Kids}} ) {
		    next if ref($trk_pt_kid) eq "GPX::Characters";
		    #print "Track Point Kid:".Dumper(\$trk_pt_kid);
		    my $ref = ref($trk_pt_kid);
		    my ( $type ) = ($ref =~ m/GPX::(.*)/ );
		    $trk_pt->{$type} = $trk_pt_kid->{Kids}->[0]->{Text};
		}
		if ( defined $trk_pt->{time} ) {
		    my $time = str2time( $trk_pt->{time});
		    $trk_pt->{time_string} = $trk_pt->{time};
		    $trk_pt->{time} = $time;
		}

		delete $trk_pt->{Kids};
		#print "Final Track Point:".Dumper(\$trk_pt);
		push(@{$new_track},$trk_pt);
	    }
	}
	push(@{$new_tracks},$new_track);
    }

    #print Dumper(\$new_tracks);
    return $new_tracks;
}

#------------------------------------------------------------------
sub write_gpx_file($$) { # Write an gpx File
    my $tracks = shift;
    my $file_name = shift;

    my $start_time=time();

    print("Writing GPS File $file_name\n") if $verbose || $debug;

    my $fh = IO::File->new(">$file_name");
    print $fh "<gpx \n";
    print $fh "    version=\"1.0\"\n";
    print $fh "    creator=\"osm-filter Converter\"\n";
    print $fh "    xmlns=\"http://www.ostertag.name\"\n";
    print $fh "    >\n";
    my $track_id=0;
    my $point_count=0;
    for my $track ( @{$tracks} ) {
	$track_id++;
	print $fh "\n";
	print $fh "<trk>\n";
	print $fh "   <name>$file_name $track_id</name>\n";
	print $fh "   <number>$track_id</number>\n";
	print $fh "    <trkseg>\n";

	for my $elem ( @{$track} ) {
	    $point_count++;
	    my $lat  = $elem->{lat};
	    my $lon  = $elem->{lon};
	    print $fh "     <trkpt lat=\"$lat\" lon=\"$lon\">\n";
	    if( defined $elem->{ele} ) {
		print $fh "       <ele>$elem->{ele}</ele>\n";
	    };
	    if ( defined ( $elem->{time} ) ) {
		$elem->{time_sec}=int($elem->{time});
		$elem->{time_usec}=$elem->{time}-$elem->{time_sec};
		my $time = UnixDate("epoch ".$elem->{time_sec},"%m/%d/%Y %H:%M:%S");
		$time .= ".$elem->{time_usec}" if $elem->{time_usec};
		#$time = "2004-11-12T15:04:40Z";
		if ( $debug >20) {
		    print "elem-time: $elem->{time} UnixDate: $time\n";
		}
		print $fh "       <time>".$time."</time>\n";
	    }
	    if( defined $elem->{fix} ) {
		print $fh "       <fix>$elem->{fix}</fix>\n";
	    };
	    print $fh "     </trkpt>\n";
	}
	print $fh "    </trkseg>\n";
	print $fh "</trk>\n\n";
    
    }

    print $fh "</gpx>\n";
    $fh->close();

    printf "Wrote GPX File $file_name ($track_id Tracks with $point_count Points)in  %.0f sec\n",time()-$start_time;
}

#------------------------------------------------------------------
sub write_osm_data_as_gpx_file($) { # Write an gpx File
    my $tracks = shift;
    my $file_name = shift;

    my $start_time=time();

    print("Writing GPS File $file_name\n") if $verbose || $debug;

    my $fh = IO::File->new(">$file_name");
    print $fh "<gpx version=\"1.0\" creator=\"osm-filter Converter\" xmlns=\"http://www.ostertag.name\">\n";
    # --- Segments
    for my $seg_id (  sort keys %{$osm_segments} ) {
	next unless $seg_id;
	print $fh "\n<trk>\n";
	print $fh "    <number>$seg_id</number>\n";
	#print $fh "<extensions>\n";
	#print $fh "<property key=\"foot\"  value=\"no\"/>\n";
	#print $fh "<property key=\"horse\" value=\"no\"/>\n";
	#print $fh "<property key=\"bike\"  value=\"no\"/>\n";
	#print $fh "<property key=\"car\"   value=\"no\"/>\n";
	#print $fh "<property key=\"class\" value=\"\"/>\n";
	#print $fh "</extensions>\n";

	my $segment = $osm_segments->{$seg_id};
	my $node_from = $segment->{from};
	my $node_to   = $segment->{to};
	
	print $fh "<trkseg>\n";
	for my $node_id ( $node_from, $node_to)  {
	    my $lat = $osm_nodes->{$node_id}->{lat};
	    my $lon = $osm_nodes->{$node_id}->{lon};
	    
	    print $fh "   <trkpt lat=\"$lat\" lon=\"$lon\">\n";
	    print $fh "     <time>2004-11-12T15:04:40Z</time>\n";
	    print $fh "   </trkpt>\n";
	}
	print $fh "</trkseg>\n";
	#print $fh tags2osm($segment);
	print $fh "</trk>\n\n";
    
    }

    

    print $fh "</gpx>\n";
    $fh->close();

    if ( $verbose) {
	printf "Wrote OSM File in  %.0f sec\n",time()-$start_time;
    }

}


##################################################################
package GPSDrive;
##################################################################
use Date::Parse;
use Data::Dumper;

# -----------------------------------------------------------------------------
# Read GPSDrive Track Data
sub read_gpsdrive_track_file($) { 
    my $file_name = shift;

    my $start_time=time();

    my $content=[];
    print("Reading $file_name\n") if $verbose || $debug;
    print "$file_name:	".(-s $file_name)." Bytes\n" if $debug;

    my $fh = File::data_open($file_name);
    return[] unless $fh;
    while ( my $line = $fh->getline() ) {
	chomp $line;
	#print "$line\n";
	$line =~ s/^\s*//;
	#my ($lat,$lon,$alt,$time) = ($line =~ m/\s*([\+\-\d\.]+)\s+([\+\-\d\.]+)\s+([\+\-\d\.]+)\s+(.*)/);
	my ($lat,$lon,$alt,$time) = split(/\s+/,$line,4);
	print "($lat,$lon,$alt,$time)\n" if $debug;
	my $elem = {
	    lat => $lat, 
	    lon => $lon, 
	    alt => $alt, 
	    time => str2time($time),
	    bssid => 'GP:SD:TR:AC:KL:OG',
	};
	push(@{$content},$elem);
	bless($elem,"Kismet::gps-point");
    }
    if ( $verbose) {
	printf "Read and parsed $file_name in %.0f sec\n",time()-$start_time;
    }

    return [$content];
}


my $CONFIG_DIR          = "$ENV{'HOME'}/.gpsdrive"; # Should we allow config of this?
my $WAYPT_FILE          = "$CONFIG_DIR/way.txt";
######################################################################
my $waypoints={};
sub get_waypoint($) {
    my $waypoint_name = shift;
    
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
	print "Unable to find waypoint '$waypoint_name' in '$WAYPT_FILE'\n";
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
	 
	 # Waypoints from GPSDrive ~/.gpsdrive/way.txt File proximity is circle radius
#     { wp => "Dorfen"  	,proximity => 10  },
     { wp => "Gabi"		,proximity => 4  },
#     { wp => "Gabi"		,proximity => 10  },
#     { wp => "Erding"		},
#     { wp => "Wind3"		},
#     { wp => "Taufkirchen"  	},
#     { wp => "Isen"		},
#     { wp => "Kirchdorf"  	,proximity => 6  },
#     { wp => "Pfaffing"   	,proximity => 15  },
#     { wp => "Kirchheim"	},
	 
	 # Allow Rules for square size areas
	 # min_lat,min_lon max_lat,max_lon,     (y,x)
	 #[ 48.0  , 11.6  , 48.4    , 12.0    ], # München
	 #[ 48.10  , 11.75  , 49.0    , 14.0    ], # Münchner-Osten-
	 #[ -90.0  , -180  , 90.0    , 180   ], # World
	 
	 # The rest of the World is blocked by default
	 
	 ];	

sub check_allowed_area($){
    my $elem = shift;
    
    return 1 unless $use_area_limit;
    
    for my $area ( @{$areas_allowed_squares} ) {
	if (ref($area) eq "HASH" ) {	    
	    if ( defined ( $area->{wp} ) ) { # Get from GPSDrive way.txt Waypoints
		my $proximity;
		($area->{lat},$area->{lon},$proximity) = GPSDrive::get_waypoint($area->{wp});
		$area->{proximity} ||= $proximity;
		$area->{proximity} ||= 10;
	    }
	    
	    if ( Geometry::distance_point_point_Km($area,$elem) < $area->{proximity} ) {
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
sub draw_check_areas(){
    my $new_tracks = [];
    for my $area ( @{$areas_allowed_squares} ) {
	my $new_track = [];
	if (ref($area) eq "HASH" ) {	    
	    if ( defined ( $area->{wp} ) ) { # Get from GPSDrive way.txt Waypoints
		my $proximity;
		($area->{lat},$area->{lon},$proximity) = GPSDrive::get_waypoint($area->{wp});
		$area->{proximity} ||= $proximity;
	    }
	    
	    my ($lat,$lon,$r) = ($area->{lat},$area->{lon},$area->{proximity}*360/40000);
	    for my $angel ( 0 .. 360 ) {
		my $elem;
		$elem->{lat} = $lat+sin($angel*2*pi/360)*$r;
		$elem->{lon} = $lon+cos($angel*2*pi/360)*$r;
		push(@{$new_track},$elem);
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
	push(@{$new_tracks},$new_track);
    }
    return $new_tracks;
}

# ------------------------------------------------------------------
# Add things like speed, time, .. to the Kismet GPS Data
sub enrich_data($$){
    my $tracks      = shift; # reference to tracks list
    my $comment     = shift;
    my $new_tracks = [];
    my $deleted_points=0;

    my $track_number=0;
    for my $track ( @{$tracks} ) {
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
			    print "Speed diff: old:$elem->{speed} - calc:$new_speed =  $delta_speed\n";
			}
		    } else {
			$elem->{speed} = $new_speed;
		    }
		}

		# --- Check for Track Split
		my $split_track='';
		if ( $elem->{time_diff} > 60) { # Seconds
		    $split_track .= " Delta Time: $elem->{time_diff} sec. ";
		    if ( $debug) {
			#print "Time 0: $prev_elem->{time_string}\n";
			#print "Time 1: $elem->{time_string}\n";
		    }
		    
		}
		if ( $elem->{dist} > 1) { # Km
		    $split_track .= sprintf(" Dist: %.3f Km ",$elem->{dist});
		}

		if ( $elem->{speed} && $elem->{speed} > 200) { # Km/h
		    $split_track .= sprintf(" Speed: %.1f Km/h ",$elem->{speed});
		    if ( $debug) {
			print "prev:".Dumper(\$prev_elem);
			print "".Dumper(\$elem);
		    }
		}

		if (  $split_track ne '' ) {
		    my $num_elem=scalar(@{$new_track});
		    if ( $num_elem  > 1) {
			push(@{$new_tracks},$new_track);
			print "--------------- Splitting" if $debug;
		    } else {
			print "--------------- Dropping" if $debug;
			$deleted_points+=$num_elem;
		    }
		    if ( $debug ) {
			printf "\tTrack Part (%4d Points)\t$split_track\n",$num_elem;
		    }
		    $new_track=[];
		}
	    }
	    push(@{$new_track},$elem);
	    $prev_elem=$elem;
	}


	my $num_elm_in_track = scalar(@{$new_track})||0;
	if ( $num_elm_in_track > 3 ) {
	    push(@{$new_tracks},$new_track);
	} else {
	    $deleted_points += $num_elm_in_track;
	}
	
	if ( $debug || $verbose ) {
	    printf "Enrich Data: Track $track_number from $comment\n";
	    printf "	Distance: %8.2f m .. %8.2f Km \n", $min_dist*1000,$max_dist;
	    printf "	Elements: ".(scalar(@{$track}))."\n",
	}
    }
    if ( $debug || $verbose ) {
	printf "Enrich Data: $comment:\n";
	printf "	Deleted Points: $deleted_points\n"
    }
    #print Dumper(\$new_tracks);
    @{$tracks}=@{$new_tracks};
}

# ------------------------------------------------------------------
# count tracks and points
sub count_data($){
    my $tracks      = shift; # reference to tracks list

    my $count_tracks=0;
    my $count_points=0;

    for my $track ( @{$tracks} ) {
	next if !$track;
	for my $elem ( @{$track} ) {
	    $count_points++;
	}
	$count_tracks++;
    }
    return ( $count_tracks,$count_points);
}

# ------------------------------------------------------------------
# Filter tracks with points
# check_allowed_area($elem) tells if this element is added or not
sub filter_data_by_area($){
    my $tracks      = shift; # reference to tracks list

    return unless $use_area_limit;

    my $new_tracks = [];

    my $good_points=0;
    my $deleted_points=0;
    my $good_tracks=0;
    for my $track ( @{$tracks} ) {
	my $new_track = [];
	for my $elem ( @{$track} ) {

	    my $skip_point =  ! check_allowed_area($elem);

	    if ( $skip_point ) {
		my $num_elem=scalar(@{$new_track});
		if ( $num_elem ) {
		    push(@{$new_tracks},$new_track);
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
	    push(@{$new_tracks},$new_track);
	    $good_tracks++;
	}
    }
    print "Filter by Area: Good Tracks: $good_tracks, GoodPoints: $good_points, deleted_points:$deleted_points\n"
	if $debug || $verbose;
    @{$tracks}=@{$new_tracks};
}


# ------------------------------------------------------------------
# Filter tracks with points
# delete points which are 
# inside a straight line of the point before and after
sub filter_data_reduce_points($){
    my $tracks      = shift; # reference to tracks list

    return unless $use_reduce_filter;

    my $new_tracks = [];

    my $good_points=0;
    my $deleted_points=0;
    my $good_tracks=0;
    for my $track ( @{$tracks} ) {
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
	    if ( $dist_0_2 > .5 ) { # max .5 km distanz
		print "Elem0 und Elem2 have $dist_0_2 Km Distance, which would be too much\n"
		    if $debug >10;
	    } else {
		my $dist = Geometry::distance_line_point_Km($elem0->{lat},$elem0->{lon},
							 $elem2->{lat},$elem2->{lon},
							 $elem1->{lat},$elem1->{lon}
							 );
		$skip_point =  1 if $dist < 0.001;
		print "Elem $i is $dist m away from line\n"
		    if $debug >10;
	    }
	    if ( $skip_point ) {
		print "Delete Element $i\n"
		    if $debug >10;
	    } else {
		push(@{$new_track},$elem1);
		$good_points++;
	    }
	}
	push(@{$new_track},$track->[-1]);
	my $num_elem=scalar(@{$new_track});
	if ( $num_elem ) {
	    push(@{$new_tracks},$new_track);
	    $good_tracks++;
	}
    }
    print "Filter to reduce number of points: Good Tracks: $good_tracks, GoodPoints: $good_points, deleted_points:$deleted_points\n"
	if $debug || $verbose;
    @{$tracks}=@{$new_tracks};
}

# ------------------------------------------------------------------
# add a list of tracks to another 
sub add_tracks($$){
    my $dst_tracks      = shift; # reference to tracks list
    my $src_tracks      = shift; # reference to tracks list

    for my $track ( @{$src_tracks} ) {
	push(@{$dst_tracks},$track);
    }
}



# ------------------------------------------------------------------



##################################################################
package OSM;
##################################################################

use Data::Dumper;

no warnings 'deprecated';

# ------------------------------------------------------------------
sub Tracks2osm($$){
    my $tracks = shift;
    my $reference = shift;
    $reference =~ s,/home/kismet/log/,,;

    my $last_angle         = 999999999;
    my $angle;
    my $way={};
    my $angle_to_last;

    my $node_to   = 0;
    my $node_from = 0;

    my $element_count=0;
    my $count_valid_points=0;

    for my $track ( @{$tracks} ) {
	for my $elem ( @{$track} ) {
	    my $skip_point=0;
	    my $seg_id=0;
	    my $dist=999999999;

	    print "$elem->{lat},$elem->{lon} ".Dumper(\$elem)."\n" 
		unless  defined($elem->{lat}) && defined($elem->{lon}) ;


	    $skip_point =  ! GPS::check_allowed_area($elem);

	    #print Dumper(\$elem)."\n" if $debug;
	    my $pos = "$elem->{lat},$elem->{lon}";
	    $next_osm_node_number++;
	    if ( 0 && $osm_nodes_duplicate->{$pos} ) {
		$node_to   = $osm_nodes_duplicate->{$pos};
		print "Node would $next_osm_node_number pos:$pos already exists as $node_to\n"
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
			print "Duplicate segment $osm_segment_number --> $seg_id\n";
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
    return $count_valid_points;
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

sub write_osm_file($) { # Write an osm File
    my $file_name = shift;

    my $start_time=time();

    print("Writing OSM File $file_name\n") if $verbose || $debug;

    my $fh = IO::File->new(">$file_name");
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
	printf "Wrote OSM File in  %.0f sec\n",time()-$start_time;
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

    my $filename = "/home/kismet/log/gps-Tweety/gps-14.5.2004.txt-ACTIVE_LOG_015.gps";
    my $all_tracks =[];
    my $single_file=( @ARGV ==1 );

    my ($track_count,$point_count);

    if ( @ARGV < 1 ){
	print "Need Filename(s) to convert\n";
	exit 1;
    }

    my $start_time=time();
    
    my $count=0;
    while ( $filename = shift @ARGV ) {
	my $new_tracks;
	if ( $filename =~ m/-converted.gpx$/ ) {
	    print "Skipping File $filename for read\n";
	    next;
	}

	if ( $filename =~ m/\.gps$/ ) {
	    $new_tracks = Kismet::read_gps_file($filename);
	} elsif ( $filename =~ m/\.gpx$/ ) {
	    $new_tracks = GPX::read_gpx_file($filename);
	} elsif ( $filename =~ m/\.mps$/ ) {
	    $new_tracks = Gpsbabel::read_file($filename,"mapsource");
	} elsif ( $filename =~ m/\.gdb$/ ) {
	    $new_tracks = Gpsbabel::read_file($filename,"gdb");
	} elsif ( $filename =~ m/\.ns1$/ ) {
	    $new_tracks = Gpsbabel::read_file($filename,"netstumbler");    
	} elsif ( $filename =~ m/\.sav$/ ) {
	    $new_tracks = GPSDrive::read_gpsdrive_track_file($filename);
	}
	my ($track_read_count,$point_read_count) =   GPS::count_data($new_tracks);
	if ( $verbose || $debug) {
	    printf "Read %5d Points in %d Tracks from $filename\n",$point_read_count,$track_read_count;
	}

	GPS::filter_data_by_area($new_tracks);
	if ( $verbose || $debug) {
	    ($track_count,$point_count) =   GPS::count_data($new_tracks);
	    printf "After Area Filter %5d Points in %d Tracks from $filename\n",$point_count,$track_count;
	}

	GPS::enrich_data($new_tracks,$filename);
	($track_count,$point_count) =   GPS::count_data($new_tracks);
	if ( $verbose || $debug) {
	    printf "Results in  %5d Points in %d Tracks after enriching\n",$point_count,$track_count;
	}

	GPS::filter_data_reduce_points($new_tracks);
	($track_count,$point_count) =   GPS::count_data($new_tracks);
	if ( $verbose || $debug) {
	    printf "Results in  %5d Points in %d Tracks after filtering\n",$point_count,$track_count;
	}


	$count ++ if $point_count && $track_count;


	my $osm_filename = $filename;
	if ( $track_count > 0 ) {
	    my $new_gpx_file = $osm_filename;
	    if ( $new_gpx_file =~ s/\.(sav|gps|gpx|mps|gdb|ns1)$/-converted.gpx/ ) {
		GPX::write_gpx_file($new_tracks,$new_gpx_file)
		    if $single_file;
		};
	    
	    my $new_osm_file = $osm_filename;
	    if ( $new_osm_file =~ s/\.(sav|gps|gpx|mps|gdb|ns1)$/.osm/ ) {
		my $points = OSM::Tracks2osm($new_tracks,$filename);
		# TODO this still writes out all points since beginning
		OSM::write_osm_file($new_osm_file)
		    if $out_osm;
	    }

	}

	GPS::add_tracks($all_tracks,$new_tracks);
	if ( $point_count && $track_count ) {
	    printf "Added:  %5d(%5d) Points in %3d(%3d) Tracks for %s\n",
	    $point_count,$point_read_count,$track_count,$track_read_count,$filename;

	}
    }

    OSM::write_osm_file("00__combination.osm")
	if $out_osm;

    ($track_count,$point_count) =   GPS::count_data($all_tracks);
    printf "Summary:  %5d Points in %d Tracks after enriching\n",$point_count,$track_count;

    my $check_areas = GPS::draw_check_areas();
    GPS::add_tracks($all_tracks,$check_areas);

    GPX::write_gpx_file($all_tracks,"00__combination.gpx");
    if ( $verbose) {
	printf "Converting $count  OSM Files in  %.0f sec\n",time()-$start_time;
    }
}

# ------------------------------------------------------------------

# Set defaults and get options from command line
Getopt::Long::Configure('no_ignore_case');
GetOptions ( 
	     'debug'               => \$debug,      
	     'd'                   => \$debug,      
	     'verbose+'            => \$verbose,
	     'v+'                  => \$verbose,
	     'no-mirror'           => \$no_mirror,
	     'out-osm'             => \$out_osm,
	     'limit-area'          => \$use_area_limit,
	     'draw_check_areas'    => \$draw_check_areas,
	     'use_reduce_filter'   => \$use_reduce_filter,
	     'generate_ways'       => \$generate_ways,
	     'proxy=s'             => \$PROXY,
	     'MAN'                 => \$man, 
	     'man'                 => \$man, 
	     'h|help|x'            => \$help, 
	     )
    or pod2usage(1);

pod2usage(1) if $help;
pod2usage(-verbose=>2) if $man;

convert_Data();

##################################################################
# Usage/manual

__END__

=head1 NAME

B<osm-filter.pl> Version 0.01

=head1 DESCRIPTION

B<osm-filter.pl> is a program to konvert the *.gps Files of kismet to a 
*.osm File. This File then can be loaded into josm and 

This Programm is completely experimental, but some Data 
can already be retrieved with it.
Since the script is still in Alpha Stage, please make backups 
of your source gpx/kismet,... Files.

So: Have Fun, improve it and send me fixes :-))


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
    use the option    --draw_check_areas. This will draw in the check areas 
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


=item B<--limit-area>

use the area limits coded in the source

=item B<--draw_check_areas>

draw the check_areas into the 00__combination.gpx file by adding a track with the border 
of each check_area 

For now the Filter areas have to be defined in the Source at
the definition of
  $areas_allowed_squares = 

=item B<--use_reduce_filter>

This Filter reduces the ammount of point used in the GPX/OSM File.
Each point which would almost give a streight line 
with its pre and post point will be eliminated.

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

For each File read a File *-converted.gpx will be written
All input filenames ending with -converted.gpx will be skiped.

To read all Files in a specified directory at once do the following:

 find <kismet_dir>/log -name "*.gps" | xargs ./osm-filter.pl

If you define multiple Files a summary File will automagically be written:
 ./00__combination.gpx

=back
