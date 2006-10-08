<?php 
/***************************************************************************
* Copyright 2006, Oliver White
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
************************************************************************
*/
include("../Connect/connect.inc");
include("osmpassword.inc");

  /* API calls - these mustn't have any headers */
  switch($_REQUEST["action"]){
    case "random":
      RandomOSM(0);
      break;
    case "text_list":
      header("Content-type:text/plain");
      ListPlaces("",1);
      exit;
      break;
  }
  
  include($DOCUMENT_ROOT . "/Templates/templates.php"); 
  document_header("OpenStreetMap - Places", "styles.css"); 
  document_navbar();

  if(rand(0,10) == 1)
    UpdateOSM();
  ActionList("home|search|list|admin|stats");
  
  switch($_REQUEST["action"]){
    case "places":
      ImportPlaces($_REQUEST["lat"], $_REQUEST["long"]);
      break;
    case "list":
      ListPlaces();
      break;
    case "update_osm":
     UpdateOSM();
      break;
    case "import_places":
      PlacesForm();
      break;
    case "random_update":
      RandomUpdate();
      break;
    case "stats":
      ShowStats();
      break;
    case "download";
      DownloadData($_REQUEST["id"]); 
      break;
    case "rendered_by":
      ListRenderedBy($_REQUEST["user"]);
      break;
    case "search":
      SearchForm($_REQUEST["q"]);
      break;
    case "admin":
      print "<h2>Admin actions</h2>\n<p>Any of these functions can be run by normal visitors</p>\n";
      ActionList("update_osm|import_places|random_update");
      break;
    default:
      $NoAction = 1;
  }
  
  if(array_key_exists("id", $_REQUEST)){
    ShowPlace($_REQUEST["id"]);
  }
  else if(array_key_exists("q", $_REQUEST)){
    SearchForm($_REQUEST["q"]);
    ListPlaces($_REQUEST["q"]);
  }
  else if($NoAction)
  {
    MapOf(45,20,30,60, 400, 200);
    SearchForm();
  }
  
function ActionList($Actions){
 print "<ul class=\"actions\">";
  foreach(explode("|",$Actions) as $Action){
    printf("<li><a href=\"./?action=%s\">%s</a></li>\n", $Action, $Action);
  }
  print "</ul>";
}
function ListRenderedBy($Username){
  $SQL = sprintf("select * from places2 where `renderer`='%s' order by id;",
    mysql_escape_string($Username));
  $Result = mysql_query($SQL);
  if(mysql_num_rows($Result) < 1){
    print "<p>No such user</p>\n";
    return;
  }
  
  printf("<h2>%d places rendered by %s</h2>",
    mysql_num_rows($Result),
    htmlentities($Username));
    
  print "<ul>";
  while($Data = mysql_fetch_assoc($Result)){
    printf("<li><a href=\"?id=%d\">%s</a></li>\n",
      $Data["id"],
      htmlentities($Data["name"]));
  }    
  print "</ul>";
}

function ShowStats(){
  PeopleStats();
  PlaceStats();
}

function PlaceStats(){
  print "<h2>Places</h2>\n";
  print "<ul>";
  printf("<li>%d locations</li>\n", CountWhere("1"));
  printf("<li>%d with data</li>\n", CountWhere("osm_size > 0"));
  printf("<li>%d with images</li>\n", CountWhere("img_exists != 0"));
  
  print "</ul>\n";
}
function CountWhere($Query){
  $SQL = "SELECT * FROM places2 WHERE $Query;";
  $Result = mysql_query($SQL);
  return(mysql_num_rows($Result));

}
function PeopleStats(){
  $SQL = "SELECT renderer, count(*) as count FROM places2 GROUP BY renderer ORDER BY count DESC;";
  $Result = mysql_query($SQL);
  
  print "<h2>People rendering maps</h2>\n";
  print "<ul>";
  while($Data = mysql_fetch_assoc($Result)){
    if($Data["renderer"]){
    
      $Link = sprintf("?action=rendered_by&amp;user=%s", urlencode($Data["renderer"]));
      
      printf("<li><b>%s</b> rendered <a href=\"%s\">%d maps</a></li>", 
        htmlentities($Data["renderer"]), 
        $Link,
        $Data["count"]);
      }
  }
  print "</ul>\n";
}

function SearchForm($Text=""){
  print "<form action=\"./\" method=\"get\"><p>Search for ";
  printf("<input type=\"text\" name=\"q\" value=\"%s\">", htmlentities($Text));
  print "<input type=\"submit\" value=\"Search\">";
  print "</p></form>";
}
function MapOf($Lat, $Long, $DLat, $DLong,$Width, $Height, $Label="Untitled")
{
  $Query = sprintf("Lat=%f&Long=%f&dLat=%d&dLong=%f&width=%d&height=%d",
    $Lat, $Long, $DLat, $DLong, $Width, $Height);
    
  $Image = "map.php?$Query";
  printf("<p><img src=\"%s\" width=\"%d\" height=\"%d\" border=\"1\" alt=\"%s\"></p>\n",
    $Image,
    $Width,
    $Height,
    htmlentities($Label));
}

