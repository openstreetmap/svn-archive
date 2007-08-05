<?php
  // This is the 404 handler script, for tiles not present 
  // via mod-rewrite as real files.

  include("../lib/tilenames.inc");
  include("../lib/layers.inc");
  include("../lib/blanktile.inc");
  
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
    BlankTile("black");
    }
  
  // Lookup layer, and check is valid
  $LayerID = checkLayer($Layer);
  if($LayerID < 0){
    BlankTile("error");
    }

  // look for landsea tiles if everything else fails
  if(1){
    $Blank = LookUpBlankTile($X,$Y,$Z,$LayerID);
    switch($Blank){
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
    $CacheDays = 14;
      
    header("Content-type:image/png");
    header('Last-Modified: ' . gmdate('D, j M Y H:i:s T',  $Data["date"]));
    header('Cache-Control: max-age=' . $CacheDays * 86400);
    header('Expires: ' . gmdate('D, j M Y H:i:s T',  time() + $CacheDays * 86400));

    readfile($Filename);
    exit;
  }

?>
