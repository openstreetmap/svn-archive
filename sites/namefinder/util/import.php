<?php

/* This file takes an OSM planet file, or a planet diff, and processes
   it to update the name finder database noting what has changed. A
   further program then updates the index from the total information
   this program generates.

   The process takes many hours to run, so it is better to run it on a
   temporary database and then export the tables (other than node and
   way) and import them into the production database, which takes only 
   maybe a minute or so.  */

session_start(); // only so we get a unique log file

include_once('preamble.php');
include_once('named.php');
include_once('changedid.php');
include_once('placeindex.php');
include_once('region.php');
include_once('canonical.php');
include_once('word.php');

include_once('tagged.php');
include_once('node.php');
include_once('way.php');
include_once('relation.php');
include_once('way_node.php');
include_once('relation_node.php');
include_once('relation_way.php');
include_once('relation_relation.php');

$added['node'] = $added['way'] = $added['relation'] = $added['placeindex'] = 
$added['named'] = $added['canonical'] = 0;

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
/* the main program... */

function zap($c) {
  // empty the table whose name is given
  global $db;
  echo "zapped {$c} ", $db->truncate($c), "\n";
}

// --------------------------------------------------

if (! isset($argv[1])) { die ("usage import.php planet_or_osc_filename\n"); }
$planetfilename = $argv[1];
$augment = $planetfilename{0} == '+';
if ($augment) { $planetfilename = substr($planetfilename, 1); }

/* extract the date from the planet file name so we can note the index date in the database */
if (preg_match('/-(20)?([0-9]{2})-?([0-9]{2})-?([0-9]{2})\\./', $planetfilename, $matches)) {
  $planetdate = "20{$matches[2]}-{$matches[3]}-{$matches[4]}";
  echo "for file date {$planetdate}\n";
}
if (preg_match('/\\.bz2$/i', $planetfilename)) {
  // stream_filter_append($planetfd, 'bzip2.decompress', STREAM_FILTER_READ);
  $planetfd = bzopen($planetfilename, 'r');
  $planetread = 'bzread';
  $planetclose = 'bzclose';
} else if (preg_match('/\\.gz$/i', $planetfilename)) {
  // stream_filter_append($planetfd, 'zlib.inflate', STREAM_FILTER_READ);
  $planetfd = gzopen($planetfilename, 'r');
  $planetread = 'gzread';
  $planetclose = 'gzclose';
} else {
  $planetfd = fopen($planetfilename, 'r');
  $planetread = 'fread';
  $planetclose = 'fclose';
}
if ($planetfd === FALSE) { die("cannot open '{$planetfilename}'\n"); }

$planetsize = filesize($planetfilename);
if($planetsize < 0) { $planetsize = pow(2.0, 32) + $planetsize; }
echo "planet size {$planetsize}\n";
$planetdonepermill = 0;

$starttime = time();
echo "started at ", date("H:i:s", $starttime), "\n";

/* start clean for updated items */
if (! $augment) {
  zap('canonical');
  zap('changedid');
}

$doing_delete = FALSE;
$doing_modify = FALSE;

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
if (0) {
$elementcheckfile = '/tmp/osmimportgrep';
file_put_contents('', $elementcheckfile);
exec ("grep -c \"\\(<node \\|<way \\|<relation \\)\" {$planetfilename} > {$elementcheckfile} &");
}
$elementcount = $currentelementcount = 0; 

/* initialise the node cache */
$nc = new nodecache();

/* start the xml parser. This makes callbacks when it sees the start and end of each element */
$xml_parser = xml_parser_create();
xml_set_element_handler($xml_parser, "startelement", "endelement");

