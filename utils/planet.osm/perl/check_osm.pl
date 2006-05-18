#!/usr/bin/perl

my $VERSION ="check_osm.pl (c) Joerg Ostertag
Initial Version (Apr,2006) by Joerg Ostertag <joerg.ostertag\@rechengilde.de>
Version 0.01
";

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

my $current_file ="planet-2006-05-01.osm.bz2";

my ($man,$help);

our $CONFIG_DIR    = "$ENV{'HOME'}/.gpsdrive"; # Should we allow config of this?
our $CONFIG_FILE   = "$CONFIG_DIR/gpsdriverc";
our $MIRROR_DIR   = "$CONFIG_DIR/MIRROR";
our $UNPACK_DIR   = "$CONFIG_DIR/UNPACK";

our ($lat_min,$lat_max,$lon_min,$lon_max) = (0,0,0,0);

our ($debug,$verbose,$no_mirror,$PROXY);

sub min($$){
    my $a=shift;
    my $b=shift;
    return $a<$b?$a:$b;
}
sub max($$){
    my $a=shift;
    my $b=shift;
    return $a>$b?$a:$b;
}

use strict;
use warnings;

use IO::File;
use File::Path;
use Geo::Gpsdrive::DBFuncs;
use Geo::Gpsdrive::Utils;
use Data::Dumper;
use XML::Parser;

my $osm_source_id;

my $html_files = {};
my $xml_files = {};


my $osm_nodes    = {};
my $osm_segments = {};
my $osm_ways     = {};
my $osm_stats    = {};
my $osm_obj      = undef; # OSM Object currently read

###########################################

sub node_ {
    $osm_obj = undef
}
sub node {
    my($p, $tag, %attrs) = @_;  
    my $id = delete $attrs{id};
    $osm_obj = {};
    $osm_obj->{id} = $id;

    $osm_obj->{lat} = delete $attrs{lat};
    $osm_obj->{lon} = delete $attrs{lon};

    if ( keys %attrs ) {
	warn "node $id has extra attrs: ".Dumper(\%attrs);
    }

    #obj_compare($osm_nodes->{$id},$osm_obj);
    $osm_nodes->{$id} = $osm_obj;
}

# --------------------------------------------
sub way_ {
    $osm_obj = undef
}
sub way {
    my($p, $tag, %attrs) = @_;  
    my $id = delete $attrs{id};
    $osm_obj = {};
    $osm_obj->{id} = $id;
    $osm_obj->{timestamp} = delete $attrs{timestamp} if defined $attrs{timestamp};

    if ( keys %attrs ) {
	warn "way $id has extra attrs: ".Dumper(\%attrs);
    }
    $osm_ways->{$id} = $osm_obj;
}
# --------------------------------------------
sub segment_ {
    $osm_obj = undef
}
sub segment {
    my($p, $tag, %attrs) = @_;  
    my $id = delete $attrs{id};
    $osm_obj = {};
    $osm_obj->{id} = $id;

    $osm_obj->{from} = delete $attrs{from};
    $osm_obj->{to}   = delete $attrs{to};

    if ( keys %attrs ) {
	warn "segment $id has extra attrs: ".Dumper(\%attrs);
    }
    $osm_segments->{$id} = $osm_obj;
}
# --------------------------------------------
sub seg {
    my($p, $tag, %attrs) = @_;  
    my $id = $attrs{id};
    #print "Seg $id for way($osm_obj->{id})\n";
    push(@{$osm_obj->{seg}},$id);
}
# --------------------------------------------
sub tag {
    my($p, $tag, %attrs) = @_;  
    #print "Tag - $tag: ".Dumper(\%attrs);
    my $k = delete $attrs{k};
    my $v = delete $attrs{v};

    return if $k eq "created_by";

    if ( keys %attrs ) {
	print "Unknown Tag value for ".Dumper($osm_obj)."Tags:".Dumper(\%attrs);
    }
    
    my $id = $osm_obj->{id};
    if ( defined( $osm_obj->{tag}->{$k} ) &&
	 $osm_obj->{tag}->{$k} ne $v
	 ) {
	printf "Tag %8s already exists for obj(id=$id) tag '$osm_obj->{tag}->{$k}' ne '$v'\n",$k ;
    }
    $osm_obj->{tag}->{$k} = $v;
    if ( $k eq "alt" ) {
	$osm_obj->{alt} = $v;
    }	    
}

