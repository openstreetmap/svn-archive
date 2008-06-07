<?php

/* This file updates the name finder index on the basis of which canonical strings have been 
   noted as changed, the contents of the canonical table.

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

$tooclose = $config['tooclose'];

$start = 0;
$chunk = 5000;

$starttime = time();
echo "started at ", date("H:i:s", $starttime), "\n";

$n = $db->info('canonical', y_op::count());
echo "doing {$n} synonymous objects...\n";
$prevpercent = 0;
$nc = 0;

$worktodo = $n > 0;

while ($worktodo) {
  $canonical = new canonical();
  $na = new named();
  $worktodo = FALSE;
  $q = $db->query();
  $q->limit($chunk, $start);

  /* the first time select is called below, it will take forever, because it has to sort */
  while ($q->select($canonical) > 0) {
    $worktodo = TRUE;
    $na = new named();
    $na->canonical = $canonical->canonical;
    if ($canonical->region != 99999) { 
      /* 999999 indicates "do all regions", useful for making manual corrections */
      $na->region = $canonical->region;
    }
    $qna = $db->query();
    $nas = array();
    while ($qna->select($na) > 0) { $nas[] = clone $na; }
    /*
      This code is here so that if the canonical algorith is changed,
      it is possible to update the exisiting entries without a
      complete revbuild. Use a SQL query to populate the canoncical
      table with the *old* canonical strings affected, and then run
      update with this uncommented

      foreach ($nas as $na) {
        $na1 = new named();
        $na1->id = $na->id;
        $na1->canonical = canonical::canonicalise_to_string($na->name);
        $db->update($na1, 'id');
      } 
    */
    process($nas);

    $nc++;
    $percent = (int)(($nc * 100)/$n);
    if ($percent != $prevpercent) {
      if ($percent % 20 == 1) { echo "\n"; }
      echo "{$percent}% ";
      $prevpercent = $percent;
    }
  }
  $start += $chunk;
}

/* now process anonymous changed objects */
$n = $db->info('changedid', y_op::count());
echo "\ndoing {$n} anonymous objects...\n";
$prevpercent = 0;
$nc = 0;

$start = 0;
$chunk = 500;
$worktodo = $n > 0;
while ($worktodo) {
  $changedid = new changedid();
  $worktodo = FALSE;
  $q = $db->query();
  $q->limit($chunk, $start);
  $nas = array();
  while ($q->select($changedid) > 0) {
    $worktodo = TRUE;
    $na = new named();
    $na->id = $changedid->id;
    if ($db->select($na) > 0) { $nas[] = clone $na; }
  }
  if (! empty($nas)) { makeallindexes($nas); }
  $nc += count($nas);
  $percent = (int)(($nc * 100)/$n);
  if ($percent != $prevpercent) {
    if ($percent % 20 == 1) { echo "\n"; }
    echo "{$percent}% ";
    $prevpercent = $percent;
  }
  $start += $chunk;
}

include_once('options.php');
$options = new options();
$options->name = 'indexdate';
if ($db->select($options, 'name') > 0) {
  $db->delete($options, 'name');
  $db->insert($options);
}

$endtime = time();
echo "\nfinished at ", date("H:i:s", $endtime), "\n";
$seconds = $endtime - $starttime;
$minutes = (int)($seconds/60);
$seconds -= $minutes*60;
$hours = (int)($minutes/60);
$minutes -= $hours*60;
printf("took %d:%02d:%02d\n", $hours, $minutes, $seconds);

