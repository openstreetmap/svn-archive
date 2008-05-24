<?php

$time_start = microtime(true);

include_once("gpx.php");
include_once("projection.php");

# Create a transparent image
$W = $H = 256;
$Image = imagecreatetruecolor($W,$H);
$BG = imagecolorallocate($Image,255,255,0);
imagecolortransparent($Image, $BG);
imagefilledrectangle($Image,0,0,$W,$H,$BG);

# Which tile are we?
if(preg_match("{(\d+)/(\d+)/(\d+)}", $_GET['t'], $Matches))
{
  
  # Load the GPX file
  $GPX = $_GET["gpx"] + 0;
  $Filename = getGpx($GPX);

  # Plot the GPX
  makeTile(
    $Image, 
    $W,
    $H, 
    $Matches[2] + 0,
    $Matches[3] + 0,
    $Matches[1] + 0,
    $Filename);
}


if($_GET['benchmark'])
{
  $time_end = microtime(true);
  $time = $time_end - $time_start;
  
  header("Content-type:text/plain");
  printf("Tile took %1.2f ms\n", $time * 1000.0);
  exit;
}

# Send image to browser
header("Content-type:image/PNG");
imagepng($Image);


function makeTile($Image,$W,$H,$x,$y,$z,$Filename)
{
  #imagestring($Image,4,10,10,"$x,$y,$z", $Image);return;
  
  if($z > 19)
    return;
  if(!tileValid($x,$y,$z))
    return;

  list($x1,$y1,$x2,$y2,$dx,$dy) = relativeTileEdges($x,$y,$z);

  #$a = proj_init($x,$y,$z,$W,$H);
  $FG = imagecolorallocate($Image,0,0,50);
  
  # Get the GPX details
  $Data = getMeta($Filename, 1); // 1 means leave the filepointer open
  
  $Debug = 0;
  if($Debug)
    {
    header("Content-type:text/plain");
    print_r($Data);
    print "From $x1,$y1\n To $x2,$y2\n Size $dx,$dy\n"; 
    }

  if(!$Data['exists'] || !$Data['valid'])
    {
    print "Invalid";
    return;
    }
    
  # If the data was valid, then getMeta will have returned an open
  # filepointer
  $fp = $Data['fp'];
  
  $NothingOnThisTile = (
    $Data['points'] < 1
    || $Data['xmin'] > $x2
    || $Data['xmax'] < $x1
    || $Data['ymin'] > $y2
    || $Data['ymax'] < $y1);

  if(!$NothingOnThisTile)
    {
    
    $s = 2; # half-size of GPX points (pixels)
    $Resolution = 1 / pow(2.0, 32);
    
    # Loop through the points
    for($ii = 0; $ii < $Data['points']; $ii++)
    {
      # rx,ry are relative to the mercator projection (0-1)
      if(0)
        list($spare, $rx, $ry) = unpack("d2", fread($fp, 16));
      else
      {
        list($spare, $rx, $ry) = unpack("N2", fread($fp, 8));
        $rx *= $Resolution;
        $ry *= $Resolution;
      }
  
      # Convert to pixel positions on the image
      $x = floor($W * ($rx - $x1) / $dx);
      $y = floor($H * ($ry - $y1) / $dy);
      
      if($x >= 0 and $y >= 0 and $x < $W and $y < $H)
        {
        imagefilledrectangle($Image, $x-$s,$y-$s,$x+$s,$y+$s, $FG);
        }
    }
  }
  
  fclose($fp);
}

?>