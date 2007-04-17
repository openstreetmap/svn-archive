<?php
define("REQUEST_PENDING", 0);
define("REQUEST_NEW", 1);
define("REQUEST_ACTIVE", 2);
define("REQUEST_DONE", 3);

function deleteRequest($X, $Y, $Mode){
  printf("Deleting request $X, $Y, $Mode\n");

  $SQL = sprintf(
    "delete from tiles_queue where `x`=%d and `y`=%d and `status`=%d;",
    $X,
    $Y,
    $Mode);
 
  mysql_query($SQL);
}

function moveRequest($X, $Y, $FromMode, $ToMode, $Debug = 0){

  # Delete any existing request already occupying the new mode  
  deleteRequest($X, $Y, $ToMode);  

  # Move the specified request to the new mode
  $SqlMove = sprintf(
    "update tiles_queue set `status`=%d, `date`=now() where `x`=%d and `y`=%d and `status`=%d;",
    $ToMode,
    $X,
    $Y,
    $FromMode);
  
  if($Debug)
    printf("\n\n%s", $SqlMove);
  $Result = mysql_query($SqlMove);
}

?>