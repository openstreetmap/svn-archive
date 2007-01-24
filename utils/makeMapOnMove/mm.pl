#!/usr/bin/perl
use strict;
#---------------------------------------------------------------------------------
# Make map on move
#
# Usage: mm.pl
#  and then open index.htm in a browser where you can see it with the console still open
#  then reselect the console window so that it receives keystrokes
# 
#  All input should be to console window via the numeric keypad
#  All output will be to the browser
#  Press q to quit
#---------------------------------------------------------------------------------
# Copyright 2007, Oliver White
# 
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
#---------------------------------------------------------------------------------

# We only use this for non-blocking getch()
require Term::Screen;
my $scr = new Term::Screen;
die("Can't init term::screen\n") if(!$scr);
$scr->clrscr();

# Setup stuff for a fake GPS (that reads a logfile)
use Time::HiRes qw(sleep);
open(my $fpLogIn, "<log.nmea") || die("Can't open log");

# Global variables
my (@Nodes, @CurrentWay, @Poi);
my %Tags = ("highway"=>"unclassified"); # default tags
my $NodeCount = 0; my $SegCount = 0;
my ($posLat,$posLon) = (0,0);
my $Mode = "default";
my ($LastKey, $LastError);
my @Interface;

# Interface commands
loadInterface("interface.txt");

# Obsolete: remove
my %WayTypes = (
"1" => "Track",
"2" => "Unclassified",
"3" => "Residential",
"4" => "Secondary",
"5" => "Primary",
"6" => "Motorway",
"7" => "Trunk",
"8" => "Motorway link",
"9" => "Service");

# Run (100 steps only in testing)
foreach(1..100){
  update();
  render();
  sleep(0.2);
}


# Updates everything
sub update{
  # Get the position
  my ($lat,$lon,$valid) = getPos();
  if($valid){
    # Record the position, add it to our journey
    addNode($lat,$lon);
    $posLat = $lat; 
    $posLon = $lon;
  }
  processInput();
  showInterface();
  showHtml();
  outputOsm();
}
sub showInterface{
  # Obsolete: remove
}
sub showHtml{
  # Create an HTML page as output
  
  # Take input from a template HTML file
  open(my $fp, "<template.htm") || die("Can't read HTML template\n");
  my $template = join("", <$fp>);
  close $fp;
  
  # List of keywords to look for
  foreach my $Keyword( qw(KEYPAD POS MODE ATTRIBUTES LAST_KEYPRESS LAST_ERROR)){
    # Lookup each keyword in the getHtmlPart function
    my $Data = getHtmlPart($Keyword);
    $template =~ s/\{\{$Keyword\}\}/$Data/g;
  }
  
  # Save the output to index.htm
  open(my $fp, ">index.htm") || die("Can't write HTML\n");
  print $fp $template;
  close $fp;
}

sub getHtmlPart{
  my $Key = shift();
  # HTML keywords that can be replaced
  return getHtmlKeypad() if($Key eq "KEYPAD");
  return sprintf("%1.5f, %1.5f", $posLat,$posLon) if($Key eq "POS");
  return $Mode if($Key eq "MODE");
  return getHtmlAttributes() if($Key eq "ATTRIBUTES");
  return $LastKey if($Key eq "LAST_KEYPRESS");
  return $LastError if($Key eq "LAST_ERROR");
}
sub getHtmlKeypad{
  # Get the currently-available interface, as an HTML table
  my %Keys;
  
  # Look through the interface definition for keypresses which exist in this mode
  foreach my $Interface(@Interface){
    my($ifMode,$ifKey,$ifLabel,$ifAction,$ifParams) = split(/:/,$Interface);
    if($ifMode eq $Mode){
      $Keys{$ifKey} = $ifLabel;
    }
  }
  
  # Create an HTML table representing our input device
  my $Keypad = 
    "<table class=\"keypad\" cellspacing=\"0\"><tr><td>{7}</td><td>{8}</td><td>{9}</td></tr>\n".
    "<tr><td>{4}</td><td>{5}</td><td>{6}</td></tr>\n".
    "<tr><td>{1}</td><td>{2}</td><td>{3}</td></tr>\n".
    "<tr><td>{0}</td><td>{.}</td><td>{Enter}</td></tr></table>\n";
    
  # Replace every {{key}} entry with a label from the interface definition
  while(my ($k,$v) = each(%Keys)){
    my $htmlVal = "<span class=\"keyname\">$k</span><br><span class=\"keyaction\">$v</span>";
    $Keypad =~ s/\{$k\}/$htmlVal/g;
  }
  
  # Replace any unused key entries with HTML spaces
  $Keypad =~ s/\{.*?\}/&nbsp;/g;
  
  return $Keypad;
}

