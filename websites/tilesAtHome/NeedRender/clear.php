<?php
  header("Content-type:text/plain");
  
  include("../connect/connect.php");
  $SQL = sprintf("delete from tiles_queue  where `sent`=1;");
  mysql_query($SQL);
  if(mysql_error()){
    printf("Database error %s\n", mysql_error());
    exit;
    }
  
  print "OK\n";
?>