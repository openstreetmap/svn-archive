#!/usr/bin/perl -w
#-----------------------------------------------------------------------------
# OpenStreetMap osm2ozi_tah,
# Please refer to Openstreetmap wiki "Oziexplorer"
# written by Oliver Reimann
# and enhanced by Holger Issle for offline use:
# - Added Parameter Tiles (number of rings of tiles around center tile)
#   This creates a number of image files: 1 ring is 9 files, 2 rings is 25 files
# - Added Parameter size (number of 256 pixel tiles to glue together)
#   (use size = 2 for Glopus, otherwise use 5)
#   (size 5 needs ~275MB, size 6 needs ~1.5GB RAM)
# - optimzed PWconv.bat to have all tiles in one file, with error check
#-----------------------------------------------------------------------------
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
#-----------------------------------------------------------------------------

use strict;
use warnings;
use Getopt::Long;
#use LWP::Simple;
use LWP::Simple qw($ua get);
use File::Copy;
use Math::Trig;
use GD;

#-----------------------------------------------------------------------------
our $help=0;
my $coord; 
my $tilename;
my $tilesource = "http://dev.openstreetmap.org/~ojw/Tiles/tile.php";
my $tilesource_fallback1 = "http://tile.openstreetmap.org";
my $tilesource_fallback2 = "http://dev.openstreetmap.org/~ojw/Tiles/tile.php";
my $proxyname; 
my $extension = "png";
my $size=2;
my $neighbormaps=0;
my $cache=1;

Getopt::Long::Configure('no_ignore_case');
GetOptions ( 
	     'h|help|x'         => \$help, 

	     'tilesource:s'	=> \$tilesource,
	     'coord:s'		=> \$coord,
	     'tilename:s'	=> \$tilename,
	     'proxy:s'		=> \$proxyname,
	     'ext:s'		=> \$extension,
             'size:i'       => \$size,
             'neighbormaps:i'      => \$neighbormaps,
             'cache:i'       => \$cache,
	     ) or usage(1);

usage(1) if $help;
usage(1) unless ($coord || $tilename);
usage(1) unless ($extension eq "jpg" || $extension eq "png");

if ($size gt 5) {print "size to large -> for memory reasons use values lower or equal 5\n"; exit };
if ($size le 1) {print "size to small -> use values larger 1\n"; exit } ;

#-----------------------------------------------------------------------------
$ua->proxy('http',$proxyname) if $proxyname;
#$cache=1 unless $proxyname; #only cache if no proxy set 
#-----------------------------------------------------------------------------
my $tah="tah/"; mkdir $tah if $cache; 
my $out="map/"; mkdir $out;
my $tilesize = 256;

#initialise the Pathaway conversion batch program
my $PWconvBatchname = "PWconf";
Ini_batch_PWMapConvert("$PWconvBatchname");

#-----------------------------------------------------------------------------
# get the tile x and y coordinates
my ($Z,$xtile, $ytile);
if ($tilename)
{
  ($Z,$xtile,$ytile) = split(/,/, $tilename);
}else{
  ($Z,my $lat,my $lon) = split(/,/, $coord);
  ($xtile, $ytile) = getTileNumber($lat,$lon) ;
}
#-----------------------------------------------------------------------------
usage(1) if ($Z<0);
if ($Z+$size>16) {
  print "zoom+size larger than 16, please use lower zoom or lower size!\n";
  exit;
}
#-----------------------------------------------------------------------------
my $Z2 = $Z+$size; 
# $size=5 means 2**5 * 256 pixel == 7936 pixel per side, this is the limit for my 2GB Ram laptop!
# $size=2 means 1024x1024 pixel, best for Glopus

#-----------------------------------------------------------------------------
# loop through the tiles to be made
my $Width = $tilesize * 2**($Z2-$Z);
my $file ;

