#!/usr/bin/perl
use LWP::Simple;
#-----------------------------------------------------------------------------
# OpenStreetMap @ Home
# 
# This program will:
# - get a request from the almien website for a map that needs rendering
# - render it, using tools such as xmlstarlet and inkscape on your computer
# - upload the result to the almien website, for public display
# 
# This program will use considerable amounts of system resource (cpu, memory, disk)
# It is designed for use by a few trusted users, who will create maps and not
# upload malicious data.  Hence, the script includes a password which is required
# to upload the resultant map back to the almien website.
# 
# You can run this program without a password, and it will render maps from
# OSM data and save them on your disk.
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

my $Password = "username|password";  # Ask OJW for a password to use this script
UpdateOsmarender();
ProcessRequestFromWeb($Password);
exit;

#-----------------------------------------------------------------------------
# Gets latest copy of osmarender from repository
#-----------------------------------------------------------------------------
sub UpdateOsmarender(){
  foreach $File(("osm-map-features.xml", "osmarender.xsl", "Osm_linkage.png", "somerights20.png")){
  
    DownloadFile(
    "http://almien.co.uk/OSM/Places/Download/$File",
    $File,
    1,
    "Osmarender ($File)");
  }
}

#-----------------------------------------------------------------------------
# Gets a request from almien for a map that needs rendering, and does it
#-----------------------------------------------------------------------------
sub ProcessRequestFromWeb(){
  my ($Password) = @_;
  DownloadFile(
    "http://almien.co.uk/OSM/Places/?action=random", 
    "random.txt",
    0,
    "Rendering requests");
  
  ProcessRequest("random.txt", $Password);
}

#-----------------------------------------------------------------------------
# Processes a textfile containing request for a map-rendering
#-----------------------------------------------------------------------------
sub ProcessRequest(){
  my ($File, $Password) = @_;
  
  # File comprises pipe-separated fields
  open(IN, "<", $File);
  my ($Version, $ID, $Width, $URL) = split(/\|/, <IN>);
  close IN;
  
  if($Version != 2){
    print STDERR "A new version of this script is available\n";
    print STDERR "Not processing requests, as the interface may have changed\n";
    print STDERR "Please download latest script and run that instead of this one\n";
    exit;
  }
  if($ID == -1){
    print STDERR "Nothing to do!\n";
    exit;
  }
  print STDERR "Using interface version $Version\n";
  print STDERR "Downloading $ID from $URL\n";
  
  foreach $OldFile("output.png", "output.svg", "data.osm"){
    unlink $OldFile if(-f $OldFile);
  }
  
  # Get the OSM data
  DownloadFile($URL, "data.osm.gz", 0, "OSM data for location (ID $ID)");
  # Decompress data
  `gunzip data.osm.gz`;
  
  # Transform it to SVG
  xml2svg("output.svg");
  
  # Render it to PNG
  svg2png("output.svg", "output.png", $Width);

  # Upload it
  upload("output.png", $ID, $Password)
}

#-----------------------------------------------------------------------------
# 
#-----------------------------------------------------------------------------
sub DownloadFile(){
  my ($URL, $File, $UseExisting, $Title) = @_;
  
  print STDERR "Downloading: $Title...";
  
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
  print STDERR "Transforming...";
  `$Cmd`;
  print STDERR " done\n";
}

#-----------------------------------------------------------------------------
# Render a SVG file
#-----------------------------------------------------------------------------
sub svg2png(){
  my($SVG, $PNG, $Size) = @_;	
  my $Cmd = sprintf("%sinkscape -w %d -D -b FFFFFF -e %s %s", 
    "nice ", # Blank this out for use on windows
    $Size,
    $PNG, 
    $SVG);
  print STDERR "Rendering...";
  `$Cmd`;
  print STDERR " done\n";
}


#-----------------------------------------------------------------------------
# Upload a rendered map to almien
#-----------------------------------------------------------------------------
sub upload(){
  my ($File, $ID, $Password) = @_;
  $URL = "http://almien.co.uk/OSM/Places/upload.php";
  
  my $ua = LWP::UserAgent->new(env_proxy => 0,
    keep_alive => 1,
    timeout => 60);
  $ua->protocols_allowed( ['http'] );
  $ua->agent("OsmAtHome");

  my $res = $ua->post($URL,
    Content_Type => 'form-data',
    Content => [ file => [$File], id => $ID, mp => $Password ]);
    
  if(!$res->is_success()){
    die("Post error: " . $res->error);
  } 
}