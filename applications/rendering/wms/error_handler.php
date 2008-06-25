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

/// A custom error handling function will ensure that the WMS exceptions can be thrown in either XML or image format.

/// TODO: what if the installation does not have the GD libraries and the exception format is an image?
/// TODO: How to return OGC-compliant error codes?
function wms_error_handler($errno, $errstr, $errfile, $errline)
{
	if ( $errno != E_USER_ERROR && $errno != E_ERROR )	// Do nothing if the error is not critical.
		return false;
	
	
	if ($_REQUEST['EXCEPTIONS']=='XML' || $_REQUEST['EXCEPTIONS']=='application/vnd.ogc.se_xml')
	{
	/// Only return errors as XML if the client explicitly asks for it - most WMS clients are lazy and will probably like an image better.
	
	echo "<?xml version='1.0' encoding='UTF-8'?>
<ServiceExceptionReport version='1.3.0'
  xmlns='http://www.opengis.net/ogc'
  xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
  xsi:schemaLocation='http://www.opengis.net/ogc
http://schemas.opengis.net/wms/1.3.0/exceptions_1_3_0.xsd'>
<ServiceException>
<![CDATA[
Fatal error:
$errstr
	
(Triggered in file $errfile, line $errline).
]]>
</ServiceException>
</ServiceExceptionReport>
";

	die();


	}
	else
	{
		// Return an image, with either nothing or the error message printed.
		
		
		$height = (int) $_REQUEST['HEIGHT'];
		$width  = (int) $_REQUEST['WIDTH'];
		
		$im = imagecreate ( $width , $height );
		
		$backgroundcolor = imagecolorallocate ( $im , 255 , 255 , 255 );	// White
		
		if ($_REQUEST['TRANSPARENT'])
			imagecolortransparent( $im , $backgroundcolor );
		
		if ($_REQUEST['EXCEPTIONS'] != 'BLANK' && $_REQUEST['EXCEPTIONS'] != 'application/vnd.ogc.se_blank')
		{
			$textcolor = imagecolorallocate ( $im , 255 , 0 , 0 );	// Red
		
			$errortext = "Fatal error:\n$errstr\n\n(Triggered in file $errfile, line $errline).";
			imagettftext ( $im , 8 , 0 , 0 , 0 , $textcolor , './DejaVuSans.ttf' , $errortext );
		}
		
		
		if ($_REQUEST['FORMAT'] == 'image/png')
		{
			header('Content-type: image/png');
			imagepng($im);
		}
		if ($_REQUEST['FORMAT'] == 'image/jpeg')
		{
			header('Content-type: image/jpeg');
			imagejpeg($im);
		}
		if ($_REQUEST['FORMAT'] == 'image/gif')
		{
			header('Content-type: image/gif');
			imagegif($im);
		}
	}
	
	
	
	

}


set_error_handler("wms_error_handler");









