<?php

/// @author Iv�n S�nchez Ortega <ivan@sanchezortega.es>

/**
    OSM WMS ("OpenStreetMap Web Map Service")
    Copyright (C) 2008, Iv�n S�nchez Ortega

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

/// Backend for using the OSM 0.5 API to retrieve the data


class backend_api extends backend
{

	const base_api_url = "http://www.openstreetmap.org";
	
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
		
		if ( ($top-$bottom > 0.25) || ($right-$left > 0.25) )
			trigger_error("OSM API backend won't accept a request greater than 0.25 degrees (measured in latitude-longitude). Please request a smaller area.",E_USER_ERROR);
		
		
		return self::base_api_url . "/api/0.5/map?bbox=$left,$bottom,$right,$top";
	}
	
	/// Returns the data as a string, corresponding to an .osm file
	/// TODO: catch any errors encountered when downloading data.
	/// TODO: use CURL.
	function get_data_as_osm($bbox)
	{
		$ch = curl_init();

		// set URL and other appropriate options
		curl_setopt($ch, CURLOPT_URL, $this->data_url($bbox) );
// 		curl_setopt($ch, CURLOPT_HEADER, true);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($ch, CURLOPT_USERAGENT, 'OSM WMS');
		curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
		
		$data = curl_exec($ch);
	
		$http_code = curl_getinfo($ch,CURLINFO_HTTP_CODE);
		
		if ($http_code != 200)
		{
			/// FIXME: The CURL request has to follow 302 headers, in order to work with OSMXAPI. This, in turn, makes it more difficult to extract the headers from the returned text.
/*			$headers = explode("\n",substr($data,0,strpos($data,"\r\n\r\n")));
			foreach($headers as $header)
			{
				if (strstr($header,'Error: '))
					trigger_error("OSM server returned HTTP code $http_code.\nOSM server also returned the following error information:\n$header\n(URL was " . $this->data_url($bbox) . ")"  ,E_USER_ERROR);
			}*/
			
			trigger_error("OSM server returned HTTP code $http_code.\n(URL was " . $this->data_url($bbox) . ")"  ,E_USER_ERROR);
		}
	
		// No errors, cut the header...
// 	var_dump($data);
// 	var_dump( strstr($data,"\r\n\r\n"));
// 		$data = strstr($data,"\r\n\r\n");
// 		return substr($data,4);


		return $data;
	}
	
	
	/// Returns two arrays, filled with the requested data
// 	static function get_parsed_data($bbox,&$nodes,&$ways)
// 	{
// 		$nodes = $ways = array();
// 		$xml = simplexml_load_string($this->get_data_as_osm($bbox));
// 	
// // 	var_dump($xml);
// 	
// 		foreach($xml->node as $parsed_node)
// 		{
// 		// 	$attrs = $parsed_node->attributes();
// 			$nodes[ (int)$parsed_node['id'] ] = array( (float)$parsed_node['lat'], (float)$parsed_node['lon'] );
// 		}
// 		
// 		
// 		foreach($xml->way as $parsed_way)
// 		{
// 			$way_id = (int)$parsed_way['id'];
// 			foreach($parsed_way->nd as $nd)
// 			{
// 				$ways[ $way_id ][] = (int)$nd['ref'];
// 			}
// 				
// 		// 	$attrs = $parsed_node->attributes();
// 		// 	$nodes[ $attrs['id'] ] = array( $attrs['lat'],$attrs['lon'] );
// 		}	
// 	}
	
}