############################################
# -----------------------------------------------------------------------------
sub read_osm_file($) { # Insert Streets from osm File
    my $file_name = shift;

    my $start_time=time();

    print("Reading $file_name\n") if $verbose || $debug;
    print "$file_name:	".(-s $file_name)." Bytes\n" if $debug;

    if ( $file_name =~ m/planet.*osm/ &&
	 -s "$file_name.storable.node" &&
         -s "$file_name.storable.segment" &&
         -s "$file_name.storable.way" 
         ) {
        $osm_nodes    = Storable::retrieve("$file_name.storable.node");
        $osm_segments = Storable::retrieve("$file_name.storable.segment");
        $osm_ways     = Storable::retrieve("$file_name.storable.way");
	if ( $verbose) {
	    printf "Read $file_name.storable.* in %.0f sec\n",time()-$start_time;
	}
    } else {
	print STDERR "Parsing file: $file_name\n" if $debug;
	my $p = XML::Parser->new( Style => 'Subs' ,
				  );
	
	my $fh = data_open($file_name);
	my $content = $p->parse($fh);
	if (not $p) {
	    print STDERR "WARNING: Could not parse osm data\n";
	    return;
	}
	if ( $verbose) {
	    printf "Read and parsed $file_name in %.0f sec\n",time()-$start_time;
	}
	if ( $file_name eq "planet.osm" ) {
	        Storable::store($osm_nodes   ,"$file_name.node.storable");
		Storable::store($osm_segments,"$file_name.segment.storable");
		Storable::store($osm_ways    ,"$file_name.way.storable");
		if ( $verbose) {
		    printf "Read and parsed and stored $file_name in %.0f sec\n",time()-$start_time;
		}
	    }
    }

    return;
}

# ------------------------------------------------------------------
# Write html form of Problems
sub html_out($$){
    my $type = shift;
    my $message = shift;

    my $fh = undef;
    if ( defined($html_files->{$type}->{fh})) {
	$fh = $html_files->{$type}->{fh};
    } else {
	$fh = IO::File->new(">OSM_errors_$type.html");
	$html_files->{$type}->{fh}=$fh;
	print $fh "<html>\n<head>\n";
	print $fh "<link rel=\"stylesheet\" type=\"text/css\" href=\"/site.css\"/>\n";
	print $fh "</head>\n";
	print $fh "<BODY BGCOLOR=\"#000066\" LINK=\"#6699FF\" ALINK=\"#7799FF\" VLINK=\"#FFFF66\" \n";
	print $fh "text=white marginwidth=\"0\" marginheight=\"0\" leftmargin=\"0\" topmargin=\"0\" >\n";
	print $fh "<title>Open Street Map $type for  $current_file</title>\n";

	print $fh "<h3>Type for $current_file</h3>\n";
	print $fh "<A href=\"index.html\">Back to the Index</a><br/>\n\n";
	if ( defined($html_files->{$type}->{header}) ){
	    print $fh $html_files->{$type}->{header};
	}
    }
    
    print $fh "$message";
}

# ------------------------------------------------------------------
# Write xml form of Problems
sub xml_out($$){
    my $type = shift;
    my $id   = shift;

    my $fh = undef;
    if ( defined($xml_files->{$type}->{fh})) {
	$fh = $xml_files->{$type}->{fh};
    } else {
	$fh = IO::File->new(">OSM_errors_$type.xml");
	$xml_files->{$type}->{fh}=$fh;
	print $fh "<?xml version=\"1.0\"?>\n";
	print $fh "<osm version=\"0.3\" generator=\"OpenStreetMap planet.osm checker\">\n";
    }

    $type =~ s/-.*//;
    print $fh "<$type id=\"$id\"/>\n";
}

# ------------------------------------------------------------------
# Write html form of Problems
sub html_header($$){
    my $type = shift;
    my $message = shift;

    $html_files->{$type}->{header} .= $message;
}

# ------------------------------------------------------------------
# Guess the Street Type if we got a Streetname
sub street_name_2_id($) {
    my $street_name = shift;
    my $streets_type_id =0;
    if ( $street_name =~ m/^A\s*\d+/ ) {
	$streets_type_id = streets_type_name2id('Strassen.Autobahn');
    } elsif ( $street_name =~ m/^ST\s*\d+/ ) {
	$streets_type_id = streets_type_name2id('Strassen.Bundesstrasse');
    } elsif ( $street_name =~ m/^B\s*\d+/ ) {
	$streets_type_id = streets_type_name2id('Strassen.Bundesstrasse');
    }   
    return $streets_type_id;
}

