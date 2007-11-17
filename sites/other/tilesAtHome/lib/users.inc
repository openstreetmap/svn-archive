<?php
include_once('log.inc');
require_once('config.inc.php');
require_once($conf['BASEDIR']."/connect/connect.php");

/// authenticates a user against the password in the database and returns the user id or -1 on wrong password
/// returns NULL on error
function checkUser($SuppliedUsername, $SuppliedPassword){
    $SQL = sprintf("SELECT id,disabled FROM tiles_users WHERE name='%s' and password='%s';", 
      mysql_real_escape_string($SuppliedUsername),
      md5($SuppliedPassword));
    $Result = mysql_query($SQL);
    if(mysql_error()){
      logSqlError();
      return (-1);
    }
    if(mysql_num_rows($Result) == 0){
      logMsg("Failed user authentication for user ".$SuppliedUsername, 5);
      return(-1);
    }
    $Row = mysql_fetch_assoc($Result);
    if ($Row['disabled']) {
      return -1;
    } else {  
      return($Row['id']);
    }  
}

/// return a user name from its ID
/// return NULL on error
function lookupUser($UserID){
    $SQL = sprintf("SELECT name FROM tiles_users WHERE `id`=%s;", mysql_real_escape_string($UserID));
    $Result = mysql_query($SQL);
    if(mysql_error()){
    	logSqlError();
	return (NULL);
    }
    if(mysql_num_rows($Result) == 0){return(-1);}
    $Row = mysql_fetch_assoc($Result);

    return($Row['name']);
}
?>
