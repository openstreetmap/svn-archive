<?php
require_once('common/freemap_functions.php');
require_once('common/functionsnew.php');
session_start();
?>
<html>
<head>
<link rel='stylesheet' type='text/css' href='/css/freemap2.css'/>
<title><?php echo $_SESSION['gatekeeper']?>'s Home Page</title>
</head>
<body>
<?
if(!isset($_SESSION["gatekeeper"]))
{
	echo "You need to be logged in to view your home page!";
}
else
{
	$conn=dbconnect();
	$userid=get_user_id($_SESSION['gatekeeper'],'freemap_users');
	echo "<h1>{$_SESSION[gatekeeper]}'s home page</h1>";
	echo "<hr/>";
	echo "<h2>Your uploaded tracks</h2>";
	$tracks=get_user_tracks($userid);
	display_user_tracks($tracks);
	echo "<h2>Your walk routes</h2>";
	$walkroutes=get_user_walkroutes($userid);
	show_user_walkroutes($walkroutes);
	mysql_close($conn);
	echo "<hr/>";
	echo "<p><a href='/index.php'>Back to map</a></p>";
}
?>
</body>
</html>
