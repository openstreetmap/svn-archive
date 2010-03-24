<?php

require_once('/home/www-data/private/defines.php');
require_once('../lib/freemap_functions.php');
require_once('functionsnew.php');

session_start();
$conn=pg_connect("dbname=freemap");
if(!isset($_REQUEST['id']))
{
	echo "How do you expect this to work if you don't provide a user ID???";
}
elseif(!isset($_SESSION['gatekeeper']))
{
	?>
	<html><body>
	<h1>Login</h1>
	<form method='post' action='login.php?redirect=deleteaccount.php'>
	<label for='username'>Username</label><br/>
	<input name='username' id='username' /><br/>
	<label for='password'>Password</label><br/>
	<input name='password' id='password' type='password'/><br/>
	<input type='hidden' name='id' value='<?php echo $_REQUEST['id']?>'>
	<input type='submit' value='Go!'/>
	</form>
	</body></html>
	<?php
}
else if (get_user_level($_SESSION['gatekeeper'],'freemap_users','isadmin',
				'username','pgsql') != 1)
{
	echo "Stop trying to delete other people's accounts!!!!!";
}
else 
{
	$result=pg_query("SELECT * FROM freemap_users WHERE id=$_REQUEST[id]");
	if($row=pg_fetch_array($result,null,PGSQL_ASSOC))
	{
		pg_query("DELETE FROM freemap_users WHERE id=$_REQUEST[id]");
		mail($row['email'],"Freemap account deleted",
			"Due to a suspicious looking email address and/or ".
			"attempted spamming, your account has been deleted - please ".
			"email me on nick_whitelegg@yahoo.co.uk if you think this ".
			"is an error.");
	}
	else
	{
		echo "Invalid user ID";
	}
}
pg_close($conn);
?>
