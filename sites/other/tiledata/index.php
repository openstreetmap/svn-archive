<?php
# Oliver White, 2008. This file is public domain.

# change this to something with a mysql_connect and mysql_select_db in:
include("../connect/connect.php"); 

list($Spare, $Action,$Params) = explode("/", $_SERVER["QUERY_STRING"], 3);
list($z,$x,$y) = explode("/", $Params);

$res = 1.0 / pow(2.0,31.0);

switch($Action)
{
  case "ways_in":
    checkXYZ($x,$y,$z);
    listWays($x,$y,$z);
    exit;
  case "nodes_in":
    checkXYZ($x,$y,$z);
    listNodes($x,$y,$z);
    exit;
  case "map":
    checkXYZ($x,$y,$z);
    map($x,$y,$z,true);
    exit;
  case "basic_map":
    checkXYZ($x,$y,$z);
    map($x,$y,$z,false);
    exit;
  case "way":
    getWay($Params);
    exit;
  case "help":
  default:
    header("Content-type:text/plain");
    readfile('help.txt');
    exit;
}
exit;

function checkXYZ($x,$y,$z)
{

  if($z > 17)  die("ERR\nZoom level not supported");
  if($z < 0)  die("ERR\nInvalid tile number");
  if($x < 0 || $y < 0) die("ERR\nInvalid tile number");
  $Limit = pow(2,$z);
  if($x >= $Limit || $y >= $Limit) die("ERR\nInvalid tile number");
}
function getWay($Way)
{
  header("Content-type:text/plain");
  
  $SQL = sprintf("select * from waydata_old where id=%d", $Way);
  $Result = mysql_query($SQL);
  if(!$Result) die("ERR\nNo result");
  if(mysql_errno()) die("ERR\n".mysql_error());
  if(mysql_num_rows($Result) < 1) die("ERR\nNOSUCH");
  $Data = mysql_fetch_array($Result);
  print $Data[1];
}

function listWays($x,$y,$z)
// List of all ways within a tile
// returns text, one node per line, with wayId
{
  if($z != 15) die("ERR\nOnly z15 supported at the moment");
  header("Content-type:text/plain");
  
  $SQL = sprintf("select * from wayloc_old where tile='%d,%d'", $x,$y);
  #print $SQL; die;
  $Result = mysql_query($SQL);
  if(!$Result) die("ERR\nNo result");
  if(mysql_errno()) die("ERR\n".mysql_error());
  
  printf("OK\n%d ways\n", mysql_num_rows($Result));
  while($Data = mysql_fetch_array($Result))
  {
    printf("%d\n", $Data[0]);
  }
}
function listNodes($x,$y,$z)
// List of all nodes within a tile
// returns text, one node per line, with nodeId,x,y
// x and y are (1/2^31)th of the width of the tile map
{
  if($z != 15) die("ERR\nOnly z15 supported at the moment");
  
  header("Content-type:text/plain");
  $SQL = sprintf("select * from nodepos_old where tile='%d,%d'", $x,$y);
  #print $SQL; die;
  $Result = mysql_query($SQL);
  if(!$Result) die("NUL\nNo result");
  if(mysql_errno()) die("ERR\n".mysql_error());
  
  printf("OK\n%d nodes\n", mysql_num_rows($Result));
  while($Data = mysql_fetch_array($Result))
  {
    printf("%d,%d,%d\n", $Data[0], $Data[1], $Data[2]);
  }
}


function map($x,$y,$z,$includeNodes)
{
  if($z != 15) die("ERR\nOnly z15 supported at the moment");
  header("Content-type:text/plain");
  
  $SQL_1 = sprintf("select * from wayloc_old where tile='%d,%d'", $x,$y);
  #print $SQL; die;
  $Result_1 = mysql_query($SQL_1);
  if(!$Result_1) die("ERR\nNo result");
  if(mysql_errno()) die("ERR\n".mysql_error());
  
  printf("<?xml version='1.0' encoding='UTF-8'?>\n");
  printf("<osm version='0.5' numways='%d' generator='Tile data server'>\n", mysql_num_rows($Result_1));
  while($Data_1 = mysql_fetch_array($Result_1))
  {
    $WayID = $Data_1[0];
  
    $SQL_2 = sprintf("select * from waydata_old where id=%d", $WayID);
    $Result_2 = mysql_query($SQL_2);
    if(!$Result_2) die("ERR\nNo result");
    if(mysql_errno()) die("ERR\n".mysql_error());
    if(mysql_num_rows($Result_2) < 1) die("ERR\nNOSUCH");
    $Data_2 = mysql_fetch_array($Result_2);
    print $Data_2[1];
      
  }
  print "</osm>\n";
}

header("Content-type:text/plain");
getTileData($_GET["x"], $_GET["y"], $_GET["z"], $_GET["nodes"]);


function getTileData($x,$y,$z,$nodes)
{
  // TODO: cache
}

?>