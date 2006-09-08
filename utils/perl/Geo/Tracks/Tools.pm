##################################################################
package Geo::Tracks::Tools;
##################################################################

use Exporter;
@ISA = qw( Exporter );
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
@EXPORT = qw( copy_track_structure copy_track_wpt
	      tracks_only_good_point tracks_only_good_point_split
	      count_good_point
	      set_number_bad_points
	      enrich_tracks
	      track_point_speed
	      track_part_angle
	      track_part_distance
	      print_count_data
	      count_data
	      add_tracks
);


use strict;
use warnings;
use Carp;

use Geo::Geometry;
use Utils::File;
use Utils::Math;
use Utils::Debug;

# Copy the track structure
sub copy_track_structure($$){
    my $tracks     = shift;
    my $new_tracks = shift;
    
    Carp::confess "Must get Hashref to copy structure to\n"
	unless ref($new_tracks) eq "HASH";

    $new_tracks->{filename}  = $tracks->{filename};
    $new_tracks->{tracks}  ||= [];
    $new_tracks->{wpt}     ||= [];
}

# Copy the track waypoints
sub copy_track_wpt($$){
    my $tracks = shift;
    my $new_tracks = shift;

    Carp::confess "Must get Hashref to copy structure to\n"
	unless ref($new_tracks) eq "HASH";

    # Keep WPT
    for my $elem ( @{$tracks->{wpt}} ) {
	next unless $elem;
	push(@{$new_tracks->{wpt}},$elem);
    }

    return $new_tracks;
}


##################################################################
# Copy only those trackpoints with the good_point Flag set
# RETURN: Tracks Structure
sub tracks_only_good_point($){
    my $tracks = shift;

    my $new_tracks={};
    copy_track_structure($tracks,$new_tracks);
    copy_track_wpt($tracks,$new_tracks);

    
    Carp::Confess("Tracks to copy to musst be of Type Hash")
	unless ref($new_tracks) eq "HASH";

    for my $track ( @{$tracks->{tracks}} ) {
	next if !$track;
	my $new_track=[];

	# Copy only those with good_point set to 1
	for my $track_pos ( 0 .. $#{@{$track}} ) {
	    my $elem=$track->[$track_pos];
	    next unless $elem->{good_point};
	    push(@{$new_track},$elem);
	}
	if ( scalar(@{$new_track} ) ) {
	    push(@{$new_tracks->{tracks}},$new_track);
	}
    }
    return $new_tracks;
}

##################################################################
# Copy only those trackpoints with the good_point Flag set
# split tracks at all positions wher we have a gap in good points
# RETURN: Tracks Structure
sub tracks_only_good_point_split($){
    my $tracks = shift;

    my $new_tracks={};
    copy_track_structure($tracks,$new_tracks);
    copy_track_wpt($tracks,$new_tracks);

    
    Carp::Confess("Tracks to copy to musst be of Type Hash")
	unless ref($new_tracks) eq "HASH";

    for my $track ( @{$tracks->{tracks}} ) {
	next if !$track;
	my $new_track=[];

	# Copy only those with good_point set to 1
	for my $track_pos ( 0 .. $#{@{$track}} ) {
	    my $elem0=$track->[$track_pos-1];
	    my $elem1=$track->[$track_pos];
	    my $elem2=$track->[$track_pos+1];
	    my $skip_point = !$elem1->{good_point};
	    # This should only skip the point if the one before and after are skiped too
	    # But currentls it's not working yet
	    $skip_point=0 if ( $track_pos > 0             ) && ( $elem0->{good_point} );
	    $skip_point=0 if ( $track_pos < $#{@{$track}} ) && ( $elem2->{good_point} );
	    
	    if ( $skip_point ) {
		my $num_elem=scalar(@{$new_track});
		if ( $num_elem >2 ) {
		    push(@{$new_tracks->{tracks}},$new_track);
		}
		$new_track=[];
	    } else {
		push(@{$new_track},$elem1);
	    }
	}
	my $num_elem=scalar(@{$new_track});
	if ( $num_elem >2 ) {
	    push(@{$new_tracks->{tracks}},$new_track);
	}
    }
    return $new_tracks;
}



##################################################################
# Set number of points in track to bad
# 
sub set_number_bad_points($$$){
    my $track = shift;
    my $start_pos = shift;
    my $count = shift;

    return unless $count;

    my $max_pos = $#{@{$track}};
    for my $i ( 0 .. $count-1 ){
	last if $start_pos+$i > $max_pos;
	$track->[$start_pos+$i]->{good_point}= 0;
    }
}

##################################################################
# Returns number of points with set good_point
sub count_good_point($){
    my $tracks = shift;
    my $count =0;
    for my $track ( @{$tracks->{tracks}} ) {
	next if !$track;
	for  ( my $track_pos=0; $track_pos <= $#{@{$track}};$track_pos++ ) {
	    $count++ if $track->[$track_pos]->{good_point};
	}
    }
    return $count;
};


# ------------------------------------------------------------------
# Enrich Track Data by adding:;
#    dist: Distance to next point in meters
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

	if ( ref($elem1) eq "ARRAY" ) {
 	    print Dumper(\$track);
	    Carp::confess("enrich_single_track(): track_pos  $track_pos has ARRAY instead of Hash");
	}

	$elem1->{good_point}= 1;

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
	if ( ($track_pos > 0) &&
	     ( $track_pos < $last_track_point ) ) {
	    $elem2->{angle_to_last} =$elem1->{angle};
	}
	# Distance between line of segment($segment)  to trackpoint $elem1
	$elem1->{dist} = 1000*distance_point_point_Km($elem1,$elem2);

	if ( defined($elem1->{time}) && defined($elem2->{time}) ) {
	    $elem1->{time_diff} = $elem1->{time} - $elem2->{time};
	}
    }
}

