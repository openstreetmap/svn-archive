#!/usr/bin/perl

# Simplify OSM by removing invisible data at high zome levels
#
# When displaying a map of the whole of the UK there is no point including
# information on nodes < 1km apart since this will be less then 1 pixel
# This script removes the redundant data using the following algorithm
#
# Some code taken from check-osm.pl by Joerg Ostertag
# 
# User specifies the minimum "interesting" square grid of D
#
# The rough design is as follows.
# Note: These were the initial design idea, the implementation does not
# follow these exactly, but is based on the same core ideas.
# 
# Read in all nodes.
# Nodes are read an Lat / Lon is quantised to nearest multiple of D
# - If this is a new unique location, it gets added to nodes_unique.
# - Otherwise added to nodes_dupe with link to node in nodes_unique.
# - (maybe) store unique nodes in multilevel hash on lat/lon.
# - (maybe) store dupes as hash with key(dupe id) = value(unique id)
# - ? store unique nodes in dupe table to with map to self.
#
# Read in all segments
# - Lookup to/from in nodes_dupe & nodes_unique to find the quantised node for these positions.
# - If to/from map to the same node then discard the segment
# - Search segments list to see if a segment for this to/from pair exists
# - If a new segment pair, add to segments_unique
# - If pair already exists add to segments_dupe with reference to segments_unique
#
# Read in all ways, for each segment in a way
# - Look up each segment in segments_{unique,dupe} to find the unique ID.
# - If segment not present then assume if maps to the same from/to quantised position and discard.
# - Look through list of segments for this way, discard if already present in list for this way
# 
# Once all segments for a way have been read
# - Check to see if any segments are valid (may all have been discarded).
# - If at least one valid, write to way table.
# (maybe) Search list of ways and discard duplicates and/or overlapping 
#
# Iterate all ways
# - Mark segments which are referenced.
#
# Iterate all segments
# - If segment not referenced by way then skip
# - Mark nodes which are referenced
#
# Iterate all nodes
# - If node not referenced by segment then skip
# - Write out referenced unique nodes.
#
# Iterate all segments
# - Write out referenced unique segments.
# 
# Iterate all ways
# - Write out way segments 

my $VERSION ="simplify.pl (c) Jon Burgess
Initial Version (Oct,2006) by Jon Burgess <jburgess@uklinux.net>
Version 0.01
";

BEGIN {
    my $dir = $0;
    $dir =~s,[^/]+/[^/]+$,,;
    unshift(@INC,"$dir/perl");

    unshift(@INC,"../perl");
    unshift(@INC,"~/svn.openstreetmap.org/utils/perl");
    unshift(@INC,"$ENV{HOME}/svn.openstreetmap.org/utils/perl");
}


use strict;
use warnings;

use File::Basename;
use File::Copy;
use File::Path;
use Getopt::Long;
use HTTP::Request;
use IO::File;
use POSIX qw(ceil floor);
use Pod::Usage;
use Geo::OSM::Planet;
use Geo::OSM::Write;
use Utils::Debug;
use Utils::LWP::Utils;
use Utils::File;
use Data::Dumper;
use XML::Parser;

my ($man,$help);

our $osm_file; # The complete osm Filename (including path)
my $simplify_degree; # Reduce data grid to this disatance (in degrees).

use strict;
use warnings;

my $OSM_NODES    = {};
my $OSM_SEGMENTS = {};
my $OSM_WAYS     = {};
my $OSM_OBJ      = undef; # OSM Object currently read

my $count_node=0;
my $count_segment=0;
my $count_way=0;
my $count_node_all=0;
my $count_segment_all=0;
my $count_way_all=0;

# -------------------------------------------------------------------

sub node_ {
    $OSM_OBJ = undef;
}
sub node {
    my($p, $tag, %attrs) = @_;  
    my $id = delete $attrs{id};
    $OSM_OBJ = {};
    $OSM_OBJ->{id} = $id;

    $OSM_OBJ->{lat} = delete $attrs{lat};
    $OSM_OBJ->{lon} = delete $attrs{lon};
    $OSM_OBJ->{timestamp} = delete $attrs{timestamp} if defined $attrs{timestamp};

    if ( keys %attrs ) {
	warn "node $id has extra attrs: ".Dumper(\%attrs);
    }

    $count_node_all++;
    $OSM_NODES->{$id} = $OSM_OBJ;
    $count_node++;
    if ( $VERBOSE || $DEBUG ) {
	if (!($count_node_all % 1000) ) {
	    printf("node %d (%d)\r",$count_node,$count_node_all);
	}
    }
}

