<?php
# OpenStreetMap Simple Map - MediaWiki extension
# 
# This defines what happens when <map> tag is placed in the wikitext
# 
# We show a map based on the lat/lon/zoom data passed in. This extension brings in
# image generated by the static map image service called 'GetMap' maintained by OJW.  
#
# Usage example:
# <map lat=51.485 lon=-0.15 z=11 w=300 h=200 format=jpeg /> 
#
# Images are not cached local to the wiki.
# To acheive this (remove the OSM dependency) you might set up a squid proxy,
# and modify the requests URLs here accordingly.
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


class SimpleMap {

	function SimpleMap() {
	}

	# The callback function for converting the input text to HTML output
	function parse( $input, $argv ) {
		global $wgScriptPath, $wgMapOfServiceUrl;

		wfLoadExtensionMessages( 'SimpleMap' );
		
		
		//Receive args of the form <map aaa=bbb ccc=ddd />
		if ( isset( $argv['lat'] ) ) { 
			$lat = $argv['lat'];
		} else {
			$lat = '';
		}
		if ( isset( $argv['lon'] ) ) { 
			$lon = $argv['lon'];
		} else {
			$lon = '';
		}
		if ( isset( $argv['z'] ) ) { 
			$zoom = $argv['z'];
		} else {
			$zoom = '';
		}
		if ( isset( $argv['w'] ) ) { 
			$width = $argv['w'];
		} else {
			$width = '';
		}
		if ( isset( $argv['h'] ) ) { 
			$height	= $argv['h'];
		} else {
			$height = '';
		}
		if ( isset( $argv['format'] ) ) { 
			$format = $argv['format'];
		} else {
			$format	= '';
		}
		if ( isset( $argv['marker'] ) ) { 
			$marker = $argv['marker'];
		} else {
			$marker	= '';
		}

		$error='';

		//default values (meaning these parameters can be missed out)
		if ($width=='')		$width ='450'; 
		if ($height=='')	$height='320'; 
		if ($format=='')	$format='jpeg'; 

		if ($zoom=='') {
			//see if they used 'zoom' rather than 'z' (and allow it)
			if ( isset( $argv['zoom'] ) ) { 
				$zoom = $argv['zoom'];
			} else {
				$zoom = $oldStyleParams['zoom'];
			}
		}

		
		//trim off the 'px' on the end of pixel measurement numbers (ignore if present)
		if (substr($width,-2)=='px')	$width = (int) substr($width,0,-2);
		if (substr($height,-2)=='px')	$height = (int) substr($height,0,-2);

		$input = trim($input); 	
		if ($input!='') {
			if (strpos($input,'|')!==false) {
				$error = 'Old style tag syntax no longer supported';
			} else {	
				$error = 'slippymap tag contents. Were you trying to input KML? KML support ' .
				         'is disabled pending discussions about wiki syntax<br>';
			}
		}
			
		if ($marker) $error = 'No marker support in the &lt;map&gt; tag extension (yet)';
	
		if ($error=='') {
			//Check required parameters values are provided
			if ( $lat==''  ) $error .= wfMsg( 'simplemap_latmissing' );
			if ( $lon==''  ) $error .= wfMsg( 'simplemap_lonmissing' );
			if ( $zoom=='' ) $error .= wfMsg( 'simplemap_zoommissing' );
			
			//no errors so far. Now check the values	
			if (!is_numeric($width)) {
				$error = wfMsg( 'simplemap_widthnan', $width );
			} else if (!is_numeric($height)) {
				$error = wfMsg( 'simplemap_heightnan', $height );
			} else if (!is_numeric($zoom)) {
				$error = wfMsg( 'simplemap_zoomnan', $zoom );
			} else if (!is_numeric($lat)) {
				$error = wfMsg( 'simplemap_latnan', $lat );
			} else if (!is_numeric($lon)) {
				$error = wfMsg( 'simplemap_lonnan', $lon );
			} else if ($width>1000) {
				$error = wfMsg( 'simplemap_widthbig' );
			} else if ($width<100) {
				$error = wfMsg( 'simplemap_widthsmall' );
			} else if ($height>1000) {
				$error = wfMsg( 'simplemap_heightbig' );
			} else if ($height<100) {
				$error = wfMsg( 'simplemap_heightsmall' );
			} else if ($lat>90) {
				$error = wfMsg( 'simplemap_latbig' );
			} else if ($lat<-90) {
				$error = wfMsg( 'simplemap_latsmall' );
			} else if ($lon>180) {
				$error = wfMsg( 'simplemap_lonbig' );
			} else if ($lon<-180) {
				$error = wfMsg( 'simplemap_lonsmall' );
			} else if ($zoom<0) {
				$error = wfMsg( 'simplemap_zoomsmall' );
			} else if ($zoom==18) {
				$error = wfMsg( 'simplemap_zoom18' );
			} else if ($zoom>17) {
				$error = wfMsg( 'simplemap_zoombig' );
			}
		}

		
		if ($error!="") {
			//Something was wrong. Spew the error message and input text.
			$output  = '';
			$output .= "<span class=\"error\">". wfMsg( 'simplemap_maperror' ) . ' ' . $error . "</span><br />";
			$output .= htmlspecialchars($input);
		} else {
			//HTML for the openstreetmap image and link:
			$output  = "";
			$output .= "<a href=\"http://www.openstreetmap.org/?lat=".$lat."&lon=".$lon."&zoom=".$zoom."\" title=\"See this map on OpenStreetMap.org\">";
			$output .= "<img src=\"";
			$output .= $wgMapOfServiceUrl . "lat=".$lat."&long=".$lon."&z=".$zoom."&w=".$width."&h=".$height."&format=".$format;
			$output .= "\" width=\"". $width."\" height=\"".$height."\" border=\"0\">";
			$output .= "</a>";
			
		}
		return $output;
	}
}
