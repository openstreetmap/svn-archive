#!/usr/bin/perl
use LWP::Simple;
use LWP::UserAgent;
use Math::Trig;
use strict;
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

# Secrets file, should contain all your passwords
my ($UploadPassword, $UploadURL, $OsmPassword);
ReadSecrets("secret.txt");

my $LimitY = ProjectF(85.0511);
my $LimitY2 = ProjectF(-85.0511);
my $RangeY = $LimitY - $LimitY2;

# Options: maximum zoom level to render, temporary directory for tiles
our $MaxZoom = 17; # TODO: server should specify this
our $BaseDir = "gfx";
mkdir $BaseDir if(!-d $BaseDir);

# Handle the command-line
my $Mode = shift();
if($Mode eq "xy"){
  # ----------------------------------
  # "xy" as first argument means you want to specify a tileset to render
  # ----------------------------------
  my $X = shift();
  my $Y = shift();
  my $Zoom = 12;
  print "Generating area $X,$Y,$Zoom\n";
  GenerateTileset($X, $Y, $Zoom);
}
elsif($Mode =~ /-*h(elp)?/i){
  # ----------------------------------
  # "help" as first argument tells how to use the program
  # ----------------------------------
  my $Bar = "-" x 78;
  print "\n$Bar\nOpenStreetMap tiles\@home client\n$Bar\n";
  print "Usage: \nNormal mode:\n  \"$0\", will download requests from server\n";
  print "Specific area:\n  \"$0 xy [x] [y]\"\n  (x and y coordinates of a zoom-12 tile in the slippy-map coordinate system)\n  See [[Slippy Map Tilenames]] on wiki.openstreetmap.org for details\n";
  print "\nGNU General Public license, version 2 or later\n$Bar\n";
}
else{
  # ----------------------------------
  # Normal mode downloads request from server
  # ----------------------------------
  ProcessRequestsFromServer();
}

#-----------------------------------------------------------------------------
# Ask the server what tileset needs rendering next
#-----------------------------------------------------------------------------
sub ProcessRequestsFromServer(){
  my $LocalFilename = "request.txt";
  
  # ----------------------------------
  # Download the request, and check it
  # Note: to find out exactly what this server is telling you, 
  # add ?help to the end of the URL and view it in a browser.
  # It will give you details of other help pages available,
  # such as the list of fields that it's sending out in requests
  # ----------------------------------
  killafile($LocalFilename);
  DownloadFile(
    "http://osmathome.bandnet.org/Requests/", 
    $LocalFilename, 
    0, 
    "Request from server");
    
  if(! -f $LocalFilename){
    print "Couldn't get request from server";
    return;
  }

  # Read into memory
  open(my $fp, "<", $LocalFilename) || return;
  my $Request = <$fp>;
  chomp $Request;
  close $fp;
  
  # Parse the request
  my ($ValidFlag,$Version,$X,$Y,$Z,$ModuleName) = split(/\|/, $Request);
  
  # First field is always "OK" if the server has actually sent a request
  if($ValidFlag != "OK"){
    print "Server didn't really give a good answer\n";
    return;
  }
  
  # Check what format the results were in
  # If you get this message, please do check for a new version, rather than
  # commenting-out the test - it means the field order has changed and this
  # program no longer makes sense!
  if($Version != 3){
    print "Server is speaking a different version of the protocol to us\n";
    print "Check to see whether a new version of this program was released\n";
    return;
  }
  
  # Information text to say what's happening
  print "OK, got something... (from the \"$ModuleName\" server module)\n";
  print "Doing zoom level $Z, location $X, $Y\n";
  
  # Create the tileset requested
  GenerateTileset($X, $Y, $Z);
}

