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
  if (!(requestExists($X,$Y,REQUEST_PENDING) or requestExists($X,$Y,REQUEST_NEW) or requestExists($X,$Y,REQUEST_ACTIVE))){
     $SQL = sprintf(
       "INSERT into tiles_queue (`x`,`y`,`status`,`src`,`date`,`priority`) values (%d,%d,%d,'%s',now(),%s);", 
       $X, 
       $Y, 
       REQUEST_PENDING,
       mysql_escape_string($Src),
       $P);
  
  
     mysql_query($SQL);
     if(mysql_error()){
       printf("Database error %s\n", mysql_error());
       exit;
     }
  }
  
  print "OK\n";
?>