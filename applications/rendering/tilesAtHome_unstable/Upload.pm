package Upload;

use warnings;
use strict;
use LWP::UserAgent;
use File::Copy;
use File::Spec;
use Fcntl ':flock'; #import LOCK_* constants
use English '-no_match_vars';
use Error qw(:try);
use tahlib;
use lib::TahConf;

#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home, upload module
# Takes any tiles generated, adds them into ZIP files, and uploads them
#
# Contact OJW on the Openstreetmap wiki for help using this program
#-----------------------------------------------------------------------------
# Copyright 2006, Oliver White, Dirk-Lueder Kreie, Sebastian Spaeth
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

    $self->{sleepdelay} = ::getFault('upload') * 10;
    # uploading to a local directory is less costly
    $self->{MaxSleep} = $self->{Config}->get("UploadToDirectory") ? 30 : 600;

    #set global progressbar task
    $::currentSubTask ='upload';
    return $self;
}



#-------------------------------------------------------------------
# Returns number of uploaded zip files.
#-------------------------------------------------------------------
sub uploadAllZips
{
    my $self = shift;
    my $Config = $self->{Config};
    my $uploaded = 0; # num handled files
    $::progressPercent = 0;
    my $LOCKFILE;

    if ($Config->get("LocalSlippymap"))
    {
        throw UploadError "No upload - LocalSlippymap set in config file";
    }

    # read in all the zip files in ZipDir
    my @zipfiles;
    if (opendir(ZIPDIR, $self->{ZipDir}))
    {
        @zipfiles = grep { /\.zip$/ } readdir(ZIPDIR);
        closedir ZIPDIR;
    }
    else 
    {
        throw UploadError "could not read $self->{ZipDir}";
    }
    my $zipCount = scalar(@zipfiles);

    while(my $File = shift @zipfiles)
    {
        ::statusMessage((scalar(@zipfiles)+1)." zip files to upload",0,0);
        # get a file handle, then try to lock the file exclusively.
        # if flock fails it is being handled by a different upload process
        # also check if the file still exists when we get to it
        open ($LOCKFILE, '>', File::Spec->join($self->{ZipDir},$File.".lock"));
        my $flocked = !$Config->get('flock_available')
                      || ($LOCKFILE && flock($LOCKFILE, LOCK_EX|LOCK_NB));
        if ($flocked && -e File::Spec->join($self->{ZipDir},$File))
        {   # got exclusive lock, now upload

            my $Load;
            my $UploadFailedHardOrDone=0;
            # while not upload success or complete failure
            while ($UploadFailedHardOrDone != 1)
            {
		my $res_str; #stores success or error msg for status line
                try {
                    $Load = $self->upload($File);

                    # reduce sleepdelay by 25 per cent
                    $self->{sleepdelay} = 0.75 * $self->{sleepdelay};
                    $res_str = "uploaded ".$File;
                    $uploaded++;
                    $::progressPercent = $uploaded * 100 / $zipCount;
                    $UploadFailedHardOrDone = 1;
                }
                catch UploadError with {
                    my $err = shift();
                    if ($Load = $err->value()) { 
                        # try to keep the queue at 80% full,
                        # if more increase sleepdelay
                        $self->{sleepdelay} = $self->{sleepdelay} + ($Load - 800)/10;
                        $self->{sleepdelay} = 4  if ($self->{sleepdelay} < 4);
                        $res_str = "queue full";
                    }
                    else {
                        $err->throw();
                    }
                };

                # Finally wait sleepdelay seconds until next upload
                if ($self->{sleepdelay} > $self->{MaxSleep})
                {
                    $self->{sleepdelay} = $self->{MaxSleep};
                }
                ::talkInSleep($res_str, int($self->{sleepdelay}));
            }

        }
        else
        {   # could not get exclusive lock, this is being handled elsewhere now
            ::statusMessage("$File uploaded by different process. skipping",0,3);
        }
        # finally unlock zipfile and release handle
        if ($LOCKFILE)
        {
            flock ($LOCKFILE, LOCK_UN);
            close ($LOCKFILE);
            unlink(File::Spec->join($self->{ZipDir},$File.".lock")) if $flocked;
        }
    }
    ::statusMessage("uploaded $uploaded zip files",1,3) unless $uploaded == 0;
    return $uploaded;
}

