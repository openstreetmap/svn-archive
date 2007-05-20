<?php
  // This is the 404 handler script, for tiles not present 
  // via mod-rewrite as real files.
  
  include("../lib/tilenames.inc");
  include("../lib/layers.inc");
  
  if(0){ // Option to turn off non-standard tiles
    BlankTile();
    }
  
  $URL = $_SERVER["REQUEST_URI"];
  
  // Look for tile x,y,z (TODO: layer)
  if(!preg_match("/(\w+)\.php\/(\d+)\/(\d+)\/(\d+)\.png(_\w+)?/", $URL, $Matches)){
    BlankTile("error");
  }
  
  $Layer = $Matches[1];
  $Z = $Matches[2];
  $X = $Matches[3];
  $Y = $Matches[4];
  
  // Check x,y,z is valid
  if(!TileValid($X,$Y,$Z)){
    BlankTile("error");
    }
  
  // Lookup layer, and check is valid
  $LayerID = checkLayer($Layer);
  if($LayerID < 0){
    BlankTile("error");
    }

  // Optional: look on old filesystem
  if(0)
    SearchFilesystem($X,$Y,$Z,$LayerID);
  
  // Optional: look for landsea tiles if everything else fails
  if(1)
    SearchDatabase($X,$Y,$Z,$LayerID);
  
  // Look for a file on the filesystem
  function SearchFilesystem($X,$Y,$Z,$LayerID){
    $LayerName = LayerDir($LayerID);
    $Filename = TileName($X,$Y,$Z,$LayerName,1);
    if(file_exists($Filename)){
      header("Content-type: image/PNG");
      readfile($Filename);
      exit;
    }
  }
  
  function SearchDatabase($X,$Y,$Z,$LayerID){
    if(0){ // option to turn-off database lookups
      BlankTile();
    }
    
    $NoExitOnDbFail = 1;
    include("../connect/connect.php");
    
    if(!$DbSuccess){
      BlankTile("database_error");
    }
   
    $SQL = sprintf("select * from tiles_blank where `x`=%d and `y`=%d and `z`=%d and `layer`=%d limit 1;", 
      $X, $Y, $Z, $LayerID);
    #printf("<p>%s</p>\n", htmlentities($SQL));
    
    $Result = mysql_query($SQL);
    if(mysql_error()){
      BlankTile("error");
    }
    
    if(mysql_num_rows($Result) == 0){
	if($Z < 2)
	  BlankTile("404");
	else
          SearchDatabase($X>>1,$Y>>1,$Z-1,$LayerID);
    }
  
    $Data = mysql_fetch_assoc($Result);
    mysql_close();

    $CacheDays = 14;
    switch($Data["type"]){
      case 1:
        BlankTile("sea");
        break;
      case 2:
        BlankTile("land");
        break;
      default:
        BlankTile("unknown"); // probably shouldn't reach this line
        break;
      }
  }
  
  function BlankTile($Type="404"){
    $Filename = "Gfx/$Type.png";
    
    header("Content-type:image/PNG");
    header('Last-Modified: ' . gmdate('D, j M Y H:i:s T',  $Data["date"]));
    header('Cache-Control: ' . $CacheDays * 86400);
    header('Expires: ' . gmdate('D, j M Y H:i:s T',  $Data["date"] + $CacheDays * 86400));

    readfile($Filename);
    exit;
  }

?>
