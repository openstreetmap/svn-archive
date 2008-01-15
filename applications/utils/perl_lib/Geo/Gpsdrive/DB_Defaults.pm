# Database Defaults for poi/streets Table for poi.pl

package Geo::Gpsdrive::DB_Defaults;

use strict;
use warnings;

use POSIX qw(strftime);
use Time::Local;
use DBI;
use Geo::Gpsdrive::Utils;
use Data::Dumper;
use IO::File;
use Geo::Gpsdrive::DBFuncs;
use XML::Twig;

$|= 1;                          # Autoflush

BEGIN {
    use Exporter   ();
    our ($VERSION, @ISA, @EXPORT, @EXPORT_OK, %EXPORT_TAGS);

    # set the version for version checking
    $VERSION     = 1.00;
    # if using RCS/CVS, this may be preferred
    #$VERSION = sprintf "%d.%03d", q$Revision: 1254 $ =~ /(\d+)/g;

    @ISA         = qw(Exporter);
    @EXPORT = qw( );
    %EXPORT_TAGS = ( );
    @EXPORT_OK   = qw();

}


# -----------------------------------------------------------------------------
# Fill poi_type database
sub fill_default_poi_types {
    our $lang = $main::lang || 'de';
    my $i=1;
    my $used_icons ={};
    my $poi_type_id=20;

    # for debug purpose
    Geo::Gpsdrive::DBFuncs::db_exec("TRUNCATE TABLE `poi_type`;");

    my $unused_icon ={};
    my $existing_icon ={};

    my $icon_file='../data/map-icons/icons.xml';
    $icon_file = '../share/map-icons/icons.xml'         unless -s $icon_file;
    $icon_file = '/usr/local/share/map-icons/icons.xml' unless -s $icon_file;
    $icon_file = '/usr/share/icons/map-icons/icons.xml'       unless -s $icon_file;
    $icon_file = '/usr/share/map-icons/icons.xml'       unless -s $icon_file;
    $icon_file = '/opt/gpsdrive/icons.xml'              unless -s $icon_file;
    die "no Icon File found" unless -s $icon_file;

    our $title = ''; our $title_en = '';
    our $description = ''; our $description_en = '';

    # parse icon file
    #
    my $twig= new XML::Twig
    (
       TwigHandlers => { rule        => \&sub_poi,
                         title       => \&sub_title,
                         description => \&sub_desc }
    );
    $twig->parsefile( "$icon_file");
    my $rules= $twig->root;

    $twig->purge;

    sub sub_poi
    {
      my ($twig, $poi_elm) = @_;
      if ($poi_elm->first_child('condition')->att('k') eq 'poi')
      {
        my $poi_type_id =
          $poi_elm->first_child('geoinfo')->first_child('poi_type_id')->text;
        my $name = $poi_elm->first_child('geoinfo')->first_child('name')->text;
        my $scale_min = $poi_elm->first_child('scale_min')->text;
        my $scale_max = $poi_elm->first_child('scale_max')->text;
        $title = $title_en unless ($title);
	$description = $description_en unless ($description);

	Geo::Gpsdrive::DBFuncs::db_exec(
	  "DELETE FROM `poi_type` WHERE poi_type_id = $poi_type_id ;");
	Geo::Gpsdrive::DBFuncs::db_exec(
	  "INSERT INTO `poi_type` ".
          "(poi_type_id, name, scale_min, scale_max, title, title_en, ".
	  "description, description_en) ".
	  "VALUES ($poi_type_id,'$name','$scale_min','$scale_max','$title',".
	  "'$title_en','$description','$description_en');") 
	    or die;
      }
      $title = ''; $title_en = '';
      $description = ''; $description_en = '';
    }

    sub sub_title
    {
      my ($twig, $title_elm) = @_;
      if ($title_elm->att('lang') eq 'en')
        { $title_en = $title_elm->text; }
      elsif ($title_elm->att('lang') eq $lang)
        { $title = $title_elm->text; }
    }

    sub sub_desc
    {
      my ($twig, $desc_elm) = @_;
      if ($desc_elm->att('lang') eq 'en')
        { $description_en = $desc_elm->text; }
      elsif ($desc_elm->att('lang') eq $lang)
        { $description = $desc_elm->text; }
    }
}

# -----------------------------------------------------------------------------
sub fill_default_sources() {   # Just some Default Sources

    my $default_licence =
      $main::default_licence || 'Creative Commons Attribution-ShareAlike 2.0';

    my @sources = (
      { source_id   => '1',
        name        => 'unknown',
        comment     => 'Unknown source or source not defined', 
        last_update => '2007-01-03',
        url         => 'http://www.gpsdrive.cc/',
        licence     => 'unknown'
      },
      { source_id   => '2',
        name        => 'way.txt',
        comment     => 'Data imported from way.txt', 
        last_update => '2007-01-03',
        url         => 'http://www.gpsdrive.cc/',
        licence     => 'unknown'
      },
      { source_id   => '3',
        name        => 'user',
	comment     => 'Data entered by the GPSDrive-User',
	last_update => '2007-01-23',
	url         => 'http://www.gpsdrive.cc/',
	licence     => $default_licence
      },
      { source_id   => '4',
        name        => 'OpenStreetMap.org',
        comment     => 'General Data imported from the OpenStreetMap Project', 
        last_update => '2007-01-03',
        url         => 'http://www.openstreetmap.org/',
        licence     => 'Creative Commons Attribution-ShareAlike 2.0'
      },
      { source_id   => '5',
        name        => 'groundspeak',
        comment     => 'Geocache data from Groundspeak', 
        last_update => '2007-01-30',
        url         => 'http://www.groundspeak.com/',
        licence     => 'unknown'
      },
      { source_id   => '6',
        name        => 'opencaching',
        comment     => 'Geocache data from Opencaching', 
        last_update => '2007-09-30',
        url         => 'http://www.opencaching.de/',
        licence     => 'unknown'
      },
      
      { source_id   => '7',
        name        => 'friendsd',
        comment     => 'Position received from friendsd server', 
        last_update => '2007-09-30',
        url         => 'http://friendsd.gpsdrive.de/',
        licence     => 'none'
      },
      { source_id   => '8',
        name        => 'fon',
        comment     => 'Access point data from FON', 
        last_update => '2007-09-30',
        url         => 'http://www.fon.com/',
        licence     => 'unknown'
      },
    );

    foreach (@sources) {
      Geo::Gpsdrive::DBFuncs::db_exec(
        "DELETE FROM `source` WHERE source_id = $$_{'source_id'};");
      Geo::Gpsdrive::DBFuncs::db_exec(
        "INSERT INTO `source` ".
          "(source_id, name, comment, last_update, url, licence) ".
	  "VALUES ($$_{'source_id'},'$$_{'name'}','$$_{'comment'}',".
	  "'$$_{'last_update'}','$$_{'url'}','$$_{'licence'}');") or die;
    }

}

# -----------------------------------------------------------------------------

sub fill_defaults(){
    print "Create Defaults ...\n";
    fill_default_poi_types();
    fill_default_sources();
    print "Create Defaults completed\n";
}


1;
