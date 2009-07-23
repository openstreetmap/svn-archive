<?php
include_once("map.php.inc");
include_once("imagemap_projection.php");
include_once("limits.php.inc");

# Standard fields
$Fields = array(
  "lat"=>array(
      'name'=>"Latitude", 
      'type'=>'numeric', 
      'default'=>45,  
      'min'=> -90, 
      'max'=> 90),
  "lon"=>array(
      'name'=>"Longitude", 
      'type'=>'numeric',  
      'default'=>0,  
      'min'=> -180, 
      'max'=> 180),
  "z"=>array(
      'name'=>"Zoom", 
      'type'=>'numeric', 
      'default'=> 1,  
      'min'=> 1, 
      'max'=> 18),
  "w"=>array(
      'name'=>"Width, px", 
      'type'=>'numeric', 
      'default'=> 600,  
      'min'=> 40, 
      'max'=> 2000),
  "h"=>array(
      'name'=>"Height, px", 
      'type'=>'numeric',  
      'default'=> 450,  
      'min'=> 30, 
      'max'=> 2000),
  "layer"=>array(
      'name'=>"Base map", 
      'type'=>'option',  
      'options'=>array_keys(getLayers())),
  "fmt"=>array(
      'name'=>"Image format", 
      'type'=>'option',  
      'options'=>array('jpg','png')),
  "filter"=>array(
      'name'=>"Filter for base-image", 
      'type'=>'option',  
      'options'=>array('none','grey','lightgrey','darkgrey','invert','bright','dark','verydark')),
  "lang"=>array(
      'name'=>"Language", 
      'type'=>'option', 
       'options'=>array('en')),
  "mode"=>array(
      'name'=>"Edit mode", 
      'type'=>'tab',  
      'options'=>array(
	  'Location', 
	  'Resize', 
	  'Style', 
	  'Add icon', 
	  'Draw', 
	  'Overlays',
	  'Export', 
	  'Community', 
	  'Report error', 
	  #'Debug',
	  'API')),
  "gpx"=>array(
      'name'=>"GPX trace", 
      'type'=>'numeric',  
      'default'=> -1,  
      'min'=> -1, 
      'max'=> 1E10),
  "rel"=>array(
      'name'=>"Relation", 
      'type'=>'numeric',  
      'default'=> -1,  
      'min'=> -1, 
      'max'=> 1E10),
  "show_icon_list"=>array(
      'name'=>"Show choice of icons", 
      'type'=>'numeric', 
      'default'=> 0,  
      'min'=> 0, 
      'max'=> 1),
  "zoom_to_clicks"=>array(
      'name'=>"Zoom to mouse position when recentering map", 
      'type'=>'option', 
      'options'=> array('on', 'off')),
  "att"=>array(
      'name'=>"Attribution", 
      'type'=>'option',  
      'options'=>array(
	  'text', 
	  'logo', 
	  'none'))
  );

//----------------------------------------------------------------------------
// Generate the fields used to specify and edit map-markers
//----------------------------------------------------------------------------
$MaxIcons = MaxIcons();;
for($i = 0; $i < $MaxIcons; $i++)
{
  $Fields["mlat$i"] = array(
      'name'=>"Marker $i latitude", 
      'type'=>'numeric',  
      'default'=> 0,  
      'min'=> -90, 
      'max'=> 90);
  $Fields["mlon$i"] = array(
      'name'=>"Marker $i longitude", 
      'type'=>'numeric',  
      'default'=> 0,  
      'min'=> -180, 
      'max'=> 180);
  $Fields["mico$i"] = array(
      'name'=>"Marker $i icon (see list in <a href='symbols/'>symbols</a> directory)", 
      'type'=>'numeric',  
      'default'=> 0,  
      'min'=> 0, 
      'max'=> 65535);
}
$Fields["choose_marker_icon"] = array(
      'name'=>"Which marker's icon to modify", 
      'type'=>'numeric', 
      'default'=> 0,  
      'min'=> 0, 
      'max'=> $MaxIcons);