# -----------------------------------------------------------------------------
# Open Data File in predefined Directories
sub data_open($){
    my $file_name = shift;

    if ( -s $file_name ) {
	debug("Opening $file_name");
	my $fh;
	if ( $file_name =~ m/\.gz$/ ) {
	    $fh = IO::File->new("gzip -dc $file_name|")
		or die("cannot open $file_name: $!");
	} elsif ( $file_name =~ m/\.bz2$/ ) {
	    $fh = IO::File->new("bzip2 -dc $file_name|")
		or die("cannot open $file_name: $!");
	} else {
	    $fh = IO::File->new("<$file_name")
		or die("cannot open $file_name: $!");
	}
	return $fh;
    }
    die "cannot Find $file_name\n";
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
    $segment = $osm_segments->{$segment}
    if $segment =~ m/^\d+$/;
    my $node_from = $segment->{from};
    my $node_to   = $segment->{to};
    my $lat1 = $osm_nodes->{$node_from}->{lat};
    my $lon1 = $osm_nodes->{$node_from}->{lon};
    my $lat2 = $osm_nodes->{$node_to}->{lat};
    my $lon2 = $osm_nodes->{$node_to}->{lon};
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
	    next unless defined $osm_segments->{$seg_id};
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
    $erg .= tag_string($obj);
    $erg .= "\n";
    if ( $type ne "Node" ) {
	link_download_bbox($bbox);
	#$erg .= "Bounding Box:\n";
	#$erg .= sprintf( "(%6.5f,%6.5f) "  ,$bbox->{lat_min},$bbox->{lon_min});
	#$erg .= sprintf( "(%6.5f,%6.5f)\n" ,$bbox->{lat_max},$bbox->{lon_max});
    }


    $erg .= "<td>";
    $erg .= "Map with Zoom: ";
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
    $node = $osm_nodes->{$node} if $node =~ m/^\d+$/;

    my $osm_link = "<A target=\"map\" href=\"http://www.openstreetmap.org/index.html?";
    return $osm_link . "lat=$node->{lat}&lon=$node->{lon}&zoom=23\">";
}

sub link_download_bbox($){
    my $bbox = shift;
    my $osm_base_url="http://www.openstreetmap.org/api/0.3/map?bbox=";
    my $box = "$bbox->{lat_min},$bbox->{lon_min},$bbox->{lat_max},$bbox->{lon_max}";
    my $erg .= "Bounding Box:\n";
    $erg .= "<A href =\"$osm_base_url$box\">";
    $erg .= sprintf( "(%6.5f,%6.5f) "  ,$bbox->{lat_min},$bbox->{lon_min});
    $erg .= sprintf( "(%6.5f,%6.5f)\n" ,$bbox->{lat_max},$bbox->{lon_max});
    $erg .= "</a>";
    return $erg; 
}

