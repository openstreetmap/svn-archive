<?php
	header ("content-type: text/xml");
	echo "<";
	echo "?xml version=\"1.0\" encoding=\"UTF-8\" ?";
	echo ">\n";

	echo "<reversegeocode";
	echo " timestamp='".date(DATE_RFC822)."'";
	echo " attribution='Data Copyright OpenStreetMap Contributors, Some Rights Reserved. CC-BY-SA 2.0.'";
	echo " querystring='".htmlspecialchars($_SERVER['QUERY_STRING'], ENT_QUOTES)."'";
	echo ">\n";

	echo "<result";
	if ($aPlace['place_id']) echo ' place_id="'.$aPlace['place_id'].'"';
	if ($aPlace['osm_type']&&$aPlace['osm_id']) echo ' osm_type="'.($aPlace['osm_type']=='N'?'node':($aPlace['osm_type']=='W'?'way':'relation')).'"'.' osm_id="'.$aPlace['osm_id'].'"';
	echo ">".htmlspecialchars($aPlace['langaddress'])."</result>";

	echo "<addressparts>";
	foreach($aAddress as $sKey => $sValue)
	{
		$sKey = str_replace(' ','_',$sKey);
		echo "<$sKey>";
		echo htmlspecialchars($sValue);
		echo "</$sKey>";
	}
	echo "</addressparts>";
	
	echo "</reversegeocode>";
