package Compress;

use warnings;
use strict;
use File::Copy;
use File::Path;
use Fcntl ':flock'; #import LOCK_* constants
use English '-no_match_vars';
use Error qw(:try);
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
# Create a new Compress instance
#-------------------------------------------------------------------
sub new
{
    my $class = shift;
    my $self  = {};

    $self = {
        Config  => TahConf->getConfig(),
    };
    $self->{TileDir} = $self->{Config}->get("WorkingDirectory"),
    $self->{UploadDir}  = File::Spec->catdir($self->{Config}->get("WorkingDirectory"), "/uploadable"),

    bless ($self, $class);

    #set global progressbar task
    $::currentSubTask ='compress';
    return $self;
}

#-------------------------------------------------------------------
# main function. Compresses all tileset.dir's in $self->{TileDir}.
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
       throw CompressError "No compressing - LocalSlippymap set in config file", "LocalSlippymap";
    }

    my (@prefixes,$allowedPrefixes);
    
    ::statusMessage("Searching for tilesets to be compressed",0,3);

    # compile a list of the "Prefix" values of all configured layers,
    # separated by |
    foreach my $layer(split(/,/, $Config->get("LayersCapability")))
    {
        push(@prefixes, $Config->get($layer."_Prefix"));
    }
    $allowedPrefixes = join('|', @prefixes);

    # Go through all files in TileDir and grep the right directories
    opendir(my $dp, $self->{TileDir}) or throw CompressError "Can't open directory " . $self->{TileDir};
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
            ::statusMessage("optimizing PNG files",0,3);
            $self->optimizePNGs($FullTilesetPath, $layer);
            $::currentSubTask ='compress';
            $::progressPercent = 0;
            ::statusMessage("compressing $File",0,3);
            if ($Config->get("CreateTilesetFile")) {
                $self->createTilesetFile($FullTilesetPath);
            }
            else {
                $self->compress($FullTilesetPath);
            }
            # TODO: We always kill the tileset.dir independent of success and never return a success value!
            rmtree $FullTilesetPath;    # should be empty now
        }
        else
        {   # could not get exclusive lock, this is being handled elsewhere now
            ::statusMessage("$File compressed by different process. skipping",1,3);
        }
        # finally unlock zipfile and release handle
        if ($LOCKFILE)
        {
            flock ($LOCKFILE, LOCK_UN);
            close ($LOCKFILE);
            unlink($FullTilesetPath."lock") if $flocked;
	}
    }
}

#-----------------------------------------------------------------------------
# Compress all PNG files from one directory, creating a .zip file.
# Parameters:  FullTilesetPath
#
# It will never delete the source files, so the caller has to delete them
#-----------------------------------------------------------------------------
sub compress
{
    my $self = shift;
    my $Config = $self->{Config};

    my ($FullTilesetPathDir) = @_;
  
    my $Filename;
    my $Tempfile;

    $FullTilesetPathDir =~ m{([^_\/\\]+)_(\d+)_(\d+)_(\d+).dir$};
    my ($layer, $Z, $X, $Y) = ($1, $2, $3, $4);

    # Create the output directory if it doesn't exist...
    if( ! -d $self->{UploadDir} )
    {
        mkpath $self->{UploadDir};# TODO: Error handling
    }

    $Filename = File::Spec->join($self->{UploadDir},
                                sprintf("%s_%d_%d_%d_%d.zip",
                                $layer, $Z, $X, $Y, ::GetClientId()));
    
    $Tempfile = File::Spec->join($self->{TileDir},
                                sprintf("%s_%d_%d_%d_%d.zip",
                                $layer, $Z, $X, $Y, ::GetClientId()));
    # ZIP all the tiles into a single file
    # First zip into "$Filename.part" and move to "$Filename" when finished
    my $stdOut = File::Spec->join($self->{TileDir}, "zip.stdout");
    my $zipCmd;
    if ($Config->get("7zipWin"))
    {
        $zipCmd = sprintf('"%s" %s "%s" "%s"',
          $Config->get("Zip"),
          "a -tzip",
          $Tempfile,
          File::Spec->join($FullTilesetPathDir,"*.png"));
    }
    else
    {
        $zipCmd = sprintf('"%s" -r -j "%s" "%s" > "%s"',
          $Config->get("Zip"),
          $Tempfile,
          $FullTilesetPathDir,
          $stdOut);
    }

    if (::dirEmpty($FullTilesetPathDir))
    {
        ::statusMessage("Skipping emtpy tileset directory: $FullTilesetPathDir",1,0);
        return 0;
    }
    # Run the zip command
    ::runCommand($zipCmd, $PID) or throw CompressError "Error running $zipCmd";

    # stdOut is currently never used, so delete it unconditionally    
    unlink($stdOut);
    
    # rename to final name so any uploader could pick it up now
    move ($Tempfile, $Filename); # TODO: Error handling
    
    return 1;
}

