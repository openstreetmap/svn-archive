# Import Data from http://openstreetmap.org/

use strict;
use warnings;

use Storable ();

package Geo::Gpsdrive::OSM;
use strict;
use warnings;

use IO::File;
use File::Path;
use Geo::Gpsdrive::DBFuncs;
use Geo::Gpsdrive::Utils;
use Geo::OSM::Planet;
use Data::Dumper;
use XML::Parser;
use XML::Simple;
use Geo::Filter::Area;
use Geo::OSM::Planet;
use Utils::Debug;
use Utils::File;
use Utils::LWP::Utils;
use Utils::Math;

our $OSM_polite  = 10; # Wait n times as long as the request lasted
our $AREA_FILTER;
our $PARSING_START_TIME=0;
our $PARSING_DISPLAY_TIME=0;
our $ICON_RULES;
our $ELEMSTYLES;
our $ELEMSTYLES_RULES;
my $SOURCE_ID_NODE;
my $SOURCE_ID_SEGMENT;

our (%MainAttr,%Tags, @WaySegments);
# Stored data
our (%OSM_Nodes, %OSM_Segments, %Stats);
my @needed_segment_tags=qw( name amenity class highway);
my $all_unknown_tags={};

# Estimated Number of elements to show progress while reading in percent
for my $type ( qw( tag node segment way )) {
    $Stats{"${type}s estim"} = estimated_max_count($type);
}
$Stats{"tags"}     = 0;
$Stats{"nodes"}    = 0;
$Stats{"segments"} = 0;
$Stats{"ways"}     = 0;

my $IGNORE_TAG={
    ''            => 1,
    'access'      => 1,
    'angle'       => 1,
    'maxweight'   => 1,
    'bicycle'     => 1,
    'bike'        => 1,
    'bridge'      => 1,
    'bridge'      => 1,
    'car'         => 1,
    'city'        => 1,
    'converted_by' => 1,
    'course'      => 1,
    'created_by'  => 1,
    'creator'     => 1,
    'difficulty'  => 1,
    'direction'   => 1,
    'editor'      => 1,
    'ele'         => 1,
    'elevated'    => 1,
    'elevation'   => 1,
    'fix'         => 1,
    'foot'        => 1,
    'from'        => 1,
    'hdop'        => 1,
    'horse'       => 1,
    'id'          => 1,
    'int_ref'     => 1,
    'is_in'       => 1,
    'junction'    => 1,
    'lanes'       => 1,
    'lat'         => 1,
    'layer'       => 1,
    'level'       => 1,
    'ski'         => 1,
    'country'     => 1,
    'lane'        => 1,
    'loc_ref'     => 1,
    'lon'         => 1,
    'mapping_status' => 1,
    'max_speed'   => 1,
    'maxspeed'    => 1,
    'min_speed'   => 1,
    'motorbike'   => 1,
    'motorcar'    => 1,
    'motorcycle'  => 1,
    'name'        => 1,
    'nat_ref'     => 1,
    'FACC_CODE'   => 1,
    'note'        => 1, 
    'one_way'     => 1,
    'oneway'      => 1,
    'osm_obj_type' => 1,
    'pdop'        => 1,
    'postal_code' => 1,
    'rail'        => 1,
    'ref'         => 1,
    'sat'         => 1,
    'source'      => 1,
    'speed'       => 1,
    'speedlimit'  => 1,
    'time'        => 1,
    'timestamp'   => 1,
    'to'          => 1,
    'tracktype'   => 1,
    'tunnel'      => 1,
    'upload_tag'  => 1,
    'vdop'        => 1,
    'width'       => 1,
    'surface'      => 1,
};
# --------------------------------------------
sub display_status($){
    my $mode = shift;
    return unless $VERBOSE || $DEBUG ;
    return unless time()-$PARSING_DISPLAY_TIME >2;

    $PARSING_DISPLAY_TIME= time();
    print STDERR "\r";
    #print STDERR "$mode(".$AREA_FILTER->name()."): ";
    print STDERR " ". $Stats{"elem read"}. "elem " if $DEBUG>2;

    print STDERR time_estimate($PARSING_START_TIME,
			       $Stats{"elem read"},
			       estimated_max_count("elem"));
    
    print STDERR mem_usage();
    print STDERR "\r";

}

