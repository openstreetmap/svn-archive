<?php
/*
This script is released into public domain. You can do whatever you want with it.

You don't have to say that this script originated from me, but I'd feel honored
when you do so on a voluntary basis.

regards, Sven
aka fröstel

*/
?>

<html>
 <head>
  <title>OSM relation-browser</title>
  <meta http-equiv="content-type" content="text/html; charset=UTF-8">
  
  <style type="text/css">
   body
   {
   font: 9pt Verdana;
   }
   
   img.icon
   {
   border-style:none;
   vertical-align:middle;
   }
   
   div.parents
   {
   margin-left:110px;
   }
   
   div.element
   {
   font-weight:bold;
   font-size:10pt;
   padding:4px;
   margin:4px;
   max-width:600px;
   background-color:#EEEEEE;
   }
   
   div.tags
   {
   margin-left:20px;
   padding-left:4px;
   }
   
   td.key, td.value
   {
   font-weight:normal;
   font-size:8pt;
   background-color:#FFFFFF;
   vertical-align:top;
   padding:0px 2px;
   }
  
  </style>
  
  <script type="text/javascript">
   
   window.onload = function()
   {
   	if(document.cookie)
   	{
		var a = document.cookie;
		var cookiename = a.substr(0,a.search('='));
		var cookiewert = a.substr(a.search('=')+1,a.search(';'));
		if(cookiewert == '')
		{
			cookiewert = a.substr(a.search('=')+1,a.length);
		}
		
		if(cookiename == 'hide_tags')
		{
			var fields = document.getElementsByName(name).length;
			for(var i = 0 ; i < fields; i++) 
			{
				document.getElementsByName(name)[i].style.display='none';
			}
			alert(fields + ' eingeklappt!');
		}
	}
   }
   
   function show(name)
   {
   	var fields = document.getElementsByName(name).length;
	for(var i = 0 ; i < fields; i++) 
   	{
   		document.getElementsByName(name)[i].style.display='block';
   	}
	
	var a = new Date();
	a = new Date(a.getTime() +1000*60*60*24*31);
	document.cookie = 'hide_tags=false; expires='+a.toGMTString()+';';
   }
   
   function hide(name)
   {
   	var fields = document.getElementsByName(name).length;
	for(var i = 0 ; i < fields; i++) 
   	{
   		document.getElementsByName(name)[i].style.display='none';
   	}
	
	var a = new Date();
	a = new Date(a.getTime() +1000*60*60*24*31);
	document.cookie = 'hide_tags=true; expires='+a.toGMTString()+';';
	alert(document.cookie);
   }
  </script>
  
 </head>
 
 <body>
 
 <a href="javascript:hide('tags');">hide tags</a>
 |
 <a href="javascript:show('tags');">show tags</a>

<?php

// When a special object was requested, otherwise start with world-relation
if(array_key_exists('type',$_GET) AND array_key_exists('id',$_GET))
{
	$elementID = $_GET['id'];
	$elementType = $_GET['type'];
}
else
{
	$elementID = 7889;
	$elementType = 'relation';
}

// Assembling the API-query
if($elementType == 'node')
	$file = "http://api.openstreetmap.org/api/0.5/$elementType/$elementID";
else
	$file = "http://api.openstreetmap.org/api/0.5/$elementType/$elementID/full";

// The API-Query to ask for parent-realtions when existent
$file2 = "http://api.openstreetmap.org/api/0.5/$elementType/$elementID/relations";

// Initialising the arrays für all elements
$relations = array();
$ways = array();
$nodes = array();
$element = array();

// This function will output the given element nicely formatted with all it's tags
function draw_element($type, $id)
{
	global $elementType;
	global $elementID;
	global $relations;
	$tags = element::getTags($type,$id);
	
	echo "<div class=element>
	       <a href=$_SERVER[PHP_SELF]?type=$type&id=$id>
	       <img src=./images/$type.png class=icon></a> ";
	
	if(array_key_exists('name',$tags))
		echo  "$tags[name] ";
	else
		echo  "$id ";
	
	if($elementType == 'relation')
		if(array_key_exists($id,$relations[$elementID]->roles))
			echo " <i>(" . $relations[$elementID]->roles[$id] . ")</i>";
	if($type == 'node')
	{
		node::drawLink($id);
		echo "<img src=./images/loupe.png class=icon></a>";
	}
	
	echo  "<div class='tags' name='tags' style='display:block'><table width=100%>";
	
	ksort($tags);
	
	foreach($tags as $key => $value)
		echo "<tr><td class=key>$key</td>
		      <td class=value width=100%>" . wordwrap($value, 70, "<br />\n", TRUE). "</td></tr>";
	
	echo '</table></div></div>';
}

class element
{
	var $id;
	var $tags = array();
	var $nodes = array();
	var $ways = array();
	var $relations = array();
	var $roles = array();
	static $now;
	
	// Constructor, sets the $id of the object to the given value
	function element($eid)
	{
		$this->id = $eid;
		element::$now = $this;
	}
	
	// Adds a key-value-pair to the $tags-array
	static function addTag($key, $value)
	{
		element::$now->tags[$key] = $value;
	}
	
