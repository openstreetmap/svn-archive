#!/usr/bin/perl
#-------------------------------------------------------------
# OpenStreetMap tiles@home
#
# Contact Deelkar or OJW on the Openstreetmap wiki for help using this program
#-----------------------------------------------------------------------------
# Copyright 2006, Oliver White, Etienne Cherdlu, Dirk-Lueder Kreie,
# Sebastian Spaeth and others
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

use warnings;
use strict;
use lib './lib';
use File::Copy;
use File::Path;
use File::Temp qw(tempfile);
use File::Spec;
use Scalar::Util qw(blessed);
use IO::Socket;
use Error qw(:try);
use tahlib;
use TahConf;
use Tileset;
use Server;
use Request;
use Upload;
use SVG::Rasterize;
use SVG::Rasterize::CoordinateBox;
use English '-no_match_vars';
use POSIX;

#---------------------------------

# Read the config file
my $Config = TahConf->getConfig();

# Handle the command-line
our $Mode = shift() || '';
my $LoopMode = (($Mode eq "loop") or ($Mode eq "upload_loop")) ? 1 : 0;
my $RenderMode = (($Mode eq "") or ($Mode eq "xy") or ($Mode eq "loop")) ? 1 : 0;
my $UploadMode = (($Mode eq "upload") or ($Mode eq "upload_loop")) ? 1 : 0;
my %EnvironmentInfo;

# Override *nix locales
delete $ENV{LC_ALL};
delete $ENV{LC_NUMERIC};
delete $ENV{LANG};
$ENV{LANG} = 'C';

if ($RenderMode)
{   # need to check that we can render and stuff
    %EnvironmentInfo = $Config->CheckConfig();
}
else
{   # for uploading we need only basic settings
    %EnvironmentInfo = $Config->CheckBasicConfig();
}

# set the progress indicator variables
our $currentSubTask;
my $progress = 0;
our $progressJobs = 1;
our $progressPercent = 0;

my $LastTimeVersionChecked = 0;   # version is only checked when last time was more than 10 min ago

UpdateClient();
reExec(-1);

#-----------------------------------------------------------------------------
# Gets latest copy of client from svn repository
# returns 1 on perceived success.
#-----------------------------------------------------------------------------
sub UpdateClient
{
    my $Config = TahConf->getConfig();
    my $Cmd = sprintf("\"%s\" %s",
        $Config->get("Subversion"),
        $Config->get("SubversionUpdateCmd"));

    if (ClientModified())
    {
        return cleanUpAndDie("Auto-update failed","EXIT",1);
    }
    
    statusMessage("Updating the Client",1,0);
    if (runCommand($Cmd,$PID)) # FIXME: evaluate output and handle locally changed files that need updating!
    {
        my $versionfile = "version.txt";
        DownloadFile($Config->get("VersionCheckURL"), $versionfile ,0);
        
        return 1;
    }
    else  # runCommand failed somehow
    {
        statusMessage("Update Failed for some reason. Check \"subversion\" is installed.",1,0);
        statusMessage("Command run was: \n".$Cmd,1,0);
    }
}

#-----------------------------------------------------------------------------
# Checks svn status for local code modifications
# returns 1 if such modifications exist
#-----------------------------------------------------------------------------
sub ClientModified
{
    my $Cmd = sprintf("\"%s\" %s",
        $Config->get("Subversion"),
        "status -q --ignore-externals");

    my $svn_status = `$Cmd`;

    chomp $svn_status;

    if ($svn_status ne '')
    {
        statusMessage("svn status did not come back clean, check your installation",1,0);
        print STDERR $svn_status;
    }
    return ($svn_status ne '');
}

sub NewClientVersion 
{
    return 1; # this client is outdated, using tags of tilesAtHome-dev now.
}


#-----------------------------------------------------------------------------
# A function to re-execute the program.  
#
# This function restarts the program unconditionally.
#-----------------------------------------------------------------------------
sub reExec
{
    my $child_pid = shift();## FIXME: make more general
    my $Config = TahConf->getConfig();
    # until proven to work with other systems, only attempt a re-exec
    # on linux. 
    return unless ($^O eq "linux" || $^O eq "cygwin" ||  $^O eq "darwin");

    statusMessage("tilesGen.pl has changed, re-start new version",1,0);
    if ($Config->get("ForkForUpload") && $child_pid != -1)  ## FIXME: make more general
    {
        statusMessage("Waiting for child process (this can take a while)",1,0);
        waitpid($child_pid, 0);
    }
    exec "perl", $0, $Mode, "reexec", 
        "progressJobs=" . $progressJobs, 
        "idleSeconds=" . getIdle(1), 
        "idleFor=" . getIdle(0) or die("could not reExec");
}

