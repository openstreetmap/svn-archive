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
      return("Tiles");
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


?>