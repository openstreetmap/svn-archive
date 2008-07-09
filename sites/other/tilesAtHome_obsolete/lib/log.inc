<?php
require_once('config.inc.php');


function logMsg($Text, $Priority){
  global $conf;  

  $Filename = sprintf("%s/messages_p%d.txt", $conf['LOGDIR'], $Priority);
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