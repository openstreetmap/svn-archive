<?php
  /* Displays logfiles of recent requests for the tiles@home website
  ** OJW 2006
  ** License: GNU GPL v2 or at your option any later version
  */
  include("../../lib/gui.inc");
  navHeader("Outstanding rendering requests","../../",0); 

  ListRenderRequests();
  navFooter();
  
  function ListRenderRequests(){
    # Database connection - connects to MySQL and selects a database
    include("../../connect/connect.php");
    
    # SQL query to get those records
    $DateLimit = date("Y-m-d H:i:s", time()- 3600);
    $SQL = sprintf("SELECT `x`,`y`,`sent`,`priority`,`src`,UNIX_TIMESTAMP(`date`) as date FROM tiles_queue ORDER BY date desc;", $DateLimit);
    $Result = mysql_query($SQL);
    
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
    print(TableRow(Array("Tileset", "Status", "Source", "Priority","Time")));
    
    # For each entry...
    while($Data = mysql_fetch_assoc($Result)){
      
      $Link = sprintf("../../Browse/?x=%d&amp;y=%d&amp;z=12", $Data["x"],$Data["y"]);
      
      # Logfile entry
      print(TableRow(Array(
        sprintf("<a href=\"%s\">%d,%d</a>", $Link,$Data["x"],$Data["y"]),
        $Data["sent"]?"done":"todo",
        htmlentities($Data["src"]),
        $Data["priority"],
        date("H:i:s", $Data["date"]) )));
      
    }
    print "</table>\n";
  }

  # Displays an array, as items in an HTML table
  function TableRow($Array){
  	  return("<tr><td>".implode("</td>\n<td>", $Array)."</td></tr>\n");
  }
  
  ?>