<style>.chat{color:#AAA;font-size:small;}
	</style>
<?php
  
  include("../lib/tilenames.inc");
  include("../lib/layers.inc");
  
  
  $Layer = $_GET["layer"];
  $Z = $_GET["z"];
  $X = $_GET["x"];
  $Y = $_GET["y"];
  // with default layer
  if($Layer=="")$Layer="tile";

  print "<p><form action=\"./info.php\" action=\"get\">\n";
  printf("<input type=\"text\" name=\"x\" size=5 value=\"%d\">\n", $X);
  printf("<input type=\"text\" name=\"y\" size=5 value=\"%d\">\n", $Y);
  printf("<input type=\"text\" name=\"z\" size=3 value=\"%d\">\n", $Z);
  printf("<input type=\"text\" name=\"layer\" size=7 value=\"%s\">\n", htmlentities($Layer));
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
  $NoExitOnDbFail = 1;
  include("../connect/connect.php");
  if(!$DbSuccess){
    dieMessage("Can't open database to check anything else");
  }

  // look on new filesystem
  print "<h2>New disk</h2>\n";
  SearchFilesystem($X,$Y,$Z,$LayerID,0);

  // look on old filesystem
  print "<h2>Old disk</h2>\n";  
  SearchFilesystem($X,$Y,$Z,$LayerID,1);

  // look for blank tiles
  print "<h2>Blank db</h2>\n";  
  SearchDatabase($X,$Y,$Z,$LayerID);


  // Look for a file on the filesystem
  function SearchFilesystem($X,$Y,$Z,$LayerID,$Old){
    $LayerName = LayerDir($LayerID);
    $Filename = TileName($X,$Y,$Z,$LayerName,$Old);
    if(file_exists($Filename)){
      $ActualSize = filesize($Filename);
      $ActualDate = filemtime($Filename);
      printf("<p>Found %s (%d bytes, modified %s)</p>", 
	     htmlentities($Filename), 
	     $ActualSize,
	     date("r", $ActualDate));
    }
  }
  
  function SearchDatabase($X,$Y,$Z,$LayerID){

    $SQL = sprintf("select * from tiles_blank where `x`=%d and `y`=%d and `z`=%d and `layer`=%d limit 1;", 
      $X, $Y, $Z, $LayerID);
    
    $Result = mysql_query($SQL);
    if(mysql_error()){
      BlankTile("error");
    }
    
    if(mysql_num_rows($Result) == 0){
	if($Z < 2)
	{
	  print "<p>Nothing found in blank tile database</p>";
	  return;
	}
	else
	{
	  print "<p class=\"chat\">Nothing found at z-$Z, searching upwards...</p>\n";
          SearchDatabase($X>>1,$Y>>1,$Z-1,$LayerID);
	  return;
	}
    }
  
    $Data = mysql_fetch_assoc($Result);

    switch($Data["type"]){
      case 1:
        $TypeName = "sea";
        break;
      case 2:
        $TypeName = "land";
        break;
      default:
        $TypeName = "unknown type";
        break;
      }
    printf("<p>$X,$Y at z-$Z is a blank tile of type %d = %s", $Data["type"], $TypeName);
  }
  

  function dieMsg($Text){
    print("<p>$Text</p>");
    exit;
  }
?>