# ------------------------------------------------------------------
# Enrich Tracks Data by doing enrich_singleTrack an all tracks
sub enrich_tracks($){
    my $tracks = shift;
    for my $track ( @{$tracks->{tracks}} ) {
	enrich_single_track($track);
    }
}


# ------------------------------------------------------------------
# Calculate Average Speed of track segment
sub track_point_speed($$){
    my $track = shift;
    my $track_pos = shift;

    my $elem = $track->[$track_pos];
    return $elem->{speed} if defined $elem->{speed};
    
    my $pos_start = $track_pos-10;
    $pos_start = 0        if $pos_start<0;

    my $pos_end =  $pos_start+20;
    my $max_pos = $#{@{$track}};
    $pos_end   = $max_pos if $pos_end> $max_pos;
 
    return track_part_speed($track,$pos_start,$pos_end);
}

# ------------------------------------------------------------------
# Calculate Average Speed of track segment
sub track_part_speed($$$){
    my $track = shift;
    my $pos_start = shift;
    my $pos_end = shift;
    my $avg_speed = 0;

    return 0 unless defined $track;

    my $max_pos = $#{@{$track}};
    $pos_start = 0        if $pos_start<0;
    $pos_end   = $max_pos if $pos_end> $max_pos;

    my $dist = track_part_distance($track,$pos_start,$pos_end)/1000;
    my $elem_s = $track->[$pos_start];
    my $elem_e = $track->[$pos_end];

    my $speed=0;
    if ( defined($elem_s->{time}) && defined($elem_e->{time}) ) {
	my $time_diff = $elem_s->{time} - $elem_e->{time};
	if ( $time_diff ) {
	    my $speed = $dist/$time_diff*3600;
	};
    };
    $avg_speed = $speed;
    
    my $sum_speed=0;
    for my $track_pos ( $pos_start .. $pos_end ) {
	my $elem = $track->[$track_pos];

	return $speed # Abort if any sub-speeds are not defined
	    unless defined $elem->{speed};

	$sum_speed += $elem->{speed};
    }
    $avg_speed = $sum_speed/($pos_end-$pos_start+1);

    return $avg_speed;
}

# ------------------------------------------------------------------
# Summarize all angles between start and endpoint of a single track
sub track_part_angle($$$){
    my $track = shift;
    my $pos_start = shift;
    my $pos_end = shift;
    my $sum_angle=0;
    
    for my $track_pos ( $pos_start .. $pos_end ) {
	$sum_angle += $track->[$track_pos]->{angle};
    }
    return $sum_angle;
}

# ------------------------------------------------------------------
# Summarize distance between start and endpoint of a single track
sub track_part_distance($$$){
    my $track = shift;
    my $pos_start = shift;
    my $pos_end = shift;
    my $sum_angle=0;
    
    for my $track_pos ( $pos_start .. $pos_end ) {
	$sum_angle += $track->[$track_pos]->{dist};
    }
    return $sum_angle;
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
    if ( $DEBUG>5 || $VERBOSE>5 || ($used_time >5 )) {
	printf STDERR "Counted ( $count_tracks Tracks,$count_points Points)";
	print_time($start_time);
    }

    return ( $count_tracks,$count_points);
}

# ------------------------------------------------------------------
# Print Number of points/tracks with a comment
# and print them with a comment and the filename stored in the track
sub print_count_data($$){
    my $tracks   = shift; # reference to tracks list
    my $comment  = shift;

    my $filename =     $tracks->{filename};

    my ($track_count,$point_count) = GPS::count_data($tracks);
    if ( $VERBOSE || $DEBUG) {
	printf STDERR "%-35s:	%5d Points in %d Tracks $comment",$filename,$point_count,$track_count;
    }
}


# ------------------------------------------------------------------
# add a list of tracks to another list of Tracks
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

1;
