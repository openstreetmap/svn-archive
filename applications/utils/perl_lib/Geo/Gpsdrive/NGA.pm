# Import Data from http://earth-info.nga.mil/
#
# $Log$
# Revision 1.4  2006/08/08 08:18:51  tweety
# nsert points too
#
# Revision 1.3  2006/03/10 08:37:09  tweety
# - Replace Street/Track find algorithmus in Query Funktion
#   against real Distance Algorithm (distance_line_point).
# - Query only reports Track/poi/Streets if currently displaying
#   on map is selected for these
# - replace old top/map Selection by a MapServer based selection
# - Draw White map if no Mapserver is selected
# - Remove some useless Street Data from Examples
# - Take the real colors defined in Database to draw Streets
# - Add a frame to the Streets to make them look nicer
# - Added Highlight Option for Tracks/Streets to see which streets are
#   displayed for a Query output
# - displaymap_top und displaymap_map removed and replaced by a
#   Mapserver centric approach.
# - Treaked a little bit with Font Sizes
# - Added a very simple clipping to the lat of the draw_grid
#   Either the draw_drid or the projection routines still have a slight
#   problem if acting on negative values
# - draw_grid with XOR: This way you can see it much better.
# - move the default map dir to ~/.gpsdrive/maps
# - new enum map_projections to be able to easily add more projections
#   later
# - remove history from gpsmisc.c
# - try to reduce compiler warnings
# - search maps also in ./data/maps/ for debugging purpose
# - cleanup and expand unit_test.c a little bit
# - add some more rules to the Makefiles so more files get into the
#   tar.gz
# - DB_Examples.pm test also for ../data and data directory to
#   read files from
# - geoinfo.pl: limit visibility of Simple POI data to a zoom level of 1-20000
# - geoinfo.pl NGA.pm: Output Bounding Box for read Data
# - gpsfetchmap.pl:
#   - adapt zoom levels for landsat maps
#   - correct eniro File Download. Not working yet, but gets closer
#   - add/correct some of the Help Text
# - Update makefiles with a more recent automake Version
# - update po files
#
# Revision 1.2  2005/10/11 08:28:35  tweety
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
# Revision 1.10  2005/08/09 01:08:30  tweety
# Twist and bend in the Makefiles to install the DataDirectory more apropriate
# move the perl Functions to Geo::Gpsdrive in /usr/share/perl5/Geo/Gpsdrive/POI
# adapt icons.txt loading according to these directories
#
# Revision 1.9  2005/05/14 21:21:23  tweety
# Update Index createion
# Update default Streets
# Eliminate some undefined Value
#
# Revision 1.8  2005/05/01 13:49:36  tweety
# Added more Icons
# Moved filling with defaults to DB_Defaults.pm
# Added some more default POI Types
# Added icons.html to see which icons are used
# Added more Comments
# Reformating Makefiles
# Added new options for importing from way*.txt and adding defaults
# Added more source_id and type_id
#
# Revision 1.7  2005/04/13 19:58:30  tweety
# renew indentation to 4 spaces + tabstop=8
#
# Revision 1.6  2005/04/10 00:15:58  tweety
# changed primary language for poi-type generation to english
# added translation for POI-types
# added some icons classifications to poi-types
# added LOG: Entry for CVS to some *.pm Files
#

package Geo::Gpsdrive::NGA;

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

use strict;
use warnings;

use IO::File;
use File::Path;
use Geo::Gpsdrive::DBFuncs;
use Geo::Gpsdrive::Utils;
use Data::Dumper;


our $write_defaults_poi_list;

