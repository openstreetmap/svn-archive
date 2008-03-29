<?php

/// Given the XML capabilities document of the Naga City WFS server, downloads all the layers
/// TODO: generalize the script??



$xml = simplexml_load_file('http://gis.naga.gov.ph/cgi-bin/mapserv?MAP=/home/senenebio/public_html/nagacity_data.map&SERVICE=WFS&VERSION=1.1.1&REQUEST=GetCapabilities');



foreach($xml->FeatureTypeList->FeatureType as $layer)
{

	echo "-----------\n
	Downloading data from layer:\n";

	print_r($layer);
	
	passthru("wget 'http://gis.naga.gov.ph/cgi-bin/mapserv?MAP=/home/senenebio/public_html/nagacity_data.map&SERVICE=WMS&VERSION=1.1.1&SERVICE=WFS&VERSION=1.0.0&REQUEST=GetFeature&TYPENAME={$layer->Name}' -O {$layer->Name}.gml -c");
	
}








