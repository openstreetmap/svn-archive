<?php
  header("Content-type:text/plain");
  $X = $_GET["x"];
  $Y = $_GET["y"];
  $Z = 12;
  $P = $_GET["priority"];
  $Src = $_GET["src"];
  
  if($Z < 12 || $Z > 17){
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

  if (!requestExists($X,$Y,NULL)){
     $SQL = sprintf(
       "INSERT into tiles_queue (`x`,`y`,`status`,`src`,`date`,`priority`,`ip`) values (%d,%d,%d,'%s',now(),%s,'%s');", 
       $X, 
       $Y, 
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