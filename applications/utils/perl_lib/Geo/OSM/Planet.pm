##################################################################
package Geo::OSM::Planet;
##################################################################

use Exporter;
@ISA = qw( Exporter );
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
@EXPORT = qw( mirror_planet
	      osm_dir
	      planet_dir
	      UTF8sanitize
	      estimated_max_id
	      estimated_max_count
	      );

use strict;
use warnings;

use HTTP::Request;
use File::Basename;
use File::Copy;
use File::Path;
use File::Slurp;
use Getopt::Long;
use HTTP::Request;
use Storable ();
use Data::Dumper;

use Utils::File;
use Utils::Debug;
use Utils::LWP::Utils;


# As of planet-061220
my $estimations = {
    'way' => {
	'count' => 349551,
	'max_id' => 4080938,
    },
    'elem' => {
	'count' => 82792930,
	'max_id' => 24620105,
    },
    'seg' => {
	'count' => 3044669,
	'max_id' => 21402132,
    },
    'segment' => {
	'count' => 7725721,
	'max_id' => 17716919,
    },
    'tag' => {
	'count' => 82790717,
	'max_id' => 1,
    },
    'node' => {
	'count' => 7501130,
	'max_id' => 21650908,
    },
    'line' => {
	'count' => 31736573,
	'max_id' => 24620105,
    }
};

# ------------------------------------------------------------------
# This routine estimates the maximum id for way,elem,seg,... 
# The values are taken from older planet.osm Files
# So they mostly are a little bit to low
# ARGS: 
#   $type: line|way|tag|...
# RETURNS:
#   $result: number of estimated max_id
sub estimated_max_id($){
    my $type= shift;
    unless ( defined ($estimations->{$type}->{max_id})) {
	warn("\n estimated_max_id($type): unknown Tag-Type\n");
	return 0;
    };
    return $estimations->{$type}->{max_id};
}

# ------------------------------------------------------------------
# This routine estimates the maximim number of elements for way,elem,seg,... 
# The values are taken from older planet.osm Files
# So they mostly are a little bit to low
# ARGS: 
#   $type: line|way|tag|...
# RETURNS:
#   $result: number of estimated elements
sub estimated_max_count($){
    my $type= shift;
    unless ( defined ($estimations->{$type}->{count})) {
	warn("\n estimated_max_id($type): unknown Tag-Type\n");
	return 0;
    };
    return $estimations->{$type}->{count};
}

# ------------------------------------------------------------------
# returns the osm main directory for holding data
sub osm_dir() {
    # For later these are the defaults
    # edpending on where we can read/write
    #  ~/osm
    # /var/data/osm
    my $dir;

    my $home = $ENV{HOME};
    unless ( $home ) {
	$home = `whoami`;
	chomp $home;
	$home = "/home/$home";
    }
    
    $dir = "$home/osm";
    return $dir;
}

# ------------------------------------------------------------------
# Returns the directory where the planet.osm files will be found
sub planet_dir() {
    my $dir = osm_dir();
    $dir = "$dir/planet";
    return $dir;
}


