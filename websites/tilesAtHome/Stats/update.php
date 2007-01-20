<?php
	
header("Content-type:text/plain");

if($_SERVER["REMOTE_ADDR"] != $_SERVER["SERVER_ADDR"]){
	print "This page can only be run by the dev server\n";
	exit;
}
# Database connection
include("../connect/connect.php");
include("../lib/log.inc");

logMsg("Starting to update stats", 3);

// How long can the script run for
set_time_limit(3 * 3600);

// Choose a filename
$Filename = "Data/stats.txt";
$CompressedFilename = $Filename . ".gz";

// Do the stats update (and time the result)
$Start = time();
doStats($Filename);
$End = time();

$TextSize = filesize($Filename);

# Delete any existing compressed file
if(file_exists($CompressedFilename)){
  unlink($CompressedFilename);
}

# Compress the stats file
system("gzip $Filename");

$GzipSize = filesize($CompressedFilename);

# Delete text file if it couldn't be compressed
if(file_exists($Filename)){
  logMsg("Forced to delete $Filename", 2);
  unlink($Filename);
}

# Inform the client, and the logfile
$MB = 1024*1024;
print "OK";
logMsg(sprintf(
  "Finished updating stats (%d seconds) - %1.2f MB text, %1.2f MB compressed", 
  $End-$Start,
  $TextSize / $MB,
  $GzipSize / $MB
  ), 3);

#----------------------------------------------------------------------
# Generate a list of tiles, save to disk
#----------------------------------------------------------------------
function doStats($Filename){
    
  # Ask the database for the list of tile details
  $SQL = "SELECT `x`,`y`,`z`,`size`,`user`,`date` FROM tiles;";
  
  $Result = mysql_query($SQL);
  if(mysql_error()){
    print "Error";
    logMsg("MySQL error in stats: " . mysql_error(), 2);
    return;
    }

  # Output file
  $fp = fopen($Filename,"w");
  if(!$fp){
    logMsg("Can't write to stats file $Filename", 2);
    return;
    }
  
  # For each tile...
  while($Data = mysql_fetch_assoc($Result)){
  
    # Store as one line of text:
    # - x,y,z (see [[Slippy Map Tilenames]] on OSM wiki for details)
    # - username who uploaded it
    # - image size in bytes
    # - date image was created (still stored as unix timestamp in the database)
    fputs($fp, sprintf("%d,%d,%d,%s,%d,%d\n", 
      $Data["x"],
      $Data["y"],
      $Data["z"],
      $Data["user"],
      $Data["size"],
      $Data["date"]));
  }
  
  fclose($fp);
}
