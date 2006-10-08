<?php
include("../Connect/connect.inc");
$Password = $_POST["mp"];
switch($Password)
{
  // Passwords are hardcoded here: edit this file to change permissions
  case "user1|password1":
  case "user2|password2":

  list($User,$Pass) = explode("|", $Password);

  // Size limit for uploaded files
  if($_FILES['file']['size'] > 800000)
    exit;
  $Filename = sprintf("Maps/%d.png", $_POST["id"]);
  move_uploaded_file($_FILES['file']['tmp_name'], $Filename);

  $SQL = sprintf("update `places2` set `renderer`='%s' where id=%d;",
    mysql_escape_string($User),
    $_POST["id"]);
  mysql_query($SQL);  
  
  break;
default:
}
?>