# 
# PERL mapweaver module by gary68
#
#
#
#
# Copyright (C) 2011, Gerhard Schwanz
#
# This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 3 of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>
#


package mwConfig ; 

use strict ;
use warnings ;

use Getopt::Long ;

use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw ( 	cv
			initConfig
			readConfigFile
			setConfigValue
			printConfig
			printConfigDescriptions
			getProgramOptions
		 ) ;

my @initial = (	["verbose",0,  "print some more information (CLO)", "misc"],
			["debug",0,    "print debug information (CLO)",     "misc"],
			["projection", "merc","Used projection",            "map"],
			["ellipsoid",  "WGS84","Used ellipsoid",            "map"],

			["ruleDefaultNodeSize",      "20",      "default size of dot for nodes",  "nodes"],
			["ruleDefaultNodeColor",     "black",   "default color of dot for nodes", "nodes"],
			["ruleDefaultNodeShape",     "circle",  "default shape of node",          "nodes"],
			["ruleDefaultNodeLabel",     "name",  "default key for label",          "nodes"],
			["ruleDefaultNodelabelSize",     30,  "default size of label text",          "nodes"],
			["ruleDefaultNodeIconSize",  40,        "default size of icon",           "nodes"],
			["ruleDefaultNodeFromScale",  0,        "default fromScale of node",      "nodes"],
			["ruleDefaultNodeToScale",   1000000,   "default toScale of node",        "nodes"],

			["ruleDefaultWayLabel","name","default label of way", "ways"],
			["ruleDefaultWayLabelColor","black","default label color of way", "ways"],
			["ruleDefaultWayLabelSize",30,"default label size of way", "ways"],
			["ruleDefaultWayLabelFont","sans-serif","default label font of way", "ways"],
			["ruleDefaultWayLabelOffset",15,"default label Tspan offset of way", "ways"],

			["ruleDefaultWayColor","gray","default color of way", "ways"],
			["ruleDefaultWaySize",20,"default size of way", "ways"],
			["ruleDefaultWayBorderColor","black","default color of border of way", "ways"],
			["ruleDefaultWayBorderSize",2,"default size of border of way", "ways"],
			["ruledefaultWayFromScale",0,"default fromScale of way", "ways"],
			["ruledefaultWayToScale",1000000,"default toScale of way", "ways"],

			["ruleDefaultAreaColor","lightgray","default area color", "areas"],
			["ruleDefaultAreaMinSize",0,"minimum size of area to be drawn", "areas"],
			["ruledefaultAreaFromScale",0,"default fromScale of way", "areas"],
			["ruledefaultAreaToScale",1000000,"default toScale of way", "areas"],

			["in","map.osm","osm in file (CLO)", "job"],
			["ini","mwconfig.ini","file with configuration values (CLO)", "misc"],
			["out","mapweaver.svg","svg output name (CLO)", "job"],
			["style","mwStandardRules.txt","file with render rules (CLO)", "job"],
			["svgname","mapweaver.svg","output file name for svg graphics (CLO)", "job"],
			["size",2200,"size in pixels x axis, 300dpi (CLO)", "map"],
			["legend",0,"appearance and position of legend (CLO)", "map"],
			["bgcolor","white","background color of map (CLO)", "map"],
			["grid",0,"number of grid cells, 0 = no grid (CLO)", "map"],
			["gridcolor","black","color of grid lines (CLO)", "map"],
			["coords",0,"draw coordinate system (CLO)", "map"],
			["coordsexp",-2,"size of grid cells, exp 10 (CLO)", "map"],
			["coordscolor","black","color of coordinates grid lines (CLO)", "map"],
			["clip",0," (CLO)", "job"],
			["clipbbox",""," (CLO)", "job"],
			["pad",0," (CLO)", "job"],
			["ppc",6,"points per character (CLO)", "misc", "map"],
			["pdf",0,"convert output to pdf (CLO)", "job"],
			["png",0,"convert output to png (CLO)", "job"],
			["dir",0,"add directory (CLO)", "additional information"],
			["poi",0,"add POI directory (CLO)", "additional information"],
			["dirpdf",0,"create directory pdf (CLO)", "additional information"],
			["dircolnum",2,"number of text columns for directory pdf (CLO)", "additional information"],
			["dirtitle","Directory","title for directory (CLO)", "additional information"],
			["tagstat",0,"print tag statistics (CLO)", "misc"],
			["declutter",1," (CLO)", "map"],
			["allowIconMove",0," (CLO)", "map"],
			["lineDist",10,"distance between text lines in pixels", "map"],
			["maxCharPerLine",20,"maximum characters per line in node label", "map"],
			["help",0,"prints help texts (CLO)", "misc"],
			["oneway",0,"add oneway arrows (CLO)", "map"],
			["onewaycolor","white","color of oneway arrows (CLO)", "map"],
			["nobridge",0,"omit bridges and tunnels (CLO)", "map"],
			["noLabel",0,"", "map"],
			["place","","search for place name in osm file and create map (CLO)", "job"],
			["placefile","","name of file containing only place information (CLO)", "job"],
			["lonrad",2,"radius lon in km for place map (CLO)", "job"],
			["latrad",2,"radius lat in km for place map (CLO)", "job"],
			["ruler",0,"draw ruler; positions 1..4 (CLO)", "map"],
			["rulercolor","black","color of ruler (CLO)", "map"],
			["rulerbackground","none","background of ruler, none=transparent (CLO)", "map"],
			["scale",0,"draw scale; positions 1..4 (CLO)", "map"],
			["scalecolor","black","color of scale (CLO)", "map"],
			["scalebackground","none","color of scale background; none=transparent (CLO)", "map"],
			["scaleset",0,"set scale of map (i.e. 10000) (CLO)", "map"],
			["rulescaleset",0,"set assumed scale for rules (CLO)", "map"],
			["routelabelcolor","black","", "routes"],
			["routelabelsize",30,"", "routes"],
			["routelabelfont","sans-serif","", "routes"],
			["routelabeloffset",-5,"", "routes"],
			["routeicondist",70,"", "routes"],
			["routeiconscale",1,"", "routes"],
			["icondir","./routeicons","", "routes"],
			["poifile","","name of external POI file (CLO)", "job"],
			["relid",0,"relation ID for hikingbook (CLO)", "misc"],
			["rectangles","","draw rectangles for hikingbook (CLO)", "misc"],
			["pagenumbers","","add page numbers to map (CLO)", "misc"],
			["ra",0,"relation analyzer mode (CLO)", "misc"],
			["multionly",0,"draw only multipolygons (CLO)", "misc"],
			["foot","mapweaver by gary68 - data by www.openstreetmap.org","text for footer (CLO)", "map"],
			["footcolor","black","color for footer (CLO)", "map"],
			["footbackground","none","background color for footer (CLO)", "map"],
			["footsize",40,"font size for footer (CLO)", "map"],
			["head","","text for header (CLO)", "map"],
			["headcolor","black","color for header (CLO)", "map"],
			["headbackground","none","background color for header (CLO)", "map"],
			["headsize",40,"font size for header (CLO)", "map"],

			["minAreaSize",400,"min size of area to be drawn on map", "map"],
			["oceanColor","lightblue","color of ocean", "map"],
			["cIE",0,"osmosis clipIncompleteEntities instead of completeObjects (CLP)", "map"]

		  ) ;

