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
use TahConf;

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
# Returns number of uploaded files.
#-------------------------------------------------------------------
sub uploadAllZips
{
    my $self = shift;
    my $Config = $self->{Config};
    $::progressPercent = 0;
    my $LOCKFILE;

    if ($Config->get("LocalSlippymap")) {
        throw UploadError "No upload - LocalSlippymap set in config file";
    }

    # read in all the zip files in ZipDir
    my @files;
    if (opendir(ZIPDIR, $self->{ZipDir})) {
        @files = grep { /\.(zip|tileset)$/ } readdir(ZIPDIR);
        closedir ZIPDIR;
    }
    else {
        return 0; # do nothing if ZipDir doesn't exist, just assume there is nothing to upload
    }
    my $file_count = scalar(@files);

    my $files_uploaded = 0; # num handled files
    foreach my $file (@files) {
        ::statusMessage(($file_count - $files_uploaded) . " files to upload", 0, 0);
        # get a file handle, then try to lock the file exclusively.
        # if flock fails it is being handled by a different upload process
        # also check if the file still exists when we get to it
        open ($LOCKFILE, '>', File::Spec->join($self->{ZipDir},$file . ".lock"));
        my $flocked = !$Config->get('flock_available')
                      || ($LOCKFILE && flock($LOCKFILE, LOCK_EX|LOCK_NB));
        if ($flocked && -e File::Spec->join($self->{ZipDir}, $file)) {
            # got exclusive lock, now upload

            my $UploadFailedHardOrDone=0;
            # while not upload success or complete failure
            while ($UploadFailedHardOrDone != 1) {
		my $res_str; #stores success or error msg for status line
                try {
                    $self->upload($file);

                    # reset sleepdelay
                    $self->{sleepdelay} = 0;
                    $res_str = "uploaded " . $file;
                    $files_uploaded++;
                    $::progressPercent = $files_uploaded * 100 / $file_count;
                    $UploadFailedHardOrDone = 1;
                }
                catch UploadError with {
                    my $err = shift();
                    if (my $queue_length = $err->value()) { 
                        # set sleepdelay to 30 seconds
                        $self->{sleepdelay} = 30;
                        $res_str = $err->text();
                    }
                    else {
                        $err->throw();
                    }
                };

                # Finally wait sleepdelay seconds until next upload
                ::talkInSleep($res_str, int($self->{sleepdelay}));
            }

        }
        else {
            # could not get exclusive lock, this is being handled elsewhere now
            ::statusMessage("$file uploaded by different process. skipping", 0, 3);
        }
        # finally unlock zipfile and release handle
        if ($LOCKFILE) {
            flock ($LOCKFILE, LOCK_UN);
            close ($LOCKFILE);
            unlink(File::Spec->join($self->{ZipDir}, $file . ".lock")) if $flocked;
        }
    }
    if($files_uploaded)
    {
      ::statusMessage("uploaded $files_uploaded ". ($files_uploaded == 1 ? "file" :"files"),1,3);
    }
    elsif($file_count) # we need to print something to get a line end
    {
      ::statusMessage("Nothing uploaded",1,3);
    }

    return $files_uploaded;
}

#-----------------------------------------------------------------------------
# Upload a ZIP or tileset file
# Parameter (filename) is the name of a file in ZipDir
#-----------------------------------------------------------------------------
sub upload
{
    my $self = shift;
    my $file_name = shift;
    my $file      = File::Spec->join($self->{ZipDir}, $file_name);
    my $file_size = -s $file;   # zip file size
    my $file_age  = -M $file;   # days since last modified
    my $Config = $self->{Config};

    # delete zips that are already older than 2 days.
    if ($file_age > 2) {
        if ($Config->get("DeleteZipFilesAfterUpload")) {
            unlink($file);
        }
        else {
            rename($file, $file . "_overage"); 
        }

        throw UploadError "File $file too old", "overage";
    }

    $file_name =~ m{^([^_]+)_(\d+)_(\d+)_(\d+)_(\d+)\.(zip|tileset)$};
    my $layer = $1;
    my $zoom = $2;
    my $x = $3;
    my $y = $4;
    my $client_id = $5;

    if (! $Config->get("UploadToDirectory")) {
        my $ua = LWP::UserAgent->new(keep_alive => 1, timeout => 360);
        
        $ua->protocols_allowed( ['http'] );
        $ua->agent("tilesAtHomeZip");
        $ua->env_proxy();
        push @{ $ua->requests_redirectable }, 'POST';
        
        my $URL = $Config->get("UploadURL");
        
        ::statusMessage("Uploading $file_name", 0, 3);
        my $res = $ua->post($URL,
                            Content_Type => 'form-data',
                            Content => [ file => [$file],
                                         user => $Config->get("UploadUsername"),
                                         passwd => $Config->get("UploadPassword"),
                                         version => $Config->get("ClientVersion"),
                                         layer => $layer,
                                         z => $zoom,
                                         x => $x,
                                         y => $y,
                                         client_uuid => ($::Mode eq "upload_loop") ? $client_id : ::GetClientId() ]);

        if (!$res->is_success()) {
            throw UploadError "Error uploading $file_name to $URL: " . $res->status_line, "ServerError"; # hard fail
        }
        else {
            print $res->content if ($Config->get("Debug"));
        }
    }
    else {
        #Upload To Directory rather than server
        ## Check "queue" length
        my @queue_files;
        if (opendir(UPDIR, $Config->get("UploadTargetDirectory"))) {
            @queue_files = grep { /\.(zip|tileset)$/ } readdir(UPDIR);
            closedir UPDIR;
        }
        else {
            throw UploadError "Can not open target directory";
        }

        my $queue_length = scalar(@queue_files);
        my $max_queue = $Config->get("UploadToDirectoryMaxQueue");
        if ($queue_length >= $max_queue) {
            ::statusMessage("Not uploading, upload directory full",0,0);
            sleep(1);
            throw UploadError "Not uploading, upload directory full", $queue_length;
        }
        else {
            my $tmpfile = File::Spec->join($Config->get("UploadTargetDirectory"), $file_name . "_part");

            # copy the file over using a temporary name
            copy($file, $tmpfile) or throw UploadError "Failed to copy file to Upload Directory: $!";
            # rename so it can be picked up by central uploading client.
            move($tmpfile, File::Spec->join($Config->get("UploadTargetDirectory"), $file_name)) 
                or throw UploadError "Failed to rename file in Upload Directory: $!";
        }
    }

    # if we didn't encounter any errors error we get here
    if($Config->get("DeleteZipFilesAfterUpload")) {
        unlink($file);
    }
    else {
        rename($file, $file . "_uploaded");
    }
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
