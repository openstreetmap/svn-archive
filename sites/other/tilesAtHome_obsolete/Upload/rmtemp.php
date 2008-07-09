<?php
include("../connect/connect.php");
include("../lib/log.inc");
logMsg("Deleting temporary directories", 3);

header("content-type:text/plain");
clearTemp("temp");


#----------------------------------------------------------------------
# Deletes any old temporary directories
# (these may be caused by problems in the upload script, so if this
#  script needs to be run often, consider auditing Upload/tile2.php)
#----------------------------------------------------------------------
function clearTemp($dir){
  $countA = $countB = 0;
  $dp = opendir($dir);
  while(($file = readdir($dp)) !== false){
    $countA++;
    $filename = $dir."/".$file;
    
    if(preg_match("/^\w{32}$/",$file)){
      if(filemtime($filename) < (time() - 3600)){
        DelDir($filename);
        print "Deleted $filename\n";
        $countB++;
      }
    }
  }
  
  closedir($dp);
  
  printf("Done, deleted %d directories of %d\n", $countB, $countA);
}


#----------------------------------------------------------------------
# Delete a directory and everything inside (not recursive)
#----------------------------------------------------------------------
function DelDir($Dir){
  $dp = opendir($Dir);
  while(($file = readdir($dp)) !== false){
    if($file != "." && $file != ".."){
      $Filename = "$Dir/$file";
      unlink($Filename);
    }
  }
  closedir($dp);
  rmdir($Dir);
}

?>