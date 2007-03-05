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
  
  <p><a href="http://www.openstreetmap.org/munin/openstreetmap/dev.openstreetmap.html">Performance graphs</a> are available, to say how the server is coping with all this data.
  <a href="Log/">recent uploads and messages</a> can be viewed, as can 
  the <a href="Log/Requests/">areas people are requesting</a> to be rendered.</p>
  
  <?php
  $Z = 16;
  $X = 32684;
  $Y = 21834;
  
  print "<table border=\"0\">";
  for($yi = 0; $yi < 2; $yi++){
    print "<tr>";
    for($xi = 0; $xi < 2; $xi++){
      $Img = sprintf(
      "http://dev.openstreetmap.org/~ojw/Tiles/tile.php/%d/%d/%d.png", 
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