#-----------------------------------------------------------------------------
# Upload a ZIP file
# Parameter (filename) is the name of a .zip file in ZipDir
# returns: Load
#   Load: Server queue 'fullness' between [0,1000]
#-----------------------------------------------------------------------------
sub upload
{
    my $self = shift;
    my $FileName = shift;
    my $File     = File::Spec->join($self->{ZipDir},$FileName);
    my $ZipSize  = -s $File;   # zip file size
    my $ZipAge   = -M $File;   # days since last modified
    my $Config = $self->{Config};

    # delete zips that are already older than 2 days.
    if($ZipAge > 2)
    {
        if($Config->get("DeleteZipFilesAfterUpload"))
        {
            unlink($File);
        }
        else
        {
            rename($File, $File."_overage"); 
        }

        throw UploadError "ZIP file $File too old", "overage";
    }

    $FileName =~ m{^([^_]+)_\d+_\d+_\d+_(\d+)\.zip$};
    my $Layer=$1;
    my $clientId = $2;

    if(! $Config->get("UploadToDirectory"))
    {
        my $ua = LWP::UserAgent->new(keep_alive => 1, timeout => 360);
        
        $ua->protocols_allowed( ['http'] );
        $ua->agent("tilesAtHomeZip");
        $ua->env_proxy();
        push @{ $ua->requests_redirectable }, 'POST';
        
        my $URL = $Config->get("UploadURL");
        
        my $Load = $self->UploadOkOrNot();

        # The server normalises to 1 (*1000) so 1000 means "queue is really 
        # full or even over-filled", so only do something if the load is 
        # less than that.
        if ($Load < 1000) 
        {
            ::statusMessage("Uploading $FileName",0,3);
            my $res = $ua->post($URL,
              Content_Type => 'form-data',
              Content => [ file => [$File],
                           user => $Config->get("UploadUsername"),
                           passwd => $Config->get("UploadPassword"),
                           version => $Config->get("ClientVersion"),
                           layer => $Layer,
                           client_uuid => ($::Mode eq "upload_loop") ? $clientId : ::GetClientId() ]);
             
            if(!$res->is_success())
            {
                ::statusMessage("ERROR",1,0);
                ::statusMessage("  Error uploading $FileName to $URL:",1,0);
                ::statusMessage("  ".$res->status_line,1,0);
                ::addFault('upload');
                throw UploadError "Error uploading $FileName to $URL: $res->status_line", "ServerError"; # hard fail
            }
            else
            {
                print $res->content if ($Config->get("Debug"));
                ::resetFault('upload');
            }
            
        }
        else
        {
            ::statusMessage("Not uploading, server queue full",0,0);
            sleep(1);
            throw UploadError "Not uploading, server queue full", $Load; #soft fail
        }
    }
    else
    {   #Upload To Directory rather than server
        ## Check "queue" length
        my $RemoteZipFileCount = 0;
        my @QueueFiles;
        if(opendir(UPDIR, $Config->get("UploadTargetDirectory")))
        {
            @QueueFiles = grep { /\.zip$/ } readdir(UPDIR);
            closedir UPDIR;
        }
        else 
        {
            throw UploadError "Can not open target directory";
        }
        my $QueueLength = scalar(@QueueFiles);
        my $Load = 1000 * $QueueLength/$Config->get("UploadToDirectoryMaxQueue");
        if ($Load > 900) # 95% or 100% with MaxQueue=20
        {
            ::statusMessage("Not uploading, upload directory full",0,0);
            sleep(1);
            throw UploadError "Not uploading, upload directory full", $Load;
        }
        else
        {
            my $tmpfile = File::Spec->join($self->{Config}->get("UploadTargetDirectory"),$FileName."_part");

            ## FIXME: Don't necessarily die here
            # copy the file over using a temporary name
            copy($File,$tmpfile) or throw UploadError "Failed to copy file to Upload Directory: $!";
            # rename so it can be picked up by central uploading client.
            move($tmpfile, File::Spec->join($Config->get("UploadTargetDirectory"), $FileName)) 
                or throw UploadError "Failed to rename file in Upload Directory: $!";
        }
    }

    # if we didn't encounter any errors error we get here
    if($Config->get("DeleteZipFilesAfterUpload"))
    {
        unlink($File);
    }
    else
    {
        rename($File, $File."_uploaded");
    }

    return 0;
}


#-----------------------------------------------------------
# check the go_nogo URL and retrieve the server upload queue
# returns the status between [0,1000] (0:empty, 1000:full)
# returns 1000 if an error occured while fetching the load
#-----------------------------------------------------------
sub UploadOkOrNot
{
    my $self = shift;
    my $Config = $self->{Config};
    ::statusMessage("Checking server queue",0,3);
    my $ua = LWP::UserAgent->new('agent' =>'tilesAtHome');
    $ua->env_proxy();
    my $res = $ua->get($Config->get("GoNogoURL"));

    if (! $res->is_success)
    {    # Failed to retrieve server load
         # $res->status_line; contains result here.
         ::statusMessage("Failed to retrieve server queue load. Assuming full queue.",1,0);
         return 1000;
   }
    # Load is a float value between [0,1]
    my $Load = $res->content;
    chomp $Load;
    return ($Load*1000);
}

#-----------------------------------------------------------------------------------------------------------------
# class UploadError
#
# Exception to be thrown by Upload methods

package UploadError;
use base 'Error::Simple';

1;
