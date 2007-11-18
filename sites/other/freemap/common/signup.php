<?php
session_start();
require_once('defines.php');


if(isset($_POST["username"]) && isset($_POST["password"]))
{
	$conn = mysql_connect("localhost",DB_USERNAME,DB_PASSWORD);
	mysql_select_db(DB_DBASE);

	$result=mysql_query("SELECT * FROM freemap_users WHERE ".
						"username='$_POST[username]'");
	if(mysql_num_rows($result)==1)
	{
		header('Location: /freemap/common/signup.php?error=1');
	}
	else
	{
		$q = ("insert into freemap_users (username,password,active) values ".
					"('$_POST[username]',MD5('$_POST[password]'),1)");

		mysql_query($q) or die(mysql_error());
		mysql_close($conn);
		mail('nick@hogweed.org', 'New Freemap account created', 
		"New Freemap account created for $_POST[username]");
		?>
		<html>
		<head>
		<link rel='stylesheet' type='text/css' href='/css/freemap2.css' />
		</head>
		<body> 
		<h1>Signed up!</h1>
		<p>You've successfully signed up.
		<a href='/index.php'>Back to main page</a></p>
		</body>
		</html>
		<?php
	}
}
else
{
	?>
	<html>
	<head>
	<link rel='stylesheet' type='text/css' href='/css/freemap2.css' />
	</head>
	<body> 
	<?php
	if($_GET['error']==1)
	{
		echo "<p class='error'>Error: Username already taken. ";
		echo "Please choose another one.</p>";
	}
	else
	{
		?>
	<h2>Sign up</h2>
	<p>Signing up and creating an account allows you to keep a record of your
	own personal walking routes on Freemap, each of which you can either 
	share with others or keep private. When logged in, you can view your
	own routes with the &quot;My routes&quot; option.
	This is also useful if you run a walking
	club and want to keep a record of the club's past walks, or add future
	routes, without cluttering up the public map.</p>
		<?php
	}
	?>
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
	</body>
	</html>
	<?php
}
?>
