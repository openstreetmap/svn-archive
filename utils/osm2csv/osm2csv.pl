#!/usr/bin/perl

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

use XML::Parser;
use Getopt::Long;
use Storable ();
use IO::File;
use Pod::Usage;
use Data::Dumper;

use Geo::Filter::Area;
use Geo::OSM::Planet;
use Utils::Debug;
use Utils::File;
use Utils::LWP::Utils;
use Utils::Math;

sub combine_way_into_segments($); # {}
sub output_osm($); # {}
sub output_named_points($); # {}
sub output_statistic($); # {}
sub parse_planet($$); # {}

our $man=0;
our $help=0;
my $areas_todo;
my $do_list_areas=0;
my $do_update_only=0;

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

	     'area=s'              => \$areas_todo,
	     'list-areas'          => \$do_list_areas,
	     'update-only'         => \$do_update_only,
	     )
    or pod2usage(1);

pod2usage(1) if $help;
pod2usage(-verbose=>2) if $man;


my $Filename = shift();
unless ( $Filename && -s $Filename ) {
    $Filename = mirror_planet();
};
if ( ! -s $Filename ) {
    die "Cannot read $Filename\n";
}

pod2usage(1) unless $Filename;

our $READ_FH=undef;


$areas_todo ||= 'germany';
$areas_todo=lc($areas_todo);
if ( $do_list_areas ) {
    print Geo::Filter::Area->list_areas()."\n";
    exit;
}

our (%MainAttr,$Type,%Tags, @WaySegments);
# Stats
our %AllTags;
# Stored data
our (%Nodes, %Segments, %Ways, %Stats);
our $AREA_FILTER;
our $PARSING_START_TIME=0;
our $PARSING_DISPLAY_TIME=0;


my $data_dir=planet_dir()."/csv";
mkdir_if_needed( $data_dir );

for my $area_name ( split(",",$areas_todo) ) {
    if ( $do_update_only ) {
	my $needs_update=0;
	$needs_update ||= file_needs_re_generation($Filename,"$data_dir/ways-$area_name.csv");
	$needs_update ||= file_needs_re_generation($Filename,"$data_dir/osm-$area_name.csv");
	$needs_update ||= file_needs_re_generation($Filename,"$data_dir/points-$area_name.csv");
	$needs_update ||= file_needs_re_generation($Filename,"$data_dir/stats-$area_name.txt");
	next unless $needs_update;
	print STDERR "Update needed. One of the files is old or non existent\n" if $VERBOSE;
    }
    # -----------------------------------------------------------------------------
    # Temporary data

    (%MainAttr,%Tags)=((),());
    $Type='';
    @WaySegments = ();
    (%AllTags,%Nodes, %Segments, %Ways, %Stats)=((),(),(),(),());

    # Currently active Area Filter
    $PARSING_START_TIME=0;
    # Estimated Number of elements to show readin progress in percent
    # Currently taken from planet-060818
    $Stats{"nodes estim"}    = 14135968;
    $Stats{"segments estim"} = 10697464;
    $Stats{"ways estim"}     = 2758781;
    $Stats{"tags estim"}     = 51450000;


    #----------------------------------------------
    # Processing stage
    #----------------------------------------------

    print STDERR "creating $data_dir/osm-$area_name.csv\n" if $VERBOSE;

    parse_planet($Filename,$area_name);

    printf STDERR "Creating output files\n";
    die "No Area Name defined\n"
	unless $area_name;
    combine_way_into_segments("$data_dir/ways-$area_name.csv");
    output_osm("$data_dir/osm-$area_name.csv");
    output_named_points("$data_dir/points-$area_name.csv");
    output_statistic("$data_dir/stats-$area_name.txt");
    printf STDERR "$area_name Done\n";
}
exit;

