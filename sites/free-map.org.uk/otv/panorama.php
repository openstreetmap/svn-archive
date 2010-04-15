<?php
session_start();
include('../lib/functionsnew.php');
$conn=dbconnect("otv");
$cleaned=clean_input($_GET);
$result=mysql_query("SELECT * FROM panoramas WHERE ID=$cleaned[id]");
if(mysql_num_rows($result)==1)
{
    $row=mysql_fetch_array($result);
        switch($cleaned['action'])
        {
            case "rotate":
                if(isset($_SESSION['gatekeeper']))
                {
                    mysql_query
                    ("UPDATE panoramas SET direction=$cleaned[angle] ".
                     "WHERE ID=$cleaned[id]");
                    echo "Angle set to $cleaned[angle]";
                }
                else
                {
                    header("HTTP/1.1 401 Unauthorized");
                }
                break;

            case "authorise":
                if(isset($_SESSION['admin']))
                {
                    mysql_query("UPDATE panoramas SET authorised=1 ".
                                "WHERE ID=$cleaned[id]");
                    echo "Authorised";
                }
                else
                {
                    header("HTTP/1.1 401 Unauthorized");
                }
                break;
            case "delete":
                if( (isset($_SESSION['gatekeeper']) &&
						$row['user']==$_SESSION['gatekeeper']) ||
							isset($_SESSION['admin']))
				{
                    	mysql_query
							("DELETE FROM panoramas WHERE ID=$cleaned[id]");
						unlink("/home/www-data/uploads/otv/${cleaned[id]}.jpg");
                    	echo "Deleted";
                }
                else
                {
                    header("HTTP/1.1 401 Unauthorized");
                }
                break;
            case "moderate":
                if(isset($_SESSION['admin']))
                {
                    echo "<html>";
					echo "<head>";
					echo "<link rel='stylesheet' type='text/css' ".
						"href='css/osv.css' />";
					echo "</head>";
					echo "<body><p>";
                    echo "<h1>Submitted photo $cleaned[id]</h1>\n";
                    echo "<p><img src='/otv/panorama/$cleaned[id]' ".
                    "alt='Panorama $cleaned[id]' /></p>\n";
                    echo "<a href='/otv/panorama/$cleaned[id]/authorise'>".
                        "Authorise</a> ";
                    echo 
                      "<a href='/otv/panorama/$cleaned[id]/delete'>Delete</a>";
                    echo "</p></body></html>";
                }
                else
                {
                    header("Location: ".
                        "/otv/user.php?action=login&redirect=".
						"/otv/panorama/$cleaned[id]/moderate");
                }
                break;
            default:
                if($row['authorised']==1 || isset($_SESSION['admin']))
                {
                    $file = "/home/www-data/uploads/otv/${cleaned[id]}.jpg";
                    if(file_exists($file))
                    {
                        header("Content-type: image/jpeg");
                        $f = file_get_contents($file);
                        echo $f;
                    }
                    else
                    {
                        header("HTTP/1.1 404 Not Found");
                    }
                }
                else
                {
                    header("HTTP/1.1 401 Unauthorized");
                }
                break;
        }
}
else
{
    header("HTTP/1.1 404 Not Found");
} 
mysql_close($conn);
?>
