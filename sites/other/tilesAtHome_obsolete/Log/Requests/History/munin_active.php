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

  $statuses = array('','high','medium','low');
  $Result = mysql_query("select `priority`,count(*) as count from `tiles_queue` WHERE z=12 and status = 2 group by `priority` order by `priority` asc;");
  while ($row = mysql_fetch_array($Result)) {
      printf("%s.value %d\n", $statuses[$row['priority']],$row['count']);
  }
?>
