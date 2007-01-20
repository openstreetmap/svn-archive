<?php

function logMsg($Text, $Priority){
  
  $SQL = sprintf("insert delayed into tiles_msg (`text`,`priority`) values ('%s',%d);", 
    mysql_escape_string($Text), 
    $Priority);
  mysql_query($SQL);
}

function logSqlError(){
  if(mysql_errno())
    logMsg(mysql_error(), 2);
}
?>