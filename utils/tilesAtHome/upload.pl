#!/usr/bin/perl
use strict;
use LWP::UserAgent;
use File::Copy;
use config;
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
my %Config = ReadConfig("tilesAtHome.conf", "general.conf", "authentication.conf");
my $ZipFileCount = 0;

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

my $TileDir = $Config{WorkingDirectory};

# Group and upload the tiles
print "Searching for tiles in $TileDir\n";
opendir(my $dp, $TileDir) or die("Can't open directory $TileDir\n");
my @tiles = grep { /tile_[0-9]*_[0-9]*_[0-9]*\.png$/ } readdir($dp);
closedir($dp);

while (uploadTileBatch(
  $TileDir, 
  $TileDir . "/gather", 
  $ZipDir)) {};

#-----------------------------------------------------------------------------
# Moves tiles into a "gather" directory until a certain size is reached,
# then compress and upload those files
#-----------------------------------------------------------------------------
sub uploadTileBatch(){
  my ($TileDir, $TempDir, $OutputDir) = @_;
  my ($Size,$Count) = (0,0);
  my $MB = 1024*1024;
  my $SizeLimit = $Config{"UploadChunkSize"} * $MB;
  my $CountLimit = $Config{"UploadChunkCount"};

  #prevent too small zips, 683=half a tileset
  $CountLimit = 683 if ($CountLimit < 100);

  mkdir $TempDir if ! -d $TempDir;
  mkdir $OutputDir if ! -d $OutputDir;
  
  print @tiles . " tiles to process\n";

  while((my $file = shift @tiles) && ($Size < $SizeLimit) && ($Count < $CountLimit)){
    my $Filename1 = "$TileDir/$file";
    my $Filename2 = "$TempDir/$file";
    if($file =~ /tile_\d+_\d+_\d+\.png$/i){
      $Size += -s $Filename1;
      $Count++;
      
      rename($Filename1, $Filename2);
    }
  }
  
  if($Count){
    printf("Got %d files (%d bytes), compressing...", $Count, $Size);
    return compressTiles($TempDir, $OutputDir);
  }
  else
  {
    print "Finished.\n";
    return 0;
  }
}

#-----------------------------------------------------------------------------
# Compress all PNG files from one directory, creating 
#-----------------------------------------------------------------------------
sub compressTiles(){
  my ($Dir, $OutputDir) = @_;
  
  my $Filename;

  my $epochtime = time;
  
  if($Config{UseHostnameInZipname}){
      my $hostname = `hostname`."XXX";
      $Filename = sprintf("%s/%s_%d_%d.zip", $OutputDir, substr($hostname,0,3), $$, $ZipFileCount++);
  } else {
      $Filename = sprintf("%s/%d_%d_%d.zip", $OutputDir, $epochtime, $$, $ZipFileCount++);
  }
  
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
  
  return upload($Filename);
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
    return 0;
  } 
  
  if($Config{DeleteZipFilesAfterUpload}){
    unlink($File);
  }
  else
  {
    rename($File, $File."_uploaded");
  }
  
  return 1;
}

