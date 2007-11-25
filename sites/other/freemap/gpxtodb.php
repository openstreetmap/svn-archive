<?php
require_once('common/osmclient.php');
require_once('common/functionsnew.php');

session_start();
?>

<html>
<head>
<style type='text/css'>
textarea { width: 800px; height:300px;}
</style>
<link rel='stylesheet' type='text/css' href='/css/freemap2.css' />
</head>
<body>

<?php
require_once('common/gpxnew.php');
require_once('common/defines.php');

if(isset($_POST['gpx']))
{
$conn=dbconnect();
$userid=isset($_SESSION['gatekeeper'])?  
	get_user_id($_SESSION['gatekeeper'],'freemap_users') : 0;
$gpx = stripslashes($_POST['gpx']);
$gpxdata= parseGPX(explode("\n",$gpx));

$result=mysql_query("select max(trackid) as maxtrackid from trackpoints");
$row=mysql_fetch_array($result);
$trackid=$row["maxtrackid"] + 1;

$first=true;
$query = "insert into trackpoints (trackid,lat,lon) values ";
foreach($gpxdata['trackpoints'] as $tp)
{
	if($first)
		$first=false;
	else
		$query .= ",";
		
	$query .= "($trackid,$tp[lat],$tp[long])";
}

mysql_query($query) or die(mysql_error());
foreach ($gpxdata['waypoints'] as $wp)
{
	mysql_query("INSERT INTO waypoints (trackid,lat,lon,name) VALUES ".
				" ($trackid,$wp[lat],$wp[lon],'$wp[name]') ");
}

mysql_query("INSERT INTO tracks (trackid,timestamp,userid,description) VALUES ".
					" ($trackid,NOW(),$userid,'".
					mysql_real_escape_string($_POST['description'])."')");
mysql_close($conn);

echo "<h1>Upload successful</h1>";
echo "<p>Your track has been uploaded successfully. You can now go to the ";
echo "<a href='/freemap/osmajax.php?trackid=$trackid'>";
echo "edit page</a> to draw new paths on top of your data.</p>";

if($_POST["osmusername"]!="" && $_POST["osmpassword"]!="")
{
	echo "<h2>Result from upload to OpenStreetMap</h2>";
	$result=callOSM ("gpxupload", 
	$_POST["osmusername"], $_POST["osmpassword"], "POST", 
		array("data"=> $gpx,	
		"description"=>$_POST["description"],
			"tags"=>$_POST["tags"],"public"=>$_POST["public"],
			"trackid"=>$trackid) );
	if ($result["code"]==200)
	{
		echo "<p>Your GPX was uploaded to OpenStreetMap successfully.";
		echo "Your OSM track id is: $result[content]</p>";
	}
	else
	{
		echo "<p>OpenStreetMap returned an error code ($result[code]) when ";
		echo "it received your GPX file. ";
		switch($result["code"])
		{
			case 500:
			echo "This is an OSM internal error, please try again later.";
			break;

			case 401:
			echo "Your OSM username and/or password was wrong.";
			break;

			case 400:
			echo "Either your GPX was in the incorrect format, or you ";
			echo "didn't supply a description.";
			break;
		}
		echo "</p>";
	}
}

}
else
{
?>
<h1>Upload a GPX File</h1>
<p>This page allows you to upload a GPX file to the Freemap server. Once your
file is uploaded, you'll be taken to the online editor (Osmajax) which will
allow you to draw on top of your GPX track to add new footpaths and bridleways
to OpenStreetMap. You'll need an OpenStreetMap account for this, please
<a href='http://www.openstreetmap.org'>visit the OSM site</a> if you don't
have one already. Your new data will appear in Freemap from next Thursday
morning.</p>
<p>Your GPX track will be stored on Freemap for one month, so you can return
to editing at a later date if you don't want to edit straight away.</p>
<?php
if(isset($_SESSION["gatekeeper"]))
{
	echo "<p>You're logged in, so you'll be able to view this GPX track, and ";
	echo "any others you upload while logged in, from your Freemap home page.";
	echo "</p>";
}
else
{
	echo "<p><em>You're not logged in, so you won't be able to view this track";
	echo " later. Please log in "; 
	echo "if you want to be able to ";
	echo "view your track at a later date.</em> If you upload your track ";
	echo "whilst logged in, it will be accessible from your Freemap home page.";
	echo "</p><p>";
	echo "<form method='post' action='/freemap/common/login.php?";
	echo "redirect=/freemap/gpxtodb.php'>\n";
	?>
	<label for="username">Username</label> 
	<input name="username" id="username" /> 
	<label for="password">Password</label> 
	<input name="password" id="password" type="password" /> 
	<input type='submit' value='go'/> 
	</form>
	</p>
	<?php
}
?>
<h2>Enter the GPX file</h2>
<p>Copy and paste your GPX into the text area below:</p>
<form method="post" action="">
Description:<br/>
<input name="description"/> <br/>
GPX: <br/>
<textarea name="gpx"></textarea>
<br/>
<h2>Upload to OpenStreetMap</h2>
<p>It's recommended to upload your GPX track to OpenStreetMap
if you have an OSM account (and since you're probably coming here 
to add new data to OpenStreetMap, you probably have!) Please specify your OSM
username and password below:</p>
OSM Username:<br/>
<input name='osmusername'/><br/>
OSM Password:<br/>
<input name='osmpassword' type='password'/><br/>
Can other people see my GPX track? (select 'no' if you don't want other people
to find out where you were at the time of making the track!) <br/>
<input name='public' value='0' type='radio' checked='checked'/>no
<input name='public' value='1' type='radio'/>yes
<br/>
Tags (optional):<br/>
<input name='tags'/><br/>
<br/>
<input type='submit' value='go'/>
</form>
<?php
}
?>
</body></html>
