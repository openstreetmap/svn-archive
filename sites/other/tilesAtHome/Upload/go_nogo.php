<?php

  header("Content-type: text/plain");
  include("../lib/queue.inc");
  
  $Value = QueueLength();
  $Min = 10;
  $Max = 500;
  
  $Portion = ($Max - $Value) / ($Max - $Min);
  if($Portion < 0) $Portion = 0;
  if($Portion > 1) $Portion = 1;

  $Message = "First line is always a number from 0 (stop) to 1 (full speed). Subsequent lines may contain human-readable text.";
  
  printf("%1.2f\n%s\n%s %1.2f. trying to keep between %1.2f and %1.2f\n", 
    $Portion,
    $Message,
    "Queue length",
    $Value,
    $Min,
    $Max);

?>