#----------------------------------------------
# Parsing planet.osm File
#----------------------------------------------
sub parse_planet($$){
    my $Filename = shift;
    my $area_name = shift;

    print STDERR "Reading and Parsing XML from $Filename for $area_name\n" if $DEBUG;

    $AREA_FILTER = Geo::Filter::Area->new( area => $area_name );

    $PARSING_START_TIME=time();
    $READ_FH = data_open($Filename);
    if ( $VERBOSE || $DEBUG )  {
	print STDERR "\n";
	print STDERR "osm2csv: Parsing $Filename for area ".$AREA_FILTER->name()."\n";
    }
    my $P = new XML::Parser( Handlers => {
	Start => \&DoStart, 
	End => \&DoEnd, 
	Char => \&DoChar});
    eval {
	$P->parse($READ_FH);
	$READ_FH->close();
    };
    if ( $VERBOSE || $DEBUG )  {
	print STDERR "\n";
    }

    if ($@) {
	print STDERR "WARNING: Could not parse osm data $Filename\n";
	print STDERR "ERROR: $@\n";
	return;
    }
    if (not $P) {
	print STDERR "WARNING: Could not parse osm data $Filename\n";
	return;
    }
    $Stats{"time parsing"} = time()-$PARSING_START_TIME;
    printf("osm2csv: Parsing Osm-Data in %.0f sec\n",time()-$PARSING_START_TIME )
	if $DEBUG || $VERBOSE;

}

#----------------------------------------------
# Combine way data into segments
#----------------------------------------------
sub combine_way_into_segments($) {
    my $filename = shift;
    $PARSING_START_TIME=time();

    print STDERR "Combine way data into segments --> $filename\n" if $DEBUG ||$VERBOSE;
    if(! open(WAYS,">$filename")) {
	warn "combine_way_into_segments: Cannot write to $filename\n";
	return;
    }
    my $way_count=0;
    foreach my $id ( keys %Ways){
	$way_count++;
	my $Way = $Ways{$id};
	next unless defined $Way;
	if ( ( $VERBOSE || $DEBUG ) &&
	     ( time()-$PARSING_DISPLAY_TIME >0.9)
	     )  {
	    $PARSING_DISPLAY_TIME= time();
	    print STDERR "Combine way ".mem_usage();
	    print STDERR time_estimate($PARSING_START_TIME,$way_count,$Stats{"ways"});
	    print STDERR "\r";
	}
	my $segments=$Way->{"segments"};
	my @SubSegments = split(/,/, $segments);
	unless ( scalar(@SubSegments) ) {
	    $Stats{"empty ways"}++; 
	    if ( $DEBUG ) {
		printf WAYS "No Segments for Way: Name:%s\n",($Way->{"name"}||'');
	    }
	    next;
	}
	if ( $DEBUG ) {
	    printf WAYS "Way: %s,%s\n", $Way->{"segments"}, ($Way->{"name"}||'');
	}
	$Stats{"untagged ways"}++ 
	    unless scalar( keys (%$Way)); 
	
	if ( $DEBUG) {
	    printf WAYS "Copying keys: %s to segments %s\n",
	    join(",",keys(%$Way)),
	    join(",",@SubSegments);
	}
	
	# Each segment in a way inherits the way's attributes
	foreach my $Segment(@SubSegments){
	    foreach my $Key(keys(%$Way)){
		$Segments{$Segment}{$Key} = $Way->{$Key}
	    }
	}
    }
    close WAYS;
    if ( ( $VERBOSE || $DEBUG ) ) {
	print STDERR "\n";
    }
    $Stats{"time combining"} = time()-$PARSING_START_TIME;
}

