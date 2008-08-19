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

#---------------------------------
use strict;
use LWP::UserAgent;
use Math::Trig;
use File::Copy;
use File::Temp qw(tempfile);
use IO::Socket;
use FindBin qw($Bin);
use tahconfig;
use tahlib;
use tahproject;
use lib::TahConf;
use Request;
use English '-no_match_vars';
use GD qw(:DEFAULT :cmp);
use AppConfig qw(:argcount);
use locale;
use POSIX qw(locale_h);
use Encode;

#---------------------------------

# Read the config file
our $Config = TahConf->getConfig();
ApplyConfigLogic($Config);

# Handle the command-line
our $Mode = shift();
my $LoopMode = (($Mode eq "loop") or ($Mode eq "upload_loop")) ? 1 : 0;
my $RenderMode = (($Mode eq "") or ($Mode eq "xy") or ($Mode eq "loop")) ? 1 : 0;
my $UploadMode = (($Mode eq "upload") or ($Mode eq "upload_conditional") or ($Mode eq "upload_loop")) ? 1 : 0;
my %EnvironmentInfo;

# set the progress indicator variables
our $currentSubTask;
my $progress = 0;
our $progressJobs = 0;
our $progressPercent = 0;

# keep track of time running
our $progstart = time();

if ($UploadMode)
{
    %EnvironmentInfo = CheckBasicConfig($Config);
}
else
{
    %EnvironmentInfo = CheckConfig($Config);
}

# Create the working directory if necessary
mkdir $Config->get("WorkingDirectory") if(!-d $Config->get("WorkingDirectory"));

my $LastTimeVersionChecked = 0;   # version is only checked when last time was more than 10 min ago
if ($UploadMode or $RenderMode) {
    if (NewClientVersion()) {
        UpdateClient();
        if ($LoopMode) {
            reExec(-1);
        } else {
            print STDERR "tilesGen.pl has changed. Please restart new version.";
            exit;
        }
    }
}

# Get version number from version-control system, as integer
my $Version = '$Revision$';
$Version =~ s/\$Revision:\s*(\d+)\s*\$/$1/;
printf STDERR "This is version %d (%s) of tilesgen running on %s, ID: %s\n", 
    $Version, $Config->get("ClientVersion"), $^O, GetClientId();

my $dirent; 

if ($LoopMode) {
    # if this is a re-exec, we want to capture some of our status
    # information from the command line. this feature allows setting
    # any numeric variable by specifying "variablename=value" on the
    # command line after the keyword "reexec". Currently unsuitable 
    # for alphanumeric variables.
    
    if (shift() eq "reexec") {
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

my ($EmptyLandImage, $EmptySeaImage, $BlackTileImage);
my ($MapLandBackground, $MapSeaBackground, $BlackTileBackground);

if ($RenderMode) {
    # check GD
    eval GD::Image->trueColor(1);
    if ($@ ne '') {
        print STDERR "please update your libgd to version 2 for TrueColor support";
        cleanUpAndDie("init:libGD check failed, exiting","EXIT",4,$PID);
    }

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

# Subdirectory for the current job (layer & z12 tileset),
# as used in sub GenerateTileset() and tileFilename()
my $JobDirectory;

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
        cleanUpAndDie("init.osmarender_stylesheet_check repair failed","EXIT",4,$PID);
    }
}

## set all fault counters to 0;
resetFault("fatal");
resetFault("inkscape");
resetFault("nodata");
resetFault("nodataXAPI");
resetFault("renderer");
resetFault("utf8");
resetFault("upload");

unlink("stopfile.txt") if $Config->get("AutoResetStopfile");


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
    GenerateTileset($req);
}
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
                statusMessage("Waiting for previous upload process",0,0);
                waitpid($upload_pid, 0);
            }
            cleanUpAndDie("Stopfile found, exiting","EXIT",7,$PID); ## TODO: agree on an exit code scheme for different types of errors
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

        if ($upload_result)
        {     # we got an error in the upload process
              addFault("upload",1);
        }
        else
        {     #reset fault counter if we uploaded successfully
              resetFault("upload");
        }

        if ($did_something == 0) 
        {
            talkInSleep($message, 60);
        }
        else
        {   # Rendered tileset, don't idle in next round
            setIdle(0,0);
        }
    }
}
elsif ($Mode eq "upload" or $Mode eq "upload_conditional") 
{   # Upload modes. Note:"upload_conditional" is deprecated and will be removed
    compressAndUpload();
}
elsif ($Mode eq "upload_loop")
{
    my $startTime = time();
    my $elapsedTime;
    while(1) 
    {
        ## before we start (another) round of uploads we first check if something bad happened in the past.
        checkFaults();

        my $sleepdelay = 1;
        # look for stopfile and exit if found
        if (-e "stopfile.txt")
        {
            cleanUpAndDie("Stopfile found, exiting","EXIT",7,$PID); ## TODO: agree on an exit code scheme for different types of errors
        }

        # Add a basic auto-updating mechanism. 
        if (NewClientVersion()) 
        {
            UpdateClient();
            reExec(-1);
        }

        reExecIfRequired(-1); ## check for new version of tilesGen.pl and reExec if true

        if (countZips() > 0)
        {
            my $upload_result = upload(); # only uploading ZIP files here
            
            if ($upload_result)  # we got an error in the upload process
            {
                addFault("upload",1); # we only track errors that occur multple times in a row
            }
            else
            {
                resetFault("upload"); #reset fault counter for uploads if once without error
                statusMessage("upload finished",1,0);
                $progressJobs++;
            }
            $startTime = time();
        }
        else
        {
            $currentSubTask="uploadloop";
            $elapsedTime = time() - $startTime;
            statusMessage(sprintf("waiting for new ZIP files to upload   %d:%02d", $elapsedTime/60, $elapsedTime%60),0,0);
            sleep(30); # no reason to do this *every second* since we won't fall into this case as long as there are zips to upload anyway.
        }
    }
}
elsif ($Mode eq "version") 
{
    exit(1);
}
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
elsif ($Mode eq "update") 
{
    UpdateClient();
}
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
elsif ($Mode eq "startBatik")
{
    startBatikAgent();
}
elsif ($Mode eq "stopBatik")
{
    stopBatikAgent();
}
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

