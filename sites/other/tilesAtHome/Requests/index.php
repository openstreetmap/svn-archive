<?php
  header("Content-type:text/plain");

  $APIVersion=3;

  if(0){ // Option to kill database access, but keep sending request queries
    printf("OK|%d|%d|%d|%d|dev_random",
      $APIVersion,
      rand(0,4095),
      rand(0,4095),
      12);
  exit;
  }

  if(0){ // Option to kill request queries
    printf("XX|%d||||disabled",
      $APIVersion);
    exit;
  }


  include("../connect/connect.php");
  include("../lib/log.inc");
  include("../lib/requests.inc");
  include("../lib/versions.inc");

  $Z=12;

  # Check which layers the client wants to render
  if ($_GET["layers"] == 'lowzoom'){
    $Z=8;
  } else {
    //Rate limiting: never hand out more than 350 active requests at a time
    #$SQL='SELECT COUNT(*) FROM tiles_queue WHERE z=12 and status='.REQUEST_ACTIVE.';';
    #$res = mysql_query($SQL);
    #$row=mysql_fetch_row($res);
    #if($row[0] >= 350){
    #  print 'XX|3||||rate_limiting ('.$row[0].' requests out)';
    #  exit;
    #}
  }

  # Check whether the client version is allowed to upload
  # (if they can't, there's no point in them taking requests)
  $VersionID = checkVersion($_GET["version"]);
  if($VersionID < 0){
    printf("OK|%d||||client_version_unacceptable",($APIVersion+1)); ## Use a fake Version for the answer to make clients abort loop, need OK for buggy old clients, can be changed back to "XX" in the next version.
    exit;
  }

  CheckForRequest($Z);
  printf("XX|%d||||nothing_to_do",$APIVersion);
  // THE END

function CheckForRequest($Z){
  
  global $APIVersion;
  # next request that is handed out: order by priority, then date.
  $SQL =  "select `x`,`y`,`status`,`priority`,`date` from `tiles_queue` where `status` = ".REQUEST_PENDING." and `z`=".$Z." order by `priority`,`date` limit 1;";

//  print "$SQL\n";return;
  $Result = mysql_query($SQL);

  if(mysql_errno()){
    printf("XX|%d||||error: %s",
      $APIVersion,
      mysql_error());
    exit;
  }
  if(mysql_num_rows($Result) == 0){
     return;
  }

  $Data = mysql_fetch_assoc($Result);
  printf("OK|%d|%d|%d|%d|db",
    $APIVersion,
    $Data["x"],
    $Data["y"],
    $Z);

  moveRequest(
    $Data["x"],
    $Data["y"],
    $Z,
    $Data["status"],
    REQUEST_ACTIVE,
    0);

  logSqlError();
  exit;
}

?>
