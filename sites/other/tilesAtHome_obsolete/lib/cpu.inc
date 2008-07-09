<?php

function TestLoad(){
  header("Content-type:text/plain");
  print "Testing GetLoad()\n";
  foreach(array("user","idle","iowait","system") as $Item){
      printf("%s = %1.2f\n", $Item, GetLoad($Item));
  }
}

function GetLoadAvg()
{
  if(!($fp = fopen("/proc/loadavg", "r"))){
    return(-1);
  }
  $Info = fgets($fp, 4096);
  fclose($fp);
  
  # Split into fields
  $Parts  = explode(" ", $Info);

  return($Parts[0]);
}

function GetLoad($Type = "idle"){
  # Get first line from /proc/stat, of the form:
  # "cpu  41644332 829140 7041132 54137011 110995115 807596 568065 0"
  if(!($fp = fopen("/proc/stat", "r"))){
    return(-1);
  }
  $Info = fgets($fp, 4096);
  fclose($fp);
  
  # Split into fields
  $t = array();
  list(
    $label, 
    $t["user"], 
    $t["nice"], 
    $t["system"], 
    $t["idle"], 
    $t["iowait"], 
    $t["irq"], 
    $t["softirq"])  = preg_split("/\s+/", $Info);
  
  # Calculate total
  $Total = 0;
  foreach($t as $name => $val){
    $Total += $val;
  }
  
  # Calcuate requested field as percentage
  $Percent = 100 * $t[$Type] / $Total;
  
  if(0){
    printf("%d of %d = %1.2f in %s", 
      $t["idle"],
      $Total,
      $Percent,
      $label);
    }
    
  return($Percent);
}


?>