<?php
session_start();
unset($_SESSION["osmusername"]);
unset($_SESSION["password"]);
header("Location: /freemap/edit.php");
?>
