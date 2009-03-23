<?php
$ps = array();
$output = array();

echo '<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">';
echo '<head></head><body>';

echo "Start killing Gosmore processes:<br><br>";

exec("ps ax | grep gosmore", $ps, $return_var);

echo "Found ".Count($ps)." processes containing 'Gosmore'<br>";
//print_r($ps);
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
		echo "Killing ".$properties[0]."<br>";
		exec("kill -9 ".$properties[1], $output);
	}
}
echo '</body>';
?>
