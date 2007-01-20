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
    print "XX|3||||db_disabled";
    exit;
  }
  
  include("../connect/connect.php");
  
  $SQL = "select x,y from tiles_queue where `sent`=0 limit 1;";
  $Result = mysql_query($SQL);
  
  if(mysql_errno()){
    print "XX|3||||db_error";
    exit;
  }
  if(mysql_num_rows($Result) == 0){
    print "XX|3||||db_nothing_to_do";
    exit;
  }
  
  $Data = mysql_fetch_assoc($Result);
  printf("OK|3|%d|%d|%d|db", 
    $Data["x"],
    $Data["y"],
    12);
    
  # Mark that tileset as "sent to client, but not necessarily done yet"
  $SQL = sprintf("delete from tiles_queue where `x`=%d and `y`=%d;",
    $Data["x"],
    $Data["y"]);
  $Result = mysql_query($SQL);

?>