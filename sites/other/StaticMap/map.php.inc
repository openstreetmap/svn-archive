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

  _imagefilter($Image, $Data['filter']);
  

  imagealphablending($Image, true);


  if($Data['gpx'] > 0)
    {
    addLayer($Image, $Data, sprintf(
      "http://dev.openstreetmap.org/~ojw/gpx/tile.php?gpx=%d&t=", $Data['gpx']));
    }
  if($Data['rel'] > 0)
    {
    addLayer($Image, $Data, sprintf(
      "http://dev.openstreetmap.org/~ojw/relation/relation.php?rel=%d&t=", $Data['rel']));
    }

  addDrawings($Image, $Data);
  addIcons($Image, $Data);

  switch($Data['att'])
    {
    case 'text':
      addText($Image, $Width, $Height, attribution($Layer));
      break;
    case 'logo':
      $Logo1 = imagecreatefrompng('gfx/osm_small.png');
      $Logo2 = imagecreatefrompng('gfx/cc_by_sa.png');
      imagecopy($Image, $Logo1, 10, 10, 0, 0, imagesx($Logo1), imagesy($Logo1));
      imagecopy($Image, $Logo2, 20 + imagesx($Logo1), 10, 0, 0, imagesx($Logo2), imagesy($Logo2));
      break;
    }
  
  if($Format == "jpeg"){
    header("Content-Type: image/jpeg");
    imagejpeg($Image);
  }
  else{
    header("Content-Type: image/png");
    imagepng($Image);
  }

}

function addLayer($Image, $Data, $Base)
{
  $Tilesize = 256;
  $GridSizeX = ceil(($Data['w'] / 2.0) / $Tilesize);
  $GridSizeY = ceil(($Data['h'] / 2.0) / $Tilesize);

  list($X,$Y) = XY($Data['lat'], $Data['lon'], $Data['z']);

  $XC = floor($X);
  $YC = floor($Y);
  
  $XA = ($X - $XC) * $Tilesize;
  $YA = ($Y - $YC) * $Tilesize;
  for($xi = -$GridSizeX; $xi <= $GridSizeX; $xi++)
    {
    for($yi = -$GridSizeY; $yi <= $GridSizeY; $yi++)
      {
      $ToX = (floor($Data['w']/2) - $XA + $xi * $Tilesize);
      $ToY = (floor($Data['h']/2) - $YA + $yi * $Tilesize);

      if($ToX > -$Tilesize && $ToX < $Data['w']
	&& $ToY > -$Tilesize && $ToY < $Data['h'])
	{
	
	$Part = imagecreatefrompng(sprintf("%s%d/%d/%d.png", 
	  $Base,
	  $Data['z'], $X + $xi, $Y + $yi));
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

function addDrawings($Image, $Data)
{
  for($i = 0; $i < MaxDrawings(); $i++)
    {
    $lastX = $lastY = $lastValid = 0;

    if(preg_match("/^([0-9A-F])([0-9A-F])([0-9A-F])$/", $Data["d${i}_colour"], $Matches))
      {
      
      $R = hexdec($Matches[1]) * 16;
      $G = hexdec($Matches[2]) * 16;
      $B = hexdec($Matches[3]) * 16;
      $Colour = imagecolorallocate($Image, $R, $G, $B);

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
	$Colour = imagecolorallocatealpha($Image, $R, $G, $B, 64);
	imagefilledpolygon($Image, $Points, $NumPoints, $Colour);
	}
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
        $ImgBg = imagecolorallocate($Image, 255, 255, 200);
        # $Icon = imagerotate($Icon, 30, $ImgBg);
        $sx = imagesx($Icon);
	$sy = imagesy($Icon);
	imagecopy(
	  $Image,
	  $Icon, 
	  $mx - 0.5 * $sx,
	  $my - 0.5 * $sy,
	  0, 0, 
	  $sx, $sy);
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

function _imagefilter($Image, $Mode)
{
  if($Mode == "none" or $Mode == "normal" or $Mode == "")
    return;

  $w = imagesx($Image);
  $h = imagesy($Image);
  for($x = 0; $x < $w; $x++)
    {
    for($y = 0; $y < $w; $y++)
      {
      list($R,$G,$B) = int2rgb(imagecolorat($Image, $x, $y));
      switch($Mode)
	{
	case "grey":
	  $R = $G = $B = ($R + $G + $B) / 3.0;
	  break;
	case "lightgrey":
	  $R = $G = $B = 0.75 + 0.25 * ($R + $G + $B) / 3.0;
	  break;
	case "darkgrey":
	  $R = $G = $B = 0.25 * ($R + $G + $B) / 3.0;
	  break;
	case "bright":
	  $R = pow($R, 0.5);
	  $G = pow($G, 0.5);
	  $B = pow($B, 0.5);
	  break;
	case "dark":
	  $R *= 0.25;
	  $G *= 0.25;
	  $B *= 0.25;
	  break;
	case "verydark":
	  $R = 0.25 * pow($R, 0.5);
	  $G = 0.25 * pow($G, 0.5);
	  $B = 0.25 * pow($B, 0.5);
	  break;
	case "invert":
	  $R = 1.0 - $R;
	  $G = 1.0 - $G;
	  $B = 1.0 - $B;
	  break;
	}
      imagesetpixel($Image, $x, $y, imagecolorallocate($Image, $R*255,$G*255,$B*255));
      }
    }
}

function int2rgb($Int)
{
  $R = ($Int >> 16) & 0xFF;
  $G = ($Int >> 8) & 0xFF;
  $B = ($Int) & 0xFF;
  return(array($R / 255.0, $G / 255.0,$B / 255.0));
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