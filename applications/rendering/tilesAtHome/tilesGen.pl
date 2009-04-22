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
## NOTE: Version Check will not be run in xy mode, but on subsequent upload.
## this will fix issues with modified, locally used clients that never ever upload
if ($UploadMode or $LoopMode)
{
    if (NewClientVersion())
    {
        UpdateClient();
        if ($LoopMode) 
        {
            reExec(-1);
        }
        else
        {
            statusMessage("tilesGen.pl has changed. Please restart new version.",1,0);
            exit;
        }
    }
}
elsif ($RenderMode and ClientModified())
{
    statusMessage("! tilesGen.pl differs from svn repository. DO NOT UPLOAD to t\@h server.",1,0);
}

# Get version number from version-control system, as integer
my $Version = '$Revision$';
$Version =~ s/\$Revision:\s*(\d+)\s*\$/$1/;
printf STDERR "This is version %d (%s) of tilesgen running on %s, ID: %s\n", 
    $Version, $Config->get("ClientVersion"), $^O, GetClientId();

# autotuning complexity setting
my $complexity = 0;

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

# Setup SVG::Rasterize
if( $RenderMode || $Mode eq 'startBatik' || $Mode eq 'stopBatik' ){
    $SVG::Rasterize::object = SVG::Rasterize->new();
    if( $Config->get("Rasterizer") ){
        $SVG::Rasterize::object->engine( $Config->get("Rasterizer") );
    }

    print "- rasterizing using ".ref($SVG::Rasterize::object->engine)."\n";

    if( $SVG::Rasterize::object->engine()->isa('SVG::Rasterize::Engine::BatikAgent') )
    {
        $SVG::Rasterize::object->engine()->heapsize($Config->get("BatikJVMSize"));
        $SVG::Rasterize::object->engine()->host('localhost');
        $SVG::Rasterize::object->engine()->port($Config->get("BatikPort"));
    }

    # Check for broken Inkscape versions
    if( $SVG::Rasterize::object->engine()->isa('SVG::Rasterize::Engine::Inkscape') ){
        my %brokenInkscapeVersions = (
            "RenderStripes=0 will not work" => [0, 45, 1]
            );

        try {
            my @version = $SVG::Rasterize::object->engine()->version();
            die if scalar(@version) == 0;

            while( my( $reason, $ver ) = each %brokenInkscapeVersions ){
                my @brokenVersion = @{ $ver };

                my $equal = 1;
                if( $#brokenVersion == $#version ){
                    for( my $i=0; $i < @version; $i++ ){
                        $equal = $version[$i] eq $brokenVersion[$i];
                        last if ! $equal;
                    }
                } else {
                    $equal = 0;
                }

                if( $equal ){
                    printf("! You have a broken version of Inkscape, %s. %s\n", join('.', @version), $reason);
                }
            }
        } otherwise {
            print "! Could not determine your Inkscape version\n";
        };
        print "* Take care to manually backup your inkscape user preferences\n"; 
        print "  if you have knowingly changed them. \n";
        print "  Some tilesets will cause inkscape to clobber that file!\n";
#        print "  ~/.inkscape/preferences.xml\n";
    }
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

## set all fault counters to 0;
resetFault("fatal");
resetFault("rasterizer");
resetFault("nodata");
resetFault("nodataROMA");
resetFault("nodataXAPI");
resetFault("renderer");
resetFault("utf8");
resetFault("upload");

unlink("stopfile.txt") if $Config->get("AutoResetStopfile");

# Be nice. Reduce program priority
if( my $nice = $Config->get("Niceness") ){
    if( $nice =~ /nice/ ){
        $nice =~ s/nice\s*-n\s*//;
        warn "You have Niceness set to a command, it should be only a number.\n";
    }

    if( $nice =~ /^\d+$/ ){
        my $success=POSIX::nice($nice);
        if( !defined($success) ){
            printf STDERR "WARNING: Unable to apply Niceness. Will run at normal priority";
        }
    }
}

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
        print STDERR "Usage: $0 xy <X> <Y> [<ZOOM> [<LAYERS>]]\n";
        print STDERR "where <X> and <Y> are the tile coordinates and \n";
        print STDERR "<ZOOM> is an optional zoom level (defaults to 12).\n";
        print STDERR "<LAYERS> is a comma separated list (no spaces) of the layers to render.\n";
        print STDERR "This overrides the layers specified in the configuration.\n";
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

    my $Layers = shift();
    if (not defined $Layers) {
        if ($Zoom >= 12) {
            $Layers = $Config->get("Layers");
        }
        elsif ($Zoom >= 6) {
            $Layers = $Config->get("LowzoomLayers");
        }
        else {
            $Layers = $Config->get("WorldLayers");
        }
    }
    $req->layers_str($Layers);

    my $tileset = Tileset->new($req);
    my $tilestart = time();
    $tileset->generate();
    autotuneComplexity($tilestart, time(), $req->complexity);
}
#---------------------------------
elsif ($Mode eq "loop") 
{
    # ----------------------------------
    # Continuously process requests from server
    # ----------------------------------

    # Start batik agent if it's not runnig
    if( $SVG::Rasterize::object->engine()->isa('SVG::Rasterize::Engine::BatikAgent') ){
        my $result = $SVG::Rasterize::object->engine()->start_agent();
        if( $result ){
            $StartedBatikAgent = 1;
            statusMessage("Started Batik agent", 0, 0);
        }
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
                statusMessage("Waiting for previous upload process (this can take a while)",1,0);
                waitpid($upload_pid, 0);
            }
            print "We suggest that you set MaxTilesetComplexity to ".int($complexity)."\n";
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
        $progressJobs++;
        # Render stuff if we get a job from server
        ProcessRequestsFromServer();
        # upload results
        uploadTilesets();
    }
}
#---------------------------------
elsif ($Mode eq "upload") 
{   # Upload mode
    upload();
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
            
        if ($files_uploaded == 0) {
            # no error, but no files uploaded
            talkInSleep("waiting for new ZIP files to upload",30);
        }
        else {
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
    my $result = $SVG::Rasterize::object->engine()->start_agent();
    if( $result ){
        $StartedBatikAgent = 1;
        statusMessage("Started Batik agent", 0, 0);
    } else {
        statusMessage("Batik agent already running");
    }
}
#---------------------------------
elsif ($Mode eq "stopBatik")
{
    my $result = $SVG::Rasterize::object->engine()->stop_agent();
    if( $result == 1 ){
        statusMessage("Successfully sent stop message to Batik agent", 0, 0);
    } elsif( $result == 0 ){
        statusMessage("Could not contact Batik agent", 0, 0);
    } else {
        statusMessage($result, 0, 0);
    }
}
#---------------------------------
else {
    # ----------------------------------
    # "help" (or any other non understood parameter) as first argument tells how to use the program
    # ----------------------------------
    my $Bar = "-" x 78;
    print "\n$Bar\nOpenStreetMap tiles\@home client\n$Bar\n";
    print "Usage: \nNormal mode:\n  \"$0\", will download requests from server\n";
    print "Specific area:\n  \"$0 xy <x> <y> [z [layers]]\"\n  (x and y coordinates of a\n";
    print "zoom-12 (default) tile in the slippy-map coordinate system)\n";
    print "See [[Slippy Map Tilenames]] on wiki.openstreetmap.org for details\n";
    print "z is optional and can be used for low-zoom tilesets\n";
    print "layers is a comma separated list (no spaces) of layers and overrides the config.\n";
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
# uploads available tile data.
# returns >=0 on success, -1 otherwise and dies if it could not fork
#-----------------------------------------------------------------------------
sub uploadTilesets
{
    my $Config = TahConf->getConfig();
    if ($Config->get("ForkForUpload") and ($Mode eq "loop")) # makes no sense to fork upload if not looping.
    {
        # Upload is handled by another process, so that we can generate another tile at the same time.
        # We still don't want to have two uploading process running at the same time, so we wait for the previous one to finish.
        if ($upload_pid != -1)
        {
            statusMessage("Waiting for previous upload process to finish (this can take a while)",1,3);
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
            try {
                upload();
            }
            otherwise {
                exit 0;
            };
            exit 1;
        }
    }
    else
    {   ## no forking going on
        try {
            my $result = upload();

            if ($result == -1)
            {     # we got an error in the upload process
                addFault("upload",1);
            }
            else
            {     #reset fault counter if we uploaded successfully
                resetFault("upload");
            }
        }
        catch UploadError with {
            my $err = shift();
            if (!$err->value() eq "QueueFull") {
                cleanUpAndDie("Error uploading tiles: " . $err->text(), "EXIT", 1);
            }
        };
    }
    # no error, just nothing to upload
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
    my $files_uploaded = 0;
    try {
        $files_uploaded = $upload->uploadAllZips();
        resetFault("upload");
    }
    catch UploadError with {
        my $error = shift();
        if ($error->value() eq "ServerError") {
            statusMessage("Server error: " . $error->text(), 1, 0);
            addFault("upload");
            talkInSleep("Waiting before attempting new upload", 300) if ($LoopMode);
        }
        elsif ($error->value() eq "0") {
            statusMessage("Upload error: " . $error->text(), 1, 0);
            addFault("upload");
            talkInSleep("Waiting before attempting new upload", 300) if ($LoopMode);
        }
        else {
            $error->throw();
        }
    };

    keepLog($PID, "upload", "stop", 0);
    return $files_uploaded;
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

    if ($Config->get("CreateTilesetFile"))
    {
        print "Config option CreateTilesetFile is set. We can not upload Tileset\n";
        print "files, yet. Downloading requests\n";
        print "from the server in this mode would take them from the tiles\@home\n";
        print "queue and never upload the results. Please use xy mode. Program aborted.\n";
        cleanUpAndDie("ProcessRequestFromServer:CreateTilesetFile set, exiting","EXIT",1);
    }

    my $req;
    my $Server = Server->new();
    do {
        statusMessage("Retrieving next job", 0, 3);
        try {
            $req = $Server->fetchRequest();

            # got request, now check that it's not too complex
            if ($Config->get('MaxTilesetComplexity')) {
                #the setting is enabled
                if ($req->complexity() > $Config->get('MaxTilesetComplexity')) 
                {
                    # too complex!
                    statusMessage("Ignoring too complex tile (" . $req->ZXY_str() . ", "
                    . int($req->complexity()) . " > " . int($Config->get('MaxTilesetComplexity')). ")", 1, 3);
                    eval {
                        $Server->putRequestBack($req, "TooComplex");
                    }; # ignoring exceptions
                    $req = undef;  # set to undef, need another loop
                    talkInSleep("Waiting before new tile is requested", 15); # to avoid re-requesting the same tile
                }
                elsif (not $req->complexity())
                {
                    # unknown complexity!
                    if ($Config->get('MaxTilesetComplexity') < $Config->get('AT_average')) 
                    {
                        # we have a weak client so we do not trust unknown complexity
                        statusMessage("Ignoring unknown complexity tile (" . $req->ZXY_str() . ", "
                           . int($req->complexity()) . " > " . int($Config->get('MaxTilesetComplexity')). ")", 1, 3);
                        eval {
                            $Server->putRequestBack($req, "NoComplexity");
                        }; # ignoring exceptions
                        $req = undef;  # set to undef, need another loop
                        talkInSleep("Waiting before new tile is requested", 15); # to avoid re-requesting the same tile
                    }
                }
            }
            # and now check whether we found it unrenderable before
            if (defined $req and $req->is_unrenderable()) {
                statusMessage("Ignoring unrenderable tile (" . $req->ZXY_str . ')', 1, 3);
                eval {
                    $Server->putRequestBack($req, "Unrenderable");
                }; # ignoring exceptions
                $req = undef;   # we need to loop yet again
                talkInSleep("Waiting before new tile is requested", 15); # to avoid re-requesting the same tile
            }
            # check whether there are any layers requested
            if (defined $req and scalar($req->layers()) == 0) {
                my $layers;
                if ($req->Z() >= 12) {
                    $layers = $Config->get("Layers");
                }
                else {
                    $layers = $Config->get("LowzoomLayers");
                }
                statusMessage("Tile request with no layers, assuming default layers ($layers)", 1, 3);
                $req->layers_str($layers);
            }
            # check whether we can actually render the requested zoom level for all requested layers
            if (defined $req) {
                my $zoom = $req->Z();
                foreach my $layer ($req->layers()) {
                    if (($zoom < $Config->get("${layer}_MinZoom")) or ($zoom > $Config->get("${layer}_MaxZoom"))) {
                        statusMessage("Zoom level $zoom is out of the configured range for Layer $layer. Ignoring tile.", 1, 3);
                        eval {
                            $Server->putRequestBack($req, "ZoomOutOfRange ($layer z$zoom)");
                        };
                        $req = undef;
                        last; # don't check any more layers
                    }
                }
            }
        }
        catch ServerError with {
            my $err = shift();
            if ($err->value() eq "PermError") {
                cleanUpAndDie($err->text(), "EXIT", 1);
            }
            else {
                talkInSleep($err->text(), 60);
            }
        };
    } until ($req);

    # Information text to say what's happening
    statusMessage("Got work from the server: " . $req->layers_str() . ' (' . $req->ZXY_str() . ')', 0, 6);

    try {
        my $tileset = Tileset->new($req);
        my $tilestart = time();
        $tileset->generate();
        autotuneComplexity($tilestart, time(), $req->complexity);

        # successfully received data, reset data faults
        resetFault("nodata");
        resetFault("nodataROMA");
        resetFault("nodataXAPI");

        # successfully rendered, so reset renderer faults
        resetFault("renderer");
        resetFault("inkscape");
        resetFault("utf8");

        # Rendered tileset, don't idle in next round
        setIdle(0,0);
    }
    catch RequestError with {
        my $err = shift();
        cleanUpAndDie($err->text(), "EXIT", 1);
    }
    catch TilesetError with {
        my $err = shift();
        eval {
            $Server->putRequestBack($req, $err->text()) unless $Mode eq 'xy';
        }; # ignoring exceptions
        if ($err->value() eq "fatal") {
            # $err->value() is "fatal" for fatal errors 
            cleanUpAndDie($err->text(), "EXIT", 1);
        }
        else {
            # $err->value() contains the error category for non-fatal errors
            addFault($err->value(), 1);
            statusMessage($err->text(), 1, 0);
        }
        talkInSleep("Waiting before new tile is requested", 15); # to avoid re-requesting the same tile
    };
}
#-----------------------------------------------------------------------------
# autotunes the complexity variable to avoid too complex tiles
#-----------------------------------------------------------------------------
sub autotuneComplexity #
{
    my $start = shift();
    my $stop = shift();
    my $tilecomplexity = shift();
    my $deltaT = $stop - $start;

#    my $Config = TahConf->getConfig();
    my $timeaim = $Config->get("AT_timeaim");
    my $minimum = $Config->get("AT_minimum");
    my $alpha = $Config->get("AT_alpha");

    if($Mode eq "xy") {
        statusMessage ("Tile took us ".$deltaT." seconds to render",1,3);
    } else {
        statusMessage ("Tile of complexity ".$tilecomplexity." took us ".$deltaT." seconds to render",1,3);
    }

    if(! $complexity) { # this is the first call of this function
        if($Config->get('MaxTilesetComplexity')) {
            $complexity = $Config->get('MaxTilesetComplexity');
        } elsif (($tilecomplexity > 5472) && ($deltaT > 0)) {
            $complexity = $tilecomplexity * $timeaim / $deltaT;
        } else {
            $complexity = 10 * $tilecomplexity;
        }
    }

    # empty tiles might have size 0 or 5472. if that changes, change this magic number too.
    if (($tilecomplexity > 5472) && ($deltaT > 0)) {
        $complexity = $alpha * ($tilecomplexity * $timeaim / $deltaT) + (1-$alpha) * $complexity;
    }
    $complexity = $minimum if $complexity < $minimum;

    statusMessage("Suggested complexity is currently: ".int($complexity)." ",1,6);

    if($Config->get('MaxTilesetComplexity')) {
        # if MaxTilesetComplexity is not set we still do our calculations
        # but we don't limit the client. The hint on client exit has to be enough.
        $Config->set('MaxTilesetComplexity', $complexity);
    }
}
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
        # rename($curVerFile,$versionfile); # if enabled, this assumes the client is immediately, and successfully updated afterwards!
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
    my($osmData, $bbox, $MapFeatures, $SVG, $zoom) = @_;
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

        $XslFile = "osmarender/xslt/osmarender.xsl";

        my $Cmd = sprintf(
          "\"%s\" tr --maxdepth %s %s -s osmfile=%s -s minlat=%s -s minlon=%s -s maxlat=%s -s maxlon=%s %s > \"%s\"",
          $Config->get("XmlStarlet"),
          $Config->get("XmlStarletMaxDepth"),
          $XslFile,
          $osmData,
          $bbox->S, $bbox->W, $bbox->N, $bbox->E,
          "$MapFeatures",
          $TSVG);

        statusMessage("Transforming zoom level $zoom with XSLT",0,3);
        $success = runCommand($Cmd,$PID);
    }
    elsif($Config->get("Osmarender") eq "orp")
    {
        my $Cmd = sprintf("perl osmarender/orp/orp.pl -r %s -o %s -b %s,%s,%s,%s %s",
          $MapFeatures,
          $TSVG,
          $bbox->S, $bbox->W, $bbox->N, $bbox->E,
          $osmData);

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
        my $Cmd = sprintf("perl ./lines2curves.pl %s > %s",
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

#------------------------------------------------------------
# check for faults and die when too many have occured
#------------------------------------------------------------
sub checkFaults
{
    if (getFault("fatal") > 0) {
        cleanUpAndDie("Fatal error occurred during loop, exiting","EXIT",1);
    }
    elsif (getFault("rasterizer") > 5) {
        cleanUpAndDie("Five times rasterizer failed, exiting","EXIT",1);
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

#sub fileUTF8ErrCheck moved to lib/tahlib.pm
