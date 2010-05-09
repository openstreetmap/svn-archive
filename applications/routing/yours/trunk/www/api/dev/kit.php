<?php
/* Copyright (c) 2010, L. IJsselstein and others
  Yournavigation.org All rights reserved.
 */

/* Interfaces with a routing engine developed by 
 * KIT (Karlsruhe Institute of Technology)
 *
 * This engine uses the contraction hierarchies algorithm
 * to achieve very fast route results.
 */
 
$maxAttempts = 25; //5
$maxInstances = 4;
$www_dir = '/home/lambertus/public_html/yours';
$yours_dir = '/home/lambertus/yours';
$admin_email = '[admin email address here]';
$action = "http://[KIT server address here]/route";
$ulimit = 30;

$output = "";
$nAttempts = 0;
$script_start = microtime(true);

// Allowed parameters
$vehicles = array("motorcar");
$layers = array("mapnik");
$formats = array("kml", "geojson", "gpx");

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
	exit("Your IP is blocked. Please send an email to $admin_email describing why you are constantly sending automated requests to the API.");
}

$query = "";

//Coordinates
if (isset($_GET['flat']) && is_numeric($_GET['flat'])) {
	$query .= "&".$_GET['flat'];
	$flat = $_GET['flat'];
}
else {	
	$query .= 'flat=53.04821';
	$flat = 53.04821;
}

if (isset($_GET['flon']) && is_numeric($_GET['flon'])) {
	$query .= "&".$_GET['flon'];
	$flon = $_GET['flon'];
}
else {
	$query .= '&flon=5.65922';
	$flon = '5.65922';
}

if (isset($_GET['tlat']) && is_numeric($_GET['tlat'])) {
	$query .= "&".$_GET['tlat'];
}
else {
	$query .= '&tlat=53.02616';
}

if (isset($_GET['tlon']) && is_numeric($_GET['tlon'])) {
	$query .= "&".$_GET['tlon'];
	$tlon = $_GET['tlon'];
}
else {
	$query .= '&tlon=5.66875';
	$tlon = '5.66875';
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

$fh = fopen($www_dir.'/commands.log', 'a+');
if ($fh) {
	fwrite($fh, date('Y-m-d H:i:s').", ".$dir.', '.$action.$query."\n");
	fclose($fh);
}
$kit_start = microtime(true);

// Initiate cURL
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $action.$query);

// Follow redirects and return the transfer
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);

// Get result and close cURL
$result = curl_exec($ch);

$curl_info = curl_getinfo($ch);

curl_close($ch);

$kit_end = microtime(true);

// Return the response
header("Content-type: ".$curl_info['content_type']);
echo $result;

// Do some housekeeping (update logfiles)

$file = $www_dir.'/requests.csv';
if (!file_exists($file)) {
	$fh = fopen($file, 'a+');
	if ($fh) {		
		fwrite($fh, "date, ip, client id, referrer, user_agent, query, length, nodes, script time, kit time\n");
		fclose($fh);
	}
}

$fh = fopen($file, 'a+');
if ($fh) {
	$script_end = microtime(true);
	$script = $script_end - $script_start;
	$runtime = $kit_end - $kit_start;
	
	fwrite($fh, date('Y-m-d H:i:s').", ".$ip.", ".$yours_client.", ".$referrer.", ".$user_agent.", ".$query.", ".strlen($output).", ".$nodes.", ".round($script, 2).", ".round($runtime, 2)."\n");
	fclose($fh);
}
// Done!

function getDistance($latitudeFrom, $longitudeFrom, $latituteTo, $longitudeTo) {
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

function kmlError($error) {
	// meta data
	header('Content-Type: text/xml');
	
	$kml = '<?xml version="1.0" encoding="UTF-8"?>'."\n";
	$kml = '<?xml version="1.0" encoding="UTF-8"?>'."\n";
	$kml .= "<error>\n";
	$kml .= "  <description>$error</description>\n";
	$kml .= "</error>\n";
	
	return $kml;
}

function asKML($elements, $distance) {
	// meta data
	header('Content-Type: text/xml');

	// KML body
	$kml = '<?xml version="1.0" encoding="UTF-8"?>'."\n";
	$kml .= '<kml xmlns="http://earth.google.com/kml/2.0">'."\n";
	$kml .= '  <Document>'."\n";
	$kml .= '    <name>KML Samples</name>'."\n";
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
	
	$nElements = count($elements);
	$nCount = 1;
	foreach($elements as $element) {
		if ($nCount < $nElements) {
			$geoJSON .= "    [".$element["lon"].", ".$element["lat"]."],\n";
		} else {
			$geoJSON .= "    [".$element["lon"].", ".$element["lat"]."]\n";
		}
		$nCount++;
	}
	$geoJSON .= "  ],";
	$geoJSON .= "  \"properties\": {\n";
	$geoJSON .= "    \"distance\": ".$distance.",\n";
	$geoJSON .= "    \"description\": \"GeoJSON route result created by http://www.yournavigation.org\"\n";
	$geoJSON .= "    }\n";
	$geoJSON .= "}\n";
	return $geoJSON;
}

function asGPX($elements, $distance) {
	// meta data
	header('Content-Type: text/xml');

	// GPX body     
	$kml = '<?xml version="1.0" encoding="UTF-8"?>'."\n";
	$kml .= '<gpx version="1.0" creator="http://www.yournavigation.org/" xmlns="http://www.topografix.com/GPX/1/0">'."\n";
	$kml .= '  <trk>'."\n";
	$kml .= '    <trkseg>'."\n";	
	foreach($elements as $element) {
		$kml .= '        <trkpt lon="'.$element["lon"].'" lat="'.$element["lat"].'" junction="'.$element["junction"].'"/>'."\n";
	}
	$kml .= '    </trkseg>'."\n";
	$kml .= '  </trk>'."\n";
	$kml .= '</gpx>'."\n";
	return $kml;
}
