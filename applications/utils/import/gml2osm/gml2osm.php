<?php

/**
 * @file gml2osm
 * @brief GML to OSM convertor
 * 
 * This set of scripts can convert data from Geographic Markup Language (GML) to OpenStreetMap XML format (OSM).
 * 
 * @version 0.2
 * @date 2007-07-25
 *
 *
 * Changelog:
 * v0.2 - Added basic EGM metadata to OSM tags 
 * v0.1 - Basic Point, Linestring, MultiLinestring and MultiPolygon conversion to n/s/w.
 */

// proj4-based coordinate conversion wrapper
include('cs2cs_wrapper.php');


// aux. functions, to manage adding nodes/segments to the internal tables
// main reason behind this is to not duplicate nodes nor segments.
include('gml2osm_funcs.php');


if ($argc < 4)
{
	echo "Usage: gml2osm.php infile namespace outfile\n";
	echo "example: gml2osm.php foobar.gml foowfs foobar.osm\n";
	echo "The namespace is dependant on the GML source; peek at the GML file to guess it. It's the only namespace not being 'gml','xlink' or 'wfs'.\n\n";
	die();
}




/// TODO: add sanity check: does the input file exist?
/// TODO: add variable arguments to specify custom tags to be added to every element (source tags, mostly)
		    
$file = $argv[1];
$namespace = $argv[2];
$outfile = $argv[3];


// $filename gets printed on the tags, as source:file.
$filename = $file;

// The namespaces for features (featuremember) and tags (elements inside a featuremember) might be different in strange cases.
$tags_namespace = $feature_namespace = $namespace;


// ofd = Output File Descriptor
$ofd = fopen($outfile,'w');


$xml = simplexml_load_file($file);

// print_r($xml);
// print_r($xml->'gml:featureMember');


fwrite($ofd, "<?xml version='1.0' encoding='UTF-8'?>
<osm version='0.4' generator='ivansanchez_idee_importer'>");

$namespaces   = $xml->getDocNamespaces();
// print_r($xml_namespaces);
/// FIXME: add error checking: the namespaces used in the tags should be declared!!
/// TODO: This should detect any namespace different than xpath, gml and wfs.

/// Every GML document puts all the data in a gml:featureList and gml:featureMembers ... So, every GML document we'll parse will have the 'gml' namespace declared.
$xml_gml = $xml->children($namespaces['gml']);


/// $feature  is a <featureMember>
/// $feature2 is a <featureMember><custom>

$element_id = 0;
$way_tags = array();

foreach ($xml_gml->featureMember as $feature)
{
	// Parse data:
	/// TODO: one featuremember can have more than one piece of data inside??
	$feature = $feature->children($namespaces[$feature_namespace]);
	
	foreach($feature as $featuretype=>$feature2)
	{
		$tags = array();
		$mynodes= array();
		$myways = array();

		foreach($feature2 as $key=>$tag)
		{
			if ($tag)	// Standard namespace data, add to tags
			{
				$tags["$tags_namespace:$key"] = (string) $tag;
			}
			else	// This might be an invisible SimpleXML object, containing the geometry.
			{
				$geometries = $tag->children($namespaces['gml']);
				
				foreach($geometries as $geom_type=>$geometry)
				{
					if ($geom_type=='Point')
					{
						$mynodes[] = point2node($geometry);
					}
					else if ($geom_type=='LineString')
					{
						$myways[] = linestring2way($geometry);
					}
					else if ($geom_type=='MultiLineString')
					{
						foreach($geometry->lineStringMember as $linestringmember)
						{
							$myways[] = linestring2way($linestringmember->LineString);
						}
					}
					else if ($geom_type=='MultiPolygon')
					{
						foreach($geometry->polygonMember as $polygonMember)
						{
							/// TODO: check that a linear ring is indeed a linear ring (a linestring that ends in its first point)
							/// TODO: Add support for inner rings
							/// TODO: merge segments from the inner rings into the outer ring, delete inner ring way IDs.
							/// TODO: check topology of polygons - we're supposing that they're just right.
							$myways[] = $outerring = linestring2way($polygonMember->Polygon->outerBoundaryIs->LinearRing);
						}
					}
				}
			}
		}
		
		
		
		/// Generic gml-to-osm metadata-to-tags conversion
		// Two generic conversions are done: everything in the $tags_namespace has already been added as a $namespace:$tag=$value tag, and every attribute in the GML nodes will be added as gml:$attr=$value.
		
		$attrs = $feature2->attributes($namespaces['gml']);
		
		foreach ($attrs as $key=>$attr)
		{
			$tags["gml:$key"] = $attr;
		}
		
		$tags['source:filename'] = $file;
		
		/// EuroGlobalMaps metadata conversion
		include('gml2osm_egmtags.php');
		
		
// 		print_r($tags);
		
		// This may overwrite previously defined tags for that node!!!!!!!!
		foreach($mynodes as $node_id)
		{
			$node_tags[$node_id] = $tags;
		}
		foreach($myways as $way_id)
		{
			print_r($myways);
			$way_tags[$way_id] = $tags;
		}
	}
}



// Everything has been parsed; now, let's output everything...


// print_r($node_list);

foreach($node_tags as $node_id=>$tags)
{
	$node_count++;
	list($lat,$lon) = $node_coords[$node_id];
	fwrite($ofd, "<node id='$node_id' visible='true' lat='$lat' lon='$lon' >\n");
	foreach($tags as $k=>$tag)
	{
		$k   = htmlspecialchars($k);
		$tag = htmlspecialchars($tag);
		fwrite($ofd, "<tag k=\"$k\" v=\"$tag\" />\n");
	}
	fwrite($ofd, "</node>\n");
}



foreach($segment_list as $from=>$tos)
{
	$segment_count++;
	foreach($tos as $to=>$segment_id)
	{
		fwrite($ofd, "<segment id='$segment_id' visible='true' from='$from' to='$to' />\n");
	}
}



foreach($way_tags as $way_id=>$tags)
{
	$way_count++;
	fwrite($ofd, "<way id='$way_id' visible='true'>\n");
	
	$segments_in_this_way = $way_list[$way_id];
	foreach($segments_in_this_way as $segment_id)
	{
		fwrite($ofd, "<seg id='$segment_id'/>\n");
	}
	
	foreach($tags as $k=>$tag)
	{
		$k   = htmlspecialchars($k);
		$tag = htmlspecialchars($tag);
		fwrite($ofd, "<tag k=\"$k\" v=\"$tag\" />\n");
	}
	fwrite($ofd, "</way>\n");
}


fwrite($ofd, "<!-- \nTotal:\nNodes: $node_count\nSegments: $segment_count\nWays:$way_count\n-->\n");




fwrite($ofd, "\n</osm>\n");
?>