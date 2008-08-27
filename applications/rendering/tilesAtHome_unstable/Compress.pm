package Compress;

use warnings;
use strict;
use File::Copy;
use File::Path;
use Fcntl ':flock'; #import LOCK_* constants
use English '-no_match_vars';
use tahlib;
use lib::TahConf;

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

#-------------------------------------------------------------------
# Create a new Upload instance
#-------------------------------------------------------------------
sub new
{
    my $class = shift;
    my $self  = {};

    $self = {
        Config  => TahConf->getConfig(),
    };
    $self->{TileDir} = $self->{Config}->get("WorkingDirectory"),
    $self->{ZipDir}  = File::Spec->catdir($self->{Config}->get("WorkingDirectory"), "/uploadable"),

    bless ($self, $class);

    #set global progressbar task
    $::currentSubTask ='compress';
    return $self;
}

#-------------------------------------------------------------------
# main function. Compresses all tileset.dir's in $self->{WorkingDir}.
# returns (success, reason), with success being 1
# -1 on error. reason is a string explaining the error.
#-------------------------------------------------------------------
sub compressAll
{
    my $self = shift;
    my $Config = $self->{Config};

    my $progress = 0;
    $::progressPercent = 0;
    $::currentSubTask = "compress";
    my $LOCKFILE;

    if ($Config->get("LocalSlippymap"))
    {
       my $reason = "No compressing - LocalSlippymap set in config file";
       ::statusMessage(1,6);
       return (0, $reason);
    }

    my (@prefixes,$allowedPrefixes);
    
    ::statusMessage("Searching for tilesets in ".$self->{TileDir},0,3);

    # compile a list of the "Prefix" values of all configured layers,
    # separated by |
    foreach my $layer(split(/,/, $Config->get("LayersCapability")))
    {
        push(@prefixes, $Config->get($layer."_Prefix"));
    }
    $allowedPrefixes = join('|', @prefixes);

    # Go through all files in TileDir and grep the right directories
    opendir(my $dp, $self->{TileDir}) 
      or return (-1, "Can't open directory ".$self->{TileDir});
    my @dir = readdir($dp);
    my @tilesets = grep { /($allowedPrefixes)_\d+_\d+_\d+\.dir$/ } @dir;
    closedir($dp);
        
    foreach my $File(@tilesets)
    {   # go through all complete tilesets ie "*.dir" firectories
        $File =~ m{^([^_]+)_};
        my $layer = $1;
        my $FullTilesetPath = File::Spec->join($self->{TileDir}, $File);

        # get a file handle, then try to lock the file exclusively.
        # if flock fails it is being handled by a different upload process
        # also check if the file still exists when we get to it
        open ($LOCKFILE, '>', $FullTilesetPath."lock");
        my $flocked = !$Config->get('flock_available')
                      || ($LOCKFILE && flock($LOCKFILE, LOCK_EX|LOCK_NB));
        if ($flocked && -d $FullTilesetPath )
        {   # got exclusive lock, now compress
            $::currentSubTask ='optimize';
            ::statusMessage("optimizing PNG files",1,6);
            $self->optimizePNGs($FullTilesetPath, $layer);
            $::currentSubTask ='compress';
            $::progressPercent = 0;
            ::statusMessage("compressing $File",1,6);
            $self->compress($FullTilesetPath, $layer);
            # TODO: We always kill the tileset.dir independent of success and never return a success value!
            rmtree $FullTilesetPath;    # should be empty now
        }
        else
        {   # could not get exclusive lock, this is being handled elsewhere now
            ::statusMessage("$File compressed by different process. skipping",0,3);
        }
        # finally unlock zipfile and release handle
        if ($LOCKFILE)
        {
            flock ($LOCKFILE, LOCK_UN);
            close ($LOCKFILE);
            unlink($FullTilesetPath."lock") if $flocked;
	}
    }
    return (1,"");
}

#-----------------------------------------------------------------------------
# Compress all PNG files from one directory, creating a .zip file.
# Parameters:  FullTilesetPath, layername
#
# It will never delete the source files, so the caller has to delete them
# returns 1, if the zip command succeeded and 0 otherwise
#-----------------------------------------------------------------------------
sub compress
{
    my $self = shift;
    my $Config = $self->{Config};

    my ($FullTilesetPathDir, $Layer) = @_;
  
    my $Filename;

    my $hostname = '';
    if ($Config->get('UseHostnameInZipname'))
    {
        $hostname = `hostname`;
        chomp $hostname;
        $hostname = substr($hostname,0,4);
    }

    # Create the output directory if it doesn't exist...
    if( ! -d $self->{ZipDir} )
    {
        mkpath $self->{ZipDir};
    }

    $Filename = File::Spec->join($self->{ZipDir},
                                sprintf("%s_%d_%d_%s_tileset.zip",
                                $hostname, ::GetClientId(), $$, $Layer));
    
    # ZIP all the tiles into a single file
    my $stdOut = File::Spec->join($Config->get("WorkingDirectory"),"zip.stdout");
    my $Command1;
    if ($Config->get("7zipWin"))
    {
        $Command1 = sprintf("\"%s\" %s %s %s",
          $Config->get("Zip"),
          "a -tzip",
          $Filename,
          File::Spec->join($FullTilesetPathDir,"*.png"));

        print STDERR "7zip: $Command1\n\n";
    }
    else
    {
        $Command1 = sprintf("\"%s\" -r -j %s %s > %s",
          $Config->get("Zip"),
          $Filename,
          "$FullTilesetPathDir",
          $stdOut);
    }

    # Run the zip command
    my $zip_result = ::runCommand($Command1,$PID);

    # stdOut is currently never used, so delete it unconditionally    
    unlink($stdOut);
    
    return $zip_result;
}

