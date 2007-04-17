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
  include("../../../lib/requests.inc");

  countRequests(REQUEST_PENDING, "pending");
  countRequests(REQUEST_NEW, "new");
  countRequests(REQUEST_ACTIVE, "active");
  countRequests(REQUEST_DONE, "done");

  function countRequests($Status, $Label){    

    // Find old requests in the "active" queue
    $SQL = sprintf("select NULL from `tiles_queue` where `status`=%d;", $Status);

    $Result = mysql_query($SQL);
    $Count = mysql_num_rows($Result);

    printf("%s.value %d\n", $Label, $Count);
  }

  
  ?>
