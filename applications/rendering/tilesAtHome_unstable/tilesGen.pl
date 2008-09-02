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
use File::Copy;
use File::Path;
use File::Temp qw(tempfile);
use File::Spec;
use Scalar::Util qw(blessed);
use IO::Socket;
use tahlib;
use lib::TahConf;
use lib::TahExceptions;
use lib::Tileset;
use Request;
use Upload;
use Compress;
use English '-no_match_vars';
use GD qw(:DEFAULT :cmp);
use POSIX qw(locale_h);
use Encode;

#---------------------------------

# Read the config file
my $Config = TahConf->getConfig();

# Handle the command-line
our $Mode = shift() || '';
my $LoopMode = (($Mode eq "loop") or ($Mode eq "upload_loop")) ? 1 : 0;
my $RenderMode = (($Mode eq "") or ($Mode eq "xy") or ($Mode eq "loop")) ? 1 : 0;
my $UploadMode = (($Mode eq "upload") or ($Mode eq "upload_loop")) ? 1 : 0;
my %EnvironmentInfo;

if ($RenderMode)
{   # need to check that we can render and stuff
    %EnvironmentInfo = $Config->CheckConfig();
}
else
{   # for uploading we need only basic settings
    %EnvironmentInfo = $Config->CheckBasicConfig();
}

# set the progress indicatOr variables
our $currentSubTask;
my $progress = 0;
our $progressJobs = 0;
our $progressPercent = 0;

my $LastTimeVersionChecked = 0;   # version is only checked when last time was more than 10 min ago
if ($UploadMode or $RenderMode) {
    if (NewClientVersion()) {
        UpdateClient();
        if ($LoopMode) {
            reExec(-1);
        } else {
            statusMessage("tilesGen.pl has changed. Please restart new version.",1,0);
            exit;
        }
    }
}

# Get version number from version-control system, as integer
my $Version = '$Revision$';
$Version =~ s/\$Revision:\s*(\d+)\s*\$/$1/;
printf STDERR "This is version %d (%s) of tilesgen running on %s, ID: %s\n", 
    $Version, $Config->get("ClientVersion"), $^O, GetClientId();

# filestat: Used by reExecIfRequired.
# This gets set to filesize/mtime/ctime of this script, and reExecIfRequired
# checks to see if those parameters have changed since the last time it ran
# to decide if it should reExec
my $filestat;
reExecIfRequired(-1); # This will not actually reExec, only set $filestat

if ($LoopMode) {
    # if this is a re-exec, we want to capture some of our status
    # information from the command line. this feature allows setting
    # any numeric variable by specifying "variablename=value" on the
    # command line after the keyword "reexec". Currently unsuitable 
    # for alphanumeric variables.
    
    if ((shift||'') eq "reexec") {
        my $idleSeconds; my $idleFor;
        while(my $evalstr = shift()) {
            die("$evalstr does not match option=value") unless $evalstr =~ /^[A-Za-z]+=\d+/;
            eval('$'.$evalstr);
            print STDERR "$evalstr\n" if ($Config->get("Verbose") >= 10);
        }
        setIdle($idleSeconds, 1);
        setIdle($idleFor, 0);
    }
}

# global test images, used for comparing to render results
my ($EmptyLandImage, $EmptySeaImage, $BlackTileImage);

if ($RenderMode) {
    # check GD
    eval GD::Image->trueColor(1);
    if ($@ ne '') {
        print STDERR "please update your libgd to version 2 for TrueColor support";
        cleanUpAndDie("init:libGD check failed, exiting","EXIT",4);
    }

    my ($MapLandBackground, $MapSeaBackground, $BlackTileBackground);

    # create a comparison blank image
    $EmptyLandImage = new GD::Image(256,256);
    $MapLandBackground = $EmptyLandImage->colorAllocate(248,248,248);
    $EmptyLandImage->fill(127,127,$MapLandBackground);

    $EmptySeaImage = new GD::Image(256,256);
    $MapSeaBackground = $EmptySeaImage->colorAllocate(181,214,241);
    $EmptySeaImage->fill(127,127,$MapSeaBackground);

    # Some broken versions of Inkscape occasionally produce totally black
    # output. We detect this case and throw an error when that happens.
    $BlackTileImage = new GD::Image(256,256);
    $BlackTileBackground = $BlackTileImage->colorAllocate(0,0,0);
    $BlackTileImage->fill(127,127,$BlackTileBackground);
}

# We need to keep parent PID so that child get the correct files after fork()
my $parent_pid = $PID;
my $upload_pid = -1;

# keep track of the server time for current job
my $JobTime;

# If batik agent was started automatically, turn it off at exit
our $StartedBatikAgent = 0;

# Check the stylesheets for corruption and out of dateness, but only in loop mode
# The existance check is to attempt to determine we're on a UNIX-like system

