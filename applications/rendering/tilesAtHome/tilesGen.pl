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
use English '-no_match_vars';
use GD qw(:DEFAULT :cmp);
use AppConfig qw(:argcount);
use locale;
use POSIX qw(locale_h);

#---------------------------------

# Read the config file
our $Config = AppConfig->new({
                CREATE => 1,                      # Autocreate unknown config variables
                GLOBAL => {
                  DEFAULT  => undef,    # Create undefined Variables by default
                  ARGCOUNT => ARGCOUNT_ONE, # Simple Values (no arrays, no hashmaps)
                }
              });

$Config->define("help|usage!");
$Config->define("nodownload=s");
$Config->set("nodownload",0);
$Config->file("config.defaults", "layers.conf", "tilesAtHome.conf", "authentication.conf"); #first read configs in order, each (possibly) overwriting settings from the previous
$Config->args();              # overwrite config options with command line options
$Config->file("general.conf");  # overwrite with hardcoded values that must not be changed
ApplyConfigLogic($Config);

# Handle the command-line
my $Mode = shift();
my $LoopMode = (($Mode eq "loop") or ($Mode eq "upload_loop")) ? 1 : 0;
my $RenderMode = (($Mode eq "") or ($Mode eq "xy") or ($Mode eq "loop")) ? 1 : 0;
my $UploadMode = (($Mode eq "upload") or ($Mode eq "upload_conditional") or ($Mode eq "upload_loop")) ? 1 : 0;
my %EnvironmentInfo;

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

my $Layers = $Config->get("Layers");

# Get version number from version-control system, as integer
my $Version = '$Revision$';
$Version =~ s/\$Revision:\s*(\d+)\s*\$/$1/;
printf STDERR "This is version %d (%s) of tilesgen running on %s, ID: %s\n", 
    $Version, $Config->get("ClientVersion"), $^O, GetClientId();

# Keep track of unrenderable tiles. 
# This should not be saved, as they may render later. 
# there also might be false positives due to mangled inkscape preference file.
my %unrenderable;

# set the progress indicator variables
my $currentSubTask;
my $progress = 0;
my $progressJobs = 0;
my $progressPercent = 0;