# ------------------------------------------------------------------
sub check_osm_segments() { # Insert Streets from osm variables into mysql-db for gpsdrive
    my $start_time=time();

    print "------------ Check Segments\n";

    my $max_dist=0;
    my $min_dist=99999999999999;
    my $dist_ranges;
    html_header("segment-from_eq_to","<H2>Segments with same from and to</H2>\n");
    html_header("segment-from_eq_to","<br><table border=1>");
    html_header("segment-from_eq_to","<tr><th>Segment</th><th>Segment<br>Tags</th><th>Node(from==To)</th></tr>");

    html_header("segment-long","<H2>Long Segments </H2>\n");
    html_header("segment-long","<br><table border=1>\n");
    html_header("segment-long","<tr><th>Segment</th> <th>Distance</th> <th>Node From</th><th>Node To</th></tr>");

    html_header("segment-no-distance","<H2>Segments with 0m Distance</H2>\n");
    html_header("segment-no-distance","<table border=1>\n");
    html_header("segment-no-distance","<tr><th>Segment</th><th>Segment<br>Tags</th><th>Node From<br/>Node to</th></tr>");

    html_header("segment-small-distance","<H2>Segments with small Distance</H2>\n");
    html_header("segment-small-distance","<table border=1>\n");
    html_header("segment-small-distance","<tr><th>Segment</th><th>Distance</th></tr>");

    my $segment_tags={};
    for my $seg_id (  keys %{$osm_segments} ) {
	my $segment = $osm_segments->{$seg_id};
	my $node_from = $segment->{from};
	my $node_to   = $segment->{to};


	print "Undefined Node From for Segment $seg_id:".Dumper(\$segment)."\n"
	    unless $node_from;
	print "Undefined Node To   for Segment $seg_id:".Dumper(\$segment)."\n"
	    unless $node_to;
	$osm_nodes->{$node_from}->{connections}++;
	$osm_nodes->{$node_to}->{connections}++;

	my $lat1 = $osm_nodes->{$node_from}->{lat};
	my $lon1 = $osm_nodes->{$node_from}->{lon};
	my $lat2 = $osm_nodes->{$node_to}->{lat};
	my $lon2 = $osm_nodes->{$node_to}->{lon};
	my $delta_lat=abs($lat1-$lat2);
	my $delta_lon=abs($lon1-$lon2);
	my $dist_deg = sqrt($delta_lat*$delta_lat+$delta_lon*$delta_lon);
	my $dist = sqrt($delta_lat*$delta_lat+$delta_lon*$delta_lon)*40000/360;
	$osm_segments->{$seg_id}->{distance}=$dist;

	for my $k ( keys %{$segment->{tag}} ) {
	    $segment_tags->{$k}->{count}++;
	    $segment_tags->{$k}->{values}->{$segment->{tag}->{$k}}++;
	}

	if ( $node_from &&  $node_to && 
	     $node_from == $node_to ) {
	    xml_out( "segment-from_eq_to",$seg_id);
	    html_out("segment-from_eq_to",sprintf( "<tr><td>%d</td> <td>%d</td> " ,$seg_id,$node_from));
	    html_out("segment-from_eq_to","<td>&nbsp;". tag_string($osm_segments->{$seg_id}));
	    html_out("segment-from_eq_to","<td>".link_node($node_from)."link</a></td> </tr>\n");
	    next;
	}

	if ( $lat1 == $lat2 && $lon1 == $lon2 ) {
	    xml_out( "segment-no-distance",$seg_id);
	    html_out("segment-no-distance", sprintf ("<tr><td>%d</td>\n",$seg_id));
	    html_out("segment-no-distance","<td>&nbsp;". tag_string($osm_segments->{$seg_id}));
	    html_out("segment-no-distance",sprintf ("<td>%6d(%14.11f,%14.11f)<br/>",$node_from, $lat1,$lon1));
	    html_out("segment-no-distance",sprintf ("    %6d(%14.11f,%14.11f)</td>",$node_to,$lat2,$lon2));
	    html_out("segment-no-distance","<td>".link_node($node_from)."link</a></td></tr>\n");
	    next;
	} 
	
	if ( $dist > 5 ) {
	    xml_out( "segment-long",$seg_id);
	    html_out("segment-long","<tr>". sprintf("<td>%d</td><td> %f Km</td>\n",$seg_id,$dist));
	    html_out("segment-long",sprintf("<td>%6d</td>\n",$node_from));
	    html_out("segment-long",sprintf("<td>%6d</td>\n",$node_to));
	    html_out("segment-long","<td>".link_to_obj($segment)."</td>");
	    html_out("segment-long","</tr>");
	} 
	my $dist_meters = $dist*1000;
	if ( $dist_meters < 0.1 ) {
	    xml_out( "segment-small-distance",$seg_id);
	    html_out("segment-small-distance",
		     "<li>".
		     sprintf("Segment %d has small Distance %20.18f m <br>\n", $seg_id, $dist_meters).
		     sprintf(" %f Grad  \n",$dist_deg). link_to_obj($segment)."<br>\n".
		     sprintf("\tfrom:%6d(%14.11f,%14.11f)<br>\n",$node_from, $lat1,$lon1).
		     sprintf("\tto:  %6d(%14.11f,%14.11f)<br>\n",$node_to,$lat2,$lon2).
		     sprintf("\tdelta:     (%14.11f,%14.11f)<br>\n",$delta_lat,$delta_lon)
		     );
	} 
	$max_dist=max($dist,$max_dist);
   	$min_dist=min($dist,$min_dist);
	my $dist_range = int($dist/1)*1;
	$dist_range = int($dist*10)/10 if $dist < 1;
	$dist_range = int($dist*100)/100 if $dist < 0.1;
	$dist_ranges->{$dist_range}++;
    }

    html_out("segment-from_eq_to","</table>");

    # ----------------------
    html_out("statistics-segments","<h3>Segment Length</h3>\n");
    html_out("statistics-segments", sprintf("Minimum Segment length: %f m<br/>\n",$min_dist/1000));
    html_out("statistics-segments", sprintf("Maximum Segment length: %5.2f Km<br/>\n",$max_dist));
    html_out("statistics-segments","<br/>\n<table border=1>");
    html_out("statistics-segments",sprintf("<tr><th>Segments Length</th><th>Number of Segments</th></tr>\n",));
    for my $dist_range ( sort { $a <=> $b } 
			 keys %{$dist_ranges} ) {
	html_out("statistics-segments",
		 sprintf("<tr><td> &gt;= $dist_range</td><td align=right> $dist_ranges->{$dist_range}</td></tr>\n",)
		 );
    }
    html_out("statistics-segments","</table>");

    # ----------------------
    html_out("statistics-segments","<h2>Tags in Segments </h2>");
    html_out("statistics-segments","<table border=1><tr><th>Tag Name</th> <th># of Tag usage</th> <th># of different Values</th> </tr>");
    for my $k ( sort keys %{$segment_tags} ) {
	html_out("statistics-segments","<tr>");
	html_out("statistics-segments","<td>$k</td>");
	html_out("statistics-segments","<td align=right>$segment_tags->{$k}->{count}</td>");
	my $values = $segment_tags->{$k}->{values};
	my $num_values = scalar( keys(%{$values}));
	html_out("statistics-segments","<td align=right>$num_values</td>");
	html_out("statistics-segments","<td>");
	my $count =0;
	for my $v ( sort keys %{$values}){
	    html_out("statistics-segments","$v($values->{$v}), ");
	    if ( $count++ > 20 ){ html_out("statistics-segments","..."); last};
	}
	html_out("statistics-segments","</td>");
	html_out("statistics-segments","</tr>\n");
    }
    html_out("statistics-segments","</table>");


    if ( $verbose) {
	printf "Checked OSM Segments in %.0f sec\n",time()-$start_time;
    }
}

