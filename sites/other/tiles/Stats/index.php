<html><head>
<title>Tiles@home stats</title>
<link rel="stylesheet" href="../styles.css">
</head>
<body>
<h1>Tiles@home stats</h1>
<div id="main">
<?php

$Filename = "stats.htm";
DoCached($Filename, $_GET["killcache"] == "883");
readfile($Filename);

function DoCached($Filename, $KillCache){

	$Age = time() - filemtime($Filename);
	if($Age < 12 * 86400 && !$KillCache){
		printf("<p><i>Cached, age %d sec</i></p>\n", $Age);
		return;	
	}
	
	print "<p>Regenerating stats, this will take a while...</p>\n";
	flush();
	
	ob_start();
	include("../connect/connect.php");
	TileStats();
	InterestingTiles();
	PeopleStats();
	$Data = ob_get_contents();
	ob_end_clean();
	
	
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

function PeopleStats(){
  $SQL = "SELECT user, count(*) as count FROM tiles WHERE `exists`=1 GROUP BY user ORDER BY count desc;";
  
  $Result = mysql_query($SQL);
  if(mysql_error()){
    printf("<p>%s</p>\n", htmlentities(mysql_error()));
    return;
    }
    
  print "<h2>Uploaders</h2>\n";
  if(mysql_num_rows($Result) < 1)
    {
    print "<p>None</p>\n";
    return;
    }
    
  print "<table border=\"1\" cellpadding=\"5\">";
  $Count = 1;
  while($Data = mysql_fetch_assoc($Result)){
    
    printf("<tr><td>%d</td><td>%s</td><td>%d tiles</td></tr>", 
      $Count++,
      $Data["user"],
      $Data["count"]);
  }
  print "</table>\n";

}
function TileStats(){
  $SQL = "SELECT z, count(*) as count FROM tiles WHERE `exists`=1 GROUP BY z ORDER BY z;";
  
  $Result = mysql_query($SQL);
  if(mysql_error()){
    printf("<p>%s</p>\n", htmlentities(mysql_error()));
    return;
    }
    
  print "<h2>Tiles available</h2>\n";
  if(mysql_num_rows($Result) < 1)
    {
    print "<p>None</p>\n";
    return;
    }
    
  print "<table border=\"1\" cellpadding=\"5\">\n";
  while($Data = mysql_fetch_assoc($Result)){
  	  $Z = $Data["z"];
  	  if($Z > 0 && $Z < 20){
  	  	  
      $MaxTiles = pow(4, $Z);
      if($MaxTiles>0)
	      $PercentDone = 100 * $Data["count"] / $MaxTiles;
	  else
	  	  $PercentDone = 0;
    	
	    printf("<tr><td>Zoom-%d:</td><td>%d tiles</td><td>%1.3f%%</td></tr>\n", 
	      $Z,
	      $Data["count"], 
	    	$PercentDone);
         }
  }
  print "</table>\n";
}

function InterestingTiles(){
  print "<h2>Interesting tilesets</h2>\n";
  $SQL = "select `x`,`y`,`z`,`size` from `tiles` where `exists`=1 and `z`=12 and `size`>50000 order by `size` desc limit 30;";
  $Results = mysql_query($SQL);
  if(mysql_num_rows($Results)){
  print "<table border=\"1\" cellpadding=\"5\">";
    while($Data = mysql_fetch_assoc($Results)){
      $Link = sprintf("../Browse/?x=%d&amp;y=%d&amp;z=%d",
        $Data["x"],
        $Data["y"],
        $Data["z"]);

      printf("<tr><td><a href=\"%s\">%d,%d,%d</a></td><td>%1.1fK</td></tr>\n", 
        $Link,
        $Data["x"],
        $Data["y"],
        $Data["z"],
        $Data["size"] / 1024);
    }
  print "</table>\n";
  }

}

?></div>
</body>
</html>
	