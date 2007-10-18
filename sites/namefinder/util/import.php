<?php

/* This file takes an OSM planet file, and processes it to produce the 
   name finder index.

   It requires temporary 'node' and 'way' tables in order to retrieve
   references by id from within the planet xml file, but these large
   tables can be emptied on completion.

   node and way classes for these tables are defined inline below 
   as subclasses of 'tagged'

   The process takes many hours to run, so it is better to run it on a
   temporary database and then export the tables (other than node and
   way) and import them into the production database, which takes only 
   maybe a minute or so.  */

session_start(); // only so we get a unique log file

include_once('preamble.php');
include_once('named.php');
include_once('placeindex.php');
include_once('region.php');
include_once('canon.php');

if (empty($config['area'])) {
  $area = array('n'=>90.0, 's'=>-90.0, 'w'=>-180.0,'e'=>180.0);
} else {
  $area =& $config['area'];
}

$added['node'] = $added['placeindex'] = $added['named'] = $added['way'] =$added['canon'] = 0;

$tooclose = $config['tooclose'];

// ==================================================
class nodecache {
  /* we maintain a node cache as we create nodes and use them because we frequently 
     refer to the same node a few times in quick succession from say a way or relation */
  var $cache = array();
  var $size = 0;
  var $maxsize = 500;

  function getnode($uid) {
    if (isset($this->cache[$uid])) {
      return $this->cache[$uid];
    }
    global $db;
    $node = new node($uid, TRUE);
    if ($db->select($node) > 0) {
      $this->putnode($node, $uid);
      return $node;
    }
    return FALSE;
  }
  
  function putnode($node, $uid) {
    if ($this->size == $this->maxsize) {
      array_shift($this->cache);
    } else {
      $this->size++;
    }
    $this->cache[$uid] = clone $node;
  }
}

// ==================================================
class tagged {

  /* tagged is the superclass of node, way and relation (the latter is not 
     mapped to a database table, as we don't need to store ways).

     It represents the class of things in the planet file which have
     tags. It'smain purpose is to digest those tags picking out the
     ones that look interesting for our index.
  */

  var $id;
  var $tags;
  var $lat, $lon;

  // --------------------------------------------------
  function add_tag($key, $value) { $this->tags[$key] = $value; }

  // --------------------------------------------------
  function ok_latlon() {
    /* there was a preponderance at one time for dud objects to end up
       in mid-Atlantic, which we can safely ignore */
    return isset($this->lat) && ($this->lat !=0.0 || $this->lon != 0.0);
  }

  // --------------------------------------------------
  function append_for_canonicalisation(&$canonicalise, $category) {
    if (empty($canonicalise[0])) {
      $canonicalise[] = $category;
    } else {
      $canonicalise[] = $canonicalise[0] . ' ' . $category;
    }
  }

