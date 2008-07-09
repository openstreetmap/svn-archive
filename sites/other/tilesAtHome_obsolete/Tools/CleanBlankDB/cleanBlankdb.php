<?php

include_once("../../connect/connect.php");
include_once("../../lib/blanktile.inc");

removeRedundantBlankEntries();

function removeRedundantBlankEntries() {
 $deleted = 0;

 if (!mysql_query('SET AUTOCOMMIT = 0;')) {print mysql_error();exit;}

 $res=mysql_query('select max(x) from tiles_blank;');
 $row = mysql_fetch_row($res);
 $max_x = $row[0];


for ($x=$max_x; $x>=0; --$x) {
  $i=0;
  $SQL="SELECT x,y,z,layer,type from tiles_blank WHERE x=$x;";
  $res = mysql_query($SQL);
  if ($x % 10 == 0) print "x $x, ".($rows=mysql_num_rows($res))." tiles. ";
  if ($rows) {sleep (.1);};
  if (!mysql_query('START TRANSACTION;')) {print mysql_error();exit;}
  while ($row=mysql_fetch_assoc($res)) {
     $blank=$row[type];
     $blank2=LookupBlankTile($row[x]>>1,$row[y]>>1,$row[z]-1,$row[layer]);
     #print("\n($row[x],$row[y],$row[z],$row[layer]): ".$blank." (".$blank2.").");
     if ($blank==$blank2) {
       $SQL="DELETE FROM tiles_blank WHERE x=$row[x] and y=$row[y] and z=$row[z] and layer=$row[layer];";
       ++$i;
       $res2 = mysql_query($SQL);
       if (!$res2) print mysql_error();
       if(!mysql_affected_rows()) print "Error when deleting: $SQL\n";
     }
   }
  $deleted += $i; if ($x % 10 == 0) print "$i deleted\n";
  mysql_free_result($res);
  if (!mysql_query('COMMIT;')) {print mysql_error();exit;}
}



for ($z=0; $z<=17; ++$z) {
 $SQL="SELECT z,COUNT(*) FROM tiles_blank WHERE z=$z;";
 $res = mysql_query($SQL);
 while ($row=mysql_fetch_assoc($res)) {
   print_r($row);
 }
 mysql_free_result($res);
}

 $SQL="SET AUTOCOMMIT = 1;";
 mysql_query($SQL);

 print "deleted $deleted entries";
}
?>
