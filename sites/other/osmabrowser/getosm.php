<?php
require_once('defines.php');
//header("Content-type: text/xml");

$bbox = (isset($_GET['bbox'])) ? $_GET['bbox'] : '-0.75,51.02,-0.7,51.07';

echo grabosm ($bbox);

function grabosm($bbox)
{
	$url = "http://www.openstreetmap.org/api/0.3/map?bbox=$bbox";
	$ch=curl_init ($url);
	curl_setopt($ch,CURLOPT_RETURNTRANSFER,true);
	curl_setopt($ch,CURLOPT_HEADER,false);
	curl_setopt($ch,CURLOPT_USERPWD,OSM_LOGIN);
	$resp=curl_exec($ch);
	curl_close($ch);
	return $resp;
}

?>
