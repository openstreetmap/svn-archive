<?php
  // Map tile server, serves snippets of map from a database
  //
  // Specification that this program would ideally support later:
  // http://wiki.osgeo.org/index.php/Tile_Map_Service_Specification
  //
  // Copyright 2006, authors:
  // * Oliver White (original version)
  //
  // License: GNU GPL v2 or at your option any later version
  
  
  header("Content-type:image/PNG");
  
  include("../connect/connect.php");


  $URL = $_SERVER["REQUEST_URI"];
  #printf("<p>%s</p>\n", htmlentities($URL));
  
  if(preg_match("/(\d+)\/(\d+)\/(\d+)\.png/", $URL, $Matches)){
    $Z = $Matches[1];
    $X = $Matches[2];
    $Y = $Matches[3];
   
    $SQL = sprintf("select * from tiles where `x`=%d and `y`=%d and `z`=%d;", $X, $Y, $Z);
    #printf("<p>%s</p>\n", htmlentities($SQL));
    
    $Result = mysql_query($SQL);
    if(mysql_error()){
      print "Error";
      exit;
    }
    if(mysql_num_rows($Result) == 0){
      readfile("404.png");
      exit;
    }
  
    $Data = mysql_fetch_assoc($Result);
    
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