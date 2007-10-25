<?php

function checkVersion($VersionString){

  # x_ prefix means an experimental client (ID=11) which is default-accept
  if(substr($VersionString, 0, 2) == "x_"){
    return(11);
  }

  # Look in the list of versions
  $fp = fopen(versionFilename(), "r");
  if(!$fp){
    return(-1);
  }
  while(($Line = fgets($fp, 100)) !== false){
    if(preg_match("/(\d+)\|(\w+)\|(\w+)/", $Line, $Matches)){
        if($Matches[3] == $VersionString){
          fclose($fp);

          # Disabled versions
          if($Matches[2] != "OK"){
            return(-2);
          }
 
          # Version name found, return version number
          return($Matches[1]);
        }
      }
    }

  fclose($fp);

  # None found
  return(-1);
}

function getVersions(){
  $fp = fopen(versionFilename(), "r");
  if(!$fp){
    return(Array());
  }
  $Versions = Array();
  while(($Line = fgets($fp, 100)) !== false){
    if(preg_match("/(\d+)\|(\w+)\|(\w+)/", $Line, $Matches)){
      $Versions[$Matches[1]]["name"] = $Matches[3];
      $Versions[$Matches[1]]["ok"] = ($Matches[2] == "OK" ? 1 : 0);
    }
  }
  fclose($fp);
  return($Versions);
}

function versionName($ID){
  $Versions = getVersions();
  $Version = $Versions[$ID]["name"];
  if(!$Version)
    return("unknown");
  return($Version);
}

function versionFilename(){
  return("/home/ojw/public_html/Data/Versions/versions.txt");
}

?>