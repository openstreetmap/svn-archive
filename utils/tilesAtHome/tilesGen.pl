#!/usr/bin/perl
use LWP::Simple;
use LWP::UserAgent;
use Math::Trig;
use File::Copy;
use FindBin qw($Bin);
use config;
use English '-no_match_vars';
use GD; 
use strict;
#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home
#
# Contact OJW on the Openstreetmap wiki for help using this program
#-----------------------------------------------------------------------------
# Copyright 2006, Oliver White, Etienne Cherdlu, Dirk-Lueder Kreie
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
# Read the config file
my %Config = ReadConfig("tilesAtHome.conf");
CheckConfig(%Config);

# Setup map projection
my $LimitY = ProjectF(85.0511);
my $LimitY2 = ProjectF(-85.0511);
my $RangeY = $LimitY - $LimitY2;

# Create the working directory if necessary
mkdir $Config{WorkingDirectory} if(!-d $Config{WorkingDirectory});

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
elsif ($Mode eq "loop") {
  # ----------------------------------
  # Continuously process requests from server
  # ----------------------------------
  while(1){
    ProcessRequestsFromServer();
    uploadIfEnoughTiles();
    sleep(60);
  }
}
elsif ($Mode eq "upload") {
  upload();
}
elsif ($Mode eq "upload_conditional") {
  uploadIfEnoughTiles();
}
elsif ($Mode eq "") {
  # ----------------------------------
  # Normal mode downloads request from server
  # ----------------------------------
  ProcessRequestsFromServer();
}
else{
  # ----------------------------------
  # "help" as first argument tells how to use the program
  # ----------------------------------
  my $Bar = "-" x 78;
  print "\n$Bar\nOpenStreetMap tiles\@home client\n$Bar\n";
  print "Usage: \nNormal mode:\n  \"$0\", will download requests from server\n";
  print "Specific area:\n  \"$0 xy [x] [y]\"\n  (x and y coordinates of a zoom-12 tile in the slippy-map coordinate system)\n  See [[Slippy Map Tilenames]] on wiki.openstreetmap.org for details\n";
  print "Other modes:\n";
  print "  $0 loop - runs continuously\n";
  print "  $0 upload - uploads any tiles\n";
  print "  $0 upload_conditional - uploads tiles if there are many waiting\n";
  print "\nGNU General Public license, version 2 or later\n$Bar\n";

}

sub uploadIfEnoughTiles{
  my $Count = 0;
  opendir(my $dp, $Config{WorkingDirectory}) || return;
  while(my $File = readdir($dp)){
    $Count++ if($File =~ /tile_.*\.png/);
  }
  closedir($dp);
  
  if($Count < 200){
    print "Not uploading yet, only $Count tiles\n";
  }
  else{
    upload();
  }
}

