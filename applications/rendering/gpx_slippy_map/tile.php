<?php
#----------------------------------------------------------------
# Render a GPX tracklog onto a slippy-map tile
#
#------------------------------------------------------
# Usage: 
#   tile.php?gpx=12345&t=7/8/9
#  where:
#   12345 is the ID of the GPX file on openstreetmap.org/traces
#   7/8/9 is the tile ID (z/x/y)
#------------------------------------------------------
# Copyright 2008, Oliver White
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#------------------------------------------------------
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

  $FG = imagecolorallocate($Image,0,0,50);
  
  # Get the GPX details
  $Data = getMeta($Filename, 1); // 1 means leave the filepointer open
  
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
      list($spare, $rx, $ry) = unpack("N2", fread($fp, 8));
      $rx *= $Resolution;
      $ry *= $Resolution;
  
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