<?php
  /// \file
  /// Hands out render request to tah clients
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
  include_once("../lib/log.inc");
  include("../lib/requests.inc");
  include("../lib/versions.inc");
  include("../lib/users.inc");
  include("../lib/queue.inc");

  $Value = QueueLength();
  $Min = 10;
  $Max = MaxQueueLength();
  
  $Portion = ($Max - $Value) / ($Max - $Min);
  if($Portion < 0) $Portion = 0;
  if($Portion > 1) $Portion = 1;

  // if (rand(1,100) > 100*$Portion) {
  //   printf("XX|%d||||Upload Queue Full",$APIVersion);
  //   exit; # probably Redundant
  // }

  $User = $_POST["user"];
  $Pass = $_POST["pass"];
  $UserID = checkUser($User, $Pass);

  if ($UserID == -1) {
    printf("XX|%d||||client disabled",$APIVersion);
  }

  # If credentials are valid
  if($UserID < 1){
    AbortWithError(401, "Invalid username");
    printf("XX|%d||||Invalid username", ($APIVersion+1));
    exit; # Redundant, failsafe
  }

  # Check whether the client version is allowed to upload
  # (if they can't, there's no point in them taking requests)
  $VersionID = checkVersion($_POST["version"]);
  if($VersionID < 0){
    printf("XX|%d||||client_version_unacceptable",($APIVersion+100)); ## Use a fake Version for the answer to make clients abort loop.
    exit;
  }


  $Z=12;

  # Check which layers the client wants to render
  if ($_POST["layers"] == 'lowzoom'){
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

//  if ($_POST["layerspossible"] == 'default,maplint,lowzoom'){ // should check for "default" and "lowzoom" each instead of entire string.
//    $Z = -1;
//  }

  CheckForRequest($Z, $UserID);
  printf("XX|%d||||nothing_to_do",$APIVersion);
  // THE END

function CheckForRequest($Z, $UserID = 0){
  
  global $APIVersion;
  # next request that is handed out: order by priority, then date.
//  if ($Z >= 0){
    $SQL =  "select `x`,`y`,`status`,`priority`,`date` from `tiles_queue` where `status` = ".REQUEST_PENDING." and `z`=".$Z." order by `priority`,`date` limit 1;";
//  } else {
//    $SQL =  "select `x`,`y`,`status`,`priority`,`date` from `tiles_queue` where `status` = ".REQUEST_PENDING." order by `priority`,`date` limit 1;";
//  }
  
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
    0, $UserID);

  logSqlError();
  exit;
}

/// Exit with an HTTP error code
function AbortWithError($Code, $Message){
  header(sprintf("HTTP/1.0 %d %s", $Code, $Message));
  header("Content-type:text/plain");
  printf("%s\n", $Message);
  exit;
}

?>