sub upload{
  my $UploadScript = "$Bin/upload.pl";
  print "Uploading... ($UploadScript)\n";
  `$UploadScript`;
}
#-----------------------------------------------------------------------------
# Ask the server what tileset needs rendering next
#-----------------------------------------------------------------------------
sub ProcessRequestsFromServer(){
  my $LocalFilename = "$Config{WorkingDirectory}request.txt";
  
  # ----------------------------------
  # Download the request, and check it
  # Note: to find out exactly what this server is telling you, 
  # add ?help to the end of the URL and view it in a browser.
  # It will give you details of other help pages available,
  # such as the list of fields that it's sending out in requests
  # ----------------------------------
  killafile($LocalFilename);
  DownloadFile(
    "http://dev.openstreetmap.org/~ojw/Requests/",  # TODO: this should be in config file
    $LocalFilename, 
    0, 
    "Request from server");
    
  if(! -f $LocalFilename){
    print "Couldn't get request from server";
    sleep(5 * 60);
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
  if($ValidFlag eq "XX"){
    print "Nothing to do!  Please wait a while, and check later for requests\n";
    sleep(40 * 60);
  }
  elsif($ValidFlag ne "OK"){
    print "Server doesn't seem to be responding as expected\n";

    # this timeout should adapt (like exponential backoff), requires the 
    # looping to happen inside this script, like  requests.pl -f 
    print "Sleeping a while to reduce server load\n";
    sleep(5 * 60);
    return;
  }
  
  # Check what format the results were in
  # If you get this message, please do check for a new version, rather than
  # commenting-out the test - it means the field order has changed and this
  # program no longer makes sense!
  if($Version != 3){
    print "Server is speaking a different version of the protocol to us\n";
    print "Check to see whether a new version of this program was released\n";
    exit(2);
  }
  
  # Information text to say what's happening
  print "OK, got something... (from the \"$ModuleName\" server module)\n";
  print "Doing zoom level $Z, location $X, $Y\n";
  
  # Create the tileset requested
  GenerateTileset($X, $Y, $Z);
}

#-----------------------------------------------------------------------------
# Render a tile (and all subtiles, down to a certain depth)
#-----------------------------------------------------------------------------
sub GenerateTileset(){
  my ($X, $Y, $Zoom) = @_;
    
  my ($N, $S) = Project($Y, $Zoom);
  my ($W, $E) = ProjectL($X, $Zoom);
  
  printf("Doing area around %f,%f\n", ($N+$S)/2, ($W+$E)/2);

  my $DataFile = "data-$PID.osm";

  # Adjust requested area to avoid boundary conditions
  my $N1 = $N + $Config{BorderN};
  my $S1 = $S - $Config{BorderS};
  my $E1 = $E + $Config{BorderE};
  my $W1 = $W - $Config{BorderW};


  # TODO: verify the current system cannot handle segments/ways crossing the 
  # 180/-180 deg meridian and implement proper handling of this case, until 
  # then use this workaround: 

  if($W1 <= -180) {
    $W1 = -180; # api apparently can handle -180°
  }
  if($E > 180) {
    $E1 = 180;
  }

  #------------------------------------------------------
  # Download data
  #------------------------------------------------------
  killafile($DataFile);
  my $URL = sprintf("http://%s:%s\@www.openstreetmap.org/api/0.3/map?bbox=%f,%f,%f,%f",
    $Config{OsmUsername}, $Config{OsmPassword}, $W1, $S1, $E1, $N1);
  
  DownloadFile($URL, $DataFile, 0, "Map data to $DataFile");
  if(-s $DataFile == 0){
    printf("No data at this location\n");
    printf("Trying smaller slices...\n");

    copy("stub.osm",$DataFile) or die "Cannot create $DataFile from stub.osm"; 

    my $slice=(($E1-$W1)/10); # A chunk is one tenth of the width

    for (my $i = 0 ; $i<10 ; $i++) {
      $URL = sprintf("http://%s:%s\@www.openstreetmap.org/api/0.3/map?bbox=%f,%f,%f,%f",
        $Config{OsmUsername}, $Config{OsmPassword}, ($W1+($slice*$i)), $S1, ($W1+($slice*($i+1))), $N1);
      my $DataFile1 = "data-$PID-$i.osm";
      DownloadFile($URL, $DataFile1, 0, "Map data to $DataFile1");
      if(-s $DataFile1 == 0){
        printf("No data here either\n");
        exit(1);
      }
    appendOSMfile($DataFile,$DataFile1);
    killafile($DataFile1);
    }
  }
  
  # Faff around
  for (my $i = $Zoom ; $i <= $Config{MaxZoom} ; $i++) {
    killafile("$Config{WorkingDirectory}output-$PID-z$i.svg");
  }

  my $Margin = " " x ($Zoom - 8);
  printf "%03d %s%d,%d: %1.2f - %1.2f, %1.2f - %1.2f\n", $Zoom, $Margin, $X, $Y, $S,$N, $W,$E;
  
  # Add bounding box to osmarender
  # then set the data source
  # then transform it to SVG
  for (my $i = $Zoom ; $i <= $Config{MaxZoom} ; $i++) {
  
    # Create a new copy of osmarender
    copy(
      "osm-map-features-z$i.xml",
      "osm-map-features-$PID-z$i.xml")
       or die "Cannot make copy of osm-map-features-z$i.xml"; 
    
    # Update the osmarender with details of what to do (where to get data, what bounds to use)
    AddBounds("osm-map-features-$PID-z$i.xml",$W,$S,$E,$N);    
    SetDataSource("osm-map-features-$PID-z$i.xml");
    
    # Render the file
    xml2svg(
      "osm-map-features-$PID-z$i.xml",
      "$Config{WorkingDirectory}output-$PID-z$i.svg");
    
    # Delete temporary osmarender
    killafile("osm-map-features-$PID-z$i.xml");
  }
  
  # Delete OSM map data
  killafile($DataFile);
  
  # Find the size of the SVG file
  my ($ImgW,$ImgH,$Valid) = getSize("$Config{WorkingDirectory}output-$PID-z$Config{MaxZoom}.svg");

  # Render it as loads of recursive tiles
  my $progress = 0;
  RenderTile($X, $Y, $Y, $Zoom, $N, $S, $W, $E, 0,0,$ImgW,$ImgH,$ImgH,0);

  # Clean-up he SVG files
  for (my $i = $Zoom ; $i <= $Config{MaxZoom} ; $i++) {
    killafile("$Config{WorkingDirectory}output-$PID-z$i.svg");
  }
}

#-----------------------------------------------------------------------------
# Render a tile
#   $X, $Y, $Zoom - which tile
#   $N, $S, $W, $E - bounds of the tile
#   $ImgX1,$ImgY1,$ImgX2,$ImgY2 - location of the tile in the SVG file
#-----------------------------------------------------------------------------
sub RenderTile(){
  my ($X, $Y, $Ytile, $Zoom, $N, $S, $W, $E, $ImgX1,$ImgY1,$ImgX2,$ImgY2,$ImageHeight,$empty) = @_;
  
  return if($Zoom > $Config{MaxZoom});

  # no need to render subtiles if empty
  return if($empty == 1);
  
  my $Filename = tileFilename($X, $Ytile, $Zoom);

  # Render it to PNG
  printf "$Filename: Lat %1.3f,%1.3f, Long %1.3f,%1.3f, X %1.1f,%1.1f, Y %1.1f,%1.1f\n", $N,$S,$W,$E,$ImgX1,$ImgX2,$ImgY1,$ImgY2; 
  my $Width = 256 * (2 ** ($Zoom - 12));  # Pixel size of tiles  
  my $Height = 256; # Pixel height of tile
  svg2png($Zoom, $Filename, $Width, $Height,$ImgX1,$ImgY1,$ImgX2,$ImgY2,$ImageHeight,$X,$Y,$Ytile);

  if ((-s $Filename < 1000) and ( $Zoom == 12 )) {
    $empty=1;
  }

  # Get progress percentage 
  if($empty == 1) {
    # leap forward because this tile and all higher zoom tiles of it are "done" (empty).
    for (my $j = $Config{MaxZoom} ; $j >= $Zoom ; $j--) {
      $GenerateTileset::progress += 2 ** ($Config{MaxZoom}-$j);
    }
  }
  else {
    $GenerateTileset::progress += 1;
  }
  my $progress=$GenerateTileset::progress;  
  #TODO: instead of putting 63 calculate number of tiles depending on min and max zoom:
  my $progressPercent=$progress*100/63;
  printf "Job %1.1f %% done.\n", $progressPercent;


  # Sub-tiles
  my $MercY2 = ProjectF($N);
  my $MercY1 = ProjectF($S);
  my $MercYC = 0.5 * ($MercY1 + $MercY2);
  my $LatC = ProjectMercToLat($MercYC);
  
  my $ImgYCP = ($MercYC - $MercY1) / ($MercY2 - $MercY1);
  my $ImgYC = $ImgY1 + ($ImgY2 - $ImgY1) * $ImgYCP;
  
  my $YA = $Ytile * 2;
  my $YB = $YA + 1;

  RenderTile($X, $Y, $YA, $Zoom+1, $N, $LatC, $W, $E, $ImgX1, $ImgYC, $ImgX2, $ImgY2,$ImageHeight,$empty);
  RenderTile($X, $Y, $YB, $Zoom+1, $LatC, $S, $W, $E, $ImgX1, $ImgY1, $ImgX2, $ImgYC,$ImageHeight,$empty);

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
    "http://almien.co.uk/OSM/Places/Download/$File", # TODO: should be config option. TODO: should be SVN. TODO: should be called
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
  my($MapFeatures,$SVG) = @_;
  my $Cmd = sprintf("%s \"%s\" tr %s %s > \"%s\"",
    $Config{Niceness},
    $Config{XmlStarlet},
    "osmarender.xsl",
    "$MapFeatures",
    $SVG);
  
  print STDERR "Transforming ...";
  `$Cmd`;
  print STDERR " done\n";
}

#-----------------------------------------------------------------------------
# Render a SVG file
#-----------------------------------------------------------------------------
sub svg2png(){
  my($Zoom, $PNG, $SizeX, $SizeY, $X1, $Y1, $X2, $Y2, $ImageHeight, $X, $Y, $Ytile) = @_;

  my $TempFile = $PNG."_part";
  
  my $Cmd = sprintf("%s \"%s\" -w %d -h %d --export-area=%f:%f:%f:%f --export-png=\"%s\" \"%s%s\"", 
    $Config{Niceness},
    $Config{Inkscape},
    $SizeX,
    $SizeY,
    $X1,$Y1,$X2,$Y2,
    $TempFile,
    $Config{WorkingDirectory},
    "output-$PID-z$Zoom.svg");
  
  print STDERR "Rendering ...";
  `$Cmd`;
  print STDERR " done\n";

  splitImageX($TempFile, $Config{WorkingDirectory}, 12, $X, $Y, $Zoom, $Ytile);
  
  unlink($TempFile);

}
sub writeToFile(){
  open(my $fp, ">", shift()) || return;
  print $fp shift();
  close $fp;
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
# Set data source file name in map-features file
#-----------------------------------------------------------------------------
sub SetDataSource(){
  my ($Filename) = @_;
  
  # Read the old file
  open(my $fpIn, "<", "$Filename");
  my $Data = join("",<$fpIn>);
  close $fpIn;
  die("no such $Filename") if(! -f $Filename);
  
  # Change some stuff
  my $DataSource = sprintf("data-%s.osm",
    $PID);
  
  $Data =~ s/(  data=\").*(  scale=\")/\1$DataSource\"\n\2/s;

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

  return(sprintf("%s/tile_%d_%d_%d.png",$Config{WorkingDirectory},$Zoom,$X,$Y));
}

#-----------------------------------------------------------------------------
# Set data source file name in map-features file
#-----------------------------------------------------------------------------
sub appendOSMfile(){
  my ($Datafile,$Datafile1) = @_;
  
  # Strip the trailing </osm> from the datafile
  open(my $fpIn1, "<", "$Datafile");
  my $Data = join("",<$fpIn1>);
  close $fpIn1;
  die("no such $Datafile") if(! -f $Datafile);
    
  $Data =~ s/<\/osm>//s;

  # Save back to the datafile file
  open(my $fpOut1, ">$Datafile");
  print $fpOut1 $Data;
  close $fpOut1;

  # Read the merge file remove the xml prolog and opening <osm> tag and append to the datafile
  open(my $fpIn2, "<", "$Datafile1");
  my $Data = join("",<$fpIn2>);
  close $fpIn2;
  die("no such $Datafile1") if(! -f $Datafile1);
    
  $Data =~ s/.*server\">//s;

  # Append to the data file
  open(my $fpOut2, ">>", "$Datafile");
  print $fpOut2 $Data;
  close $fpOut2;
}


#-----------------------------------------------------------------------------
# Split a tileset image into tiles
#-----------------------------------------------------------------------------
sub splitImageX(){
  my ($File, $OutputDir, $ZOrig, $X, $Y, $Z, $Ytile) = @_;
  
  # Size of tiles
  my $Pixels = 256;
  
  # Number of tiles
  my $Size = 2 ** ($Z - $ZOrig);
  
  # Load the tileset image
  print "Loading $File ($Size x $Size)\n";
  my $Image = newFromPng GD::Image($File);
  
  # Use one subimage for everything, and keep copying data into it
  my $SubImage = new GD::Image($Pixels,$Pixels);
  
  # For each subimage
  for(my $xi = 0; $xi < $Size; $xi++){
  
    # Get a tiles'worth of data from the main image
    $SubImage->copy($Image,
      0,                   # Dest X offset
      0,                   # Dest Y offset
      $xi * $Pixels,       # Source X offset
      0,                   # Source Y offset # always 0 because we only cut from one row
      $Pixels,             # Copy width
      $Pixels);            # Copy height
  
    # Decide what the tile should be called
    my $Filename = $OutputDir . "/" . 
      sprintf("tile_%d_%d_%d.png", 
        $Z, 
        $X * $Size + $xi, 
        $Ytile); 
    # Temporary filename
    my $Filename2 = "$Filename.cut";
    
    # Store the tile
    print " -> $Filename\n";
    WriteImage($SubImage,$Filename2);
    rename($Filename2, $Filename);
    
  }
  undef $SubImage;
}

#-----------------------------------------------------------------------------
# Write a GD image to disk
#-----------------------------------------------------------------------------
sub WriteImage(){
  my ($Image, $Filename) = @_;
  
  # Get the image as PNG data
  my $png_data = $Image->png;
  
  # Store it
  open (my $fp, ">$Filename") || die;
  binmode $fp;
  print $fp $png_data;
  close $fp;
}
