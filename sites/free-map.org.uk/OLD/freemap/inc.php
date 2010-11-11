<?php
session_start();

function write_sidebar($homepage=false)
{
?>
	<div id='sidebar'>
	<div class='titlebox'>
	<img src='/freemap/images/freemap_small.png' alt='freemap_small' /><br/>
	</div>
	<p><strong>...interactive OSM maps for the countryside.
	Data CC-by-SA from 
	<a href='http://www.openstreetmap.org'>OpenStreetMap</a> |
	<a href='about.php'> More...</a></strong></p>
	<div>
	<div id='logindiv'>
	<?php
	/*
	if(!isset($_SESSION['gatekeeper']))
	{
		echo "<form method='post' action='login.php?redirect=".
			htmlentities($_SERVER['PHP_SELF'])."'>\n";
		?>
		<label for="username">Username</label> <br/>
		<input name="username" id="username" /> <br/>
		<label for="password">Password</label> <br/>
		<input name="password" id="password" type="password" /> <br/>
		<input type='submit' value='go' id='loginbtn'/>
		</form>
		<p><a href='/freemap/signup.php'>Sign up</a></p>
		<?php
	}
	else
	{
		echo "<em>Logged in as $_SESSION[gatekeeper]</em>\n";
		echo "<a href='logout.php?referrer=".
			htmlentities($_SERVER['PHP_SELF'])."'>Log out</a>\n";
	}
	*/
	echo "</div>";
	if($homepage==true)
	{
		write_searchbar();
		write_milometer();
		?>
		<a id='osmedit' href='http://www.openstreetmap.org/edit.html'>
		Edit in OSM</a>
		<?php
	}
	else
	{
		echo "<a href='/freemap/index.php'>Map</a><br/>\n";
		echo "<a href='/freemap/about.php'>About/Contact</a><br/>\n";
		echo "<a href='/freemap/stats/index.php'>Stats</a><br/>\n";
		echo "<a href='/wordpress'>Blog</a><br/>\n";
		echo "<a href='/freemap/other.php'>Other stuff</a><br/>\n";
	}

	?>
	</div>



	<!--
	<div id='wrdonediv'>
	<input type='button' value='Done with walk route!' id='wrdonebtn'/>
	</div>
	-->


	<div id='loading'>
	<img src='/freemap/images/ajax-loader.gif' alt='Loading...' id='ajaxloader'
	/></div>

	<div id='editpanel'></div>
	<div id='infopanel'></div>
	<div id="message"> </div>


	</div>
	<?php
}

function write_searchbar()
{
?>
<input id="search" /> 
<input type='button' id='searchButton' value='Search'/><br/>
<?php
}

function write_milometer()
{
?>
<div>
<strong>Distance </strong>
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
	</form>
	</p>
	<?php
}

function do_coords($proj,$inp)
{
	switch($proj)
	{
		case "Mercator":
			$en = ll_to_merc ($inp['lat'],$inp['lon']);
			break;
	
		case "OSGB":
			$en = wgs84_ll_to_gr 
			(array("lat"=>$inp['lat'],"long"=>$inp['lon']));
			break;

		case "Google":
			$en=array();
         	$a = log(tan((90+$inp['lat'])*M_PI / 360))/(M_PI / 180);
 			$en['n'] = $a * 20037508.34 / 180;
			$en['e']=$inp['lon'];
 			$en['e'] = $en['e'] * 20037508.34 / 180;
			break;

		default:
			$en = array ("e"=>$inp['lon'], "n"=>$inp['lat']);
	}
	return $en;
}
?>
