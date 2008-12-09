use strict;
use TahConf;

# =====================================================================
# The following is duplicated from tilesGen.pl
# =====================================================================
my $lastmsglen = 0;

my $idleFor = 0;
my $idleSeconds = 0;

my %faults; #variable to track non transient errors


#-----------------------------------------------------------------------------
# Prints status message without newline, overwrites previous message
# (if $newline set, starts new line after message)
# only prints something if $VerbosityTriggerLevel is >= Verbosity
#-----------------------------------------------------------------------------
sub statusMessage 
{
    my $Config = TahConf->getConfig();
    my $currentSubTask = $main::currentSubTask;
    my $progressJobs = $main::progressJobs;
    my $progressPercent = $main::progressPercent;

    my ($msg, $newline, $VerbosityTriggerLevel) = @_;
    
    my $toprint = sprintf("[#%d %3d%% %s] %s%s ", $progressJobs, $progressPercent+.5, $currentSubTask, $msg, ($newline) ? "" : "...");

    if ($Config->get("Verbose") >= 10)
    {
        print STDERR "$toprint\n";
        return;
    }

    return if ($Config->get("Verbose") < $VerbosityTriggerLevel); # don't print anything if we set verbosity below triggerlevel

    my $curmsglen = length($toprint);
    print STDERR "\r$toprint";
    print STDERR " " x ($lastmsglen-$curmsglen);
    if ($newline)
    {
        $lastmsglen = 0;
        print STDERR "\n";
    }
    else
    {
        $lastmsglen = $curmsglen;
    }

}

#-----------------------------------------------------------------------------
# Used to display task completion. Only for verbose mode.
#-----------------------------------------------------------------------------
sub doneMessage
{
    my $Config = TahConf->getConfig();
    my $msg = shift;
    
    $msg = "done" if ($msg eq "");
    
    if ($Config->get("Verbose") >= 10)
    {
        print STDERR "$msg\n";
        return;
    }
}

#-----------------------------------------------------------------------------
# A sleep function with visible countdown
#-----------------------------------------------------------------------------
sub talkInSleep
{
    my $Config = TahConf->getConfig();
    my ($message, $duration) = @_;
    
    if ($Config->get("Verbose") >= 10)
    {
        print STDERR "$message: sleeping $duration seconds\n";
        sleep $duration;
        return;
    }

    for (my $i = 0; $i< $duration; $i++)
    {
         statusMessage(sprintf("%s. Idle for %d (Total %d:%02d)", 
                $message,
                $duration - $i,
                $idleFor/60, $idleFor%60,
                ),0,3);
        sleep 1;
        $idleFor++;
    }
}

sub setIdle
{
    my ($idle,$setTotal) = @_;
    if ($setTotal)
    {
        $idleSeconds = $idle;
    }
    else
    {
        $idleFor = $idle;
    }
}

sub getIdle
{
    my $getTotal = @_;
    if ($getTotal)
    {
      return $idleSeconds;
    }
    else
    {
      return $idleFor;
    }
}


#-----------------------------------------------------------------------------
# fault handling
#-----------------------------------------------------------------------------
sub addFault
{
    my ($faulttype,$diff) = @_;
    $diff = 1 if (not $diff);
    $faults{$faulttype} += $diff;
    return $faults{$faulttype};
}

sub getFault
{
    my ($faulttype) = @_;
    return $faults{$faulttype};
}

sub resetFault
{
    my ($faulttype) = @_;
    $faults{$faulttype} = 0;
    return "0 but true";
}