#############################################################################
our @countries = qw(aa ac ae af ag aj al am an ao ar as at au av 
		    ba bb bc bd be bf bg bh bk bl bm bn bo bp br bs bt bu bv bx by 
		    ca cb cd ce cf cg ch ci cj ck cm cn co cr cs ct cu cv cw cy
		    da dj do dr ec eg ei ek en er es et eu ez 
		    fg fi fj fk fm fo fp fr fs
		    ga gb gg gh gi gj  gk gl gm go gp gr gt gv gy gz 
		    ha hk hm ho hr hu
		    ic id im in io ip ir is it iv iz 
		    ja je jm jn jo ju ke kg kn kr ks kt ku kz
		    la le lg lh li lo ls lt lu ly 
		    ma mb mc md mf mg mh mi mk ml mn mo mp mr mt mu mv mx my mz 
		    nc ne nf ng nh ni nl nm no np nr ns nt nu nz 
		    os 
		    pa pc pe pf pg pk pl pm po pp ps pu
		    qa re rm ro 
		    rp rs rw 
		    sa sb sc se sf sg sh si sl sm sn so sp st su sv sw sx sy sz
		    td te th ti tk tl tn to tp ts tt tu tv tw tx tz 
		    uf ug uk up uv uy uz 
		    vc ve vi vm vt
		    wa we wf wi ws wz 
		    yi ym 
		    za zi);