my %cv = () ;
my %explanation = () ;

# --------------------------------------------------------------------------------

sub initConfig {

	# set initial values according to program internal values from array @initial

	foreach my $kv (@initial) {
		$cv{ lc( $kv->[0] ) } = $kv->[1] ;
		$explanation{ lc( $kv->[0] ) } = $kv->[2] ;
	}
}


sub setConfigValue {

	# allows any module to change a certain k/v pair

	my ($k, $v) = @_ ;

	$k = lc ( $k ) ;
	$cv{$k} = $v ;
	if ($cv{"verbose"} > 1) { print "config key $k. value changed to $v\n" ; }
}

sub cv {

	# access a value by key

	my $k = shift ;

	$k = lc ( $k ) ;
	if ( ! defined $cv{ $k } ) { print "WARNING: requested config key $k not defined!\n" ; }
	return ( $cv{ $k } ) ;
}

sub printConfig {

	# print actual config to stdout

	print "\nActual configuration\n" ;

	my %cats = () ;
	foreach my $e (@initial) {
		$cats{ $e->[3] } = 1 ;
	}

	foreach my $cat (sort keys %cats) {
		my @entries = () ;
		foreach my $e (@initial) {
			if ($e->[3] eq $cat) {
				push @entries, $e->[0] ;
			}
		}
		print "\nCATEGORY $cat\n" ;
		print "--------\n" ;
		foreach my $e ( sort { $a cmp $b } @entries ) {
			printf "%-30s %-30s\n", $e, cv($e) ;
		}
	}

	print "\n" ;
}

sub readConfigFile {

	# read ini file; initial k/v pairs might be changed

	my $fileName = shift ;
	my $lc = 0 ;
	
	print "reading config file $fileName\n" ;

	open (my $file, "<", $fileName) or die ("ERROR: could not open ini file $fileName\n") ;
	my $line = "" ;
	while ($line = <$file>) {
		$lc ++ ;
		if ( ! grep /^#/, $line) {
			my ($k, $v) = ( $line =~ /(.+?)=(.+)/ ) ;
			if ( ( ! defined $k ) or ( ! defined $v ) ) {
				print "WARNING: could not parse config line: $line" ;
			}
			else {
				$k = lc ( $k ) ;
				$cv{ $k } = $v ;
			}
		}
	}
	close ($file) ;
	print "$lc lines read.\n\n" ;
}


# ---------------------------------------------------------------------------------------

