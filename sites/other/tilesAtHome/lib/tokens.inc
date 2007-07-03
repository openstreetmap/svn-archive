<?php

function GetTokens($UserID = -1, $Secret = "secret"){
  $Interval = 10 * 60;
  $Time = time();
  
  $Timeslot1 = floor($Time / $Interval);
  $Timeslot2 = $Timeslot1 - 1;

  $Token1 = Token($Timeslot1, $UserID, $Secret);
  $Token2 = Token($Timeslot2, $UserID, $Secret);

  return(array($Token1, $Token2));
}

function Token($Time, $UserID, $Secret){
  return(md5(sprintf("%s_%d_%d", $Secret, $Time, $UserID)));
}
?>
