<?php
  header("Content-type:text/plain");
  $X = $_GET["x"];
  $Y = $_GET["y"];
  $Z = $_GET["z"];
  if (!$Z) {$Z=12};
  $P = $_GET["priority"];
  $Src = $_GET["src"];
  
  if($Z != 12 || $Z != 8){
     print "Invalid Z\n";
     exit;
  }
  $Max = pow(2,$Z);
  if($X < 0 || $Y < 0 || $X >= $Max || $Y >= $Max){
     print "Invalid XY\n";
     exit;
  }
  
  if($P < 1 || $P > 2){
     print "Invalid P\n";
     exit;
  }
  
  include("../connect/connect.php");
  include("../lib/requests.inc");

  if (!requestExists($X,$Y,$Z,NULL)){
     //Check the number of existing requests by that ip address and downgrade if needed
     $SQL = sprintf ("SELECT COUNT(*) FROM tiles_queue WHERE `status` < %d AND `ip` = '%s'",
	REQUEST_DONE,
	$_SERVER['REMOTE_ADDR']);
     $Result = mysql_query($SQL);
     if ($row = mysql_fetch_row($Result) and $row[0] >= 10) {$P = 2;}

     $SQL = sprintf(
       "INSERT into tiles_queue (`x`,`y`,`z`,`status`,`src`,`date`,`priority`,`ip`) values (%d,%d,%d,%d,'%s',now(),%s,'%s');", 
       $X, 
       $Y,
       $Z,
       REQUEST_PENDING,
       mysql_escape_string($Src),
       $P,
       $_SERVER['REMOTE_ADDR']);

     mysql_query($SQL);
     if(mysql_error()){
       printf("Database error %s\n", mysql_error());
       exit;
     }
  }
  
  print "OK\n";
?>