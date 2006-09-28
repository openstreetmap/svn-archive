<?php
require_once('inc.php');

session_start();


?>
<html>
<head>
<link rel='stylesheet' type='text/css' href='css/freemap2.css' />
</head>
<body> 
<?php write_sidebar(); ?>
<div id='main'>
<h1>About Freemap</h1>
<hr/>
<h2>For users...</h2>
<h3>Free countryside maps, derived from OpenStreetMap</h3>
<p>Freemap's aim is to provide a wide-ranging resource for users of the 
countryside, with the key feature (as the name might suggest) being free 
maps. In contrast to many other mapping resources on the web, Freemap maps are
truly free, being not only free of cost but also free of the type of licencing 
restrictions which prevent you from redistributing and modifying the maps.
In other words, you can use original or modified Freemap maps on your own
website, or print and photocopy them, without fear of legal reprisals.</p>
<p>After a hiatus of about a year, Freemap is back in development as a user
of <a href='http://www.openstreetmap.org'>
OpenStreetMap</a> data.  As you may well know already, OpenStreetMap (OSM) is 
the leading project to collect and store free geodata. 
Freemap grabs mapping data from the OSM database every 
week, and focuses on those aspects of OSM most of interest to walkers and
other users of the countryside, such as footpaths, bridleways and other rights 
of way, as well as points of interest such as pubs and viewpoints. The OSM
data is overlayed on freely-available NASA SRTM contour data.</p>
<h3>Interactive, living maps</h3>
<p>It doesn't stop there though.  Freemap maps will not simply be static,
non-interactive maps. Freemap will allow users to annotate the 
maps with information of interest to other users such as interesting 
viewpoints, pub reviews, directions on difficult-to-follow parts of paths,
and even photos. Other
users will then be able to view that information by clicking on a
&quot;marker&quot; icon on the map. Furthermore users will be able to 
provide a summary of a whole path, so that, on the one hand if a path is
particularly attractive or has good views, or, on the other, if it is
boggy, overgrown by nettles or giant hogweeds or obstructed by barbed-wire
fences or killer cows, or even that the view is spoilt by a landfill site, a 
user will be able to report this information so that other
users can view it when they click on the path. Users will also be able
to report path closures such as those that occasionally happen in the New
Forest due to forestry operations, minimising the possibility of someone 
having to go round in a big loop and perhaps miss their train....</p>
<h3>Public and private data</h3>
<p>The Freemap login system allows users to provide either public
annotations (accessible to all) or private annotations (accessible only
to people who know your login). For instance, say you wanted to organise a walk
for a small group of people. You could annotate the map privately with text 
specific to your particular event; in this way, the publicly-available maps
would not be cluttered up with your specific details.</p> 
<h3>Historical maps</h3>
<p>Whilst the default Freemap maps are created by overlaying OSM data
on NASA SRTM data, there will be the option to overlay OSM footpath data on
out-of-copyright (1940s) Ordnance Survey maps, which are still more or less
completely usable in the countryside... aside from the lack of footpaths.
This will be done in conjunction with another project, more of which later. 
</p>
<h3>Other plans</h3>
<p>Freemap will aim to become a wide-ranging facility for walkers,
cyclists and other users of the countryside who want to find out more about
where they are going before they set out.  Future plans include the ability
to share favourite walks with others and overlay the route on the background 
OSM-derived map as well as add a textual description. </p>
</div>
</body>
</html>
