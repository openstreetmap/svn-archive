<?php
include_once("layers.php.inc");
include_once("limits.php.inc");

#----------------------------------------------------------------------------
# Create a map, output it to browser
#----------------------------------------------------------------------------
function doMap($Lat, $Long, $Zoom, $Width, $Height, $Layer, $Format, $AddAttribution, $Data)
{
  $Tilesize = 256;
  $GridSizeX = ceil(($Width / 2.0) / $Tilesize);
  $GridSizeY = ceil(($Height / 2.0) / $Tilesize);

  $Image = imagecreatetruecolor($Width, $Height);
  $BG = imagecolorallocate($Image, 248, 248, 248);
  imagefilledrectangle($Image, 0,0,$Width, $Height, $BG);
  
  $Layers = getLayers();
  if(array_key_exists($Layer, $Layers))
    {
    $LayerData = $Layers[$Layer];
    $LayerData['name'] = $Layer; // so we can pass LayerData and still use the name


    list($X,$Y) = XY($Lat, $Long, $Zoom);

    $XC = floor($X);
    $YC = floor($Y);
    
    $XA = ($X - $XC) * $Tilesize;
    $YA = ($Y - $YC) * $Tilesize;
    for($xi = -$GridSizeX; $xi <= $GridSizeX; $xi++)
    {
      for($yi = -$GridSizeY; $yi <= $GridSizeY; $yi++)
      {
	$Filename = cacheableTile($X + $xi, $Y + $yi, $Zoom, $LayerData);
	if($Filename)
	  {

	  $ToX = (floor($Width/2) - $XA + $xi * $Tilesize);
	  $ToY = (floor($Height/2) - $YA + $yi * $Tilesize);

	  if($ToX > -$Tilesize && $ToX < $Width
	    && $ToY > -$Tilesize && $ToY < $Height)
	    {
	    
	    $Part = '';
	    if(substr($Filename,-3) == "png")
	      $Part = imagecreatefrompng($Filename);
	    elseif(substr($Filename,-4) == "jpeg")
	      $Part = imagecreatefromjpeg($Filename);
	    
	    if($Part)
	      {
	      imagecopy($Image, $Part, 
		$ToX,
		$ToY, 
		0, 0, 
		$Tilesize, $Tilesize);
	      }
	    }
	  }
	}
      }
    } // End of base-map

  // Expire old cache entries
  walkCache('expire');

  imagealphablending($Image, true);
  addDrawings($Image, $Data);
  addIcons($Image, $Data);

  if($AddAttribution)
    addText($Image, $Width, $Height, attribution($Layer));
  
  if($Format == "jpeg"){
    header("Content-Type: image/jpeg");
    imagejpeg($Image);
  }
  else{
    header("Content-Type: image/png");
    imagepng($Image);
  }

}

function addDrawings($Image, $Data)
{
  for($i = 0; $i < MaxDrawings(); $i++)
    {
    $lastX = $lastY = $lastValid = 0;
    $Colour = imagecolorallocate($Image, 0,0,128);
    $Points = array();
    $NumPoints = 0;
    $Style = $Data["d{$i}_style"];

    for($j = 0; $j < MaxPoints(); $j++)
      {
      $Lat = $Data["d{$i}p${j}lat"];
      $Lon = $Data["d{$i}p${j}lon"];

      if($Lat != 0 || $Lon != 0)
	{
	list($mx,$my) = imagemap_ll2xy($Lat, $Lon, $Data);

	if($lastValid && $Style == "line")
	  {
	  imageLineThick($Image, $lastX, $lastY, $mx, $my, $Colour, 4);
	  }

	array_push($Points, $mx);
	array_push($Points, $my);
	$NumPoints++;
	
	$lastX = $mx;
	$lastY = $my;
	$lastValid = true;
	}
      }

    if($NumPoints > 2 && $Style=="polygon")
      {
      $Colour = imagecolorallocatealpha($Image, 0,128,0, 64);
      imagefilledpolygon($Image, $Points, $NumPoints, $Colour);
      }

    }

}

function addIcons($Image, $Data)
{
  for($i = 0; $i < MaxIcons(); $i++)
    {
    $Lat = $Data["mlat$i"];
    $Lon = $Data["mlon$i"];
    
    if($Lat != 0 || $Lon != 0)
      {
      list($mx,$my) = imagemap_ll2xy($Lat, $Lon, $Data);

      $IconSize = 40; // TODO: query from icon image

      if($Icon = imagecreatefrompng(sprintf("symbols/%d.png", $Data["mico$i"])))
	{
	imagecopy(
	  $Image,
	  $Icon, 
	  $mx - 0.5 * $IconSize,
	  $my - 0.5 * $IconSize, 
	  0, 0, 
	  $IconSize, $IconSize);
	}
      }
    }
}

