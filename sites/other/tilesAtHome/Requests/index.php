<?php
  header("Content-type:text/plain");

  if(0){ // Option to kill database access, but keep sending request queries
    printf("OK|3|%d|%d|%d|dev_random",
    	rand(0,4095),
    	rand(0,4095),
	    12);
	exit;
  }
  
  if(0){ // Option to kill request queries
    print "XX|3||||disabled";
    exit;
  }
  
  if(0){
    if(rand(0,3) != 0){
      print "XX|3||||rate_limiting";
      exit;
    }
  }
  
  include("../connect/connect.php");
  include("../lib/log.inc");
  include("../lib/requests.inc");

  $SQL = "select x,y,status from tiles_queue where `status`=0 or `status`=1 limit 1;";
  $Result = mysql_query($SQL);
  
  if(mysql_errno()){
    print "XX|3||||error";
    exit;
  }
  if(mysql_num_rows($Result) == 0){
    print "XX|3||||nothing_to_do";
    exit;
  }
  
  $Data = mysql_fetch_assoc($Result);
  printf("OK|3|%d|%d|%d|db", 
    $Data["x"],
    $Data["y"],
    12);
    
  moveRequest(
    $Data["x"],
    $Data["y"],
    $Data["status"],
    2,
    $_GET["debug"]==1);

?>