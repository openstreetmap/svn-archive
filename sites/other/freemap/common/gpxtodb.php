<?php
session_start();

require_once('gpxnew.php');
require_once('defines.php');

if(isset($_POST['gpx']))
{
$trackpoints = parseGPX(explode("\n",stripslashes($_POST["gpx"])));
$conn=mysql_connect('localhost',DB_USERNAME,DB_PASSWORD);
mysql_select_db(DB_DBASE);

$result=mysql_query("select max(trackid) as maxtrackid from trackpoints");
$row=mysql_fetch_array($result);
$trackid=$row["maxtrackid"] + 1;

$first=true;
$lats=0;
$longs=0;
$query = "insert into trackpoints (trackid,lat,lon) values ";
foreach($trackpoints as $tp)
{
	if($first)
		$first=false;
	else
		$query .= ",";
		
	$query .= "($trackid,$tp[lat],$tp[long])";
	$lats += $tp['lat'];
	$longs += $tp['long'];
}
$avlat = $lats / count($trackpoints);
$avlong = $longs / count($trackpoints);

mysql_query($query);
mysql_close($conn);

$_SESSION["trackid"] = $trackid;
//header("Location: /freemap/index.php?mode=osmajax");
echo "<p>Average lat : $avlat, average long: $avlong, session : $trackid</p>";
echo "<p><a href='/freemap/index.php?mode=osmajax&lat=$avlat&lon=$avlong'>";
echo "Now edit</a></p>";
}
else
{
?>
<html>
<head>
<style type='text/css'>
textarea { width: 800px; height:300px;}
</style>
</head>
<body>
<h1>Enter GPX</h1>
<form method="post" action="">
<textarea name="gpx"></textarea>
<br/>
<input type='submit' value='go'/>
</form>
</body></html>
<?php
}
?>
