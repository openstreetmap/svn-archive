<html><head><title>Create user</title>
</head><body>

<table><form action="./" method="post">
<tr><td>User</td><td><input type="text" name="user"></td></tr>
<tr><td>Password</td><td><input type="text" name="pass"></td></tr>
<tr><td>&nbsp;</td><td><input type="submit" name="create" value="create"></td></tr>
</form></table>

<?php

if($_POST["create"] == "create"){
  CreateUser($_POST["user"], $_POST["pass"]);
}

function CreateUser($User, $Pass){
  include("../../connect/connect.php");
  $SQL = sprintf("insert into tiles_users (`name`,`password`) values ('%s', MD5('%s'));",
    mysql_escape_string($User),
    mysql_escape_string($Pass));
  

  printf("<p>Creating %s, %s</p>", htmlentities($User), htmlentities($Pass));
  printf("<p>%s</p>", htmlentities($SQL));
  
  mysql_query($SQL);  
  printf("<p>%s</p>", mysql_error());
}

?>
<p>Then <a href="../exportpasswords.php">export the passwords list</a> to filesystem</p>
</body></html>