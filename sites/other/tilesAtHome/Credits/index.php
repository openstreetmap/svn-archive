<html>
<head>
<title>tiles@home credits</title>
<link rel="stylesheet" href="../styles.css">
<meta name="robots" content="nofollow,noindex">
</head>
<body>
<div class="all">
<h1 class="title"><a href="../"><img src="../Gfx/tah.png" alt="tiles@home" width="600" height="109"></a></h1>
<p class="title">Credits</p>
<hr>
<p>The following people have been uploading tiles to the program:</p>

<p><i>Counting began 10pm, 6<sup>th</sup> March 2007 - no records are available for earlier uploads. Blank tiles not included in count.</i></p>
<p>blue row = upload within the last 10 minutes.</p>
<?php
include("../connect/connect.php");
include("../lib/versions.inc");

print("<p>Sort by <a href=\"./?sort=id\">user</a> or <a href=\"./\">uploads</a></p>\n");


$SQL = sprintf(
  "select *, unix_timestamp(`last_upload`) as unixtime from tiles_users order by %s;",
  $_GET["sort"] == "id" ? "id" : "tiles desc");
  
$Result = mysql_query($SQL);
if(!mysql_error()){
  if(mysql_num_rows($Result) > 0){
    
    # Start the HTML table
    print "<table border=1 cellspacing=0 cellpadding=5>";
    
    # Header
    $Columns = "ID, Name, Activity, Last upload, Version, Notes, Samples";
    print "<tr><th>" . str_replace(", ", "</th><th>", $Columns) . "</th></tr>\n";
    
    ##-------------------------------------------------------
    ## For each user...
    ##-------------------------------------------------------
    while($Data = mysql_fetch_assoc($Result)){
      $Row = array();

      # User ID
      array_push($Row, sprintf("#%d", $Data["id"]));
      
      # Username
      array_push($Row, sprintf("<b>%s</b>",htmlentities($Data["name"])));
      
      # Upload details
      array_push($Row, sprintf("%s tiles in %s uploads",
        number_format($Data["tiles"], 0, ".", ","),
        number_format($Data["uploads"], 0, ".", ",")));
    
      # Time last seen
      $Age = ageOf($Data["unixtime"]);
      $OnNow = ($Age < 10 * 60)
        && ($Data["tiles"] != 0);
      array_push($Row, FormatAge($Age));
     
      # Version ID
      array_push($Row, htmlentities(versionName($Data["version"])));
      
      # Notes
      array_push($Row, sprintf("--"));
      
      # Link to samples
      array_push($Row, sprintf("<a href=\"ByUser/?id=%d\">...</a>", $Data["id"]));
      
      # Convert all the data into a row of HTML table
      $Style = $OnNow
        ? "background-color:#44C"
        : "background-color:#444";
      print "<tr style=\"$Style\"><td>" . implode("</td><td>", $Row) . "</td><tr>\n";
    
    }
    ##-------------------------------------------------------
    print "</table>\n";
  }
}

function ageOf($Timestamp){
  if($Timestamp == 0)
    return("never");
  
  $Age = time() - $Timestamp;
  return($Age);
}

function FormatAge($Age){
  if($Age < 0)
    return("future");
  if($Age < 120)
    return(sprintf("%d seconds ago", $Age));
  $Age /= 60;
  if($Age < 120)
    return(sprintf("%d minutes ago", $Age));
  $Age /= 60;
  if($Age < 24)
    return(sprintf("%d hours ago", $Age));
  $Age /= 24;
  if($Age < 7)
    return(sprintf("%d days ago", $Age));
  $Age /= 7;
  if($Age < 40)
    return(sprintf("%d weeks ago", $Age));
  $Age /= 30;
  if($Age < 12)
    return(sprintf("%d months ago", $Age));
  
  return("ages ago...");
}
?>

</div>
</body>
</html>