sub countZips
{
    my $ZipCount = 0;
    if (opendir(my $dp, $Config->get("WorkingDirectory")."uploadable"))
    {
        while(my $File = readdir($dp))
        {
            $ZipCount++ if ($File =~ /\.zip$/);
        }
        closedir($dp);
    }
    else 
    {
        mkdir $Config->get("WorkingDirectory")."uploadable";
    }
    return $ZipCount;
}

#-----------------------------------------------------------------------------
# forks to a new process when it makes sense,
# compresses all existing tileset dirs, uploads the resulting zip.
# returns 0 on success, >0 otherwisse and dies if it could not fork
#-----------------------------------------------------------------------------
sub compressAndUploadTilesets
{
    if ($Config->get("ForkForUpload") and ($Mode eq "loop")) # makes no sense to fork upload if not looping.
    {
        # Upload is handled by another process, so that we can generate another tile at the same time.
        # We still don't want to have two uploading process running at the same time, so we wait for the previous one to finish.
        if ($upload_pid != -1)
        {
            statusMessage("Waiting for previous upload process to finish",0,3);
            waitpid($upload_pid, 0);
            #FIXME: $upload_result is apparently never returned?! skip?
            #$upload_result = $? >> 8;
        }
        # compress before fork so we don't get temp files mangled.
        # Workaround for batik support.
        # FIXME: spaetz asks "FOR WHAT REASON?" Let's fix this the right way
        compress();
        $upload_pid = fork();
        if ((not defined $upload_pid) or ($upload_pid == -1))
        {   # exit if asked to fork but unable to
            cleanUpAndDie("loop: could not fork, exiting","EXIT",4,$PID);
        }
        elsif ($upload_pid == 0)
        {   # we are the child, so we run the upload and exit the thread
            exit (upload());
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
# upload(). It returns 0 on success and >0 otherwise.
#-----------------------------------------------------------------------------
sub compressAndUpload
{
  my $error = 0;
  $error += compress();
  $error += upload();
  return $error;
}

#-----------------------------------------------------------------------------
# compress() calls the external compress.pl which zips up all existing
# tileset directories. It returns 0 on success and >0 otherwise.
#-----------------------------------------------------------------------------
sub compress
{
    keepLog($PID,"compress","start","$progressJobs");

    my $CompressScript = "perl $Bin/compress.pl $progressJobs";
    my $retval = system($CompressScript);

    keepLog($PID,"compress","stop","return=$retval");

    return $retval;
}

#-----------------------------------------------------------------------------
# upload() calls the external upload.pl which uploads all previously
# zipped up tilesets. It returns 0 on success and >0 otherwise.
#-----------------------------------------------------------------------------
sub upload
{
    ## Run upload directly because it uses same messaging as tilesGen.pl, 
    ## no need to hide output at all.

    keepLog($PID,"upload","start","$progressJobs");

    my $UploadMode = ($Mode eq "upload_loop") ? "upload_loop" : "upload";
    my $UploadScript = "perl $Bin/upload.pl $UploadMode $progressJobs";
    my $retval = system($UploadScript);

    keepLog($PID,"upload","stop","return=$retval");

    return $retval;
}

#-----------------------------------------------------------------------------
# Ask the server what tileset needs rendering next
#-----------------------------------------------------------------------------
sub ProcessRequestsFromServer 
{
    if ($Config->get("LocalSlippymap"))
    {
        print "Config option LocalSlippymap is set. Downloading requests\n";
        print "from the server in this mode would take them from the tiles\@home\n";
        print "queue and never upload the results. Program aborted.\n";
        cleanUpAndDie("ProcessRequestFromServer:LocalSlippymap set, exiting","EXIT",1,$PID);
    }
    my ($success, $reason);
    my $req = new Request;
    ($success, $reason) = $req->fetchFromServer();

    if ($success)
    {
        GenerateTileset($req);
    }
    return ($success, $reason);
}

#-----------------------------------------------------------------------------
# Render a tile (and all subtiles, down to a certain depth)
#-----------------------------------------------------------------------------
sub GenerateTileset ## TODO: split some subprocesses to own subs
{
    # $req is a 'Request' object
    my $req = shift;
    
    keepLog($PID,"GenerateTileset","start","x=".$req->X.',y='.$req->Y.',z='.$req->Z." for layers ".$req->layers_str);
    
    my ($N, $S) = Project($req->Y, $req->Z);
    my ($W, $E) = ProjectL($req->X, $req->Z);
    
    $progress = 0;
    $progressPercent = 0;
    $progressJobs++;
    $currentSubTask = "jobinit";
    
    statusMessage(sprintf("Doing tileset (%d,%d,%d) (area around %f,%f)", $req->Z, $req->X, $req->Y, ($N+$S)/2, ($W+$E)/2),1,0);
    
    my $maxCoords = (2 ** $req->Z - 1);
    
    if ( ($req->X < 0) or ($req->X > $maxCoords) 
      or ($req->Y < 0) or ($req->Y > $maxCoords) )
    {
        #maybe do something else here
        die("\n Coordinates out of bounds (0..$maxCoords)\n");
    }
    
    $currentSubTask = "Preproc";
    
    # Adjust requested area to avoid boundary conditions
    my $N1 = $N + ($N-$S)*$Config->get("BorderN");
    my $S1 = $S - ($N-$S)*$Config->get("BorderS");
    my $E1 = $E + ($E-$W)*$Config->get("BorderE");
    my $W1 = $W - ($E-$W)*$Config->get("BorderW");

    # TODO: verify the current system cannot handle segments/ways crossing the 
    # 180/-180 deg meridian and implement proper handling of this case, until 
    # then use this workaround: 

    if($W1 <= -180) {
      $W1 = -180; # api apparently can handle -180
    }
    if($E1 > 180) {
      $E1 = 180;
    }

    my $bbox = sprintf("%f,%f,%f,%f",
      $W1, $S1, $E1, $N1);

    #------------------------------------------------------
    # Download data
    #------------------------------------------------------
    my $DataFile = $Config->get("WorkingDirectory")."data-$PID.osm";
    
    unlink($DataFile);
    my $URLS = sprintf("%s%s/map?bbox=%s",
      $Config->get("APIURL"),$Config->get("OSMVersion"),$bbox);
    if ($req->Z < 12) 
    {
        # FIXME: zoom 12 hardcoded: assume lowzoom caption layer now!
        # only in xy mode since in loop mode a different method that does not depend on hardcoded zoomlevel will be used, where the layer is set by the server.
        if ($Mode eq "xy") 
        {
            $req->layers("caption");
            statusMessage("Warning: lowzoom zoom detected, autoswitching to ".$req->layers_str." layer",1,0);
        }
        else
        {
            statusMessage("Warning: lowzoom zoom detected, but ".$req->layers_str." configured",1,0);
        }
        # Get the predicates for lowzoom caption layer, and build the URLS for them
        my $predicates = $Config->get($req->layers_str."_Predicates");
        # strip spaces in predicates because that is the separator used below
        $predicates =~ s/\s+//g;
        $URLS="";
        foreach my $predicate (split(/,/,$predicates)) {
            $URLS = $URLS . sprintf("%s%s/%s[bbox=%s] ",
                $Config->get("XAPIURL"),$Config->get("OSMVersion"),$predicate,$bbox);
        }
    }
    my @tempfiles;
    push(@tempfiles, $DataFile);
    my $filelist = [];
    my $i=0;
    foreach my $URL (split(/ /,$URLS)) 
    {
        ++$i;
        my $partialFile = $Config->get("WorkingDirectory")."data-$PID-$i.osm";
        push(@{$filelist}, $partialFile);
        push(@tempfiles, $partialFile);
        statusMessage("Downloading: Map data for ".$req->layers_str,0,3);
        print "Download\n$URL\n" if ($Config->get("Debug"));
        my $res = DownloadFile($URL, $partialFile, 0);

        if (! $res)
        {
            if ($req->Z < 12)
            {
                statusMessage("No data here...",1,0);
                # if loop was requested just return  or else exit with an error. 
                # (to enable wrappers to better handle this situation 
                # i.e. tell the server the job hasn't been done yet)
                $req->putBackToServer("NoData");
                unlink (@tempfiles);
                addFault("nodataXAPI",1);
                return cleanUpAndDie("GenerateTileset: no data!",$Mode,1,$PID);
            }
            elsif ($Config->get("FallBackToXAPI"))
            {
                statusMessage("No data here, trying OSMXAPI",1,0);
                $bbox = $URL;
                $bbox =~ s/.*bbox=//;
                $URL=sprintf("%s%s/%s[bbox=%s] ",
                    $Config->get("XAPIURL"),
                    $Config->get("OSMVersion"),
                    "*",
                    $bbox);
                statusMessage("Downloading: Map data for ".$req->layers_str." to $partialFile",0,3);
                print "Download\n$URL\n" if ($Config->get("Debug"));
                my $res = DownloadFile($URL, $partialFile, 0);
                if (! $res)
                {
                    statusMessage("No data on OSMXAPI either...",1,0);
                    $req->putBackToServer("NoData");
                    unlink(@tempfiles);
                    addFault("nodataXAPI",1);
                    return cleanUpAndDie("GenerateTileset: no data! (OSMXAPI)",$Mode,1,$PID);
                }
                else
                {
                    resetFault("nodataXAPI"); #reset to zero if data downloaded
                }
            }
            else
            {
                statusMessage("No data here, trying smaller slices",1,0);
                my $slice=(($E1-$W1)/10); # A chunk is one tenth of the width 
                for (my $j = 1 ; $j<=10 ; $j++)
                {
                    $URL = sprintf("%s%s/map?bbox=%f,%f,%f,%f", 
                      $Config->get("APIURL"),$Config->get("OSMVersion"), ($W1+($slice*($j-1))), $S1, ($W1+($slice*$j)), $N1); 
                    $partialFile = $Config->get("WorkingDirectory")."data-$PID-$i-$j.osm";
                    push(@{$filelist}, $partialFile);
                    push(@tempfiles, $partialFile);
                    statusMessage("Downloading: Map data to $partialFile (slice $j of 10)",0,3);
                    print "Download\n$URL\n" if ($Config->get("Debug"));
                    $res = DownloadFile($URL, $partialFile, 0);

                    if (! $res)
                    {
                        statusMessage("No data here (sliced)...",1,0);
                        $req->putBackToServer("NoData");
                        unlink(@tempfiles);
                        addFault("nodata",1);
                        return cleanUpAndDie("GenerateTileset: no data! (sliced).",$Mode,1,$PID);
                    }
                    else
                    {
                        resetFault("nodata"); #reset to zero if data downloaded
                    }
                }
                print STDERR "\n";
            }
        }
        else
        {
            if ($req->Z < 12) ## FIXME: hardcoded zoom
            {
                resetFault("nodataXAPI"); #reset to zero if data downloaded
            }
            else 
            {
                resetFault("nodata"); #reset to zero if data downloaded
            }
        }
    }

    mergeOsmFiles($DataFile, $filelist);

    if ($Config->get("KeepDataFile"))
    {
        copy($DataFile, $Config->get("WorkingDirectory") . "/" . "data.osm");
    }
  
    # Get the server time for the data so we can assign it to the generated image (for tracking from when a tile actually is)
    $JobTime = [stat $DataFile]->[9];
    
    # Check for correct UTF8 (else inkscape will run amok later)
    # FIXME: This doesn't seem to catch all string errors that inkscape trips over.
    statusMessage("Checking for UTF-8 errors in $DataFile",0,3);
    if (fileUTF8ErrCheck($DataFile))
    {
        statusMessage(sprintf("found incorrect UTF-8 chars in %s, job (%d,%d,%d)",$DataFile, $req->Z, $req->X, $req->Y),1,0);
        $req->putBackToServer("BadUTF8");
        addFault("utf8",1);
        return cleanUpAndDie("GenerateTileset:UTF8 test failed",$Mode,1,$PID);
    }
    resetFault("utf8"); #reset to zero if no UTF8 errors found.

    #------------------------------------------------------
    # Handle all layers, one after the other
    #------------------------------------------------------

    foreach my $layer($req->layers)
    {
        #reset progress for each layer
        $progress=0;
        $progressPercent=0;
        $currentSubTask = $layer;
        
        $JobDirectory = sprintf("%s%s_%d_%d_%d.tmpdir",
                                $Config->get("WorkingDirectory"),
                                $Config->get($layer."_Prefix"),
                                $req->Z, $req->X, $req->Y);
        mkdir $JobDirectory unless -d $JobDirectory;

        my $maxzoom = $Config->get($layer."_MaxZoom");
        my $layerDataFile;

        # Faff around
        for (my $i = $req->Z ; $i <= $maxzoom ; $i++) 
        {
            unlink($Config->get("WorkingDirectory")."output-$parent_pid-z$i.svg");
        }
        
        my $Margin = " " x ($req->Z - 8);
        printf "%03d %s%d,%d: %1.2f - %1.2f, %1.2f - %1.2f\n", $req->Z, $Margin, $req->X, $req->Y, $S,$N, $W,$E if ($Config->get("Debug"));
        
        
        #------------------------------------------------------
        # Go through preprocessing steps for the current layer
        #------------------------------------------------------
        my @ppchain = ($PID);
        # config option may be empty, or a comma separated list of preprocessors
        foreach my $preprocessor(split /,/, $Config->get($layer."_Preprocessor"))
        {
            my $inputFile = sprintf("%sdata-%s.osm", 
                $Config->get("WorkingDirectory"),
                join("-", @ppchain));
            push(@ppchain, $preprocessor);
            my $outputFile = sprintf("%sdata-%s.osm", 
                $Config->get("WorkingDirectory"),
                join("-", @ppchain));

            if (-f $outputFile)
            {
                # no action; files for this preprocessing step seem to have been created 
                # by another layer already!
            }
            elsif ($preprocessor eq "maplint")
            {
                # Pre-process the data file using maplint
                # TODO may put this into a subroutine of its own
                my $Cmd = sprintf("%s \"%s\" tr %s %s > \"%s\"",
                        $Config->get("Niceness"),
                        $Config->get("XmlStarlet"),
                        "maplint/lib/run-tests.xsl",
                        "$inputFile",
                        "tmp.$PID");
                statusMessage("Running maplint",0,3);
                runCommand($Cmd,$PID);
                $Cmd = sprintf("%s \"%s\" tr %s %s > \"%s\"",
                        $Config->get("Niceness"),
                        $Config->get("XmlStarlet"),
                        "maplint/lib/convert-to-tags.xsl",
                        "tmp.$PID",
                        "$outputFile");
                statusMessage("Creating tags from maplint",0,3);
                runCommand($Cmd,$PID);
                unlink("tmp.$PID");
            }
            elsif ($preprocessor eq "close-areas")
            {
                my $Cmd = sprintf("%s perl close-areas.pl %d %d %d < %s > %s",
                        $Config->get("Niceness"),
                        $req->X,
                        $req->Y,
                        $req->Z,
                        "$inputFile",
                        "$outputFile");
                statusMessage("Running close-areas",0,3);
                runCommand($Cmd,$PID);
            }
            elsif ($preprocessor eq "noop")
            {
                copy($inputFile,$outputFile);
            }
            else
            {
                die "Invalid preprocessing step '$preprocessor'";
            }
## Uncomment to have the output files checked for validity
#            if( $preprocessor ne "maplint" )
#            {
#              runCommand( qq(xmllint --dtdvalid http://dev.openstreetmap.org/~kleptog/tilesAtHome-0.3.dtd --noout $outputFile), $PID );
#            }
            push(@tempfiles, $outputFile);
        }

        #------------------------------------------------------
        # Preprocessing finished, start rendering
        #------------------------------------------------------

        #$layerDataFile = sprintf("%sdata-%s.osm", $Config->get("WorkingDirectory"), join("-", @ppchain));
        $layerDataFile = sprintf("data-%s.osm", join("-", @ppchain)); # Don't put working directory here, the path is relative to the rulesfile
        
        # Add bounding box to osmarender
        # then set the data source
        # then transform it to SVG
        if ($Config->get("Fork")) 
        {
            my $minimum_zoom = $req->Z;
            my $increment = 2 * $Config->get("Fork");
            my @children_pid;
            my $error = 0;
            for (my $i = 0; $i < 2 * $Config->get("Fork") - 1; $i ++) 
            {
                my $pid = fork();
                if (not defined $pid) 
                {
                    cleanUpAndDie("GenerateTileset: could not fork, exiting","EXIT",4,$PID); # exit if asked to fork but unable to
                }
                elsif ($pid == 0) 
                {
                    for (my $i = $minimum_zoom ; $i <= $maxzoom; $i += $increment) 
                    {
                        if (GenerateSVG($layerDataFile, $layer, $req->X, $req->Y, $i, $N, $S, $W, $E)) # if true then error occured
                        {
                             exit(1);
                        }
                    }
                    exit(0);
                }
                else
                {
                    push(@children_pid, $pid);
                    $minimum_zoom ++;
                }
            }
            for (my $i = $minimum_zoom ; $i <= $maxzoom; $i += $increment) 
            {
                if (GenerateSVG($layerDataFile, $layer, $req->X, $req->Y, $i, $N, $S, $W, $E))
                {
                    $error = 1;
                    last;
                }
            }
            foreach (@children_pid) 
            {
                waitpid($_, 0);
                $error |= $?;
            }
            if ($error) 
            {
                unlink(@tempfiles) if (!$Config->get("Debug"));
                $req->putBackToServer("RenderFailure");
                addFault("renderer",1);
                return 0;
            }
        }
        else
        {
            for (my $i = $req->Z ; $i <= $maxzoom; $i++)
            {
                if (GenerateSVG($layerDataFile, $layer, $req->X, $req->Y, $i, $N, $S, $W, $E))
                {
                    unlink(@tempfiles)if (!$Config->get("Debug"));
                    $req->putBackToServer("RenderFailure");
                    addFault("renderer",1);
                    return 0;
                }
            }
        }
        
        # Find the size of the SVG file
        my ($ImgH,$ImgW,$Valid) = getSize($Config->get("WorkingDirectory")."output-$parent_pid-z$maxzoom.svg");

        # Render it as loads of recursive tiles
        my ($success,$empty) = RenderTile($layer, $req, $req->Y, $req->Z, $N, $S, $W, $E, 0,0,$ImgW,$ImgH,$ImgH,0);
        if (!$success)
        {
            addFault("renderer",1);
            return cleanUpAndDie("GenerateTileset: could not render tileset",$Mode,1,$PID);
        }
        else
        {
            resetFault("renderer");
        }
        # Clean-up the SVG files
        for (my $i = $req->Z ; $i <= $maxzoom; $i++) 
        {
            unlink($Config->get("WorkingDirectory")."output-$parent_pid-z$i.svg") if (!$Config->get("Debug"));
        }

        #if $empty then the next zoom level was empty, so we only upload one tile unless RenderFullTileset is set.
        if ($empty == 1 && $Config->get("GatherBlankTiles")) 
        {
            my $Filename=sprintf("%s_%s_%s_%s.png",$Config->get($layer."_Prefix"), $req->Z, $req->X, $req->Y);
            my $oldFilename = sprintf("%s/%s",$JobDirectory, $Filename); 
            my $newFilename = sprintf("%s%s",$Config->get("WorkingDirectory"),$Filename);
            rename($oldFilename, $newFilename);
            rmdir($JobDirectory);
        }
        else
        {
            # This directory is now ready for upload.
            # How should errors in renaming be handled?
            my $Dir = $JobDirectory;
            $Dir =~ s|\.tmpdir|.dir|;
            rename $JobDirectory, $Dir;
        }

    }

    unlink(@tempfiles) if (!$Config->get("Debug"));

    keepLog($PID,"GenerateTileset","stop",'x='.$req->X.',y='.$req->Y.',z='.$req->Z." for layers ".$req->layers_str);

    return 1;
}

#-----------------------------------------------------------------------------
# Generate SVG for one zoom level
#   $layerDataFile - name of the OSM data file
#   $X, $Y - which tileset (Always the tilenumbers of the base zoom. i.e. z12)
#   $Zoom - which zoom currently is processsed
#   $N, $S, $W, $E - bounds of the tile
#-----------------------------------------------------------------------------
sub GenerateSVG 
{
    my ($layerDataFile, $layer, $X, $Y, $Zoom, $N, $S, $W, $E) = @_;
    # Create a new copy of rules file to allow background update
    # don't need layer in name of file as we'll
    # process one layer after the other
    my $error = 0;
    my $source = $Config->get($layer."_Rules.".$Zoom);
    my $TempFeatures = $Config->get("WorkingDirectory")."map-features-$PID-z$Zoom.xml";
    copy($source, $TempFeatures)
        or die "Cannot make copy of $source";

    # Update the rules file  with details of what to do (where to get data, what bounds to use)
    AddBounds($TempFeatures,$W,$S,$E,$N);
    SetDataSource($layerDataFile, $TempFeatures);

    # Render the file
    if (! xml2svg(
            $TempFeatures,
            $Config->get("WorkingDirectory")."output-$parent_pid-z$Zoom.svg",
            $Zoom))
    {
        $error = 1;
    }
    # Delete temporary rules file
    unlink($TempFeatures) if (! $Config->get("Debug"));
    return $error;
}

#-----------------------------------------------------------------------------
# Render a tile
#   $X, $Y - which tileset (Always the tilenumbers at $ZOrig)
#   $Ytile, $Zoom - which tilestripe
#   $ZOrig, the lowest zoom level which called tileset generation (i.e. z12 for "normal" operation)
#   $N, $S, $W, $E - bounds of the tile
#   $ImgX1,$ImgY1,$ImgX2,$ImgY2 - location of the tile in the SVG file
#   $ImageHeight - Height of the entire SVG in SVG units
#   $empty - put forward "empty" tilestripe information.
#-----------------------------------------------------------------------------
sub RenderTile 
{
    my ($layer, $req, $Ytile, $Zoom, $N, $S, $W, $E, $ImgX1,$ImgY1,$ImgX2,$ImgY2,$ImageHeight,$SkipEmpty) = @_;

    return (1,1) if($Zoom > $Config->get($layer."_MaxZoom"));
    
    # no need to render subtiles if empty
    return (1,$SkipEmpty) if($SkipEmpty == 1);

    # Render it to PNG
    printf "Tilestripe %s (%s,%s): Lat %1.3f,%1.3f, Long %1.3f,%1.3f, X %1.1f,%1.1f, Y %1.1f,%1.1f\n",       $Ytile,$req->X,$req->Y,$N,$S,$W,$E,$ImgX1,$ImgX2,$ImgY1,$ImgY2 if ($Config->get("Debug")); 
    my $Width = 256 * (2 ** ($Zoom - $req->Z));  # Pixel size of tiles  
    my $Height = 256; # Pixel height of tile

    # svg2png returns true if all tiles extracted were empty. this might break 
    # if a higher zoom tile would contain data that is not rendered at the 
    # current zoom level. 
    my ($success,$empty) = svg2png($layer, $req, $Ytile, $Zoom, $Width, $Height,$ImgX1,$ImgY1,$ImgX2,$ImgY2,$ImageHeight);
    if (!$success)
    {
       return (0,$empty);
    }
    if ($empty and !$Config->get($layer."_RenderFullTileset")) 
    {
        $SkipEmpty=1;
    }

    # Get progress percentage 
    if($SkipEmpty == 1) 
    {
        # leap forward because this tile and all higher zoom tiles of it are "done" (empty).
        for (my $j = $Config->get($layer."_MaxZoom"); $j >= $Zoom ; $j--)
        {
            $progress += 2 ** ($Config->get($layer."_MaxZoom")-$j);
        }
    }
    else
    {
        $progress += 1;
    }

    if (($progressPercent=$progress*100/(2**($Config->get($layer."_MaxZoom")-$req->Z+1)-1)) == 100)
    {
        statusMessage("Finished ".$req->X.",".$req->Y." for layer $layer",1,0);
    }
    else
    {
        if ($Config->get("Verbose") >= 10)
        {
            printf STDERR "Job No. %d %1.1f %% done.\n",$progressJobs, $progressPercent;
        }
        else
        {
            statusMessage("Working",0,3);
        }
    }
    
    # Sub-tiles
    my $MercY2 = ProjectF($N); # get mercator coordinates for North border of tile
    my $MercY1 = ProjectF($S); # get mercator coordinates for South border of tile
    my $MercYC = 0.5 * ($MercY1 + $MercY2); # get center of tile in mercator
    my $LatC = ProjectMercToLat($MercYC); # reproject centerline to latlon

    my $ImgYCP = ($MercYC - $MercY1) / ($MercY2 - $MercY1); 
    my $ImgYC = $ImgY1 + ($ImgY2 - $ImgY1) * $ImgYCP;       # find mercator coordinates for bottom/top of subtiles

    my $YA = $Ytile * 2;
    my $YB = $YA + 1;

    if ($Config->get("Fork") && $Zoom >= $req->Z && $Zoom < ($req->Z + $Config->get("Fork")))
    {
        my $pid = fork();
        if (not defined $pid) 
        {
            cleanUpAndDie("RenderTile: could not fork, exiting","EXIT",4,$PID); # exit if asked to fork but unable to
        }
        elsif ($pid == 0) 
        {
            # we are the child process and can't talk to our parent other than through exit codes
            ($success,$empty) = RenderTile($layer, $req, $YA, $Zoom+1, $N, $LatC, $W, $E, $ImgX1, $ImgYC, $ImgX2, $ImgY2,$ImageHeight,$SkipEmpty);
            if ($success)
            {
                exit(0);
            }
            else
            {
                exit(1);
            }
        }
        else
        {
            ($success,$empty) = RenderTile($layer, $req, $YB, $Zoom+1, $LatC, $S, $W, $E, $ImgX1, $ImgY1, $ImgX2, $ImgYC,$ImageHeight,$SkipEmpty);
            waitpid($pid,0);
            my $ChildExitValue = $?; # we don't want the details, only if it exited normally or not.
            if ($ChildExitValue or !$success)
            {
                return (0,$SkipEmpty);
            }
        }
        if ($Zoom == $req->Z)
        {
            $progressPercent=100 if (! $Config->get("Debug")); # workaround for not correctly updating %age in fork, disable in debug mode
            statusMessage("Finished ".$req->X.",".$req->Y." for layer $layer",1,0);
        }
    }
    else
    {
        ($success,$empty) = RenderTile($layer, $req, $YA, $Zoom+1, $N, $LatC, $W, $E, $ImgX1, $ImgYC, $ImgX2, $ImgY2,$ImageHeight,$SkipEmpty);
        return (0,$empty) if (!$success);
        ($success,$empty) = RenderTile($layer, $req, $YB, $Zoom+1, $LatC, $S, $W, $E, $ImgX1, $ImgY1, $ImgX2, $ImgYC,$ImageHeight,$SkipEmpty);
        return (0,$empty) if (!$success);
    }

    return (1,$SkipEmpty); ## main call wants to know wether the entire tileset was empty so we return 1 for success and 1 if the tile was empty
}


#-----------------------------------------------------------------------------
# Gets latest copy of client from svn repository
# returns 1 on perceived success.
#-----------------------------------------------------------------------------
sub UpdateClient # 
{
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

    if ($svn_status eq '')
    {
        my $versionfile = "version.txt";
        DownloadFile($Config->get("VersionCheckURL"), $versionfile ,0);
        return 1;
    }
    else
    {
        statusMessage("svn status did not come back clean, check your installation",1,0);
        print STDERR $svn_status;
        return cleanUpAndDie("Auto-update failed","EXIT",1,$PID);
    }
}

sub NewClientVersion 
{
    return 0 if (time() - $LastTimeVersionChecked < 600);
    my $versionfile = "version.txt";
    my $runningVersion;
    if (open(versionfile, "<", $versionfile))
    {
        $runningVersion = <versionfile>;
        chomp $runningVersion;
        close versionfile;
    }
    elsif (open(versionfile, ">", $versionfile))
    {
        $runningVersion = 0; 
        print versionfile $runningVersion;
        close versionfile;
    }
    else
    {
        die("can't open $versionfile");
    }
    # return 0;

    my $curVerFile = "newversion.txt";
    my $currentVersion;
    
    DownloadFile($Config->get("VersionCheckURL"), $curVerFile ,0);
    if (open(versionfile, "<", $curVerFile))
    {
        $currentVersion = <versionfile>;
        chomp $runningVersion;
        close versionfile;
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
#-----------------------------------------------------------------------------
sub xml2svg 
{
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
        chdir "orp";
        my $Cmd = sprintf("%s perl orp.pl -r %s -o %s",
          $Config->get("Niceness"),
          $MapFeatures,
          $TSVG);

        statusMessage("Transforming zoom level $zoom with or/p",0,3);
        $success = runCommand($Cmd,$PID);
        chdir "..";
    }
    else
    {
        die "invalid Osmarender setting in config";
    }
    if (!$success) {
        statusMessage(sprintf("%s produced an error, aborting render.", $Config->get("Osmarender")),1,0);
        return cleanUpAndDie("xml2svg failed",$Mode,3,$PID);
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
       return cleanUpAndDie("xml2svg failed",$Mode,3,$PID);
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
        unlink($TSVG) if (!$Config->get("Debug"));
    }
    else
    {   # don't do bezier curve hinting
        statusMessage("Bezier Curve hinting disabled.",1,0);
    }
    return 1;
}


#-----------------------------------------------------------------------------
# Render a SVG file
# $ZOrig - the lowest zoom level of the tileset
# $X, $Y - tilemnumbers of the tileset
# $Ytile - the actual tilenumber in Y-coordinate of the zoom we are processing
#-----------------------------------------------------------------------------
sub svg2png
{
    my($layer, $req, $Ytile, $Zoom, $SizeX, $SizeY, $X1, $Y1, $X2, $Y2, $ImageHeight) = @_;
    
    my $TempFile;
    my $stdOut;
    my $TempDir = $Config->get("WorkingDirectory") . $PID . "/"; # avoid upload.pl looking at the wrong PNG (Regression caused by batik support)
    if (! -e $TempDir ) 
    {
        mkdir($TempDir) or cleanUpAndDie("cannot create working directory $TempDir","EXIT",3,$PID);
    }
    elsif (! -d $TempDir )
    {
        cleanUpAndDie("could not use $TempDir: is not a directory","EXIT",3,$PID);
    }
    (undef, $TempFile) = tempfile($PID."_part-XXXXXX", DIR => $TempDir, SUFFIX => ".png", OPEN => 0);
    (undef, $stdOut) = tempfile("$PID-XXXXXX", DIR => $Config->get("WorkingDirectory"), SUFFIX => ".stdout", OPEN => 0);

    
    my $Cmd = "";
    
    my $Left = $X1;
    my $Top = $ImageHeight - $Y2;
    my $Width = $X2 - $X1;
    my $Height = $Y2 - $Y1;
    
    my $svgFile = "output-$parent_pid-z$Zoom.svg";

    if ($Config->get("Batik") == "1") # batik as jar
    {
        $Cmd = sprintf("%s%s java -Xms256M -Xmx%s -jar %s -w %d -h %d -a %f,%f,%f,%f -m image/png -d \"%s\" \"%s%s\" > %s", 
        $Config->get("i18n") ? "LC_ALL=C " : "",
        $Config->get("Niceness"),
        $Config->get("BatikJVMSize"),
        $Config->get("BatikPath"),
        $SizeX,
        $SizeY,
        $Left,$Top,$Width,$Height,
        $TempFile,
        $Config->get("WorkingDirectory"),
        $svgFile,
        $stdOut);
    }
    elsif ($Config->get("Batik") == "2") # batik as executable (wrapper of some sort, i.e. on gentoo)
    {
        $Cmd = sprintf("%s%s \"%s\" -w %d -h %d -a %f,%f,%f,%f -m image/png -d \"%s\" \"%s%s\" > %s",
        $Config->get("i18n") ? "LC_ALL=C " : "",
        $Config->get("Niceness"),
        $Config->get("BatikPath"),
        $SizeX,
        $SizeY,
        $Left,$Top,$Width,$Height,
        $TempFile,
        $Config->get("WorkingDirectory"),
        $svgFile,
        $stdOut);
    }
    elsif ($Config->get("Batik") == "3") # agent
    {
        $Cmd = sprintf("svg2png\nwidth=%d\nheight=%d\narea=%f,%f,%f,%f\ndestination=%s\nsource=%s%s\nlog=%s\n\n", 
        $SizeX,
        $SizeY,
        $Left,$Top,$Width,$Height,
        $TempFile,
        $Config->get("WorkingDirectory"),
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

        $Cmd = sprintf("%s%s \"%s\" -z -w %d -h %d --export-area=%f:%f:%f:%f --export-png=\"%s\" \"%s%s\" > %s", 
        $Config->get("i18n") ? "LC_ALL=C " : "",
        $Config->get("Niceness"),
        $Config->get("Inkscape"),
        $SizeX,
        $SizeY,
        $X1,$Y1,$X2,$Y2,
        $TempFile,
        $Config->get("WorkingDirectory"),
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
    if (!$commandResult or ! -e $TempFile )
    {
        statusMessage("$Cmd failed",1,0);
        if ($Config->get("Batik") == "3" && !getBatikStatus())
        {
            statusMessage("Batik agent is not running, use $0 startBatik to start batik agent\n",1,0);
        }
        $req->putBackToServer("BadSVG");
        addFault("inkscape",1);
        $req->is_unrenderable(1);
        cleanUpAndDie("svg2png failed",$Mode,3,$PID);
        return (0,0);
    }
    resetFault("inkscape"); # reset to zero if inkscape succeeds at least once
    unlink($stdOut) if (not $Config->get("Debug"));
    
    my $ReturnValue = splitImageX($layer, $req, $Zoom, $Ytile, $TempFile); # returns true if tiles were all empty
    
    unlink($TempFile) if (not $Config->get("Debug"));
    rmdir ($TempDir);
    return (1,$ReturnValue); #return true if empty
}


sub writeToFile 
{
    open(my $fp, ">", shift()) || return;
    print $fp shift();
    close $fp;
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
# Temporary filename to store a tile
#-----------------------------------------------------------------------------
sub tileFilename 
{
    my($layer,$X,$Y,$Zoom) = @_;
    return(sprintf($Config->get("LocalSlippymap") ? "%s/%s/%d/%d/%d.png" : "%s/%s_%d_%d_%d.png",
        $Config->get("LocalSlippymap") ? $Config->get("LocalSlippymap") : $JobDirectory,
        $Config->get($layer."_Prefix"),
        $Zoom,
        $X,
        $Y));
}

#-----------------------------------------------------------------------------
# Split a tileset image into tiles
#-----------------------------------------------------------------------------
sub splitImageX 
{
    my ($layer, $req, $Z, $Ytile, $File) = @_;
  
    # Size of tiles
    my $Pixels = 256;
  
    # Number of tiles
    my $Size = 2 ** ($Z - $req->Z);

    # Assume the tileset is empty by default
    my $allempty=1;
  
    # Load the tileset image
    statusMessage(sprintf("Splitting %s (%d x 1)", $File, $Size),0,3);
    my $Image = newFromPng GD::Image($File);
    if( not defined $Image )
    {
        print STDERR "\nERROR: Failed to read in file $File\n";
        $req->putBackToServer("MissingFile");
        cleanUpAndDie("SplitImageX:MissingFile encountered, exiting","EXIT",4,$PID);
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
        my $Filename = tileFilename($layer, $req->X * $Size + $xi, $Ytile, $Z);
        MagicMkdir($Filename) if ($Config->get("LocalSlippymap"));
   
        # Temporary filename
        my $Filename2_suffix = ".cut";
        my $Filename2 = $Filename.$Filename2_suffix;
        my $Basename = $Filename;   # used for statusMessage()
        $Basename =~ s|.*/||;

        # Check for black tile output
        if (not ($SubImage->compare($BlackTileImage) & GD_CMP_IMAGE)) 
        {
            print STDERR "\nERROR: Your inkscape has just produced a totally black tile. This usually indicates a broken Inkscape, please upgrade.\n";
            $req->putBackToServer("BlackTile");
            cleanUpAndDie("SplitImageX:BlackTile encountered, exiting","EXIT",4,$PID);
        }
        # Detect empty tile here:
        elsif (not($SubImage->compare($EmptyLandImage) & GD_CMP_IMAGE)) # libGD comparison returns true if images are different. (i.e. non-empty Land tile) so return the opposite (false) if the tile doesn''t look like an empty land tile
        {
            copy("emptyland.png", $Filename);
        }
        elsif (not($SubImage->compare($EmptySeaImage) & GD_CMP_IMAGE)) # same for Sea tiles
        {
	    copy("emptysea.png", $Filename);
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
            statusMessage(" -> $Basename",0,10);
            WriteImage($SubImage,$Filename);
#-----------------------------------------------------------------------------
# Run pngcrush on each split tile, then delete the temporary cut file
#-----------------------------------------------------------------------------
            my $Redirect = ">/dev/null";
            my $Cmd;
            if ($^O eq "MSWin32")
            {
                $Redirect = "";
            }

            if ($Config->get($layer."_Transparent"))
            {
                rename($Filename, $Filename2);
            }
            elsif ($Config->get("PngQuantizer") eq "pngnq") {
                if ($EnvironmentInfo{"pngnq"})
                {
                    $Cmd = sprintf("%s \"%s\" -e .png%s -s1 -n256 %s %s",
                                   $Config->get("Niceness"),
                                   $Config->get("pngnq"),
                                   $Filename2_suffix,
                                   $Filename,
                                   $Redirect);

                    statusMessage("ColorQuantizing $Basename",0,6);
                    if(runCommand($Cmd,$PID))
                    {
                        unlink($Filename);
                    }
                    else
                    {
                        statusMessage("ColorQuantizing $Basename with ".$Config->get("PngQuantizer")." failed",1,0);
                        rename($Filename, $Filename2);
                    }
                }
                else
                {
                    statusMessage("ColorQuantizing $Basename with \"".$Config->get("PngQuantizer")."\" failed, pngnq not installed?",1,0);
                    rename($Filename, $Filename2);
                }
            } else {
                rename($Filename, $Filename2);
            }

            if ($Config->get("PngOptimizer") eq "pngcrush")
            {
                $Cmd = sprintf("%s \"%s\" -q %s %s %s",
                  $Config->get("Niceness"),
                  $Config->get("Pngcrush"),
                  $Filename2,
                  $Filename,
                  $Redirect);
            }
            elsif ($Config->get("PngOptimizer") eq "optipng")
            {
                $Cmd = sprintf("%s \"%s\" %s -out %s %s", #no quiet, because it even suppresses error output
                  $Config->get("Niceness"),
                  $Config->get("Optipng"),
                  $Filename2,
                  $Filename,
                  $Redirect);
            }
            else
            {
                cleanUpAndDie("SplitImageX:PngOptimizer not configured, exiting (should not happen, update from svn, and check config file)","EXIT",4,$PID);
            }
            statusMessage("Optimizing $Basename",0,6);
            if(runCommand($Cmd,$PID))
            {
                unlink($Filename2);
            }
            else
            {
                statusMessage("Optimizing $Basename with ".$Config->get("PngOptimizer")." failed",1,0);
                rename($Filename2, $Filename);
            }
        }
        # Assign the job time to this file
        utime $JobTime, $JobTime, $Filename;
    }
    undef $SubImage;
    undef $Image;
    # tell the rendering queue wether the tiles are empty or not
    return $allempty;
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
    open (my $fp, ">$Filename") || cleanUpAndDie("WriteImage:could not open file for writing, exiting","EXIT",3,$PID);
    binmode $fp;
    print $fp $png_data;
    close $fp;
}

# sub MagicMkdir moved to tahlib.pm

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
    if (!defined($dirent))
    {
        $dirent = $de; 
        return;
    }
    elsif ($dirent ne $de)
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
    # until proven to work with other systems, only attempt a re-exec
    # on linux. 
    return unless ($^O eq "linux" || $^O eq "cygwin");

    statusMessage("tilesGen.pl has changed, re-start new version",1,0);
    if ($Config->get("ForkForUpload") && $child_pid != -1)  ## FIXME: make more general
    {
        statusMessage("Waiting for child process",0,0);
        waitpid($child_pid, 0);
    }
    exec "perl", $0, $Mode, "reexec", 
        "progressJobs=" . $progressJobs, 
        "idleSeconds=" . getIdle(1), 
        "idleFor=" . getIdle(0), 
        "progstart=" . $progstart  or die("could not reExec");
}


sub startBatikAgent
{
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
        cleanUpAndDie("Fatal error occurred during loop, exiting","EXIT",1,$PID);
    }
    elsif (getFault("inkscape") > 5) {
        cleanUpAndDie("Five times inkscape failed, exiting","EXIT",1,$PID);
    }
    elsif (getFault("renderer") > 10) {
        cleanUpAndDie("rendering a tileset failed 10 times in a row, exiting","EXIT",1,$PID);
    }
    elsif (getFault("upload") > 5) {
        cleanUpAndDie("Five times the upload failed, perhaps the server doesn't like us, exiting","EXIT",1,$PID);
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
        if ($numfaults > 5) {
            cleanUpAndDie("More than five times no data, perhaps the server doesn't like us, exiting","EXIT",1,$PID);
        }
        else {
            $sleepdelay=16*(4**$numfaults); # wait 64, 256, 1024, 4096, 16384 seconds. for a total of about 6 hours
            $sleepdelay=int($sleepdelay)+1;
            talkInSleep($numfaults." times no data", $sleepdelay);
        }
    }
    if (getFault("nodataXAPI") > 0) {
        my $numfaults=getFault("nodataXAPI");
        if ($numfaults >= 20) {
            cleanUpAndDie("20 times no data from XAPI, perhaps the server doesn't like us, exiting","EXIT",1,$PID); # allow XAPI more leeway
        }
        else {
            $sleepdelay=16*(2**$numfaults); # wait 32, 64, 128, 256, 512, 1024, 4096, 8192, 14400, 14400, 14400... seconds.
            $sleepdelay=int($sleepdelay)+1;
            $sleepdelay=14400 if ($sleepdelay > 14400);
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

