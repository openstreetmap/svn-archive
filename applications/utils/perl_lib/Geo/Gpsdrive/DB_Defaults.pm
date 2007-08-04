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
#our @EXPORT_OK;



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
        name        => 'osm',
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
        name        => 'osm.node',
        comment     => 'OpenStreetMap.org Node', 
        last_update => '2007-03-06',
        url         => 'http://www.openstreetmap.org/',
        licence     => 'Creative Commons Attribution-ShareAlike 2.0'
      },
      { source_id   => '7',
        name        => 'osm.way',
        comment     => 'OpenStreetMap.org Way', 
        last_update => '2007-03-06',
        url         => 'http://www.openstreetmap.org/',
        licence     => 'Creative Commons Attribution-ShareAlike 2.0'
      },
      { source_id   => '8',
        name        => 'osm.segment',
        comment     => 'OpenStreetMap.org Segment', 
        last_update => '2007-03-06',
        url         => 'http://www.openstreetmap.org/',
        licence     => 'Creative Commons Attribution-ShareAlike 2.0'
      },
      { source_id   => '9',
        name        => 'osm.tag',
        comment     => 'OpenStreetMap.org Tag', 
        last_update => '2007-03-06',
        url         => 'http://www.openstreetmap.org/',
        licence     => 'Creative Commons Attribution-ShareAlike 2.0'
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


# -------------------------------------------- NGA
    my $coutry2name;
    for my $k ( keys %{$Geo::Gpsdrive::NGA::name2country} ) {
	$coutry2name->{$Geo::Gpsdrive::NGA::name2country->{$k}} =$k;
    }

    for my $country  ( @Geo::Gpsdrive::NGA::countries ) {    
	my $name ="earth-info.nga.mil $country";

	Geo::Gpsdrive::DBFuncs::db_exec("DELETE FROM `source` WHERE source.name = '$name';");
	my $source_hash = {
	    'source.url'     => "http://www.evl.uic.edu/pape/data/WDB/WDB-text.tar.gz",
	    'source.name'    => $name,
	    'source.comment' => "GeoData for $coutry2name->{$country}($country)",
	    'source.licence' => ""
	    };
	Geo::Gpsdrive::DBFuncs::insert_hash("source", $source_hash);
    }

}