  // --------------------------------------------------
  function interesting_name() {

    /* This is the key indexing function. It chooses an item (node,
       way, relation) with tags sufficiently interesting to go into the
       index. This is anything with a name, certain classes of object
       which we might search for by class ("pubs near Cambridge") and
       proxy-names like road numbers ('ref') and IATA airport codes */

    if (empty($this->tags['name']) && 
        empty($this->tags['ref']) && 
        empty($this->tags['iata']) && 
        empty($this->tags['icao']) && 
        empty($this->tags['place_name']) &&
        (empty($this->tags['amenity']) || 
         ($this->tags['amenity'] != 'post_office' &&
          $this->tags['amenity'] != 'fuel' &&
          $this->tags['amenity'] != 'supermarket' &&
          $this->tags['amenity'] != 'pharmacy' &&
          $this->tags['amenity'] != 'hospital' &&
          $this->tags['amenity'] != 'police' &&
          $this->tags['amenity'] != 'fire_station' &&
          $this->tags['amenity'] != 'bus_station' &&
          $this->tags['amenity'] != 'atm' &&
          $this->tags['amenity'] != 'bank' &&
          $this->tags['amenity'] != 'place_of_worship' &&
          $this->tags['amenity'] != 'school' &&
          $this->tags['amenity'] != 'college' &&
          $this->tags['amenity'] != 'university' &&
          $this->tags['amenity'] != 'cinema' &&
          $this->tags['amenity'] != 'theatre')) &&
        (empty($this->tags['tourism']) || 
         ($this->tags['tourism'] != 'hotel' &&
          $this->tags['tourism'] != 'motel' &&
          $this->tags['tourism'] != 'hostel' &&
          $this->tags['tourism'] != 'guest_house' &&
          $this->tags['tourism'] != 'camp_site' &&
          $this->tags['tourism'] != 'caravan_site')) &&
        (empty($this->tags['historic']) ||
         ($this->tags['historic'] != 'castle' &&
          $this->tags['historic'] != 'monument' &&
          $this->tags['historic'] != 'museum' &&
          $this->tags['historic'] != 'ruins')) &&
        (empty($this->tags['railway']) ||
         ($this->tags['railway'] != 'station'))
        )
    {
      return; 
    }

    global $db, $tooclose, $added;

    if (! $this->calc_latlong()) { return; }

    /* It has interest, and it has location, so make a named object to
       consider it further. Build a name for it which is a
       composite of the ruename, and the various other
       possibilities like ref, and non-native names (we usually
       put these in square brackets) */

    $named = new named();
    $namestring = '';
    $canonicalise = array();
    if (! empty($this->tags['name'])) { 
      $namestring .= $this->tags['name']; 
      $canonicalise[] = $namestring;
    } else if (! empty($this->tags['place_name'])) { 
      $namestring .= $this->tags['place_name']; 
      $canonicalise[] = $namestring;
    }
    
    foreach (array('ref', 'iata', 'icao', 'old_name','loc_name') as $refkey) {
      if (! empty($this->tags[$refkey])) { 
        if (empty($namestring)) {
          $namestring = $this->tags[$refkey];
        } else {
          $namestring .= ' [' . $this->tags[$refkey] .']';
        }
        $canonicalise[] = $this->tags[$refkey];
      }
    }
    foreach ($this->tags as $key=>$value) {
      if (substr($key,0,5) == 'name:') {
        $namestring .= ' ['.substr($key,5).':'.$value.']';
        $canonicalise[] = $value;
      }
    }

    /* and then the other properties of named... */

    $region = new region($this->lat, $this->lon);
    $named->region = $region->regionnumber();
    $named->id = $this->id;
    $named->name = $namestring;
    $named->lat = $this->lat;
    $named->lon = $this->lon;
    $named->info = '';
    $named->category = '';
    $named->rank = 0;
    $named->is_in = '';

    /* now construct a useful description of the class of the item, so
       we can say, for example, "school St Bede's found ...". This is
       closely related to the tag name of the main tag of the item,
       but sometimes we need to construct it from more than one, and
       remove non-linguistic things like underscores */
    
    $prefix = '';
    $isplace = FALSE;
    if (is_a($this, 'relation')) {
      foreach ($this->tags as $key=>$value) {
        switch ($key) {
        case 'type':
          $named->info = str_replace('_', ' ', $value);
          break;
        }
      }
    } else /* way or node */ {
      foreach ($this->tags as $key=>$value) {
        switch ($key) {
        case 'type':
          if (is_a($this, 'relation')) {
            $named->info = str_replace('_', ' ', $value);
          }
          break;
        case 'highway':
          $named->category = $key;
          switch ($value) {
          case 'trunk':
          case 'primary':
          case 'secondary':
          case 'service':
          case 'unclassified':
            $residential = (! empty($this->tags['abutters']) && 
                            $this->tags['abutters'] == 'residential') ? 'residential ' : '';
            $named->info .= "{$prefix}{$residential}{$value} road"; 
            break;
          case 'track':
            $residential = (! empty($this->tags['abutters']) && 
                            $this->tags['abutters'] == 'residential') ? 'residential ' : '';
            $named->info .= "{$prefix}{$residential}{$value}"; 
            break;
          case 'trunk_link':
          case 'primary_link':
          case 'motorway_link':
            $named->info .= "{$prefix}link road"; 
            break;
          case 'tertiary':
            $named->info .= "{$prefix}road"; 
            break;
          case 'residential':
            $named->info .= "{$prefix}street"; 
            break;
          case 'cycleway':
          case 'bridleway':
          case 'footway':
          case 'footpath':
            $named->info .= "{$prefix}{$value}"; 
            break;
          default:
            $value = str_replace('_', ' ', $value);
            $named->info .= "{$prefix}{$value}"; 
          }
          $prefix = '; ';
          break;
        case 'amenity':
          $named->category = $key;
          switch ($value) {
          case 'fast_food':
            $named->info .= "{$prefix}take-away"; 
            $this->append_for_canonicalisation($canonicalise, 'fast food');
            $this->append_for_canonicalisation($canonicalise, 'take away');
            $prefix = '; ';
            break;
          case 'place_of_worship':
            $powtype = 'place of worship';
            // $canonicalise[] = $powtype;
            if (! empty($this->tags['religion'])) {
              switch($this->tags['religion']) {
              case 'christian':
              case 'church_of_england':
              case 'catholic':
              case 'anglican':
              case 'methodist':
              case 'baptist':
                $powtype = 'church';
                $this->append_for_canonicalisation($canonicalise, 'church');
                break;
              case 'moslem':
              case 'islam':
                $powtype = 'mosque';
                $this->append_for_canonicalisation($canonicalise, 'mosque');
                break;
              }
            } else if (! empty($this->tags['denomination'])) {
              switch($this->tags['denomination']) {
              case 'christian':
              case 'church_of_england':
              case 'catholic':
              case 'anglican':
              case 'methodist':
              case 'baptist':
                $powtype = 'church';
                $this->append_for_canonicalisation($canonicalise, 'church');
                break;
              case 'moslem':
              case 'islam':
                $powtype = 'mosque';
                $this->append_for_canonicalisation($canonicalise, 'mosque');
                break;
              }
            }
            if (! empty($powtype) && strpos($named->info, $powtype) === FALSE) { 
              $named->info .= "{$prefix}{$powtype}"; 
              $prefix = '; ';
            }
            $named->category = $key;
            break;
          default:
            $value = str_replace('_', ' ', $value);
            if (! empty($value) && strpos($named->info, $value) === FALSE) { 
              $named->info .= "{$prefix}{$value}";
              $prefix = '; ';
              $this->append_for_canonicalisation($canonicalise, $value);
            }
            break;
          }
          break;
        case 'landuse':
          $named->category = $key;
          switch ($value) {
          case 'farm':
            if (! empty($value) && strpos($named->info, $value) === FALSE) { 
              $named->info .= "{$prefix}{$value}"; 
              $prefix = '; ';
            }
            break;
          default:
            if (! empty($value) && strpos($named->info, $value) === FALSE) { 
              $named->info .= "{$prefix}{$value} area"; 
              $prefix = '; ';
            }
            break;
          }
          break;
        case 'railway':        
        case 'aeroway':        
        case 'man_made':
        case 'military':
        case 'tourism':
        case 'waterway':
        case 'leisure':
        case 'shop':
        case 'tourism':
        case 'historic':
        case 'natural':
        case 'sport':
          $value = str_replace('_', ' ', $value);
          if (! empty($value) && strpos($named->info, $value) === FALSE) { 
            $named->info .= "{$prefix}{$value}";
            $this->append_for_canonicalisation($canonicalise, $value);
            $prefix = '; ';
          }
          $named->category = $key;
          break;
        case 'place':
          $value = str_replace('_', ' ', $value);
          if (! empty($value) && strpos($named->info, $value) === FALSE) { 
            $named->info .= "{$prefix}{$value}";
            $named->rank = named::placerank($value);
            $this->append_for_canonicalisation($canonicalise, $value);
            $this->append_for_canonicalisation($canonicalise, $key);
            $named->category = $key;
            $prefix = '; ';
          }
          break;        
        case 'is_in':
          $named->is_in = $value;
          break;
        case 'religion':
        case 'denomination':
          break;
        }
      }
    }

    /* build the array of canonical name strings from the real name so that we
       have something more suitable for matching, no puntuation,
       variants and so on. See canon.php for details */

    $canonstrings = array();
    foreach ($canonicalise as $termstring) {
      $canonstrings = 
        array_merge($canonstrings, canon::canonical($termstring, TRUE /* alternates */));
    }

    /* do we already know about something with this name (same
       canonical form) and in the same locality. We look up only in the
       same region, so a road that spans a region boundary may appear
       twice, just as a rather longer road intentionally does. It
       slowsit down rather to consider neighbouring regions. If we
       find a hit, we make sure the category (kind of object -
       amenity, highway etc) is the same, and reject those that are
       the same within 3km (or whatever configured distance) of each
       other. */

    if ($named->name != '') {
      $canonexists = new canon(NULL, $canonstrings[0]);
      $namedexists = new named();
      $namedexists->category = $named->category;
      $q = $db->query();
      while ($q->selectjoin($canonexists, $namedexists) > 0) {
        $named->localdistancefrom($namedexists);
        if ($named->distance < $tooclose) { 
          // echo "{$named->name} within {$named->distance} km of another one\n";
          return; 
        }
      }
    }

    /* OK, we've got a complete, neighbourhod-unique named object. If
       it is also a place (village, town etc) create a separate subset
       of the information so we can rapidly look up nearby places from
       a much smaller set */

    if ($named->rank > 0) {
      /* only those places I know to be settlements, not things like
         place=wood, place=island and place=airport, or even
         place=country. These will still go in the general index, just
         not inthe place index for determining where a named thing is
         near */
      $db->insert(placeindex::placeindexfromnamed($named));
      $added['placeindex']++;
    }

    /* and finally queue the named object for insertion. However, we
       immediately flush the table, as the test for proximal
       similarity above needs to find them. We could implement a
       similar search on the queue I suppose, which would definitely
       speed things up, but is tricky */

    $db->insert($named);
    $added['named']++;

    $canons = canon::canonfactory($named->id, $canonstrings);
    foreach ($canons as $canon) { 
      $db->insert($canon); 
      $added['canon']++;
    }
  }
}

