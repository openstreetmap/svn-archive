##################################################################
package Geo::Tracks::Netmonitor;
##################################################################

use Exporter;
@ISA = qw( Exporter );
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
@EXPORT = qw( read_track_Netmonitor);

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
# Read GPS Data from Netmonitor - File (www.nobbi.com)
sub read_track_Netmonitor($) { 
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

    my $fh = data_open($filename);
    return $new_tracks unless $fh;
    my $new_track=[];
    
    while ( my $line = $fh->getline() ) {
	chomp $line;

	print "$line\n" if $DEBUG>4;

	my ($time,$networkstate,$lon,$lat,$dummy1,$dummy2,$dummy3) = split(/[\t\ \s]+/,$line);
	my $alt=0;
	if ( ( !( $lat && $lon) )
	     || ( $lat eq "---.-----" ) || ($lon eq "---.-----")
	     || ( $lat eq "-" ) || ($lon eq "-") ) {
	    # XXXX: Maybe we can split the track here if this happens
	    next;
	}

	$time =~ s/^(\d\d\d\d)(\d\d)(\d\d)(\d\d)(\d\d)(\d\d)/$1-$2-$3 $4:$5:$6/;

	$lat=~ s/\s*//g;
	$lon=~ s/\s*//g;
	$lat=~ s/,/\./g;
	$lon=~ s/,/\./g;
#	printf STDERR "$time\t, la: $lat, lo: $lon\n";
	if ( $lon =~ s/^E// ) {
	} elsif ( $lon =~ s/^W// ) {
	    $lon = -$lon;
	};
	if ( $lat =~ s/^N// ) {
	} elsif ( $lat =~ s/^S// ) {
	    $lat = -$lat;
	};
	if ( ( abs($lon)+ abs($lat)) < 0.1 ) {
	    printf STDERR "Skipping  |$lat|+|$lon| <0.01\n"
		if $DEBUG >5;
	    next;
	}

	$time = str2time($time);
	if ( $DEBUG >3) {
	    printf STDERR "".localtime($time)."\t, la: $lat, lo: $lon. alt:$alt\n";
	};


	unless ( defined( $lat) && ($lat ne "" )&& defined( $lon) && ($lon ne "")) {
	    print "ERROR in Line: $line\n";
	    next;
	};

	my $elem ={};
	$elem->{lat} = $lat;
	$elem->{lon} = $lon;
	$elem->{alt} = $alt if defined $alt;
	$elem->{time} = $time if defined $time;
	$time ||=0;

    	if ( defined $elem->{time} &&
	     defined $elem->{lat} &&
	     defined $elem->{lon} ) { 
	    bless($elem,"TRK::gps-point");
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

Netmonitor.pm

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

Jörg Ostertag (Netmonitor.pm.openstreetmap@ostertag.name)

=head1 SEE ALSO

http://www.openstreetmap.org/

=cut
