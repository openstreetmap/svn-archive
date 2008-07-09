<html><head><title>Create user</title>
</head><body>

<table><form action="./" method="post">
<tr><td>User</td><td><input type="text" name="user"></td></tr>
<tr><td>Password</td><td><input type="text" name="pass" value="<?=make_password(10)?>" /></td></tr>
<tr><td>&nbsp;</td><td><input type="submit" name="create" value="create"></td></tr>
</form></table>

<?php

if($_POST["create"] == "create"){
  CreateUser($_POST["user"], $_POST["pass"]);
}
function make_password($length,$strength=0) {
  $vowels = 'aeiouy';
  $consonants = 'bdghjlmnpqrstvwxz';
  if ($strength & 1) {
    $consonants .= 'BDGHJLMNPQRSTVWXZ';
  }
  if ($strength & 2) {
    $vowels .= "AEIOUY";
  }
  if ($strength & 4) {
    $consonants .= '0123456789';
  }
  if ($strength & 8) {
    $consonants .= '@#$%^';
  }
  $password = '';
  $alt = time() % 2;
  srand(time());
  for ($i = 0; $i < $length; $i++) {
    if ($alt == 1) {
      $password .= $consonants[(rand() % strlen($consonants))];
      $alt = 0;
    } else {
        $password .= $vowels[(rand() % strlen($vowels))];
      $alt = 1;
    }
  }
  return $password;
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
    "http://tah.openstreetmap.org/Credits/",
    "http://tah.openstreetmap.org/Credits/ByUser/?id=$ID",
    "http://tah.openstreetmap.org/Upload/");
}

?>
</body></html>