#-----------------------------------------------------------------------------
# Pack all PNG files from one directory into a Tileset file.
# Parameters:  FullTilesetPath
#
# It will never delete the source files, so the caller has to delete them
#-----------------------------------------------------------------------------
sub createTilesetFile
{
    my $self = shift;
    my $Config = $self->{Config};

    my ($FullTilesetPathDir) = @_;
  
    my $Filename;
    my $Tempfile;

    $FullTilesetPathDir =~ m{([^_\/\\]+)_(\d+)_(\d+)_(\d+).dir$};
    my ($layer, $Z, $X, $Y) = ($1, $2, $3, $4);

    my @offsets;
    my $levels = 6;                       # number of layers in a tileset file, currently 6
    my $tiles = ((4 ** $levels) - 1) / 3; # 1365 for 6 zoom levels
    my $currpos = 8 + (4 * ($tiles + 1)); # 5472 for 6 zoom levels

    my $userid = 0; # the server will fill this in

    $Filename = File::Spec->join($self->{UploadDir},
                                 sprintf("%s_%d_%d_%d_%d.tileset",
                                         $layer, $Z, $X, $Y, ::GetClientId()));
    
    $Tempfile = File::Spec->join($self->{TileDir},
                                 sprintf("%s_%d_%d_%d_%d.tileset",
                                         $layer, $Z, $X, $Y, ::GetClientId()));

    open my $fh, ">$Tempfile" or throw CompressError "Couldn't open '$Tempfile' ($!)";
    seek $fh, $currpos, 0 or throw CompressError "Couldn't seek.";

    for my $iz (0 .. $levels - 1) {
        my $width = 2**$iz;
        for my $iy (0 .. $width-1) {
            for my $ix (0 .. $width-1) {
                my $Pngname = File::Spec->join($FullTilesetPathDir,
                                               sprintf("%s_%d_%d_%d.png",
                                                       $layer, $Z+$iz, $X*$width+$ix, $Y*$width+$iy));
                my $length = -s $Pngname;
                if (! -e $Pngname) {
                    push(@offsets, 0);
                }
                elsif ($length == 67) {
                    push(@offsets, 2);
                }
                elsif ($length == 69) {
                    push(@offsets, 1);
                }
                else {
                    open my $png, "<$Pngname" or throw CompressError "Couldn't open file $Pngname ($!)";
                    my $buffer;
                    if( read($png, $buffer, $length) != $length ) {
                        throw CompressError "Read failed from $Pngname ($!)"
                    }
                    close $png;
                    print $fh $buffer or throw CompressError "Write failed on output to $Tempfile ($!)";
                    push @offsets, $currpos;
                    $currpos += $length;
                }
            }
        }
    }
    push @offsets, $currpos;

    if( scalar( @offsets ) != $tiles + 1 ) {
        throw CompressError "Bad number of offsets: " . scalar( @offsets );
    }

    seek $fh, 0, 0;
    print $fh pack("CxxxVV*", 1, $userid, @offsets) or throw CompressError "Write failed to $Tempfile ($!)";
    close $fh;

    move($Tempfile, $Filename) or throw CompressError "Could not move tileset file $Tempfile to $Filename ($!)";
}


#-----------------------------------------------------------------------------
# Run pngcrush on each split tile, then delete the temporary cut file
# parameter (FullPathToPNGDir, $layer)
#-----------------------------------------------------------------------------
sub optimizePNGs
{
    my $self = shift;
    my $Config = $self->{Config};
    my $PNGDir = shift;
    my $layer  = shift;
    my $transparent_layer = int($Config->get($layer."_Transparent"));

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
        closedir PNGDIR;
    }
    else 
    {
       throw CompressError "could not read $PNGDir";
    }

    my $NumPNG = scalar(@pngfiles);
    my $progress = 0;
    ::statusMessage("Optimizing $NumPNG images", 0, 3);

    foreach my $PngFileName(@pngfiles)
    {  # go through all PNG files
       $progress ++;
       $::progressPercent = 100 * $progress / $NumPNG;

       my $PngFullFileName = File::Spec->join($PNGDir, $PngFileName);
       # don't optimize empty sea or empty land tiles (file size 67 and 69)
       next if ((-s $PngFullFileName) =~ /67|69/);

       # Temporary filename between quantizing and optimizing
       my $TmpFullFileName = $PngFullFileName.$TmpFilename_suffix;

       if ($transparent_layer)
       {    # Don't quantize if it's transparent
            rename($PngFullFileName, $TmpFullFileName);
       }
       elsif (($Config->get("PngQuantizer")||'') eq "pngnq") 
       {
           $Cmd = sprintf("%s \"%s\" -e .png%s -s1 -n256 %s %s",
                                   $Config->get("Niceness"),
                                   $Config->get("pngnq"),
                                   $TmpFilename_suffix,
                                   $PngFullFileName,
                                   $Redirect);

           ::statusMessage("ColorQuantizing $PngFileName",0,6); 
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

       ::statusMessage("Optimizing $PngFileName",0,6);
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


#-----------------------------------------------------------------------------------------------------------------
# class CompressError
#
# Exception to be thrown by Compress methods

package CompressError;
use base 'Error::Simple';

1;
