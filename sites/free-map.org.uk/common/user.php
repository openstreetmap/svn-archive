<?php
require_once('/home/www-data/private/defines.php');
require_once('../lib/functionsnew.php');

session_start();

$conn=pg_connect("dbname=gis user=gis");
$cleaned=clean_input($_REQUEST,'pgsql');


switch($cleaned["action"])
{
    case "login":
        if (isset($cleaned['username']) && isset($cleaned['password']))
        {

            $q=    
            ("select * from users where username='$cleaned[username]' ".
              "and password='".sha1($cleaned['password'])."' and active=1");
            $result=pg_query($q);
            $row=pg_fetch_array($result,null,PGSQL_ASSOC);
            if($row)
            {
                $_SESSION["gatekeeper"] = $row["id"];
                $qs="";
                foreach ($cleaned as $k=>$v)
                {
                    if($k!="username" && $k!="password" && $k!="redirect" &&
						$k!="action")
                    {
                        $qs .= "$k=$v&";
                    }
                }
				if(strpos($cleaned['redirect'],"?")!==false)
				{
					list($cleaned['redirect'],$redirqs) = explode("?",
						$cleaned['redirect']);
					$qs = $redirqs."&".$qs;
				}
            	header("Location: $cleaned[redirect]?$qs");
			}
            else
            {
                pg_close($conn);
                js_error('Invalid login',$cleaned['redirect']);
            }
        }
		else
		{
			if(!isset($cleaned['redirect']))
				$cleaned['redirect'] = '/index.php';
			echo "<h1>Login</h1>\n";
			echo "<form method='post' action='user.php?action=login&redirect=$cleaned[redirect]'>\n";
			?>
			<label for="username">Username</label><br/>
			<input name="username" id="username" /><br/>
			<label for="password">Password</label><br/>
			<input name="password" id="password" type="password" /><br/>
			<input type="submit" value="Go!" />
			<?php
		}
        break;

    case "signup":

        if(isset($cleaned["username"]) && isset($cleaned["password"]))
        {

            $result=pg_query("SELECT * FROM users WHERE ".
                        "username='$cleaned[username]'");
            $row=pg_fetch_array($result,null,PGSQL_ASSOC);
            if($row)
            {
                header('Location: /common/user.php?action=signup&error=1');
            }
            elseif(strstr($cleaned["username"]," "))
            {
                header('Location: /common/user.php?action=signup&error=2');
            }
            elseif($cleaned['username']=="" || $cleaned['password']=="")
            {
                header('Location: /common/user.php?action=signup&error=3');
            }
            else
            {
                $random = rand (1000000,9999999);
                $q = ("insert into users (email,".
                    "username,password,active,k) ".
                    "values ('$cleaned[email]',".
                    "'$cleaned[username]','".
                    sha1($cleaned['password']).
                    "',0,$random)");
            
                pg_query($q); 
                $result=pg_query("select currval('users_id_seq') as lastid");
                $row=pg_fetch_array($result,null,PGSQL_ASSOC);
                mail('nick_whitelegg@yahoo.co.uk',
                    'New Freemap account created', 
                    "New Freemap account created for $cleaned[username] ".
                    "(email $cleaned[email]). ".
                    "<a href=".
                    "\"http://www.free-map.org.uk/common/user.php?action=".
						"delete&id=$row[lastid]\">Delete</a>");
                mail($cleaned['email'], 'New Freemap account created', 
                    "New Freemap account created for $cleaned[username].".
                    "Please activate by visiting this address: ".
                    "http://www.free-map.org.uk".
                    "/common/user.php?action=activate&userid=$row[lastid]".
                "&key=$random");
                ?>
                <html>
                <head>
                <link rel='stylesheet' 
                type='text/css' href='/freemap/css/freemap2.css'
                />
                </head>
                <body> 
                <h1>Signed up!</h1>
                <p>You've successfully 
                signed up. You will receive an email asking 
                you to confirm your 
                registration shortly. Once you've gone to the
                address mentioned on the email, you'll be able to login.
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
            <link rel='stylesheet' 
            type='text/css' href='/freemap/css/freemap2.css' />
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
                echo "<p class='error'>You've got to 
                    <em>actually provide</em> ".
                "a username and password!!!!!</p>";
            }
            else
            {
                ?>
                <h1>Sign up</h1>
                <p>Signing up and creating a Freemap/OpenTrailView account will
				allow you to:
					<ul>
					<li>annotate paths and save walking routes on 
					<a href='http://www.free-map.org.uk'>Freemap</a>;</li>
					<li>contribute panoramas on 
					<a href='http://www.opentrailview.org'>OpenTrailView</a>.
					</li>
					</ul>
				You can use the same login on both sites.</p>
                <?php
            }
            ?>
            <div>
            <form method="post" action="?action=signup">
            <label for="username">Enter your email address</label><br/>
            <input name="email" id="email" /> <br/>
            <label for="username">Enter a username</label><br/>
            <input name="username" id="username" /> <br/>
            <label for="password">Enter a password</label> <br/>
            <input name="password" id="password" type="password" /> <br/>
            <input type='submit' value='go'/>
            </form>
            </div>
            <p>Your email will be 
            used to send you a message to activate your account,
            and also to guard against spam: 
            accounts with &quot;suspicious&quot; 
            looking email addresses run the risk of being deleted! You will be 
            informed if this happens.</p>
            <p><a href='/index.php'>Back to map</a></p>
            </body>
            </html>
            <?php
        }
        break;
    case "delete":
        if(!isset($cleaned['id']))
        {
            echo "How do you expect this ".
            "to work if you don't provide a user ID? ;-)";
        }
        elseif(!isset($_SESSION['gatekeeper']))
        {
            ?>
            <html>
			<head>
			<link rel='stylesheet' type='text/css' href='css/freemap2.css' />
			</head>
			<body>
            <h1>Login</h1>
            <form method='post' 
			action="user.php?action=login&redirect=user.php?action=delete">
            <label for='username'>Username</label><br/>
            <input name='username' id='username' /><br/>
            <label for='password'>Password</label><br/>
            <input name='password' id='password' type='password'/><br/>
            <input type='hidden' name='id' value='<?php echo $cleaned['id']?>'>
            <input type='submit' value='Go!'/>
            </form>
            </body></html>
            <?php
        }
        else if (get_user_level($_SESSION['gatekeeper'],'users','isadmin',
                'id','pgsql') != 1)
        {
            echo "Stop trying to delete other people's accounts!!!!!";
        }
        else 
        {
            $id=(int)($cleaned['id']);
            $result=pg_query("SELECT * FROM users WHERE id=$id");
            if($row=pg_fetch_array($result,null,PGSQL_ASSOC))
            {
                pg_query("DELETE FROM users WHERE id=$cleaned[id]");
                mail($row['email'],"Freemap account deleted",
                "Due to a suspicious looking email address and/or ".
                "attempted spamming, your account has been deleted - please ".
                "email me on nick_whitelegg@yahoo.co.uk if you think this ".
                "is an error.");
            }
            else
            {
                echo "Invalid user ID";
            }
        }
        break;

    case "activate":
        if(isset($_GET['userid']) && isset($_GET['key']))
        {
            $id=$cleaned['userid'];
            $key=$cleaned['key'];
            $result=pg_query
                ("SELECT * FROM users WHERE id=$id AND active=0");
            if($result)
             {
                $row=pg_fetch_array($result,null,PGSQL_ASSOC);
                if($row['k']==$key)
                {
                    pg_query("UPDATE users SET active=1,k=0 WHERE id=$id");
                    echo "You have now activated your account and may login.";
                }
                else
                {
                    pg_query("DELETE FROM users WHERE id=$id");
                    echo "Invalid key. ";
                    echo "This account has now been deleted, you'll need ".
                         "to sign up again.";
                }
            }
            else
            {
                echo "Invalid id/key.";
            }
        }
        else
        {
            echo "id/key not supplied.";
        }
 
    	break;

	case "logout":
		session_start();
		session_destroy();
		header("Location: /index.php");
		break;

	case "routes":
		$result=pg_query("SELECT * FROM users WHERE id=$_SESSION[gatekeeper]");
		$row=pg_fetch_array($result,null,PGSQL_ASSOC);
		?>
		<html>
		<head>
		<title><?php echo $row['username']?>'s walk routes</title>
		<link rel='stylesheet' 
		type='text/css' href='/freemap/css/freemap2.css' />
		</head>
		<body>
		<h1><?php echo $row['username']?>'s walk routes</h1>
		<?php
		$formats = array ("htmlpage" => "HTML",
						"pdf" => "PDF",
						"xml" => "XML" );
		echo "<ul>\n";
		$result=pg_query("SELECT *,length(route) FROM routes WHERE userid=".
						"$_SESSION[gatekeeper] ORDER BY id");
		while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
		{
			echo "<li>Walk route of ".
				(round($row['length']/1000,2))." km ";
			foreach($formats as $qsformat=>$txtformat)
			{
				echo "<a href='route.php?action=get&id=$row[id]&source=db".
					"&format=$qsformat'>$txtformat</a> ";
			}
			echo "</li>\n";
		}
		echo "</ul>\n";
		echo "<p><a href='/index.php'>Back to map</a></p>\n";
		break;
}

pg_close($conn);
?>
</body>
</html>