if( $RenderMode and -e "/dev/null" )
{
    my $svn = $Config->get("Subversion");
    if( qx($svn status osmarender/*.x[ms]l 2>/dev/null) ne "" )
    {
        print STDERR "Custom changes in osmarender stylesheets. Examine the following output to fix:\n";
        system($Config->get("Subversion")." status osmarender/*.x[ms]l");
        cleanUpAndDie("init.osmarender_stylesheet_check repair failed","EXIT",4);
    }
}

## set all fault counters to 0;
resetFault("fatal");
resetFault("inkscape");
resetFault("nodata");
resetFault("nodataTAPI");
resetFault("nodataXAPI");
resetFault("renderer");
resetFault("utf8");
resetFault("upload");

unlink("stopfile.txt") if $Config->get("AutoResetStopfile");

#---------------------------------
## Start processing

if ($Mode eq "xy")
{
    # ----------------------------------
    # "xy" as first argument means you want to specify a tileset to render
    # ----------------------------------

    my $X = shift();
    my $Y = shift();
    my $req = new Request;
    if (not defined $X or not defined $Y)
    { 
        print STDERR "Usage: $0 xy <X> <Y> [<ZOOM>]\n";
        print STDERR "where <X> and <Y> are the tile coordinates and \n";
        print STDERR "<ZOOM> is an optional zoom level (defaults to 12).\n";
        exit;
    }
    my $Zoom = shift();
    if (not defined $Zoom)
    {
       $Zoom = 12;
       $currentSubTask = "warning";
       statusMessage(" *** No zoomlevel specified! Assuming z12 *** ",1,0);
    }

    $req->ZXY($Zoom, $X, $Y);
    $req->layers($Config->get("Layers"));

    my $tileset = Tileset->new($req);
    $tileset->generate();
}
#---------------------------------
elsif ($Mode eq "loop") 
{
    # ----------------------------------
    # Continuously process requests from server
    # ----------------------------------

    # Start batik agent if it's not runnig
    if ($Config->get("Batik") == "3" && !getBatikStatus())
    {
        startBatikAgent();
        $StartedBatikAgent = 1;
    }

    # this is the actual processing loop
    
    while(1) 
    {
        ## before we start (another) round of rendering we first check if something bad happened in the past.
        checkFaults();

        ## note: Timeouts are cumulative so if there are X timeouts from api and Y timeouts from XAPI then we wait for each timeout, one after the other
        checkDataFaults();

        # look for stopfile and exit if found
        if (-e "stopfile.txt")
        {
            if ($Config->get("ForkForUpload") && $upload_pid != -1)
            {
                statusMessage("Waiting for previous upload process (this can take while)",1,0);
                waitpid($upload_pid, 0);
            }
            cleanUpAndDie("Stopfile found, exiting","EXIT",7); ## TODO: agree on an exit code scheme for different types of errors
        }

        # Add a basic auto-updating mechanism. 
        if (NewClientVersion()) 
        {
            UpdateClient();
            reExec($upload_pid);
        }

        reExecIfRequired($upload_pid); ## check for new version of tilesGen.pl and reExec if true

        ### start processing here:
        # Render stuff if we get a job from server
        my ($did_something, $message) = ProcessRequestsFromServer();
        # compress and upload results
        my $upload_result = compressAndUploadTilesets();

        if ($upload_result == -1)
        {     # we got an error in the upload process
              addFault("upload",1);
        }
        else
        {     #reset fault counter if we uploaded successfully
              resetFault("upload");
        }

        if ($did_something == 1) 
        {   # Rendered tileset, don't idle in next round
            setIdle(0,0);
        }
    }
}
#---------------------------------
elsif ($Mode eq "upload") 
{   # Upload mode
    compressAndUpload();
}
#---------------------------------
elsif ($Mode eq "upload_loop")
{
    while(1) 
    {
        my $startTime = time();
        my $elapsedTime;

        # before we start (another) round of uploads we first check 
        # if something bad happened in the past.
        checkFaults();

        # look for stopfile and exit if found
        if (-e "stopfile.txt")
        {
            statusMessage("Stopfile found, exiting",1,0);
            exit;
        }

        # Add a basic auto-updating mechanism. 
        if (NewClientVersion()) 
        {
            UpdateClient();
            reExec(-1);
        }

        # check for new version of tilesGen.pl and reExec if true
        reExecIfRequired(-1);

        # uploading ZIP files here, returns 0 if nothing to do and -1 on error
        my $files_uploaded = upload();
            
        if ($files_uploaded == -1)  # we got an error in the upload process
        {   # increase fault counter
            addFault("upload",1);
        }
        elsif ($files_uploaded == 0) # no error, but no files uploaded
        {
            talkInSleep("waiting for new ZIP files to upload",30);
        }
        else
        {   #reset fault counter for uploads if once without error
            resetFault("upload");
            $elapsedTime = time() - $startTime;
            statusMessage(sprintf("upload finished in  %d:%02d", 
              $elapsedTime/60, $elapsedTime%60),1,0);
            $progressJobs++;
        }
    } #end of infinite while loop
}
#---------------------------------
elsif ($Mode eq "version") 
{
    exit(1);
}
#---------------------------------
elsif ($Mode eq "stop")
{
    if (open F, '>', "stopfile.txt") 
    {
        close F;
        statusMessage("stop signal was sent to the currently running tilesGen.pl",1,0);
        statusMessage("please note that it may take quite a while for it to exit",1,0);
        exit(0);
    }
    else
    {
        statusMessage("stop signal was NOT sent to the currently running tilesGen.pl - stopfile.txt could NOT be created",1,0);
    }
    exit(1);
}
#---------------------------------
elsif ($Mode eq "update") 
{
    UpdateClient();
}
#---------------------------------
elsif ($Mode eq "") 
{
    # ----------------------------------
    # Normal mode renders a single request from server and exits
    # ----------------------------------
    my ($did_something, $message) = ProcessRequestsFromServer();
    
    if (! $did_something)
    {
        statusMessage("you may safely press Ctrl-C now if you want to exit tilesGen.pl",1,0);
        talkInSleep($message, 60);
    }
    statusMessage("if you want to run this program continuously, use loop mode",1,0);
    statusMessage("please run \"tilesGen.pl upload\" now",1,0);
}
#---------------------------------
elsif ($Mode eq "startBatik")
{
    startBatikAgent();
}
#---------------------------------
elsif ($Mode eq "stopBatik")
{
    stopBatikAgent();
}
#---------------------------------
else {
    # ----------------------------------
    # "help" (or any other non understood parameter) as first argument tells how to use the program
    # ----------------------------------
    my $Bar = "-" x 78;
    print "\n$Bar\nOpenStreetMap tiles\@home client\n$Bar\n";
    print "Usage: \nNormal mode:\n  \"$0\", will download requests from server\n";
    print "Specific area:\n  \"$0 xy <x> <y> [z]\"\n  (x and y coordinates of a zoom-12 (default) tile in the slippy-map coordinate system)\n  See [[Slippy Map Tilenames]] on wiki.openstreetmap.org for details\nz is optional and can be used for low-zoom tilesets\n";
    print "Other modes:\n";
    print "  $0 loop - runs continuously\n";
    print "  $0 upload - uploads any tiles\n";
    print "  $0 upload_loop - uploads tiles in loop mode\n";
    print "  $0 startBatik - start batik agent\n";
    print "  $0 stopBatik - stop batik agent\n";
    print "  $0 version - prints out version string and exits\n";
    print "\nGNU General Public license, version 2 or later\n$Bar\n";
}


#-----------------------------------------------------------------------------
# forks to a new process when it makes sense,
# compresses all existing tileset dirs, uploads the resulting zip.
# returns >=0 on success, -1 otherwise and dies if it could not fork
#-----------------------------------------------------------------------------
sub compressAndUploadTilesets
{
    my $Config = TahConf->getConfig();
    if ($Config->get("ForkForUpload") and ($Mode eq "loop")) # makes no sense to fork upload if not looping.
    {
        # Upload is handled by another process, so that we can generate another tile at the same time.
        # We still don't want to have two uploading process running at the same time, so we wait for the previous one to finish.
        if ($upload_pid != -1)
        {
            statusMessage("Waiting for previous upload process to finish (this can take while)",1,3);
            waitpid($upload_pid, 0);
            #FIXME: $upload_result is apparently never returned?! skip?
            #$upload_result = $? >> 8;
        }

        $upload_pid = fork();
        if ((not defined $upload_pid) or ($upload_pid == -1))
        {   # exit if asked to fork but unable to
            cleanUpAndDie("loop: could not fork, exiting","EXIT",4);
        }
        elsif ($upload_pid == 0)
        {   # we are the child, so we run the upload and exit the thread
            exit compressAndUpload();
        }
    }
    else
    {   ## no forking going on
        return compressAndUpload();
    }
    # no error, just nothing to upload
    return 0;
}

#-----------------------------------------------------------------------------
# compressAndUpload() is just a shorthand for calling compress() and
# upload(). It returns >=0 on success and -1 otherwise.
#-----------------------------------------------------------------------------
sub compressAndUpload
{
  my $retval  = compress();
  my $retval2 = upload();
  # return the smaller of both values for now
  return ($retval > $retval2)? $retval2 : $retval;
}

#-----------------------------------------------------------------------------
# compress() calls the external compress.pl which zips up all existing
# tileset directories. It returns # of compressed dirs or -1 on error.
#-----------------------------------------------------------------------------
sub compress
{
    keepLog($$,"compress","start","$progressJobs");

    my $compress = new Compress;
    my ($retval, $reason) = $compress->compressAll();

    keepLog($$,"compress","stop","return=$retval");

    return $retval;
}

#-----------------------------------------------------------------------------
# upload() uploads all previously
# zipped up tilesets. It returns the number of uploaded files or -1 on error
#-----------------------------------------------------------------------------
sub upload
{
    #upload all existing zip files
    keepLog($PID,"upload","start","$progressJobs");

    my $upload = new Upload;
    my ($retval, $reason) = $upload->uploadAllZips();

    keepLog($PID,"upload","stop","return=$retval");

    return $retval;
}

#-----------------------------------------------------------------------------
# Ask the server what tileset needs rendering next
#-----------------------------------------------------------------------------
sub ProcessRequestsFromServer 
{
    my $Config = TahConf->getConfig();
    if ($Config->get("LocalSlippymap"))
    {
        print "Config option LocalSlippymap is set. Downloading requests\n";
        print "from the server in this mode would take them from the tiles\@home\n";
        print "queue and never upload the results. Program aborted.\n";
        cleanUpAndDie("ProcessRequestFromServer:LocalSlippymap set, exiting","EXIT",1);
    }

    statusMessage("Retrieving next job", 0, 3);
    my $req = new Request;
    eval {
        $req->fetchFromServer();
    };

    if (my $error = $@) {
        if (blessed($error) && $error->isa("TahError")) {
            cleanUpAndDie($error->text(), "EXIT", 1);
        }
        else {
            die;
        }
    }

    #TODO: return result of GenerateTileset?
    my $tileset = Tileset->new($req);
    my ($success, $reason) = $tileset->generate();
    if (!$success)
    {
        eval {
            $req->putBackToServer($reason) unless $Mode eq 'xy';
        };
    }
    return ($success, $reason);
}

#-----------------------------------------------------------------------------
# Gets latest copy of client from svn repository
# returns 1 on perceived success.
#-----------------------------------------------------------------------------
sub UpdateClient # 
{
    my $Config = TahConf->getConfig();
    my $Cmd = sprintf("%s\"%s\" %s",
        $Config->get("i18n") ? "LC_ALL=C " : "",
        $Config->get("Subversion"),
        $Config->get("SubversionUpdateCmd"));

    statusMessage("Updating the Client",1,0);
    runCommand($Cmd,$PID); # FIXME: evaluate output and handle locally changed files that need updating!
    ## FIXME TODO: Implement and check output from svn status, too.

    $Cmd = sprintf("%s\"%s\" %s",
        $Config->get("i18n") ? "LC_ALL=C " : "",
        $Config->get("Subversion"),
        "status -q --ignore-externals");

    my $svn_status = `$Cmd`;

    chomp $svn_status;

    if (1 || $svn_status eq '')
    {
        my $versionfile = "version.txt";
        DownloadFile($Config->get("VersionCheckURL"), $versionfile ,0);
        return 1;
    }
    else
    {
        statusMessage("svn status did not come back clean, check your installation",1,0);
        print STDERR $svn_status;
        return cleanUpAndDie("Auto-update failed","EXIT",1);
    }
}

sub NewClientVersion 
{
    my $Config = TahConf->getConfig();
    return 0 if (time() - $LastTimeVersionChecked < 600);
    my $versionfile = "version.txt";
    my $runningVersion;
    if (open(VERFILE, "<", $versionfile))
    {
        $runningVersion = <VERFILE>;
        chomp $runningVersion;
        close VERFILE;
    }
    elsif (open(VERFILE, ">", $versionfile))
    {
        $runningVersion = 0; 
        print VERFILE $runningVersion;
        close VERFILE;
    }
    else
    {
        die("can't open $versionfile");
    }
    # return 0;

    my $curVerFile = "newversion.txt";
    my $currentVersion;
    
    DownloadFile($Config->get("VersionCheckURL"), $curVerFile ,0);
    if (open(VERFILE, "<", $curVerFile))
    {
        $currentVersion = <VERFILE>;
        chomp $runningVersion;
        close VERFILE;
        # rename($curVerFile,$versionfile); # FIXME: This assumes the client is immediately, and successfully updated afterwards!
    }
    if ($currentVersion)
    {
        $LastTimeVersionChecked = time();
        if ($runningVersion > $currentVersion)
        {
            statusMessage("\n! WARNNG: you cannot have a more current client than the server: $runningVersion > $currentVersion",1,0);
            return 0;
        }
        elsif ($runningVersion == $currentVersion)
        {
            return 0; # no new version
        }
        else
        {
            return 1; # version on server is newer
        }
    }
    else
    {
        statusMessage(" ! WARNING: Could not get version info from server!",1,0);
        return 0;
    }
}


#-----------------------------------------------------------------------------
# Transform an OSM file (using osmarender) into SVG
# returns 1 on success, 0 on failure
#-----------------------------------------------------------------------------
sub xml2svg 
{
    my $Config = TahConf->getConfig();
    my($MapFeatures, $SVG, $zoom) = @_;
    my $TSVG = "$SVG";
    my $NoBezier = $Config->get("NoBezier") || $zoom <= 11;

    if (!$NoBezier) 
    {
        $TSVG = "$SVG-temp.svg";
    }

    my $success = 0;
    if ($Config->get("Osmarender") eq "XSLT")
    {
        my $XslFile;

        $XslFile = "osmarender/osmarender.xsl";

        my $Cmd = sprintf("%s \"%s\" tr --maxdepth %s %s %s > \"%s\"",
          $Config->get("Niceness"),
          $Config->get("XmlStarlet"),
          $Config->get("XmlStarletMaxDepth"),
          $XslFile,
          "$MapFeatures",
          $TSVG);

        statusMessage("Transforming zoom level $zoom with XSLT",0,3);
        $success = runCommand($Cmd,$PID);
    }
    elsif($Config->get("Osmarender") eq "orp")
    {
        my $Cmd = sprintf("%s perl orp/orp.pl -r %s -o %s",
          $Config->get("Niceness"),
          $MapFeatures,
          $TSVG);

        statusMessage("Transforming zoom level $zoom with or/p",0,3);
        $success = runCommand($Cmd,$PID);
    }
    else
    {
        die "invalid Osmarender setting in config";
    }
    if (!$success) {
        statusMessage(sprintf("%s produced an error, aborting render.", $Config->get("Osmarender")),1,0);
        return cleanUpAndDie("xml2svg failed",$Mode,3);
    }

    # look at temporary svg wether it really is a svg or just the 
    # xmlstarlet dump and exit if the latter.
    open(SVGTEST, "<", $TSVG) || return;
    my $TestLine = <SVGTEST>;
    chomp $TestLine;
    close SVGTEST;

    if (grep(!/</, $TestLine))
    {
       statusMessage("File $TSVG doesn't look like svg, aborting render.",1,0);
       return cleanUpAndDie("xml2svg failed",$Mode,3);
    }
#-----------------------------------------------------------------------------
# Process lines to Bezier curve hinting
#-----------------------------------------------------------------------------
    if (!$NoBezier) 
    {   # do bezier curve hinting
        my $Cmd = sprintf("%s perl ./lines2curves.pl %s > %s",
          $Config->get("Niceness"),
          $TSVG,
          $SVG);
        statusMessage("Beziercurvehinting zoom level $zoom",0,3);
        runCommand($Cmd,$PID);
#-----------------------------------------------------------------------------
# Sanitycheck for Bezier curve hinting, no output = bezier curve hinting failed
#-----------------------------------------------------------------------------
        my $filesize= -s $SVG;
        if (!$filesize) 
        {
            copy($TSVG,$SVG);
            statusMessage("Error on Bezier Curve hinting, rendering without bezier curves",1,0);
        }
    }
    else
    {   # don't do bezier curve hinting
        statusMessage("Bezier Curve hinting disabled.",0,3);
    }
    return 1;
}


#-----------------------------------------------------------------------------
# Render a SVG file
# jobdir - dir in which the svg is stored, and temporary files can be put.
# $X, $Y - tilemnumbers of the tileset
# $Ytile - the actual tilenumber in Y-coordinate of the zoom we are processing
# returns (success, reason), success is 0 or 1
# reason is a string on failure
#-----------------------------------------------------------------------------
sub svg2png
{
    my($jobdir, $req, $Ytile, $Zoom, $Left, $Y1, $Right, $Y2, $ImageHeight) = @_;
    my $Config = TahConf->getConfig();
    
    # File locations
    my $svgFile = File::Spec->join($jobdir,"output-z$Zoom.svg");
    my $FullSplitPngFile = File::Spec->join($jobdir,"split-z$Zoom-$Ytile.png");
    my $stdOut = File::Spec->join($jobdir,"split-z$Zoom-$Ytile.stdout");

    
    my $Cmd = "";
    
    # SizeX, SizeY are height/width dimensions of resulting PNG file
    my $SizeX = 256 * (2 ** ($Zoom - $req->Z));
    my $SizeY = 256;

    # SVG excerpt in SVG units
    my $Top = $ImageHeight - $Y2;
    my $Width = $Right - $Left;
    my $Height = $Y2 - $Y1;
    


    if ($Config->get("Batik") == "1") # batik as jar
    {
        $Cmd = sprintf("%s%s java -Xms256M -Xmx%s -jar %s -w %d -h %d -a %f,%f,%f,%f -m image/png -d \"%s\" \"%s\" > %s", 
        $Config->get("i18n") ? "LC_ALL=C " : "",
        $Config->get("Niceness"),
        $Config->get("BatikJVMSize"),
        $Config->get("BatikPath"),
        $SizeX,
        $SizeY,
        $Left,$Top,$Width,$Height,
        $FullSplitPngFile,
        $svgFile,
        $stdOut);
    }
    elsif ($Config->get("Batik") == "2") # batik as executable (wrapper of some sort, i.e. on gentoo)
    {
        $Cmd = sprintf("%s%s \"%s\" -w %d -h %d -a %f,%f,%f,%f -m image/png -d \"%s\" \"%s\" > %s",
        $Config->get("i18n") ? "LC_ALL=C " : "",
        $Config->get("Niceness"),
        $Config->get("BatikPath"),
        $SizeX,
        $SizeY,
        $Left,$Top,$Width,$Height,
        $FullSplitPngFile,
        $svgFile,
        $stdOut);
    }
    elsif ($Config->get("Batik") == "3") # agent
    {
        $Cmd = sprintf("svg2png\nwidth=%d\nheight=%d\narea=%f,%f,%f,%f\ndestination=%s\nsource=%s\nlog=%s\n\n", 
        $SizeX,
        $SizeY,
        $Left,$Top,$Width,$Height,
        $FullSplitPngFile,
        $svgFile,
        $stdOut);
    }
    else
    {
        my $locale = $Config->get("InkscapeLocale");
        my $oldLocale;
        if ($locale ne "0") {
                $oldLocale=setlocale(LC_ALL, $locale);
        } 

        $Cmd = sprintf("%s%s \"%s\" -z -w %d -h %d --export-area=%f:%f:%f:%f --export-png=\"%s\" \"%s\" > %s", 
        $Config->get("i18n") ? "LC_ALL=C " : "",
        $Config->get("Niceness"),
        $Config->get("Inkscape"),
        $SizeX,
        $SizeY,
        $Left,$Y1,$Right,$Y2,
        $FullSplitPngFile,
        $svgFile,
        $stdOut);

        if ($locale ne "0") {
                setlocale(LC_ALL, $oldLocale);
        } 
    }
    
    # stop rendering the current job when inkscape fails
    statusMessage("Rendering",0,3);
    print STDERR "\n$Cmd\n" if ($Config->get("Debug"));


    my $commandResult = $Config->get("Batik") == "3"?sendCommandToBatik($Cmd) eq "OK":runCommand($Cmd,$PID);
    if (!$commandResult or ! -e $FullSplitPngFile )
    {
        statusMessage("$Cmd failed",1,0);
        if ($Config->get("Batik") == "3" && !getBatikStatus())
        {
            statusMessage("Batik agent is not running, use $0 startBatik to start batik agent\n",1,0);
        }
        my $reason = "BadSVG (svg2png)";
        addFault("inkscape",1);
        $req->is_unrenderable(1);
        return (0, $reason);
    }
    resetFault("inkscape"); # reset to zero if inkscape succeeds at least once
    
     return ($FullSplitPngFile, ""); #return success
}


#-----------------------------------------------------------------------------
# Add bounding-box information to an osm-map-features file
#-----------------------------------------------------------------------------
sub AddBounds 
{
    my ($Filename,$W,$S,$E,$N,$Size) = @_;
    
    # Read the old file
    open(my $fpIn, "<", "$Filename");
    my $Data = join("",<$fpIn>);
    close $fpIn;
    die("no such $Filename") if(! -f $Filename);
    
    # Change some stuff
    no locale;                # use dot as separator even for Germans!
    my $BoundsInfo = sprintf(
      "<bounds minlat=\"%f\" minlon=\"%f\" maxlat=\"%f\" maxlon=\"%f\" />",
      $S, $W, $N, $E);
    
    $Data =~ s/(<!--bounds_mkr1-->).*(<!--bounds_mkr2-->)/$1\n<!-- Inserted by tilesGen -->\n$BoundsInfo\n$2/s;
    
    # Save back to the same location
    open(my $fpOut, ">$Filename");
    print $fpOut $Data;
    close $fpOut;
}

#-----------------------------------------------------------------------------
# Set data source file name in map-features file
#-----------------------------------------------------------------------------
sub SetDataSource 
{
    my ($Datafile, $Rulesfile) = @_;

    # Read the old file
    open(my $fpIn, "<", "$Rulesfile");
    my $Data = join("",<$fpIn>);
    close $fpIn;
    die("no such $Rulesfile") if(! -f $Rulesfile);

    $Data =~ s/(  data=\").*(  scale=\")/$1$Datafile\"\n$2/s;

    # Save back to the same location
    open(my $fpOut, ">$Rulesfile");
    print $fpOut $Data;
    close $fpOut;
}

#-----------------------------------------------------------------------------
# Get the width and height (in SVG units, must be pixels) of an SVG file
#-----------------------------------------------------------------------------
sub getSize($)
{
    my $SVG = shift();
    open(my $fpSvg,"<",$SVG);
    while(my $Line = <$fpSvg>)
    {
        if($Line =~ /height=\"(.*)px\" width=\"(.*)px\"/)
        {
            close $fpSvg;
            return(($1,$2,1));
        }
    }
    close $fpSvg;
    return((0,0,0));
}

#-----------------------------------------------------------------------------
# Filename to store a tile.
# returns a (path and a filename) component
#-----------------------------------------------------------------------------
sub tileFilename 
{
    my($req, $layer, $X, $Y,$Zoom) = @_;
    my $Config = TahConf->getConfig();
    my ($path, $name);
    if ($Config->get("LocalSlippymap"))
    {	
        $path = File::Spec->catdir($Config->get($layer."_Prefix"),$Zoom,$X);
        $name = $Y.'.png';
    }
    else
    {
         $path = sprintf("%s_%d_%d_%d.dir",
				     $Config->get($layer."_Prefix"),$req->ZXY);
         $name = sprintf("%s_%d_%d_%d.png",
                  $Config->get($layer."_Prefix"),$Zoom,$X,$Y);
    }

    return ($path, $name);
}

#-----------------------------------------------------------------------------
# Split a tileset image into tiles
# $File points to a png with complete path
# returns (success, allempty, reason)
#-----------------------------------------------------------------------------
sub splitImageX
{
    my ($layer, $req, $Z, $Ytile, $File) = @_;
    my $Config = TahConf->getConfig();
    my ($JobVolume, $JobDir, $BigPNGFileName) = File::Spec->splitpath($File);

    # Size of tiles
    my $Pixels = 256;
  
    # Number of tiles
    my $Size = 2 ** ($Z - $req->Z);

    # Assume the tileset is empty by default
    my $allempty=1;
  
    # Load the tileset image
    statusMessage(sprintf("Splitting %s (%d x 1)", $BigPNGFileName, $Size),0,3);
    my $Image = newFromPng GD::Image($File);
    if( not defined $Image )
    {
        print STDERR "\nERROR: Failed to read in file $BigPNGFileName\n";
        $req->putBackToServer("MissingFile");
        cleanUpAndDie("SplitImageX:MissingFile encountered, exiting","EXIT",4);
	return (0, 0, "SplitImage: MissingFile encountered");
    }
  
    # Use one subimage for everything, and keep copying data into it
    my $SubImage = new GD::Image($Pixels,$Pixels);
  
    # For each subimage
    for(my $xi = 0; $xi < $Size; $xi++)
    {
        # Get a tiles'worth of data from the main image
        $SubImage->copy($Image,
          0,                   # Dest X offset
          0,                   # Dest Y offset
          $xi * $Pixels,       # Source X offset
          0,                   # Source Y offset # always 0 because we only cut from one row
          $Pixels,             # Copy width
          $Pixels);            # Copy height

        # Decide what the tile should be called
        my ($PngDirPart, $PngFileName)  = tileFilename($req, $layer, $req->X * $Size + $xi, $Ytile, $Z);
        my $PngFullFileName;

        if ($Config->get("LocalSlippymap"))
        {
            my $PngFullDir = File::Spec->catdir($Config->get("LocalSlippymap"), $PngDirPart);
            File::Path::mkpath($PngFullDir);
            $PngFullFileName = File::Spec->join($PngFullDir, $PngFileName);

        } else
        {   # Construct base png directory
            my $PngFullDir = File::Spec->catpath($JobVolume, $JobDir, $PngDirPart);
            File::Path::mkpath($PngFullDir);
            $PngFullFileName = File::Spec->join($PngFullDir, $PngFileName);
	}

        # Check for black tile output
        if (not ($SubImage->compare($BlackTileImage) & GD_CMP_IMAGE)) 
        {
            print STDERR "\nERROR: Your inkscape has just produced a totally black tile. This usually indicates a broken Inkscape, please upgrade.\n";
            cleanUpAndDie("SplitImageX:BlackTile encountered, exiting","EXIT",4);
            return (0, 0,"BlackTile");
        }
        # Detect empty tile here:
        elsif (not($SubImage->compare($EmptyLandImage) & GD_CMP_IMAGE)) # libGD comparison returns true if images are different. (i.e. non-empty Land tile) so return the opposite (false) if the tile doesn''t look like an empty land tile
        {
            copy("emptyland.png", $PngFullFileName);
        }
        elsif (not($SubImage->compare($EmptySeaImage) & GD_CMP_IMAGE)) # same for Sea tiles
        {
	    copy("emptysea.png", $PngFullFileName);
        }
        else
        {
            # If at least one tile is not empty set $allempty false:
            $allempty = 0;
    
            if ($Config->get($layer."_Transparent")) 
            {
                $SubImage->transparent($SubImage->colorAllocate(248,248,248));
            }
            else 
            {
                $SubImage->transparent(-1);
            }

            # Store the tile
            statusMessage(" -> $PngFileName",0,10);
            WriteImage($SubImage,$PngFullFileName);
        }
    }
    undef $SubImage;
    undef $Image;
    return (1, $allempty, "");
}

#-----------------------------------------------------------------------------
# Write a GD image to disk
#-----------------------------------------------------------------------------
sub WriteImage 
{
    my ($Image, $Filename) = @_;
    
    # Get the image as PNG data
    my $png_data = $Image->png;
    
    # Store it
    open (my $fp, ">$Filename") || cleanUpAndDie("WriteImage:could not open file for writing, exiting","EXIT",3);
    binmode $fp;
    print $fp $png_data;
    close $fp;
}


#-----------------------------------------------------------------------------
# A function to re-execute the program.  
#
# This function attempts to detect whether the perl script has changed
# since it was invoked initially, and if so, just runs the new version.
# This can be used to update the program while it is running (as it is
# sometimes hard to hit Ctrl-C at exactly the right moment!)
#-----------------------------------------------------------------------------
sub reExecIfRequired
{
    my $child_pid = shift();## FIXME: make more general

    my ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size,$atime,$mtime,
        $ctime,$blksize,$blocks) = stat($0);
    my $de = "$size/$mtime/$ctime";
    if (!defined($filestat))
    {
        $filestat = $de; 
        return;
    }
    elsif ($filestat ne $de)
    {
        reExec($child_pid);
    }
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
    return unless ($^O eq "linux" || $^O eq "cygwin");

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


sub startBatikAgent
{
    my $Config = TahConf->getConfig();
    if (getBatikStatus()) {
        statusMessage("BatikAgent is already running\n",0,0);
        return;
    }

    statusMessage("Starting BatikAgent\n",0,0);
    my $Cmd;
    if ($^O eq "linux" || $^O eq "cygwin") 
    {
        $Cmd = sprintf("%s%s java -Xms256M -Xmx%s -cp %s org.tah.batik.ServerMain -p %d > /dev/null&", 
          $Config->get("i18n") ? "LC_ALL=C " : "",
          $Config->get("Niceness"),
          $Config->get("BatikJVMSize"),
          $Config->get("BatikClasspath"),
          $Config->get("BatikPort")
        );
    }
    elsif ($^O eq "MSWin32")
    {
        $Cmd = sprintf("%s java -Xms256M -Xmx%s -cp %s org.tah.batik.ServerMain -p %d", 
           "start /B /LOW",
           $Config->get("BatikJVMSize"),
           $Config->get("BatikClasspath"),
           $Config->get("BatikPort")
         );
    }
    else ## just try the linux variant and hope for the best
    {
        $Cmd = sprintf("%s%s java -Xms256M -Xmx%s -cp %s org.tah.batik.ServerMain -p %d > /dev/null&", 
          $Config->get("i18n") ? "LC_ALL=C " : "",
          $Config->get("Niceness"),
          $Config->get("BatikJVMSize"),
          $Config->get("BatikClasspath"),
          $Config->get("BatikPort") 
         );
        statusMessage("Could not determine Operating System ".$^O.", please report to tilesathome mailing list",1,0);
    }
    
    system($Cmd);

    for (my $i = 0; $i < 10; $i++) {
        sleep(1);
        if (getBatikStatus()) {
            statusMessage("BatikAgent started succesfully",0,0);
            return;
        }
    }
    print STDERR "Unable to start BatikAgent with this command:\n";
    print STDERR "$Cmd\n";
}

sub stopBatikAgent
{
    if (!getBatikStatus()) {
        statusMessage("BatikAgent is not running\n",0,0);
        return;
    }

    sendCommandToBatik("stop\n\n");
    statusMessage("Send stop command to BatikAgent\n",0,0);
}

sub sendCommandToBatik
{
    (my $command) = @_;
    my $Config = TahConf->getConfig();

    my $sock = new IO::Socket::INET( PeerAddr => 'localhost', PeerPort => $Config->get("BatikPort"), Proto => 'tcp');
    return "ERROR" unless $sock;    

    print $sock $command;
    flush $sock;
    my $reply = <$sock>;
    $reply =~ s/\n//;
    close($sock);

    return $reply;
}

sub getBatikStatus
{
    return sendCommandToBatik("status\n\n") eq "OK";
}


#------------------------------------------------------------
# check for faults and die when too many have occured
#------------------------------------------------------------
sub checkFaults
{
    if (getFault("fatal") > 0) {
        cleanUpAndDie("Fatal error occurred during loop, exiting","EXIT",1);
    }
    elsif (getFault("inkscape") > 5) {
        cleanUpAndDie("Five times inkscape failed, exiting","EXIT",1);
    }
    elsif (getFault("renderer") > 10) {
        cleanUpAndDie("rendering a tileset failed 10 times in a row, exiting","EXIT",1);
    }
    elsif (getFault("upload") > 50) {
        cleanUpAndDie("Five times the upload failed, perhaps the server doesn't like us, exiting","EXIT",1);
    }
}


#--------------------------------------------------------------------------------------
# check for faults with data downloads and add delays or die when too many have occured
#--------------------------------------------------------------------------------------
sub checkDataFaults
{
    my $sleepdelay = 1;
    if (getFault("nodata") > 0) { # check every network condition regardless of the other network outcomes
        my $numfaults=getFault("nodata");
        if ($numfaults > 25) {
            cleanUpAndDie("More than 25 times no data, perhaps the server doesn't like us, exiting","EXIT",1);
        }
        else {
            $sleepdelay=5*(2**$numfaults); # wait 10, 20, 40, 80, ... seconds. for a total of about 6 hours
            $sleepdelay=600 if ($sleepdelay > 600);
            talkInSleep($numfaults." times no data", $sleepdelay);
        }
    }
    if (getFault("nodataXAPI") > 0) {
        my $numfaults=getFault("nodataXAPI");
        if ($numfaults >= 20) {
            cleanUpAndDie("20 times no data from XAPI, perhaps the server doesn't like us, exiting","EXIT",1); # allow XAPI more leeway
        }
        else {
            $sleepdelay=5*(2**$numfaults); # wait 10, 20, 49, 80 seconds
            $sleepdelay=600 if ($sleepdelay > 600);
            talkInSleep($numfaults." times no XAPI data", $sleepdelay);
        }
    }
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
        eval { decode("utf8",$osmline, Encode::FB_CROAK) };
        if ($@)
        {
            return $line; # returns the line the error occured on
        }
    }
    return 0;
}