our $name2country = {
    'afghanistan'	=> 'af',
    'albania'	=> 'al',
    'algeria'	=> 'ag',
    'andorra'	=> 'an',
    'angola'	=> 'ao',
    'anguilla'	=> 'av',
    'antigua and barbuda'	=> 'ac',
    'argentina'	=> 'ar',
    'armenia'	=> 'am',
    'aruba'	=> 'aa',
    'ashmore and cartier islands'	=> 'at',
    'australia'	=> 'as',
    'austria'	=> 'au',
    'azerbaijan'	=> 'aj',
    'bahamas, the'	=> 'bf',
    'bahrain'	=> 'ba',
    'bangladesh'	=> 'bg',
    'barbados'	=> 'bb',
    'bassas da india'	=> 'bs',
    'belarus'	=> 'bo',
    'belgium'	=> 'be',
    'belize'	=> 'bh',
    'benin'	=> 'bn',
    'bermuda'	=> 'bd',
    'bhutan'	=> 'bt',
    'bolivia'	=> 'bl',
    'bosnia and herzegovina'	=> 'bk',
    'botswana'	=> 'bc',
    'bouvet island'	=> 'bv',
    'brazil'	=> 'br',
    'british indian ocean territory'	=> 'io',
    'british virgin islands'	=> 'vi',
    'brunei'	=> 'bx',
    'bulgaria'	=> 'bu',
    'burkina faso'	=> 'uv',
    'burma'	=> 'bm',
    'burundi'	=> 'by',
    'cambodia'	=> 'cb',
    'cameroon'	=> 'cm',
    'canada'	=> 'ca',
    'cape verde'	=> 'cv',
    'cayman islands'	=> 'cj',
    'central african republic'	=> 'ct',
    'chad'	=> 'cd',
    'chile'	=> 'ci',
    'china'	=> 'ch',
    'christmas island'	=> 'kt',
    'clipperton island'	=> 'ip',
    'cocos (keeling) islands'	=> 'ck',
    'colombia'	=> 'co',
    'comoros'	=> 'cn',
    'congo'	=> 'cf',
    'congo, democratic republic of the'	=> 'cg',
    'cook islands'	=> 'cw',
    'coral sea islands'	=> 'cr',
    'costa rica'	=> 'cs',
    'croatia'	=> 'hr',
    'cuba'	=> 'cu',
    'cyprus'	=> 'cy',
    'czech republic'	=> 'ez',
    'cote d\'ivoire'	=> 'iv',
    'denmark'	=> 'da',
    'djibouti'	=> 'dj',
    'dominica'	=> 'do',
    'dominican republic'	=> 'dr',
    'east timor'	=> 'tt',
    'ecuador'	=> 'ec',
    'egypt'	=> 'eg',
    'el salvador'	=> 'es',
    'equatorial guinea'	=> 'ek',
    'eritrea'	=> 'er',
    'estonia'	=> 'en',
    'ethiopia'	=> 'et',
    'europa island'	=> 'eu',
    'falkland islands (islas malvinas)'	=> 'fk',
    'faroe islands'	=> 'fo',
    'fiji'	=> 'fj',
    'finland'	=> 'fi',
    'france'	=> 'fr',
    'french guiana'	=> 'fg',
    'french polynesia'	=> 'fp',
    'french southern and antarctic lands'	=> 'fs',
    'gabon'	=> 'gb',
    'gambia, the'	=> 'ga',
    'gaza strip'	=> 'gz',
    'georgia'	=> 'gg',
    'germany'	=> 'gm',
    'ghana'	=> 'gh',
    'gibraltar'	=> 'gi',
    'glorioso islands'	=> 'go',
    'greece'	=> 'gr',
    'greenland'	=> 'gl',
    'grenada'	=> 'gj',
    'guadeloupe'	=> 'gp',
    'guatemala'	=> 'gt',
    'guernsey'	=> 'gk',
    'guinea'	=> 'gv',
    'guinea-bissau'	=> 'pu',
    'guyana'	=> 'gy',
    'haiti'	=> 'ha',
    'heard island and mcdonald islands'	=> 'hm',
    'honduras'	=> 'ho',
    'hong kong'	=> 'hk',
    'hungary'	=> 'hu',
    'iceland'	=> 'ic',
    'india'	=> 'in',
    'indonesia'	=> 'id',
    'iran'	=> 'ir',
    'iraq'	=> 'iz',
    'ireland'	=> 'ei',
    'isle of man'	=> 'im',
    'israel'	=> 'is',
    'italy'	=> 'it',
    'jamaica'	=> 'jm',
    'jan mayen'	=> 'jn',
    'japan'	=> 'ja',
    'jersey'	=> 'je',
    'jordan'	=> 'jo',
    'juan de nova island'	=> 'ju',
    'kazakhstan'	=> 'kz',
    'kenya'	=> 'ke',
    'kiribati'	=> 'kr',
    'kuwait'	=> 'ku',
    'kyrgyzstan'	=> 'kg',
    'laos'	=> 'la',
    'latvia'	=> 'lg',
    'lebanon'	=> 'le',
    'lesotho'	=> 'lt',
    'liberia'	=> 'li',
    'libya'	=> 'ly',
    'liechtenstein'	=> 'ls',
    'lithuania'	=> 'lh',
    'luxembourg'	=> 'lu',
    'macau'	=> 'mc',
    'macedonia, the former yugoslav republic of'	=> 'mk',
    'madagascar'	=> 'ma',
    'malawi'	=> 'mi',
    'malaysia'	=> 'my',
    'maldives'	=> 'mv',
    'mali'	=> 'ml',
    'malta'	=> 'mt',
    'marshall islands'	=> 'rm',
    'martinique'	=> 'mb',
    'mauritania'	=> 'mr',
    'mauritius'	=> 'mp',
    'mayotte'	=> 'mf',
    'mexico'	=> 'mx',
    'micronesia, federated states of'	=> 'fm',
    'moldova'	=> 'md',
    'monaco'	=> 'mn',
    'mongolia'	=> 'mg',
    'montserrat'	=> 'mh',
    'morocco'	=> 'mo',
    'mozambique'	=> 'mz',
    'namibia'	=> 'wa',
    'nauru'	=> 'nr',
    'nepal'	=> 'np',
    'netherlands antilles'	=> 'nt',
    'netherlands'	=> 'nl',
    'new caledonia'	=> 'nc',
    'new zealand'	=> 'nz',
    'nicaragua'	=> 'nu',
    'niger'	=> 'ng',
    'nigeria'	=> 'ni',
    'niue'	=> 'ne',
    'no man\'s land'	=> 'nm',
    'norfolk island'	=> 'nf',
    'north korea'	=> 'kn',
    'norway'	=> 'no',
    'oceans'	=> 'os',
    'oman'	=> 'mu',
    'pakistan'	=> 'pk',
    'palau'	=> 'ps',
    'panama'	=> 'pm',
    'papua new guinea'	=> 'pp',
    'paracel islands'	=> 'pf',
    'paraguay'	=> 'pa',
    'peru'	=> 'pe',
    'philippines'	=> 'rp',
    'pitcairn islands'	=> 'pc',
    'poland'	=> 'pl',
    'portugal'	=> 'po',
    'qatar'	=> 'qa',
    'reunion'	=> 're',
    'romania'	=> 'ro',
    'russia'	=> 'rs',
    'rwanda'	=> 'rw',
    'saint helena'	=> 'sh',
    'saint kitts and nevis'	=> 'sc',
    'saint lucia'	=> 'st',
    'saint pierre and miquelon'	=> 'sb',
    'saint vincent and the grenadines'	=> 'vc',
    'samoa'	=> 'ws',
    'san marino'	=> 'sm',
    'sao tome and principe'	=> 'tp',
    'saudi arabia'	=> 'sa',
    'senegal'	=> 'sg',
    'serbia and montenegro'	=> 'yi',
    'seychelles'	=> 'se',
    'sierra leone'	=> 'sl',
    'singapore'	=> 'sn',
    'slovakia'	=> 'lo',
    'slovenia'	=> 'si',
    'solomon islands'	=> 'bp',
    'somalia'	=> 'so',
    'south africa'	=> 'sf',
    'south georgia and the south sandwich islands'	=> 'sx',
    'south korea'	=> 'ks',
    'spain'	=> 'sp',
    'spratly islands'	=> 'pg',
    'sri lanka'	=> 'ce',
    'sudan'	=> 'su',
    'suriname'	=> 'ns',
    'svalbard'	=> 'sv',
    'swaziland'	=> 'wz',
    'sweden'	=> 'sw',
    'switzerland'	=> 'sz',
    'syria'	=> 'sy',
    'taiwan'	=> 'tw',
    'tajikistan'	=> 'ti',
    'tanzania'	=> 'tz',
    'thailand'	=> 'th',
    'togo'	=> 'to',
    'tokelau'	=> 'tl',
    'tonga'	=> 'tn',
    'trinidad and tobago'	=> 'td',
    'tromelin island'	=> 'te',
    'tunisia'	=> 'ts',
    'turkey'	=> 'tu',
    'turkmenistan'	=> 'tx',
    'turks and caicos islands'	=> 'tk',
    'tuvalu'	=> 'tv',
    'uganda'	=> 'ug',
    'ukraine'	=> 'up',
    'undersea features'	=> 'uf',
    'united arab emirates'	=> 'ae',
    'united kingdom'	=> 'uk',
    'uruguay'	=> 'uy',
    'uzbekistan'	=> 'uz',
    'vanuatu'	=> 'nh',
    'vatican city'	=> 'vt',
    'venezuela'	=> 've',
    'vietnam'	=> 'vm',
    'wallis and futuna'	=> 'wf',
    'west bank'	=> 'we',
    'western sahara'	=> 'wi',
    'yemen'	=> 'ym',
    'zambia '	=> 'za',
    'zimbabwe'	=> 'zi',
};

