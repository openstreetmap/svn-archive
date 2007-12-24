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

require_once("../../lib/log.inc");
require_once("../../lib/tilenames.inc");
require_once("../../lib/users.inc");
require_once("../../lib/versions.inc");
require_once("../../lib/queue.inc");
require_once("../../lib/layers.inc");
require_once("../../lib/requests.inc");
require_once("../../lib/checkupload.inc");
require_once("../../connect/connect.php");
require_once("../../lib/blanktile.inc");


if(0){ // Option to turn off uploads
  AbortWithError("Disabled");
}


if(0){
  include_once("../../lib/cpu.inc");
  $Load = GetLoadAvg();
  if($Load < 0){
    logMsg("Load average failed", 4);
  }
  elseif($Load > 4.0){
    logMsg("Too busy...", 2);
    print "Too busy";
    exit;
  }
}


list($Uploads, $Tiles) = HandleNextFilesFromQueue(QueueDirectory(),200);
//logMsg(sprintf("Queue runner - done %d uploads with %d tiles", $Uploads, $Tiles), 24);

//----------------------------------------------------------------------------------
function HandleNextFilesFromQueue($Dir, $NumToProcess){
  $CountUploads = 0;
  $CountTiles = 0;

  foreach(SortFiles($Dir) as $File => $Time) {
    if($CountUploads < $NumToProcess){
      if(preg_match("#(\w+)\.zip$#", $File, $Matches)){
        $Name = $Matches[1];
        $run = @mkdir($Name);
        if ($run) {
            printf( "===%s===\n", htmlentities($Name));
            $CountTiles += HandleQueueItem($Name, $Dir);
            $CountUploads++;
            rmdir($Name);
        } else {
            printf("Another thread already grabbed $Name\n");
        }    

      }
    }
  }
  return(array($CountUploads, $CountTiles));
}

//----------------------------------------------------------------------------------
function HandleQueueItem($Name, $Dir){
    $MetaFile = $Dir . $Name . ".txt";
    $ZipFile = $Dir . $Name . ".zip";

    print "$ZipFile\n";

    if(!file_exists($MetaFile)){
	print "No meta file\n";
        unlink($ZipFile);        
	return (0);
    }

    if(!file_exists($ZipFile)){
        // We should never end up here, theoretically
	print "No zip file\n";
        unlink($MetaFile);
	return (0);
    }
    
    $Meta = MetaFileInfo($MetaFile);
    
    $Size = HandleUpload($ZipFile, $Meta["user"], $Meta["version"]);
    
    unlink($MetaFile);
    unlink($ZipFile);
    
    return($Size);
}

//----------------------------------------------------------------------------------
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
  print "Handling $File from $UserID\n";
  # Decide on the name of a Temporary directory
  $FileIdentifier = substr(strrchr($File, '/'), 1, -4 );
  $LogIdentifier = substr( $FileIdentifier, 0, 6 );
  $Dir = TempDir($FileIdentifier);
  
  # Check the uploadedZIP file
  $Size = filesize($File);

  logMsg("$LogIdentifier: Handling tileset ($Size bytes) by $UserID (version $VersionID)", 4);
  
  if($Size <= 0){ 
    print("No file uploaded or file too large\n");
    return(0);
  }

  # Keep going if the user presses stop, to ensure temporary directories get erased
  # see also register_shutdown_function() for another option
  ignore_user_abort();
    
  # Create temporary directory
  if(!mkdir($Dir)){
    AbortWithError("Can't create temporary directory $Dir");
  }
      
  # Uncompress the uploaded tiles
  # -j means to ignore any pathnames in the ZIP file
  # -d $Dir specifies the directory to unzip to
  # $Filename is the zip file
  $ziptime = microtime(true);
  print "Unzipping $File\n";
  $Command = sprintf("unzip -q -j -d %s %s", $Dir, $File);
  #logMsg("Running '$Command'", 3);
  system($Command);
  print "Finished unzipping $File\n";
  $ziptime = microtime(true) - $ziptime;

  # Process all the tiles (return number of tiles done)
  $handletime = microtime(true);
  $Count = HandleDir($Dir, $UserID, $VersionID, $Size);
  $handletime = microtime(true) - $handletime;

  # Delete the temporary directory and everything inside
  $deletetime = microtime(true);
  DelDir($Dir);
  $deletetime = microtime(true) - $deletetime;
  
  logMsg(sprintf("$LogIdentifier: Unzip: %.6f seconds, Handling took %.6f secs, Delete took %.6f seconds.", $ziptime, $handletime, $deletetime), 4);
  logMsg("$LogIdentifier: OK, $Count tiles", 4);
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
function HandleDir($Dir, $UserID, $VersionID, $Size = 0){
  $Count = 0;
  $TileList = array();
  $BlankTileList = array();

  list($ValidTileset, $TilesetX, $TilesetY, $TilesetLayer, $Tilesetcount) = CheckUploadDir($Dir);
  
  $dp = opendir($Dir);
  while(($file = readdir($dp)) !== false){
    $Filename = "$Dir/$file";
    //logMsg("Handling file $file", 6);
    $Count += HandleFile($Filename, $VersionID, $TileList, $BlankTileList);
  }
  closedir($dp);
   
  if($ValidTileset)
    SaveTilesetMetadata($TilesetX,$TilesetY,$TilesetLayer,$Tilesetcount,$UserID, $VersionID, $Size);
  else
    SaveMetadata($TileList, $UserID, $VersionID, $Size);

  $deletetime = microtime(true);
  SaveBlankTiles($BlankTileList, $UserID,$ValidTileset,$TilesetX, $TilesetY, $TilesetLayer);
  $blanktime = microtime(true) - $deletetime;
  $FileIdentifier = substr(strrchr($Dir, '/'), 1, 6);
  logMsg("$FileIdentifier: Blank tiles for $TilesetX,$TilesetY took $blanktime.", 4);

  return($Count);
}