function attribution($Layer)
{
  $Layers = getLayers();
  return(sprintf("%s - %s", 
    $Layers[$Layer]["license"],
    $Layers[$Layer]["source"]));
  
}

function addText($Image, $Width, $Height, $Text)
{
  $Font = 2;
  $Colour = imagecolorallocate($Image, 0,0,0);
  $BgColour  = imagecolorallocate($Image, 255,255,255);
  
  $H = imagefontheight($Font);
  $W = imagefontwidth($Font) * strlen($Text);
  
  $x = 3;
  $y = 3;
  $border = 3;
  
  imagefilledrectangle(
    $Image,
    $x-$border, 
    $y-$border, 
    $W+2*$border, 
    $H+2*$border, 
    $BgColour);
  
  imagestring(
    $Image, 
    $Font, 
    1, // x
    1, // y
    $Text,
    $Colour);
}

function cacheableTile($X,$Y,$Z, $LayerData)
{
  if(!tileValid($X,$Y,$Z))
    return("");

  $RemoteFilename = tileURL($X,$Y,$Z, $LayerData);
  $LocalFilename = sprintf("%s/%s_%02d_%05d_%05d.%s", 
    cacheDir(),
    $LayerData['name'], 
    $Z,
    $X,
    $Y,
    $LayerData['format']);

  if(!file_exists($LocalFilename)
    || 0) // TODO: expire after a week
    {
    copy($RemoteFilename, $LocalFilename);
    # TODO: chgrp and chmod($LocalFilename, ); // allow group to delete cache entries!
    }
  return($LocalFilename);
}

function walkCache($Action)
{
  $Dir = cacheDir();
  if(is_dir($Dir))
    {
    if($fp = opendir($Dir))
      {
      while(($File = readdir($fp)) !== false)
	{
	$FullFile = sprintf("%s/%s", $Dir, $File);
	if(is_file($FullFile))
	  {

	  if($Action == "del")
	    {
	    unlink($FullFile);
	    }
	  if($Action == "expire" and 0) // TODO: older than a week
	    {
	    unlink($FullFile);
	    }
	  $Count++;
	  }
        }
      closedir($fp);
      }
    }

  if($Action == "stat")
    {
    printf("%d objects\n", $Count);
    }
}

function cacheDir()
{
  return("cache/tiles");
}

function tileURL($X,$Y,$Z, $LayerData)
{
  if(!tileValid($X,$Y,$Z))
    return("");

  $Base = $LayerData["tiles"];
  $Format = $LayerData["format"];
    
  return(sprintf("%s/%d/%d/%d.%s", $Base, $Z, $X, $Y, $Format));
}


// Sample code from http://uk2.php.net/manual/en/function.imageline.php
function imagelinethick($image, $x1, $y1, $x2, $y2, $color, $thick = 1)
{
    /* this way it works well only for orthogonal lines
    imagesetthickness($image, $thick);
    return imageline($image, $x1, $y1, $x2, $y2, $color);
    */
    if ($thick == 1) {
        return imageline($image, $x1, $y1, $x2, $y2, $color);
    }
    $t = $thick / 2 - 0.5;
    if ($x1 == $x2 || $y1 == $y2) {
        return imagefilledrectangle($image, round(min($x1, $x2) - $t), round(min($y1, $y2) - $t), round(max($x1, $x2) + $t), round(max($y1, $y2) + $t), $color);
    }
    $k = ($y2 - $y1) / ($x2 - $x1); //y = kx + q
    $a = $t / sqrt(1 + pow($k, 2));
    $points = array(
        round($x1 - (1+$k)*$a), round($y1 + (1-$k)*$a),
        round($x1 - (1-$k)*$a), round($y1 - (1+$k)*$a),
        round($x2 + (1+$k)*$a), round($y2 - (1-$k)*$a),
        round($x2 + (1-$k)*$a), round($y2 + (1+$k)*$a),
    );
    imagefilledpolygon($image, $points, 4, $color);
    return imagepolygon($image, $points, 4, $color);
}

#-----------------------------------------------------------------------------
# Slippy map coordinate functions: should probably use a library
#-----------------------------------------------------------------------------
function XY($Lat, $Long, $Zoom){
  $PortionY = Lat2Y($Lat);
  $PortionX = Long2X($Long);
  $Size = pow(2,$Zoom);
  $X = $PortionX * $Size;
  $Y = $PortionY * $Size;
  return(array($X,$Y));
}

function Long2X($Long){
  return(($Long + 180) / 360);
}
function Lat2Y($Lat){
  $LimitY = 3.14159265358979;
  $Y = ProjectF($Lat);
  
  $PY = ($LimitY - $Y) / (2 * $LimitY);
  return($PY);
}
function ProjectF($Lat){
  $Lat = deg2rad($Lat);
  $Y = log(tan($Lat) + (1/cos($Lat)));
  return($Y);
}
?>