my $country2name={};
for my $name ( keys %{$name2country} ) {
    $country2name->{$name2country->{$name}}=$name;
}



#############################################################################
# Args: 
#    $filename : Filename to read 
# returns:
#    $waypoints : Hash of read Waypoints
#############################################################################
sub add_earthinfo_nga_mil_to_db($$){
    my $full_filename = shift;
    my $source = shift;

    my ($country) = ($full_filename =~ m,/([^/]+).txt,);
    
    print "Reading earthinfo_nga_mil ($full_filename) [$country2name->{$country}] and writing to db\n";

    my $lat_min= 1000;
    my $lat_max=-1000;
    my $lon_min= 1000;
    my $lon_max=-1000;

    my $fh = IO::File->new("<$full_filename");
    $fh or die ("add_earthinfo_nga_mil_to_db: Cannot open $full_filename:$!\n");
    
    Geo::Gpsdrive::DBFuncs::delete_all_from_source($source);
    my $source_id = Geo::Gpsdrive::DBFuncs::source_name2id($source);

    unless ( $source_id ) {
	my $source_hash = {
	    'source.url'     => "http://earth-info.nga.mil/gns/html/",
	    'source.name'    => $source ,
	    'source.comment' => '' ,
	    'source.licence' => "Licensing GNS Data".
		"There are no licensing requirements or restrictions ".
		"in place for the use of  ".
		"the GNS data.\n".
		"\n".
		"Toponymic information is based on the Geographic Names Data Base, ".
		"containing official standard names approved by the ".
		"United States Board on Geographic Names and maintained ".
		"by the National Geospatial-Intelligence Agency. ".
		"More information is available at the Products and Services ".
		"link at www.nga.mil. The National Geospatial-Intelligence ".
		"Agency name, initials, and seal are protected by ".
		"10 United States Code Section xxx445."
	    };
	Geo::Gpsdrive::DBFuncs::insert_hash("source", $source_hash);
	$source_id = Geo::Gpsdrive::DBFuncs::source_name2id($source);
    }
    

    my @columns;
    @columns = qw( rc      ufi     uni     
		   poi.lat     poi.lon    
		   dms_lat dms_long        
		   utm     jog     fc      
		   dsg     pc      cc1     
		   adm1    adm2    
		   dim     
		   cc2     nt      lc      
		   short_form      
		   generic 
		   sort_name full_name full_name_nd
		   mod_date
		   );
    my $lines_count_file =0;
    my $line = $fh->getline(); # Header entfernen
    my $count_entries = {}; # Count the entries for special types
    while ( $line = $fh->getline() ) {
	$lines_count_file ++;
	print "$lines_count_file\r" if $verbose &&  ! ($lines_count_file % 100);
	$line =~ s/[\t\r\n\s]*$//g;
#	 print "line: '$line'\n";
	if ( $line =~ m/^$/ ) {
	} elsif ( $line =~ m/^\#/ ) {
	} else {
	    die "Spalten nicht definiert" unless @columns;
#	    print "-----------------------------------------------------------------\n";
#	    print "WP: $line\n";
	    my @values = split(/\t/,$line);
#	    print Dumper(\@values);
	    my $values;
	    for my $i ( 0 .. scalar @columns -1 ) {
#		next unless $columns[$i] =~ m/\./;
		$values->{$columns[$i]} = ($values[$i]||'');
#		print $columns[$i].": \t".($values[$i]||'-')."\n" if $debug;
	    }


	    $values->{'poi.source_id'}=$source_id;
	    #$values->{'source.name'} = $source;
#	    print Dumper(\$values);

	    # A specific part of the name that could substitute for the full name.
	    $values->{'poi.name'}=$values->{'short_form'}||$values->{'full_name'};

	    print "Name longer than 80 chars: $values->{'poi.name'} \n"
		if length($values->{'poi.name'}) > 80 ;
	    
	    $values->{'poi.comment'}=$values->{'generic'};
	    # GENERIC
	    # The descriptive part of the full name (does not apply to populated 
	    # place names).


	    # SORT_NAME
	    # A form of the full name which allows for easy sorting of the name 
	    # into alpha-numeric sequence. It is comprised of the specific name, 
	    # generic name, and any articles or prepositions. 
	    # This field is all upper case with spaces, diacritics, and hyphens 
	    # removed and numbers are substituted with lower case alphabetic characters.

	    # FULL_NAME
	    # The full name is a complete name which identifies the named feature.  
	    # It is comprised of  the specific name, generic name, and any articles 
	    # or prepositions (refer to REGIONS.PDF for character mapping).
	    if ( $values->{'full_name'} eq $values->{'poi.name'} ) {
		$values->{'poi.comment'}='';
	    } else {
		$values->{'poi.comment'}=$values->{'full_name'};
	    }
	    
	    # FULL_NAME_ND
	    # Same as the full name but the diacritics and special characters are 
	    # substituted with Roman characters (refer to REGIONS.PDF for character 
	    # mapping).   ND = No Diacritics / Stripped Diacritics.


	    $values->{'poi.last_modified'}=$values->{'mod_date'};

	{
	    my $pc = $values->{'pc'};
	    # Populated Place Classification.  
	    # A graduated numerical scale denoting the relative importance 
	    # of a populated place.  
	    # The scale ranges from 1,  relatively high, to 5, relatively low.  
	    # The scale could also include NULL (no value) as a value for 
	    # populated places with unknown or undetermined classification.
	    my $scale_min = 0;
	    my $scale_max = 99;
	    if ( defined($pc) && $pc ne '' ) { 
		#print "pc : $pc \n";
		if    ( $pc == 1 ) {   $scale_min = 1;  $scale_max = 100000000; }
		elsif ( $pc == 2 ) {   $scale_min = 1;	$scale_max = 10000000; }
		elsif ( $pc == 3 ) {   $scale_min = 1;	$scale_max = 1000000; }
		elsif ( $pc == 4 ) {   $scale_min = 1;	$scale_max = 100000; }
		elsif ( $pc == 5 ) {   $scale_min = 1;  $scale_max = 10000; }
	    } else {
		$scale_min = 1;	$scale_max = 10000; 
	    };
	    $values->{'poi.scale_min'} = $scale_min;
	    $values->{'poi.scale_max'} = $scale_max;
	}


	    
	{   # NT Name Type:
	    # C = Conventional;
	    # D = Not verified;
	    # N = Native;
	    # V = Variant or alternate.
	    my $nt = $values->{'nt'};
	}

	{ # decide which symbol
	    my $fc = $values->{'fc'};
	    my $symbol = "City";
	    #FC
	    #Feature Classification:
	    if    ( $fc eq "A" ) { $symbol = "Administrative region" }
	    elsif ( $fc eq "P" ) { $symbol = "Populated place" }
	    elsif ( $fc eq "V" ) { $symbol = "Vegetation" }
	    elsif ( $fc eq "L" ) { $symbol = "Locality or area" }
	    elsif ( $fc eq "U" ) { $symbol = "Undersea" }
	    elsif ( $fc eq "R" ) { $symbol = "Streets, highways, roads, or railroad" }
	    elsif ( $fc eq "T" ) { $symbol = "Hypsographic" }
	    elsif ( $fc eq "H" ) { $symbol = "Hydrographic" }
	    elsif ( $fc eq "S" ) { $symbol = "Spot feature." }
	    else                 { $symbol = "Unknown" }
	    $values->{'type.name'} = $symbol;
	}
	    
	{   # DIM Dimension.  
	    #     Usually used to display elevation or population data.
	    #     +-10 Digits
	    my $proximity = $values->{'dim'} ;
	    $proximity ||= 1   if  $values->{'fc'} eq 'R'; # Roads
	    $proximity ||= 800 if  $values->{'fc'} eq 'P'; # Populated Place
	    $proximity ||= ' 100m';
	    $values->{'poi.proximity'} = $proximity;
	}

	    # RC Region Code.  
	    #    A code that determines the character mapping used in the Full_Name 
	    #    field (refer to REGIONS.PDF for character mapping):
	    # 1 = Western Europe/Americas;
	    # 2 = Eastern Europe;
	    # 3 = Africa/Middle East;
	    # 4 = Central Asia;
	    # 5 = Asia/Pacific;
	    # 6 = Vietnam.

	    # UFI
	    # Unique Feature Identifier.  A number which uniquely identifies the feature. 
	    # number +- 10 Digits

	    # UNI
	    # Unique Name Identifier.  A number which uniquely identifies a name.
	    # number +- 10 Digits


	    # LAT      Latitude of the feature in +- decimal degrees (WGS84):                +- 2.7 Digits
	    # LONG     Longitude of the feature in +- decimal degrees (WGS84):               +- 3.7 Digits
	    # DMS_LAT  Latitude of the feature in +- degrees, minutes, and seconds (WGS84):  +- 6 Digits
	    # DMS_LONG Longitude of the feature in +- degrees, minutes, and seconds (WGS84): +- 7 Digits
	    # UTM      Universal Transverse Mercator coordinate grid reference.               4 Characters
	    # JOG      Joint Operations Graphic reference.                                    7 Characters


	    # DSG Feature Designation Code.  
	    #     A two to five-character code used to identify the type of feature a name is applied to.

	    # PC Populated Place Classification.  
	    #    A graduated numerical scale denoting the relative importance of a populated place.  
	    #    The scale ranges from 1,  relatively high, to 5, relatively low.  The scale could also
	    #    include NULL (no value) as a value for populated places with unknown or undetermined classification.

	    # CC1 Primary Country Code.  
	    #     A two alphabetic character code uniquely identifying a 
	    #     geopolitical entity (countries, dependencies, 
	    #     and areas of special sovereignty).

	    # ADM1 First-order administrative division.  
	    #      A two alphanumeric character code uniquely identifying a 
	    #      primary administrative division of a country,
	    #      such as a state in the United States.
	    
	    # ADM2 Second-order administrative division.  
	    #      The name of a subdivision of a first-order administrative division, 
	    #      such as a county in the United States.
	    #      200 Characters


	    # CC2 Secondary Country Code.  
	    #     A two alphabetic character code uniquely identifying the 
	    #     country code of a particular name if different than that of the feature.

	    # LC Language Code.  
	    #    A two alphabetic character code uniquely identifying a 
	    #    language of a country if multiple official languages are used.
	    #    2 Characters
	
	    # SHORT_FORM
	    #     A specific part of the name that could substitute for the full name.
	    #     128 Characters

	    # GENERIC
	    #     The descriptive part of the full name (does not apply to populated place names).
	    #     128 Characters

	    # SORT_NAME
	    #      A form of the full name which allows for easy sorting of the name 
	    #      into alpha-numeric sequence.  It is comprised of the specific name, 
	    #      generic name, and any articles or prepositions. This field is all 
	    #      upper case with spaces, diacritics, and hyphens removed and numbers
	    #      are substituted with lower case alphabetic characters.
	    #      200 Characters

	    # FULL_NAME
	    #      The full name is a complete name which identifies the named feature.  
	    #      It is comprised of  the specific name, generic name, and any articles 
	    #      or prepositions (refer to REGIONS.PDF for character mapping).
	    #      200 Characters

	    # FULL_NAME_ND
	    #      Same as the full name but the diacritics and special characters are
	    #      substituted with Roman characters (refer to REGIONS.PDF for character mapping).
	    #      ND = No Diacritics / Stripped Diacritics.
	    #      200 Characters

	    # MOD_DATE
	    # The date a new feature was added or any part of an existing feature was modified (YYYY-MM-DD).


	    my $lat = $values->{'poi.lat'};
	    my $lon = $values->{'poi.lon'};
	    #print substr($line,10,30)."\n" 	    if ( $lon < 10 );
	    $lat_min= min($lat_min,$lat);
	    $lat_max= max($lat_max,$lat);
	    $lon_min= min($lon_min,$lon);
	    $lon_max= max($lon_max,$lon);

#	    printf "lat,lon %.2f,%.2f	(%.2f,%.2f) - (%.2f,%.2f)\n",$lat,$lon,$lat_min,$lon_min,$lat_max,$lon_max if $verbose;

	    if ($main::do_collect_init_data ) { # Collect Major Cities
		for my $type ( qw(pc) ) {
		    $count_entries->{$type}->{$values->{$type}}++;
		}		

		#$count_entries->{dim}->{x} if defined($values->{dim}) && ( $values->{dim} > 0);
		
		if ( $values->{pc} eq "1"  && $write_defaults_poi_list ) {   
		    my $name = $values->{'poi.name'};
		    $name =~ s/'/\\'/g; # '
		    $name =~ s/`/\\`/g; # `
		    print $write_defaults_poi_list '		';
		    print $write_defaults_poi_list "{ name => '$name',";
		    print $write_defaults_poi_list '	lat => '.$values->{'poi.lat'}.",";
		    print $write_defaults_poi_list '	lon => '.$values->{'poi.lon'}." },\n";
		    print "Name:".$values->{'poi.name'}."\n";
		    print "DIM:".$values->{dim}."\n" if $values->{dim};
#		print "ADM1:".$values->{adm1}."\n";
#		print "ADM2:".$values->{adm2}."\n";
#		print "FC:".$values->{fc}."\n";
#		print join("",map{"\t$_ \t=> ".$values->{$_}."\n" } keys %$values);
		    add_poi($values);
		}
	    }

# DEBUG
	    add_poi($values);
	}
    }
    print "$lines_count_file read\n" if $verbose;
    print "lat($lat_min , $lat_max)	lon($lon_min , $lon_max)\n" if $verbose;
    if ( $debug && $verbose ) {
	for my $type ( keys %{$count_entries} ) {
	    for my $sub_type ( keys %{$count_entries->{$type}} ) {
		print "  $type=$sub_type: ".$count_entries->{$type}->{$sub_type}."\n";
	    }
	}
    }
}