# --------------------------------------------
sub way_ {
    my $id = $OSM_OBJ->{id};
    if ( @{$OSM_OBJ->{seg}} >0 ) {
	$OSM_WAYS->{$id} = $OSM_OBJ;
	$count_way++;
    }
    $OSM_OBJ = undef
}
sub way {
    my($p, $tag, %attrs) = @_;  
    my $id = delete $attrs{id};
    $OSM_OBJ = {};
    $OSM_OBJ->{id} = $id;
    $OSM_OBJ->{timestamp} = delete $attrs{timestamp} if defined $attrs{timestamp};

    if ( keys %attrs ) {
	warn "way $id has extra attrs: ".Dumper(\%attrs);
    }
    print "\n" if !$count_way_all && ($VERBOSE || $DEBUG);
    $count_way_all++;
    printf("way %d(%d)\r",$count_way,$count_way_all) 
	if !( $count_way_all % 1000 ) && ($VERBOSE || $DEBUG);
}
# --------------------------------------------
sub segment_ {
    $OSM_OBJ = undef
}
sub segment {
    my($p, $tag, %attrs) = @_;  
    my $id = delete $attrs{id};
    $OSM_OBJ = {};
    $OSM_OBJ->{id} = $id;

    my $from = $OSM_OBJ->{from} = delete $attrs{from};
    my $to   = $OSM_OBJ->{to}   = delete $attrs{to};
    $OSM_OBJ->{timestamp} = delete $attrs{timestamp} if defined $attrs{timestamp};

    if ( keys %attrs ) {
	warn "segment $id has extra attrs: ".Dumper(\%attrs);
    }
    if ( defined($OSM_NODES->{$from}) && defined($OSM_NODES->{$to}) ) {
	$OSM_SEGMENTS->{$id} = $OSM_OBJ;
	$count_segment++;
    }
    print "\n" if !$count_segment_all && ($VERBOSE || $DEBUG);
    $count_segment_all++;
    printf("segment %d (%d)\r",$count_segment,$count_segment_all) 
	if !($count_segment_all%5000) && ($VERBOSE || $DEBUG);
}
# --------------------------------------------
sub seg {
    my($p, $tag, %attrs) = @_;  
    my $id = $attrs{id};
    delete $attrs{timestamp} if defined $attrs{timestamp};
    #print "Seg $id for way($OSM_OBJ->{id})\n";
    if (defined($OSM_SEGMENTS->{$id})) {
	push(@{$OSM_OBJ->{seg}},$id);
    }
}
# --------------------------------------------
sub tag {
    my($p, $tag, %attrs) = @_;  
    #print "Tag - $tag: ".Dumper(\%attrs);
    my $k = delete $attrs{k};
    my $v = delete $attrs{v};
    delete $attrs{timestamp};

    return if $k eq "created_by";

    if ( keys %attrs ) {
	print "Unknown Tag value for ".Dumper($OSM_OBJ)."Tags:".Dumper(\%attrs);
    }
    
    my $id = $OSM_OBJ->{id};
    if ( defined( $OSM_OBJ->{tag}->{$k} ) &&
	 $OSM_OBJ->{tag}->{$k} ne $v
	 ) {
	printf "Tag %8s already exists for obj(id=$id) tag '$OSM_OBJ->{tag}->{$k}' ne '$v'\n",$k ;
    }
    $OSM_OBJ->{tag}->{$k} = $v;
    if ( $k eq "alt" ) {
	$OSM_OBJ->{alt} = $v;
    }	    
}

############################################
# -----------------------------------------------------------------------------
sub read_osm_file($) { 
    my $file_name = shift;

    die "No OSM file specified\n" unless $file_name;
    my $start_time=time();

    print "Unpack and Read OSM Data from file $osm_file\n" if $VERBOSE || $DEBUG;
    print "$file_name:	".(-s $file_name)." Bytes\n" if $DEBUG;

    print STDERR "Parsing file: $file_name\n" if $DEBUG;
    my $p = XML::Parser->new( Style => 'Subs' , ErrorContext => 10);
	
    my $fh = data_open($file_name);
    die "Cannot open OSM File $file_name\n" unless $fh;
    #eval {
	$p->parse($fh);
    #};
    print "\n" if $DEBUG || $VERBOSE;
    if ( $VERBOSE) {
        printf "Read and parsed $file_name in %.0f sec\n",time()-$start_time;
    }
    if ( $@ ) {
	warn "$@Error while parsing\n $file_name\n";
	return;
    }
    if (not $p) {
	warn "WARNING: Could not parse osm data\n";
	return;
    }
    return;
}