#-----------------------------------------------------------------------------
# Run a shell command. Suppress command's stderr output unless it terminates
# with an error code.
#
# Return 1 if ok, 0 on error.
#-----------------------------------------------------------------------------
sub runCommand
{
    my $Config = TahConf->getConfig();
    my ($cmd,$mainPID) = @_;

    if ($Config->get("Verbose") >= 10)
    {
        my $retval = system($cmd);
        return $retval == 0;
    }

    my $ErrorFile = $Config->get("WorkingDirectory")."/".$mainPID.".stderr";
    # force inkscape and others into non-GUI mode, does not work for older version of inkscape
    # local %ENV;
    # delete $ENV{DISPLAY};
    my $retval = system("$cmd 2> $ErrorFile");
    my $ok = 0;
    my $ExtraInfo = "\nAdditional info about the Error(s):\n";

    # <0 means that the process could not start
    if ($retval < 0)
    {
        print STDERR "ERROR:\n";
        print STDERR "  Could not run the following command:\n";
        print STDERR "  $cmd\n";
        print STDERR "  Please check your installation.\n";
    } 
    else
    {
        # Technically the return value is ($retval >> 8) but if we only look
        # at that we will miss the situations where the program died due to
        # a signal. In that case $retval will be the signal that killed it.
        # So any non-zero value is an error.

        if ($retval)
        {
            print STDERR "ERROR\n";
            print STDERR "  The following command produced an error message:\n";
            print STDERR "  $cmd\n";
            print STDERR "  Debug output follows:\n";
            open(ERR, $ErrorFile);
            while(<ERR>)
            {
                print STDERR "  | $_";
                if (grep(/infinite template recursion/,$_))
                {
                    $ExtraInfo=$ExtraInfo."\n * Tile too complex for Xmlstarlet, possibly an excessively long way, or too many maplint errors";
                }
            }
            close(ERR);
            print STDERR $ExtraInfo."\n\n";
        }
        else
        {
            $ok = 1;
        }
    }

    unlink($ErrorFile);
    return $ok;
}

#-----------------------------------------------------------------------------
# GET a URL and save contents to file
#-----------------------------------------------------------------------------
sub DownloadFile 
{
    my $Config = TahConf->getConfig();
    my ($URL, $File, $UseExisting) = @_;

    my $ua = LWP::UserAgent->new(keep_alive => 1, timeout => $Config->get("DownloadTimeout"));
    $ua->agent("tilesAtHome");
    $ua->env_proxy();

    if(!$UseExisting) 
    {
        unlink($File);
    }
    # Note: mirror sets the time on the file to match the server time. This
    # is important for the handling of JobTime later.
    my $res = $ua->mirror($URL, $File);

    if ($res->is_success()) 
    {
        doneMessage(sprintf("downloaded %d bytes", -s $File));
        return 1;
    }
    else
    {
        unlink($File) if (! $UseExisting);
        doneMessage("failed with: ".$res->status_line);
        return 0;
    }
}

