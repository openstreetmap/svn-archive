# Database Defaults for poi/streets Table for poi.pl

package Geo::Gpsdrive::DB_Examples;

use strict;
use warnings;

use POSIX qw(strftime);
use Time::Local;
use DBI;
use Data::Dumper;
use IO::File;

use Geo::Gpsdrive::DBFuncs;
use Geo::Gpsdrive::Utils;

my $example_source_name = "Examples";
my $example_source_id=0;

$|= 1;                          # Autoflush

BEGIN {
    use Exporter   ();
    our ($VERSION, @ISA, @EXPORT, @EXPORT_OK, %EXPORT_TAGS);

    # set the version for version checking
    $VERSION     = 1.00;
    # if using RCS/CVS, this may be preferred
    #$VERSION = sprintf "%d.%03d", q$Revision: 1190 $ =~ /(\d+)/g;

    @ISA         = qw(Exporter);
    @EXPORT = qw( );
    %EXPORT_TAGS = ( );
    @EXPORT_OK   = qw();

}
#our @EXPORT_OK;


sub min($$){
    my $a=shift;
    my $b=shift;
    return $a<$b?$a:$b;
}
sub max($$){
    my $a=shift;
    my $b=shift;
    return $a>$b?$a:$b;
}

# ------------------------------------------------------------------
# Guess the Street Type if we got a Streetname
sub street_name_2_id($) {
    my $street_name = shift;
    my $streets_type_id =0;
    if ( $street_name =~ m/^A/ ) {
	$streets_type_id = streets_type_name2id('Strassen.Autobahn');
    } elsif ( $street_name =~ m/^ST/ ) {
	$streets_type_id = streets_type_name2id('Strassen.Bundesstrasse');
    } elsif ( $street_name =~ m/^B/ ) {
	$streets_type_id = streets_type_name2id('Strassen.Bundesstrasse');
    }   else {
	$streets_type_id = streets_type_name2id('Strassen.Allgemein');
    };
    return $streets_type_id;
}

# -----------------------------------------------------------------------------
# Open Data File in predefined Directories
sub data_open($){
    my $file_name = shift;
    for my $path ( qw(
		      ../data/
		      data/
		      /usr/local/share/gpsdrive/
		      /usr/share/gpsdrive/
		      ) ) {
	my $file_with_path=$path.$file_name;
	if ( -s $file_with_path ) {
	    debug("Opening $file_with_path");
	    my $fh;
	    if ( $file_with_path =~ m/\.gz$/ ) {
		$fh = IO::File->new("gzip -dc $file_with_path|")
		    or die("cannot open $file_with_path: $!");
	    } else {
		$fh = IO::File->new("<$file_with_path")
		    or die("cannot open $file_with_path: $!");
	    }
	    return $fh;
	}
    }
    die "cannot Find $file_name";
}
	
# -----------------------------------------------------------------------------
# Insert Example Waypoints
sub fill_example_waypoints($) {
    my $file_name = shift;
    my $fh = data_open($file_name);

    die "No Example Source ID defined\n" unless $example_source_id;

    my $error=0;
    while ( my $line = $fh->getline() ) {
	chomp $line;
	my ( $name, $lat, $lon, $type_name ) = split(/\s+/,$line);
	
	debug("\n\nInsert Waypoint '$name' '$lat','$lon' '$type_name'");

	die "Fehler in lat,lon: '$lat','$lon' for $name,'$type_name'\n"
	    unless ( $lat =~ m/^\s*\d+\.\d+\s*$/  &&
		     $lon =~ m/^\s*\d+\.\d+\s*$/ );

	$type_name =~ s/_/ /g;
	my $type_id = poi_type_name2id($type_name);
	unless ( $type_id ){
	    $error++;
	    die "--------------- Type for '$type_name' not found \n";
	    next;
	}
	
	#for  my $t ( qw(waypoints poi)) {
	{
	    my $t = 'poi';
	    my $loc  = { "$t.name" => $name, "$t.lat" => $lat, "$t.lon" => $lon};
	    my $wp_defaults = { "$t.wep"         => 0 ,
				"$t.nettype"     => '',
				"$t.scale_min"   => 1,
				"$t.scale_max"   => 20000,
				"$t.source_id"   => $example_source_id,
				"$t.poi_type_id" => $type_id,
				"$t.last_modified" => time(),
			    };
	    #print "Sample WP:$t	'$name'\n";
	    Geo::Gpsdrive::DBFuncs::insert_hash($t, $wp_defaults, $loc );
	}
    }
}

