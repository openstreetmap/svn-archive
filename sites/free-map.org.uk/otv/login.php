<?php
require_once('/home/www-data/private/defines.php');
require_once('../lib/functionsnew.php');

$conn=dbconnect("otv");
session_start();

if (isset($_POST['username']) && isset($_POST['password']))
{
    
    $cleaned=clean_input($_POST);
    $q=    
        ("select * from users where username='$cleaned[username]' ".
          "and password='".sha1($cleaned['password'])."' and active=1");
    $result=mysql_query($q);
    if(mysql_num_rows($result)==1)
    {
        $row=mysql_fetch_array($result);
        $_SESSION["gatekeeper"] = $row["id"];
        if($row['isadmin']==1)
            $_SESSION['admin'] = true;
        mysql_close($conn);
        $qs="";
        foreach ($cleaned as $k=>$v)
        {
            if($k!="username" && $k!="password" && $k!="redirect")
            {
                $qs .= "$k=$v&";
            }
        }
        header("Location: $cleaned[redirect]?$qs");
    }
    else
    {
        mysql_close($conn);
        js_error('Invalid login',$_POST['redirect']);
    }
}
else
{
    ?>
    <html>
    <head>
    <title>Log in</title>
	<link rel='stylesheet' type='text/css' href='css/osv.css' />
    </head>
    <body>
    <h1>Log in</h1>
    <p>You need to login to contribute panoramas. If you do not have an
    account, <a href='signup.php'>sign up</a>!</p>
    <p>
    <form method="post" action="">
    <label for="username">Username:</label> <br />
    <input name="username" id="username" /> <br />
    <label for="password">Password:</label> <br />
    <input type="password" name="password" id="password" /> <br />
    <?php
    if(isset($_REQUEST['redirect']))
    {
        echo "<input type='hidden' name='redirect' ".
            "value='$_REQUEST[redirect]' />\n";
    
    }
    ?>
    <input type="submit" value="Go!" />
    </form>
    </p>
    <?php
}
?>
