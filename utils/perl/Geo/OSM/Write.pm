##################################################################
package Geo::OSM::Write;
##################################################################

use Exporter;
@ISA = qw( Exporter );
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
@EXPORT = qw( write_osm_file );

use strict;
use warnings;

use Math::Trig;
use Data::Dumper;

use Geo::Geometry;
use Utils::Debug;
use Utils::File;
use Utils::Math;


# ------------------------------------------------------------------
sub tags2osm($){
    my $obj = shift;
    
    my $erg = "\n";
    for my $k ( keys %{$obj->{tag}} ) {
	my $v = $obj->{tag}{$k};
	if ( ! defined $v ) {
	    warn "incomplete Object: ".Dumper($obj);
	}
	#next unless defined $v;
	$erg .= "    <tag k=\'$k\' v=\'$v\' />\n";
    }
    return $erg;
}

sub write_osm_file($$) { # Write an osm File
    my $filename = shift;
    my $osm = shift;

    my $osm_nodes    = $osm->{nodes};
    my $osm_segments = $osm->{segments};
    my $osm_ways     = $osm->{ways};

    my $count_nodes    = 0;
    my $count_segments = 0;
    my $count_ways     = 0;

    my $generate_ways=$main::generate_ways;

    my $start_time=time();

    printf STDERR ("Writing OSM File $filename\n") if $VERBOSE >1 || $DEBUG>1;

    my $fh;
    if ( $filename eq "-" ) {
	$fh = IO::File->new('>&STDOUT');
	$fh or  die("cannot open STDOUT: $!");
    } else {
	$fh = IO::File->new(">$filename");
    }
    print $fh "<?xml version='1.0' encoding='UTF-8'?>\n";
    print $fh "<osm version=\'0.3\' generator=\'".$osm->{tool}."\'>\n";
    
    # --- Nodes
    for my $node_id (  sort keys %{$osm_nodes} ) {
	next unless $node_id;
	my $node = $osm_nodes->{$node_id};
	my $lat = $node->{lat};
	my $lon = $node->{lon};
	unless ( defined($lat) && defined($lon)){
	    printf STDERR "Node '$node_id' not complete\n";
	    next;
	}
	print $fh "  <node id=\'$node_id\' ";
	print $fh " timestamp=\'".$node->{timestamp}."\' " 
	    if defined $node->{timestamp};
	print $fh " lat=\'$lat\' ";
	print $fh " lon=\'$lon\' ";
	print $fh ">\t";
	print $fh tags2osm($node);
	print $fh "  </node>\n";
	$count_nodes++;
    }

    # --- Segments
    for my $segment_id (  sort keys %{$osm_segments} ) {
	next unless $segment_id;
	my $segment = $osm_segments->{$segment_id};
	my $node_from = $segment->{from};
	my $node_to   = $segment->{to};
	print $fh "  <segment id=\'$segment_id\' ";
	print $fh " timestamp=\'".$segment->{timestamp}."\' " 
	    if defined $segment->{timestamp};
	print $fh " from=\'$node_from\' ";
	print $fh " to=\'$node_to\' ";
	print $fh ">";
	print $fh tags2osm($segment);
	print $fh "  </segment>\n";
	$count_segments++;
    }

    # --- Ways
    for my $way_id ( sort keys %{$osm_ways} ) {
	next unless $way_id;
	my $way = $osm_ways->{$way_id};
	print $fh "  <way id=\'$way_id\'";
	print $fh " timestamp=\'".$way->{timestamp}."\'" 
	    if defined $way->{timestamp};
	print $fh ">";
	print $fh tags2osm($way);
	
	for my $seg_id ( @{$way->{seg}} ) {
	    next unless $seg_id;
	    print $fh "    <seg id=\'$seg_id\'";
	    print $fh " />\n";
	}
	print $fh "  </way>\n";
	$count_ways++;
	
    }

    print $fh "</osm>\n";
    $fh->close();

    if ( $VERBOSE || $DEBUG ) {
	printf STDERR "%-35s:	",$filename;
	printf STDERR " Wrote OSM File ".
	    "($count_nodes Nodes, $count_segments Segments, $count_ways Ways)";
	print_time($start_time);
    }

}

1;
