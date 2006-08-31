##################################################################
package Geo::OSM::SegmentList;
##################################################################

use Exporter; require DynaLoader; require AutoLoader;
@ISA = qw(Exporter DynaLoader);
@EXPORT = qw( LoadOSM_segment_csv
	      );

use Math::Trig;

use Utils::File;
use Utils::Math;
use Utils::Timing;

sub LoadOSM_segment_csv($)
{
    my $filename = shift;

    printf STDERR "Reading OSM File: $filename\n"
	if $debug || $verbose;
    my $start_time=time();

    my $segments;

    if ( -s "$filename.storable") {
	# later we should compare if the file also is newer than the source
	$filename .= ".storable";
	$segments = Storable::retrieve($filename);
    } else {
	my $fh = File::data_open($filename);

	die "Cannot open $filename in LoadOSM_segment_csv.\n".
	    "Please create it first to use the option --osm.\n".
	    "See --help for more info"  unless $fh;

	while ( my $line = $fh ->getline() ) {
	    my @segment;
	    my $dummy;
	    ($segment[0],$segment[1],$segment[2],$segment[3],$dummy) = split(/,/,$line,5);
	    $segment[4] = angle_north_relative(
					    { lat => $segment[0] , lon => $segment[1] },
					    { lat => $segment[2] , lon => $segment[3] });
	    $segment[5] = $dummy if $debug;
	    push (@{$segments},\@segment);
	}
	$fh->close();
	Storable::store($segments   ,"$filename.storable");
    }

    if ( $verbose >1 || $debug) {
	printf STDERR "Read and parsed $filename";
	print_time($start_time);
    }

    return($segments);
}
