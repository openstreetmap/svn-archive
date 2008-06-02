#!/usr/bin/perl
use strict;
use FindBin qw($Bin);
use LWP::UserAgent;
use File::Copy;
use English '-no_match_vars';
use tahconfig;
use tahlib;
use AppConfig qw(:argcount);

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

if (not $ARGV[0]) 
{
   die "please call \"tilesGen.pl upload\" instead";
}

# conf file, will contain username/password and environment info
# Read the config file
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

## FIXME: this is one of the things that make upload.pl not multithread safe
my $ZipDir = $Config->get("WorkingDirectory") . "/uploadable";

my @sorted;

# when called from tilesGen, use these for nice display
my $progress = 0;
my $progressPercent = 0;
my $progressJobs = $ARGV[1];
my $currentSubTask;
 
my $lastmsglen;

### TODO: implement locking, this is one of the things that make upload not multithread-safe.
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


### don't compress, this is handled from tilesGen.pl now

# Upload any ZIP files which are still waiting to go
processOldZips($ARGV[0]); # ARGV[0] is there or we would have exited in init (on or about line 32)

## update the failFile with current failure count from processOldZips

if (open(FAILFILE, ">", $failFile))
{
    print FAILFILE $sleepdelay;
    close FAILFILE;
}

## end main

sub processOldZips
{
    my $MaxDelay;
    my ($runNumber) = @_;
    my @zipfiles;
    if(opendir(ZIPDIR, $ZipDir))
    {
        $currentSubTask = "upload" . $runNumber;
        $progress = 0;
        $progressPercent = 0;
        @zipfiles = grep { /\.zip$/ } readdir(ZIPDIR);
        close ZIPDIR;
    }
    else 
    {
        return 0;
    }
    @sorted = sort { $a cmp $b } @zipfiles; # sort by ASCII value (i.e. upload oldest first if timestamps used)
    my $zipCount = scalar(@sorted);
    statusMessage(scalar(@sorted)." zip files to upload", $currentSubTask, $progressJobs, $progressPercent,0);
    my $Reason = "queue full";
    if(($Config->get("UploadToDirectory")) and (-d $Config->get("UploadTargetDirectory")))
    {
        $MaxDelay = 30; ## uploading to a local directory is a lot less costly than checking the tileserver.
    }
    else
    {
        $MaxDelay = 600;
    }
    while(my $File = shift @sorted)
    {
        if($File =~ /\.zip$/i)
        {
            
            my $FailureMode = 0; # 0 ->hard failure (i.e. Err503 on upload), 
                                 # 1 ->no failure,
                                 # 10..1000 ->soft failure (with load% * 10)
            while ($FailureMode != 1) # while not upload success or complete failure
            {
                $FailureMode = upload("$ZipDir/$File");

                if ($FailureMode >= 10)
                {
                    $sleepdelay = 1.25 * $sleepdelay * (1.25 * ($FailureMode/1000)); ## 1.25 * 0.8 = 1 -> try to keep the queue at 80% full, if more increase sleepdelay by 25% plus the amount the queue is too full.
                    $Reason = "queue full";
                }
                elsif ($FailureMode == 1) ## success
                {
                    $sleepdelay = 0.75 * $sleepdelay; # reduce sleepdelay by 25%
                    $Reason = "uploaded ".$File;
                    $progress++;
                    $progressPercent = $progress * 100 / $zipCount;
                }
                elsif ($FailureMode == 0) ## hard fail
                {
                    last;
                }
                $sleepdelay = int($sleepdelay) + 1; 
                if ($sleepdelay > $MaxDelay)
                {
                    $sleepdelay = $MaxDelay;
                }

                statusMessage($Reason.", sleeping for " . $sleepdelay . " seconds", $currentSubTask, $progressJobs, $progressPercent,0);
                sleep ($sleepdelay);
            }

        }
        statusMessage(scalar(@sorted)." zip files left to upload", $currentSubTask, $progressJobs, $progressPercent,0);
        
    }
}

