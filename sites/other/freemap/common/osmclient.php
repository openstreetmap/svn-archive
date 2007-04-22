<?php

// OSM Proxy
// Input:
//     call - the OSM API call
//     bbox - bounding box (map only)
//     id   - the OSM ID (node, segment and way only)
//     data - the data to send

require_once('defines.php');

function check_osm_login ($username, $password)
{
	$result = callOSM("auth", $username, $password);
	return ($result["code"]==401) ? false: true;
}

function callOSM ($call, $username, $password, $method='GET', $input=null)
{
// bbox id data
//$method = (isset($_REQUEST['method'])) ? $_REQUEST['method'] : 'GET';
$valid=true;

	switch($call)
	{
	case "map":
		if(isset($input["bbox"]))
			$url = "http://www.openstreetmap.org/api/0.3/map?bbox=$input[bbox]";
		else
			$valid=false;
		break;

	case "node":
	case "segment":
	case "way":
		if(isset($input["id"]))
			$url = "http://www.openstreetmap.org/api/0.3/$call/$input[id]";
		else
			$valid=false;
		break;

	case "auth":
		// Send a request for an arbitrary way for authentication purposes only
		$url = "http://www.openstreetmap.org/api/0.3/way/1";
		break;
	}

if($valid)
{
	$out = "Method $method data= " . stripslashes($input['data']).  " result=";


	$ch=curl_init ($url);
	curl_setopt($ch,CURLOPT_RETURNTRANSFER,true);
	//curl_setopt($ch,CURLOPT_HEADER,false);
	curl_setopt ($ch,CURLOPT_USERPWD,"$username:$password");

	if($method=="PUT")
	{
	$data = stripslashes($input['data']);
	$out .=" doing a PUT request with data $data ";
	curl_setopt($ch,CURLOPT_PUT,1);
	$fp=tmpfile();
	fwrite($fp, $data);
	fseek($fp, 0);
	curl_setopt($ch,CURLOPT_INFILE,$fp);
	curl_setopt($ch,CURLOPT_INFILESIZE,strlen($data));
	//echo "PUTting to live OSM server temporarily disabled";
	}
	$resp=curl_exec($ch);
	$httpCode=curl_getinfo($ch,CURLINFO_HTTP_CODE);
	curl_close($ch);
	if($fp) fclose($fp);
	//echo $out." ".$resp;
	return array ("content" => $resp, "code"=>$httpCode);
}

return null;
}
?>
