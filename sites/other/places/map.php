<?php include("../Connect/connect.inc");

$Lat = $_GET["Lat"];
$Long = $_GET["Long"];
$DLon = Limit($_GET["dLong"] * 2, 1E-3, 360); // Width of coverage
$DLat = Limit($_GET["dLat"] * 2, 1E-3, 180); // Height of coverage

$W = Limit($Long - 0.5 * $DLon, -180, 180);  // West of coverage area
$N = Limit($Lat + 0.5 * $DLat, -90, 90);    // North of coverage area
$Width = Limit($_GET["width"], 50, 1000); // Image w,h
$Height = Limit($_GET["height"], 50, 1000);
$S = 2; // Blob half-size in pixels

if(0){
  header("Content-type:text/plain");
  printf("%f, %f");
}

$Image = imagecreatetruecolor($Width, $Height);
$Basemap = imagecreatefromjpeg("../Basemaps/earth_1024.jpg");

if(!$Image)
  exit;


if($Basemap){
  $BW = imagesx($Basemap); // Basemap w,h
  $BH = imagesy($Basemap);
  imagecopyresampled(
    $Image, 
    $Basemap, 
    0,  // Destination x,y
    0, 
    $BW * ($W + 180) / 360,  // Source x,y
    $BH * (90 - $N) / 180, 
    $Width,  // Destination w,h
    $Height, 
    $BW * $DLon / 360,  // Source w,h
    $BH * $DLat / 180);
}

$Colour = ImageColorAllocate($Image, 255,255,0);
$Result = mysql_query("select * from places2;"); 
if($Result){
  while($Details = mysql_fetch_assoc($Result)){
    $X = $Width * ($Details["lon"] - $W) / $DLon;
    $Y = $Height * ($N - $Details["lat"]) / $DLat;
    imagefilledrectangle($Image,$X-$S,$Y-$S,$X+$S,$Y+$S,$Colour);
  }
} 


if($Debug)
  exit;

function Limit($Val, $Min, $Max){
  if($Val < $Min)
    return($Min);
  if($Val > $Max)
    return($Max);
  return($Val + 0);
}

header("Content-type:image/JPEG");
imagejpeg($Image);
 
?>
