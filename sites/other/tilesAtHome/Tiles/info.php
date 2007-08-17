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

  // look on new filesystem
  print "<h2>Tiles on the disk</h2>\n";
  SearchMetaDB($X,$Y,$Z,$LayerID,0);
  SearchFilesystem($X,$Y,$Z,$LayerID);
  print "<p class=\"notes\">Date and size should match if the image was uploaded through the proper interface.</p>\n";
  
  // Look for a complete tileset in new system
  if($Z >= 12){
    list($Valid,$X12,$Y12) = WhichTileset($X,$Y,$Z);
    if($Valid){
      print "<h2>Tileset-at-once uploads</h2>\n";
      if(SearchMetaDB($X12,$Y12,12,$LayerID,1)){
        SearchFilesystem($X12,$Y12,12,$LayerID);
      }
      print "<p class=\"notes\">If there is both a tile and a tileset entry, the one with the latest timestamp is the correct one</p>\n";
    }
  }
  
  // look for blank tiles
  print "<h2>Blank db</h2>\n";  
  SearchBlankTiles($X,$Y,$Z,$LayerID);
  print "<p class=\"notes\">Blank tiles are shown only if no images exist, and they're recursively-searched so that one tile can cover many zoom levels</p>\n";
  
  // Look for a file on the filesystem
  function SearchFilesystem($X,$Y,$Z,$LayerID){
    $LayerName = LayerDir($LayerID);
    $Filename = TileName($X,$Y,$Z,$LayerName);
    
    if(file_exists($Filename)){
      FormatFilenameInfo($Filename);
    }
  }
  
  function SearchMetaDB($X,$Y,$Z,$LayerID,$RequireTileset){
    $Data = MetaInfo($X,$Y,$Z,$LayerID);
    if(!$Data["valid"]){
      printf("<p>No entry in meta db</p>\n");
      return(0);
    }
    if($RequireTileset && $Data["tileset"] != 1){
      printf("<p>None found</p>\n");
      return(0);
    }
    printf("<p>Database says %s</p>\n", FormatMetaInfo($Data));
    return(1);
  }
      
  function FormatFilenameInfo($Filename){
      $ActualSize = filesize($Filename);
      $ActualDate = filemtime($Filename);
      
      printf("<p>Found image: %d bytes, modified %s</p>", 
        $ActualSize,
        date("Y-m-d H:i:s", $ActualDate));  
  }
  function FormatMetaInfo($Data){
    return(sprintf(
      "%d bytes, uploaded %s by user %d '<b>%s</b>' with version %d '<b>%s</b>'",
      $Data["size"],
      $Data["date"],
      $Data["user"],
      htmlentities(lookupUser($Data["user"])),
      $Data["version"],
      htmlentities(versionName($Data["version"]))));
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
