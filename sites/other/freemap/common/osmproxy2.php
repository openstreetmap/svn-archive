<?php
require_once('defines.php');
require_once('osmclient.php');
session_start();

if($_REQUEST['call']=='logout')
{
	unset($_SESSION['osmusername']);
	unset($_SESSION['osmpassword']);
	if(isset($_REQUEST['redirect']))
	{
		header("Location: $_REQUEST[redirect]");
	}
}
else
{
	if(isset($_REQUEST['osmusername']))
		$_SESSION['osmusername'] = $_REQUEST['osmusername'];
	if(isset($_REQUEST['osmpassword']))
		$_SESSION['osmpassword'] = $_REQUEST['osmpassword'];

	if($_REQUEST['call']=='map' || $_REQUEST['call']=='parentways' ||
		!isset($_REQUEST['call']) ||
		(isset($_SESSION["osmusername"]) && isset($_SESSION["osmpassword"])))
	{

		if($_REQUEST['method']=='GET' || !isset($_REQUEST['method']))
			header("Content-type: text/xml");

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

?>
