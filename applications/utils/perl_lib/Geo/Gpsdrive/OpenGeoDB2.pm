# Import Data from Open GEO DB to geoinfo.poi
#
# $Log$
# Revision 1.2  2006/02/13 23:29:45  tweety
# get actual Version of OpenGeodb
#
# Revision 1.1  2005/10/11 08:28:35  tweety
# gpsdrive:
# - add Tracks(MySql) displaying
# - reindent files modified
# - Fix setting of Color for Grid
# - poi Text is different in size depending on Number of POIs shown on
#   screen
#
# geoinfo:
#  - get Proxy settings from Environment
#  - create tracks Table in Database and fill it
#    this separates Street Data from Track Data
#  - make geoinfo.pl download also Opengeodb Version 2
#  - add some poi-types
#  - Split off Filling DB with example Data
#  - extract some more Funtionality to Procedures
#  - Add some Example POI for Kirchheim(Munich) Area
#  - Adjust some Output for what is done at the moment
#  - Add more delayed index generations 'disable/enable key'
#  - If LANG=*de_DE* then only impert europe with --all option
#  - WDB will import more than one country if you wish
#  - add more things to be done with the --all option
#
# Revision 1.1  2005/08/15 13:54:22  tweety
# move scripts/POI --> scripts/Geo/Gpsdrive to reflect final Structure and make debugging easier
#
# Revision 1.12  2005/08/09 01:08:30  tweety
# Twist and bend in the Makefiles to install the DataDirectory more apropriate
# move the perl Functions to Geo::Gpsdrive in /usr/share/perl5/Geo/Gpsdrive/POI
# adapt icons.txt loading according to these directories
#
# Revision 1.11  2005/05/10 05:28:49  tweety
# type in disable_keys
#
# Revision 1.10  2005/05/01 13:49:36  tweety
# Added more Icons
# Moved filling with defaults to DB_Defaults.pm
# Added some more default POI Types
# Added icons.html to see which icons are used
# Added more Comments
# Reformating Makefiles
# Added new options for importing from way*.txt and adding defaults
# Added more source_id and type_id
#
# Revision 1.9  2005/04/13 19:58:30  tweety
# renew indentation to 4 spaces + tabstop=8
#
# Revision 1.8  2005/04/10 00:15:58  tweety
# changed primary language for poi-type generation to english
# added translation for POI-types
# added some icons classifications to poi-types
# added LOG: Entry for CVS to some *.pm Files
#

package Geo::Gpsdrive::OpenGeoDB2;

use strict;
use warnings;

use IO::File;
use File::Path;

use Geo::Gpsdrive::DBFuncs;
use Geo::Gpsdrive::Utils;
use Geo::Gpsdrive::Gps;

