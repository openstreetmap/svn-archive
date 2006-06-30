#!/usr/bin/perl
# This Script converts/filters GPS-Track-Data 
# Input is one of the folowing:
#   - Kismet-GPS File *.gps 
#   - GpsDrive-Track  *.sav
#   - GPX File        *.gpx
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
use Geo::Gpsdrive::Utils;

my ($man,$help);
our ($debug,$verbose,$no_mirror,$PROXY);


my $osm_nodes        = {};
my $osm_nodes_duplicate   = {};
my $osm_segments     = {};
my $osm_segments_duplicate ={};
my $osm_ways          = {};
my $osm_stats         = {};
my $osm_obj           = undef; # OSM Object currently read
my $out_osm           = 0;
my $use_area_limit    = 0;

my $first_id=100000000;
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
sub distance_point_point($$) {
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
# Minimal Distance between line and point
sub distance_line_point($$$$$$) {
    my $x1 = shift;
    my $y1 = shift;
    my $x2 = shift;
    my $y2 = shift;

    my $xp = shift;
    my $yp = shift;


    printf "distance_line_point(%f,%f, %f,%f,   %f,%f)\n", $x1, $y1, $x2, $y2,  $xp, $yp
	if ( $debug >0 ) ;
    
    my $dx1p = $x1 - $xp;
    my $dx21 = $x2 - $x1;
    my $dy1p = $y1 - $yp;
    my $dy21 = $y2 - $y1;
    my $frac = $dx21 * $dx21 + $dy21 * $dy21;

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

use Geo::Gpsdrive::Utils;
# -----------------------------------------------------------------------------
# Open Data File in predefined Directories
sub data_open($){
    my $file_name = shift;

    my $file_with_path="$file_name";
    if ( -s $file_with_path < 270 ) {
	warn "cannot Open $file_name (".(-s $file_with_path)." Bytes is too small)\n"
	    if $verbose || $debug;
	return 0;
    }

    debug("Opening $file_with_path");
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
use Geo::Gpsdrive::Utils;
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
	#print Dumper(\$elem);
	push(@{$track},$elem);
    };
    
#    print Dumper(\$track);

    return [$track];
}


##################################################################
package GPX;
##################################################################
use Date::Parse;
use Geo::Gpsdrive::Utils;
use Data::Dumper;
use Date::Parse;
use Date::Manip;

# -----------------------------------------------------------------------------
# Read GPS Data from GPX - File
sub read_gpx_file($) { 
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
    print $fh "    creator=\"kismet2osm Converter\"\n";
    print $fh "    xmlns=\"http://www.ostertag.name\"\n";
    print $fh "    >\n";
    my $track_id=0;
    for my $track ( @{$tracks} ) {
	$track_id++;
	print $fh "\n";
	print $fh "<trk>\n";
	print $fh "   <name>$file_name $track_id</name>\n";
	print $fh "   <number>$track_id</number>\n";
	print $fh "    <trkseg>\n";

	for my $elem ( @{$track} ) {
    	    my $lat  = $elem->{lat};
	    my $lon  = $elem->{lon};
	    print $fh "     <trkpt lat=\"$lat\" lon=\"$lon\">\n";
	    if( defined $elem->{ele} ) {
		print $fh "       <ele>$elem->{ele}</ele>\n";
	    };
	    if ( defined ( $elem->{time} ) ) {
		my $time = UnixDate("epoch ".$elem->{time},"%m/%d/%Y %H:%M:%S");
		#$time = "2004-11-12T15:04:40Z";
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

    if ( $verbose) {
	printf "Wrote GPX File $file_name in  %.0f sec\n",time()-$start_time;
    }

}

#------------------------------------------------------------------
sub write_osm_data_as_gpx_file($) { # Write an gpx File
    my $tracks = shift;
    my $file_name = shift;

    my $start_time=time();

    print("Writing GPS File $file_name\n") if $verbose || $debug;

    my $fh = IO::File->new(">$file_name");
    print $fh "<gpx version=\"1.0\" creator=\"kismet2osm Converter\" xmlns=\"http://www.ostertag.name\">\n";
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
use Geo::Gpsdrive::Utils;
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

##################################################################
package GPS;
##################################################################
use Date::Parse;
use Geo::Gpsdrive::Utils;
use Data::Dumper;

# ------------------------------------------------------------------
# Check if the point is in the area to currently evaluate
my $areas_allowed_squares = 
    [
     # min_lat,min_lon max_lat,max_lon,     (y,x)
     #[ 48.1758, 11.7654 , 48.18095 , 11.7695 ], # Test - Kirchheim Gewerbegebiet
     #[ 48.169, 11.66  , 48.184 , 11.774 ], # Kirchheim
     #[ 48.147, 11.74  , 48.172 , 11.781 ], # Heimstetten
     #[ 48.154, 11.693 , 48.196 , 11.735 ], # Aschheim
     #[ 48.1  , 11.68  , 48.24  , 11.82  ], # Muc-N-O
     #[ 48.1  , 11.68  , 48.4  , 12.2  ], # Muc-N-O++
     #[ 48.0136 , 11.4211  , 48.2644  , 11.684  ], # Muc-O
     #[ 48.0  , 11.6  , 48.4    , 12.0    ], # München
     #[ 48.10  , 11.75  , 49.0    , 14.0    ], # Münchner-Osten-
     #[ 48.22  , 12.0  , 49.0    , 13.0    ], # Dorfen-Sued
     [ 48.15  , 11.85  , 48.45    , 12.36    ], # Anzing - Vilsbiburg
     [ -90.0  , -180  , 90.0    , 180   ], # World
     ];
my $area_block_circles=
    [ 
  { lat =>  48.175921 	,lon => 11.754312  ,circle => .030 },
  { lat =>  48.175710 	,lon => 11.754400  ,circle => .030 },
  { lat =>  48.175681 	,lon => 11.7547203 ,circle => .030 },
  { lat =>  48.175527 	,lon => 11.7586399 ,circle => .030 },
  { lat =>  48.1750 	,lon => 11.7536    ,circle => .10  },
      ];	


sub check_allowed_area($){
    my $elem = shift;
    
    return 1 unless $use_area_limit;
    
    # Block a circle of <circle> Km arround each point 
    
    for my $block ( @{$area_block_circles} ) {
	return 0 
	    if Geometry::distance_point_point($block,$elem) < $block->{circle};
    }
    
    
    for my $area ( @{$areas_allowed_squares} ) {
	my ($min_lat,$min_lon, $max_lat,$max_lon ) = @{$area};
	if ( $min_lat <= $elem->{lat} &&	 $max_lat >= $elem->{lat} &&
	     $min_lon <= $elem->{lon} &&	 $max_lon >= $elem->{lon} ) {
	    return 1;
	}
    }
    return 0;
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
		my $dist  = Geometry::distance_point_point($prev_elem,$elem);
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
		if ( $elem->{time_diff} > 10) { # Seconds
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
	if ( $num_elm_in_track > 5 ) {
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
# Filter tracks and points
sub filter_data_by_area($){
    my $tracks      = shift; # reference to tracks list

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
		$dist = Geometry::distance_point_point($osm_nodes->{$node_from},$elem);
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
			if ( $debug) {
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
		    $osm_segments->{$seg_id}->{tag}->{time_diff} = $elem->{time_diff};
		    if ( defined ( $elem->{speed} ) ) {
			$osm_segments->{$seg_id}->{tag}->{speed} = $elem->{speed};
		    }
		    $osm_segments->{$seg_id}->{tag}->{angle} = $angle;
		    $osm_segments->{$seg_id}->{tag}->{d_lat} = $elem->{d_lat};
		    $osm_segments->{$seg_id}->{tag}->{d_lon} = $elem->{d_lon};
		}
		if ( defined ( $last_angle )) {
		    $angle_to_last = $angle - $last_angle;
		    $angle_to_last = - ( 360 - $angle_to_last) if $angle_to_last > 180;
		    if ( $debug) {
			$osm_segments->{$seg_id}->{tag}->{angle_to_last} = $angle_to_last;
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
	if ( $filename =~ m/\.gps$/ ) {
	    $new_tracks = Kismet::read_gps_file($filename);
	} elsif ( $filename =~ m/\.gpx$/ ) {
	    $new_tracks = GPX::read_gpx_file($filename);
	} elsif ( $filename =~ m/\.sav$/ ) {
	    $new_tracks = GPSDrive::read_gpsdrive_track_file($filename);
	}
	if ( $verbose || $debug) {
	    ($track_count,$point_count) =   GPS::count_data($new_tracks);
	    printf "Read %5d Points in %d Tracks from $filename\n",$point_count,$track_count;
	}
	GPS::enrich_data($new_tracks,$filename);
	($track_count,$point_count) =   GPS::count_data($new_tracks);
	if ( $verbose || $debug) {
	    printf "Results in  %5d Points in %d Tracks after enriching\n",$point_count,$track_count;
	}

	$count ++ if $point_count && $track_count;

	GPS::filter_data_by_area($new_tracks);

	my $osm_filename = $filename;
	if ( $track_count > 0 ) {
	    my $new_gpx_file = $osm_filename;
	    if ( $filename =~ m/\.gpx$/ ) {
		$new_gpx_file =~ s/\.(sav|gps|gpx)$/-converted.gpx/;
	    } else {
		$new_gpx_file =~ s/\.(sav|gps)$/.gpx/;
	    }
	    GPX::write_gpx_file($new_tracks,$new_gpx_file)
		if $single_file;
	    
	    my $new_osm_file = $osm_filename;
	    $new_osm_file =~ s/\.(sav|gps|gpx)$/.osm/;
	    my $points = OSM::Tracks2osm($new_tracks,$filename);
	    OSM::write_osm_file($new_osm_file)
		if $out_osm;

	}
	GPS::add_tracks($all_tracks,$new_tracks);
	if ( $point_count && $track_count ) {
	    printf "Added:  %5d Points in %3d Tracks for %s\n",
	    $point_count,$track_count,$filename;
	}
    }
    
    OSM::write_osm_file("__combination.osm")
	if $out_osm;
    ($track_count,$point_count) =   GPS::count_data($all_tracks);
    printf "Summary:  %5d Points in %d Tracks after enriching\n",$point_count,$track_count;
    GPX::write_gpx_file($all_tracks,"__combination.gpx");
    if ( $verbose) {
	printf "Converting $count  OSM Files in  %.0f sec\n",time()-$start_time;
    }
}

# ------------------------------------------------------------------

# Set defaults and get options from command line
Getopt::Long::Configure('no_ignore_case');
GetOptions ( 
	     'debug'               => \$debug,      
	     'verbose+'            => \$verbose,
	     'no-mirror'           => \$no_mirror,
	     'out-osm'             => \$out_osm,
	     'limit-area'          => \$use_area_limit,
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

B<kismet2osm.pl> Version 0.00001

=head1 DESCRIPTION

B<kismet2osm.pl> is a program to konvert the *.gps Files of kismet to a 
*.osm File. This File then can be loaded into josm and 

This Programm is completely experimental, but some Data 
can already be retrieved with it.

So: Have Fun, improve it and send me fixes :-))

=head1 SYNOPSIS

B<Common usages:>

kismet2osm_osm.pl [-d] [-v] [-h] <File1.gps> [<File2.gps>,...]

=head1 OPTIONS

=over 2

=item B<--man> Complete documentation

Complete documentation

=item B<File1> The Kismet/gpx/sav Files to read

to read all Files in a specified directory at once do the following:

 find <kismet_dir>/log -name "*.gps" | xargs ./kismet2osm.pl

this will result in only one File with the name 
 ./__combination.osm

=item B<out-osm>

*.osm files will only be generated if this option is set.

=item B<limit-area>

use the area limits coded in the source


=back
