#!/usr/bin/perl
use LWP::Simple;
use LWP::UserAgent;
use Math::Trig;
use File::Copy;
use strict;
#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home, download module
# Takes a queue of tile requests, and downloads the appropriate OSM XML map data
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
require("../secret.pl");
my $RequestDir = "../requests";
my $Dir = "../data";

# Use the -f flag to run the program continuously
DownloadContinuously($Dir, $RequestDir, 10) if(shift() eq "-f");

DownloadOsm($Dir, $RequestDir);

#-----------------------------------------------------------------------------
# Continuously download requests, to keep a "queue" of N files in the data
# directory
#-----------------------------------------------------------------------------
sub DownloadContinuously(){
  my ($DataDir, $RequestDir, $QueueLength) = @_;
  while(1){
    # If the queue of requsts isn't full, then download one
    if(CountFilesInDir($DataDir) < $QueueLength){
      DownloadOsm($DataDir, $RequestDir);
    }
    sleep(5);
  }
}

#-----------------------------------------------------------------------------
# Download a request for while tileset to render next
#-----------------------------------------------------------------------------
sub DownloadOsm(){
  my ($DataDir, $RequestDir) = @_;

  opendir(my $dp, $RequestDir) || return;
  while(my $file = readdir($dp)){
    if($file =~ /(\d+)_(\d+)_(\d+)/){
      DownloadOsmArea($2, $3, $1, $DataDir);
      print "Deleting $RequestDir/$file\n";
      unlink("$RequestDir/$file");
    }
  }
  closedir($dp);
}

sub DownloadOsmArea(){
  my ($X, $Y, $Z, $DataDir) = @_;
    
  my ($N, $S) = Project($Y, $Z);
  my ($W, $E) = ProjectL($X, $Z);
  
  printf("$Z ($X,$Y) Getting data near %f,%f\n", ($N+$S)/2, ($W+$E)/2);
  
  my $DataFile = $DataDir.sprintf("/data_%d_%d_%d.osm", $Z, $X, $Y);
  my $DataFilePart = "$DataFile.part";
  
  #------------------------------------------------------
  # Download data
  #------------------------------------------------------
  my $URL = sprintf("http://%s\@www.openstreetmap.org/api/0.3/map?bbox=%f,%f,%f,%f",
    GetOsmPassword(), $W, $S, $E, $N);
  
  getstore($URL, $DataFilePart);
  rename($DataFilePart, $DataFile);
}