# ----------------------------------------------------------------------------------------
sub check_osm_nodes() { 
    my $start_time=time();
    
    print "------------ Check Nodes\n";
    my $node_positions={};
    html_header("node-no-connections","<h2>Nodes without any Connections</h2>\n<br/>\n");
    html_header("node-no-connections","<table border=1>\n\n");
    html_header("node-no-connections","<tr>".
		"<th>Node ID</th>".
		"<th>Lat</th>".
		"<th>Lon</th>".
		"<th>OSM Link</th>".
		"<th>Tags</th>".
		"</tr>\n\n");
    
    my $node_tags={};
    for my $node_id (  keys %{$osm_nodes} ) {
	my $node = $osm_nodes->{$node_id};
	my $lat = $osm_nodes->{$node_id}->{lat};
	my $lon = $osm_nodes->{$node_id}->{lon};
	push(@{$node_positions->{"$lat,$lon"}},$node_id);
	if (abs($lat)>90) {
	    print "Node $node_id: lat: $lat out of range\n";
	}
	if (abs($lon)>180) {
	    print "Node $node_id: lon: $lon out of range\n";
	}
	if ( !$osm_nodes->{$node_id}->{connections} ) {
	    my $tag_string = tag_string($osm_nodes->{$node_id});
	    $tag_string =~ s/Tags\[\s*//g;
	    $tag_string =~ s/\s*\]\s*//g;
	    $tag_string =~ s/class:(node|trackpoint)//g;
	    $tag_string =~ s/created_by:JOSM//g;
	    $tag_string =~ s/editor:osmpedit-svn//g;
	    $tag_string =~ s/editor:osmpedit//g;
	    $tag_string =~ s/\s*//g;
	    if ($tag_string =~ m/class:viewpoint/ ) {
	    } elsif ($tag_string =~ m/class:village/ ) {
	    } elsif ($tag_string =~ m/class:point of interest/ ) {
	    } elsif ($tag_string) {
		if (0){
		    print "Node $node_id is not connected, but has Tag: ";
		    print tag_string($osm_nodes->{$node_id})." ";
		    print " ($tag_string) ";
		    print "\n";
		}
	    } else {
		my $link = link_node($node_id);
		xml_out( "node-no-connections",$node_id);
		html_out("node-no-connections","<tr>");
		html_out("node-no-connections","<td>$node_id </td>");
		html_out("node-no-connections","<td>$lat </td>");
		html_out("node-no-connections","<td>$lon </td>");
		html_out("node-no-connections","<td>$link Link </a> </td>");
		html_out("node-no-connections","<td>&nbsp;". tag_string($osm_nodes->{$node_id}));
		html_out("node-no-connections","</td></tr>");
	    }
	    for my $k ( keys %{$node->{tag}} ) {
		$node_tags->{$k}->{count} ++;
		$node_tags->{$k}->{values}->{$node->{tag}->{$k}} ++;
	    }
	}
    }

    html_out("statistics-nodes","<h2>Tags in Nodes </h2>");
    html_out("statistics-nodes","<table border=1><tr><th>Tag Name</th>  <th># of Tag usage</th> <th># of different Values</th> </tr>");
    for my $k ( sort keys %{$node_tags} ) {
	html_out("statistics-nodes","<tr>");
	html_out("statistics-nodes","<td>$k</td>");
	html_out("statistics-nodes","<td align=right>$node_tags->{$k}->{count}</td>");
	my $values = $node_tags->{$k}->{values};
	my $num_values = scalar( keys(%{$values}));
	html_out("statistics-nodes","<td align=right>$num_values</td>");
	html_out("statistics-nodes","<td>");
	my $count =0;
	for my $v ( sort keys %{$values}){
	    html_out("statistics-nodes","$v($values->{$v}), ");
	    if ( $count++ > 20 ){ html_out("statistics-nodes","..."); last};
	}
	html_out("statistics-nodes","</td>");
	html_out("statistics-nodes","</tr>\n");
    }
    html_out("statistics-nodes","</table>");


    for my $lat ( -180 .. 180 ) {
	for my $lon ( -90 .. 90 ) {
	    html_header("node-$lat-$lon","<H1>Duplicate nodes in $lat/$lon</H1>\n\n\n");
	    html_header("node-$lat-$lon","<br><a href=\"OSM_errors_node.html\">other Nodes</a>\n<br>\n<br>\n");
	    html_header("node-$lat-$lon","<table border=1>\n\n");
	    html_header("node-$lat-$lon","<tr>".
			"<th>Lat</th>".
			"<th>Lon</th>".
			"<th>OSM Link</th>".
			"<th>Node_Id ( # Segments Connected, Tags), ...</th>".
			"</tr>\n\n");
	}
    }
    my $count;
    for my $position ( keys %{$node_positions} ) {
	my $nodes = $node_positions->{$position};
	my $node0=$osm_nodes->{$nodes->[0]};
	my $node_lat = $node0->{lat};
	my $node_lon = $node0->{lon};
	my $lat = int($node_lat/10)*10;
	my $lon = int($node_lon/10)*10;
	$count->{"node-$lat-$lon"}->{points}++;
	if ( @{$nodes} > 1 ) {
	    $count->{"node-$lat-$lon"}->{err}++;
	    my $link = link_node($node0);
	    html_out("node-$lat-$lon","<tr>");
	    html_out("node-$lat-$lon","<td>$node_lat </td>");
	    html_out("node-$lat-$lon","<td>$node_lon </td>");
	    html_out("node-$lat-$lon","<td>$link Link </a> </td>");
	    html_out("node-$lat-$lon","<td>");
	    for my $node ( @{$nodes} ) {
		xml_out( "node-duplicate",$node);
		html_out("node-$lat-$lon",
			 sprintf( "%s ( %d Seg, %s )<br>",
				  $node,
				  $osm_nodes->{$node}->{connections}||0 ,
				  tag_string($osm_nodes->{$node})
				  ));
	    }
	    html_out("node-$lat-$lon","</td></tr>");
	}
    }

    html_out("node","<h3>Duplicate Nodes</h3>\n");
    html_out("node","<table border=1>\n");
    html_out("node","<tr>".
	     "<th>From<br>lat,lon</th>".
	     "<th><br></th>".
	     "<th>To<br>lat,lon</th>".
	     "<th>Points in Area</th>".
	     "<th>Duplicate<br>Locations</th>".
	     "<th>File Size</th>".
	     "<th>Errors</th>".
	     "</tr>\n");
    for  ( my $lat=-180 ; $lat<180 ; $lat+=10 ) {
	for  ( my $lon=-90 ; $lon<90 ; $lon+=10 ) {
	    my $count_err    = $count->{"node-$lat-$lon"}->{err};
	    my $count_points = $count->{"node-$lat-$lon"}->{points};
	    next unless $count_points;
	    my $size='';
	    my $size_end='';
	    my $file = "OSM_errors_node-$lat-$lon.html";
	    my $link = "";
	    my $link_e = "";
	    if ( $count_err ) {
		print "node $lat/$lon: $count_err Errors\n";
		html_out("node-$lat-$lon","</table>");
		html_out("node-$lat-$lon","<br><br>");
		html_out("node-$lat-$lon","$count_err Locations with more than 1 node defined<br>\n ");
		$link   = "<A href=\"$file\">";
		$link_e = "</A>";
	    } else {
		$size="<font size=-4>";
		$size_end='</font>';
	    }
	    
	    html_out("node","<tr>");
	    html_out("node","<td>$size $link ($lat,$lon) $size_end</td>");
	    html_out("node","<td>... </td>");
	    html_out("node","<td>$size (".($lat+10).",".($lon+10).") $link_e$size_end</td>");
	    html_out("node","<td align=right>$size $count_points $size_end</td>");
	    if ( $count_err ) {
		html_out("node","<td align=right> $link $count_err $link_e </td>");
		html_out("node","<td>$link ".sprintf("%4.2f MB ",(-s $file)/1024/1024 )."$link_e</td>");
		html_out("node",sprintf("<td align=right>  %5.2f %%</td>",$count_err/$count_points*100));
	    } else {
		html_out("node","<td align=right>$size 0      $size_end</td>");
		html_out("node","<td>$size &nbsp; $size_end</td>");
		html_out("node","<td align=right>$size 0 %    $size_end</td>");
	    }
	    html_out("node","</tr>");
	}
    }
    html_out("node","</table>\n");

    # ------------
    my $con_counter;
    for my $node_id (  keys %{$osm_nodes} ) {
	#print "$node_id\n";
	my $con = 0;
	$con = $osm_nodes->{$node_id}->{connections} if defined $osm_nodes->{$node_id}->{connections};
	$con ||= 0;

	$con_counter->{$con} = 0 unless defined $con_counter->{$con};
	$con_counter->{$con} ++;
    }

    html_out("statistics-nodes", "<h3>Nodes with connected Segments</h3>");
    html_out("statistics-nodes", "<table border=1>");
    html_out("statistics-nodes", "<tr> <th>Segments <br/>connected</th> <th>Number<br/> of Nodes</th></tr>\n");
    for my $connections ( sort { $a <=> $b } keys %{$con_counter} ) {
	html_out("statistics-nodes","<tr>");
	html_out("statistics-nodes","<td>".$connections."</td>");
	html_out("statistics-nodes","<td>".$con_counter->{$connections}."</td>");
	html_out("statistics-nodes","</tr>");
    }
    html_out("statistics-nodes", "</table>");

    
    if ( $verbose) {
	printf "Checked OSM Segments in %.0f sec\n",time()-$start_time;
    }
}

