#!/usr/bin/perl
use strict;
use LWP::UserAgent;
use File::Copy;
use tahconfig;
use tahlib;
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
my %Config = ReadConfig("tilesAtHome.conf", "general.conf", "authentication.conf", "layers.conf");
my $ZipFileCount = 0;

my $ZipDir = $Config{WorkingDirectory} . "/uploadable";

my @sorted;

# when called from tilesGen, use these for nice display
my $progress = 0;
my $progressPercent = 0;
my $progressJobs = $ARGV[0] or 1;
my $currentSubTask;
 
my $lastmsglen;

# Upload any ZIP files which are still waiting to go
if(opendir(ZIPDIR, $ZipDir)){
  $currentSubTask = "uploadZ";
  $progress = 0;
  my @zipfiles = grep { /\.zip$/ } readdir(ZIPDIR);
  close ZIPDIR;
  @sorted = sort { $a cmp $b } @zipfiles; # sort by ASCII value (i.e. upload oldest first if timestamps used)
  my $zipCount = scalar(@sorted);
  statusMessage(scalar(@sorted)." zip files to upload", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
  while(my $File = shift @sorted){
    if($File =~ /\.zip$/i){
      upload("$ZipDir/$File");
    }
    $progress++;
    $progressPercent = $progress * 100 / $zipCount;
    statusMessage(scalar(@sorted)." zip files left to upload", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
  }
}

$currentSubTask = " upload";

# We calculate % differently this time so we don't need "progress"
# $progress = 0;

$progressPercent = 0;

my $TileDir = $Config{WorkingDirectory};

# Group and upload the tiles
statusMessage("Searching for tiles in $TileDir", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
opendir(my $dp, $TileDir) or die("Can't open directory $TileDir\n");
# compile a list of the "Prefix" values of all configured layers,
#     # separated by |
my $allowedPrefixes = join("|",
  map($Config{"Layer.$_.Prefix"}, split(/,/,$Config{"Layers"})));

my @tiles = grep { /($allowedPrefixes)_[0-9]*_[0-9]*_[0-9]*\.png$/ } readdir($dp);
closedir($dp);

my $tileCount = scalar(@tiles);

exit if ($tileCount == 0);

while (uploadTileBatch(
  $TileDir, 
  $TileDir . "/gather", 
  $ZipDir, $allowedPrefixes)) {};

#-----------------------------------------------------------------------------
# Moves tiles into a "gather" directory until a certain size is reached,
# then compress and upload those files
#-----------------------------------------------------------------------------
sub uploadTileBatch(){
  my ($TileDir, $TempDir, $OutputDir, $allowedPrefixes) = @_;
  my ($Size,$Count) = (0,0);
  my $MB = 1024*1024;
  my $SizeLimit = $Config{"UploadChunkSize"} * $MB;
  my $CountLimit = $Config{"UploadChunkCount"};

  #prevent too small zips, 683=half a tileset
  $CountLimit = 683 if ($CountLimit < 100);

  mkdir $TempDir if ! -d $TempDir;
  mkdir $OutputDir if ! -d $OutputDir;
 
  $progressPercent = ( $tileCount - scalar(@tiles) ) * 100 / $tileCount;
  statusMessage(scalar(@tiles)." tiles to process", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);

  while(($Size < $SizeLimit) && ($Count < $CountLimit) && (my $file = shift @tiles)){
    my $Filename1 = "$TileDir/$file";
    my $Filename2 = "$TempDir/$file";
    if($file =~ /($allowedPrefixes)_\d+_\d+_\d+\.png$/i){
      $Size += -s $Filename1;
      $Count++;
      
      rename($Filename1, $Filename2);
    }
  }
  
  if($Count){
    statusMessage(sprintf("Got %d files (%d bytes), compressing", $Count, $Size), $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
    return compressTiles($TempDir, $OutputDir);
  }
  else
  {
    $progressPercent = ( $tileCount - scalar(@tiles) ) * 100 / $tileCount;
    statusMessage("upload finished", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
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
  
  statusMessage("Uploading $File", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
  my $res = $ua->post($URL,
    Content_Type => 'form-data',
    Content => [ file => [$File], mp => $Password, version => $Config{ClientVersion} ]);
    
  if(!$res->is_success()){
    print STDERR "ERROR\n";
    print STDERR "  Error uploading $File to $URL:\n";
    print STDERR "  ".$res->status_line."\n";
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

