<?php

/* This file takes an OSM planet file, and processes it to produce the 
   name finder index.

   It requires temporary tables 'node' and 'segment' in order to
   retrieve references by id from within the planet xml file, but these large tables 
   can be emptied on completion. 

   node and segment classes for these tables are defined inline below 
   as subclasses of 'tagged'

   The process takes many hours to run, so it is better to run it on a
   temporary database and then export the tables (other than node and
   segment) and import them into the production database, which takes only 
   maybe a minute or so. 
*/

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

$added = array('node'=>0,
               'placeindex'=>0,
               'named'=>0,
               'segment'=>0);

$pending = array();
$chunk = $config['insertchunk'];
$tooclose = $config['tooclose'];

function insert($object) {
  global $pending, $chunk;
  $cn = get_class($object);
  $pending[$cn][] = $object;
  if (count($pending[$cn]) >= $chunk) {
     flush_inserts($cn);
  }
}

/* static */ function flush_inserts($cn) {
  global $pending, $db, $added;
  $n = count($pending[$cn]);
  if ($n > 0) { 
    // echo "adding {$n} {$cn}\n";
    $added[$cn] += $n;
    $db->insert($pending[$cn]); 
    $pending[$cn] = array();
  }
}

// ==================================================
class nodecache {
  /* we maintain a node cache as we create nodes and use them because we frequently 
     refer to the same node a few times in quick succession from say a way or segment */
  var $cache;
  var $size;
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
    $this->last++;
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

  /* tagged is the superclass of node, segment and way (the latter is not 
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
  function interesting_name() {

    /* This is the key indexing function. It chooses an item (node,
       way, segment) with tags sufficiently interesting to go into the
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

    global $db, $tooclose;
    if (! $this->calc_latlong()) { return; }

    /* It has interest, and it has location, so make a named object to
       consider it further. Build a name for it which is a
       composite of the ruename, and the various other
       possibilities like ref, and non-native names (we usually
       put these in square brackets) */

