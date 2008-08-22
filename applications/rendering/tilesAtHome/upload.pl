#!/usr/bin/perl
use strict;
use LWP::UserAgent;
use File::Copy;
use Fcntl ':flock'; #import LOCK_* constants
use English '-no_match_vars';
use tahlib;
use lib::TahConf;

#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home, upload module
# Takes any tiles generated, adds them into ZIP files, and uploads them
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
if ($#ARGV < 0) 
{  # no command line option supplied, we require ($Mode, $progressJobs)
   die "please call \"tilesGen.pl upload\" instead";
}


# conf file, will contain username/password and environment info
# Read the config
my $Config = TahConf->getConfig();

if ($Config->get("LocalSlippymap"))
{
    print "No upload - LocalSlippymap set in config file\n";
    exit 1;
}


my $ZipFileCount = 0;

## FIXME: this is one of the things that make upload.pl not multithread safe
my $ZipDir = $Config->get("WorkingDirectory") . "/uploadable";

my @sorted;

# when called from tilesGen, use these for nice display
my $progress = 0;
our $progressPercent = 0;
our $progressJobs;
our $currentSubTask = "upload";

my $Mode;

($Mode, $progressJobs) = @ARGV;

my $sleepdelay;
my $failFile = $Config->get("WorkingDirectory") . "/failurecount.txt";
if (open(FAILFILE, "<", $failFile))
{
    $sleepdelay = <FAILFILE>;
    chomp $sleepdelay;
    close FAILFILE;
}
elsif (open(FAILFILE, ">", $failFile))
{
    $sleepdelay = 0; 
    print FAILFILE $sleepdelay;
    close FAILFILE;
}
else
{
    die("can't open $failFile");

}

# Upload any ZIP files which are still waiting to go. This is the main part.
processOldZips();

## update the failFile with current failure count from processOldZips

if (open(FAILFILE, ">", $failFile))
{
    print FAILFILE $sleepdelay;
    close FAILFILE;
}

### end main
###-------------------------------------------------------------------------

## FIXME: All the load processing here (the 1000 factor) assumes the server
## only ever returns load in full 1% steps, this breaks if it reports with
## 0.1% "accuracy".

sub processOldZips
{
    my $Config = TahConf->getConfig();
    my $MaxDelay;
    my @zipfiles;
    if(opendir(ZIPDIR, $ZipDir))
    {
        $progress = 0;
        $progressPercent = 0;
        @zipfiles = grep { /\.zip$/ } readdir(ZIPDIR);
        close ZIPDIR;
    }
    else 
    {
        return 0;
    }
    my $zipCount = scalar(@zipfiles);
    statusMessage($zipCount." zip files to upload",0,0);
    my $Reason = "queue full";
    if(($Config->get("UploadToDirectory")) and (-d $Config->get("UploadTargetDirectory")))
    {
        $MaxDelay = 30; ## uploading to a local directory is a lot less costly than checking the tileserver.
    }
    else
    {
        $MaxDelay = 600;
    }
    while(my $File = shift @zipfiles)
    {
        # get a file handle, then try to lock the file exclusively.
        # if open fails (file has been uploaded and removed by other process)
        # the subsequent flock will also fail and skip the file.
        # if just flock fails it is being handled by a different upload process
        open (ZIPFILE, "$ZipDir/$File");
        if (flock(ZIPFILE, LOCK_EX|LOCK_NB))
        {   # got exclusive lock, now upload

            my $Load;
            my $UploadFailedHardOrDone=0;
            # while not upload success or complete failure
            while ($UploadFailedHardOrDone != 1)
            {
                ($UploadFailedHardOrDone,$Load) = upload("$ZipDir/$File");

                # 10 is 1% of 1000, which is the assumed minimum resolution of the server return value
                if (($UploadFailedHardOrDone == 0) and ($Load > 10))
                {
                    $sleepdelay = 4  if ($sleepdelay < 4);
                    $sleepdelay = 1.25 * $sleepdelay * (1.25 * ($Load/1000)); ## 1.25 * 0.8 = 1 -> try to keep the queue at 80% full, if more increase sleepdelay by 25% plus the amount the queue is too full.
                    $Reason = "queue full";
                }
                elsif ($UploadFailedHardOrDone == 1) ## success
                {
                    $sleepdelay = 0.75 * $sleepdelay; # reduce sleepdelay by 25%
                    $Reason = "uploaded ".$File;
                    $progress++;
                    $progressPercent = $progress * 100 / $zipCount;
                }
                elsif ($UploadFailedHardOrDone == -1) ## hard fail
                {
                    $sleepdelay = int($sleepdelay) + 1; 
                    last;
                }

                if ($sleepdelay > $MaxDelay)
                {
                    $sleepdelay = $MaxDelay;
                }

                talkInSleep($Reason, int($sleepdelay));
            }

        }
        else
        {   # could not get exclusive lock, this is being handled elsewhere now
            statusMessage("$File uploaded by different process. skipping",0,3);
        }
        # finally unlock zipfile and release handle
        flock (ZIPFILE, LOCK_UN);
        close (ZIPFILE);
        statusMessage(scalar(@zipfiles)." zip files left to upload",0,3);

    }
}

#-----------------------------------------------------------------------------
# Upload a ZIP file, returns Status and Load
#-----------------------------------------------------------------------------
sub upload
{
    my ($File) = @_;
    my $Config = TahConf->getConfig();
    my $ZipSize += -s $File;
    my $ZipAge = -M $File;   # days since last modified

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

        return (-1,0);
    }

    $File =~ m{_(\d+)_\d+_\d+_([^_]+)(_tileset)?\.zip}x;
    my $clientId = $1;
    my $Layer=$2;

    if((! $Config->get("UploadToDirectory")) or (! -d $Config->get("UploadTargetDirectory")))
    {
        my $ua = LWP::UserAgent->new(keep_alive => 1, timeout => 360);
        
        $ua->protocols_allowed( ['http'] );
        $ua->agent("tilesAtHomeZip");
        $ua->env_proxy();
        push @{ $ua->requests_redirectable }, 'POST';
        
        my $URL = $Config->get("UploadURL");
        
        my $Load = UploadOkOrNot();

        # The server normalises to 1 (*1000) so 1000 means "queue is really 
        # full or even over-filled", so only do something if the load is 
        # less than that.
        if ($Load < 1000) 
        {
            statusMessage("Uploading $File",0,3);
            my $res = $ua->post($URL,
              Content_Type => 'form-data',
              Content => [ file => [$File],
                           user => $Config->get("UploadUsername"),
                           passwd => $Config->get("UploadPassword"),
                           version => $Config->get("ClientVersion"),
                           layer => $Layer,
                           client_uuid => ($Mode eq "upload_loop") ? $clientId : GetClientId() ]);
             
            if(!$res->is_success())
            {
                statusMessage("ERROR",1,0);
                statusMessage("  Error uploading $File to $URL:",1,0);
                statusMessage("  ".$res->status_line,1,0);
                return (-1,$Load); # hard fail
            }
            else
            {
                print $res->content if ($Config->get("Debug"));
            }
            
        }
        else
        {
            statusMessage("Not uploading, server queue full",0,0);
            sleep(1);
            return (0,$Load); #soft fail
        }
    }
    else
    {
        ## Check "queue" length
        my $RemoteZipFileCount = 0;
        my $MaxQueue = 20;
        my @QueueFiles;
        if(opendir(UPDIR, $Config->get("UploadTargetDirectory")))
        {
            @QueueFiles = grep { /\.zip$/ } readdir(UPDIR);
            close UPDIR;
        }
        else 
        {
            return (-1,1000);
        }
        my $QueueLength = scalar(@QueueFiles);
        my $Load = 1000 * $QueueLength/$MaxQueue;
        if ($Load > 900) # 95% or 100% with MaxQueue=20
        {
            statusMessage("Not uploading, upload directory full",0,0);
            sleep(1);
            return (0,$Load);
        }
        else
        {
            my $FileName = $File;
            $FileName =~ s|.*/||;       # Get the source filename without path
            print "\n$File $FileName\n" if $Config->get("Debug");    #Debug info

            ## FIXME: Don't necessarily die here
            copy($File,$Config->get("UploadTargetDirectory")."/".$FileName."_trans") or die "$!\n"; # copy the file over using a temporary name
            rename($Config->get("UploadTargetDirectory")."/".$FileName."_trans", $Config->get("UploadTargetDirectory")."/".$FileName) or die "$!\n"; 
            # rename so it can be picked up by central uploading client.
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

    return (1,0);
}


#-----------------------------------------------------------
# check the go_nogo URL and retrieve the server upload queue
# returns the status between [0,1000] (0:empty, 1000:full)
# returns 1000 if an error occured while fetching the load
#-----------------------------------------------------------
sub UploadOkOrNot
{
    my $Config = TahConf->getConfig();
    statusMessage("Checking server queue",0,3);
    my $ua = LWP::UserAgent->new('agent' =>'tilesAtHome');
    my $res = $ua->get($Config->get("GoNogoURL"));

    if (! $res->is_success)
    {    # Failed to retrieve server load
         # $res->status_line; contains result here.
         statusMessage("Failed to retrieve server queue load. Assuming full queue.",1,0);
         return 1000;
   }
    # Load is a float value between [0,1]
    my $Load = $res->content;
    chomp $Load;
    return ($Load*1000);
}
