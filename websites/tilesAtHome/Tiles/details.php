<?php

header("Content-type: text/plain");
include("../lib/tilenames.inc");
include("../lib/users.inc");
include("../connect/connect.php");


if(preg_match("{/(\d+)/(\d+)/(\d+)\.png_(\w+)}", $_SERVER["REQUEST_URI"], $Matches)){
  
  DisplayDetails(
    $Matches[2],
    $Matches[3],
    $Matches[1],
    $Matches[4]);
}

function DisplayDetails($X,$Y,$Z,$Mode){
  
  // Check tile x,y,z is valid
  if(!TileValid($X,$Y,$Z)){
    FormatOutput(0, $Mode);
    return;
  }
  
  // Optional debug
  if(0)
    printf("Requested %d, %d, %d\n", $X,$Y,$Z);
  
  // Find the tile in the database
  $SQL = sprintf("select size, user, unix_timestamp(`date`) as unixtime from tiles_meta where `x`=%d and `y`=%d and `z`=%d and `type`=1 limit 1", $X,$Y,$Z);
  $Result = mysql_query($SQL);
  
  // Check for errors
  if(mysql_error()){
    FormatOutput(0, $Mode);
    return;
  }
  
  // Check whether anything found
  if(mysql_num_rows($Result) < 1){
    FormatOutput(0, $Mode);
    return;
  }
  
  // Get data
  $Data = mysql_fetch_assoc($Result);
  
  // Format it suitable for the API
  FormatOutput(
    1,
    $Mode,
    $Data["size"],
    $Data["unixtime"],
    lookupUser($Data["user"])
    );
}

function FormatOutput($Exists, $Mode, $Size=0, $Timestamp = 0, $Username=""){
  
  switch($Mode){
    case "details":
      $API = "api_1.0";
      printf("%s|%s|%d|bytes|%s|%d|%s",
        $API,
        $Exists?"EXISTS":"NOSUCH",
        $Size,
        $Username,
        $Timestamp,
        date('Y-m-d\TH:i:s', $Timestamp));
      break;
    case "exists":
      print $Exists?"YES":"NO";
      break;
    default:
      print "Malformatted request";
      break;
    }

}

?>