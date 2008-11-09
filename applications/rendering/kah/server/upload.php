<?php
header("Content-type: text/plain");
include("checkFile.php");
include("config.php");

if($_POST["password"] != "some_password")
{
  showError("Password required");
}
else
{
  $z = $_POST["z"]+0;
  if($z != 6 && $z != 12)
  {
    showError("Z6/12 only");
  }
  else
  {
    $x = floor($_POST["x"]+0);
    $y = floor($_POST["y"]+0);
    $size = pow(2, $z);
    if($x < 0 || $y < 0 || $x >= $size || $y >= $size)
    {
      showError("Invalid tile number");
    }
    else
    {
      $Layer = $_POST["layer"];
      if(preg_match("{\W}", $Layer))
      {
        showError("Invalid layer");
      }
      elseif(!isValidLayer($Layer))
      {
        showError("Unsupported layer");
      }
      else
      {
        ignore_user_abort(TRUE);
        
        $BaseDir = getBaseDir();
        md("$BaseDir/$Layer/$z");
        md("$BaseDir/$Layer/$z/$x");
        $Filename = "$BaseDir/$Layer/$z/$x/$y.dat";
        $FilenameTemp = "$Filename.part";
        $fp = fopen($FilenameTemp, "wb");
        if(!$fp)
        {
          showError("Cant write to temp file");
        }
        else
        {
          fputs($fp, stripslashes($_POST["data"]));
          fprintf($fp, "\n\n*** From %s", $_SERVER["REMOTE_ADDR"]);
          fclose($fp);

          $FileCheck = checkFile($FilenameTemp);
          if($FileCheck == "OK")
          {
            if(rename($FilenameTemp, $Filename))
              {
              printf("OK");
              }
            else
              {
              showError("Couldn't overwrite file");
              }
          }
          else
          {
            showError($FileCheck);
          }
        }
      }
    }
  }
}

function md($Dir)
{
  if(!file_exists($Dir))
    mkdir($Dir);
}
function showError($Text)
{
  printf("Error: %s\n", $Text);
  # TODO: log errors
  exit;
}
?>