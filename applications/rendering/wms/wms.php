<?php

/// @author Iván Sánchez Ortega <ivan@sanchezortega.es>
    
/**
    OSM WMS ("OpenStreetMap Web Map Service")
    Copyright (C) 2008, Iván Sánchez Ortega

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/    
    
    
/// Entry WMS script

/// TODO: __autoload() classes.

require('error_handler.php');
require_once('class.wireframe.php');
require_once('class.backend.php');
require_once('class.backend_api.php');
require_once('class.backend_osmxapi.php');
require_once('class.datafactory.php');



if ($_REQUEST['REQUEST'] == 'GetCapabilities')
{
// 	if ($_REQUEST['EXCEPTIONS']=='XML')
// 		fpassthru(fopen('exceptions_1_3_0.xml','r'));
// 	else
// 		fpassthru(fopen('osm_capabilities_1_3_0.xml','r'));

	header('Content-type: text/xml');

// 	require("capabilities.php");
// 	require("capabilities_1_3_0.php");
	require("capabilities_1_1_0.php");

}
elseif ($_REQUEST['REQUEST'] == 'GetMap')
{
	/// TODO: depending on the layer&style, get a renderer

	$bbox   = $_REQUEST['BBOX'];
	$height = (int) $_REQUEST['HEIGHT'];
	$width  = (int) $_REQUEST['WIDTH'];
	$crs    = $_REQUEST['SRS'];
	$format = $_REQUEST['FORMAT'];

// 	require("wms_get_image.php");
	
	if ($_REQUEST['LAYERS']=='wireframe')
	{
		wireframe::getMap($bbox,$crs,$height,$width,$format);
	}
	
}



