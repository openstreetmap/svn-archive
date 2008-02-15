#!/usr/bin/perl

# Simplify OSM by removing invisible data at high zome levels
#
# When displaying a map of the whole of the UK there is no point including
# information on nodes < 1km apart since this will be less then 1 pixel
# This script removes the redundant data using the following algorithm
#
# Some code taken from check-osm.pl by Joerg Ostertag
# 
# User specifies the minimum "interesting" square grid size in degrees.
#
# The rough design is as follows.
#
# 
# Read in nodes from OSM file
# - Lat / Lon is quantised to nearest multiple of the specified grid size
# - If this is a new unique location, it gets added as a new node.
# - Otherwise, a small node entry is created pointing back to the existing node for this square
# - The position of the node representing this grid square is the average of all nodes within the square.
#
# Read in all segments
# - Lookup the to/from endpoints in the node table.
# - If these nodes are duplicate entries then lookup the node representing this grid position
# - If to/from map to the same grid position then discard the segment
# - Search segments list to see if a segment for this to/from pair exists
# - If this is a new segment, add it to the list.
# - If pair already exists, add a small entry pointing to the existing segment
#
# Read in all ways, for each segment in a way
# - Find each segment in the segment list.
# - If segment not present then assume if maps to the same from/to quantised position and discard.
# - If this entry is a duplicate segment entry, locate the unique segment for this node pair.
# - Look through list of segments for this way, discard if already present in list for this way
# 
# When all segments for a way have been processed
# - Discard if the way has no segment entries (e.g. doesn't cross a grid square boundary).
#
# 
# Once all data has been read in.
# - Delete duplicate node & segment entries leaving just those that we want
# - Write out the simplified OSM file.
#

my $VERSION ="simplify.pl (c) Jon Burgess
Initial Version (Oct,2006) by Jon Burgess <jburgess@uklinux.net>
Version 0.03
";