# -----------------------------------------------------------------------------
sub fill_default_street_types() {   # Fill streets_type database
    my $i=1;
    my $streets_type_id=0;

    # ------------ Entries for WDB
    for my $area ( qw(africa asia europe namer samer ) ) {
	for my $kind ( qw(bdy cil riv pby) ) {
	    for my $rank ( 1 .. 15 ) {
		my $name;
		my $color;
		my $color_bg  = "#000000";
		my $width     = 2;
		my $width_bg  = 0;
		my $scale_min =                             1;
		my $scale_max =                        100000;
		if ( $rank == 1 ) { $scale_max    = 100000000; $width=2 };
		if ( $rank == 2 ) { $scale_max    =   1000000; $width=2};
		if ( $rank == 3	) { $scale_max    =    100000; $width=3};
		if ( $rank == 4 ) { $scale_max    =    100000; $width=2};
		if ( $rank == 5 ) { $scale_max    =    100000; $width=1};
		$color = "#000000";
		if ( $kind eq "riv" ) { $color = "#0000FF"; $width=4; }; # riv  rivers
		if ( $kind eq "cil" ) { $color = "#0044FF";           }; # cil  coastlines, islands, and lakes
		if ( $kind eq "bdy" ) {	$color = "#001010";           }; # bdy  national boundaries
		if ( $kind eq "pby" ) {	$color = "#001010";           }; # bdy  national boundaries
		$name = "WDB $area $kind rank ${rank}";
		my $linetype='';
		$streets_type_id++;
		Geo::Gpsdrive::DBFuncs::db_exec("DELETE FROM `streets_type` WHERE streets_type_id = '$streets_type_id' ;");
		Geo::Gpsdrive::DBFuncs::db_exec
		    ("INSERT INTO `streets_type` ".
		     "        (streets_type_id, name,  color ,   color_bg,   width ,  width_bg ,  linetype ,  scale_min ,  scale_max )".
		     " VALUES ($i,            '$name','$color','$color_bg','$width','$width_bg','$linetype','$scale_min','$scale_max');");
		
		$i++;
	    }
	}
    }


    Geo::Gpsdrive::OSM::load_elemstyles();
    
    for my $entry  ( @{$Geo::Gpsdrive::OSM::ELEMSTYLES_RULES} ) {    
	next unless defined $entry->{line};
	#print Dumper(\$entry);
	$streets_type_id = $entry->{streets_type_id};
	my $color     = $entry->{line}->{colour} || "#110000";
	my $color_bg  = $entry->{line}->{colour_bg} || $color;
	my $width     = $entry->{line}->{width} || 1;
	my $width_bg  = $entry->{line}->{width_bg} || $width;
	my $scale_max = $entry->{scale_max} || 50000;
	my $name      = $entry->{condition}->{k}.":".$entry->{condition}->{v};
	my $scale_min = $entry->{scale_min} || 1;
	$name =~ s/_/ /g;
	my $linetype='';

	if( 0 && $debug ){	# For testing and displaying all streets in every scale
	    $scale_max = 1000000000000;
	}

	print "color($streets_type_id): '$color', name ='$name'\n";
		
	Geo::Gpsdrive::DBFuncs::db_exec("DELETE FROM `streets_type` WHERE streets_type_id = '$streets_type_id';");
	Geo::Gpsdrive::DBFuncs::db_exec
	    ("INSERT INTO `streets_type` ".
	     "        (streets_type_id,   name,   color ,  color_bg  , width ,  width_bg  , linetype  , scale_min , scale_max )".
	     " VALUES ($streets_type_id,'$name','$color','$color_bg','$width','$width_bg','$linetype','$scale_min','$scale_max');");
	
	$i++;
    }

    # Reserve Type 500 so users place there id's behind it
    $streets_type_id =500;
    Geo::Gpsdrive::DBFuncs::db_exec("DELETE FROM `streets_type` WHERE streets_type_id = '$streets_type_id';");
    Geo::Gpsdrive::DBFuncs::db_exec("INSERT INTO `streets_type` ".
				    "        (streets_type_id,  name     ,  color  ,  color_bg , linetype )".
				    " VALUES ($streets_type_id,'Reserved','#000000','#000000'  , '');");
    
} # of fill_default_street_types()

# -----------------------------------------------------------------------------

sub fill_defaults(){
    print "Create Defaults ...\n";
    fill_default_poi_types();
    fill_default_sources();
    fill_default_street_types();
    print "Create Defaults completed\n";
}


# Here comes some icon translation stuff that isn't used anymore, and could be
# removed. But we keep it, to memorize, what icons should be created.
#
#
# Some suggestions for pictures
#  education.nursery ==> Schnuller
#  Entrance          ==> Door
#  police            ==> Sherrif Stern
#  park_and_ride     ==> blaues P + Auto + xxx
#  recreation.fairground    ==> Riessenrad
#  gift_shop         ==> Geschenk Paeckchen
#  model.airplanes   ==> Airplane + Fernsteuerung
#  Bei Sportarten evtl. die Olympischen Symbole verwenden
#  Zoo               ==> Giraffe+ Voegel
#  WC                ==> WC oder 00
#  Recycling         ==> R oder Gruene Punkt

