<?php
/// \file
/// \brief Entrypoint which links to some status pages. Virtually no php here.
///
/// The only php used here embeds a sample map excerpt in the page.
?>
<html>
<head>
<title>OpenStreetMap tiles@home</title>
<link rel="stylesheet" href="styles.css">
</head>
<body>
<div class="all">
  <h1 class="title"><a href="http://wiki.openstreetmap.org/index.php/Tiles%40home"><img src="Gfx/tah.png" alt="tiles@home" width="600" height="109"></a></h1>
  <p class="title">A project to render free maps of the whole world.</p>
  
  <hr>
  <p>Welcome to the world of Creative Commons mapping.  The main page is at
  <a href="http://www.openstreetmap.org/">openstreetmap.org</a>.  This page
  provides map images, which you can use on your website, on a handheld device
  or mobile phone, or anywhere else that you need maps.</p>

  <p><a href="Browse/">Browse the map</a> tile-by-tile, or use a <a href="http://informationfreeway.org/">slippy map</a>.
  These maps are also available on the <a href="http://www.openstreetmap.org/index.html">OSM</a> page - you just need
  to select the &quot;osmarender&quot; layer using the + to the right of the map.
  </p>
  
  <p><a href="MapOf/">Download a map image</a> of anywhere you like</p>
  
  <p>Status pages:<ul>
	<li><a href="http://munin.openstreetmap.org/openstreetmap/dev.openstreetmap.html">Server status graphs</a></li>
	<li><a href="Credits/">List of people uploading tiles</a></li>
	<li><a href="Stats/">Downloadable list of all tiles</a></li>
	<li><a href="Stats/Data/access.htm">Access logs</a></li>
	<li><a href="Log/Requests/">Status of the requests queue</a></li>
	<li><a href="Log/Requests/Recent/">Recent requests</a></li>
	<li><a href="Log/">Error messages</a></li>
  </ul></p>
  
  <?php
  $Z = 16;    ///< z value of the displayed example maps
  $X = 32684; ///< x value of the displayed example map
  $Y = 21834; ///< y value of the displayed example map
  
  print "<table border=\"0\">";
  for($yi = 0; $yi < 2; $yi++){
    print "<tr>";
    for($xi = 0; $xi < 2; $xi++){
      $Img = sprintf(
      "http://tah.openstreetmap.org/Tiles/tile/%d/%d/%d.png", 
        $Z, 
        $X + $xi, 
        $Y + $yi);
      printf("<td><img src=\"%s\" width=\"256\" height=\"256\" alt=\"map snippet\"></td>\n", $Img);
    }
    print "</tr>";
  }
  print "</table>";
  
  ?>
  
  <p>(You can request an area be updated using the <a href="http://wiki.openstreetmap.org/index.php/Tiles%40home/APIs">API</a>, or
  when <a href="Browse/">viewing</a> the map here)</p>
  
  <p>Loads more information is available on the 
  <a href="http://wiki.openstreetmap.org/index.php/Tiles%40home">tiles@home wiki page</a>,
  including development activity, the people involved, and discussing future directions
  for the project</p>
    
  
</div>
</body>
</html>