# *****************************************************************************
sub import_Data($){
    my $what = shift;

    my $earthinfo_dir="$main::CONFIG_DIR/MIRROR/earthinfo";

    print "\nDownload an import NGA Data\n";

    # If the File exist it will be filled with the Major cities
    # So just touch it and it will be filled
    if ($main::do_collect_init_data ) { # Collect Major Cities
	if ( -d "../data/" ) {
	    $write_defaults_poi_list = IO::File->new(">../data/Default_poi.txt");
	} else {
	    print "Warning: ../data not a directory; writing no default poi list\n";
	};
    }

    unless ( -d $earthinfo_dir ) {
	print "Creating Directory $earthinfo_dir\n";
	mkpath $earthinfo_dir
	    or die "Cannot create Directory $earthinfo_dir:$!\n";
    }
    
    my @do_countries;
    my $country;
    if ($what eq "??" ) {
	print "Available counties:\n\n	";
	print join("\n	",map { "$_ ($name2country->{$_})" } sort keys %{$name2country} );
	print "\n";
	print "\n";
	print "See http://earth-info.nga.mil/gns/html/cntry_files.html for more Information\n";
	print "\n";
    } elsif ( $what =~  m/^\D/ ) {
	for $country ( split(",",$what) ) {
	    if ( $name2country->{$country}  ) {
		push ( @do_countries , $name2country->{$country} );
	    } else {
		if ( grep { $_ eq $country } @countries ) {
		    push ( @do_countries , $country );
		} else {
		    print "Country $country not valid\n";
		    print "List of valid countries:\n";
		    print join(",",@countries);
		    print "\n";
		    print "Use:\n";
		    print "poi.pl -earthinfo_nga_mil=??\n";
		    print "For detailed list\n;"
			
		    }
	    }
	}
    } else {
	@do_countries =  @countries;
    }
    
    for $country ( @do_countries ) {
	# download
	# http://earth-info.nga.mil/gns/html/cntyfile/gm.zip # Germany
	my $url="http://earth-info.nga.mil/gns/html/cntyfile/$country.zip";
	print "Mirror $url\n";
	my $mirror = mirror_file($url ,"$earthinfo_dir/geonames_$country.zip");
	
	# print "Mirror: $mirror\n";
	if ( (!-s "$earthinfo_dir/$country.txt") ||
	     file_newer("$earthinfo_dir/geonames_$country.zip",
			"$earthinfo_dir/$country.txt") ) {
	    print "Unpacking geonames_$country.zip\n";
	    `(cd $earthinfo_dir/; unzip -o geonames_$country.zip)`;
	} else {
	    print "unpack: $country.txt up to date\n" unless $verbose;
	}
	
	add_earthinfo_nga_mil_to_db("$earthinfo_dir/$country.txt","earth-info.nga.mil $country");
    }
    
    print "Download an import NGA Data FINISHED\n";
}

1;