#----------------------------------------------
# Statistics output
sub output_statistic($){
    my $filename = shift;
    print STDERR "Statistics output $filename\n" if $DEBUG;
    if(! open(STATS,">$filename")) {
	warn "output_osm: Cannot write to $filename\n";
	return;
    }
    binmode(STATS,":utf8");

    printf STATS "\n\nStats:\n";
    for my $k ( keys(%Stats) ){
	printf STATS "* %5d %s\n", $Stats{$k}, ($k||'');
    }

}

#----------------------------------------------
sub print_obj($){
    my $obj = shift;
    my $tags_string="";
    for my $k ( sort keys %{$obj} ){
	next if $k =~ m/^(timestamp|id|osm_obj_type|created_by|name)$/;
	$tags_string .= "$k = $obj->{$k}, ";
    }
    warn "  $obj->{id} $obj->{osm_obj_type}\t$tags_string\n" 
	if $tags_string;
};


#----------------------------------------------
# Function is called whenever an XML tag is started
sub DoStart()
{
    my ($expat, $name, %attr) = @_;
    
    if($name eq "node"){
	undef %Tags;
	%MainAttr = %attr;
    }
    if($name eq "segment"){
	undef %Tags;
	%MainAttr = %attr;
    }
    if($name eq "way"){
	undef %Tags;
	undef @WaySegments;
	%MainAttr = %attr;
    }
    if($name eq "tag"){
	# TODO: protect against id,from,to,lat,long,etc. being used as tags
	$Tags{$attr{"k"}} = $attr{"v"};
	$Stats{"tags"}++;
    }
    if($name eq "seg"){
	my $id = $attr{"id"};
	if ( defined ( $OSM_Segments{$id} ) ) {
	    push(@WaySegments, $id);
	}
    }
}

