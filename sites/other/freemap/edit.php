<?php

require_once('common/inc.php');
require_once('common/defines.php');
require_once('common/latlong.php');
require_once('common/osmclient.php');

session_start();

die("Temporarily unavailable");

if(isset($_POST["osmusername"]) && isset($_POST["osmpassword"]))
{
	$a = check_osm_login($_POST["osmusername"],$_POST["osmpassword"]);
	if($a==401)
	{
		echo "Invalid OSM username/password";
		exit;
	}
	elseif($a==500)
	{
		echo "There was an internal error in the OSM server.<br/>";
		echo "This is likely to be a temporary problem, try later";
		exit;
	}
	else
	{
		$_SESSION["osmusername"] = $_POST["osmusername"];
		$_SESSION["osmpassword"] = $_POST["osmpassword"];
	}
}

$basemaps = array ("npe" => "OSGB",
					"freemap" => "Mercator",
					"osm" => "GOOG",
					"landsat" => "" );
$basemap=isset($_GET['basemap']) ? $_GET['basemap'] : "npe";
$proj = $basemaps[$basemap];
$en=do_coords($proj, $_GET);
?>
<html>
<head>
<title>OSM Point Of Interest Editor</title>
<link rel='alternate' type='application/rss+xml' href='/wordpress/?feed=rss2'/>
<style type='text/css'>
a{ text-decoration: none; }
a.hover { text-decoration: underline; }
body { font-family: helvetica, arial, sans-serif; }
.menubar { background-color: #000080; color: white; width: 640px } 
.menubar a { color: white; } 
.menubar a.hover { color: white; text-decoration:none }
#sidebar { top:0%; left:0%; width:20%; height:100%; position:fixed }
#main { top:0%; left:20%; width:80%; height:100%; position:absolute }
#map {  width:800px; height:600px; }
#editbox { background-color: #ffffc0; visibility:hidden; }
#search { width: 50% }
#inpLat, #inpLon { width: 25% }
h2 { font-size: 120% }
</style>
<script type='text/javascript'>
var easting = <?php echo $en["e"]; ?>;
var northing = <?php echo $en["n"]; ?>;
var basemap = "<?php echo $basemap; ?>";
</script>

<script src='http://www.openlayers.org/dev/lib/OpenLayers.js'></script>
<script src='/freemap/javascript/vector/init.js'></script>
<script src='/freemap/javascript/vector/OSMMarkers.js'></script>
<script src='/freemap/javascript/vector/OSMMarkerFeature.js'></script>
<script src='/freemap/javascript/vector/OSMItem.js'></script>
<script src='/freemap/javascript/vector/GeometriedOSMItem.js'></script>
<script src='/freemap/javascript/vector/OSMNode.js'></script>
<script src='/freemap/javascript/vector/OSMSegment.js'></script>
<script src='/freemap/javascript/vector/OSMWay.js'></script>
<script src='/freemap/javascript/vector/routetypes.js'></script>
<script src='/freemap/javascript/vector/DrawOSMFeature.js'></script>
<script src='/freemap/javascript/vector/ajax.js'></script>
<script type='text/javascript' src="/freemap/javascript/jscoord-1.0.js" > 
</script>
<script type='text/javascript' src="/freemap/javascript/converter.js" > 
</script>
<style type='text/css'>
#status { color: #000080; }
#position { font-weight: bold; font-style: italic; font-size: 80% }
</style>
</head>
<body onload='init()'>


<?php

if(isset($_SESSION["osmusername"]))
{
	echo "<div id='sidebar'>";
	echo "<p id='osmloginmsg'><em>Logged into OSM as ".
 		"$_SESSION[osmusername]</em></p>".
		"<p><a href='common/osmlogout.php'>Log out</a></p>";
	?>

	<div id='searchDiv'>
	<input name='search' id='search'/>
	<select name='country' id='country'>
	<option selected='selected'>uk</option>
	<option>fr</option>
	<option>de</option>
	<option>es</option>
	<option>it</option>
	<option>be</option>
	<option>nl</option>
	<option>no</option>
	<option>se</option>
	</select>
	<input type='button' id='searchButton' value='Search'/>
	</div>

	<p>
	<label for='inpLat'>Lat:</label>
	<input id='inpLat'/>
	<label for='inpLon'>Lon:</label>
	<input id='inpLon'/>
	<input type='button' value='Go!' id='goButton'/>
	</p>

	<p id="position"></p>
	<p id="status"></p>

	<!--
	<div id="editbox">
	<h3>Please enter details</h3>";
	<label for='fname'>Name</label><br/>
	<input id='fname' class='textbox' value=""/><br/>
	<label for='ftype'>Type</label><br/>
	<select id='ftype'> </select>
	<br/>
	<h4>Full tags:</h4>
	<select id='tagkey'></select>
	<input id='tagvalue'/>
	<input type='button' id='ubtn' value='Update!'/>
	<input type='button' id='delt' value='Delete'/>
	<br/>
	<input type='button' id='addt' value='Add tag'/></p>
	<input type='button' id='fbutton1' value='Go!'/>
	<input type='button' value='Cancel' id='fbutton2'/>
	</div>
	-->

	</div> <!--sidebar -->

	<div id='main'>
	<?php write_editcontrols(); ?>
	<div id="map"> </div>
	</div>
	<?php
}


else
{
	write_osmloginform();	
	?>
	<!--
	<p><strong>This is an experimental feature!</strong> Osmajax is in
	very early development. It's almost certainly got bugs. If you 
	create OSM features using osmajax, I would recommend you check them
	in JOSM afterwards.</p>
	-->
	<h2>How to add a point of interest</h2>
	<ol>
	<li>Click on the map at the desired location and wait for a marker to
	appear. This means the point of interest has been added to OSM. Now you 
	need to give it tags.</li>
	<li>Click on the marker.</li>
	<li>Set its type and add tags using the dialog box.</li>
	</ol>
	<?php
}
?>

</body>
</html>
