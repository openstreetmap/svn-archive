<?php 
  include("../lib/gui.inc");
  navHeader("Tiles@home statistics", "../"); 
?>

<p>This is a list of every map tile available, with the person who uploaded it, and the date it was uploaded.</p>

<p><a href="Data/stats.txt.gz"><b>Download</b> the tile details</a> (as a large gzipped textfile)</p>  

<?php
  $StatsFile = "Data/stats.txt.gz";
  if(!file_exists($StatsFile)){
    printf("<p>File doesn't currently exist</p>");
  }
  else
  {
    $Size = filesize($StatsFile);
    $Date = filemtime($StatsFile);
    
    printf("<p>File is <b>%1.1f MB</b> (%d bytes), created on <b>%s</b></p>", 
      $Size / (1024 * 1024),
      $Size,
      date("Y-m-d H:i", $Date));
    
  } 
  
?>
<p>There may also be manually-generated files available, see the <a href="Data/">data directory</a> for details</p>

<hr />
<p><a href="source.php">Source code</a> for the perl script which generates stats</p>  

<p>The <a href="Old/">old stats page</a> (no longer updated) is still available to view</p>  

<?php navFooter() ?>
