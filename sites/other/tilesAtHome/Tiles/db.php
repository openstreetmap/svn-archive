<?php
  // This is the 404 handler script, for tiles not present 
  // via mod-rewrite as real files.

  include("../connect/connect.php");
  include("../lib/tilenames.inc");
  include("../lib/layers.inc");
  include("../lib/blanktile.inc");
  
  if(0){ // Option to turn off non-standard tiles
    BlankTile();
    }
  
  $URL = $_SERVER["REQUEST_URI"];
  
  // Look for tile x,y,z (TODO: layer)
  if(!preg_match("/(\w+)(\.php)?\/(-?\d+)\/(-?\d+)\/(-?\d+)\.png(_\w+)?/", $URL, $Matches)){
    BlankTile("error");
  }
  
  $Layer = $Matches[1];
  $Z = $Matches[3];
  $X = $Matches[4];
  $Y = $Matches[5];
  
  // Check x,y,z is valid
  if(!TileValid($X,$Y,$Z)){
    header("HTTP/1.0 404 Not Found");
    header('Expires: Mon, 26 Jul 1997 05:00:00 GMT');
    header('Cache-Control: no-store, no-cache, must-revalidate');
    header('Cache-Control: post-check=0, pre-check=0', false);
    header('Pragma: no-cache');
    exit;
    }
  
  // Lookup layer, and check is valid
  $LayerID = checkLayer($Layer);
  if($LayerID < 0){
    BlankTile("error", TRUE);
    }

  // look for landsea tiles if everything else fails
  if(1){
    $Blank = LookUpBlankTile($X,$Y,$Z,$LayerID);
    if ($Z == 12) {
      $complexity = FindComplexity($X, $Y, $Z);
    } else {
      $complexity = 5;
    }
    switch($Blank){
      case 1:
        BlankTile("sea", FALSE);
        break;
      case 2:
        BlankTile("land", FALSE);
        break;
      default:
        header("HTTP/1.0 302 Found");
        header("Location: http://dev.openstreetmap.org/~ojw/Tiles/tile.php/$Z/$X/$Y.png");
        //BlankTile("unknown", TRUE); // probably shouldn't reach this line
        //Request the render of this missing tile
        if ($Z==12 && $complexity) {
          fopen("http://tah.openstreetmap.org/NeedRender?priority=3&x=$X&y=$Y&z=12&src=server:MissingTile:$complexity","r");
        }
        break;
      }
  }

  function FindComplexity($X, $Y, $Z = 12) {
    $sql = "SELECT * FROM tiles_complexity WHERE x=$X and y=$Y";
    $r = mysql_query($sql); 
    if (mysql_num_rows($r)) {
        $result = mysql_fetch_assoc($r);
        return $result['complexity'];
    }
    return 0;
  }  
  function BlankTile($Type="404", $Error=TRUE){
    $Filename = "Gfx/$Type.png";
    $CacheDays = 14;
          
    header("Content-type:image/png");
    header('Last-Modified: ' . gmdate('D, j M Y H:i:s T',  $Data["date"]));

    if ($Error==TRUE) {
            //Do not cache errors
            header('Expires: Mon, 26 Jul 1997 05:00:00 GMT');
            header('Cache-Control: no-store, no-cache, must-revalidate');
            header('Cache-Control: post-check=0, pre-check=0', false);
            header('Pragma: no-cache');
        } else {
            //Proxy friendly caching
            header('Vary: Accept-Encoding');
            //Cache tile for $CacheDays on proxy and 24 hour client.
            header('Cache-Control: s-maxage='.($CacheDays*86400).', must-revalidate, max-age=86400');
        }

    readfile($Filename);
    exit;
  }

?>
