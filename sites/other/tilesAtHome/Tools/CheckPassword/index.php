<h1>Test a password</h1>

<form action="./" method="post">
<input name="user" type="text">
<input name="pass" type="password">
<input type="submit" value="Check">
</form>

<?php
include("../../lib/users.inc");

  $SuppliedUsername = $_POST["user"];
  $SuppliedPassword = $_POST["pass"];
  
  if($SuppliedUsername){
  
    $ID = checkUser($SuppliedUsername, $SuppliedPassword);
    if($ID < 0){
      printf("<p>Unrecognised username or password</p>\n"); 
    }
    else
    {
      printf("<p>User ID <b>%d</b></p>\n", $ID); 
    }
  }


?>
