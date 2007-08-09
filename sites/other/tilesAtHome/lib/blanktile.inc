<?php

#------------------------------------------------------------------------------
# This function looks up the blankness entries recursively and returns the 
# blankness type that comes next. It returns NULL in case of error or the
# blankness type which contains the blankness information
# blankness can be 1=sea,2=land,0= no blankness info found
#------------------------------------------------------------------------------
function LookUpBlankTile($X,$Y,$Z,$LayerID){
    $NoExitOnDbFail = 1; 
    if (file_exists("../connect/connect.php")) include_once("../connect/connect.php");
    if (file_exists("../../connect/connect.php")) include_once("../../connect/connect.php");
    if (!DbSuccess) return NULL;

    $SQL = sprintf("select type from tiles_blank where `x`=%d and `y`=%d and `z`=%d and `layer`=%d limit 1;",
	$X, $Y, $Z, $LayerID);
    $Result = mysql_query($SQL);
    if(mysql_error()){
      return (NULL);
    }
    
    if(mysql_num_rows($Result) == 0){
        ## no blankness information found, so go one level up
	if($Z < 2)
	  return (0);
	else
          return (LookUpBlankTile($X>>1,$Y>>1,$Z-1,$LayerID));
    }
  
    $blank = mysql_fetch_assoc($Result);
    // Don't close MySQL connection, we might need it later on
    //mysql_close();

    return $blank["type"];
}



#------------------------------------------------------------------------------
# This function insert new blankness information if it is necessary
# blankness can be 1=sea,2=land. It returns FALSE on error, or TRUE on success
#------------------------------------------------------------------------------
function InsertBlankTile($X,$Y,$Z,$LayerID,$UserID,$Blanktype){
   if (($Blanktype < 1) or ($Blanktype > 2)) return FALSE;

   if (LookUpBlankTile($X,$Y,$Z,$LayerID) == $Blanktype) {
     # "info already there, nothing to do\n";
     return TRUE;
   }

   #otherwise we really need to insert stuff
   $NoExitOnDbFail = 1; 
   if (file_exists("../connect/connect.php")) include_once("../connect/connect.php");
   if (file_exists("../../connect/connect.php")) include_once("../../connect/connect.php");
   if (!DbSuccess) return FALSE;
   $Fields = "x, y, z, layer, date, user, type";
   $Values = sprintf("%d, %d, %d, %d, now(), %d, %d", $X, $Y, $Z, $LayerID, $UserID, $Blanktype);
   $SQL = sprintf("REPLACE INTO `tiles_blank` (%s) values (%s);", $Fields, $Values);
   return mysql_query($SQL);
}

?>