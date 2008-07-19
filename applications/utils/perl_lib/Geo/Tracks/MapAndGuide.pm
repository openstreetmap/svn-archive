##################################################################
package Geo::Tracks::MapAndGuide;
##################################################################

use Exporter;
@ISA = qw( Exporter );
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
@EXPORT = qw( read_track_MapAndGuide );

use strict;
use warnings;

use Data::Dumper;
use IO::File;
use Utils::Debug;
use Utils::File;
use Utils::Math;
use Date::Manip;
use Date::Parse;
use Time::Local;

# -----------------------------------------------------------------------------
# Read GPS Data from MapAndGuide - File
sub read_track_MapAndGuide($) { 
    my $filename = shift;

    my $start_time=time();

    my $new_tracks={ 
	filename => $filename,
	tracks => [],
	wpt => []
	};
    printf STDERR ("Reading $filename\n") if $VERBOSE || $DEBUG;
    printf STDERR "$filename:	".(-s $filename)." Bytes\n" if $DEBUG;

    my ($fn_mtime) = (stat($filename))[9] || 0;
    #print "$filename mtime: ".localtime($fn_mtime)."\n";
    my $date ="01-01-2007";

    my $fh = data_open($filename);
    return $new_tracks unless $fh;
    my $new_track=[];
    
    while ( my $line = $fh->getline() ) {
	chomp $line;

	print "$line\n" if $DEBUG>10;

	my ($lon,$lat,$heading,$dummy,$speed) = split(/\s*,\s*/,$line);
	$lat /= 100000;
	$lon /= 100000;
	if ( ( abs($lon)+ abs($lat)) < 0.1 ) {
	    printf STDERR "Skipping  |$lat|+|$lon| <0.01\n"
		if $DEBUG >5;
	    next;
	}
	if ( $DEBUG >3) {
	    printf STDERR "\t, la: $lat, lo: $lon,";
	    printf STDERR "\tHeading: %6.2f\n",$heading;
	};


	if ( $heading>360) {
	    print STDERR "Here something is wrong the heading ($heading) ".
		"is larger than 360 degrees\n";
	    print STDERR "Line: $line\n";
	}
	my ($msg_anz,$msg_no,$rest);

	unless ( defined( $lat) && ($lat ne "" )&& defined( $lon) && ($lon ne "")) {
	    print "ERROR in Line: $line\n";
	    next;
	};

	my $elem ={};
	$elem->{lat} = $lat;
	$elem->{lon} = $lon;
	$elem->{speed} = $speed;
	my $time ||=0;

    	if ( defined $elem->{lat} &&
	     defined $elem->{lon} ) { 
	    bless($elem,"MapAndGuide::gps-point");
	    push(@{$new_track},$elem);
	    $elem ={};
	} else {
	    if ( @$new_track ) {
		push(@{$new_tracks->{tracks}},$new_track);
	    }
	    $new_track=[];
	}
    }
       
    push(@{$new_tracks->{tracks}},$new_track);
    if ( $VERBOSE >1 ) {
	printf STDERR "Read and parsed $filename";
	print_time($start_time);
    }
    
    return $new_tracks;
}

1;

__END__

=head1 NAME

MapAndGuide.pm

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

Jörg Ostertag (MapAndGuide.pm.openstreetmap@ostertag.name)

=head1 SEE ALSO

http://www.openstreetmap.org/

=cut
