<html>
<head>
<title>tiles@home metadata</title>
<link rel="stylesheet" href="../styles.css">
</head>
<body>
<div class="all">
  <h1 class="title"><a href="../"><img src="../Gfx/tah.png" alt="tiles@home" width="600" height="109"></a></h1>
  <p class="title">Tile metadata</p>
  <hr>
  
<p><a href="Data/latest.txt.gz"><b>Download</b> the tile details</a> (as a large gzipped textfile)</p>  

<?php
  $StatsFile = "Data/latest.txt.gz";
  if(!file_exists($StatsFile)){
    printf("<p>File doesn't currently exist</p>");
  }
  else
  {
    $Size = filesize($StatsFile);
    $Date = filemtime($StatsFile);
    
    printf("<p>File is <b>%1.1f MB</b> compressed (%d bytes), created on <b>%s</b></p>", 
      $Size / (1024 * 1024),
      $Size,
      date("Y-m-d H:i", $Date));
    
  } 
  
?>
<p>Fields currently in the export file:
<ul>
<li>x, y, zoom</li>
<li>Type - which tile layer (default: 1 = osmarender)</li>
<li>Filesize in bytes</li>
<li>Date of upload</li>
<li>User ID (<a href="Data/userlist.txt">see list of users</a>)</li>
<li>Software version used to upload (<a href="../Versions/">see list of versions</a>)</li>
<li>Spare</li>
<li>Spare</li>
</ul></p>

<h3>Other files</h3>

<p>There may be other stuff created from time to time - 
<a href="Data/">view the data directory</a> to see them</p>

</body></html>