function RandomOSM($Stopped){
  $Version = 2;
  header("Content-type: text/plain");
  
  if($Stopped){
    printf("%d|-1||",$Version);
    exit;
    }
  
  $SQL = "select * from `places2` where `osm_size` >0 and `img_exists` = 0 order by rand( ) limit 1;";
  $Result = mysql_query($SQL);
  if(mysql_num_rows($Result) == 0){
    printf("%d|-1||",$Version);
    exit;
    }
  $Data = mysql_fetch_assoc($Result);
  $ID = $Data["id"];

  printf("%d|%d|%d|%s%s",
    $Version,
    $ID,  
    500, // width
    ThisDir(),
    OsmFilename($ID));
  exit;

}
function ThisDir(){
  return("http://almien.co.uk/OSM/Places/");
}
function UpdateOSM(){
  print "<p>Updating data...done</p>\n";
  $SQL = sprintf("select * from places2 order by name;");
  $Result = mysql_query($SQL);
  while($Data = mysql_fetch_assoc($Result)){
    $ID = $Data["id"];
    $Filename = OsmFilename($ID);
    if(file_exists($Filename)){
      $Date = filemtime($Filename);
      $Size = filesize($Filename) / 1024;
      $Age = (time() - $Date) / 86400;
      $SQL = sprintf("update places2 set `osm_date`=%d, `osm_size`=%1.2f where `id`=%d;",
        $Date / 86400,
        $Size,
        $ID);
     mysql_query($SQL);
    }
    
    $ImageExists = file_exists(ImgFilename($ID));
    $SQL = sprintf("update places2 set `img_exists`=%d where `id`=%d;",
      $ImageExists,
      $ID);
    mysql_query($SQL);
  }
}

function DataLink($ID){
  $Filename = OsmFilename($ID);
  if(!file_exists($Filename))
    return("Data not yet copied from OSM");
    
  $Size = filesize($Filename);
  
  return(sprintf("<a href=\"%s\">XML data</a> (%1.0fK)",
    htmlentities($Filename),
    $Size / 1024));
}
function OsmFilename($ID){
  return(sprintf("Data/%d.osm.gz", $ID));
}

function ImgFilename($ID){
  return(sprintf("Maps/%d.png", $ID));
}

function DownloadData($ID){
  list($Lat, $Long) = LatLong($ID);
  $Filename = sprintf("Data/%d.osm", $ID);
  printf("<p>Downloading place #%d at %f, %f</p>", $ID, $Lat, $Long);
  
  DownloadMap($Lat, $Long, 0.05, $Filename);
  system("gzip $Filename");
  
  printf("<p>Done, %d bytes</p>", filesize("$Filename.gz"));
}
function RandomUpdate(){
  $SQL = "select * from `places2` where `osm_size` =0 order by rand( ) limit 1;";
  $Result = mysql_query($SQL);
  $Data = mysql_fetch_assoc($Result);
  printf("<p>Downloading OSM data for %s</p>", htmlentities($Data["name"]));
  DownloadData($Data["id"]);
  UpdateOSM();
}
function LatLong($ID){
  $SQL = sprintf("select * from places2 where id=%d;", $ID);
  $Result = mysql_query($SQL);
  $Data = mysql_fetch_assoc($Result);
  return(array($Data["lat"], $Data["lon"]));
}

function DownloadMap($Lat, $Long, $Size, $Filename){
  $Credentials = osmPassword();
  $URL = sprintf("http://%s@www.openstreetmap.org/api/0.3/map?bbox=%f,%f,%f,%f",
    osmPassword(),
    $Long - $Size,
    $Lat - $Size,
    $Long + $Size,
    $Lat + $Size);
  $fpIn = fopen($URL, "r");
  if(!$fpIn){
    printf("<p>Couldn't download (%f,%f,%f)</p>", $Lat, $Long, $Size);
    return;
    }
  $fpOut = fopen($Filename, "w");
  if(!$fpOut){
    printf("<p>Couldn't save to %s</p>", htmlentities($Filename));
    return;
  }
  while($Data = fgets($fpIn, 1000)){
    fputs($fpOut, $Data);
  }
  fclose($fpIn);
  fclose($fpOut);
}

