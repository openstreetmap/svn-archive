<?php
  header("Content-type:text/plain");
  if(0){ // Option to kill request queries
    print "XX|3||||db_disabled";
    exit;
  }
  
  include("../connect/connect.php");
  
  $SQL = "select x,y,z from tiles where `todo`=1 limit 1;";
  $Result = mysql_query($SQL);
  
  if(mysql_errno()){
    print "XX|3||||db_error";
    exit;
  }
  if(mysql_num_rows($Result) == 0){
    print "XX|3||||db";
    exit;
  }
  
  $Data = mysql_fetch_assoc($Result);
  printf("OK|3|%d|%d|%d|db", 
    $Data["x"],
    $Data["y"],
    $Data["z"]);
    
  # Mark that tileset as "sent to client, but not necessarily done yet"
  $SQL = sprintf("update tiles set `todo`=2 where `x`=%d and `y`=%d and `z`=%d;",
    $Data["x"],
    $Data["y"],
    $Data["z"]);
  $Result = mysql_query($SQL);

?>