#!/usr/bin/perl
use LWP::Simple;
use LWP::UserAgent;
use Math::Trig;
use File::Copy;
use strict;
#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home, transformation module
# Takes a queue of OSM XML data files, and transforms them into SVG 
# using osmarender
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
my $DataDir = "../data";
my $Dir = "../SVG";

# Use the -f flag to run the program continuously
TransformContinuously($Dir, $DataDir, 10) if(shift() eq "-f");

Transform($Dir, $DataDir);

#-----------------------------------------------------------------------------
# Continuously download requests, to keep a "queue" of N files in the data
# directory
#-----------------------------------------------------------------------------
sub TransformContinuously(){
  my ($Dir, $DataDir, $QueueLength) = @_;
  while(1){
    # If the queue of requsts isn't full, then download one
    Transform($Dir, $DataDir);
    sleep(5);
  }
}

#-----------------------------------------------------------------------------
# Download a request for while tileset to render next
#-----------------------------------------------------------------------------
sub Transform(){
  my ($Dir, $DataDir) = @_;

  opendir(my $dp, $DataDir) || return;
  while(my $file = readdir($dp)){
    if($file =~ /data_(\d+)_(\d+)_(\d+).osm/){
      my $filename = "$DataDir/$file";
      if(-s $filename > 1024){
        TransformFile($2, $3, $1, $filename, $Dir);
      }
      else
      {
        MarkBlankTile($2, $3, $1);
      }
      unlink($filename);
    }
  }
  closedir($dp);
}

sub MarkBlankTile(){
  my($X,$Y,$Z) = @_;
  printf("Marking %d %d,%d as blank\n", $Z, $X,$Y);
  my $OutputFilename = sprintf("../tiles2/%d_%d_%d.png", $Z,$X,$Y);
  my $BlankFile = "resources/blank.png";
  copy($BlankFile, $OutputFilename);
}

sub TransformFile(){
  my($X,$Y,$Z,$filename, $Dir) = @_;
  my $RenderDir = "$Dir/temp";
  my $DataOsm = "$RenderDir/data.osm";
  my $SvgFile = $Dir.sprintf("/%d_%d_%d.svg",$Z,$X,$Y);
  my $SvgTemp = "$SvgFile.part";
  
  print "Copying $filename to $DataOsm\n";
  copy($filename, $DataOsm);
  
  my $Cmd = sprintf("xmlstarlet tr %s %s > %s",
    "$RenderDir/osmarender.xsl",
    "$RenderDir/osm-map-features.xml",
    $SvgTemp);
  
  `$Cmd`;
  
  rename($SvgTemp, $SvgFile);
}