#-----------------------------------------------------------------------------
# Run pngcrush on each split tile, then delete the temporary cut file
# parameter (FullPathToPNGDir, $layer)
# returns (success, reason)
#-----------------------------------------------------------------------------
sub optimizePNGs
{
    my $self = shift;
    my $Config = $self->{Config};
    my $PNGDir = shift;
    my $layer  = shift;

    $::progressPercent = 0;
    my $TmpFilename_suffix = ".cut";
    my $Redirect = ">/dev/null";
    my $Cmd;

    if ($^O eq "MSWin32")
    {
        $Redirect = "";
    }

    # read in all the PNG files in the Dir
    my @pngfiles;
    if (opendir(PNGDIR, $PNGDir))
    {
        @pngfiles = grep { /\.png$/ } readdir(PNGDIR);
        close PNGDIR;
    }
    else 
    {
       return (-1, "could not read $PNGDir");
    }

    my $NumPNG = scalar(@pngfiles);
    my $progress = 0;
    ::statusMessage("Optimizing $NumPNG images", 0, 3);

    foreach my $PngFileName(@pngfiles)
    {  # go through all PNG files
       $progress ++;
       $::progressPercent = 100 * $progress / $NumPNG;

       my $PngFullFileName = File::Spec->join($PNGDir, $PngFileName);
       # Temporary filename between quantizing and optimizing
       my $TmpFullFileName = $PngFullFileName.$TmpFilename_suffix;

       if ($Config->get($layer."_Transparent"))
       {    # Don't quantize if it's transparent
            rename($PngFullFileName, $TmpFullFileName);
       }
       elsif ($Config->get("PngQuantizer") eq "pngnq") 
       {
           $Cmd = sprintf("%s \"%s\" -e .png%s -s1 -n256 %s %s",
                                   $Config->get("Niceness"),
                                   $Config->get("pngnq"),
                                   $TmpFilename_suffix,
                                   $PngFullFileName,
                                   $Redirect);

           ::statusMessage("ColorQuantizing $PngFileName",0,10);
           if(::runCommand($Cmd,$PID))
           {   # Color quantizing successful
               unlink($PngFullFileName);
           }
           else
           {   # Color quantizing failed
               ::statusMessage("ColorQuantizing $PngFileName with ".$Config->get("PngQuantizer")." failed",1,0);
               rename($PngFullFileName, $TmpFullFileName);
            }
       }
       else
       {
           ::statusMessage("Not Color Quantizing $PngFileName, pngnq not installed?",0,6);
           rename($PngFullFileName, $TmpFullFileName);
       }

       # Finished quantizing. The file is in TmpFullFileName now.

       if ($Config->get("PngOptimizer") eq "pngcrush")
       {
           $Cmd = sprintf("%s \"%s\" -q %s %s %s",
                  $Config->get("Niceness"),
                  $Config->get("Pngcrush"),
                  $TmpFullFileName,
                  $PngFullFileName,
                  $Redirect);
       }
       elsif ($Config->get("PngOptimizer") eq "optipng")
       {
           $Cmd = sprintf("%s \"%s\" %s -out %s %s", #no quiet, because it even suppresses error output
                  $Config->get("Niceness"),
                  $Config->get("Optipng"),
                  $TmpFullFileName,
                  $PngFullFileName,
                  $Redirect);
       }
       else
       {
           ::statusMessage("SplitImageX:PngOptimizer not configured (should not happen, update from svn, and check config file)",1,0);
           ::talkInSleep("Install a PNG optimizer and configure it.",15);
       }

       ::statusMessage("Optimizing $PngFileName",0,10);
       if(::runCommand($Cmd,$PID))
       {
           unlink($TmpFullFileName);
       }
       else
       {
           ::statusMessage("Optimizing $PngFileName with ".$Config->get("PngOptimizer")." failed",1,0);
           rename($TmpFullFileName, $PngFullFileName);
       }

       # Assign the job time to this file
       # TODO:
       #utime $JobTime, $JobTime, $PngFullFileName;

    } # foreach my $PngFileName
}

1;
