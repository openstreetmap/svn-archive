<?php
require_once('inc.php');
require_once('defines.php');

session_start();


$conn = mysql_connect("localhost",DB_USERNAME,DB_PASSWORD);
mysql_select_db(DB_DBASE);

if(isset($_POST['username']) && isset($_POST['password']))
{

	$result = mysql_query
		("select * from freemap_users where username='$_POST[username]' and ".
			"password=MD5('$_POST[password]')");
	if(mysql_num_rows($result))
		$_SESSION["gatekeeper"] = $_POST["username"];
}
?>
<html>
<head>
<title>FREEMAP - The OpenStreetMap renderer for the countryside</title>
<link rel='stylesheet' type='text/css' href='css/freemap2.css' />
<script type='text/javascript'>
var initLon = <?php echo isset($_GET['lon']) ? $_GET['lon'] : -0.72; ?>;
var initLat = <?php echo isset($_GET['lat']) ? $_GET['lat'] : 51.05; ?>;
initLon =    (initLon>=1.565&&initLon<=1.575) ? -initLon: initLon;
</script>
<!--
<script src="http://openlayers.org/dev/lib/OpenLayers.js"></script>
-->
<script src="http://www.openlayers.org/api/2.1-rc3/OpenLayers.js"></script>
<script type='text/javascript' src="main.js" > </script>
<script type='text/javascript' src="freemaplayer.js" > </script>
<script type='text/javascript' src="freemaptile.js" > </script>
<script type='text/javascript' src="freemapfeature.js" > </script>
</head>
<body onload='init()'>
<div id='main'>
<div id='menubar'>
<span><strong>Mode:</strong></span>
<span id='mode0'>Normal </span> |
<span id='mode1'>Annotate</span> |
<span id='mode2'>Delete</span> |
<span id='mode3'>Distance</span> |
<span id='mode4'>Path report </span> |
<span id='mode5'>Walk route </span>
</div>



<!--
<div id="rightbar">
</div>
-->

<div id='inputbox'>
<h3>Please enter details of the feature</h3> 
<label for='title'>Title (e.g. name)</label>  <br/>
<input name='title' id='title' class='textbox' /> <br/> 
<label for='description'>Description or comments</label>  <br/>
<textarea id='description' class='textbox' ></textarea> <br/> 
<label for='type'>Type</label>  <br/>
<select name='type' id='type' class='textbox'> 
<?php

$result = mysql_query("select * from freemap_marker_types");
while($row = mysql_fetch_array($result))
{
	echo "<option value='$row[id]'>$row[name]</option>\n";
}

mysql_close($conn);

?>
</select> 
<br/> 
<label for='link'>Hyperlink, providing more info about the feature</label> <br/>
<input name='link' id='link' /> <br/>
<?php
if(isset($_SESSION['gatekeeper']))
{
	echo "<label for='visibility'>Visibility</label>\n";
	echo "<select name='visibility' id='visibility' class='textbox'>\n";
	echo "<option value='0'>all</option>\n";
	echo "<option value='1'>private(this login only)</option>\n";
	echo "</select>\n";
}
?>
<br/>
<input type='button' id='descButton' value='Go!' onclick='descSend()' /> 
<input type='button' value='Cancel' onclick="removePopup('inputbox')" /> 
</div>

<!--
<div id="walkinputbox">
<h3>Please enter a summary of the walk</h3>
<p>
<textarea id="walksummary"> </textarea>
</p>
<input type='button' id='descButton' value='Go!' onclick='descSendWalk()' /> 
<input type='button' value='Cancel' onclick="removePopup('walkinputbox')" /> 
</div>
-->

<!--<div id="mapcontainer">-->
<div id="map"> </div>

<div id="rightbar">
<input type='button' id='wrgo' class='wrcontrol' value='Upload' 
disabled='true'/>
<input type='button' id='wrclear' value='Clear' class='wrcontrol'
disabled='true'/>
<input type='button' id='gpsmap' value='GPS Map' class='wrcontrol' />
<div id="wrDiv">
</div>
</div>

<!--</div>-->

<div id='srch'>
<label for='search'>Search:</label>
<input id="search" /> 
<input type='button' id='searchButton' value='Go!' />
<span id='milometer'>
<span id='distUnits'>000</span>.<span id='distTenths'>0</span>
</span>
<select id='units'>
<option>miles</option>
<option>km</option>
</select>
<input type='button' value='Reset' id='resetDist' />
</div>

</div>
</div>

<?php write_sidebar(true); ?>
</body>
</html>
