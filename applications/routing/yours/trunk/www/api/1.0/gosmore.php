<?php
$maxAttempts = 5;
$maxInstances = 6;
$www_dir = '/home/pnorman/osm/routing/www';
$yours_dir = '/home/pnorman/osm/routing/gosmore';
$ulimit = 30;

$output = "";
$nAttempts = 0;
$script_start = microtime(true);

// Allowed parameters
$vehicles = array("motorcar", "bicycle", "foot");
$layers = array("mapnik", "cn", "test");
$formats = array("kml", "geojson");

$blocked = array('77.215.30.153');

$ip = $_SERVER['REMOTE_ADDR'];
$user_agent = $_SERVER['HTTP_USER_AGENT'];
$yours_client = $_SERVER['HTTP_X_YOURS_CLIENT'];
$referrer = $_SERVER['HTTP_REFERER'];

// Check for blocked API users
if (in_array($ip, $blocked) == true) {
	$file = $www_dir.'/blocked.csv';
	$fh = fopen($file, 'a+');
	if ($fh) {
		fwrite($fh, date('Y-m-d H:i:s').", ".$ip.", ".$yours_client.", ".$referrer.", ".$user_agent.", ".$query."\n");
		fclose($fh);
	}
	exit("Your IP is blocked. Please send an email to osm@na1400.info describing why you are constantly sending automated requests to the API.");
}

// Check for running Gosmore instances, max wait is 1 sec
for ($nAttempts = 0; $nAttempts < $maxAttempts; $nAttempts++) {
	if (getProcesses() > $maxInstances) {
		// sleep for 200 msec
		usleep(200000);
	} else {
		break;
	}
} 

if ($nAttempts == $maxAttempts) {
	$file = $www_dir.'/busy.csv';
	$fh = fopen($file, 'a+');
	if ($fh) {		
		fwrite($fh, date('Y-m-d H:i:s').", ".$ip.", ".$yours_client.", ".$referrer.", ".$user_agent.", ".$query."\n");
		fclose($fh);
	}
	$output = "Server is busy, please try again later (".getProcesses().")";
	exit($output);
}

$query = "QUERY_STRING='";


//Coordinates
if (isset($_GET['flat']) && is_numeric($_GET['flat'])) {
	$query .= 'flat='.$_GET['flat'];
	$flat = $_GET['flat'];
}
else {	
	$query .= 'flat=53.04821';
	$flat = 53.04821;
}

if (isset($_GET['flon']) && is_numeric($_GET['flon'])) {
	$query .= '&flon='.$_GET['flon'];
	$flon = $_GET['flon'];
}
else {
	$query .= '&flon=5.65922';
	$flon = '5.65922';
}

if (isset($_GET['tlat']) && is_numeric($_GET['tlat'])) {
	$query .= '&tlat='.$_GET['tlat'];
}
else {
	$query .= '&tlat=53.02616';
}

if (isset($_GET['tlon']) && is_numeric($_GET['tlon'])) {
	$query .= '&tlon='.$_GET['tlon'];
	$tlon = $_GET['tlon'];
}
else {
	$query .= '&tlon=5.66875';
	$tlon = '5.66875';
}

//Fastest/shortest route
if (isset($_GET['fast']) && is_numeric($_GET['fast'])) {
	$query .= '&fast='.$_GET['fast'];
}
else if (isset($_GET['short']) && is_numeric($_GET['short'])) {
	if ($_GET['short'] == '1') {
		$query .= '&fast=0';
	}
}
else {
	$query .= '&fast=1';
}

//Transportation
if (isset($_GET['v']) && in_array($_GET['v'], $vehicles)) {
	$query .= '&v='.$_GET['v'];
}
else 
{
	$query .= '&v=motorcar';
}

//Map layer
$layer = 'mapnik';
if (isset($_GET['layer']) && in_array($_GET['layer'], $layers)) {
	$layer = $_GET['layer'];
}

// Query result return format
$format = 'kml';
if (isset($_GET['format']) && in_array($_GET['format'], $formats)) {
	$format = $_GET['format'];
}

$query .= "'";
//$dir = $yours_dir.'/normal';

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
$command = $query." nice ".$yours_dir."/gosmore ".$pak." ".$style;

$fh = fopen($www_dir.'/commands.log', 'a+');
if ($fh) {
	fwrite($fh, date('Y-m-d H:i:s').", ".$dir.', '.$command."\n");
	fclose($fh);
}

$res = chdir($dir);
$gosmore_start = microtime(true);
exec("ulimit -t ".$ulimit);
$result = exec("ulimit -t 30 && ".$command, $output);
exec("ulimit");
$gosmore_end = microtime(true);

$nodes = 0;

