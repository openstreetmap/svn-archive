<?php


/// Naga City metadata conversion, reverse-engineered from public domain data.
/// This script supposes an already defined $tags array, containing elements of the form $tags["$tags_namespace:$key"] = $value.
/// Those values will be mapped to OSM tags standards as best as it can be done.




// Temp variable to improbe code readability
$n = 'nagacity';






if (isset($tags["$n:HOSP_NO"]))	// This is a hospital
{
	if ($tags["$n:TYPE"] == 'PUBLIC')
		$tags['amenity'] = 'hospital';
	else
		$tags['amenity'] = 'clinic';
	
	$tags['name'] = ucwords(strtolower($tags["$n:NAME"]));
	$tags["$n:address"] = ucwords(strtolower($tags["$n:ADDRESS"]));
	$tags["$n:owner"] = ucwords(strtolower($tags["$n:OWNER"]));

	unset ($tags["$n:ID"]);
	unset ($tags["$n:HOSP_NO"]);
	unset ($tags["$n:NAME"]);
	unset ($tags["$n:DATEFOUND"]);
	unset ($tags["$n:ADDRESS"]);
	unset ($tags["$n:OWNER"]);
	unset ($tags["$n:TYPE"]);
	unset ($tags["$n:BEDS"]);
	unset ($tags["$n:DOCTORS"]);
	unset ($tags["$n:NURSES"]);
	unset ($tags["$n:DENTIST"]);
	unset ($tags["$n:MIDWIVES"]);
	unset ($tags["$n:ATTENDANTS"]);
	unset ($tags["$n:AVEPAY"]);
	unset ($tags["$n:AVECHA"]);
	unset ($tags["$n:AVEMED"]);
}










if (isset($tags["$n:SCH_NO"]))	// This is a school
{
	if ($tags["$n:CLASS"] == 'ELEMENTARY')
		$tags['amenity'] = 'school';
	elseif ($tags["$n:CLASS"] == 'HIGH SCHOOL')
	{
		$tags['amenity'] = 'school';
		$tags['note'] = 'High school';
	}
	elseif ($tags["$n:CLASS"] == 'COLLEGE')
		$tags['amenity'] = 'college';
	elseif ($tags["$n:CLASS"] == 'PRE-SCHOOL')
		$tags['amenity'] = 'preschool';
	
	
	$tags['name'] = ucwords(strtolower($tags["$n:NAME"]));
	$tags["$n:address"] = ucwords(strtolower($tags["$n:ADDRESS"]));
	$tags["$n:contact"] = ucwords(strtolower($tags["$n:CONTACT"]));
	$tags["$n:contact_position"] = ucwords(strtolower($tags["$n:POSITION"]));
	$tags["$n:classrooms"] = ucwords(strtolower($tags["$n:CLASSROOMS"]));
	$tags["$n:teachers"] = ucwords(strtolower($tags["$n:TEACHERS"]));
	$tags["$n:ctype"] = ucwords(strtolower($tags["$n:CTYPE"]));
	$tags["$n:type"] = ucwords(strtolower($tags["$n:TYPE"]));

	unset ($tags["$n:ID"]);
	unset ($tags["$n:SCH_NO"]);
	unset ($tags["$n:NAME"]);
	unset ($tags["$n:CLASS"]);
	unset ($tags["$n:ADDRESS"]);
	unset ($tags["$n:CONTACT"]);
	unset ($tags["$n:POSITION"]);
	unset ($tags["$n:CTYPE"]);
	unset ($tags["$n:TYPE"]);
	unset ($tags["$n:CLASSROOMS"]);
	unset ($tags["$n:TEACHERS"]);
	
	if (!$tags["$n:EELEM"]) unset ($tags["$n:EELEM"]);
	if (!$tags["$n:EHS"]) unset ($tags["$n:EHS"]);
	if (!$tags["$n:ECOL1"]) unset ($tags["$n:ECOL1"]);
	if (!$tags["$n:ECOL2"]) unset ($tags["$n:ECOL2"]);
	if (!$tags["$n:DELEM"]) unset ($tags["$n:DELEM"]);
	if (!$tags["$n:DHS"]) unset ($tags["$n:DHS"]);
	if (!$tags["$n:DCOL1"]) unset ($tags["$n:DCOL1"]);
	if (!$tags["$n:DCOL2"]) unset ($tags["$n:DCOL2"]);

}




