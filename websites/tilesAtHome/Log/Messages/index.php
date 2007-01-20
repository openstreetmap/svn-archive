<?php
  include("../../lib/gui.inc");
  include("../../connect/connect.php");

  navHeader("Log messages", "../../",0);
  
  $SQL = "select * from tiles_msg order by date desc limit 30;";
  $Result = mysql_query($SQL);
  
  print "<table cellpadding=\"4\" cellspacing=\"0\" border=\"0\">";
  while($Data = mysql_fetch_assoc($Result)){
    $Style="";
    switch($Data["priority"]){
      case 1: $Style="background-color:lightred;color:darkred"; break;
      case 2: $Style="background-color:white;color:darkred"; break;
      case 3: $Style="background-color:white;color:black"; break;
      default: $Style="background-color:white;color:grey"; break;
    }
    
    printf("<tr style=\"%s\"><td>%s</td><td>%s</td></tr>\n",
      $Style,
      $Data["date"],
      htmlentities($Data["text"]));
  }
  print "</table>";

?>