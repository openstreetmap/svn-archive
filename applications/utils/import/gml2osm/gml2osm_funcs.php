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
function linestring2way($gml)
{
	$gml_attrs  = $gml->attributes();
	$source_srs = $gml_atrts['srsName'];
	$line_attrs = $gml->coordinates->attributes();
	$cs      = $line_attrs['cs'];	// Coordinate separator
	$decimal = $line_attrs['decimal'];	// Decimal point separator
	$ts      = $line_attrs['ts'];	// Tuple separator
	
	$points = explode($ts,$gml->coordinates);
	
	/// TODO: refactor code: lots in common with point2node!
	global $node_list,$segment_list,$way_list;
	global $entity_id, $node_coords, $node_tags;
	$mynodes = array();
	$mysegments = array();
	$lastnode = null;
	foreach($points as $point)
	{
		list($lon,$lat) = explode($cs,$point);
	
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
			echo $entity_id . "            ($lat,$lon)\n";
			$node_coords[$entity_id] = array($lat,$lon);
			$nodeid = $node_list[$lat][$lon] = $entity_id;
			$node_tags[$nodeid] = array();	// The main script needs $node_tags to iterate over.
		}
		$mynodes[] = $node_list[$lat][$lon] = $nodeid;
		
		
		// Build a new segment
		if ($lastnode)
		{
			if (isset($segment_list[$lastnode][$nodeid]))	// Segment already exists
			{
				$mysegments[] = $segment_list[$lastnode][$nodeid];
			}
			else if (isset($segment_list[$nodeid][$lastnode]))	// Segment exists, but in the opposite direction
			{
				$mysegments[] = $segment_list[$nodeid][$lastnode];
			}
			else
			{
				$entity_id--;
				echo $entity_id . "       (seg) \n";
				$mysegments[] = $segment_list[$lastnode][$nodeid] = $entity_id;
			}
		}
		$lastnode = $nodeid;
		
	}
	
	$entity_id--;
	echo $entity_id . " (way) \n";
	$way_list[$entity_id] = $mysegments;
	
	return $entity_id;
	
}










