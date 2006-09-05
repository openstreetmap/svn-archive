##################################################################
package Geo::OSM::Planet;
##################################################################

use Exporter;
@ISA = qw( Exporter );
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
@EXPORT = qw( mirror_planet
	      osm_dir
	      UTF8sanitize
	      );

use strict;
use warnings;

use Utils::File;
use HTTP::Request;

use File::Basename;
use File::Copy;
use File::Path;
use Getopt::Long;
use HTTP::Request;
use Storable ();
use Utils::LWP::Utils;
use File::Slurp;
use Utils::Debug;
use Utils::Timing;


sub osm_dir() {
    # For later these are the defaults
    # edpending on where we can read/write
    #  ~/osm
    # /var/data/osm
    return "$ENV{HOME}/osm/planet";
}


# *****************************************************************************
# mirror the newest planet.osm File to ~/osm/planet/planet.osm.bz2
sub mirror_planet(){

    my $planet_server="http://planet.openstreetmap.org";
    my $url = "$planet_server";

    my $mirror_dir=osm_dir();
    mkdir_if_needed( $mirror_dir );
    
    my $current_file;
    if ( $Utils::LWP::Utils::NO_MIRROR ) {
	my @files= sort { $b cmp $a}  glob("$mirror_dir/planet-*.osm.bz2");
	if ( $DEBUG) {
	    print STDERR "Existing Files: \n\t".join("\n\t",@files)."\n";
	}
	$current_file = $files[0];
    } else {
	# Get Index.html of Planet.osm.org
	my $apache_sort_hy_date="/?C=M;O=D";
	my $index_file="$mirror_dir/planet_index.html";
	my $result = mirror_file("$url/$apache_sort_hy_date",$index_file);
	return undef unless $result;
	my $index_content = read_file( $index_file ) ;

	# Get the current planet.osm File
	( $current_file ) = ($index_content =~ m/(planet-\d\d\d\d\d\d.osm.bz2)/);
	return undef unless $current_file;
	$url .= "/$current_file";
	$current_file = "$mirror_dir/$current_file";
	print STDERR "\nMirror OSM Data from $url\n" if $VERBOSE || $DEBUG;
	$result = mirror_file($url,$current_file);
	return undef unless $result;

	if ( $result == 1 ) { # Modified
	    # Link the current File to planet.osm(.bz2)
	    # symlink
	}
    }

    if ( $DEBUG) {
	print STDERR "Choosen File: $current_file\n";
    }

    return undef unless $current_file;

    $current_file = UTF8sanitize($current_file);

    if ( $DEBUG) {
	print STDERR "Sanitized File: $current_file\n";
    }

    return $current_file;

}

sub UTF8sanitize($){
    my $filename = shift;

    my $start_time=time();

    my $filename_new= $filename;
    $filename_new =~ s/\.osm/-a.osm/;

    return $filename_new 
	unless file_needs_re_generation($filename,$filename_new);

    print STDERR "UTF8 Sanitize $filename ... \n";
    # Uggly Hack, but for now it works
    my $UTF8sanitizer=find_file_in_perl_path('../planet.osm/C/UTF8sanitizer');
    die "Sanitizer not found\n" unless -x $UTF8sanitizer;

    print STDERR "     this may take some time ... \n";
    my $result = `bzip2 -dc $filename | $UTF8sanitizer  | bzip2 >$filename_new.part`;
    print $result if $DEBUG || $VERBOSE;
  
    print "Sanitized $filename " if $DEBUG || $VERBOSE;
	print_time($start_time);

    my $file_size     = -s "$filename";
    my $file_size_new = -s "$filename_new.part";
    if ( $file_size_new < ($file_size*0.9) ) {
	die "File Sanitize seems not successfull.\n".
	    "Original Size $file_size\n".
	    "Sanitized Size $file_size_new\n";
    }
    rename "$filename_new.part","$filename_new";
    if ( ! -s $filename_new ) {
	die "Cannot sanitize $filename\n";
    }
    print "now we have a sanitizes $filename_new\n" if $DEBUG || $VERBOSE;
    return $filename_new;
}

sub find_file_in_perl_path($){
    my $file = shift;

    my $found_file = '';
    for my $path ( @INC ) {
	my $filename = "$path/$file";
	print "find_file_in_perl_path: looking in '$filename'\n" if $DEBUG>2;
	if ( -s $filename){
	    $found_file = $filename;
	    last;
	};
    }
    
    print "find_file_in_perl_path($file): --> $found_file\n" if $DEBUG;
    return $found_file;
}
1;
