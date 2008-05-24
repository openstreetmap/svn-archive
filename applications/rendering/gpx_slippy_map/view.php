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

include_once("gpx.php");
header("Content-type:text/plain");

# Load the GPX file
$GPX = $_GET["gpx"] + 0;
$Filename = getGpx($GPX);

$Data = getMeta($Filename, 1); // 1 means leave the filepointer open
print_r($Data);
  
if(!$Data['exists'] || !$Data['valid'])
  {
  print "Invalid";
  return;
  }
  
$fp = $Data['fp'];
  
$Resolution = 1 / pow(2.0, 31);

print "------------------------------\n";
# Loop through the points
for($ii = 0; $ii < $Data['points']; $ii++)
{
  # rx,ry are relative to the mercator projection (0-1)
  list($spare, $rx, $ry) = unpack("N2", fread($fp, 8));
  $rx *= $Resolution;
  $ry *= $Resolution;
  
  $lat = mercatorToLat(M_PI * (1.0 - 2.0 * $ry));
  $lon = -180.0 + 360.0 * $rx;
  
  printf("%f, %f\n", $lat, $lon);
}

fclose($fp);

?>