// ==================================================
class node extends tagged {

  /* constructor */ function node($id, $amended=FALSE) { 
    if (! $amended) { $id = canon::getuniqueid($id, 'node'); }
    $this->id = $id;
  }  

  // --------------------------------------------------
  function set_latlon($lat, $lon) {
    $this->lat = $lat;
    $this->lon = $lon;
  }

  // --------------------------------------------------
  /* a node has its own natural lat/lon so no calculation needed in this subclass */
  function calc_latlong() { return TRUE; }

  // --------------------------------------------------
  function complete() {
    global $db, $added;
    /* called when the node is completely read from the planet */
    $this->interesting_name();
    
    // and in any case, add it to the node table
    $db->insert($this);
    $added['node']++;

    /*
    global $area, $db;
    if ($this->lat > $area['s'] && $this->lat < $area['n'] && 
        $this->lon > $area['w'] && $this->lon < $area['e'])
    {
      // only interested in nodes in the requested area

      // does it have a name; is it a place?
      $this->interesting_name();

      // and in any case, add it to the node table
      $db->insert($this);
      $added[get_class($this)]++;
    }
    */
  }

}

// ==================================================
class way extends tagged {
  var $midpoint;
  var $nodes;

  // --------------------------------------------------
  function way($id, $amended=FALSE) { 
    if (! $amended) { $id = canon::getuniqueid($id, 'way'); }
    $this->id = $id; 
  }