#-----------------------------------------------------------------------------
# Merge multiple OSM files into one, making sure that elements are present in
# the destination file only once even if present in more than one of the input
# files.
# 
# This has become necessary in the course of supporting maplint, which would
# get upset about duplicate objects created by combining downloaded stripes.
#-----------------------------------------------------------------------------
sub mergeOsmFiles
{
    my $Config = TahConf->getConfig();
    my ($destFile, $sourceFiles) = @_;
    my $existing = {};

    # If there's only one file, just copy the input to the output
    if( scalar(@$sourceFiles) == 1 )
    {
      copy $sourceFiles->[0], $destFile;
      unlink($sourceFiles->[0]) if (!$Config->get("Debug"));
      return (1, "");
    }
    
    open (DEST, "> $destFile");

    print DEST qq(<?xml version="1.0" encoding="UTF-8"?>\n);
    my $headerwritten = 0;
    my $reason = "";

    foreach my $sourceFile(@{$sourceFiles})
    {
        my $headerseen = 0;
        my $footerseen = 0;

        open(SOURCE, $sourceFile);
        while(<SOURCE>)
        {
            next if /^\s*<\?xml/;
            # We want to copy the version number, but only the first time (obviously)
            # Handle where the input doesn't have a version
            if (/^\s*<osm.*(?:version=([\d.'"]+))?/)
            {
              $headerseen = 1;
              if( not $headerwritten )
              {
                my $version = $1 || "'".$Config->get("OSMVersion")."'";
                print DEST qq(<osm version=$version generator="tahlib.pm mergeOsmFiles" xmlns:osmxapi="http://www.informationfreeway.org/osmxapi/0.5">\n);
                $headerwritten = 1;
              }
              next;
            }
            if (/^\s*<\/osm>/)
            {
                $footerseen = 1;
                last;
            }
            if (/^\s*<(node|segment|way|relation) id=['"](\d+)['"].*(.)>/)
            {
                my ($what, $id, $slash) = ($1, $2, $3);
                my $key = substr($what, 0, 1) . $id;
                if (defined($existing->{$key}))
                {
                    # object exists already. skip!
                    next if ($slash eq "/");
                    while(<SOURCE>)
                    {
                        last if (/^\s*<\/$what>/);
                    }
                    next;
                }
                else
                {
                    # object didn't exist, note
                    $existing->{$key} = 1;
                }
            }
            print DEST;
        }
        close(SOURCE);
        unlink ($sourceFile) if (!$Config->get("Debug"));
        if (($headerseen == 0) || ($footerseen == 0))
        {
            $reason = $reason . $sourceFile . " not well formed. ";
        }
    }
    print DEST "</osm>\n";
    close(DEST);
    if ($reason != "")
    {
        return (0, $reason);
    } else {
        return (1, "");
    }
}


#-----------------------------------------------------------------------------
# write log about t@h progress
#-----------------------------------------------------------------------------

sub keepLog
{
    my $Config = TahConf->getConfig();
    if ($Config->get("ProcessLog")) {
        my ($Pid,$Process,$Action,$Message) = @_;
        my $logFile = $Config->get("ProcessLogFile");
        my $log = $Config->get("ProcessLog");
        my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime();
        $year += 1900;
        
        open(my $fpLog, ">>$logFile");
        if ($fpLog) {
            print $fpLog sprintf("%04d-%02d-%02d %02d:%02d:%02d [%s] %s %s %s %s\n", $year, $mon+1, $mday, $hour, $min, $sec, $Config->get("ClientVersion"), $Pid, $Process, $Action, $Message);
            close $fpLog;
        }
    }
}

#-----------------------------------------------------------------------------
# Clean up temporary files before exit, then exit or return with error 
# depending on mode (loop, xy, ...)
#-----------------------------------------------------------------------------
sub cleanUpAndDie
{
    my $Config = TahConf->getConfig();
    my ($Reason,$Mode,$Severity) = @_;

    statusMessage ($Reason, 1,0);

    return 0 if ($Mode eq "loop");

    if ($main::StartedBatikAgent)
    {
        my $result = $SVG::Rasterize::object->engine()->stop_agent();
        if( $result == 1 ){
            statusMessage("Successfully sent stop message to Batik agent", 1, 0);
        } elsif( $result == 0 ){
            statusMessage("Could not contact Batik agent", 1, 0);
        } else {
            statusMessage($result, 1, 0);
        }
    }
    exit($Severity);
}


#-------------------------------------------------------------
# Get client ID from file or create one if file doesn't exist.
#-------------------------------------------------------------
sub GetClientId
{
    my $Config = TahConf->getConfig();
    my $clientId = $Config->get("ClientID");
    if (!$clientId)
    {
        my $idfile = $Config->get("WorkingDirectory") . "/client-id.txt";
        if (open(idfile, "<", $idfile))
        {
            $clientId = <idfile>;
            chomp $clientId;
            close idfile;
        }
        elsif (open(idfile, ">", $idfile))
        {
            $clientId = int(rand(65535)); 
            print idfile $clientId;
            close idfile;
        }
        else
        {
            die("can't open $idfile");
        }
    }
    return $clientId;
}

#-------------------------------------------------------------
# Check wether directory is empty and return true if so.
#-------------------------------------------------------------
sub dirEmpty
{
    my ($path) = @_;
    opendir DIR, $path;
    while(my $entry = readdir DIR) 
    {
        next if($entry =~ /^\.\.?$/);
        closedir DIR;
        return 0; # if $entry not "." or ".."
    }
    closedir DIR;
    return 1; 
}

#--------------------------------------------------------------------------------------
# check for utf-8 faults in file and return false if UTF-8 clean, otherwise return the 
# number of the first line where an utf-8 error occured
#--------------------------------------------------------------------------------------
sub fileUTF8ErrCheck
{
    my $DataFile = shift();
    open(OSMDATA, $DataFile) || die ("could not open $DataFile for UTF-8 check");
    my @toCheck = <OSMDATA>;
    close(OSMDATA);
    my $line=0;
    while (my $osmline = shift @toCheck)
    {
        $line++;
        eval { decode("UTF-8",$osmline, Encode::FB_CROAK) };
        if ($@)
        {
            return $line; # returns the line the error occured on
        }
    }
    return 0;
}


1;

