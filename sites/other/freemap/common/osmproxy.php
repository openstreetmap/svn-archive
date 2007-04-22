<?php
require_once('../common/defines.php');
require_once('../common/latlong.php');

$bbox = isset($_GET['bbox']) ? explode(",",$_GET['bbox']) :
	(isset($_GET['BBOX']) ? explode(",",$_GET['BBOX']): null);
$bb = $_GET['bbox'];

if($_GET["inp"]=="osgb")
{
	list($w,$s,$e,$n) = explode(",",$bb);
	$a = gr_to_wgs84_ll(array("e"=>$w,"n"=>$s)); 
	$b = gr_to_wgs84_ll(array("e"=>$e,"n"=>$n)); 
	$bb = implode (",",array($a["long"],$a["lat"],$b["long"],$b["lat"]));
}

$url = "http://www.openstreetmap.org/api/0.3/map?bbox=$bb";
$ch=curl_init ($url);
curl_setopt($ch,CURLOPT_RETURNTRANSFER,true);
curl_setopt($ch,CURLOPT_HEADER,false);
curl_setopt($ch,CURLOPT_USERPWD,OSM_LOGIN);
$resp=curl_exec($ch);
echo $resp;
curl_close($ch);

?>