# *****************************************************************************

sub quantise($) {
	# Quantise input latitude/longitude to nearest multiple of precision
	my $input = shift;
	# Note: int() has 'issues' with some edge cases but it is good enough for our purposes.
	return int($input/$simplify_degree)*$simplify_degree;
}

# Read in all nodes.
# Nodes are read an Lat / Lon is quantised to nearest multiple of D
# - If this is a new unique location, it gets added to nodes_unique.
# - Otherwise added to nodes_dupe with link to node in nodes_unique.
# - (maybe) store unique nodes in multilevel hash on lat/lon.
# - (maybe) store dupes as hash with key(dupe id) = value(unique id)
# - ? store unique nodes in dupe table to with map to self.
#
sub simplify_nodes() {
    my $count_unique=0;
    my $count_node=0;

    my $out_nodes = {};
    
    print "------------ Simplifying Nodes\n";
    my $node_positions={};
    for my $node_id (  keys %{$OSM_NODES} ) {
	my $node = $OSM_NODES->{$node_id};
	my $lat = quantise($node->{lat});
	my $lon = quantise($node->{lon});
	my $dupe_id = $node_positions->{"$lat,$lon"};
	if (!defined($dupe_id)) {
		$dupe_id = $node_id;
		$node_positions->{"$lat,$lon"} = $node_id;
		$count_unique++;
		# Update this node data for output
		$node->{lat} = $lat;
		$node->{lon} = $lon;
		$out_nodes->{$node_id} = $node;
	}
	$node->{dupe_id} = $dupe_id;
	$count_node++;
#	if ( $VERBOSE || $DEBUG ) {
	if ( 1 ) {
		if (!($count_node % 1000) ) {
	    		printf("node %d (%d)\r",$count_node,$count_unique);
		}
	}
    }
    printf("Final nodes input=%d, output=%d, shrunk by %d\n",
	$count_node,$count_unique, $count_node/$count_unique);
#    for my $position ( keys %{$node_positions} ) {
#	my $node_id = $node_positions->{$position};
#	print(" id=$node_id, position=$position\n");
#    }
     return $out_nodes;
}

# Read in all segments
# - Lookup to/from in nodes_dupe & nodes_unique to find the quantised node for these positions.
# - If to/from map to the same node then discard the segment
# - Search segments list to see if a segment for this to/from pair exists
# - If a new segment pair, add to segments_unique
# - If pair already exists add to segments_dupe with reference to segments_unique
#
sub simplify_segments() {
    print "------------ Simplify Segments\n";

    my $count_segment = 0;
    my $count_unique = 0;
    my $count_interesting = 0;
    my $segment_unique = {};
    my $out_segs = {};

    for my $seg_id (  keys %{$OSM_SEGMENTS} ) {
	my $segment = $OSM_SEGMENTS->{$seg_id};
	my $node_from = $segment->{from};
	my $node_to   = $segment->{to};
	$count_segment++;

	# Find unique node at these positions
	$node_from = $OSM_NODES->{$node_from}->{dupe_id};
	$node_to   = $OSM_NODES->{$node_to}->{dupe_id};
	
	next if ($node_from == $node_to);

	$count_interesting++;

	# Re-order all segments, we only want one segment for (A->B, B->A)
	if ($node_from > $node_to) {
		my $tmp = $node_from;
		$node_from = $node_to;
		$node_to = $tmp;
	}
	
	my $dupe_id = $segment_unique->{"$node_from,$node_to"};
	if (!defined($dupe_id)) {
		$dupe_id = $seg_id;
		$segment_unique->{"$node_from,$node_to"} = $seg_id;
		$count_unique++;
		# Update segment data for output
		$segment->{from} = $node_from;
		$segment->{to} = $node_to;
		$out_segs->{$seg_id} = $segment;
	}
	$segment->{dupe_id} = $dupe_id;	

#	if ( $VERBOSE || $DEBUG ) {
	if ( 1 ) {
		if (!($count_segment % 1000) ) {
	    		printf("segment %d, %d, %d\r",
				$count_segment, $count_interesting, $count_unique);
		}
	}
   }	
    printf("Final segments input=%d, interesting=%d, output=%d, shrunk by %d\n",
	$count_segment, $count_interesting, $count_unique, $count_segment/$count_unique);
   return $out_segs;
}

