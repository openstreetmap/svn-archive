#!/usr/bin/perl
use LWP::Simple;
#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home
#
# Contact OJW on the Openstreetmap wiki for help using this program
#-----------------------------------------------------------------------------
# Copyright 2006, Oliver White
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
my $Password = "user|password";  # Not required
our $Credentials = "a%40b:c";

use Math::Trig;
sub DegToRad($){return pi * shift() / 180;}
sub RadToDeg($){return 180 * shift() / pi;}
my $LimitY = ProjectF(85.0511);
my $LimitY2 = ProjectF(-85.0511);
my $RangeY = 2 * $LimitY;
printf "LimitY = %f, %f (R %f)\n", $LimitY, $LimitY2, $RangeY;

our $MaxZoom = 17;
GenerateTileset(1021*4, 682*4, 13);

sub GenerateTileset(){
  my ($X, $Y, $Zoom) = @_;
    
  my ($N, $S) = Project($Y, $Zoom);
  my ($W, $E) = ProjectL($X, $Zoom);
  my $DataFile = "data.osm";
  #------------------------------------------------------
  # Download data
  #------------------------------------------------------
  #killafile($DataFile);
  my $URL = sprintf("http://%s\@www.openstreetmap.org/api/0.3/map?bbox=%f,%f,%f,%f",
    $Credentials, $W, $S, $E, $N);
  
  #DownloadFile($URL, $DataFile, 0, "Map data");
  if(-s $DataFile == 0){
    print "No data at this location";
    return;
  }
  
  # Faff around
  my $SVG = "output.svg";
  killafile($SVG);

  my $Margin = " " x ($Zoom - 8);
  printf "%03d %s%d,%d: %1.2f - %1.2f, %1.2f - %1.2f\n", $Zoom, $Margin, $X, $Y, $S,$N, $W,$E;
  
  # Add bounding box to osmarender
  AddBounds("osm-map-features.xml",$W,$S,$E,$N);
  
  # Transform it to SVG
  xml2svg($SVG);
  
  my ($ImgW,$ImgH,$Valid) = getSize($SVG);
  RenderTile($SVG, $X, $Y, $Zoom, $N, $S, $W, $E, 0,0,$ImgW,$ImgH);
}

sub RenderTile(){
  my ($SVG, $X, $Y, $Zoom, $N, $S, $W, $E, $ImgX1,$ImgY1,$ImgX2,$ImgY2) = @_;
  
  return if($Zoom > $MaxZoom);
  
  my $Filename = "gfx/".join("_", ($Zoom, $X, $Y)) . ".png";
  #killafile $Filename;
  
  printf "$Filename: Lat %1.3f,%1.3f, Long %1.3f,%1.3f, X %1.1f,%1.1f, Y %1.1f,%1.1f\n", $N,$S,$W,$E,$ImgX1,$ImgX2,$ImgY1,$ImgY2; 
  
  # Render it to PNG
  my $Width = 256; # Pixel size of tiles  
  svg2png($SVG, $Filename, $Width,$ImgX1,$ImgY1,$ImgX2,$ImgY2);

  # Upload it
  #upload("output.png", $ID, $Password);
  
  # Say where to find the result
  #printf "Result saved to $Filename (%d bytes)\n", -s $Filename; 
  
  # Sub-tiles
  my $XA = $X * 2;
  my $XB = $XA + 1;
  my $YA = $Y * 2;
  my $YB = $YA + 1;

  my $LongC = 0.5 * ($W + $E);
  
  my $MercY2 = ProjectF($N);
  my $MercY1 = ProjectF($S);
  my $MercYC = 0.5 * ($MercY1 + $MercY2);
  my $LatC = ProjectMercToLat($MercYC);
  
  my $ImgXC = 0.5 * ($ImgX1 + $ImgX2);
  my $ImgYCP = ($MercYC - $MercY1) / ($MercY2 - $MercY1);
  my $ImgYC = $ImgY1 + ($ImgY2 - $ImgY1) * $ImgYCP;
  
  RenderTile($SVG, $XA, $YA, $Zoom+1, $N, $LatC, $W, $LongC, $ImgX1, $ImgYC, $ImgXC, $ImgY2);
  RenderTile($SVG, $XB, $YA, $Zoom+1, $N, $LatC, $LongC, $E, $ImgXC, $ImgYC, $ImgX2, $ImgY2);
  RenderTile($SVG, $XA, $YB, $Zoom+1, $LatC, $S, $W, $LongC, $ImgX1, $ImgY1, $ImgXC, $ImgYC);
  RenderTile($SVG, $XB, $YB, $Zoom+1, $LatC, $S, $LongC, $E, $ImgXC, $ImgY1, $ImgX2, $ImgYC);
}
sub killafile($){
  my $file = shift();
  unlink $file if(! -f $file);
}
sub ProjectF($){
  my $Lat = DegToRad(shift());
  my $Y = log(tan($Lat) + sec($Lat));
  return($Y);
}
sub Project(){
  my ($Y, $Zoom) = @_;
  
  my $Unit = 1 / (2 ** $Zoom);
  my $relY1 = $Y * $Unit;
  my $relY2 = $relY1 + $Unit;
  
  $relY1 = $LimitY - $RangeY * $relY1;
  $relY2 = $LimitY - $RangeY * $relY2;
    
  my $Lat1 = ProjectMercToLat($relY1);
  my $Lat2 = ProjectMercToLat($relY2);
  return(($Lat1, $Lat2));  
}
sub ProjectMercToLat($){
  my $MercY = shift();
  return(RadToDeg(atan(sinh($MercY))));
}
sub ProjectL(){
  my ($X, $Zoom) = @_;
  
  my $Unit = 360 / (2 ** $Zoom);
  my $Long1 = -180 + $X * $Unit;
  return(($Long1, $Long1 + $Unit));  
}


