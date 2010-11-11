<?php

require_once('/home/www-data/private/defines.php');
require_once('freemap_functions.php');
require_once('../lib/functionsnew.php');

session_start();
$conn=pg_connect("dbname=freemap");
if(!isset($_SESSION['gatekeeper']))
{
	?>
	<html><body>
	<h1>Admin Login</h1>
	<form method='post' action='login.php?redirect=admin.php'>
	<label for='username'>Username</label><br/>
	<input name='username' id='username' /><br/>
	<label for='password'>Password</label><br/>
	<input name='password' id='password' type='password' /><br/>
	<input type='submit' value='Go!'/>
	</form>
	</body></html>
	<?php
}
else if (get_user_level($_SESSION['gatekeeper'],'freemap_users','isadmin',
				'username','pgsql') != 1)
{
	echo "You're not admin. Go away!";
}
else 
{
	?>
	<html>
	<head>
	<style type='text/css'>
	table,td{border-style:solid;border-width:2px; }
	</style>
	</head>
	<body>
	<?php
	echo "<h2>Pending Images</h2>\n";
	$result=pg_query("SELECT * FROM freemap_markers WHERE photoauth=0");
	echo "<table>\n";
	while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
	{
		if(file_exists("/home/www-data/uploads/photos/${row[id]}.jpg"))
		{
			echo "<tr><td>ID $row[id]</td>";
			echo "<td><a href='api/markers.php?id=$row[id]&".
				"action=getPhoto'>View</a></td>\n";
			echo "<td><a href='api/markers.php?id=$row[id]&".
				"action=authorisePhoto'>Authorise</a></td>\n";
			echo "<td><a href='api/markers.php?id=$row[id]&".
				"action=delete'>Delete</a></td>\n";
			echo "</tr>\n";
		}
	}
	echo "</table>\n";

	echo "<h2>Users</h2>\n";
	$result=pg_query("SELECT * FROM freemap_users ORDER BY id");
	echo "<table>\n";
	while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
	{
		echo "<tr><td>$row[username]</td><td>$row[email]</td>";
		echo "<td><a href='deleteaccount.php?id=$row[id]'>Delete</td></tr>\n";
	}
	echo "</table>\n";
	echo "<a href='logout.php?referrer=admin.php'>Log out</a>\n";
	echo "</body></html>\n";
}
pg_close($conn);
?>
