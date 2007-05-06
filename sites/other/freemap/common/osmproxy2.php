<?php
require_once('osmclient.php');

session_start();

if(isset($_SESSION["osmusername"]) && isset($_SESSION["osmpassword"]))
{
	$call = (isset($_REQUEST['call'])) ? $_REQUEST['call']: 'map';
	$result = callOSM ($call, 
					$_SESSION['osmusername'], $_SESSION['osmpassword'],
					$_REQUEST['method'], $_REQUEST);
	echo $result["content"];
}
else
{
	echo "This feature is only available if you're logged in to OSM.";
}

?>
