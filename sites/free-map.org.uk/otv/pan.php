<?php
include('connect.php');
include('../lib/functionsnew.php');
$cleaned=clean_input($_GET);
$result=mysql_query("SELECT * FROM panoramas WHERE ID=$cleaned[id]");
if(mysql_num_rows($result)==1)
{
    $row=mysql_fetch_array($result);
    if($row['authorised']==1)
    {
        switch($cleaned['action'])
        {

            case "changeAngle":
                mysql_query
                    ("UPDATE panoramas SET direction=$cleaned[angle] ".
                     "WHERE ID=$cleaned[id]");
				echo "Angle set to $cleaned[angle]";
                break;

            default:
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
    }
    else
    {
        header("HTTP/1.1 401 Unauthorized");
    }
}
else
{
    header("HTTP/1.1 404 Not Found");
} 

?>
