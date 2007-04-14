<?php
  include("../connect/connect.php");
  set_time_limit(60 * 5);
  $CountA = $CountB = $CountC = $CountD = 0;
  
  $Exists = array();
  $SQL = "select x,y from tiles where `z`=12 and `todo`=0;";
  $Result = mysql_query($SQL);
  while($Data = mysql_fetch_assoc($Result)){
    $Key = sprintf("%d:%d", $Data["x"], $Data["y"]);
    $Exists[$Key] = 1;
    $CountA++;
  }
  
  $Max = pow(2, 12);
  $Size = 4;
  foreach($Exists as $XY=>$Value){
    list($XC,$YC) = explode(":", $XY);
    for($xi = -$Size; $xi <= $Size; $xi++){
      for($yi = -$Size; $yi <= $Size; $yi++){
        $X = $XC + $xi;
        $Y = $YC + $yi;
        if($X >= 0 && $Y > 0 && $X < $Max && $Y < $Max){
          $Key = sprintf("%d:%d", $X,$Y);
          if(!$Exists[$Key]){
            $CountD++;
            CreateRequest($X,$Y,12);
          }
          $CountC++;
        }
      $CountB++;
      }
    }
  }
  
  printf("<p>%d exist, %d potentials of which %d exist... %d chosen (0..%d)</p>", $CountA, $CountB, $CountC, $CountD, $Max);
  
  function CreateRequest($X,$Y,$Z){
    $SQL = sprintf("insert into tiles (`x`,`y`,`z`,`exists`,`todo`) values (%d,%d,%d,0,1);", $X, $Y, $Z);
    mysql_query($SQL);
  }
?>