# keep track of time running
my $progstart = time();
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
            print STDERR "$evalstr\n" if ($Config->get("Verbose"));
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

    # Check the on disk image tiles havn't been corrupted.
    # these are flagfiles that tell the server certain metainfo through their filesize.
    if((-s "emptyland.png" != 67) or (-s "emptysea.png" != 69)) {
        statusMessage("Corruption detected in empty land/sea tile", $currentSubTask, $progressJobs, $progressPercent,1);
        UpdateClient();
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

my $upload_result = 0;

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
resetFault("requestUnrenderable");

killafile("stopfile.txt") if $Config->get("AutoResetStopfile");


## Start processing

if ($Mode eq "xy")
{
    # ----------------------------------
    # "xy" as first argument means you want to specify a tileset to render
    # ----------------------------------

    my $X = shift();
    my $Y = shift();
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
       statusMessage(" *** No zoomlevel specified! Assuming z12 *** ", "warning", $progressJobs, $progressPercent,1);
    }
    GenerateTileset($X, $Y, $Zoom);
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
                statusMessage("Waiting for previous upload process", $currentSubTask, $progressJobs, $progressPercent,0);
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

        ## start processing here:

        my ($did_something, $message) = ProcessRequestsFromServer(); # Actually render stuff if job on server

        $upload_result = compressAndUploadTilesets(); # upload if enough work done

        if ($upload_result)  # we got an error in the upload process
        {
              addFault("upload",1); # we only track errors that occur multple times in a row
        }
        else
        {
              resetFault("upload"); #reset fault counter for uploads if once without error
        }

        if ($did_something == 0) 
        {
            talkInSleep($message, 60);
        }
        else
        {
            setIdle(0,0);
        }
    }
}
elsif ($Mode eq "upload" or $Mode eq "upload_conditional") 
{
    $currentSubTask = "warning";
    statusMessage("don't run this parallel to another tilesGen.pl instance", $currentSubTask, $progressJobs, $progressPercent,1);
    compressAndUpload();
}
elsif ($Mode eq "upload_loop")
{
    statusMessage("don't run this parallel to another tilesGen.pl instance", $currentSubTask, $progressJobs, $progressPercent,1);
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
            $upload_result = upload(); # only uploading ZIP files here
            
            if ($upload_result)  # we got an error in the upload process
            {
                addFault("upload",1); # we only track errors that occur multple times in a row
            }
            else
            {
                resetFault("upload"); #reset fault counter for uploads if once without error
                statusMessage("upload finished", $currentSubTask, $progressJobs, $progressPercent,1);
                $progressJobs++;
            }
            $startTime = time();
        }
        else
        {
            $elapsedTime = time() - $startTime;
            statusMessage(sprintf("waiting for new ZIP files to upload   %d:%02d", $elapsedTime/60, $elapsedTime%60), $currentSubTask, $progressJobs, $progressPercent,0);
            sleep(1);
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
        statusMessage("stop signal was sent to the currently running tilesGen.pl", $currentSubTask, $progressJobs, $progressPercent,1);
        statusMessage("please note that it may take quite a while for it to exit", $currentSubTask, $progressJobs, $progressPercent,1);
    }
    else
    {
        statusMessage("stop signal was NOT sent to the currently running tilesGen.pl - stopfile.txt could NOT be created", $currentSubTask, $progressJobs, $progressPercent,1);
    }
 #   talkInSleep("you may safely press Ctrl-C now if you ran this as \"tilesGen.pl\" from the command line", 60);
    exit(1);
}
elsif ($Mode eq "update") 
{
    UpdateClient();
}
elsif ($Mode eq "") 
{
    # ----------------------------------
    # Normal mode downloads request from server
    # ----------------------------------

    my ($did_something, $message) = ProcessRequestsFromServer();
    
    if (! $did_something)
    {
        statusMessage("you may safely press Ctrl-C now if you ran this as \"tilesGen.pl\" from the command line.", $currentSubTask, $progressJobs, $progressPercent,1);
        talkInSleep($message, 60);
    }
    statusMessage("if you want to run this program continuously, use loop mode", $currentSubTask, $progressJobs, $progressPercent,1);
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
            statusMessage("Waiting for previous upload process to finish", $currentSubTask, $progressJobs, $progressPercent,0);
            waitpid($upload_pid, 0);
            $upload_result = $? >> 8;
        }
        compress(); #compress before fork so we don't get temp files mangled. Workaround for batik support.
        $upload_pid = fork();
        if ((not defined $upload_pid) or ($upload_pid == -1))
        {
            cleanUpAndDie("loop: could not fork, exiting","EXIT",4,$PID); # exit if asked to fork but unable to
        }
        elsif ($upload_pid == 0)
        {
            ## we are the child, so we run the upload
            my $res = upload(); # upload if enough work done
            exit($res);
        }
    }
    else
    {
        ## no forking going on
        return compressAndUpload();
    }
    return 0; # no error, just nothing to upload
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

    my $UploadScript = "perl $Bin/upload.pl $progressJobs";
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
    
    my $ValidFlag;
    my $Version;
    my $TilesetLastModified; # Unix timestamp of tileset on server
    my $TilesetComplexity;   # tileset complexity. still unused.
    my $X;
    my $Y;
    my $Z;
    
    # ----------------------------------
    # Download the request, and check it
    # Note: to find out exactly what this server is telling you, 
    # add ?help to the end of the URL and view it in a browser.
    # It will give you details of other help pages available,
    # such as the list of fields that it's sending out in requests
    # ----------------------------------

    for (;;) 
    {
        my $Request = GetRequestFromServer($Config->get("RequestMethod"));

        return (0, "Error reading request from server") unless ($Request);
        
        ($ValidFlag,$Version) = split(/\|/, $Request);
        
        # Check what format the results were in
        # If you get this message, please do check for a new version, rather than
        # commenting-out the test - it means the field order has changed and this
        # program no longer makes sense!

        ## it is also important that we check the field that we *think* has the version first, before attempting anything else. 

        if ($Version < 4 or $Version > 5)
        {
            print STDERR "\n";
            print STDERR "Server is speaking a different version of the protocol to us.\n";
            print STDERR "Check to see whether a new version of this program was released!\n";
            cleanUpAndDie("ProcessRequestFromServer:Request API version mismatch, exiting \n".$Request,"EXIT",1,$PID);
            ## No need to return, we exit the program at this point
        }
        elsif ($Version == 4)
        {
            ($ValidFlag,$Version,$X,$Y,$Z,$Layers) = split(/\|/, $Request);
        }
        elsif ($Version == 5)
        {
            ($ValidFlag,$Version,$X,$Y,$Z,$Layers,$TilesetLastModified,$TilesetComplexity) = split(/\|/, $Request);
        }
        else
        {
            die "Version is \"".$Version."\". This should not have happened.";
        }
        
        # First field is always "OK" if the server has actually sent a request
        if ($ValidFlag eq "XX")
        {
            if ($Request =~ /Invalid username/)
            {
                die "ERROR: Authentication failed - please check your username "
                        . "and password in 'authentication.conf'.\n\n"
                        . "! If this worked just yesterday, you now need to put your osm account e-mail and password there.";
            }
            elsif ($Request =~ /Invalid client version/)
            {
                die "ERROR: This client version (".$Config->get("ClientVersion").") was not accepted by the server.";  ## this should never happen as long as auto-update works
            }
            elsif ($ValidFlag ne "OK")
            {
                return (0, "Unknown server response");
            }
        
        }
        last unless ($unrenderable{"$X $Y $Z"});
        $unrenderable{"$X $Y $Z"}++;

        PutRequestBackToServer($X,$Y,$Z,"Unrenderable");

        # make sure we don't loop like crazy should we get another or the same unrenderable tile back over and over again
        my $UnrenderableBackoff = addFault("requestUnrenderable",1); 
        $UnrenderableBackoff = int(1.8 ** $UnrenderableBackoff);
        $UnrenderableBackoff = 300 if ($UnrenderableBackoff > 300);
        talkInSleep("Ignoring unrenderable tile $X $Y $Z",$UnrenderableBackoff);
    }
    
    # Information text to say what's happening
    statusMessage("Got work from the server", $currentSubTask, $progressJobs, $progressPercent,0);
    
    resetFault("requestUnrenderable"); #reset if we actually start trying to render a tileset.

    # Create the tileset requested
    GenerateTileset($X, $Y, $Z);
    return (1, "");
}


