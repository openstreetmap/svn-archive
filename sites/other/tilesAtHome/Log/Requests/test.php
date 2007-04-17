<?php
  include("../../connect/connect.php");
  include("../../lib/requests.inc");
  
  showTileRequestStatus($_GET["x"], $_GET["y"]);
  
  function showTileRequestStatus($X,$Y){
    header("Content-type:text/plain");
    printf("OK|%d|%d|%s\n", $X, $Y, tileRequestStatus($X,$Y));
    }
  
  function tileRequestStatus($X,$Y){
    $SQL = sprintf(
      "select * from tiles_queue where `x`=%d and `y`=%d order by `status` limit 1;",
      $X,
      $Y);
    
    $Result = mysql_query($SQL);
    if(mysql_num_rows($Result) < 1){
      return("0|NOT_REQUESTED");
      }
    
    $Data = mysql_fetch_assoc($Result);
    switch($Data["status"]){
      case REQUEST_PENDING:
      case REQUEST_NEW:
        return("1|REQUESTED");
      case REQUEST_ACTIVE:
        return("1|RENDERING");
      case REQUEST_DONE:
        return("0|HISTORICAL");
      default:
        return("0|ERROR_UNKNOWN_STATUS");
    }
  }
    

?>