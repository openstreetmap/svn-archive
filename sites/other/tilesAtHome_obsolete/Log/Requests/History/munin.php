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

  //Show just high priority pending
  $Result = mysql_query("select count(*) as count from `tiles_queue` WHERE status <=1 and z=12 and priority<3;");
  $row = mysql_fetch_array($Result);
  printf("prio_high_pending.value %d\n", $row['count']);

  $statuses = array('pending','new','active', 'done');
  $Result = mysql_query("select `status`,count(*) as count from `tiles_queue` WHERE z=12 and status > 1 group by `status` order by `status`;");
  while ($row = mysql_fetch_array($Result)) {
    if ($row['status'] == 3) {
      // Divide done by 48 as it represents all that have been done in the last 48 hours.
      printf("done.value %d\n", $row['count']/48);
    } else {
      printf("%s.value %d\n", $statuses[$row['status']],$row['count']);
    }
  }
  $Result = mysql_query("select count(*) as count from `tiles_queue` WHERE status =3 and z=12 and timediff(now(),date)<1;");
  $row = mysql_fetch_array($Result);
  printf("_req_processed_last_hour.value %d\n", $row['count']);
?>
