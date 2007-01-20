<html><head>
<title>Tiles@home upload graphs</title>
<link rel="stylesheet" href="../styles.css">
</head>
<body>
<h1>Tiles@home upload graphs</h1>
<div id="main">
<?php

$Filename = "graphs.htm";
$ImgFilename = "graph.png";

DoCached($Filename, $ImgFilename, $_GET["killcache"] == "ok");
readfile($Filename);


function DoCached($Filename, $ImgFilename, $KillCache){
  $Age = time() - filemtime($Filename);
  
  # Minimum time between refresh, hours
  if($Age < 12 * 3600){
    printf("<p><i>Cached, age %d sec</i></p>\n", $Age);
    return;	
  }
  
  # If page is old enough, the default is still "view only" unless someone requests a refresh
  if(!$KillCache){
    printf("<p><i>Cached, <a href=\"./graphs.php?killcache=ok\">click to update</a> (may take a while)</i></p>\n", $Age);
    return;
  }
  
  print "<p>Regenerating stats, this will take a while...</p>\n";
  flush();
  
  # Do the refresh, and store all the STDOUT 
  ob_start();
  include("../connect/connect.php");
  UploadGraphs($ImgFilename);
  
  $Data = ob_get_contents();
  ob_end_clean();
  
  # Write that STDOUT into a cached file
  $fp = fopen($Filename, "w");
  if($fp){
    fputs($fp, $Data);
    fclose($fp);
    }
  else
  {
    print "<p>error writing to cache file</p>\n";
  }
}

function UploadGraphs($ImgFilename){
  $SQL = "SELECT time, size FROM tiles_log;";
  
  $Result = mysql_query($SQL);
  if(mysql_error()){
    printf("<p>%s</p>\n", htmlentities(mysql_error()));
    return;
    }
    
  if(mysql_num_rows($Result) < 1)
    {
    print "<p>None</p>\n";
    return;
    }
    
  $CountStat = array();
  $SizeStat = array();
  $TimePeriod = 3600;
  
  $SizeUnit = 1024*1024;
  $SizeUnitName = "MB per hour";
  
  $GraphWidthPx = 300;
  
  $MaxSize = 1;
  $Earliest = time();
  $Latest = 0;
  
  while($Data = mysql_fetch_assoc($Result)){
    $Time = $Data["time"];
    $TimeP = floor($Time / $TimePeriod);
    $CountStat[$TimeP]++;
    $SizeStat[$TimeP] += $Data["size"];
    
    if($SizeStat[$TimeP] > $MaxSize)
      $MaxSize = $SizeStat[$TimeP];
      
    if($Time > $Latest)
      $Latest = $Time;
    if($Time < $Earliest)
      $Earliest = $Time;
  }
  
  if(0){
    print "<table border=\"1\" cellpadding=\"5\">";
    foreach($CountStat as $TimeP => $Count){
      $Size = $SizeStat[$TimeP];
      $Time = $TimeP * $TimePeriod;
      
      $ImgW = $GraphWidthPx * $Size / $MaxSize;
      $ImgHtml = sprintf("<img src=\"g.png\" width=\"%d\" height=\"10\">", $ImgW);
      
      printf("<tr><td>%s</td><td>%d uploads</td><td>%1.1f %s</td><td>%s</td></tr>\n",
        date("D j M, g a", $Time),
        $Count,
        $Size / $SizeUnit,
        $SizeUnitName,
        $ImgHtml);
    }
  }
  print "</table>";

  $W = sizeof($CountStat);
  $H = 150;
  
  $Image = imagecreatetruecolor($W,$H);
  $FG1 = imagecolorallocate($Image,128,64,0);
  $FG2 = imagecolorallocate($Image,64,128,0);
  $GridColA = imagecolorallocate($Image,255,180,180);
  $GridColB = imagecolorallocate($Image,255,220,220);
  
  $BG = imagecolorallocate($Image,255,255,220);
  imagefilledrectangle($Image,0,0,$W,$H,$BG);
  $X = 0;
  
  
  # Horizontal gridlines
  $Grid = 25 * 1024 * 1024; // 25MB gridlines
  $TickMarks = 4; // Highlight every x gridlines
  $NumGrid = $MaxSize / $Grid;
  for($V = 1; $V < $NumGrid; $V++){
    $Y = $H * (1-($V / $NumGrid));
    imageline($Image,0,$Y,$W,$Y, !($V % $TickMarks)?$GridColA:$GridColB);
  }
  # Barchart
  foreach($CountStat as $TimeP => $Count){
    $Size = $SizeStat[$TimeP];
    $Time = $TimeP * $TimePeriod;
    
    $Y = $H * (1-($Size / $MaxSize));
    $X++;
    
    $OddDay = floor($Time / 86400) % 2 == 0;
    
    imageline($Image,$X,$Y,$X,$H,$OddDay ? $FG1 : $FG2);
  }
  # Save image
  imagepng($Image, $ImgFilename);
  
  printf("<table>");
  printf("<tr><td>&nbsp;</td>");
  printf("<td><img src=\"%s\" width=\"%d\" height=\"%d\" alt=\"%s\"></td>", "graph.png", $W,$H,"Upload stats graph");
  printf("<td valign=\"top\" align=\"left\">%1.0f %s</td></tr>",
    $MaxSize / $SizeUnit, 
    $SizeUnitName);
  
  printf("<tr><td align=\"right\">%s</td>", date("Y-m-d", $Earliest));
  printf("<td align=\"center\">Uploads per hour<br>(colour bands are days)</td>\n", $MaxSize / $SizeUnit, $SizeUnitName);
  printf("<td align=\"left\">%s</td></tr>", date("Y-m-d", $Latest));
  printf("</table>");
  
}
?></div>
</body>
</html>
	