#############################################################################
# Args: 
#    $filename : Filename to read 
#############################################################################
sub read_open_geo_db2($){
    my $full_filename = shift;

    print "Reading open geo db: $full_filename\n";
    my $fh = IO::File->new("<$full_filename");
    $fh or die ("read_open_geo_db: Cannot open $full_filename:$!\n");

    my $source = "open geo db";
    delete_all_from_source($source);
    my $source_id = Geo::Gpsdrive::DBFuncs::source_name2id($source);

    unless ( $source_id ) {
	my $source_hash = {
	    'source.url'     => "http://ovh.dl.sourceforge.net/".
		"sourceforge/geoclassphp/opengeodb-0.1.3-txt.tar.gz",
		'source.name'    => $source ,
		'source.comment' => '' ,
		'source.licence' => ""
	    };
	Geo::Gpsdrive::DBFuncs::insert_hash("source", $source_hash);
	$source_id = Geo::Gpsdrive::DBFuncs::source_name2id($source);
    }

    my @columns;
    @columns = qw( primarykey 
		   address.state address.bundesland address.regierungsbezirk 
		   address.landkreis address.verwaltungszusammenschluss
		   address.ort   address.ortsteil address.gemeindeteil
		   poi.lat poi.lon
		   poi.autokennzeichen
		   address.plz);
    my $lines_count_file =0;
    debug( "  ". join("\t",@columns));
    while ( my $line = $fh->getline() ) {
	$lines_count_file ++;
	$line =~ s/[\t\r\n\s]*$//g;;
#	 print "line: '$line'\n";
	if ( $line =~ m/^$/ ) {
	} elsif ( $line =~ m/^\#/ ) {
	} else {
	    die "Spalten nicht definiert" unless @columns;
	    
#	    print "WP: $line\n";
	    my @values = split(/\s*\;\s*/,$line);
	    #print Dumper(\@values);
	    my $values;
	    for my $i ( 0 .. scalar @columns -1 ) {
		$values->{$columns[$i]} = $values[$i];
		$values->{'poi.comment'} .= "$values[$i]\n" if $i>2;
	    }

	    ############################################
	    # Set Default Proximity 
	    $values->{'poi.proximity'} ||= "1000 m";

	    $values->{'poi.symbol'} ||= "City";


	    my $first_in_a_row=1;
	    for my $plz ( split(',',$values->{'address.plz'})) {
		#	    print Dumper($values);
		my $wp_name = '';
		$wp_name .= "$values->{'address.state'}-";
		$wp_name .= $plz;
		#	    $wp_name .= "_$values->{'poi.primarykey'}";
		#	    $wp_name .= "_$values->{'address.regierungsbezirk'}";
		#	    $wp_name .= "_$values->{'address.landkreis'}";
		#	    $wp_name .= "_$values->{'address.verwaltungszusammenschluss'}";
		$wp_name .= " $values->{'address.ort'}\n";
		$wp_name .= " $values->{'address.bundesland'}";
		$wp_name .= " $values->{'address.ortsteil'}";
		$wp_name .= " $values->{'address.gemeindeteil'}";
		$values->{'poi.name'}=$wp_name;
		if (  $plz =~ m/000$/ ) {
		    print "$values->{'address.state'}-$plz $values->{'address.ort'}\n";
		    $values->{'poi.scale_max'} = 1000000000;
		    $values->{'poi.proximity'} = "10000m";
		} elsif ( $values->{'address.ortsteil'}         eq "-" && 
			  $values->{'address.gemeindeteil'}     eq "-" &&
			  $values->{'address.regierungsbezirk'} eq "-" &&
			  $values->{'address.verwaltungszusammenschluss'} eq "-" &&
			  $first_in_a_row
			  ) {
		    debug( "$values->{'address.state'}-$plz :". join("\t",@values));
		    $values->{'poi.scale_max'} = 100000000;
		    $values->{'poi.proximity'} = "5000m";
		    $first_in_a_row=0;
		} elsif ( $plz =~ m/00$/ ) {
		    $values->{'poi.scale_max'} = 5000000;
		    $values->{'poi.proximity'} = "5000m";
		} elsif (  $plz =~ m/0$/ ) {
		    $values->{'poi.scale_max'} = 1000000;
		    $values->{'poi.proximity'} = "1000m";
		} else {
		    $values->{'poi.scale_max'} = 100000;
		    $values->{'poi.proximity'} = "300m";
		}
		$values->{'poi.scale_min'} = 0;
		
		$values->{'poi.name'}.=$values->{'poi.scale_max'};
		unless ( defined($values->{'poi.lat'}) ) {
		    print "Undefined lat".Dumper(\$values);
		}
		unless ( defined($values->{'poi.lon'}) ) {
		    print "Undefined lon".Dumper(\$values);
		}

		$values->{'poi.source_id'}=$source_id;
		
		correct_lat_lon($values);
		Geo::Gpsdrive::DBFuncs::add_poi($values);
		#print "Values:".Dumper(\$values);
	    }
	}
    }
}

########################################################################################
# Get and Unpack opengeodb
# http://www.opengeodb.de/download/
########################################################################################
sub import_Data() {
    my $mirror_dir="$main::MIRROR_DIR/opengeodb";
    my $unpack_dir="$main::UNPACK_DIR/opengeodb";

    print "\nDownload an import OpenGeoDB2 Data\n";

    -d $mirror_dir or mkpath $mirror_dir
	or die "Cannot create Directory $mirror_dir:$!\n";
    
    -d $unpack_dir or mkpath $unpack_dir
	or die "Cannot create Directory $unpack_dir:$!\n";
    
    # download
    my $file_name = "opengeodb-0.2.4c-UTF8-mysql.zip";
    my $url = "http://dl.sourceforge.net/sourceforge/opengeodb/$file_name";
    print "Mirror $url\n";
    my $mirror = mirror_file($url,"$mirror_dir/$file_name");
    print "Mirror: $mirror\n";

    # Unpack it
    print "Unpack\n";
    `(cd $unpack_dir/; unzip -o $mirror_dir/$file_name)`;

    print "Drop DB\n";
    `echo "drop database opengeodb;"|mysql -u$main::db_user -p$main::db_password`;
    print "Create DB\n";
    `echo 'CREATE DATABASE \`opengeodb\` ;'|mysql -u$main::db_user -p$main::db_password`;
    print "Insert into DB\n";
    `mysql -u$main::db_user -p$main::db_password opengeodb <$unpack_dir/opengeodb-0.2.4a-UTF8-mysql.sql`;

if (0){
    disable_keys('poi');

    for my $file_name ( glob("$unpack_dir/opengeodb*.txt") ) {
	my $out_file_name = "$main::CONFIG_DIR/way_opengeodb.txt";
	read_open_geo_db2($file_name);
    }

    enable_keys('poi');
    print "Download an import OpenGeoDB Data FINISHED\n";
}
    print "Download  OpenGeoDB2 Data FINISHED\n";

}

1;
