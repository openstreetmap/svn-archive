<?php
session_start();

function write_sidebar()
{
?>
	<div id='sidebar'>
	<div class='titlebox'>
	<img src='/images/freemap_small.png' alt='freemap_small' /><br/>
	</div>
	<div id='login'>
	<?php
	if(!isset($_SESSION['gatekeeper']))
	{
		?>
		<form method="post" action="/freemap/common/login.php">
		<label for="username">Username</label> <br/>
		<input name="username" id="username" /> <br/>
		<label for="password">Password</label> <br/>
		<input name="password" id="password" type="password" /> <br/>
		<input type='submit' value='go'/>
		</form>
		<p><a href='/freemap/common/signup.php'>Sign up</a></p>
		<?php
	}
	else
	{
		echo "<p>Logged in as <em>$_SESSION[gatekeeper]</em><br/>".
		 "<a href='/freemap/home.php'>".
		 "My home page</a>".
		 "<a href='/freemap/gpxtodb.php'>".
		 "Upload GPX</a>".
		 " <a href='/freemap/common/logout.php?referrer=$_SERVER[PHP_SELF]'>".
		 "Log out</a></p>\n";
	}

	?>
	</div>

	<div>
	<input type='button' value='Done with walk route!' id='wrdonebtn'/>
	</div>


	<div id='editpanel'></div>
	<div id='infopanel'></div>
	<div id="message"> </div>

	</div>
	<?php
}

function write_searchbar()
{
?>
<div id='srch'>
<label for='search'>Search:</label>
<input id="search" /> 
<input type='hidden' id='country' value='uk' />
<input type='button' id='searchButton' value='Go!'/>
<?php
}

function write_milometer()
{
?>
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

function write_editcontrols()
{
echo "<div>";
/*
$controls = array("navigate"=>"navigate","draw"=> "add feature",
					"edit"=>"edit feature");
echo "<span class='menubar'>";
$first=true;
foreach ($controls as $control=>$displayed)
{
	if($first)
		$first=false;
	else
		echo " | ";
	echo "<span id=\"mode_$control\" onclick=\"setEditMode('$control')\">".
		 "$displayed</span>";
}
echo "</span>";
*/
echo "<span class='menubar'>";
//echo "<span onclick='changeFeature()'>CHANGE</span>";
echo "</span>  <span class='menubar'>";
echo "<a href='edit.php?basemap=npe' id='base_npe'>NPE</a> | ";
echo "<a href='edit.php?basemap=freemap' id='base_freemap'>Freemap</a> | ";
echo "<a href='edit.php?basemap=osm' id='base_osm'>tiles@home</a> | ";
echo "<a href='edit.php?basemap=landsat' id='base_landsat'>Landsat</a> ";
//echo "<input type='button' onclick='testlayer()' value='testlayer'/>";
echo "</div>";
}


function write_osmloginform($post_script="")
{
	?>
	<h1>OpenStreetMap POI Editor</h1>
	<h2>Please log in to OSM</h2>
	<p>To use this feature, you need to provide your OpenStreetMap
	username and password. If you don't have one, please visit
	<a href='http://www.openstreetmap.org'>OpenStreetMap</a>.</p>
	<p><strong>Important:</strong>
	Please note that Freemap will store your OSM username and password on
	the server until you choose to log out of OSM, and will send your OSM 
	login details to the OSM server every time an edit is made. <em>Please 
	only continue if you're happy with this.</em></p>
	<p>
	<form method="post" action="<?php echo $post_script; ?>">
	<label for="osmusername">OSM Username:</label><br/>
	<input name="osmusername" id="osmusername"/><br/>
	<label for="osmusername">OSM Password:</label><br/>
	<input name="osmpassword" id="osmpassword" type="password"/><br/>
	<input type="submit" value="Go!"/>
	</p>
	</form>
	<?php
}

function do_coords($proj,$inp)
{
	$_SESSION['lon'] = isset($inp['lon']) ? $inp['lon']:
		(isset($_SESSION['lon'])  ? $_SESSION['lon'] : -0.72 );
	$_SESSION['lat'] = isset($inp['lat']) ? $inp['lat']:
		(isset($_SESSION['lat'])  ? $_SESSION['lat'] : 51.05 );

	switch($proj)
	{
		case "Mercator":
			$en = ll_to_merc ($_SESSION['lat'],$_SESSION['lon']);
			break;
	
		case "OSGB":
			$en = wgs84_ll_to_gr 
			(array("lat"=>$_SESSION['lat'],"long"=>$_SESSION['lon']));
			break;

		case "GOOG":
			$en=array();
         	$a = log(tan((90+$_SESSION['lat'])*M_PI / 360))/(M_PI / 180);
 			$en['n'] = $a * 20037508.34 / 180;
			$en['e']=$_SESSION['lon'];
 			$en['e'] = $en['e'] * 20037508.34 / 180;
			break;

		default:
			$en = array ("e"=>$_SESSION['lon'], "n"=>$_SESSION['lat']);
	}
	return $en;
}
?>
