#!/usr/bin/perl

use strict;
use warnings;

use XML::Parser;
use Getopt::Long;
use Storable ();
use IO::File;
use Pod::Usage;
use Data::Dumper;

sub data_open($);
	      sub combine_way_into_segments();
	      sub output_osm();
	      sub output_named_points();
	      sub output_statistic();

	      our $debug=0;
	      our $verbose=0;
	      our $man=0;
	      our $help=0;
	      my $areas_todo;
Getopt::Long::Configure('no_ignore_case');
	      GetOptions ( 
			   'verbose+'            => \$verbose,
			   'debug'               => \$debug,      
			   'd'                   => \$debug,      
			   'MAN'                 => \$man, 
			   'man'                 => \$man, 
			   'h|help|x'            => \$help, 
			   'area=s'              => \$areas_todo,
			   )
	      or pod2usage(1);

	      pod2usage(1) if $help;
	      pod2usage(-verbose=>2) if $man;


	      my $Filename = shift();
	      pod2usage(1) unless $Filename;

	      our $READ_FH=undef;

# ------------------------------------------------------------------
	      my $area_definitions = {
		  #                     min    |    max  
		  #                  lat   lon |  lat lon
		  uk         => [ [  49  , -11,   64,   3],
				  [ 49.9 ,  -5.8, 54,0.80],
				  ],
		  iom        => [ [  49  , -11,   64,   3] ],
		  germany    => [ [  47  ,   5,   54,  16] ],
		  spain      => [ [  35.5,  -9,   44,   4] ],
		  europe     => [ [  35  , -12,   75,  35] ],
		  africa     => [ [ -45  , -20,   30,  55] ],
		  world_east => [ [ -90  , -30,   90, 180] ],
		  world_west => [ [ -90  ,-180,   90, -30] ],
	      };
my $SELECTED_AREA_filters=undef;

#$areas_todo=join(',',sort keys %{$area_definitions}) unless defined $areas_todo;
$areas_todo ||= 'germany';
$areas_todo=lc($areas_todo);
my $SELECTED_AREA=$areas_todo;
our $SELECTED_AREA_filters = $area_definitions->{$SELECTED_AREA};

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
print STDERR "Reading and Parsing XML from $Filename\n" if $debug;
my $start_time=time();
$READ_FH = data_open($Filename);
if ( $verbose || $debug )  {
    print STDERR "\n";
    print STDERR "Parsing $Filename for area $SELECTED_AREA\n";
}
my $P = new XML::Parser( Handlers => {
    Start => \&DoStart, 
    End => \&DoEnd, 
    Char => \&DoChar});
eval {
    $P->parse($READ_FH);
    $READ_FH->close();
};
if ( $verbose || $debug )  {
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
printf("Parsing Osm-Data in %.0f sec\n",time()-$start_time )
    if $debug;


printf STDERR "Creating output files\n";

combine_way_into_segments();
output_osm();
output_named_points();
output_statistic();
printf STDERR "Done\n";
exit;

# -----------------------------------------------------------------------------
# Open Planet.osm Data File
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
	print STDERR "Opening $file_name" if $debug || $verbose;
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

#----------------------------------------------
# Combine way data into segments
#----------------------------------------------
sub combine_way_into_segments() {
    print STDERR "Combine way data into segments\n" if $debug;
    if(open(WAYS,">Data/ways-$SELECTED_AREA.csv")){
	foreach my $id ( keys %Ways){
	    my $Way = $Ways{$id};
	    next unless defined $Way;
	    my $segments=$Way->{"segments"};
	    my @SubSegments = split(/,/, $segments);
	    unless ( scalar(@SubSegments) ) {
		$Stats{"empty ways"}++; 
		if ( $debug ) {
		    printf WAYS "No Segments for Way: Name:%s\n",($Way->{"name"}||'');
		}
		next;
	    }
	    if ( $debug ) {
		printf WAYS "Way: %s,%s\n", $Way->{"segments"}, ($Way->{"name"}||'');
	    }
	    $Stats{"untagged ways"}++ 
		unless scalar( keys (%$Way)); 
	    
	    if ( $debug) {
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
}

#----------------------------------------------
# Main output (segments)
#----------------------------------------------
sub output_osm(){
    print STDERR "Writing Segments to Data/osm-$SELECTED_AREA.csv\n" if $debug;
    if(open(OSM, ">Data/osm-$SELECTED_AREA.csv")){
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
}

#----------------------------------------------
# Secondary output (named points)
#----------------------------------------------
sub output_named_points(){
    print STDERR "Writing Points to Data/points-$SELECTED_AREA.csv\n" if $debug;
    if(open(POINTS, ">Data/points-$SELECTED_AREA.csv")){
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
}

#----------------------------------------------
# Statistics output
#----------------------------------------------
sub output_statistic(){
    print STDERR "Statistics output\n" if $debug;
    if(open(STATS, ">Data/stats-$SELECTED_AREA.txt")){
	foreach(sort {$AllTags{$b} <=> $AllTags{$a}} keys(%AllTags)){
	    printf STATS "* %d %s\n", $AllTags{$_}, $_;
	}
	printf STATS "\n\nStats:\n";
	foreach(keys(%Stats)){
	    printf STATS "* %d %s\n", $Stats{$_}, $_;
	}
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
    if ( ( $verbose || $debug ) && ! ( $Stats{"tags read"} % 10000 ))  {
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

B<planet_osm2txt.pl> Version 0.01

=head1 DESCRIPTION

B<planet_osm2txt.pl> is a program to convert osm-data from xml format to 
a plain text file in cvs form

=head1 SYNOPSIS

B<Common usages:>

planet_osm2txt.pl [-d] [-v] [-h]  <planet_filename.osm>

=head1 OPTIONS

=over 2

=item B<--man> Complete documentation

Complete documentation

=item B<planet_filename.osm>

the file to read from

=back
