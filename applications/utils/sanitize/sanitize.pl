#!/usr/bin/perl
# Copyright (C) 2006, and GNU GPL'd, by Joerg Ostertag

my $VERSION ="check_osm.pl (c) Joerg Ostertag
Initial Version (Sept,2006) by Joerg Ostertag <sanitize-openstreetmap\@ostertag.name>
Version 0.01
";

BEGIN {
    my $dir = $0;
    $dir =~s,[^/]+/[^/]+$,,;
    unshift(@INC,"$dir/perl_lib");
    unshift(@INC,"../perl_lib");
}


use strict;
use warnings;

use File::Basename;
use File::Copy;
use File::Path;
use Getopt::Long;
use IO::File;
use POSIX qw(ceil floor);
use Pod::Usage;

use Utils::File;
use Utils::Math;
use Utils::Debug;
use Utils::LWP::Utils;
use Geo::OSM::Write;
use Data::Dumper;
use XML::Parser;

sub read_osm_file($); # {}
sub check_Data(); #{ }

my ($man,$help);

our ($lat_min,$lat_max,$lon_min,$lon_max) = (0,0,0,0);

our $osm_file ='-';     # The complete osm Filename (including path) for reading
our $osm_out_file ="-"; # if needed, the pure filename of the osm output File
our $do_redeem_lonesome_nodes = 0;

our $OSM;

# Set defaults and get options from command line
Getopt::Long::Configure('no_ignore_case');
GetOptions ( 
	     'debug+'        => \$DEBUG,      
	     'd+'            => \$DEBUG,      
	     'verbose+'      => \$VERBOSE,
	     'MAN'           => \$man, 
	     'man'           => \$man, 
	     'h|help|x'      => \$help, 

	     'osm-file=s'    => \$osm_file,
	     'osm-out-file'  => \$osm_out_file,
	     'redeem-lonesome-nodes' => \$do_redeem_lonesome_nodes,
	     )
    or pod2usage(1);

pod2usage(1) if $help;
pod2usage(-verbose=>2) if $man;

if ( $VERBOSE || $DEBUG ) {
    print STDERR "\nChecking\n";
}
# Empty out global Variables for next loop
$OSM->{nodes}    = {};
$OSM->{segments} = {};
$OSM->{ways}     = {};
$OSM->{tool} = "OpenStreetMap Sanitizer";
#$OSM->{tool} = "JOSM";

my $start_time=time();

read_osm_file( $osm_file );

#  check openstreetmap data in Memory
check_Data();

#print STDERR Dumper(\$OSM);
Geo::OSM::Write::write_osm_file($osm_out_file,$OSM);

if ( $VERBOSE || $DEBUG ) {
    printf STDERR "Check Completed for $osm_file in %.0f sec\n\n",time()-$start_time;
}


exit 0;

##################################################################
# Functions
##################################################################

our ($MainAttr,$Tags, $WaySegments);

# Function is called whenever an XML tag is started
#----------------------------------------------
sub DoStart() {
    my ($expat, $name, %attr) = @_;
    
    if($name eq "node"){
	$Tags={};
	%{$MainAttr} = %attr;
    }
    if($name eq "segment"){
	$Tags={};
	%{$MainAttr} = %attr;
    }
    if($name eq "way"){
	$Tags={};
	$WaySegments=[];
	%{$MainAttr} = %attr;
    }
    if($name eq "tag"){
	# TODO: protect against id,from,to,lat,long,etc. being used as tags
	$Tags->{$attr{"k"}} = $attr{"v"};
    }
    if($name eq "seg"){
	my $id = $attr{"id"};
	push(@{$WaySegments}, $id);
    }
}

# Function is called whenever an XML tag is ended
#----------------------------------------------
sub DoEnd(){
    my ($expat, $element) = @_;
    my $id = $MainAttr->{"id"};
    if($element eq "node"){
	my $node={};
	%{$node} = %{$MainAttr};
	$node->{tag}=$Tags;
	$OSM->{nodes}->{$id} = $node;
	#print "Node:".join(",",keys(%{$Tags}))."\n" if(scalar(keys(%{$Tags}))>0);
    }

    if($element eq "segment"){
	my $segment={};
	%{$segment} = %{$MainAttr};
	$segment->{tag} = $Tags;
	$OSM->{segments}->{$id}=$segment;
    }

    if($element eq "way"){
	my $way={};
	$way->{seg} = $WaySegments;
	$way->{tag} = $Tags;
	$OSM->{ways}->{$id} = $way;
    }

}