#-----------------------------------------------------------------------------
# Upload a ZIP file
#-----------------------------------------------------------------------------
sub upload
{
    my ($File) = @_;
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

        return 0;
    }

    if($ZipSize > $Config->get("ZipHardLimit") * 1000 * 1000) 
    {
        statusMessage("zip is larger than ".$Config->get("ZipHardLimit")." MB, retrying as split tileset.", $currentSubTask, $progressJobs, $progressPercent,1);
        runCommand("unzip -qj $File -d ".$Config->get("WorkingDirectory") ,$PID);

        if($Config->get("DeleteZipFilesAfterUpload"))
        {
            unlink($File);
        }
        else
        {
            rename($File, $File."_oversized"); 
        }

        return 0;
    }
    my $SingleTileset = ($File =~ /_tileset\.zip/) ? 'yes' : 'no';
    
    my $Layer;
    if ($Config->get("UploadConfiguredLayersOnly") == 1)
    {
        foreach my $layer(split(/,/, $Config->get("Layers")))
        {
            $Layer=$Config->get($layer."_Prefix") if ($File =~ /$Config->get($layer."_Prefix")/);
            print "\n.$Layer.\n.$layer.\n" if $Config->get("Debug");
        }
    }
    else
    {
        $File=~m{.*_([^_]+)(_tileset)*\.zip}x;
        $Layer=$1;
    }
    if((! $Config->get("UploadToDirectory")) or (! -d $Config->get("UploadTargetDirectory")))
    {
        my $ua = LWP::UserAgent->new(keep_alive => 1, timeout => 360);
        
        $ua->protocols_allowed( ['http'] );
        $ua->agent("tilesAtHomeZip");
        $ua->env_proxy();
        push @{ $ua->requests_redirectable }, 'POST';
        
        my $Password = join("|", ($Config->get("UploadUsername"), $Config->get("UploadPassword")));
        my $URL = $Config->get("UploadURL");
        
        my ($UploadToken,$Load) = UploadOkOrNot();
        
        if ($UploadToken) 
        {
            statusMessage("Uploading $File", $currentSubTask, $progressJobs, $progressPercent,0);
            my $res = $ua->post($URL,
              Content_Type => 'form-data',
              Content => [ file => [$File],
              mp => $Password,
              version => $Config->get("ClientVersion"),
              single_tileset => $SingleTileset,
              token => $UploadToken,
              layer => $Layer ]);
             
            if(!$res->is_success())
            {
                print STDERR "ERROR\n";
                print STDERR "  Error uploading $File to $URL:\n";
                print STDERR "  ".$res->status_line."\n";
                return 0; # hard fail
            }
            else
            {
                print $res->content if ($Config->get("Debug"));
            }
            
        }
        else
        {
            return $Load; #soft fail
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
            return 0;
        }
        my $QueueLength = scalar(@QueueFiles);
        my $Load = $QueueLength/$MaxQueue;
        if ($Load > 0.7)
        {
            statusMessage("Not uploading, upload directory full", $currentSubTask, $progressJobs, $progressPercent,0);
            sleep(1);
            return $Load * 1000;
        }
        else
        {
            my $FileName = $File;
            $FileName =~ s|.*/||;       # Get the source filename without path
            print "\n$File $FileName\n" if $Config->get("Debug");    #Debug info
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

    return 1;
}


sub UploadOkOrNot
{
    my $LocalFilename = $Config->get("WorkingDirectory") . "/go-nogo-".$PID.".tmp";
    statusMessage("Checking server queue", $currentSubTask, $progressJobs, $progressPercent,0);
    DownloadFile($Config->get("GoNogoURL"), $LocalFilename, 1);
    open(my $fp, "<", $LocalFilename) || return;
    my $Load = <$fp>; ##read first line from file
    my $Token = <$fp>; ##read another line from file
    chomp $Load;
    chomp $Token;
    close $fp;
    killafile($LocalFilename);
    $Load=1-$Load;
    ##DEBUG print STDERR "\nLoad: $Load \n";
    # $Token=1 if (! $Token);
    if ($Load > 0.8) 
    {
        statusMessage("Not uploading, server queue full", $currentSubTask, $progressJobs, $progressPercent,0);
        sleep(1);
        return (0,$Load*1000);
    }
    else
    {
        #DEBUG: print STDERR "\n $Token\n";
        return ($Token,$Load*1000);
    }
}
