<?php
session_start();

require_once('../common/defines.php');

if(isset($_SESSION['osmusername']) && isset($_SESSION['osmpassword']))
{
	$lon = $_REQUEST["lon"];
	$lat = $_REQUEST["lat"];
	$name = $_REQUEST["title"];
	$type = $_REQUEST["type"];
	$note = (isset($_REQUEST["description"]) &&
			$_REQUEST["description"]!="") ? $_REQUEST["description"]: null;

	switch($_REQUEST["action"])
	{
		case "add":
			addToOSM("$_SESSION[osmusername]:$_SESSION[osmpassword]",
						$lon,$lat,$name,$type,$note);
			break;
	}
}
else
{
	echo "You haven't provided your OSM login details.";
}

function addToOSM($osmlogin,$lon,$lat,$name,$type,$note)
{
	$classes = array ("pub" => array("amenity","pub"),
					  "peak" => array("natural","peak"),
					  "hamlet" => array("place","hamlet"),
					  "village" => array("place","village"),
					  "town" => array("place","town"),
					  "city" => array("place","city"),
					  "church" => array("amenity","place_of_worship"),
					  "farm" => array("residence","farm"),
					  "viewpoint" => array("tourism","viewpoint") );
					  
	$osm = "<osm version='0.3'><node lat='$lat' lon='$lon'>";
	$osm .= "<tag k='name' v='$name' />";
	if(isset($classes[$type]))
	{
		$osm .= "<tag k='".$classes[$type][0]."' v='".$classes[$type][1].
				"' />";
	}
	if($note)
	{
		$osm .= "<tag k='note' v='$note' />";
	}
	$osm .= "<tag k='created_by' v='Freemap POIEditor' />";
	$osm .= "</node></osm>";
	$url = "http://www.openstreetmap.org/api/0.3/node/0";
	$ch=curl_init ($url);
	curl_setopt($ch,CURLOPT_RETURNTRANSFER,true);
	curl_setopt($ch,CURLOPT_HEADER,false);
	curl_setopt($ch,CURLOPT_USERPWD,$osmlogin);
	curl_setopt($ch,CURLOPT_PUT,1);
	$fp=tmpfile();
	fwrite($fp, $osm);
	fseek($fp, 0);
	curl_setopt($ch,CURLOPT_INFILE,$fp);
	curl_setopt($ch,CURLOPT_INFILESIZE,strlen($osm));
	$resp=curl_exec($ch);
	curl_close($ch);
	fclose($fp);
	//$resp = "Demo only at the moment.";
	echo $resp;
}

?>
