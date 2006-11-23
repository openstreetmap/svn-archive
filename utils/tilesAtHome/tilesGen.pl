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
# Not required
my $Password = "user|password";  

# Requires OSM username for downloading data
our $Credentials = "user%40domain:password";

use Math::Trig;
sub DegToRad($){return pi * shift() / 180;}
sub RadToDeg($){return 180 * shift() / pi;}
my $LimitY = ProjectF(85.0511);
my $LimitY2 = ProjectF(-85.0511);
my $RangeY = 2 * $LimitY;
printf "LimitY = %f, %f (R %f)\n", $LimitY, $LimitY2, $RangeY;

# Specify what to render (x,y,zoom, 1)
GenerateTile(1021*4, 682*4, 13, 1);


#-----------------------------------------------------------------------------
# Render a tile
# See [[Slippy Map Tilenames]] for details of what x,y,zoom mean
#-----------------------------------------------------------------------------
sub GenerateTile(){
  my ($X, $Y, $Zoom, $Initial) = @_;
  return if($Zoom > 17);
    
  
  my ($N, $S) = Project($Y, $Zoom);
  my ($W, $E) = ProjectL($X, $Zoom);
  my $DataFile = "data.osm";
  #------------------------------------------------------
  # Download data
  #------------------------------------------------------
  if($Initial){
    unlink $DataFile if -f $DataFile;
    my $URL = sprintf("http://%s\@www.openstreetmap.org/api/0.3/map?bbox=%f,%f,%f,%f",
      $Credentials, $W, $S, $E, $N);
    
    DownloadFile($URL, $DataFile, 0, "Map data") if(!-f$DataFile);
    if(-s $DataFile == 0){
      print "No data at this location";
      return;
    }
  }
  
  # Faff around
  my $Filename = join("_", ($Zoom, $X, $Y)) . ".png";
  foreach my $OldFile("output.svg", $Filename){
    unlink $OldFile if(-f $OldFile);
  }

  my $Margin = " " x ($Zoom - 8);
  printf "%03d %s%d,%d: %1.2f - %1.2f, %1.2f - %1.2f\n", $Zoom, $Margin, $X, $Y, $S,$N, $W,$E;
  
  # Add bounding box to osmarender
  AddBounds("osm-map-features.xml",$W,$S,$E,$N);
  
  # Transform it to SVG
  xml2svg("output.svg");
  
  # Render it to PNG
  my $Width = 256; # Pixel size of tiles  
  svg2png("output.svg", $Filename, $Width);

  # Upload it
  #upload("output.png", $ID, $Password);
  
  # Say where to find the result
  printf "Done. Result saved to $Filename (%d bytes)\n", -s $Filename; 
  
  # Sub-tiles
  my $XA = $X * 2;
  my $XB = $XA + 1;
  my $YA = $Y * 2;
  my $YB = $YA + 1;
  GenerateTile($XA, $YA, $Zoom+1, 0);
  GenerateTile($XB, $YA, $Zoom+1, 0);
  GenerateTile($XA, $YB, $Zoom+1, 0);
  GenerateTile($XB, $YB, $Zoom+1, 0);
}

#-----------------------------------------------------------------------------
# Project latitude to mercatorY
#-----------------------------------------------------------------------------
sub ProjectF($){
  my $Lat = DegToRad(shift());
  my $Y = log(tan($Lat) + sec($Lat));
  return($Y);
}
#-----------------------------------------------------------------------------
# Project Y to latitudes
#-----------------------------------------------------------------------------
sub Project(){
  my ($Y, $Zoom) = @_;
  
  my $Unit = 1 / (2 ** $Zoom);
  my $relY1 = $Y * $Unit;
  my $relY2 = $relY1 + $Unit;
  
  $relY1 = $LimitY - $RangeY * $relY1;
  $relY2 = $LimitY - $RangeY * $relY2;
    
  my $Lat1 = RadToDeg(atan(sinh($relY1)));
  my $Lat2 = RadToDeg(atan(sinh($relY2)));
  return(($Lat1, $Lat2));  
}
#-----------------------------------------------------------------------------
# Project X to longitudes
#-----------------------------------------------------------------------------
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
  my($SVG, $PNG, $Size) = @_;	
  my $Cmd = sprintf("%sinkscape -w %d -h %d --export-png=%s %s", 
    "nice ", # Blank this out for use on windows
    $Size,
    $Size,
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
