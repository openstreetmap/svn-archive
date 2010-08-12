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
        echo "You need to be logged in to manage your photos!";
    }
    else if (!isset($_POST['submitted']))
    {
        $cleaned=clean_input($_GET);
        $parent = $cleaned['parent'];
        $others = explode(",", $cleaned['others']);
        echo "<html>";
        echo "<body>";
        echo "<h2>Parent photo</h2>";
        echo "<img src='/otv/panorama/$parent/' alt='Parent photo' /><br />";
        echo "<h2>Other photos</h2>";
        echo "<form method='post' action=''>";
        foreach($others as $other)
        {
            echo "<p>";
            echo "<img src='/otv/panorama/$other/' ".
                "alt='ID $other' width='25%' />".  "<br />";
            echo "Angle, with respect to parent: ";
            echo "<select name='angle$other'>\n";
            echo "<option value='90'>90 deg clockwise</option>";
            echo "<option value='180'>180 deg</option>";
            echo "<option value='-90'>90 deg anticlockwise</option>";
            echo "</select>";
            echo "</p>";
        }
        echo "<input type='hidden' name='parent' value='$parent' />";
        echo "<input type='hidden' name='action' value='add' />";
        echo "<input type='submit' value='Go!' name='submitted' />";
        echo "</body></html>\n";
    } 
    else
    {
        foreach($_POST as $field=>$value)
        {
            if(substr($field,0,5)=="angle")
            {
                $id=substr($field,5);
				/*
                $q="INSERT INTO photogroups(photoID,parentID,orientation) ".
                    "VALUES ($id,$_POST[parent],$value)";
				*/
				$q = "UPDATE panoramas SET parent=$_POST[parent],".
					"orientation=$value WHERE ID=$id";
                mysql_query($q) or die(mysql_error());
            }
        }
        js_error('grouped successfully',"/otv/photomgr.php");
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
