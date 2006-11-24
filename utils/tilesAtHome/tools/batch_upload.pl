#!/usr/bin/perl
use strict;
use LWP::Simple;
use LWP::UserAgent;
#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home batch-upload tool
#
# Uploads a directory full of tiles in the zoom_x_y.png format, to a tileserver
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

# Password for the receiving website
my $Password = "user|password";

# Usage: 
# perl upload.pl directoryname
uploadDir(shift());

#-----------------------------------------------------------------------------
# Uploads a directory full of tile images
# Each image must be of the format:
# zoom_x_y.png
#-----------------------------------------------------------------------------
sub uploadDir(){
  my $Dir = shift();
  die("No such directory $Dir\n") if(!-d $Dir);
  print "$Dir\n";
  opendir(my $dp, $Dir);
  while(my $File = readdir($dp)){
    if($File =~ /(\d+)_(\d+)_(\d+).png/){
      my ($Zoom,$X,$Y) = ($1,$2,$3);
      upload("$Dir/$File",$X,$Y, $Zoom, $Password);
      print "  $Zoom, $X, $Y\n";
    }
  }
}

#-----------------------------------------------------------------------------
# Upload a single tile
#-----------------------------------------------------------------------------
sub upload(){
  my ($File, $X, $Y, $Zoom, $Password) = @_;
  my $URL = "http://osmathome.bandnet.org/test.php"; 
  
  my $ua = LWP::UserAgent->new(env_proxy => 0,
    keep_alive => 1,
    timeout => 60);
        
  $ua->protocols_allowed( ['http'] );
  $ua->agent("tilesAtHomeBatch");

  my $res = $ua->post($URL,
    Content_Type => 'form-data',
    Content => [ file => [$File], x => $X, y => $Y, z => $Zoom, mp => $Password ]);
    
  if(!$res->is_success()){
    die("Post error: " . $res->error);
  } 

  # Uncomment this to move files once they've been uploaded
  # `mv $File done/`;
}
