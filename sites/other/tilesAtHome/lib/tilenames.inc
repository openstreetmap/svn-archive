<?php

function TileName($X,$Y,$Z,$Layer="tile"){
  if(!$Layer)
    return("");
  if(!TileValid($X,$Y,$Z))
    return("");

    $BaseDir = "/mnt/agami/openstreetmap/tah/Tiles";

  $Filename = $BaseDir . "/" . $Layer . "/" . TilePartName($X,$Y,$Z);
  return($Filename);
}

function TileURL($X,$Y,$Z,$Layer="tile"){
  if(!TileValid($X,$Y,$Z))
    return("");
  return(sprintf("http://tah.openstreetmap.org/Tiles/%s/%d/%d/%d.png",$Layer,$Z,$X,$Y));
}
  
function TilePartName($X,$Y,$Z){
  if(!TileValid($X,$Y,$Z))
    return("");
  return sprintf(
    "%02d/%03d/%03d/%03d/%03d.png",
    $Z,
    $X/1000, 
    $X%1000,
    $Y/1000,
    $Y%1000);
}

#----------------------------------------------------------------------
# Gets the tileset this tile is in
#----------------------------------------------------------------------
function WhichTileset($X,$Y,$Z){
  if($Z < 12 || $Z > 20)
    return(array(0,0,0));
  if($Z == 12)
    return(array(1,$X,$Y));
  
  $X2 = $X >> ($Z - 12);
  $Y2 = $Y >> ($Z - 12);
  
  return(array(1, $X2, $Y2));
}

#----------------------------------------------------------------------
# Tests if a tile coordinate is valid
#
# See [[Slippy Map Tilenames]] on openstreetmap wiki for details
#----------------------------------------------------------------------
function TileValid($X,$Y,$Z){
  
  # Zoom depth is limited to 17
  if($Z > 17)
    return(0);
    
  if($Z < 0)
    return(0);
    
  # Check that the specified x,y exist at this zoom level
  if($X < 0 || $Y < 0)
    return(0);
  $Limit = pow(2,$Z);
  if($X >= $Limit || $Y >= $Limit)
    return(0);
  return(1);
}
?>
