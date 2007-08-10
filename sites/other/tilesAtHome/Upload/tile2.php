<?php
# Tiles upload handler, accepts ZIP files, saves to database 
# OJW 2006, GNU GPL v2 or later

if(0){ // Option to turn off uploads
  AbortWithError(503, "Uploads temporarily disabled");
}

include("../lib/log.inc");
include("../lib/tilenames.inc");
include("../lib/users.inc");
include("../lib/versions.inc");
include("../lib/layers.inc");
include("../lib/requests.inc");
include("../lib/checkupload.inc");
include("../lib/cpu.inc");
include("../lib/queue.inc");
include("../lib/tokens.inc");

# Option to turn-off non-single-tileset uploads (was only used for testing)
if(0){
  if($_POST["single_tileset"] != "yes"){
    AbortWithError(401, "We're testing LA2's single-tileset uploads, normal ones are being discarded for now");    
  }
}

if(0){
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
}
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

#-----------------------------------------------------------------------------------
# Save metadata for each tile in the upload
#-----------------------------------------------------------------------------------
function SaveMetadata($TileList, $UserID, $VersionID){
  
  SaveUserStats($UserID, $VersionID, count($TileList));
  
  RemoveFromQueue($TileList);
  
  # Each element in TileList is a snippet of values (x,y,z,type,size) for each tile
  foreach($TileList as $SqlSnippet){
    
    // Use this line if you need access to separate fields
    // list($X, $Y, $Z, $Layer, $Size) = explode(",", $CSV);

    $Fields = "x, y, z, type, size, date, user, version, tileset";
    $Values = sprintf("%s, now(), %d, %d, 0", $SqlSnippet, $UserID, $VersionID);
 
    $SQL = sprintf("replace into `tiles_meta` (%s) values (%s);", $Fields, $Values);
    mysql_query($SQL);
  }
}

#------------------------------------------------------------------------------------
# Save uploaded blank tiles in the database
#------------------------------------------------------------------------------------
function SaveBlankTiles($BlankTileList, $UserID){
  # Each element in BlankTileList is a snippet of values (x,y,z,type,size) for each tile
  foreach($BlankTileList as $SqlSnippet){

    // TODO: blank tiles can be z-12, which means they can fulfil a request
    list($X, $Y, $Z, $Layer, $Type) = explode(",", $SqlSnippet);
    if($Z == 12){
      moveRequest($X, $Y, REQUEST_ACTIVE, REQUEST_DONE, 0);
    }
    
    # Make a blank tile
    if( $Type >= 0 )
    {
      $Fields = "x, y, z, layer, type, date, user";
      $Values = sprintf("%s, now(), %d", $SqlSnippet, $UserID);

      $SQL = sprintf("replace into `tiles_blank` (%s) values (%s);", $Fields, $Values);
    }
    else
    {
      # Delete a blank tile
      $SQL = sprintf("delete from `tiles_blank` where `x`=%d AND `y`=%s AND `z`=%s AND `layer`=%d", $X, $Y, $Z, $Layer);
    }
    DeleteRealTile($X,$Y,$Z,$Layer);

    mysql_query($SQL);
    
    logSqlError();
  }
}

#------------------------------------------------------------------------------------
# Delete a tile and its metadata (usually when a blank tile is uploaded in its place)
#------------------------------------------------------------------------------------
function DeleteRealTile($X,$Y,$Z,$LayerID){
  
  # Delete the meta database entry
  $SQL = sprintf(
    "DELETE FROM `tiles_meta` WHERE `x`=%d AND `y`=%d AND `z`=%d AND `type`=%d;",
      $X,$Y,$Z,$LayerID);
  mysql_query($SQL);
  logSqlError();
  
  # Delete the image, if exists
  $NewFilename = TileName($X,$Y,$Z, layerDir($LayerID));
  if($NewFilename){
    if(file_exists($NewFilename)){
      unlink($NewFilename);
    }
  }
}

#-----------------------------------------------------------------------------
# Save metadata when an entire tileset is uploaded at once
#-----------------------------------------------------------------------------
function SaveTilesetMetadata($X,$Y,$Layer,$UserID, $VersionID){
  SaveUserStats($UserID, $VersionID, 1365);
  
  moveRequest($X, $Y, REQUEST_ACTIVE, REQUEST_DONE, 0);
  
  $LayerID = checkLayer($Layer);

  $Fields = "x, y, z, type, size, date, user, version, tileset";
  $Values = sprintf("%d,%d,%d,%d,%d,now(), %d, %d, 1", $X,$Y,12, $LayerID, 0, $UserID, $VersionID);
 
  $SQL = sprintf("replace into `tiles_meta` (%s) values (%s);", $Fields, $Values);
  mysql_query($SQL);
  
  logSqlError();

}

#-----------------------------------------------------------------------------
# Removes completed* tilesets from queue
# * where completed means "z12 was uploaded"
#-----------------------------------------------------------------------------
function RemoveFromQueue($TileList){
  foreach($TileList as $CSV){
    list($X, $Y, $Z, $Layer, $Size) = explode(",", $CSV);
    if($Z == 12){
    
      moveRequest($X, $Y, REQUEST_ACTIVE, REQUEST_DONE, 0);
        
      #logMsg(sprintf("Moving tile %d, %d from %d to %d", $X, $Y, REQUEST_ACTIVE, REQUEST_DONE), 4);
      logSqlError();
    }
  }
}

