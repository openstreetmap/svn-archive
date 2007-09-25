# Import Data from http://openstreetmap.org/

use strict;
use warnings;

use Storable ();

package Geo::Gpsdrive::FON;
use strict;
use warnings;

use IO::File;
use File::Path;
use Geo::Gpsdrive::DBFuncs;
use Geo::Gpsdrive::Utils;
use Geo::OSM::Planet;
use Data::Dumper;
use XML::Parser;
use XML::Simple;
use Geo::Filter::Area;
use Geo::OSM::Planet;
use Utils::Debug;
use Utils::File;
use Utils::LWP::Utils;
use Utils::Math;
use XML::Simple;

my $SOURCE_FON_ID;
my $WLAN_TYPE_ID;

# ******************************************************************
sub get_source_id($){
    my $country = shift;

    my $source_fon_name = "Fon Accesspoints $country";
    $SOURCE_FON_ID = Geo::Gpsdrive::DBFuncs::source_name2id($source_fon_name);

    unless ( $SOURCE_FON_ID ) {
	my $source_hash = {
	    'source.url'     => "http://www.fon.com/",
	    'source.name'    => $source_fon_name ,
	    'source.comment' => 'Accesspoints from Fonero' ,
	    'source.licence' => ""
	    };
	Geo::Gpsdrive::DBFuncs::insert_hash("source", $source_hash);
	$SOURCE_FON_ID = Geo::Gpsdrive::DBFuncs::source_name2id($source_fon_name);
    }
    die "Cannot get Source ID for $source_fon_name\n" 
	unless $SOURCE_FON_ID;
    return $SOURCE_FON_ID;
}

# *****************************************************************************
sub read_fon_file($) {
    my $filename = shift;

    my $fon_data = {};

    my $fh = data_open($filename);
    if ( ! ref($filename) =~ m/IO::File/ ) {
	print STDERR "Parsing file: $filename\n" if $DEBUG;
    }
    return $fon_data unless $fh;

    $fon_data = XMLin($fh, 
		      Forcearray => ['name'],
		      #KeyAttr    => ['name'],
		      #GroupTags => { fon => 'name'},
		      );
#    print Dumper(\$fon_data);
    my $wpts = $fon_data->{wpt};
#    print Dumper(\$wpts);
    for my $wpt ( @{$wpts} ) {
	$wpt->{name}=	$wpt->{name}->[0];
	#print Dumper(\$wpt);
    }
    return $wpts;
}

# *****************************************************************************

sub write_named_point($$) {
    my $node = shift;
    my $source_id = shift;

    print Dumper(\$node);
    
    return unless defined $node;
    my $unknown_keys='';
    my $values;
    $values->{'wlan.source_id'} = $source_id;
    $values->{'wlan.wlan_type_id'} = $WLAN_TYPE_ID;
    $values->{'wlan.name'}      = $node->{name}||'Unknown FON WLAN';
    $values->{'wlan.lat'}       = $node->{lat};
    $values->{'wlan.lon'}       = $node->{lon};
    $values->{'wlan.macaddr'}   = "00000";
    $values->{'wlan.essid'}     = "FON_$node->{name}";
    $values->{'wlan.nettype'}   = 2;
    $values->{'wlan.wep'}       = 0;
    $values->{'wlan.cloaked'}   = 0;
    $values->{'wlan.comment'}   = $node->{desc};
    print Dumper(\$values);
    Geo::Gpsdrive::DBFuncs::add_wlan($values);
}
# ******************************************************************

sub import_fon_points($$){
    my $wpts = shift;
    my $source_id = shift;

    for my $wpt ( @{$wpts} ) {
	write_named_point($wpt,$source_id);
    }
}

# ******************************************************************

sub import_fon_file(@){
    my $filename = shift;
    my $country  = shift;

    print "\nImport FON Data from $filename\n";

    my $source_id   = get_source_id($country);
    
    if ( -s $filename ) {
	my $wpts = read_fon_file($filename);
	import_fon_points( $wpts,$source_id);
    } else {
	die "FON::import_Data: Cannot find File '$filename'\n";
	print "Read FON Data\n";
    };
    
    print "\nImport of FON Data from $filename FINISHED\n";
}

# ******************************************************************

sub import_Data(@){
    my @countries = @_;

    printf "\nDownload and Import FON Accesspoint Positions for %s\n",
	join(",",@countries);

    my $fon_dir="$main::CONFIG_DIR/MIRROR/fon";
    unless ( -d $fon_dir ) {
	print "Creating Directory $fon_dir\n";
	mkpath $fon_dir
	    or die "Cannot create Directory $fon_dir:$!\n";
    }

    if ( ! $no_mirror ) {
	for my $country ( @countries ) {
	    $country =uc($country);
	    #my $url="http://www.fon.com/de/maps/$country.gpx";
	    my $url="http://maps.fon.com/pois/download.php?country=$country&format=gpx&exp=oo";
	    print "Mirror $url\n";
	    my $mirror = mirror_file($url ,"$fon_dir/FON_$country.gpx");
	    die "Mirror for '$url' was not successfull\n" unless $mirror;
	}
    }
	
    $WLAN_TYPE_ID=poi_type_name2id("wlan.pay.fon");
    disable_keys('poi');
    for my $country ( @countries ) {
	my $source_id   = get_source_id($country);
	Geo::Gpsdrive::DBFuncs::delete_all_from_source($source_id);
	import_fon_file("$fon_dir/FON_$country.gpx",$country);
    }
    enable_keys('poi');
}

1;
