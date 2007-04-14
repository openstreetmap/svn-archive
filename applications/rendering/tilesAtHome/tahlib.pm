use strict; 
use tahconfig;

# =====================================================================
# The following is duplicated from tilesGen.pl
# =====================================================================

#-----------------------------------------------------------------------------
# Prints status message without newline, overwrites previous message
# (if $newline set, starts new line after message)
#-----------------------------------------------------------------------------
sub statusMessage 
{
    my ($msg, $newline) = @_;

    if ($Config{Verbose})
    {
        print STDERR "$msg\n";
        return;
    }

    my $toprint = sprintf("[#%d %3d%% %s] %s%s ", $progressJobs, $progressPercent+.5, $currentSubTask, $msg, ($newline) ? "" : "...");
    print STDERR "\r";
    print STDERR " " x $lastmsglen;
    print STDERR "\r$toprint";
    if ($newline)
    {
        $lastmsglen = 0;
        print STDERR "\n";
    }
    else
    {
        $lastmsglen = length($toprint);
    }
}

#-----------------------------------------------------------------------------
# Used to display task completion. Only for verbose mode.
#-----------------------------------------------------------------------------
sub doneMessage
{
    my $msg = shift;
    $msg = "done" if ($msg eq "");

    if ($Config{Verbose})
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
    my ($message, $duration) = @_;
    if ($Config{Verbose})
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

