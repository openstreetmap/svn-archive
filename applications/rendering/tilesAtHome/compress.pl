#!/usr/bin/perl
use strict;
use File::Copy;
use Fcntl ':flock'; #import LOCK_* constants
use English '-no_match_vars';
use tahconfig;
use tahlib;
use AppConfig qw(:argcount);

#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home, compress module
# Takes any tiles generated and adds them into ZIP files
#
# Contact OJW on the Openstreetmap wiki for help using this program
#-----------------------------------------------------------------------------
# Copyright 2006, Oliver White, Dirk-Lueder Kreie
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
our $Config = AppConfig->new({
                CREATE => 1,                      # Autocreate unknown config variables
                GLOBAL => {
                  DEFAULT  => undef,    # Create undefined Variables by default
                  ARGCOUNT => ARGCOUNT_ONE, # Simple Values (no arrays, no hashmaps)
                }
              });

$Config->define("help|usage!");
$Config->define("nodownload=s");
$Config->set("nodownload",0);
$Config->file("config.defaults", "layers.conf", "tilesAtHome.conf", "authentication.conf"); #first read configs in order, each (possibly) overwriting settings from the previous
$Config->args();              # overwrite config options with command line options
$Config->file("general.conf");  # overwrite with hardcoded values that must not be changed

if ($Config->get("LocalSlippymap"))
{
    print "No upload - LocalSlippymap set in config file\n";
    exit 1;
}

my $ZipFileCount = 0;

my $ZipDir = $Config->get("WorkingDirectory") . "/uploadable";

my @sorted;

# when called from tilesGen, use these for nice display
my $progress = 0;
our $progressPercent = 0;
our $progressJobs = $ARGV[0] or 1;
our $currentSubTask = "zipping";


my $tileCount;

my @tiles;

## TODO: re-implement MultipleClients so it shoves the completed zips onto the upload computer instead of the single tiles.

if($Config->get("MultipleClients")) #Trigger the _other_ codepath...
# move the finished tiles to a subfolder of UploadDirectory
# First name the folder timestamp_hostname_inprogress
# then rename the folder to timestamp_hostname
{
    print "MultipleClients config option is deprecated, use UploadToDirectory instead.\n\n";
    my $epochtime = time;
    my $hostname = `hostname`;
    chomp $hostname;
    $hostname.="XXXXXXXX";
    my $UploadDir = $Config->get("UploadDirectory");
    my $WorkDir = $Config->get("WorkingDirectory");
    my $folder = sprintf("%s/%s_%s_%d", $UploadDir, $epochtime, substr($hostname,0,8),$$);
    while(-e $folder)  # avoid the improbable... the folder exists.
    {
        $folder .= "x";
    }
    my $inprogress = $folder."_inprogress";
    print "Making dir\n";
    mkdir($inprogress);
    print "Moving to progress\n";
    for my $tilefile ( glob "$WorkDir/*" ) 
    {
         next if -d $tilefile; # skip folders
         print "Moving $tilefile to $inprogress\n";
         move($tilefile,$inprogress) or die "$!\n";
    }  
    print "Rename progress dir\n";
    move("$folder"."_inprogress",$folder) or die "$!\n"; 
  
    # the files should be on the upload computer now!!!
}
else
{

   
    # We calculate % differently this time so we don't need "progress"
    # $progress = 0;
    
    $progressPercent = 0;
    
    my $allowedPrefixes;
    
    my $TileDir = $Config->get("WorkingDirectory");
    
    # Group and upload the tiles
    statusMessage("Searching for tiles in $TileDir",0,3);
    # compile a list of the "Prefix" values of all configured layers,
    #     # separated by |
    
    foreach my $UploadLayer (split(/,/, $Config->get("LayersCapability")))
    {
        $allowedPrefixes = $Config->get($UploadLayer."_Prefix"); #just select the current layer for compressing
        ## DEBUG print "\n.$allowedPrefixes.\n";
        opendir(my $dp, $TileDir) or die("Can't open directory $TileDir\n");
        my @dir = readdir($dp);
        @tiles = grep { /($allowedPrefixes)_\d+_\d+_\d+\.png$/ } @dir;
        my @tilesets = grep { /($allowedPrefixes)_\d+_\d+_\d+\.dir$/ } @dir;
        closedir($dp);
        
        foreach my $File(@tilesets)
        {   # go through all complete tilesets ie "*.dir" firectories
            my $FullTileDirPath = "$TileDir/$File";

            # get a file handle, then try to lock the file exclusively.
            # if open fails (file has been uploaded and removed by other process)
            # the subsequent flock will also fail and skip the file.
            # if just flock fails it is being handled by a different upload process
            open (ZIPDIR, $FullTileDirPath);
            if (flock(ZIPDIR, LOCK_EX|LOCK_NB))
            {   # got exclusive lock, now compress
                compress($FullTileDirPath, $ZipDir, 'yes', $allowedPrefixes);
                # TODO: We always kill the tileset.dir independent of success and never return a success value!
                rmdir $FullTileDirPath;    # should be empty now
            }
            else
            {   # could not get exclusive lock, this is being handled elsewhere now
                statusMessage("$File compressed by different process. skipping",0,3);
            }
            # finally unlock zipfile and release handle
            flock (ZIPDIR, LOCK_UN);
            close (ZIPDIR);
        }

        ## look again in the workdir, there might be new files from split tilesets:
        
        opendir($dp, $TileDir) or die("Can't open directory $TileDir\n");
        @dir = readdir($dp);
        @tiles = grep { /($allowedPrefixes)_\d+_\d+_\d+\.png$/ } @dir;
        closedir($dp);
        
        $tileCount = scalar(@tiles);
        
        if ($tileCount) 
        {
            while (processTileBatch(
              $TileDir, 
              $TileDir . "/gather", ## FIXME: this is one of the things that make compress.pl not multithread safe
              $ZipDir, 
              $allowedPrefixes)) 
            {};
        }
        statusMessage("done",0,3); 
        ## TODO: fix progress display
    }
} #done main/else.

