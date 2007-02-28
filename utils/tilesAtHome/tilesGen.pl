#!/usr/bin/perl
use LWP::Simple;
use LWP::UserAgent;
use Math::Trig;
use File::Copy;
use FindBin qw($Bin);
use tahconfig;
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
my %Config = ReadConfig("tilesAtHome.conf", "general.conf", "authentication.conf");
CheckConfig(%Config);

# Get version number from version-control system, as integer
my $Version = '$Revision$';
$Version =~ s/\$Revision:\s*(\d+)\s*\$/$1/;
printf STDERR "This is version %d (%s) of tilesgen\n", 
    $Version, $Config{ClientVersion};

unless ($Config{Verbose})
{
    # TODO: remove this later.
    printf STDERR "Running in concise mode. Set config option Verbose=1\n";
    printf STDERR "for old, chatty behaviour.\n";
}

# check GD
eval GD::Image->trueColor(1);
if ($@ ne '') {
  print STDERR "please update your libgd to version 2 for TrueColor support";
  exit(3);
}
# Setup GD options
my $numcolors = 256; # 256 is maximum for paletted output and should be used
my $dither = 0; # dithering on or off.

# dithering off should try to find a good palette, might look ugly on 
# neighboring tiles with different features as the "optimal" palette might 
# be different for adjacent tiles. To be tested.


# Setup map projection
my $LimitY = ProjectF(85.0511);
my $LimitY2 = ProjectF(-85.0511);
my $RangeY = $LimitY - $LimitY2;

# Create the working directory if necessary
mkdir $Config{WorkingDirectory} if(!-d $Config{WorkingDirectory});

# set the progress indicator variables
my $progress = 0;
my $progressJobs = 0;
my $progressPercent = 0;
my $lastmsglen = 0;

# Handle the command-line
my $Mode = shift();
if($Mode eq "xy"){
  # ----------------------------------
  # "xy" as first argument means you want to specify a tileset to render
  # ----------------------------------
  my $X = shift();
  my $Y = shift();
  my $Zoom = 12;
  GenerateTileset($X, $Y, $Zoom);
}
elsif ($Mode eq "loop") 
{
  # ----------------------------------
  # Continuously process requests from server
  # ----------------------------------
  while(1) 
  {
    my ($did_something, $message) = ProcessRequestsFromServer();
    uploadIfEnoughTiles();
    if ($did_something == 0) {
	  talkInSleep($message, 60);
    }
  }
}
elsif ($Mode eq "upload") {
  upload();
}
elsif ($Mode eq "upload_conditional") {
  uploadIfEnoughTiles();
}
elsif ($Mode eq "version") {
  exit(1);
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
  print "  $0 version - prints out version string and exits\n";
  print "\nGNU General Public license, version 2 or later\n$Bar\n";

}

sub uploadIfEnoughTiles{
  my $Count = 0;
  opendir(my $dp, $Config{WorkingDirectory}) || return;
  while(my $File = readdir($dp)){
    $Count++ if($File =~ /tile_.*\.png/);
  }
  closedir($dp);
  
  if ($Count < 200)
  {
    # print "Not uploading yet, only $Count tiles\n";
  }
  else
  {
    upload();
  }
}

