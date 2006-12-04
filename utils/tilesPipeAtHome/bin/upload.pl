#!/usr/bin/perl
use LWP::Simple;
use LWP::UserAgent;
use strict;
#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home, upload module
# Uploads ZIP files containing a load of tiles, to the server
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
require("../secret.pl");

# Directory that contains the ZIP files to upload
my $Dir = "../uploadable";

# -f option to upload data every couple of minutes
if(shift() eq "-f")
{
  while(1){
    uploadArchiveDir($Dir);
    sleep(2 * 60);
  }
}
else
{
  # Normal operation: upload just once
  uploadArchiveDir($Dir);
}


sub uploadArchiveDir(){
  my ($Dir) = @_;
  
  opendir(my $dp, $Dir) || return;
  while(my $file = readdir($dp)){
    if($file =~ /\.zip$/i){
      my $Filename = "$Dir/$file";
      upload($Filename);
    }
  }
  close $dp;
}

#-----------------------------------------------------------------------------
# Upload a file
#-----------------------------------------------------------------------------
sub upload(){
  my ($File) = @_;
  
  print "Uploading $File\n";
  my $ua = LWP::UserAgent->new(keep_alive => 1, timeout => 120);

  $ua->protocols_allowed( ['http'] );
  $ua->agent("tilesPipeAtHome");

  my $res = $ua->post(GetUploadURL(),
    Content_Type => 'form-data',
    Content => [ file => [$File], mp => GetUploadPassword()]);
    
  if(!$res->is_success()){
    print("Error uploading file");
    return;
  } 
  
  # Comment-this out to leave your files on local disk after uploading
  unlink($File);
}
