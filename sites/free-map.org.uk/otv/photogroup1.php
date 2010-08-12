<?php

include ('../lib/functionsnew.php');

session_start();

$conn=dbconnect("otv");
$cleaned=clean_input($_REQUEST);

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

mysql_close($conn);

?>
