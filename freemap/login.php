<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################

require_once('defines.php');

session_start();

$conn = mysql_connect (DB_HOST,DB_USERNAME,DB_PASSWORD);
mysql_select_db (DB_DBASE);

if(preg_match
	("/^[a-zA-Z0-9-_\.]+@([a-zA-Z0-9-_]+\.)+[a-zA-Z0-9-_]+$/",
	$_POST['username']))
{
	$q=("select * from usersnew where email='".
					$_POST['username']."'");
	$result=mysql_query($q);
	if(mysql_num_rows($result)==1)
	{
		$row=mysql_fetch_array($result);
		if(sha1($_POST['password'])==$row['password'])
		{
			$_SESSION["ngatekeeper"] = $_POST["username"]; 
			header("Location: ".$_POST["referrer"]);
		}
		else
		{
			header("Location: ".$_POST["referrer"]);
		}
	}
	else
	{
		header("Location: ".$_POST["referrer"]);
	}	
}
else
{
	header("Location: ".$_POST["referrer"]);
}
?>