# Read in all ways, for each segment in a way
# - Look up each segment in segments_{unique,dupe} to find the unique ID.
# - If segment not present then assume if maps to the same from/to quantised position and discard.
# - Look through list of segments for this way, discard if already present in list for this way
# 
sub simplify_ways() {
    print "------------ Simplifying Ways\n";

    my $count_way = 0;
    my $count_interesting = 0;
    my $out_ways = {};

    for my $way_id ( keys %{$OSM_WAYS} ) {
	my $way = $OSM_WAYS->{$way_id};
	my $new_way = {};
	$count_way++;

	SEGMENT: for my $seg_id ( @{$way->{seg}} ) {
		my $segment = $OSM_SEGMENTS->{$seg_id};
	    	next if (!defined $segment);

		my $dupe_id = $segment->{dupe_id};
		next if (!defined $dupe_id); # zero length segment
		
		for my $id (@{$new_way->{seg}}) {
			next SEGMENT if ($id == $dupe_id);
		}
		push(@{$new_way->{seg}},$dupe_id);
	}

	if (defined @{$new_way->{seg}}) {
		$count_interesting++;
		# Update way segments, keep rest of attributes
		$way->{seg} = $new_way->{seg};
		$out_ways->{$way_id} = $way;
	}

#	if ( $VERBOSE || $DEBUG ) {
	if ( 1 ) {
		if (!($count_way % 100) ) {
	    		printf("way %d, %d\r",
				$count_way, $count_interesting);
		}
	}
    }	
    printf("Final ways input=%d, output=%d, shrunk by %d\n",
	$count_way, $count_interesting, $count_way/$count_interesting);
    return $out_ways;
}


########################################################################################
########################################################################################
########################################################################################
#
#                     Main
#
########################################################################################
########################################################################################
########################################################################################


# Set defaults and get options from command line
Getopt::Long::Configure('no_ignore_case');
GetOptions ( 
	     'debug+'              => \$DEBUG,      
	     'd+'                  => \$DEBUG,      
	     'verbose+'            => \$VERBOSE,
	     'MAN'                 => \$man, 
	     'man'                 => \$man, 
	     'h|help|x'            => \$help, 

	     'no-mirror'           => \$Utils::LWP::Utils::NO_MIRROR,
	     'proxy=s'             => \$Utils::LWP::Utils::PROXY,

	     'osm-file=s'          => \$osm_file,
	     'simplify=s'          => \$simplify_degree,
	     )
    or pod2usage(1);

pod2usage(1) if $help;
pod2usage(-verbose=>2) if $man;

die "Must specify minimum feature size in degrees with --simplify=N\n"
	unless defined($simplify_degree);

if ( ! -s $osm_file ) {
    $osm_file = mirror_planet();
};

die "No existing osm File $osm_file\n" 
    unless -s $osm_file;

my $start_time=time();
read_osm_file($osm_file);

my $OUT = {};
$OUT->{tool}     = 'simplify.py';
$OUT->{nodes}    = simplify_nodes();
$OUT->{segments} = simplify_segments();
$OUT->{ways}     = simplify_ways();
write_osm_file("simplify.osm", $OUT);

printf "Simplfied output file \"simplified.osm\" produced at $simplify_degree from $osm_file in %.0f sec\n\n",time()-$start_time;

exit 0;

##################################################################
# Usage/manual

__END__

=head1 NAME

B<simplify.pl> Version 0.01

=head1 DESCRIPTION

B<simplify.pl> is a program to download the planet.osm
Data from Openstreetmap and reduce shrink the data set
by removing data of less than a given size (in degrees).

This Programm is completely experimental, but some Data 
can already be retrieved with it.

So: Have Fun, improve it and send me fixes :-))

=head1 SYNOPSIS

B<Common usages:>

simplify.pl [-d] [-v] [-h] --simplify=<Degrees>

Output is written to "simplify.osm"

=head1 OPTIONS

=over 2

=item B<--man> Complete documentation

Complete documentation

=item B<--no-mirror>

Do not try mirroring the files from the original Server. Only use
files found on local Filesystem.

=item B<--proxy>

use proxy for download

=item B<--osm-file=path/planet.osm>

Select the "path/planet.osm" file to use for the checks

=item B<--simplify=0.1>

Remove all features of less then "0.1" degrees

=back
