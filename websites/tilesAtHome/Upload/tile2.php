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

# TODO: check whether version number is acceptable

HandleUpload($_FILES['file'], $User, $UserID, $VersionID);

exit;

function AbortWithError($Code, $Message){
  header(sprintf("HTTP/1.0 %d %s", $Code, $Message));
  header("Content-type:text/plain");
  printf("%s\n", $Message);
  exit;
}


function HandleUpload($File, $User, $UserID, $VersionID){

  # All error-messages etc are plain text for use by clients
  header("Content-type:text/plain");
  
  # Decide on the name of a Temporary directory
  $Dir = TempDir();
  
  # Check the uploaded ZIP file
  $Size = $File['size'];

  if($Size <= 0){
    AbortWithError(400, "No file uploaded");
  }

  # Keep going if the user presses stop, to ensure temporary directories get erased
  # see also register_shutdown_function() for another option
  ignore_user_abort();
    
  # Create temporary directory
  if(!mkdir($Dir)){
    AbortWithError(503, "Can't create temporary directory");
  }
      
  # Uncompress the uploaded tiles
  # -j means to ignore any pathnames in the ZIP file
  # -d $Dir specifies the directory to unzip to
  # $Filename is the zip file
  system(sprintf("unzip -j -d %s %s", $Dir, $File['tmp_name']));
      
  # Process all the tiles (return number of tiles done)
  $Count = HandleDir($Dir, $User, $UserID, $VersionID);
        
  # Delete the temporary directory and everything inside
  DelDir($Dir);
  
  logMsg("$User (user #$UserID, version #$VersionID) uploaded $Count tiles in $Size bytes", 3);
  printf("OK, %d", $Count);
}

#----------------------------------------------------------------------
# Delete the temporary directory and everything inside
#----------------------------------------------------------------------
function DelDir($Dir){
  $dp = opendir($Dir);
  while(($file = readdir($dp)) !== false){
    if($file != "." && $file != ".."){
      $Filename = "$Dir/$file";
      unlink($Filename);
    }
  }
  closedir($dp);
  rmdir($Dir);
}

#----------------------------------------------------------------------
# Processes tiles that are currently sitting in a temp directory
#----------------------------------------------------------------------
function HandleDir($Dir, $User, $UserID, $VersionID){
  $Count = 0;
  $dp = opendir($Dir);
  $TileList = array();
  while(($file = readdir($dp)) !== false){
    $Filename = "$Dir/$file";
    $Count += HandleFile($Filename, $User, $VersionID, $TileList);
  }
  closedir($dp);
  
  SaveMetadata($TileList, $UserID, $VersionID);
  return($Count);
}

function SaveMetadata($TileList, $UserID, $VersionID){
  #logMsg(sprintf("%d, %d -- ", $UserID, $VersionID) . implode(": ", $TileList), 5);
  
  # Connect to the database
  include("../connect/connect.php");
  
  SaveUserStats($UserID, $VersionID, count($TileList));
  
  RemoveFromQueue($TileList);
  
  # Each element in TileList is a snippet of values (x,y,z,type,size) for each tile
  foreach($TileList as $SqlSnippet){
    
    $Fields = "x, y, z, type, size, date, user, version";
    $Values = sprintf("%s, now(), %d, %d", $SqlSnippet, $UserID, $VersionID);
    
    $SQL = sprintf("replace into `tiles_meta` (%s) values (%s);", $Fields, $Values);
    mysql_query($SQL);
  }
  
  # Disconnect from database
  mysql_close();
}

function RemoveFromQueue($TileList){
  foreach($TileList as $CSV){
    list($X, $Y, $Z, $Layer, $Size) = explode(",", $CSV);
    if($Z == 12){
      logMsg("Uploaded $X,$Y on $Layer", 4);
      
      moveRequest($X, $Y, REQUEST_ACTIVE, REQUEST_DONE);
    }
  }
}

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
function HandleFile($Filename, $User, $VersionID, &$TileList){
  if(preg_match("/([a-z]+)_(\d+)_(\d+)_(\d+)\.png/", $Filename, $Matches)){
    $Layername = $Matches[1];
    $Z = $Matches[2];
    $X = $Matches[3];
    $Y = $Matches[4];
    $Valid = TileValid($X,$Y,$Z);
    if($Valid){
      
      $Layer = checkLayer($Layername);
      if($Layer > 0){
        InsertTile($X,$Y,$Z,$Layer,$User,$Filename, $VersionID, $TileList);
        return(1);
      }
      else{
        logMsg("Invalid layer $Layer from $User", 2);
      }
    }
    else{
      #logMsg("Invalid tile $Filename from $User", 3);
    }
  }
  return(0);
}

function InsertTile($X,$Y,$Z,$Layer,$User,$OldFilename, $VersionID, &$TileList){
  if(!TileValid($X,$Y,$Z)){
    printf("INVALID %d,%d,%d\n", $X,$Y,$Z);
    return;
  }
  
  $Size = filesize($OldFilename);

  if($VersionID < 5){ // Prior to "cambridge", no blank-tile detection
    # Don't store blank tiles
    if($Size < 1000){
      printf("%s -> blank, not saved\n", $OldFilename);
      return;
    }
  }
  
  # Don't store *really* blank tiles, no matter who they're from
  if($Size < 100){
    
    # TODO: this is a request to delete existing tiles - need to handle it!
  
    return;
  }
  
  # Remember tile details, in a form that can be added to SQL easily
  $SqlSnippet = sprintf("%d,%d,%d,%d,%d", $X, $Y, $Z, $Layer, $Size);
  array_push($TileList, $SqlSnippet);
  
  # Decide on a filename
  $NewFilename = TileName($X,$Y,$Z, layerDir($Layer));
  if(!$NewFilename){
    logMsg("Invalid filename created for $X,$Y,$Z,$Layer",2);
    return;
  }
  
  # Check directory exists
  CreateDirectoryToHold($NewFilename);
  
  # Move the file to its new home
  rename($OldFilename, $NewFilename);
  printf("%s -> %s\n", $OldFilename, $NewFilename);

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
  return(sprintf("temp/%s", md5(uniqid(rand(), 1))));
}

?>