for (my $xloop = $xtile - $neighbormaps;$xloop <= $xtile + $neighbormaps;$xloop++) {
  for (my $yloop = $ytile - $neighbormaps;$yloop <= $ytile + $neighbormaps;$yloop++) {
    $file = "OSM_z${Z}_x${xloop}_y${yloop}_p${Width}" ;
    print "########## start generating $file \n";
    MakeMapTile ($xloop,$yloop,$Z,$Z2,$extension);
    MakeMapCal  ($xloop,$yloop,$Z,$Z2,$extension);
  }
}

#printNeighborMap( $xtile , $ytile , $Z, $neighbormaps );

print "\nIf you intend to use the PWconf.bat file first convert all $extension files\n";
print "to .BMP with 256 colors.\n\n";

##############################################################################
# download the tiles and glow them togehter
##############################################################################
sub MakeMapTile{
  my ($xtile,$ytile) = @_;

  print "##### start $0:MakeMapTile ; ";
  print "(Z,x,y) =  ($Z,$xtile,$ytile)\n" ;

  my $xtile1 = $xtile * 2**($Z2-$Z);
  my $xtile2 = $xtile * 2**($Z2-$Z) + 2**($Z2-$Z) -1;
  my $ytile1 = $ytile * 2**($Z2-$Z);
  my $ytile2 = $ytile * 2**($Z2-$Z) + 2**($Z2-$Z) -1;

  print "zoom=$Z2 xtile=$xtile1:$xtile2 ytile=$ytile1:$ytile2  \n";

  if (-s "${out}${file}.$extension") {
    print "file exist: ${out}${file}.$extension\n";
    return;
  }

  my $tile1;
  my $img = GD::Image->newTrueColor($Width,$Width);
  for(my $X = $xtile1; $X <= $xtile2; $X++) {
    for(my $Y = $ytile1; $Y <= $ytile2; $Y++) {
      ($tile1) = getsaveTileWithCache   ($X,$Y) if     $cache;
      ($tile1) = getsaveTileWithoutCache($X,$Y) unless $cache;
      my $tile = GD::Image->new($tile1);
      $img->copy($tile, ($X-$xtile1)*$tilesize, ($Y-$ytile1)*$tilesize ,0,0,$tilesize,$tilesize); 
    }
    printf  "tileset %i of  %i done\n" , $X-$xtile1 , $xtile2-$xtile1 ; 
  }

  print "write image file: ${out}${file}.$extension \n";
  $img->trueColorToPalette ; # reduce color to 256

  unlink <${out}${file}*>;
  if ($extension eq "jpg") {
    open(my $fpOut, ">${out}${file}.jpg"); binmode $fpOut ; print $fpOut $img->jpeg(92); close $fpOut;
  } elsif ($extension eq "png"){
    open(my $fpOut, ">${out}${file}.png"); binmode $fpOut ; print $fpOut $img->png; close $fpOut;
  } else {
    die "ERROR!!, unknow file format"; exit
  };

  undef $img ; # delete image-variable to save memory
}
##############################################################################
# generates all the calibration files
##############################################################################
sub MakeMapCal{ 
  my ($xtile,$ytile) = @_;

  my($S,$W,$N,$E) = Project($xtile , $ytile , $Z) ;
  print "(S,W,N,E) = ($S,$W,$N,$E)\n";

  print "##### start $0:MakeMapCal \n";

  print "generate calibration file for OZI: $out$file.map\n";
  GenCalibrationMap_OZI("${out}${file}.map", "$file",$W,$S,$E,$N,$Width);

  print "generate calibration file for Fugawi: $out${file}.jpr\n";
  GenCalibrationMap_Fugawi("$out${file}.jpr","$file",$W,$S,$E,$N,$Width); 

  print "generate calibration file for TTQV: $out${file}_$extension.cal\n";
  GenCalibrationMap_TTQV("${out}${file}_$extension.cal", "$file",$W,$S,$E,$N,$Width);

  print "generate calibration file for Glopus: $out${file}.kal\n";
  GenCalibrationMap_Glopus("${out}${file}.kal", "$file",$W,$S,$E,$N,$Width);

  print "generate calibration file for Pathaway with converter: $out${file}.pwm\n";
  GenCalibrationMap_PathawayConverter("${out}${file}.pwm", "$file",$W,$S,$E,$N,$Width);
  
  print "generate calibration file for Pathaway direkt: $out${file}_$extension.pwm\n";
  GenCalibrationMap_PathawayDirect("${out}${file}_${extension}.pwm", "$file",$W,$S,$E,$N,$Width);

  print "generate batch sript for Pathaway converting\n";
  Gen_batch_PWMapConvert("$PWconvBatchname", "$file");
}
##############################################################################
# print the call to generate the neighbar maps 
##############################################################################
sub printNeighborMap {
  my ($X,$Y,$Z,$T) = @_;
  my $xloop = 0;
  my $yloop = 0;

  my $add="";
  $add = $add . " -proxy=" . $proxyname  if $proxyname;
  $add = $add . " -tilesource=" . $tilesource  if $tilesource;
  $add = $add . " -size=" . $size  unless ($size eq 5);
  print "Neighbor Tiles:\n" ; 

  $xloop = $X - $T;
  $yloop = $Y - $T;

  for ($xloop = $X - $T;$xloop <= $X + $T;$xloop++) {
    for ($yloop = $Y - $T;$yloop <= $Y + $T;$yloop++) {
      print "b_osm2ozi_tah.pl " . " -tilename=" . $Z . "," . ($xloop) . "," . ($yloop) . $add . "\n";
    }
  }
}