# actually get the request from the server
sub GetRequestFromServer
{
    my $RequestMethod=shift();
    my $LocalFilename = $Config->get("WorkingDirectory") . "request-" . $PID . ".txt";
    killafile($LocalFilename); ## make sure no old request file is laying around.

    my $Request;

    if ($RequestMethod eq "POST")
    {
        my $URL = $Config->get("RequestURL");
    
        my $ua = LWP::UserAgent->new(keep_alive => 1, timeout => 360);

        $ua->protocols_allowed( ['http'] );
        $ua->agent("tilesAtHome");
        $ua->env_proxy();
        push @{ $ua->requests_redirectable }, 'POST';

        my $res = $ua->post($URL,
          Content_Type => 'form-data',
          Content => [ user => $Config->get("UploadUsername"),
                       passwd => $Config->get("UploadPassword"),
                       version => $Config->get("ClientVersion"),
                       layers => $Layers,
                       layerspossible => $Config->get("LayersCapability"),
                       client_id => GetClientId() ]);
      
        if(!$res->is_success())
        {
            print $res->content if ($Config->get("Debug"));
            return 0;
        }
        else
        {
            print $res->content if ($Config->get("Debug"));
            $Request = $res->content;  ## FIXME: check single line returned. grep?
            chomp $Request;
        }

    }
    else
    {
        return 0;
    }
    return $Request;
}

#-----------------------------------------------------------------------------
# this is called when the client encounters errors in processing a tileset,
# it's designed to tell the server the tileset will not be returned because
# of said error
#-----------------------------------------------------------------------------
sub PutRequestBackToServer 
{
    ## TODO: will not be called in some libGD abort situations
    my ($X,$Y,$Z,$Cause) = @_;

    ## do not do this if called in xy mode!
    return if($Mode eq "xy");
    
    my $ua = LWP::UserAgent->new(keep_alive => 1, timeout => 360);

    $ua->protocols_allowed( ['http'] );
    $ua->agent("tilesAtHomeZip");
    $ua->env_proxy();
    push @{ $ua->requests_redirectable }, 'POST';

    statusMessage("Putting Job ".$X." ".$Y." ".$Z." back to server", $currentSubTask, $progressJobs, $progressPercent,1);
    my $res = $ua->post($Config->get("ReRequestURL"),
              Content_Type => 'form-data',
              Content => [ x => $X,
                           y => $Y,
                           min_z => $Z,
                           user => $Config->get("UploadUsername"),
                           passwd => $Config->get("UploadPassword"),
                           version => $Config->get("ClientVersion"),
                           cause => $Cause,
                           client_id => GetClientId() ]);

    if(!$res->is_success())
    {
        return (0, "Error reading response from server");
    }
    
    talkInSleep("Waiting before new tile is requested", 10);
}

