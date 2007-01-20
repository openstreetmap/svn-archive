<?php

  if(0){ // Option to turn off tile browsing
    readfile("tile_maintenance.png");
    exit;
    }
      
  $URL = $_SERVER["REQUEST_URI"];
  #printf("<p>%s</p>\n", htmlentities($URL));
  
  if(preg_match("/(\d+)\/(\d+)\/(\d+)\.png(_\w+)?/", $URL, $Matches)){
    $Z = $Matches[1];
    $X = $Matches[2];
    $Y = $Matches[3];
  
    $NoExitOnDbFail = 1;
    include("../connect/connect.php");
    
    if($Matches[4] == "_details"){
      if(!$DbSuccess){
        print "Unknown - no database connection\n";
        exit;
      }
      $SQL = sprintf("select `exists`,`size`,`user`,`date` from tiles where `x`=%d and `y`=%d and `z`=%d;", $X, $Y, $Z);
      $Result = mysql_query($SQL);
      header("Content-type:text/plain");
      if(mysql_error())
        print "Error";
      else
        {
        if(mysql_num_rows($Result) > 0)
          $Data = mysql_fetch_assoc($Result);
        else
          $Data = array();
          
        printf("api_1.0|%s|%d|%s|%s|%d|%s",
         $Data["exists"] ? "EXISTS":"NOSUCH",
         $Data["size"],
         "bytes",
         addslashes($Data["user"]),
         $Data["date"],
         date("Y-m-d\TH:i:s", $Data["date"]));
        }
      exit;
    }
    
    if($Matches[4] == "_exists"){
      if(!$DbSuccess){
        print "Unknown - no database connection\n";
        exit;
      }
      
      $SQL = sprintf("select NULL from tiles where `x`=%d and `y`=%d and `z`=%d and `exists`=1;", $X, $Y, $Z);
      $Result = mysql_query($SQL);
      
      header("Content-type:text/plain");
      print(mysql_num_rows($Result) == 0 ? "NO" : "YES");
      exit;
    }
   
      if(!$DbSuccess){
        readfile("tile_error.png");
        exit;
      }
   
    $SQL = sprintf("select * from tiles where `x`=%d and `y`=%d and `z`=%d limit 1;", $X, $Y, $Z);
    #printf("<p>%s</p>\n", htmlentities($SQL));
    
    $Result = mysql_query($SQL);
    if(mysql_error()){
      readfile("tile_error.png");
      exit;
    }
    
    header("Content-type:image/PNG");
    if(mysql_num_rows($Result) == 0){
      readfile("404.png");
      exit;
    }
  
    $Data = mysql_fetch_assoc($Result);
    mysql_close();
    
    if($Data["exists"] == 0){
      readfile("404.png");
      exit;
    }
    
    $CacheDays = 14;
    header('Last-Modified: ' . gmdate('D, j M Y H:i:s T',  $Data["date"]));
    header('Cache-Control: ' . $CacheDays * 86400);
    header('Expires: ' . gmdate('D, j M Y H:i:s T',  $Data["date"] + $CacheDays * 86400));
    
    print $Data["tile"]; 
  }
?>