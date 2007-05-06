<?php
session_start();

function write_sidebar($mainpage=false)
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
		<!--
		<form method="post" action="">
		<label for="username">Username</label> <br/>
		<input name="username" id="username" /> <br/>
		<label for="password">Password</label> <br/>
		<input name="password" id="password" type="password" /> <br/>
		<input type='submit' value='go'/>
		</form>
		<p><a href='/freemap/common/signup.php'>Sign up</a></p>
		-->
		<?php
	}
	else
	{
		echo "<p>Logged in as <em>$_SESSION[gatekeeper]</em> ".
		 " <a href='/freemap/common/logout.php?referrer=$_SERVER[PHP_SELF]'>".
		 "Log out</a></p>\n";
	}

	?>
	</div>

	<?php
	if($mainpage)
	{
		?>
		<div id='brief_summary'>
		<p><em>Freemap</em> 
		is a project to provide free and annotatable maps of the countryside 
		using  <a href='http://www.openstreetmap.org'>OpenStreetMap</a> data 
		together with other freely-available data such as NASA SRTM contours,
		<a href='http://www.npemap.org.uk'>New Popular Edition</a> (NPE) maps,
		and <a href='http://www.geograph.org.uk'>Geograph</a> photos.</p>
		</div>
		<?php
	}
	?>

	<div id='links'>

	<?php
	$links = array ("Main Map" => array("/freemap/index.php?mode=mapnik",
					"mapnikLink"),
					"NPE map" => array("/freemap/index.php?mode=npe","npeLink"),
					"osmajax" => array("/freemap/edit.php", "osmajaxLink"),
					"Blog" => "/wordpress/index.php" ,
					"Wiki" => "/wiki/index.php",
					"Login" => "/freemap/common/login.php",
					"Signup" => "/freemap/common/signup.php");
	$first = true;
	foreach($links as $text=>$link)
	{
		if(is_array($link))
		{
			echo "<a href='$link[0]' id='$link[1]'>$text</a><br/>";
		}
		else
		{

			echo "<a href='$link'>$text</a><br/>";
		}
	}
	?>

	</div>

	<div id="message"> </div>

	</div>
	<?php
}

function write_inputbox()
{
?>
<div id='inputbox'>
<h3>Please enter details of the feature</h3> 
<label for='title'>Title (e.g. name)</label>  <br/>
<input name='title' id='title' class='textbox' /> <br/> 
<label for='description'>Description or comments</label>  <br/>
<textarea id='description' class='textbox' ></textarea> <br/> 
<label for='type'>Type</label>  <br/>
<select name='type' id='type' class='textbox'> 
<option value='hazard'>Hazard/path blockage</option>
<option value='view'>Nice view</option>
<option value='meeting'>Meeting point</option>
<option value='other'>Other</option>
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
<?php
}

function write_searchbar()
{
?>
<div id='srch'>
<label for='search'>Search:</label>
<input id="search" /> 
<select id='country'>
<option>uk</option>
<option>fr</option>
<option>de</option>
<option>it</option>
<option>se</option>
<option>no</option>
<option>es</option>
<option>be</option>
<option>nl</option>
</select>
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
$controls = array("navigate","select","draw","polygon");
echo "<div id='menubar'>";
$first=true;
foreach ($controls as $control)
{
	if($first)
		$first=false;
	else
		echo "|";
	echo "<span id=\"mode_$control\" onclick=\"setEditMode('$control')\"".
		 "$control</span>";
}
echo "</div>";
}


function write_osmloginform($post_script="")
{
	?>
	<h1>Please login to OpenStreetMap</h1>
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

		default:
			$en = array ("e"=>$_SESSION['lon'], "n"=>$_SESSION['lat']);
	}
	return $en;
}
?>
