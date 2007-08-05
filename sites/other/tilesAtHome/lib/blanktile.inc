<?php
    $NoExitOnDbFail = 1;
    include_once("../connect/connect.php");
    

#------------------------------------------------------------------------------
# This function looks up the blankness entries recursively and returns the 
# blankness type that comes next. It returns NULL in case of error or an array
# (blankness, zoomlevel) which contains the blankness information, and the zoom-
# level that provided that information (so zoomlevel will always be <= z)
# blankness can be 1=sea,2=land,0= no blankness info found
#------------------------------------------------------------------------------
function LookUpBlankTile($X,$Y,$Z,$LayerID){

    $SQL = sprintf("select type from tiles_blank where `x`=%d and `y`=%d and `z`=%d and `layer`=%d limit 1;", 
      $X, $Y, $Z, $LayerID);
    $Result = mysql_query($SQL);
    if(mysql_error()){
      return (NULL);
    }
    
    if(mysql_num_rows($Result) == 0){
        ## no blankness information found, so go one level up
	if($Z < 2)
	  return array (0,$Z);
	else
          return (LookUpBlankTile($X>>1,$Y>>1,$Z-1,$LayerID));
    }
  
    $Data = mysql_fetch_assoc($Result);
    mysql_close();

    return array ($Data["type"], $Z);
}

?>