<?php
session_start();

function write_sidebar($mainpage=false)
{
?>
	<div id='sidebar'>
	<img src='images/freemap_small.png' alt='freemap_small' /><br/>
	<div id='login'>
	<?php
	if(!isset($_SESSION['gatekeeper']))
	{
		?>
		<form method="post" action="">
		<label for="username">Username</label> <br/>
		<input name="username" id="username" /> <br/>
		<label for="password">Password</label> <br/>
		<input name="password" id="password" type="password" /> <br/>
		<input type='submit' value='go'/>
		</form>
		<p><a href='signup.php'>Sign up</a></p>
		<?php
	}
	else
	{
		echo "<p>Logged in as <em>$_SESSION[gatekeeper]</em> ".
		 " <a href='logout.php?referrer=$_SERVER[PHP_SELF]'>Log out</a></p>\n";
	}

	?>
	</div>

	<?php
	if($mainpage)
	{
		?>
		<div id='brief_summary'>
		<p><em>Freemap</em> 
		aims to provide maps of the countryside based on 
		<a href='http://www.openstreetmap.org'>OpenStreetMap</a> data, 
		updated as often as planet.osm comes out. 
		It also allows users to annotate the
		maps with interesting features such as path blockages, photos, pub 
		reviews 
		and the like.</p>
		<p>Freemap makes use of 
		<a href="http://www.openlayers.org">OpenLayers</a>.</p>
		</div>
		<?php
	}
	?>

	<div id='links'>

	<?php
	$links = array ("Map" => "index.php",
			  "About Freemap" => "about.php",
			  "GPS maps" => "gpsmap.php");
	foreach($links as $text=>$link)
	{
		if(basename($_SERVER['PHP_SELF']) != $link)
		{
			echo "<a href='$link'>$text</a><br/>\n";
		}
	}
	?>

	</div>

	<div id="message"> </div>

	</div>
	<?php
}
?>