sub getHtmlAttributes{
  # Get the current way's attributes, as an HTML table
  my $Html = "<table class=\"attributes\">";
  while(my($k,$v) = each(%Tags)){
    $Html .= "<tr><td>$k</td><td>$v</td></tr>\n";
  }
  return($Html ."</table>");
}
sub processInput{
  # Handle any keyboard input
  return if(!$scr->key_pressed());
  my $c = $scr->getch();
  finish() if($c eq "q");
  $LastKey = $c;
  
  # Look for an interface command that matches what was just pressed
  # and which is valid in the current mode, and implement it
  foreach my $Interface(@Interface){
    my($ifMode,$ifKey,$ifLabel,$ifAction,$ifParams) = split(/:/,$Interface);
    if($ifMode eq $Mode and $ifKey eq $c){
      handleAction($ifAction, $ifParams);
      return;
    }
  }
  $Mode = "default";
  return;
  
}
sub handleAction{
  # Do something as the result of a keypress
  my ($action, $params) = @_;
  if($action eq "mode"){
    $Mode = $params;
  }
  elsif($action eq "set"){
    setTags(split(/=/,$params));
    $Mode = "default";
  }
  elsif($action eq "add"){
    addPoi($params);
  }
  elsif($action eq "action"){
  }
  else{
    $LastError = "Unrecognised action $action";
  }
}
sub addPoi{
  # Add a node as POI (not in route)
  my $KeyVal = shift();
  my $PoiData;
  
  foreach my $Part(split(/,\s*/,$KeyVal)){
    my($k,$v) = split(/=/,$Part);
    $PoiData->{$k} = $v;
    }
  $PoiData->{"lat"} = $posLat;
  $PoiData->{"lon"} = $posLon;
  
  push(@Poi, $PoiData);
}
sub setTags{
  # Set attributes of the current Way
  my ($k,$v) = @_;
  $Tags{$k} = $v;
}

sub addNode{
  # Add a node to the current Way
  my $Node;
  $Node->{lat} = shift();
  $Node->{lon} = shift();
  push @Nodes, $Node;
  push @CurrentWay, scalar(@Nodes);
}

sub outputOsm{
  # Save everything we know as an OSM file
  open(my $fp, ">data.osm") || die("Can't write to OSM file\n");
  
  print $fp "<?xml version='1.0' encoding='UTF-8'?>\n";
  print $fp "<osm version='0.3' generator='mm'>\n";

  my $Count = 0;
  
  # Save route nodes to OSM file
  foreach my $Node (@Nodes){
    printf $fp "<node id='%d' lat='%f' lon='%f' />\n",
      ++$Count,
      $Node->{lat},
      $Node->{lon};
  }
  
  # Save POI nodes to OSM file
  foreach my $Poi (@Poi){
    printf $fp "<node id='%d' lat='%f' lon='%f'>\n", ++$Count, $Poi->{"lat"},$Poi->{"lon"};
    while(my($k,$v) = each(%$Poi)){
      printf $fp "<tag k='%s' v='%s' />\n",$k,$v if($k !~ /^(lat|lon)$/);
    }
    printf $fp "</node>\n",
  }
  
  # Save current position as a node
  printf $fp "<node id='%d' lat='%f' lon='%f'>\n", ++$Count, $posLat, $posLon;
  printf $fp "<tag k='mapmaker' v='current_position' />\n";
  printf $fp "</node>\n",
  
  # Save bounding box, makes osmarender give a map centred on the current position
  my $HalfAreaLat = 0.003;
  my $HalfAreaLon = $HalfAreaLat * (cos($posLon / 57));
  
  printf $fp "<bounds returned_minlat=\"%f\" returned_minlon=\"%f\" returned_maxlat=\"%f\" returned_maxlon=\"%f\" />\n",
    $posLat - $HalfAreaLat,
    $posLon - $HalfAreaLon,
    $posLat + $HalfAreaLat,
    $posLon + $HalfAreaLon;
  
  
  # Save the current Way
  $Count = 0;
  my $LastPoint;
  
  my @WaySegs;
  foreach my $Point (@CurrentWay){
    if($Count > 0){
      printf $fp "<segment id='%d' from='%d' to='%d' />\n",
        $Count,
        $LastPoint,
        $Point;
      push(@WaySegs, $Count);
    }
    $Count++;
    $LastPoint = $Point;
  }
  
  my $Segs = "";
  foreach my $Seg (@WaySegs){
    $Segs .= sprintf("<seg id='%d' />\n", $Seg);
  }

  my $Tags = "";
  while(my ($k,$v) = each(%Tags)){
    $Tags .= sprintf("<tag k='%s' v='%s' />\n",$k,$v);
  }
  
  printf $fp "<way id='1'>\n$Segs$Tags\n</way>\n";

  print $fp "</osm>";
  close $fp;
}

sub render(){
  # Render the OSM data we just created
  `cd render;./render.sh 2>/dev/null &`;
}

sub getPos{
  # Get current position as WGS-84 lat/long
  # This should be replaced with a GPSD call later
  my $Line = <$fpLogIn>;
  chomp $Line;
  if($Line =~ /\$GPGGA,(.*)/){
    my ($Time,$Lat,$NS,$Long,$WE,@others) = split(/,/,$1);
    
    my $Lat = convertPos($Lat,$NS);
    my $Long = convertPos($Long,$WE);
    
    printf "%1.13f, %1.13f\n", $Lat, $Long if(0);

    return($Lat,$Long,1);
  }
  return(0,0,0);
}


sub convertPos{
  # Convert NMEA-style number format (DDMM.MMM) to decimal
  my ($Num,$Quadrant) = @_;
  if($Num =~ /(\d+)(\d{2}\.\d+)/){
    $Num = $1 + $2 / 60;
  }
  if($Quadrant =~ /[SW]/){
    $Num *= -1;
  }
  return($Num);
}
sub finish{
  # Exit the program
  $scr->clrscr();
  exit;
}

sub loadInterface{
  # Load interface definition file from disk
  open(my $fp, "<", shift()) || die("Can't read interface definition\n");
  while(my $Line = <$fp>){
    chomp $Line;
    $Line =~ s/\s*#.*$//;  # Remove comments
    push @Interface, $Line;
  }
  close $fp;
}