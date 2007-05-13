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
  include("../lib/versions.inc");

  # Check whether the client version is allowed to upload
  # (if they can't, there's no point in them taking requests)
  $VersionID = checkVersion($_GET["version"]);
  if($VersionID == -1){
    print "XX|3||||client_version_unacceptable";
    exit;
  }

  CheckForRequest(1);
  CheckForRequest(2);
  print "XX|3||||nothing_to_do";


function CheckForRequest($Priority){
  $SQL = sprintf("select `x`,`y`,`status` from `tiles_queue` where (`status`=0 or `status`=1) and `priority`=%d limit 1;",
    $Priority);

//  print "$SQL\n";return;
  $Result = mysql_query($SQL);
  
  if(mysql_errno()){
    print "XX|3||||error";
    exit;
  }
  if(mysql_num_rows($Result) == 0){
    return;
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
    REQUEST_ACTIVE);
    
  logSqlError();
  exit;
}

?>