<html>
<body>
<?php
require_once('/home/www-data/private/defines.php');
require_once('../lib/functionsnew.php');
include('connect.php');


$inp=clean_input($_GET);

if(isset($_GET['userid']) && isset($_GET['key']))
{
	$id=$inp['userid'];
	$key=$inp['key'];
	$result=mysql_query
		("SELECT * FROM users WHERE id=$id AND active=0");
	if(mysql_num_rows($result)==1)
	{
		$row=mysql_fetch_array($result);
		if($row['k']==$key)
		{
			mysql_query("UPDATE users SET active=1,k=0 WHERE id=$id");
			echo "You have now activated your account and may login.";
		}
		else
		{
			mysql_query("DELETE FROM users WHERE id=$id");
			echo "Invalid key. This account has now been deleted, you'll need ".
				 "to sign up again.";
		}
	}
	else
	{
		echo "No unactivated account with that ID.";
	}
}
else
{
	echo "id/key not supplied.";
}

mysql_close($conn);
?>
</body>
</html>
