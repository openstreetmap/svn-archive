<?php
#----------------------------------------------------------------
# Load a GPX tracklog from openstreetmap.org/traces and
# store it in a binary file
#
#------------------------------------------------------
# File format:
#
# +---------+--------+------+--------------------------+
# | size    | type   | name | description              |
# +---------+--------+------+--------------------------+
# | 4 bytes | int    | n    | number of trackpoints    |
# | 8 bytes | double | xmin | bounding box (W)         |
# | 8 bytes | double | xmax | bounding box (E)         |
# | 8 bytes | double | ymin | bounding box (S)         |
# | 8 bytes | double | ymax | bounding box (N)         |
# +---------+--------+------+--------------------------+
#   then n pairs of:
# +---------+--------+------+--------------------------+
# | 4 bytes | int    | x    | coded position           |
# | 4 bytes | int    | y    | coded position           |
# +---------+--------+------+--------------------------+
#
# xmin,xmax,ymin,ymax,x,and y are stored as positions 
# relative to the coverage of the slippy map system
#
# x positions from 0 = 180 degrees west to 1 = 180 degrees west
# y positions from 0 = 85.0511 north to 1 = 85.0511 south
#
# note that these are in the mercator projection, i.e.
# dLat/dy is not constant
#
# encoding of x and y:
# the positions described above are divided by 2^32 for
# storage as an integer, 
# i.e.:
#  x: 0 = fully west, to 2^32-1 = fully east
#  y: 0 = fully north, to 2^32-1 = fully south
#
# integers are big-endian
# double-precision floats are platform-dependant
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
include_once("projection.php");

function getGpx($ID)
{
  $URL = sprintf("http://www.openstreetmap.org/trace/%d/data", $ID);
  $Filename = sprintf("cache/gpx_%d", $ID);
  
  if(file_exists($Filename))
    return($Filename);
  
  $fpOut = fopen($Filename, "wb");
  if(!$fpOut)
    return("");
    
  $fpIn = fopen($URL, "r");
  if(!$fpIn)
    {
    fclose($fpOut);
    return("");
    }
  
  # extents of data
  $A = 1;
  $B = 0;
  $C = 1;
  $D = 0;
  $Count = 0;
  
  # Reserve space for header
  fwrite($fpOut, pack("N", 0), 4);
  fwrite($fpOut, pack("d4", 0,0,0,0), 32);
  
  $Resolution = 1 / pow(2.0, 32);
  
  while(!feof($fpIn))
  {
    $Line = fgets($fpIn, 400);
    if(preg_match('{<trkpt lat="(.*?)" lon="(.*?)">}', $Line, $Matches))
    {
      list($x,$y) = latlon2relativeXY($Matches[1]+0, $Matches[2]+0);
      
      fwrite($fpOut, pack("NN", $x / $Resolution, $y / $Resolution), 8);
      
      if($x < $A) $A = $x;
      if($x > $B) $B = $x;
      if($y < $C) $C = $y;
      if($y > $D) $D = $y;
      $Count++;
    }
  }
  
  # Write the file header
  fseek($fpOut, 0, SEEK_SET);
  fwrite($fpOut, pack("N", $Count), 4);
  fwrite($fpOut, pack("d4", $A,$B,$C,$D), 32);
  
  fclose($fpIn);
  fclose($fpOut);
  return($Filename);
}

function getMeta($Filename, $LeaveOpen = 0)
{
  # Default data
  $Data = array(
    'exists'=>0, 
    'size'=>filesize($Filename),
    'filename'=>$Filename);
  
  # Does it contain enough data for the header?
  $Data['valid'] = ($Data['size'] >= 4 + 32);
  
  # Try to open file
  $fp = fopen($Filename, "rb");
  if($fp)
    {
    $Data['exists'] = 1;
    if($Data['valid'])
      {
      list(
        $Spare, 
        $Data['points']) = unpack("N", fread($fp, 4));
      list(
        $Spare,
        $Data['xmin'], 
        $Data['xmax'], 
        $Data['ymin'], 
        $Data['ymax']) = unpack("d4", fread($fp, 32));
      }

    # Now that we have the file open, option to return the filehandle
    # if the calling function wants to continue reading data
    if($LeaveOpen && $Data['valid'])
      {
      $Data['fp'] = $fp;
      }
    else
      {
      fclose($fp);
      }
    }
  return($Data);
}

if($_GET['test_gpx_reader'])
{
  header("Content-type:text/plain");
  $a = getGpx(112168);
  print_r(getMeta($a));
}

?>