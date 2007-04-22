<?php

require_once('common/inc.php');
require_once('common/defines.php');
require_once('common/latlong.php');
require_once('common/osmclient.php');

session_start();

if(isset($_POST["osmusername"]) && isset($_POST["osmpassword"]))
{
	if(check_osm_login ($_POST["osmusername"],$_POST["osmpassword"]))
	{
		$_SESSION["osmusername"] = $_POST["osmusername"];
		$_SESSION["osmpassword"] = $_POST["osmpassword"];
	}
	else
	{
		echo "Invalid OSM username/password";
	}
}

$modes = array
	("mapnik" => array ("UI" => 
		"css,sidebar,modebar,searchbar,milometer,popups",
						"projection" => "Mercator"),
	 "npe" => array ("UI" => "css,sidebar,searchbar", "projection" => "OSGB"),
	 "POIeditor" => array ("UI" => "css,sidebar,modebar,searchbar,milometer",
	 					"projection" => "OSGB"),
	 "osmajax" => array ("UI" => "searchbar,editcontrols",
	 					"projection" => "OSGB") );

$m = isset($_GET["mode"]) ? $_GET["mode"] : "mapnik";
$mode = (isset($modes[$m])) ? $modes[$m] : $modes["mapnik"];
$defaultN = ($mode["projection"]=="Mercator") ? 6597000:128500;

$_SESSION['lon'] = isset($_GET['lon']) ? $_GET['lon']: 
	(isset($_SESSION['lon'])  ? $_SESSION['lon'] : -0.72 );
$_SESSION['lat'] = isset($_GET['lat']) ? $_GET['lat']: 
	(isset($_SESSION['lat'])  ? $_SESSION['lat'] : 51.05 );

switch($mode["projection"])
{
	case "Mercator":
		$en = ll_to_merc ($_SESSION['lat'],$_SESSION['lon']);
		break;
	
	case "OSGB":
		$en = wgs84_ll_to_gr 
			(array("lat"=>$_SESSION['lat'],"long"=>$_SESSION['lon']));
		break;

	default:
		$en = array ("e"=>$_SESSION['lon'], "n"=>$_SESSION['lat']);
}

$conn = mysql_connect("localhost",DB_USERNAME,DB_PASSWORD);
mysql_select_db(DB_DBASE);

?>
<html>
<head>
<title>FREEMAP - OpenStreetMap maps for the countryside</title>
<?php
if(strpos($mode["UI"],"css")!==false)
	echo "<link rel='stylesheet' type='text/css' href='/css/freemap2.css' />";
?>
<link rel='alternate' type='application/rss+xml' href='/wordpress/?feed=rss2'/>
<script type='text/javascript'>
var easting = <?php echo $en["e"]; ?>;
var northing = <?php echo $en["n"]; ?>;
</script>

<?php


if($m=="osmajax")
{
	// Include vector stuff
	?>
	<script src='http://www.openlayers.org/dev/lib/OpenLayers.js'></script>
	<script src='/freemap/javascript/vector/init.js'></script>
	<script src='/freemap/javascript/vector/OSM.js'></script>
	<script src='/freemap/javascript/vector/OSMFeature.js'></script>
	<script src='/freemap/javascript/vector/OSMItem.js'></script>
	<script src='/freemap/javascript/vector/GeometriedOSMItem.js'></script>
	<script src='/freemap/javascript/vector/OSMNode.js'></script>
	<script src='/freemap/javascript/vector/OSMSegment.js'></script>
	<script src='/freemap/javascript/vector/OSMWay.js'></script>
	<script src='/freemap/javascript/vector/routetypes.js'></script>
	<script src='/freemap/javascript/vector/DrawOSMFeature.js'></script>
	<script src='/freemap/javascript/vector/ajax.js'></script>
	<script type='text/javascript' src="/freemap/javascript/jscoord-1.0.js" > </script>
	<script type='text/javascript' src="/freemap/javascript/converter.js" > </script>
	<style type='text/css'>
	/*
	#top { height: 20% }
	#top1 { width: 50%; top:0%; left:0%;
	 position:absolute}
	#status { background-color: #ffffc0; width: 50%; left: 50%;top:0%;
			 position:absolute}
	*/
	#status { background-color: #ffffc0; }
	</style>
	<?php
}
else
{
echo "<script type='text/javascript' src='/freemap/javascript/$m/init.js'>".
"</script>\n";
?>
<script type='text/javascript' src="/freemap/javascript/converter.js" > </script>
<script type='text/javascript'>
</script>
<script src="http://www.openlayers.org/api/2.2/OpenLayers.js"></script>
<script type='text/javascript' src="/freemap/javascript/GeoRSSClient.js" > </script>
<script type='text/javascript' src="/freemap/javascript/FreemapClient.js" > 
</script>
<script type='text/javascript' src="/freemap/javascript/main.js" > </script>
<script type='text/javascript' src="/freemap/javascript/jscoord-1.0.js" > </script>
<script type='text/javascript' src='/freemap/javascript/BboxMarkersLayer.js'> 
</script>
<script type='text/javascript' src='/freemap/javascript/GeoRSS2i.js' > </script>
<script type='text/javascript' src='/freemap/javascript/GGKMLi.js' > </script>
<script type='text/javascript' src="/freemap/javascript/pngfix.js" > </script>
<?php
}
?>
</head>
<body onload='init()'>
<div id='main'>
<?php
if(isset($_SESSION["osmusername"]))
{
	if ($m=="osmajax")
	{
		echo "<p id='osmloginmsg'><em>Logged into OSM as ".
		 "$_SESSION[osmusername]</em> ".
		"<a href='common/osmlogout.php'>Log out</a></p>";

		if(strpos($mode["UI"],"editcontrols")!==false)
			write_editcontrols();

		echo "<p id='status'> </p>";

	}
	else
	{
		echo "<p id='osmloginmsg'><em>Logged into OSM as ".
		 "$_SESSION[osmusername]</em> ".
		"<a href='common/osmlogout.php'>Log out</a></p>";
	}

}

if(($m == "POIeditor" || $m=="osmajax") && !isset($_SESSION['osmusername']))
{
	write_osmloginform();	
	if($m=="osmajax")
	{
		?>
		<p><strong>This is an experimental feature!</strong> Osmajax is in
		very early development. It's almost certainly got bugs. If you 
		create OSM features using osmajax, I would recommend you check them
		in JOSM afterwards.</p>
		<?php
	}
}
else
{
if(strpos($mode["UI"],"modebar")!==false)
{
	?>
	<div id='menubar'>
	<span><strong>Mode:</strong></span>
	<span id='mode0'>Normal </span> |
	<span id='mode1'>Annotate</span> |
	<span id='mode2'>Delete</span> |
	<span id='mode3'>Distance</span> 
	</div>
	<?php
}
?>



<?php 
if(strpos($mode["UI"],"popups")!==false)
	write_inputbox(); 

?>

<div id="map"> </div>

<?php 
if(strpos($mode["UI"],"searchbar")!==false)
	write_searchbar(); 
if(strpos($mode["UI"],"milometer")!==false)
	write_milometer(); 
}
?>

</div>
</div>
<?php 
if(strpos($mode["UI"],"sidebar")!==false)
	write_sidebar(true); 
?>
</body>
</html>