#----------------------------------------------
# Main output (segments)
#----------------------------------------------
sub output_osm($){
    my $filename = shift;

    $PARSING_START_TIME=time();

    print STDERR "Writing Segments to $filename\n" if $DEBUG || $VERBOSE;
    if(! open(OSM,">$filename")) {
	warn "output_osm: Cannot write to $filename\n";
	return;
    }
    foreach my $id (keys %Segments){
	my $Segment = $Segments{$id};
	next unless defined $Segment;
	my $From = $Segment->{"from"};
	my $To = $Segment->{"to"};
	unless ( $From && $To ) {
	    $Stats{"segments without endpoints"}++;
	    next;
	}
	unless ( defined($Nodes{$From}) && defined($Nodes{$To}) ) {
	    $Stats{"segments without endpoint nodes defined"}++;
	    next;
	}
	printf OSM "%f,%f,%f,%f,%s,%s,%s",
	$Nodes{$From}{"lat"},
	$Nodes{$From}{"lon"},
	$Nodes{$To}{"lat"},
	$Nodes{$To}{"lon"},
	($Segment->{"class"}||''),
	($Segment->{"name"}||''),
	($Segment->{"highway"}||'');
	foreach my $k ( keys %{$Segment} ){
	    next if $k =~ m/^(class|name|highway)$/;
	    my $v = $Segment->{$k};
	    printf OSM ",%s=%s",$k,$v
	}
	printf OSM "\n",
    }
    close OSM;
    $Stats{"time output"} = time()-$PARSING_START_TIME;
}

#----------------------------------------------
# Secondary output (named points)
#----------------------------------------------
sub output_named_points($){
    my $filename = shift;
    print STDERR "Writing Points to $filename\n" if $DEBUG ||$VERBOSE;
    if(! open(POINTS,">$filename")) {
	warn "output_osm: Cannot write to $filename\n";
	return;
    }
    foreach my $id ( keys %Nodes ){
	my $Node = $Nodes{$id};
	next unless defined $Node;
	$Stats{"Nodes with zero lat/long"}++ 
	    if($Node->{"lat"} == 0 and $Node->{"lon"} == 0);
	
	if($Node->{"name"} || $Node->{"amenity"} || $Node->{"class"}){
	    printf POINTS "%f,%f,%s,%s,%s\n",
	    $Node->{"lat"},
	    $Node->{"lon"},
	    ($Node->{"name"}||''),
	    ($Node->{"amenity"}||''),
	    ($Node->{"class"}||'');
	}
    }
    close POINTS;
    
}

#----------------------------------------------
# Statistics output
#----------------------------------------------
sub output_statistic($){
    my $filename = shift;
    print STDERR "Statistics output $filename\n" if $DEBUG;
    if(! open(STATS,">$filename")) {
	warn "output_osm: Cannot write to $filename\n";
	return;
    }
    foreach(sort {$AllTags{$b} <=> $AllTags{$a}} keys(%AllTags)){
	printf STATS "* %d %s\n", $AllTags{$_}, $_;
    }
    printf STATS "\n\nStats:\n";
    foreach(keys(%Stats)){
	printf STATS "* %d %s\n", $Stats{$_}, $_;
    }
}

# Function is called whenever an XML tag is started
#----------------------------------------------
sub DoStart()
{
    my ($Expat, $Name, %Attr) = @_;
    
    if($Name eq "node"){
	undef %Tags;
	%MainAttr = %Attr;
	$Type = "n";
    }
    if($Name eq "segment"){
	undef %Tags;
	%MainAttr = %Attr;
	$Type = "s";
    }
    if($Name eq "way"){
	undef %Tags;
	undef @WaySegments;
	%MainAttr = %Attr;
	$Type = "w";
    }
    if($Name eq "tag"){
	# TODO: protect against id,from,to,lat,long,etc. being used as tags
	$Tags{$Attr{"k"}} = $Attr{"v"};
	$AllTags{$Attr{"k"}}++;
	$Stats{"tags"}++;
    }
    if($Name eq "seg"){
	my $id = $Attr{"id"};
	if ( defined ( $Segments{$id} ) ) {
	    push(@WaySegments, $id);
	}
    }
}

