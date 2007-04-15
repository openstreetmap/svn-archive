#!/usr/bin/perl
use strict;
#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home, blank tile detection module
# Finds any blank tiles which were generated, and replaces them with an
# empty placeholder file indicating that that tile is valid but blank
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

handleBlankTiles("../tiles2");

#-----------------------------------------------------------------------------
# Split a directory full of tilesets into individual tiles
#-----------------------------------------------------------------------------
sub handleBlankTiles(){
  my ($Dir) = @_;
  my $FileSizeLimit = 1300; # bytes
  my ($Count, $CountBlank) = (0,0);
   
  opendir(my $dp, $Dir) || return;
  while(my $file = readdir($dp)){
    if($file =~ /^tile_(\d+)_(\d+)_(\d+)\.png$/){
      my $Filename = "$Dir/$file";
      if(-s $Filename <  $FileSizeLimit){
        
        # Create a placeholder textfile indicating blank tile
        my $NewFile = $Dir.sprintf("/blank_%d_%d_%d.txt", $1,$2,$3);
        Touch($NewFile);
        
        # Delete the blank tile
        unlink($Filename);
        
        $CountBlank++;
      }
      $Count++;
    }
  }
  print "$CountBlank of $Count tiles are blank\n";
  closedir($dp);
}

#-----------------------------------------------------------------------------
# Create an empty file
#-----------------------------------------------------------------------------
sub Touch(){
  my $Filename = shift();
  open(my $fp, ">$Filename")||return;
  close $fp;
}