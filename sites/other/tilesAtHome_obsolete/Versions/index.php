<html>
<head>
<title>tiles@home versions</title>
<link rel="stylesheet" href="../styles.css">
</head>
<body>
<div class="all">
<h1 class="title"><a href="../"><img src="../Gfx/tah.png" alt="tiles@home" width="600" height="109"></a></h1>
<p class="title">Versions</p>
<hr>
<p>Versions of the client software which have been released</p>


<?php
# uncomment for database access
# include("../connect/connect.php");

include("../lib/versions.inc");

# Start the HTML table
print "<table border=1 cellspacing=0 cellpadding=5>";

# Get a list of versions, and loop through it
$Versions = getVersions();
foreach($Versions as $ID => $Version){
  $Row = array();
  
  array_push($Row, $ID);
  array_push($Row, $Version["name"]);
  array_push($Row, $Version["ok"] ? "Uploads accepted" : "Uploads blocked");
  
  # Convert all the data into a row of HTML table
  print "<tr><td>" . implode("</td><td>", $Row) . "</td><tr>\n";
}
print "</table>\n";

?>
<p><i>Versions</i> of tiles@home indicate which release of software someone is running.</p>

<p>They are typically used to prevent uploads by any version which is known 
to cause problems, or which would clash with the map styles used by the 
majority of uploaders</p>

<p>Only externally-visible changes cause a new version name (things like optimisation which
only affect the individual computer, don't require a new version name)</p>

<p>Version names are approximately alphabetical, and are chosen by whoever makes the 
significant change to tiles@home.  They are never announced in advance.</p>

<p>For help in choosing version names, try the <a href="http://almien.co.uk/City/Page/index_alphabetical.htm">city list</a> or on 
<a href="http://en.wikipedia.org/w/index.php?title=Special%3AAllpages&amp;from=List+of+cities">wikipedia</a></p>

</div>
</body>
</html>
