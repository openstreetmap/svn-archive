<?php
  
  include("../lib/tilenames.inc");
  include("../lib/layers.inc");
  include("../lib/users.inc");
  include("../lib/versions.inc");
  
  
  $Layer = $_GET["layer"];
  $Z = $_GET["z"];
  $X = $_GET["x"];
  $Y = $_GET["y"];
  // with default layer
  if($Layer=="") $Layer="tile";

  // Check x,y,z is valid
  if(!TileValid($X,$Y,$Z)){
    dieMsg("Invalid tile coordinates");
    }
  
  // Lookup layer, and check is valid
  $LayerID = checkLayer($Layer);
  if($LayerID < 0){
    dieMsg("Invalid layer");
    }

  include("../connect/connect.php");

  print "OK | $X | $Y | $Z | $Layer ";


  // look on filesystem
  if ($FileExists = SearchFilesystem($X,$Y,$Z,$LayerID)) {
  	$Data = SearchMetaDB($X,$Y,$Z,$LayerID,0);  
	// Look for a complete tileset and use the newer entry
  	if($Z > 12){
    	  list($Valid,$X12,$Y12) = WhichTileset($X,$Y,$Z);
    	  if($Valid){
      	    if ($TilesetData = SearchMetaDB($X12,$Y12,12,$LayerID,1)){
	      if ($TilesetData['date'] > $Data['date']) {
	        $Date = $TilesetData;
	      }
      	    }
    	  }
	}
  }
  PrintMetaInfo($Data);
  if (!$FileExists) {
	// look for blank tiles
	SearchBlankTiles($X,$Y,$Z,$LayerID);
  }
  
  // Look for a file on the filesystem
  function SearchFilesystem($X,$Y,$Z,$LayerID){
    $LayerName = LayerDir($LayerID);
    $Filename = TileName($X,$Y,$Z,$LayerName);
    $ActualSize = 0;
    $ActualDate = 0;
    $FileExists = 0;

    if(file_exists($Filename)){
      $ActualSize = filesize($Filename);
      $ActualDate = filemtime($Filename);
      $FileExists = 1;
     }

    printf("| bytes %d | modified %s ", 
        $ActualSize,date("Y-m-d H:i:s", $ActualDate));  
    return $FileExists;
  }
  
  function SearchMetaDB($X,$Y,$Z,$LayerID,$RequireTileset){
    $Data = MetaInfo($X,$Y,$Z,$LayerID);
    if(!$Data["valid"]){
      //No entry in meta db
      return(NULL);
    }
    if($RequireTileset && $Data["tileset"] != 1){
      //None found
      return(NULL);
    }
    return($Data);
  }
      
  function PrintMetaInfo($Data){
    printf(
      "| uploaded %s | user %d (%s) | version %d (%s)",
      $Data["date"],
      $Data["user"],
      htmlentities(lookupUser($Data["user"])),
      $Data["version"],
      htmlentities(versionName($Data["version"])));
  }
  
  function MetaInfo($X,$Y,$Z,$LayerID){
    $SQL = sprintf("select * from tiles_meta where `x`=%d and `y`=%d and `z`=%d and `type`=%d limit 1;", 
      $X, $Y, $Z, $LayerID);
    $Result = mysql_query($SQL);
    if(mysql_error()){
      dieMsg("Error in SQL");
    }
    if(mysql_num_rows($Result) == 0){
      return(Array("valid"=>0));
    }
    $Data = mysql_fetch_assoc($Result);
    $Data["valid"] = 1;
    return($Data);
  }

  function SearchBlankTiles($X,$Y,$Z,$LayerID){
    $SQL = sprintf("select * from tiles_blank where `x`=%d and `y`=%d and `z`=%d and `layer`=%d limit 1;", 
      $X, $Y, $Z, $LayerID);  
    $Result = mysql_query($SQL);
    if(mysql_error()){
      dieMsg("Error in SQL</p>");
    } 
    if(mysql_num_rows($Result) == 0){
	if($Z < 2)
	{
	  return;
	}
	else
	{
          SearchBlankTiles($X>>1,$Y>>1,$Z-1,$LayerID);
	  return;
	}
    }
  
    $Data = mysql_fetch_assoc($Result);

    switch($Data["type"]){
      case 1:
        $TypeName = "blank sea";
        break;
      case 2:
        $TypeName = "blank land";
        break;
      default:
        $TypeName = "blank unknown";
        break;
      }
    printf("| %s",$TypeName);
  }
  

  function dieMsg($Text){
    print("XX | $Text");
    exit;
  }
?>