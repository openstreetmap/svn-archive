<?php
include("map.php.inc");
include("imagemap_projection.php");

# Standard fields
$Fields = array(
  "lat"=>array("Latitude", 'numeric', 52, -90, 90),
  "lon"=>array("Longitude", 'numeric', -1, -180, 180),
  "z"=>array("Zoom", 'numeric', 8, 4, 18),
  "w"=>array("Width, px", 'numeric', 400, 40, 2000),
  "h"=>array("Height, px", 'numeric', 300, 30, 1500),
  "layer"=>array("Base map", 'option', array_keys(getLayers())),
  "fmt"=>array("Image format", 'option', array('jpg','png')),
  "lang"=>array("Language", 'option', array('en')),
  "mode"=>array("Edit mode", 'tab', array('Start', 'Edit', 'Recentre', 'Resize', 'Add icon', 'Draw', 'Export', 'Community', 'Report error', 'Help')),
  "gpx"=>array("GPX trace", 'numeric', -1, -1, 1E10),
  "rel"=>array("Relation", 'numeric', -1, -1, 1E10),
  "mlat"=>array("Marker latitude", 'numeric', 0, -90, 90),
  "mlon"=>array("Marker longitude", 'numeric', 0, -180, 180),
  );

$MaxIcons = 10;
for($i = 0; $i < $MaxIcons; $i++)
{
  $Fields["mlat$i"] = array("Marker $i latitude", 'numeric', 0, -90, 90);
  $Fields["mlon$i"] = array("Marker $i longitude", 'numeric', 0, -180, 180);
}

if(1 && $_GET["clear_cache"] == "yes") // TODO: disable in general use
{
  walkCache('del');
  walkCache('stat');
  exit;
}

# Handle imagemaps
if(preg_match("{&?(\d+),(\d+)$}", $_SERVER['QUERY_STRING'], $Matches))
{
  switch($_REQUEST['mode'])
    {
    case 'Resize':
      {
      $_REQUEST['w'] = $Matches[1] * 4;
      $_REQUEST['h'] = $Matches[2] * 4;
      $_REQUEST['mode'] = "Edit";
      break;
      }
    case 'Recentre':
      {
      list($_REQUEST['lat'], $_REQUEST['lon']) 
	= imagemap_xy2ll($Matches[1], $Matches[2], ReadFields($_REQUEST));
      break;
      }
    case 'Draw':
      {
      list($_REQUEST['plat'], $_REQUEST['plon']) 
	= imagemap_xy2ll($Matches[1], $Matches[2], ReadFields($_REQUEST));
      break;
      }
    case 'Add icon':
      {
      $Data = ReadFields($_REQUEST);
      list($mlat, $mlon) = imagemap_xy2ll($Matches[1], $Matches[2], $Data);

      for($i = 0; $i < $MaxIcons; $i++)
	{
	if($Data["mlat$i"] == 0 && $Data["mlon$i"] == 0)
	  {
	  $_REQUEST["mlat$i"] = $mlat;
	  $_REQUEST["mlon$i"] = $mlon;
	  break;
	  }
	}
      break;
      }
    default:
     print "Unrecognised imagemap";
    }
}

$Data = ReadFields($_REQUEST);

if($_REQUEST['show'])
{
  doMap(
    $Data['lat'],
    $Data['lon'],
    $Data['z'],
    $Data['w'],
    $Data['h'],
    $Data['layer'],
    'jpg',
    true,
    $Data);
  exit;
}

printf("<html><head><title>%s</title>\n", T(title()));
printf("<link rel='stylesheet' href='styles.php?look=default'/>");
printf("</head>\n");

printf("<h1>%s</h1>\n", T(title()));

printf("<div class='everything'>\n");
printf("<div class='tabs'>\n");
foreach($Fields['mode'][2] as $Mode)
{
  printf(" <a href='%s' class='tab%s'>%s</a>\n", 
    LinkSelf(array('mode' => $Mode)), 
    $Mode == $Data['mode'] ? '_selected' : '',
    $Mode);
}
print "</div>\n\n<div class='main'>\n\n";

