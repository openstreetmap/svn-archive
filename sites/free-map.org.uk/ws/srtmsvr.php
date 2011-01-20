<?php

require_once('getData.php');
require_once('../lib/functionsnew.php');
require_once('../lib/GoogleProjection.php');

header("Content-type: application/json");
$cleaned = clean_input($_REQUEST);
$x = $cleaned["x"];
$y = $cleaned["y"];
$z = $cleaned["z"];
$goog = new GoogleProjection();
list($w,$s) = $goog->fromPixelToLL($x*256,($y+1)*256,$z);
list($e,$n) = $goog->fromPixelToLL(($x+1)*256,$y*256,$z);
getSRTM($w,$s,$e,$n);


?>