if (isset($tags["$n:RAILROAD_"]))	// This is a school
{
	$tags['railway'] = 'rail';

	unset ($tags["$n:LENGTH_"]);
	unset ($tags["$n:FNODE_"]);
	unset ($tags["$n:TNODE_"]);
	unset ($tags["$n:LPOLY_"]);
	unset ($tags["$n:RPOLY_"]);
	unset ($tags["$n:RAILROAD_"]);
	unset ($tags["$n:RAILROAD_I"]);
}






if (isset($tags["$n:ROAD_NO"]))	// This is a school
{
	if ($tags["$n:SURF_TYPE"] == '********************')
	{
		$tags['highway'] = 'track';
		$tags['surface'] = 'unpaved';
	}
	else
	{
		$tags['surface'] = 'paved';
		$tags['highway'] = 'unclassified';
// 	else
// 		$tags['highway'] = 'motorway';
	}
	
	if ($tags["$n:CATEG_ID"] == '1')
		$tags['highway'] = 'residential';
	elseif ($tags["$n:CATEG_ID"] == '2')
		$tags['highway'] = 'secondary';
	elseif ($tags["$n:CATEG_ID"] == '3')
 		$tags['highway'] = 'footway';
	elseif ($tags["$n:CATEG_ID"] == '4')
		$tags['highway'] = 'motorway';
	elseif ($tags["$n:CATEG_ID"] == '5')
		$tags['highway'] = 'track';
	else
		$tags['highway'] = 'motorway';
	
	
	$tags['width'] = (float) $tags["$n:RWIDTH"];

	$tags['name'] = ucwords(strtolower($tags["$n:ROADNAME"]));
// 	$tags['name'] = ucwords(strtolower($tags["$n:ROADNAME"]));
	$tags["$n:rdbuffer"] = (float) $tags["$n:RFBUFFER"];
	$tags["$n:surf_type"] = (int) $tags["$n:SURF_TYPE"];
	$tags["$n:cond_id"] = (int) $tags["$n:COND_ID"];
	$tags["$n:categ_id"] = (int) $tags["$n:CATEG_ID"];
	$tags["$n:length"] = (int) $tags["$n:LENGTH"];

	unset ($tags["$n:LENGTH_"]);
	unset ($tags["$n:FNODE_"]);
	unset ($tags["$n:TNODE_"]);
	unset ($tags["$n:LPOLY_"]);
	unset ($tags["$n:RPOLY_"]);
	unset ($tags["$n:RSHOULDER"]);
	unset ($tags["$n:ROADNAME"]);
	unset ($tags["$n:SURF_TYPE"]);
	unset ($tags["$n:RWIDTH"]);
	unset ($tags["$n:LENGTH"]);
	unset ($tags["$n:RDBUFFER"]);
	unset ($tags["$n:ROAD99V4_"]);
	unset ($tags["$n:ROAD99V4_I"]);
	unset ($tags["$n:ROAD_NO"]);
	unset ($tags["$n:COND_ID"]);
	unset ($tags["$n:CATEG_ID"]);
	
// 	var_dump($newpoints[$way_id]);
	if (! $newpoints[$way_id])
	{
		$tags['gml2osm_conversion_note'] = 'FIXME';
		$tags['FIXME'] = 'This way may be missing a node in the endpoint.';
	}
	
}




if (isset($tags["$n:RIVER99_ID"]))	// This is a river area
{
	$tags['waterway'] = 'riverbank';
}



if (isset($tags["$n:BLDG99_"]))	// This is a building
{
	$tags['building']   = 'true';
	$tags['source']     = 'Naga City GIS data';
	$tags['source:url'] = 'http://gis.naga.gov.ph/pmwiki/pmwiki.php/Main/Data';
	unset ($tags["$n:BLDG99_"]);
	unset ($tags["$n:BLDG99_ID"]);
	unset ($tags["$n:AREA"]);
	unset ($tags["$n:PERIMETER"]);
}


