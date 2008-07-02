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

/// Data factory: ask this for some data in some projection, and the factory will asj the appropiate backend and (re)project it.


class datafactory
{
	
	static private $backend = 'backend_osmxapi';	/// Which backend to use. Valid values are "backend_api", "backend_osmxapi".
	//static private $backend = 'backend_api';	/// Which backend to use. Valid values are "backend_api", "backend_osmxapi".
	
	
	static public $available_crs = array('EPSG:4326','CRS:84','EPSG:32630');

	/**
	 * @param bbox  A bounding box, as requested via WMS
	 * @param crs   The Coordinate Reference System as requested via WMS. EPSG:3426 and CRS:84 are unprojected, any other has to be reprojected.
	 * @param nodes A reference to an arrays of nodes, which will be filled up with data.
	 * @param ways  A reference to an array of ways, which will be filled up with data.
	 *
	 * TODO: refactor the check for non-numeric bbox elements; the only entry point for the data requests is this datagactory method, so the backends shouldn't have to check for this.
	 */
	static function get_parsed_data($bbox,$crs,&$nodes,&$ways)
	{
		$backend = new self::$backend;
		
// 		var_dump($backend);

		// If the CRS is not WPSG:4326 or CRS:84, then the bbox has to be reprojected in order for the backends to fetch the appropiate set of data.
		if ($crs != 'EPSG:4326' && $crs != 'CRS:84')
		{
			list($left,$bottom,$right,$top) = explode(',',$bbox);
		
			if ( !is_numeric($left) ||
			!is_numeric($bottom) ||
			!is_numeric($right) ||
			!is_numeric($top))
				trigger_error("Coordinates of the bounding box are not numbers!",E_USER_ERROR);
			
			// cs2cs gets numbers in an x-y fashion...
			$corners = array ('UL' => array( $left , $top    )
			                 ,'UR' => array( $right, $top    )
			                 ,'BR' => array( $right, $bottom )
			                 ,'BL' => array( $left , $bottom )
			                 );
			
			/// TODO: check that the target CRS is valid (i.e. in the array of available CRSs)
			self::cs2cs($corners,strtolower($crs));
			
// 			var_dump($corners);
// 			$bbox = "$left,$bottom,$right,$top";
			
			// Get the min and max latitudes and longitudes
			foreach($corners as $corner)
			{
				list ($lon,$lat) = $corner;
				if (!isset($east))	// Init from the first set of coords
				{
					$east = $west = $lon;
					$south = $north = $lat;
				}
				else	// check maximums
				{
					if ($lon > $west)  $west  = $lon;
					if ($lon < $east)  $east  = $lon;
					if ($lat > $north) $north = $lat;
					if ($lat < $south) $south = $lat;
				}
			}
			$bbox = "$east,$south,$west,$north";
			
			$backend->get_parsed_data($bbox,&$nodes,&$ways);
			
// 			var_dump($bbox,$nodes);
			// Now, convert back all nodes' coordinates to the requested CRS...
			
			self::cs2cs($nodes,'epsg:4326',strtolower($crs),true);
			
// 			var_dump($bbox,$nodes);
		}
		else
		{
			// Requested a CRS native to the backends, just get the data...
			
			$backend->get_parsed_data($bbox,&$nodes,&$ways);
			
		}
		
		
		
	}
	
	
	
	

/*	function cs2cs($x,$y,$srs_in='epsg:4258',$srs_out='epsg:4326')
	{
		$r = shell_exec("echo \"$x $y\" | cs2cs -f %.20f +init=$srs_in +to +init=$srs_out");
		sscanf($r,"%f %f",$x,$y);
		return (array($x,$y));
	}*/
		
	function cs2cs(& $points,$srs_in='epsg:4326',$srs_out='epsg:4326',$invert_input=false)
	{
		// Open some pipes to a "cs2cs" process, feed the points, parse the resulting points...
	
		$descriptorspec = array(
			0 => array("pipe", "r"),  // stdin is a pipe that the child will read from
			1 => array("pipe", "w"),  // stdout is a pipe that the child will write to
			2 => array("file", "/tmp/error-output.txt", "a") // stderr is a file to write to
		);
		
		$cwd = '/tmp';
		$env = array();
		
		if ($invert_input)	$invert_flag = " -r -s"; else $invert_flag = '';
		
		$process = proc_open("cs2cs -f %.20f +init=$srs_in +to +init=$srs_out $invert_flag", $descriptorspec, $pipes, $cwd, $env);
		
// 		$result = array();
		if (is_resource($process)) {
			
			foreach($points as $id=>$point)
			{
				list($x,$y) = $point;
				fwrite($pipes[0], "$x $y\n");
// 				echo "Point $id ($x,$y) passed to cs2cs\n"; ob_flush();
			}
			fclose($pipes[0]);
			
			foreach($points as $id=>$point)
			{
// 				echo "Recovering point $id\n"; ob_flush();
				fscanf($pipes[1], "%f %f\n", $x2, $y2);
				$points[$id] = array( $x2 , $y2 );
			}
			
			fclose($pipes[1]);
		}
	
// 		return ($result);
	}

	
}




