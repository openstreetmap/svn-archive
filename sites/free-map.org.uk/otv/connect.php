<?php
require_once("/home/www-data/private/defines.php")
global $conn;
$conn=mysql_connect("localhost",DB_USER,DB_PASSWORD);
mysql_select_db("otv");
?>
