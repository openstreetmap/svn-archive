<?php
// note: large sample test is 91949
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
  
  while(!feof($fpIn))
  {
    $Line = fgets($fpIn, 400);
    if(preg_match('{<trkpt lat="(.*?)" lon="(.*?)">}', $Line, $Matches))
    {
      fwrite($fpOut, pack("dd", $Matches[1]+0, $Matches[2]+0), 16);
    }
  }
  fclose($fpIn);
  fclose($fpOut);
  return($Filename);
}
#getGpx(112168);
?>