# ------------------------------------------------------------------
sub fill_example_cities($){
    my $file_name = shift;
    my $fh = data_open($file_name);

    # Insert Example Cities
    my $type_name = "places.city";
    my $type_id = poi_type_name2id($type_name);
    die "Type for '$type_name' not found \n" unless $type_id;

    die "No Example Source ID defined\n" unless $example_source_id;

#    for  my $t ( qw(waypoints poi)) {
    {
	my $t = 'poi';
	my $wp_defaults = { "$t.wep"         => 0 ,
			    "$t.nettype"     => '',
			    "$t.scale_min"   => 1,
			    "$t.scale_max"   => 1000000000,
			    "$t.type"        => 'City',
			    "$t.source_id"   => $example_source_id,
			    "$t.poi_type_id" => $type_id,
			    "$t.last_modified" => time(),
			};
	my $type_query = $t  eq "poi" 
	    ? " poi_type_id = '$type_id'" 
	    : " type = '". $wp_defaults->{"$t.type"} ."'";
	
	while ( my $line = $fh->getline() ) {
	    chomp $line;
	    my ( $lat, $lon, $name ) = split(/\s+/,$line);
	    my $loc;
	    $loc->{"$t.name"} = $loc->{name} = $name;
	    $loc->{"$t.lat"}  = $loc->{lat}  = $lat;
	    $loc->{"$t.lon"}  = $loc->{lon}  = $lon;

	    #my $delete_query=sprintf("DELETE FROM $t ".
	    #"WHERE name = '%s' AND $type_query",
	    #$loc->{"$t.name"});
	    #Geo::Gpsdrive::DBFuncs::db_exec( $delete_query);
	    Geo::Gpsdrive::DBFuncs::insert_hash($t, $wp_defaults, $loc );
	}
    }
} # of fill_example_cities()



# -----------------------------------------------------------------------------
sub fill_example_streets($) { # Insert Street Sample
    my $file_name = shift;
    my $fh = data_open($file_name);
    my $street_name='';
    my $multi_segment={};
    my $lat1=1003;
    my $lat2=1003;
    my $lon1=1003;
    my $lon2=1003;
    my $max_allowed_dist=10; # Distance in degrees to warn for more precise Data
    my $max_dist =0;
    my $line_number=0;
    while ( 1 ) {
	my $line = $fh->getline();
	$line_number++;
	chomp $line;
	if ( $line =~ m/^\S+/ || $fh->eof() ) {
	    if ( $street_name ) { # Es ist eine Strasse gespeichert
		#print Dumper($multi_segment);
		debug("Importing Street: $street_name");
		# street_segments_add_from_segment_array($multi_segment);
		print "Max Dist: $max_dist	$street_name\n" 
		    if $debug;
		$max_dist=0;
		street_segments_add($multi_segment);
	    }

	    last 
		if $fh->eof();
	    
	    # Neue Strasse anfangen
	    $street_name = $line;
	    $multi_segment={};
	    $multi_segment->{'source_id'} = $example_source_id;
	    $multi_segment->{'streets_type_id'} = '';
	    if ( $street_name =~ m/^\#/ ) { # Komments
		next;
	    }

	    $multi_segment->{'streets_type_id'} = street_name_2_id($street_name);
	    if ( $street_name =~ m/^A/ ) {
		$max_allowed_dist = 0.4;
	    } elsif ( $street_name =~ m/^ST/ ) {
		$max_allowed_dist = 0.05;
	    } elsif ( $street_name =~ m/^B/ ) {
		$max_allowed_dist = 0.05;
	    }   else {
		$max_allowed_dist = 0.05;
	    }
	    $multi_segment->{'name'}            = $street_name;
	    $multi_segment->{'scale_min'}       = 1;
	    $multi_segment->{'scale_max'}       = 50000000;
	    $multi_segment->{'segments'} =[];
	    
	} elsif ( $line =~ m/^\s*$/ ) { # Empty Line
	    next;
	} else {
#	} elsif ( $line =~ m/^[\t\s]+[\d\+\-\.\,]+[\t\s]+[\d\+\-\.\,]+[\t\s]+$/) {
	    my $indent;
	    ($indent,$lat2,$lon2) = split(/\s+/,$line);
	    if ( @{$multi_segment->{'segments'}}>0 ) {
		my $d_lat=abs($lat1-$lat2);
		my $d_lon=abs($lon1-$lon2);
		my $dist = $d_lat+$d_lon;
		$max_dist = $dist if $dist>$max_dist;
		if ( 
		     $street_name !~ m/grober Verlauf/ && 
		     $dist > $max_allowed_dist 
		     ) {
		    print "Splitting Track $street_name\n";
		    printf( "Dist: %.4f	($lat1,$lon1) -> ($lat2,lon2) $street_name [$file_name:$line_number]\n",$dist);
		    street_segments_add($multi_segment);
		    $multi_segment->{'segments'} =[];
		    
		}
	    }
	    $lat1 = $lat2;
	    $lon1 = $lon2;
	    push(@{$multi_segment->{'segments'}},[$lat2,$lon2]);
#	} else {
#	    warn "Error in File: $file_name Line: '$line'";
	}
    }
}; # of fill_example_streets()

# ------------------------------------------------------------------
sub fill_examples(){

    $example_source_id = source_name2id($example_source_name);
    die "Unknown Source ID: $example_source_name\n" unless $example_source_id;

    delete_all_from_source($example_source_name);

    print "\nCreate Examples ...(source_id=$example_source_id)\n";

    fill_example_cities("poi/cities.txt");
    fill_example_cities("poi/cities_germany.txt");
    fill_example_waypoints("poi/germany.txt");
#    fill_example_streets("streets/Autobahnen.txt");
#    fill_example_streets("streets/Streets.txt");
#    fill_example_streets("streets/Streets_au.txt");

    print "Create Examples completed\n";
}


1;
