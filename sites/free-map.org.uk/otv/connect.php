<?php

// define DB_USERNAME, DB_PASSWORD and DB_DBASE in this file
// Not in SVN for obvious reasons
require_once('/var/www-data/private/defines.php');

$conn=mysql_connect("localhost",DB_USER,DB_PASSWORD);
mysql_select_db(DB_DBASE);
?>
