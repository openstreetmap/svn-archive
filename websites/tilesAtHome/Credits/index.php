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

<?php
include("../connect/connect.php");
include("../lib/versions.inc");

$SQL = "select *, unix_timestamp(`last_upload`) as unixtime from tiles_users order by tiles desc;";
$Result = mysql_query($SQL);
if(!mysql_error()){
  if(mysql_num_rows($Result) > 0){
    
    # Start the HTML table
    print "<table border=1 cellspacing=0 cellpadding=5>";
    
    # Header
    $Columns = "Name, Activity, Last upload, Version, Samples";
    print "<tr><th>" . str_replace(", ", "</th><th>", $Columns) . "</th></tr>\n";
    
    ##-------------------------------------------------------
    ## For each user...
    ##-------------------------------------------------------
    while($Data = mysql_fetch_assoc($Result)){
      $Row = array();
      
      # Username
      array_push($Row, sprintf("<b>%s</b>",htmlentities($Data["name"])));
      
      # Upload details
      array_push($Row, sprintf("%d tiles in %d uploads",
        $Data["tiles"],
        $Data["uploads"]));
    
      # Time last seen
      array_push($Row, ageOf($Data["unixtime"]));
      
      # Version ID
      array_push($Row, htmlentities(versionName($Data["version"])));
      
      # Version ID
      array_push($Row, sprintf("<a href=\"ByUser/?id=%d\">...</a>", $Data["id"]));
      
      # Convert all the data into a row of HTML table
      print "<tr><td>" . implode("</td><td>", $Row) . "</td><tr>\n";
    
    }
    ##-------------------------------------------------------
    print "</table>\n";
  }
}

function ageOf($Timestamp){
  if($Timestamp == 0)
    return("never");
  
  $Age = time() - $Timestamp;
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
