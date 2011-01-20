<?php

// Rendered ways server
// Provides ways straight from the database, optimised for rendering
// (i.e. coordinates)

require_once('../lib/functionsnew.php');
require_once('../lib/GoogleProjection.php');
require_once('getData.php');

// 4079 2740 13

// Input:
// poi=[comma separated tag list]
// way=[comma separated tag list]
// bbox=[bbox, in latlon]

$cleaned = clean_input($_REQUEST);
$cleaned["format"] = (isset($cleaned["format"])) ? $cleaned["format"]:"xml";
$cleaned["poi"] = (isset($cleaned["poi"])) ? $cleaned["poi"]:"all";
$cleaned["way"] = (isset($cleaned["way"])) ? $cleaned["way"]:"all";

$x = $cleaned["x"];
$y = $cleaned["y"];
$z = $cleaned["z"];
$goog = new GoogleProjection();
$values = array();
list($w,$s) = $goog->fromPixelToLL($x*256,($y+1)*256,$z);
list($e,$n) = $goog->fromPixelToLL(($x+1)*256,$y*256,$z);
//echo "latlon: $w $s $e $n";
getData($w,$s,$e,$n,$cleaned);
?>
