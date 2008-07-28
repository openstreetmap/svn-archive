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

/// Wireframe class
/// Lots of TODOs here - at some time, this will be a full-blown WMS conformant server.
/// TODO: reproject the queried area into EPSG:4236
/// TODO: reproject the data from OSM API into the queried coordinate system.
/// TODO: rely the data parsing/downloading to some backend class
/// TODO: fix for near-180 longitudes
/// TODO: error messages



class wireframe /* extends layer */
{

static public function GetCapabilities()
{
// return "
//       <Layer queryable='0' opaque='0'>
//         <Name>wireframe</Name>
//         <Title>OSM Wireframe</Title>
//         <EX_GeographicBoundingBox>
//           <westBoundLongitude>-180</westBoundLongitude>
//           <eastBoundLongitude>180</eastBoundLongitude>
//           <southBoundLatitude>-90</southBoundLatitude>
//           <northBoundLatitude>90</northBoundLatitude>
//         </EX_GeographicBoundingBox>
//         <Style>
//           <Name>Default</Name>
//           <Title>Default</Title>
//         </Style>
//       </Layer>
// ";

// Capabilities for WMS 1.1.0 
return "
      <Layer queryable='0' opaque='0'>
        <Name>wireframe</Name>
        <Title>OSM Wireframe</Title>
	<Style>
          <Name>Default</Name>
          <Title>Default</Title>
        </Style>
      </Layer>
";


// 	<BoundingBox SRS='EPSG:4326' minx='-180' miny='-90' maxx='180' maxy='90' />
// 	<BoundingBox SRS='EPSG:32630' minx='-1050000' miny='3000000' maxx='1150000' maxy='5000000' />


}


static public function GetMap($bbox,$crs,$height,$width,$format)
{
	// Backend class should be able to return PHP arrays (nodes/ways), string, URL.
// 	list($left,$bottom,$right,$top) = explode(',',$bbox);
	
// 	backend_api::get_parsed_data($bbox,$nodes,$ways);
// 	backend_osmxapi::get_parsed_data($bbox,$nodes,$ways);
// 	$backend = new backend_osmxapi;
// 	$backend->get_parsed_data($bbox,$nodes,$ways);

	datafactory::get_parsed_data($bbox,$crs,$nodes,$ways,$relations);
	
	/// Convert the node's coordinates into X-Y, related to (0,0) and the final image size
	
	list($left,$bottom,$right,$top) = explode(',',$bbox);	// Numbers are OK if the backend hasn't thrown an error yet.
	
	$x_factor = $width  / ($right - $left)  ;
	$y_factor = $height / ($top   - $bottom);

	foreach($nodes as &$node)
	{
		// Lat
		$node[0] = $height - ($node[0]-$bottom) * $y_factor;
		
		// Lon
		$node[1] = ($node[1]-$left) * $x_factor;
	}
	
// 	foreach($nodes as $id=>$node)
// 	{
// 		echo "$id: {$node[0]},{$node[1]}\n";
// 	}
// 	print_r($nodes);
	
// 	echo "x-f: $x_factor; y-f: $y_factor; bbox: $bbox\n\n";
	
	
	/// Prepare GD stuff for the image
	
	$im = imagecreate ( $width , $height );
	
	if (isset($_REQUEST['BGCOLOR']))
	{
		sscanf ($_REQUEST['BGCOLOR'],"0x%02X%02X%02X",$red,$green,$blue);
		$backgroundcolor = imagecolorallocate ( $im , $red,$green,$blue );
	}
	else
	{
		$backgroundcolor = imagecolorallocate ( $im , 0 , 0 , 0 );	// Black
	}
	
	if ($_REQUEST['TRANSPARENT'])
		imagecolortransparent( $im , $backgroundcolor );
	
	$nodecolor = imagecolorallocate ( $im , 255 , 0 , 0 );	// Red
	$waycolor  = imagecolorallocate ( $im , 0 , 0 , 255 );	// Blue
	
	
	foreach($ways as $way)
	{
		$first = true;
		foreach($way['nodes'] as $node_ref)
		{
			list ($y,$x) = $nodes[$node_ref];
			if ($first)
				$first = false;
			else
				imageline( $im , $oldx , $oldy , $x , $y , $waycolor );
			$oldx = $x; $oldy = $y;
		}
	}
	
	foreach($nodes as $node)
	{
		list ($y,$x,$ts) = $node;
// 		if (!empty($ts))
			imagerectangle( $im , $x-1 , $y-1 , $x+1 , $y+1 , $nodecolor );
// 		else
// 		{
// 			imagerectangle( $im , $x-1 , $y-1 , $x+1 , $y+1 , $nodecolor );
// 			imagerectangle( $im , $x-3 , $y-3 , $x+3 , $y+3 , $nodecolor );
// 		}
	}
	
	
	// imagepng($im,'output.png');
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