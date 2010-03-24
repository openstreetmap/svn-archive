<html>
<body>
<?php
require_once('/home/www-data/private/defines.php');
require_once('../lib/functionsnew.php');


$inp=clean_input($_GET);
$conn = pg_connect("dbname=freemap");

if(isset($_GET['userid']) && isset($_GET['key']))
{
	$id=$inp['userid'];
	$key=$inp['key'];
	$result=pg_query
		("SELECT * FROM freemap_users WHERE id=$id AND active=0");
	if($result)
	{
		$row=pg_fetch_array($result,null,PGSQL_ASSOC);
		if($row['k']==$key)
		{
			pg_query("UPDATE freemap_users SET active=1,k=0 WHERE id=$id");
			echo "You have now activated your account and may login.";
		}
		else
		{
			pg_query("DELETE FROM freemap_users WHERE id=$id");
			echo "Invalid key. This account has now been deleted, you'll need ".
				 "to sign up again.";
		}
	}
	else
	{
		echo "Invalid id/key.";
	}
}
else
{
	echo "id/key not supplied.";
}

pg_close($conn);
?>
</body>
</html>
