<?php 
require_once('inc.php');
require_once('functions.php');

session_start();


//session_start();

require_once('gpsmapgen.php');

$modes = array
		(24 => "500ft",
		 23 => "800ft-0.2 miles", 
		 22 => "0.3 miles", 
		 21 => "0.5 miles", 
		 20 => "0.8-1.2 miles",
		 19 => "2-3 miles", 
		 18 => "5 miles", 
		 17 => "8-12 miles",
		 16 => "20-30 miles",
		 15 => "50 miles",
		 14 => "80-120 miles",
		 13 => "200-300 miles",
		 12 => "500 miles",
		 0 => "Do not show");


if(isset($_REQUEST["bbox"]))
{
	list($w,$s,$e,$n) = explode(",",$_REQUEST['bbox']);
}
elseif(isset($_REQUEST['east']) && isset($_REQUEST['west']) &&
	isset($_REQUEST['south']) &&  isset($_REQUEST['north']))
{
	$w = $_REQUEST['west'];
	$s = $_REQUEST['south'];
	$e = $_REQUEST['east'];
	$n = $_REQUEST['north'];
}

if($w && $s && $e && $n)
{
	if(!wholly_numeric($w) || !wholly_numeric($s) || !wholly_numeric($e) 
	|| !wholly_numeric($n) || $w>$e || $s>$n)
	{
		echo "<html><body><p>Incorrect input data - maybe you have mixed up " .
			 "west and east, or south and north? ";
		echo "<a href='gpsmap.php'>Have another go</a></p>".
			 "</body></html>";
	}
	else
	{
	header("Content-type: text/plain");
	/*
	$arr = array();
	foreach ($_POST as $k=>$v)
	{
		if($k!="bbox" && $k!="PHPSESSID")
			$arr[$k] = $v;
	}
	*/

	$arr = array
			("path" => 23, 
			"minor_road"=>20,
			"secondary_road" => 19, 
			"primary_road" => 16,
			"motorway" => 13,
			"railway" => 13,
			"city" => 13,
			"town" => 19,
			"village" => 20,
			"pub" => 22,
			"restaurant" => 22,
			"church" => 22,
			"summit" => 22,
			"car_park" => 22,
			"mast" => 22 );

	arsort($arr);
	$zooms=get_zooms($arr);
	$map = new GPSMapGen($w,$s,$e,$n,$zooms, $arr);
	}
}
else
{
	
	
?>
<html>
<head>
<title>GPS Map Creation from OSM Data</title>
<link rel='stylesheet' type='text/css' href='css/freemap2.css' />
</head>
<body> 
<?php write_sidebar(); ?>
<div id='main'>
	<h1>Create GPS Maps from OSM Data!</h1>
	<hr/>
	<p>This part of the Freemap site allows you to, in conjunction with
	Stanislaw Kozicki's <em>cGPSmapper</em> software, create detailed 
	OpenStreetMap-derived maps for your Garmin GPS, showing not only roads but 
	footpaths, landscape features, pubs, villages, etc. To create and upload a
	map to your GPS, do the following:
		<ol>
		<li>Select the latitude and longitude bounding box, 
		on the form below. Alternatively, just click on the &quot;GPS Map&quot;
		button on the main map page, and a GPS map of the currently-displayed
		area will be generated automatically.</li>
		<li>Copy and paste the generated &quot;Polish format&quot; file to
		a text editor like Notepad, KWrite or similar, and save it with an
		.mp extension. (For some reason, if you try and save it as a text file
		in Firefox, it saves the HTML of this page instead)
		</li>
		<li>If you don't have them already, download the
		<em>cGPSmapper</em> and <em>sendmap</em> utilities from the
		<a href='http://www.cgpsmapper.com'>cGPSMapper website</a>.</li>
		<li>Convert the Polish format (.mp) file to the binary .img format,
		e.g.
		<pre>cgpsmapper file.mp</pre>
		</li>
		<li>The .img format is the format recognised by your Garmin GPS
		device. Send this to the device with sendmap, e.g.:
		<pre>sendmap COM1 file.img</pre>
		</li>
		</ol>
	</p>	
	<p>Much more information on creating your own GPS maps is available at
	the <a href="http://www.cgpsmapper.com">cGPSmapper</a> website.</p>
	<p><em>Warning: strange results may occur if the underlying OSM data
	quality is poor. If you are familiar with the OSM data model, this is
	likely to occur if there are adjacent segments unjoined by a common node.
	</em></p>

		<form method='post' action=''>
		<h3>Enter bounding box:</h3>
		<label for="west">Enter westernmost longitude</label>
		<input name="west" id="west"/> <br/>
		<label for="south">Enter southernnmost latitude</label>
		<input name="south" id="south"/> <br/>
		<label for="east">Enter easternmost longitude</label>
		<input name="east" id="east"/> <br/>
		<label for="north">Enter northernnmost latitude</label>
		<input name="north" id="north"/> <br/>
	<?php
		/*
		if(isset($_GET['bbox']))
			echo "<input name='bbox' type='hidden' value='$_GET[bbox]' />";
		else
		{
			echo "<p>Bounding box:"; 
			echo "<input name='bbox' />";
		}
	echo "<h3>Select the least detailed GPS resolution to show the following...
	</h3>\n";
		echo "<div id='resselect'>\n";
		mode_select_box("path","Paths",23,$modes);
		mode_select_box("minor_road","Minor roads",20,$modes);
		mode_select_box("secondary_road","B roads",19,$modes);
		mode_select_box("primary_road","A roads",16,$modes);
		mode_select_box("motorway","Motorways",13,$modes);
		mode_select_box("city","Cities",13,$modes);
		mode_select_box("town","Towns",19,$modes);
		mode_select_box("village","Villages",20,$modes);
		mode_select_box("campsite","Camp sites",22,$modes);
		mode_select_box("pub","Pubs",22,$modes);
		mode_select_box("church","Churches",22,$modes);
		mode_select_box("summit","Hill summits",22,$modes);
		mode_select_box("car_park","Car parks",22,$modes);
		mode_select_box("mast","Masts",22,$modes);
		echo "</div>\n";
		*/
		?>
		<input type="submit" value="Go!" />
		</form>
		<h3>Technical notes</h3>
		<ul>
		<li>These maps, like Freemap itself, are based on the planet.osm
		data dump.
		</li>
		<li>Only nodes and ways will be sent to your GPS device; segments 
		which are not part of ways are ignored. If you don't like this,
		get going on creating ways out of your segments! :-)</li>
		<li>The 
		<a href='freemap.xml'>XML look-and-feel configuration file</a> for 
		Freemap describes the relationship between the feature types here and 
		OSM tags.</li>
		</ul>
		</div>
		</body></html>
		<?php
}

function get_zooms($hwzooms)
{
	$last = -1;
	$count = 0;

	foreach($hwzooms as $field=>$hwzoom)
	{
		if($hwzoom != $last && $hwzoom != 0)
		{
			$zooms[$hwzoom] = $count++;
		}

		$last = $hwzoom;
	}
	return $zooms;
}


function mode_select_box($fieldname,$label,$default,$modes)
{
	
	//echo "<label for='$fieldname'>$label</label> ";
	echo "<select name='$fieldname' id='$fieldname'>\n";
	foreach ($modes as $key => $value)
	{
		echo "<option value='$key'";
		if($key==$default)
			echo " selected='selected'";
		echo ">$value</option>\n";
	}
	echo "</select>\n";
	echo "<br/>\n";
}

?>
