<?php 

require_once('FreemapTemplate.php');
require_once('functions.php');
require_once('classes.php');
require_once('defines.php');

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

$mainpage=new FreemapBaseTemplate
			("FREEMAP - Free mapping for the UK outdoors");

$mainpage->addInternalSelector 
	("#main { height:100%; position: absolute; }");
$mainpage->addInternalSelector("#main { text-align:center }"); # IE CSS bug

session_start();

# Use JavaScript, AJAX and cookies?
/*
if(isset($_GET['js']))
	$_SESSION['js'] = $_GET['js'];
elseif(!isset($_SESSION['js']))
	$_SESSION['js'] = 1;

$js = $_SESSION['js'];
*/
$js = 0;


if($js)
	$mainpage->addJavascript("main.js");


$vars = array ("mapview"=>9,"e"=>487600,"n"=>126900,"view"=>0);

foreach ($vars as $var=>$default)
{
	if($js)
	{
		/*
		if(wholly_numeric($_GET[$var]))
			setcookie($var,$_GET[$var]);
			*/
	}
	else
	{
		if(wholly_numeric($_GET[$var]))
			$_SESSION[$var] = $_GET[$var];
		elseif(!isset($_SESSION[$var]))
			$_SESSION[$var] = $default; 
	}
}
	
if(isset($_POST["lat"]) && isset($_POST["long"]))
{
	$gr = ll_to_gr($_POST);
	$_SESSION['e'] = $gr['e'];
	$_SESSION['n'] = $gr['n'];
}

$mainpage->writeHead(false);
echo "<body";
if($js)
	echo " onload='init()' onunload='closedown()'";
else
{
	$mv = get_mapview($_SESSION['mapview']);
	$map = new Map (array("e"=>$_SESSION['e'],"n"=>$_SESSION['n']),
						MAP_WIDTH, MAP_HEIGHT, 
						$mv["scale"]);
}

echo ">\n";
?>

<div id="freemap">

<?php
//$mainpage->writeNavbar();
?>

<div id="vpcontainer">
		
		<?php showArrowKeys ($map, $mv, $js); ?>
		<img id='pleasewait' src='images/clock.png' alt='Please wait...'/>
		<div id="promptbox">
		<h3>Enter details</h3>
		<label for="featurename">Name</label>
		<input id="featurename"/>
		<label for="featuredesc">Description</label>
		<textarea id="featuredesc"></textarea>
		<input type='button' id="featurego" value="go"/>
		</div>

		<div class='viewport' id='viewport'>

		<?php

		echo '<img id="map" alt="map" '; 
		if(!$js)
		{
			echo "src='drawmap.php?e=".$_SESSION['e']."&amp;n=".$_SESSION['n'].
				  "&amp;mapview=".$_SESSION['mapview'].
				  "&amp;landsat=".$_SESSION['view']."'";
		}
		echo "/>\n";	
		?>

		<img id='xmarksthespot' src='images/xmarksthespot.png' alt='x' />
		</div>
</div>

<div id="mapdiv3">
	
		<div class="panel">
		<?php
		if(!$js)
		{
			echo 
			"<form id='form1' method='post' action='srch.php'>\n"; 
			echo "<input type='hidden' name='scale' value='".
					$mv['scale']."'/>\n";
		}
		?>
		<h1>Search</h1>
		<label for="name">Name</label>
		<input name="name" id="name" class="inputelement"/>
		<label for="gridref">Grid ref</label>
		<input name="gridref" id="gridref" class="inputelement"/>
		<input class="submit" type="<?php echo ($js)?"button":"submit"?>" 
				id="searchsubmit" value="Go!"/>
		<?php 
		if(!$js) echo "</form>\n"; ?>
		</div>


		<?php
		if($js)
		{
		?>

		<div class="panel">

		<h1>Distance</h1>

		<div id="milometer">
		<span id="distUnits">000</span>.<span id="distTenths">0
		</span></div>
		<span id="units">miles</span>

		<div id="distcontrol">
		<span class='toolbarentry' id='reset'>Reset dist</span>
		<span class='toolbarentry' id='distchange'>Use km</span>
		</div>
		
		</div>
		<?php
		}
		showScaleControl ($map, $mv, $js); 
		?>

</div>
<div id="mapdiv4">
		<div class="panel">
		<h1>Go to</h1>
		<form method="post" action="">
		<div>
		<label for="lat">Latitude:</label>
		<input name="lat" id="lat" class="inputelement"/>
		<label for="lat">Longitude:</label>
		<input name="long" id="long" class="inputelement"/>
		<input type="submit" value="Go!"/>
		</div>
		</form>
		</div>
