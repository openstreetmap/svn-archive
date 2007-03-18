<?php

$Start = microtime(1);

usleep(2500000);

$End = microtime(1);

printf("Took %1.3f seconds", $End-$Start);

?>