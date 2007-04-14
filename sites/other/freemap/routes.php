<?php
require_once('ajaxfunctions.php');
require_once('defines.php');

// AJAX server script to add walk routes 
// Input: list of lat,lon points in format lat1,lon1; lat2,lon2; etc 

session_start();

$conn = mysql_connect("localhost",DB_USERNAME,DB_PASSWORD);
mysql_select_db(DB_DBASE);

$params = array ("points");

foreach ($params as $param)
{
	$cleaned[$param] = (isset($_REQUEST[$param])) ?
		mysql_real_escape_string($_REQUEST[$param]) : '';
}

$uid = get_user_id($_SESSION['gatekeeper']);
route_to_db($cleaned['points'], $uid);
mysql_close($conn);

function route_to_db($points, $userid)
{
	$pts=explode(";",$points);
	$prevX=null;
	mysql_query("insert into walkroutes set userid=$userid");
	$result=mysql_query("select max(id) as a from walkroutes");
	$row=mysql_fetch_array($result);
	$id=$row["a"];


	for($count=1; $count<=count($pts); $count++)
	{
		list($lat,$lon)=explode(",",$pts[$count-1]);
		mysql_query
                    ("insert into walkroutepoints ".
                     "values($id, $count, $lat, $lon)");
	}
	echo "Route added successfully to database.";
}
?>