$bytesread = 0.0;
$onemb = 1024.0 * 1024.0;
$mb = 0;
while ($data = $planetread($planetfd, 4096)) {
  $bytesread += 4096.0;
  if ($bytesread > $onemb) {
    $mb += 1;
    echo "\n", with_commas($mb), "Mb\n";
    $bytesread -= $onemb;
  }
  if (!xml_parse($xml_parser, $data, feof($planetfd))) {
    die(sprintf("XML error: %s at line %d",
                xml_error_string(xml_get_error_code($xml_parser)),
                xml_get_current_line_number($xml_parser)));
  }
}
xml_parser_free($xml_parser);
$planetclose($planetfd);

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
  global $tagged, $elementcount, $elementcheckfile, $currentelementcount, $added;
  global $doing_delete, $doing_modify;
  static $seenway = FALSE;
  static $seenrelation = FALSE;
  static $lastelementcheck = 0;
  static $lastpermill = 0;

  /* Do progress information */

  /* 
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
  */

  /* What we do depends on element type... */

  switch($name) {
  case 'DELETE':
    $doing_delete = TRUE;
    $doing_modify = FALSE;
    break;
  case 'MODIFY':
    $doing_delete = FALSE;
    $doing_modify = TRUE;
    break;
  case 'ADD':
    $doing_delete = FALSE;
    $doing_modify = FALSE;
    break;

  case 'TAG':
    /* Disribute tags to the object which encloses the tag elements */
    if (empty($tagged)) { die("no tagged\n"); }
    if (!isset($attrs['K']) || !isset($attrs['V'])) { 
      die("no tag K or V".print_r($tagged,1)."\n".print_r($attrs,1)); }
    $tagged->add_tag($attrs['K'], $attrs['V']);
    break;
  case 'ND': /* constituent node of way */
    if ($doing_delete) { break; }
    if (empty($tagged)) { die("no tagged\n"); }
    if (! isset($attrs['REF'])) { 
      echo("no node REF".print_r($tagged,1).print_r($attrs,1)); 
    }  else {
      $tagged->add_node((int)$attrs['REF']);
    }
    break;
  case 'MEMBER': /* constituent member of relation */
    if ($doing_delete) { break; }
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
      case 'relation':
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
    if (! isset($attrs['LAT']) || ! isset($attrs['LON'])) { 
      die("no node LAT/LON".print_r($tagged,1)); }
    $tagged->set_latlon((double)$attrs['LAT'], (double)$attrs['LON']);
    $currentelementcount++;
    break;
  case 'WAY':
    if (! $seenway) {
      /* first way ... */
      echo "\nstarting ways at " . date("H:i:s") . "\n";
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
      echo "\nstarting relations at " . date("H:i:s") . "\n";
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
  global $tagged, $doing_delete, $doing_modify, $added;
  global $db;
  static $lastadded = 0;

  switch($name) {
  case 'NODE':
  case 'WAY':
  case 'RELATION':
    /* ignore coastlines */
    if (isset($tagged->tags['natural']) && $tagged->tags['natural'] == 'coastline') { 
      $tagged = null;
      return; 
    }

    /* analyse the object */
    $tagged->interesting_name();

    /* we have four possibilities:
       1. simple addition: as per a raw planet file
       2. deletion: the object is going away for ever
       3. modification(1): the object does not already exist - the diffs only say modified 
          so we can't tell - so treat this as addition
       4. modification(2): the object does exist, so treat as deletion followed by addition

       In all cases we need to note that the canonical names have changed so 
       that we can reprocess them; in case 4, boththe old canonical and the new one 
       have changed.
    */

    $do_delete = $doing_delete;

    if ($doing_modify) {
      /* if the object already exists, as well as deleting it below, add the *old*
         canonical form to the list of those changed, so that if
         there are others with the same canonical nearby they will
         eventually be revealed in place of this one */
      $named = new named();
      $named->id = $tagged->id;
      $q = $db->query();
      while ($q->select($named) > 0) {
        $do_delete = TRUE;
        if (empty($named->canonical)) { continue; }
        $canonical = new canonical($named->canonical, $named->region);
        $db->insert($canonical);
      }
    }

    if ($doing_modify || $doing_delete) {
      /* a node that is changed or deleted that is part of a way or
         relation or a way that is part of a relation will affect its
         parent - e.g. a node moving may change the location of a way,
         and that in turn may affect other similar names nearby as
         well as the parent. For example node gets deleted, owning way
         shifts north, section of the same road, but different way,
         further south is now more than 3km from the original way, so
         gets added to the index. This kind of indirect effect is what
         makes updating the index incrementally so problematic, and
         why it is done on the basis of changes affecting the
         canonical string, not by id */
      $parent_ids = $tagged->parent_ids();
      if (! empty($parent_ids)) {
        $named = new named();
        $ors = array();
        foreach ($parent_ids as $id) { $ors[] = y_op::eq('id',$id); }
        $q = $db->query();
        $q->where(count($ors) == 1 ? $ors[0] : y_op::oor($ors));
        while ($q->select($named) > 0) {
          if (empty($named->canonical)) { continue; }
          $canonical = new canonical($named->canonical, $named->region);
          $db->insert($canonical);
        }        
      }
    }

    /* delete object, named's whether a delete or a
       modify (we'll add modified object back again in a moment). There may not be 
       any index words to delete, if the named was not already in the index but no matter */
    if ($do_delete) {
      $tagged->delete();
      if (deletebyid('named', $tagged->id)) {
        deletebyid('word', $tagged->id);
        deletebyid('placeindex', $tagged->id);
      }
    }

    /* whether we delete, add or modify, the canonical form we made
       is changed, so add it to the table of changed canonicals. In
       cases of deletion only, if there are others with the same
       canonical nearby they will eventually be revealed in place of
       this one by virtue of these entries */
    if (! empty($tagged->named)) {
      if (! empty($tagged->named->canonical)) {
        $canonical = new canonical($tagged->named->canonical, $tagged->named->region);
        $db->insert($canonical);
        $added['canonical']++;
      } else {
        /* for anonymous items that we still want to index by type, keep a record 
           of their id for the second pass */
        $changedid = new changedid($tagged->id);
        $db->insert($changedid);
      }
    }

    if (! $doing_delete) {
      /* add the object, named (duplicated for as many different canonical forms 
         as required) */
      $tagged->insert();
      if (! is_null($tagged->named)) {
        $db->insert($tagged->named);
        $added['named']++;
      }
    }

    $class = get_class($tagged);
    $n = $added[$class];
    if ($n % 1000 == 0 && $n != $lastadded) { echo with_commas($n), ' '; $lastadded = $n; }

    $tagged = NULL;
    break;
  }

}

// --------------------------------------------------
function with_commas($n) {
  $divisor = 1000000000;
  $s = '';
  $pattern = '%d,';
  while ($divisor > 1) {
    $section = (int)($n/$divisor);
    if ($section > 0) {
      $s .= sprintf($pattern, $section);
      $pattern = '%03d,';
      $n = $n % $divisor;
    }
    $divisor /= 1000;
  }
  $pattern = substr($pattern, 0, -1);
  $s .= sprintf($pattern, $n);
  return $s;
}

// --------------------------------------------------
function deletebyid($class, $id) {
  /* deletion is very slow, so it's worth checking whether any exists first */
  global $db;
  $o = new $class();
  $o->id = $id;
  $q = $db->query();
  $q->limit(1);
  if ($q->select($o) == 0) { return FALSE; }
  $db->delete($o, 'id');
  return TRUE;
}

?>
