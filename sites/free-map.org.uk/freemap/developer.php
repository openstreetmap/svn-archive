<?php

require_once('inc.php');
session_start();

?>
<html>
<head>
<title>Freemap - Developers</title>
<link rel='stylesheet' type='text/css' href='/freemap/css/freemap2.css' />
<body> 

<?php write_sidebar(); ?>

<div id='main' class='content'>
<h1>Developer/Technical Page</h1>
<hr/>
<p>This page describes things of interest to developers who might wish to make
use of Freemap data or contribute to Freemap, and also provides a description
of how Freemap works and its relationship to OpenStreetMap.</p>
<h2>The Freemap API</h2>
<p>Freemap now offers an API allowing developers of other applications to
access and even change Freemap data, such as walk routes or annotations.
Details are available <a href='api.php'>here</a>.</p>
<h2>Freemap source code</h2>
<p>If you fancy actually making modifications to Freemap itself, the source
code is available <a href='/downloads/freemap.tar.bz2'>here</a> under the
LGPL. If you 
want your modifications to go live on the actual Freemap site, please send
them <a href='mailto:nick_whitelegg@yahoo.co.uk'>to me</a>.</p>
<h2>Freemap and OpenStreetMap - technical details</h2>
<h3>Map rendering</h3>
<p>Freemap maps are derived from the weekly OpenStreetMap planet dumps and
are normally up-to-date within a week or two.</p>
</div>
</body>
</html>
