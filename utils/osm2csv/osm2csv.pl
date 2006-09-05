#!/usr/bin/perl

BEGIN {
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

use Geo::OSM::Planet;
use Utils::Debug;
use Utils::LWP::Utils;
use Utils::File;

sub combine_way_into_segments($); # {}
sub output_osm($); # {}
sub output_named_points($); # {}
sub output_statistic($); # {}

our $man=0;
our $help=0;
my $areas_todo;
my $do_list_areas=0;
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

# ------------------------------------------------------------------
my $area_definitions = {
    #                     min    |    max  
    #                  lat   lon |  lat lon
    uk         => [ [  49  , -11,   64,   3  ],
		    [ 49.9 ,  -5.8, 54,   0.80 ],
		    ],
    iom        => [ [  49  , -11,   64,   3  ] ],
    germany    => [ [  47  ,   5,   54,  16  ] ],
    spain      => [ [  35.5,  -9,   44,   4  ] ],
    europe     => [ [  35  , -12,   75,  35  ],
		    [  62.2,-24.4,66.8,-12.2], # Iceland
		    ],
    africa     => [ [ -45  , -20,   30,  55  ] ],
    # Those eat up all memory on normal machines
    # world_east => [ [ -90  , -30,   90, 180  ] ], 
    # world_west => [ [ -90  ,-180,   90, -30  ] ],
};
my $stripe_lon=-180;
my $stripe_step=10;
while ( $stripe_lon < 180 ){
    my $stripe_lon1=$stripe_lon+$stripe_step+1;
    $area_definitions->{"stripe_${stripe_lon}_${stripe_lon1}"} = [ [ -90  ,$stripe_lon,   90, $stripe_lon1] ];
    $stripe_lon=$stripe_lon+$stripe_step;
}
my $SELECTED_AREA_filters=undef;

#$areas_todo=join(',',sort keys %{$area_definitions}) unless defined $areas_todo;
$areas_todo ||= 'germany';
$areas_todo=lc($areas_todo);
my $SELECTED_AREA=$areas_todo;
our $SELECTED_AREA_filters = $area_definitions->{$SELECTED_AREA};

if ( $do_list_areas ) {
    print join("\n",sort keys %{$area_definitions})."\n";
    exit;
}
if ( ! defined ($area_definitions->{$SELECTED_AREA} ) ) {
    die "unknown area $SELECTED_AREA.\n".
	"Allowed Areas:\n\t".join("\n\t",sort keys %{$area_definitions})."\n";
}

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

# -----------------------------------------------------------------------------
# Temporary data
my (%MainAttr,$Type,%Tags, @WaySegments);
# Stats
my %AllTags;
# Stored data
my (%Nodes, %Segments, %Ways, %Stats);

# Estimated Number of elements to show readin progress in percent
# Currently taken from planet-060818
$Stats{"nodes estim"}    = 13436254;
$Stats{"segments estim"} = 9566333;
$Stats{"ways estim"}     = 2548673;
$Stats{"tags estim"}     = 46330000;


#----------------------------------------------
# Processing stage
#----------------------------------------------

#----------------------------------------------
# Parsing planet.osm File
#----------------------------------------------
print STDERR "Reading and Parsing XML from $Filename\n" if $DEBUG;
our $PARSING_START_TIME=time();
$READ_FH = data_open($Filename);
if ( $VERBOSE || $DEBUG )  {
    print STDERR "\n";
    print STDERR "osm2csv: Parsing $Filename for area $SELECTED_AREA\n";
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
printf("osm2csv: Parsing Osm-Data in %.0f sec\n",time()-$PARSING_START_TIME )
    if $DEBUG || $VERBOSE;


printf STDERR "Creating output files\n";

my $data_dir=osm_dir()."/planet/csv";
mkdir_if_needed( $data_dir );
combine_way_into_segments("$data_dir/ways-$SELECTED_AREA.csv");
output_osm("$data_dir/osm-$SELECTED_AREA.csv");
output_named_points("$data_dir/points-$SELECTED_AREA.csv");
output_statistic("$data_dir/stats-$SELECTED_AREA.txt");
printf STDERR "Done\n";
exit;

#----------------------------------------------
# Combine way data into segments
#----------------------------------------------
sub combine_way_into_segments($) {
    my $filename = shift;
    print STDERR "Combine way data into segments --> $filename\n" if $DEBUG;
    if(! open(WAYS,">$filename")) {
	warn "combine_way_into_segments: Cannot write to $filename\n";
	return;
    }
    foreach my $id ( keys %Ways){
	my $Way = $Ways{$id};
	next unless defined $Way;
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

}

#----------------------------------------------
# Main output (segments)
#----------------------------------------------
sub output_osm($){
    my $filename = shift;
    print STDERR "Writing Segments to $filename\n" if $DEBUG;
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
	printf OSM "%f,%f,%f,%f,%s,%s,%s\n",
	$Nodes{$From}{"lat"},
	$Nodes{$From}{"lon"},
	$Nodes{$To}{"lat"},
	$Nodes{$To}{"lon"},
	($Segment->{"class"}||''),
	($Segment->{"name"}||''),
	($Segment->{"highway"}||'');
    }
    close OSM;
}

#----------------------------------------------
# Secondary output (named points)
#----------------------------------------------
sub output_named_points($){
    my $filename = shift;
    print STDERR "Writing Points to $filename\n" if $DEBUG;
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
	
	if ( in_area($osm_obj) ) {
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
    if ( ( $VERBOSE || $DEBUG ) && ! ( $Stats{"tags read"} % 10000 ))  {
	print STDERR "\r";
	print STDERR "Read($SELECTED_AREA): ";
	for my $k ( sort keys %Stats ) {
	    next if $k =~ m/( estim| read)$/;
	    print STDERR " $k:".$Stats{$k};
	    if ( defined($Stats{"$k read"}) ) {
		printf STDERR " %.0f%%",(100*$Stats{"$k read"}/$Stats{"$k estim"}) 
		    if defined($Stats{"$k estim"});
		print STDERR "(".$Stats{"$k read"}.") ";
	    }
	    print STDERR " ";
	}
	my $proc_file = "/proc/$$/statm";
	if ( -r $proc_file ) {
	    my $statm = `cat $proc_file`;
	    chomp $statm;
	    my @statm = split(/\s+/,$statm);
	    my $vsz = ($statm[0]*4)/1024;
	    my $rss = ($statm[1]*4)/1024;
	    #      printf STDERR " PID: $$ ";
	    printf STDERR "VSZ: %.0f MB ",$vsz;
	    printf STDERR "RSS: %.0f MB",$rss;
	}
	my $time_diff=time()-$PARSING_START_TIME;
	my $time_estimated= $time_diff*$Stats{"tags estim"}/$Stats{"tags read"};
	printf STDERR " time %.0f min rest: %.0f min",$time_diff/60,$time_estimated/60;
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