  // --------------------------------------------------
  function add_node($osmid) { $this->nodes[] = canon::getuniqueid($osmid, 'node'); }

  // --------------------------------------------------
  function calc_latlong() { 
    /* The lat/lon of a way is detemrined to be the lat/lon of its middle node */
    if (! $this->get_midpoint()) { return FALSE; }
    if (! $this->midpoint->calc_latlong()) { return FALSE; } 
    $this->lat = $this->midpoint->lat;
    $this->lon = $this->midpoint->lon;
    return TRUE;
  }

  // --------------------------------------------------
  function get_midpoint() {
    global $db;
    if (empty($this->nodes)) { return FALSE; }
    $nodeid = $this->nodes[count($this->nodes)/2];
    $node = new node($nodeid, TRUE);
    if ($db->select($node) != 1) { return FALSE; }
    $this->midpoint = $node;
    return TRUE;
  }

  // --------------------------------------------------
  function complete() {
    /* called on completion of reading the way.Is it of any interest? */
    global $db, $added;
    $this->interesting_name();

    // and in any case, add it to the way table
    $db->insert($this);
    $added['way']++;
  }
}

// ==================================================
class relation extends tagged {
  var $representativemember;
  var $nodes;
  var $ways;

  // --------------------------------------------------
  function relation($id, $amended=FALSE) { 
    if (! $amended) { $id = canon::getuniqueid($id, 'relation'); }
    $this->id = $id; 
  }

  // --------------------------------------------------
  function add_node($osmid) { $this->nodes[] = canon::getuniqueid($osmid, 'node'); }