#---------------------------------------------------------------------------
# Update user info with their latest upload
#---------------------------------------------------------------------------
function SaveUserStats($UserID, $VersionID, $NumTiles){
  $SQL = 
    "update `tiles_users` set ".
      "`uploads` = `uploads` + 1, ".
      sprintf("`tiles` = `tiles` + %d, ", $NumTiles).
      sprintf("`version` = %d, ", $VersionID).
      "`last_upload` = now() ".
    " where ".
    sprintf("`id`=%d;", $UserID);
    
  mysql_query($SQL);
}

#----------------------------------------------------------------------
# Processes tile PNG images
#----------------------------------------------------------------------
function HandleFile($Filename, $User, $VersionID, &$TileList, &$BlankTileList){
  if(preg_match("/([a-z]+)_(\d+)_(\d+)_(\d+)\.png/", $Filename, $Matches)){
    $Layername = $Matches[1];
    $Z = $Matches[2];
    $X = $Matches[3];
    $Y = $Matches[4];
    $Valid = TileValid($X,$Y,$Z);
    if($Valid){
      
      $Layer = checkLayer($Layername);
      if($Layer > 0){
        InsertTile($X,$Y,$Z,$Layer,$User,$Filename, $VersionID, $TileList, $BlankTileList);
        return(1);
      }
      else{
        logMsg("Invalid layer $Layer from $User ($Layername)", 2);
      }
    }
    else{
      #logMsg("Invalid tile $Filename from $User", 3);
    }
  }
  return(0);
}

function InsertTile($X,$Y,$Z,$Layer,$User,$OldFilename, $VersionID, &$TileList, &$BlankTileList){
  if(!TileValid($X,$Y,$Z)){
    printf("INVALID %d,%d,%d\n", $X,$Y,$Z);
    return;
  }
  
  $Size = filesize($OldFilename);

  
  # Decide on a filename
  $NewFilename = TileName($X,$Y,$Z, layerDir($Layer));
  if(!$NewFilename){
    logMsg("Invalid filename created for $X,$Y,$Z,$Layer",2);
    return;
  }
  
  if($VersionID < 5){ // Prior to "cambridge", no blank-tile detection
    # Don't store blank tiles
    if($Size < 1000){
      printf("%s -> blank, not saved\n", $OldFilename);
      return;
    }
  }
  
  if($Size == 67){
    # This is a request to delete existing tiles and create a "blank land" tile
    # TODO: make an enumeration for blank land/sea
    $SqlSnippet = sprintf("%d,%d,%d,%d,%d", $X, $Y, $Z, $Layer, 2);
    array_push($BlankTileList, $SqlSnippet);
    return;
  }
  if($Size == 69){
    # This is a request to create a sea tile
    $SqlSnippet = sprintf("%d,%d,%d,%d,%d", $X, $Y, $Z, $Layer, 1);
    array_push($BlankTileList, $SqlSnippet);
    return;
  }
  if($Size == 0){
    # This is a request to delete a tile (both real and blank)
    $SqlSnippet = sprintf("%d,%d,%d,%d,%d", $X, $Y, $Z, $Layer, -1);
    array_push($BlankTileList, $SqlSnippet);
    return;
  }

  if($Size < 100){
    # TODO: WTF is this tile
    return;
  }
  
  # Remember tile details, in a form that can be added to SQL easily
  $SqlSnippet = sprintf("%d,%d,%d,%d,%d", $X, $Y, $Z, $Layer, $Size);
  array_push($TileList, $SqlSnippet);

  
  # Check directory exists
  CreateDirectoryToHold($NewFilename);
  
  # Move the file to its new home
  rename($OldFilename, $NewFilename);
  printf("%s -> %s\n", $OldFilename, $NewFilename);

  # Make world-writeable, so that it's easier to move files using shell
  # (note: anyone with shell account on dev can access these files anyway
  #  through their website running as htuser)
  chmod($NewFilename, 0666);
}

function CreateDirectoryToHold($Filename){
  # Get the components of the directory structure
  $Parts = explode("/", $Filename);
  
  # Remove the last element, which is the filename
  array_pop($Parts);
  # and the first element, which is a zero-length string
  array_shift($Parts);
  
  $AssumedToExist = 4; // var/www/ojw/Tiles don't get created
  
  # For each part...
  $Dir = "";
  $Count = 0;
  foreach($Parts as $Part){
    $Dir .= "/".$Part;
    $Count++;
    
    if($Count > $AssumedToExist){ 
      CreateDir($Dir);
    }
  }
}

function CreateDir($Dir){
  if(file_exists($Dir)){
    #printf("Directory exists: %s\n", $Dir);
    return(1);
  }
  
  if(!mkdir($Dir, 0777)){
    printf("Failed to create directory %s\n", $Dir);
    return(0);
  }
  
  #printf("Creating dir \"%s\"\n", $Dir);
  return(1);
}

#----------------------------------------------------------------------
# Chooses the name for a temporary directory
#
# * everything under one temp dir
# * md5 gives alphanumeric filename
# * uniqid means multiple threads are unlikely to conflict
#----------------------------------------------------------------------
function TempDir(){
  return(sprintf("/home/ojw/tiles-ojw/temp/%s", md5(uniqid(rand(), 1))));
}

?>
