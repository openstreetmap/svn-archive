<?php
require_once('defines.php');
require_once('freemap_functions.php');

// AJAX server script to add or retrieve annotations 
// Input: latitude and longitude of a clicked point, and associated 
// annotation data

session_start();

$conn = mysql_connect("localhost",DB_USERNAME,DB_PASSWORD);
mysql_select_db(DB_DBASE);

$params = array ("action","lat","lon","link","type","description",
					"title","private","guid");

foreach ($params as $param)
{
	$cleaned[$param] = (isset($_REQUEST[$param])) ?
		mysql_real_escape_string($_REQUEST[$param]) : '';
}

$id=0;
if(isset($_SESSION['gatekeeper']))
	$id=get_user_id($_SESSION['gatekeeper'],"freemap_users");

switch($cleaned['action'])
{
	case "add":
		$q=("insert into freemap_markers (userid,title,description,type,lat,lon,link,private) values ($id,'$cleaned[title]','$cleaned[description]','$cleaned[type]',$cleaned[lat],$cleaned[lon],'$cleaned[link]','$cleaned[private]')");
		mysql_query($q);
		break;

	case "delete":
		$q = 
			"delete from freemap_markers where id=$cleaned[guid] and ".
			"(userid=$id or private=0)";
		mysql_query($q) or die (mysql_error());
		break;
}

mysql_close($conn);

?>
