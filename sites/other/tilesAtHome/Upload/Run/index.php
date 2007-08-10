<?php
# Tiles queue handler, accepts ZIP files, saves to database 
# OJW 2007, GNU GPL v2 or later

# All error-messages etc are plain text for use by clients
header("Content-type:text/plain");

if(1){
  if($_SERVER["REMOTE_ADDR"] != $_SERVER["SERVER_ADDR"]){
    print "This page can only be run by the dev server\n";
    exit;
  }
}

include("../../lib/log.inc");
include("../../lib/tilenames.inc");
include("../../lib/users.inc");
include("../../lib/versions.inc");
include("../../lib/layers.inc");
include("../../lib/requests.inc");
include("../../lib/checkupload.inc");
include("../../lib/cpu.inc");
include("../../connect/connect.php");
include("../../lib/blanktile.inc");


if(0){ // Option to turn off uploads
  AbortWithError("Disabled");
}


if(0){
  $Load = GetLoadAvg();
  //logMsg("$Load load", 4);
  if($Load < 0){
    logMsg("Load average failed", 4);
  }
  elseif($Load > 4.0){
    logMsg("Too busy...", 2);
    print "Too busy";
    exit;
  }
}


$QueueDir = "/home/ojw/tiles-ojw/Queue/";
list($Uploads, $Tiles) = HandleNextFilesFromQueue($QueueDir, 24);

logMsg(sprintf("Queue runner - done %d uploads with %d tiles", $Uploads, $Tiles), 24);


function HandleNextFilesFromQueue($Dir, $NumToProcess){
  $CountUploads = 0;
  $CountTiles = 0;

  foreach(SortFiles($Dir) as $File => $Time) {
    if($CountUploads < $NumToProcess){
      if(preg_match("{(\w+)\.txt}", $File, $Matches)){
        $Name = $Matches[1];
        printf( "\n\n===%s===\n\n", htmlentities($Name));
        $CountTiles += HandleQueueItem($Name, $Dir);
        $CountUploads++;
      }
    }
  }
  return(array($CountUploads, $CountTiles));
}

function HandleQueueItem($Name, $Dir){
    $MetaFile = $Dir . $Name . ".txt";
    $ZipFile = $Dir . $Name . ".zip";
    print "$ZipFile\n";
    if(!file_exists($MetaFile)){
	print "No meta file\n";
        if(file_exists($ZipFile))
          unlink($ZipFile);        
	return;
    }
    if(!file_exists($ZipFile)){
	print "No zip file\n";
        if(file_exists($MetaFile))
          unlink($MetaFile);
	return;
    }
    
    $Meta = MetaFileInfo($MetaFile);
    
    logMsg(sprintf("Doing %s by user %d version %d", $ZipFile, $Meta["user"], $Meta["version"]), 3);
    
    $Count = HandleUpload($ZipFile, $Meta["user"], $Meta["version"]);
    
    unlink($MetaFile);
    unlink($ZipFile);
    
    return($Count);
}
function MetaFileInfo($File){
    $fp = fopen($File, "r");
    if(!$fp)
      return(array("valid"=>0));
    $Return = array();
    while($Line = fgets($fp, 200)){
	if(preg_match('{^(\w+)\s*=\s*(.*)$}', $Line, $Matches)){
	    $Return[$Matches[1]] = $Matches[2];
	}
    }
    fclose($fp);
    return($Return);
    
}
function AbortWithError($Message){
  logMsg("Aborted with $Message", 5);
  printf("\n\nError: %s\n", $Message);
  exit;
}


