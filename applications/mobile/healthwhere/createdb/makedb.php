#!/usr/bin/env php
<?
require_once ("phpcoord-2.3.php");

// Open files
$db = sqlite_open ("healthware.db");
$csv = fopen("allpostcodes.csv", "r");

// Set up database
$sql = "CREATE TABLE postcodes ('outward','inward','lat','lon','source');\n";
sqlite_exec ($db, $sql);

// Loop through CSV
while (($data = fgetcsv ($csv)) !== FALSE) {
	$pc = $data [0];
	$in = substr ($pc, -3);
	$out = trim (substr ($pc, 0, 4));

	//Get lat/lon
	$os = new OSRef ($data [10], $data [11]);
	$ll = $os->toLatLng ();
	$ll->OSGB36ToWGS84 ();

	$lat = $ll->lat;
	$lon = $ll->lng;

	$sql = "INSERT INTO postcodes ('outward','inward','lat','lon','source') " .
		"VALUES ('$out', '$in', $lat, $lon, 'OS OpenData CodePoint Open');\n";
	sqlite_exec ($db, $sql);
}

// Clean up
fclose($csv);
sqlite_close ($db);
