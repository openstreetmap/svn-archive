<?php
header('Content-type: application/xml');
// validate key and value
$key=$_GET['key'];
$value=$_GET['value'];
$key   = urlencode(urldecode($key));
$value = urlencode(urldecode($value));

// get the xml .osm data
readfile('http://www.informationfreeway.org/api/0.6/*[' . $key .'='. $value .']');
?>
