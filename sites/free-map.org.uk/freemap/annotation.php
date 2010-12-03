<?php
require_once('../lib/functionsnew.php');
session_start();

if(!isset($_SESSION['gatekeeper']))
{
	header("HTTP/1.1 401 Unauthorized");
	echo "unauthorized";
	exit;
}

$conn=pg_connect("dbname=gis user=gis");
$cleaned = clean_input($_REQUEST);

pg_query("UPDATE annotations set annotationId=annotationId+1 WHERE ".
		"wayid=$cleaned[wayid] and annotationId>=$cleaned[annotationId]");

$q= "INSERT INTO annotations(wayid,annotationId,seg,text,x,y,dir) VALUES ($cleaned[wayid],$cleaned[annotationId],$cleaned[seg],'$cleaned[text]',$cleaned[x],$cleaned[y],$cleaned[dir])";
pg_query($q);
pg_close($conn);
echo $q;
?>

