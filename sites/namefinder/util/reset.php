<?php

/* 

Start from an empty database

 */

session_start(); // only so we get a unique log file

include_once('preamble.php');

$classes = array('named', 'node', 'options', 'placeindex', 
                 'relation_node', 'relation_way', 'relation_relation', 
                 'way_node', 'word');

foreach ($classes as $class) {
  include_once("{$class}.php");
  echo "zapped {$class}\n";
  $db->truncate($class);
}

?>
