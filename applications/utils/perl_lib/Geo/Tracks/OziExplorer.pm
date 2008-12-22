##################################################################
package Geo::Tracks::OziExplorer;
##################################################################

use Exporter;
@ISA = qw( Exporter );
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
@EXPORT = qw( read_track_OziExplorer is_format_OziExplorer );

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

=begin

Track File (.plt) Format
Source: http://www.rus-roads.ru/gps/help_ozi/fileformats.html

Line 1 : File type and version information
Line 2 : Geodetic Datum used for the Lat/Lon positions for each trackpoint
Line 3 : "Altitude is in feet" - just a reminder that the altitude is always stored in feet
Line 4 : Reserved for future use
Line 5 : multiple fields as below

    Field 1 : always zero (0)
    Field 2 : width of track plot line on screen - 1 or 2 are usually the best
    Field 3 : track color (RGB)
    Field 4 : track description (no commas allowed)
    Field 5 : track skip value - reduces number of track points plotted, usually set to 1
    Field 6 : track type - 0 = normal , 10 = closed polygon , 20 = Alarm Zone
    Field 7 : track fill style - 0 =bsSolid; 1 =bsClear; 2 =bsBdiagonal; 3 =bsFdiagonal; 4 =bsCross;
    5 =bsDiagCross; 6 =bsHorizontal; 7 =bsVertical;
    Field 8 : track fill color (RGB)

Line 6 : Number of track points in the track, not used, the number of points is determined when reading the points file

Trackpoint data

    * One line per trackpoint
    * each field separated by a comma
    * non essential fields need not be entered but comma separators must still be used (example ,,)
      defaults will be used for empty fields

Field 1 : Latitude - decimal degrees.
Field 2 : Longitude - decimal degrees.
Field 3 : Code - 0 if normal, 1 if break in track line
Field 4 : Altitude in feet (-777 if not valid)
Field 5 : Date - see Date Format below, if blank a preset date will be used
Field 6 : Date as a string
Field 7 : Time as a string

Note that OziExplorer reads the Date/Time from field 5, the date and time in fields 6 & 7 are ignored.

Example
-27.350436, 153.055540,1,-777,36169.6307194, 09-Jan-99, 3:08:14
-27.348610, 153.055867,0,-777,36169.6307194, 09-Jan-99, 3:08:14

Date Format

Delphi stores date and time values in the TDateTime type. The integral part of a TDateTime value is the number of days that have passed since 12/30/1899. The fractional part of a TDateTime value is the time of day.

Following are some examples of TDateTime values and their corresponding dates and times:

0 - 12/30/1899 12:00 am
2.75 - 1/1/1900 6:00 pm
-1.25 - 12/29/1899 6:00 am
35065 - 1/1/1996 12:00 am

=cut

# -----------------------------------------------------------------------------
# Check, if input file is really from OziExplorer
sub is_format_OziExplorer($) {
    my $filename = shift;
    my $fh       = data_open($filename);
    return undef if ( !$fh );

    my $line = $fh->getline();
    $line =~ s/\r?\n//;

    # Line must look like
    # OziExplorer Track Point File Version 2.1
    # Next line must be WGS 84
    if ( $line eq "OziExplorer Track Point File Version 2.1" ) {
	$line = $fh->getline();
	$line =~ s/\r?\n//;
	print STDERR "Check line: '$line'\n";
	$fh->close();
	return 1 if ( $line eq "WGS 84" );
    }
    $fh->close();

    return 0;
}

# -----------------------------------------------------------------------------
# Read GPS Data from OziExplorer - File
sub read_track_OziExplorer($) {
    my $filename = shift;

    my $start_time = time();

    my $new_tracks = {
        filename => $filename,
        tracks   => [],
        wpt      => []
    };
    printf STDERR ("Reading $filename\n") if $VERBOSE || $DEBUG;
    printf STDERR "$filename:	" . ( -s $filename ) . " Bytes\n" if $DEBUG;

    my ($fn_mtime) = ( stat($filename) )[9] || 0;

    my $fh = data_open($filename);
    return $new_tracks unless $fh;
    my $new_track = [];

    my $time        = time();
    my $description = '';
    while ( my $line = $fh->getline() ) {
	$line =~ s/\r?\n//;

	# Parse some information out of the header
	# Fetch Track description
	if ( $line =~ /^0,\d+,\d+,([^,]*),\d+/ ) {
	    $description = $1 || undef; # undef to prevent writing this tag into GPX
	    next;
	}

	# No Trackpoint data? -> Skip it
	# Field 1 : Latitude - decimal degrees.
	# Field 2 : Longitude - decimal degrees.
	# Field 3 : Code - 0 if normal, 1 if break in track line
	# Field 4 : Altitude in feet (-777 if not valid)
	# Field 5 : Date - see Date Format below, if blank a preset date will be used
	# Field 6 : Date as a string
	# Field 7 : Time as a string
	next if $line !~ m/^\d+\.\d+,\d+\.\d+,\d+,\d+\.\d+,\d+\.\d+,,$/;

	print "$line\n" if $DEBUG > 10;

	my ( $lat, $lon, $track_brk, $altitude, $date, $date_string,
	    $time_string ) = split( /\s*,\s*/, $line );
	$altitude = 0 if $altitude == -777;
	$altitude *= 0.3048;    # Convert feet to meters

	# $date are the days since 12/30/1899, so we have to convert
	# 0 in Unix timestamp is the same as 25569.0 in TDateTime;
	$date -= 25569.0;
	$date = int($date * 86400);

	if ( ( abs($lon) + abs($lat) ) < 0.1 ) {
	    printf STDERR "Skipping  |$lat|+|$lon| <0.01\n"
	      if $DEBUG > 5;
	    next;
	}
	if ( $DEBUG > 3 ) {
	    printf STDERR "\t lat: %lf, lon: %lf, alt: %lf, track_brk: %d, date: %s\n",
	      $lat, $lon, $altitude, $track_brk, scalar localtime($date);
	}

	unless ( defined($lat)
	    && ( $lat ne "" )
	    && defined($lon)
	    && ( $lon ne "" ) )
	{
	    print "ERROR in Line: $line\n";
	    next;
	}

	my $elem = {};
	$elem->{time} = $date;
	$elem->{lat}  = $lat;
	$elem->{lon}  = $lon;
	$elem->{ele}  = $altitude;
	$elem->{cmt}  = $description;

	if ( $track_brk ) {
	    push( @{ $new_tracks->{tracks} }, $new_track ) if (@$new_track);
	    $new_track = [];
	} elsif (   defined $elem->{lat}
	    && defined $elem->{lon} )
	{
	    bless( $elem, "OziExplorer::gps-point" );
	    push( @{$new_track}, $elem );
	    $elem = {};
	} else {
	    if (@$new_track) {
	        push( @{ $new_tracks->{tracks} }, $new_track );
	    }
	    $new_track = [];
	}
    }

    push( @{ $new_tracks->{tracks} }, $new_track );
    if ( $VERBOSE > 1 ) {
        printf STDERR "Read and parsed $filename";
        print_time($start_time);
    }

    return $new_tracks;
}

1;

__END__

=head1 NAME

OziExplorer.pm

=head1 COPYRIGHT

Copyright 2008, Matthias Pitzl

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

Matthias Pitzl 

=head1 SEE ALSO

http://www.openstreetmap.org/

=cut
