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

/// Data factory: ask this for some data in some projection, and the factory will ask the appropiate backend and (re)project it.


class datafactory
{
	
	static private $backend = 'backend_osmxapi';	/// Which backend to use. Valid values are "backend_api", "backend_osmxapi".
	//static private $backend = 'backend_api';	/// Which backend to use. Valid values are "backend_api", "backend_osmxapi".
	
	
	static public $available_crs = array('EPSG:4326' =>array(-180,-90,180,90)
	,'CRS:84'    =>array(-180,-90,180,90)
	
	// Infamous "google" projection
	,'EPSG:900913'=>array(-20037508.34, -20037508.34, 20037508.34, 20037508.34)
	
	// UTM zones, WGS 84
	,'EPSG:32601'=>array(-1500000,0,1500000,10000000)	// UTM 1 N
	,'EPSG:32602'=>array(-1500000,0,1500000,10000000)	// UTM 2 N
	,'EPSG:32603'=>array(-1500000,0,1500000,10000000)	// UTM 3 N
	,'EPSG:32604'=>array(-1500000,0,1500000,10000000)	// UTM 4 N
	,'EPSG:32605'=>array(-1500000,0,1500000,10000000)	// UTM 5 N
	,'EPSG:32606'=>array(-1500000,0,1500000,10000000)	// UTM 6 N
	,'EPSG:32607'=>array(-1500000,0,1500000,10000000)	// UTM 7 N
	,'EPSG:32608'=>array(-1500000,0,1500000,10000000)	// UTM 8 N
	,'EPSG:32609'=>array(-1500000,0,1500000,10000000)	// UTM 9 N
	,'EPSG:32610'=>array(-1500000,0,1500000,10000000)	// UTM 10 N
	,'EPSG:32611'=>array(-1500000,0,1500000,10000000)	// UTM 11 N
	,'EPSG:32612'=>array(-1500000,0,1500000,10000000)	// UTM 12 N
	,'EPSG:32613'=>array(-1500000,0,1500000,10000000)	// UTM 13 N
	,'EPSG:32614'=>array(-1500000,0,1500000,10000000)	// UTM 14 N
	,'EPSG:32615'=>array(-1500000,0,1500000,10000000)	// UTM 15 N
	,'EPSG:32616'=>array(-1500000,0,1500000,10000000)	// UTM 16 N
	,'EPSG:32617'=>array(-1500000,0,1500000,10000000)	// UTM 17 N
	,'EPSG:32618'=>array(-1500000,0,1500000,10000000)	// UTM 18 N
	,'EPSG:32619'=>array(-1500000,0,1500000,10000000)	// UTM 19 N
	,'EPSG:32620'=>array(-1500000,0,1500000,10000000)	// UTM 20 N
	,'EPSG:32621'=>array(-1500000,0,1500000,10000000)	// UTM 21 N
	,'EPSG:32622'=>array(-1500000,0,1500000,10000000)	// UTM 22 N
	,'EPSG:32623'=>array(-1500000,0,1500000,10000000)	// UTM 23 N
	,'EPSG:32624'=>array(-1500000,0,1500000,10000000)	// UTM 24 N
	,'EPSG:32625'=>array(-1500000,0,1500000,10000000)	// UTM 25 N
	,'EPSG:32626'=>array(-1500000,0,1500000,10000000)	// UTM 26 N
	,'EPSG:32627'=>array(-1500000,0,1500000,10000000)	// UTM 27 N
	,'EPSG:32628'=>array(-1500000,0,1500000,10000000)	// UTM 28 N
	,'EPSG:32629'=>array(-1500000,0,1500000,10000000)	// UTM 29 N
	,'EPSG:32630'=>array(-1500000,0,1500000,10000000)	// UTM 30 N
	,'EPSG:32631'=>array(-1500000,0,1500000,10000000)	// UTM 31 N
	,'EPSG:32632'=>array(-1500000,0,1500000,10000000)	// UTM 32 N
	,'EPSG:32633'=>array(-1500000,0,1500000,10000000)	// UTM 33 N
	,'EPSG:32634'=>array(-1500000,0,1500000,10000000)	// UTM 34 N
	,'EPSG:32635'=>array(-1500000,0,1500000,10000000)	// UTM 35 N
	,'EPSG:32636'=>array(-1500000,0,1500000,10000000)	// UTM 36 N
	,'EPSG:32637'=>array(-1500000,0,1500000,10000000)	// UTM 37 N
	,'EPSG:32638'=>array(-1500000,0,1500000,10000000)	// UTM 38 N
	,'EPSG:32639'=>array(-1500000,0,1500000,10000000)	// UTM 39 N
	,'EPSG:32640'=>array(-1500000,0,1500000,10000000)	// UTM 40 N
	,'EPSG:32641'=>array(-1500000,0,1500000,10000000)	// UTM 41 N
	,'EPSG:32642'=>array(-1500000,0,1500000,10000000)	// UTM 42 N
	,'EPSG:32643'=>array(-1500000,0,1500000,10000000)	// UTM 43 N
	,'EPSG:32644'=>array(-1500000,0,1500000,10000000)	// UTM 44 N
	,'EPSG:32645'=>array(-1500000,0,1500000,10000000)	// UTM 45 N
	,'EPSG:32646'=>array(-1500000,0,1500000,10000000)	// UTM 46 N
	,'EPSG:32647'=>array(-1500000,0,1500000,10000000)	// UTM 47 N
	,'EPSG:32648'=>array(-1500000,0,1500000,10000000)	// UTM 48 N
	,'EPSG:32649'=>array(-1500000,0,1500000,10000000)	// UTM 49 N
	,'EPSG:32650'=>array(-1500000,0,1500000,10000000)	// UTM 50 N
	,'EPSG:32651'=>array(-1500000,0,1500000,10000000)	// UTM 51 N
	,'EPSG:32652'=>array(-1500000,0,1500000,10000000)	// UTM 52 N
	,'EPSG:32653'=>array(-1500000,0,1500000,10000000)	// UTM 53 N
	,'EPSG:32654'=>array(-1500000,0,1500000,10000000)	// UTM 54 N
	,'EPSG:32655'=>array(-1500000,0,1500000,10000000)	// UTM 55 N
	,'EPSG:32656'=>array(-1500000,0,1500000,10000000)	// UTM 56 N
	,'EPSG:32657'=>array(-1500000,0,1500000,10000000)	// UTM 57 N
	,'EPSG:32658'=>array(-1500000,0,1500000,10000000)	// UTM 58 N
	,'EPSG:32659'=>array(-1500000,0,1500000,10000000)	// UTM 59 N
	,'EPSG:32660'=>array(-1500000,0,1500000,10000000)	// UTM 60 N
	
	,'EPSG:32701'=>array(-1500000,0,1500000,10000000)	// UTM 1 S
	,'EPSG:32702'=>array(-1500000,0,1500000,10000000)	// UTM 2 S
	,'EPSG:32703'=>array(-1500000,0,1500000,10000000)	// UTM 3 S
	,'EPSG:32704'=>array(-1500000,0,1500000,10000000)	// UTM 4 S
	,'EPSG:32705'=>array(-1500000,0,1500000,10000000)	// UTM 5 S
	,'EPSG:32706'=>array(-1500000,0,1500000,10000000)	// UTM 6 S
	,'EPSG:32707'=>array(-1500000,0,1500000,10000000)	// UTM 7 S
	,'EPSG:32708'=>array(-1500000,0,1500000,10000000)	// UTM 8 S
	,'EPSG:32709'=>array(-1500000,0,1500000,10000000)	// UTM 9 S
	,'EPSG:32710'=>array(-1500000,0,1500000,10000000)	// UTM 10 S
	,'EPSG:32711'=>array(-1500000,0,1500000,10000000)	// UTM 11 S
	,'EPSG:32712'=>array(-1500000,0,1500000,10000000)	// UTM 12 S
	,'EPSG:32713'=>array(-1500000,0,1500000,10000000)	// UTM 13 S
	,'EPSG:32714'=>array(-1500000,0,1500000,10000000)	// UTM 14 S
	,'EPSG:32715'=>array(-1500000,0,1500000,10000000)	// UTM 15 S
	,'EPSG:32716'=>array(-1500000,0,1500000,10000000)	// UTM 16 S
	,'EPSG:32717'=>array(-1500000,0,1500000,10000000)	// UTM 17 S
	,'EPSG:32718'=>array(-1500000,0,1500000,10000000)	// UTM 18 S
	,'EPSG:32719'=>array(-1500000,0,1500000,10000000)	// UTM 19 S
	,'EPSG:32720'=>array(-1500000,0,1500000,10000000)	// UTM 20 S
	,'EPSG:32721'=>array(-1500000,0,1500000,10000000)	// UTM 21 S
	,'EPSG:32722'=>array(-1500000,0,1500000,10000000)	// UTM 22 S
	,'EPSG:32723'=>array(-1500000,0,1500000,10000000)	// UTM 23 S
	,'EPSG:32724'=>array(-1500000,0,1500000,10000000)	// UTM 24 S
	,'EPSG:32725'=>array(-1500000,0,1500000,10000000)	// UTM 25 S
	,'EPSG:32726'=>array(-1500000,0,1500000,10000000)	// UTM 26 S
	,'EPSG:32727'=>array(-1500000,0,1500000,10000000)	// UTM 27 S
	,'EPSG:32728'=>array(-1500000,0,1500000,10000000)	// UTM 28 S
	,'EPSG:32729'=>array(-1500000,0,1500000,10000000)	// UTM 29 S
	,'EPSG:32730'=>array(-1500000,0,1500000,10000000)	// UTM 30 S
	,'EPSG:32731'=>array(-1500000,0,1500000,10000000)	// UTM 31 S
	,'EPSG:32732'=>array(-1500000,0,1500000,10000000)	// UTM 32 S
	,'EPSG:32733'=>array(-1500000,0,1500000,10000000)	// UTM 33 S
	,'EPSG:32734'=>array(-1500000,0,1500000,10000000)	// UTM 34 S
	,'EPSG:32735'=>array(-1500000,0,1500000,10000000)	// UTM 35 S
	,'EPSG:32736'=>array(-1500000,0,1500000,10000000)	// UTM 36 S
	,'EPSG:32737'=>array(-1500000,0,1500000,10000000)	// UTM 37 S
	,'EPSG:32738'=>array(-1500000,0,1500000,10000000)	// UTM 38 S
	,'EPSG:32739'=>array(-1500000,0,1500000,10000000)	// UTM 39 S
	,'EPSG:32740'=>array(-1500000,0,1500000,10000000)	// UTM 40 S
	,'EPSG:32741'=>array(-1500000,0,1500000,10000000)	// UTM 41 S
	,'EPSG:32742'=>array(-1500000,0,1500000,10000000)	// UTM 42 S
	,'EPSG:32743'=>array(-1500000,0,1500000,10000000)	// UTM 43 S
	,'EPSG:32744'=>array(-1500000,0,1500000,10000000)	// UTM 44 S
	,'EPSG:32745'=>array(-1500000,0,1500000,10000000)	// UTM 45 S
	,'EPSG:32746'=>array(-1500000,0,1500000,10000000)	// UTM 46 S
	,'EPSG:32747'=>array(-1500000,0,1500000,10000000)	// UTM 47 S
	,'EPSG:32748'=>array(-1500000,0,1500000,10000000)	// UTM 48 S
	,'EPSG:32749'=>array(-1500000,0,1500000,10000000)	// UTM 49 S
	,'EPSG:32750'=>array(-1500000,0,1500000,10000000)	// UTM 50 S
	,'EPSG:32751'=>array(-1500000,0,1500000,10000000)	// UTM 51 S
	,'EPSG:32752'=>array(-1500000,0,1500000,10000000)	// UTM 52 S
	,'EPSG:32753'=>array(-1500000,0,1500000,10000000)	// UTM 53 S
	,'EPSG:32754'=>array(-1500000,0,1500000,10000000)	// UTM 54 S
	,'EPSG:32755'=>array(-1500000,0,1500000,10000000)	// UTM 55 S
	,'EPSG:32756'=>array(-1500000,0,1500000,10000000)	// UTM 56 S
	,'EPSG:32757'=>array(-1500000,0,1500000,10000000)	// UTM 57 S
	,'EPSG:32758'=>array(-1500000,0,1500000,10000000)	// UTM 58 S
	,'EPSG:32759'=>array(-1500000,0,1500000,10000000)	// UTM 59 S
	,'EPSG:32760'=>array(-1500000,0,1500000,10000000)	// UTM 60 S
	
	// Universal Polar Stereographic (WGS 84)
	,'EPSG:32661'=>array(-4000000,-4000000,4000000,-4000000)	// UPS north
	,'EPSG:32761'=>array(-4500000,-4000000,4000000,-4000000)	// UPS south
);
	
	
	

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




