##################################################################
package Geo::Tracks::Kismet;
##################################################################

use Exporter;
@ISA = qw( Exporter );
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
@EXPORT = qw( read_kismet_file );

use strict;
use warnings;

use Math::Trig;
use Date::Parse;
use Data::Dumper;

use Data::Dumper;
use Geo::Geometry;
use Utils::Debug;
use Utils::File;
use Utils::Math;

# -----------------------------------------------------------------------------
# Read GPS Data from Kismet File
sub read_kismet_file($) { 
    my $filename = shift;

    my $start_time=time();

    my $data = {
	filename => $filename,
	tracks => [],
	wpt => [],
	};

    printf STDERR ("Reading $filename\n") if $VERBOSE>1 || $DEBUG;
    printf STDERR "$filename:	".(-s $filename)." Bytes\n" if $DEBUG;

    print STDERR "Parsing file: $filename\n" if $DEBUG;
    my $p = XML::Parser->new( Style => 'Objects' ,
			      );
    
    my $fh = data_open($filename);
    return $data unless $fh;
    my $content = [{Kids => []}];
    eval {
	$content = $p->parse($fh);
    };
    if ( $@ ) {
	warn "$@Error while parsing\n $filename\n";
	printf STDERR Dumper(\$content);
	#return $content->[0]->{Kids};
    }
    if (not $p) {
	print STDERR "WARNING: Could not parse osm data\n";
	return $data;
    }
    if ( $DEBUG ) {
	printf STDERR "Read and parsed $filename";
	print_time($start_time);
    }
    my $track=[];
    for my $elem  ( @{$content->[0]->{Kids}} ) {
	next unless ref($elem) eq "Geo::Tracks::Kismet::gps-point";
	next unless $elem->{'bssid'} eq 'GP:SD:TR:AC:KL:OG';
	delete $elem->{Kids};
	if ( defined($elem->{"time-sec"}) && defined($elem->{"time-usec"}) ) {
	    $elem->{time} = $elem->{"time-sec"}+($elem->{"time-usec"}/1000000);
	    #printf STDERR "$elem->{time} = $elem->{'time-sec'}  $elem->{'time-usec'}\n";
	}
	delete $elem->{'time-sec'};
	delete $elem->{'time-usec'};
	delete $elem->{'bssid'};
	delete $elem->{'signal'};
	delete $elem->{'quality'};
	delete $elem->{'noise'};
	$elem->{'speed'} = delete($elem->{'spd'}) * 1;
	if ( $DEBUG > 10 ) {
	    printf STDERR "read element: ".Dumper(\$elem);
	}
	push(@{$track},$elem);
    };
    
#    printf STDERR Dumper(\$track);

    $data->{tracks}=[$track];
    return $data;
}

1;

