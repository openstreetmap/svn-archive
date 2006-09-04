##################################################################
package Geo::OSM::Tracks2OSM;
# Functions:
# tracks2osm:
#     converts a tracks Hash to an OSM Datastructure
#
##################################################################

use Exporter;
@ISA = qw( Exporter );
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
@EXPORT = qw( tracks2osm );

use strict;
use warnings;

use Data::Dumper;

use Geo::Geometry;
use Utils::File;
use Utils::Math;
use Utils::Timing;
use Utils::Debug;
use Geo::Tracks::Tools;

my $first_id = -10000;

my $lat_lon2node={};
my $next_osm_node_number = $first_id;
my $osm_nodes_duplicate   = {};
# Check if a node at this position exists
# if it exists we get the old id; otherwise we create a new one
sub create_node($$) {
    my $osm_nodes = shift;
    my $elem      = shift;

    printf STDERR "create_node(): lat or lon undefined : $elem->{lat},$elem->{lon} ".Dumper(\$elem)."\n" 
	unless  defined($elem->{lat}) && defined($elem->{lon}) ;

    my $id=0;
    my $lat_lon = sprintf("%f_%f",$elem->{lat},$elem->{lon});
    if ( defined( $osm_nodes_duplicate->{$lat_lon} ) ) {
	$id = $osm_nodes_duplicate->{$lat_lon};
	printf STDERR "Node already exists as $id	pos:$lat_lon\n"
	    if $DEBUG>2;
	$osm_nodes->{$id}=$elem;
	# TODO: We have to check that the tags of the old and new nodes don't differ
	# or we have to merge them
    } else {
	$next_osm_node_number--;
	$id = $next_osm_node_number;
	$elem->{tag}->{converted_by} = "Track2osm" ;
	$osm_nodes->{$id}=$elem;
	$lat_lon2node->{$lat_lon}=$id;
	$osm_nodes_duplicate->{$lat_lon}=$id;
    };
    if ( !$id  ) {
	print STDERR "create_node(): Null node($id,$lat_lon) created\n".
	    Dumper($elem)
	    if $DEBUG;
    }
    return $id;
}

my $next_osm_segment_number = $first_id;
my $osm_segments_duplicate= {};
sub create_segment($$){
    my $elem = shift;
    my $osm_segments = shift;
    
    my $seg_id = 0;
    my $from_to = $elem->{from}.",".$elem->{to};
    if ( defined($osm_segments_duplicate->{$from_to}) ) {
	$seg_id = $osm_segments_duplicate->{$from_to};
	printf STDERR "Duplicate segment $next_osm_segment_number --> $seg_id\n";
    } else {
	$next_osm_segment_number--;
	$seg_id=$next_osm_segment_number;
	$elem->{tag}->{converted_by} = "Track2osm" ;
	$osm_segments->{$seg_id} = { 
	    from => $elem->{from},
	    to   => $elem->{to},
	    tag  => $elem->{tag},
	};
    };
    return $seg_id;
}

my $next_osm_way_number     = $first_id;
# ------------------------------------------------------------------
sub tracks2osm($){
    my $tracks = shift;


    my $osm_nodes = {};
    my $osm_segments     = {};
    my $osm_ways          = {};
    my $reference = $tracks->{filename};

    my $last_angle         = 999999999;
    my $angle;
    my $way={};
    my $angle_to_last;


    my $count_valid_points_for_ways=0;

    # TODO: We have to find a better solution for this
    my $generate_ways=$main::generate_ways;

    my $track_nr=0;

    enrich_tracks($tracks);

    for my $track ( @{$tracks->{tracks}} ) {
	$track_nr++;

	my $element_count=0;
	my $last_elem = $track->[0];
	$last_elem->{node_id}=create_node($osm_nodes,$last_elem);

	for my $track_pos ( 1 .. $#{@{$track}} ) {
	    my $elem = $track->[$track_pos];

	    my $seg_id=0;
	    my $dist=$last_elem->{dist};

	    # -------------------------------------------- Create Nodes
	    my $from = $last_elem->{node_id};
	    my $to   = $elem->{node_id}      || create_node($osm_nodes,$elem);
	    $elem->{node_id} ||=  $to;

	    # -------------------------------------------- Create Segments
	    if ( ! $from ) {
		printf STDERR "From Part of Segment not existent $from -> $to\n"
		    if $DEBUG >2;
		next;
	    }
	    if ( $from == $to ) {
		printf STDERR "Null length Segment $from -> $to	Track: $track_nr Pos:$track_pos\n"
		    if $DEBUG >2;
		next;
	    }
	    my $tags = {"converted_by" => "Track2osm"};
	    if ( $DEBUG >10 ) {
		$tags->{distance} = $dist;
		$tags->{distance_meter} = $dist*1000;
		$tags->{reference} = "$reference $track_pos, Track:$track_nr";
		$tags->{from_to} = "$from $to";
	    };
	    if ( $DEBUG >12 ) {
		for my $k ( keys %{$elem} ) {
		    next if $k =~ m/^sat_/;
		    $tags->{$k}=$elem->{$k};
		}
	    }
	    
	    $seg_id = create_segment(
				 {
				     from => $from,
				     to   => $to,
				     tag  => $tags,
				 },$osm_segments);
	    
	    
	    # -------------------------------------------- Create Ways
	    if ( $generate_ways ) {
		$angle=$elem->{angle};
		$last_angle = $last_elem->{angle};
		
		if ( ! $seg_id               # Wir haben ein neues Segment
		     || abs($last_angle) > 25 # over x Grad Lenkeinschlag
		     || $dist > 5             # more than x Km Distance
		     ) {
		    if ( defined($way->{seg}) 
			 && ( @{$way->{seg}} > 4)
			 ) {
			$next_osm_way_number--;
			if ( $DEBUG >10 ) {
			    $way->{reference} = $reference;
			}
			$osm_ways->{$next_osm_way_number} = $way;
		    }
		    $way={};
		}

		push(@{$way->{seg}},$seg_id);
		$count_valid_points_for_ways++;
		
		my $tags = {"converted_by" => "Track2osm"};
		$way->{tag} = $tags;
	    }

	    $last_elem=$elem;
	}
    }
    return { nodes    => $osm_nodes,
	     segments => $osm_segments,
	     ways     => $osm_ways,
	 };
}