my $translate_de = { 
    '100_kmh'               => "100_kmh",
    '120_kmh'               => "120_kmh",
    '30_kmh'                => "30_kmh",
    '50_kmh'                => "50_kmh",
    '60_kmh'                => "60_kmh",
    '80_kmh'                => "80_kmh",
    'ATM'                   => 'Geldautomat',
    'EC'                    => 'EC-Automat',
    'HypoVereinsbank' 	=> 'HypoVereinsbank',
    'Postbank'              => 'Postbank',
    'Reifeisenbank'         => 'Reifeisenbank',
    'Sparda'                => 'Sparda',
    'Sparkasse'             => "Sparkasse",
    'WC'                    => 'WC',
    'accommodation'         => 'Uebernachtung',
    'administration'        => 'Verwaltung',
    'adult_education'       => 'VHS',
    'aeroplane'             => "Motor Flugzeug",
    'agency'                => "Filiale",
    'agip'                  => "Agip",
    'airport'               => 'Flugplatz',
    'aldi'                  => "Aldi",
    'alpha_flag'            => 'Taucher',
    'ambulance'             => 'Ambulanz',
    'amusement'             => "Freizeit",
    'anker'                 => "Anker",
    'aquarium'              => "Aquarium",
    'aral'                  => "Aral",
    'area'                  => "Gebiet",
    'area'                  => "Gebiet",
    'asia'                  => "Asiatisch",
    'avis'                  => "Avis",
    'bakery'                => "Bakery",
    'bank'                  => 'Bank',
    'barn'                  => "Heuschober",
    'bavarian'              => "Bayrisch",
    'beergarden'            => 'Biergarten',
    'beverages'             => "Getraenke",
    'bicycling'             => "Fahrrad",
    'boat'                  => "Boot",
    'bob'                   => "Bob",
    'books'                 => 'Buecher',
    'bowling'               => "Kegeln",
    'bridge'                => "Bruecke",
    'building'              => "Gebaeude",
    'burger_king'           => 'Burger_King',
    'bus'                   => "Bus",
    'butcher'               => "Metzger",
    'cafe'                  => 'cafe',
    'campground'            => 'Campingplatz',
    'camping'               => "Camping",
    'capital'               => "Hauptstadt",
    'car'                   => 'Auto',
    'car_loading_terminal'  => 'Auto_Verlade_Bahnhof',
    'car_registration'      => 'Zulassungsstelle',
    'catholic'              => 'Katholisch',
    'caution'               => "Achtung",
    'cemetery'              => "Friedhof",
    'chinese'               => "Chinesisch",
    'church'                => 'Kirche',
    'cinema'                => 'Kino',
    'city'                  => 'Stadt',
    'climbing'              => "Klettern",
    'coach'                 => 'Kutsche',
    'coast'                 => 'rodeln',
    'computer'              => 'Computer',
    'concert_hall'          => "Konzert_Halle",
    'construction'          => 'Baustelle',
    'container_loading_terminal' => 'Gueter_Verlade_Bahnhof',
    'country'               => "Land",
    'cross-country'         => "langlauf",
    'custom'                => 'Benutzer_Eigene',
    'dam'                   => "Wehr",
    'dance_floor'           => "Tanzen",
    'dart'                  => 'Dart',
    'dea'                   => "Dea",
    'dead_end'              => "Sackgasse",
    'delivery_service'      => 'Lieferservice',
    'developer'             => 'Entwickler',
    'disco'                 => 'disco',
    'dive_flag'             => 'Taucher',
    'diving'                => "Tauchen",
    'doctor'                => 'Arzt',
    'down-hill'             => "Abfahrt",
    'drinking_water'        => "Trinkwasser",
    'driver-guide_service'  => 'Lotsendienst',
    'dry_cleaner'           => "Reinigung",
    'dumpstation'           => "Dump Platz",
    'education'             => 'Ausbildung',
    'electronics'           => "Electronik",
    'emergency'             => 'Notaufnahme',
    'emergency_call'        => 'Notruf_saeule',
    'end'                   => "Ende",
    'entrance'              => 'Eingang',
    'esso'                  => "Esso",
    'event_location'        => 'Veranstaltungshalle',
    'exhibition'            => 'Messe',
    'exhibitions'           => 'Ausstellungen',
    'exibition'             => "Ausstellung",
    'fairground'            => 'Rummelplatz',
    'farm'                  => "Felder",
    'farmhouse'             => "Bauernhof",
    'fastfood'              => 'Schnellrestaurant',
    'ferry'                 => 'Faehre',
    'fire_department'       => 'Feuerwehr',
    'fishing'               => "angeln",
    'fittness'              => "Fittness",
    'flea_market'           => 'Flohmarkt',
    'food'                  => 'Essen',
    'forest'                => "Wald",
    'friends'               => 'Freunde',
    'fruit'                 => 'Obst',
    'furnishing_house'      => 'Einrichtungs_Haus',
    'furniture'             => "Moebel",
    'games'                 => "Spiele",
    'garage'                => 'Werkstatt',
    'gas_station'           => 'Tankstelle',
    'general'               => "Allgemein",
    'geocache'              => 'Geocache',
    'gift_shop'             => 'Geschenke_Laden',
    'give_way'              => "Vorfahrt Achten",
    'glass'                 => 'Glas',
    'glider'                => "Selgelflugzeug",
    'golf_place'            => 'Golfplatz',
    'greek'                 => "Griechisch",
    'groceries'             => 'Lebensmittel',
    'hall'                  => "Halle",
    'hamlet'                => "kleineres Dorf",
    'handicapped'           => "Behinderte",
    'harbor'                => "Hafen",
    'hardware'              => 'Hardware',
    'hardware_store'        => 'Baumarkt',
    'health'                => 'Gesundheit',
    'helicopter'            => "Hubschrauber",
    'herz'                  => "Herz",
    'highschool'            => 'Gymnasium',
    'hill'                  => 'Berg',
    'home'                  => 'Daheim',
    'horse'                 => "Pferde",
    'hospital'              => 'Krankenhaus',
    'hotel'                 => 'Hotel',
    'hunt'                  => "Jagd",
    'ice_cream_parlor'      => 'Eisdiele',
    'ice_scating'           => "Eislaufen",
    'import_way'            => 'import_way',
    'indian'                => "Indisch",
    'industial-area'        => "Industrie_Gebiet",
    'information'           => 'Information',
    'internet'              => 'internet',
    'interurban_train_station' => 'S-Bahn-Haltestelle',
    'italian'               => "Italienisch",
    'japanese'              => "japanisch",
    'jet'                   => "Jet",
    'kindergarden'          => 'Kindergarten',
    'lake'                  => "See",
    'landmark'              => 'Sehenswuerdigkeit',
    'large'                 => "Gross",
    'letter-box'            => 'Briefkasten',
    'lidl'                  => "Lidl",
    'lift'                  => "Lift",
    'literature'            => 'Literatur',
    'marker-1'              => "Marke_1",
    'marker-2'              => "Marke_2",
    'marker-3'              => "Marke_3",
    'marker-4'              => "Marke_4",
    'marker-5'              => "Marke 5",
    'market'                => "Markt",
    'mayor'                 => "haupt",
    'mc_donalds'            => 'MC_Donalds',
    'medium'                => "mittel",
    'memorial'              => "Denkmal",
    'mine'                  => "Miene",
    'minigolf_place'        => 'Minigolfplatz',
    'model_aircrafts'       => 'Modellflugplatz',
    'money_exchange'        => 'Geldwechsel',
    'monument'              => "Denkmal",
    'motel'                 => 'Motel',
    'motorbike'             => "Motorrad",
    'mountain'              => "Gebirge",
    'museum'                => 'Museum',
    'music'                 => "Music",
    'my'                    => "Meine",
    'national_park'         => 'Nationalpark',
    'nautical'              => 'Schiffahrt',
    'night_club'            => 'Nachtclub',
    'nursery'               => 'Kinderkrippe',
    'nursing_home'          => 'Altenheim',
    'office'                => 'Amt',
    'omv'                   => "Omv",
    'oneway'                => "Einbahn",
    'open'                  => 'Offen',
    'opera'                 => 'Oper',
    'paper'                 => 'Papier',
    'parachute'             => "Fallschirmspringen",
    'park'                  => "Park",
    'park_and_ride'         => 'Park_and_Ride',
    'parking'               => "Parken",
    'parkinglot'            => 'Parkplatz',
    'pedestrian'            => "Fussgaenger",
    'pedestrian_zone'       => 'Fussgaenger_Zone',
    'penny'                 => "Penny",
    'pharmacy'              => 'Apotheke',
    'phototrap_traffic_light' => 'Blitzampel',
    'picnic_area'           => "Rastplatz",
    'pizza'                 => "Pizza",
    'pizza_hut'             => "Pizza_Hut",
    'play_street'           => "Spiel Strasse",
    'playground'            => 'Spielplatz',
    'plus'                  => "Plus",
    'plus'                  => "Plus",
    'point-of-interest'     => "Interesanter Punkt",
    'police'                => 'Polizei',
    'postal'                => 'Post',
    'postoffice'            => 'Post',
    'primary_school'         => 'GrundSchule',
    'protestant'            => "evangelisch",
    'pub'                   => 'Kneipe',
    'public'                => 'oeffentlich',
    'racing'                => "Rennen",
    'railroad'              => "Eisenbahn",
    'railway'               => "Eisenbahn",
    'railway-crossing'      => "Bahnuebergang",
    'recreation'            => 'Freizeit',
    'recycling'             => 'Recycling',
    'recycling_centre'      => 'Wertstoffhof',
    'rental'                => "Vermietung",
    'rest_area'             => 'Raststaette',
    'restaurant'            => 'Gaststaette',
    'restaurant'            => 'Restaurant',
    'riding'                => 'Reiten',
    'right_of_way'          => "Vorfahrt Achten",
    'river'                 => "Fluss",
    'rock_climbing'         => "Fels klettern",
    'rocks'                 => "Fels",
    'rollerblades'          => "Rollerblades",
    'rollerscates'          => "Rollschuhe",
    'scale'                 => "Waage",
    'school'                => "Schule",
    'secondary_school'      => 'HauptSchule',
    'shell'                 => "Shell",
    'ship'                  => "Boot",
    'shopping'              => 'Einkaufen',
    'shopping_center'       => 'Einkaufszentrum',
    'shower'                => "Dusche",
    'signs'                 => "Zeichen",
    'sixt'                  => "Sixt",
    'ski'                   => "Ski",
    'skiing'                => 'Skifahren',
    'small'                 => "klein",
    'snakeboard'            => "Snakeboard",
    'soccer_field'          => 'Fussballplatz',
    'soccer_stadion'        => 'Fussball_Stadion',
    'software'              => 'Software',
    'speedlimit'            => "Geschwindigkeitbeschraenkung",
    'speedtrap'             => 'Radarfalle',
    'sport'                 => "Sport",
    'sports'                => 'Sport',
    'state'                 => "Staat",
    'station'               => "Station",
    'stop'                  => "Stop",
    'subway_city'           => "U-Bahn",
    'subway_regional'       => "S-Bahn",
    'supermarket'           => 'Supermarkt',
    'swimming'              => "schwimmen",
    'swimming_area'         => 'Schwimmbad',
    'synagoge'              => 'Synagoge',
    'taxi'                  => "Taxi",
    'taxi_stand'            => 'Taxi-Stand',
    'tea'                   => "Tee",
    'telephone'             => 'Telefon-Zelle',
    'tengelmann'            => "Tengelmann",
    'tennis'                => "Tennis",
    'tennis_place'          => 'Tennisplatz',
    'tent'                  => "Zelt",
    'texaco'                => "Texaco",
    'theater'               => "Theater",
    'theatre'               => 'Theater',
    'toll_station'          => 'Mautstation',
    'town_hall'             => 'Stadthalle',
    'toys'                  => 'Spielwaren',
    'trade_show'            => "Handels_Messe",
    'traffic'               => 'Verkehr',
    'traffic_jam'           => 'Stau',
    'trafficlight'          => "VerkehrsAmpel",
    'trailer'               => "Wohnwagen",
    'train'                 => 'Zug',
    'tram'                  => "Strassenbahn",
    'tram_station'          => 'Strassen-Bahn_Haltestelle',
    'transport'             => 'Transport',
    'travel-agency'         => "Reisebuero",
    'truck'                 => "Lastwagen",
    'tunnel'                => "Tunnel",
    'txt'                   => 'txt',
    'underground'           => "U-Bahn",
    'underground_station'   => 'U-Bahn_Haltestelle',
    'university'            => 'UNI',
    'unknown'               => 'unknown',
    'vegetables'            => 'Gemuese',
    'viewpoint'             => "Aussichtspunkt",
    'w-lan'                 => 'W-LAN',
    'water'                 => "Wasser",
    'water_ski'             => "Wasser Ski",
    'waypoint'              => "Wegpunkt",
    'wep'                   => 'WEP',
    'work'                  => 'Arbeit',
    'wrecking_service'      => "Abschleppdienst",
    'youth_hostel'          => 'Jugend_Herberge',
    'zoo'                   => 'Zoo',
};


1;
