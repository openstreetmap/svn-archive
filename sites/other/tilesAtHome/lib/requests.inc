<?php
define("REQUEST_PENDING", 0);
define("REQUEST_NEW", 1);
define("REQUEST_ACTIVE", 2);
define("REQUEST_DONE", 3);
#include("log.inc");

function deleteRequest($X, $Y, $Mode){

  $SQL = sprintf(
    "delete from `tiles_queue` where `x`=%d and `y`=%d and `status`=%d;",
    $X,
    $Y,
    $Mode);
 
  mysql_query($SQL);
  #logSqlError();
}

#--------------------------------------------------
# Checks whether a certain request (X,Y) exists
# If $Mode is NULL, it checks for all unfinished requests
#--------------------------------------------------
function requestExists($X,$Y,$Mode){
  if ($Mode != NULL)
    // Check specific state
    $SQL = sprintf("select NULL from tiles_queue where `x`=%d and `y`=%d and `status`=%d;",$X,$Y,$Mode);
  else
    //Check for all unfinished requests
    $SQL = sprintf("select NULL from tiles_queue where `x`=%d and `y`=%d and `status`!=%d;",$X,$Y,REQUEST_DONE);

  $Result = mysql_query($SQL);
  if(mysql_error())
    return(0);
  
  return(mysql_num_rows($Result) > 0 ? 1 : 0);
}
function moveRequest($X, $Y, $FromMode, $ToMode, $Debug = 0){

  if(!requestExists($X,$Y,$FromMode))
    return(0);
    
  # Delete any existing request already occupying the new mode (
  deleteRequest($X, $Y, $ToMode);

  # Move the specified request to the new mode
  $SQL = sprintf(
    "update `tiles_queue` set `status`=%d, `retries`=`retries`+1, `date`=now() where `x`=%d and `y`=%d and `status`=%d;",
    $ToMode,
    $X,
    $Y,
    $FromMode);
  
  $Result = mysql_query($SQL);
  #logSqlError();
  
  if($Debug)
    printf("\n$SQL\n".mysql_error()."\n");
  
  return(1);
}

?>