#-----------------------------------------------------------------------------
# Read passwords from a configuration textfile
#-----------------------------------------------------------------------------
sub ReadSecrets(){
  open(my $fp, "<", shift()) || die("Check your password file exists!");
  my ($UploadOK, $OsmOK) = (0,0);
  
  while(my $Line = <$fp>){
    next if($Line =~ /^\s*#/); # Comments
    
    if($Line =~ /Upload\((.*) (.*)\)/){
      $UploadURL = $1;
      $UploadPassword = $2;
      $UploadOK = 1;
    }

    if($Line =~ /OsmPassword\((.*)\)/){
      $OsmPassword = $1;
      $OsmOK = 1;
    }
  }
  close $fp;
  
  if(!$UploadOK or !$OsmOK){
    die("Check your secrets file - some passwords not found");
  }
}

#-----------------------------------------------------------------------------
# Render a tile (and all subtiles, down to a certain depth)
#-----------------------------------------------------------------------------
sub GenerateTileset(){
  my ($X, $Y, $Zoom) = @_;
    
  my ($N, $S) = Project($Y, $Zoom);
  my ($W, $E) = ProjectL($X, $Zoom);
  
  printf("Doing area around %f,%f\n", ($N+$S)/2, ($W+$E)/2);
  
  my $DataFile = "data.osm";
  #------------------------------------------------------
  # Download data
  #------------------------------------------------------
  killafile($DataFile);
  my $URL = sprintf("http://%s\@www.openstreetmap.org/api/0.3/map?bbox=%f,%f,%f,%f",
    $OsmPassword, $W, $S, $E, $N);
  
  DownloadFile($URL, $DataFile, 0, "Map data");
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

#-----------------------------------------------------------------------------
# Render a tile
#   $SVG - SVG file to render
#   $X, $Y, $Zoom - which tile
#   $N, $S, $W, $E - bounds of the tile
#   $ImgX1,$ImgY1,$ImgX2,$ImgY2 - location of the tile in the SVG file
#-----------------------------------------------------------------------------
sub RenderTile(){
  my ($SVG, $X, $Y, $Zoom, $N, $S, $W, $E, $ImgX1,$ImgY1,$ImgX2,$ImgY2) = @_;
  
  return if($Zoom > $MaxZoom);
  
  my $Filename = tileFilename($X, $Y, $Zoom);
  killafile($Filename);
  
  printf "$Filename: Lat %1.3f,%1.3f, Long %1.3f,%1.3f, X %1.1f,%1.1f, Y %1.1f,%1.1f\n", $N,$S,$W,$E,$ImgX1,$ImgX2,$ImgY1,$ImgY2; 
  
  # Render it to PNG
  my $Width = 256; # Pixel size of tiles  
  svg2png($SVG, $Filename, $Width,$ImgX1,$ImgY1,$ImgX2,$ImgY2);

  # Upload it
  upload($Filename, $X, $Y, $Zoom);
    
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

#-----------------------------------------------------------------------------
# Delete a file if it exists
#-----------------------------------------------------------------------------
sub killafile($){
  my $file = shift();
  unlink $file if(-f $file);
}

#-----------------------------------------------------------------------------
# Project latitude in degrees to Y coordinates in mercator projection
#-----------------------------------------------------------------------------
sub ProjectF($){
  my $Lat = DegToRad(shift());
  my $Y = log(tan($Lat) + sec($Lat));
  return($Y);
}
#-----------------------------------------------------------------------------
# Project Y to latitude bounds
#-----------------------------------------------------------------------------
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

#-----------------------------------------------------------------------------
# Convert Y units in mercator projection to latitudes in degrees
#-----------------------------------------------------------------------------
sub ProjectMercToLat($){
  my $MercY = shift();
  return(RadToDeg(atan(sinh($MercY))));
}

#-----------------------------------------------------------------------------
# Project X to longitude bounds
#-----------------------------------------------------------------------------
sub ProjectL(){
  my ($X, $Zoom) = @_;
  
  my $Unit = 360 / (2 ** $Zoom);
  my $Long1 = -180 + $X * $Unit;
  return(($Long1, $Long1 + $Unit));  
}

#-----------------------------------------------------------------------------
# Angle unit-conversions
#-----------------------------------------------------------------------------
sub DegToRad($){return pi * shift() / 180;}
sub RadToDeg($){return 180 * shift() / pi;}

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
  
  writeToFile("update_svg.sh", $Cmd."\n");
  
  print STDERR "Transforming $Cmd...";
  `$Cmd`;
  print STDERR " done\n";
}

#-----------------------------------------------------------------------------
# Render a SVG file
#-----------------------------------------------------------------------------
sub svg2png(){
  my($SVG, $PNG, $Size,$X1,$Y1,$X2,$Y2) = @_;

  my $Cmd = sprintf("%sinkscape -w %d -h %d --export-area=%f:%f:%f:%f --export-png=%s %s", 
    "nice ", # Blank this out for use on windows
    $Size,
    $Size,
    $X1,$Y1,$X2,$Y2,
    $PNG, 
    $SVG);
  
  writeToFile("update_png.sh", $Cmd."\n");
  print STDERR "Rendering $Cmd...";
  `$Cmd`;
  print STDERR " done\n";
}
sub writeToFile(){
  open(my $fp, ">", shift()) || return;
  print $fp shift();
  close $fp;
}

#-----------------------------------------------------------------------------
# Upload a rendered map 
#-----------------------------------------------------------------------------
sub upload(){
  my ($File, $X, $Y, $Zoom) = @_;
  
  my $ua = LWP::UserAgent->new(env_proxy => 0,
    keep_alive => 1,
    timeout => 60);

  $ua->protocols_allowed( ['http'] );
  $ua->agent("tilesAtHome");

  my $res = $ua->post($UploadURL,
    Content_Type => 'form-data',
    Content => [ file => [$File], x => $X, y => $Y, z => $Zoom, mp => $UploadPassword ]);
    
  if(!$res->is_success()){
    print("Error uploading file");
    sleep(600); # wait 10 minutes for reconnect before continuing. TODO: this will lose one upload
  } 
  
  # Comment-this out to leave your files on local disk after uploading
  unlink($File);
}
#-----------------------------------------------------------------------------
# Add bounding-box information to an osm-map-features file
#-----------------------------------------------------------------------------
sub AddBounds(){
  my ($Filename,$W,$S,$E,$N,$Size) = @_;
  
  # Read the old file
  open(my $fpIn, "<", "$Filename");
  my $Data = join("",<$fpIn>);
  close $fpIn;
  die("no such $Filename") if(! -f $Filename);
  
  # Change some stuff
  my $BoundsInfo = sprintf(
    "<bounds minlat=\"%f\" minlon=\"%f\" maxlat=\"%f\" maxlon=\"%f\" />",
    $S, $W, $N, $E);
  
  $Data =~ s/(<!--bounds_mkr1-->).*(<!--bounds_mkr2-->)/\1\n<!-- Inserted by tilesGen -->\n$BoundsInfo\n\2/s;

  # Save back to the same location
  open(my $fpOut, ">$Filename");
  print $fpOut $Data;
  close $fpOut;
}
#-----------------------------------------------------------------------------
# Get the width and height (in SVG units, must be pixels) of an SVG file
#-----------------------------------------------------------------------------
sub getSize($){
  my $SVG = shift();
  open(my $fpSvg,"<",$SVG);
  while(my $Line = <$fpSvg>){
    if($Line =~ /height=\"(.*)px\" width=\"(.*)px\"/){
      close $fpSvg;
      return(($1,$2,1));
    }
  }
  close $fpSvg;
  return((0,0,0));
}
#-----------------------------------------------------------------------------
# Temporary filename to store a tile
#-----------------------------------------------------------------------------
sub tileFilename(){
  my($X,$Y,$Zoom) = @_;
  
  # Comment-out this line to generate directories
  return(sprintf("%s/%d_%d_%d.png",$BaseDir,$Zoom,$X,$Y));
  
  my $A = "$BaseDir/$Zoom";
  mkdir $A if(!-d $A);
  
  my $B = "$A/$X";
  mkdir $B if(!-d $B);
  
  my $C = "$B/$Y.png";
  return($C);
}
