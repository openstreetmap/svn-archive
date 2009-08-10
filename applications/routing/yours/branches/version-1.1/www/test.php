<?php 
	$output = array();
	
	exec("stat /home/lambertus/planet.openstreetmap.org/planet-latest.osm.bz2", $output);
	foreach ($output as $line) {
		//echo $line."\n";
		$parts = explode(" ", $line);
		if ($parts[0] == 'Modify:') {
			echo $parts[1]."\n";
		}
	}
	
	
?>
