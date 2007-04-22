<?php
require_once('osmclient.php');

session_start();

if(isset($_SESSION["osmusername"]) && isset($_SESSION["osmpassword"]))
{
	$result = callOSM ($_REQUEST['call'], 
					$_SESSION['osmusername'], $_SESSION['osmpassword'],
					$_REQUEST['method'], $_REQUEST);
	echo $result["content"];
}
else
{
	echo "This feature is only available if you're logged in to OSM.";
}

?>
