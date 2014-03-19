<?php

global $CONNECT,$RESULT,$DBDATABASE,$DBUSER,$DBPASSWORD;

$DBHOST = "<server>";
$DBDATABASE = "<dba>";
$DBUSER = "<user>";
$DBPASSWORD = "<<password>>";
$CONNECT = pg_connect("host=$DBHOST dbname=$DBDATABASE password=$DBPASSWORD user=$DBUSER")
 or die("Database is not available.");
$set = pg_query($CONNECT,"set search_path to ruian,osmtables,public;");
?>
