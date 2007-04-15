#!/usr/bin/perl
use strict;
#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home, compress module
# Takes a load of tiles, and zips them into an archive ready to upload
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
my $MB = 1024 * 1024;

getTiles("../tiles2", "../temp", "../uploadable", 0.5 * $MB);

sub getTiles(){
  my ($TileDir, $TempDir, $OutputDir, $SizeLimit) = @_;
  
  my $Size = 0;
  
  opendir(my $dp, $TileDir) || return;
  while((my $file = readdir($dp)) && ($Size < $SizeLimit)){
    my $Filename1 = "$TileDir/$file";
    my $Filename2 = "$TempDir/$file";
    $Size += -s $Filename1;
    rename($Filename1, $Filename2);
  }
  closedir($dp);
  
  compressTiles($TempDir, $OutputDir);
}

#-----------------------------------------------------------------------------
# Compress all PNG files from one directory, creating 
#-----------------------------------------------------------------------------
sub compressTiles(){
  my ($Dir, $OutputDir) = @_;
  
  # ZIP all the tiles into a single file
  my $Command1 = sprintf("%s %s %s",
    "zip",
    "$OutputDir/$$.zip",
    "$Dir/*");
  # ZIP filename is currently our process ID - change this if one process
  # becomes capable of generating multiple zip files
  
  # Delete those PNGs
  my $Command2 = sprintf("%s %s",
    "rm",
    "$Dir/*.png");
  # TODO: this deletes any tiles which were generated between the ZIP and RM commands!
    
  # Run the two commands
  `$Command1`;
  `$Command2`;
}
