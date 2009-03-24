<?php
    /**
     * mod_OSMMap module Entry Point
     *
     * @license        GNU/GPL, see LICENSE.php
     * mod_OSMMap is free software. This version may have been modified pursuant
     * to the GNU General Public License, and as distributed it includes or
     * is derivative of works licensed under the GNU General Public License or
     * other free or open source software licenses.
     */

    defined( '_JEXEC' ) or die( 'Restricted access' );

	//Get Values of the parameters
	$height = $params->get('height','');
	$width = $params->get('width','');
	$myLatitude = $params->get('myLatitude','');
	$myLongitude = $params->get('myLongitude','');
	$zoom = $params->get('zoom','');
	$mapdiv = $params->get('mapdiv','map');
	$maptype = $params->get('maptype','0');
	// type has no user, perhas it would be good if we would give the user 
	// the option to select a openstreetmap server.
	// $type = $params->get('type','');
	$panzoombar = $params->get('panzoombar','0');
	$mini = $params->get('mini','0');
	$layerswitcher = $params->get('layerswitcher','0');
	$mouseposition = $params->get('mouseposition','0');
	$mousedefaults = $params->get('mousedefaults','0');
	$keyboarddefaults = $params->get('keyboardefaults','0');
	$reset_link = $params->get('reset_link','');

	echo "<script src=\"http://www.openlayers.org/api/OpenLayers.js\" type=\"text/javascript\"></script>";
	echo "<script src=\"http://www.opengeo.nl/files/osmtiles.js\" type=\"text/javascript\"></script>";
 	echo "<script type=\"text/javascript\">";
 	echo "var map;";
 	echo "var lat;";
 	echo "var lon;";
	echo "var zoom;";
	echo "function initialize() {";
	echo "  lat = $myLatitude;";
	echo "  lon = $myLongitude;";
	echo "  zoom = $zoom;"; 
	if ($maptype == 0) {
		echo "  center = new OpenLayers.LonLat(lon,lat);";
	} else {
		echo "  center = new OpenLayers.LonLat(lon,lat);";
		echo "	center.transform(new OpenLayers.Projection(\"EPSG:4326\"), new OpenLayers.Projection(\"EPSG:900913\"));";
	}
	echo "	map = new OpenLayers.Map('$mapdiv', {";
	if ($maptype == "0") {
		echo "	projection: \"EPSG:28992\",";
		echo "	maxResolution: 1328.125,";
		echo "	numZoomLevels: 14,";
		echo "	maxExtent : new OpenLayers.Bounds(-23500,289000,316500,629000),";
		echo "	units : \"meters\",";
		echo "	controls: []});";
		echo "	var layerFastRD = new OpenLayers.Layer.TMS (";
		echo "	\"OpenstreetMap RD\",";
		echo "	\"http://nl.openstreet.nl/\","; 
		echo "	{ratio:1, type:'png', getURL: get_render_rd_url});"; 
		echo "	map.addLayers([layerFastRD]);"; 
	} else {
		echo "	projection: \"EPSG:900913\",";
		echo "  maxResolution: 156543.0339,";
		echo "  numZoomLevels: 20,";
        	echo "  maxExtent: new OpenLayers.Bounds(-20037508.34,-20037508.34,20037508.34,20037508.34),";
	        echo "  units: 'meters',";
		echo "	controls: []});";
		echo "	var mapnik = new OpenLayers.Layer.TMS(";
		echo "  \"Mapnik\",";
		echo "  [\"http://a.tile.openstreetmap.org/\",\"http://b.tile.openstreetmap.org/\",\"http://c.tile.openstreetmap.org/\"],";
		echo "	{ratio:1, type:'png', getURL: get_render_world_url});"; 
		echo "	map.addLayers([mapnik]);"; 
	}

	echo "	map.setCenter(center,zoom);"; 
	if ($panzoombar == "1" && $mini == "0") {
		echo "map.addControl(new OpenLayers.Control.PanZoomBar({zoomWorldIcon: true}) );";
	} 
	if ($panzoombar == "1" && $mini == "1") {
		echo "map.addControl(new OpenLayers.Control.PanZoom({zoomWorldIcon: true}) );";
	} 

	if ($layerswitcher == "1") {
		echo "map.addControl(new OpenLayers.Control.LayerSwitcher({'ascending':false}) );";
	}
	if ($mouseposition == "1") {
		echo "map.addControl(new OpenLayers.Control.MousePosition() );";
	}
	if ($mousedefaults == "1") {
		echo "map.addControl(new OpenLayers.Control.MouseDefaults() );";
	}
	if ($keyboardefaults == "1") {
		echo "map.addControl(new OpenLayers.Control.KeyboardDefaults() );";
	}
	echo "	}"; 
	echo "</script>";

	echo "<div id=\"$mapdiv\" style=\"width: $width";
	echo "px; height:$height";
	echo "px\"></div>";
 	echo "<script type=\"text/javascript\">";
	echo "	initialize();";
	echo "</script>";

	if ($reset_link == "1") {
		echo "<a href=\"javascript:initialize();\">Reset Map</a>";
	}
?>
