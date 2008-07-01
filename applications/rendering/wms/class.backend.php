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

/// Common backend functionality


abstract class backend
{
	/// Returns two arrays, filled with the requested data
	/// TODO: relations
	function get_parsed_data($bbox,&$nodes,&$ways)
	{
		$nodes = $ways = array();
		$xml = simplexml_load_string($this->get_data_as_osm($bbox));
// 		$xml = simplexml_load_file("/tmp/osmxapi.xml");
	
// 	var_dump($xml);
	
		foreach($xml->node as $parsed_node)
		{
		// 	$attrs = $parsed_node->attributes();
			$nodes[ (int)$parsed_node['id'] ] = array( (float)$parsed_node['lat'], (float)$parsed_node['lon'] );
		}
		
		
		foreach($xml->way as $parsed_way)
		{
			$way_id = (int)$parsed_way['id'];
			foreach($parsed_way->nd as $nd)
			{
				$ways[ $way_id ][] = (int)$nd['ref'];
			}
				
		// 	$attrs = $parsed_node->attributes();
		// 	$nodes[ $attrs['id'] ] = array( $attrs['lat'],$attrs['lon'] );
		}	
	}
	
}




