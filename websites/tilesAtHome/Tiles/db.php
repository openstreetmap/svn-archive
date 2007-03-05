<?php
  header("Content-type:image/PNG");
  
  include("../lib/tilenames.inc");
  
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
  
    if(TileValid($X,$Y,$Z))
    {
      SearchFilesystem($X,$Y,$Z);
      SearchDatabase($X,$Y,$Z);
    }
    else
      readfile("tile_error.png");
    }
  else
  {
    readfile("tile_error.png");
  }
  
  function SearchFilesystem($X,$Y,$Z){
    $Filename = TileName($X,$Y,$Z);
    if(file_exists($Filename)){
    
      readfile($Filename);
      exit;
    }
  }
  
  function SearchDatabase($X,$Y,$Z){
    if(0){
      readfile("tile_error.png");
      exit;
    }
    
    $NoExitOnDbFail = 1;
    include("../connect/connect.php");
    
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