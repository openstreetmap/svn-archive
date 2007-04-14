<?php

function get_user_id($username)
{
	$result = mysql_query
		("select id from freemap_users where username='$username'");
	if(mysql_num_rows($result))
	{
		$row=mysql_fetch_array($result);
		$id = $row["id"];
		return $id;
	}
	return 0;
}


?>
