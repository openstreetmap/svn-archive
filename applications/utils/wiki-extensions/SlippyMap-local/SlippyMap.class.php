<?php
# OpenStreetMap SlippyMap - MediaWiki extension
# 
# This defines what happens when <slippymap> tag is placed in the wikitext
# 
# We show a map based on the lat/lon/zoom data passed in. This extension brings in
# the OpenLayers javascript, to show a slippy map.  
#
# Usage example:
# <slippymap lat=51.485 lon=-0.15 z=11 w=300 h=200 layer=osmarender marker=0></slippymap> 
#
# Tile images are not cached local to the wiki.
# To acheive this (remove the OSM dependency) you might set up a squid proxy,
# and modify the requests URLs here accordingly.
# 
# This file should be placed in the mediawiki 'extensions' directory
# ...and then it needs to be 'included' within LocalSettings.php
#
##################################################################################
#
# Copyright 2008 Harry Wood, Jens Frank, Grant Slater, Raymond Spekking and others
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
#
# @addtogroup Extensions
#


class SlippyMap {

	function SlippyMap() {
	}

	# The callback function for converting the input text to HTML output
	function parse( $input, $argv ) {
		global $wgMapOfServiceUrl, $wgSlippyMapVersion;

		wfLoadExtensionMessages( 'SlippyMap' );
		
		
		//Support old style parameters from $input
		//Parse the pipe separated name value pairs (e.g. 'aaa=bbb|ccc=ddd')
		//With the new syntax we expect nothing in the $input, so this will result in '' values
		$oldStyleParamStrings=explode('|',$input);
		foreach ($oldStyleParamStrings as $oldStyleParamString) {
			$oldStyleParamString = trim($oldStyleParamString);
			$eqPos = strpos($oldStyleParamString,"=");
			if ($eqPos===false) {
				$oldStyleParams[$oldStyleParamString] = "true";
			} else {
				$oldStyleParams[substr($oldStyleParamString,0,$eqPos)] = trim(htmlspecialchars(substr($oldStyleParamString,$eqPos+1)));
			}
		}	
		
		//Receive new style args: <slippymap aaa=bbb ccc=ddd></slippymap>
		if ( isset( $argv['lat'] ) ) { 
			$lat		= $argv['lat'];
		} else {
			$lat		= $oldStyleParams['lat'];
		}
		if ( isset( $argv['lon'] ) ) { 
			$lon		= $argv['lon'];
		} else {
			$lon		= $oldStyleParams['lon'];
		}
		if ( isset( $argv['z'] ) ) { 
			$zoom		= $argv['z'];
		} else {
			$zoom		= $oldStyleParams['z'];
		}
		if ( isset( $argv['w'] ) ) { 
			$width		= $argv['w'];
		} else {
			$width		= $oldStyleParams['w'];
		}
		if ( isset( $argv['h'] ) ) { 
			$height		= $argv['h'];
		} else {
			$height		= $oldStyleParams['h'];
		}
		if ( isset( $argv['layer'] ) ) { 
			$layer		= $argv['layer'];
		} else {
			$layer		= $oldStyleParams['layer'];
		}
		if ( isset( $argv['marker'] ) ) { 
			$marker		= $argv['marker'];
		} else {
			$marker		= '';
		}

		$error='';

		//default values (meaning these parameters can be missed out)
		if ($width=='')		$width ='450'; 
		if ($height=='')	$height='320'; 
		if ($layer=='')		$layer='mapnik'; 

		if ($zoom=='' && isset( $argv['zoom'] ) ) {
			$zoom = $argv['zoom']; //see if they used 'zoom' rather than 'z' (and allow it)
		}

		$marker = ( $marker != '' && $marker != '0' );
		
		//trim off the 'px' on the end of pixel measurement numbers (ignore if present)
		if (substr($width,-2)=='px')	$width = (int) substr($width,0,-2);
		if (substr($height,-2)=='px')	$height = (int) substr($height,0,-2);


		if (trim($input)!='' && sizeof($oldStyleParamStrings)<3) {
			$error = 'slippymap tag contents. Were you trying to input KML? KML support '.
			         'is disactivated on the OSM wiki pending discussions about wiki syntax';
			$showkml = false;
		} else {
			$showkml = false;
		}
		
		
		if ($marker) $error = 'marker support is disactivated on the OSM wiki pending discussions about wiki syntax';
	

		//Check required parameters values are provided
		if ( $lat==''  ) $error .= wfMsg( 'slippymap_latmissing' );
		if ( $lon==''  ) $error .= wfMsg( 'slippymap_lonmissing' );
		if ( $zoom=='' ) $error .= wfMsg( 'slippymap_zoommissing' );
		
		if ($error=='') {
			//no errors so far. Now check the values	
			if (!is_numeric($width)) {
				$error = wfMsg( 'slippymap_widthnan', $width );
			} else if (!is_numeric($height)) {
				$error = wfMsg( 'slippymap_heightnan', $height );
			} else if (!is_numeric($zoom)) {
				$error = wfMsg( 'slippymap_zoomnan', $zoom );
			} else if (!is_numeric($lat)) {
				$error = wfMsg( 'slippymap_latnan', $lat );
			} else if (!is_numeric($lon)) {
				$error = wfMsg( 'slippymap_lonnan', $lon );
			} else if ($width>1000) {
				$error = wfMsg( 'slippymap_widthbig' );
			} else if ($width<100) {
				$error = wfMsg( 'slippymap_widthsmall' );
			} else if ($height>1000) {
				$error = wfMsg( 'slippymap_heightbig' );
			} else if ($height<100) {
				$error = wfMsg( 'slippymap_heightsmall' );
			} else if ($lat>90) {
				$error = wfMsg( 'slippymap_latbig' );
			} else if ($lat<-90) {
				$error = wfMsg( 'slippymap_latsmall' );
			} else if ($lon>180) {
				$error = wfMsg( 'slippymap_lonbig' );
			} else if ($lon<-180) {
				$error = wfMsg( 'slippymap_lonsmall' );
			} else if ($zoom<0) {
				$error = wfMsg( 'slippymap_zoomsmall' );
			} else if ($zoom==18) {
				$error = wfMsg( 'slippymap_zoom18' );
			} else if ($zoom>17) {
				$error = wfMsg( 'slippymap_zoombig' );
			}
		}

		//Find the tile server URL to use.  Note that we could allow the user to override that with
		//*any* tile server URL for more flexibility, but that might be a security concern.

		$layer = strtolower($layer);
		$layerObjectDef = '';
		if ($layer=='osmarender') {        
			$layerObjectDef = 'OpenLayers.Layer.OSM.Osmarender("Osmarender"); ';
		} elseif ($layer=='mapnik') {
			$layerObjectDef = 'OpenLayers.Layer.OSM.Mapnik("Mapnik"); ';
		} elseif ($layer=='maplint') {
			$layerObjectDef = 'OpenLayers.Layer.OSM.Maplint("Maplint"); ';
		} else {
			$error = wfMsg( 'slippymap_invalidlayer',  htmlspecialchars($layer) );
		}
		

		if ($error!="") {
			//Something was wrong. Spew the error message and input text.
			$output  = '';
			$output .= "<span class=\"error\">". wfMsg( 'slippymap_maperror' ) . ' ' . $error . "</span><br />";
			$output .= htmlspecialchars($input);
		} else {
			//HTML output for the slippy map.
			//Note that this must all be output on one line (no linefeeds)
			//otherwise MediaWiki adds <BR> tags, which is bad in the middle of a block of javascript.
			//There are other ways of fixing this, but not for MediaWiki v4
			//(See http://www.mediawiki.org/wiki/Manual:Tag_extensions#How_can_I_avoid_modification_of_my_extension.27s_HTML_output.3F)

			
			$output  = '<!-- slippy map -->';
			
			//This inline stylesheet defines how the two extra buttons look, and where they are positioned.
			//Chaging positioning is not so easy though, because it seems positioning of the visible button divs
			//much match the positioning of the mysterious olControlPanel divs.
			//TODO: figure out how to position the buttons at the side-by-side in the bottom right. would look better.
			$output .= '<style>'.
				'.getWikiCodeButton, .resetButton {'.
				'   margin-left: 60px;'.
				'   margin-top: 10px;'.
				'   width:  80px;'.
				'   height: 18px;'.
				'   background-color: DARKBLUE;'.
				'   color: WHITE;'.
				'   padding:0px;'.
				'   text-align:center;'.
				'   font-size:12px;'.
				'   font-family: Verdana, Helvetica, Arial, sans-serif;'.
				"}\n".
			
				'.getWikiCodeButton {'.
				'	margin-top: 10px;'.
				"}\n".
			
				'.resetButton {'.
				'	margin-top: 38px;'.
				"}\n".
			
				'.olControlPanel div {'.   //mysterious openlayers divs. Invisible but receive mouse click events.
				'	margin-left: 60px;'.
				'	margin-top: 10px;'.
				'	width:  80px;'.
				'	height: 18px;'.
				"}\n".
				'</style>';
			
			$output .= '<script type="text/javascript"> var osm_fully_loaded=false;';

			// defer loading of the javascript. Since the script is quite big, it would delay
			// page loading and rendering dramatically. This necessitates fetching a modified version of OpenStreetMap.js (nasty kludge)
			$output .= 'addOnloadHook( function() { ' .
			 	'	var sc = document.createElement("script");' .
				'	sc.src = "http://www.openlayers.org/api/OpenLayers.js";' .
			 	'	document.body.appendChild( sc );' .
			 	'	var sc = document.createElement("script");' .
			 	'	sc.src = "http://svn.wikimedia.org/viewvc/mediawiki/trunk/extensions/SlippyMap/OpenStreetMap.js?view=co&' . $wgSlippyMapVersion . '";'.
			 	'	document.body.appendChild( sc );' .
			 	'} );';

			$output .= "var lon= ${lon}; var lat= ${lat}; var zoom= ${zoom}; var lonLat;";

			$output .= 'var map; ';

			$output .= 'addOnloadHook( slippymap_init ); ';

			
			$output .= 'function slippymap_resetPosition() {';
			$output .= '	map.setCenter(lonLat, zoom);';
			$output .= '}';
			
			$output .= 'function slippymap_getWikicode() {';
			$output .= '	LL = map.getCenter().transform(map.getProjectionObject(), new OpenLayers.Projection("EPSG:4326"));';
			$output .= '    Z = map.getZoom(); ';
			$output .= '    size = map.getSize();';

			$output .= '    prompt( "' . wfMsg('slippymap_code') .'", "<slippymap h="+size.h+" w="+size.w+" z="+Z+" lat="+LL.lat+" lon="+LL.lon+" layer=mapnik marker=1></slippymap>" ); ';
			$output .= '}';
			
			$output .= 'function slippymap_init() { ';
			$output .= '	if (!osm_fully_loaded) { window.setTimeout("slippymap_init()",500); return 0; } ' ;

			$output .= '	map = new OpenLayers.Map("map", { ';
			$output .= '		controls:[ ';
			$output .= '			new OpenLayers.Control.Navigation(), ';

			if ($height>320) {
				//Add the zoom bar control, except if the map is only little
				$output .= '		new OpenLayers.Control.PanZoomBar(),';   
			} else if ( $height > 140 ) {
				$output .= '            new OpenLayers.Control.PanZoom(),';
			}

			$output .= '			new OpenLayers.Control.Attribution()], ';
			$output .= '		maxExtent: new OpenLayers.Bounds(-20037508.34,-20037508.34,20037508.34,20037508.34), ';
			$output .= '			maxResolution:156543.0399, units:\'meters\', projection: "EPSG:900913"} ); ';

			$output .= '	layer = new ' . $layerObjectDef;

			$output .= '	map.addLayer(layer); ';

			$output .= '	epsg4326 = new OpenLayers.Projection("EPSG:4326"); ';
			$output .= '	lonLat = new OpenLayers.LonLat(lon, lat).transform( epsg4326, map.getProjectionObject()); ';

			if ( $marker ) {
				$output .= 'var markers = new OpenLayers.Layer.Markers( "Markers" ); ' .
			           	'   map.addLayer(markers); ' .
				   	'   var size = new OpenLayers.Size(20,34); ' .
				   	'   var offset = new OpenLayers.Pixel(-(size.w/2), -size.h); ' .
				   	"   var icon = new OpenLayers.Icon('http://boston.openguides.org/markers/YELLOW.png',size,offset);" .
			           	'   markers.addMarker(new OpenLayers.Marker( lonLat,icon)); ';
			}

			if ( $showkml ) {
				$input = str_replace( array( '%',   "\n" , "'"  , '"'  , '<'  , '>'  , ' '   ), 
						      array( '%25', '%0A', '%27', '%22', '%3C', '%3E', '%20' ), $input );
				$output .= 'var vector = new OpenLayers.Layer.Vector("Vector Layer"); ' .
					'   map.addLayer(vector); ' .
					'   kml = new OpenLayers.Format.KML( { "internalProjection": map.baseLayer.projection, ' .
					'                                      "externalProjection": epsg4326, ' .
					'                                      "extractStyles": true, ' .
					'                                      "extractAttributes": true } ); ' .
					"   features = kml.read(unescape('$input')); " .
					'   vector.addFeatures( features ); ';
			}

			$output .= '	map.setCenter (lonLat, zoom); ';
			$output .= '	var getWikiCodeButton = new OpenLayers.Control.Button({displayClass: "getWikiCodeButton", trigger: slippymap_getWikicode}); ';
 			$output .= '	var resetButton = new OpenLayers.Control.Button({displayClass: "resetButton", trigger: slippymap_resetPosition}); ';
 			$output .= '	var panel = new OpenLayers.Control.Panel(); ';
            $output .= '	panel.addControls([getWikiCodeButton, resetButton]); ';
			$output .= '	map.addControl(panel); ';
			$output .= '	getWikiCodeButton.div.innerHTML="' . wfMsg('slippymap_button_code') . '"; ';
			$output .= '	resetButton.div.innerHTML="' . wfMsg('slippymap_resetview') . '"; ';
			$output .= '} ';


			$output .= "</script> ";

			$output .= "<div style=\"width: {$width}px; height:{$height}px; border-style:solid; border-width:1px; border-color:lightgrey;\" id=\"map\">";
			$output .= "<noscript><a href=\"http://www.openstreetmap.org/?lat=$lat&lon=$lon&zoom=$zoom\" title=\"See this map on OpenStreetMap.org\" style=\"text-decoration:none\">";
			$output .= "<img src=\"".$wgMapOfServiceUrl."lat=${lat}&long=${lon}&z=${zoom}&w=${width}&h=${height}&format=jpeg\" width=\"${width}\" height=\"${height}\" border=\"0\"><br/>";
			$output .= '</a></noscript>';
			$output .= '</div>';
			
			if (sizeof($oldStyleParamStrings) >2 )  $output .= '<div style="font-size:0.8em;"><i>please change to <a href="http://wiki.openstreetmap.org/index.php/Slippy_Map_MediaWiki_Extension">new syntax</a></i></div>';
		}
		return $output;
	}
}