# Function is called whenever an XML tag is ended
#----------------------------------------------
sub DoEnd(){
    my ($expat, $element) = @_;
    my $id = $MainAttr{"id"};
    $Stats{"${element}s read"}++;
    $Stats{"tags read"}++;
    if ( defined( $Stats{"${element}s read"} )
	 &&( $Stats{"${element}s read"}== 1 ) ){
	$Stats{"memory at 1st $element rss"} = sprintf("%.0f",mem_usage('rss'));
	$Stats{"memory at 1st $element vsz"} = sprintf("%.0f",mem_usage('vsz'));
	if ( $DEBUG >1 || $VERBOSE >1) {
	    print STDERR "\n";
	}
    }
    
    if($element eq "node"){
	my $node={ osm_obj_type => 'node' };
	$node->{"lat"} = $MainAttr{"lat"};
	$node->{"lon"} = $MainAttr{"lon"};
	
	if ( $AREA_FILTER->inside($node) ) {
	    $OSM_Nodes{$id} = sprintf("%f,%f",$MainAttr{lat}, $MainAttr{lon});
	    foreach(keys(%Tags)){
		$node->{$_} = $Tags{$_};
	    }
	    write_named_point($node);
	    $Stats{"nodes"}++;
	}
    }

    if($element eq "segment"){
	my $from = $MainAttr{"from"};
	my $to   = $MainAttr{"to"};
	if ( defined($OSM_Nodes{$from}) && 
	     defined($OSM_Nodes{$to})   &&
	     $OSM_Nodes{$from} && 
	     $OSM_Nodes{$to} ) {
	    my $segment={ osm_obj_type => 'segment' };
	    for my $k ( keys %MainAttr ) {
		$segment->{$k} = $MainAttr{$k};
	    }
	    for my $k ( keys %Tags ) {
		$segment->{$k} = $Tags{$k};
	    }
	    $OSM_Segments{$id} = "$from,$to,".find_streets_type_id($segment);
	    
	    $Stats{"segments"}++;
	}
    }

    if($element eq "way"){
	if ( @WaySegments ) {
	    my $way = { osm_obj_type => 'way' };
	    my ($lat1,$lon1,$lat2,$lon2);
	    for my $k ( keys %MainAttr ) {
		$way->{$k} = $MainAttr{$k};
	    }
	    for my $k ( keys %Tags ) {
		$way->{$k} = $Tags{$k};
	    }
	    my $streets_type_id = find_streets_type_id($way);
	    unless ( $streets_type_id ) {
		warn "cannot determine streets_type_id for way($way->{id})\n";
		print_obj($way);
		return;
	    };

	    my $multi_segment= {
		'source_id'       => $SOURCE_ID_SEGMENT,
		'segments'        => [],
		'streets_type_id' => $streets_type_id,
		'streets.streets_type_id' => $streets_type_id,
	    };
	    foreach my $seg_id( @WaySegments ){ # we only have the needed ones in here
		next unless $seg_id;
		next unless defined $OSM_Segments{$seg_id};
		next unless $OSM_Segments{$seg_id};
		my ( $node_from,$node_to,$segment_type_id) = split(",", $OSM_Segments{$seg_id});

		$segment_type_id ||= $streets_type_id;
		unless ( $segment_type_id ) {
		    warn "cannot determine segment_type_id for way($way->{id}) segment($seg_id)\n";
		    next;
		}

#		$segment->{way_element}++;

		($lat1,$lon1) = split(",",$OSM_Nodes{$node_from});
		($lat2,$lon2) = split(",",$OSM_Nodes{$node_to});
		
		$multi_segment->{'streets_type_id'} = $segment_type_id;
		
#		my $street_name=$segment->{name}||$way->{name};
		my $street_name=$way->{name};
		my $comment ='';
		$comment .= "way_id=$id ";
		$comment .= "seg_id=$seg_id ";
		push(@{$multi_segment->{'segments'}},
		 {
		     'lat1' => $lat1, 'lon1' => $lon1, 'alt1' => -99,
		     'lat2' => $lat2, 'lon2' => $lon2, 'alt2' => -99,
		     'name' => $street_name,
		     'comment' => $comment,
		     'streets.source_id' => $SOURCE_ID_SEGMENT,
		     'streets.streets_type_id' => $segment_type_id,
		     'streets_type_id' => $segment_type_id,
		 }
		     );
	    }
	    print "Writing Segments\n" if $debug;
	    street_segments_add($multi_segment);
	    $multi_segment->{'segments'}=[];
	    
	    $Stats{"ways"}++;
	}
    }

    if ( ( $VERBOSE || $DEBUG ) &&
#	 ! ( $Stats{"tags read"} % 10000 ) &&
	 ( time()-$PARSING_DISPLAY_TIME >0.9)

	 )  {
	$PARSING_DISPLAY_TIME= time();
	print STDERR "\r";
	print STDERR "Read(".$AREA_FILTER->name()."): ";
	for my $k ( qw(tags nodes segments ways ) ) {
	    next if $k =~ m/( estim| read| rss| vsz)$/;
	    if ( $DEBUG>6 || $VERBOSE>6) {
		print STDERR $k;
	    } else {
		print STDERR substr($k,0,1);
	    }
	    print STDERR ":".$Stats{$k};
	    printf STDERR "=%.0f%%",(100*$Stats{"$k"}/$Stats{"$k read"})
		if $Stats{"$k read"};
	    printf STDERR "(%d",($Stats{"$k read"}||0);
	    if ( $Stats{"$k read"} && defined($Stats{"$k estim"}) ) {
		printf STDERR "=%.0f%%",(100*($Stats{"$k read"}||0)/$Stats{"$k estim"});
	    }
	    print STDERR ") ";
	}
	
	my $rss = sprintf("%.0f",mem_usage('rss'));
	$Stats{"max rss"} = max($Stats{"max rss"},$rss) if $rss;
	printf STDERR "max-rss %d" ,($Stats{"max rss"}) if $Stats{"max rss"} >$rss*1.3;
	my $vsz = sprintf("%.0f",mem_usage('vsz'));
	$Stats{"max vsz"} = max($Stats{"max vsz"},$vsz) if $vsz;
	printf STDERR "max-vsz %d" ,($Stats{"max vsz"}) if $Stats{"max vsz"} >$vsz*1.3;

	print STDERR mem_usage();
	print STDERR time_estimate($PARSING_START_TIME,$Stats{"tags read"},$Stats{"tags estim"});
	print STDERR "\r";
	if ($DEBUG > 5 ) {
	    print STDERR "\n";
	    for my $k ( sort keys %{$all_unknown_tags}){
		my $v = $all_unknown_tags->{$k};
		next unless $v>1;
		print "\t$v*\t$k\n";
	    }
	}

    }
}

# Function is called whenever text is encountered in the XML file
#----------------------------------------------
sub DoChar(){
    my ($expat, $string) = @_;
}