<?php
if($js)
{
	?>
	<div class="panel">
	<h1>Options</h1>
	<label for="view">View</label>
	<select id="view" class="inputelement">
	<option value="0">Normal</option>
	<option value="1">Landsat</option>
	<option value="2">Landsat with polygons</option>
	</select>
	
	<label for="action">Action</label>
	<select id="action" class="inputelement">
	<option value="distance">Distance</option>
	<option value='featurequery'>Feature query</option>
	<?php
	if(isset($_SESSION['gatekeeper']))
	{
		echo "<option value='polygon'>Polygon add</option>\n";
		echo "<option value='feature'>Feature add</option>\n";
		echo "<option value='featuredel'>Feature delete</option>\n";
		echo "<option value='featureupdate'>Feature update</option>\n";
	}
	?>
	</select>

	<?php
	if(isset($_SESSION['gatekeeper']))
	{


		echo "<label for='polygontype'>Polygon type</label>\n";
		echo "<select id='polygontype' class='textbox'>\n";

		echo "</select>\n";
		echo "<label for='featuretype'>Feature type</label>\n";
		echo "<select id='featuretype' class='textbox'>\n";

		echo "</select>\n";
		
	}
	?>

	</div>
	<?php
}
else
{
	?>
	<div class="panel">
	<form method="get" action="">
	<label for="view">View</label>
	<select id="view" name="view" class="inputelement">
	<option value="0">Normal</option>
	<option value="1">Landsat</option>
	<option value="2">Landsat with contours</option>
	</select>
	<input type="submit" value="Go!"/>
	</form>
	</div>
	<?php
}
?>
</div>

<div id="bottomarea"><h3>Freemap-OSM v0.0.3</h3><p>
<p>The result of the combination of 
<a href="http://www.free-map.org.uk">Freemap</a>
code with <a href="http://www.openstreetmap.org">OpenStreetMap</a> data,
and the first (?) OpenStreetMap web client.  All the data you see has been
fetched from the OpenStreetMap database. <strong>NEW!</strong> Now 
with road names,
Landsat data and different levels of detail at different zoom levels.</p>
<?php
if($js)
{
?>
<?php
}
else
{
}
?>


</div>

</body>
</html>
<?php



function showScaleControl($map, $mv, $js)
{
	echo "<div class='panel'>\n";

	# 28/02/05 changed calculation of new bottom left to reflect the
	# preset scale of the adjacent viewing modes; this will not necessarily
	# be the current scale multiplied or divided by 2.
	if(!$js)
	{
		if($mv['id']<11)
		{
			$a=get_mapview($mv['id']+1);
			$magnify_bl = $map->get_new_bottom_left($a);
			echo "<a href='index003.php?js=0&amp;e=".
		  		$magnify_bl['e']."&amp;n=".$magnify_bl['n']."&amp;mapview=".
		  		($mv['id']+1)."'>\n";
			echo '<img class="scaleimg" id="magnify" '.
			'src="images/magnify.png" alt="Increase scale 2x" /></a>'."\n";
		}

		if($mv['id']>5) // only 5-11 available atm 24/07/05
		{
			$b=get_mapview($mv['id']-1);
			$shrink_bl = $map->get_new_bottom_left($b);

			echo '<a href="index003.php?js=0&amp;e='.
			$shrink_bl['e']."&amp;n=".$shrink_bl['n']."&amp;mapview=".
			($mv['id']-1);
			echo '"><img class="scaleimg" id="shrink" src="images/shrink.png"'.
   		 	' alt="Decrease scale 2x" /></a>'.
		 	"\n";
		}
	}
	else
	{
		?>
		<img class="scaleimg" id="magnify" 
			src="images/magnify.png" alt="Increase scale 2x" />
		<img class="scaleimg" id="shrink" src="images/shrink.png"
   		 	 alt="Decrease scale 2x" />
		<?php
	}
	echo "</div>\n";
}

function showArrowKeys($map, $mv, $js)
{


	if(!$js)
	{
		$dim_km_w =  $map->width/$mv['scale']; 
		$dim_km_h =  $map->height/$mv['scale']; 

		echo "<a href='index003.php?js=0&amp;e=".
		 round($_SESSION['e']-$dim_km_w*1000).
		 "&amp;n=".$_SESSION['n'].
 		"'>\n";
	}
		
	echo '<img id="left" src="images/arrow_left.png" alt="Move west"/></a>'.
		 "\n";
	
	if(!$js)
	{
		echo "</a><a href='index003.php?js=0&amp;e=".
	     round($_SESSION['e']+$dim_km_w*1000)."&amp;n=".
		 $_SESSION['n'].
 		"'>\n";
	}

	echo '<img id="right" src="images/arrow_right.png" alt="Move east"/></a>'.
		 "\n";
	
	if(!$js)
	{
		echo "</a><a href='index003.php?js=0&amp;n=".
	     round($_SESSION['n']-$dim_km_h*1000)."&amp;e=".
		 $_SESSION['e'].
 		"'>\n";
	}

	echo '<img id="down" src="images/arrow_down.png" alt="Move south"/></a>'.
		 "\n";
	
	if(!$js)
	{
		echo "</a><a href='index003.php?js=0&amp;n=".
         round($_SESSION['n']+$dim_km_h*1000)."&amp;e=".
		 $_SESSION['e'].
 		"'>\n";
	}

	echo '<img id="up" src="images/arrow_up.png" alt="Move north"/></a>'.
		 "\n";

	if(!$js) echo "</a>\n";
}
?>