function HandleUpload($File, $UserID, $VersionID){
  # Decide on the name of a Temporary directory
  $Dir = TempDir();
  
  # Check the uploaded ZIP file
  $Size = filesize($File);

  logMsg("Handling $File ($Size bytes) by $UserID (version $VersionID)", 4);
  
  if($Size <= 0){ 
    print("No file uploaded or file too large\n");
    return;
  }

  # Keep going if the user presses stop, to ensure temporary directories get erased
  # see also register_shutdown_function() for another option
  ignore_user_abort();
    
  # Create temporary directory
  if(!mkdir($Dir)){
    AbortWithError("Can't create temporary directory");
  }
      
  # Uncompress the uploaded tiles
  # -j means to ignore any pathnames in the ZIP file
  # -d $Dir specifies the directory to unzip to
  # $Filename is the zip file
  $Command = sprintf("unzip -j -d %s %s", $Dir, $File);
  logMsg("Running '$Command'", 3);
  system($Command);
  
  logMsg("Handling directory $Dir", 3);
  
  # Process all the tiles (return number of tiles done)
  $Count = HandleDir($Dir, $UserID, $VersionID);
        
  # Delete the temporary directory and everything inside
  DelDir($Dir);
  
  logMsg("OK, $Count tiles in upload", 3);
  return($Count);
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
function HandleDir($Dir, $UserID, $VersionID){
  $Count = 0;
  $TileList = array();
  $BlankTileList = array();

  list($ValidTileset, $TilesetX, $TilesetY, $TilesetLayer) = CheckUploadDir($Dir);
  
  $dp = opendir($Dir);
  while(($file = readdir($dp)) !== false){
    $Filename = "$Dir/$file";
    //logMsg("Handling file $file", 6);
    $Count += HandleFile($Filename, $VersionID, $TileList, $BlankTileList);
  }
  closedir($dp);

  if($ValidTileset)
    SaveTilesetMetadata($TilesetX,$TilesetY,$TilesetLayer, $UserID, $VersionID);
  else
    SaveMetadata($TileList, $UserID, $VersionID);

  SaveBlankTiles($BlankTileList, $UserID);

  return($Count);
}

#-----------------------------------------------------------------------------------
# Save metadata for each tile in the upload
#-----------------------------------------------------------------------------------
function SaveMetadata($TileList, $UserID, $VersionID){
  
  SaveUserStats($UserID, $VersionID, count($TileList));
  
  RemoveFromQueue($TileList);

  # wrap all tile meta updates in a single transaction;
  mysql_query('START TRANSACTION;');  
  # Each element in TileList is a snippet of values (x,y,z,type,size) for each tile
  foreach($TileList as $SqlSnippet){
    
    // Use this line if you need access to separate fields
    list($X, $Y, $Z, $Layer, $Size) = explode(",", $SqlSnippet);
    
    $Fields = "x, y, z, type, size, date, user, version, tileset";
    $Values = sprintf("%s, now(), %d, %d, %d", $SqlSnippet, $UserID, $VersionID, 0);
    $UpdateValues = sprintf ("size = VALUES(size), date = VALUES(date), user = VALUES(user), version = VALUES(version), tileset = VALUES(tileset)");

    $SQL = sprintf("INSERT INTO `tiles_meta` (%s) values (%s) ON DUPLICATE KEY UPDATE %s;", $Fields, $Values, $UpdateValues);
    mysql_query($SQL);
  }
  # end transaction after tiles are updated
  mysql_query('COMMIT;');  
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
      moveRequest($X, $Y, NULL, REQUEST_DONE, 0);
    }
    
    # Make a blank tile
    if( $Type >= 0 )
    {
      InsertBlankTile($X,$Y,$Z,$Layer,$UserID,$Type);
    }
    else
    {
      # Delete a blank tile
      $SQL = sprintf("delete from `tiles_blank` where `x`=%d AND `y`=%s AND `z`=%s AND `layer`=%d", $X, $Y, $Z, $Layer);
      mysql_query($SQL);
      logSqlError();
    }
    DeleteRealTile($X,$Y,$Z,$Layer);

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
  
  moveRequest($X, $Y, NULL, REQUEST_DONE, 0);
  logMsg("Tileset $X,$Y uploaded at once\n", 4);
  
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
    
      moveRequest($X, $Y, NULL, REQUEST_DONE, 0);
        
      logMsg(sprintf("Tileset %d, %d moved from mode %d to mode %d", $X, $Y, REQUEST_ACTIVE, REQUEST_DONE), 4);
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
function HandleFile($Filename, $VersionID, &$TileList, &$BlankTileList){
  if(preg_match("/([a-z]+)_(\d+)_(\d+)_(\d+)\.png/", $Filename, $Matches)){
    $Layername = $Matches[1];
    $Z = $Matches[2];
    $X = $Matches[3];
    $Y = $Matches[4];
    $Valid = TileValid($X,$Y,$Z);
    if($Valid){
      
      $Layer = checkLayer($Layername);
      if($Layer > 0){
        InsertTile($X,$Y,$Z,$Layer,$Filename, $VersionID, $TileList, $BlankTileList);
        return(1);
      }
      else{
        logMsg("Invalid layer $Layer from $UserID ($Layername)", 2);
      }
    }
    else{
      logMsg("Invalid tile $Filename from $UserID", 3);
    }
  }
  else{
    # logMsg("$Filename doesn't match regular expression", 2);
  }
  return(0);
}

function InsertTile($X,$Y,$Z,$Layer,$OldFilename, $VersionID, &$TileList, &$BlankTileList){
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

#----------------------------------------------------------
# Returns array of files in Dir, oldest first
#----------------------------------------------------------
function SortFiles($Dir){
 $fd=opendir($Dir);
 while ($file=readdir($fd)){
  $times["$file"]=filemtime($Dir.'/'.$file);
 }
 closedir($fd);
 asort($times);
 return $times;
}
?>