if (count($output) > 1)
{	// Loop through all the coordinates
	$flat = $flon = 360.0;
	$distance = 0;
	$elements = array();
	foreach ($output as $line)
	{
		$pos = strripos($line, ',');
		if ($pos)
		{
			$node = split(",", $line);
			for ($i = 0; $i < count($node); $i++)
			{
				switch ($i)
				{
				case 0:
					$lat = trim($node[0], "\n\r0");
					break;
				case 1:
					$lon = trim($node[1], "\n\r0");
					break;
				case 2:
					$junction = trim($node[2], "\n\r0");
					break;
				case 3:
					$name = trim($node[3], "\n\r0");
					break;
				}
			}
			$element = array("lat" => $lat, "lon" => $lon, "junction" => $junction, "name" => $name);
			array_push($elements, $element);
			
			if ($flat < 360)
			{
				$distance += getDistance($flat, $flon, $lat, $lon);
			}
			$flat = $lat;
			$flon = $lon;
			$nodes++;
		}
	}
	
	// Convert the returned coordinates to the requested output format
	switch ($format) {
	case 'kml':
		$output = asKML($elements, $distance);
		break;
	case 'geojson':
		$output = asGeoJSON($elements, $distance);
		break;
	default:
		$output = "unrecognised output format given";
	}
}

if ($nodes == 0) 
{
	if (count($output) > 1)
	{
		if (strcmp($output[2], 'No route found')) {
			$output = 'Unable to calculate a route';
		}
		else
		{
			$output = "An unexpected error occured in Gosmore:\n".print_r($output);
		}	
	}
	else if (count($output) == 0)
	{
		$output = "An unexpected error occured in Gosmore:\n".$result;
	}
	
}

// Return the result
echo $output;

// Do some housekeeping (update logfiles)

$file = $www_dir.'/requests.csv';
if (!file_exists($file)) {
	$fh = fopen($file, 'a+');
	if ($fh) {		
		fwrite($fh, "date, ip, client id, referrer, user_agent, query, length, nodes, script time, gosmore time\n");
		fclose($fh);
	}
}

$fh = fopen($file, 'a+');
if ($fh) {
	$script_end = microtime(true);
	$script = $script_end - $script_start;
	$runtime = $gosmore_end - $gosmore_start;
	
	fwrite($fh, date('Y-m-d H:i:s').", ".$ip.", ".$yours_client.", ".$referrer.", ".$user_agent.", ".$query.", ".strlen($output).", ".$nodes.", ".round($script, 2).", ".round($runtime, 2)."\n");
	fclose($fh);
}
// Done!

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
	return exec('ps ax | grep "nice '.$yours_dir.'/[g]osmore" | wc -l');
}

function asKML($elements, $distance) {
	// meta data
	header('Content-Type: text/xml');

	// KML body
	$kml = '<?xml version="1.0" encoding="UTF-8"?>'."\n";
	$kml .= '<kml xmlns="http://earth.google.com/kml/2.0">'."\n";
	$kml .= '  <Document>'."\n";	$kml .= '    <name>KML Samples</name>'."\n";
	$kml .= '    <open>1</open>'."\n";
	$kml .= '    <distance>'.$distance.'</distance>'."\n";
	$kml .= '    <description>Unleash your creativity with the help of these examples!</description>'."\n";
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
	foreach($elements as $element) {
		$kml .= $element["lon"].",".$element["lat"]."\n";
	}
	$kml .= '          </coordinates>'."\n";
	$kml .= '        </LineString>'."\n";
	$kml .= '      </Placemark>'."\n";
	$kml .= '    </Folder>'."\n";
	$kml .= '  </Document>'."\n";
	$kml .= '</kml>'."\n";
	return $kml;
}
function asGeoJSON($elements, $distance) {
	// meta data
	header('Content-Type: application/json');

	$geoJSON = "{\n";
	$geoJSON .= "  \"type\": \"LineString\",\n";
	$geoJSON .= "  \"crs\": {\n";
	$geoJSON .= "    \"type\": \"name\",\n";
	$geoJSON .= "    \"properties\": {\n";
	$geoJSON .= "      \"name\": \"urn:ogc:def:crs:OGC:1.3:CRS84\"\n";
	$geoJSON .= "    }\n";
	$geoJSON .= "  },\n";
	$geoJSON .= "  \"coordinates\":\n";
	$geoJSON .= "  [\n";
	foreach($elements as $element) {
		$geoJSON .= "    [".$element["lon"].", ".$element["lat"]."],\n";
	}
	$geoJSON .= "  ],";
	$geoJSON .= "  \"properties\": {\n";
	$geoJSON .= "    \"distance\": \"".$distance."\",\n";
	$geoJSON .= "    \"description\": \"GeoJSON route result created by http://www.yournavigation.org\"\n";
	$geoJSON .= "    }\n";
	$geoJSON .= "}\n";
	return $geoJSON;
}