# ------------------------------------------------------------------
# mirror the newest planet.osm File to
#  ~/osm/planet/planet.osm.7z
# the file is Sanitized afterwards  and the resulting 
# Filename is returned
sub mirror_planet(){
    my $planet_server="http://planet.openstreetmap.org";
    my $url = "$planet_server";

    my $mirror_dir=planet_dir();
    mkdir_if_needed( $mirror_dir );
    
    my $current_file;
    if ( !$Utils::LWP::Utils::NO_MIRROR ) {
	# Get Index.html of Planet.osm.org
	my $apache_sort_hy_date="?C=M;O=D";
	my $index_file="$mirror_dir/planet_index.html";
	my $result = mirror_file("$url/$apache_sort_hy_date",$index_file);
	if ( $result ) {
	    my $index_content = read_file( $index_file ) ;

	    # Get the current planet.osm File
	    my @all_files = ($index_content =~ m/(planet-\d\d\d\d\d\d.osm.7z)/g);
	    ( $current_file ) = grep { $_ !~ m/planet-061008/ } @all_files;
	    if ( $current_file ) {
		$url .= "/$current_file";
		$current_file = "$mirror_dir/$current_file";
		print STDERR "Mirror OSM Data from $url\n" if $VERBOSE || $DEBUG;
		$result = mirror_file($url,$current_file);
		#return undef unless $result;
		
		if ( $result == 1 ) { # Modified
		    # Link the current File to planet.osm(.7z)
		    # symlink
		}
	    }
	}
    }

    my @files= sort { $b cmp $a}  
    grep { $_ !~ m/planet-061008/ } 
    glob("$mirror_dir/planet-*.osm.7z");
    if ( $DEBUG) {
	print STDERR "Existing Files: \n\t".join("\n\t",@files)."\n";
    }
    $current_file = $files[0];
    
    if ( $DEBUG) {
	print STDERR "Choosen File: $current_file\n";
    }
    
    return undef unless $current_file;

    $current_file = UTF8sanitize($current_file);

    if ( $DEBUG >2 || $VERBOSE>3) {
	print STDERR "Sanitized File: $current_file\n";
    }

    my ($unpacked_file) = ($current_file=~ m/(.*\.osm)/);
    $current_file = $unpacked_file
	unless file_needs_re_generation($current_file,$unpacked_file);

    print STDERR "Mirror done, using '$current_file'\n" if $VERBOSE>1 || $DEBUG>1;
    return $current_file;
}

# ------------------------------------------------------------------
# creates a seconf file with a sanitized Version of planet.osm
# the resulting file can be found at
#    ~/osm/planet/planet-07XXXX-a.osm.7z
# If a recent enought Version is found in ~/osm/planet/
# nothing is done, but the filename of the file is returned
# if the routine finds an uncompressed up to date Version 
#   ~/osm/planet/planet-07XXXX-a.osm
# this Filename is returned.
sub UTF8sanitize($){
    my $filename = shift;
    if ( $DEBUG) {
	print STDERR "UTF8sanitize($filename)\n";
    }
    my $start_time=time();

    # the newer Files do not need to be sanitized
    my ($file_date) = ($filename =~ m/planet-(\d+)/ );
    return $filename
	if ($file_date >= 061205) && ( $file_date < 061213);

    my $filename_new= $filename;
    $filename_new =~ s/\.osm/-a.osm/;
    my $filename_new_check=newest_unpacked_filename($filename_new);

    # check if planet-070101-a.osm[.7z] is newer than  planet-070101.osm.7z
    return $filename_new_check
	unless file_needs_re_generation($filename,$filename_new_check);

    # We have to create a new one
    print STDERR "UTF8 Sanitize $filename ... \n";
    # Uggly Hack, but for now it works
    my $UTF8sanitizer=`which UTF8sanitizer`;
    chomp $UTF8sanitizer;
    unless ( -x $UTF8sanitizer ) {
	$UTF8sanitizer=find_file_in_perl_path('../planet.osm/C/UTF8sanitizer');
    }
    die "Sanitizer not found\n" unless -x $UTF8sanitizer;
    print STDERR "Sanitizer found at '$UTF8sanitizer'\n" if $DEBUG;

    print STDERR "     this may take some time ... \n";
    my $result = `7z -so -si <$filename | $UTF8sanitizer  | bzip2 >$filename_new.part`;
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
    print "now we have a sanitized $filename_new\n" if $DEBUG || $VERBOSE;
    return $filename_new;
}

# ------------------------------------------------------------------
# find a file in the current Perl Search path. For now this was the 
# easiest solution to find programms like UTF8Sanitize
# ARGS: relative filename (relative to @INC-path
# RETURNS: Absolute path to file
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

# ------------------------------------------------------------------
1;

=head1 NAME

Geo::OSM::Planet

=head1 COPYRIGHT

Copyright 2006, J�rg Ostertag

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

J�rg Ostertag (planet-count-for-openstreetmap@ostertag.name)

=head1 SEE ALSO

http://www.openstreetmap.org/

=cut
