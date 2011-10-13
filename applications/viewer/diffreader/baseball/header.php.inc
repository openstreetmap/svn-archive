<?php
include('countdown.php.inc');

$dbFile = "./baseball-edits.db";

$lastUpdate = date ("F d Y H:i:s.", filemtime("../on_seq.txt"));


if (!file_exists($dbFile)) die('Missing db file ' . $dbFile);
$db = new SQLite3($dbFile);


//No cache 
header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");    // Date in the past 
header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT"); // always modified 
header("Cache-Control: no-store, no-cache, must-revalidate");  // HTTP/1.1 
header("Cache-Control: post-check=0, pre-check=0", false); 
header("Pragma: no-cache");                          // HTTP/1.0

?>
<html>
<head>

<title>OpenStreetMap's Big Baseball Project</title>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<style>
TABLE {
   border-style:solid;
   border-width:1px;
   border-color:#aaa;
   background: #f9f9f9;
   border-collapse: collapse;
   font-size: 95%;
}
 
TABLE TD {
   padding:4px;
   border-style:solid;
   border-width:1px;
   border-color:#aaa;
}
TABLE TH {
   padding:4px;
   background-color:#E9E9E9;
   border-style:solid;
   border-width:1px;
   border-color:#aaa;
}
#list {
   align:center;
   
}
#ball {
   float:left;
   margin:10px;
   margin-bottom:40px;
   border:none;
}
#toptext {
   clear:both;
}
#menu {
   background:#EEE;
   border: 1px solid;
   padding:3px;
   font-weight:bold;
}
.tablelabel {
   width:70%; margin:10px; text-align:left;
}
</style>
</head>

<body>


<div id="toptext">
  <img src="./baseball-ball.png" id="ball" alt="baseball" title="baseball - thanks to vedub4us on openclipart.org">
  <h1>OpenStreetMap's Big Baseball Project</h1>
  <p>
  Welcome to the big baseball project. We are adding baseball fields to the map! <a href="http://wiki.openstreetmap.org/wiki/Big_baseball_project_2011" title="More details on the OpenStreetMap wiki">Find out more</a>
  </p>
  <p><?php echo countdown(2011, 10, 27, 23, 59); /* $year, $month, $day, $hour, $minute */ ?></p>
  
  
  <span id="menu">
  <a href="./" title="list of edits coming in">Edits</a> | <a href="./rankings.php" title="which users are making most edits">Rankings</a>
  </span>
  <br><br>

</div>

