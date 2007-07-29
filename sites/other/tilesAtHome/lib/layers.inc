<?php

function checkLayer($LayerString){
  switch($LayerString){
    case "Tile":
    case "Tiles":
    case "tile":
      return(1);
    case "cycle":
      return(2);
    case "maplint":
      return(3);
    case "landsea":
      return(4);
    default:
      return(-1);
    }
}

function layerValid($Name){
  return($checkLayer($Name) != -1);
}

function layerDir($ID){
  switch($ID){
    case 1:
      return("tile");
    case 2:
      return("cycle");
    case 3:
      return("maplint");
    case 4:
      return("landsea");
    default:
      return("");
    }
}

function layerName($ID){
  switch($ID){
    case 1:
      return("tile");
    case 2:
      return("cycle");
    case 3:
      return("maplint");
    default:
      return("");
    }
}


#------------------------------------------------------------------------------
# returns the maximum zoom level we want to be rendered.
# It is passed the "layer name".
# This is currently used to check if a tileset is complete or not.
#------------------------------------------------------------------------------
function layerMaxZoom($layername){
  switch (checkLayer($layername)){
    case 1:  return (17);
    case 3:  return(16);
    default: return(17);
  }
}


#------------------------------------------------------------------------------
# Returns the number of tiles which make a complete tileset per layer
# this varies as not all layers have all zoom levels
# it's handed the "layername" of the layer
#------------------------------------------------------------------------------
function layerNumberOfTiles($layername){
  switch (checkLayer($layername)){
    case 1:  return (1365);
    case 3:  return(341);
    default: return(1365);
  }
}
?>