sub write_named_point($) {
    my $node = shift;
    return unless defined $node;
    my $poi_type_id=0;
    my $unknown_keys='';
    for my $k ( keys %{$node}) {
	next if $k =~ m/^(highway)$/;
	next if $k =~ m/^(osmarender:|source_ref:)/;
	next if defined($IGNORE_TAG->{$k});
	next if defined($IGNORE_TAG->{lc($k)});

	# for streets/segments we need to have a  look at
	# bridge=yes
	# oneway=true
	# ref=A21
	# layer=1
	my $v = $node->{$k};
	#print "check $k=$v\n";
	$poi_type_id = poi_type_id($k,$v);
	if ( $poi_type_id ) {
	    my $values;
	    $values->{'poi.source_id'} = $SOURCE_ID_NODE;
	    $values->{'poi.name'}      = $node->{name}||$node->{ref}||'Unknown OSM POI';
	    $values->{'poi.lat'}       = $node->{lat};
	    $values->{'poi.lon'}       = $node->{lon};
	    $values->{'poi.poi_type_id'} = $poi_type_id;
#		$values->{'poi.comment'}   = $comment;
	    Geo::Gpsdrive::DBFuncs::add_poi($values);
	} else {
	    my $streets_type_id = streets_type_id($k,$v);
	    if ( $streets_type_id ){
		my $icon = $streets_type_id->{icon}->{src};
		if ( $icon ){
		    $unknown_keys .= "$k=$v (elemstyles $icon)";
		    if ( $DEBUG>2) { # Simply a question of available memory
			$all_unknown_tags->{"    <condition k=\"$k\" v=\"$v\"/>\n\tjosm icon=$icon"} ++;
		    }
		};
	    } else {
		$unknown_keys .= "$k=$v ";
		if ( $DEBUG>2) { # Simply a question of available memory
		    $all_unknown_tags->{"    <condition k=\"$k\" v=\"$v\"/>"}++;
		}
	    }
	}
    }
    if ( $unknown_keys && !$poi_type_id ) {
	print STDERR "Unknown Keys:  $unknown_keys\n" if $DEBUG>5;
    }
}


sub find_streets_type_id($) {
    my $obj = shift;
    return unless defined $obj;
    my $poi_type_id=0;
    my $unknown_keys='';
    my $id = $obj->{id};
    my $unknown_tags='';
    for my $k ( keys %{$obj}) {
	next if $k =~ m/^(osmarender:|source_ref:)/;
	next if defined($IGNORE_TAG->{$k});
	next if defined($IGNORE_TAG->{lc($k)});

	my $v = $obj->{$k};
	my $streets_type_id = streets_type_id($k,$v);
	if ( $streets_type_id ) {
	    #my $from = $OSM_Nodes{$obj->{from}};
	    #my $to = $OSM_Nodes{$obj->{from}};
	    #print "Street $id ($from,$to) identified with $k=$v\n";
	    #print Dumper(\$streets_type_id);
	    return $streets_type_id->{streets_type_id};
	}
	$unknown_tags.= " $k=$v ";
    }
    if ( $unknown_tags ) {
	print "  $obj->{osm_obj_type}($id) has unknown tags: $unknown_tags\n" if $DEBUG;
    } else {
	print "  $obj->{osm_obj_type}($id) has no usegfull tags\n" if $DEBUG>3;
    }
    return 0;
}

###########################################

# -----------------------------------------------------------------------------
#my $step = 5;
my $step = .1;
my $download_area = {
    #world      => [-90,-180,  90-$step,180-$step],
    #europe     => [-10,  10,  10,60],
    #usa        => [-xx,  xx,  xx,xx],
    ireland     => [-11,  51,  -4,56],
    uk          => [ -6,  51,   2,60],
    germany     => [  0,  10,  50,60],
    bayern      => [  9,  47,  12,50],
    muenchen    => [ 10,  44,  12,49],
    schweiz     => [  5,  45,  11,48],
    oesterreich => [  9,  46,  17,49],
    italy       => [  6,  36,  20,47.5],
    italyN     => [  6,  44,  14,47.5],
    italyS     => [  9,  36,  20,44],
};

