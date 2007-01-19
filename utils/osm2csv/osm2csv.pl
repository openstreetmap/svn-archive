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
use File::Slurp;

sub combine_way_into_segments($); # {}
sub output_osm($); # {}
sub output_statistic($); # {}
sub parse_planet($$); # {}

our $man=0;
our $help=0;
my $areas_todo;
my $do_list_areas=0;
my $do_update_only=0;
my $Filename;

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
	     'osm=s'               => \$Filename,
	     'area=s'              => \$areas_todo,
	     'list-areas'          => \$do_list_areas,
	     'update-only'         => \$do_update_only,
	     )
    or pod2usage(1);

pod2usage(1) if $help;
pod2usage(-verbose=>2) if $man;


# TODO:
# if the input filename is not planet*osm* we have to change the output filename too.
$Filename ||= shift();
unless ( $Filename && -s $Filename ) {
    $Filename = mirror_planet();
};
if ( ! -s $Filename ) {
    die "Cannot read $Filename\n";
}

pod2usage(1) unless $Filename;

our $READ_FH=undef;
our $OK_POS=0;

#$areas_todo ||= 'germany';
$areas_todo ||= 'world';
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

my $fn_named_points;
my $fh_named_points;


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
    $fn_named_points="$data_dir/points-$area_name.csv";
    #unlink $fn_named_points if -s  $fn_named_points;
    $fh_named_points = IO::File->new(">$fn_named_points");
    $fh_named_points->binmode(':utf8');

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
    $Stats{"tags estim"}     = 53518120;
    $Stats{"nodes"}     = 0;
    $Stats{"segments"}     = 0;
    $Stats{"ways"}     = 0;


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

    # Print out not parsed lines
    my $count=20;
    $READ_FH->setpos($OK_POS);
    while ( ($count--) && (my $line = $READ_FH->getline() )) {
	print "REST: $line";
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
	    #next if $k =~ m/^(class|name|highway)$/;
	    next if $k =~ m/^(from|to|segments)$/;
	    my $v = $Segment->{$k};
	    printf OSM ",%s=%s",$k,$v
	}
	printf OSM "\n",
    }
    close OSM;
    $Stats{"time output"} = time()-$PARSING_START_TIME;
}

#----------------------------------------------
# output (named point)
#----------------------------------------------
sub write_named_point($$){
    my $fh_named_points = shift;
    my $Node = shift;
    return unless defined $Node;
    $Stats{"Nodes with zero lat/long"}++ 
	if($Node->{"lat"} == 0 and $Node->{"lon"} == 0);
    
    my $result = '';
    if($Node->{"name"} || 
       $Node->{"amenity"} || 
       $Node->{"class"} || 
       $Node->{"abutters"}){
	$result = sprintf "%f,%f,%s,%s,%s",
	$Node->{"lat"},
	$Node->{"lon"},
	($Node->{"name"}||''),
	($Node->{"amenity"}||''),
	($Node->{"class"}||'');
	foreach my $k ( keys %{$Node} ) {
	    #next if $k =~ m/^(class|name|highway)$/;
	    next if $k =~ m/^(lat|lon)$/;
	    my $v = $Node->{$k};
	    $result .= sprintf ",%s=%s",$k,$v;
	}	    
	print $fh_named_points "$result\n";
    }
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
    my $ID = $MainAttr{"id"};
    if($Element eq "node"){
	my $node={};
	$node->{"lat"} = $MainAttr{"lat"};
	$node->{"lon"} = $MainAttr{"lon"};
	
	if ( $AREA_FILTER->inside($node) ) {
	    $Nodes{$ID}{"lat"} = $MainAttr{"lat"};
	    $Nodes{$ID}{"lon"} = $MainAttr{"lon"};
	    foreach(keys(%Tags)){
		next if /created_by/;
		next if /time/;
		$node->{$_} = $Tags{$_};
	    }
	    write_named_point($fh_named_points,$node);
	    $Stats{"nodes named"}++ if($node->{"name"});
	    $Stats{"nodes tagged "}++ if($MainAttr{"tags"});
	    $Stats{"nodes"}++;
	}
	$Stats{"nodes read"}++;
    }

    if($Element eq "segment"){
	my $from = $MainAttr{"from"};
	my $to   = $MainAttr{"to"};
	if ( defined($Nodes{$from}) && defined($Nodes{$to}) ) {
	    $Segments{$ID}{"from"} = $from;
	    $Segments{$ID}{"to"} = $to;
	    foreach(keys(%Tags)){
		next if /created_by/;
		next if /time/;
		$Segments{$ID}{$_} = $Tags{$_};
	    }
	    $Stats{"segments tagged"}++ if($MainAttr{"tags"});
	    $Stats{"segments"}++;
	}
	$Stats{"segments read"}++;
    }

    if($Element eq "way"){
	if ( @WaySegments ) {
	    $Ways{$ID}{"segments"} = join(",",@WaySegments);
	    foreach(keys(%Tags)){
		next if /created_by/;
		next if /time/;
		$Ways{$ID}{$_} = $Tags{$_};
	    }    
	    $Stats{"ways"}++;
	}
	$Stats{"ways read"}++;
    }

    $Stats{"tags read"}++;
#    $OK_POS=$READ_FH->getpos();
    if ( ( $VERBOSE || $DEBUG ) &&
#	 ! ( $Stats{"tags read"} % 10000 ) &&
	 ( time()-$PARSING_DISPLAY_TIME >0.9)

	 )  {
	$PARSING_DISPLAY_TIME= time();
	print STDERR "\r";
	print STDERR "Read(".$AREA_FILTER->name()."): ";
	my $last_k='';
	for my $k ( sort ( qw(tags nodes segments ways ) ,keys %Stats )) {
	    next if $last_k eq $k; $last_k=$k;
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

	my $rss = sprintf("%.0f",mem_usage('rss'));
	$Stats{"max rss"} = max($Stats{"max rss"},$rss) if $rss;
	my $vsz = sprintf("%.0f",mem_usage('vsz'));
	$Stats{"max vsz"} = max($Stats{"max vsz"},$vsz) if $vsz;

	print STDERR mem_usage();
	print STDERR time_estimate($PARSING_START_TIME,$Stats{"tags read"},$Stats{"tags estim"});
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
This format then is normally used by osmpdfatlas.pl

=head1 SYNOPSIS

B<Common usages:>

osm2csv.pl [-d] [-v] [-h] [--no-mirror] [--proxy=<proxy:port>] [--list-areas] <planet_filename.osm>

=head1 OPTIONS

=over 2

=item B<--man> Complete documentation

Complete documentation

=item B<--proxy=<proxy:port>>

Use proxy Server to get the newest planet.osm File

=item B<--no-mirror>

do not try to get the newest planet.osm first

=item B<--osm=filename>

Source File in OSM Format

=item B<--area=germany> Area Filter

Only read area for processing

=item B<--list-areas>

print all areas possible

=item B<planet_filename.osm>

the file to read from

=back

=head1 COPYRIGHT

Copyright 2006, OJW

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

OJW <streetmap@blibbleblobble.co.uk>
Jörg Ostertag (osm2csv-for-openstreetmap@ostertag.name)

=head1 SEE ALSO

http://www.openstreetmap.org/

=cut
