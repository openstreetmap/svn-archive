<?php
# Tiles upload handler, accepts ZIP files, saves to database 
# OJW 2006, GNU GPL v2 or later

# All error-messages etc are plain text for use by clients
header("Content-type:text/plain");

# Include a function which checks passwords
include("passwords/passwords.inc");

# Get password from posted form data (format mp=user|pass)
$Password = $_POST["mp"];
list($User,$Pass) = explode("|", $Password);

# If credentials are valid
if(testPassword($Password) == "ok"){
  
  # Connect to the database
  include("../connect/connect.php");

  # Keep going if the user presses stop, to ensure temporary directories get erased
  # see also register_shutdown_function() for another option
  ignore_user_abort();

  # Decide on the name of a Temporary directory
  $Dir = TempDir();
  
  # Check the uploaded ZIP file
  $Size = $_FILES['file']['size'];
  
  $SQL = sprintf("insert into `tiles_log` (`user`,`size`,`time`,`filename`) values('%s','%d','%d','%s');",
    mysql_escape_string($User),
    $Size,
    time(),
    mysql_escape_string($_FILES['file']['name']));
  mysql_query($SQL);
  
  if($Size > 0){
  
    # Create temporary directory
    if(mkdir($Dir)){
      
      # Store the ZIP file
      $Filename = "$Dir/incoming.zip";
      move_uploaded_file($_FILES['file']['tmp_name'], $Filename);
      
      # Uncompress the uploaded tiles
      # -j means to ignore any pathnames in the ZIP file
      # -d $Dir specifies the directory to unzip to
      # $Filename is the zip file
      system("unzip -j -d $Dir $Filename");
      
      # Process all the tiles (return number of tiles done)
      $Count = HandleDir($Dir, $User);
        
      # Delete the temporary directory and everything inside
      DelDir($Dir);
      
      if($Count == 0)
        print "No tiles";
      else
        printf("OK, %d", $Count);
    }
    else
    {
      print "Can't create temp directory";
    }
  }
  else
  {
    print "No file uploaded\n";
  }
}
else
{
  print "No such user/password\n";
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
function HandleDir($Dir, $User){
  $Count = 0;
  $dp = opendir($Dir);
  while(($file = readdir($dp)) !== false){
    $Filename = "$Dir/$file";
    $Count += HandleFile($Filename, $User);
  }
  closedir($dp);
  return($Count);
}

#----------------------------------------------------------------------
# Processes tile PNG images
#----------------------------------------------------------------------
function HandleFile($Filename, $User){
  if(preg_match("/tile_(\d+)_(\d+)_(\d+)\.png/", $Filename, $Matches)){
    $Z = $Matches[1];
    $X = $Matches[2];
    $Y = $Matches[3];
    $Valid = tileValid($X,$Y,$Z);
    if($Valid){
      
      $ImageData = file_get_contents($Filename);
      InsertTile($X,$Y,$Z,$User,$ImageData);

      return(1);
    }
  }
  return(0);
}

function InsertTile($X,$Y,$Z,$User,$ImageData){
  if(!tileValid($X,$Y,$Z)){
	  printf("INVALID %d,%d,%d\n", $X,$Y,$Z);
  	  return;
  }
  
  $SQL = sprintf("delete from tiles where `x`=%d and `y`=%d and `z`=%d;",$X,$Y,$Z); 
  mysql_query($SQL);

  $SQL = sprintf("insert into tiles (`x`,`y`,`z`,`tile`,`user`,`exists`,`date`,`size`) values('%d','%d','%d','%s','%s','1','%d','%d');",
    $X,
    $Y,
    $Z,
    mysql_escape_string($ImageData),
    mysql_escape_string($User),
    time(),
    strlen($ImageData));

  mysql_query($SQL);
  printf("+ %d,%d,%d\n", $X,$Y,$Z);
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

#----------------------------------------------------------------------
# Tests if a tile coordinate is valid
#
# See [[Slippy Map Tilenames]] on openstreetmap wiki for details
#----------------------------------------------------------------------
function tileValid($X,$Y,$Zoom){
  # Tiles below zoom-12 are generated on the server
  # and can't be directly uploaded
  if($Zoom < 12)
    return(0);
    
  # Zoom depth is limited to 17
  if($Zoom > 17)
    return(0);
    
  # Check that the specified x,y exist at this zoom level
  if($X < 0 || $Y < 0)
    return(0);
  $Limit = pow(2,$Zoom);
  if($X >= $Limit || $Y >= $Limit)
    return(0);
  return(1);
}
?>