  // --------------------------------------------------
  function add_way($osmid) { $this->ways[] = canon::getuniqueid($osmid, 'way'); }

  // --------------------------------------------------
  function calc_latlong() { 
    /* The lat/lon of a relation is detemrined to be the lat/lon of a
       representative member: its first node if there is one, or if
       not via middle way. In time we might want to refine this to
       take roles and/or the type of relation into account */
    if (! $this->get_representativemember()) { return FALSE; }
    if (! $this->representativemember->calc_latlong()) { return FALSE; } 
    $this->lat = $this->representativemember->lat;
    $this->lon = $this->representativemember->lon;
    return TRUE;
  }

  // --------------------------------------------------
  function get_representativemember() {
    global $db;
    if (empty($this->nodes)) { 
      if (empty($this->ways)) { return FALSE; }
      $wayid = $this->ways[count($this->ways)/2];
      $way = new way($wayid, TRUE);
      if ($db->select($way) != 1) { return FALSE; }
      $this->representativemember = $way;
      return TRUE;
    }
    $nodeid = $this->nodes[0];
    $node = new node($nodeid, TRUE);
    if ($db->select($node) != 1) { return FALSE; }
    $this->representativemember = $node;
    return TRUE;
  }

  // --------------------------------------------------
  function complete() {
    /* called on completion of reading the relation.Is it of any interest? */
    global $db;
    $this->interesting_name();
  }
}

// ==================================================
/* the main program... */

function zap($c) {
  // empty the table whose name is given
  global $db;
  echo "zapped {$c} ", $db->truncate($c), "\n";
}

// --------------------------------------------------

if (! isset($argv[1])) { die ("usage import.php planetfilename\n"); }
$planetfilename = $argv[1];

/* extract the date from the planet file name so we can note the index date in the database */
if (preg_match('/-([0-9]{2})([0-9]{2})([0-9]{2})\\./', $planetfilename, $matches)) {
  $planetdate = "20{$matches[1]}-{$matches[2]}-{$matches[3]}";
}
$planetfd = fopen($planetfilename, 'r');
if ($planetfd === FALSE) { die("cannot open '{$planetfilename}'\n"); }
$planetsize = filesize($planetfilename);
if($planetsize < 0) { $planetsize = pow(2.0, 32) + $planetsize; }
echo "planet size {$planetsize}\n";
$planetdonepermill = 0;

$starttime = time();
echo "started at ", date("H:i:s", $starttime), "\n";

/* start clean... */
zap('node');
zap('way');
zap('placeindex');
zap('named');
zap('canon');

/* update the index date */
include_once('options.php');
$options = new options();
$options->name = 'indexdate';
$db->delete($options, 'name');
if (isset($planetdate)) {
  $options->value = $planetdate;
  $db->insert($options);
}

/* the planet file is too big to do a filesize call on (> 4Gb) so
   instead set off a grep to find out how many elements there are and
   do progress based on proportion of elements. We'll check back later
   to see if this has finished */
$elementcheckfile = '/tmp/osmimportgrep';
file_put_contents('', $elementcheckfile);
exec ("grep -c \"\\(<node \\|<way \\|<relation \\)\" {$planetfilename} > {$elementcheckfile} &");
$elementcount = $currentelementcount = 0; 

/* initialise the node cache */
$nc = new nodecache();

/* start the xml parser. This makes callbacks when it sees the start and end of each element */
$xml_parser = xml_parser_create();
xml_set_element_handler($xml_parser, "startelement", "endelement");

while ($data = fread($planetfd, 4096)) {
  if (!xml_parse($xml_parser, $data, feof($planetfd))) {
    die(sprintf("XML error: %s at line %d",
                xml_error_string(xml_get_error_code($xml_parser)),
                xml_get_current_line_number($xml_parser)));
  }
}
xml_parser_free($xml_parser);
fclose($planetfd);

echo "added:\n";
foreach ($added as $class => $count) {
  echo "  {$class}: {$count}\n";
}

$endtime = time();
echo "finished at ", date("H:i:s", $endtime), "\n";
$seconds = $endtime - $starttime;
$minutes = (int)($seconds/60);
$seconds -= $minutes*60;
$hours = (int)($minutes/60);
$minutes -= $hours*60;
printf("took %d:%02d:%02d\n", $hours, $minutes, $seconds);

/* and we're done */