##############################################################################
# transform the latitude and logitude to the OSM tile number -> needed for tile-download 
##############################################################################
sub getTileNumber {
  my ($lat,$lon) = @_;
  my $xtile = int( ($lon+180)/360 *2**$Z ) ;
  my $ytile = int( (1 - log(tan($lat*pi/180) + sec($lat*pi/180))/pi)/2 *2**$Z ) ;
  return(($xtile, $ytile));
}

##############################################################################
# download the specific tile, no local cache is build or use
# the sub try various internet adress
##############################################################################
sub getsaveTileWithoutCache {
  my ($X,$Y) = @_;
  my $Image;  
  $Image = getTile($X,$Y,$tilesource);
  if ( length($Image) < 100  ) {
   print "!! problems with primary tile server, switching over to fallback $tilesource_fallback1\n" ; 
   $Image = getTile($X,$Y,$tilesource_fallback1);
  }
  if ( length($Image) < 100  ) {
   print "!! problems with fallback1 tile server, switching over to fallback $tilesource_fallback2\n" ; 
   $Image = getTile($X,$Y,$tilesource_fallback2);
  }
  #die "filesize to small!\n" if ( length($Image) < 100  );
  if ( length($Image) < 100  ) {
    print "!! problems with fallback tileserver -> using blank tile \n";
    $Image = GD::Image->newTrueColor($tilesize,$tilesize)->png ;
  }
  return (($Image));
} 

