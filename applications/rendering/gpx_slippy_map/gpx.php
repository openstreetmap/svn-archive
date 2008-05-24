<?php
// note: large sample test is 91949

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
      
      if(0)
      {
      fwrite($fpOut, pack("dd", $x, $y), 16);
      }
      else
      {
      fwrite($fpOut, pack("NN", $x / $Resolution, $y / $Resolution), 8);
      }
      
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

if(0)
{
  header("Content-type:text/plain");
  $a = getGpx(112168);
  print_r(getMeta($a));
}

?>