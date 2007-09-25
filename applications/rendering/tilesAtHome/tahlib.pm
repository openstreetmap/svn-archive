use strict; 

# =====================================================================
# The following is duplicated from tilesGen.pl
# =====================================================================
my %Config = ReadConfig("tilesAtHome.conf", "general.conf", "authentication.conf", "layers.conf");
my $lastmsglen = 0;

my $idleFor = 0;
my $idleSeconds = 0;

#-----------------------------------------------------------------------------
# Prints status message without newline, overwrites previous message
# (if $newline set, starts new line after message)
#-----------------------------------------------------------------------------
sub statusMessage 
{
    my ($msg, $Verbose, $currentSubTask, $progressJobs, $progressPercent, $newline) = @_;

    if ($Verbose)
    {
        print STDERR "$msg\n";
        return;
    }

    my $toprint = sprintf("[#%d %3d%% %s] %s%s ", $progressJobs, $progressPercent+.5, $currentSubTask, $msg, ($newline) ? "" : "...");
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
# Run a shell command. Suppress command's stderr output unless it terminates
# with an error code.
#
# Return 1 if ok, 0 on error.
#-----------------------------------------------------------------------------
sub runCommand
{
    my ($cmd,$mainPID) = @_;

    # $message is deprecated, issue statusmessage prior to exec.
    # statusMessage($message, $Config{Verbose}, $currentSubTask, $progressJobs, $progressPercent,0);


    if ($Config{Verbose})
    {
        my $retval = system($cmd);
        return ($retval<0) ? 0 : ($retval>>8) ? 0 : 1;
    }

    my $ErrorFile = $Config{WorkingDirectory}."/".$mainPID.".stderr";
    my $retval = system("$cmd 2> $ErrorFile");
    my $ok = 0;

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
        $retval = $retval >> 8;
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
            }
            close(ERR);
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
# GET a URL and save contents to file
#-----------------------------------------------------------------------------
sub DownloadFile 
{
    my ($URL, $File, $UseExisting) = @_;

    my $ua = LWP::UserAgent->new(keep_alive => 1, timeout => 1800);
    $ua->agent("tilesAtHome");
    $ua->env_proxy();

    if(!$UseExisting) 
    {
        killafile($File);
    }
    # Note: mirror sets the time on the file to match the server time. This
    # is important for the handling of JobTime later.
		 $ua->mirror($URL, $File);

    doneMessage(sprintf("done, %d bytes", -s $File));
}

#-----------------------------------------------------------------------------
# Clean up temporary files before exit, then exit or return with error 
# depending on mode (loop, xy, ...)
#-----------------------------------------------------------------------------
sub cleanUpAndDie
{
    my ($Reason,$Mode,$mainPID) = @_;

    ## TODO: clean up *.tempdir too

    if (! $Config{"Debug"}) 
    {
        opendir (TEMPDIR, $Config{"WorkingDirectory"});
        my @files = grep { /$mainPID/ } readdir(TEMPDIR); # FIXME: this will get files from other processes using the same tempdir for low pids
        closedir (TEMPDIR);
        while (my $file = shift @files)
        {
             print STDERR "deleting ".$Config{"WorkingDirectory"}."/".$file."\n" if ($Config{"Verbose"});
             killafile($Config{"WorkingDirectory"}."/".$file);
        }
        
    }
    
    return 0 if ($Mode eq "loop");
    exit(1);
}

1;

