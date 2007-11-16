#!/usr/bin/perl
# Licence GPL

BEGIN {
    my $dir = $0;
    $dir =~s,[^/]+/[^/]+$,,;
    unshift(@INC,"$dir/perl_lib");
    unshift(@INC,"../perl_lib");
}


use strict;
use warnings;

use XML::Parser;
use Getopt::Long;
use IO::File;
use Pod::Usage;
use Data::Dumper;
use Carp;

use Geo::Filter::Area;
use Geo::OSM::Planet;
use Utils::Debug;
use Utils::File;
use Utils::Math;

sub parse_planet($$$); # {}

our $man=0;
our $help=0;
my $areas_todo;
my $do_list_areas=0;
my $do_update_only=0;

my $use_max_mem=0;

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


# TODO:
# if the input filename is not planet*osm* we have to change the output filename too.
my $Filename = shift();
unless ( $Filename && -s $Filename ) {
    $Filename = mirror_planet();
};
if ( ! -s $Filename ) {
    die "Cannot read $Filename\n";
}

pod2usage(1) unless $Filename;

our $READ_FH=undef;
our $WRITE_FH=undef;

$areas_todo ||= 'europe';
$areas_todo=lc($areas_todo);
if ( $do_list_areas ) {
    print Geo::Filter::Area->list_areas()."\n";
    exit;
}

print STDERR "Using OSM-subset: $Filename\n" if $VERBOSE || $DEBUG;


our (%MainAttr,$Type,%Tags, @WaySegments);
# Stats
# Stored data
our @NODES;
our @SEGMENTS;

our %Stats;
our $AREA_FILTER;
our $PARSING_START_TIME=0;
our $PARSING_DISPLAY_TIME=0;

our $vsz0 = mem_usage('vsz');

for my $area_name ( split(",",$areas_todo) ) {
    # -----------------------------------------------------------------------------
    # Temporary data

    (%MainAttr,%Tags)=((),());
    $Type='';
    @WaySegments = ();
    (%Stats)=('elem'=>0,'node'=>0,'segment'=>0,'way'=>0);
    # Currently active Area Filter
    $PARSING_START_TIME=0;

    #----------------------------------------------
    # Processing stage
    #----------------------------------------------

    my $file_out = $Filename;
    $file_out =~ s/\.osm(\.gz|\.bz2)?$/-$area_name.osm/;
    parse_planet($Filename,$file_out,$area_name);

    printf STDERR "$area_name Done\n";
}
exit;

#----------------------------------------------
# Parsing planet.osm File
#----------------------------------------------
sub parse_planet($$$){
    my $Filename_in = shift;
    my $Filename_out = shift;
    my $area_name = shift;

    print STDERR "Reading and Parsing XML from $Filename for $area_name\n" if $DEBUG;

    $AREA_FILTER = Geo::Filter::Area->new( area => $area_name );

    $PARSING_START_TIME=time();
    $READ_FH = data_open($Filename_in);
    $WRITE_FH = IO::File->new(">$Filename_out");
    $WRITE_FH->binmode(':utf8');
    print $WRITE_FH '<?xml version="1.0" encoding="UTF-8"?>'."\n";
    print $WRITE_FH '<osm version="0.3" generator="OpenStreetMap planet.rb">'."\n";

    if ( $VERBOSE || $DEBUG )  {
	print STDERR "\n";
	print STDERR "osm-subset.pl: Parsing $Filename_in for area ".$AREA_FILTER->name()."\n";
    }
    my $P = new XML::Parser( Handlers => {
	Start => \&DoStart, 
	End => \&DoEnd, 
	Char => \&DoChar});
    eval {
	$P->parse($READ_FH);
	$READ_FH->close();
	print $WRITE_FH '</osm>'."\n";
	$WRITE_FH->close();
    };
    if ( $VERBOSE || $DEBUG )  {
	print STDERR "\n";
    }

    if ($@) {
	print STDERR "WARNING: Could not parse osm data $Filename_in\n";
	print STDERR "ERROR: $@\n";
	return;
    }
    if (not $P) {
	print STDERR "WARNING: Could not parse osm data $Filename_in\n";
	return;
    }
    $Stats{"time parsing"} = time()-$PARSING_START_TIME;
    printf("osm-subset.pl: Parsing Osm-Data in %.0f sec\n",time()-$PARSING_START_TIME )
	if $DEBUG || $VERBOSE;

}

sub display_status($){
    my $mode = shift;
    return unless $VERBOSE || $DEBUG ;
    return unless time()-$PARSING_DISPLAY_TIME >2;

    $PARSING_DISPLAY_TIME= time();
    print STDERR "\r";
    #print STDERR "$mode(".$AREA_FILTER->name()."): ";

    print STDERR time_estimate($PARSING_START_TIME,
			       $Stats{"elem read"},estimated_max_count("elem"));

    my $pos = $READ_FH->tell();
    printf STDERR " pos:%.2fGB ",$pos/1024/1024/1024;
    for my $k ( sort keys %Stats ) {
	next if $k =~ m/( read)$/;
	next if $k =~ m/(tag|mem)$/;
	next if $k =~ m/named/ && $VERBOSE <=1;
	next if $k !~ m/elem/ && $VERBOSE <=2;
	print STDERR "$k:".$Stats{$k};
	my $estim=estimated_max_count($k);
	if ( defined($Stats{"$k read"}) ) {
	    printf STDERR "=%.0f%%",(100*$Stats{"$k read"}/$estim) 
		if $estim;
	}
	print STDERR "($estim) ";
    }

    my $vsz = mem_usage('vsz');

    if ( ! $use_max_mem and (mem_info("MemFree")<10) ){
	die "\nToo much memory ($vsz MB) used; MemFree: ".mem_info("MemFree")."MB\n";
    }
    if ( $use_max_mem >0 and ( $vsz > $use_max_mem ) ) {
	die "\nToo much memory($vsz MB) used; max allowed: $use_max_mem MB ".mem_info()."\n";
    }
    
    print STDERR mem_usage();
    print STDERR "\r";
    
    #print STDERR "\n";
    #store_mem_arrays();

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
	$Stats{"tag"}++;
    }
    if($Name eq "seg"){
	my $id = $Attr{"id"};
	if ( $SEGMENTS[$id] ) {
	    push(@WaySegments, $id);
	}
    }
    $Stats{"elem"}++;
}

