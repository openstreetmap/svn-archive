<?php
include("blanktiles.php");
include("config.php");

#showFile("layer1",1280,1000,12);
#showFile("layer1",1280*8+4,1000*8+4,15);

$Layer = $_GET["layer"];
if(preg_match("{^(\d+)/(\d+)/(\d+)\.png$}", $_GET["loc"], $Matches))
{
  showFile($Layer, $Matches[2], $Matches[3], $Matches[1]);
}
else
  print "Don't understand URL";


function showFile($Layer,$x,$y,$z)
{
  if(!isValidLayer($Layer))
    {
    imageHeader();
    print_blank_tile();
    return;
    }

  $TX = $x;
  $TY = $y;
  $TZ = $z;
  $RecordSize = 1;
  $OffsetSize = 1;
  $Record = 0;
  
  while($TZ > 12)
  {
    $TX *= 0.5;
    $TY *= 0.5;
    $TZ--;
    $Record += $RecordSize;
    $RecordSize *= 4;
    $OffsetSize *= 2;
  }
  $TX = floor($TX);
  $TY = floor($TY);

  // TODO: could have used the float part of $TX multiplied by $OffsetSize
  $OX = $x - $TX * $OffsetSize;
  $OY = $y - $TY * $OffsetSize;
  $Record += $OY * $OffsetSize + $OX;

  $Filename = sprintf("files/%s/%d/%d/%d.dat", $Layer, $TZ,$TX,$TY);
  $IndexSize = 1366*4;

  # Check filesize
  if(!file_exists($Filename))
    {
    ShowError("No such file");
    return;
    }
    
  $Filesize = filesize($Filename);
  if($Filesize < $IndexSize)
    {
    ShowError("File too small");
    }
  else
    {
    # Open file
    $fp = fopen($Filename, "rb");
    if($fp)
    {
      # Read the 2 records that give offset of this tile and next tile
      fseek($fp, $Record * 4, SEEK_SET);
      $Bin = fread($fp,8);
      $Num = unpack("L2", $Bin);

      $Offset = $Num[1];
      $Size = $Num[2] - $Num[1];

      # TODO: small offsets mean blank tiles
      if($Offset < $IndexSize)
        {
        switch($Offset)
          {
          case 1: # sea
            imageHeader();
            print_sea_tile();
            break;
            
          case 2: # land
            imageHeader();
            print_blank_tile();
            break;
          
          case 0: # unknown
          case 3: # transparent
          default:
            imageHeader();
            print_transparent_tile();
            break;
          }
        }
      elseif($Size > 8 and $Offset+$Size <= $Filesize) # Sanity-check
        {
        # Go back to start of image, and read the data
        fseek($fp, $Offset, SEEK_SET);
        imageHeader();
        print fread($fp, $Size);
        }
      else
        {
        ShowError("Invalid offset/size");
        }
        
      fclose($fp);
      }  
    else
      {
      ShowError("Cant open file");
      }
    }
}

function imageHeader()
{
  // TODO: cache-control headers
  header("Content-type:image/PNG");
}

function ShowError($Text)
{
  imageHeader();
  print_blank_tile();
  return;

  header("Content-type:text/plain");
  print "Error: $Text\n";
  exit;
}
?>
