<html>
<head>
<title>tiles@home - lookup tile meta-information</title>
<link rel="stylesheet" href="../styles.css">
<style>
.chat{color:#AAA;font-size:small;}
.notes{color:#4F4; font-size:small}
</style>
<meta name="robots" content="nofollow,noindex">
</head>
<body>
<div class="all">
<h1 class="title"><a href="../"><img src="../Gfx/tah.png" alt="tiles@home" width="600" height="109"></a></h1>
<p class="title">Lookup tile meta-information</p>
<hr>
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

  print "<a href='http://tah.openstreetmap.org/Browse/?x=$X&y=$Y&z=$Z&layer=$Layer'><img src='http://tah.openstreetmap.org/Tiles/$Layer/$Z/$X/$Y.png' align='right'></a>";

  print "<p><form action=\"./info.php\" action=\"get\">\n";
  printf("x <input type=\"text\" name=\"x\" size=5 value=\"%d\">\n", $X);
  printf("y <input type=\"text\" name=\"y\" size=5 value=\"%d\">\n", $Y);
  printf("z <input type=\"text\" name=\"z\" size=3 value=\"%d\">\n", $Z);
  printf("layer <input type=\"text\" name=\"layer\" size=7 value=\"%s\">\n", htmlentities($Layer));
  print "<input type=\"submit\" value=\"Lookup\">";
  print "</form></p>";

  // Check x,y,z is valid
  if(!TileValid($X,$Y,$Z)){
    dieMsg("Invalid tile coordinates");
    }
  
  // Lookup layer, and check is valid
  $LayerID = checkLayer($Layer);
  if($LayerID < 0){
    dieMsg("Invalid layer");
    }

  // Open database connection
  include("../connect/connect.php");

  print "<h2>Tile on disk</h2>\n";
  // look up filesystem
  if ($FileExists = SearchFilesystem($X,$Y,$Z,$LayerID)) {
    $Data =  SearchMetaDB($X,$Y,$Z,$LayerID,0);
  
    // Look for a complete tileset information and use newer entry
    if($Z >= 12) {
      list($Valid,$X12,$Y12) = WhichTileset($X,$Y,$Z);
      if($Valid)
        if($TilesetData = SearchMetaDB($X12,$Y12,12,$LayerID,1))
          if ($TilesetData['date'] > $Data['date'])
            $Data = $TilesetData;
    }
    if ($Data) PrintMetaInfo($Data);
     else echo "<p>No meta data in data base.</p>";
  } else {
    echo "<p>No file found.</p>";
  }

 
  // look for blank tiles
  print "<h2>Blank db</h2>\n";  
  SearchBlankTiles($X,$Y,$Z,$LayerID);
  print "<p class=\"notes\">Blank tiles are shown only if no images exist, and they're recursively-searched so that one tile can cover many zoom levels</p>\n";
  
  // Look for a file on the filesystem
  function SearchFilesystem($X,$Y,$Z,$LayerID){
    $LayerName = LayerDir($LayerID);
    $Filename = TileName($X,$Y,$Z,$LayerName);
    
    if($fileexists = file_exists($Filename)){
      FormatFilenameInfo($Filename);
    }
    return $fileexists;
  }
  
  function SearchMetaDB($X,$Y,$Z,$LayerID,$RequireTileset){
    $Data = MetaInfo($X,$Y,$Z,$LayerID);
    if(!$Data["valid"]){
      return(NULL);
    }
    if($RequireTileset && $Data["tileset"] != 1){
      return(NULL);
    }
    return($Data);
  }

  //------------------------------------------------------------------------
  function PrintMetaInfo($Data) {
    printf(
      "<p>%s upload by user %d (%s) with client %d (%s) recorded on %s</p>",
      ($Data['tileset']?'Full tileset':'Single tile'),
      $Data["user"],
      htmlentities(lookupUser($Data["user"])),
      $Data["version"],
      htmlentities(versionName($Data["version"])),
      $Data['date']
    );
  }
  //------------------------------------------------------------------------

  function FormatFilenameInfo($Filename){
      $ActualSize = filesize($Filename);
      $ActualDate = filemtime($Filename);
      
      printf("<p>Image file: %d bytes, modified %s</p>", 
        $ActualSize,
        date("Y-m-d H:i:s", $ActualDate));  
  }
  
  function MetaInfo($X,$Y,$Z,$LayerID){
    $SQL = sprintf("select * from tiles_meta where `x`=%d and `y`=%d and `z`=%d and `type`=%d limit 1;", 
      $X, $Y, $Z, $LayerID);
    $Result = mysql_query($SQL);
    if(mysql_error()){
      dieMsg("<p>Error in SQL</p>");
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
      dieMsg("<p>Error in SQL</p>");
    }
    
    if(mysql_num_rows($Result) == 0){
	if($Z < 2)
	{
	  print "<p>Nothing found in blank tile database</p>";
	  return;
	}
	else
	{
	  if ($Z == $_GET["z"])
              print "<p class=\"chat\">Nothing found at z-$Z, searching upwards...</p>\n";
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
        $TypeName = "an unknown type";
        break;
      }
    printf("<p>$X,$Y at z-$Z is %s, uploaded by user %d '<b>%s</b>' on %s", 
      $TypeName,
      $Data["user"],
      htmlentities(lookupUser($Data["user"])), 
      $Data["date"]);
  }
  

  function dieMsg($Text){
    print("<p>$Text</p>");
    exit;
  }
?>
</div>
</body>
</html>
