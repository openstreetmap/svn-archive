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
use Utils::Timing;
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
