<?php 

require_once('defines.php');
require_once('functions.php');
require_once('classes.php');
require_once('latlong.php');

/* Main page for FREEMAP 
   =====================


   Modifications 30/06/03
   ----------------------
   a) Validation now done client-side;
   b) $_POST[] finally recognised on the server.


   Modification 01/07/03
   --------------------- 
   User-supplied parameters now a botom left grid reference and a scale
   (pixels per km) 


   Modifications 15/12/03
   ----------------------
   Put constant stuff in functions.php file.
   Added decoration/positioning for map.

*/

################################################################################

session_start();

echo "<html>\n";
echo "<body>\n";


$vars = array ("mapview"=>9,"e"=>487600,"n"=>126900);

foreach ($vars as $var=>$default)
{
		if(wholly_numeric($_GET[$var]))
			$_SESSION[$var] = $_GET[$var];
		elseif(!isset($_SESSION[$var]))
			$_SESSION[$var] = $default; 
}

if(isset($_POST["lat"]) && isset($_POST["long"]))
{
	$gr = ll_to_gr($_POST);
	$_SESSION['e'] = $gr['e'];
	$_SESSION['n'] = $gr['n'];
}

$mv = get_mapview($_SESSION['mapview']);
$map = new Map (array("e"=>$_SESSION['e'],"n"=>$_SESSION['n']),
						MAP_WIDTH, MAP_HEIGHT, $mv["scale"]);

?>
<h1>Freemap-OSM v0.0.1</h1>
<p>The result of the combination of 
<a href="http://www.free-map.org.uk">Freemap</a>
code with <a href="http://www.openstreetmap.org">OpenStreetMap</a> data,
and the first (?) OpenStreetMap web client.  All the data you see has been
fetched from the OpenStreetMap database. The interface is extremely basic at
the moment and the maps don't (yet) show villages, towns, pubs etc. But 
this should appear very soon....</p>
<div id="freemap">
<div id="vpcontainer">
		
		<?php showArrowKeys ($map, $mv); ?>

		<div class='viewport' id='viewport'>

		<?php

		echo '<img id="map" alt="map" '; 
			echo "src='drawmap.php?e=".$_SESSION['e']."&amp;n=".$_SESSION['n'].
				  "&amp;mapview=".$_SESSION['mapview']."'";
		echo "/>\n";	
		showScaleControl ($map, $mv); 
		?>

		</div>
		<form method="post" action="">
		<div>
		<label for="lat">Latitude:</label>
		<input name="lat" id="lat"/>
		<label for="lat">Longitude:</label>
		<input name="long" id="long"/>
		<input type="submit" value="Go!"/>
		</div>
		</form>
</div>

	
</div>

<p>Black lines are roads; red dots are footpaths; red dashes are bridleways;
red lines are byways; grey lines are routes of unknown type. Types of path
are based on personal observation and may be wrong; also some may be 
&quot;permissive&quot; paths not rights of way. The only 100% accurate
sources of rights of way information are the Definitive Maps of the County
Council.</p>
</body>
</html>
<?php



function showScaleControl($map, $mv)
{
	echo "<div class='panel'>\n";

	# 28/02/05 changed calculation of new bottom left to reflect the
	# preset scale of the adjacent viewing modes; this will not necessarily
	# be the current scale multiplied or divided by 2.
		if($mv['id']<11)
		{
			$a=get_mapview($mv['id']+1);
			$magnify_bl = $map->get_new_bottom_left($a);
			echo "<a href='index.php?js=0&amp;e=".
		  		$magnify_bl['e']."&amp;n=".$magnify_bl['n']."&amp;mapview=".
		  		($mv['id']+1)."'>\n";
			echo '<img class="scaleimg" id="magnify" '.
			'src="images/magnify.png" alt="Increase scale 2x" /></a>'."\n";
		}

		if($mv['id']>5) // only 5-11 available atm 24/07/05
		{
			$b=get_mapview($mv['id']-1);
			$shrink_bl = $map->get_new_bottom_left($b);

			echo '<a href="index.php?js=0&amp;e='.
			$shrink_bl['e']."&amp;n=".$shrink_bl['n']."&amp;mapview=".
			($mv['id']-1);
			echo '"><img class="scaleimg" id="shrink" src="images/shrink.png"'.
   		 	' alt="Decrease scale 2x" /></a>'.
		 	"\n";
		}
	echo "</div>\n";
}

function showArrowKeys($map, $mv)
{
	echo "<div class='panel'>\n";

		$dim_km_w =  $map->width/$mv['scale']; 
		$dim_km_h =  $map->height/$mv['scale']; 

		echo "<a href='index.php?js=0&amp;e=".
		 round($_SESSION['e']-$dim_km_w*1000).
		 "&amp;n=".$_SESSION['n'].
 		"'>\n";
		
	echo '<img id="left" src="images/arrow_left.png" alt="Move west"/></a>'.
		 "\n";
	
		echo "</a><a href='index.php?js=0&amp;e=".
	     round($_SESSION['e']+$dim_km_w*1000)."&amp;n=".
		 $_SESSION['n'].
 		"'>\n";

	echo '<img id="right" src="images/arrow_right.png" alt="Move east"/></a>'.
		 "\n";
	
		echo "</a><a href='index.php?js=0&amp;n=".
	     round($_SESSION['n']-$dim_km_h*1000)."&amp;e=".
		 $_SESSION['e'].
 		"'>\n";

	echo '<img id="down" src="images/arrow_down.png" alt="Move south"/></a>'.
		 "\n";
	
		echo "</a><a href='index.php?js=0&amp;n=".
         round($_SESSION['n']+$dim_km_h*1000)."&amp;e=".
		 $_SESSION['e'].
 		"'>\n";

	echo '<img id="up" src="images/arrow_up.png" alt="Move north"/></a>'.
		 "\n";

	echo "</a>\n";
	echo "</div>\n";
}

?>