# ------------------------------------------------------------------
sub tags2osm($){
    my $tags = shift;
    
    my $erg = "";
    for my $k ( keys %{$tags} ) {
	my $v = $tags->{$k};
	if ( ! defined $v ) {
	    warn "incomplete Object: ".Dumper(\$tags);
	}
	#next unless defined $v;

	# character escaping as per http://www.w3.org/TR/REC-xml/
	$v =~ s/&/&amp;/g;
	$v =~ s/\'/&apos;/g;
	$v =~ s/</&lt;/g;
	$v =~ s/>/&gt;/g;
	$v =~ s/\"/&quot;/g;

	$erg .= "    <tag k=\"$k\" v=\"$v\" />\n";
    }
    return $erg;
}

# Function is called whenever an XML tag is ended
#----------------------------------------------
sub DoEnd(){
    my ($Expat, $Element) = @_;
    my $id = $MainAttr{"id"};
    $Stats{"elem read"}++;
    display_status("Read: ");

    if($Element eq "node"){
	$Stats{"node read"}++;
	if ( $AREA_FILTER->inside(\%MainAttr) ) {
	    $NODES[$id]=1;

	    print $WRITE_FH "  <node id=\"$id\"";
	    print $WRITE_FH " lat=\"$MainAttr{lat}\"";
	    print $WRITE_FH " lon=\"$MainAttr{lon}\"";
	    print $WRITE_FH " timestamp=\"".$MainAttr{timestamp}."\"" 
		if defined $MainAttr{timestamp};
	    if ( keys %Tags ){
		print $WRITE_FH ">\n";
		print $WRITE_FH tags2osm(\%Tags);
		print $WRITE_FH "  </node>\n";
	    } else {
		print $WRITE_FH "/>\n";
	    }
	    $Stats{"node"}++;
	}
    }

    if($Element eq "segment"){
	print STDERR "\n" unless $Stats{"segment read"}++ && $VERBOSE;
	my $from = $MainAttr{"from"};
	my $to   = $MainAttr{"to"};
	if ( $NODES[$from] and $NODES[$to] ) {
	    print $WRITE_FH "  <segment id=\"$id\"";
	    print $WRITE_FH " from=\"$MainAttr{from}\"";
	    print $WRITE_FH " to=\"$MainAttr{to}\" ";
	    print $WRITE_FH " timestamp=\"".$MainAttr{timestamp}."\"" 
		if defined $MainAttr{timestamp};
	    if ( keys %Tags ){
		print $WRITE_FH ">\n";
		print $WRITE_FH tags2osm(\%Tags);
		print $WRITE_FH "  </segment>\n";
	    } else {
		print $WRITE_FH "/>\n";
	    }
	    $Stats{"segment"}++;
	    $SEGMENTS[$id]=1;
	}
    }

    if($Element eq "way"){
	print STDERR "\n" unless $Stats{"way read"}++ && $VERBOSE;
	if ( @WaySegments ) {
	print $WRITE_FH "  <way id=\'$id\'";
	print $WRITE_FH " timestamp=\'".$MainAttr{timestamp}."\'" 
	    if defined $MainAttr{timestamp};
	print $WRITE_FH ">";
	print $WRITE_FH tags2osm(\%Tags);
	for my $seg_id ( @WaySegments) {
	    next unless $seg_id;
	    print $WRITE_FH "    <seg id=\'$seg_id\'/>\n";
	}
	print $WRITE_FH "  </way>\n";
	$Stats{"way"}++;
	}
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

B<osm-subset.pl.pl> Version 0.02

=head1 DESCRIPTION

B<osm-subset.pl.pl> is a program to extract an area from an osm File

=head1 SYNOPSIS

B<Common usages:>

osm-subset.pl.pl [-d] [-v] [-h] [--no-mirror] [--proxy=<proxy:port>] [--list-areas] <planet_filename.osm>

=head1 OPTIONS

=over 2

=item B<--man> Complete documentation

Complete documentation

=item B<--proxy=<proxy:port>>

Use proxy Server to get the newest planet.osm File

=item B<--no-mirror>

do not try to get the newest planet.osm first

=item B<--debug> B<-d>

write out some debug info too

=item B<--verbose> B<-v>

write out more info while processing

=item B<--area=germany> Area Filter

Select Area for processing

=item B<--list-areas>

print all areas possible

=item B<planet_filename.osm>

the file to read from

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

Jörg Ostertag (planet-count-for-openstreetmap@ostertag.name)

=head1 SEE ALSO

http://www.openstreetmap.org/

=cut
