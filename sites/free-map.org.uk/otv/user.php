<?php
require_once('/home/www-data/private/defines.php');
require_once('../lib/functionsnew.php');

$conn=dbconnect("otv");
session_start();

$cleaned=clean_input($_REQUEST);
$cleaned["action"] = (isset($cleaned["action"]))?$cleaned["action"]:"login";

switch($cleaned['action'])
{
    case "login":
        if (isset($_POST['username']) && isset($_POST['password']))
        {
    
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
            account, <a href='user.php?action=signup'>sign up</a>!</p>
            <p>
            <form method="post" action="user.php?action=login">
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
    break;

    case "activate":
        if(isset($_GET['userid']) && isset($_GET['key']))
        {
            $id=$cleaned['userid'];
            $key=$cleaned['key'];
            $result=mysql_query
                ("SELECT * FROM users WHERE id=$id AND active=0");
            if(mysql_num_rows($result)==1)
            {
                $row=mysql_fetch_array($result);
                if($row['k']==$key)
                {
                    mysql_query("UPDATE users SET active=1,k=0 WHERE id=$id");
                    echo "You have now activated your account and may login.";
                }
                else
                {
                    mysql_query("DELETE FROM users WHERE id=$id");
                    echo "Invalid key. ".
                        "This account has now been deleted, you'll need ".
                     "to sign up again.";
                }
            }
            else
            {
                echo "No unactivated account with that ID.";
            }
        }
        else
        {
            echo "id/key not supplied.";
        }
        break;

    case "signup":
        if(isset($_POST["username"]) && isset($_POST["password"]))
        {
            $inp=clean_input($_POST);

            $result=mysql_query("SELECT * FROM users WHERE ".
                    "username='$_POST[username]'");
            if(mysql_num_rows($result)==1)
            {
                header('Location: /otv/user.php?action=signup&error=1');
            }
            elseif(strstr($inp["username"]," "))
            {
                header('Location: /otv/user.php?action=signup&error=2');
            }
            elseif($inp['username']=="" || $inp['password']=="" ||
                    $inp["email"]=="")
            {
                header('Location: /otv/user.php?action=signup&error=3');
            }
            else if (!isset($inp['agree']))
            {
                header('Location: /otv/user.php?action=signup&error=4');
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
                "http://www.free-map.org.uk/otv/user.php?".
                "userid=$id".
                "&key=$random&action=activate");
                ?>
                <html>
                <head>
                <link rel='stylesheet' type='text/css' href='/otv/css/osv.css'/>
                </head>
                <body> 
                <h1>Signed up!</h1>
                <p>You've successfully signed up. You will receive an email 
                asking you to confirm your registration shortly. Once you've 
                gone to the address mentioned on the email, you'll be able to 
                login and contribute photos and access your home page.
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
            <title>signup</title>
            <link rel='stylesheet' type='text/css' href='/otv/css/osv.css' />
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
                echo "<p class='error'>You've got to ".
                    "<em>actually provide</em> ".
                    "a username, password and email address!!!!!</p>";
            }
            elseif($_GET["error"]==4)
            {
                echo "<p class='error'>".
                    "You need to agree to the copyright statement.".
                "</p>";
            }
            ?>
            <h1>Sign up</h1>
            <p>Signing up and 
            creating an account allows you to upload photos to
            OpenTrailView.</p>
            <div>
            <form method="post" action="user.php?action=signup">
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
            (meaning: 
            people can do what they like with it as long as the copyright 
            holder is mentioned). Panoramas will be attributed to your OTV
            username.<br />
            <input type='submit' value='go'/>
            </form>
            </div>
            <p>Your email 
            will be used to send you a message to activate your account,
            and 
            also to guard against spam: accounts with &quot;suspicious&quot; 
            looking email addresses run the risk of being deleted! You will be 
            informed if this happens.</p>
            <p><a href='/otv/index.php'>Back to map</a></p>
            </body>
            </html>
            <?php
        }
        break;

    case "logout":
        session_destroy();
        header("Location: /otv/index.php");
        break;
}
mysql_close($conn);
?>