// ==================================================
/* XML parser callbacks

   $tagged is the current node, way or relation we are processing, where tags go 
   when we encounter them, and where nodes of ways and members of relations go */

function startelement($parser, $name, $attrs)
{
  global $tagged, $elementcount, $elementcheckfile, $currentelementcount;
  static $seenway = FALSE;
  static $seenrelation = FALSE;
  static $lastelementcheck = 0;
  static $lastpermill = 0;

  /* Do progress information */

  if ($elementcount == 0) {
    $elementcheck = time();
    if ($elementcheck - $lastelementcheck > 60) {
      echo "checking {$elementcheckfile}\n";
      $lastelementcheck = $elementcheck;
      clearstatcache();
      $size = filesize($elementcheckfile);
      if ($size > 0) {
        $elementcount = file_get_contents($elementcheckfile);
        $elementcount = (int) $elementcount;
        echo "noted {$elementcount} elements\n";
      }
    }
  } else {
    $permill = (int) floor(($currentelementcount * 1000.0 / $elementcount));
    if ($permill != $lastpermill) {
      $dp = $permill % 10;
      echo (int)floor($permill/10), '.', $dp, "% ";
      if ($dp == 9) { echo "\n"; }
      $lastpermill = $permill;
    }
  }

  /* What we do depends on element type... */

  switch($name) {
  case 'TAG':
    /* Disribute tags to the object which encloses the tag elements */
    if (empty($tagged)) { die("no tagged\n"); }
    if (!isset($attrs['K']) || !isset($attrs['V'])) { 
      die("no tag K or V".print_r($tagged,1)."\n".print_r($attrs,1)); }
    $tagged->add_tag($attrs['K'], $attrs['V']);
    break;
  case 'ND': /* constituent node of way */
    if (empty($tagged)) { die("no tagged\n"); }
    if (! isset($attrs['REF'])) { 
      echo("no node REF".print_r($tagged,1).print_r($attrs,1)); 
    }  else {
      $tagged->add_node((int)$attrs['REF']);
    }
    break;
  case 'MEMBER': /* constituent member of relation */
    if (empty($tagged)) { die("no tagged\n"); }
    if (! isset($attrs['REF']) || ! isset($attrs['TYPE'])) { 
      echo("no node REF".print_r($tagged,1).print_r($attrs,1)); 
    }  else {
      switch ($attrs['TYPE']) {
      case 'node':
        $tagged->add_node((int)$attrs['REF']);
        break;
      case 'way':
        $tagged->add_way((int)$attrs['REF']);
        break;
      default:
        echo("unrecognised TYPE".print_r($tagged,1).print_r($attrs,1)); 
      }
    }
    break;

  case 'NODE':
    if (! empty($tagged)) { die("tagged active\n".print_r($tagged,1)); }
    if (empty($attrs['ID'])) { die("no node ID"); }
    $tagged = new node((int)$attrs['ID']);
    if (empty($attrs['LAT']) || empty($attrs['LON'])) { 
      die("no node LAT/LON".print_r($tagged,1)); }
    $tagged->set_latlon((double)$attrs['LAT'], (double)$attrs['LON']);
    $currentelementcount++;
    break;
  case 'WAY':
    if (! $seenway) {
      /* first way ... */
      echo "starting ways at " . date("H:i:s") . "\n";
      $seenway = TRUE;
    }
    if (! empty($tagged)) { die("tagged active\n".print_r($tagged,1)); }
    if (empty($attrs['ID'])) { die("no way ID"); }
    $tagged = new way((int)$attrs['ID']);
    $currentelementcount++;
    break;
  case 'RELATION':
    if (! $seenrelation) {
      /* first relation ... */
      echo "starting relations at " . date("H:i:s") . "\n";
      $seenrelation = TRUE;
    }
    if (! empty($tagged)) { die("tagged active\n".print_r($tagged,1)); }
    if (empty($attrs['ID'])) { die("no relation ID"); }
    $tagged = new relation((int)$attrs['ID']);
    $currentelementcount++;
    break;
  }
}

// --------------------------------------------------
function endelement($parser, $name)
{
  /* called at the end of the element. Call the relevant method according to node type */
  global $tagged;

  switch($name) {
  case 'NODE':
  case 'WAY':
  case 'RELATION':
    $tagged->complete();
    $tagged = NULL;
    break;
  }
}

?>
