<?php

require_once('inc.php');
session_start();

?>
<html>
<head>
<title>Freemap - Other stuff</title>
<link rel='stylesheet' type='text/css' href='/freemap/css/freemap2.css' />
</head>
<body> 

<?php write_sidebar(); ?>

<div id='main' class='content'>
<h1>Other stuff!</h1>
<hr/>
<p>This page lists other stuff available on the Freemap site.</p>
<h2>Online software</h2>
<p>The Freemap server also hosts other, mostly experimental,
online software. Currently available is:</p>
<ul>
<li><a href='/firsted/index.php'>First Edition/OSM Mashup</a> -
Rights of way from OSM overlaid on the out-of-copyright OS First Edition
maps, actually works quite well IMO ;-)</li>
<li><a href='/3d'>Freemap 3D</a> - 3D rendering of OSM data in the browser
using WebGL; runs on nightly builds of Firefox (and possibly of Safari and
Chrome) only.</a></li>
<li><a href='/~nick/webgl/'>WebGL examples</a></li>
</ul>
<h2>Standalone software</h2>
<p>I have written various standalone applications which are available
for download here. They're all available under open-source licences,
generally the GPL or LGPL; details are available from the source package
of each.</p>
<p>
<strong>FreemapMobile</strong>: Java ME based mobile navigation
application aimed at countryside users, allowing you to see a Freemap map
of your current location on your phone, as well as find nearby points of
interest and survey Freemap map
annotations (path blockages, interesting views, path directions) in the
field and send them to the server. As an alternative to Freemap maps, you can
also view standard OSM Mapnik or Osmarender maps, and New Popular Edition or
First Edition out-of-copyright maps. Requires JSR179 (Location API) support
on your phone; only tested on an N95 though I have had a report of successful
use on an N96.
	<ul>
	<li><a href='/downloads/FreemapMobile/FreemapMobile_r3fe.zip'>
	Source code</a></li>
	<li><a href='/downloads/FreemapMobile/FreemapMobile.jar'>JAR</a> and
	<a href='/downloads/FreemapMobile/FreemapMobile.jad'>JAD</a> files
	for installation to your phone</li>
	</ul>
</p>
<p><strong>Footnav</strong>: 3D visualisation application in very early
stages of development. Footnav can load and visualise OSM and NASA SRTM height
data in 3D. Source code package available
<a href='/downloads/footnav/footnav-r11.tar.bz2'>here</a>; requires 
OpenGL and Qt development libraries to be installed on your machine.
Should work on Linux, Mac OS X or Windows (untested on the latter); 
must be compiled from source. See the README file for compilation
instructions.</p>
</div>
</body>
</html>
