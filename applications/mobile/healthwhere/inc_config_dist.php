<?php
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

//Rename or copy this file to "inc_config.php" then edit to suit

//Log files
$access_log = "/dev/null";
$debug_log = "/dev/null";
$error_log = "/dev/null";

//Default maximum distance
define ("DEFAULT_MAX_DIST", 5);
//Maximum distance that can be requested
define ("MAX_DIST", 100);

//OSM authentication
$osm_user = "";
$osm_password = "";

//OSM API & XAPI base URLs - not including trailing /
//See http://wiki.openstreetmap.org/wiki/Protocol
$osm_api_base = "http://api.openstreetmap.org/api/0.6";
$osm_xapi_base = "http://osmxapi.hypercube.telascience.org/api/0.6";
//$osm_xapi_base = "http://www.informationfreeway.org/api/0.6";
//$osm_xapi_base = "http://xapi.openstreetmap.org/api/0.6";

//Local SQLite database file used for postcodes etc
$db_file = "healthware.db";

//Base URL - do not include trailing /
define ("BASE_URL", "http://www.example.com");
?>
