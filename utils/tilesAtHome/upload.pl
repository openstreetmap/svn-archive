#!/usr/bin/perl
use strict;
use LWP::UserAgent;
use File::Copy;
#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home, upload module
# Takes any tiles generated, adds them into ZIP files, and uploads them
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

# conf file, will contain username/password and environment info
my %Config;
my $ZipFileCount = 0;
ReadConfigFile("tilesAtHome.conf");

my $ZipDir = $Config{WorkingDirectory} . "/uploadable";

# Upload any ZIP files which are still waiting to go
if(opendir(ZIPDIR, $ZipDir)){
  while(my $File = readdir(ZIPDIR)){
    if($File =~ /\.zip$/i){
      upload("$ZipDir/$File");
    }
  }
  close ZIPDIR;
}

# Group and upload the tiles
uploadTileBatch(
  $Config{WorkingDirectory}, 
  $Config{WorkingDirectory} . "/gather", 
  $ZipDir);

#-----------------------------------------------------------------------------
# Moves tiles into a "gather" directory until a certain size is reached,
# then compress and upload those files
#-----------------------------------------------------------------------------
sub uploadTileBatch(){
  my ($TileDir, $TempDir, $OutputDir) = @_;
  my ($Size,$Count) = (0,0);
  my $MB = 1024*1024;
  my $SizeLimit = $Config{"UploadChunkSize"} * $MB;
  
  mkdir $TempDir if ! -d $TempDir;
  mkdir $OutputDir if ! -d $OutputDir;
  
  print "Searching for tiles in $TileDir\n";
  opendir(my $dp, $TileDir) || return;
  while((my $file = readdir($dp)) && ($Size < $SizeLimit)){
    my $Filename1 = "$TileDir/$file";
    my $Filename2 = "$TempDir/$file";
    if($file =~ /tile_\d+_\d+_\d+\.png$/i){
      $Size += -s $Filename1;
      $Count++;
      
      rename($Filename1, $Filename2);
    }
  }
  closedir($dp);
  
  if($Count){
    printf("Got %d files (%d bytes), compressing...", $Count, $Size);
    compressTiles($TempDir, $OutputDir);
  }
  else
  {
    print "No tiles found\n";
  }
}

#-----------------------------------------------------------------------------
# Compress all PNG files from one directory, creating 
#-----------------------------------------------------------------------------
sub compressTiles(){
  my ($Dir, $OutputDir) = @_;
  
  my $Filename = sprintf("%s/%d_%d.zip", $OutputDir, $$, $ZipFileCount++);
  
  # ZIP all the tiles into a single file
  my $Command1 = sprintf("%s %s %s",
    "zip",
    $Filename,
    "$Dir/*");
  # ZIP filename is currently our process ID - change this if one process
  # becomes capable of generating multiple zip files
  
  # Delete files in the gather directory
  my $Command2 = sprintf("%s %s",
    "rm",
    "$Dir/*.png");
    
  # Run the two commands
  `$Command1`;
  `$Command2`;
  
  upload($Filename);
}

#-----------------------------------------------------------------------------
# Upload a ZIP file
#-----------------------------------------------------------------------------
sub upload(){
  my ($File) = @_;
  
  my $ua = LWP::UserAgent->new(keep_alive => 1, timeout => 120);

  $ua->protocols_allowed( ['http'] );
  $ua->agent("tilesAtHomeZip");
  
  my $Password = join("|", ($Config{UploadUsername}, $Config{UploadPassword}));
  my $URL = $Config{"UploadURL2"};
  
  print "Uploading \n  $File\n  to $URL\n";
  my $res = $ua->post($URL,
    Content_Type => 'form-data',
    Content => [ file => [$File], mp => $Password]);
    
  if(!$res->is_success()){
    print("Error uploading file");
    return;
  } 
  
  if($Config{DeleteZipFilesAfterUpload}){
    unlink($File);
  }
  else
  {
    rename($File, $File."_uploaded");
  }
}

sub ReadConfigFile(){
  open(my $fp, "<", shift()) || die("Can't open config file\n");
  foreach my $Line(<$fp>){
    $Line =~ s/\s*#.*$//; # Strip out comments
    if($Line =~ /(\w+)=(.*)/){
      $Config{$1} = $2;
    }
  }
}

