<?php
# Tiles upload handler, accepts ZIP files, saves to database 
# OJW 2006, GNU GPL v2 or later

if(0){ // Option to turn off uploads
  AbortWithError(503, "Uploads temporarily disabled");
}

include("../lib/log.inc");
include("../lib/users.inc");
include("../lib/versions.inc");
include("../lib/queue.inc");
include("../lib/tokens.inc");

# Option to turn-off non-single-tileset uploads (was only used for testing)
if(0){
  if($_POST["single_tileset"] != "yes"){
    AbortWithError(401, "We're testing LA2's single-tileset uploads, normal ones are being discarded for now");    
  }
}

if(0){
  include_once("../lib/cpu.inc");
  $Load = GetLoadAvg();
  //logMsg("$Load load", 4);
  if($Load < 0){
    logMsg("Load average failed", 4);
  }
  elseif($Load > 2.6){
    AbortWithError(503, "Server is very very busy...");
  }
}


# Option to limit by CPU
if(0){
  include_once("../lib/cpu.inc");
  $Idle = GetLoad("idle");
  logMsg("$Idle idle", 4);
  if($Idle < 0){
    logMsg("Idle count failed", 4);
  }
  elseif($Idle < 25){
    AbortWithError(503, "Server is very very busy...");
  }
}

# Get password from posted form data (format mp=user|pass)
$Password = $_POST["mp"];
list($User,$Pass) = explode("|", $Password);

$UserID = checkUser($User, $Pass);
$VersionID = checkVersion($_POST["version"]);

# If credentials are valid
if($UserID < 1){
  AbortWithError(401, "Invalid username");
  exit; # Redundant, failsafe
}

# Check whether version number is acceptable
if($VersionID < 0){
  AbortWithError(401, "Client version not recognised or too old");
}

# Option to check upload tokens
if(1){
  list($Token1, $Token2) = GetTokens(-1, "testing");
  $Token = $_POST["token"];
  if($Token == $Token1 || $Token == $Token2){
    //logMsg("Valid token from user $UserID", 5);
  }
  else{
    logMsg(sprintf("Invalid token from user %d (%s)", $UserID, htmlentities(lookupUser($UserID))), 5);
  }
}


// New method
if(QueueLength() > MaxQueueLength())
  AbortWithError(503, "too much stuff in the queue already");

PlaceInQueue($_FILES['file'], $UserID, $VersionID);
exit;


function PlaceInQueue($Filename, $UserID, $VersionID){
  $QueueLocation = QueueDirectory();
  $Name = md5(uniqid(rand(), true));
  
  $MetaFile = $QueueLocation . $Name . ".txt";
  $fp = fopen($MetaFile, "w");
  if(!$fp)
      return;
  fputs($fp, "user = $UserID\nversion = $VersionID\n");
  fclose($fp);
  
  $ZipFile = $QueueLocation . $Name . ".zip";
  move_uploaded_file($Filename["tmp_name"], $ZipFile);
}

function AbortWithError($Code, $Message){
  header(sprintf("HTTP/1.0 %d %s", $Code, $Message));
  header("Content-type:text/plain");
  printf("%s\n", $Message);
  exit;
}

?>
