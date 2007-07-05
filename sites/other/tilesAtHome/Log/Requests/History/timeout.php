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

  // timeout(REQUEST_ACTIVE, 6, "restart");
  timeout(REQUEST_DONE, 2 * 24, "delete");

  #---------------------------------------------------------------------------
  # timeout a set of requests
  # 
  # Parmameters:
  #   Status - what status a request must be, to be considered
  #   MaxAge - how many hours old the request must be
  #   Effect - what happens to it when this age is reached
  #     : "restart" - put it back in the queue
  #     : "delete" - delete it
  #--------------------------------------------------------------------------
  function timeout($Status, $MaxAge, $Effect){    
    printf("Requests of status %d more than %1.1f hours old, %s:\n", $Status, $MaxAge, $Effect);

    // Find old requests in the "active" queue
    $SQL = sprintf(
      "select * from `tiles_queue` where `date` < date_sub(now(), INTERVAL %d HOUR) and `status`=%d;", 
      $MaxAge,
      $Status);

    $Result = mysql_query($SQL);
    $Count = mysql_num_rows($Result);

    // For each request that hasn't been done after x hours...
    while($Data = mysql_fetch_assoc($Result)){
      printf("  - %d, %d, %d\n", $Data["x"], $Data["y"], $Data["status"]);

      switch($Effect){
        case "restart":
          // ...move it to the "new requests" queue
          moveRequest($Data["x"], $Data["y"], $Data["status"], REQUEST_NEW);
          break;
        case "delete":
          deleteRequest($Data["x"], $Data["y"], $Data["status"]);
          break;
        default:
          print "Error: unknown effect\n";
          break;
      }

    }

  }

  
  ?>
