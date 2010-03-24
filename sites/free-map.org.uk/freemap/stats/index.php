<?php

require_once('../inc.php');
session_start();

?>
<html>
<head>
<title>Freemap - Stats</title>
<link rel='stylesheet' type='text/css' href='/freemap/css/freemap2.css' />
<body> 

<?php write_sidebar(); ?>

<div id='main' class='content'>
<h1>Stats section</h1>
<hr/>
<p>Welcome to the <em>new</em> stats section. The aim of this part of the
Freemap site is to highlight which parts of England and Wales have had their
rights of way well mapped in OSM, 
and which places need their inhabitants to get off their behind 
and map their local footpaths! ;-) Future plans include a "hall of fame" and
corresponding "hall of shame" to highlight the best- and worst- mapped
areas, but for now, a graphical illustration of coverage is available
<a href='statsmap.php'>here</a>.
</div>
</body>
</html>