BEGIN {
    my $dir = $0;
    $dir =~s,[^/]+/[^/]+$,,;
    unshift(@INC,"$dir/perl");

    unshift(@INC,"../../perl_lib");
    unshift(@INC,"~/svn.openstreetmap.org/applications/utils/perl_lib");
    unshift(@INC,"$ENV{HOME}/svn.openstreetmap.org/applications/utils/perl_lib");
    unshift(@INC,"$ENV{HOME}/projects/openstreetmap/applications/utils/perl_lib");
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

our $osm_file; # The complete osm Filename (including path)
my $simplify_degree; # Reduce data grid to this disatance (in degrees).

use strict;
use warnings;

my $OSM_NODES    = {};
my $OSM_WAYS     = {};
my $OSM_OBJ      = undef; # OSM Object currently read

my $count_node=0;
my $count_node_all=0;
my $count_way=0;
my $count_way_all=0;
my $output;

my $node_positions={};
my $node_unique={};


# -------------------------------------------------------------------
sub quantise($) {
	# Quantise input latitude/longitude to nearest multiple of precision
	my $input = shift;
	# Note: int() has 'issues' with some edge cases but it is good enough for our purposes.
	return int($input/$simplify_degree)*$simplify_degree;
}

# Remove duplicate node entries.
# Duplicate nodes are references to the original node so look like they have the wrong ID
sub delete_duplicate_nodes_data() {
    for my $node_id ( keys %{$OSM_NODES} ) {
	if ($OSM_NODES->{$node_id}->{id} != $node_id) {
		delete $OSM_NODES->{$node_id};
	}
    }
}

sub node_ {
    $OSM_OBJ = undef;
}

sub node {
    my($p, $tag, %attrs) = @_;  
    my $id = delete $attrs{id};
    $OSM_OBJ = {};

    my $lat = delete $attrs{lat};
    my $lon = delete $attrs{lon};
    delete $attrs{timestamp} if defined $attrs{timestamp};

    if ( keys %attrs ) {
	warn "node $id has extra attrs: ".Dumper(\%attrs);
    }

    my $qlat = quantise($lat);
    my $qlon = quantise($lon);
    my $dupe_id = $node_positions->{"$qlat,$qlon"};

    if (!defined($dupe_id)) {
	$node_positions->{"$qlat,$qlon"} = $id;
	$count_node++;
	# Create initial data for this unique node, more duplicates may come along later.
	$OSM_OBJ->{id} = $id;
	$OSM_OBJ->{lat} = $lat;
	$OSM_OBJ->{lon} = $lon;
	$OSM_OBJ->{dupes} = 1;
    	$OSM_NODES->{$id} = $OSM_OBJ;
    } else {
	# Update running average position for the unique node based on this duplicate	
	my $unique_node = $OSM_NODES->{$dupe_id};
	my $count = $unique_node->{dupes};
	$unique_node->{lat} = ($lat + ($unique_node->{lat} * $count)) / ($count + 1);
	$unique_node->{lon} = ($lon + ($unique_node->{lon} * $count)) / ($count + 1);
	$unique_node->{dupes} += 1;
	# Take reference to the original node at this position
	$OSM_NODES->{$id} = $unique_node;
    }

    $count_node_all++;
    if ( $VERBOSE || $DEBUG ) {
	if (!($count_node_all % 1000) ) {
	    printf("node %d (%d) - %dMB\r",$count_node,$count_node_all, mem_usage('vsz'));
	}
    }
}

# --------------------------------------------
sub way_ {
    my $id = $OSM_OBJ->{id};
    if ( @{$OSM_OBJ->{nd}} >0 ) {
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
    delete $attrs{timestamp} if defined $attrs{timestamp};

    if ( keys %attrs ) {
	warn "way $id has extra attrs: ".Dumper(\%attrs);
    }

    if (!$count_way_all ) {
	# Free memory used for processing nodes, not needed for way processing
	$node_unique = undef;
	delete_duplicate_nodes_data();
    }

    print "\n" if !$count_way_all && ($VERBOSE || $DEBUG);
    $count_way_all++;
    printf("way %d(%d) - %dMB\r",$count_way,$count_way_all, mem_usage('vsz')) 
	if !( $count_way_all % 1000 ) && ($VERBOSE || $DEBUG);
}

sub nd {
    my($p, $tag, %attrs) = @_;
    my $id = $attrs{ref} 
    delete $attrs{timestamp} if defined $attrs{timestamp};
    return if (!defined($OSM_NODES->{$id}));

    # If a duplicate segment, locate the unique segment ID.
    $id = $OSM_NODES->{$id}->{id};

    for my $exist_id (@{$OSM_OBJ->{nd}}) {
	return if ($exist_id == $id);
    }

    push(@{$OSM_OBJ->{nd}},$id);
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
	     'out=s'               => \$output,
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

if (!defined($output)) {
	$output = $osm_file;
	$output =~ s/\.osm(\.gz|\.bz2)?$/-simplify-$simplify_degree.osm/;
}

my $OSM = {};
$OSM->{tool}     = 'simplify.py';
$OSM->{nodes}    = $OSM_NODES;
$OSM->{ways}     = $OSM_WAYS;

# Make sure we can create the output file before we start processing data
open(OUTFILE, ">$output") or die "Can’t write to $output: $!";
close OUTFILE;

my $start_time=time();
read_osm_file($osm_file);

delete_duplicate_nodes_data();

printf "Finished processing. Final statistics:\n";
printf " Nodes:    $count_node of $count_node_all\n";
printf " Ways:     $count_way of $count_way_all\n";

write_osm_file($output, $OSM);

printf "$output produced at $simplify_degree degrees resolution from $osm_file in %.0f sec\n\n",time()-$start_time;

exit 0;

##################################################################
# Usage/manual

__END__

=head1 NAME

B<simplify.pl> Version 0.02

=head1 DESCRIPTION

B<simplify.pl> is a program to download the planet.osm
Data from Openstreetmap and reduce shrink the data set
by removing data of less than a given size (in degrees).

This Programm is completely experimental, but some Data 
can already be retrieved with it.

So: Have Fun, improve it and send me fixes :-))

=head1 SYNOPSIS

B<Common usages:>

simplify.pl [-d] [-v] [-h] --simplify=<Degrees> [--osm-file=planet.osm] [--out=<filename>]

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
