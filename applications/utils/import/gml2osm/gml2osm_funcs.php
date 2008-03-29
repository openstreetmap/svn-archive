<?php

/// Auxiliary functions for the gml2osm script. These manage querying and inserting data into internal data structures, like the node list and the segment list.
/// These do *not* manage the metadata (tags) for the nodes and ways; that's the work of the main script.
/// Also, this doesn't manage writing out the OSM XML to the output file. Again, main script's work.




// Global vars
$entity_id = 0;
$node_list = array();
$segment_list = array();
$way_list = array();

$node_coords = array();



/// @param gml A SimpleXML instance of gml:Point, containing the node coordinates
function point2node($gml)
{
	$gml_attrs = $gml->attributes();
	$source_srs = $gml_atrts['srsName'];
	$point_attrs = $gml->coordinates->attributes();
	$cs      = $point_attrs['cs'];	// Coordinate separator
	$decimal = $point_attrs['decimal'];	// Decimal point separator.
	
		// Default values...
	if (!$cs) $cs = ',';
	if (!$decimal) $decimal = '.';
	
	list($lon,$lat) = explode($cs,$gml->coordinates);
	
	// coordinate transformation
	if ($source_srs)
		list($lat,$lon) = cs2cs($lat,$lon,$source_srs);
	
	
	global $node_list;
	if (isset($node_list[$lat][$lon]))
	{
		return $node_list[$lat][$lon]; // Node already exists, return its ID.
		// This, happening in point2node, might be problematic. 
		/// FIXME: throw a warning.
	}
	else
	{
		global $entity_id, $node_coords;
		$entity_id--;
		$node_coords[$entity_id] = array($lat,$lon);
		return $node_list[$lat][$lon] = $entity_id;
	}
	
}





/// @param gml A SimpleXML instance of gml:lineString, containing the way coordinates
/// @param boundingbox A SimpleXML instance of gml:Box, containing the way bounding box. Will be used for fixing malformed GML linestrings on a best-effort basis, using the data on the bounding box as an endpoint of the linestring, if possible.
/// @return An entity ID corresponding to the newly created way.
function linestring2way($gml,$boundingbox=NULL)
{
	$gml_attrs  = $gml->attributes();
	$source_srs = $gml_atrts['srsName'];
	$line_attrs = trim($gml->coordinates->attributes());
	$cs      = $line_attrs['cs'];	// Coordinate separator
	$decimal = $line_attrs['decimal'];	// Decimal point separator
	$ts      = $line_attrs['ts'];	// Tuple separator
	
	// Default values...
	if (!$cs) $cs = ',';
	if (!$decimal) $decimal = '.';
	if (!$ts) $ts = ' ';
	
	$points = explode($ts,$gml->coordinates);
	
	/// TODO: refactor code: lots in common with point2node!
	global $node_list,$segment_list,$way_list;
	global $entity_id, $node_coords, $node_tags;
	$mynodes = array();
	$mysegments = array();
	$lats = array();
	$lons = array();
	$lastnode = null;
	
	$coord_count = 0;
	foreach($points as $point)
	{
		if ($point = trim($point))
		{
			list($lon,$lat) = explode($cs,$point);
			
			$lats[] = $lat;
			$lons[] = $lon;
			$coord_count++;
		}
	}
	
	
	/// HACK: if the starting point of the geometry is in a corner of the bounding box, will force the endpoint to be the opposite corner.
	if ($boundingbox)
	{
		$bpoints = explode($ts,trim($boundingbox->coordinates));
		list($blon0, $blat0) = explode($cs,$bpoints[0]);
		list($blon1, $blat1) = explode($cs,$bpoints[1]);
		$firstlat = $lats[0];
		$firstlon = $lons[0];
		
		echo "Way with $coord_count points so far; $firstlat vs ($blat0, $blat1) ,$firstlon vs ($blon0,$blon1)\n";
		
		// Case-by-case:
		$newpoint = true;
		if ($firstlat == $blat0 && $firstlon == $blon0)
			{$lats[] = $blat1; $lons[] = $blon1;}
		elseif ($firstlat == $blat0 && $firstlon == $blon1)
			{$lats[] = $blat1; $lons[] = $blon0;}
		elseif ($firstlat == $blat1 && $firstlon == $blon0)
			{$lats[] = $blat0; $lons[] = $blon1;}
		elseif ($firstlat == $blat1 && $firstlon == $blon1)
			{$lats[] = $blat0; $lons[] = $blon0;}
		else
			$newpoint = false;
			
		if ($newpoint) echo "Added a new point to the tail of the way\n";
	}

// 	if ($coord_count == 2 && $hint)
// 	{
// // 		print_r($lats); print_r($lons); print_r($hint);
// 	
// 		if (($lats[0] == $hint['lat']
// 		  && $lons[1] == $hint['lon'])
// 		 || ($lats[1] == $hint['lat']
// 		  && $lons[0] == $hint['lon']))
// 		{
// 			//Swap just the lats
// 			$lat0 = $lats[0];
// 			$lat1 = $lats[1];
// 			$lats = array( 0=>$lat1, 1=>$lat0 );
// 			echo "Warning: While fixing malformed GML, swapped the bounding box!\n";
// 		}
// 	}
	
	
	foreach ($lats as $key=>$lat)
	{
		$lon = $lons[$key];
		
		// coordinate transformation
		if ($source_srs)
			list($lat,$lon) = cs2cs($lat,$lon,$source_srs);
		
		
		if (isset($node_list[$lat][$lon]))
		{
			$nodeid = $node_list[$lat][$lon]; // Node already exists, return its ID.
		}
		else
		{
			$entity_id--;
			echo $entity_id . "        ($lat,$lon)\n";
			$node_coords[$entity_id] = array($lat,$lon);
			$nodeid = $node_list[$lat][$lon] = $entity_id;
			$node_tags[$nodeid] = array();	// The main script needs $node_tags to iterate over.
		}
		$mynodes[] = $node_list[$lat][$lon] = $nodeid;
	}
	
	$entity_id--;
	echo $entity_id . " (way) \n";
	$way_list[$entity_id] = $mynodes;
	
	return $entity_id;
	
}










