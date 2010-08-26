<?php

include ('../lib/functionsnew.php');

session_start();

$conn=dbconnect("otv");
$cleaned=clean_input($_REQUEST);

switch($cleaned['action'])
{
    case "add":
    if(!isset($_SESSION["gatekeeper"]))
    {
           header("HTTP/1.1 401 Unauthorized"); 
    }
    else
    {
        $others = explode(",", $cleaned['others']);
        $angles = explode(",", $cleaned['angles']);

        if(count($others) != count($angles) || !isset($cleaned['parent']))
        {
            header("HTTP/1.1 400 Bad Request");
        }
        else
        {
            for($i=0; $i<count($others); $i++)
            {
                $q = "UPDATE panoramas SET parent=$cleaned[parent],".
                    "orientation=".$angles[$i]." WHERE ID=".$others[$i];
                mysql_query($q) or die(mysql_error());
            }    
        }
    }

    break;


    case "view":
        ?>
        <html>
        <head>
        <link rel='stylesheet' type='text/css' href='css/osv.css' />
        </head>
        <body>
        <?php
        $parent = $cleaned['parent'];
        $result=mysql_query("SELECT * FROM panoramas WHERE parent=$parent");
        $nresults = mysql_num_rows($result);
        if($nresults>0)
        {    
            echo "<table>";
            echo "<tr>";
            echo "<td width='50%'>".
                "<p>Parent</p>".
                "<img src='/otv/panorama/$parent' alt='photo $parent' /> ";
            echo "</td>";
            $count=1;
            while($row=mysql_fetch_array($result))
            {
                if(!($count%2))
                    echo "<tr>";
                echo "<td width='50%'>".
                    "<p>".
                    "$row[orientation] deg from parent </p>".
                    "<img src='/otv/panorama/$row[ID]' ".
                    "alt='photo $row[ID]' />";
                echo "</td>";
                if($count%2)
                    echo "</tr>";
                $count++;
            }
            echo "</table>";
            echo "</body></html>";
        }
    break;
}
mysql_close($conn);
?>
