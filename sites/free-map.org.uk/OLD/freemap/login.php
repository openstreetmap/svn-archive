<?php
require_once('/home/www-data/private/defines.php');
require_once('../lib/functionsnew.php');

session_start();
if (isset($_REQUEST['username']) && isset($_REQUEST['password']))
{
	$conn=pg_connect("dbname=freemap");

	$q=	
        ("select * from freemap_users where username='$_REQUEST[username]' ".
		  "and password='".md5($_REQUEST['password'])."' and active=1");
	$result=pg_query($q);
	$row=pg_fetch_array($result,null,PGSQL_ASSOC);
	if($row)
	{
		$_SESSION["gatekeeper"] = $_REQUEST["username"];
		pg_close($conn);
		$qs="";
		foreach ($_POST as $k=>$v)
		{
			if($k!="username" && $k!="password" && $k!="redirect")
			{
				$qs .= "$k=$v&";
			}
		}
		header("Location: $_REQUEST[redirect]?$qs");
	}
	else
	{
		pg_close($conn);
		js_error('Invalid login',$_REQUEST['redirect']);
	}
}
else
{
	echo "Why are you trying to access this script without providing ".
		 "a login!!!???";
}
?>
