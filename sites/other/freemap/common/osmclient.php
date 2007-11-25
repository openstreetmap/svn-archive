<?php

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

	case "parentways":
		if(isset($input["id"]))
			$url = "http://www.openstreetmap.org/api/0.5/node/$input[id]/ways";
		else
			$valid=false;
		break;

	case "login":
		// Send a request for user details for authentication purposes only
		$url = "http://www.openstreetmap.org/api/0.5/user/details";
		break;

	case "gpxupload":
		$url = "http://www.openstreetmap.org/api/0.5/gpx/create";
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
		elseif($method=="POST" && $call=="gpxupload")
		{
			$filename = "/home/www-data/uploads/fmaptrk".$input['trackid'].
					".gpx";
			$fp=fopen($filename,"w");
			fwrite($fp,$input['data']);
			curl_setopt($ch,CURLOPT_POST,1 );
			curl_setopt ($ch, CURLOPT_HTTPHEADER,array("Expect:"));
			// blank description gives 400 error
			$postdata = array
				("file"=>"@$filename",
				"description"=>$input['description'],
				"tags"=>$input['tags'],
				"public"=>$input['public']);
			curl_setopt($ch,CURLOPT_POSTFIELDS, $postdata);
		}

		$resp=curl_exec($ch);
		$httpCode=curl_getinfo($ch,CURLINFO_HTTP_CODE);
		curl_close($ch);
		if($fp) fclose($fp);
		//if($filename) unlink($filename);
		return array ("content"=>($call=="login" ? "":$resp),"code"=>$httpCode);
	}

	return null;
}
?>
