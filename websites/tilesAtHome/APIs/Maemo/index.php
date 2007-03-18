<?php
$X = $_GET["x"];
$Y = $_GET["y"];
$Z = 17-$_GET["zoom"];

include("../../lib/tilenames.inc");

showImg($X,$Y,$Z);

function showImg($X,$Y,$Z){
  $Name = TileName($X,$Y,$Z);
  if(!$Name){
    header("HTTP/1.0 404 Not available");
    return;
  }
  
  header("Content-type: image/PNG");
  readfile($Name);
}
?>