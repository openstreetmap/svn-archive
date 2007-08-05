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
  
  # We counted both textfiles and zipfiles, 
  # which are parts of the same upload
  $Count = $Count / 2;
  
  return($Count);
}

function QueueDirectory(){
  return("/home/ojw/tiles-ojw/Queue/");
}

function MaxQueueLength(){
  return(300);
}

?>