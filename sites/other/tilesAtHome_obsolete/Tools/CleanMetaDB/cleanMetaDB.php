<?php

include_once("../../connect/connect.php");

removeOlderMetaData();

function removeOlderMetaData() {
 $total = 0;
 
 $res=mysql_query('select max(x) from tiles_meta WHERE z=12;');
 $row = mysql_fetch_row($res);
 $max_x = $row[0];


  for ($x=2260; $x<=$max_x; ++$x) {
    $SQL="SELECT y,type as layer, date from tiles_meta WHERE x=$x and z=12 AND tileset=1;";
    $res = mysql_query($SQL);
    print "x $x, ".($rows=mysql_num_rows($res))." tiles. ";
    #Sleep(1);
    if (!mysql_query('START TRANSACTION;')) {print mysql_error();exit;}
    while ($row=mysql_fetch_assoc($res)) {
      $i = checkTilesAndDelete($x,$row[y],$row[layer],$row[date],12);
     
      print ".";
    }
    print "$i deleted\n";

    mysql_free_result($res);
   }

 #Clean up
 print "Total number of deleted entries $total.\n";
 mysql_query($SQL);
}

function checkTilesAndDelete($x,$y,$layer,$date,$z){
 if ($z>17) return (0);

 $x_min = $x * pow(2, $z-12);
 $x_max = ($x+1) * pow(2, $z-12) - 1;
 $y_min = $y * pow(2, $z-12);
 $y_max = ($y+1) * pow(2, $z-12) - 1;
 $deleted = 0;

 $SQL = "SELECT x,y,z,type as layer,date from tiles_meta WHERE z=$z AND type = $layer AND x >= $x_min and x <= $x_max and y >= $y_min and y <= $y_max AND tileset=0 and date<='$date';";
 if (!$res = mysql_query ($SQL)) {print mysql_error();exit;}
 while ($row=mysql_fetch_assoc($res)) {
   #print("\n$x,$y,$layer $date: ($row[x],$row[y],$row[z],$row[layer]): $row[date]\n");
   $SQL="DELETE FROM tiles_meta WHERE x=$row[x] and y=$row[y] and z=$row[z] and type=$row[layer] AND tileset=0;";
   ++$deleted;
   $res3 = mysql_query($SQL);
   if (!$res3) {print mysql_error();exit;}
   if(!mysql_affected_rows()) {print "Error when deleting: $SQL\n"; exit; }
 }
 if ($res) {mysql_free_result($res);}

 return (checkTilesAndDelete($x,$y,$layer,$date,$z+1) + $deleted);
}
?>
