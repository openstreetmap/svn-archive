<?php header('Content-type:text/HTML; charset="UTF-8"'); ?>
<html><head><title>Slippy-map for GPX tracklogs</title>
</head>
<body>

<?php
switch($_GET['error'])
{
  case "nofile": headerMessage("The GPX file you selected couldn't be downloaded from openstreetmap.org/traces");break;
  case "nopoints": headerMessage("The GPX file you selected doesn't seem to contain any data (this may be a parser problem - report it if you think this is incorrect)");break;
}
function headerMessage($Message)
{
  print "<p style='background-color:#EFE;border:1px dashed green;padding:8px'>$Message</p>";
}
?>
<h1>Slippy-map for GPX tracklogs</h1>

<p>This page lets you view public GPX traces from
<a href="http://openstreetmap.org/traces">http://openstreetmap.org/traces</a>
on a slippy-map</p>

<p>Just enter the ID number of the trace you want to view, and you will be
taken to a map with your trace overlaid on the map</p>

<p><i>You can't upload GPX files here</i> - to do that, login to
<a href="http://openstreetmap.org/">openstreetmap.org</a> and go to
'traces' &rarr; 'view your own traces' and use the upload form.</p> 

<p>Don't forget to mark your traces as <b>public</b> if you want to
use this tool to view them.  We can't download private ones!</p>

<h2>If you know the ID of your trace</h2>

<p><form action="./" method="get">
<input type="text" name="gpx" size="10" />
<input type="submit" value="View tracklog" />
</form></p>

<h2>Recent tracklogs:</h2>

<?php

# Download the latest RSS of tracklogs from OSM
$Filename = getRss();

# Parse the RSS as XML
$xml_parser = xml_parser_create();
xml_set_element_handler($xml_parser, "startElement", "endElement");
xml_set_character_data_handler($xml_parser, "xmlText");

if (!($fp = fopen($Filename, "r"))) {
    die("could not open XML input");
}

$fields = array();
$inItem = 0;
$field = '';
while ($data = fread($fp, 4096)) {
  if (!xml_parse($xml_parser, $data, feof($fp))) {
      die(sprintf("<p>XML error: %s at line %d</p>",
                  xml_error_string(xml_get_error_code($xml_parser)),
                  xml_get_current_line_number($xml_parser)));
  }
}
xml_parser_free($xml_parser);

if($_GET["print_xml"])
  print "<pre>".nl2br(htmlentities(file_get_contents($Filename)))."</pre>";

function startElement($parser, $tag, $attributes) 
{
  global $fields, $field, $inItem;
  if(strtolower($tag) == 'item')
  {
    $fields = array();
    $inItem = 1;
  }
  elseif($inItem)
  {
    $field = strtolower($tag);
  }
}

function xmlText($parser, $text) 
{
  global $fields, $field, $inItem;
  if($inItem && $field != "")
    $fields[$field] .= $text;
}

function endElement($parser, $tag) 
{
  global $fields, $field, $inItem;
  if(strtolower($tag) == 'item')
  {
    $inItem = 0;
    
    if(preg_match("{traces/(\d+)}", $fields['link'], $Matches))
    {
      $GPX = $Matches[1]+0;
      
      if(array_key_exists('geo:lat', $fields))
      {
        $URL = sprintf("./?gpx=%d&lat=%f&lon=%f&zoom=12",
          $GPX,
          $fields['geo:lat'],
          $fields['geo:long']);
      }
      else
      {
        $URL = sprintf("./?gpx=%d",$GPX);
      }
      
      $User = sprintf("<a href='http://openstreetmap.org/user/%s'>%s</a>",
        urlencode($fields['author']),
        htmlentities($fields['author'], ENT_COMPAT, "UTF-8"));
        
    printf("<p><a href='%s'>%d</a>: %s by %s</p>\n",
      $URL,
      $GPX,
      htmlentities($fields['title'], ENT_COMPAT, "UTF-8"),
      $User
      );

    }
  }
  $field = "";
}


function getRss()
{
  $Filename = "cache/rss";
  $URL = "http://openstreetmap.org/traces/rss";
  $StaleTime = 0.5; // hours
  
  printf("<p>Updated every %1.0f hours</p>", $StaleTime);
  
  if(file_exists($Filename))
    {
    
    $Age = (time() - filemtime($Filename)) / 3600.0;
    #printf("<p>RSS age: %1.1f hr</p>", $Age);
    
    if($Age < 1.0)  # age at which to download new RSS
      return($Filename);
    }
    
  #print "<p>Downloading RSS!</p>";
  file_put_contents($Filename, file_get_contents($URL));
  return($Filename);
}
?>

<h2>Copying</h2>
<p>The map data you see underneath the tracklogs is all licensed as 
Creative Commons CC-BY-SA 2.0, from OpenStreetMap contributors</p>

<p>Tracklogs themselves are also licensed as 
Creative Commons CC-BY-SA 2.0, from OpenStreetMap contributors</p>

<p>This website is available for download on the <a href=http://svn.openstreetmap.org/applications/rendering/gpx_slippy_map/">SVN repositary</a>.</p>

<h2>Help</h2>

<p>See the wiki page for <a href="http://wiki.openstreetmap.org/index.php/GPX_slippy_map">GPX_slippy_map</a></p>

<p>Use the talk page to report any problems</p>

</body></html>