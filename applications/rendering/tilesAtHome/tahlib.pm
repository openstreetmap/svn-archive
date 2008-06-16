use strict;

# =====================================================================
# The following is duplicated from tilesGen.pl
# =====================================================================
#my %Config = ReadConfig("tilesAtHome.conf", "general.conf", "authentication.conf", "layers.conf");
my $lastmsglen = 0;

my $idleFor = 0;
my $idleSeconds = 0;

my %faults; #variable to track non transient errors

# hash for MagicMkdir
my %madeDir;

#-----------------------------------------------------------------------------
# Prints status message without newline, overwrites previous message
# (if $newline set, starts new line after message)
#-----------------------------------------------------------------------------
sub statusMessage 
{
    my $Config = $main::Config;

    my ($msg, $currentSubTask, $progressJobs, $progressPercent, $newline) = @_;
    
    my $toprint = sprintf("[#%d %3d%% %s] %s%s ", $progressJobs, $progressPercent+.5, $currentSubTask, $msg, ($newline) ? "" : "...");

    if ($Config->get("Verbose"))
    {
        print STDERR "$toprint\n";
        return;
    }

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
    my ($msg,$Verbose) = @_;
    $msg = "done" if ($msg eq "");

    if ($Verbose)
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
    my ($message, $duration,$progstart,$Verbose) = @_;
    if ($Verbose)
    {
        print STDERR "$message: sleeping $duration seconds\n";
        sleep $duration;
        return;
    }

    for (my $i = 0; $i< $duration; $i++)
    {
        my $totalseconds = time() - $progstart;
        statusMessage(sprintf("%s. Idle for %d:%02d (%d%% idle) ", 
                $message,
                $idleFor/60, $idleFor%60,
                $totalseconds ? $idleSeconds * 100 / $totalseconds : 100));
        sleep 1;
        $idleFor++;
        $idleSeconds++;
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
    my $Config = $main::Config;
    my ($cmd,$mainPID) = @_;

    # $message is deprecated, issue statusmessage prior to exec.
    # statusMessage($message, $Config->get("Verbose"), $currentSubTask, $progressJobs, $progressPercent,0);


    if ($Config->get("Verbose"))
    {
        my $retval = system($cmd);
        return $retval == 0;
    }

    my $ErrorFile = $Config->get("WorkingDirectory")."/".$mainPID.".stderr";
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
                if (grep(/preferences.xml/,$_))
                {
                    $ExtraInfo=$ExtraInfo."\n * Inkscape preference file corrupt. Delete ~/.inkscape/preferences.xml to continue";
                    if ($Config->get("AutoResetInkscapePrefs") == 1)
                    {
                        $ExtraInfo=$ExtraInfo."\n   AutoResetInkscapePrefs set, trying to reset ~/.inkscape/preferences.xml";
                        unlink (glob("~/.inkscape/preferences.xml")) or addFault("fatal",1);
                    }
                    else
                    {
                        addFault("fatal",1); ## this error is fatal because it needs human intervention before processing can continue
                    }
                }
                elsif (grep(/infinite template recursion/,$_))
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
    
    killafile($ErrorFile);
    return $ok;
}

#-----------------------------------------------------------------------------
# Delete a file if it exists
#-----------------------------------------------------------------------------
sub killafile($){
  my $file = shift();
  unlink $file if(-f $file);
}

#-----------------------------------------------------------------------------
# Create a directory and all its parent directories
# (equivalent to a "mkdir -p" on Unix, but stores already-created dirs
# in a hash to avoid unnecessary system calls)
#-----------------------------------------------------------------------------
sub MagicMkdir
{
    my $file = shift;
    my @paths = split("/", $file);
    pop(@paths);
    my $dir = (substr($file,0,1) eq "/") ? "/" : "";
    foreach my $path(@paths)
    {
        if ($dir eq "")
        {
            $dir .= $path; # how are paths with leading "/" handled now?
        }
        else
        {
            $dir .= "/".$path;
        }

        if (!defined($madeDir{$dir}))
        {
            mkdir $dir;
            $madeDir{$dir}=1;
        }
    }
}

#-----------------------------------------------------------------------------
# GET a URL and save contents to file
#-----------------------------------------------------------------------------
sub DownloadFile 
{
    my $Config = $main::Config;
    my ($URL, $File, $UseExisting) = @_;

    my $ua = LWP::UserAgent->new(keep_alive => 1, timeout => $Config->get("DownloadTimeout"));
    $ua->agent("tilesAtHome");
    $ua->env_proxy();

    if(!$UseExisting) 
    {
        killafile($File);
    }
    # Note: mirror sets the time on the file to match the server time. This
    # is important for the handling of JobTime later.
    my $res = $ua->mirror($URL, $File);

    if ($res->is_success()) 
    {
        doneMessage(sprintf("done, %d bytes", -s $File));
        return 1;
    }
    else
    {
        killafile($File) if (! $UseExisting);
        doneMessage("failed");
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
    my $Config = $main::Config;
    my ($destFile, $sourceFiles) = @_;
    my $existing = {};

    # If there's only one file, just copy the input to the output
    if( scalar(@$sourceFiles) == 1 )
    {
      copy $sourceFiles->[0], $destFile;
      killafile ($sourceFiles->[0]) if (!$Config->get("Debug"));
      return;
    }
    
    open (DEST, "> $destFile");

    print DEST qq(<?xml version="1.0" encoding="UTF-8"?>\n);
    my $header = 0;

    foreach my $sourceFile(@{$sourceFiles})
    {
        open(SOURCE, $sourceFile);
        while(<SOURCE>)
        {
            next if /^\s*<\?xml/;
            # We want to copy the version number, but only the first time (obviously)
            # Handle where the input doesn't have a version
            if (/^\s*<osm.*(?:version=([\d.'"]+))?/)
            {
              if( not $header )
              {
                my $version = $1 || "'".$Config->get("OSMVersion")."'";
                print DEST qq(<osm version=$version generator="tahlib.pm mergeOsmFiles" xmlns:osmxapi="http://www.informationfreeway.org/osmxapi/0.5">\n);
                $header = 1;
              }
              next;
            }
            last if (/^\s*<\/osm>/);
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
        killafile ($sourceFile) if (!$Config->get("Debug"));
    }
    print DEST "</osm>\n";
    close(DEST);
}


#-----------------------------------------------------------------------------
# write log about t@h progress
#-----------------------------------------------------------------------------

sub keepLog
{
    my ($Pid,$Process,$Action,$Message) = @_;
    my $Config = $main::Config;
    my $logFile = $Config->get("ProcessLogFile");

    open(my $fpLog, ">>$logFile");
    if ($fpLog) {
	print $fpLog time()." [".$Config->get("ClientVersion")."] $Pid $Process $Action $Message\n";
	close $fpLog;
    }
}

#-----------------------------------------------------------------------------
# Clean up temporary files before exit, then exit or return with error 
# depending on mode (loop, xy, ...)
#-----------------------------------------------------------------------------
sub cleanUpAndDie
{
    my $Config = $main::Config;
    my ($Reason,$Mode,$Severity,$mainPID) = @_;

    ## TODO: clean up *.tempdir too

    print STDERR "\n$Reason\n" if ($Config->get("Verbose"));

    if (! $Config->get("Debug")) 
    {
        opendir (TEMPDIR, $Config->get("WorkingDirectory"));
        my @files = grep { /$mainPID/ } readdir(TEMPDIR); # FIXME: this will get files from other processes using the same Working Directory for low pids because the numbers will collide with tile coordinates
        closedir (TEMPDIR);
        while (my $file = shift @files)
        {
             print STDERR "deleting ".$Config->get("WorkingDirectory")."/".$file."\n" if ($Config->get("Verbose"));
             killafile($Config->get("WorkingDirectory")."/".$file);
        }
        
    }
    
    return 0 if ($Mode eq "loop");
    print STDERR "\n$Reason\n" if (! $Config->get("Verbose")); #print error only once, and only if fatal.
    exit($Severity);
}

1;

