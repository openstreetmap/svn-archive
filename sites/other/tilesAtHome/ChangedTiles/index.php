<?php

  $handle = fopen("http://www.openstreetmap.org/api/0.5/changes?hours=6", "r");
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
           "INSERT into tiles_queue (`x`,`y`,`z`,`status`,`src`,`date`,`priority`,`ip`, `request_date`) values (%d,%d,%d,%d,'%s',now(),%s,'%s', now());", 
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
        print "Already in queue: ";
        if ($P != 3) {
        # Check existing priority, and increase priority if requested/allowed priority is higher.
        # There should only ever be one row returned here, so the 'max' is just a safety case.
        $SQL = sprintf("SELECT priority as p FROM tiles_queue WHERE `x`=%d AND `y`=%d AND `z`=%d AND `status`!=%d ",
                $X, $Y, $Z, REQUEST_DONE);
        $Result = mysql_query($SQL);
        if ($row = mysql_fetch_assoc($Result) and $row['p'] > $P) {
            $SQL = sprintf("UPDATE `tiles_queue` SET `priority`=%d WHERE `x`=%d AND `y`=%d AND `z`=%d AND `status`!=%d",
              $P, $X, $Y, $Z, REQUEST_DONE);
            mysql_query($SQL);
            if(mysql_error()) {
               printf("Database error %s ", mysql_error());
            } else {
                print "Updated priority from ".$row['p'] ." to $P ";
            }
        } else {
          print "Current priority is ".$row['p'].".";
        }
        print "\n";
      }
    }
  }
}
?>