//----------------------------------------------------------------------------
// Generate the fields used to specify and edit lines and polygons
//----------------------------------------------------------------------------
$MaxDrawings = MaxDrawings();
$MaxPoints = MaxPoints(); // per drawing
for($i = 0; $i < $MaxDrawings; $i++)
  {
  $Fields["d${i}_style"] = array(
      'name'=>"Style of drawing $i", 
      'type'=>'option',  
      'options'=> array('line','polygon'));

  $Fields["d${i}_colour"] = array(
      'name'=>"Colour of drawing $i", 
      'type'=>'colour',  
      'default'=> "008");

  for($j = 0; $j < $MaxPoints; $j++)
    {
    $Fields["d${i}p${j}lat"] = array(
	'name'=>"Latitude of point $j in drawing $i", 
	'type'=>'numeric',  
	'default'=> 0,  
	'min'=> -90, 
	'max'=> 90);
    $Fields["d${i}p${j}lon"] = array(
	'name'=>"Longitude of point $j in drawing $i", 
	'type'=>'numeric',  
	'default'=> 0,  
	'min'=> -180, 
	'max'=> 180);
    }
  }
$Fields["d_num"] = array(
      'name'=>"Which drawing to modify", 
      'type'=>'numeric', 
      'default'=> 0,  
      'min'=> 0, 
      'max'=> $MaxDrawings);

$Fields["dp_num"] = array(
      'name'=>"Which point to insert next in drawing", 
      'type'=>'numeric', 
      'default'=> 0,  
      'min'=> 0, 
      'max'=> $MaxPoints);


# Option to export the API documentation
if($_REQUEST['api'] == "json")
{
  header("content-type: text/plain"); // or could be text/x-json
  print json_encode($Fields);
  exit;
}

if(0 && $_GET["clear_cache"] == "yes") // TODO: disable in general use
{
  walkCache('del');
  walkCache('stat');
  exit;
}


# if mlat/mlon but not lat/lon supplied, then use those for map centre
if((array_key_exists('mlat', $_REQUEST) && array_key_exists('mlon', $_REQUEST)) && !(array_key_exists('lat', $_REQUEST) && array_key_exists('lon', $_REQUEST)))
{
  $_REQUEST['lat'] = $_REQUEST['mlat'];
  $_REQUEST['lon'] = $_REQUEST['mlon'];
}

# Handle 'standard' field names as used in other slippy-maps
foreach(array("zoom"=>"z", "mlat" => "mlat0", "mlon" => "mlon0") as $k => $v)
{
  if(array_key_exists($k, $_REQUEST))
    {
    $_REQUEST[$v] = $_REQUEST[$k];
    }
}


# if zoom supplied but not lat/lon, kill it!!!
if(array_key_exists('z', $_REQUEST) && 
  !(array_key_exists('lat', $_REQUEST) && array_key_exists('lon', $_REQUEST)))
{
  $_REQUEST['z'] = FieldDefault('z');
}

