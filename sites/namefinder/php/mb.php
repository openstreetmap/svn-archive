<?php

/* Provides replacements for the mb_ functions needed when PHP is compiled without them; 
   loaded in preamble.php if necessary  */

function mb_strlen($s, $utf8) {
  $c = 0;
  for($i = 0; $i < strlen($s); $i++) {
    $c++;
    $o = ord($s{$i});
    if (($o & 0xF8) == 0xF0) { $i += 3; } 
    else if (($o & 0xF0) == 0xE0) { $i += 2; } 
    else if (($o & 0xE0) == 0xC0) { $i += 1; } 
  }
  return $c;
}

function mb_substr($s, $f, $l, $utf8) {
  $c = 0;
  for($i = 0; $i < strlen($s); $i++) {
    if ($f == $c) { $sf = $i; }
    if ($f + $l == $c) { break; }
    $c++;
    $o = ord($s{$i});
    if (($o & 0xF8) == 0xF0) { $i += 3; } 
    else if (($o & 0xF0) == 0xE0) { $i += 2; } 
    else if (($o & 0xE0) == 0xC0) { $i += 1; } 
  }
  $sl = $i - $sf; 
  return substr($s, $sf, $sl);  
}

?>