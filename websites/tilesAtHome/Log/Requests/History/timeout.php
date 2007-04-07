<?php
  /* 
  ** OJW 2007
  ** License: GNU GPL v2 or at your option any later version
  */
  header("Content-type:text/plain");
  if(1){
    if($_SERVER["REMOTE_ADDR"] != $_SERVER["SERVER_ADDR"]){
      print "This page can only be run by the dev server\n";
      exit;
    }
  }

  include("../../../connect/connect.php");
  include("../../../lib/requests.inc");
    
  // Find old requests in the "active" queue
  $AgeLimit = 6; // hours
  $SQL = sprintf(
    "select * from `tiles_queue` where `date` < date_sub(now(), INTERVAL %d HOUR) and `status`=%d;", 
    $AgeLimit,
    REQUEST_ACTIVE);

  print $SQL . "\n";

  $Result = mysql_query($SQL);
  $Count = mysql_num_rows($Result);
  printf("%d items found. %s\n", $Count, mysql_error());

  // For each request that hasn't been done after x hours...
  while($Data = mysql_fetch_assoc($Result)){

    // ...move it to the "new requests" queue
    moveRequest($Data["x"], $Data["y"], REQUEST_ACTIVE, REQUEST_NEW);

    // Display some details to the browser
    printf("\n----\n%d,%d (%d) at %s. %s\n", 
      $Data["x"], 
      $Data["y"], 
      $Data["status"], 
      $Data["date"], 
      mysql_error());
  }

  
  ?>
