#####################################################################################################
# OSM-MATRIX - create a gpx-grid for show the grid of osm-matrix by monty																 #
#                                                                                                   																			#
# Copyright (C) 2010 Jan Tappenbeck, osm(at)tappenbeck.net                                          															#
# components in use of garry68 (gpx-file-componetents), components for tile-calculation (http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames)				#
#                                                                                                   																			#
# This program is free software; you can redistribute it and/or modify it under the terms of the    													#
# GNU General Public License as published by the Free Software Foundation; either version 3 of      												#
# the License, or (at your option) any later version.                                               																#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;         												#
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.         										#
# See the GNU General Public License for more details.                                              															#
# You should have received a copy of the GNU General Public License along with this program;        													#
# if not, see <http://www.gnu.org/licenses/>.                                                       																#	
#                                                                                                   																			#
#  DEVELOP and TESTED in WINDOWS VISTA 64bit / ActivePerl																		#
#																												#
#####################################################################################################

#*****************
# HISTORIE
#*****************
# 2010-01-25  first in use by haiti-earthquake

#!/usr/bin/perl
# permalink für test

use strict;
use LWP;
#use OSM::osm;
#use OSM::osm_jt ; #paketbasis von gary68
use Math::Trig;
use Getopt::Long;

my $go_Help			= 0;		# help please
my $matrix_zoom 	= 15;
my $counter 		= 0;
my $output_prefix 	= "";
my $coord_west 		= -9999;
my $coord_north 	= -9999;
my $coord_east	 	= -9999;
my $coord_south 	= -9999;

GetOptions
(
	"help!"					=> \$go_Help,
	"name=s"				=> \$output_prefix,
	"w=f"					=> \$coord_west,
	"n=f"					=> \$coord_north,
	"e=f"					=> \$coord_east,
	"s=f"					=> \$coord_south,
	"z=i"					=> \$matrix_zoom,
) or Usage();

Usage() if( $go_Help );									# Hilfe wenn erwuenscht

#----------------------------------------------------------------------------------------
sub Usage
{
	my( $message ) = @_;

	if( $message )
	{
		print "$message\n";
	}

	print "\tgpx-grid for osm-matrix\n\n";
	
	print "\tparameters\n";
	
	print "\tosm [s]\tinputfile\n";
	print "\tname [s]\tprefix for the gpx-file -> osm_matrix-[name].gpx\n";
	print "\n\tmin/max geographic coordinates\n";
	print "\tw [f]\twest-limit\n";
	print "\tn [f]\tnorth-limit\n";
	print "\te [f]\teast-limit\n";
	print "\ts [f]\tsouth-limit\n";

	print "\n\tz [i]\ttile-zoom - default 15\n";
	
	print "\n\n\t-- end --\n";

	exit 0;					# beenden
}


my $output_gpx_filename = "osm_matrix_".$output_prefix.".gpx";

if ($coord_east < $coord_west){
  print "*"x51,"\n";
  print "******* the area change the date-border !!! *******\n";
  print "******* please split the area into 2 grid *******\n";
  print "*"x51,"\n";
  exit 0;
}

#if ($coord_north < $coord_south){
#  print "*"x59,"\n";
#  print "******* north < south - please check the parameters *******\n";
#  print "*"x59,"\n";
#  exit 0;
#}

# transform geo. coord into tile-coord. for the define zoom-scale


my ($lat_start,$lon_end) = getTileNumber($coord_south, $coord_west, $matrix_zoom);
my ($lat_end,$lon_start) = getTileNumber($coord_north, $coord_east, $matrix_zoom);

# to get the right border of the selected area
$lon_end++;

print "\n\ngeo-coord.\n";
print "buttom-left: lon=".$coord_south."  lat=".$coord_west."\n";
print "top-right  : lon=".$coord_north." lat=".$coord_east."\n\n";

print "\n\ntile-numbers\n";
print "buttom-left: lat=".$lat_start." / lon=".$lon_start."\n";
print "top-right  : lat=".$lat_end." / lon=".$lon_end."\n\n";

open (my $gpxfile, ">", $output_gpx_filename) || die ("Can't open html output file: ".$output_gpx_filename) ;
printGPXHeader($gpxfile);

