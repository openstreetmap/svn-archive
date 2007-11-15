<html>
<head>
<title>OpenStreetMap tiles@home</title>
<link rel="stylesheet" href="../../../styles.css">
</head>
<body>
<div class="all">
<h1 class="title"><img src="../../../Gfx/tah.png" alt="tiles@home" width="600" height="109"></h1>
<p class="title">Rendering requests</p>
<hr>

<?php
  /* Displays logfiles of recent requests for the tiles@home website
  ** OJW 2006
  ** License: GNU GPL v2 or at your option any later version
  */
  include("../../../connect/connect.php");
  include("../../../lib/requests.inc");
  
  ListRenderRequests();
  
  function ListRenderRequests(){
    print "<h2>Most recent 30 requests in each queue</h2>\n";
    RenderList("Pending", REQUEST_PENDING, "Date requested");
    RenderList("New", REQUEST_NEW, "Date requested");
    RenderList("Active", REQUEST_ACTIVE, "Date taken by renderer");
    RenderList("Completed", REQUEST_DONE, "Date uploaded");
    
    print "<p><i>Current local server time is ".date('r')."</i></p>";
    }
    
  function RenderList($Title, $Status, $DateLabel="Date"){
  
    $SQL = sprintf(
      "SELECT * FROM tiles_queue WHERE `status`=%d ORDER BY date desc limit 30;",
      $Status);
      
    # SQL query to get those records
    $Result = mysql_query($SQL);
    
    print("<h3>$Title (status=$Status)</h3>\n");
    
    # Check for SQL errors
    if(mysql_error()){
      printf("<p>%s</p>\n", htmlentities(mysql_error()));
      return;
      }
  
    # Check for zero results
    $NumRows = mysql_num_rows($Result);
    if($NumRows < 1)
      {
      print "<p>None</p>\n";
      return;
      }
    
    # Display the logfile itself, as a table
    print "<table border=\"1\" cellpadding=\"5\">";
  
    # Table header
    print(TableRow(Array("Tileset", "Source", "Status", "Priority", $DateLabel,"Tile details*")));
    
    # For each entry...
    while($Data = mysql_fetch_assoc($Result)){
      $X = $Data["x"];
      $Y = $Data["y"];
      $Z = $Data["z"];
      
      $BrowseURL = sprintf("../../../Browse/?x=%d&amp;y=%d&amp;z=%d",$X,$Y,$Z);
      $DetailsURL = sprintf("../../../Tiles/info.php?x=%d&amp;y=%d&amp;z=%d",$X,$Y,$Z);
      
      $TileHtml = sprintf("<a href=\"%s\">%d,%d</a>", $BrowseURL, $X, $Y);
      $DetailsHtml = sprintf("<a href=\"%s\">...</a>", $DetailsURL);
      
      print(TableRow(Array(
        $TileHtml,
        htmlentities($Data["src"]),
        $Data["status"],
        $Data["priority"],
        $Data["date"],
        $DetailsHtml)));
    }
    print "</table>\n";
  }

  # Displays an array of items in an HTML table
  function TableRow($Array){
    return("<tr><td>".implode("</td>\n<td>", $Array)."</td></tr>\n");
  }
  
  ?>
</div>
</body>
</html>
