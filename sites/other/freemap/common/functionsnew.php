<?php

require_once('defines.php');

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
							$userfield='username')
{
	$result = mysql_query
		("select $levelfield from $usertable where $userfield='$username'")
		or die (mysql_error());
	if(mysql_num_rows($result)==1)
	{
		$row=mysql_fetch_array($result);
		return $row[$levelfield];
	}
	return null;
}

function get_user_id ($username,$usertable='users',$userfield='username',
						$idfield='id')
{
	$q=("select $idfield from $usertable where $userfield='$username'");
	$result=mysql_query($q);
	if(mysql_num_rows($result)==1)
	{
		$row=mysql_fetch_array($result);
		return $row[$idfield];
	}
	return 0;
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
function edit_table ($table, $id, $inp, $uniquefield='id')
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
		mysql_query($q) or die(mysql_error());
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
function delete_from_table ($table,$id,$uniquefield='id')
{
	mysql_query("DELETE FROM $table WHERE $uniquefield='$id'") 
				or die (mysql_error());
}

function searchby($table,$searchterm,$searchby)
{
	$userdetails = array();
	$result = mysql_query
		("SELECT * FROM $table WHERE $searchby LIKE '%$searchterm%'");
	while($row=mysql_fetch_assoc($result))
		$userdetails[] = $row;
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
