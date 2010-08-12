<?php

include ('../lib/functionsnew.php');
include('index_funcs.php');

session_start();

if(!isset($_SESSION["gatekeeper"]))
{
   	header("HTTP/1.1 401 Unauthorized"); 
}
else
{
	$cleaned = clean_input($_GET);
    $conn=dbconnect("otv");
	$result=mysql_query("SELECT * FROM panoramas WHERE user=".
						"$_SESSION[gatekeeper] AND parent IS NULL ".
						"LIMIT ". ($cleaned["pg"]*$cleaned["n"]).
							",$cleaned[n]");

	$result_all=mysql_query
		("SELECT * FROM panoramas WHERE user=$_SESSION[gatekeeper] AND ".
		"parent IS NULL");
	$num_photos=mysql_num_rows($result_all);

	$data = array();
	if(($cleaned['pg']+1)*$cleaned['n'] >= $num_photos)
		$data['lst'] = 1;

	while($row=mysql_fetch_assoc($result))
	{
		$curRow = array();
		$curRow["lat"] = ($row["lat"]);
		$curRow["lon"] = $row["lon"];
		$curRow["id"] = $row["ID"];
		$curRow["isPano"] = ($row["isPano"])?1:0;

		$result2=mysql_query("SELECT * FROM panoramas WHERE parent=".
							"$row[ID]");
		$curRow["parent"] = (mysql_num_rows($result2)>0) ? true:false;
		$data['photos'][] = $curRow;
	}

	mysql_close($conn);

	header("Content-type: application/json");
	echo json_encode($data);
}

?>
