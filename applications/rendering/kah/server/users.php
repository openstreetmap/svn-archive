<?php

if($_GET["test"])
  print testUser($_GET["user"],$_GET["pass"]) ? "OK":"FAIL";

function testUser($User,$Pass)
{
  // If we have validated this info before, we mark it as such
  // by creating a file with the hashed info.
  // File exists = user/pass was previously checked.
  // Every 10 uploads (on average) we double-check that the
  // OSM ID is *still* valid
  $IDENT = md5(sprintf("%s:%s:%s", $User,$Pass,"llzial,2190i9"));
  $IdentFile = "users/$IDENT.exists";
  if(file_exists($IdentFile) && (rand(0,10) != 5))
    return(1);

  // Ask OSM if this user exists, by using their credentials to
  // download their preferences file.  If it works, the user/pass
  // were a valid OSM user/pass
  $URL = sprintf(
    "http://%s:%s@api.openstreetmap.org/api/0.5/user/preferences",
    urlencode($User),
    urlencode($Pass));
  $Document = @file_get_contents($URL);

  if(strpos($Document, "<preferences") !== false)
  {
    file_put_contents($IdentFile, "");
    return(1);
  }

  // Incorrect credentials.
  // If these *used to be* correct, then delete the file which would
  // otherwise validate these credentials.
  if(file_exists($IdentFile))
    unlink($IdentFile);
  return(0);
}
?>