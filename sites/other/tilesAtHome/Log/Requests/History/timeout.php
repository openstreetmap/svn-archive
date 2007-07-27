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

  timeout(REQUEST_ACTIVE, 24, "delete");
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
    printf("Timeout requests of status %d more than %1.1f hours old, %s:\n", $Status, $MaxAge, $Effect);

    switch($Effect){
      case "restart":
         // ...move it to the "new requests" queue
         // TODO extend move request to be able to handle multiple requests at once
	 // then use  moveRequest($Data["x"], $Data["y"], $Data["status"], REQUEST_NEW);
         // Find old requests in the "active" queue
         $SQL = sprintf("UPDATE `tiles_queue` set `status`=%d where `date` < date_sub(now(), INTERVAL %d HOUR) and `status`=%d;", 
            $MaxAge,
            $Status,
            REQUEST_NEW);
          break;
         // Finished handling the restart case

      case "delete":
          //Simply time out and delete still existing requests
          // TODO see above. Make deleteRequest be able to handle multiple requests
          // and use deleteRequest($Data["x"], $Data["y"], $Data["status"]);
          // LOW_PRIO and QUICK don't help with InnoDB but can't hurt either
          $SQL = sprintf("DELETE LOW_PRIORITY QUICK from `tiles_queue` where `date` < date_sub(now(), INTERVAL %d HOUR) and `status`=%d;", 
            $MaxAge,
            $Status);
          break;
          // Finished handling the delete case here

      default:
          print "Error: unknown effect\n";
          break;

    } // end switch(Effect)      

    print $Result = mysql_query($SQL);
    $Count = mysql_affected_rows();

    // print some stats (do we want to use msglog() here?
    //printf ("%s %d tiles of status %d",$Effect,$Count,$Status);

  }   // end function timeout
  
  ?>
