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
use POSIX qw(ceil floor);


my $current_file ="planet-2006-07.osm.bz2";
#$current_file ="planet.osm.bz2";
#$current_file ="planet-2006-07-a.osm";
$current_file ="planet.osm";

my ($man,$help);

our $CONFIG_DIR    = "$ENV{'HOME'}/.gpsdrive"; # Should we allow config of this?
our $CONFIG_FILE   = "$CONFIG_DIR/gpsdriverc";
our $MIRROR_DIR   = "$CONFIG_DIR/MIRROR";
our $UNPACK_DIR   = "$CONFIG_DIR/UNPACK";

our ($lat_min,$lat_max,$lon_min,$lon_max) = (0,0,0,0);

our ($debug,$verbose,$no_mirror,$PROXY);
our $osm_file; # The complete osm Filename (including path)
my $osm_file_name; # later the pure filename (without dir) of the osm File
our $SELECTED_AREA; # a selected area in lower case for example germany
my $OUTPUT_BASE_DIR="stats";
my $OUTPUT_DIR=" $OUTPUT_BASE_DIR/all"; # this is the directory where all the html file go in

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


my $OSM_NODES    = {};
my $OSM_SEGMENTS = {};
my $OSM_WAYS     = {};
my $OSM_OBJ      = undef; # OSM Object currently read

###########################################

my $area_definitions = {
    #                     min    |    max  
    #                  lat   lon |  lat lon
    uk         => [ [  49  , -11,   64,   3],
		    [ 49.9 ,  -5.8, 54,0.80],
		    ],
    germany    => [ [  47  ,   5,   54,  16] ],
    spain      => [ [  35.5,  -9,   44,   4] ],
    europe     => [ [  35  , -12,   75,  35] ],
    africa     => [ [ -45  , -20,   30,  55] ],
    world_east => [ [ -90  ,  90,  -30, 180] ],
    world_west => [ [ -90  ,  90, -180,  -3] ],
};
my $SELECTED_AREA_filters=undef;

sub in_area($){
    my $obj = shift;
    
    #print "in_area(".Dumper(\$obj).")";;
    #print Dumper(\$SELECTED_AREA_filters);
    for my $a ( @{$SELECTED_AREA_filters}  ) {
	#print Dumper(\$a);
	if (
	    $obj->{lat} >= $a->[0] &&
	    $obj->{lon} >= $a->[1] &&
	    $obj->{lat} <= $a->[2] &&
	    $obj->{lon} <= $a->[3] ) {
	    return 1;
	}
    }
    return 0;
}

