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

sub output_osm($); # {}
sub output_statistic($); # {}
sub parse_planet($$); # {}

our $man=0;
our $help=0;
my $areas_todo;
my $do_list_areas=0;
my $do_update_only=0;
my $tie_nodes_hash=undef;
my $Filename;

my @needed_segment_tags=qw( name amenity class highway);

Getopt::Long::Configure('no_ignore_case');
GetOptions ( 
	     'debug+'              => \$DEBUG,      
	     'd+'                  => \$DEBUG,      
	     'verbose+'            => \$VERBOSE,
	     'MAN'                 => \$man, 
	     'man'                 => \$man, 
	     'h|help|x'            => \$help, 

	     'tie-nodes-hash'      => \$tie_nodes_hash,
	     'no-mirror'           => \$Utils::LWP::Utils::NO_MIRROR,
	     'proxy=s'             => \$Utils::LWP::Utils::PROXY,
	     'osm=s'               => \$Filename,
	     'area=s'              => \$areas_todo,
	     'list-areas'          => \$do_list_areas,
	     'update-only'         => \$do_update_only,
	     )
    or pod2usage(1);

$areas_todo ||= 'germany';
$areas_todo=lc($areas_todo);

# See if we'll have to tie the Nodes Hash to a File
# This is at least 10 times slower, but we have less problems with
# running out of memory
if ( ! defined $tie_nodes_hash ) {
    my $max_ram=mem_info("MemTotal");
    $max_ram =~ s/MB//;
    my $estimated_memory = {
	africa     => 2500,
	france     =>  192,
	europe     => 3000,
	germany    =>  500,
	uk         =>  660,
	world      => 4000,
	world_east => 4000,
	world_west => 4000,
    };
    for my $area ( split(",",$areas_todo )){
	$tie_nodes_hash=1
	    if $estimated_memory->{$area} > $max_ram;
     }
}

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

if ( $do_list_areas ) {
    print Geo::Filter::Area->list_areas()."\n";
    exit;
}

