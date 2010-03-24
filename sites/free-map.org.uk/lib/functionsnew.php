<?php

// define DB_USERNAME, DB_PASSWORD and DB_DBASE in this file
// Not in SVN for obvious reasons
require_once('/var/www-data/private/defines.php');

// Generic stuff - might be useful for other projects
// This code is licenced under the LGPL

function dbconnect($db=DB_DBASE)
{
	$conn = mysql_connect("localhost",DB_USERNAME,DB_PASSWORD);
	mysql_select_db($db);
	return $conn;
}

// get user level for a username
// return null if the username can't be found

function get_user_level($username,$usertable='users',$levelfield='level',
							$userfield='username',$db='mysql')
{
	switch($db)
	{
		case "mysql":
			$result = mysql_query
			("select $levelfield from $usertable where $userfield='$username'")
			or die (mysql_error());
			if(mysql_num_rows($result)==1)
			{
				$row=mysql_fetch_array($result);
				return $row[$levelfield];
			}
			break;
		case "pgsql":
			$result = pg_query
			("select $levelfield from $usertable where $userfield='$username'");
			$row=pg_fetch_array($result,null,PGSQL_ASSOC);
			if($row)
				return $row[$levelfield];
			break;
	}
	return null;
}

function get_user_id ($username,$usertable='users',$userfield='username',
						$idfield='id',$db='mysql')
{
	$q=("select $idfield from $usertable where $userfield='$username'");
	switch($db)
	{
		case "mysql":
			$result=mysql_query($q);
			if(mysql_num_rows($result)==1)
			{
				$row=mysql_fetch_array($result);
				return $row[$idfield];
			}
			break;
		case "pgsql":
			$result=pg_query($q);
			$row=pg_fetch_array($result,null,PGSQL_ASSOC);
			if($row)
			{
				return $row[$idfield];
			}
			break;
	}
	return 0;
}

function check_login($username,$password,$usertable='users',
					 $db='mysql',$encode='MD5')
{
	$q=
		("SELECT * FROM $usertable WHERE username='$username' and ".
		 "password=MD5('$password')");
	$code=false;
	switch($db)
	{
		case "mysql":
			$result=mysql_query($q);
			$code=mysql_num_rows($result)==1;
			break;
		case "pgsql":
			$result=pg_query($q);
			$row=pg_fetch_array($result,null,PGSQL_ASSOC);
			$code=($row) ? true:false;
			break;
	}
	return $code;
}

function check_all_params_supplied ($params, $expected)
{
	if ($expected!==null)
	{
		foreach ($expected as $param)
		{
			if(!isset($params[$param]))
			{
				return false;
			}
		}
	}
	return true;
}

function clean_input ($inp)
{
	$cleaned = array();
	foreach ($inp as $k=>$v)
		$cleaned[$k] = htmlentities(mysql_real_escape_string($inp[$k]));
	return $cleaned;
}

function make_sql_date($day, $month, $year)
{
	return sprintf("%04d-%02d-%02d", $year, $month, $day);
}

// Generic edit table by ID function
function edit_table ($table, $id, $inp, $uniquefield='id',$db='mysql')
{
	if (is_array($inp) && count($inp))
	{
		$first=true;
		$q = "UPDATE $table SET ";
		foreach($inp as $field=>$value)
		{
			if (!$first)
				$q .= ",";
			else
				$first=false;
			$q .= ($value===null) ? "$field=NULL":"$field='$value'"	;
		}
		$q .= " WHERE $uniquefield='$id'";
		if($db=='pgsql')
			pg_query($q);
		else
			mysql_query($q);
		return true;
	}
	return false;
}

function filter_array($inp,$keys)
{
	$out=array();
	foreach ($keys as $key)
	{
		if(isset($inp[$key]))
			$out[$key] = $inp[$key];
	}
	return $out;
}

// General delete by ID function
function delete_from_table ($table,$id,$uniquefield='id',$db='mysql')
{
	$q="DELETE FROM $table WHERE $uniquefield='$id'";
	if($db=='pgsql')
		pg_query($q);
	else
		mysql_query($q);
}

function searchby($table,$searchterm,$searchby,$db='mysql')
{
	$userdetails = array();
	switch($db)
	{
		case 'mysql':
			$result = mysql_query
				("SELECT * FROM $table WHERE $searchby LIKE '%$searchterm%'");
			while($row=mysql_fetch_assoc($result))
				$userdetails[] = $row;
			break;
		case 'pgsql':
			$result = pg_query
				("SELECT * FROM $table WHERE $searchby ILIKE '%$searchterm%'");
			while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
				$userdetails[] = $row;
			break;
	}

	return $userdetails;
}

function upload_file($uploaddir="/home/www-data/uploads")
{
	$userfile = $_FILES['userfile']['tmp_name'];
	$userfile_name = $_FILES['userfile']['name'];
	$userfile_size = $_FILES['userfile']['size'];
	$userfile_type = $_FILES['userfile']['type'];
	$userfile_error = $_FILES['userfile']['error'];

	if ($userfile_error>0)
	{
		switch($userfile_error)
		{
			case 1: $err =  "exceeded upload max filesize"; break;
			case 2: $err =  "exceeded max filesize"; break;
			case 3: $err =  "partially uploaded"; break;
			case 4: $err =  "not uploaded"; break;
		}
	}
	else
	{
		$upfile = "$uploaddir/$userfile_name";
		if(is_uploaded_file($userfile))
		{
			if(!move_uploaded_file($userfile,$upfile))
			{
				$err =  "could not move file"; 
			}
		}
		else
		{
			$err = "File upload security violation detected";
		}
	}

	$ret = array("file"=>(isset($err) ? null: $upfile),
				 "error"=>(isset($err) ? $err: null) ); 
	return $ret;
}

function display_record ($row,$fields)
{
	echo "<p>";
	foreach ($fields as $fieldname=>$displayed)
		echo "$displayed : $row[$fieldname]<br/>";
	echo "</p>";
}

function check_month($month)
{
	$months = array ("January","February","March","April","May","June",
				  "July","August","September","October","November","December" );
	return in_array ( $month, $months );
}

function js_error($err, $redirect)
{
	?>
	<html>
	<head>
	<script type='text/javascript'>
	<?php
	echo "alert('$err');\n";
	echo "location='$redirect';\n";
	?>
	</script>
	</head>
	</html>
	<?php
}

?>
