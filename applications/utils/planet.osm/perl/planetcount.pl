#!/usr/bin/perl
# Takes a planet.osm, and counts elements

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

use Data::Dumper;
use Getopt::Long;

use Utils::Debug;
use Geo::OSM::Planet;
use Pod::Usage;
use Utils::Math;
use Utils::File;


our $man=0;
our $help=0;
my $do_empty=0;

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
	     ) or pod2usage(1);

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

$READ_FH = data_open($Filename);

my $last_id;
my $last_type;
my $PARSING_START_TIME=time();
my $PARSING_DISPLAY_TIME= 0;
my $line_count=0;
my $stat = {};
my $max_line = estimated_max_id("line");
while(my $line = $READ_FH->getline() ) {
    $line_count++;

    my ($type,$id);
    # Process the line of XML
    if ( $line =~ m/^\s*<(node|segment|way|seg|tag)\s*id\=[\'\"](\d+)[\'\"]/ ){
	($type,$id) = ($1,$2);
    } elsif ( $line =~ m/^\s*<(tag)\s*/ ){
	($type,$id) = ($1,1);
    } elsif($line =~ /^\s*\<\?xml/) {
    } elsif($line =~ /^\s*\<osm /) {
    } elsif($line =~ /^\s*\<\/osm|xml|node|segment|way|seg|tag\>/) {
    } else {
	warn "\nUnknown line $line\n";
    };


    if ( $type ) {
	#print "$type $id\n";
	$stat->{$type}->{max_id}=max($stat->{$type}->{max_id},$id);
	$stat->{$type}->{count}++;
	$stat->{line}->{max_id}++;
	$stat->{elem}->{count}++;
	$stat->{elem}->{max_id}++;
    }
    $stat->{line}->{count}++;

    if ( ( $VERBOSE || $DEBUG ) &&
	 ( time()-$PARSING_DISPLAY_TIME >0.9)
	 )  {
	$PARSING_DISPLAY_TIME= time();
	print STDERR "\r";
	print STDERR "Read: ";

	print STDERR time_estimate($PARSING_START_TIME,$line_count,$max_line);
	for my $type ( sort keys %{$stat} ) {
	    print STDERR " $type: $stat->{$type}->{max_id}($stat->{$type}->{count}) ";
	}
	#print STDERR mem_usage();
	print STDERR "\r";
    }
}

print Dumper(\$stat);

##################################################################
# Usage/manual

__END__

=head1 NAME

B<planetcount.pl>

=head1 DESCRIPTION

Takes a planet.osm, and counts elements.

=head1 SYNOPSIS

planetcount.pl [-d] [-v] [-h] [--no-mirror] [--proxy=<proxy:port>] <planet_filename.osm>

planetcount.pl [<planet.osm>]\t - parse planet.osm file and count the number of tags.

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
