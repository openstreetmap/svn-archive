use strict; 

# =====================================================================
# The following is duplicated from tilesGen.pl
# =====================================================================

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
    my ($message, $duration,$progstart,$idleFor,$idleSeconds,$Verbose) = @_;
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
    my ($idleNow,$idleTotal) = @_;
    $idleFor = $idleNow;
    $idleSeconds = $idleTotal;
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

1;