#-----------------------------------------------------------------------------
# Render a tile (and all subtiles, down to a certain depth)
#-----------------------------------------------------------------------------
sub GenerateTileset ## TODO: split some subprocesses to own subs
{
    my ($X, $Y, $Zoom) = @_;
    
    keepLog($PID,"GenerateTileset","start","x=$X,y=$Y,z=$Zoom for layers $Layers");
    
    my ($N, $S) = Project($Y, $Zoom);
    my ($W, $E) = ProjectL($X, $Zoom);
    
    $progress = 0;
    $progressPercent = 0;
    $progressJobs++;
    $currentSubTask = "jobinit";
    
    statusMessage(sprintf("Doing tileset $X,$Y (zoom $Zoom) (area around %f,%f)", ($N+$S)/2, ($W+$E)/2), $currentSubTask, $progressJobs, $progressPercent, 1);
    
    my $maxCoords = (2 ** $Zoom - 1);
    
    if ( ($X < 0) or ($X > $maxCoords) or ($Y < 0) or ($Y > $maxCoords) )
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
    
    killafile($DataFile);
    my $URLS = sprintf("%s%s/map?bbox=%s",
      $Config->get("APIURL"),$Config->get("OSMVersion"),$bbox);
    if ($Zoom < 12) 
    {
        # FIXME: zoom 12 hardcoded: assume lowzoom layer now!
        # only in xy mode since in loop mode a different method that does not depend on hardcoded zoomlevel will be used, where the layer is set by the server.
        $Layers="lowzoom" if ($Mode eq "xy");
        
        # Get the predicates for lowzoom, and build the URLS for them
        my $predicates = $Config->get($Layers."_Predicates");
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
        statusMessage("Downloading: Map data for $Layers to $partialFile", $currentSubTask, $progressJobs, $progressPercent,0);
        print "Download\n$URL\n" if ($Config->get("Debug"));
        my $res = DownloadFile($URL, $partialFile, 0);

        if (! $res)
        {
            if ($Zoom < 12)
            {
                statusMessage("No data here...", $currentSubTask, $progressJobs, $progressPercent, 1);
                # if loop was requested just return  or else exit with an error. 
                # (to enable wrappers to better handle this situation 
                # i.e. tell the server the job hasn't been done yet)
                PutRequestBackToServer($X,$Y,$Zoom,"NoData");
                foreach my $file(@tempfiles) { killafile($file); }
                addFault("nodataXAPI",1);
                return cleanUpAndDie("GenerateTileset: no data!",$Mode,1,$PID);
            }
            elsif ($Config->get("FallBackToXAPI"))
            {
                statusMessage("No data here, trying OSMXAPI", $currentSubTask, $progressJobs, $progressPercent, 1);
                $bbox = $URL;
                $bbox =~ s/.*bbox=//;
                $URL=sprintf("%s%s/%s[bbox=%s] ",
                    $Config->get("XAPIURL"),
                    $Config->get("OSMVersion"),
                    "*",
                    $bbox);
                statusMessage("Downloading: Map data for $Layers to $partialFile", $currentSubTask, $progressJobs, $progressPercent,0);
                print "Download\n$URL\n" if ($Config->get("Debug"));
                my $res = DownloadFile($URL, $partialFile, 0);
                if (! $res)
                {
                    statusMessage("No data on OSMXAPI either...", $currentSubTask, $progressJobs, $progressPercent, 1);
                    PutRequestBackToServer($X,$Y,$Zoom,"NoData");
                    foreach my $file(@tempfiles) { killafile($file); }
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
                statusMessage("No data here, trying smaller slices", $currentSubTask, $progressJobs, $progressPercent, 1);
                my $slice=(($E1-$W1)/10); # A chunk is one tenth of the width 
                for (my $j = 1 ; $j<=10 ; $j++)
                {
                    $URL = sprintf("%s%s/map?bbox=%f,%f,%f,%f", 
                      $Config->get("APIURL"),$Config->get("OSMVersion"), ($W1+($slice*($j-1))), $S1, ($W1+($slice*$j)), $N1); 
                    $partialFile = $Config->get("WorkingDirectory")."data-$PID-$i-$j.osm";
                    push(@{$filelist}, $partialFile);
                    push(@tempfiles, $partialFile);
                    statusMessage("Downloading: Map data to $partialFile (slice $j of 10)", $currentSubTask, $progressJobs, $progressPercent,0);
                    print "Download\n$URL\n" if ($Config->get("Debug"));
                    $res = DownloadFile($URL, $partialFile, 0);

                    if (! $res)
                    {
                        statusMessage("No data here (sliced)...", $currentSubTask, $progressJobs, $progressPercent, 1);
                        PutRequestBackToServer($X,$Y,$Zoom,"NoData");
                        foreach my $file(@tempfiles) { killafile($file); }
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
            if ($Zoom < 12) ## FIXME: hardcoded zoom
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
    statusMessage("Checking for UTF-8 errors in $DataFile", $currentSubTask, $progressJobs, $progressPercent, 0);
    open(OSMDATA, $DataFile) || die ("could not open $DataFile for UTF-8 check");
    my @toCheck = <OSMDATA>;
    close(OSMDATA);
    while (my $osmline = shift @toCheck)
    {
      if (utf8::is_utf8($osmline)) # this might require perl 5.8.1 or an explicit use statement
      {
        statusMessage("found incorrect UTF-8 chars in $DataFile, job $X $Y  $Zoom", $currentSubTask, $progressJobs, $progressPercent, 1);
        PutRequestBackToServer($X,$Y,$Zoom,"BadUTF8");
        addFault("utf8",1);
        return cleanUpAndDie("GenerateTileset:UTF8 test failed",$Mode,1,$PID);
      }
    }
    resetFault("utf8"); #reset to zero if no UTF8 errors found.
    #------------------------------------------------------
    # Handle all layers, one after the other
    #------------------------------------------------------

    foreach my $layer(split(/,/, $Layers))
    {
        #reset progress for each layer
        $progress=0;
        $progressPercent=0;
        $currentSubTask = $layer;
        
        $JobDirectory = sprintf("%s%s_%d_%d_%d.tmpdir",
                                $Config->get("WorkingDirectory"),
                                $Config->get($layer."_Prefix"),
                                $Zoom, $X, $Y);
        mkdir $JobDirectory unless -d $JobDirectory;

        my $maxzoom = $Config->get($layer."_MaxZoom");
        my $layerDataFile;

        # Faff around
        for (my $i = $Zoom ; $i <= $maxzoom ; $i++) 
        {
            killafile($Config->get("WorkingDirectory")."output-$parent_pid-z$i.svg");
        }
        
        my $Margin = " " x ($Zoom - 8);
        printf "%03d %s%d,%d: %1.2f - %1.2f, %1.2f - %1.2f\n", $Zoom, $Margin, $X, $Y, $S,$N, $W,$E if ($Config->get("Debug"));
        
        
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
                statusMessage("Running maplint", $currentSubTask, $progressJobs, $progressPercent,0);
                runCommand($Cmd,$PID);
                $Cmd = sprintf("%s \"%s\" tr %s %s > \"%s\"",
                        $Config->get("Niceness"),
                        $Config->get("XmlStarlet"),
                        "maplint/lib/convert-to-tags.xsl",
                        "tmp.$PID",
                        "$outputFile");
                statusMessage("Creating tags from maplint", $currentSubTask, $progressJobs, $progressPercent,0);
                runCommand($Cmd,$PID);
                killafile("tmp.$PID");
            }
            elsif ($preprocessor eq "close-areas")
            {
                my $Cmd = sprintf("%s perl close-areas.pl $X $Y $Zoom < %s > %s",
                        $Config->get("Niceness"),
                        "$inputFile",
                        "$outputFile");
                statusMessage("Running close-areas", $currentSubTask, $progressJobs, $progressPercent,0);
                runCommand($Cmd,$PID);
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
            my $minimum_zoom = $Zoom;
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
                        if (GenerateSVG($layerDataFile, $layer, $X, $Y, $i, $N, $S, $W, $E)) # if true then error occured
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
                if (GenerateSVG($layerDataFile, $layer, $X, $Y, $i, $N, $S, $W, $E))
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
                foreach my $file(@tempfiles) { killafile($file) if (!$Config->get("Debug")); }
                PutRequestBackToServer($X,$Y,$Zoom,"RenderFailure");
                addFault("renderer",1);
                return 0;
            }
        }
        else
        {
            for (my $i = $Zoom ; $i <= $maxzoom; $i++)
            {
                if (GenerateSVG($layerDataFile, $layer, $X, $Y, $i, $N, $S, $W, $E))
                {
                    foreach my $file(@tempfiles) { killafile($file) if (!$Config->get("Debug")); }
                    PutRequestBackToServer($X,$Y,$Zoom,"RenderFailure");
                    addFault("renderer",1);
                    return 0;
                }
            }
        }
        
        # Find the size of the SVG file
        my ($ImgH,$ImgW,$Valid) = getSize($Config->get("WorkingDirectory")."output-$parent_pid-z$maxzoom.svg");

        # Render it as loads of recursive tiles
        my ($success,$empty) = RenderTile($layer, $X, $Y, $Y, $Zoom, $Zoom, $N, $S, $W, $E, 0,0,$ImgW,$ImgH,$ImgH,0);
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
        for (my $i = $Zoom ; $i <= $maxzoom; $i++) 
        {
            killafile($Config->get("WorkingDirectory")."output-$parent_pid-z$i.svg") if (!$Config->get("Debug"));
        }

        #if $empty then the next zoom level was empty, so we only upload one tile unless RenderFullTileset is set.
        if ($empty == 1 && $Config->get("GatherBlankTiles")) 
        {
            my $Filename=sprintf("%s_%s_%s_%s.png",$Config->get($layer."_Prefix"), $Zoom, $X, $Y);
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

    foreach my $file(@tempfiles) { killafile($file) if (!$Config->get("Debug")); }

    keepLog($PID,"GenerateTileset","stop","x=$X,y=$Y,z=$Zoom for layers $Layers");

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
    killafile($TempFeatures) if (! $Config->get("Debug"));
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
    my ($layer, $X, $Y, $Ytile, $Zoom, $ZOrig, $N, $S, $W, $E, $ImgX1,$ImgY1,$ImgX2,$ImgY2,$ImageHeight,$SkipEmpty) = @_;

    return (1,1) if($Zoom > $Config->get($layer."_MaxZoom"));
    
    # no need to render subtiles if empty
    return (1,$SkipEmpty) if($SkipEmpty == 1);

    # Render it to PNG
    printf "Tilestripe %s (%s,%s): Lat %1.3f,%1.3f, Long %1.3f,%1.3f, X %1.1f,%1.1f, Y %1.1f,%1.1f\n",       $Ytile,$X,$Y,$N,$S,$W,$E,$ImgX1,$ImgX2,$ImgY1,$ImgY2 if ($Config->get("Debug")); 
    my $Width = 256 * (2 ** ($Zoom - $ZOrig));  # Pixel size of tiles  
    my $Height = 256; # Pixel height of tile

    # svg2png returns true if all tiles extracted were empty. this might break 
    # if a higher zoom tile would contain data that is not rendered at the 
    # current zoom level. 
    my ($success,$empty) = svg2png($Zoom, $ZOrig, $layer, $Width, $Height,$ImgX1,$ImgY1,$ImgX2,$ImgY2,$ImageHeight,$X,$Y,$Ytile);
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

    if (($progressPercent=$progress*100/(2**($Config->get($layer."_MaxZoom")-$ZOrig+1)-1)) == 100)
    {
        statusMessage("Finished $X,$Y for layer $layer", $currentSubTask, $progressJobs, $progressPercent, 1);
    }
    else
    {
        if ($Config->get("Verbose"))
        {
            printf STDERR "Job No. %d %1.1f %% done.\n",$progressJobs, $progressPercent;
        }
        else
        {
            statusMessage("Working", $currentSubTask, $progressJobs, $progressPercent,0);
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

    if ($Config->get("Fork") && $Zoom >= $ZOrig && $Zoom < ($ZOrig + $Config->get("Fork")))
    {
        my $pid = fork();
        if (not defined $pid) 
        {
            cleanUpAndDie("RenderTile: could not fork, exiting","EXIT",4,$PID); # exit if asked to fork but unable to
        }
        elsif ($pid == 0) 
        {
            # we are the child process and can't talk to our parent other than through exit codes
            ($success,$empty) = RenderTile($layer, $X, $Y, $YA, $Zoom+1, $ZOrig, $N, $LatC, $W, $E, $ImgX1, $ImgYC, $ImgX2, $ImgY2,$ImageHeight,$SkipEmpty);
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
            ($success,$empty) = RenderTile($layer, $X, $Y, $YB, $Zoom+1, $ZOrig, $LatC, $S, $W, $E, $ImgX1, $ImgY1, $ImgX2, $ImgYC,$ImageHeight,$SkipEmpty);
            waitpid($pid,0);
            my $ChildExitValue = $?; # we don't want the details, only if it exited normally or not.
            if ($ChildExitValue or !$success)
            {
                return (0,$SkipEmpty);
            }
        }
        if ($Zoom == $ZOrig)
        {
            $progressPercent=100 if (! $Config->get("Debug")); # workaround for not correctly updating %age in fork, disable in debug mode
            statusMessage("Finished $X,$Y for layer $layer", $currentSubTask, $progressJobs, $progressPercent, 1);
        }
    }
    else
    {
        ($success,$empty) = RenderTile($layer, $X, $Y, $YA, $Zoom+1, $ZOrig, $N, $LatC, $W, $E, $ImgX1, $ImgYC, $ImgX2, $ImgY2,$ImageHeight,$SkipEmpty);
        return (0,$empty) if (!$success);
        ($success,$empty) = RenderTile($layer, $X, $Y, $YB, $Zoom+1, $ZOrig, $LatC, $S, $W, $E, $ImgX1, $ImgY1, $ImgX2, $ImgYC,$ImageHeight,$SkipEmpty);
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

    statusMessage("Updating the Client", $currentSubTask, $progressJobs, $progressPercent,1);
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
        statusMessage("svn status did not come back clean, check your installation",$currentSubTask, $progressJobs, $progressPercent,1);
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
            print "\n! WARNNG: you cannot have a more current client than the server: $runningVersion > $currentVersion\n";
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
        print "\n! WARNING: Could not get version info from server!\n";
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

        statusMessage("Transforming zoom level $zoom with XSLT", $currentSubTask, $progressJobs, $progressPercent,0);
        $success = runCommand($Cmd,$PID);
    }
    elsif($Config->get("Osmarender") eq "orp")
    {
        chdir "orp";
        my $Cmd = sprintf("%s perl orp.pl -r %s -o %s",
          $Config->get("Niceness"),
          $MapFeatures,
          $TSVG);

        statusMessage("Transforming zoom level $zoom with or/p", $currentSubTask, $progressJobs, $progressPercent,0);
        $success = runCommand($Cmd,$PID);
        chdir "..";
    }
    else
    {
        die "invalid Osmarender setting in config";
    }
    if (!$success) {
        statusMessage(sprintf("%s produced an error, aborting render.", $Config->get("Osmarender")), 
                      $currentSubTask, $progressJobs, $progressPercent, 1);
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
       statusMessage("File $TSVG doesn't look like svg, aborting render.", $currentSubTask, $progressJobs, $progressPercent,1);
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
        statusMessage("Beziercurvehinting zoom level $zoom", $currentSubTask, $progressJobs, $progressPercent,0);
        runCommand($Cmd,$PID);
#-----------------------------------------------------------------------------
# Sanitycheck for Bezier curve hinting, no output = bezier curve hinting failed
#-----------------------------------------------------------------------------
        my $filesize= -s $SVG;
        if (!$filesize) 
        {
            copy($TSVG,$SVG);
            statusMessage("Error on Bezier Curve hinting, rendering without bezier curves", $currentSubTask, $progressJobs, $progressPercent,0);
        }
        killafile($TSVG) if (!$Config->get("Debug"));
    }
    else
    {   # don't do bezier curve hinting
        statusMessage("Bezier Curve hinting disabled.", $currentSubTask, $progressJobs, $progressPercent,0);
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
    my($Zoom, $ZOrig, $layer, $SizeX, $SizeY, $X1, $Y1, $X2, $Y2, $ImageHeight, $X, $Y, $Ytile) = @_;
    
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
    statusMessage("Rendering", $currentSubTask, $progressJobs, $progressPercent,0);
    print STDERR "\n$Cmd\n" if ($Config->get("Debug"));


    my $commandResult = $Config->get("Batik") == "3"?sendCommandToBatik($Cmd) eq "OK":runCommand($Cmd,$PID);
    if (!$commandResult or ! -e $TempFile )
    {
        statusMessage("$Cmd failed", $currentSubTask, $progressJobs, $progressPercent, 1);
        if ($Config->get("Batik") == "3" && !getBatikStatus())
        {
            statusMessage("Batik agent is not running, use $0 startBatik to start batik agent\n", $currentSubTask, $progressJobs, $progressPercent, 1);
        }
        ## TODO: check this actually gets the correct coords 
        PutRequestBackToServer($X,$Y,$ZOrig,"BadSVG");
        addFault("inkscape",1);
        $unrenderable{"$X $Y $ZOrig"}++;
        cleanUpAndDie("svg2png failed",$Mode,3,$PID);
        return (0,0);
    }
    resetFault("inkscape"); # reset to zero if inkscape succeeds at least once
    killafile($stdOut) if (not $Config->get("Debug"));
    
    my $ReturnValue = splitImageX($TempFile, $layer, $ZOrig, $X, $Y, $Zoom, $Ytile); # returns true if tiles were all empty
    
    killafile($TempFile) if (not $Config->get("Debug"));
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

## sub mergeOsmFiles moved to tahlib.pm

#-----------------------------------------------------------------------------
# Split a tileset image into tiles
#-----------------------------------------------------------------------------
sub splitImageX 
{
    my ($File, $layer, $ZOrig, $X, $Y, $Z, $Ytile) = @_;
  
    # Size of tiles
    my $Pixels = 256;
  
    # Number of tiles
    my $Size = 2 ** ($Z - $ZOrig);

    # Assume the tileset is empty by default
    my $allempty=1;
  
    # Load the tileset image
    statusMessage(sprintf("Splitting %s (%d x 1)", $File, $Size), $currentSubTask, $progressJobs, $progressPercent, 0);
    my $Image = newFromPng GD::Image($File);
    if( not defined $Image )
    {
        print STDERR "\nERROR: Failed to read in file $File\n";
        PutRequestBackToServer($X,$Y,$ZOrig,"MissingFile");
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
        my $Filename = tileFilename($layer, $X * $Size + $xi, $Ytile, $Z);
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
            PutRequestBackToServer($X,$Y,$ZOrig,"BlackTile");
            cleanUpAndDie("SplitImageX:BlackTile encountered, exiting","EXIT",4,$PID);
        }
        # Detect empty tile here:
        elsif (not($SubImage->compare($EmptyLandImage) & GD_CMP_IMAGE)) # libGD comparison returns true if images are different. (i.e. non-empty Land tile) so return the opposite (false) if the tile doesn''t look like an empty land tile
        {
            copy("emptyland.png", $Filename);
            # Change the tile to a zero-length file if it's as blank as the parent
            # We keep the ones at level 15 so the server fallback never has to go more than 3 levels.
            if( $Z > 12 and $Z != 15 and not $Config->get("LocalSlippymap") )
            {
                my $upfile = tileFilename($layer, ($X * $Size + $xi)>>1, $Ytile>>1, $Z-1);
                my $upsize = -e $upfile ? -s $upfile : -1;
                if( $upsize == 0 or $upsize == -s "emptyland.png" )
                { open my $fh, ">$Filename" }
            }
        }
        elsif (not($SubImage->compare($EmptySeaImage) & GD_CMP_IMAGE)) # same for Sea tiles
        {
            copy("emptysea.png",$Filename);
            # Change the tile to a zero-length file if it's as blank as the parent
            if( $Z > 12 and $Z != 15 and not $Config->get("LocalSlippymap") )
            {
                my $upfile = tileFilename($layer, ($X * $Size + $xi)>>1, $Ytile>>1, $Z-1);
                my $upsize = -e $upfile ? -s $upfile : -1;
                if( $upsize == 0 or $upsize == -s "emptysea.png" )
                { open my $fh, ">$Filename" }
            }
#            $allempty = 0; # TODO: enable this line if/when serverside empty tile methods is implemented. Used to make sure we                                     generate all blank seatiles in a tileset.
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
            statusMessage(" -> $Basename", $currentSubTask, $progressJobs, $progressPercent,0) if ($Config->get("Verbose"));
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

                    statusMessage("ColorQuantizing $Basename", $currentSubTask, $progressJobs, $progressPercent,0);
                    if(runCommand($Cmd,$PID))
                    {
                        unlink($Filename);
                    }
                    else
                    {
                        statusMessage("ColorQuantizing $Basename with ".$Config->get("PngQuantizer")." failed",
                                      $currentSubTask, $progressJobs, $progressPercent,1);
                        rename($Filename, $Filename2);
                    }
                }
                else
                {
                    statusMessage("ColorQuantizing $Basename with \"".$Config->get("PngQuantizer")."\" failed, pngnq not installed?",
                                  $currentSubTask, $progressJobs, $progressPercent,1);
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
            statusMessage("Optimizing $Basename", $currentSubTask, $progressJobs, $progressPercent,0);
            if(runCommand($Cmd,$PID))
            {
                unlink($Filename2);
            }
            else
            {
                statusMessage("Optimizing $Basename with ".$Config->get("PngOptimizer")." failed", $currentSubTask, $progressJobs, $progressPercent,1);
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

    statusMessage("tilesGen.pl has changed, re-start new version", $currentSubTask, $progressJobs, $progressPercent, 1);
    if ($Config->get("ForkForUpload") && $child_pid != -1)  ## FIXME: make more general
    {
        statusMessage("Waiting for child process", $currentSubTask, $progressJobs, $progressPercent,0);
        waitpid($child_pid, 0);
    }
    exec "perl", $0, $Mode, "reexec", 
        "progressJobs=" . $progressJobs, 
        "idleSeconds=" . getIdle(1), 
        "idleFor=" . getIdle(0), 
        "progstart=$progstart" or die("could not reExec");
}


sub startBatikAgent
{
    if (getBatikStatus()) {
        statusMessage("BatikAgent is already running\n", $currentSubTask, $progressJobs, $progressPercent,0);
        return;
    }

    statusMessage("Starting BatikAgent\n", $currentSubTask, $progressJobs, $progressPercent,0);
    my $Cmd = sprintf("%s%s java -Xms256M -Xmx%s -cp %s org.tah.batik.ServerMain -p %d > /dev/null&", 
    $Config->get("i18n") ? "LC_ALL=C " : "",
    $Config->get("Niceness"),
    $Config->get("BatikJVMSize"),
    $Config->get("BatikClasspath"),
    $Config->get("BatikPort")
    );
    system($Cmd);

    for (my $i = 0; $i < 10; $i++) {
        sleep(1);
        if (getBatikStatus()) {
            statusMessage("BatikAgent started succesfully");
            return;
        }
    }
    print STDERR "Unable to start BatikAgent with this command:\n";
    print STDERR "$Cmd\n";
}

sub stopBatikAgent
{
    if (!getBatikStatus()) {
        statusMessage("BatikAgent is not running\n", $currentSubTask, $progressJobs, $progressPercent,0);
        return;
    }

    sendCommandToBatik("stop\n\n");
    statusMessage("Send stop command to BatikAgent\n", $currentSubTask, $progressJobs, $progressPercent,0);
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
