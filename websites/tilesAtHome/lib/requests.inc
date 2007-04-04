<?php
define("REQUEST_PENDING", 0);
define("REQUEST_NEW", 1);
define("REQUEST_ACTIVE", 2);
define("REQUEST_DONE", 3);

function moveRequest($X, $Y, $FromMode, $ToMode, $Debug = 0){
  $Tablename = "tiles_queue";
  
  
  $SqlDelete = sprintf(
    "delete from %s where `x`=%d and `y`=%d and `status`=%d;",
    $Tablename,
    $X,
    $Y,
    $ToMode);
 
  if($Debug)
    printf("\n\n%s", $SqlDelete);
  else
    mysql_query($SqlDelete);

  $SqlMove = sprintf(
    "update %s set `status`=%d, `date`=now() where `x`=%d and `y`=%d and `status`=%d;",
    $Tablename,
    $ToMode,
    $X,
    $Y,
    $FromMode);
  
  if($Debug)
    printf("\n\n%s", $SqlMove);
  else
    $Result = mysql_query($SqlMove);
}

?>