<?php

function QueueLength(){
  $dp = opendir(QueueDirectory());
  if(!$dp)
    AbortWithError(500, "Can't read queue directory");
    
  while($File = readdir($dp)){
    if(substr($File,0,1) != "."){
      $Count++;
    }
  }
  closedir($dp);
  return($Count);
}

function QueueDirectory(){
  return("/home/ojw/tiles-ojw2/Queue/");
}
?>