# Function is called whenever an XML tag is ended
#----------------------------------------------
sub DoEnd(){
    my ($Expat, $Element) = @_;
    if($Element eq "node"){
	my $ID = $MainAttr{"id"};
	my $osm_obj={};
	$osm_obj->{"lat"} = $MainAttr{"lat"};
	$osm_obj->{"lon"} = $MainAttr{"lon"};
	
	if ( $AREA_FILTER->inside($osm_obj) ) {
	    $Nodes{$ID}{"lat"} = $MainAttr{"lat"};
	    $Nodes{$ID}{"lon"} = $MainAttr{"lon"};
	    foreach(keys(%Tags)){
		$Nodes{$ID}{$_} = $Tags{$_};
	    }
	    $Stats{"nodes named"}++ if($Nodes{$ID}{"name"});
	    $Stats{"nodes tagged "}++ if($MainAttr{"tags"});
	    $Stats{"nodes"}++;
	}
	$Stats{"nodes read"}++;
	#print "Node:".join(",",keys(%Tags))."\n" if(scalar(keys(%Tags))>0);
    }

    if($Element eq "segment"){
	my $ID = $MainAttr{"id"};

	my $from = $MainAttr{"from"};
	my $to   = $MainAttr{"to"};
	if ( defined($Nodes{$from}) && defined($Nodes{$to}) ) {
	    $Segments{$ID}{"from"} = $from;
	    $Segments{$ID}{"to"} = $to;
	    foreach(keys(%Tags)){
		$Segments{$ID}{$_} = $Tags{$_};
	    }
	    $Stats{"segments tagged"}++ if($MainAttr{"tags"});
	    $Stats{"segments"}++;
	}
	$Stats{"segments read"}++;
    }

    if($Element eq "way"){
	my $ID = $MainAttr{"id"};
	if ( @WaySegments ) {
	    $Ways{$ID}{"segments"} = join(",",@WaySegments);
	    foreach(keys(%Tags)){
		$Ways{$ID}{$_} = $Tags{$_};
	    }    
	    $Stats{"ways"}++;
	}
	$Stats{"ways read"}++;
    }

    $Stats{"tags read"}++;
    if ( ( $VERBOSE || $DEBUG ) &&
#	 ! ( $Stats{"tags read"} % 10000 ) &&
	 ( time()-$PARSING_DISPLAY_TIME >0.9)

	 )  {
	$PARSING_DISPLAY_TIME= time();
	print STDERR "\r";
	print STDERR "Read(".$AREA_FILTER->name()."): ";
	for my $k ( sort keys %Stats ) {
	    next if $k =~ m/( estim| read)$/;
	    next if $k =~ m/named/ && $VERBOSE <=1;
	    next if $k !~ m/tags/ && $VERBOSE <=2;
	    print STDERR " $k:".$Stats{$k};
	    if ( defined($Stats{"$k read"}) ) {
		printf STDERR " %.0f%%",(100*$Stats{"$k read"}/$Stats{"$k estim"}) 
		    if defined($Stats{"$k estim"});
		print STDERR "(".$Stats{"$k read"}.") ";
	    }
	    print STDERR " ";
	}

	$Stats{"max rss"} = max($Stats{"max rss"},mem_usage('rss'));
	$Stats{"max vsz"} = max($Stats{"max vsz"},mem_usage('vsz'));

	print STDERR mem_usage();
	print STDERR time_estimate($PARSING_START_TIME,$Stats{"tags estim"},$Stats{"tags read"});
	print STDERR "\r";
    }
}

# Function is called whenever text is encountered in the XML file
#----------------------------------------------
sub DoChar(){
    my ($Expat, $String) = @_;
}

##################################################################
# Usage/manual

__END__

=head1 NAME

B<osm2csv.pl> Version 0.02

=head1 DESCRIPTION

B<osm2csv.pl> is a program to convert osm-data from xml format to 
a plain text file in cvs form.

=head1 SYNOPSIS

B<Common usages:>

planet_osm2txt.pl [-d] [-v] [-h] [--no-mirror] [--proxy=<proxy:port>] [--list-areas] <planet_filename.osm>

=head1 OPTIONS

=over 2

=item B<--man> Complete documentation

Complete documentation

=item B<--proxy=<proxy:port>>

Use proxy Server to get the newest planet.osm File

=item B<--no-mirror>

do not try to get the newest planet.osm first

=item B<--area=germany> Area Filter

Only read area for processing

=item B<--list-areas>

print all areas possible

=item B<planet_filename.osm>

the file to read from

=back
