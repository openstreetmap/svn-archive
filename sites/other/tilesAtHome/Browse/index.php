<?php
  $x = $_GET["x"];
  $y = $_GET["y"];
  $z = $_GET["z"];
  $layer = ($_GET["layer"]>''?$_GET["layer"]:'tile');
  
  include_once("../lib/tilenames.inc");
  
  if(array_key_exists("lat", $_GET)){
    list($x,$y,$z) = ll2xyz($_GET["lat"], $_GET["lon"]);
  }
  $valid = 1;
  if($z < 0 || $z > 18)
    $valid = $z = 0;
    
  $Max = pow(2,$z);
  if($x < 0 || $x >= $Max)
    $valid = 0;
  if($y < 0 || $y >= $Max)
    $valid = 0;
  
  if(!$valid || ($x+$y+$z == 0)){
      // Default start view
      $z = 9;
      $x = 255;
      $y = 170;
      }
  
  $Grid = 3;
  $Centre = floor(($Grid + 1) / 2)-1;

  
  $Title = sprintf("OpenStreetMap tile browser (%d,%d at zoom %d)",$x,$y,$z);
  $Robots = "<meta name=\"robots\" content=\"nofollow,noindex\">";
  print("<html><head><title>$Title</title>\n<link rel=\"stylesheet\" href=\"styles.css\">\n$Robots\n</head>\n<body>\n");
  
  TableStart();
  
  # Top row:
  print "<tr>";
  print TableImg("gfx/out.png", LinkTile($x/2,$y/2,$z-1), 29, 29);
  for($i = 0; $i < $Grid; $i++){
    print TableImg(($i == $Centre ? "gfx/N.png" : "gfx/EW.png"), LinkTile($x,$y-2,$z), 256, 29);
    }
  print TableImg("gfx/in.png", LinkTile($x*2,$y*2,$z+1), 29, 29);
  print "</tr>";
  
  # Rows:
  for($iy = 0; $iy < $Grid; $iy++)
  {
    $RelY = $iy - $Centre;
    print "<tr>";
    print TableImg(($iy == $Centre ? "gfx/W.png" : "gfx/NS.png"), LinkTile($x-2,$y,$z), 29, 256);
    for($ix = 0; $ix < $Grid; $ix++)
      {
      $RelX = $ix - $Centre;
      print TableLinkedImage($x + $RelX, $y + $RelY, $z, 256, 256);
      }
    print TableImg(($iy == $Centre ? "gfx/E.png" : "gfx/NS.png"), LinkTile($x+2,$y,$z), 29, 256);
    print "</tr>";
  }

  # Bottom row:
  print "<tr>";
  print TableImg("gfx/C.png", "", 29, 29);
  for($i = 0; $i < $Grid; $i++){
    print TableImg(($i == $Centre ? "gfx/S.png" : "gfx/EW.png"), LinkTile($x,$y+2,$z), 256, 29);
    }
  print TableImg("gfx/C.png", "", 29, 29);
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
  
  $LatLongURL = sprintf(
    "./?lat=%1.3f&lon=%1.3f", 
    $Lat, 
    $Long);
    
  $LatLongHTML = sprintf(
    "Location: <a href=\"%s\">%f, %f</a>", 
    $LatLongURL,
    $Lat, 
    $Long);
  
  $TileDetailsURL = sprintf(
    "../Tiles/info.php?z=%d&amp;x=%d&amp;y=%d&layer=%s",
    $z,
    $x,
    $y,
    $layer);
  
  $PrintableURL = sprintf(
    "http://tah.openstreetmap.org/MapOf/index.php?lat=%d&amp;long=%d&amp;z=%d&amp;w=800&amp;h=600&amp;format=jpeg",
      $Lat, $Lon, $z);
  
  $DataURL = sprintf(" http://www.openstreetmap.org/api/0.4/map?bbox=%f,%f,%f,%f",
    $W,$S,$E,$N);
  
  $Tools1HTML = sprintf(
    "<a href=\"%s\">slippy map</a> (<a href=\"%s\">zoom out</a>, <a href=\"%s\">zoom in</a>) <a href=\"%s\">tile details</a>",
    $URL,
    $ZoomOut,
    $ZoomIn,
    $TileDetailsURL);
  
  $Tools2HTML = sprintf(
    "<a href=\"%s\">Printable version</a>, <a href=\"%s\">Data</a>",
    $PrintableURL,
    $DataURL);
    
  printf("<tr><td colspan=\"%d\" class=\"tbl\">%s - %s</td></tr>\n",
    $Grid + 2,
    $LatLongHTML,
    $Tools1HTML);
  printf("<tr><td colspan=\"%d\" class=\"tbl\">%s</td></tr>\n",
    $Grid + 2,
    $Tools2HTML);
  
  printf("<tr><td colspan=\"%d\" class=\"tbl\">Display layer: %s</td></tr>\n",
    $Grid + 2,
    implode(", ", array(
      TilesetLink("tile","Tile@Home"), 
      TilesetLink("mapnik","Mapnik"),
      TilesetLink("cycle","Experimental lowzoom"),
      TilesetLink("maplint","maplint")
      )));
  
  if($z >= 12){
    printf("<tr><td colspan=\"%d\" class=\"tbl\">%s</td></tr>\n",
      $Grid + 2,
      UpdateForm($x,$y,$z));
  }
  
  TableEnd();
  
  printf("<p class=\"copy\">Copyright &copy; %s, various <a href=\"%s\">OpenStreetMap</a> contributors.\n<b>Some rights reserved</b>.\nLicensed as Creative Commons <a href=\"%s\">CC-BY-SA 2.0</a></p>\n",
    date("Y"),
    "http://www.openstreetmap.org/",
    "http://creativecommons.org/licenses/by-sa/2.0/");
  
  # Optional form, to let you type in lat/long
  if(0){
    printf("<p><form action=\"./\" method=\"get\">\n");
    printf("Lat/long: <input type=\"text\" name=\"lat\" value=\"%1.3f\" size=\"8\">,\n", $Lat);
    printf("<input type=\"text\" name=\"lon\" value=\"%1.3f\" size=\"8\">\n", $Long);
    printf("<input type=\"submit\" value=\"Go\">\n");
    printf("</form></p>\n");
  }
  
  function UpdateForm($X,$Y,$Z){
    if($Z < 12) return("");
    while($Z > 12){
      $X = floor($X / 2);
      $Y = floor($Y / 2);
      $Z--;
    }
    $Html = "<form action=\"../NeedRender/\" method=\"get\">\n";
    $Html .= "<input type=\"hidden\" name=\"src\" value=\"user_req\">\n";
    $Html .= "<input type=\"hidden\" name=\"priority\" value=\"1\">\n";
    $Html .= "<input type=\"hidden\" name=\"x\" value=\"$X\">\n";
    $Html .= "<input type=\"hidden\" name=\"y\" value=\"$Y\">\n";
    $Html .= "<input type=\"submit\" value=\"Request update\">\n";
    $Html .= "</form>\n";
    return($Html);
    }
  function TilesetLink($layerDir,$displayName){
    global $x,$y,$z;
    return(sprintf("<a href=\"%s\">%s</a>", LinkTile($x,$y,$z), $displayName));
    
  }
  function MoreLink($More){ 
    global $x,$y,$z;
    return(LinkTile($x,$y,$z) . "&amp;$More=yes");
  }
  function TableEnd(){ print "</table>"; }
  function TableStart(){ print "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">"; }
  
  function TableLinkedImage($x,$y,$z,$w,$h){
    global $layer;
    return(TableImg(ImageURL($x,$y,$z,$layer), LinkTile($x,$y,$z),$w,$h));
  }

  function ImageURL($x,$y,$z,$layerdir){
    if($layerdir == "mapnik")
      return(sprintf("http://tile.openstreetmap.org/%d/%d/%d.png", $z,$x,$y));
    else
      return(TileURL($x,$y,$z,$layerdir));
  }

  function LinkTile($X,$Y,$Z){
    global $layer;
    return(sprintf("./?x=%d&amp;y=%d&amp;z=%d&amp;layer=%s",$X,$Y,$Z,$layer));
  }
  function TableRow($Blocks){
    return("<tr><td>".implode("</td><td>", $Blocks) . "</td></tr>");
  }
  function TableImg($Img="",$URL="",$w,$h){
    $Html = "<img src=\"$Img\" border=\"0\" width=\"$w\" height=\"$h\"/>";
    if($URL)
      $Html = "<a href=\"$URL\">$Html</a>";
    $Html = "  <td>$Html</td>\n";
    return($Html);
  }

  function ll2xyz($Lat, $Long){
    $z = 12;
    $Size = pow(2,$z);
    $pi = 3.1415926535;
    $x = floor($Size * ($Long + 180) / 360);
    $y = floor($Size * ($pi -ProjectF($Lat)) / (2 * $pi));
    printf("<!-- translating %f,%f to %1.3f,%1.3f,%1.3f -->", $Lat, $Long, $x,$y,$z);
    return(array($x,$y,$z));
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