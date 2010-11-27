<?php
session_start();

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
	<a href='http://www.openstreetmap.org'>OpenStreetMap</a> </p>

	<div>

	<?php

	echo "<div id='logindiv'>";

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
?>
