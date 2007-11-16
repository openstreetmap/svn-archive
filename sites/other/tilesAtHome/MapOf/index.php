<?php
#-----------------------------------------------------------------------------
# Creates a map image, by tiling together slippy map images
# Oliver White
# GNU GPL version 2 or later
#-----------------------------------------------------------------------------

include("../lib/tilenames.inc");

if(!tryMap(
  $_GET["lat"], 
  $_GET["long"], 
  $_GET["z"],
  $_GET["w"], 
  $_GET["h"],
  $_GET["format"])){
  CreateForm($_GET);
  }

#-----------------------------------------------------------------------------
# Validate a set of inputs. if ok, then output map to browser, otherwise return 0
#-----------------------------------------------------------------------------
function tryMap($Lat, $Long, $Zoom, $Width, $Height, $Format){
  if($Lat < -90 || $Lat > 90)
    return(0);
  if($Long < -180 || $Long > 180)
    return(0);
  if($Zoom < 4 || $Zoom > 17)
    return(0);
  if($Width < 40 || $Width > 2000)
    return(0);
  if($Height < 40 || $Height > 2000)
    return(0);
  doMap($Lat, $Long, $Zoom, $Width, $Height, $Format);
  return(1);
}
#-----------------------------------------------------------------------------
# Create a map, output it to browser
#-----------------------------------------------------------------------------
function doMap($Lat, $Long, $Zoom, $Width, $Height, $Format){
  $Tilesize = 256;
  $GridSize = 10;
  
  $Image = imagecreatetruecolor($Width, $Height);
  $BG = imagecolorallocate($Image, 255, 255, 255);
  imagefilledrectangle($Image, 0,0,$Width, $Height, $BG);
  
  list($X,$Y) = XY($Lat, $Long, $Zoom);

  $XC = floor($X);
  $YC = floor($Y);
  
  $XA = ($X - $XC) * $Tilesize;
  $YA = ($Y - $YC) * $Tilesize;
  
  for($xi = -$GridSize; $xi <= $GridSize; $xi++){
    for($yi = -$GridSize; $yi <= $GridSize; $yi++){
      $Filename = TileName($X + $xi, $Y + $yi, $Zoom);

      if(file_exists($Filename)){
        $ToX = (floor($Width/2) - $XA + $xi * $Tilesize);
        $ToY = (floor($Height/2) - $YA + $yi * $Tilesize);
  
        if($ToX > -$Tilesize && $ToX < $Width
          && $ToY > -$Tilesize && $ToY < $Height){
          $Part = imagecreatefrompng($Filename);
          imagecopy($Image, $Part, 
            $ToX,
            $ToY, 
            0, 0, 
            $Tilesize, $Tilesize);
          }
        }
    }
  }

  if($Format == "jpeg"){
    header("Content-type: image/JPEG");
    imagejpeg($Image);
  }
  else{
    header("Content-type: image/PNG");
    imagepng($Image);
  }
}

#-----------------------------------------------------------------------------
# Location of map tiles
#-----------------------------------------------------------------------------
function tileURLB($X,$Y,$Z){
  return(sprintf("http://tah.openstreetmap.org/Tiles/tile/%d/%d/%d.png", $Z, $X, $Y));
}
#-----------------------------------------------------------------------------
# Slippy map coordinate functions: should probably use a library
#-----------------------------------------------------------------------------
function XY($Lat, $Long, $Zoom){
  $PortionY = Lat2Y($Lat);
  $PortionX = Long2X($Long);
  $Size = pow(2,$Zoom);
  $X = $PortionX * $Size;
  $Y = $PortionY * $Size;
  return(array($X,$Y));
}

function Long2X($Long){
  return(($Long + 180) / 360);
}
function Lat2Y($Lat){
  $LimitY = 3.14159265358979;
  $Y = ProjectF($Lat);
  
  $PY = ($LimitY - $Y) / (2 * $LimitY);
  return($PY);
}
function ProjectF($Lat){
  $Lat = deg2rad($Lat);
  $Y = log(tan($Lat) + (1/cos($Lat)));
  return($Y);
}

#-----------------------------------------------------------------------------
# HTML form that lets you enter map parameters
#-----------------------------------------------------------------------------
function CreateForm($ParamArray){
  print "<table cellpadding=\"6\" cellspacing=\"0\">";
  print "<form action=\"./\" method=\"GET\">\n";
  
  print TableRow("Map centre lat/long", 
    FormNumericInput("lat", "%1.8f") . FormNumericInput("long", "%1.8f"));
  
  print TableRow("Zoom", DropdownInput("z", range(7, 17)));
  
  print TableRow("Image size, pixels",
    FormNumericInput("w", "%d") . " x " . FormNumericInput("h", "%d"));
    
  print TableRow("Output", DropdownInput("format", array("jpeg", "png")));
  
  print TableRow("&nbsp;", "<input type=\"submit\" value=\"OK\">");
  
  print "</form></table>\n";
}

#-----------------------------------------------------------------------------
# Create a row containing two items: a and b
#-----------------------------------------------------------------------------
function TableRow($A, $B){
 return("<tr><td>$A</td><td>$B</td></tr>\n");
}
#-----------------------------------------------------------------------------
# HTML select field, with a list of options
#-----------------------------------------------------------------------------
function DropdownInput($Name, $Options){
  $Html = "<select name=\"$Name\">\n";
  foreach($Options as $Option){
    $Html .= "<option value=\"$Option\">$Option</option>\n";
  }
  $Html .= "</select>\n";
  return($Html);
}
#-----------------------------------------------------------------------------
# HTML input field
#-----------------------------------------------------------------------------
function FormNumericInput($Name, $Format){
  return(sprintf("<input type=\"text\" name=\"%s\">", $Name));
}
?>