#-----------------------------------------------------------------------------------
# Save metadata for each tile in the upload
#-----------------------------------------------------------------------------------
function SaveMetadata($TileList, $UserID, $VersionID, $Size = 0){
  
  SaveUserStats($UserID, $VersionID, count($TileList), $Size);
  
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
function SaveBlankTiles($BlankTileList, $UserID, $ValidTileset, $TilesetX, $TilesetY, $TilesetLayer){
  # First we run through the set to find the predominant type
  $CommonType = 0;
  $ReplaceList = array();
  
  # This optimisation is only possible if we have a valid full tileset
  if( $ValidTileset )
  {
    $CountTypes = array( -1 => 0, 1 => 0, 2 => 0 );
    
    foreach($BlankTileList as $SqlSnippet){

      list($X, $Y, $Z, $Layer, $Type) = explode(",", $SqlSnippet);
      # If we find levels <12, we set a flag to enable old behaviour
      if( $Z < 12 ) {
        $CommonType = -2;
        break;
      }
      # Since we save storage by storing in level 12, if we are given the type we have to use it
      if( $Z == 12 ) {
        $CommonType = $Type;
        break;
      }
      $CountType[$Type]++;
    }
    # Determine the common type, if we have a choice
    if( $CommonType == 0 )
    {
      if( $CountType[1] > $CountType[2] ) {
        $CommonType = 1;
      } else {
        $CommonType = 2;
      }
    }
    # Store the "common tile" at zoom-12, now we don't have to store this type at any lower levels...
    if( $CommonType > 0 ) {
      array_push( $ReplaceList, sprintf("(%d, %d, %d, '%s', %d, now(), %d)", $TilesetX, $TilesetY, 12, $TilesetLayer, $CommonType, $UserID) );
    }
  }
  
  # Each element in BlankTileList is a snippet of values (x,y,z,type,size) for each tile
  foreach($BlankTileList as $SqlSnippet){

    list($X, $Y, $Z, $Layer, $Type) = explode(",", $SqlSnippet);

    // blank tiles can be z-12, which means they can fulfil a request
    if($Tileset == 0 && ($Z == 12 || $Z == 8)){
      moveRequest($X, $Y, $Z, NULL, REQUEST_DONE, 0);
    }
    
    # Make a blank tile. Level 12 and 15 are always stored, otherwise we only store tiles not equal to the "common tile"
    if( $Type >= 0 && ($Z == 12 || $Z == 15 || $Type != $CommonType) )
    {
      $Fields = "x, y, z, layer, type, date, user";
      $Values = sprintf("%s, now(), %d", $SqlSnippet, $UserID);

      $SQL = sprintf("replace into `tiles_blank` (%s) values (%s);", $Fields, $Values);
      # Store the stuff to replace in the array
      $Values = sprintf("(%s, now(), %d)", $SqlSnippet, $UserID);
      array_push( $ReplaceList, $Values );
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
  # Execute all queued replacements...
  if( count($ReplaceList) > 0 ) {
    $Fields = "x, y, z, layer, type, date, user";
    $SQL = sprintf( "replace into `tiles_blank` (%s) values %s", $Fields, implode(",",$ReplaceList) );
    mysql_query($SQL);
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
function SaveTilesetMetadata($X,$Y,$Layer,$Count, $UserID, $VersionID, $Size = 0){
  SaveUserStats($UserID, $VersionID, $Count, $Size);
  
  moveRequest($X, $Y, 12, NULL, REQUEST_DONE, 0);
  logMsg("Tileset $X,$Y uploaded at once", 4);
  
  $LayerID = checkLayer($Layer);

  $Fields = "x, y, z, type, size, date, user, version, tileset";
  $Values = sprintf("%d,%d,%d,%d,%d,now(), %d, %d, 1", $X,$Y,12, $LayerID, 0, $UserID, $VersionID);
 
  $SQL = sprintf("replace into `tiles_meta` (%s) values (%s);", $Fields, $Values);
  mysql_query($SQL);
  
  logSqlError();

}

#-----------------------------------------------------------------------------
# Removes completed* tilesets from queue
# * where completed means "z[8|12] was uploaded"
#-----------------------------------------------------------------------------
function RemoveFromQueue($TileList){
  foreach($TileList as $CSV){
    list($X, $Y, $Z, $Layer, $Size) = explode(",", $CSV);
    if($Z == 12 || $Z == 8){
    
      moveRequest($X, $Y, $Z, NULL, REQUEST_DONE, 0);
        
      logMsg(sprintf("Tileset %d, %d (z%d) moved from mode %d to mode %d", $X, $Y, $Z, REQUEST_ACTIVE, REQUEST_DONE), 4);
      logSqlError();
    }
  }
}

#---------------------------------------------------------------------------
# Update user info with their latest upload
#---------------------------------------------------------------------------
function SaveUserStats($UserID, $VersionID, $NumTiles, $NumBytes = 0){
  $SQL = 
    "update `tiles_users` set ".
      "`uploads` = `uploads` + 1, ".
      sprintf("`tiles` = `tiles` + %d, ", $NumTiles).
      sprintf("`bytes` = `bytes` + %d, ", $NumBytes).
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
  
  if(!mkdir($Dir, 0770)){
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
function TempDir($FileIdentifier){
  return(sprintf("/mnt/agami/openstreetmap/tah/temp/%s", $FileIdentifier));
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
