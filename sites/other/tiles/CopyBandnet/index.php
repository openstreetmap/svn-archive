<pre><?php
//print "disabled\n"; exit;
include("../connect/connect.php");

$Result = mysql_query("select `x`,`y`,`z` from `tiles` where `to_import`=1 and `z`=12 limit 1;");
if(mysql_errno()){
  print "Error"; exit;
}
if(mysql_num_rows($Result) < 1){
  print "Nothing to do"; exit;
}
$Data = mysql_fetch_assoc($Result);

CopyBandnetTileSet($Data["x"], $Data["y"], $Data["z"]);

$SQL = sprintf("update `tiles` set `to_import`=0 where `x`=%d and `y`=%d and `z`=%d;",
  $Data["x"], $Data["y"], $Data["z"]);
mysql_query($SQL);

function CopyBandnetTileSet($X,$Y,$Z){
  if($Z != 12)
    return;
    
  $ViewURL = sprintf("../Browse/?x=%d&amp;y=%d&amp;z=%d", $X,$Y,$Z);
  
  $FromURL = sprintf("http://osmathome.bandnet.org/7877.php?x=%d&y=%d", $X,$Y);

  $Status = "";
  $fp = fopen($FromURL, "rb");
  if(!$fp){
    $Status="Can't open";
  }
  else
  {
    $Status = "OK";
    while(($line = fgets($fp, 500)) !== false){
      if(preg_match("/^Tiles\/(\d+)\/(\d+)\/(\d+)\.png\s*$/sm", rtrim($line), $Matches)){
        $X = $Matches[2];
        $Y = $Matches[3];
        $Z = $Matches[1];
        $URL = "http://osmathome.bandnet.org/".$Matches[0];


        printf("Downloading %d,%d,%d from %s - %s\n", 
          $X,$Y,$Z,$URL,
          downloadStore($X,$Y,$Z,$URL));
      }
    else
      {
      #printf("Not matched %s\n", htmlentities($line));
      }
    }
    fclose($fp);
  }
  
  printf("<a href=\"%s\">%d,%d,%d</a> from <a href=\"%s\">here</a>. %s\n", $ViewURL,$X,$Y,$Z, $FromURL, $Status);
    
}

function downloadStore($X,$Y,$Z,$URL){
  $ImageData = file_get_contents($URL);
  if(!$ImageData)
    return("Nothing got");

  $SQL = sprintf("insert into tiles (`x`,`y`,`z`,`tile`,`user`,`exists`,`date`,`size`) values('%d','%d','%d','%s','%s','1','%d','%d');",
    $X,
    $Y,
    $Z,
    mysql_escape_string($ImageData),
    "from_bandnet",
    time(),
    strlen($ImageData));

  mysql_query($SQL);
  if(mysql_errno())
    return(mysql_error());
  
  return("OK");
}




?></pre>