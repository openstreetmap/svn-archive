<?php

function logMsg($Text, $Priority){
  $Filename = sprintf("/home/ojw/public_html/Log/Data/messages_p%d.txt", $Priority);
  $fp = fopen($Filename, "a");
  if(!$fp)
    return;
  
  fputs($fp, date("Y-m-d H:i:s ") . $Text . "\n");
}

function logSqlError(){
  if(mysql_errno())
    logMsg(mysql_error(), 2);
}
?>