<?php
session_start();
require_once('/home/www-data/private/defines.php');
require_once('../lib/functionsnew.php');

if(true)
{
	header("Location: /freemap/index.php");
}
elseif(isset($_POST["username"]) && isset($_POST["password"]))
{
	$inp=clean_input($_POST);
	$conn = pg_connect("dbname=freemap");

	$result=pg_query("SELECT * FROM freemap_users WHERE ".
						"username='$_POST[username]'");
	$row=pg_fetch_array($result,null,PGSQL_ASSOC);
	if($row)
	{
		header('Location: /freemap/signup.php?error=1');
	}
	elseif(strstr($inp["username"]," "))
	{
		header('Location: /freemap/signup.php?error=2');
	}
	elseif($inp['username']=="" || $inp['password']=="")
	{
		header('Location: /freemap/signup.php?error=3');
	}
	else
	{
		$random = rand (1000000,9999999);
		$q = ("insert into freemap_users (email,".
			"username,password,active,k) ".
				"values ('$inp[email]',".
				"'$inp[username]','".
				md5($inp['password']).
				"',0,$random)");
			
		pg_query($q); 
		$result=pg_query("select currval('freemap_users_id_seq') as lastid");
		$row=pg_fetch_array($result,null,PGSQL_ASSOC);
		pg_close($conn);
		mail('nick_whitelegg@yahoo.co.uk','New Freemap account created', 
		"New Freemap account created for $_POST[username] ".
		"(email $inp[email]). ".
		"<a href='http://www.free-map.org.uk/freemap/deleteaccount.php?".
		"id=$row[lastid]'>Delete</a>");
		mail($inp['email'], 'New Freemap account created', 
		"New Freemap account created for $_POST[username].".
		"Please activate by visiting this address: ".
		"http://www.free-map.org.uk/freemap/activate.php?userid=$row[lastid]".
		"&key=$random");
		?>
		<html>
		<head>
		<link rel='stylesheet' type='text/css' href='/freemap/css/freemap2.css'
		/>
		</head>
		<body> 
		<h1>Signed up!</h1>
		<p>You've successfully signed up. You will receive an email asking 
		you to confirm your registration shortly. Once you've gone to the
		address mentioned on the email, you'll be able to login and 
		contribute photos and access your home page.
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
	<link rel='stylesheet' type='text/css' href='/freemap/css/freemap2.css' />
	</head>
	<body> 
	<?php
	if($_GET['error']==1)
	{
		echo "<p class='error'>Error: Username already taken. ";
		echo "Please choose another one.</p>";
	}
	elseif($_GET["error"]==2)
	{
		echo "<p class='error'>Spaces not allowed in usernames.</p>";
	}
	elseif($_GET["error"]==3)
	{
		echo "<p class='error'>You've got to <em>actually provide</em> ".
			"a username and password!!!!!</p>";
	}
	else
	{
		?>
	<h1>Sign up</h1>
	<p>Signing up and creating an account allows you to:
		<ul>
		<li>upload photos taken on your walk or diagrams to illustrate
		points of difficulty along a walk;</li>
		<li>keep a record of your
	own personal walking routes on Freemap, each of which you can either 
	share with others or keep private. When logged in, you can view your
	own routes with the &quot;My routes&quot; option.
	This is also useful if you run a walking
	club and want to keep a record of the club's past walks, or add future
	routes, without cluttering up the public map.</li>
		</ul>
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
	<p>Your email will be used to send you a message to activate your account,
	and also to guard against spam: accounts with &quot;suspicious&quot; 
	looking email addresses run the risk of being deleted! You will be 
	informed if this happens.</p>
	<p><a href='/index.php'>Back to map</a></p>
	</body>
	</html>
	<?php
}
?>
