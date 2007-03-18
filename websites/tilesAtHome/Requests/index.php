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

  $SQL = "select x,y from tiles_queue where `sent`=0 limit 1;";
  $Result = mysql_query($SQL);
  
  if(0){
    logMsg(sprintf("request from version \"%s\", claims to be user \"%s\"", 
      addslashes($_GET["version"]), 
      addslashes($_GET["user"])),
      4);
  }
  
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
    
  # Call this page with ?test=1 to test the interface without downloading anything
  if($_REQUEST["test"] != 1){
    # Mark that tileset as "sent to client, but not necessarily done yet"
    $SQL = sprintf("delete from tiles_queue where `x`=%d and `y`=%d and `sent`=1;",
      $Data["x"],
      $Data["y"]);
    $Result = mysql_query($SQL);
  
    $SQL = sprintf("update tiles_queue set `sent`=1, `date_taken`=now() where `x`=%d and `y`=%d and `sent`=0;",
      $Data["x"],
      $Data["y"]);
    $Result = mysql_query($SQL);
  }

?>