sub download_osm_streets($;$) { # Insert Streets from osm File  
    my $osm_dir = shift;
    my $area    = shift;

    return 
	if $main::no_mirror;

    print "Download OSM Data\n";



    my $osm_auth = " --http-user=$ENV{OSMUSER} --http-passwd=$ENV{OSMPASSWD} ";
    my $osm_base_url="http://www.openstreetmap.org/api/0.3/map?bbox=";
    #$osm_base_url="http://www.openstreetmap.org/api/0.3/map?bbox=11.0,48.0,12.0,49.0

    my ($lat_min,$lon_min,     $lat_max,$lon_max);
    if ( defined $area ) {
	if ( defined ( $download_area->{$area} ) ) {
	    ($lat_min,$lon_min,     $lat_max,$lon_max) = @{$download_area->{$area}};
	} else {
	    warn "Download Area '$area' not registered\n";
	    warn "Existing Areas:\n\t".join("\n\t",keys %{$download_area})."\n";
	    die "\n";	    
	};
	print "Download OSM Area $area ($lat_min,$lon_min,     $lat_max,$lon_max)\n";
	
    } elsif ( $main::lat_min && $main::lat_max && $main::lon_min && $main::lon_max ){
	$lat_min=$main::lat_min;
	$lat_max=$main::lat_max;
	$lon_min=$main::lon_min;
	$lon_max=$main::lon_max;
    } else {
	die "Undefined OSM-Area for Download\n";
    }

    for ( my $lon = $lon_min; $lon < $lon_max; $lon += $step ) {
	for ( my $lat = $lat_min; $lat< $lat_max; $lat += $step ) {
	    my $lap  = $lat + $step;
	    my $lop  = $lon + $step;
	    print "Get $lat,$lon                       \r";
	    my $abs_filename= sprintf("$osm_dir/Streets_osm_%05.2f,%06.2f,%05.2f,%06.2f.osm",
				      $lon,$lat, $lop,$lap  );
	    my $bbox = sprintf("%.1f,%.1f,%.1f,%.1f",$lat,$lon,$lap,$lop);

	    #my $mirror = mirror_file("$osm_base_url$bbox",$abs_filename);
	    
	    if ( $debug ) {
		print "mirror: $bbox                                       \n";
	    }
	    
	    my $rest_tries=10;
	    while ($rest_tries){
		my $size = (-s $abs_filename)||0;
		if ( $size == 538 || $size < 76 ) {
		    my $get_cmd ='';
		    # $get_cmd .= "time " if $debug;
		    $get_cmd .= "curl --netrc ";
		    $get_cmd .= " -o  $abs_filename ";
		    # $get_cmd .= "wget -O $abs_filename $osm_auth ";
		    #$get_cmd .= " -q " unless $debug || $VERBOSE;
		    $get_cmd .= " -v " if $debug && $VERBOSE;
		    $get_cmd .= " -s " unless $debug || $VERBOSE;
		    $get_cmd .= "    '$osm_base_url$bbox'"; 
		    print "$get_cmd\n";
		    my $start_time=time();
		    `$get_cmd`;		    $size = (-s $abs_filename)||0;
		    my $diff_time = time()-$start_time;
		    my $sleep_time= $OSM_polite*$diff_time;
		    print "sleep ".($sleep_time)." seconds\n";
		    sleep $sleep_time;
		}

		my $count_error = `grep -e '400 Bad Request' -e '<H1>ERROR</H1>' -e '<h1>Internal Server Error</h1>' $abs_filename | wc -l`;
		if ( $count_error >0 ) {
		    `cat $abs_filename`;
		    $rest_tries --;
		    print "	ERROR in downloading\n";
		    print "   $rest_tries retries\n";
		    print "	Got $lat,$lon          $size Bytes\n";
		    my $err_filename = $abs_filename;
		    $err_filename =~ s/\.osm$/.error/;
		    rename($abs_filename,$err_filename);
		} else {
		    print "Got $lat,$lon             $size Bytes\n"
			unless $size == 76;
		    $rest_tries=0;
		}
	    }
	}
    }
}

# ------------------------------------------------------------------
# get the poi_type_id from icon_rules without pulluting the hash-structure
sub streets_type_id($$){
    my $k = lc(shift);  # for now lowercase, so we catch more
    my $v = lc(shift);  # for now lowercase, so we catch more
    return 0 unless defined $ELEMSTYLES->{$k};
    return 0 unless defined $ELEMSTYLES->{$k}->{$v};
    my $rule =  $ELEMSTYLES->{$k}->{$v};
    return $rule;
}

# ------------------------------------------------------------------
# load the complete MapFeatures Structure into memory
sub load_elemstyles($){
    my $filename = shift;
    return unless $filename && -s $filename;
    print("Loading Elemstyles $filename\n") if $VERBOSE || $DEBUG;
    print "$filename:	".(-s $filename)." Bytes\n" if $DEBUG;
    print STDERR "Parsing file: $filename\n" if $DEBUG;

    my $fh = data_open($filename);
    die "Could not open $filename\n" unless $fh;
    my $rules = XMLin($fh);   
    die "Could not parse $filename\n" unless $rules;

    my $max_street_id=100;
    for my $rule ( @{$rules->{'rule'}} ) {
	#print Dumper(\$rule);
	unless ( defined($rule->{streets_type_id} ) ){
	    $rule->{streets_type_id} = $max_street_id++;
	}
	my $k = $rule->{condition}->{k};
	my $v = $rule->{condition}->{v};
	if ( defined( $rule->{icon} ) ) {
	    $ELEMSTYLES->{$k}->{$v} = $rule;
	}
	if ( defined( $rule->{line} ) ) {
	    $ELEMSTYLES->{$k}->{$v} = $rule;
	    push(@{$ELEMSTYLES_RULES}, $rule);
	}
	if ( defined( $rule->{area} ) ) {
	    $ELEMSTYLES->{$k}->{$v} = $rule;
	}
    }
    print "read up to id:$max_street_id\n";
}


