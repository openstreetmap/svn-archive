<?php
$layerData = array (
 "name"   => array(1=>"tile", 2=>"poi", 3=>"maplint", 4=>"lowzoom"),
 "dir"    => array(1=>"tile", 2=>"poi", 3=>"maplint", 4=>"tile"),
 "maxZoom"=> array(1=>17, 2=>17, 3=>16, 4=>11),
 "numTileset"=> array(1=>1365, 2=>1365, 3=>341, 4=>1),
);

#----------------------------------------------------
function checkLayer($LayerString){
  global $layerData;
  $ID = array_search($LayerString,$layerData['name']);
  return ($ID != FALSE?$ID:-1);
}

#----------------------------------------------------
function layerValid($Name){
  return($checkLayer($Name) != -1);
}

#----------------------------------------------------
function layerDir($ID){
  global $layerData;
  $data = $layerData['dir'][$ID];
  return ($data != NULL?$data:'');
}

#----------------------------------------------------
function layerName($ID){
  global $layerData;
  $data = $layerData['name'][$ID];
  return ($data != NULL?$data:'');
}

#------------------------------------------------------------------------------
# returns the maximum zoom level we want to be rendered.
# It is passed the "layer name".
# This is currently used to check if a tileset is complete or not.
#------------------------------------------------------------------------------
function layerMaxZoom($layername){
  global $layerData;
  $data = $layerData['maxZoom'][checkLayer($layername)];
  return ($data != NULL?$data:17);
}

#------------------------------------------------------------------------------
# Returns the number of tiles which make a complete tileset per layer
# this varies as not all layers have all zoom levels
# it's handed the "layername" of the layer
#------------------------------------------------------------------------------
function layerNumberOfTiles($layername){
  global $layerData;
  $data = $layerData['numTileset'][checkLayer($layername)];
  return ($data != NULL?$data:1365);
}
?>