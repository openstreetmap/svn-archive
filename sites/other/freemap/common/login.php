<?php
require_once('defines.php');

session_start();

if(isset($_POST['username']) && isset($_POST['password']))
{
	$conn=mysql_connect(DB_HOST,DB_USERNAME,DB_PASSWORD);
	mysql_select_db(DB_DBASE);

    $result = mysql_query
        ("select * from freemap_users where username='$_POST[username]' and ".
            "password=MD5('$_POST[password]') and active=1");
    if(mysql_num_rows($result))
    {
        $_SESSION["gatekeeper"] = $_POST["username"];
		mysql_close($conn);
        header("Location: /index.php");
	}
	else
	{
		mysql_close($conn);
		header("Location: /common/login.php?error=1");
	}
}
else
{
    ?>
    <html>
    <head>
    <title>Login to Freemap</title>
	<link rel='stylesheet' type='text/css' href='/css/freemap2.css'/>
    <style type="text/css">
    body { font-size: 120% }
	.error { color: red }
	fieldset { width: 40% }
    </style>
    </head>
    <body>
	<img src='/images/freemap.png' alt='freemap logo' />
	<?php
	if (isset($_GET['error']))
		echo "<p class='error'>Error: Incorrect Login.</p>\n";
	?>
    <h1>Login to Freemap </h1>
    <form method="post" action="">
    <fieldset>
    <label for="username">Username</label>
    <input name="username" id="username"/> <br/>
    <label for="password">Password</label>
    <input name="password" id="password" type="password"/><br/>
    <input type="submit" value="Login"/>
    </fieldset>
    </form>
    </body>
    </html>
    <?php
}
?>
