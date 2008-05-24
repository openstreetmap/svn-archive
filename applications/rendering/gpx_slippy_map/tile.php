<?php

include("route.php");
include("gpx.php");
include("projection.php");

$W = $H = 256;
$Image = imagecreatetruecolor($W,$H);
$BG = imagecolorallocate($Image,255,255,0);
imagecolortransparent($Image, $BG);
imagefilledrectangle($Image,0,0,$W,$H,$BG);

$GPX = $_GET["gpx"] + 0;

$Filename = getGpx($GPX);
# $Filename = doRoute(0,0,0,0)

if(preg_match("{(\d+)/(\d+)/(\d+)}", $_GET['t'], $Matches))
{
makeTile(
  $Image, 
  $W,
  $H, 
  $Matches[2] + 0,
  $Matches[3] + 0,
  $Matches[1] + 0,
  $Filename);
}
#exit;
header("Content-type:image/PNG");
imagepng($Image);


function makeTile($Image,$W,$H,$x,$y,$z,$Filename)
{
  #imagestring($Image,4,10,10,"$x,$y,$z", $Image);return;
  
  if($z > 19)
    return;
  if(!tileValid($x,$y,$z))
    return;

  $a = proj_init($x,$y,$z,$W,$H);
  #header("Content-type:text/plain");print_r($a);
  $FG = imagecolorallocate($Image,0,0,50);

  $s = 2;
  
  $count = 0;
  $fp = fopen($Filename, "rb");
  if($fp)
  {
    $num = filesize($Filename) / 16;
    for($ii = 0; $ii < $num; $ii++)
    {
      #$line = fgets($fp, 400);
      #list($lat, $lon) = sscanf($line, "%f, %f");
      list($spare, $lat, $lon) = unpack("d2", fread($fp, 16));
      
      list($x,$y) = project($a, $lat, $lon);
      $x = floor($x); 
      $y = floor($y); 
      #print "$lat, $lon    ->   $x, $y\n";
      if($x >= 0 and $y >= 0 and $x < $W and $y < $H)
        {
        imagefilledrectangle($Image, $x-$s,$y-$s,$x+$s,$y+$s, $FG);
        }
    }
    fclose($fp);
  }
  
}

?>