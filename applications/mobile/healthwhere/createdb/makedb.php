#!/usr/bin/env php
<?
/*
Healthwhere, a web service to find local pharmacies and hospitals
Copyright (C) 2009-2010 Russell Phillips (russ@phillipsuk.org)

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

require_once ("phpcoord-2.3.php");

// Open files
$db = sqlite_open ("healthware.db");
$csv = fopen("allpostcodes.csv", "r");

// Set up database
$sql = "CREATE TABLE postcodes ('outward','inward','lat','lon');\n";
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

	$sql = "INSERT INTO postcodes ('outward','inward','lat','lon') " .
		"VALUES ('$out', '$in', $lat, $lon);\n";
	sqlite_exec ($db, $sql);
}

// Clean up
fclose($csv);
sqlite_close ($db);
