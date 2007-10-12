##################################################################
package Geo::OSM::SegmentList;
##################################################################

use Exporter;
@ISA = qw( Exporter );
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
@EXPORT = qw( LoadOSM_segment_csv
	      reduce_segments_list
	      read_osm_file
	      load_segment_list
	      );

use strict;
use warnings;

use Math::Trig;

use Data::Dumper;
use Geo::Geometry;
use Geo::OSM::Planet;
use Utils::Debug;
use Utils::File;
use Utils::Math;

sub load_segment_list($){
    my $do_filter_against_osm = shift;

    my $osm_segments;
    if ( -s $do_filter_against_osm ) {
	if (  $do_filter_against_osm =~ m/\.csv/ ) {
	    $osm_segments = Geo::OSM::SegmentList::LoadOSM_segment_csv($do_filter_against_osm);
	} elsif ( $do_filter_against_osm =~ m/\.osm/ ) {
	    $osm_segments = Geo::OSM::SegmentList::read_osm_file($do_filter_against_osm);
	} else {
	    die "Unknown Datatype for $do_filter_against_osm\n";
	}
	#print Dumper(\$osm_segments ) if $DEBUG;
    } elsif (  $do_filter_against_osm !~ m/^\d*$/ ) {
	    die "Unknown Datatype for $do_filter_against_osm\n";
    } else {

	# later we search in:
	#  ~/.osm/data/planet.osm.csv
	# /var/data/osm/planet.osm.csv

	my $home = $ENV{HOME}|| '~/';
	my $path=	planet_dir();
	my $osm_filename = "${path}/csv/osm.csv";
	$osm_filename =~ s,\~/,$home/,;
	printf STDERR "check $osm_filename for loading\n" if $DEBUG;
	
	die "Cannot open $osm_filename\n" unless -s $osm_filename;
	$osm_segments = Geo::OSM::SegmentList::LoadOSM_segment_csv($osm_filename);
    };
    return $osm_segments
}

# ------------------------------------------------------------------
# reduce osm Segments to only those inside the bounding box
# This make comparison faster
sub reduce_segments_list($$) {
    my $all_osm_segments = shift;
    my $bounds           = shift;

    my $start_time=time();

    #printf STDERR "reduce_osm_segments(".Dumper(\$bounds).")\n" if $DEBUG;

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
    if ( $VERBOSE > 3 || $DEBUG > 3 ) {
	printf STDERR "		Reduced OSM Data to $count( $all_count) OSM-Segments ";
	print_time($start_time);
    }

    return $osm_segments;
}

# -------------------------------------------------------
# Load the csv Version of a segment list
sub LoadOSM_segment_csv($)
{
    my $filename = shift;

    printf STDERR "Reading OSM File: $filename\n"
	if $DEBUG || $VERBOSE;
    my $start_time=time();

    my $segments;
    my $count=0;

    if ( -s "$filename.storable" && 
	 ! file_needs_re_generation($filename,"$filename.storable")) {
	# later we should compare if the file also is newer than the source
	$filename .= ".storable";
	$segments = Storable::retrieve($filename);
    } else {
	my $fh = data_open($filename);

	die "Cannot open $filename in LoadOSM_segment_csv.\n".
	    "Please create it first to use the option --osm.\n".
	    "See --help for more info"  unless $fh;

	while ( my $line = $fh ->getline() ) {
	    chomp $line;
	    my @segment;
	    my $dummy;
	    ($segment[0],$segment[1],$segment[2],$segment[3],$dummy) = split(/,/,$line,5);
#	    print STDERR Dumper(\@segment);
	    $segment[4] = angle_north_relative(
					    { lat => $segment[0] , lon => $segment[1] },
					    { lat => $segment[2] , lon => $segment[3] });
	    $segment[5] = $dummy if $DEBUG;
	    push (@{$segments},\@segment);
	    $count++;
	}
	$fh->close();
	Storable::store($segments   ,"$filename.storable");
    }

    if ( $VERBOSE >1 || $DEBUG) {
	printf STDERR "Read and parsed $count Lines in $filename";
	print_time($start_time);
    }

    return($segments);
}


# ----------------------
sub Storable_save($$){
    my $filename = shift;
    my $segments = shift;
    eval{
	Storable::store($segments   ,"$filename.storable");
	};
    if ( $@ ) {
	#warn Dumper(\$segments);
	die "Storable_save(): $@\n";
    }
    printf STDERR "Stored OSM File: $filename as storable\n"
	if $DEBUG || $VERBOSE;
}

# ----------------------
sub Storable_load($){
    my $filename = shift;
    $filename .= ".storable";
    my $segments  = Storable::retrieve($filename);
    printf STDERR "Loaded OSM File: $filename as storable\n"
	if $DEBUG || $VERBOSE;
    return $segments;
}

