<?php
  header("Content-type:text/plain");
  include("../connect/connect.php");
  doExport();
  
  function doExport(){
    $SQL = sprintf("select * from tiles_users;");
    $Result = mysql_query($SQL);
    if(mysql_error()){
      print "Error: ". mysql_error();
      return;
    }
    
    if(mysql_num_rows($Result) < 1){
      print "No data - not touching file\n";
      return;
    }
    
    $fp = fopen("../Data/Users/user_pw.txt", "w");
    if(!$fp){
      print "Can't write to file\n";
      return;
    }
    
    while($Data = mysql_fetch_assoc($Result)){
      fputs($fp, sprintf("%d|%s|%s\n", $Data["id"], $Data["name"], $Data["password"]));
    }
    fclose($fp);
    
    print "OK";
  }
?>