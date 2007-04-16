##################################################################
package Geo::Tracks::GpsBabel;
##################################################################

use Exporter;
@ISA = qw( Exporter );
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
@EXPORT = qw( read_track_GpsBabel );

use strict;
use warnings;
use IO::File;

use Geo::Geometry;
use Utils::File;
use Utils::Math;
use Utils::Debug;
use Geo::GPX::File;


# -----------------------------------------------------------------------------
# Read GPS Data with the help of gpsbabel converting a file to a GPX-File
sub read_track_GpsBabel($$) { 
    my $filename     = shift;
    my $gpsbabel_type = shift;

    my $data = {
	filename => $filename,
	tracks => [],
	wpt => [],
	};

    printf STDERR ("Reading $filename\n") if $VERBOSE>1 || $DEBUG;
    printf STDERR "$filename:	".(-s $filename)." Bytes\n" if $DEBUG;

    my $gpsbabel_call="gpsbabel  -i $gpsbabel_type -f '$filename' -o gpx -F - ";
    printf STDERR "calling gpsbabel:\n$gpsbabel_call\n " if $DEBUG>3;
    my $fh = IO::File->new("$gpsbabel_call |");
    if ( !$fh )  {
	warn "Cannot Convert $filename as Type $gpsbabel_type\n";
	return $data;
    }
    $data = read_gpx_file($fh);
    $data->{filename} = $filename;
    return $data;
}

1;

__END__

=head1 NAME

GpsBabel.pm

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
