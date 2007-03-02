#!/usr/bin/perl
use LWP::Simple;
use File::Copy;
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
# Prerequisites for the program :
# Perl
# xmlstarlet
# inkscape
# perlmodules LWP::Simple, File::Copy and Math::Vec. First two modules are
# normally included in a standard perl installation, and the latter module
# can be installed via CPAN (or PPM on Windows). Debian users can also
# install the latter module with this command : "apt-get install libmath-vec-perl"
#
#------------------------------------------------------------------------------
my $Password = "user|password";  # Ask OJW for a password to use this script

my $args = shift;
if($args eq "-h") {
	print "OpenStreetMap @ Home Client\n\n";
	print "You will need a username and password for the almien site to participate.\n";
	print "See http://wiki.openstreetmap.org/index.php/OSM%40home for details\n";
	exit 1;
}

# Process
UpdateOsmarender();
while(1){
ProcessRequestFromWeb($Password);
sleep(60);
};
exit;

#-----------------------------------------------------------------------------
# Gets latest copy of osmarender from repository
#-----------------------------------------------------------------------------
sub UpdateOsmarender(){
  foreach $File(("osm-map-features.xml", "osmarender.xsl", "Osm_linkage.png", "somerights20.png")){
  
    DownloadFile(
    "http://svn.openstreetmap.org/utils/osmAtHome/$File",
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
    print STDERR "Nothing to do!\nSleeping for 1 hour : press Ctrl-C to quit\n";
    sleep(3600);
#    exit 1;
    return;
 }
  
#  UpdateOsmarender();
  
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
  upload("output.png", $ID, $Password);
  
  # Say where to find the result
  print "Done. View the result at\nhttp://almien.co.uk/OSM/Places/?id=$ID\n";
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
  my($TSVG) = "temp.svg";
  my $Cmd = sprintf("%sxmlstarlet tr %s %s > %s",
    "nice ", # Blank this out for use on windows
    "osmarender.xsl",
    "osm-map-features.xml",
    $TSVG);
  print STDERR "Transforming...";
  `$Cmd`;
#-----------------------------------------------------------------------------
# Process lines to bezier
#-----------------------------------------------------------------------------
  my $Cmd = sprintf("%s ./lines2curves.pl %s > %s",
     "nice ",
     $TSVG,
     $SVG);
  print STDERR "Lines to Bezier..";
  `$Cmd`;
#------------------------------------------------------------------------------
# Quickfix in case lines2curves found a zerolenght segment
#------------------------------------------------------------------------------  
  my $filesize = -s "output.svg";
  if (!$filesize) {
  copy($TSVG,$SVG);
  print STDERR "zerolenght segment, no bezier hinting. Rendering without bezier hinting.\n";
    } 
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

#-----------------------------------------------------------------------------
# Modifying this script:
#
# You probably won't have the exact same setup as me, so this script might need
# some changes to work properly.  Since I only expect a handful of people to run
# this script, I think they can probably modify it themselves to suit their 
# computer's configuration.  Here's some of the things to look at:
#
# Location of inkscape - I've just called it "inkscape", but you can change that to
# the path of your inkscape program if it's elsewhere
#
# Location of xmlstarlet - on Windows, it's c:\xml\xml.exe by default, so change that
#
# "Nice" statements - the script uses the nice keyword to give the rendering 
# low scheduling priority, so it doesn't lock-up your computer and stop your desktop
# working.  Remove this on Windows, as it won't work
#
# Other SVG->PNG renderers - careful, as many renderers don't render the same area of
# the SVG.  It would probably be a good idea if we all used inkscape, as that makes the
# generated images much more predictable in terms of where the edges are exactly.
#
# Other XSLT programs - this is easier, as you can pick any one.  I've used xmlstarlet,
# but other people prefer xsltproc, xalan, etc.  If you use a different program,
# you'll need to update the command-line options this script supplies, so that
# it does the right thing.
#
#-----------------------------------------------------------------------------
# Testing:
# 
# To test changes to this script without affecting other users, simply comment-out
# the upload() function call, so that renders things and leaves them on your 
# computer, rather than trying to upload the result.
# 
# To test the effect of changes to the osmarender files, comment-out the
# UpdateOsmarender() function call, so that it keeps your modified version of
# osmarender, instead of downloading a new copy each time.
#
# To render a particular town, look-up the town's ID at almien.co.uk/OSM/Places,
# then hardcode that ID in the script rather than downloading IDs from the web
#-----------------------------------------------------------------------------
