#!/usr/bin/perl
use strict;
use FindBin qw($Bin);
use LWP::UserAgent;
use File::Copy;
use English '-no_match_vars';
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
my $progressJobs = $ARGV[0] or 1;
my $currentSubTask;
 
my $lastmsglen;

### TODO: implement locking, this is one of the things that make upload not multithread-safe.
my $sleepdelay;
my $failFile = $Config{WorkingDirectory} . "/failurecount.txt";
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

compress(1); ## first run

# Upload any ZIP files which are still waiting to go
processOldZips(1);

# We might have created lots of single tiles if some tileset zips were larger than 10 MB, so re-check here
compress(2); ## second (and last) run.

# Do we have new zips? (try to) upload them all!
processOldZips(2);


## update the failFile with current failure count from processOldZips

if (open(FAILFILE, ">", $failFile))
{
    print FAILFILE $sleepdelay;
    close FAILFILE;
}

## end main

sub processOldZips
{
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
    statusMessage(scalar(@sorted)." zip files to upload", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
    my $Reason = "queue full";
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
                }
                elsif ($FailureMode == 0) ## hard fail
                {
                    last;
                }
                $sleepdelay = int($sleepdelay) + 1; 
                if ($sleepdelay > 600)  ## needs adjusting based on real-world experience, if this check is true the above load adapting failed and the server is too overloaded to reasonably process the queue relative to the rendering speed
                {
                    $sleepdelay = 600; ## FIXME: since the checking of the queue is much less costly than trying to upload, need to further adapt the max delay.
                }

                statusMessage($Reason.", sleeping for " . $sleepdelay . " seconds", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
                sleep ($sleepdelay);
            }

        }
        $progress++;
        $progressPercent = $progress * 100 / $zipCount;
        statusMessage(scalar(@sorted)." zip files left to upload", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
        
    }
}

#-----------------------------------------------------------------------------
# Upload a ZIP file
#-----------------------------------------------------------------------------
sub upload
{
    my ($File) = @_;
    my $ZipSize += -s $File;
    if($ZipSize > $Config{ZipHardLimit} * 1000 * 1000) 
    {
        statusMessage("zip is larger than ".$Config{ZipHardLimit}." MB, retrying as split tileset.", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,1);
        runCommand("unzip -qj $File -d $Config{WorkingDirectory}",$PID);

        if($Config{DeleteZipFilesAfterUpload})
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
    foreach my $layer(split(/,/, $Config{Layers}))
    {
        $Layer=$Config{"Layer.$layer.Prefix"} if ($File =~ /$Config{"Layer.$layer.Prefix"}/);
        ## DEBUG print "\n.$Layer.\n.$layer.\n";
    }
    
    my $ua = LWP::UserAgent->new(keep_alive => 1, timeout => 360);

    $ua->protocols_allowed( ['http'] );
    $ua->agent("tilesAtHomeZip");
    $ua->env_proxy();
    
    my $Password = join("|", ($Config{UploadUsername}, $Config{UploadPassword}));
    my $URL = $Config{"UploadURL2"};
    
    my ($UploadToken,$Load) = UploadOkOrNot();
    
    if ($UploadToken) 
    {
        statusMessage("Uploading $File", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
        my $res = $ua->post($URL,
          Content_Type => 'form-data',
          Content => [ file => [$File],
          mp => $Password,
          version => $Config{ClientVersion},
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
    
        if($Config{DeleteZipFilesAfterUpload})
        {
            unlink($File);
        }
        else
        {
            rename($File, $File."_uploaded");
        }
    }
    else
    {
        return $Load; #soft fail
    }
    
    return 1;
}

sub compress
{
    ## Run compress directly because it uses same messaging as tilesGen.pl and upload.pl
    ## no need to hide output at all.

    my ($runNumber) = @_;

    my $UploadScript = "perl $Bin/compress.pl $runNumber $progressJobs";
    my $retval = system($UploadScript);
    return $retval;
}


sub UploadOkOrNot
{
    my $LocalFilename = $Config{WorkingDirectory} . "/go-nogo-".$PID.".tmp";
    statusMessage("Checking server queue", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
    DownloadFile($Config{GoNogoURL}, $LocalFilename, 1);
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
        statusMessage("Not uploading, server queue full", $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);
        sleep(1);
        return (0,$Load*1000);
    }
    else
    {
        #DEBUG: print STDERR "\n $Token\n";
        return ($Token,$Load*1000);
    }
}