# ------------------------------------------------------------------
# get the poi_type_id from icon_rules without pulluting the hash-structure
sub poi_type_id($$){
    my $k = lc(shift);  # for now lowercase, so we catch more
    my $v = lc(shift);  # for now lowercase, so we catch more
    return 0 unless defined $ICON_RULES->{$k};
    return 0 unless defined $ICON_RULES->{$k}->{$v};
    return $ICON_RULES->{$k}->{$v};
}

# ------------------------------------------------------------------
# load the complete ixons.xml into memory
# in the end we have a hash structure where you get the poi_type_id 
# by simply asking for 
#     $ICON_RULES->{$k}->{$v};
sub load_icons($){
    my $filename = shift;
    return unless $filename && -s $filename;

    print("Loading Icons $filename\n") if $VERBOSE || $DEBUG;
    print "$filename:	".(-s $filename)." Bytes\n" if $DEBUG;
    print STDERR "Parsing file: $filename\n" if $DEBUG;

    my $fh = data_open($filename);
    if (not $fh) {
	die "Could not open $filename\n";
	return;
    }
    my $rules = XMLin($fh);   
    if (not $rules) {
	die "Could not parse $filename\n";
	return;
    }

    $ICON_RULES={};

    sub add_rule($$){
	my $rule = shift;
	my $condition = shift;
	my $k = $condition->{'k'};
	my $v = $condition->{'v'};
	$ICON_RULES->{$k}->{$v}=$rule->{'geoinfo'}->{'poi_type_id'};
    }
    for my $rule ( @{$rules->{'rule'}} ) {
	my $condition = $rule->{'condition'};
	if ( ref($condition) eq "HASH" ) {
	    add_rule($rule,$condition);
	} else {
	    for my $cond (@{$condition} ) {
		add_rule($rule,$cond);
	    }
	}
    }
    #warn Dumper(\$rules);
    #warn Dumper(\$ICON_RULES);
}


# -----------------------------------------------------------------------------
sub read_osm_file($$) { # Insert Streets from osm File
    my $file_name = shift;
    my $area_name = shift;

    my $start_time=time();

    $AREA_FILTER = Geo::Filter::Area->new( area => $area_name );

    print("\rReading $file_name for $area_name\n") if $VERBOSE || $debug;
    print "$file_name:	".(-s $file_name)." Bytes\n" if $debug;
    
    print STDERR "Parsing file: $file_name\n" if $debug;
    $PARSING_START_TIME=time();
    
    my $p = new XML::Parser( Handlers => {
	Start => \&DoStart, 
	End => \&DoEnd, 
	Char => \&DoChar});
    
    my $fh = data_open($file_name);
    my $content;
    eval {
	$content = $p->parse($fh);
    };
    
    if ($DEBUG >2 ) {
	print STDERR "Unknown Tags in Area($area_name)of File($file_name):\n";
	for my $k ( sort keys %{$all_unknown_tags}){
	    my $v = $all_unknown_tags->{$k};
	    next unless $v>1;
	    print "\t$v*\t$k\n";
	}
    }
    
    if ($@) {
	die "ERROR: Could not parse osm data $file_name\n".
	    "$@\n";
    }
    if (not $p) {
	die "ERROR: Could not parse osm data $file_name\n";
    }
    if ( $VERBOSE) {
	printf "Read and parsed $file_name in %.0f sec\n",time()-$start_time;
    }
    return;
}

