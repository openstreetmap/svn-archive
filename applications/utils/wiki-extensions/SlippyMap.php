<?php
# OpenStreetMap SlippyMap - MediaWiki extension
# 
# This defines what happens when <slippymap> tag is placed in the wikitext
# 
# We show a map based on the lat/lon/zoom data passed in. This extension brings in
# the OpenLayers javascript, to show a slippy map.  
#
# Usage example:
# <slippymap>lat=51.485|lon=-0.15|z=11|w=300|h=200|layer=osmarender</slippymap> 
#
# Tile images are not cached local to the wiki.
# To acheive this (remove the OSM dependency) you might set up a squid proxy,
# and modify the requests URLs here accordingly.
# 
# This file should be placed in the mediawiki 'extensions' directory
# ...and then it needs to be 'included' within LocalSettings.php
# OpenLayers.js and get_osm_url.js must also be placed in the extensions directory

$wgExtensionFunctions[] = 'wfslippymap';

$wgExtensionCredits['parserhook'][] = array(
	'name' => 'OpenStreetMap Slippy Map',
	'author' =>'[http://harrywood.co.uk Harry Wood]',
	'url' => 'http://wiki.openstreetmap.org/index.php/Slippy_Map_MediaWiki_Extension',
	'description' => 'Allows the use of the &lt;slippymap&gt; tag to display an OpenLayers slippy map. Maps are from [http://openstreetmap.org openstreetmap.org]'
);

function wfslippymap() {
	global $wgParser;
	# register the extension with the WikiText parser
	# the first parameter is the name of the new tag.
	# In this case it defines the tag <slippymap> ... </slippymap>
	# the second parameter is the callback function for
	# processing the text between the tags
	$wgParser->setHook( "slippymap", "slippymap" );
}