# Handle imagemaps
if(preg_match("{\&\?(\d+),(\d+)$}", $_SERVER['QUERY_STRING'], $Matches))
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
    case 'Location':
      {
      $Data = ReadFields($_REQUEST);
      list($_REQUEST['lat'], $_REQUEST['lon']) 
	= imagemap_xy2ll($Matches[1], $Matches[2], $Data);

      if($Data["zoom_to_clicks"] == 'on')
	$_REQUEST['z'] = $Data['z'] + 2;

      break;
      }
    case 'Draw':
      {
      $Data = ReadFields($_REQUEST);
      $FieldBase = sprintf("d%dp%d", $Data["d_num"], $Data["dp_num"]);

      list($_REQUEST[$FieldBase.'lat'], $_REQUEST[$FieldBase.'lon']) 
	= imagemap_xy2ll($Matches[1], $Matches[2], $Data);

      $_REQUEST['dp_num'] = min($Data["dp_num"]+1, $MaxPoints);
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

printf("<p style='float:right'><a href='./'>Restart</a></p>");
printf("<h1>%s</h1>\n", T(title()));

printf("<div class='everything'>\n");
printf("<div class='tabs'>\n");
foreach($Fields['mode']['options'] as $Mode)
{
  printf(" <a href='%s' class='tab%s'>%s</a>\n", 
    LinkSelf(array('mode' => $Mode)), 
    $Mode == $Data['mode'] ? '_selected' : '',
    $Mode);
}
print "</div>\n\n<div class='main'>\n\n";

switch($Data['mode'])
  {
  case "Debug":
    {
    ShowImage();

    printf("<form action='.' method='get'>");
    foreach($Fields as $Field => $Details)
    {
      printf("<p>%s:", $Details['name']);
      switch($Details['type'])
      {
	case "numeric":
	  printf("<input type='text' name='%s' value='%s'/></p>\n", 
	    $Field, 
	    htmlentities($Data[$Field]));
	  break;
	case 'option':
	  printf("<select name='%s'>\n", $Field);
	  foreach($Details['options'] as $Option)
	  {
	    printf(" <option%s>%s</option>\n", $Data[$Field]==$Option ? " selected":"", $Option);
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
  case 'Location':
    {
    printf("<p>");
    printf("<a class='zoom' href='%s'>Zoom Out</a> ", LinkSelf(array('z'=>$Data['z']-1)));
    printf("<a class='zoom' href='%s'>Zoom In</a>", LinkSelf(array('z'=>$Data['z']+1)));
    printf("</p>");

    ShowImage(true);
    printf("<p>Click map to recenter (zooming to mouse click: %s)</p>", OptionList("zoom_to_clicks"));
    break;
    }
  case 'Resize':
    {
    printf("<p><a href='%s'><img src='gfx/screen_sizes.png' ismap/></a></p>", LinkSelf());

    printf("<form action='.' method='get'>");
    printf("<input type='text' name='w' value='%u' size='4' /> &times; \n", $Data['w']);
    printf("<input type='text' name='h' value='%u' size='4' />\n", $Data['h']);
    HiddenFields(array('h','w'));
    printf("<p><input type='submit' value='OK'></p>\n");
    printf("</form>");
    break;
    }
  case 'Style':
    {
    $SampleSize = 200;
    printf("<table border=0>\n");
    foreach(getLayers() as $Layer => $LayerData)
      {
      printf("<tr%s>", $Layer == $Data['layer'] ? " id='selected_style'":"");

      printf("<td><h2>%s</h2></td>", $Layer);
      printf("<td><a href='%s'><img src='%s' width='%d' height='%d'/></a></td>\n",
	LinkSelf(array('layer'=>$Layer)),
	LinkSelf(array('w'=>$SampleSize, 'h'=>$SampleSize, 'layer'=>$Layer, 'att'=>'none')). "show=1",
	$SampleSize,
	$SampleSize);
      printf("<td>");
      foreach($LayerData as $FieldName => $FieldValue)
	{
	printf("%s: %s<br/>", $FieldName, htmlentities($FieldValue));
	}
      printf("</td></tr>\n");
      }
    printf("</table>\n");

    printf("\n\n<hr>\n<h2>Filters</h2>\n<p>%s</p>\n", OptionList('filter'));

    break;
    }
  case 'Add icon':
    {
    if($Data['show_icon_list'])
      {
      iconSelector(sprintf("mico%d", $Data['choose_marker_icon']));
      }
    else
      {
      ShowImage(true);
      printf("<p>Click map to add a new marker</p>");
      $Count = 0;
      for($i = 0; $i < $MaxIcons; $i++)
	{
	if(markerInUse($i))
	  {
	  // TODO: image-align no longer in HTML spec?
	  $Icon = sprintf("<a href='%s'><img src='%s' align='middle' border='0' title='Click to change icon'/></a>",
	    LinkSelf(array("choose_marker_icon" => $i, 'show_icon_list'=>1)),
	    iconName($Data["mico$i"]));

	  printf("<p>%s marker %d: Location (%1.5f, %1.5f)  <a href='%s'>delete</a></p>\n", 
	    $Icon,
	    $i,
	    $Data["mlat$i"],
	    $Data["mlon$i"],
	    LinkSelf(array("mlat$i" => 0, "mlon$i" => 0)));
	  $Count++;
	  }
	}

      if($Count > 0)
	printf("<hr/><p>&nbsp;&nbsp;<b>&uarr;</b> <i>click icons to change them</i></p>\n");

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
      }
    break;
    }
  case 'Draw':
    {
    ShowImage(true);
    printf("<p>Click image to add point %d to drawing %d</p><hr/>\n", $Data["dp_num"], $Data["d_num"]);
    
    for($i = 0; $i < $MaxDrawings; $i++)
      {
      $Html = "";
      $Count = 0;
      $DelAll = array();
      for($j = 0; $j < $MaxPoints; $j++)
	{
	$FieldLat = "d${i}p${j}lat";
	$FieldLon = "d${i}p${j}lon";

	$Lat = $Data[$FieldLat];
	$Lon = $Data[$FieldLon];
	if($Lat != 0 && $Lon != 0)
	  {
	  $Html .= sprintf("<p>%d: %f, %f</p>\n", $j, $Lat, $Lon);
	  $Count++;
	  }
	$DelAll[$FieldLat] = 0;
	$DelAll[$FieldLon] = 0;
	}
      if($Count)
	{
	printf("<h2>Drawing %d: (<a href='%s'>delete</a>)</h2>\n", $i, LinkSelf($DelAll));

	printf("<p>Style: %s</p>", OptionList("d${i}_style"));
	printf("%s", ColourChooser("d${i}_colour"));

	printf("%s\n", $Html);
	}
      else
	{
	printf("<h2>Drawing %d:</h2>\n<p><a href='%s'>Start</a></p>\n", $i, LinkSelf(array('d_num'=>$i, 'dp_num'=>0)));
	}
      }


    break;
    }
  case 'Overlays':
    {
    ShowImage(false);

    printf("<h2>GPS tracklogs</h2>\n<form action='.' method='get'>");
    printf("<p>GPX trace ID: <input type='text' name='gpx' value='%d' size='6' />", $Data['gpx']);
    HiddenFields(array('gpx'));
    printf("<input type='submit' value='OK'></p>\n</form>\n");
    printf("<p class='note'>This must be the ID of an openstreetmap public GPX trace. <a href='%s'>Upload files here</a> (account required)</p>\n",
      "http://www.openstreetmap.org/traces/mine");

    printf("<h2>Routes</h2>\n<form action='.' method='get'>");
    printf("<p>Route ID: <input type='text' name='rel' value='%d' size='6' />", $Data['rel']);
    HiddenFields(array('rel'));
    printf("<input type='submit' value='OK'></p>\n</form>\n");
    printf("<p class='note'>This must be the ID of an openstreetmap <a href='%s'>route relation</a>, e.g. a bus route, train line, or cycle route</p>\n",
      "http://wiki.openstreetmap.org/wiki/Relation:route");

    printf("<h2>Attribution</h2>\n<p>%s</p>\n", OptionList('att'));
    print("<p class='note'>If you select none, then attribution should be provided elsewhere, e.g. in the website or document which includes the image</p>\n");

    break;
    }
  case 'Export':
    {
    printf("<p><a href='http://tinyurl.com/create.php?url=%s'>Make a TinyURL</a> to your map</p>",
      htmlentities(urlencode(FullImageURL())));

    ShowImage();
    printf("<p>Right-click the image and &quot;save as&quot; to get an image that you can use in any document</p>"); // TODO: different instructions depending on user-agent?


    printf("<h2>HTML code</h2><p>(paste this to your website)</p>");

    $AltText = sprintf("OpenStreetMap (%s) map of the area around %1.5f, %1.5f",
      $Data['layer'],
      $Data['lat'],
      $Data['lon']);

    $Html = sprintf("<img src=\"%s\" width=\"%d\" height=\"%d\" alt=\"%s\" />", 
      FullImageURL(),
      $Data['w'],
      $Data['h'],
      $AltText);

    printf("<form><textarea rows='10' cols='80'>%s</textarea></form>", htmlentities($Html));

    printf("<h2>Image URL</h2><p>(paste this anywhere that you have to enter an image URL)</p>");
    printf("<form><textarea rows='7' cols='80'>%s</textarea></form>", htmlentities(FullImageURL()));
    printf("<p>(right-clicking on the image above and selecting <i>'copy image location'</i> should also give you the same URL)</p>");

    break;
    }
  case 'Community':
    {
    printf("<h2>Share this map</h2>\n");
    printf("<p><a href='http://delicious.com/search?p=osm_static_maps'>Browse other people's maps</a></p>");

    printf("<p><a href='http://delicious.com/save?url=%s'>Share this map on Del.icio.us</a> (account required) and give it a tag of <i>osm_static_maps</i></p>", 
      htmlentities(urlencode(FullImageURL())));

    printf("<p>Use this map to <a href='%s'>illustrate a wikipedia article</a> <i>(link goes to wikiproject page, not an upload form)</i></p>",
      'http://en.wikipedia.org/wiki/Wikipedia:WikiProject_Maps');

    printf("<p>Make a <a href='http://tinyurl.com/create.php?url=%s'>TinyURL</a></p>",
      htmlentities(urlencode(FullImageURL())));

    printf("<h2>Edit the maps</h2>\n<p>Get involved with <a href='%s'>editing OpenStreetmap</a></p>\n", 
      "http://wiki.openstreetmap.org/wiki/Beginners%27_Guide");

    break;
    }
  case 'Report error':
    {
    $URL = sprintf("http://openstreetbugs.appspot.com/?lat=%f&lon=%f&z=14", $Data['lat'], $Data['lon']);
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
  case 'API':
    {
    printf("<h2>API for accessing these maps</h2>\n");
    printf("<p>All aspects of this site are available through HTTP GET requests.  The fields are described below:</p>");
    ##printf("<p><i>Many of these fields are generated</i></p>");
    printf("<p>Some of these fields are for navigating the website, and would not typically be used when requesting an image (e.g. show_icon_list or zoom_to_clicks)</p>");
    printf("<p>Be sure to include show=1 to get the image instead of this website!</p>");

    printf("<p><a href='./?api=json'>Get this API as JSON</a></p>");

    printf("<hr/>\n<div class='api'>");

    printf("<p><b>show</b> (Returns the image rather than this web interface)</p><ul><li>0 = view image-editing tools</li><li>1 = view as image</li></ul>");
    foreach($Fields as $Field => $Details)
      {
      printf("<p><b>%s</b> (%s): ", $Field, $Details['name']);
      switch($Details['type'])
	{
	case "numeric":
	  printf("numeric (%1.2f to %1.2f)\n", $Details['min'], $Details['max']);
	  break;
	case 'option':
	  printf("</p><ul>\n");
	  foreach($Details['options'] as $Option)
	    {
	    printf("<li>%s</li>\n", $Option);
	    }
	  printf("</ul>\n");
	  break;
	case 'tab':
	  print "one of the tab names</p>\n";
          break;
	case 'colour':
	  print "in 3-character hexadecimal RGB format, from 000 = black to F00 = red to FFF = white</p>\n";
	default:
	  print "</p>\n";
	}
      }
    printf("<p><b>&?123,456</b> (imagemap coordinates, must be at end of query string): handles actions caused by clicking on a server-side imagemap.  The action taken depends on <b>mode=</b> (e.g. <i>mode=Location</i> causes the map to be centred on this pixel, and possibly zoomed-in if <i>zoom_to_clicks=on</i>)</p>");
    printf("</div>"); // api
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
  return("<hr><p>Map data <a href='$OsmLicense'>CC-BY-SA 2.0</a>. Main site: <a href=http://'$URL'>$URL</a><br><span class='help_footer'>Use your browser's <i>back</i> button to undo mistakes.  Bookmark the page to save your map.</span></p>");
}
function LinkSelf($Changes = array(), $Base = "./")
{
  global $Data;
  global $Fields;
  $NewData = $Data;
  foreach($Changes as $k => $v)
    {
    $NewData[$k] = $v;
    }
  $Query = "";
  foreach($Fields as $Field => $Details)
    {
    if($NewData[$Field] != FieldDefault($Field) || $Field == "mode")
      {
      $Query .= sprintf("%s=%s&", urlencode($Field), urlencode($NewData[$Field]));
      }
    }

  return($Base . "?" . $Query);
}
function imageUrl($Changes = array(), $Base="./")
{
  return(LinkSelf($Changes, $Base) . "show=1");
}
function FullImageUrl()
{
  return(imageUrl(array(), "http://" . $_SERVER["HTTP_HOST"] . "/~ojw/StaticMap/"));
}
function HiddenFields($Omit = array())
{
  global $Data;
  global $Fields;
  foreach($Fields as $Field => $Details)
    {
    if(!in_array($Field, $Omit))
      {
      if($Data[$Field] != FieldDefault($Field))
	{
	printf("<input type='hidden' name='%s' value='%s'/>\n", 
	  htmlentities($Field), 
	  htmlentities($Data[$Field]));
	}
      }
    }
  return("./?" . $Query);
}

function ReadFields($Req)
{
  global $Fields;
  # Interpret our standard fields
  $Data = array();
  foreach($Fields as $Field => $Details)
  {
    $Value = $Req[$Field];
    switch($Details['type'])
    {
      case "numeric":
	if($Value < $Details['min'] || $Value > $Details['max'])
	  $Value = FieldDefault($Field);
	break;
      case "tab":
      case "option":
	if(!in_array($Value, $Details['options']))
	  $Value = FieldDefault($Field);
	break;
      case "colour":
	if(!preg_match("/^[0-9A-F]{3}$/", $Value))
	  $Value = FieldDefault($Field);
	break;
      default:
	printf("<p>Unrecognised field type %s (default-deny means you need to specify what values are valid!)</p>", htmlentities($Details['type']));
	$Value = 0;
	break;
    }
    $Data[$Field] = $Value;
  }
  return($Data);
}

function FieldDefault($Field)
{
  global $Fields;
  if(array_key_exists('default', $Fields[$Field]))
    return($Fields[$Field]['default']);
  
  switch($Fields[$Field]['type'])
    {
    case "tab":
    case "option":
      return($Fields[$Field]['options'][0]);
    case "color":
      return("00F"); // what's a good default-default colour?
    }
  return(0);
}

function OptionList($Field)
{
  global $Fields;
  global $Data;
  $Html = "";
  foreach($Fields[$Field]['options'] as $Style)
    {
    $Html .= sprintf("<a %s href='%s'>%s</a> ",
      $Data[$Field] == $Style ? " class='selected_span'":"",
      LinkSelf(array($Field => $Style)),
      $Style);
    }
  return($Html);
}

function ColourChooser($Field)
{
  $Html = "<div class='colour_chooser'><table border='0'><tr><td>";
  global $Data;
  $Stops = array(0,4,8,12,15);
  foreach($Stops as $g)
    {
    foreach($Stops as $b)
      {
      foreach($Stops as $r)
	{
	$Colour = sprintf("%X%X%X", $r,$g,$b);
	$Html .= sprintf("<a class='colour' href='%s' style='background-color:#%s;'>&nbsp;</a>", 
	  LinkSelf(array($Field=>$Colour)),
	  $Colour);
	}
      }
    $Html .= "<br/>";
    }
  $Html .= sprintf("</td><td style='background-color:#%s;width:3em;'>&nbsp;", $Data[$Field]);;
  $Html .= "</td></tr></table></div>";
  return($Html);
}

function iconSelector($OutputSymbol)
{
  $SymbolDir = "symbols";
  printf("<p>Choose an image for %s<br/>\n", htmlentities($OutputSymbol));
  if($fp = opendir($SymbolDir))
    {
    while(($File = readdir($fp)) !== false)
      {
      if(preg_match("{(\d+)\.png}", $File, $Matches))
	{
	$FullFile = sprintf("%s/%s", $SymbolDir, $File);
	$IconID = $Matches[1];

	printf("<span style='symbol'><a href='%s'><img src='%s' border=0 alt='icon $IconID' title='icon $IconID' /></a></span>\n", 
	  LinkSelf(array(
	    'show_icon_list'=>0, 
	    'choose_marker_icon'=>0,
	    $OutputSymbol=>$IconID)),
	  $FullFile);
	}
      }
    
    closedir($fp);
    }
  printf("</p>");
}
function iconName($IconID)
{
  return(sprintf("symbols/%d.png", $IconID));
}

?>