# -----------------------------------------------------------------------------
sub read_osm_dir($$) { # read all OSM Files in Directory
    my $osm_dir = shift;
    my $area_name = shift;

    my $osm_auth = " --http-user=$ENV{OSMUSER} --http-passwd=$ENV{OSMPASSWD} ";
    my $osm_base_url="http://www.openstreetmap.org/api/0.3/map?bbox=";
    # http://www.openstreetmap.org/api/0.3/map?bbox=11.0,48.0,12.0,49.0

    for my $abs_filename ( 
			   #glob("$osm_dir/planet.osm"),
			   glob("$osm_dir/Streets*.osm"),
			   glob("$osm_dir/Streets*.xml"),
			   glob("$osm_dir/Streets*.gz"),
			   glob("$osm_dir/Streets*.bz2"),
			   ) {
	$abs_filename .= ".gz"	if  -s "$abs_filename.gz" && ! -s $abs_filename;
	my $size = (-s $abs_filename)||0;
	if ( $size == 538 ) { # Probably Internal Error Message
	    `cat $abs_filename`;
	    next;
	}
	next unless $size >76; # Empty File (Only Header, no nodes)
	print "$abs_filename:	$size Bytes\n";
	read_osm_file($abs_filename,$area_name);
    }
}


# ------------------------------------------------------------------
my $class2type = {
    "barn"		=> "area.building.barn",
    "bridge"		=> "area.bridge",
    "campsite"		=> "accommodation.camping",
    "car park"		=> "vehicle.parking",
    "caution"		=> "misc.caution",
    "church"		=> "religion.church",
    "city"		=> "places.settlement.city",
    "country park"	=> "recreation.park",
    "farm"		=> "area.area.farm",
    "hamlet"		=> "places.settlement.hamlet",
    "hill"		=> "area.area.hill",
    "historic-name"	=> "sightseeing",
    "industrial area"	=> "area.area.industial-area",
    "large town"	=> "places.settlement.city",
    "lift"		=> "transport.station.lift",
    "locality"		=> "unknown",
    "parking"		=> "vehicle.parking",
    "point of interest"	=> "unknown",
    "pub"		=> "food.pub",
    "railway crossing"	=> "area.railway-crossing",
    "railway station"	=> "transport.railway",
    "restaurant"	=> "food.restaurant",
    "school"		=> "education.school",
    "small town"	=> "places.settlement.town",
    "suburb"		=> "places.settlement.town",
    "tea shop"		=> "shopping.groceries.tea",
    "town"		=> "places.settlement.town",
    "trafficlight"	=> "vehicle.trafficlight",
    "tunnel"		=> "area.tunnel",
    "viewpoint"		=> "sightseeing.viewpoint",
    "village"		=> "places.settlement.village",
    "Village"		=> "places.settlement.village",
    "waypoint"		=> "waypoint",
};

# ------------------------------------------------------ 
# Segments not in Ways
# Insert Streets from osm variables into mysql-db for gpsdrive
# Take all the segments which are not connected otherwise
sub fill_osm_rest_segments() { 
    my $source_id= get_source_id("segment");

    my $start_time=time();

    my $rest_segments=1; # wrong estimation

    my $segment_count=0;
    my $new_segment_count=0;
    my $streets_type_id = streets_type_name2id('Strassen.minor');
    my $estimate_timer=time();
    for my $seg_id ( keys %OSM_Segments ) {
	my $segment = $OSM_Segments{$seg_id};
	$segment_count++;
	# For DEBUGGING:
	# last if $segment_count>1000;
	if ( !($segment_count % 1000)) {
	    my $percent = $segment_count/$rest_segments*100;
	    my $time_diff=time()-$estimate_timer;
	    my $segments_missing=$rest_segments-$segment_count;
	    my $rest_estimate = $time_diff/1000*$segments_missing;
	    $estimate_timer=time();
	    printf "Writing Segment $segment_count to DB ( %d%%) %d Segments missing; estimating %4.2f min\r"
		,$percent,$segments_missing,$rest_estimate/60;
	}
	next 
	    if defined($segment->{way_element})
	    && $segment->{way_element} > 0;
	$new_segment_count++;
	my $tags = $segment->{tag};
	my $street_name=$tags->{name} || $tags->{ref} || "Segment=$seg_id";

	my $node_from = $segment->{from}||0;
	my $node_to   = $segment->{to}||0;
	#	print Dumper( $segment);
	unless ( $node_from && $node_to ) {
	    #print "Missing node '$node_from' or '$node_to' for Segment # $seg_id\n";
	    next;
	}
	my $lat1 = $OSM_Nodes{$node_from}->{lat};
	my $lon1 = $OSM_Nodes{$node_from}->{lon};
	#print Dumper( $osm_nodes->{$node_to});
	my $lat2 = $OSM_Nodes{$node_to}->{lat};
	my $lon2 = $OSM_Nodes{$node_to}->{lon};

	my $comment ='';
	$comment .= "seg_id=$seg_id ";

	my $segment4db = {
	    'streets.lat1' => $lat1, 'streets.lon1' => $lon1, 'streets.alt1' =>-99,
	    'streets.lat2' => $lat2, 'streets.lon2' => $lon2, 'streets.alt2' =>-99,
	    'streets.streets_type_id' => $streets_type_id,
	    'streets.scale_min' => 1,
	    'streets.scale_max' => 40000,
	    'streets.last_modified'  => time(),
	    'streets.source_id' => $source_id,
	    'streets.name'      => $street_name
	    };
	insert_hash("streets",$segment4db);
    }
    if ( $debug) {
	debug("Imported another $new_segment_count Segments not in ways for  ".
	      scalar( keys %OSM_Segments )." osm Segments".
	      sprintf(" in %.0f sec\n",time()-$start_time));
    }
}




