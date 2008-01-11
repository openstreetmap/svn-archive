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
  return("/mnt/agami/openstreetmap/tah/Incoming/");
}

function MaxQueueLength(){
  return(1500);
}

function QueueSpace(){
  $QueueDir=QueueDirectory();
  $bytes=`du $QueueDir | awk '{ print $1 }'`; # FIXME: use php instead of awk
  return($bytes * 1024); 
}

function MaxQueueSpace(){
  return(1024*1024*1024); # 1 GiB
}

?>