print "==> horizontal lines\n";
 for( my $h=$lon_start; $h <= $lon_end; $h++ )
   {
	print $lat_start." / ".$h." - ".$lat_end." / ".$h."\n";
	my ($lat1, $lon1, $lat_tmp1, $lon_tmp1) =  Project($lat_start,$h,$matrix_zoom);
	my ($lat2, $lon2, $lat_tmp2, $lon_tmp2) =  Project($lat_end,$h,$matrix_zoom);
    print $lat1." / ".$lon1." - ".$lat2." / ".$lon2."\n";
	printOpenGpsTrack($gpxfile, $counter);
	printGPXTrackpoint($gpxfile, $lon1, $lat1);
	printGPXTrackpoint($gpxfile, $lon2, $lat2);
	printCloseGpsTrack($gpxfile);
  }

print "==> vertical lines\n";
for( my $r=$lat_start; $r <= $lat_end; $r++ )
{
    print $r." / ".$lon_start." - ".$r." / ".$lon_end."\n";
	my ($lat1, $lon1, $lat_tmp1, $lon_tmp1) =  Project($r,$lon_start,$matrix_zoom);
	my ($lat2, $lon2, $lat_tmp2, $lon_tmp2) =  Project($r,$lon_end,$matrix_zoom);
    print $lat1." / ".$lon1." - ".$lat2." / ".$lon2."\n";
	printOpenGpsTrack($gpxfile, $counter);
	printGPXTrackpoint($gpxfile, $lon1, $lat1);
	printGPXTrackpoint($gpxfile, $lon2, $lat2);
	printCloseGpsTrack($gpxfile);
} 

printGPXFoot($gpxfile);

#*****************
# SUBROUTINES 
#*****************

############
# tile-calculation
############

# source for the calculation of the request coord.
# http://projekte.eiops.de/osm-matrix/?zoom=16&lat=18.49757&lon=-72.63565&layers=B0FTFFFFFFFFFT
#  http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames

sub getTileNumber {
  my ($lat,$lon,$zoom) = @_;
  my $xtile = int( ($lon+180)/360 *2**$zoom ) ;
  my $ytile = int( (1 - log(tan($lat*pi/180) + sec($lat*pi/180))/pi)/2 *2**$zoom ) ;
  return(($xtile, $ytile));
}

 sub Project {
  my ($X,$Y, $Zoom) = @_;
  my $Unit = 1 / (2 ** $Zoom);
  my $relY1 = $Y * $Unit;
  my $relY2 = $relY1 + $Unit;

  # note: $LimitY = ProjectF(degrees(atan(sinh(pi)))) = log(sinh(pi)+cosh(pi)) = pi
  # note: degrees(atan(sinh(pi))) = 85.051128..
  #my $LimitY = ProjectF(85.0511);

  # so stay simple and more accurate
  my $LimitY = pi;
  my $RangeY = 2 * $LimitY;
  $relY1 = $LimitY - $RangeY * $relY1;
  $relY2 = $LimitY - $RangeY * $relY2;
  my $Lat1 = ProjectMercToLat($relY1);
  my $Lat2 = ProjectMercToLat($relY2);
  $Unit = 360 / (2 ** $Zoom);
  my $Long1 = -180 + $X * $Unit;
  return(($Lat2, $Long1, $Lat1, $Long1 + $Unit)); # S,W,N,E
 }
 sub ProjectMercToLat($){
  my $MercY = shift();
  return( 180/pi* atan(sinh($MercY)));
 }
 sub ProjectF
 {
  my $Lat = shift;
  $Lat = deg2rad($Lat);
  my $Y = log(tan($Lat) + (1/cos($Lat)));
  return($Y);
 }

########
# gpx-track
########
# legt einen GPS-Track an
sub printGPXHeader {
	my $file = shift ;

	print $file "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" ;
	print $file "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"Gary68script\" version=\"1.1\"\n" ;
	print $file "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" ;
	print $file "    xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n" ;
}
sub printGPXFoot {
	my $file = shift ;

	print $file "</gpx>\n" ;
}
sub printGPXWaypoint {
	my ($file, $lon, $lat, $text) = @_ ;

	print $file "<wpt lat=\"", $lat, "\" lon=\"", $lon, "\">" ;
	print $file "<desc>", $text, "</desc></wpt>\n" ;
}
sub printOpenGpsTrack {
	my $file = shift ;
	my $name = shift ;

	print $file " <trk>\n";
	print $file "  <name>$name</name>\n";
	print $file "   <trkseg>\n";
}
sub printCloseGpsTrack {
	my $file = shift ;

	print $file "  </trkseg>\n" ;
	print $file "</trk>\n" ;
}
sub printGPXTrackpoint {
	my ($file, $lon, $lat) = @_ ;
	print $file "    <trkpt lat=\"", $lat, "\" lon=\"", $lon, "\">\n";
	print $file "    </trkpt>\n" ;
}
