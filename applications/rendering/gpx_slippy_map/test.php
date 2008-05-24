<?php

$a = 1.23456789;
$b = 9.87654321;

$c = pack("dd", $a, $b);

list($spare,$aa, $bb) = unpack("d2", $c);
$cc = unpack("d2", $c);

header("Content-type:text/plain");
print_r(gd_info());

print_r($cc);

print "\n\n$aa, $bb\n\n";
printf("%d len\n", strlen($c));
 
?>