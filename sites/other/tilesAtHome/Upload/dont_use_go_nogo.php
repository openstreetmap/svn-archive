<?php

  header("Content-type: text/plain");
  include("../lib/queue.inc");
  include("../lib/tokens.inc");
  
  $Value = QueueLength();
  $Min = 10;
  $Max = MaxQueueLength();

  $SpaceUsed = QueueSpace();
  $MaxSpace = MaxQueueSpace();

  $Portion = ($Max - $Value) / ($Max - $Min);
  if($Portion < 0) $Portion = 0;
  if($Portion > 1) $Portion = 1;

  $PortionSize = ($MaxSpace - $SpaceUsed) / $MaxSpace;
 
  $Message = "First line is always a number from 0 (stop) to 1 (full speed). \nSecond line is an upload token, as text. \nSubsequent lines may contain human-readable text.";
  list($Token1, $Token2) = GetTokens(-1, "testing");

  if($PortionSize > $Portion){

    printf("%1.2f\n%s\n%s\n%s %1.2f. trying to keep between %1.2f and %1.2f\n", 
      $PortionSize,
      $Token1,
      $Message,
      "Queue disk space",
      $SpaceUsed,
      0,
      $MaxSpace);

  } else {

    printf("%1.2f\n%s\n%s\n%s %1.2f. trying to keep between %1.2f and %1.2f\n", 
      $Portion,
      $Token1,
      $Message,
      "Queue length",
      $Value,
      $Min,
      $Max);
  }
?>