// --------------------------------------------------
function process($nas) {
  // var_dump($nas); echo "\n\n";
  global $tooclose, $db;
  $c = count($nas);

  if (TRUE /* do deletes */) {
    $ors = array();
    $orspi = array();

    for ($i = 0; $i < $c; $i++) { 
      $nai = $nas[$i];

      $word = new word();
      $word->id = $nai->id;
      if ($db->info($word, y_op::count()) == 0) { continue; }

      $ors[] = y_op::eq('id', $nai->id); 
      if ($nai->rank > 0) {
        /* remove any corresponding place index entries */
        $orspi[] = y_op::eq('id', $nai->id);
      }
    }

    if (count($ors) > 0) {
      $q = $db->query();
      $q->where(count($ors) == 1 ? $ors[0] : y_op::oor($ors));
      $q->delete('word', 'id');
      // outputsql($q);

      if (count($orspi) > 0) {
        $q = $db->query();
        $q->where(count($orspi) == 1 ? $orspi[0] : y_op::oor($orspi));
        $q->delete('placeindex', 'id');
        // outputsql($q);
      }
    }
  }

  for ($i = 0; $i < $c; $i++) {
    $nai = $nas[$i];
    if (is_null($nai)) { continue; }
    for ($j = $i + 1; $j < $c; $j++) {
      $naj = $nas[$j];
      if (is_null($naj)) { continue; }
      if ($nai->category != $naj->category) { continue; }
      $nai->localdistancefrom($naj);
      if ($nai->distance < $tooclose) {
        // prefer a way to a node, otherwise take the later one
        canonical::getosmid($naj->id, $type);
        if ($type == 'node') { $nas[$j] = null; } else { $nas[$i] = null; }
      }
    }
  }

  /* we have now culled nearby duplicates, so insert the rest */
  makeallindexes($nas);

  /* insert any placeindexes */
  foreach ($nas as $na) {
    if (is_null($na)) { continue; }
    if ($na->rank > 0) {
      /* only those places I know to be settlements, not things like
         place=wood, place=island and place=airport, or even
         place=country. These will still go in the general index,
         just not inthe place index for determining where a named
         thing is near */
      $placeindex = placeindex::placeindexfromnamed($na);
      $db->insert($placeindex);
    }
  }
}

// --------------------------------------------------
/* inserts indexes for each word relating to the given named
*/
function makeallindexes($nas) {
  global $db;
  $c = count($nas);
  for ($i = 0; $i < $c; $i++) {
    $na = $nas[$i];
    if (is_null($na)) { continue; }
    $words = makeindexes($na);
    if (! empty($words)) { 
      $db->insert($words);
    }
  }
}

// --------------------------------------------------
/* returns array of word objects for a phrase */
function makeindexes($named) {
  static $strangeplurals = array('church'=>'churches',
                                'city'=>'cities',
                                'police'=>'stations',
                                'ruins'=>'',
                                'university'=>'universities');
  /* omit some infos from the index. For example, if we include
   'street' the a search for 'High Street' would return 'High Road' */
  static $exclusions =    array('street'=>TRUE,
                                'trunk road'=>TRUE,
                                'primary road'=>TRUE,
                                'secondary road'=>TRUE,
                                'tertiary road'=>TRUE,
                                'service road'=>TRUE,
                                'motorway link road'=>TRUE,
                                'primary link road'=>TRUE,
                                'secondary link road'=>TRUE,
                                'unclassified road'=>TRUE,
                                'residential street'=>TRUE,
                                'residential track'=>TRUE,
                                'residential trunk road'=>TRUE,
                                'residential primary road'=>TRUE,
                                'residential secondary road'=>TRUE,
                                'residential tertiary road'=>TRUE,
                                'residential unclassified road'=>TRUE,
                                'road'=>TRUE,
                                'place of worship'=>TRUE);

  $info = $named->info;
  $terms = canonical::canonical_with_synonym(
    array_key_exists($info, $exclusions) ?
       $named->name :
       (array_key_exists($info, $strangeplurals) ?
         "{$named->name} [{$info}] [{$strangeplurals[$info]}]" : 
         "{$named->name} [{$info}] [{$info}s]")
  );
  $ordinal = 0;
  $words = array();
  $c = count($terms);
  for ($i = 0; $i < $c; $i++) {
    $firstword = $i == 0 || (! is_array($terms[$i - 1]) && $terms[$i - 1] == '|');
    $lastword = $i == $c - 1 || (! is_array($terms[$i + 1]) && $terms[$i + 1] == '|');
    if (is_array($terms[$i])) {
      foreach ($terms[$i] as $term) {
        if ($term == '') { continue; }
        $words[] = new word($term, $ordinal, $named->region, $named->id, $firstword, $lastword);
      }
    } else {
      $term = $terms[$i];
      if ($term == '') { continue; }
      if ($term != '|') { 
        $firstword = TRUE;
        $words[] = new word($term, $ordinal, $named->region, $named->id, $firstword, $lastword);
      }
    }
    $ordinal++;
  }
  return $words;
}


?>
