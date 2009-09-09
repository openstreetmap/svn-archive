<?php
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
$osm_xapi_base = "http://www.informationfreeway.org/api/0.6";

//Local SQLite database file used for postcodes etc
$db_file = "healthware.db";

//Version info etc.
define ("SERVICE_NAME", "Healthwhere");
define ("VERSION", "2.0");
//Base URL - do not include trailing /
define ("BASE_URL", "http://www.example.com");
?>