# The callback function for converting the input text to HTML output
function slippymap( $input ) {
	
	//Parse pipe separated name value pairs (e.g. 'aaa=bbb|ccc=ddd')
	$paramStrings=explode('|',$input);
	foreach ($paramStrings as $paramString) {
		$paramString = trim($paramString);
		$eqPos = strpos($paramString,"=");
		if ($eqPos===false) {
			$params[$paramString] = "true";
		} else {
			$params[substr($paramString,0,$eqPos)] = trim(htmlspecialchars(substr($paramString,$eqPos+1)));
		}
	}

	$lat		= $params['lat'];
	$lon		= $params['lon'];
	$zoom		= $params['z'];
	$width		= $params['w'];
	$height		= $params['h'];
	$layer		= $params['layer'];

	$error="";

	//default values (meaning these parameters can be missed out)
	if ($width=='')		$width ='450'; 
	if ($height=='')	$height='320'; 
	if ($layer=='')		$layer='mapnik'; 

	if ($zoom=='')		$zoom = $params['zoom']; //see if they used 'zoom' rather than 'z' (and allow it)

	//trim off the 'px' on the end of pixel measurement numbers (ignore if present)
	if (substr($width,-2)=='px')	$width = substr($width,0,-2);
	if (substr($height,-2)=='px')	$height = substr($height,0,-2);

	//Check required parameters values are provided
	if ($lat=='') $error .= "Missing lat value (for the lattitude). ";
	if ($lon=='') $error .= "Missing lon value (for the longitude). ";
	if ($zoom=='') $error .= "Missing z value (for the zoom level). ";
	if ($params['long']!='') $error .= "Please use 'lon' instead of 'long' (parameter was renamed). ";

	if ($error=='') {
		//no errors so far. Now check the values	
		if (!is_numeric($width)) {
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
			$error = "width (w) value cannot be greater than 1000";
		} else if ($width<100) {
			$error = "width (w) value cannot be less than 100";
		} else if ($height>1000) {
			$error = "height (h) value cannot be greater than 1000";
		} else if ($height<100) {
			$error = "height (h) value cannot be less than 100";
		} else if ($lat>90) {
			$error = "lattitude (lat) value cannot be greater than 90";
		} else if ($lat<-90) {
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

	//Find the tile server URL to use.  Note that we could allow the user to override that with
	//*any* tile server URL for more flexibility, but that might be a security concern.

	$layer = strtolower($layer);
	$layerObjectDef = '';
	if ($layer=='osmarender') {        
		$layerObjectDef = "OpenLayers.Layer.OSM.Osmarender(\"Osmarender\"); ";
	} elseif ($layer=='mapnik') {
		$layerObjectDef = "OpenLayers.Layer.OSM.Mapnik(\"Mapnik\"); ";
	} elseif ($layer=='maplint') {
		$layerObjectDef = "OpenLayers.Layer.OSM.Maplint(\"Maplint\"); ";
	} else {
		$error = "Invalid 'layer' value '" . htmlspecialchars($layer) . "'";
	}


	if ($error!="") {
		//Something was wrong. Spew the error message and input text.
		$output  = '';
		$output .= "<FONT COLOR=\"RED\"><B>map error:</B> " . $error . "</FONT><BR>";
		$output .= htmlspecialchars($input);
	} else {
		//HTML output for the slippy map.
		//Note that this must all be output on one line (no linefeeds)
		//otherwise MediaWiki adds <BR> tags, which is bad in the middle of block of javascript.
		//There are other ways of fixing this, but not for MediaWiki v4
		//(See http://www.mediawiki.org/wiki/Manual:Tag_extensions#How_can_I_avoid_modification_of_my_extension.27s_HTML_output.3F)

		$output  = '';
		$output .= "<!-- bring in the OpenLayers javascript library -->";
		$output .= "<script src=\"http://openlayers.org/api/OpenLayers.js\"></script> ";

		$output .= "<!-- bring in the OpenStreetMap OpenLayers layers. ";
		$output .= "     Using this hosted file will make sure we are kept up ";
		$output .= "     to date with any necessary changes --> ";
		$output .= "<script src=\"http://openstreetmap.org/openlayers/OpenStreetMap.js\"></script> ";

		$output .= "<script type=\"text/javascript\"> ";

		$output .= "var lon=". $lon ."; ";
		$output .= "var lat=". $lat ."; ";

		$output .= "var zoom=". $zoom ."; ";

		$output .= "var map; ";

		$output .= "function lonLatToMercator(ll) { ";
		$output .= "	var lon = ll.lon * 20037508.34 / 180; ";
		$output .= "	var lat = Math.log (Math.tan ((90 + ll.lat) * Math.PI / 360)) / (Math.PI / 180); ";
		$output .= "	lat = lat * 20037508.34 / 180; ";

		$output .= "	return new OpenLayers.LonLat(lon, lat); ";
		$output .= "} ";

		$output .= "window.onload=init; "; //seems to work as an alternative to body.onLoad()

		$output .= "function init() { ";

		$output .= "	map = new OpenLayers.Map(\"map\", { ";
		$output .= "		controls:[ ";
		$output .= "			new OpenLayers.Control.Navigation(), ";

		if ($height>320) {
			//Add the zoom bar control, except if the map is only little
			$output .= "		new OpenLayers.Control.PanZoomBar(),";   
		}

		$output .= "			new OpenLayers.Control.Attribution()], ";
		$output .= "		maxExtent: new OpenLayers.Bounds(-20037508.34,-20037508.34,20037508.34,20037508.34), ";
		$output .= "			maxResolution:156543.0399, units:'meters', projection: \"EPSG:900913\"} ); ";


		$output .= "	layer = new " . $layerObjectDef;

		$output .= "	map.addLayer(layer); ";

		$output .= "	var lonLat = lonLatToMercator(new OpenLayers.LonLat(lon, lat)); ";

		$output .= "	map.setCenter (lonLat, zoom); ";
		$output .= "} ";

		$output .= "</script> ";


		$output .= "<div class=\"map\" style=\"margin-bottom:-35px;\"> ";
		$output .= "<!-- define a DIV into which the map will appear --> ";
		$output .= "<div style=\"width:". $width ."px; height:".$height."px; border-style:solid; border-width:1px; border-color:lightgrey;\" id=\"map\"></div><br/>";

		//copyright message & link to OSM
		$output .= "<span style=\"";
		if ($height<600) $output .= "font-size:60%; ";
		$output .= "background-color:white; position:relative; top:-35px;\">";
		$output .= "<a href=\"http://www.openstreetmap.org/?lat=".$lat."&lon=".$lon."&zoom=".$zoom."\" title=\"See this map on OpenStreetMap.org\">";
		$output .= "OpenStreetMap - CC-BY-SA-2.0";
		$output .= "</a>";
		$output .= "</span>";
		$output .= "</div>";
	}
	return $output;
}
?>
