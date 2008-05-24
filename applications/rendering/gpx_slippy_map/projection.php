<?php
function degrees($rad){return($rad * 57.2957795);}
function radians($deg){return($deg / 57.2957795);}

function numTiles($z)
{
  return(pow(2.0,$z));
}

function tileValid($x,$y,$z)
{
  if($x < 0 or $y < 0 or $z < 0)
    return(0);
  $n = numTiles($z);
  if($x >= $n or $y >= $n)
    return(0);
  return(1);
}

function sec($x)
{
  return(1.0/cos($x));
}
function relativeTileEdges($x,$y,$z)
{
  $n = numTiles($z);
  $portion = 1.0 / $n;
  
  $x1 = $x * $portion;
  $y1 = $y * $portion; 

  return(array($x1,$y1,$x1+$portion,$y1+$portion, $portion, $portion));
}

function latlon2relativeXY($lat,$lon)
{
  $x = ($lon + 180.0) / 360.0;
  $y = (1.0 - log(tan(radians($lat)) + sec(radians($lat))) / M_PI) / 2.0;
  return(array($x,$y));
}
function latlon2xy($lat,$lon,$z)
{
  $n = numTiles($z);
  list($x,$y) = latlon2relativeXY($lat,$lon);
  return(array($n*$x, $n*$y));
  }
  
function tileXY($lat, $lon, $z)
{
  list($x,$y) = latlon2xy($lat,$lon,$z);
  return(array(floor($x),floor($y)));
}
function xy2latlon($x,$y,$z)
{
  $n = numTiles($z);
  $relY = $y / $n;
  $lat = mercatorToLat(M_PI * (1.0 - 2.0 * $relY));
  $lon = -180.0 + 360.0 * $x / $n;
  return(array($lat,$lon));
}
function mercatorToLat($mercatorY)
{
  $val = degrees(atan(sinh($mercatorY)));
  return($val);
}
function latEdges($y,$z)
{
  $n = numTiles($z);
  $unit = 1.0 / $n;
  $relY1 = $y * $unit;
  $relY2 = $relY1 + $unit;
  $lat1 = mercatorToLat(M_PI * (1.0 - 2.0 * $relY1));
  $lat2 = mercatorToLat(M_PI * (1.0 - 2.0 * $relY2));
  return(array($lat1,$lat2));
}
function lonEdges($x,$z)
{
  $n = numTiles($z);
  $unit = 360.0 / $n;
  $lon1 = -180.0 + $x * $unit;
  $lon2 = $lon1 + $unit;
  return(array($lon1,$lon2));
  }
function tileEdges($x,$y,$z)
{
  list($lat1,$lat2) = latEdges($y,$z);
  list($lon1,$lon2) = lonEdges($x,$z);
  return(array($lat2, $lon1, $lat1, $lon2)); # S,W,N,E
}




function proj_init($tx,$ty,$tz, $w, $h)
{
  $Proj = array('tx'=>$tx,'ty'=>$ty,'tz'=>$tz);
  
  list(
    $Proj['S'],
    $Proj['W'],
    $Proj['N'],
    $Proj['E']) = tileEdges($tx,$ty,$tz); // S,W,N,E
  $Proj['dLat'] = $Proj['N'] - $Proj['S'];
  $Proj['dLon'] = $Proj['E'] - $Proj['W'];
  $Proj['dx'] = $w;
  $Proj['dy'] = $h;
  //print_r($Proj);
  return($Proj);
  }
  
function project($Proj, $lat,$lon)
{
  $pLat = ($lat - $Proj['S']) / $Proj['dLat'];
  $pLon = ($lon - $Proj['W']) / $Proj['dLon'];
  $x = $Proj['dx'] * $pLon;
  $y = $Proj['dy'] * (1 - $pLat);
  return(array($x,$y));
}


?>