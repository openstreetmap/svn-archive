#!/usr/bin/perl
use strict;
use File::Copy;
use English '-no_match_vars';
use tahconfig;
use tahlib;
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
my %Config = ReadConfig("tilesAtHome.conf", "general.conf", "authentication.conf", "layers.conf");

if ($Config{"LocalSlippymap"})
{
    print "No upload - LocalSlippymap set in config file\n";
    exit 1;
}

my $ZipFileCount = 0;

## FIXME: this is one of the things that make upload.pl not multithread safe
my $ZipDir = $Config{WorkingDirectory} . "/uploadable";

my @sorted;

# when called from tilesGen, use these for nice display
my $progress = 0;
my $progressPercent = 0;
my $progressJobs = $ARGV[1] or 1;
my $currentSubTask = "ziprun" . $ARGV[0] or " ";


my $lastmsglen;

### TODO: implement locking, this is one of the things that make compress.pl not multithread-safe.

my $tileCount;

my @tiles;

## TODO: re-implement MultipleClients so it shoves the completed zips onto the upload computer instead of the single tiles.

if($Config{MultipleClients}) #Trigger the _other_ codepath...
# move the finished tiles to a subfolder of UploadDirectory
# First name the folder timestamp_hostname_inprogress
# then rename the folder to timestamp_hostname
{
    my $epochtime = time;
    my $hostname = `hostname`;
    chomp $hostname;
    $hostname.="XXXXXXXX";
    my $UploadDir = $Config{UploadDirectory};
    my $WorkDir = $Config{WorkingDirectory};
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
    
    my $TileDir = $Config{WorkingDirectory};
    
    # Group and upload the tiles
    statusMessage("Searching for tiles in $TileDir", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
    # compile a list of the "Prefix" values of all configured layers,
    #     # separated by |
    
    foreach my $UploadLayer (split(/,/, $Config{"Layers"}))
    {
        $allowedPrefixes = $Config{"Layer.$UploadLayer.Prefix"}; #just select the current layer for compressing
        ## DEBUG print "\n.$allowedPrefixes.\n";
        opendir(my $dp, $TileDir) or die("Can't open directory $TileDir\n");
        my @dir = readdir($dp);
        @tiles = grep { /($allowedPrefixes)_\d+_\d+_\d+\.png$/ } @dir;
        my @tilesets = grep { /($allowedPrefixes)_\d+_\d+_\d+\.dir$/ } @dir;
        closedir($dp);
        
        foreach (@tilesets)        # not split into chunks
        {
            my $set = "$TileDir/$_";
            $set =~ s|\.dir$||;
            if (rename "$set.dir", "$set.upload") 
            {
                compress("$set.upload", $ZipDir, 'yes', $allowedPrefixes);
                rmdir "$set.upload";    # should be empty now
            }
            else
            {
                print STDERR "ERROR\n  Failed to rename $set.dir to $set.upload --tileset not uploaded\n";
            }
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
        statusMessage("done", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent, 0); 
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
    my $SizeLimit = $Config{"UploadChunkSize"} * $MB;
    my $CountLimit = $Config{"UploadChunkCount"};

    #prevent too small zips, 683=half a tileset
    $CountLimit = 683 if ($CountLimit < 100);

    mkdir $TempDir if ! -d $TempDir;
    mkdir $OutputDir if ! -d $OutputDir;

    $progressPercent = ( $tileCount - scalar(@tiles) ) * 100 / $tileCount;
    statusMessage(scalar(@tiles)." tiles to process for ".$allowedPrefixes, $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);

    while(($Size < $SizeLimit) && ($Count < $CountLimit) && (my $file = shift @tiles))
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
        statusMessage(sprintf("Got %d files (%d bytes), compressing", $Count, $Size), $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
        return compress($TempDir, $OutputDir, 'no', $allowedPrefixes);
    }
    else
    {

        statusMessage("compress finished", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
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
    
    if($Config{UseHostnameInZipname})
    {
        my $hostname = `hostname`."XXX";
        $Filename = sprintf("%s/%s_%d_%d_%s%s.zip", $OutputDir,
          substr($hostname,0,3), $$, $ZipFileCount++, $Layer, $SingleTileset);
    }
    else 
    {
        $Filename = sprintf("%s/%d_%d_%d_%s%s.zip", $OutputDir,
          $epochtime, $$, $ZipFileCount++, $Layer, $SingleTileset);
    }
    
    # ZIP all the tiles into a single file
    my $stdOut = $Config{WorkingDirectory}."/".$PID.".stdout";
    my $Command1;
    if ($Config{"7zipWin"})
    {
        $Command1 = sprintf("%s %s %s %s",
          $Config{"Zip"},
          "a -tzip",
          $Filename,
          "$Dir/*.png");
    }
    else
    {
        $Command1 = sprintf("%s -r -j %s %s > %s",
          $Config{"Zip"},
          $Filename,
          "$Dir",
          $stdOut);
    }
    # ZIP filename is currently our process ID plus a counter
    
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
            rename($Dir . "/" . $File, $Config{WorkingDirectory} . $File);
        }
    }
    
    my $ZipSize += -s $Filename; ## Add the 10 MB check here.
    if($ZipSize > $Config{ZipHardLimit} * 1000 * 1000) 
    {
        statusMessage($Filename." is larger than ".$Config{ZipHardLimit}." MB, retrying as split tileset.", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
        runCommand("unzip -qj $Filename -d $Config{WorkingDirectory}",$PID);

        if($Config{DeleteZipFilesAfterUpload})
        {
            unlink($Filename);
        }
        else
        {
            rename($Filename, $Filename."_oversized"); 
        }

    }

    return 1;
}
