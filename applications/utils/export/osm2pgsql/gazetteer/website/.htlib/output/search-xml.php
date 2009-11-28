<?php
	header ("content-type: text/xml");
	echo "<";
	echo "?xml version=\"1.0\" encoding=\"UTF-8\" ?";
	echo ">\n";

	echo "<searchresults";
	echo " timestamp='".date(DATE_RFC822)."'";
	echo " querystring='".htmlspecialchars($sQuery, ENT_QUOTES)."'";
	if ($sViewBox) echo " viewbox='".htmlspecialchars($sViewBox, ENT_QUOTES)."'";
	echo " polygon='".($bShowPolygons?'true':'false')."'";
	echo ">\n";

	foreach($aSearchResults as $iResNum => $aResult)
	{
		echo "<place place_id='".$aResult['place_id']."'";
		$sOSMType = ($aResult['osm_type'] == 'N'?'node':($aResult['osm_type'] == 'W'?'way':($aResult['osm_type'] == 'R'?'relation':'')));
		if ($sOSMType)
		{
			echo " osm_type='$sOSMType'";
			echo " osm_id='".$aResult['osm_id']."'";
		}

		if (isset($aResult['aBoundingBox']))
		{
			echo ' boundingbox="';
			echo $aResult['aBoundingBox'][0];
			echo ','.$aResult['aBoundingBox'][1];
			echo ','.$aResult['aBoundingBox'][2];
			echo ','.$aResult['aBoundingBox'][3];
			echo '"';

			if ($bShowPolygons)
			{
				echo ' polygonpoints=\'';
				echo javascript_renderData($aResult['aPolyPoints']);
				echo '\'';
			}
		}

		if (isset($aResult['zoom']))
		{
			echo " zoom='".$aResult['zoom']."'";
		}

		echo " lat='".$aResult['lat']."'";
		echo " lon='".$aResult['lon']."'";
		echo " display_name='".htmlspecialchars($aResult['name'], ENT_QUOTES)."'";

		echo " class='".htmlspecialchars($aResult['class'])."'";
		echo " type='".htmlspecialchars($aResult['type'])."'";
		if ($aResult['icon'])
		{
			echo " icon='".htmlspecialchars($aResult['icon'], ENT_QUOTES)."'";
		}

		if (isset($aResult['address']))
		{
			echo ">";
			foreach($aResult['address'] as $sKey => $sValue)
			{
				$sKey = str_replace(' ','',$sKey);
				echo "<$sKey>";
				echo htmlspecialchars($sValue);
				echo "</$sKey>";
			}

			echo "</place>";
		}
		else
		{
			echo "/>";
		}
	}
	
	echo "</searchresults>";