# Function is called whenever text is encountered in the XML file
#----------------------------------------------
sub DoChar(){
    my ($expat, $String) = @_;
}

############################################
# -----------------------------------------------------------------------------
sub read_osm_file($) { # Insert Streets from osm File
    my $file_name = shift;

    my $start_time=time();

    print "Read OSM Data\n" if $VERBOSE || $DEBUG;

    print STDERR "Parsing file: $file_name\n" if $DEBUG;
    my $p = XML::Parser->new( Handlers => {
	Start => \&DoStart, 
	End => \&DoEnd, 
	Char => \&DoChar},
			      ErrorContext => 10,
			      );
    
    my $fh = data_open($file_name);
    die "Cannot open OSM File $file_name\n" unless $fh;
    eval {
	$p->parse($fh);
    };
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


# Distance between 2 points
sub p2p_distance($$){
    my $p1 = shift;
    my $p2 = shift;
    my $delta_lat=abs( $p1->{lat} - $p2->{lat} );
    my $delta_lon=abs( $p1->{lon} - $p2->{lon} );
    my $dist = sqrt($delta_lat*$delta_lat+$delta_lon*$delta_lon)*40000/360;
    return $dist;
}


sub adjust_bounding_box($$$){
    my $bbox = shift;
    my $lat = shift;
    my $lon = shift;

    for my $type ( qw(lat_min lat_max lon_min lon_max lat lon )) {
	next if defined ($bbox->{$type});
	if ( $type =~m/min/ ) {
	    $bbox->{$type} = 1000;
	} else {
	    $bbox->{$type} = -1000;
	}
    }
    # remember lat/lon Min/Max 
    $bbox->{lat_min}= min($bbox->{lat_min},$lat);
    $bbox->{lat_max}= max($bbox->{lat_max},$lat);
    $bbox->{lon_min}= min($bbox->{lon_min},$lon);
    $bbox->{lon_max}= max($bbox->{lon_max},$lon);
    $bbox->{lat}= ($bbox->{lat_min}+$bbox->{lat_max})/2;
    $bbox->{lon}= ($bbox->{lon_min}+$bbox->{lon_max})/2;
}

sub adjust_bounding_box_for_segment($$){
    my $bbox = shift;
    my $segment = shift;
    $segment = $OSM->{segments}->{$segment}
    if $segment =~ m/^\d+$/;
    my $node_from = $segment->{from};
    my $node_to   = $segment->{to};
    my $lat1 = $OSM->{nodes}->{$node_from}->{lat};
    my $lon1 = $OSM->{nodes}->{$node_from}->{lon};
    my $lat2 = $OSM->{nodes}->{$node_to}->{lat};
    my $lon2 = $OSM->{nodes}->{$node_to}->{lon};
    adjust_bounding_box($bbox,$lat1,$lon1);
    adjust_bounding_box($bbox,$lat2,$lon2);
}

# ------------------------------------------------------------------
sub tag_string($){
    my $obj = shift;
    my $tags      = $obj->{tag};
    my $tags_string = join(" ",map{" $_:$tags->{$_} "}keys %$tags);
    my $erg ='';
    $erg = " Tags[$tags_string] " if $tags_string;
    return $erg;
}

# ------------------------------------------------------------------
# Create Link to a Node/segment/way
sub link_to_obj($){
    my $obj = shift;

    my $bbox = { lat_min => 1000, lat_max => -1000,
		 lon_min => 1000, lon_max => -1000};

    my $type = "unknown type ";
    my $obj_txt = "";
    if ( defined($obj->{lat})) {          # Node
	$type = "Node";
	$obj_txt = "Node ".$obj->{id}." (".$obj->{lat}.",".$obj->{lon}.")";
	adjust_bounding_box($bbox,$obj->{lat},$obj->{lon});
    } elsif ( defined($obj->{from})) {    # Segment
	$type = "Segment";
	$obj_txt = " ";
	#Segment ".$obj->{id}." (Node ".$obj->{from}." --> Node ".$obj->{to}.")";
	adjust_bounding_box_for_segment($bbox,$obj);
    } elsif ( defined($obj->{seg})) {     # Way
	$type = "Way";
	$obj_txt = "Way: $obj->{id}<br>\n";
	$obj_txt .= "Segments[";
	for my $seg_id ( @{$obj->{seg}} ) {
	    $obj_txt .= "$seg_id,";
	    next unless $seg_id;
	    next unless defined $OSM->{segments}->{$seg_id};
	    adjust_bounding_box_for_segment($bbox,$seg_id);
	}
	$obj_txt .= "]";
    } else {
	print "link_to_obj: unrecognized type for  ".Dumper(\$obj);
    };

    my $erg='';
    my $lat = ($bbox->{lat_min} + $bbox->{lat_max})/2;
    my $lon = ($bbox->{lon_min} + $bbox->{lon_max})/2;
    my $osm_link = "<A target=\"map\" href=\"http://www.openstreetmap.org/index.html?";

    if ( $obj_txt ){
	$erg .= $obj_txt;
    } else {
	$erg .= "$type: ".Dumper(\$obj);
    };
    $erg .= "<font size=-4>".tag_string($obj)."</font>";
    $erg .= "\n";
    if ( $type ne "Node" ) {
	$erg .= "<td>".link_download_josm($bbox). "</td>";
	#$erg .= "Bounding Box:\n";
	#$erg .= sprintf( "(%6.5f,%6.5f) "  ,$bbox->{lat_min},$bbox->{lon_min});
	#$erg .= sprintf( "(%6.5f,%6.5f)\n" ,$bbox->{lat_max},$bbox->{lon_max});
    }


    $erg .= "<td>";
    $erg .= "Map($type) with Zoom: ";
    for my $zoom ( qw( 10 15 20 24 )) {
	$erg .= $osm_link . "lat=$lat&lon=$lon&zoom=$zoom\">$zoom</a> ";
    }
    $erg .= "</td>";
    return $erg;
}

# ------------------------------------------------------------------
# Link to OSM view of Node
sub link_node($){
    my $node = shift;
    $node = $OSM->{nodes}->{$node} if $node =~ m/^\d+$/;

    my $osm_link = "<A target=\"map\" href=\"http://www.openstreetmap.org/index.html?";
    return $osm_link . "lat=$node->{lat}&lon=$node->{lon}&zoom=23\">";
}

sub link_download_josm($){
    my $bbox = shift;
    my $box = "$bbox->{lat_min},$bbox->{lon_min},$bbox->{lat_max},$bbox->{lon_max}";
    my $erg .= "<font size=-3>josm&nbsp;--download=";
    $erg .= sprintf( "%6.5f,%6.5f,"  ,$bbox->{lat_min}-0.001,$bbox->{lon_min}+0.001);
    $erg .= sprintf( "%6.5f,%6.5f\n" ,$bbox->{lat_max}-0.001,$bbox->{lon_max}+0.001);
    $erg .= "</font>";
    return $erg; 
}

# ------------------------------------------------------------------
sub check_osm_segments() { # Insert Streets from osm variables into mysql-db for gpsdrive
    my $start_time=time();

    if ( $VERBOSE || $DEBUG ) {
	print STDERR "------------ Check Segments\n";
    }

    my $max_dist=0;
    my $sum_dist=0;
    my $min_dist=99999999999999;

    my $counted_segmtnts=0;
    for my $seg_id (  keys %{$OSM->{segments}} ) {
	my $segment = $OSM->{segments}->{$seg_id};
	my $node_from = $segment->{from};
	my $node_to   = $segment->{to};


	print "Undefined Node From for Segment $seg_id:".Dumper(\$segment)."\n"
	    unless $node_from;
	print "Undefined Node To   for Segment $seg_id:".Dumper(\$segment)."\n"
	    unless $node_to;
	$OSM->{nodes}->{$node_from}->{connections}++;
	$OSM->{nodes}->{$node_to}->{connections}++;

	my $lat1 = $OSM->{nodes}->{$node_from}->{lat};
	my $lon1 = $OSM->{nodes}->{$node_from}->{lon};
	my $lat2 = $OSM->{nodes}->{$node_to}->{lat};
	my $lon2 = $OSM->{nodes}->{$node_to}->{lon};
	my $dist = p2p_distance($OSM->{nodes}->{$node_from},$OSM->{nodes}->{$node_to});
	$segment->{tag}->{distance}=$dist;

	if ( $node_from &&  $node_to && 
	     $node_from == $node_to ) {
	    $segment->{tag}->{error} .= "from == to";
	    next;
	}

	if ( $lat1 == $lat2 && $lon1 == $lon2 ) {
	    $segment->{tag}->{error} .= "no distance";
	    next;
	} 
	
	if ( $dist > 8 ) {
	    $segment->{tag}->{error} .= "long distance $dist Km,";
	} 
	my $dist_meters = $dist*1000;
	if ( $dist_meters < 10.1 ) {
	    $segment->{tag}->{error} .= sprintf("small distance %.2f Km,",$dist);
	} 
    }

    if ( $VERBOSE) {
	printf "Checked OSM Segments in %.0f sec\n",time()-$start_time;
    }
}

# ----------------------------------------------------------------------------------------
sub check_osm_nodes() { 
    my $start_time=time();
    
    if ( $VERBOSE || $DEBUG ) {
	print STDERR "------------ Check Nodes\n";
    }
    my $node_positions={};
    for my $node_id (  keys %{$OSM->{nodes}} ) {
	my $node = $OSM->{nodes}->{$node_id};
	my $lat = $node->{lat};
	my $lon = $node->{lon};
	push(@{$node_positions->{"$lat,$lon"}},$node_id);
	if (abs($lat)>90) {
	    print "Node $node_id: lat: $lat out of range\n";
	}
	if (abs($lon)>180) {
	    print "Node $node_id: lon: $lon out of range\n";
	}
	if ( !$node->{connections} ) {
	    my $tag_string = tag_string($node);
	    $tag_string =~ s/Tags\[\s*//g;
	    $tag_string =~ s/\s*\]\s*//g;
	    $tag_string =~ s/class:(node|trackpoint)//g;
	    $tag_string =~ s/created_by:JOSM//g;
	    $tag_string =~ s/converted_by:Track2osm//g;
	    $tag_string =~ s/editor:osmpedit-svn//g;
	    $tag_string =~ s/editor:osmpedit//g;
	    $tag_string =~ s/\s*//g;
	    if ($tag_string =~ m/class:viewpoint/ ) {
	    } elsif ($tag_string =~ m/class:village/ ) {
	    } elsif ($tag_string =~ m/class:point of interest/ ) {
	    } elsif ($tag_string) {
		if (0){
		    print "Node $node_id is not connected, but has Tag: ";
		    print tag_string($node)." ";
		    print " ($tag_string) ";
		    print "\n";
		}
	    } else {
		$node->{tag}->{error} .= "node-no-connections";
		if ( $do_redeem_lonesome_nodes ) {
		    delete $OSM->{nodes}->{$node_id};
		}
	    }
	}
    }

    my $count;
    for my $position ( keys %{$node_positions} ) {
	my $nodes = $node_positions->{$position};
	my $node0=$OSM->{nodes}->{$nodes->[0]};
	my $node_lat = $node0->{lat};
	my $node_lon = $node0->{lon};
	my $lat = floor($node_lat/5)*5;
	my $lon = floor($node_lon/5)*5;
	$count->{"node-$lat-$lon"}->{points}++;
	if ( @{$nodes} > 1 ) {
	    $count->{"node-$lat-$lon"}->{err}++;
	    my $link = link_node($node0);
	    #html_out("node","<td>");
	    for my $node ( @{$nodes} ) {
		#xml_out( "node-duplicate",$node);
		#html_out("node",
		#sprintf( "%s ( %d Seg, %s )<br>", $node, $OSM->{nodes}->{$node}->{connections}||0 ,
		#tag_string($OSM->{nodes}->{$node})
		#));
	    }
	    #html_out("node","</td></tr>");
	}
    }

    if ( $VERBOSE) {
	printf "Checked OSM Segments in %.0f sec\n",time()-$start_time;
    }
}

# ----------------------------------------------------------------------------------------
sub check_osm_ways() { 
    my $start_time=time();

    if ( $VERBOSE || $DEBUG ) {
	print STDERR "------------ Check Ways\n";
    }

    # ---------------------- Undefined segments in way
    for my $way_id ( keys %{$OSM->{ways}} ) {
	my $way = $OSM->{ways}->{$way_id};
	
	for my $seg_id ( @{$way->{seg}} ) {
	    if ( ! defined $OSM->{segments}->{$seg_id} ){
		$way->{tag}->{error} .= "way-undef-segment $seg_id";
		next;
	    }
	    $OSM->{segments}->{$seg_id}->{referenced_by_way} ++;
	}
    }

    # ---------------------- No Segments in way
    my $segments_per_way={};
    my $distance_per_way={};
    #html_out("way","<h2>No Segments in way</h2>\n<ul>");
    my $way_tags={};
    for my $way_id (  keys %{$OSM->{ways}} ) {
	my $way = $OSM->{ways}->{$way_id};

	for my $k ( keys %{$way->{tag}} ) {
	    $way_tags->{$k}->{count}++;
	    $way_tags->{$k}->{values}->{$way->{tag}->{$k}} ++;
	}

	if ( ! defined ( $way->{seg} )) {
	    #html_out("way","<li>Way  $way_id has no segments\n".
	    #link_to_obj($way)
	    #);
	    next;
	}
	if ( ! defined ( $way->{tag} )) {
	    #html_out("way-no-tags","<li>Way  $way_id has no tags\n".
	    #link_to_obj($way)
	    #);
	} else {
	    my $tag=$way->{tag};
	    if ( ! defined ( $tag->{name} )) {
		#html_out("way-no-name","<li>Way $way_id has no name\n".
		#link_to_obj($way)
		#);
	    }
	}
	my $count = scalar @{$way->{seg}};
	$count = int($count/10)*10;
	#$count = "$count - ".($count+9);
	$segments_per_way->{$count} ++;

	my $distance=0;
	for my $seg_id ( @{$way->{seg}} ) {
	    next unless defined ($OSM->{segments}->{$seg_id});
	    $distance +=  ($OSM->{segments}->{$seg_id}->{distance}||0);
	}
	
	$distance = int($distance/10)*10;
	#$distance = "$distance - ".($distance+9);
	$distance_per_way->{$distance}++;
    }
    #html_out("way","</ul>");

    # --------------------------
    #html_out("statistics-ways", "<h3>Number of Segments in Ways</h3>");
    #html_out("statistics-ways", "<table>");
    #html_out("statistics-ways", "<tr><th></th> <th></th> </tr>");
    for my $number_of_segments ( sort { $a <=> $b }  keys %{$segments_per_way} ) {
	my $number_of_ways = $segments_per_way->{$number_of_segments};
	#html_out("statistics-ways", "<tr>");
	#html_out("statistics-ways", "<td>"."$number_of_ways  </td><td>ways with </td>");
	#html_out("statistics-ways", "<td>$number_of_segments - ".($number_of_segments+9)." </td><td> Segments </td>\n");
	#html_out("statistics-ways", "<tr>");
    }
    #html_out("statistics-ways", "</table>");

}




# *****************************************************************************
sub check_Data(){
    # Checks and statistics
    check_osm_segments();
    check_osm_nodes();
    check_osm_ways();

}


##################################################################
# Usage/manual

__END__

=head1 NAME

B<check_osm.pl> Version 0.01

=head1 DESCRIPTION

B<sanitize.pl> is a program to check osm data and make sanitizing suggestions.
Data from Openstreetmap

This Programm is completely experimental, but for some cases it
can already be used.

So: Have Fun, improve it and send me fixes :-))

