<?php
  /* 
  ** OJW 2007
  ** License: GNU GPL v2 or at your option any later version
  */
  header("Content-type:text/plain");
  if(0){
    if($_SERVER["REMOTE_ADDR"] != $_SERVER["SERVER_ADDR"]){
      print "This page can only be run by the dev server\n";
      exit;
    }
  }

  include("../../../connect/connect.php");


  //Show just low priority pending
  $Result = mysql_query("select count(*) as count from `tiles_queue` WHERE status <=1 and z=12 and priority=3;");
  $row = mysql_fetch_array($Result);
  printf("prio_low_pending.value %d\n", $row['count']);

?>