if (isset($tags["$n:PLUSE2K_"]))
{
	unset ($tags["$n:AREA"]);
	unset ($tags["$n:PERIMETER"]);
	unset ($tags["$n:PLUSE2K_"]);
	unset ($tags["$n:PLUSE2K_ID"]);

//         <AREA>463897.40000</AREA>
//         <PERIMETER>5336.831000</PERIMETER>
//         <PLUSE2K_>6</PLUSE2K_>
//         <PLUSE2K_ID>0</PLUSE2K_ID>
//         <SYMBOL>37</SYMBOL>
//         <CLASS>RESIDENTIAL</CLASS>

	$tags["$n:landuse"] = ucwords(strtolower($tags["$n:CLASS"]));

	if     ($tags["$n:CLASS"] == 'ECOTURISM')
	{	$tags['leisure'] = 'park';	}
	elseif ($tags["$n:CLASS"] == 'FOREST RESERVES')
	{	$tags['landuse'] = 'forest';	}
	elseif ($tags["$n:CLASS"] == 'AGRICULTURAL')
	{	$tags['landuse'] = 'farm';	}
	elseif ($tags["$n:CLASS"] == 'INDUSTRIAL')
	{	$tags['landuse'] = 'industrial';	}
	elseif ($tags["$n:CLASS"] == 'RESIDENTIAL')
	{	$tags['landuse'] = 'residential';	}
	elseif ($tags["$n:CLASS"] == 'INSTITUTIONAL')
	{	$tags['landuse'] = 'commercial';	}
	elseif ($tags["$n:CLASS"] == 'COMMERCIAL')
	{	$tags['landuse'] = 'retail';	}
	elseif ($tags["$n:CLASS"] == 'IRRIGABLE LAND')
	{	$tags['landuse'] = 'farm';	}
	elseif ($tags["$n:CLASS"] == 'AGRICULTURAL NURSERY')
	{	$tags['landuse'] = 'plant nursery';	}
	elseif ($tags["$n:CLASS"] == 'ECOPARK')
	{	$tags['landuse'] = 'park';	}
	elseif ($tags["$n:CLASS"] == 'CEMETERIES')
	{	$tags['landuse'] = 'cemetery';	}
	elseif ($tags["$n:CLASS"] == 'RIVER')
	{	$tags['natural'] = 'water';	}
	elseif ($tags["$n:CLASS"] == 'AGRO-INDUSTRIAL')
	{	$tags['landuse'] = 'industrial';	}
	elseif ($tags["$n:CLASS"] == 'TRANSPORT UTILITIES')
	{	$tags['landuse'] = 'railway';	}
	elseif ($tags["$n:CLASS"] == 'DUMPSITE')
	{	$tags['landuse'] = 'landfill';	}
	elseif ($tags["$n:CLASS"] == 'PROPOSED DUMPSITE')
	{	$tags['landuse'] = 'landfill';	}
	elseif ($tags["$n:CLASS"] == 'PARKS')
	{	$tags['leisure'] = 'park';	}

	unset($tags["$n:CLASS"]);
	unset($tags["$n:SYMBOL"]);

//   'ECOTOURISM' => 2,
//   'FOREST RESERVES' => 1,
//   'AGRICULTURAL' => 23,
//   'INDUSTRIAL' => 7,
//   'RESIDENTIAL' => 36,
//   'INSTITUTIONAL' => 46,
//   'COMMERCIAL' => 15,
//   'IRRIGABLE LAND' => 20,
//   'AGRICULTURAL NURSERY' => 1,
//   'ECOPARK' => 1,
//   'CEMETERIES' => 5,
//   'RIVER' => 1,
//   'AGRO-INDUSTRIAL' => 7,
//   'TRANSPORT UTILITIES' => 4,
//   'DUMPSITE' => 1,
//   'PROPOSED DUMPSITE' => 1,
//   'PARKS' => 4,

// 	$debug_landuse_symbols[$tags["$n:SYMBOL"]]++;
// 	$debug_landuse_classes[$tags["$n:CLASS"]]++;
	
	$tags['source']     = 'Naga City GIS data';
	$tags['source:url'] = 'http://gis.naga.gov.ph/pmwiki/pmwiki.php/Main/Data';
}

