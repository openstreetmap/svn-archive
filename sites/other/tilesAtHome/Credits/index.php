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

<p>blue row = upload within the last 10 minutes.</p>
<?php
include("../connect/connect.php");
include("../lib/versions.inc");

$order = "tiles desc";
if ($_GET['order'] == "bytes") {
    $order = "bytes desc, tiles desc";
}
if ($_GET['order'] == "date") {
    $order = "last_upload desc";
}    

$SQL = sprintf(
  "select *, unix_timestamp(`last_upload`) as unixtime from tiles_users order by %s;",
    $order);
  
$Result = mysql_query($SQL);
if(!mysql_error()){
  if(mysql_num_rows($Result) > 0){
    
    # Start the HTML table
    print "<table border=1 cellspacing=0 cellpadding=5 width='100%'>";
    
    # Header
    $Columns = "Rank, Name, Activity<a href='".$_server[php-self]."?order='>&darr;</a>, Last upload<a href='".$_server[php-self]."?order=date'>&darr;</a>, Version, Uploaded Bytes (Since 2007/11/11)<a href='".$_server[php-self]."?order=bytes'>&darr;</a>";
    print "<tr><th>" . str_replace(", ", "</th><th>", $Columns) . "</th></tr>\n";
    
    ##-------------------------------------------------------
    ## For each user...
    ##-------------------------------------------------------
    $i = 0;
    while($Data = mysql_fetch_assoc($Result)){
      $Row = array();

      # Rank
      array_push($Row, sprintf("%d.", ++$i));

      # Link to samples
      array_push($Row, sprintf("<a href=\"ByUser/?id=%d\">%s</a>", $Data["id"], htmlentities($Data["name"])));
      
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
      
      # Version ID
      array_push($Row, htmlentities(number_Format($Data["bytes"], 0, ".", ",")));
      
      # Convert all the data into a row of HTML table
      $Style = $OnNow
        ? "background-color:#44C"
        : "background-color:#444";
      print "<tr style=\"$Style\"><td>" . implode("</td><td>", $Row) . "</td><tr>\n";
    
    }
    ##-------------------------------------------------------
    print "</table>\n";
    print "<p><i>Blank tiles not included in count.</i></p>\n";
  }
}

function ageOf($Timestamp){
  $Age = time() - $Timestamp;
  
  return( $Age ? $Age : "never");
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
