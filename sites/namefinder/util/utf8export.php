<?php

include_once('preamble.php');
include_once('options.php');
include_once('named.php');
include_once('placeindex.php');
include_once('canon.php');

$chunk = 1000;

echo "TRUNCATE `named`;\n";
echo "TRUNCATE `options`;\n";
echo "TRUNCATE `placeindex`;\n";
echo "TRUNCATE `canon`;\n";

$a = "'";
$count = 0;
for ($start = 0; ; $start += $chunk) {
  
  $lastcount = $count;
  $named = new named();
  $q = $db->query();
  $q->limit($chunk, $start);
  $inserts = array();

  while ($q->select($named) > 0) {
    $count++;
    $inserts[] = clone $named;
  }

  if (count($inserts) > 0) {
    echo "INSERT INTO `named` VALUES";
    $prefix = "\n";
    for($i = 0; $i < count($inserts); $i++) {
      echo $prefix, '(', 
        $inserts[$i]->id, ',',
        $inserts[$i]->region, ',',
        $inserts[$i]->lat, ',',
        $inserts[$i]->lon, ',',
        $a, str_replace("'", "\\'", str_replace('\\', '\\\\', $inserts[$i]->name)), $a, ',',
        $a, str_replace("'", "\\'", str_replace('\\', '\\\\', $inserts[$i]->category)), $a, ',',
        $a, str_replace("'", "\\'", str_replace('\\', '\\\\', $inserts[$i]->is_in)), $a, ',',
        $inserts[$i]->rank, ',',
        $a, str_replace("'", "\\'", str_replace('\\', '\\\\', $inserts[$i]->info)), $a, ')';
      $prefix = ",\n";
    }
    echo ";\n";
  }
  if ($count == $lastcount) { break; }
}

$count = 0;
for ($start = 0; ; $start += $chunk) {
  
  $lastcount = $count;
  $placeindex = new placeindex();
  $q = $db->query();
  $q->limit($chunk, $start);
  $inserts = array();

  while ($q->select($placeindex) > 0) {
    $count++;
    $inserts[] = clone $placeindex;
  }

  if (count($inserts) > 0) {
    echo "INSERT INTO `placeindex` VALUES";
    $prefix = "\n";
    $a = "'";
    for($i = 0; $i < count($inserts); $i++) {
      echo $prefix, '(', 
        $inserts[$i]->id, ',',
        $inserts[$i]->region, ',',
        $inserts[$i]->lat, ',',
        $inserts[$i]->lon, ',',
        $inserts[$i]->rank, ')';
      $prefix = ",\n";
    }
    echo ";\n";
  }
  if ($count == $lastcount) { break; }
}

$count = 0;
for ($start = 0; ; $start += $chunk) {
  
  $lastcount = $count;
  $canon = new canon();
  $q = $db->query();
  $q->limit($chunk, $start);
  $inserts = array();

  while ($q->select($canon) > 0) {
    $count++;
    $inserts[] = clone $canon;
  }

  if (count($inserts) > 0) {
    echo "INSERT INTO `canon` VALUES";
    $prefix = "\n";
    $a = "'";
    for($i = 0; $i < count($inserts); $i++) {
      echo $prefix, '(', 
        $inserts[$i]->named_id, ',',
        $a, str_replace("'", "\\'", str_replace('\\', '\\\\', $inserts[$i]->canon)), $a, ')';
      $prefix = ",\n";
    }
    echo ";\n";
  }
  if ($count == $lastcount) { break; }
}

$count = 0;
for ($start = 0; ; $start += $chunk) {
  
  $lastcount = $count;
  $options = new options();
  $q = $db->query();
  $q->limit($chunk, $start);
  $inserts = array();

  while ($q->select($options) > 0) {
    $count++;
    $inserts[] = clone $options;
  }

  if (count($inserts) > 0) {
    echo "INSERT INTO `options` VALUES";
    $prefix = "\n";
    $a = "'";
    for($i = 0; $i < count($inserts); $i++) {
      echo $prefix, '(', 
        $inserts[$i]->id, ',',
        $a, str_replace("'", "\\'", str_replace('\\', '\\\\', $inserts[$i]->name)), $a, ',',
        $a, str_replace("'", "\\'", str_replace('\\', '\\\\', $inserts[$i]->value)), $a, ')';
      $prefix = ",\n";
    }
    echo ";\n";
  }
  if ($count == $lastcount) { break; }
}


?>