##############################################################################
# download the specific tile, there is used a local cache structure. Any tile 
# which is older than 7 days (ctime) will be reloaded. 
# the sub try various internet adress
##############################################################################
sub getsaveTileWithCache {
  my ($X,$Y) = @_;
  my $file2 = $tah . "/" . $Z2 . "/" . $X . "/" . $Y . ".png" ;
 
  if (! -d $tah . "/" . $Z2 . "/" . $X) { 
    mkdir $tah . "/" . $Z2 ;
    mkdir $tah . "/" . $Z2 . "/" . $X ; 
  }

  my $Image;  
  my $bytes = (-s "$file2") ; $bytes=0 if (! $bytes) ;
  my $ctime = (-C "$file2") ; $ctime=0 if (! $ctime) ;
  if ( $bytes < 100 || $ctime>7 ) {
    print "reload file $file2, filesize=${bytes}bytes\n" if ($bytes>0);
    $Image = getTile($X,$Y,$tilesource);
    savePng($Image, $file2 );
    $bytes = (-s "$file2") ; $bytes=0 if (! $bytes) ;
  } else {
   open(my $fp, "<", "$file2");
   binmode $fp;
   $Image = join("",<$fp>);
   close $fp;
  }
  $bytes = (-s "$file2") ; $bytes=0 if (! $bytes) ;
  if ( $bytes < 100  ) {
   print "!! problems with primary tile server, switching over to fallback $tilesource_fallback1\n" ; 
   print "reload file $file2, filesize=${bytes}bytes\n" if ($bytes>0);
   $Image = getTile($X,$Y,$tilesource_fallback1);
   savePng($Image, $file2 );
  }
  if ( length($Image) < 100  ) {
   print "!! problems with fallback1 tile server, switching over to fallback $tilesource_fallback2\n" ; 
   print "reload file $file2, filesize=${bytes}bytes\n" if ($bytes>0);
   $Image = getTile($X,$Y,$tilesource_fallback2);
   savePng($Image, $file2 );
  }
  if ( length($Image) < 100  ) {
    print "!! problems with fallback tileserver -> using blank tile \n";
    $Image = GD::Image->newTrueColor($tilesize,$tilesize)->png ;
  }
  $bytes = (-s "$file2") ; $bytes=0 if (! $bytes) ;
  die "filesize of $file2 to small!\n" if ( $bytes < 100  );
  return ($Image);
}
##############################################################################
# helper sub for getsaveTileWithCache and getsaveTileWithoutCache
##############################################################################
sub getTile {
  my ($X,$Y,$tilesource) = @_;
  my $URL = sprintf $tilesource . "/%d/%d/%d.png",$Z2,$X,$Y;
  print " ...  Fetching $URL\n";
  my $Data = get($URL);
  return $Data;
}
##############################################################################
# helper sub for getsaveTileWithCache() and getsaveTileWithoutCache()
##############################################################################
sub savePng {
  my ($Image, $Filename) = @_;
  open(my $fp , '>', $Filename) || return;
  binmode $fp;
  print $fp $Image;
  close $fp;
}
##############################################################################
# transform the x,y tilenumbers to the S,W,N,E edge ccordinates
##############################################################################
sub Project{
  my ($X,$Y, $Zoom) = @_;

  my $Unit = 1 / (2 ** $Zoom);
  my $relY1 = $Y * $Unit;
  my $relY2 = $relY1 + $Unit;


  my $LimitY = ProjectF(85.0511);
  my $RangeY = 2 * $LimitY;

  $relY1 = $LimitY - $RangeY * $relY1;
  $relY2 = $LimitY - $RangeY * $relY2;

  my $Lat1 = ProjectMercToLat($relY1);
  my $Lat2 = ProjectMercToLat($relY2);

  $Unit = 360 / (2 ** $Zoom);
  my $Long1 = -180 + $X * $Unit;

  return(($Lat2, $Long1, $Lat1, $Long1 + $Unit)); # S,W,N,E
}

