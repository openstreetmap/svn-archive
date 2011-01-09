<?php
require_once('../lib/functionsnew.php');

session_start();


function newsession()
{
	$u=(isset($_SESSION['gatekeeper']))?$_SESSION['gatekeeper']:0;
	$t=time();
	pg_query("INSERT INTO photosessions (t,userid) VALUES ($t,$u)");
	$sessid = pg_insert_id('photosessions');
	return $sessid;
}

function display_map($w,$h)
{
    echo "<div style='width:${w}px; height:${h}px' id='map'></div>";
}

?>
