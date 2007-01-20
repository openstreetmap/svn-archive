<?php 
  include("lib/gui.inc");
  navHeader("Tiles@home project page"); 
?>
  
  <p><b><a href="http://wiki.openstreetmap.org/index.php/Tiles%40home">tiles@home</a></b> is a distributed project which renders OpenStreetMap data of the whole
  world, and provides free access to the result here, on this website and others.</p>
  
  <p>Map data and images can be copied under the Creative Commons CC-BY-SA 2.0 license,
  and are easy to integrate into your own project. Ask on the wiki if you need help.</p>
  
  
  <p><a href="Browse/">Browse the map</a>, see what tiles are available</p>
  
  <p><a href="http://www.openstreetmap.org/index.html">Slippy map</a> (select the "osmarender" layer, using the <span style="background-color:darkblue;color:white;padding:2px"><b>+</b></span> control at the right)</p>
  
  <h2>What's happening today?</h2>

  <p><a href="Log/Messages/">Messages</a>, errors, warnings, etc.</p>
  
  <p><a href="Stats/">Statistics</a>, 
  <a href="http://www.openstreetmap.org/munin/openstreetmap/dev.openstreetmap.html">server stats</a>, 
  <a href="Log/">recent uploads</a>, 
  <a href="Log/Requests/">render requests</a>, and
  <a href="Log/graphs.php">upload graphs</a> (shown below)</p>
  
  <p><img src="Log/graph.png" alt="graph of upload bandwidth over time"></p>
    
  <?php
    if(1){ // Optional section: displays some recent tile-uploads
      include("connect/connect.php");
      $Result = mysql_query("select * from tiles_misc limit 1;");
      $Data = mysql_fetch_assoc($Result);
      
      $X = $Data["last_12x"];
      $Y = $Data["last_12y"];
      printf("<p>Latest interesting upload: <a href=\"Browse/?x=%d&amp;y=%d&amp;z=%d\">%d,%d</a> by %s</p>\n",
        $X, $Y, 12,
        $X, $Y,
        $Data["last_user"]);
      
      printf("<table cellspacing=3 cellpadding=0 border=0>");
      printf("<tr><td>%s</td>\n<td>%s</td></tr>\n", imageOf($X*2,$Y*2,13),imageOf($X*2+1,$Y*2,13));
      printf("<tr><td>%s</td>\n<td>%s</td></tr>\n", imageOf($X*2,$Y*2+1,13),imageOf($X*2+1,$Y*2+1,13));
      printf("</table>\n");
    }
    function imageOf($X,$Y,$Z){
      return(sprintf("<img border=1 width=256 heght=256 src=\"Tiles/tile.php/%d/%d/%d.png\"></div>\n",$Z,$X,$Y));
      }
  ?>
  
  <h2>Tools</h2>
  
  <p><a href="Upload/">Upload tiles</a> as ZIP files (password required)</p>
  
  <h2>See also</h2>
  
  <p>Read the <a href="http://wiki.openstreetmap.org/index.php/Tiles%40home">tiles@home wiki page</a> for details of this project</p>

<?php navFooter() ?>