##############################################################################
# helper sub for Project()
##############################################################################
sub ProjectMercToLat($){
  my $MercY = shift();
  return( 180/pi* atan(sinh($MercY)));
}
##############################################################################
# helper sub for Project()
##############################################################################
sub ProjectF($){
  my $Lat = pi/180 * shift();
  my $Y = log(tan($Lat) + sec($Lat));
  return($Y);
}
##############################################################################
# generates the calibration file for OZI
##############################################################################
sub GenCalibrationMap_OZI{
  my ($outFilename,$mapFilename,$W,$S,$E,$N,$Width) = @_;

my $Data = << "ENDE1" ; 
OziExplorer Map Data File Version 2.2
--Filename--
--Filename--.--ext--
1 ,Map Code,
WGS 84,WGS 84,   0.0000,   0.0000,WGS 84
Reserved 1
Reserved 2
Magnetic Variation,,,E
Map Projection,Mercator,PolyCal,No,AutoCalOnly,No,BSBUseWPX,No
Point01,xy,    0,    0,                 in, deg,  --N--,0.0,--NN--,  --W--,0.0,--WW--, grid,   , , ,N
Point02,xy, --Width_m1--, --Width_m1--, in, deg,  --S--,0.0,--SS--,  --E--,0.0,--EE--, grid,   , , ,N
Point03,xy, --Width_m1--,0,             in, deg,  --N--,0.0,--NN--,  --E--,0.0,--EE--, grid,   , , ,N
Point04,xy, 0, --Width_m1--,            in, deg,  --S--,0.0,--SS--,  --W--,0.0,--WW--, grid,   , , ,N
Projection Setup,,,,,,,,,,
Map Feature = MF ; Map Comment = MC     These follow if they exist
Track File = TF      These follow if they exist
Moving Map Parameters = MM?    These follow if they exist
MM0,Yes
MMPNUM,4
MMPXY,1,0,0
MMPXY,2,--Width--,0
MMPXY,3,--Width--,--Width--
MMPXY,4,0,--Width--
MMPLL,1,  --W--,  --N--
MMPLL,2,  --E--,  --N--
MMPLL,3,  --E--,  --S--
MMPLL,4,  --W--,  --S--
MM1B,--MM1B--
LL Grid Setup
LLGRID,No,No Grid,Yes,255,16711680,0,No Labels,0,16777215,7,1,Yes,x
Other Grid Setup
GRGRID,No,No Grid,Yes,255,16711680,No Labels,0,16777215,8,1,Yes,No,No,x
MOP,Map Open Position,0,0
IWH,Map Image Width/Height,--Width--,--Width--
ENDE1

  # Change some stuff
  my$aW=abs($W); $Data =~ s/--W--/$aW/g;
  my$aS=abs($S); $Data =~ s/--S--/$aS/g;
  my$aE=abs($E); $Data =~ s/--E--/$aE/g;
  my$aN=abs($N); $Data =~ s/--N--/$aN/g;
  $Data =~ s/--Filename--/$mapFilename/g;
  $Data =~ s/--Width--/$Width/g;
  my $Width_m1 = $Width-1;
  $Data =~ s/--Width_m1--/$Width_m1/g;
  $Data =~ s/--ext--/$extension/g;

  if ($W>0)  {$Data =~ s/--WW--/E/g} else {$Data =~ s/--WW--/W/g};
  if ($E>0)  {$Data =~ s/--EE--/E/g} else {$Data =~ s/--EE--/W/g};
  if ($N>0)  {$Data =~ s/--NN--/N/g} else {$Data =~ s/--NN--/S/g};
  if ($S>0)  {$Data =~ s/--SS--/N/g} else {$Data =~ s/--SS--/S/g};

  # calcualate meter per pixel
  my $MM1B = 36000/360*1000*($N-$S)/$Width  ;
  $Data =~ s/--MM1B--/$MM1B/g;

  # convert CR to CRLF
  $Data =~ s/\n/\r\n/g;  

  # Save back to the same location
  open(my $fpOut, ">$outFilename");
  binmode $fpOut;
  print $fpOut $Data;
  close $fpOut;
}
##############################################################################
# generates the calibration file for TTQV
##############################################################################
sub GenCalibrationMap_TTQV{
  my ($outFilename,$mapFilename,$W,$S,$E,$N,$Width) = @_;

my $Width_m1 = $Width-1;
 
my $Data = << "ENDE2" ; 
name = 10 = $mapFilename
fname = 10 = $mapFilename.$extension
nord = 6 = $N
sued = 6 = $S
ost = 6 = $E
west = 6 = $W
scale_area = 6 =  2.980008e-010
proj_mode = 10 = proj
projparams = 10 = proj=merc lon_0=9
datum1 = 10 = WGS 84# 6378137# 298.257223563# 0# 0# 0#
c1_x = 7 =  0
c1_y = 7 =  0
c2_x = 7 =  $Width_m1
c2_y = 7 =  0
c3_x = 7 =  $Width_m1
c3_y = 7 =  $Width_m1
c4_x = 7 =  0
c4_y = 7 =  $Width_m1
c1_lat = 7 =  $N
c1_lon = 7 =  $W
c2_lat = 7 =  $N
c2_lon = 7 =  $E
c3_lat = 7 =  $S
c3_lon = 7 =  $E
c4_lat = 7 =  $S
c4_lon = 7 =  $W
ENDE2

  # convert CR to CRLF
  $Data =~ s/\n/\r\n/g;  

  # Save back to the same location
  open(my $fpOut, ">$outFilename");  binmode $fpOut;
  print $fpOut $Data;
  close $fpOut;
}
##############################################################################
# generates the calibration file for Pathaway for use with an windows converter
# here can be used larger maps (size<=5)
##############################################################################
sub GenCalibrationMap_PathawayConverter{
  my ($outFilename,$mapFilename,$W,$S,$E,$N,$Width) = @_;
# create the pathaway calibration file
  open(my $fpOut, ">$outFilename");
  print $fpOut "latitudeTL=", $N, "\n";
  print $fpOut "longitudeTL=", $W, "\n";
  print $fpOut "latitudeBR=", $S, "\n";
  print $fpOut "longitudeBR=", $E, "\n";
  print $fpOut "imageType=bmp\n";
  print $fpOut "name=", $mapFilename, "\n";
  close $fpOut;
}
##############################################################################
# generates the calibration file for Pathaway for direct use
# the maps shall be have a size of 2!
##############################################################################
sub GenCalibrationMap_PathawayDirect{
  my ($outFilename,$mapFilename,$W,$S,$E,$N,$Width) = @_;
# create the pathaway calibration file
  open(my $fpOut, ">$outFilename");
  print $fpOut "latitudeTL=$N\n";
  print $fpOut "longitudeTL=$W\n";
  print $fpOut "latitudeBR=$S\n";
  print $fpOut "longitudeBR=$E\n";
  print $fpOut "imageType=$extension\n";
  print $fpOut "name=$mapFilename\n";
  close $fpOut;
}
##############################################################################
# generates the calibration file for FUGAWI
##############################################################################
sub GenCalibrationMap_Fugawi{
  my ($outFilename,$mapFilename,$W,$S,$E,$N,$Width) = @_;
  my $Width_m1 = $Width-1;
  open(my $fpOut, ">$outFilename"); binmode $fpOut , ":crlf" ;
  print $fpOut "nm=$mapFilename\n";
  print $fpOut "it=$extension\n";
  print $fpOut "dm=WGS84\n";
  print $fpOut "pr=Mercator\n";
  print $fpOut "pp=9\n";
  print $fpOut "rp1=$N,$W,0,0\n";
  print $fpOut "rp2=$N,$E,$Width_m1,0\n";
  print $fpOut "rp3=$S,$E,$Width_m1,$Width_m1\n";
  print $fpOut "rp4=$S,$W,0,$Width_m1\n";
  close $fpOut;
}
##############################################################################
# generates the calibration file for Pathaway for direct use
# the maps shall be have a size<=3 !
##############################################################################
sub GenCalibrationMap_Glopus{
  my ($outFilename,$mapFilename,$W,$S,$E,$N,$Width) = @_;
# create the Glopus calibration file
  
my $Width_m1 = $Width-1;

my $Data = << "ENDE3" ; 
[Calibration Point 1]
Longitude = $W
Latitude = $N
Pixel = POINT(0,0)
[Calibration Point 2]
Longitude = $E
Latitude = $S
Pixel = POINT($Width_m1,$Width_m1)
[Map]
Bitmap = $mapFilename.$extension
Size = SIZE($Width,$Width)
[Projection]
Projection = Geodetic
CoSys = Geodetic
ENDE3
 
  # convert CR to CRLF
  $Data =~ s/\n/\r\n/g;  

  # Save back to the same location
  open(my $fpOut, ">$outFilename");  binmode $fpOut;
  print $fpOut $Data;
  close $fpOut;
}
##############################################################################
# generates the windows batch file for the pathaway windows converter
##############################################################################
sub Gen_batch_PWMapConvert{
  my ($Filestub,$mapFilename) = @_;
  #append current file to the convertion batch file
  open(my $fpOut, ">>$Filestub.bat");
  $Filestub =~ s/\//\\/g;
  my $map = $out . $mapFilename;
  $map =~ s/\//\\/g;
  print $fpOut "if not exist $map.bmp (\n echo You need to convert the File $map.$extension\n";
  print $fpOut " echo to 256 color BMP format before proceeding to run this batch file!\n pause\n exit\n)\n";
  print $fpOut "PWMapConvert.exe v2,$map.bmp,8,$map.pwm\n\n";
  close $fpOut;
}
##############################################################################
# generates the calibration file for Pathaway for use with an windows converter
# here can be used larger maps (size<=5)
##############################################################################
sub Ini_batch_PWMapConvert{
  my ($Filestub) = @_;
  #erase content of PW conversion batch file
  open(my $fpOut, ">$Filestub.bat");
  $Filestub =~ s/\//\\/g;
  print $fpOut "echo off\n\n";
  close $fpOut;
}
##############################################################################
# usage, help
##############################################################################
sub usage {
  print "usage: $0 [-tilename=Z,x,y] [-coord=Z,lat,lon] [-tilesource=source] [-proxy=proxyname] [-size=size] [-neighbormaps=neighbormaps] [-cache=cache]\n";
  print "This script downloads the tiles from the openstreetmap server and glue them together,\n";
  print "after that, it generates calibration files for Ozi, TTQV, Pathaway, Fugawi and Glopus.\n";
  print "\n";
  print "Z: Zoom 1..11\n\n";
  print "x,y: slippy map tilenames\n\n";
  print "lat,lon: :-)\n\n";
  print "source: \n";
  print "      - http://dev.openstreetmap.org/~ojw/Tiles/tile.php \n";
  print "      - http://dev.openstreetmap.org/~ojw/Tiles/maplint.php \n";
  print "      - http://tile.openstreetmap.org \n\n";
  print "proxyname:\nproxy and port number\n\n";
  print "size:\nthis parameter gives the count of glowed tiles. The parameter itself is the exponent to the basis of 2. eg size=2 mean a map of4x4 tiles, size of 5 means a map of 32x32 tiles. The default is 2.\n\n";
  print "neighbormaps:\nis the recursion count, it count the also generated maps in x and y direction. eg. neighbortiles=2 is in sum 5x5 maps (5=1+2x2), neighbortiles=4 is in sum 9x9 maps (9=1+2x4)). The default is 0. This parameter can cause extrem traffic. PLEASE USE THIS PARAMETER WITH CARE.\n\n";
  print "cache:\nthis parameter control the usage of the local cache, the default is 1\n\n";
  print " ------------------------------------------------------------------------------\n";
  print " GNU General Public license, version 2 or later\n";
  print " ------------------------------------------------------------------------------\n\n";
  print "example 1, part of germany : $0 -tilename=\"7,67,42\" -tilesource=\"http://tile.openstreetmap.org\" \n";
  print "example 2, Erlangen        : $0 -coord=\"11,49.58,11.00\" -proxy=\"http://proxy:3128\"\n";  
  print "example 3, Erlangen. Glopus: $0 -coord=\"13,49.58,11.00\" -size=3 -neighbormaps=2\n";  
  print "example 3, Pathaway        : $0 -coord=\"14,49.58,11.00\" -size=2 -neighbormaps=3\n";  
  print "example 4, Munich          : $0 -coord=\"11,48.15,11.58\" -tilesource=\"http://dev.openstreetmap.org/~ojw/Tiles/tile.php\" \n";
  print "example 5, Sao Paulo       : $0 -coord=\"11,-23.6681,-46.7520\" -tilesource=\"http://dev.openstreetmap.org/~ojw/Tiles/tile.php\" \n";
  print "\n";
  print "for more help please refer to http://wiki.openstreetmap.org/index.php/Oziexplorer\n\n\n";
  exit;
}
##############################################################################
