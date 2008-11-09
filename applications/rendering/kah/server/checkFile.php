<?php

define("TILES_PER_FILE", 1365);
define("INDEXES_PER_FILE", TILES_PER_FILE+1);
define("BYTES_PER_OFFSET", 4);
define("FILE_HEADER_SIZE", INDEXES_PER_FILE * BYTES_PER_OFFSET);
define("PNG_HEADER_SIZE", 8);
define("PNG_HEADER", "\x89\x50\x4E\x47\x0D\x0A\x1A\x0A");

if($_GET["test_filecheck"])
{
header("Content-type:text/plain");
print "Result: " . checkFile("files/layer1/12/1280/1000.dat");
}

#--------------------------------------------------------------------------
# Check that a file is a valid tileset-per-file
#--------------------------------------------------------------------------
function checkFile($Filename)
{
  if(!file_exists($Filename))
    return("File doesn't exist");
    
  $Filesize = filesize($Filename);
  if($Filesize < FILE_HEADER_SIZE)
    return("File too small");

  # Open file
  $fp = fopen($Filename, "rb");
  if(!$fp)
    return("Can't open file");

  # Check within file
  $Err = checkFilePointer($fp, $Filesize);

  fclose($fp);
  return($Err);
}
  
function checkFilePointer($fp, $Filesize)
{
  # Read-in all offsets
  $Offsets = Array();
  for($i = 0; $i < INDEXES_PER_FILE; $i++)
    {
    $Num = unpack("L", fread($fp, BYTES_PER_OFFSET));
    $Offsets[$i] = $Num[1];
    }

  # First check - offsets look reasonable.
  # Store list of non-blank tiles to check later
  $NonBlankTiles = Array();
  for($i = 0; $i < TILES_PER_FILE; $i++)
    {
    $Offset = $Offsets[$i];
    $Size = $Offsets[$i + 1] - $Offset;

    if($Offset < FILE_HEADER_SIZE)
      {
      if($Offset > 3)
        return("Unknown blank-tile or offset too small");
      }
    else
      {
      if($Size < 0)
        return("Negative size at tile $i");
      if($Size < 8)
        return("Size too small for PNG header at tile $i");
      if($Offset+$Size > $Filesize)
        return("Size too large at tile $i");
      array_push($NonBlankTiles, array($Offset, $Size, $i));
      }
    }

  # Second check: Look for PNG headers where each image should start
  foreach($NonBlankTiles as $Tile)
    {
    list($Offset, $Size, $i) = $Tile;
    
    fseek($fp, $Offset, SEEK_SET);
    if(fread($fp, PNG_HEADER_SIZE) != PNG_HEADER)
      return("No PNG header in tile $i");
    }

  # Third check: file isn't too large
  $ExtraTextAtEnd = $Filesize - $Offsets[INDEXES_PER_FILE-1];
  if($ExtraTextAtEnd > 100)
    return("Too much unused space at end ($ExtraTextAtEnd bytes)");

  # Everything OK
  return("OK"); 
}


?>
