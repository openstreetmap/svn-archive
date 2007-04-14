<?php
  /* 
  ** OJW 2007
  ** License: GNU GPL v2 or at your option any later version
  */
  header("Content-type:text/plain");
  if($_SERVER["REMOTE_ADDR"] != $_SERVER["SERVER_ADDR"]){
	print "This page can only be run by the dev server\n";
	exit;
  }

  include("../../../connect/connect.php");
  include("../../../lib/requests.inc");
    
  for($Status = 0; $Status < 4; $Status++){
    $SQL = sprintf("select NULL from `tiles_queue` where `status`=%d;", $Status);
    $Result = mysql_query($SQL);
    $Count = mysql_num_rows($Result);

    $Date = date('Y-m-d H:00:00');
    $SQL = sprintf("insert into tiles_queue_history (`date`,`status`,`count`) values ('%s',%d,%d);", $Date, $Status, $Count);

    mysql_query($SQL);
    print "$SQL\n".mysql_error()."\n";
  }
  
  ?>