	// returns the $tags-array of the given element
	static function getTags($type,$id)
	{
		switch ($type)
		{
		case 'node':
			global $nodes;
			return $nodes[$id]->tags;
			break;
		case 'way':
			global $ways;
			return $ways[$id]->tags;
			break;
		case 'relation':
			global $relations;
			return $relations[$id]->tags;
			break;
		}
	}
	
	// Adds the given element to the relation referenced by $now
	static function addElement($type, $eid, $role=FALSE)
	{
		switch ($type)
		{
		case 'node':
			element::$now->nodes[] = $eid;
			break;
		case 'way':
			element::$now->ways[] = $eid;
			break;
		case 'relation':
			element::$now->relations[] = $eid;
			/*if($eid == element::$root->id)
			{
				element::$root = element::$now;
				//echo 'Root auf ' . element::$root->id . ' gesetzt<br>';
			}*/
			break;
		}
		
		if($role)
			element::$now->roles[$eid] = $role;
		
		//echo 'Element ' . $eid . ' in ' . element::$now->id . ' eingefügt<br>';
	}
}

class node extends element
{
	var $lat;
	var $lon;
	
	// Constructor, also setting the coordinates
	function node($eid, $lat, $lon)
	{
		element::element($eid);
		$this->lat = $lat;
		$this->lon = $lon;
		// echo "Node $this->id angelegt ";
	}
	
	// Outputs a link to the relevant map-location
	static function drawLink($eid)
	{
		global $nodes;
		$lat = $nodes[$eid]->lat;
		$lon = $nodes[$eid]->lon;
		echo "<a href=http://openstreetmap.org/?mlat=$lat&mlon=$lon&zoom=17 target=_blank>";
	}
}
class way extends element
{
	
}
class relation extends element
{
	
}

// What to do when entering a new XML-element
function startElement($parser, $name, $attrs) 
{
	global $nodes;
	global $ways;
	global $relations;
	global $now;
	
	switch ($name)
	{
	
	case 'TAG':
		// it's a tag, the key-value-pair will be added to the $now-active element
		element::addTag($attrs['K'],$attrs['V']);
		break;
	
	case 'MEMBER':
		// obviously a relation is $now-active, this member will be added
		element::addElement($attrs['TYPE'],$attrs['REF'],$attrs['ROLE']);
		break;
	
	case 'ND':
		// this node will be added to the $now-active way.
		element::addElement('node',$attrs['REF']);
		break;
		
	case 'NODE':
		// a new node which will be created
		$nodes[$attrs['ID']] = new node($attrs['ID'],$attrs['LAT'],$attrs['LON']);
		break;
		
	case 'WAY':
		// a new way which will be created
		$ways[$attrs['ID']] = new way($attrs['ID']);
		break;
		
	case 'RELATION':
		// a new relation which will be created
		$relations[$attrs['ID']] = new relation($attrs['ID'],'relation');
		break;
	}	
}

// What to do when an element is closed
function endElement($parser, $name) 
{
	global $now;
	unset($now);
}

// Reading the main data from API-Output
$xml_parser = xml_parser_create();
xml_set_element_handler($xml_parser, "startElement", "endElement");

// Error-handling
if (!($fp = fopen($file, "r")))
	die("could not open XML input");

while ($data = fread($fp, 4096)) 
	if (!xml_parse($xml_parser, $data, feof($fp)))
		die(sprintf("XML error: %s at line %d",
			    xml_error_string(xml_get_error_code($xml_parser)),
			    xml_get_current_line_number($xml_parser)));

xml_parser_free($xml_parser);
fclose($fp);

// Reading the parents from API-Output
$xml_parser = xml_parser_create();
xml_set_element_handler($xml_parser, "startElement", "endElement");

if (!($fp = fopen($file2, "r"))) {
    die("could not open XML input");
}

while ($data = fread($fp, 4096)) 
{
	if (!xml_parse($xml_parser, $data, feof($fp)))
	{
	die(sprintf("XML error: %s at line %d",
		xml_error_string(xml_get_error_code($xml_parser)),
		xml_get_current_line_number($xml_parser)));
	}
}
xml_parser_free($xml_parser);
fclose($fp);


/*** GENERATING OUTPUT ****/

// Drawing parents
echo "
      <div class=parents>";

foreach($relations as $parent)
	if(in_array($elementID,$parent->relations)
	   OR in_array($elementID,$parent->ways)
	   OR in_array($elementID,$parent->nodes))
		draw_element('relation',$parent->id);

echo "</div><p style='clear:both;'> </p>";

// Drawing requested element
draw_element($elementType,$elementID);

// Drawing children
echo "<div style='padding-left:24px; clear:both;'>";

switch ($elementType)
{
case 'way':
	foreach($ways[$elementID]->nodes as $id)
	{
		draw_element('node',$id);
	}
	break;
case 'relation':
	foreach($relations[$elementID]->relations as $id)
	{
		draw_element('relation',$id);
	}
	foreach($relations[$elementID]->ways as $id)
	{
		draw_element('way',$id);
	}
	foreach($relations[$elementID]->nodes as $id)
	{
		draw_element('node',$id);
	}
	break;
}

echo '</div>';

?>


</body>
</html>