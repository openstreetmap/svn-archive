<?php

require_once('../inc.php');
session_start();

?>
<html>
<head>
<title>Freemap - Stats Map</title>
<link rel='stylesheet' type='text/css' href='/freemap/css/freemap2.css' />
<body> 

<?php write_sidebar(); ?>

<div id='main' class='content'>
<h1>Stats map</h1>
<hr/>
<p>This map shows current coverage of rights of way in the UK in
OpenStreetMap. The data is taken from Freemap's copy of the OSM database
so might be slightly out-of-date but is normally up-to-date within a week.
Note that a right of way is defined as:
	<ul>
	<li>any way with a <em>designation</em> tag containing the text
	<em>footpath</em>, <em>bridleway</em> or <em>byway</em>;<li>
	<li>any way with the <em>foot</em> or <em>horse</em> tags equal to
	<em>designated</em>;</li>
	<li>any way with <em>highway</em> equal to <em>bridleway</em> and
	<em>horse</em> not equal to <em>permissive</em>;</li>
	<li>any way with the <em>highway</em> tag containing the text
	<em>byway</em>.</li>
	</ul>
Note that I have excluded <em>foot=yes</em> due to inconsistency in its use,
though actually this makes little difference to the results.</p>
<h2>The Map!</h2>
<p>Congratulations to Hampshire, Cheshire and Hertfordshire mappers it
seems, and a clear sign that Wales, the SW and the east need more work!</p>
<p><img src="coverage.php" alt="OSM ROW Coverage" /></p>
</div>
</body>
</html>
