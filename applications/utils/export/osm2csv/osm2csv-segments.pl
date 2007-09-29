#!/usr/bin/perl

BEGIN {
    my $dir = $0;
    $dir =~s,[^/]+/[^/]+$,,;
    unshift(@INC,"$dir/../perl_lib");

    unshift(@INC,"../perl_perl_lib");
    unshift(@INC,"~/svn.openstreetmap.org/applications/utils/perl_lib");
    unshift(@INC,"$ENV{HOME}/svn.openstreetmap.org/applications/utils/perl_lib");
}


use strict;
use warnings;

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

sub parse_planet($$); # {}

our $man=0;
our $help=0;
my $areas_todo;
my $do_list_areas=0;
my $do_update_only=0;
my $tie_nodes_hash=undef;
my $Filename;

our $SEGMENTS_FILENAME;
our $FH_OSM;

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

if ( $do_list_areas ) {
    print Geo::Filter::Area->list_areas()."\n";
    exit;
}

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


our (%MainAttr,$Type,%Tags);
# Stored data
our (%Nodes, %Stats);
our $AREA_FILTER;
our $PARSING_START_TIME=0;
our $PARSING_DISPLAY_TIME=0;
our $PARSING_ELEM_COUNT=0;

my $data_dir=planet_dir()."/csv";
mkdir_if_needed( $data_dir );

for my $area_name ( split(",",$areas_todo) ) {
    if ( $do_update_only ) {
	my $needs_update=0;
	$needs_update ||= file_needs_re_generation($Filename,"$data_dir/osm-segents-$area_name.csv");
	next unless $needs_update;
	print STDERR "Update needed. One of the files is old or non existent\n" if $VERBOSE;
    }
    # -----------------------------------------------------------------------------
    # Temporary data

    (%MainAttr,%Tags)=((),());
    $Type='';
    (%Nodes, %Stats)=((),());

    # Currently active Area Filter
    $PARSING_START_TIME=0;
    # Estimated Number of elements to show progress while reading in percent
    for my $type ( qw(elem tag node segment )) {
	$Stats{"${type} estim"} = estimated_max_count($type);
	$Stats{"${type} seen"}=0;
	$Stats{"${type} read"}=0;
    }

    #----------------------------------------------
    # Processing stage
    #----------------------------------------------

    print STDERR "creating $data_dir/osm-segments-$area_name.csv\n" if $VERBOSE;

    my $base_filename="$data_dir/osm-segments-$area_name";

    if ( $tie_nodes_hash ) {
	# maybe we should move this file to /tmp 
	# and lock it, and delete it in an END {} -Block
	print STDERR "Tie-ing Nodes Hash to '$base_filename-Nodes.db'\n";
	dbmopen(%Nodes,"$base_filename-Nodes.db",0666) 
	    or die "Could not open DBM File '$base_filename-Nodes.db': $!";
    }
    $Stats{"Tie Nodes_hash"} = $tie_nodes_hash;

    printf STDERR "Creating output files\n";
    die "No Area Name defined\n"
	unless $area_name;

    $SEGMENTS_FILENAME = "$data_dir/osm-segments-$area_name.csv";
    $FH_OSM = IO::File->new(">$SEGMENTS_FILENAME.part");
    if( ! $FH_OSM ) {
	warn "output_osm: Cannot write to $SEGMENTS_FILENAME\n";
	return;
    }
    $FH_OSM->binmode(":utf8");

    parse_planet($Filename,$area_name);

    printf STDERR "$area_name Done\n";
}
exit;


sub percent_string($$){
    my $part = shift;
    my $full = shift;
    my $erg = "";
    $erg = sprintf("%.0f%%",(100*$part/$full)) if $full;
    return $erg;
}

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
    while ( my $line = $READ_FH->getline() ) {
	parse_line($line);
    };
    $READ_FH->close();
    if ( $VERBOSE || $DEBUG )  {
	print STDERR "\n";
    }
    $Stats{"time parsing"} = time()-$PARSING_START_TIME;
    printf("osm2csv: Parsing Osm-Data in %.0f sec\n",time()-$PARSING_START_TIME )
	if $DEBUG || $VERBOSE;

}