# ******************************************************************
sub delete_existing_entries($){
    my $type = shift;
    my $source = "OpenStreetMap.org $type";

    unless ( $main::no_delete ) {
	print "Delete old 'OSM $type' Data\n";
	Geo::Gpsdrive::DBFuncs::delete_all_from_source($source);
	print "Deleted old '$source' Data\n" if $VERBOSE || $debug;
    }

}

# ******************************************************************
sub get_source_id($){
    my $type = shift;

    my $source = "osm.$type";

    my $source_id = Geo::Gpsdrive::DBFuncs::source_name2id($source);

    unless ( $source_id ) {
	my $source_hash = {
	    'source.url'     => "http://openstreetmap.org/",
	    'source.name'    => $source ,
	    'source.comment' => '' ,
	    'source.licence' => ""
	    };
	Geo::Gpsdrive::DBFuncs::insert_hash("source", $source_hash);
	$source_id = Geo::Gpsdrive::DBFuncs::source_name2id($source);
    }
    exit unless $source_id;
    return $source_id;
}


# *****************************************************************************

sub import_Data($@){
    my $areas_todo = shift;
    my @filenames = @_;

    #$areas_todo ||= 'germany';
    $areas_todo ||= 'world';
    $areas_todo=lc($areas_todo);

    print "\nImport OSM Data($areas_todo)\n";
	
    my $mirror_dir="$main::MIRROR_DIR/osm";
    
    -d $mirror_dir or mkpath $mirror_dir
	    or die "Cannot create Directory $mirror_dir:$!\n";

    my $icons_filename ||= "$ENV{HOME}/.josm/icons.xml";
    $icons_filename = "../data/map-icons/icons.xml" unless -s $icons_filename;
    $icons_filename = "data/map-icons/icons.xml" unless -s $icons_filename;
    load_icons( $icons_filename );
    my $elemstyles_filename = "$ENV{HOME}/.josm/plugins/mappaint/elemstyles.xml";
    $elemstyles_filename ="$ENV{HOME}/svn.openstreetmap.org/data/elemstyles.xml" unless -s  $elemstyles_filename;
    $elemstyles_filename ="../data/elemstyles.xml" unless -s  $elemstyles_filename;
    load_elemstyles($elemstyles_filename);

    disable_keys('streets');
    disable_keys('poi');
    $SOURCE_ID_NODE   = get_source_id("node");
    $SOURCE_ID_SEGMENT = get_source_id("segment");
    die "SOURCE_ID_SEGMENT is not set\n" unless $SOURCE_ID_SEGMENT;
    
    delete_existing_entries("node");
    delete_existing_entries("segment");
    delete_existing_entries("way");
    
    for my $filename ( @filenames ) {
	print "Import OSM Data '$filename'\n";
	
	if ( $filename =~ m/^download:(.+)/ ) {
	    my $area = $1;
	    download_osm_streets($mirror_dir,$area);
	    read_osm_dir($mirror_dir,$areas_todo);
	} elsif ( -s $filename ) {
	    read_osm_file( $filename,$areas_todo);
	} elsif ( $filename eq '' ) {
	    print "Download planet-xxxxxx.osm\n";
	    $filename = mirror_planet();
	    print "Mirror $filename complete\n";
	    read_osm_file($filename,$areas_todo);
	} else {
	    die "OSM::import_Data: Cannot find File '$filename'\n";
	    print "Read OSM Data\n";
	};
    }	
    
    enable_keys('poi');
    enable_keys('streets');


    print "\nDownload and import of OSM Data FINISHED\n";
}

1;