switch($Data['mode'])
  {
  case "Edit":
    {
    ShowImage();

    printf("<form action='.' method='get'>");
    foreach($Fields as $Field => $Options)
    {
      printf("<p>%s:", $Options[0]);
      switch($Options[1])
      {
	case "numeric":
	  printf("<input type='text' name='%s' value='%s'/></p>\n", 
	    $Field, 
	    htmlentities($Data[$Field]));
	  break;
	case 'option':
	  printf("<select name='%s'>\n", $Field);
	  foreach($Options[2] as $Layer)
	  {
	    printf(" <option%s>%s</option>\n", $Data[$Field]==$Layer ? " selected":"", $Layer);
	  }
	  printf("</select>");
	  break;
      }
      printf("</p>\n");
    }

    printf("<p><input type='submit' value='Apply'></p>");
    printf("</form>");
    break;
    }
  case "Start":
    printf("<p>TODO: slippy-map here</p>");
  case 'Recentre':
    {
    ShowImage(true);
    printf("<p>Click map to recenter</p>");
    printf("<p>Select one of the other tabs to edit the image</p>");
    break;
    }
  case 'Resize':
    {
    printf("<p><a href='%s'><img src='gfx/screen_sizes.png' ismap/></a></p>", LinkSelf());

    printf("<form action='.' method='get'>");
    printf("<input type='text' name='h' value='%u' size='4' /> &times; \n", $Data['h']);
    printf("<input type='text' name='w' value='%u' size='4' />\n", $Data['w']);
    HiddenFields(array('h','w'));
    printf("<p><input type='submit' value='OK'></p>\n");
    printf("</form>");
    break;

    break;
    }
  case 'Add icon':
    {
    ShowImage(true);
    printf("<p>Click to add a marker</p>");
    $Count = 0;
    for($i = 0; $i < $MaxIcons; $i++)
      {
      if(markerInUse($i))
	{
	printf("<p>%d: Location (%1.5f, %1.5f) <a href='%s'>delete</a></p>\n", 
	  $i,
	  $Data["mlat$i"],
	  $Data["mlon$i"],
	  LinkSelf(array("mlat$i" => 0, "mlon$i" => 0)));
	$Count++;
	}
      }

    if($Count == $MaxIcons)
      {
      printf("<p>Reached the limit of %d markers</p>\n", $MaxIcons);
      }

    if($Count > 1)
      {
      $DelAll = array();
      for($i = 0; $i < $MaxIcons; $i++)
	{
	$DelAll["mlat$i"] = 0;
	$DelAll["mlon$i"] = 0;
	}
      printf("<p><a href='%s'>Delete all markers</a></p>\n", LinkSelf($DelAll));
      }

    break;
    }
  case 'Draw':
    {
    ShowImage(true);
    printf("<p>TODO: click to create polygons and polylines</p>");
    break;
    }
  case 'Export':
    {
    printf("<p>Your map:</p>");
    ShowImage();
    printf("<p>Right-click the image and &quot;save as&quot; to get an image that you can use in any document</p>"); // TODO: different instructions depending on user-agent?

    printf("<h2>HTML code</h2><p>(paste this to your website)</p>");

    $AltText = sprintf("OpenStreetMap (%s) map of the area around %1.5f, %1.5f",
      $Data['layer'],
      $Data['lat'],
      $Data['lon']);

    $Html = sprintf("<img src=\"%s\" width=\"%d\" height=\"%d\" alt=\"%s\" />", 
      "http://" . $_SERVER["HTTP_HOST"] . $_SERVER["REQUEST_URI"] . "show=1",
      $Data['w'],
      $Data['h'],
      $AltText);

    printf("<form><textarea rows='10' cols='80'>%s</textarea></form>", htmlentities($Html));
    break;
    }
  case 'Community':
    {
    printf("<p><a href='http://delicious.com/search?p=osm_static_maps'>Browse other people's maps</a></p>");

    // TODO; doesn't work
    printf("<p><a href='http://delicious.com/save?tags=osm_static_maps&url=%s'>Share this map on Del.icio.us</a> (account required)</p>", 
      urlencode($_SERVER["HTTP_HOST"] .  $_SERVER['REQUEST_URI'] . 'show=1'));

    break;
    }
  case 'Report error':
    {
    $URL = sprintf("http://openstreetbugs.appspot.com/?lat=%f&lon=-%f&z=14", $Data['lat'], $Data['lon']);
    printf("<h2>Report an error with the map data</h2>");
    printf("<p>This will report a map error to <a href='%s/'>OpenStreetBugs</a></p>",$URL);
    printf("<form method='post' action='http://openstreetbugs.appspot.com/addPOIexec'>\n");
    printf("<input name='lon' value='%f' type='hidden'>", $Data['lon']);
    printf("<input name='lat' value='%f' type='hidden'>", $Data['lat']);
    $HintText = 
      "Describe the error you can see on this map:\n\n\n\n\n\n" .
      "What's the source of your information?\n[ ] Local knowledge\n[ ] Other: _____________\n";
    printf("<textarea name='text' rows='10' cols='80'>%s</textarea><br>\n", $HintText);
    printf("<input value='Report map error' type='submit' style='padding:1em; font-weight:bold' /></form>");

    printf("<p>&nbsp;</p>\n<p>After sending this report, your message will be visible to other mappers, and to the general public.");

    break;
    }
  case 'Help':
    {
    printf("");
    break;
    }
  }
