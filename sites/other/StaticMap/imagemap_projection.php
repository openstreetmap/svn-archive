<?
include("projection.php");

function imagemap_xy2ll($x, $y, $Data)
{
  $CLat = $Data['lat'];
  $CLon = $Data['lon'];
  $Z = $Data['z'];
  $W = $Data['w'];
  $H = $Data['h'];

  // Centre of the screen in mercator units
  list($CX, $CY) = latlon2xy($CLat,$CLon,$Z);

  // Click's offset in pixels from centre of screen
  $PX = $x - 0.5 * $W;
  $PY = $y - 0.5 * $H;

  // Click's position in mercator units
  $X2 = $CX + $PX / 256.0;
  $Y2 = $CY + $PY / 256.0;
  
  // Click's position in lat/lon
  list($Lat, $Lon) = xy2latlon($X2,$Y2,$Z);

  return(array($Lat,$Lon));
}


function imagemap_ll2xy($Lat, $Lon, $Data)
{
  $CLat = $Data['lat'];
  $CLon = $Data['lon'];
  $Z = $Data['z'];
  $W = $Data['w'];
  $H = $Data['h'];

  // Centre of the screen in mercator units
  list($CX, $CY) = latlon2xy($CLat,$CLon,$Z);

  // Position in mercator units
  list($X2, $Y2) = latlon2xy($Lat,$Lon,$Z);

  // Position's offset in pixels from centre of image
  $PX = ($X2 - $CX) * 256.0;
  $PY = ($Y2 - $CY) * 256.0;

  
  $x = 0.5 * $W + $PX;
  $y = 0.5 * $H + $PY;

  return(array($x,$y));
}


?>