<?php
require_once('ajaxfunctions.php');
require_once('defines.php');

// AJAX server script to add or retrieve walk routes 
// Input: latitude and longitude of points, and associated data
 
// annotation data

session_start();

$conn = mysql_connect("localhost",DB_USERNAME,DB_PASSWORD);
mysql_select_db(DB_DBASE);

$params = array ("action", "bbox", "walkroute", "id");

$uid=0;
if(isset($_SESSION['gatekeeper']))
	$uid=get_user_id($_SESSION['gatekeeper']);

switch($_REQUEST["action"])
{
	case "add":
		$result=mysql_query("select max(id) from walkroutes");
		$row=mysql_fetch_array($result);
		$id=$row["id"];
		$walkroute = read_walkroute_xml($_REQUEST['walkroute']);

		mysql_query("insert into walkroutes ".
					"(id,length,title,userid) ".
					"values ($id,$walkroute[length],".
					"$walkroute[title],$uid)");
		
		add_points($id,$walkroute['points']);
		add_annotations($id,$walkroute['annotations']);
		break;

	case "getroutes":
		echo get_all_walkroutes(explode(",",$_REQUEST['bbox']));
		break;

	case "getroute":
		$id = (ctype_digit($_REQUEST['id'])) ? $_REQUEST['id'] : 0;
		echo get_walkroute_by_id($id);
		break;

}

mysql_close($conn);

function add_points($id,$points)
{
	$count=1;
	foreach($points as $p)
	{
		mysql_query("insert into walkroutepoints(id,point,lat,lon) ".
					"values ($id,$count++,$p[lat],$p[lon])");
	}
}

function add_annotations($id,$annotations)
{
	$count=1;
	foreach($annotations as $a)
	{
		mysql_query("insert into walkrouteannotations(id,point,lat,lon,text) ".
					"values ($id,$count++,$a[lat],$a[lon],$a[text])");
	}
}

function get_all_walkroutes($bbox)
{
	$xml = "<walkroutes>";

	$result=mysql_query
		("select * from walkroutes as w, walkroutepoints as p ".
		 "where w.id=p.id and p.lat between $bbox[1] and $bbox[3] ".
		 "and p.lon between $bbox[0] and $bbox[2] group by w.id");

	while($row=mysql_fetch_array($result))
	{
		$xml .= "<route>";
		$xml .="<id>$row[id]</id>";
		$xml .="<distance>$row[distance]</distance>";
		$xml .="<title>$row[title]</title>";
		$xml .= "</route>";
	}
	$xml .="</walkroutes>";
	return $xml;
}

function get_walkroute_by_id($id)
{
	$xml = "<walkroute>";
	$prev = null;
	$result2=mysql_query
				("select * from walkroutepoints where id=$row[id] ".
				 "order by point");
	$xml .="<distance>$row[distance]</distance>";
	$xml .="<title>$row[title]</title>";
	while($row2=mysql_fetch_array($result2))
		$xml .= "<point lat=\"$row2[lat]\" lon=\"$row2[lon]\" />";

	$result2=mysql_query
				("select * from walkrouteannotations where id=$row[id] ".
				 "order by annotation");

	while($row2=mysql_fetch_array($result2))
	{
		$xml .= "<annotation lat=\"$row2[lat]\" lon=\"$row2[lon]\">";
		$xml .= "<text>$row2[text]</text>";
		$xml .= "</annotation>";
	}
	$xml .= "</walkroute>";
}

#globals
$route = array();
$inDoc=$inDistance=$inTitle=$inPoint=$inAnnotation=$inText=false;
$curAnnotation=null;
$curPoint=null;
#end globals

function read_walkroute_xml($xml)
{
	global $rules;

	$parser = xml_parser_create();
	xml_set_element_handler($parser,"on_start_element",
				"on_end_element");
	xml_set_character_data_handler($parser,"on_characters");

	if(!xml_parse($parser,$xml))
		return false;

	fclose($fp);
	xml_parser_free($parser);
	return $rules; 
}

#NB the PHP expat library reads in all tags as capitals - even if they're
#lower case!!!
function on_start_element($parser,$element,$attrs)
{
	global $inDoc, $route, $inDistance, $inTitle, $inPoint,
			$curPoint, $inAnnotation, $curAnnotation, $inText;

	if($element=="WALKROUTES")
	{
		$inDoc = true;
		$route["points"] = array();
		$route["annotations"] = array();
	}
	elseif($inDoc)
	{
		if($element=="DISTANCE")
			$inDistance=true;
		elseif($element=="TITLE")
			$inTitle=true;
		elseif($element=="POINT")
		{
			$inPoint=true;
			$curPoint["lat"] = $attrs["LAT"];
			$curPoint["lon"] = $attrs["LON"];
		}
		elseif($element=="ANNOTATION")
		{
			$inAnnotation=true;
			$curAnnotation["lat"] = $attrs["LAT"];
			$curAnnotation["lon"] = $attrs["LON"];
		}
		elseif($element=="TEXT")
			$inText = true;
	}
}

function on_end_element($parser,$element)
{
	global $inDoc, $inDistance, $inTitle, $route, $inPoint,
		$inAnnotation;

	if($element=="WALKROUTES")
		$inDoc = false;
	elseif($element=="DISTANCE")
		$inDistance=false;
	elseif($element=="TITLE")
		$inTitle=false;
	elseif($element=="POINT")
	{
		$route["points"][] = $curPoint;
		$inPoint = false;
	}
	elseif($element=="ANNOTATION")
	{
		$route["annotations"][] = $curAnnotation;
		$inAnnotation = false;
	}
}

function on_characters($parser, $characters)
{
	global $inDistance, $inTitle, $inText, $curAnnotation, $route;

	if($inDistance)
		$route["distance"] = $characters;
	elseif($inTitle)
		$route["title"] = $characters;
	elseif($inText)
		$curAnnotation["text"] = $characters;
}
?>
