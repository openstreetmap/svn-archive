<?php

include_once ("layers.inc");

#-------------------------------------------------------------------
# Looks at an uploaded directory, and tests whether it is one
# complete tileset
#
# Returns (valid,x,y,layer) as array, where x,y are at zoom-12 and layer is a string
#-------------------------------------------------------------------
function CheckUploadDir($Dir){

  $dp = opendir($Dir);
  if(!$dp){
     print "No such $Dir\n";
     return(array(0,0,0,""));
   }

  $Count = 0;
  $XExpect = -1;
  $YExpect = -1;
  $LExpect = "";

  while($Filename = readdir($dp)){

    if($Filename != ".." && $Filename != "."){
      if(preg_match("/(\w+)_(\d+)_(\d+)_(\d+)\.png/", $Filename, $Matches)){
        $Layer = $Matches[1];
        $Z = $Matches[2];
        $X = $Matches[3];
        $Y = $Matches[4];
      
	list($Valid,$X2,$Y2) = WhichTileset($X,$Y,$Z);
        if($Valid != 1){
          print "Tile not in any tileset: $Filename\n";
          return(array(0,0,0,""));
        }
        printf("%d: %d,%d,%d in layer %s = %d, %d\n", $Count,$X,$Y,$Z,$Layer,$X2,$Y2);
       
        if($Count == 0){
          // First time: store the tileset
          $XExpect = $X2;
          $YExpect = $Y2;
          $LExpect = $Layer;
          $MaxZoom = LayerMaxZoom($Layer);
          $countExpect = layerNumberOfTiles($Layer);

        }
        else{
          // Every other time: check the tileset is same as first one
          if($XExpect != $X2){print "X bad\n"; return(array(0,0,0));}
          if($YExpect != $Y2){print "Y bad\n"; return(array(0,0,0));}
          if($LExpect != $Layer){print "L bad\n"; return(array(0,0,0));}
          if($MaxZoom < $Z){print "Wrong zoom level.\n"; return(array(0,0,0));}
        }

        $Count++;
      }
      else{
        print "$Filename doesn't match";
        return(array(0,0,0,""));
      }
    }
  }

  if($Count != $countExpect){
    print "Too few images ($Count)\, expected $countExpect in layer $Layer.\n";
    return(array(0,0,0,""));
  }
  return(array(1,$X2,$Y2,$Layer,$Count));
}

?>