# ----------------------------------------------------------------------------------------
sub check_osm_ways() { 
    my $start_time=time();

    print "------------ Check Ways\n";

    # ---------------------- Undefined segments in way
    html_out("way","<h2>Undefined segments in way</h2>\n");
    html_out("way","<table border=1>\n");
    html_out("way","<tr><th>Way Id</th> <th>Segment Id</th> </tr>\n");
    for my $way_id ( keys %{$osm_ways} ) {
	my $way = $osm_ways->{$way_id};
	
	for my $seg_id ( @{$way->{seg}} ) {
	    if ( ! defined $osm_segments->{$seg_id} ){
		xml_out("way-undef-segment",$way_id);
		html_out("way",
			 "<tr><td>$way_id</td> <td>$seg_id</td>".
			 "<td>".link_to_obj($way)."</td></tr>");
		next;
	    }
	    $osm_segments->{$seg_id}->{referenced_by_way} ++;
	}
    }
    html_out("way","</table>\n");

    # ---------------------- No Segments in way
    my $segments_per_way={};
    my $distance_per_way={};
    html_out("way","<h2>No Segments in way</h2>\n<ul>");
    my $way_tags={};
    for my $way_id (  keys %{$osm_ways} ) {
	my $way = $osm_ways->{$way_id};

	for my $k ( keys %{$way->{tag}} ) {
	    $way_tags->{$k}->{count}++;
	    $way_tags->{$k}->{values}->{$way->{tag}->{$k}} ++;
	}

	if ( ! defined ( $way->{seg} )) {
	    html_out("way","<li>Way  $way_id has no segments\n".
		     link_to_obj($way)
		     );
	    next;
	}
	if ( ! defined ( $way->{tag} )) {
	    html_out("way-no-tags","<li>Way  $way_id has no tags\n".
		     link_to_obj($way)
		     );
	} else {
	    my $tag=$way->{tag};
	    if ( ! defined ( $tag->{name} )) {
		html_out("way-no-name","<li>Way $way_id has no name\n".
			 link_to_obj($way)
		     );
	    }
	}
	my $count = scalar @{$way->{seg}};
	$count = int($count/10)*10;
	#$count = "$count - ".($count+9);
	$segments_per_way->{$count} ++;

	my $distance=0;
	for my $seg_id ( @{$way->{seg}} ) {
	    next unless defined ($osm_segments->{$seg_id});
	    $distance +=  $osm_segments->{$seg_id}->{distance};
	}
	
	$distance = int($distance/10)*10;
	#$distance = "$distance - ".($distance+9);
	$distance_per_way->{$distance}++;
    }
    html_out("way","</ul>");

    # --------------------------
    html_out("statistics-ways", "<h3>Number of Segments in Ways</h3>");
    html_out("statistics-ways", "<table>");
    html_out("statistics-ways", "<tr><th></th> <th></th> </tr>");
    for my $number_of_segments ( sort { $a <=> $b }  keys %{$segments_per_way} ) {
	my $number_of_ways = $segments_per_way->{$number_of_segments};
	html_out("statistics-ways", "<tr>");
	html_out("statistics-ways", "<td>"."$number_of_ways  </td><td>ways with </td>");
	html_out("statistics-ways", "<td>$number_of_segments - ".($number_of_segments+9)." </td><td> Segments </td>\n");
	html_out("statistics-ways", "<tr>");
    }
    html_out("statistics-ways", "</table>");

    # --------------------------
    html_out("statistics-ways", "<h3>Distance of Ways</h3>");
    for my $distance_of_segments ( sort { $a <=> $b }  keys %{$distance_per_way} ) {
	my $number_of_ways = $distance_per_way->{$distance_of_segments};
	html_out("statistics-ways", "<li>".
		      sprintf("%4d ways with %4d Km  - %4d Km Distance\n",
			      $number_of_ways,$distance_of_segments,$distance_of_segments+9)
		      );
    }

    # ----------------------
    html_out("statistics-ways","<h2>Tags in Ways </h2>");
    html_out("statistics-ways","<table border=1><tr><th>Tag Name</th>  <th># of Tag usage</th> <th># of different Values</th></tr>");
    for my $k ( sort keys %{$way_tags} ) {
	html_out("statistics-ways","<tr>");
	html_out("statistics-ways","<td>$k</td>");
	html_out("statistics-ways","<td align=right>$way_tags->{$k}->{count}</td>");
	my $values = $way_tags->{$k}->{values};
	my $num_values = scalar( keys(%{$values}));
	html_out("statistics-ways","<td align=right>$num_values</td>");
	html_out("statistics-ways","<td>");
	my $count =0;
	for my $v ( sort keys %{$values}){
	    html_out("statistics-ways","$v($values->{$v}), ");
	    if ( $count++ > 20 ){ html_out("statistics-ways","..."); last};
	}
	html_out("statistics-ways","</td>");
	html_out("statistics-ways","</tr>\n");
    }
    html_out("statistics-ways","</table>");

    # ------------
    if ( $verbose) {
	printf "Checked OSM Ways  in %.0f sec\n",time()-$start_time;
    }

}

