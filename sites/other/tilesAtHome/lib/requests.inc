<?php
define("REQUEST_PENDING", 0);
define("REQUEST_NEW", 1);
define("REQUEST_ACTIVE", 2);
define("REQUEST_DONE", 3);
#include("log.inc");

#--------------------------------------------------
# Checks whether a certain request (X,Y,Z) exists
# If $Mode is NULL, it checks for all unfinished requests
#--------------------------------------------------
function requestExists($X,$Y,$Z,$Mode){
  if ($Mode != NULL)
    // Check specific state
    $SQL = sprintf("select NULL from tiles_queue where `x`=%d and `y`=%d and `z`=%d and `status`=%d;",$X,$Y,$Z,$Mode);
  else
    //Check for all unfinished requests
    $SQL = sprintf("select NULL from tiles_queue where `x`=%d and `y`=%d and `z`=%d and `status`!=%d;",$X,$Y,$Z,REQUEST_DONE);

  $Result = mysql_query($SQL);
  if(mysql_error())
    return(0);
  
  return(mysql_num_rows($Result) > 0 ? 1 : 0);
}

#--------------------------------------------------
# Moves all requests (X,Y) (normally only 1) from Mode $FromMode to
# status $ToMode. If $FromMode is NULL it moves all unfinished requests.
# If $Create!=0 then create a request if none had existed
#--------------------------------------------------
function moveRequest($X, $Y, $Z, $FromMode, $ToMode, $Create = 0, $UserID = 0){

  if(!requestExists($X,$Y,$Z,$FromMode)) {
    if ($Create) {
      $SQL = "INSERT INTO `tiles_queue` (`status`,`x`,`y`,`z`,`ip`, `user`) VALUES($ToMode,$X,$Y,$Z,NULL, $UserID);";
      mysql_query($SQL);
      return(1);
    }

    return(0);
  }
  
  if($FromMode != NULL) {
    $time_column = "active_date";
    if ($FromMode == 2) {
        $time_column = "complete_date";
    }    
    # Move the specified request to the new mode
    $SQL = sprintf(
      "update `tiles_queue` set `status`=%d, `date`=now(), `%s`=now(), `user`=%s where `x`=%d and `y`=%d and `z`=%d and `status`=%d;",
      $ToMode,
      $time_column,
      $UserID,
      $X,
      $Y,
      $Z,
      $FromMode);
  } else {
    // Move all unfinished requests to new mode
    $SQL = sprintf(
      "update `tiles_queue` set `status`=%d, `date`=now() where `x`=%d and `y`=%d and `z`=%d and `status` < %d;",
      $ToMode,
      $X,
      $Y,
      $Z,
      REQUEST_DONE);
  }

  $Result = mysql_query($SQL);
  #logSqlError();
  
  return(1);
}

function request_info($X, $Y, $Z) {
    $query = "SELECT * FROM tiles_queue WHERE x=$X AND y=$Y AND z=$Z ORDER BY date DESC";
    $Result = mysql_query($query);
      
    if (mysql_num_rows($Result) != 0) {
      $data = mysql_fetch_assoc($Result);
      return $data;
    }
    return NULL;
}

?>
