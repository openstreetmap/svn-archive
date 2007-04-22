<?php
session_start();
?>
	<html>

	<head>
	<link rel='stylesheet' type='text/css' href='/css/freemap2.css' />
	</head>
	<body> 
<?php
require_once('defines.php');


if(isset($_POST["username"]) && isset($_POST["password"]))
{
	$conn = mysql_connect("localhost",DB_USERNAME,DB_PASSWORD);
	mysql_select_db(DB_DBASE);

	$q = ("insert into freemap_users (username,password,active) values ".
					"('$_POST[username]',MD5('$_POST[password]'),1)");

	mysql_query($q) or die(mysql_error());
	mysql_close($conn);
	mail('nick@hogweed.org', 'New Freemap account created', 
		"New Freemap account created for $_POST[username]");
	header("Location: /index.php");
	echo "<h1>Signed up!</h1>";
	echo "<p>You've successfully signed up.";
	echo "<a href='/index.php'>Back to main page</a></p>";
}
else
{
	?>
	<h2>Sign up</h2>
	<p>Signing up and creating an account will in the future (not currently
	implemented) allow you to add private annotations
	to the map. Private annotations are only available to people who know your
	login details. This is useful, for instance, to annotate the map with the
	route of a walking club walk without cluttering up the public map.</p>
	<div>
	<form method="post" action="">
	<label for="username">Enter your email address</label><br/>
	<input name="email" id="email" /> <br/>
	<label for="username">Enter a username</label><br/>
	<input name="username" id="username" /> <br/>
	<label for="password">Enter a password</label> <br/>
	<input name="password" id="password" type="password" /> <br/>
	<input type='submit' value='go'/>
	</form>
	</div>
	<p><a href='/index.php'>Back to map</a></p>
	<?php
}
?>
	</body>
	</html>
