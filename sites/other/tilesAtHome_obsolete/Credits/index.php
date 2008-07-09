<?php
$title = "Credits";
include "../lib/template/header.inc" 
?>  

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
    $Columns = "Rank, Name, Activity<a href='".$_server[php-self]."?order='>&darr;</a>, Last upload<a href='".$_server[php-self]."?order=date'>&darr;</a>, Version, Uploaded Bytes (Since 2007/11/11)<a href='".$_server[php-self]."?order=bytes'>&darr;</a>, Disabled";
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
      
      array_push($Row, $Data["disabled"]);
      
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
    return(sprintf("%d seconds", $Age));
  $Age /= 60;
  if($Age < 120)
    return(sprintf("%d minutes", $Age));
  $Age /= 60;
  if($Age < 48)
    return(sprintf("%d&nbsp;hours", $Age));
  $Age /= 24;
  if($Age < 14)
    return(sprintf("%d&nbsp;days", $Age));
  if($Age < 60)
    return(sprintf("%d&nbsp;weeks", $Age/7));
  else if($Age < 356)
    return(sprintf("%d&nbsp;months", $Age/30));
  else
    return(sprintf("%d&nbsp;years", $Age/356));
}
?>

</div>
</body>
</html>
