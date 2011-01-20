<?php

require_once('/home/www-data/private/defines.php');

session_start();

$conn=pg_connect("dbname=gis user=gis");

$lat = (isset($_GET['lat'])) ? $_GET['lat']:
    ((isset($_COOKIE['lat'])) ? $_COOKIE['lat'] : 51.05); 
    
$lon = (isset($_GET['lon'])) ? $_GET['lon']:
    ((isset($_COOKIE['lon'])) ? $_COOKIE['lon'] : -0.72); 

$zoom = (isset($_GET['zoom'])) ? $_GET['zoom']:
    ((isset($_COOKIE['zoom'])) ? $_COOKIE['zoom'] : 14); 
    

$modes = array (
                    array ("Normal", "MODE_NORMAL",true),
                    array ("Distance", "MODE_DISTANCE",true),
                    array ("Route", "MODE_ROUTE",true),
					array ("Annotate", "MODE_ANNOTATE",
							isset($_SESSION['gatekeeper']) ? true:false)
                );
?>

<html>
<head>
<title>FREEMAP - OpenStreetMap maps for the countryside </title>
<link rel='stylesheet' type='text/css' href='/freemap/css/freemap2.css' />

<script type='text/javascript'>

var freemap;
var lat=<?php echo $lat; ?>;
var lon=<?php echo $lon; ?>;
var zoom=<?php echo $zoom;?>;
var loggedin=<?php echo isset($_SESSION['gatekeeper']) ? "true":"false";?>;

<?php
for($i=0; $i<count($modes); $i++)
{
    echo "var {$modes[$i][1]} = $i;\n";
}
?>

</script>

<script src="http://www.openlayers.org/api/OpenLayers.js">
</script>

<script src="http://www.openstreetmap.org/openlayers/OpenStreetMap.js"> 
</script>

<script src="../javascript/prototype.js"></script>

<script src="js/main.js"> </script>
<script src="js/FmapGeoRSS.js"> </script>

</head>

<body onload='init()'>

<?php write_sidebar(true); ?>

<div id='main'>


<div id="mapcontainer">
<div id='menubar'>
<span><strong>Mode:</strong></span>
<?php
for($i=0; $i<count($modes); $i++)
{
    if($modes[$i][2]===true)
    {
        if($i)
            echo " | ";
        echo "<span id='mode$i' onclick='freemap.setMode($i)'>".
			"{$modes[$i][0]}</span>";
    }
}
?>
</div>
<div id="map"> </div>
<canvas id="canvas" style='visibility:hidden' width='800' height='600'>
</canvas>
</div>
<input type='button' value='Map' style='visibility:hidden' id='backToMap' />
</div>

<?php

pg_close($conn);

?>

</body>
</html>

<?php
function write_sidebar($homepage=false)
{
?>
	<div id='sidebar'>

	<div class='titlebox'>
	<img src='/freemap/images/freemap_small.png' alt='freemap_small' /><br/>
	</div>

	<p>The new Freemap, now with tiles hosted by
	<a href='http://www.sucs.org'>Swansea University Computer Society</a>.
	Data CC-by-SA from 
	<a href='http://www.openstreetmap.org'>OpenStreetMap</a>.
	<a href='about.html'>More info...</a></p>

	<?php
	write_login();
	?>

	<div>
	<?php
	write_searchbar();
	write_milometer();
	?>
	<a id='osmedit' href='http://www.openstreetmap.org/edit.html'>
	Edit in OSM</a>
	</div>



	<div id='loading'>
	<img src='/freemap/images/ajax-loader.gif' 
	alt='Loading...' id='ajaxloader' />
	</div>

	<div id='editpanel'></div>
	<div id='infopanel'></div>
	<div id="status"> </div>


	</div>
	<?php
}

function write_searchbar()
{
?>
<input id="q" /> 
<input type='button' id='searchBtn' value='Search'/><br/>
<?php
}

function write_milometer()
{
?>
<div id='distdiv'>
<span id='milometer'>
<span id='distUnits'>000</span>.<span id='distTenths'>0</span>
</span>
<select id='units'>
<option>miles</option>
<option>km</option>
</select>
<input type='button' value='Reset' id='resetDist' />

</div>
<?php
}

function write_login()
{
	echo "<div id='logindiv'>";

	if(!isset($_SESSION['gatekeeper']))
	{
		echo "<form method='post' ".
		"action='/common/user.php?action=login&redirect=".
			htmlentities($_SERVER['PHP_SELF'])."'>\n";
		?>
		<label for="username">Username</label> <br/>
		<input name="username" id="username" /> <br/>
		<label for="password">Password</label> <br/>
		<input name="password" id="password" type="password" /> <br/>
		<input type='submit' value='go' id='loginbtn'/>
		</form>
		<?php
		echo "<a href='/common/user.php?action=signup'>Sign up</a>";
	}
	else
	{
		$result=pg_query("SELECT * FROM users WHERE id=$_SESSION[gatekeeper]");
		$row=pg_fetch_array($result,null,PGSQL_ASSOC);
		echo "<em>Logged in as $row[username]</em>\n";
		echo "<a href='/common/user.php?action=logout&redirect=".
			htmlentities($_SERVER['PHP_SELF'])."'>Log out</a> |".
			" <a href='/common/user.php?action=routes'>Your walk routes</a>\n";
	}
	echo "</div>";
}
?>
