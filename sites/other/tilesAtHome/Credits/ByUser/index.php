<html>
<head>
<title>tiles@home credits by user</title>
<link rel="stylesheet" href="../../styles.css">
<meta name="robots" content="nofollow, noindex">
</head>
<body>
<div class="all">
<h1 class="title"><a href="../../"><img src="../../Gfx/tah.png" alt="tiles@home" width="600" height="109"></a></h1>
<p class="title">Credits by user</p>
<hr>

<p><form action="./" method="get">
User ID: <input type="text" name="id">
<input type="submit" name="OK"> <i>(not username)</i>
</form></p>

<?php
include("../../connect/connect.php");
include("../../lib/versions.inc");
include("../../lib/tilenames.inc");
include("../../lib/layers.inc");

showByUser($_GET["id"]);

function showByUser($User){
  $SelectLimit = 20;
  
  $SQL = sprintf("SELECT x,y,z,type,date from tiles_meta where `user`=%d limit %d;", $User,
  $SelectLimit);
  
  $Result = mysql_query($SQL);
  if(mysql_error())
    return(0);
  if(mysql_num_rows($Result) < 1)
    return(0);
  
  printf("<p>Selecting %d uploaded tiles from user #%d (not necessarily the most recent ones).</p>", 
    $SelectLimit,
    $User);
  
  # Start the HTML table
  print "<table border=1 cellspacing=0 cellpadding=5>";
  
  while($Data = mysql_fetch_assoc($Result)){
      $Row = array();

      $X = $Data["x"];
      $Y = $Data["y"];
      $Z = $Data["z"];
      
      $URL = sprintf("../../Browse/?z=%d&amp;x=%d&amp;y=%d", $Z, $X, $Y);
        
      array_push($Row, sprintf(
      "<a href=\"%s\"><img src=\"%s\" width=\"256\" height=\"256\"></a>",
        $URL,
        TileURL($X,$Y,$Z,LayerName($Data["type"])),
        $Data["z"],
        $Data["x"],
        $Data["y"]));
      
      array_push($Row, sprintf(
      "%d, %d at zoom-%d, type %d<br>%s<br>%1.1f KB",
        $X,
        $Y,
        $Z,
        $Data["type"],
        htmlentities($Data["date"]),
        $Data["size"]/1024));

      # Convert all the data into a row of HTML table
      print "<tr><td>" . implode("</td><td>", $Row) . "</td><tr>\n";
  }
  print "</table>\n";
}
?>

</div>
</body>
</html>