    $named = new named();
    $namestring = '';
    if (! empty($this->tags['name'])) { 
      $namestring .= $this->tags['name']; 
    } else if (! empty($this->tags['place_name'])) { 
      $namestring .= $this->tags['place_name']; 
    }
    foreach (array('ref', 'iata', 'icao', 'old_name','loc_name') as $refkey) {
      if (! empty($this->tags[$refkey])) { 
        if (empty($namestring)) {
          $namestring = $this->tags[$refkey];
        } else {
          $namestring .= ' [' . $this->tags[$refkey] .']';
        }
      }
    }
    $canonicalise = $namestring;
    foreach ($this->tags as $key=>$value) {
      if (substr($key,0,5) == 'name:') {
        $namestring .= ' ['.substr($key,5).':'.$value.']';
        $canonicalise .= "[{$value}]";
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

    /* now construct a useful description of the class of the item, so
       we can say, for example, "school St Bede's found ...". This is
       closely related to the tag name of the main tag of the item,
       but sometimes we need to construct it from more than one, and
       remove non-linguistic things like underscores */
    
    $prefix = '';
    $isplace = FALSE;
    foreach ($this->tags as $key=>$value) {
      switch ($key) {
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
          $canonextras[] = 'fast food';
          $canonextras[] = 'take away';
          $prefix = '; ';
          break;
        case 'place_of_worship':
          $powtype = 'place of worship';
          // $canonextras[] = $powtype;
          if (! empty($this->tags['religion'])) {
            switch($this->tags['religion']) {
            case 'christian':
            case 'church_of_england':
            case 'catholic':
            case 'anglican':
            case 'methodist':
            case 'baptist':
              $powtype = 'church';
              $canonextras[] = 'church';
              break;
            case 'moslem':
            case 'islam':
              $powtype = 'mosque';
              $canonextras[] = 'mosque';
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
              $canonextras[] = 'church';
              break;
            case 'moslem':
            case 'islam':
              $powtype = 'mosque';
              $canonextras[] = 'mosque';
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
            $canonextras[] = $value;
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
          $canonextras[] = $value;
          $prefix = '; ';
        }
        $named->category = $key;
        break;
      case 'place':
        $value = str_replace('_', ' ', $value);
        if (! empty($value) && strpos($named->info, $value) === FALSE) { 
          $named->info .= "{$prefix}{$value}";
          $named->rank = named::placerank($value);
          $canonextras[] = $value;
          $canonextras[] = $key;
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

    /* build the canonical name strings from the real name so that we
       have something more suitable for matching, no puntuation,
       variants and so on. See canon.php for details */

    $canon1 = canon::canonical($canonicalise, TRUE /* alternates */);
    $canon1 = $canon1 == '' ? '#' : "#{$canon1}#";
    $canon2 = $canon1;
    if (! empty($canonextras)) {
      foreach($canonextras as $canonextra) {
        $canon3 = canon::canonical($canonextra);
        if ($canon3 != '' && strpos($canon1, $canon3) === FALSE) {
          $canon2 .= $canon3 . '#';
        }
      }
    }
    $named->canon = $canon2;

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
      $namedexists = new named();
      $namedexists->region = $named->region;
      $namedexists->canon = $canon2;
      $q = $db->query();
      while ($q->select($namedexists) > 0) {
        $named->localdistancefrom($namedexists);
        if ($named->distance < $tooclose && $named->category == $namedexists->category) { 
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
      insert(placeindex::placeindexfromnamed($named));
    }

    /* and finally queue the named object for insertion. However, we
       immediately flush the table, as the test for proximal
       similarity above needs to find them. We could implement a
       similar search on the queue I suppose, which would definitely
       speed things up, but is tricky */

    insert($named);
    flush_inserts('named');
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
    /* called when the node is completely read from the planet */
    global $area, $db;
    if ($this->lat > $area['s'] && $this->lat < $area['n'] && 
        $this->lon > $area['w'] && $this->lon < $area['e'])
    {
      // only interested in nodes in the requested area

      // does it have a name; is it a place?
      $this->interesting_name();

      // and in any case, add it to the node table
      insert($this);
    }
  }

}

// ==================================================
class segment extends tagged {

  var $from, $to; 
  var $nodefrom, $nodeto;

  // --------------------------------------------------
  /* constructor */ function segment($id, $amended=FALSE) { 
    if (! $amended) { $id = canon::getuniqueid($id, 'segment'); }
    $this->id = $id;
  }

  // --------------------------------------------------
  function set_fromto($idfrom, $idto) { 
    $this->from = canon::getuniqueid($idfrom, 'node');
    $this->to = canon::getuniqueid($idto, 'node');
  }

  // --------------------------------------------------
  function calc_latlong() {
    /* the lat/lon of a segment is determined to be its midpoint */
    if ($this->ok_latlon()) { return TRUE; }
    if (! $this->get_nodes()) { return FALSE; }
    $this->lat = ($this->nodefrom->lat + $this->nodeto->lat) / 2.0;
    $this->lon = ($this->nodefrom->lon + $this->nodeto->lon) / 2.0;
    if (($this->nodefrom->lon > 90.0 && $this->nodeto->lon < -90.0) ||
        ($this->nodefrom->lon < -90.0 && $this->nodeto->lon > 90.0)) {
      /* very unusual case - an enormously long segment, or one which 
         crosses the antemeridian: assume the latter */
      $this->lon += 180.0;
      if ($this->lon >= 360.0) { $this->lon -= 360.0; }
    }
    return TRUE;
    // echo "calc_latlong for ",print_r($this, 1);
  }

  // --------------------------------------------------
  function get_nodes() {
    /* that is,getthe nodes into the segment from either end by looking them up 
       in the database (try the cache first) */
    global $nc;
    $n = $nc->getnode($this->from);
    if ($n === FALSE) { return FALSE; }
    $this->nodefrom = clone $n;
    $n = $nc->getnode($this->to);
    if ($n === FALSE) { return FALSE; }
    $this->nodeto = clone $n;
    return TRUE;
  }

  // --------------------------------------------------
  function complete() {
    /* called on completion of reading the segment. We ignore
       coastline because (a) they are voluminous and not interesting
       to the name finder, and (b) they have some errors in which
       makes them hard to process efficiently */

    if (! isset($this->tags['natural']) || $this->tags['natural']!='coastline') {
      $this->interesting_name(); // is it an interesting segment?
      insert($this); // add it (including lat/lon if calculated)
    }
  }
}

// ==================================================
class way extends tagged {
  var $segments;
  var $midsegment;
  var $nodefrom, $nodeto;

  // --------------------------------------------------
  function way($id, $amended=FALSE) { 
    if (! $amended) { $id = canon::getuniqueid($id, 'way'); }
    $this->id = $id; 
  }

  // --------------------------------------------------
  function add_segment($osmid) { $this->segments[] = canon::getuniqueid($osmid, 'segment'); }

  // --------------------------------------------------
  function calc_latlong() { 
    /* The lat/lon of a way is detemrined to be the lat/lon of its middle segment, which 
       in turn is that segment's midpoint. If we can't determine position, 
       we're not interested  */
    if (! $this->get_midsegment()) { return FALSE; }
    if (! $this->midsegment->calc_latlong()) { return FALSE; } 
    $this->lat = $this->midsegment->lat;
    $this->lon = $this->midsegment->lon;
    return TRUE;
  }

  // --------------------------------------------------
  function get_midsegment() {
    global $db;
    if (empty($this->segments)) { return FALSE; }
    $segmentid = $this->segments[count($this->segments)/2];
    $segment = new segment($segmentid, TRUE);
    if ($db->select($segment) == 1) {
      $this->midsegment = $segment;
      return TRUE;
    }
    return FALSE;
  }

  // --------------------------------------------------
  function complete() {
    /* called on completion of reading the way.Is it of any interest? */
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
zap('segment');
zap('placeindex');
zap('named');

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
exec ("grep -c \"\\(<node \\|<segment\\|<way \\)\" {$planetfilename} > {$elementcheckfile} &");
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

flush_inserts('named');
flush_inserts('placeindex');

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

   $tagged is the current node, segment or way we are processing, where tags go 
   when we encounter them, and where way seg's go */

function startelement($parser, $name, $attrs)
{
  global $tagged, $elementcount, $elementcheckfile, $currentelementcount;
  static $seensegment = FALSE;
  static $seenway = FALSE;
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
  case 'SEG':
    /* SEG is the element which puts a segment reference into a way */
    if (empty($tagged)) { die("no tagged\n"); }
    if (! isset($attrs['ID'])) { 
      echo("no seg ID".print_r($tagged,1).print_r($attrs,1)); 
    }  else {
      $tagged->add_segment((int)$attrs['ID']);
    }
    break;

  /* node, segment and way start those elements: create objects for
   them as they appear so we have somewhere to put the tags; call the
   relevant complete() function on eac later when we see the end of
   the tag */
  case 'NODE':
    if (! empty($tagged)) { die("tagged active\n".print_r($tagged,1)); }
    if (empty($attrs['ID'])) { die("no node ID"); }
    $tagged = new node((int)$attrs['ID']);
    if (empty($attrs['LAT']) || empty($attrs['LON'])) { 
      die("no node LAT/LON".print_r($tagged,1)); }
    $tagged->set_latlon((double)$attrs['LAT'], (double)$attrs['LON']);
    $currentelementcount++;
    break;
  case 'SEGMENT':
    if (! $seensegment) {
      /* first segment ... */
      flush_inserts('node');
      flush_inserts('named');
      flush_inserts('placeindex');
      echo "starting segments at " . date("H:i:s") . "\n";
      $seensegment = TRUE;
    }
    if (! empty($tagged)) { die("tagged active\n".print_r($tagged,1)); }
    if (empty($attrs['ID'])) { die("no segment ID"); }
    $tagged = new segment((int)$attrs['ID']);
    if (empty($attrs['FROM']) || empty($attrs['TO'])) { 
      die("no segment FROM/TO".print_r($tagged,1)); }
    $tagged->set_fromto((int)$attrs['FROM'], (int)$attrs['TO']);
    $currentelementcount++;
    break;
  case 'WAY':
    if (! $seenway) {
      /* first way ... */
      flush_inserts('segment');
      echo "starting ways at " . date("H:i:s") . "\n";
      $seenway = TRUE;
    }
    if (! empty($tagged)) { die("tagged active\n".print_r($tagged,1)); }
    if (empty($attrs['ID'])) { die("no way ID"); }
    $tagged = new way((int)$attrs['ID']);
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
  case 'SEGMENT':
  case 'WAY':
    $tagged->complete();
    $tagged = NULL;
    break;
  }
}

?>
