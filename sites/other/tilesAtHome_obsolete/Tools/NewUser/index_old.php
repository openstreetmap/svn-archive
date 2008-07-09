<html><head><title>Create user</title>
</head><body>
<?php
$Alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345678901234567890";
$RandomPassword = "";
for($i = 0; $i < 9; $i++){
    $RandomPassword .= substr($Alphabet, rand(0, strlen($Alphabet)-1), 1);
}
?>
	
<table><form action="./" method="post">
<tr><td>User</td><td><input type="text" name="user"></td></tr>
<tr><td>Password</td><td><input type="text" name="pass" value="<?= $RandomPassword ?>"></td></tr>
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
  //printf("<p>%s</p>", htmlentities($SQL));
  
  mysql_query($SQL);  
  if(mysql_error()){
    printf("<p>Error: %s</p>", mysql_error());
    return;
    }
  
  $ID = mysql_insert_id();
  
  printf("<pre>Welcome to tiles@home. Your upload username is: &quot;%s&quot;, with password &quot;%s&quot;.\n\nUploads can be checked at\n* %s\n* %s\n\nManual upload is at %s\n</pre>",
    $User, 
    $Pass,
    "http://dev.openstreetmap.org/~ojw/Credits/",
    "http://dev.openstreetmap.org/~ojw/Credits/ByUser/?id=$ID",
    "http://dev.openstreetmap.org/~ojw/Upload/");
}

?>
<p>Then <a href="../exportpasswords.php">export the passwords list</a> to filesystem</p>
</body></html>