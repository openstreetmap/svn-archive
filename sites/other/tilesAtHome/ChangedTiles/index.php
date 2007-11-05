<?php

  $handle = fopen("http://www.openstreetmap.org/api/0.5/changes?hours=4", "r");
  $contents = stream_get_contents($handle);
  fclose($handle);

  $xmlReader = new XMLReader();
  $xmlReader->XML($contents);

  include("../connect/connect.php");
  include("../lib/requests.inc");
  
  while($xmlReader->read()){
    if($xmlReader->name == tile){
      $X = $xmlReader->getAttribute("x");
      $Y = $xmlReader->getAttribute("y");
      $Z = $xmlReader->getAttribute("z");
      $Changes = $xmlReader->getAttribute("changes");

      print "X=$X Y=$Y Z=$Z numchanges=$Changes -- ";

      if (!$Z) {$Z=12;};

      $P = "2";                       ## TODO: calculate based on num_changes
      $Src = "deelkar:changedTiles";  ## TODO: Make configurable
  
      if($Z != 12 && $Z != 8){
        print "Invalid Z\n";
        exit;
      }
      $Max = pow(2,$Z);
      if($X < 0 || $Y < 0 || $X >= $Max || $Y >= $Max){
        print "Invalid XY\n";
        exit;
      }
  
      if($P < 1 || $P > 3){
        print "Invalid P\n";
        exit;
      }
  
      if (!requestExists($X,$Y,$Z,NULL)){

         $SQL = sprintf(
           "INSERT into tiles_queue (`x`,`y`,`z`,`status`,`src`,`date`,`priority`,`ip`) values (%d,%d,%d,%d,'%s',now(),%s,'%s');", 
           $X, 
           $Y,
           $Z,
           REQUEST_PENDING,
           mysql_escape_string($Src),
           $P,
           $_SERVER['REMOTE_ADDR']);
    
         mysql_query($SQL);
         if(mysql_error()){
           printf("Database error %s\n", mysql_error());
           exit;
         }
         print "OK\n";
      } else {
         print "Already in queue\n";
      }

    }
  }

?>
