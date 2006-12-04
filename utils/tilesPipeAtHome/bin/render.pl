#!/usr/bin/perl
use LWP::Simple;
use LWP::UserAgent;
use Math::Trig;
use File::Copy;
use strict;
#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home, rendering module
# Takes a queue of SVG files, and renders them into images using RSVG
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
require("lib.pl");
my $SvgDir = "../SVG";
my $TilesDir = "../tiles";

# Use the -f lag to run the program continuously
RenderContinuously($SvgDir, $TilesDir) if(shift() eq "-f");

Render($SvgDir, $TilesDir);

#-----------------------------------------------------------------------------
# Continuously download requests, to keep a "queue" of N files in the data
# directory
#-----------------------------------------------------------------------------
sub RenderContinuously(){
  my ($SvgDir, $TileDir) = @_;
  while(1){
    # If the queue of requsts isn't full, then download one
    Render($SvgDir, $TileDir);
    sleep(5);
  }
}

#-----------------------------------------------------------------------------
# Download a request for while tileset to render next
#-----------------------------------------------------------------------------
sub Render(){
  my ($SvgDir, $TileDir) = @_;
  print "looking in $SvgDir\n";
  opendir(my $dp, $SvgDir) || return;
  while(my $file = readdir($dp)){
    if($file =~ /(\d+)_(\d+)_(\d+)\.svg$/i){
      my $filename = "$SvgDir/$file";
      RenderFile($2, $3, $1, $filename, $TileDir);
      unlink($filename);
    }
  }
  closedir($dp);
}

sub RenderFile(){
  my($X,$Y,$Z,$filename, $Dir) = @_;
  
  for(my $zi = $Z; $zi <= 17; $zi++){
    my $TileFilename = $Dir.sprintf("/tileset_%d_%d_%d_level%d.png", $Z,$X,$Y, $zi);
    my $Size = 256 * (2 ** ($zi - $Z));
    my $TileFilenameTemp = "$TileFilename.part";
    my $Cmd = sprintf("%s %s %s -w %d -h %d -f png",
      "rsvg", 
      $filename,
      $TileFilenameTemp,
      $Size,
      $Size);

    print "$Cmd\n";
    `$Cmd`;
    
    rename($TileFilenameTemp, $TileFilename);
  }
}