#-----------------------------------------------------------------------------
# Moves tiles into a "gather" directory until a certain size is reached,
# then compress and upload those files
#-----------------------------------------------------------------------------
sub processTileBatch
{
    my ($TileDir, $TempDir, $OutputDir, $allowedPrefixes) = @_;
    my ($Size,$Count) = (0,0);
    my $MB = 1024*1024;

    mkdir $TempDir if ! -d $TempDir;
    mkdir $OutputDir if ! -d $OutputDir;

    $progressPercent = ( $tileCount - scalar(@tiles) ) * 100 / $tileCount;
    statusMessage(scalar(@tiles)." tiles to process for ".$allowedPrefixes,0,3);

    while(my $file = shift @tiles)
    {
        my $Filename1 = "$TileDir/$file";
        my $Filename2 = "$TempDir/$file";
        if($file =~ /($allowedPrefixes)_\d+_\d+_\d+\.png$/i)
        {
            $Size += -s $Filename1;
            $Count++;

            rename($Filename1, $Filename2);
        }
    }

    $progressPercent = ( $tileCount - scalar(@tiles) ) * 100 / $tileCount; 
    
    if($Count)
    {
        statusMessage(sprintf("Got %d files (%d bytes), compressing", $Count, $Size),0,3);
        return compress($TempDir, $OutputDir, 'no', $allowedPrefixes);
    }
    else
    {

        statusMessage("compress finished",0,3);
        return 0;
    }
}

#-----------------------------------------------------------------------------
# Compress all PNG files from one directory, creating 
#-----------------------------------------------------------------------------
sub compress
{
    my ($Dir, $OutputDir, $SingleTileset, $Layer) = @_;
    $SingleTileset = ($SingleTileset eq 'yes' ? '_tileset' : '');
  
    my $Filename;

    my $epochtime = time;
  
    # Create the output directory if it doesn't exist...
    if( ! -e $OutputDir )
    {
        mkdir $OutputDir;
    }
    
    if($Config->get("UseHostnameInZipname"))
    {
        my $hostname = `hostname`;
        chomp $hostname;
        $hostname .= "XXXXX";
        $Filename = sprintf("%s/%d_%s_%d_%d_%d_%s%s.zip", $OutputDir, $epochtime,
          substr($hostname,0,5), GetClientId(), $$, $ZipFileCount++, $Layer, $SingleTileset);
    }
    else 
    {
        $Filename = sprintf("%s/%d_%d_%d_%d_%s%s.zip", $OutputDir,
          $epochtime, GetClientId(), $$, $ZipFileCount++, $Layer, $SingleTileset);
    }
    
    # ZIP all the tiles into a single file
    my $stdOut = $Config->get("WorkingDirectory")."/".$PID.".stdout";
    my $Command1;
    if ($Config->get("7zipWin"))
    {
        $Command1 = sprintf("\"%s\" %s %s %s",
          $Config->get("Zip"),
          "a -tzip",
          $Filename,
          "$Dir/*.png");
    }
    else
    {
        $Command1 = sprintf("\"%s\" -r -j %s %s > %s",
          $Config->get("Zip"),
          $Filename,
          "$Dir",
          $stdOut);
    }
    
    ## FIXME: this is one of the things that make compress.pl not multithread safe
    # Delete files in the gather directory
    opendir (GATHERDIR, $Dir);
    my @zippedFiles = grep { /.png$/ } readdir(GATHERDIR);
    closedir (GATHERDIR);
    
    # Run the two commands
    if (runCommand($Command1,$PID)) 
    {
        killafile($stdOut);
        while(my $File = shift @zippedFiles)
        {
            killafile ($Dir . "/" . $File);
        }
    }
    else
    {
        while(my $File = shift @zippedFiles)
        {
            rename($Dir . "/" . $File, $Config->get("WorkingDirectory") . $File);
        }
    }
    
    return 1;
}
