# Einlesen der Kismet Daten und schreiben in die geoinfo Datenbank von 
# gpsdrive
#
# $Log$
# Revision 1.1  2005/08/15 13:54:22  tweety
# move scripts/POI --> scripts/Geo/Gpsdrive to reflect final Structure and make debugging easier
#
# Revision 1.6  2005/08/09 01:08:30  tweety
# Twist and bend in the Makefiles to install the DataDirectory more apropriate
# move the perl Functions to Geo::Gpsdrive in /usr/share/perl5/Geo/Gpsdrive/POI
# adapt icons.txt loading according to these directories
#
# Revision 1.5  2005/05/24 08:35:25  tweety
# move track splitting to its own function +sub track_add($)
# a little bit more error handling
# earth_distance somtimes had complex inumbers as result
# implemented streets_check_if_moved_reset which is called when you toggle the draw streets button
# this way i can re-read all currently displayed streets from the DB
# fix minor array iindex counting bugs
# add some content to the comment column
#
# Revision 1.4  2005/04/13 19:58:30  tweety
# renew indentation to 4 spaces + tabstop=8
#
# Revision 1.3  2005/04/10 00:15:58  tweety
# changed primary language for poi-type generation to english
# added translation for POI-types
# added some icons classifications to poi-types
# added LOG: Entry for CVS to some *.pm Files
#

package Geo::Gpsdrive::Kismet;

use strict;
use warnings;

use IO::File;
use File::Basename;
use File::Path;
use Data::Dumper;

use Geo::Gpsdrive::DBFuncs;
use Geo::Gpsdrive::Utils;
use Geo::Gpsdrive::Gps;

use Date::Manip;
use Time::Local;

$|=1;

##########################################################################
sub bad_location($$){
    my $lat = shift;
    my $lon = shift;
    return 1 
	if $lat>48.17552 && $lon >11.75440 
	&& $lat<48.17579 && $lon <11.75494;
    return 0;
}

##########################################################################

sub import_Kismet_track_file($$){
    my $full_filename = shift;
    my $source = shift;

    print "Reading $full_filename                   \n";

    my $fh = IO::File->new("<$full_filename");


    my $source_id = Geo::Gpsdrive::DBFuncs::source_name2id($source);

    unless ( $source_id ) {
	my $source_hash = {
	    'source.url'     => "",
	    'source.name'    => $source ,
	    'source.comment' => 'My own Tracks' ,
	    'source.licence' => "It's up to myself"
	    };
	Geo::Gpsdrive::DBFuncs::insert_hash("source", $source_hash);
	$source_id = Geo::Gpsdrive::DBFuncs::source_name2id($source);
    }
    
    my $track = { 
	scale_min => 0, 
	scale_max => 10000000,
	name      => sprintf("Track %s",basename($full_filename)),
	source_id => $source_id,
	segments  => [],
    } ;

    my $line_count=0;

    while ( my $line = $fh->getline() ) {
	$line_count++;
	chomp $line;
	# <gps-point bssid="GP:SD:TR:AC:KL:OG" time-sec="1081010927" time-usec="47374" 
	#            lat="48.175289" lon="11.747722" alt="1672.439941" spd="33.257549" 
	#            heading="267.482422" fix="3" signal="18" quality="0" noise="7"/>

        next unless ( $line =~ s/^\s*<.*gps-point\s+bssid="GP:SD:TR:AC:KL:OG"\s*// );
        unless ( $line =~ s/\/>\s*$// ) {
	    print "incomplete Line: $line\n";
	    next;
	}
	#print "$line\n";

        my %elem;
	( $elem{lat},$elem{lon},$elem{alt} ) = (1001,1001,-1001);

        %elem = split(/[\s=]+/,$line);
        for my $k ( keys %elem ) {
            $elem{$k} =~ s/^"(.*)"$/$1/;
        }
        
	$elem{time} = $elem{'time-sec'}+( $elem{'time-usec'}/1000000);

	push (@{$track->{segments}}, {
	    lat     => $elem{lat},
	    lon     => $elem{lon},
	    alt     => $elem{alt},
	    time    => $elem{time},
	    speed   => miles2km($elem{spd}),
	    heading => $elem{heading}
	});
		 
    }
    my $segment_count = @{$track->{segments}};
    Geo::Gpsdrive::DBFuncs::track_add( $track );
    print "read $line_count lines with $segment_count segments from $full_filename\n";
}




# *****************************************************************************
sub import_Data($){
    my $dir = shift;
    my $kismet_file_pattern = $dir || "$main::CONFIG_DIR}/kismet";
    my $source = "Kismet Tracks";
    delete_all_from_source($source);

    $kismet_file_pattern = "$kismet_file_pattern/*.gps" if -d $kismet_file_pattern;
    $kismet_file_pattern = "$kismet_file_pattern*.gps" unless $kismet_file_pattern =~ m/\.gps$/;

    my @files = glob($kismet_file_pattern);
    printf "Importing (%d) files from $kismet_file_pattern/*.gps\n",scalar @files;
    Geo::Gpsdrive::DBFuncs::disable_keys('streets');
    foreach  my $full_filename ( @files ) {
	import_Kismet_track_file($full_filename,$source);
    }
    Geo::Gpsdrive::DBFuncs::enable_keys('streets');
}

1;
