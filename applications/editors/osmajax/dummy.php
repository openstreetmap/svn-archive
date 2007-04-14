<?php
session_start();

if(!isset($_SESSION['id']))
	$_SESSION['id'] = 65536; 
echo $_SESSION['id']++;

?>