##################################################################
# read Segment list from osm File
##################################################################

our $read_osm_nodes;
our $read_osm_segments;
our $read_osm_obj;

sub node_ {
    $read_osm_obj = undef;
}
sub node {
    my($p, $tag, %attrs) = @_;  
    
    my $id = delete $attrs{id};
    $read_osm_obj = {};
    $read_osm_obj->{id} = $id;

    $read_osm_obj->{lat} = delete $attrs{lat};
    $read_osm_obj->{lon} = delete $attrs{lon};

    delete $attrs{timestamp};
    delete $attrs{action};
    delete $attrs{visible};
    delete $attrs{user};

    if ( keys %attrs ) {
	warn "node $id has extra attrs: ".Dumper(\%attrs);
    }

    $read_osm_nodes->{$id} = $read_osm_obj;
}

# ----------------------
sub segment_ {
    $read_osm_obj = undef;
}
sub segment {
    my($p, $tag, %attrs) = @_;  

    my $id = delete $attrs{id};
    $read_osm_obj = {};
    $read_osm_obj->{id} = $id;

    $read_osm_obj->{from} = delete $attrs{from};
    $read_osm_obj->{to}   = delete $attrs{to};

    delete $attrs{timestamp};
    delete $attrs{action};
    delete $attrs{visible};
    delete $attrs{user};

    if ( keys %attrs ) {
	warn "segment $id has extra attrs: ".Dumper(\%attrs);
    }

    my @segment;
    my $dummy;
    my $node1 = $read_osm_nodes->{$read_osm_obj->{from}};
    my $node2 = $read_osm_nodes->{$read_osm_obj->{to}};
    ($segment[0],$segment[1],$segment[2],$segment[3]) =
	($node1->{lat},$node1->{lon},$node2->{lat},$node2->{lon});
    
    $segment[4] = angle_north_relative(
				       { lat => $segment[0] , lon => $segment[1] },
				       { lat => $segment[2] , lon => $segment[3] });
    #$segment[5] = $attrs{name} if $DEBUG;
    push (@{$read_osm_segments},\@segment);
}

# ----------------------
sub way_ {
    $read_osm_obj = undef;
}
sub way {
    my($p, $tag, %attrs) = @_;  

    my $id = delete $attrs{id};
}

# ----------------------
sub tag {
    my($p, $tag, %attrs) = @_;  
    #print "Tag - $tag: ".Dumper(\%attrs);
    my $k = delete $attrs{k};
    my $v = delete $attrs{v};

    return if $k eq "created_by";

    if ( keys %attrs ) {
	print "Unknown Tag value for ".Dumper($read_osm_obj)."Tags:".Dumper(\%attrs);
    }
    
    my $id = $read_osm_obj->{id};
    if ( defined( $read_osm_obj->{tag}->{$k} ) &&
	 $read_osm_obj->{tag}->{$k} ne $v
	 ) {
	if ( $DEBUG >1 ) {
	    printf STDERR "Tag %8s already exists for obj tag '$read_osm_obj->{tag}->{$k}' ne '$v'\n",$k ;
	}
    }
    $read_osm_obj->{tag}->{$k} = $v;
    if ( $k eq "alt" ) {
	$read_osm_obj->{alt} = $v;
    }	    
}

# --------------------------------------------
sub read_osm_file($) { # Insert Segments from osm File
    my $filename = shift;

    if ( file_needs_re_generation($filename,"$filename.storable")) {
	print("Reading OSM Segment from File $filename\n") if $VERBOSE || $DEBUG;
	print "$filename:	".(-s $filename)." Bytes\n" if $DEBUG;
	
	print STDERR "Parsing file: $filename\n" if $DEBUG;
	my $p = XML::Parser->new( Style => 'Subs' ,
				  ErrorContext => 10,
				  );
	
	my $fh = data_open($filename);
	if (not $fh) {
	    print STDERR "WARNING: Could not open osm data from $filename\n";
	    return;
	}
	my $content = $p->parse($fh);
	if (not $p) {
	    print STDERR "WARNING: Could not parse osm data from $filename\n";
	    return;
	}
	#warn Dumper(\$read_osm_segments);
	Storable_save($filename,$read_osm_segments);  	
    } else {
	$read_osm_segments=Storable_load($filename);
    }
	return($read_osm_segments);
}

# -------------------------------------------------------

1;

=head1 NAME

Geo::OSM::SegmentList

=head1 COPYRIGHT

Copyright 2006, Jörg Ostertag

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

Jörg Ostertag (planet-count-for-openstreetmap@ostertag.name)

=head1 SEE ALSO

http://www.openstreetmap.org/

=cut
