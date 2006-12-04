#!/usr/bin/perl
use strict;
use GD;
#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home, tile-splitting module
# Takes a directory full of tilesets (large images covering loads of tiles)
# and splits them into individual tile images
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

splitTiles("../tiles", "../tiles2");

#-----------------------------------------------------------------------------
# Split a directory full of tilesets into individual tiles
#-----------------------------------------------------------------------------
sub splitTiles(){
  my ($Dir, $OutputDir) = @_;
  
  opendir(my $dp, $Dir) || return;
  while(my $file = readdir($dp)){
    if($file =~ /^tileset_(\d+)_(\d+)_(\d+)_level(\d+)\.png$/){
      my $Filename = "$Dir/$file";
      
      # Split the tileset
      splitImage($Filename, $OutputDir, $1, $2, $3, $4);
      
      # Delete the source image after use
      unlink $Filename;
    }
  }
  closedir($dp);
}

#-----------------------------------------------------------------------------
# Split a tileset image into tiles
#-----------------------------------------------------------------------------
sub splitImage(){
  my ($File, $OutputDir, $ZOrig, $X, $Y, $Z) = @_;
  
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
  for(my $yi = 0; $yi < $Size; $yi++){
    for(my $xi = 0; $xi < $Size; $xi++){
    
      # Get a tiles'worth of data from the main image
      $SubImage->copy($Image,
        0,                   # Dest X
        0,                   # DestY
        $xi * $Pixels,       # Source X
        $yi * $Pixels,       # Source Y
        $Pixels,             # Copy width
        $Pixels);            # Copy height
  
      # Decide what the tile should be called
      my $Filename = $OutputDir . "/" . 
        sprintf("tile_%d_%d_%d.png", 
          $Z, 
          $X * $Size + $xi, 
          $Y * $Size + $yi);
      # Temporary filename
      my $Filename2 = "$Filename.part.png";
      
      # Store the tile
      print " -> $Filename\n";
      WriteImage($SubImage,$Filename);
      rename($Filename2, $Filename);
      
    }
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
  open (OUT, ">$Filename") || die;
  binmode OUT;
  print OUT $png_data;
  close OUT;
}
