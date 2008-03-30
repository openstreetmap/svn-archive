<?php
# OpenStreetMap Simple Map - MediaWiki extension
# 
# This defines what happens when <map> tag is placed in the wikitext
# 
# We show a map image based on the lat/lon/zoom data passed in. Usage example:
# <map>lat=51.485|lon=-0.15|z=11|w=300|h=200|format=jpeg</map> 
# 
# This file should be placed in the mediawiki 'extensions' directory
# and then it needs to be 'included' within LocalSettings.php

$wgExtensionFunctions[] = "wfmap";

$wgExtensionCredits['parserhook'][] = array(
		'name' => 'OpenStreetMap Simple Map Image',
		'author' =>'[http://harrywood.co.uk Harry Wood]', 
		'url' => 'http://wiki.openstreetmap.org/index.php/Simple_image_MediaWiki_Extension', 
		'description' => 'Allows the use of the &lt;map&gt; tag to display a map image in an article. Maps are from [http://openstreetmap.org openstreetmap.org]'
);


function wfmap() {
	global $wgParser;
	# register the extension with the WikiText parser
	# the first parameter is the name of the new tag.
	# In this case it defines the tag <map> ... </map>a
	# the second parameter is the callback function for
	# processing the text between the tags
	$wgParser->setHook( "map", "map" );
}

# The callback function for converting the input text to HTML output
function map( $input ) {
	global $wgMapOfServiceUrl;

	//Parse pipe separated name value pairs (e.g. 'aaa=bbb|ccc=ddd')
	$paramStrings=explode('|',$input);
	foreach ($paramStrings as $paramString) {
		$paramString = trim($paramString);
		$eqPos = strpos($paramString,"=");
		if ($eqPos===false) {
			$params[$paramString] = "true";
		} else {
			$params[substr($paramString,0,$eqPos)] = htmlspecialchars(substr($paramString,$eqPos+1));
		}
	}

	$lat	= $params["lat"];
	$lon	= $params["lon"];
	$zoom	= $params["z"];
	$width	= $params["w"];
	$height	= $params["h"];
	$format	= $params["format"];

	$error="";
	
	//default values (meaning these parameters can be missed out)
	if ($width=="")  $width ="300"; 
	if ($height=="") $height="200"; 
	if ($format=="") $format="jpeg"; 
	
	if ($zoom=="") $zoom = $params["zoom"]; //see if they used 'zoom' rather than 'z' (and allow it)
	
	//trim of the 'px' on the end of pixel measurement numbers (ignore if present)
	if (substr($width,-2)=="px")  $width = substr($width,0,-2);
	if (substr($height,-2)=="px") $height = substr($height,0,-2);

	
	//Check required parameters values are provided
	if ($lat=='') $error .= "Missing lat value (for the lattitude). ";
	if ($lon=='') $error .= "Missing lon value (for the longitude). ";
	if ($zoom=='') $error .= "Missing z value (for the zoom level). ";
	if ($params['long']!='') $error .= "Please use 'lon' instead of 'long' (parameter was renamed). ";

	if ($error=='') {
		//no errors so far. Now check the values	
		
		if ($zoom=="") {
			$error = "missing z value (for the zoom level)";
		} else if ($lat=="") {
			$error = "missing lat value (for the lattitude)";
		} else if ($lon=="") {
			$error = "missing lon value (for the lattitude)";
			if ($params["long"]!="") $error = "Please use 'lon' instead of 'long' (parameter was renamed)";
		} else if (!is_numeric($width)) {
			$error = "width (w) value '$width' is not a valid integer";
		} else if (!is_numeric($height)) {
			$error = "height (h) value '$height' is not a valid integer";
		} else if (!is_numeric($zoom)) {
			$error = "zoom (z) value '$zoom' is not a valid integer";
		} else if (!is_numeric($lat)) {
			$error = "lattitude (lat) value '$lat' is not a valid number";
		} else if (!is_numeric($lon)) {
			$error = "logiditude (lon) value '$lon' is not a valid number";
		} else if ($width>1000) {
			$error = "width (w)) value cannot be greater than 1000";
		} else if ($width<50) {
			$error = "width (w)) value cannot be less than 50";
		} else if ($height>1000) {
			$error = "height (h)) value cannot be greater than 1000";
		} else if ($height<50) {
			$error = "height (h)) value cannot be less than 50";
		} else if ($lat>90) {
			$error = "lattitude (lat) value cannot be greater than 90";
		} else if ($lon<-90) {
			$error = "lattitude (lat) value cannot be less than -90";
		} else if ($lon>180) {
			$error = "longitude (lon) value cannot be greater than 180";
		} else if ($lon<-180) {
			$error = "longitude (lon) value cannot be less than -180";
		} else if ($zoom<0) {
			$error = "zoom (z) value cannot be less than zero";
		} else if ($zoom==18) {
			$error =	"zoom (z) value cannot be greater than 17. ".
						"Note that this mediawiki extension hooks into the OpenStreetMap 'osmarender' layer ".
						"which does not go beyond zoom level 17. The Mapnik layer available on ".
						"openstreetmap.org, goes up to zoom level 18";
		} else if ($zoom>17) {
			$error = "zoom (z) value cannot be greater than 17.";
		}
	}
	
	if ($error!="") {
		//Something was wrong. Spew the error message and input text.
		$output  = "";
		$output .= "<FONT COLOR=\"RED\"><B>map error:</B> " . $error . "</FONT><BR>\n";
		$output .= htmlspecialchars($input);
	} else {
		//HTML for the openstreetmap image and link:
		$output  = "";
		$output .= "<div class=\"map\">";
		$output .= "<a href=\"http://www.openstreetmap.org/?lat=".$lat."&lon=".$lon."&zoom=".$zoom."\" title=\"See this map on OpenStreetMap.org\" style=\"text-decoration:none\">";
		$output .= "<img src=\"".$wgMapOfServiceUrl."lat=".$lat."&long=".$lon."&z=".$zoom."&w=".$width."&h=".$height."&format=".$format."\" width=\"". $width."\" height=\"".$height."\" border=\"0\"><br/>";
		$output .= "<span style=\"font-size:60%; background-color:white; position:relative; top:-15px; /*width:150px; margin-right:-150px;*/\">OpenStreetMap - CC-BY-SA-2.0</span>";
		$output .= "</a>\n";
		$output .= "</div>";
	}
	return $output;
}
?>