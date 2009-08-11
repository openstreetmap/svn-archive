<?php

$bRunGosmore = true;
if (getProcesses() > 2) {
	$bRunGosmore = false; 
	$kml = "Server is busy, please try again later (".getProcesses().")";
}

if ($bRunGosmore) {
	$script_start = microtime(true);

	$www_dir = '/home/lambertus/public_html/yours';
	$gosmore_dir = '/home/lambertus/planet/yours';
	$ulimit = 30;

	$user_agent = $_SERVER['HTTP_USER_AGENT'];
	$query = "QUERY_STRING='";

	//Coordinates
	if (isset($_GET['flat'])) {
		$query .= 'flat='.$_GET['flat'];
		$flat = $_GET['flat'];
	}
	else {	
		$query .= 'flat=53.04821';
		$flat = 53.04821;
	}

	if (isset($_GET['flon'])) {
		$query .= '&flon='.$_GET['flon'];
		$flon = $_GET['flon'];
	}
	else {
		$query .= '&flon=5.65922';
		$flon = '5.65922';
	}

	if (isset($_GET['tlat'])) {
		$query .= '&tlat='.$_GET['tlat'];
	}
	else {
		$query .= '&tlat=53.02616';
	}

	if (isset($_GET['tlon'])) {
		$query .= '&tlon='.$_GET['tlon'];
		$tlon = $_GET['tlon'];
	}
	else {
		$query .= '&tlon=5.66875';
		$tlon = '5.66875';
	}

	//Fastest/shortest route
	if (isset($_GET['fast'])) {
		$query .= '&fast='.$_GET['fast'];
	}
	else if (isset($_GET['short'])) {
		if ($_GET['short'] == '1') {
			$query .= '&fast=0';
		}
	}
	else {
		$query .= '&fast=1';
	}

	//Transportation
	if (isset($_GET['v'])) {
		$query .= '&v='.$_GET['v'];
	}
	else {
		$query .= '&v=motorcar';
	}

	//Map layer
	if (isset($_GET['layer'])) {
		$layer = $_GET['layer'];
	}
	else {
		$layer = 'mapnik';
	}

	$query .= "'";
	
	// Geographic pak file selection
	if ($flon > -168 and $flon < -30) {
		// American continents (North and South)
		$pak = $yours_dir.'/america.pak';
	} else {
		// Europe, Asia, Africa and Oceania continents
		$pak = $yours_dir.'/eurasia.pak';
	}
	
	//Decide which routing definition file is going to be used
	switch ($layer) {
	case 'cn':
		$gosmore = '/normal';
		$style = 'cyclestyles.xml';
		break;
	case 'test':
		$gosmore = '/test';
	    $style = 'elemstyles.xml';
	    break;
	default:
		$gosmore = '/normal';
		$style = 'genericstyles.xml';
		break;
	}
	$dir = $yours_dir.$gosmore;
	$command = "ulimit -t ".$ulimit." && ".$query." nice ./gosmore ".$pak." ".$style;
	
	$fh = fopen($www_dir.'/commands.log', 'a+');
	if ($fh) {
		fwrite($fh, date('Y-m-d H:i:s').", ".$dir.', '.$command."\n");
		fclose($fh);
	}

	$res = chdir($dir);
	$gosmore_start = microtime(true);
	$result = exec($command, $output);
	$gosmore_end = microtime(true);

	$kml = '';
	$nodes = 0;

	if (count($output) > 1)
	{
		// meta data
		header('Content-Type: text/xml');
		
		// Loop through all the coordinates
		$flat = $flon = 360.0;
		$distance = 0;
		$coords = '';
		foreach ($output as $line)
		{
			$pos = strripos($line, ',');
			if ($pos)
			{
				$line_elements = split(",", $line);
				for ($i = 0; $i < count($line_elements); $i++)
				{
					switch ($i)
					{
					case 0:
						$lat = trim($line_elements[0], "\n\r0");
						break;
					case 1:
						$lon = trim($line_elements[1], "\n\r0");
						break;
					case 2:
						$junction = trim($line_elements[2], "\n\r0");
						break;
					case 3:
						$type = trim($line_elements[3], "\n\r0");
						break;
					case 4:
						$name = trim($line_elements[4], "\n\r0");
						break;
					}
				}
				$coords .= $lon.",".$lat."\n";
				
				// Directions
				switch ($junction)
				{
				case "J":
					$directions .= addDirection($name, getDistance($flat, $flon, $lat, $lon));
					break;
				}
				
				
				// Distance
				if ($flat < 360)
				{
					$distance += getDistance($flat, $flon, $lat, $lon);
				}
				
				// Prepare for the next route result
				$flat = $lat;
				$flon = $lon;
				$nodes++;
			}
		}
		
		// KML body
		$kml = '<?xml version="1.0" encoding="UTF-8"?>'."\n";
		$kml .= '<kml xmlns="http://earth.google.com/kml/2.2">'."\n";
	  	$kml .= '  <Document>'."\n";
	    $kml .= '    <name>KML Samples</name>'."\n";
	    $kml .= '    <open>1</open>'."\n";
	    //$kml .= '    <distance>'.$distance.'</distance>'."\n";
	    $kml .= '    <description>Routing information provided by YOURS the opensource OpenStreetMap routing website </description>'."\n";
	    $kml .= '    <Folder>'."\n";
	    $kml .= '      <name>Paths</name>'."\n";
	    $kml .= '      <visibility>0</visibility>'."\n";
	    $kml .= '      <description>Examples of paths.</description>'."\n";
	    $kml .= '      <Placemark>'."\n";
	    $kml .= '        <name>Tessellated</name>'."\n";
	    $kml .= '        <visibility>0</visibility>'."\n";
	    $kml .= '        <description><![CDATA[If the <tessellate> tag has a value of 1, the line will contour to the underlying terrain]]></description>'."\n";
	    $kml .= '        <LineString>'."\n";
	    $kml .= '          <tessellate>1</tessellate>'."\n";
	    $kml .= '          <coordinates> ';
	    $kml .= $coords;
		$kml .= '          </coordinates>'."\n";
	    $kml .= '        </LineString>'."\n";
		$kml .= '        <ExtendedData>'."\n";
		$kml .= '          <Data name="distance">'."\n";
	    $kml .= '            <displayName>Distance</displayName> '."\n";
		$kml .= '            <value>'.$distance.'</value>'."\n";
	    $kml .= '          </Data>'."\n";
	    $kml .= $directions;
		$kml .= '        </ExtendedData>'."\n";
	    $kml .= '      </Placemark>'."\n";
	    $kml .= '    </Folder>'."\n";
	    $kml .= '  </Document>'."\n";
		$kml .= '</kml>'."\n";
	}

	if ($nodes == 0) 
	{
		if (count($output) > 1)
		{
			if (strcmp($output[2], 'No route found')) {
				$kml = 'Unable to calculate a route';
			}
			else
			{
				$kml = "An unexpected error occured in Gosmore:\n".print_r($output);
			}	
		}
		else if (count($output) == 0)
		{
			$kml = "An unexpected error occured in Gosmore:\n".$result;
		}
		
	}

	echo $kml;

if ($bRunGosmore) {
	$file = $www_dir.'/requests.csv';
	if (!file_exists($file)) {
		$fh = fopen($file, 'a+');
		if ($fh) {		
			fwrite($fh, "date, query, length, nodes, script time, gosmore time\n");
			fclose($fh);
		}
	}

	$fh = fopen($file, 'a+');
	if ($fh) {
		$script_end = microtime(true);
		$script = $script_end - $script_start;
		$runtime = $gosmore_end - $gosmore_start;
		
		fwrite($fh, date('Y-m-d H:i:s').", ".$user_agent.", ".$query.", ".strlen($kml).", ".$nodes.", ".round($script, 2).", ".round($runtime, 2)."\n");
		fclose($fh);
	}
}

function addDirection($description, $length)
{
	$kml .= '          <Data name="direction">'."\n";
    $kml .= '            <displayName>'.$description.'</displayName> '."\n";
	$kml .= '            <value>'.$length.'</value>'."\n";
    $kml .= '          </Data>'."\n";
	
	return $kml;
}

function getDistance($latitudeFrom, $longitudeFrom,
    $latituteTo, $longitudeTo)
{
    // 1 degree equals 0.017453292519943 radius
    $degreeRadius = deg2rad(1);
 
    // convert longitude and latitude values to radians before calculation
    $latitudeFrom  *= $degreeRadius;
    $longitudeFrom *= $degreeRadius;
    $latituteTo    *= $degreeRadius;
    $longitudeTo   *= $degreeRadius;
 
    // apply the Great Circle Distance Formula
    $d = sin($latitudeFrom) * sin($latituteTo) + cos($latitudeFrom)
       * cos($latituteTo) * cos($longitudeFrom - $longitudeTo);
 
    return (6371.0 * acos($d));
}

function getProcesses()
{
	$nProcesses = 0;
	
	exec("ps ax | grep gosmore", $ps, $return_var);

	foreach ($ps as $row => $process)
	{
		$properties = array();
		$properties = split(" ", $process);
		
		foreach ($properties as $item => $property)
		{
			//echo "property ".$item." = ".$property."\n";
			if (strcmp(trim($property), "./gosmore") == 0)
			{
				$nProcesses++;
				break;
			}
		}
	}
	return $nProcesses;
}

