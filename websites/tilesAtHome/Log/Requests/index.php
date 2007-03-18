<html>
<head>
<title>OpenStreetMap tiles@home</title>
<link rel="stylesheet" href="../../styles.css">
</head>
<body>
<div class="all">
<h1 class="title"><img src="../../Gfx/tah.png" alt="tiles@home" width="600" height="109"></h1>
<p class="title">Rendering requests</p>
<hr>

<?php
  /* Displays logfiles of recent requests for the tiles@home website
  ** OJW 2006
  ** License: GNU GPL v2 or at your option any later version
  */
  ListRenderRequests();
  
  function ListRenderRequests(){
    # Database connection - connects to MySQL and selects a database
    include("../../connect/connect.php");
    RenderList(
      "Requested",
      "SELECT * FROM tiles_queue WHERE `sent`=0 ORDER BY date desc limit 30;",
      "date");
    
    RenderList(
      "Being rendered",
      "SELECT * FROM tiles_queue WHERE `sent`=1 ORDER BY date desc limit 30;",
      "date_taken");
      
    RenderList(
      "Completed",
      "SELECT * FROM tiles_queue WHERE `sent`=2 ORDER BY date desc limit 30;",
      "date_uploaded");
    
    print "<p><i>(max 30 requests shown)</i></p>";
    }
    
  /* Show a filtered view of rendering requests
  **  * Title - what to call this list
  **  * SQL - SQL statement which gives the list
  **  * Datefield - which of the datetime fields to display
  */
  function RenderList($Title, $SQL, $DateField){
    # SQL query to get those records
    $Result = mysql_query($SQL);
    
    print("<h2>$Title</h2>\n");
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
    
    # printf("<p>%d areas requested since %s</p>\n", $NumRows, $DateLimit);
    
    # Display the logfile itself, as a table
    print "<table border=\"1\" cellpadding=\"5\">";
  
    # Table header
    print(TableRow(Array("Tileset", "Source", "Priority", $DateField, "Uploaded by")));
    
    # For each entry...
    while($Data = mysql_fetch_assoc($Result)){
      
      $Link = sprintf("../../Browse/?x=%d&amp;y=%d&amp;z=12", $Data["x"],$Data["y"]);
      
      # Logfile entry
      print(TableRow(Array(
        sprintf("<a href=\"%s\">%d,%d</a>", $Link,$Data["x"],$Data["y"]),
        htmlentities($Data["src"]),
        $Data["priority"],
        $Data[$DateField],
        $Data["uploaded_by"])));
      
    }
    print "</table>\n";
  }

  # Displays an array, as items in an HTML table
  function TableRow($Array){
    return("<tr><td>".implode("</td>\n<td>", $Array)."</td></tr>\n");
  }
  
  ?>
</div>
</body>
</html>
