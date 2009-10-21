<?php
/* Copyright (c) 2009, L. IJsselstein and others
  Yournavigation.org All rights reserved.
 */
 
$allowed_hosts = array('213.10.112.251', '193.141.16.74');

$ps = array();
$output = array();
$host = $_SERVER["REMOTE_ADDR"];

echo '<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">';
echo '<head></head><body>';

if (in_array($host, $allowed_hosts) == false )
{
	echo "You are not allowed to use this function ($host)";
}
else
{
	exec("id", $output);
var_dump($output);
	echo "<br>";

	echo "Start killing Gosmore processes:<br><br>";

	exec("ps ax | grep gosmore", $ps, $return_var);

	echo "Found ".Count($ps)." processes containing 'gosmore'<br>";
	foreach ($ps as $row => $process)
	{
		echo "$process<br>";
	}
	echo "<br>Actions:<br>";
	foreach ($ps as $row => $process)
	{
		$bKill = true;
		$properties = array();
		$properties = split(" ", $process);
	
		foreach ($properties as $item => $property)
		{
			//echo "property ".$item." = ".$property."\n";
			if (strstr($property, "grep"))
			{
				echo "Not killing ".$properties[0]."<br>";
				$bKill = false;
			}
		}
		if ($bKill == true)
		{
			echo "Killing ".$properties[0];
			shell_exec("kill -9 ".$properties[0]);
			echo "<br>";
		}
	}
}
echo '</body>';
?>
