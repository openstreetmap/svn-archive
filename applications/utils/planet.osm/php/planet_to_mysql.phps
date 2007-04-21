<?php

/**
 * Code to read in the monthly data dump from OpenStreetMap (planet.osm) and insert into MySQL database
 *   by Dan Karran - dan@karran.net
 */

// database connection to go in separate file:
include("globals.php");

// read the file in and run regular expressions to put the nodes and segments into an array
print "Opening file...\n";
$filename = "planet.osm";
$file = fopen($filename, "r");
while (!feof($file)) {
  $contents .= fread($file, 8092);
}
preg_match_all("<node id=\"(.*)\" lat=\"(.*)\" lon=\"(.*)\">", $contents, $nodes[]);
preg_match_all("<segment id=\"(.*)\" from=\"(.*)\" to=\"(.*)\">", $contents, $segments[]);
fclose($file);

// write the nodes and segments to database
print "Writing nodes... ";
$index = 0;
$result = mysql_query("DELETE FROM nodes");
foreach ($nodes[0][1] as $node) {
  $sql = "INSERT INTO nodes (id, latitude, longitude) VALUES (".$nodes[0][1][$index].','.$nodes[0][2][$index].','.$nodes[0][3][$index].")";
  $result = mysql_query($sql);
  $index++;
}
print $index . "\n";

print "Writing segments... ";
$index = 0;
$result = mysql_query("DELETE FROM segments");
foreach ($segments[0][1] as $segment) { 
  $sql = "INSERT INTO segments (id, node_a, node_b) VALUES (".$segments[0][1][$index].','.$segments[0][2][$index].','.$segments[0][3][$index].")";
  $result = mysql_query($sql); 
  $index++; 
}
print $index . "\n";
