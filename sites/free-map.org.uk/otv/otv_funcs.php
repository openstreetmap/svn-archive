<?php
session_start();


function newsession()
{
	$u=(isset($_SESSION['gatekeeper']))?$_SESSION['gatekeeper']:0;
	mysql_query("INSERT INTO photosessions (t,user) VALUES (NOW(),$u)");
	$sessid = mysql_insert_id();
	return $sessid;
}

function display_map($w,$h)
{
    echo "<div style='width:${w}px; height:${h}px' id='map'></div>";
}

?>