our (%MainAttr,$Type,%Tags, @WaySegments);
# Stats
our %AllTags;
# Stored data
our (%Nodes, %Segments, %Stats);
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
    $fh_named_points = IO::File->new(">$fn_named_points.part");
    $fh_named_points->binmode(':utf8');

    (%MainAttr,%Tags)=((),());
    $Type='';
    @WaySegments = ();
    (%AllTags,%Nodes, %Segments, %Stats)=((),(),(),(),());

    # Currently active Area Filter
    $PARSING_START_TIME=0;
    # Estimated Number of elements to show progress while reading in percent
    for my $type ( qw( tag node segment way )) {
	$Stats{"${type}s estim"} = estimated_max_count($type);
    }
    $Stats{"tags"}     = 0;
    $Stats{"nodes"}    = 0;
    $Stats{"segments"} = 0;
    $Stats{"ways"}     = 0;

    #----------------------------------------------
    # Processing stage
    #----------------------------------------------

    print STDERR "creating $data_dir/osm-$area_name.csv\n" if $VERBOSE;

    my $base_filename="$data_dir/osm-$area_name";

    if ( $tie_nodes_hash ) {
	# maybe we should move this file to /tmp 
	# and lock it, and delete it in an END {} -Block
	print STDERR "Tie-ing Nodes Hash to '$base_filename-Nodes.db'\n";
	dbmopen(%Nodes,"$base_filename-Nodes.db",0666) 
	    or die "Could not open DBM File '$base_filename-Nodes.db': $!";
    }
    $Stats{"Tie Nodes_hash"} = $tie_nodes_hash;

    parse_planet($Filename,$area_name);

    printf STDERR "Creating output files\n";
    die "No Area Name defined\n"
	unless $area_name;

    # close and rename poi list
    $fh_named_points->close();
    rename("$fn_named_points.part",$fn_named_points)
	if -s "$fn_named_points.part";

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

    print STDERR "Reading and Parsing XML from $Filename for $area_name\n" if $DEBUG|| $VERBOSE;

    $AREA_FILTER = Geo::Filter::Area->new( area => $area_name );

    $PARSING_START_TIME=time();
    $READ_FH = data_open($Filename);
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
    binmode(OSM,":utf8");
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

	printf OSM "%s,%s,",$Nodes{$From},$Nodes{$To};
	for  my $tag ( @needed_segment_tags ) {
	    my $v='';
	    $v = $Segment->{$tag} if defined $Segment->{$tag};
	    print OSM "$v,";
	}
	foreach my $k ( keys %{$Segment} ){
	    #next if $k =~ m/^(class|name|highway)$/;
	    next if $k =~ m/^(from|to|segments)$/;
	    my $v = ($Segment->{$k}||'');
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
	$result = sprintf( "%f,%f",$Node->{"lat"},$Node->{"lon"});
	for  my $tag ( @needed_segment_tags ) {
	    $result .= sprintf( ",%s", ($Node->{$tag}||''));
	};
	foreach my $k ( keys %{$Node} ) {
	    next if $k =~ m/created_by/;
	    next if $k =~ m/converted_by/;
	    next if $k =~ m/source/;
	    next if $k =~ m/time/;
	    next if $k =~ m/^(lat|lon|name|amenity|class|highway)$/;
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
    binmode(STATS,":utf8");

    foreach(sort {$AllTags{$b} <=> $AllTags{$a}} keys(%AllTags)){
	printf STATS "* %d %s\n", $AllTags{$_}, $_;
    }
    printf STATS "\n\nStats:\n";
    for my $k ( keys(%Stats) ){
	printf STATS "* %5d %s\n", $Stats{$k}, ($k||'');
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
    $Stats{"${Element}s read"}++;
    $Stats{"tags read"}++;
    if ( defined( $Stats{"${Element}s read"} )
	 &&( $Stats{"${Element}s read"}== 1 ) ){
	$Stats{"memory at 1st $Element rss"} = sprintf("%.0f",mem_usage('rss'));
	$Stats{"memory at 1st $Element vsz"} = sprintf("%.0f",mem_usage('vsz'));
	if ( $DEBUG >1 || $VERBOSE >1) {
	    print STDERR "\n";
	}
    }
    
    if($Element eq "node"){
	my $node={};
	$node->{"lat"} = $MainAttr{"lat"};
	$node->{"lon"} = $MainAttr{"lon"};
	
	if ( $AREA_FILTER->inside($node) ) {
	    $Nodes{$ID} = sprintf("%f,%f",$MainAttr{lat}, $MainAttr{lon});
	    foreach(keys(%Tags)){
		$node->{$_} = $Tags{$_};
	    }
	    write_named_point($fh_named_points,$node);
	    $Stats{"nodes named"}++ if($node->{"name"});
	    $Stats{"nodes tagged "}++ if($MainAttr{"tags"});
	    $Stats{"nodes"}++;
	}
    }

    if($Element eq "segment"){
	my $from = $MainAttr{"from"};
	my $to   = $MainAttr{"to"};
	if ( defined($Nodes{$from}) && defined($Nodes{$to}) ) {
	    $Segments{$ID}{"from"} = $from;
	    $Segments{$ID}{"to"} = $to;

	    for ( @needed_segment_tags ) {
		$Segments{$ID}{$_} = $Tags{$_};
	    }
	    $Stats{"segments tagged"}++ if $MainAttr{"tags"};
	    $Stats{"segments"}++;
	}
    }

    if($Element eq "way"){
	if ( @WaySegments ) {
	    foreach my $seg_id( @WaySegments ){ # we only have the needed ones in here
		for my $tag ( @needed_segment_tags ) {
		    if ( defined($MainAttr{$tag}) && $MainAttr{$tag} ) {
			$Segments{$seg_id}{$tag} = $MainAttr{$tag};
		    }
		    if ( defined($Tags{$tag}) && $Tags{$tag} ) {
			$Segments{$seg_id}{$tag} = $Tags{$tag};
		    }
		}
	    }
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
	    next if $k =~ m/( estim| read| named| rss| vsz)$/;
	    print STDERR "$k:".$Stats{$k};
	    printf STDERR "=%.0f%%",(100*$Stats{"$k"}/$Stats{"$k read"})
		if $Stats{"$k read"};
	    printf STDERR " named:%d ",($Stats{"$k named"})
		if $Stats{"$k named"} && ($VERBOSE >4);
	    if ( $Stats{"$k read"}) {
		printf STDERR "(%d",($Stats{"$k read"}||0);
		printf STDERR "=%.0f%%",(100*($Stats{"$k read"}||0)/$Stats{"$k estim"})
		    if defined($Stats{"$k estim"});
		print STDERR ") ";
	    }
	    printf STDERR " ";
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

=item B<--tie-nodes-hash>

if set we will tie the Nodes Hash to a File
This is at least 10 times slower, but we have less problems with
running out of memory.
We have an internal list of estimated memory use and we'll try
automgically to tie it if you don't have enough memory for a
specified region.

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
