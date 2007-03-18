<html>
<head>
<title>tiles@home credits by user</title>
<link rel="stylesheet" href="../../styles.css">
</head>
<body>
<div class="all">
<h1 class="title"><a href="../../"><img src="../../Gfx/tah.png" alt="tiles@home" width="600" height="109"></a></h1>
<hr>

<?php
include("../../connect/connect.php");
include("../../lib/versions.inc");

$SQL = "select * from tiles_meta where `size`>850 and `size`<950 limit 100;";

$Result = mysql_query($SQL);
if(mysql_error())
  return(0);
if(mysql_num_rows($Result) < 1)
  return(0);

printf("<p>850-950 bytes</p>");

# Start the HTML table
print "<table border=1 cellspacing=0 cellpadding=5>";

while($Data = mysql_fetch_assoc($Result)){
  printf(
  "<tr><td><img src=\"/~ojw/Tiles/tile.php/%d/%d/%d.png\" width=\"256\" height=\"256\"></td><td>%d bytes</td></tr>\n",
    $Data["z"],
    $Data["x"],
    $Data["y"],
    $Data["size"]);
    
}
print "</table>\n";
?>

</div>
</body>
</html>
