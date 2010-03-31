<?php
session_start();
require_once('/home/www-data/private/defines.php');
require_once('../lib/functionsnew.php');
$conn=dbconnect("otv");

if(isset($_POST["username"]) && isset($_POST["password"]))
{
    $inp=clean_input($_POST);

    $result=mysql_query("SELECT * FROM users WHERE ".
        "username='$_POST[username]'");
    if(mysql_num_rows($result)==1)
    {
        header('Location: /otv/signup.php?error=1');
    }
    elseif(strstr($inp["username"]," "))
    {
        header('Location: /otv/signup.php?error=2');
    }
    elseif($inp['username']=="" || $inp['password']=="")
    {
        header('Location: /otv/signup.php?error=3');
    }
    else if (!isset($inp['agree']))
    {
        header('Location: /otv/signup.php?error=4');
    }    
    else
    {
        $random = rand (1000000,9999999);
        $q = ("insert into users (email,".
            "username,password,active,k) ".
                "values ('$inp[email]',".
                "'$inp[username]','".
                sha1($inp['password']).
                "',0,$random)");
            
        mysql_query($q); 
        $id=mysql_insert_id();
        mail('nick_whitelegg@yahoo.co.uk','New OTV account created', 
        "New OTV account created for $_POST[username] ".
        "(email $inp[email]). ");
        mail($inp['email'], 'New OpenTrailView account created', 
        "New OpenTrailView account created for $_POST[username].".
        "Please activate by visiting this address: ".
        "http://www.free-map.org.uk/otv/activate.php?userid=$id".
        "&key=$random");
        ?>
        <html>
        <head>
        <link rel='stylesheet' type='text/css' href='/otv/css/osv.css'
        />
        </head>
        <body> 
        <h1>Signed up!</h1>
        <p>You've successfully signed up. You will receive an email asking 
        you to confirm your registration shortly. Once you've gone to the
        address mentioned on the email, you'll be able to login and 
        contribute photos and access your home page.
        <a href='/otv/index.php'>Back to main page</a></p>
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
    <title>signup error</title>
    <link rel='stylesheet' type='text/css' href='/otv/css/osv.css'
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
    elseif($_GET["error"]==4)
    {
        echo "<p class='error'>You need to agree to the copyright statement.".
            "</p>";
    }
    else
    {
        ?>
    <h1>Sign up</h1>
    <p>Signing up and creating an account allows you to upload photos to
    OpenTrailView.</p>
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
    <input type="checkbox" name="agree" />
    I agree to licence my panorama under the 
    <a href='http://creativecommons.org/licenses/by/3.0/'>
    Creative Commons Attribution 3.0 licence</a>
    (meaning: people can do what they like with it as long as the copyright 
    holder is mentioned). Panoramas will be attributed to your OTV
	username.<br />
    <input type='submit' value='go'/>
    </form>
    </div>
    <p>Your email will be used to send you a message to activate your account,
    and also to guard against spam: accounts with &quot;suspicious&quot; 
    looking email addresses run the risk of being deleted! You will be 
    informed if this happens.</p>
    <p><a href='/otv/index.php'>Back to map</a></p>
    </body>
    </html>
    <?php
}
mysql_close($conn);
?>
