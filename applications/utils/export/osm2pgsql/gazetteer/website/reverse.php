<?php

        require_once('.htlib/init.php');
        ini_set('memory_limit', '200M');

        // Prefered language
        $aLangPrefOrder = getPrefferedLangauges();
        $sLanguagePrefArraySQL = "ARRAY[".join(',',array_map("getDBQuoted",$aLangPrefOrder))."]";

	// Lokcation to look up
	$fLat = (float)$_GET['lat'];
	$fLon = (float)$_GET['lon'];
	$sPointSQL = "ST_SetSRID(ST_Point($fLon,$fLat),4326)";

	// Zoom to rank, this could probably be calculated but a lookup gives fine control
	$aZoomRank = array(
		0 => 2, // Continent / Sea
		1 => 2,
		2 => 2,
		3 => 4, // Country
		4 => 4,
		5 => 8, // State
		6 => 10, // Region
		7 => 10, 
		8 => 12, // County
		9 => 12,  
		10 => 17, // City
		11 => 17, 
		12 => 18, // Town / Village
		13 => 18, 
		14 => 22, // Suburb
		15 => 22,
		16 => 26, // Street, TODO: major street?
		17 => 26, 
		18 => 28, // or >, Building
		);
	$iMaxRank = isset($aZoomRank[$_GET['zoom']])?$aZoomRank[$_GET['zoom']]:28;

	// Find the nearest point
	$fSearchDiam = 0.0001;
	$iPlaceID = null;
	$aArea = false;
	$fMaxAreaDistance = 180;
	while(!$iPlaceID && $fSearchDiam < $fMaxAreaDistance)
	{
		$fSearchDiam = $fSearchDiam * 2;

		// If we have to expand the search area by a large amount then we need a larger feature
		// then there is a limit to how small the feature should be
		if ($fSearchDiam > 2 && $iMaxRank > 4) $iMaxRank = 4;
		if ($fSearchDiam > 1 && $iMaxRank > 9) $iMaxRank = 8;
		if ($fSearchDiam > 0.8 && $iMaxRank > 10) $iMaxRank = 10;
		if ($fSearchDiam > 0.6 && $iMaxRank > 12) $iMaxRank = 12;
		if ($fSearchDiam > 0.2 && $iMaxRank > 17) $iMaxRank = 17;
		if ($fSearchDiam > 0.1 && $iMaxRank > 18) $iMaxRank = 18;
		if ($fSearchDiam > 0.01 && $iMaxRank > 22) $iMaxRank = 22;

		if ($iMaxRank >= 26)
		{
			// Street level search is done using placex table
			$sSQL = 'select place_id from placex';
			$sSQL .= ' WHERE ST_DWithin('.$sPointSQL.', geometry, '.$fSearchDiam.')';
			$sSQL .= ' and rank_search >= 26 and rank_search <= '.$iMaxRank;
			$sSQL .= ' and (ST_GeometryType(geometry) not in (\'ST_Polygon\',\'ST_MultiPolygon\') ';
			$sSQL .= ' OR ST_DWithin('.$sPointSQL.', ST_Centroid(geometry), '.$fSearchDiam.'))';
			$sSQL .= ' ORDER BY rank_search desc, ST_distance('.$sPointSQL.', geometry) ASC limit 1';
			$iPlaceID = $oDB->getOne($sSQL);
			if (PEAR::IsError($iPlaceID))
			{
				var_Dump($sSQL, $iPlaceID); 
				exit;
			}
		}
		else
		{
			// Other search uses the location_point and location_area tables

			// If we've not yet done the area search do it now
			if ($aArea === false)
			{
				$sSQL = 'select place_id,rank_address,ST_distance('.$sPointSQL.', centroid) as distance from location_area';
				$sSQL .= ' WHERE ST_Contains(area,'.$sPointSQL.') and rank_search <= '.$iMaxRank;
				$sSQL .= ' ORDER BY rank_address desc, ST_distance('.$sPointSQL.', centroid) ASC limit 1';
				$aArea = $oDB->getRow($sSQL);
				if ($aArea) $fMaxAreaDistance = $aArea['distance'];
			}

			// Different search depending if we found an area match
			if ($aArea)
			{
				// Found best match area - is there a better point match?
				$sSQL = 'select place_id from location_point_'.($iMaxRank+1);
				$sSQL .= ' WHERE ST_DWithin('.$sPointSQL.', centroid, '.$fSearchDiam.') ';
				$sSQL .= ' and rank_search > '.($aArea['rank_address']+3);
				$sSQL .= ' ORDER BY rank_address desc, ST_distance('.$sPointSQL.', centroid) ASC limit 1';
//				var_dump($sSQL);
				$iPlaceID = $oDB->getOne($sSQL);
				if (PEAR::IsError($iPlaceID))
				{
					var_Dump($sSQL, $iPlaceID); 
					exit;
				}
			}
			else
			{
				$sSQL = 'select place_id from location_point_'.($iMaxRank+1);
				$sSQL .= ' WHERE ST_DWithin('.$sPointSQL.', centroid, '.$fSearchDiam.') ';
				$sSQL .= ' ORDER BY rank_address desc, ST_distance('.$sPointSQL.', centroid) ASC limit 1';
//				var_dump($sSQL);
				$iPlaceID = $oDB->getOne($sSQL);
				if (PEAR::IsError($iPlaceID))
				{
					var_Dump($sSQL, $iPlaceID); 
					exit;
				}
			}
		}
	}
	if (!$iPlaceID && $aArea) $iPlaceID = $aArea['place_id'];

//	echo "<hr>$iMaxRank : $fSearchDiam : $iPlaceID : ";

	$sSQL = "select placex.*,";
        $sSQL .= " get_address_by_language(place_id, $sLanguagePrefArraySQL) as langaddress,";
        $sSQL .= " get_name_by_language(name, $sLanguagePrefArraySQL) as placename,";
        $sSQL .= " get_name_by_language(name, ARRAY['ref']) as ref";
        $sSQL .= " from placex where place_id = $iPlaceID ";
	$aPlace = $oDB->getRow($sSQL);
	echo $aPlace['langaddress'];