sub getProgramOptions {


my $optResult = GetOptions ( 	"in=s" 		=> \$cv{'in'},		# the in file, mandatory
				"ini:s"		=> \$cv{'ini'},
				"style=s" 	=> \$cv{'style'},		# the style file, mandatory
				"out:s"		=> \$cv{'svgname'},		# outfile name or default
				"size:i"	=> \$cv{'size'},		# specifies pic size longitude in pixels
				"legend:i"	=> \$cv{'legend'},		# legend?
				"bgcolor:s"	=> \$cv{'bgcolor'},		# background color
				"grid:i"	=> \$cv{'grid'},		# specifies grid, number of parts
				"gridcolor:s"	=> \$cv{'gridcolor'},		# color used for grid and labels
				"coords"	=> \$cv{'coords'},		# 
				"coordsexp:i"	=> \$cv{'coordsexp'},		# 
				"coordscolor:s"	=> \$cv{'coordscolor'},		# 
				"clip:i"	=> \$cv{'clip'},		# specifies how many percent data to clip on each side
				"clipbbox:s"	=> \$cv{'clipbbox'},		# bbox data for clipping map out of data
				"pad:i"		=> \$cv{'pad'},		# specifies how many percent data to pad on each side
				"ppc:f"		=> \$cv{'ppc'},		# pixels needed per label char in font size 10
				"pdf"		=> \$cv{'pdf'},		# specifies if pdf will be created
				"png"		=> \$cv{'png'},		# specifies if png will be created
				"dir"		=> \$cv{'dir'},		# specifies if directory of streets will be created
				"poi"		=> \$cv{'poi'},		# specifies if directory of pois will be created
				"dirpdf"		=> \$cv{'dirpdf'},
				"dircolnum:i"	=> \$cv{'dircolnum'},
				"dirtitle:s"	=> \$cv{'dirtitle'},
				"tagstat"	=> \$cv{'tagstat'},	# lists k/v used in osm file
				"declutter"	=> \$cv{'declutter'},
				"allowiconmove"	=> \$cv{'allowiconmove'},
				"help"		=> \$cv{'help'},		# 
				"oneways"	=> \$cv{'oneway'},
				"onewaycolor:s" => \$cv{'onewaycolor'},
				"nobridge"	=> \$cv{'nobridge'},
				"nolabel"	=> \$cv{'nolabel'},
				"place:s"	=> \$cv{'place'},		# place to draw
				"placefile:s"	=> \$cv{'placefile'},		# file to look for places
				"lonrad:f"	=> \$cv{'lonrad'},
				"latrad:f"	=> \$cv{'latrad'},
				"ruler:i"	=> \$cv{'ruler'},
				"rulercolor:s"	=> \$cv{'rulercolor'},
				"rulerbackground:s"	=> \$cv{'rulerbackground'},
				"scale:i"		=> \$cv{'scale'},
				"scalecolor:s"	=> \$cv{'scalecolor'},
				"scalebackground:s"	=> \$cv{'scalebackground'},
				"scaleset:i"	=> \$cv{'scaleset'},
				"rulescaleset:i" => \$cv{'rulescaleset'},
				"routelabelcolor:s"	=> \$cv{'routelabelcolor'},		
				"routelabelsize:i"	=> \$cv{'routelabelsize'},		
				"routelabelfont:s"	=> \$cv{'routelabelfont'},		
				"routelabeloffset:i"	=> \$cv{'routelabeloffset'},		
				"routeicondist:i"	=> \$cv{'routeicondist'},
				"routeiconscale:f"	=> \$cv{'routeiconscale'},
				"icondir:s"		=> \$cv{'icondir'},
				"foot:s"		=> \$cv{'foot'},
				"footcolor:s"		=> \$cv{'footcolor'},
				"footbackground:s"		=> \$cv{'footbackground'},
				"footsize:i"		=> \$cv{'footsize'},
				"head:s"		=> \$cv{'head'},
				"headcolor:s"		=> \$cv{'headcolor'},
				"headbackground:s"		=> \$cv{'headbackground'},
				"headsize:i"		=> \$cv{'headsize'},
				"poifile:s"	=> \$cv{'poifile'},		
				"relid:i"	=> \$cv{'relid'},
				"rectangles:s"	=> \$cv{'rectangles'},
				"pagenumbers:s"	=> \$cv{'pagenumbers'},
				"multionly"	=> \$cv{'multionly'},		# draw only areas from multipolygons
				"ra:s"		=> \$cv{'ra'},		# 
				"debug" 	=> \$cv{'debug'},			# turns debug messages on 
				"cie" 	=> \$cv{'cie'},			# turns debug messages on 
				"verbose" 	=> \$cv{'verbose'} ) ;		# turns twitter on


}

sub printConfigDescriptions {

	my @texts = @initial ;

	@texts = sort {$a->[0] cmp $b->[0]} @texts ;

	print "\nconfig value descriptions\n\n" ;
	printf "%-25s %-50s %-20s\n" , "key" , "description", "default" ;
	foreach my $t (@texts) {
		my $def = $t->[1] ;
		if ($def eq "") { $def = "<EMPTY>" ; }
		printf "%-25s %-50s %-20s\n" , $t->[0] , $t->[2], $def ;
	}
	print "\n" ;
}


1 ;


