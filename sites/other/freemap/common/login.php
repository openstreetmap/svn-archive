<?php
require_once('defines.php');
require_once('functionsnew.php');

session_start();

$conn=mysql_connect(DB_HOST,DB_USERNAME,DB_PASSWORD);
mysql_select_db(DB_DBASE);

$redirect = (isset($_REQUEST['redirect'])) ? $_REQUEST['redirect']:'/index.php';

$result = mysql_query
        ("select * from freemap_users where username='$_POST[username]' and ".
            "password=MD5('$_POST[password]') and active=1");
if(mysql_num_rows($result))
{
	$_SESSION["gatekeeper"] = $_POST["username"];
	mysql_close($conn);
	header("Location: $redirect");
}
else
{
	mysql_close($conn);
	js_error('Invalid login!', $redirect);
}
?>
