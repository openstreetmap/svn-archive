<html>
<head>
  <title>Recent upload log</title>
  <link rel="stylesheet" href="../styles.css">
</head>
<body>
  <h1>Recent upload log</h1>
  <div id="main">
  <?php
  /* Displays logfiles of recent uploads for the tiles@home website
  ** OJW 2006
  ** License: GNU GPL v2 or at your option any later version
  */
  
  # Database connection - connects to MySQL and selects a database
  include("../connect/connect.php");
  
  # Set a limit on how many records should be displayed - currently "in the last x hours"
  $TimePeriodHours = 1;
  $SinceTime = time() - $TimePeriodHours * 3600;
  
  # Format for displaying dates - see php.net/date
  $TimeFormat = "Y-m-d H:i";

  # SQL query to get those records
  $SQL = sprintf("SELECT * FROM tiles_log WHERE time > %d ORDER BY time desc;", $SinceTime);
  $Result = mysql_query($SQL);
  
  # Check for SQL errors
  if(mysql_error()){
    printf("<p>%s</p>\n", htmlentities(mysql_error()));
    exit;
    }

  # Check for zero results
  $NumRows = mysql_num_rows($Result);
  if($NumRows < 1)
    {
    print "<p>None</p>\n";
    exit;
    }
  
  # Display a summary, using the number of logfile entries
  printf("<p><b>%d uploads in the last %d hour%s</b> (average: one every %1.1f sec)</p>\n", 
  	$NumRows, 
  	$TimePeriodHours,
  	$TimePeriodHours == 1 ? "" : "s",
    $TimePeriodHours * 3600 / $NumRows);
  
  # Display the logfile itself, as a table
  print "<table border=\"1\" cellpadding=\"5\">";
 
  # Table header
  print(TableRow(Array("Upload ID", "Filesize", "Username","Time", "Filename")));
  
  # For each entry...
  while($Data = mysql_fetch_assoc($Result)){
    
    # Logfile entry
    print(TableRow(Array(
      $Data["id"],
      FormatSize($Data["size"]),
      htmlentities($Data["user"]),
	  date($TimeFormat, $Data["time"]),
      htmlentities($Data["filename"]))));
    
  }
  print "</table>\n";


  # Displays an array, as items in an HTML table
  function TableRow($Array){
  	  return("<tr><td>".implode("</td>\n<td>", $Array)."</td></tr>\n");
  }
  
  # Formats a number of bytes, as KB/MB etc.
  function FormatSize($Size){
    if($Size < 1024)
      return(sprintf("%d B", $Size));
    if($Size < 1048576)
      return(sprintf("%1.1f KB", $Size / 1024));
    return(sprintf("%1.1f MB", $Size / 1048576));
  }


  ?>
  </div>
</body>
</html>
	