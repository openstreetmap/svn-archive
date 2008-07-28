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

/// Backend for using the OSM eXtended API to retrieve the data

/// TODO: backend factory.

class backend_osmxapi extends backend_api
{

// 	const base_api_url = "http://www.informationfreeway.org";
	
	static $base_api_url = "http://osmxapi.hypercube.telascience.org";
	
	
	/// Returns an URL to retrieve the data from. Might be a live HTTP URL, or a local temporary .osm file.
	/// TODO: projection, projection, projection.
	function data_url($bbox)
	{

		list($left,$bottom,$right,$top) = explode(',',$bbox);
		
		if ( !is_numeric($left) ||
		     !is_numeric($bottom) ||
		     !is_numeric($right) ||
		     !is_numeric($top))
			trigger_error("Coordinates of the bounding box are not numbers!",E_USER_ERROR);
		
		
		// The arbitrary limit of 2 degrees can be changed as desired.
		if ( ($top-$bottom > 2) || ($right-$left > 2) )
			trigger_error("OSM eXtended API backend won't accept a request greater than 2 degrees (measured in latitude-longitude). Please request a smaller area.",E_USER_ERROR);
		
		
		return self::$base_api_url . "/api/0.5/*[*=*][bbox=$left,$bottom,$right,$top]";
	}
	

	/// get_data_as_osm uses the same code as the parent class (backend_api) - only the URL is different, and that's handled by data_url().
}