function ShowPlace($ID){
  $SQL = sprintf("select * from places2 where id=%d;", $ID);
  $Result = mysql_query($SQL);
  $Data = mysql_fetch_assoc($Result);
    
  printf("<h2><a href=\"./?id=%d\">%s</a></h2>\n", $Data["id"], htmlentities($Data["name"]));
  
  printf("<p><img src=\"%s\"></p>\n", ImgFilename($ID));
  
  print "<ul>";
  printf("<li>Position %f, %f</li>\n", 
    $Data["lat"],
    $Data["lon"]);
  printf("<li>type = %s</li>\n", htmlentities($Data["type"]));
  printf("<li>%s</li>\n", DataLink($ID));
  if($Data["renderer"])
    printf("<li>Rendered by %s</li>\n", htmlentities($Data["renderer"]));
  print "</ul>";
 
  print "<form action=\"./\" method=\"post\">";
  print "<input type=\"hidden\" name=\"action\" value=\"download\">";   
  printf("<input type=\"hidden\" name=\"id\" value=\"%d\">", $Data["id"]); 
  printf("<input type=\"submit\" value=\"Download latest data for %s\">", htmlentities($Data["name"]));
  print "</form>";
 
}
function ApiLink($Lat, $Long){  
  $URL = sprintf("http://www.openstreetmap.org/api/0.3/map?bbox=%f,%f,%f,%f",
    $Long - $Size,
    $Lat - $Size,
    $Long + $Size,
    $Lat + $Size);
  return($URL);
}
function ListPlaces($Search = "", $BasicText = 0){
  $SQL = sprintf("select * from places2 order by name;");
  $Result = mysql_query($SQL);
  
  if(0)
    printf( "Searching for %s</p>", htmlentities($Search));
  
  if(!$BasicText)
    print "<ul>";
    
  while($Data = mysql_fetch_assoc($Result)){
    if(SearchMatches($Search, $Data["name"])){
      if(!$BasicText){
        printf("<li><a href=\"./?id=%d\">%s</a>%s%s</li>\n", 
            $Data["id"], 
            htmlentities($Data["name"]),
            $Data["osm_size"] > 0 ? " - data available" : "",
            $Data["img_exists"] ? " - rendered" : "");
       }
       else{
        printf("%d: %s (%s %s)\n", 
            $Data["id"], 
            htmlentities($Data["name"]),
            $Data["osm_size"] > 0 ? "DATA" : "NODATA",
            $Data["img_exists"] ? "IMG" : "NOIMG");
       }
    }
  }
  if(!$BasicText)
    print "</ul>";
}

function SearchMatches($Search, $Data){
  if($Search == "")
    return(1);
  if(strpos(strtolower($Data), strtolower($Search)) !== false)
    return(1);
  return(0);
}
function PlacesForm(){
  print "<p><form action=\"./\" method=\"post\">";
  print "Lat: <input type=\"text\" name=\"lat\">";
  print "Long: <input type=\"text\" name=\"long\">";
  print "<input type=\"hidden\" name=\"action\" value=\"places\">";
  print "<input type=\"submit\" value=\"get places from gagravarr\">";
  print "</form></p>\n";
}

function ImportPlaces($Lat, $Long){
  $URL = PlacesURL($Lat, $Long);
  $Data = GetURL($URL);
  print "<ul>";
  ProcessPlaces($Data);
  print "</ul>";
}

function ProcessPlaces($Data){
  preg_match_all("/<place(.*?)>(.*?)<\/place>/", $Data, $Places, PREG_SET_ORDER);
  $Count = 0;
  foreach($Places as $Place){

    preg_match_all("/(\w+)=\'(.*?)\'/", $Place[1], $FieldXML, PREG_SET_ORDER);
    $Fields = array();
    $Fields["name"] = $Place[2];
    foreach($FieldXML as $Field){
      $Fields[$Field[1]] = $Field[2];
    }

  AddPlaceToDb(
    $Fields["name"], 
    $Fields["type"],
    $Fields["latitude"], 
    $Fields["longitude"]);

  } 
  printf("<p><b>%d places</b></p>\n", $Count);
}

function AddPlaceToDb($Name, $Type, $Lat, $Long){
  $SQL = sprintf("select * from places2 where `name`='%s';", 
    mysql_escape_string($Name));
  $Result = mysql_query($SQL);
  if(mysql_num_rows($Result) > 0)
    {
    return;
    }
    
  $SQL = sprintf("insert into places2 (`lat`,`lon`,`name`,`type`) values (%f,%f,'%s','%s');", 
    $Lat, 
    $Long, 
    mysql_escape_string($Name),
    mysql_escape_string($Type));
  $Result = mysql_query($SQL);
  printf("<li>Added %s</li\n", htmlentities($Name));
}
function GetURL($URL){
  $fp = fopen($URL, "r");
  $Data = "";
  while($X = fgets($fp, 4096)){
    $Data .= $X;
  }
  fclose($fp);
  return($Data);
}

function PlacesURL($Lat, $Long){
  return(sprintf("http://gagravarr.org/cgi-bin/where_am_i.py?lat=%f&long=%f&dist=500000&places=on&format=xml&node_type=&node_value=",
$Lat, $Long));
}
 document_footer(); ?>