sub upload{
  my $UploadScript = "$Bin/upload.pl $progressJobs $progressPercent";
  runCommand("Uploading", $UploadScript);
}
#-----------------------------------------------------------------------------
# Ask the server what tileset needs rendering next
#-----------------------------------------------------------------------------
sub ProcessRequestsFromServer {
  my $LocalFilename = "$Config{WorkingDirectory}request.txt";
  
  # ----------------------------------
  # Download the request, and check it
  # Note: to find out exactly what this server is telling you, 
  # add ?help to the end of the URL and view it in a browser.
  # It will give you details of other help pages available,
  # such as the list of fields that it's sending out in requests
  # ----------------------------------
  killafile($LocalFilename);
  my $RequestUrlString = $Config{RequestURL} . "?version=" . $Config{ClientVersion} . "&user=" . $Config{UploadUsername};
  # DEBUG: print "using URL " . $RequestUrlString . "\n";
  DownloadFile(
    $RequestUrlString, 
    $LocalFilename, 
    0, 
    "Request from server");
    
  if(! -f $LocalFilename){
    return (0, "Error reading request from server");
  }

  # Read into memory
  open(my $fp, "<", $LocalFilename) || return;
  my $Request = <$fp>;
  chomp $Request;
  close $fp;
  
  # Parse the request
  my ($ValidFlag,$Version,$X,$Y,$Z,$ModuleName) = split(/\|/, $Request);
  
  # First field is always "OK" if the server has actually sent a request
  if ($ValidFlag eq "XX")
  {
      return (0, "Server has no work for us"); 
  }
  elsif ($ValidFlag ne "OK")
  {
      return (0, "Server dysfunctional");
  }
  
  # Check what format the results were in
  # If you get this message, please do check for a new version, rather than
  # commenting-out the test - it means the field order has changed and this
  # program no longer makes sense!
  if ($Version != 3)
  {
      print STDERR "\n";
      print STDERR "Server is speaking a different version of the protocol to us.\n";
      print STDERR "Check to see whether a new version of this program was released!\n";
      exit(2);
  }
  
  # Information text to say what's happening
  statusMessage("Got work from the \"$ModuleName\" server module");
  
  # Create the tileset requested
  GenerateTileset($X, $Y, $Z);
  return (1, "");
}

#-----------------------------------------------------------------------------
# Render a tile (and all subtiles, down to a certain depth)
#-----------------------------------------------------------------------------
sub GenerateTileset {
  my ($X, $Y, $Zoom) = @_;
    
  my ($N, $S) = Project($Y, $Zoom);
  my ($W, $E) = ProjectL($X, $Zoom);
  
  $progress = 0;
  $progressJobs++;

  statusMessage(sprintf("Doing tileset $X,$Y (area around %f,%f)", ($N+$S)/2, ($W+$E)/2), 1);
  

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
    $W1 = -180; # api apparently can handle -180
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
        return if ($Mode eq "loop"); # if loop was requested just return (FIXME: tell the server that the job has not been done yet)
        exit(1); # or else exit with an error. (to enable wrappers to better handle this situation i.e. tell the server the job hasn't been done yet)
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
  #printf "%03d %s%d,%d: %1.2f - %1.2f, %1.2f - %1.2f\n", $Zoom, $Margin, $X, $Y, $S,$N, $W,$E;
  
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
      "$Config{WorkingDirectory}output-$PID-z$i.svg",
      "zoom level $i");
    
    # Delete temporary osmarender
    killafile("osm-map-features-$PID-z$i.xml");
  }
  
  # Delete OSM map data
  killafile($DataFile);
  
  # Find the size of the SVG file
  my ($ImgH,$ImgW,$Valid) = getSize("$Config{WorkingDirectory}output-$PID-z$Config{MaxZoom}.svg");

  # Render it as loads of recursive tiles
  RenderTile($X, $Y, $Y, $Zoom, $N, $S, $W, $E, 0,0,$ImgW,$ImgH,$ImgH,0);

  # Clean-up the SVG files
  for (my $i = $Zoom ; $i <= $Config{MaxZoom} ; $i++) {
    killafile("$Config{WorkingDirectory}output-$PID-z$i.svg");
  }
  return 1;
}

