<?php

function checkUser($SuppliedUsername, $SuppliedPassword){
  $fp = fopen(userListFilename(), "r");
  if(!$fp){
    return(-1);
  }
  $SuppliedPasswordHash = md5($SuppliedPassword);
  
  while(($Line = fgets($fp, 100)) !== false){
    if(preg_match("/(\d+)\|(\w+)\|(\w{32})/", $Line, $Matches)){
      if(strlen($Matches[3]) == 32){
        if($Matches[2] == $SuppliedUsername){
          if($Matches[3] == $SuppliedPasswordHash){
            fclose($fp);
            return($Matches[1]);
          }
        }
      }
    }
  }

  fclose($fp);
  return(-1);
}

function lookupUser($UserID){
  $fp = fopen(userListFilename(), "r");
  if(!$fp){
    return(-1);
  }
  
  while(($Line = fgets($fp, 100)) !== false){
    if(preg_match("/(\d+)\|(\w+)\|(\w{32})/", $Line, $Matches)){
      if($Matches[1] == $UserID){
        fclose($fp);
        return($Matches[2]);
      }
    }
  }
  
  fclose($fp);
  return("");

}

function userListFilename(){
  return("/home/ojw/public_html/Data/Users/user_pw.txt");
}
?>