printf("</div><!-- main -->\n");
printf("</div><!-- everything -->\n");
printf("<div class='footer'>%s</div>\n</body>\n</html>\n", footer());

function markerInUse($i)
{
  global $Data;
  return($Data["mlat$i"] != 0 && $Data["mlon$i"] != 0);
}

function ShowImage($IsMap = false)
{
  global $Data;
  printf("<p>%s<img src='%s' width='%d' height='%d' %s/>%s</p>\n",
    $IsMap?"<a href='".LinkSelf()."'>":"",
    imageUrl(),
    $Data['w'], 
    $Data['h'],
    $IsMap?"ismap":"",
    $IsMap?"</a>":"");
}
function T($EnglishText)
{
  global $Data;
  $Lang = $Data["lang"];
  return($EnglishText);
}
function title()
{
  return("Create a map");
}
function footer()
{
  $OsmLicense = "http://creativecommons.org/licenses/by-sa/2.0/";
  $URL = "openstreetmap.org";
  return("<hr><p>Map data <a href='$OsmLicense'>CC-BY-SA 2.0</a>. Main site: <a href=http://'$URL'>$URL</a></p>");
}
function LinkSelf($Changes = array())
{
  global $Data;
  global $Fields;
  $NewData = $Data;
  foreach($Changes as $k => $v)
    {
    $NewData[$k] = $v;
    }
  $Query = "";
  foreach($Fields as $Field => $Options)
    {
    $Query .= sprintf("%s=%s&", urlencode($Field), urlencode($NewData[$Field]));
    }

  $Base = ".";

  return($Base . "/?" . $Query);
}
function imageUrl()
{
  return(LinkSelf() . "show=1");
}
function HiddenFields($Omit = array())
{
  global $Data;
  global $Fields;
  foreach($Fields as $Field => $Options)
    {
    if(!in_array($Field, $Omit))
      {
      printf("<input type='hidden' name='%s' value='%s'/>\n", htmlentities($Field), htmlentities($Data[$Field]));
      }
    }
  return("./?" . $Query);
}

function ReadFields($Req)
{
  global $Fields;
  # Interpret our standard fields
  $Data = array();
  foreach($Fields as $Field => $Options)
  {
    $Value = $Req[$Field];
    switch($Options[1])
    {
      case "numeric":
	list($Name, $Type, $Default, $Min, $Max) = $Options;
	if($Value < $Min || $Value > $Max)
	  $Value = $Default;
	break;
      case "tab":
      case "option":
	$FieldOptions = $Options[2];
	if(!in_array($Value, $FieldOptions))
	  $Value = $FieldOptions[0];
	break;

    }
    $Data[$Field] = $Value;
  }
  return($Data);
}


?>