#-----------------------------------------------------------------------------
# Gets latest copy of osmarender from repository
#-----------------------------------------------------------------------------
sub UpdateOsmarender(){
  foreach my $File(("osm-map-features.xml", "osmarender.xsl", "Osm_linkage.png", "somerights20.png")){
  
    DownloadFile(
    "http://almien.co.uk/OSM/Places/Download/$File",
    $File,
    1,
    "Osmarender ($File)");
  }
}


#-----------------------------------------------------------------------------
# 
#-----------------------------------------------------------------------------
sub DownloadFile(){
  my ($URL, $File, $UseExisting, $Title) = @_;
  
  print STDERR "Downloading: $Title";
  
  if($UseExisting){
    mirror($URL, $File);
    }
  else{
    getstore($URL, $File);
    }
  
  printf STDERR " done, %d bytes\n", -s $File;
  
}

#-----------------------------------------------------------------------------
# Transform an OSM file (using osmarender) into SVG
#-----------------------------------------------------------------------------
sub xml2svg(){
  my($SVG) = @_;
  my $Cmd = sprintf("%sxmlstarlet tr %s %s > %s",
    "nice ", # Blank this out for use on windows
    "osmarender.xsl",
    "osm-map-features.xml",
    $SVG);
  open OUT, ">update_svg.sh";print OUT $Cmd."\n";close OUT;
  
 print STDERR "Transforming $Cmd...";
  `$Cmd`;
  print STDERR " done\n";
}

#-----------------------------------------------------------------------------
# Render a SVG file
#-----------------------------------------------------------------------------
sub svg2png(){
  my($SVG, $PNG, $Size,$X1,$Y1,$X2,$Y2) = @_;
  
  
  
  my $Cmd = sprintf("%sinkscape -w %d -h %d --export-area=%1.1f:%1.1f:%1.1f:%1.1f --export-png=%s %s", 
    "nice ", # Blank this out for use on windows
    $Size,
    $Size,
    $X1,$Y1,$X2,$Y2,
    $PNG, 
    $SVG);
  
  open OUT, ">update_png.sh";print OUT $Cmd."\n";close OUT;
  print STDERR "Rendering $Cmd...";
  `$Cmd`;
  print STDERR " done\n";
}


#-----------------------------------------------------------------------------
# Upload a rendered map to almien
#-----------------------------------------------------------------------------
sub upload(){
  my ($File, $ID, $Password) = @_;
  my $URL = "http://almien.co.uk/OSM/Places/upload.php"; # TODO
  
  my $ua = LWP::UserAgent->new(env_proxy => 0,
    keep_alive => 1,
    timeout => 60);
  $ua->protocols_allowed( ['http'] );
  $ua->agent("tilesAtHome");

  my $res = $ua->post($URL,
    Content_Type => 'form-data',
    Content => [ file => [$File], id => $ID, mp => $Password ]);
    
  if(!$res->is_success()){
    die("Post error: " . $res->error);
  } 
}
sub AddBounds(){
  my ($Filename,$W,$S,$E,$N,$Size) = @_;
  open(IN, "<", "$Filename");
  my $Data = join("",<IN>);
  close IN;

  die("no such $Filename") if(! -f $Filename);
  
  my $BoundsInfo = sprintf(
    "<bounds minlat=\"%f\" minlon=\"%f\" maxlat=\"%f\" maxlon=\"%f\" />",
    $S, $W, $N, $E);
  print "Adding $BoundsInfo\n";
  $Data =~ s/(<!--bounds_mkr1-->).*(<!--bounds_mkr2-->)/\1\n<!-- Inserted by tilesGen -->\n$BoundsInfo\n\2/s;

  open(OUT, ">$Filename");
  print OUT $Data;
  close OUT;
}
sub getSize($){
  my $SVG = shift();
  open(IN,"<",$SVG);
  while(my $Line = <IN>){
    if($Line =~ /height=\"(.*)px\" width=\"(.*)px\"/){
      return(($1,$2,1));
    }
  }
  return((0,0,0));
}