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


  include("../connect/connect.php");
  include("../lib/log.inc");
  include("../lib/requests.inc");
  include("../lib/versions.inc");

  $Z=12;

  # Check which layers the client wants to render
  if ($_GET["layers"] == 'lowzoom'){
    $Z=8;
  } else {
    //Rate limiting: never hand out more than 50 active requests at a time
    $SQL='SELECT COUNT(*) FROM tiles_queue WHERE status='.REQUEST_ACTIVE.';';
    $res = mysql_query($SQL);
    $row=mysql_fetch_row($res);
    if($row[0] > 50){
      print 'XX|3||||rate_limiting ('.$row[0].' requests out)';
      exit;
    }
  }

  # Check whether the client version is allowed to upload
  # (if they can't, there's no point in them taking requests)
  $VersionID = checkVersion($_GET["version"]);
  if($VersionID == -1){
    print "XX|3||||client_version_unacceptable";
    exit;
  }

  CheckForRequest($Z);
  print "XX|3||||nothing_to_do";
  // THE END

function CheckForRequest($Z){
  $SQL =  "select `x`,`y`,`z`,`status`,`priority`,`date` from `tiles_queue` where `status` <= ".REQUEST_NEW;
  $SQL .= " order by `priority`,`date` limit 1;";

//  print "$SQL\n";return;
  $Result = mysql_query($SQL);

  if(mysql_errno()){
    print "XX|3||||error: " . mysql_error();
    exit;
  }
  if(mysql_num_rows($Result) == 0){
    return;
  }

  $Data = mysql_fetch_assoc($Result);
  printf("OK|3|%d|%d|%d|db",
    $Data["x"],
    $Data["y"],
    $Data["z"]);

  moveRequest(
    $Data["x"],
    $Data["y"],
    $Data["z"],
    $Data["status"],
    REQUEST_ACTIVE);

  logSqlError();
  exit;
}

?>