###########################################
my $count_node;
my $count_segment;
my $count_way;
my $count_node_all;
my $count_segment_all;
my $count_way_all;

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
    
    #obj_compare($OSM_NODES->{$id},$OSM_OBJ);
    $count_node_all++;
    if ( in_area($OSM_OBJ) ) {
	# TODO: If I don't store the nodes I later can't detect for missing nodes.
	# so I'll have to store node with a special mark to later recognize they 
	# exist, but are not in the area
	$OSM_NODES->{$id} = $OSM_OBJ;
	$count_node++;
    }
    if ( $verbose || $debug ) {
	if (!($count_node_all % 1000) ) {
	    printf("node %d (%d)\r",$count_node,$count_node_all);
	    #$fh->getpos;
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
    print "\n" if !$count_way_all && ($verbose || $debug);
    $count_way_all++;
    printf("way %d(%d)\r",$count_way,$count_way_all) 
	if !( $count_way_all % 1000 ) && ($verbose || $debug);
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
    print "\n" if !$count_segment_all && ($verbose || $debug);
    $count_segment_all++;
    printf("segment %d (%d)\r",$count_segment,$count_segment_all) 
	if !($count_segment_all%5000) && ($verbose || $debug);
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
sub read_osm_file($$) { # Insert Streets from osm File
    my $file_name = shift;
    my $area      = shift;

    die "No OSM file specified\n" unless $file_name;
    die "No area specified\n" unless $area;
    my $start_time=time();

    print "Unpack and Read OSM Data from file $osm_file\n" if $verbose || $debug;
    print "$file_name:	".(-s $file_name)." Bytes\n" if $debug;

    my $storable_base= dirname($file_name)."/storable";
    unless ( -d $storable_base ) {
	mkpath $storable_base
	    or die "Cannot create Directory $storable_base: $!\n";
    }
    $storable_base .= "/".basename($file_name).".$area";
    #print "storable_base: $storable_base\n";
    if ( $file_name =~ m/planet.*osm/ &&
	 -s "$storable_base.node" &&
         -s "$storable_base.segment" &&
         -s "$storable_base.way" 
         ) {
        $OSM_NODES    = Storable::retrieve("$storable_base.node");
        $OSM_SEGMENTS = Storable::retrieve("$storable_base.segment");
        $OSM_WAYS     = Storable::retrieve("$storable_base.way");
	if ( $verbose) {
	    printf "Read $storable_base.* in %.0f sec\n",time()-$start_time;
	}
    } else {
	print STDERR "Parsing file: $file_name\n" if $debug;
	my $p = XML::Parser->new( Style => 'Subs' ,
				  ErrorContext => 10,
				  );
	
	my $fh = data_open($file_name);
	die "Cannot open OSM File $file_name\n" unless $fh;
	eval {
	    $p->parse($fh);
	};
	print "\n" if $debug || $verbose;
	if ( $verbose) {
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
	if ( $file_name =~ m/planet.*osm/ ) {
	        Storable::store($OSM_NODES   ,"$storable_base.node");
		Storable::store($OSM_SEGMENTS,"$storable_base.segment");
		Storable::store($OSM_WAYS    ,"$storable_base.way");
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
	$fh = IO::File->new(">$OUTPUT_DIR/OSM_errors_$type.html");
	die "cannot open  $type-File for writing: $@\n" unless $fh;
	$html_files->{$type}->{fh}=$fh;
	print $fh "<html>\n<head>\n";
	print $fh "<link rel=\"stylesheet\" type=\"text/css\" href=\"/site.css\"/>\n";
	print $fh "</head>\n";
	print $fh "<BODY BGCOLOR=\"#000066\" LINK=\"#6699FF\" ALINK=\"#7799FF\" VLINK=\"#FFFF66\" \n";
	print $fh "text=white marginwidth=\"0\" marginheight=\"0\" leftmargin=\"0\" topmargin=\"0\" >\n";
	print $fh "<title>Open Street Map $type for osm File: $osm_file_name Area:$SELECTED_AREA</title>\n";

	print $fh "<h3>Type for osm File: $osm_file_name Area:$SELECTED_AREA</h3>\n";
	print $fh "<h3>osm File: $osm_file_name Area:$SELECTED_AREA</h3>\n";
	print $fh "<A href=\"index.html\">Back to the Index</a><br/>\n\n";
	if ( defined($html_files->{$type}->{header}) ){
	    print $fh $html_files->{$type}->{header};
	}
    }

    die "undefined write file handler for $type\n" unless $fh;
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
	$fh = IO::File->new(">$OUTPUT_DIR/OSM_errors_$type.xml");
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

# -----------------------------------------------------------------------------
# Open Data File in predefined Directories
sub data_open($){
    my $file_name = shift;

    if ( ! -s $file_name ) {
	if ( -s "$file_name.bz2" ) {
	    $file_name = "$file_name.bz2";
	} elsif ( -s "$file_name.gz" ) {
	    $file_name = "$file_name.gz";
	}
    }

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
    $segment = $OSM_SEGMENTS->{$segment}
    if $segment =~ m/^\d+$/;
    my $node_from = $segment->{from};
    my $node_to   = $segment->{to};
    my $lat1 = $OSM_NODES->{$node_from}->{lat};
    my $lon1 = $OSM_NODES->{$node_from}->{lon};
    my $lat2 = $OSM_NODES->{$node_to}->{lat};
    my $lon2 = $OSM_NODES->{$node_to}->{lon};
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
	    next unless defined $OSM_SEGMENTS->{$seg_id};
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
    $node = $OSM_NODES->{$node} if $node =~ m/^\d+$/;

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

    print "------------ Check Segments\n";

    my $max_dist=0;
    my $sum_dist=0;
    my $min_dist=99999999999999;
    my $dist_ranges;
    html_header("segment-from_eq_to","<H2>Segments with same from and to</H2>\n");
    html_header("segment-from_eq_to","<br><table border=1>");
    html_header("segment-from_eq_to","<tr><th>Segment</th><th>Node(from==To)</th><th>Segment<br>Tags</th></tr>");

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
    my $counted_segmtnts=0;
    for my $seg_id (  keys %{$OSM_SEGMENTS} ) {
	my $segment = $OSM_SEGMENTS->{$seg_id};
	my $node_from = $segment->{from};
	my $node_to   = $segment->{to};


	print "Undefined Node From for Segment $seg_id:".Dumper(\$segment)."\n"
	    unless $node_from;
	print "Undefined Node To   for Segment $seg_id:".Dumper(\$segment)."\n"
	    unless $node_to;
	$OSM_NODES->{$node_from}->{connections}++;
	$OSM_NODES->{$node_to}->{connections}++;

	my $lat1 = $OSM_NODES->{$node_from}->{lat};
	my $lon1 = $OSM_NODES->{$node_from}->{lon};
	my $lat2 = $OSM_NODES->{$node_to}->{lat};
	my $lon2 = $OSM_NODES->{$node_to}->{lon};
	my $dist = p2p_distance($OSM_NODES->{$node_from},$OSM_NODES->{$node_to});
	$OSM_SEGMENTS->{$seg_id}->{distance}=$dist;

	for my $k ( keys %{$segment->{tag}} ) {
	    $segment_tags->{$k}->{count}++;
	    $segment_tags->{$k}->{values}->{$segment->{tag}->{$k}}++;
	}

	if ( $node_from &&  $node_to && 
	     $node_from == $node_to ) {
	    xml_out( "segment-from_eq_to",$seg_id);
	    html_out("segment-from_eq_to",sprintf( "<tr><td>%d</td> <td>%d</td> " ,$seg_id,$node_from));
	    html_out("segment-from_eq_to","<td>&nbsp;". tag_string($OSM_SEGMENTS->{$seg_id}));
	    html_out("segment-from_eq_to","<td>".link_node($node_from)."link</a></td> </tr>\n");
	    next;
	}

	if ( $lat1 == $lat2 && $lon1 == $lon2 ) {
	    xml_out( "segment-no-distance",$seg_id);
	    html_out("segment-no-distance", sprintf ("<tr><td>%d</td>\n",$seg_id));
	    html_out("segment-no-distance","<td>&nbsp;". tag_string($OSM_SEGMENTS->{$seg_id}));
	    html_out("segment-no-distance",sprintf ("<td>%6d(%6.4f,%6.4f)<br/>",$node_from, $lat1,$lon1));
	    html_out("segment-no-distance",sprintf ("    %6d(%6.4f,%6.4f)</td>",$node_to,$lat2,$lon2));
	    html_out("segment-no-distance","<td>".link_node($node_from)."link</a></td></tr>\n");
	    next;
	} 
	
	if ( $dist > 8 ) {
	    xml_out( "segment-long",$seg_id);
	    html_out("segment-long","<tr>". sprintf("<td>%d</td><td> %5.2f Km</td>\n",$seg_id,$dist));
	    html_out("segment-long",sprintf("<td>%6d<br><font size=-3>(%6.4f,%6.4f)</font></td>\n",$node_from, $lat1,$lon1));
	    html_out("segment-long",sprintf("<td>%6d<br><font size=-3>(%6.4f,%6.4f)</font></td>\n",$node_to,$lat2,$lon2));
	    html_out("segment-long","<td>".link_to_obj($segment)."</td>");
	    html_out("segment-long","</tr>");
	} 
	my $dist_meters = $dist*1000;
	if ( $dist_meters < 0.1 ) {
	    xml_out( "segment-small-distance",$seg_id);
	    html_out("segment-small-distance",
		     "<li>".
		     sprintf("Segment %d has small Distance %20.18f m <br>\n", $seg_id, $dist_meters).
		     sprintf("\tfrom:%6d(%6.4f,%6.4f)<br>\n",$node_from, $lat1,$lon1).
		     sprintf("\tto:  %6d(%6.4f,%6.4f)<br>\n",$node_to,$lat2,$lon2)
		     );
	} 
	$max_dist=max($dist,$max_dist);
   	$min_dist=min($dist,$min_dist);
	$sum_dist += $dist;
	$counted_segmtnts++;
	my $dist_range_h = int($dist/1)*1;
	$dist_range_h = floor($dist*10)/10 if $dist < 1;
	$dist_range_h = floor($dist*100)/100 if $dist < 0.1;
	$dist_ranges->{$dist_range_h}++;
    }

    html_out("segment-from_eq_to","</table>");

    # ----------------------
    html_out("statistics-segments","<h3>Segment Length</h3>\n");
    html_out("statistics-segments", sprintf("Minimum Segment length: %f m<br/>\n",$min_dist/1000));
    html_out("statistics-segments", sprintf("Maximum Segment length: %5.2f Km<br/>\n",$max_dist));
    html_out("statistics-segments", sprintf("Average Segment length: %5.2f Km<br/>\n",$sum_dist/$counted_segmtnts)) 
	if $counted_segmtnts;
    html_out("statistics-segments", sprintf("All Segment together length: %5.2f Km<br/>\n",$sum_dist));
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
    html_out("statistics-segments","<table border=1><tr>".
	     "<th>Tag Name</th>".
	     "<th># of Tag usage</th> ".
	     "<th># of different Values</th> </tr>");
    for my $st ( sort keys %{$segment_tags} ) {
	html_out("statistics-segments","<tr>");
	html_out("statistics-segments","<td>$st</td>");
	html_out("statistics-segments","<td align=right>$segment_tags->{$st}->{count}</td>");
	my $values = $segment_tags->{$st}->{values};
	my $num_values = scalar( keys(%{$values}));
	html_out("statistics-segments","<td align=right>$num_values</td>");
	html_out("statistics-segments","<td>");
	my $count =0;
	for my $v ( sort keys %{$values}){
	    html_out("statistics-segments","$v($values->{$v}), ");
	    if ( $count++ > 100 ){ html_out("statistics-segments","..."); last};
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
    for my $node_id (  keys %{$OSM_NODES} ) {
	my $node = $OSM_NODES->{$node_id};
	my $lat = $OSM_NODES->{$node_id}->{lat};
	my $lon = $OSM_NODES->{$node_id}->{lon};
	push(@{$node_positions->{"$lat,$lon"}},$node_id);
	if (abs($lat)>90) {
	    print "Node $node_id: lat: $lat out of range\n";
	}
	if (abs($lon)>180) {
	    print "Node $node_id: lon: $lon out of range\n";
	}
	if ( !$OSM_NODES->{$node_id}->{connections} ) {
	    my $tag_string = tag_string($OSM_NODES->{$node_id});
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
		    print tag_string($OSM_NODES->{$node_id})." ";
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
		html_out("node-no-connections","<td>&nbsp;". tag_string($OSM_NODES->{$node_id}));
		html_out("node-no-connections","</td></tr>");
	    }
	    for my $nt ( keys %{$node->{tag}} ) {
		$node_tags->{$nt}->{count} ++;
		$node_tags->{$nt}->{values}->{$node->{tag}->{$nt}} ++;
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
	    if ( $count++ > 100 ){ html_out("statistics-nodes","..."); last};
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
	my $node0=$OSM_NODES->{$nodes->[0]};
	my $node_lat = $node0->{lat};
	my $node_lon = $node0->{lon};
	my $lat = floor($node_lat/5)*5;
	my $lon = floor($node_lon/5)*5;
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
				  $OSM_NODES->{$node}->{connections}||0 ,
				  tag_string($OSM_NODES->{$node})
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
    my $step=5;
    for  ( my $lat=-180 ; $lat<180 ; $lat+=$step ) {
	for  ( my $lon=-90 ; $lon<90 ; $lon+=$step ) {
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
	    html_out("node","<td>$size $link $lat,$lon $size_end</td>");
	    html_out("node","<td>... </td>");
	    html_out("node","<td>$size ".($lat+$step).",".($lon+$step)." $link_e$size_end</td>");
	    html_out("node","<td align=right>$size $count_points $size_end</td>");
	    if ( $count_err ) {
		html_out("node","<td align=right> $link $count_err $link_e </td>");
		html_out("node","<td>$link ".sprintf("%4.2f MB ",(-s "$OUTPUT_DIR/$file")/1024/1024 )."$link_e</td>");
		html_out("node",sprintf("<td align=right>  %5.2f %%</td>",$count_err/$count_points*100))
		    if $count_points;
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
    for my $node_id (  keys %{$OSM_NODES} ) {
	#print "$node_id\n";
	my $con = 0;
	$con = $OSM_NODES->{$node_id}->{connections} if defined $OSM_NODES->{$node_id}->{connections};
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
    for my $way_id ( keys %{$OSM_WAYS} ) {
	my $way = $OSM_WAYS->{$way_id};
	
	for my $seg_id ( @{$way->{seg}} ) {
	    if ( ! defined $OSM_SEGMENTS->{$seg_id} ){
		xml_out("way-undef-segment",$way_id);
		html_out("way",
			 "<tr><td>$way_id</td> <td>$seg_id</td>".
			 "<td>".link_to_obj($way)."</td></tr>");
		next;
	    }
	    $OSM_SEGMENTS->{$seg_id}->{referenced_by_way} ++;
	}
    }
    html_out("way","</table>\n");

    # ---------------------- No Segments in way
    my $segments_per_way={};
    my $distance_per_way={};
    html_out("way","<h2>No Segments in way</h2>\n<ul>");
    my $way_tags={};
    for my $way_id (  keys %{$OSM_WAYS} ) {
	my $way = $OSM_WAYS->{$way_id};

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
	    next unless defined ($OSM_SEGMENTS->{$seg_id});
	    $distance +=  $OSM_SEGMENTS->{$seg_id}->{distance};
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
	    if ( $count++ > 100 ){ html_out("statistics-ways","..."); last};
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


sub write_top_index(){
    my $fh = IO::File->new(">$OUTPUT_BASE_DIR/index.html");
print $fh '<HTML>
<head>
      <link rel="stylesheet" type="text/css" href="/site.css"/>
</head>

<BODY BGCOLOR="#000066" 
      LINK="#6699FF" ALINK="#7799FF" VLINK="#FFFF66" 
      text=white marginwidth="0" marginheight="0" leftmargin="0" topmargin="0" >
<!-- ------------------------------------------------------------------ -->

<title>Open Street Map</title>


<a align="right" href="http://www.OpenStreetMap.org/">
<img src="../../../../osm/Osm_linkage.png">
</a>


<!-- ------------------------------------------------------------------ -->
<h1>Open Street Map Stats</h1>
';

    for my $planet ( sort glob("$OUTPUT_BASE_DIR/*")){
	next unless -d $planet;
	$planet =~ s,.*/,,;	
	print $fh "<h3>$planet</h3>\n";
	print $fh "<ul>\n";
	for my $area ( sort glob("$OUTPUT_BASE_DIR/$planet/*")){
	    $area =~ s,.*/,,;
	    next unless -d "$OUTPUT_BASE_DIR/$planet/$area";
	    next unless -s "$OUTPUT_BASE_DIR/$planet/$area/index.html";
	    print $fh "<li><A href =\"$planet/$area/index.html\">$area</a></li>\n";
	    for my $file ( sort glob("$OUTPUT_BASE_DIR/$planet/$area/OSM_*.html") ) {
		$file =~ s,.*/,,;
		my $file_short=$file;
		$file_short =~ s/OSM_errors_statistics/stat-/;
		$file_short =~ s/OSM_errors_//;
		$file_short =~ s/\.html$//;
		print $fh " <A href =\"$planet/$area/$file\">$file</a>\n";
	    }
	    print $fh "</li>\n";
	}
	print $fh "</ul>\n";
    }

print $fh '
</font>
</html>
';

}

sub write_index(){
    my $fh = IO::File->new(">$OUTPUT_DIR/index.html");
print $fh '<HTML>
<head>
      <link rel="stylesheet" type="text/css" href="/site.css"/>
</head>

<BODY BGCOLOR="#000066" 
      LINK="#6699FF" ALINK="#7799FF" VLINK="#FFFF66" 
      text=white marginwidth="0" marginheight="0" leftmargin="0" topmargin="0" >
<!-- ------------------------------------------------------------------ -->

<title>Open Street Map</title>


<a align="right" href="http://www.OpenStreetMap.org/">
<img src="../../../../osm/Osm_linkage.png">
</a>


<!-- ------------------------------------------------------------------ -->
<h1>Open Street Map Stats</h1>
';
    print $fh "osm File: $osm_file_name Area:$SELECTED_AREA\n";
print $fh '
<H3>Nodes</h3>
<ul>
<li><A href="OSM_errors_statistics-nodes.html">Statistics Nodes</a>
<li><A href="OSM_errors_node.html">Nodes with errors (html)</a>
<A href="OSM_errors_node-duplicate.xml">(XML)</a>
<li>Nodes without any Segments connected <A href="OSM_errors_node-no-connections.html">html</a>
<A href="OSM_errors_node-no-connections.xml">(XML)</a>
</ul>

<H3>Segments</h3>
<ul>
<li><A href="OSM_errors_statistics-segments.html">Statistics Segments</a>
<li><A href="OSM_errors_segment-from_eq_to.html">Segments with from equal to (html)</a>
    <A href="OSM_errors_segment-from_eq_to.xml">(XML)</a>
<li><A href="OSM_errors_segment-long.html">Long Segments (html)</a>
    <A href="OSM_errors_segment-long.xml">(XML)</a>
<li><A href="OSM_errors_segment-no-distance.html">Segments without distance (html)</a>
    <A href="OSM_errors_segment-no-distance.xml">(XML)</a>
</ul>

<H3>Ways</h3>
<ul>
<li><A href="OSM_errors_statistics-ways.html">Statistics Ways (html)</a>
<li><A href="OSM_errors_way.html">Ways with errors (html)</a>
    <A href="OSM_errors_way-undef-segment.xml">(XML)</a>
</ul>

<!-- ------------------------------------------------------------------ ->
<font size=-1>
<h5>Description how to use the XML Files with josm</h5>

You can use the XML Files in Combination with josm to select all
elements in your area. 
The commandline to do so is:

<pre>java -jar josm-latest.jar \
          osm://lat1,lon1,lat2,lon2 \
          --selection=http://www.ostertag.name/osm/...
</pre> 

This command loads josm with a bounding box (lat1,lon1),(lat2,lon2)
and automagically selects all elements given in the url/file http://www.ostertag.name/osm/...
<br>
Instead of "osm://..." you can also use a url of the OpenStreetMap
homepage. This would be for example 
<pre>http://www.openstreetmap.org/index.html?lat=11&lon=48&zoom=10</pre>

Using a url for the --selection downloads the File everytime you 
start josm. So if you want to avoid this you have to
download the *.xml File by yourself and then use:
<pre>
	--selection=file://...
</pre>
instead.

</font>
</html>
';
}

# *****************************************************************************
sub mirror_Data(){
    print "\nMirror OSM Data\n";

    if ( !$osm_file ) {

	-d "$MIRROR_DIR/osm" or mkpath "$MIRROR_DIR/osm"
	    or die "Cannot create Directory $MIRROR_DIR/osm: $!\n";
	
	my $url = "http://www.ostertag.name/osm/planet/$current_file";
	$osm_file = "$MIRROR_DIR/osm/$current_file";

	print "Mirror $url\n";
	my $mirror = mirror_file($url,$osm_file);
    }
}

# *****************************************************************************
sub check_Data(){
    # Checks and statistics
    html_out("statistics-nodes"   ,"OSM Nodes:    " . scalar keys( %$OSM_NODES)."<br>\n");
    html_out("statistics-segments","OSM Segments: " . scalar keys( %$OSM_SEGMENTS)."<br>\n");
    html_out("statistics-ways"    ,"OSM Ways:     " . scalar keys( %$OSM_WAYS)."<br>\n");

    check_osm_segments();
    check_osm_nodes();
    check_osm_ways();

    # close all html Files 
    for my $type ( keys %{$html_files} ) {
	my $fh = $html_files->{$type}->{fh};
	next unless $fh;
	print $fh "\n\n\n</html>\n";
	$fh->close();
	delete $html_files->{$type}->{fh};
    }

    # close all xml Files 
    for my $type ( keys %{$xml_files} ) {
	my $fh = $xml_files->{$type}->{fh};
	next unless $fh;
	print $fh "\n\n</osm>\n";
	$fh->close();
	delete $xml_files->{$type}->{fh};
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


my $areas_todo;
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
	     'osm-file=s'          => \$osm_file,
	     'area=s'              => \$areas_todo,
	     'base_dir=s'           => \$OUTPUT_BASE_DIR,
	     )
    or pod2usage(1);

pod2usage(1) if $help;
pod2usage(-verbose=>2) if $man;


# Get openstreetmap  planet.osm from http://www.openstreetmap.org/
mirror_Data();


my $planet_name=$osm_file;
$planet_name =~ s,.*/,,;
$osm_file_name = $planet_name;
$planet_name =~ s,^planet-?,,;
$planet_name =~ s,\.osm.*$,,;

$areas_todo=join(',',sort keys %{$area_definitions}) unless defined $areas_todo;
$areas_todo=lc($areas_todo);
write_top_index();

for my $area ( split(',',$areas_todo )) {
    my $start_time=time();
    $SELECTED_AREA = $area;
    
    my $areas = $area_definitions->{$SELECTED_AREA};
    if (! defined($areas) ){
	print "Unknown Area $SELECTED_AREA\n";
	print "Allowed Areas:".join(" ",sort keys %{$area_definitions})."\n";
	exit -1;
    }    
    $SELECTED_AREA_filters = $area_definitions->{$SELECTED_AREA};

    $OUTPUT_DIR="$OUTPUT_BASE_DIR/$planet_name/$SELECTED_AREA";
    print "output dir: $OUTPUT_DIR\n";

    # create directory
    -d $OUTPUT_DIR or mkpath $OUTPUT_DIR
	or die "Cannot create Directory $OUTPUT_DIR:$!\n";
    

    # Empty out global Variables for next loop
    $count_node=0;
    $count_segment=0;
    $count_way=0;
    $count_node_all=0;
    $count_segment_all=0;
    $count_way_all=0;
    $OSM_NODES    = {};
    $OSM_SEGMENTS = {};
    $OSM_WAYS     = {};
    $OSM_OBJ      = undef; # OSM Object currently read

    read_osm_file( $osm_file,$SELECTED_AREA );

    #  check openstreetmap data in Memory
    check_Data();

    # write index.html
    write_index();

    printf "Check Complete for $osm_file/$area in %.0f sec\n",time()-$start_time;

}
write_top_index();

exit 0;

##################################################################
# Usage/manual

__END__

=head1 NAME

B<check_osm.pl> Version 0.01

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

=item B<--osm-file>

select the planet.osm file to use for the checks

=item B<--area>

select an area to view.
Currently there a rough boundings in here for:
 UK
 Germany
 Spain
 Europe
 world_east
 world_west

=item B<--base_dir>

All statistics are written in a subtree placed in this subdirectory

=back