# Function is called whenever an XML tag is ended
#----------------------------------------------
sub parse_line(){
    my $line = shift;

    my $element='';

    if($line =~ m/node.*id="([\d\.\+\-]+)".*lat="([\d\.\+\-]+)".*lon="([\d\.\+\-]+)"/ ){
	my $node={id=>$1,lat=>$2,lon=>$3};
#	print $line. Dumper(\$node);
	my $id=$1;
	$element="node";
	if ( $AREA_FILTER->inside($node) ) {
	    $Nodes{$id} = sprintf("%f,%f",$node->{lat}, $node->{lon});
	    $Stats{"node read"}++;
	    $Stats{"elem read"}++;
	}
    } elsif ($line =~ m/segment.*id="([\d\.\+\-]+)".*from="([\d\.\+\-]+)".*to="([\d\.\+\-]+)"/ ){
	$element = "segment";
	my $id   = $1;
	my $from = $2;
	my $to   = $3;
	if ( defined($Nodes{$from}) && defined($Nodes{$to}) ) {
	    printf $FH_OSM "%s,%s\n",$from,$to;
	    $Stats{"segment read"}++;
	    $Stats{"elem read"}++;
	}
    } elsif($line =~ m/way.*id=/ ){
	$element = "way";
	if ($FH_OSM) {
	    $FH_OSM->close() || warn "Cannot close Segment File:$!\n";
	    $FH_OSM=0;
	    rename("$SEGMENTS_FILENAME.part",$SEGMENTS_FILENAME)
		if -s "$SEGMENTS_FILENAME.part";
	    
	    print STDERR "we're done; skipping rest\n";
	}
    }

    $Stats{"$element seen"}++;
    $Stats{"elem seen"}++;
    if ( defined( $Stats{"$element seen"} )
	 &&( $Stats{"$element seen"}== 1 ) ){
	$Stats{"memory at 1st $element rss"} = sprintf("%.0f",mem_usage('rss'));
	$Stats{"memory at 1st $element vsz"} = sprintf("%.0f",mem_usage('vsz'));
	if ( $DEBUG >1 || $VERBOSE >1) {
	    print STDERR "\n";
	}
    }

    $PARSING_ELEM_COUNT++;
    if ( ( $VERBOSE || $DEBUG ) &&
#	 ! ( $Stats{"tags read"} % 10000 ) &&
	 $PARSING_ELEM_COUNT >1000 && 
	 ( time()-$PARSING_DISPLAY_TIME > 0.9)
	 )  {
	$PARSING_ELEM_COUNT=0;
	$PARSING_DISPLAY_TIME= time();
	print STDERR "\r";
	print STDERR "Read(".$AREA_FILTER->name()."): ";
	for my $k ( qw(elem node segment ) ) {
	    if ( $DEBUG>6 || $VERBOSE>6) {
		print STDERR $k;
	    } else {
		print STDERR substr($k,0,1);
	    }
	    print STDERR ":";
	    printf STDERR "%d read",$Stats{"$k read"};
	    printf STDERR "=%s",percent_string($Stats{"$k read"},$Stats{"$k seen"});

	    printf STDERR "(%d seen",($Stats{"$k seen"}||0);
	    printf STDERR "=%s",percent_string($Stats{"$k seen"},$Stats{"$k estim"});
	    print STDERR ") ";
	}
	
	my $rss = sprintf("%.0f",mem_usage('rss'));
	$Stats{"max rss"} = max($Stats{"max rss"},$rss) if $rss;
	printf STDERR "max-rss %d" ,($Stats{"max rss"}) if $Stats{"max rss"} >$rss*1.3;
	my $vsz = sprintf("%.0f",mem_usage('vsz'));
	$Stats{"max vsz"} = max($Stats{"max vsz"},$vsz) if $vsz;
	printf STDERR "max-vsz %d" ,($Stats{"max vsz"}) if $Stats{"max vsz"} >$vsz*1.3;

	print STDERR mem_usage();
	print STDERR time_estimate($PARSING_START_TIME,
				   $Stats{"node seen"}+$Stats{"segment seen"},
				   $Stats{"node estim"}+$Stats{"segment estim"});
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

B<osm2csv-segments.pl> Version 0.02

=head1 DESCRIPTION

B<osm2csv-segments.pl> is a program to convert osm-segments from xml format to 
a plain text file in csv form. 
This format then is normally used by osmtrackfilter to compare against osm segments

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
