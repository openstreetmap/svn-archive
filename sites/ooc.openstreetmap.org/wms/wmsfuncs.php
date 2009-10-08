<?php

function GetBBox()
{
	$input = GetInputVar('bbox');
	if($input != NULL && isset($input)) $bbox = $input;
	if(isset($bbox)) $bbox = split(',',$bbox);

	if(function_exists(GbRefToLatLon))
	{
		$tl = GbRefToLatLon('TQ690760');
		$br = GbRefToLatLon('TQ720740');
		if(!isset($bbox[0])) $bbox[0] = $tl[1];
		if(!isset($bbox[1])) $bbox[1] = $br[0];
		if(!isset($bbox[2])) $bbox[2] = $br[1];
		if(!isset($bbox[3])) $bbox[3] = $tl[0];
	}
	else
	{
//		if(!isset($bbox[0])) $bbox[0] = 0.8813893298755063;
//		if(!isset($bbox[1])) $bbox[1] = 51.15011253931938;
//		if(!isset($bbox[2])) $bbox[2] = 0.9072189654322577;
//		if(!isset($bbox[3])) $bbox[3] = 51.17462928732297;
		if(!isset($bbox[0])) $bbox[0] = -5.1;
		if(!isset($bbox[1])) $bbox[1] = 52.0;
		if(!isset($bbox[2])) $bbox[2] = -5.0;
		if(!isset($bbox[3])) $bbox[3] = 52.1;
	}

	//print_r($bbox);
	//echo "<br/>\n".$argc;
	//print_r($_SERVER['argv']);
	
	//exit();

	return $bbox;
}

function GetWindowSize()
{
	$result = array();

	$inWidth = GetInputVar('width');
	if(isset($inWidth))
		$result[0] = $inWidth;
	else 
		$result[0] = 640;

	$inHeight = GetInputVar('height');
	if(isset($inHeight))
		$result[1] = $inHeight;
	else 
		$result[1] = 480;

	//print_r($result);
	//exit();

	return $result;	
}

function Project($latd, $lond, $projection = 'EPSG4326')
{
	list ($width, $height) = GetWindowSize();

	list ($left, $bottom, $right, $top) = GetBBox();
	
	$ret = array();

	if(strcasecmp($projection,'EPSG900913')==0
	|| strcasecmp($projection,'mercator')==0)
	{
		//Mercator
		$ret[0] = $width* ($lond - $left) / ($right - $left);
		//inverse of the Gudermannian function
		$latr = $latd * pi() / 180.0;
		$topr = $top * pi() / 180.0;
		$bottomr = $bottom * pi() / 180.0;
		$topYPos = log(tan($topr) + 1.0/cos($topr));
		$bottomYPos = log(tan($bottomr) + 1.0/cos($bottomr));
		$thisYPos = log(tan($latr) + 1.0/cos($latr));
		$ret[1] = $height*($thisYPos - $bottomYPos) / ($topYPos - $bottomYPos);

		//Flip top to bottom
		$ret[1] = $height - $ret[1];

		return $ret;
	}

	if(strcasecmp($projection,'EPSG4326')==0 || strcasecmp($projection,'EPSG:4326')==0
	|| !isset($projection))
	{
		//EPSG:4326
		$ret[0] = $width* ($lond - $left) / ($right - $left);
		$ret[1] = $height*($latd - $bottom) / ($top - $bottom);

		//Flip top to bottom
		$ret[1] = $height - $ret[1];	

		return $ret;
	}

	throw new Exception('Projection '.$projection.' not supported.');


	
	return $ret;
}

function ProjectA($posArray)
{
	return Project($posArray[0],$posArray[1]);
}

?>
