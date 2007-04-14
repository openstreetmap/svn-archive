<html><head>
<title>OpenStreetMap tile browser</title>
<link rel="stylesheet" href="styles.css">
</head>
<body>

<?php
  $x = $_GET["x"];
  $y = $_GET["y"];
  $z = $_GET["z"];
  $tileset = $_GET["tileset"];
  $valid = 1;
  if($z < 0 || $z > 18)
    $valid = $z = 0;
    
  $Max = pow(2,$z);
  if($x < 0 || $x >= $Max)
    $valid = 0;
  if($y < 0 || $y >= $Max)
    $valid = 0;
  
  if(!$valid || ($x+$y+$z == 0)){
      $z = 13;
      $x = 4086;
      $y = 2729;
      }
  
  $Grid = 3;
  $Centre = floor(($Grid + 1) / 2)-1;
  
  TableStart();
  
  # Top row:
  print "<tr>";
  print TableImg("gfx/out.png", LinkTile($x/2,$y/2,$z-1));
  for($i = 0; $i < $Grid; $i++){
    print TableImg(($i == $Centre ? "gfx/N.png" : "gfx/EW.png"), LinkTile($x,$y-2,$z));
    }
  print TableImg("gfx/in.png", LinkTile($x*2,$y*2,$z+1));
  print "</tr>";
  
  # Rows:
  for($iy = 0; $iy < $Grid; $iy++)
  {
    $RelY = $iy - $Centre;
    print "<tr>";
    print TableImg(($iy == $Centre ? "gfx/W.png" : "gfx/NS.png"), LinkTile($x-2,$y,$z));
    for($ix = 0; $ix < $Grid; $ix++)
      {
      $RelX = $ix - $Centre;
      print TableLinkedImage($x + $RelX, $y + $RelY, $z);
      }
    print TableImg(($iy == $Centre ? "gfx/E.png" : "gfx/NS.png"), LinkTile($x+2,$y,$z));
    print "</tr>";
  }

  # Bottom row:
  print "<tr>";
  print TableImg("gfx/C.png");
  for($i = 0; $i < $Grid; $i++){
    print TableImg(($i == $Centre ? "gfx/S.png" : "gfx/EW.png"), LinkTile($x,$y+2,$z));
    }
  print TableImg("gfx/C.png");
  print "</tr>";
      
  list($W, $E) = ProjectL($x, $z);
  list($N, $S) = Project($y, $z);
  
  $Lat = ($N+$S)/2;
  $Long = ($E+$W)/2;
  $URL = sprintf("http://openstreetmap.org/index.html?lat=%f&amp;lon=%f&amp;zoom=%d", 
    $Lat, 
    $Long, 
    12);
  $TileSizeDegrees = ($N-$S);
  $TileSizeKm = 6356 * $TileSizeDegrees / 360;
  $MapSizeKm = $TileSizeKm * $Grid;
  $MapSizeMile = $MapSizeKm / 1.609;
  
  $ZoomOut = LinkTile($x/2,$y/2,$z-1);
  $ZoomIn = LinkTile($x*2,$y*2,$z+1);
  
  printf("<tr><td colspan=\"%d\" class=\"tbl\">Location: %f, %f - <a href=\"%s\">slippy map</a> (<a href=\"%s\">zoom out</a>, <a href=\"%s\">zoom in</a>)</td></tr>",
    $Grid + 2,
    $Lat, 
    $Long,
    $URL,
    $ZoomOut,
    $ZoomIn);
  
  printf("<tr><td colspan=\"%d\" class=\"tbl\">Tilesets: %s</td></tr>",
    $Grid + 2,
    implode(", ", array(
      TilesetLink(0,"Dev database"), 
      TilesetLink(1,"Mapnik"),
      TilesetLink(2,"Bandnet"))));

  TableEnd();
  
 printf("<p class=\"copy\">Copyright &copy; %s, various <a href=\"%s\">OpenStreetMap</a> contributors. <b>Some rights reserved</b>. Licensed as Creative Commons <a href=\"%s\">CC-BY-SA 2.0</a></p>",
  date("Y"),
  "http://www.openstreetmap.org/",
  "http://creativecommons.org/licenses/by-sa/2.0/");
  
  function TilesetLink($Num,$Name){
    global $x,$y,$z;
    return(sprintf("<a href=\"%s\">%s</a>", LinkTile($x,$y,$z,$Num), $Name));
    
  }
  function MoreLink($More){ 
    global $x,$y,$z;
    return(LinkTile($x,$y,$z) . "&amp;$More=yes");
  }
  function TableEnd(){ print "</table>"; }
  function TableStart(){ print "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">"; }
  
  function TableLinkedImage($x,$y,$z){
    return(TableImg(ImageURL($x,$y,$z), LinkTile($x,$y,$z)));
  }
  function ImageURL($x,$y,$z){
    global $tileset;
    if($tileset == "1")
      return(sprintf("http://artem.dev.openstreetmap.org/osm_tiles/%d/%d/%d.png", $z,$x,$y));
    
    if($tileset == "2")
      return(sprintf("http://osmathome.bandnet.org/Tiles/%d/%d/%d.png", $z,$x,$y));

    return(sprintf("../Tiles/tile.php/%d/%d/%d.png",$z,$x,$y));
  }
  function LinkTile($X,$Y,$Z,$linkTileset=-1){
    global $tileset;
    if($linkTileset == -1)
      $linkTileset = $tileset;
    return(sprintf("./?x=%d&amp;y=%d&amp;z=%d&amp;tileset=%d",$X,$Y,$Z,$linkTileset));
  }
  function TableRow($Blocks){
    return("<tr><td>".implode("</td><td>", $Blocks) . "</td></tr>");
  }
  function TableImg($Img="",$URL=""){
    $Html = "<img src=\"$Img\" border=\"0\" />";
    if($URL)
      $Html = "<a href=\"$URL\">$Html</a>";
    $Html = "  <td>$Html</td>\n";
    return($Html);
  }

function ProjectF($Lat){
  $Lat = deg2rad($Lat);
  $Y = log(tan($Lat) + (1/cos($Lat)));
  return($Y);
}
function Project($Y, $Zoom){
  $LimitY = ProjectF(85.0511);
  $RangeY = 2 * $LimitY;
  
  $Unit = 1 / pow(2, $Zoom);
  $relY1 = $Y * $Unit;
  $relY2 = $relY1 + $Unit;
  
  $relY1 = $LimitY - $RangeY * $relY1;
  $relY2 = $LimitY - $RangeY * $relY2;
    
  $Lat1 = ProjectMercToLat($relY1);
  $Lat2 = ProjectMercToLat($relY2);
  return(array($Lat1, $Lat2));  
}
function ProjectMercToLat($MercY){
  return(rad2deg(atan(sinh($MercY))));
}
function ProjectL($X, $Zoom){
  $Unit = 360 / pow(2, $Zoom);
  $Long1 = -180 + $X * $Unit;
  return(array($Long1, $Long1 + $Unit));  
}
?>

</body>
</html>