=head1 SYNOPSIS

B<Common usages:>

sanitize.pl [-d] [-v] [-h] [--redeem-lonesome-nodes] [--osm-file=file.osm]  [--osm-out-file=out-file.osm] 

=head1 OPTIONS

=over 2

=item B<--man> Complete documentation

Complete documentation

=item B<--osm-file=path/data.osm>

Select the "path/data.osm" file to use for the checks

=item B<--redeem-lonesome-nodes>

Redeem lonesome Nodes. This means, if the node is not 
connected to anywhere and has no tags it is deleted.

=item B<--osm-file=file.osm>

Input Filename. Default is standardin.

=item B<--osm-out-file=out-file.osm>

Output Filename. Default is standardout.

=head1 JOSM

I you put a File named ~/.josm/external_tools with the following 
content you can access it with the tools menu inside josm.

 <tools>
 <group name="sanitizer">
  <tool
    name="Tweety's OSM sanitizer (All)"
    exec="/usr/bin/perl /home/tweety/bin/sanitize.pl"
    in="selection"
    flags="include_references,include_backreferences"
    out="replace">
  </tool>
  <tool
    name="Tweety's OSM Sanitizer (Selection)"
    exec="/usr/bin/perl /home/tweety/bin/sanitize.pl"
    in="selection"
    flags="include_references,include_backreferences"
    out="replace">
  </tool>
  <tool
    name="Tweety's OSM Sanitizer (Selection,redeem-lonesome-nodes)"
    exec="/usr/bin/perl /home/tweety/bin/sanitize.pl --redeem-lonesome-nodes"
    in="selection"
    flags="include_references,include_backreferences"
    out="replace">
  </tool>
 </group>
 </tools>
=back


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

Jörg Ostertag (sanitize-for-openstreetmap@ostertag.name)

=head1 SEE ALSO

http://www.openstreetmap.org/

=cut