# *****************************************************************************
sub check_Data(){

    print "\nDownload and import OSM Data\n";

    my $mirror_dir="$main::MIRROR_DIR/osm";

    -d $mirror_dir or mkpath $mirror_dir
	or die "Cannot create Directory $mirror_dir:$!\n";
    
    my $url = "http://www.ostertag.name/osm/$current_file";
    my $tar_file = "$mirror_dir/$current_file";

    print "Mirror $url\n";
    my $mirror = mirror_file($url,$tar_file);

    print "Read OSM Data\n";
    read_osm_file( $tar_file );
    
    # Checks and statistics
    html_out("statistics-nodes"   ,"OSM Nodes:    " . scalar keys( %$osm_nodes)."<br>\n");
    html_out("statistics-segments","OSM Segments: " . scalar keys( %$osm_segments)."<br>\n");
    html_out("statistics-ways"    ,"OSM Ways:     " . scalar keys( %$osm_ways)."<br>\n");

    check_osm_segments();
    check_osm_nodes();
    check_osm_ways();

    # close all html Files 
    for my $type ( keys %{$html_files} ) {
	my $fh = $html_files->{$type}->{fh};
	next unless $fh;
	print $fh "\n\n\n</html>\n";
	$fh->close();
    }

    # close all xml Files 
    for my $type ( keys %{$xml_files} ) {
	my $fh = $xml_files->{$type}->{fh};
	next unless $fh;
	print $fh "\n\n</osm>\n";
	$fh->close();
    }

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
	     'debug'               => \$debug,      
	     'verbose+'            => \$verbose,
	     'no-mirror'           => \$no_mirror,
	     'proxy=s'             => \$PROXY,
	     'MAN'                 => \$man, 
	     'man'                 => \$man, 
	     'h|help|x'            => \$help, 
	     )
    or pod2usage(1);

pod2usage(1) if $help;
pod2usage(-verbose=>2) if $man;

# Get and Unpack and check openstreetmap  planet.osm from http://www.openstreetmap.org/
check_Data();

print "Check Complete\n";
##################################################################
# Usage/manual

__END__

=head1 NAME

B<check_osm.pl> Version 0.00001

=head1 DESCRIPTION

B<check_osm.pl> is a program to download and check the planet.osm
Data from Openstreetmap

This Programm is completely experimental, but some Data 
can already be retrieved with it.

So: Have Fun, improve it and send me fixes :-))

=head1 SYNOPSIS

B<Common usages:>

check_osm.pl [-d] [-v] [-h] 

=head1 OPTIONS

=over 2

=item B<--man> Complete documentation

Complete documentation

=item B<--no-mirror>

Do not try mirroring the files from the original Server. Only use
files found on local Filesystem.


=item B<--proxy>

use proxy for download

=back
