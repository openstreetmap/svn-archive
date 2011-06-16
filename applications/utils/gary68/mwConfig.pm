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

my @initial = (	["verbose",0,"print some mor information"],
			["debug",0,"print debug information"],
			["projection","merc","Used projection"],
			["ellipsoid","WGS84","Used ellipsoid"],

			["ruledefaultnodeSize","20","default size of dot for nodes"],
			["ruledefaultnodeColor","black","default color of dot for nodes"],
			["ruledefaultnodeShape","circle","default shape of node"],
			["ruledefaultnodeFromScale",0,"default fromScale of node"],
			["ruledefaultnodeToScale",50000,"default toScale of node"],

			["ruledefaultWayColor","gray","default color of way"],
			["ruledefaultWaySize",20,"default size of way"],
			["ruledefaultWayBorderColor","black","default color of border of way"],
			["ruledefaultWayBorderSize",2,"default size of border of way"],

			["ruledefaultWayFromScale",0,"default fromScale of way"],
			["ruledefaultWayToScale",50000,"default toScale of way"],

			["in","map.osm","osm in file"],
			["ini","mwconfig.ini","file with configuration values"],
			["out","mapweaver.svg","svg output name"],
			["style","mwStandardRules.txt","file with render rules"],
			["svgname","mapweaver.svg","output file name for svg graphics"],
			["size",2200,"size in pixels x axis, 300dpi"],
			["legend",0,"appearance and position of legend"],
			["bgcolor","white","background color of map"],
			["grid",0,"number of grid cells, 0 = no grid"],
			["gridcolor","black","color of grid lines"],
			["coords",0,"draw coordinate system"],
			["coordsexp",-2,"size of grid cells, exp 10"],
			["coordscolor","black","color of coordinates grid lines"],
			["clip",0,""],
			["clipbbox","",""],
			["pad",0,""],
			["ppc",6,"points per character"],
			["pdf",0,"convert output to pdf"],
			["png",0,"convert output to png"],
			["dir",0,"add directory"],
			["poi",0,"add POI directory"],
			["dirpdf",0,"create directory pdf"],
			["dircolnum",2,"number of text columns for directory pdf"],
			["dirtitle","Directory","title for directory"],
			["tagstat",0,"print tag statistics"],
			["declutter",1,""],
			["allowIconMove",0,""],
			["lineDist",10,"distance between text lines in pixels"],
			["maxCharPerLine",20,"maximum characters per line in node label"],
			["help",0,""],
			["oneway",0,"add oneway arrows"],
			["onewaycolor","white","color of oneway arrows"],
			["nobridge",0,"omit bridges and tunnels"],
			["noLabel",0,""],
			["place","","search for place name in osm file and create map"],
			["placefile","","name of file containing only place information"],
			["lonrad",2,"radius lon in km for place map"],
			["latrad",2,"radius lat in km for place map"],
			["ruler",0,"draw ruler; positions 1..4"],
			["rulercolor","black","color of ruler"],
			["rulerbackground","none","background of ruler, none=transparent"],
			["scale",0,"draw scale; positions 1..4"],
			["scalecolor","black","color of scale"],
			["scalebackground","none","color of scale background; none=transparent"],
			["scaleset",0,"set scale of map (i.e. 10000)"],
			["rulescaleset",0,"set assumed scale for rules"],
			["routelabelcolor","black",""],
			["routelabelsize",30,""],
			["routelabelfont","sans-serif",""],
			["routelabeloffset",-5,""],
			["routeicondist",70,""],
			["routeiconscale",1,""],
			["icondir","./routeicons",""],
			["poifile","","name of external POI file"],
			["relid",0,"relation ID for hikingbook"],
			["rectangles","","draw rectangles for hikingbook"],
			["pagenumbers","","add page numbers to map"],
			["ra",0,"relation analyzer mode"],
			["multionly",0,"draw only multipolygons"],
			["foot","mapweaver by gary68 - data by www.openstreetmap.org","text for footer"],
			["footcolor","black","color for footer"],
			["footbackground","none","background color for footer"],
			["footsize",40,"font size for footer"],
			["head","","text for header"],
			["headcolor","black","color for header"],
			["headbackground","none","background color for header"],
			["headsize",40,"font size for header"]

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

	print "\nCONFIG VALUES:\n\n" ;
	foreach my $k (sort keys %cv) {
		# print $k, "\n" ;
		if (defined $explanation{$k}) {
			printf "%-30s %-30s %-75s\n", $k, $cv{$k}, $explanation{$k} ;
		}
		else {
			printf "%-30s %-30s\n", $k, $cv{$k} ;
		}
	}
	print "\n" ;
}

sub readConfigFile {

	# read ini file; initial k/v pairs might be changed

	my $fileName = shift ;

	open (my $file, "<", $fileName) or die ("ERROR: could not open ini file $fileName\n") ;
	my $line = "" ;
	while ($line = <$file>) {
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
				"linedist:i"		=> \$cv{'linedist'},
				"maxcharperline:i"		=> \$cv{'maxcharperline'},
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
				"verbose" 	=> \$cv{'verbose'} ) ;		# turns twitter on


}

sub printConfigDescriptions {

	my @texts = @initial ;

	@texts = sort {$a->[0] cmp $b->[0]} @texts ;

	print "\nconfig value descriptions\n\n" ;
	printf "%-25s %-50s %-20s\n" , "key" , "description", "default" ;
	foreach my $t (@texts) {
		printf "%-25s %-50s %-20s\n" , $t->[0] , $t->[2], $t->[1] ;
	}
	print "\n" ;
}


1 ;