#-----------------------------------------------------------------------------
# Render a tile
#   $X, $Y, $Zoom - which tile
#   $N, $S, $W, $E - bounds of the tile
#   $ImgX1,$ImgY1,$ImgX2,$ImgY2 - location of the tile in the SVG file
#-----------------------------------------------------------------------------
sub RenderTile {
  my ($X, $Y, $Ytile, $Zoom, $N, $S, $W, $E, $ImgX1,$ImgY1,$ImgX2,$ImgY2,$ImageHeight,$empty) = @_;
  
  return if($Zoom > $Config{MaxZoom});

  # no need to render subtiles if empty
  return if($empty == 1);
  
  my $Filename = tileFilename($X, $Ytile, $Zoom);

  # Render it to PNG
  # printf "$Filename: Lat %1.3f,%1.3f, Long %1.3f,%1.3f, X %1.1f,%1.1f, Y %1.1f,%1.1f\n", $N,$S,$W,$E,$ImgX1,$ImgX2,$ImgY1,$ImgY2; 
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
      $progress += 2 ** ($Config{MaxZoom}-$j);
    }
  }
  else {
    $progress += 1;
  }

  if (($progressPercent=$progress*100/63) == 100)
  {
      statusMessage("Finished $X,$Y", 1);
  }
  else
  {
      if ($Config{Verbose})
      {
          printf STDERR "Job No. %d %1.1f %% done.\n",$progressJobs, $progressPercent;
      }
      else
      {
          statusMessage("Working");
      }
  }

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
sub ProjectF {
  my $Lat = DegToRad(shift());
  my $Y = log(tan($Lat) + sec($Lat));
  return($Y);
}
#-----------------------------------------------------------------------------
# Project Y to latitude bounds
#-----------------------------------------------------------------------------
sub Project {
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
sub ProjectL {
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
sub UpdateOsmarender {
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
sub DownloadFile {
  my ($URL, $File, $UseExisting, $Title) = @_;
  
  statusMessage("Downloading: $Title");
  
  if($UseExisting) 
  {
    mirror($URL, $File);
  } 
  else
  {
    getstore($URL, $File);
  }
  doneMessage(sprintf("done, %d bytes", -s $File));
  
}

#-----------------------------------------------------------------------------
# Transform an OSM file (using osmarender) into SVG
#-----------------------------------------------------------------------------
sub xml2svg {
  my($MapFeatures, $SVG, $what) = @_;
  my $Cmd = sprintf("%s \"%s\" tr %s %s > \"%s\"",
    $Config{Niceness},
    $Config{XmlStarlet},
    "osmarender.xsl",
    "$MapFeatures",
    $SVG);
  
  runCommand("Transforming $what", $Cmd);
}

#-----------------------------------------------------------------------------
# Run a shell command. Suppress command's stderr output unless it terminates
# with an error code.
#
# Return 1 if ok, 0 on error.
#-----------------------------------------------------------------------------
sub runCommand
{
    my ($message, $cmd) = @_;

    statusMessage($message);

    if ($Config{Verbose})
    {
        my $retval = system($cmd);
        return ($retval<0) ? 0 : ($retval>>8) ? 0 : 1;
    }

    my $ErrorFile = $Config{WorkingDirectory}."/".$PID.".stderr";
    my $retval = system("$cmd 2> $ErrorFile");
    my $ok = 0;

    # <0 means that the process could not start
    if ($retval < 0)
    {
        print STDERR "ERROR:\n";
        print STDERR "  Could not run the following command:\n";
        print STDERR "  $cmd\n";
        print STDERR "  Please check your installation.\n";
    } 
    else
    {
        $retval = $retval >> 8;
        if ($retval)
        {
            print STDERR "ERROR\n";
            print STDERR "  The following command produced an error message:\n";
            print STDERR "  $cmd\n";
            print STDERR "  Debug output follows:\n";
            open(ERR, $ErrorFile);
            while(<ERR>)
            {
                print STDERR "  | $_";
            }
            close(ERR);
        }
        else
        {
            $ok = 1;
        }
    }
    
    killafile($ErrorFile);
    return $ok;
}

#-----------------------------------------------------------------------------
# Render a SVG file
#-----------------------------------------------------------------------------
sub svg2png {
  my($Zoom, $PNG, $SizeX, $SizeY, $X1, $Y1, $X2, $Y2, $ImageHeight, $X, $Y, $Ytile) = @_;

  my $TempFile = $PNG."_part";
  
  my $Cmd = sprintf("%s \"%s\" -w %d -h %d --export-area=%f:%f:%f:%f --export-png=\"%s\" \"%s%s\" > /dev/null", 
    $Config{Niceness},
    $Config{Inkscape},
    $SizeX,
    $SizeY,
    $X1,$Y1,$X2,$Y2,
    $TempFile,
    $Config{WorkingDirectory},
    "output-$PID-z$Zoom.svg");
  
  runCommand("Rendering", $Cmd);

  splitImageX($TempFile, 12, $X, $Y, $Zoom, $Ytile);
  
  killafile($TempFile);

}
sub writeToFile {
  open(my $fp, ">", shift()) || return;
  print $fp shift();
  close $fp;
}

#-----------------------------------------------------------------------------
# Add bounding-box information to an osm-map-features file
#-----------------------------------------------------------------------------
sub AddBounds {
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
  
  $Data =~ s/(<!--bounds_mkr1-->).*(<!--bounds_mkr2-->)/$1\n<!-- Inserted by tilesGen -->\n$BoundsInfo\n$2/s;

  # Save back to the same location
  open(my $fpOut, ">$Filename");
  print $fpOut $Data;
  close $fpOut;
}

#-----------------------------------------------------------------------------
# Set data source file name in map-features file
#-----------------------------------------------------------------------------
sub SetDataSource {
  my ($Filename) = @_;
  
  # Read the old file
  open(my $fpIn, "<", "$Filename");
  my $Data = join("",<$fpIn>);
  close $fpIn;
  die("no such $Filename") if(! -f $Filename);
  
  # Change some stuff
  my $DataSource = sprintf("data-%s.osm",
    $PID);
  
  $Data =~ s/(  data=\").*(  scale=\")/$1$DataSource\"\n$2/s;

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
sub tileFilename {
  my($X,$Y,$Zoom) = @_;

  return(sprintf("%s/tile_%d_%d_%d.png",$Config{WorkingDirectory},$Zoom,$X,$Y));
}

#-----------------------------------------------------------------------------
# Set data source file name in map-features file
#-----------------------------------------------------------------------------
sub appendOSMfile {
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
  $Data = join("",<$fpIn2>);
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
sub splitImageX {
  my ($File, $ZOrig, $X, $Y, $Z, $Ytile) = @_;
  
  # Size of tiles
  my $Pixels = 256;
  
  # Number of tiles
  my $Size = 2 ** ($Z - $ZOrig);
  
  # Load the tileset image
  statusMessage(sprintf("Splitting %s (%d x 1)", $File, $Size));
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
    my $Filename = tileFilename($X * $Size + $xi, $Ytile, $Z);
   
    # convert Tile to paletted file This *will* break stuff if different libGD versions are used
    # $SubImage->trueColorToPalette($dither,$numcolors);

    # Temporary filename
    my $Filename2 = "$Filename.cut";
    
    # Store the tile
    statusMessage(" -> $Filename") if ($Config{Verbose});
    WriteImage($SubImage,$Filename2);
    rename($Filename2, $Filename);
    
  }
  undef $SubImage;
}

#-----------------------------------------------------------------------------
# Write a GD image to disk
#-----------------------------------------------------------------------------
sub WriteImage {
  my ($Image, $Filename) = @_;
  
  # Get the image as PNG data
  my $png_data = $Image->png;
  
  # Store it
  open (my $fp, ">$Filename") || die;
  binmode $fp;
  print $fp $png_data;
  close $fp;
}

#-----------------------------------------------------------------------------
# Prints status message without newline, overwrites previous message
# (if $newline set, starts new line after message)
#-----------------------------------------------------------------------------
sub statusMessage 
{
    my ($msg, $newline) = @_;

    if ($Config{Verbose})
    {
        print STDERR "$msg\n";
        return;
    }

    my $toprint = sprintf("[#%d %3d%%] %s%s ", $progressJobs, $progressPercent+.5, $msg, ($newline) ? "" : "...");
    print STDERR "\r";
    print STDERR " " x $lastmsglen;
    print STDERR "\r$toprint";
    if ($newline)
    {
        $lastmsglen = 0;
        print STDERR "\n";
    }
    else
    {
        $lastmsglen = length($toprint);
    }
}

#-----------------------------------------------------------------------------
# Used to display task completion. Only for verbose mode.
#-----------------------------------------------------------------------------
sub doneMessage
{
    my $msg = shift;
    $msg = "done" if ($msg eq "");

    if ($Config{Verbose})
    {
        print STDERR "$msg\n";
        return;
    }
}

#-----------------------------------------------------------------------------
# A sleep function with visible countdown
#-----------------------------------------------------------------------------
sub talkInSleep
{
    my ($message, $duration) = @_;
    if ($Config{Verbose})
    {
        print STDERR "$message: sleeping $duration seconds\n";
        sleep $duration;
        return;
    }

    for (my $i = 0; $i< $duration; $i++)
    {
        statusMessage(sprintf("%s, sleeping (%d)", $message, $duration - $i));
        sleep 1;
    }
}
