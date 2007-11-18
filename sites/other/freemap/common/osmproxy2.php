<?php
require_once('defines.php');
session_start();

if($_REQUEST['call']=='logout')
{
	unset($_SESSION['osmusername']);
	unset($_SESSION['osmpassword']);
}
else
{
	if(isset($_REQUEST['osmusername']))
		$_SESSION['osmusername'] = $_REQUEST['osmusername'];
	if(isset($_REQUEST['osmpassword']))
		$_SESSION['osmpassword'] = $_REQUEST['osmpassword'];

	if($_REQUEST['call']=='map' || !isset($_REQUEST['call']) ||
		(isset($_SESSION["osmusername"]) && isset($_SESSION["osmpassword"])))
	{
		$call = (isset($_REQUEST['call'])) ? $_REQUEST['call']: 'map';
		$result = callOSM ($call, $_SESSION['osmusername'], 
					$_SESSION['osmpassword'], $_REQUEST['method'], $_REQUEST);
		if($result['code']==200)
			echo $result["content"];
		else
			header("HTTP/1.1 $result[code]");	
	}
	else
	{
		header("HTTP/1.1 401 Unauthorized");	
	}
}

function check_osm_login ($username, $password)
{
	$result = callOSM("login", $username, $password);
	return $result["code"];
}

function callOSM ($call, $username, $password, $method='GET', $input=null)
{

	$valid=true;

	switch($call)
	{
	case "map":
		if(isset($input["bbox"]))
			$url = "http://www.openstreetmap.org/api/0.5/map?bbox=$input[bbox]";
		else
			$valid=false;
		break;

	case "node":
	case "segment":
	case "way":
		if(isset($input["id"]))
		{
			if($input["id"]==0) 
				$input["id"]="create";

			$url = "http://www.openstreetmap.org/api/0.5/$call/$input[id]";
		}
		else
			$valid=false;
		break;

	case "login":
		// Send a request for user details for authentication purposes only
		$url = "http://www.openstreetmap.org/api/0.5/user/details";
		break;
	}

	if($valid)
	{
		$ch=curl_init ($url);
		curl_setopt($ch,CURLOPT_RETURNTRANSFER,true);
		//curl_setopt($ch,CURLOPT_HEADER,false);
		curl_setopt ($ch,CURLOPT_USERPWD,"$username:$password");

		if($method=="PUT")
		{
			curl_setopt ($ch, CURLOPT_HTTPHEADER,array("Expect:"));
			$data = stripslashes($input['data']);
			curl_setopt($ch,CURLOPT_PUT,1);
			$fp=tmpfile();
			fwrite($fp, $data);
			fseek($fp, 0);
			curl_setopt($ch,CURLOPT_INFILE,$fp);
			curl_setopt($ch,CURLOPT_INFILESIZE,strlen($data));
		}
		elseif($method=="DELETE")
		{
			curl_setopt($ch,CURLOPT_CUSTOMREQUEST,"DELETE");
		}

		$resp=curl_exec($ch);
		$httpCode=curl_getinfo($ch,CURLINFO_HTTP_CODE);
		curl_close($ch);
		if($fp) fclose($fp);
		return array ("content"=>($call=="login" ? "":$resp),"code"=>$httpCode);
	}

	return null;
}
?>
