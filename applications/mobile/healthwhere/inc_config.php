<?php
//Rename or copy this file to "inc_config.php" then edit to suit

//Log files
$access_log = "/home/russ/vhosts/mappage.org/hw/logs/access.csv";
$debug_log = "/home/russ/vhosts/mappage.org/hw/logs/debug.log";
$error_log = "/home/russ/vhosts/mappage.org/hw/logs/error.log";

//Default maximum distance
define ("DEFAULT_MAX_DIST", 5);
//Maximum distance that can be requested
define ("MAX_DIST", 100);

//OSM authentication
$osm_user = "healthware@mappage.org";
$osm_password = "hhq)O0-U_";

//OSM API & XAPI base URLs - not including trailing /
//See http://wiki.openstreetmap.org/wiki/Protocol
$osm_api_base = "http://api.openstreetmap.org/api/0.6";
$osm_xapi_base = "http://www.informationfreeway.org/api/0.6";

//Local SQLite database file used for postcodes etc
$db_file = "healthware.db";

//Version info etc.
define ("SERVICE_NAME", "Healthware");
define ("VERSION", "1.0");
//Base URL - do not include trailing /
define ("BASE_URL", "http://www.mappage.org/hw");
?>
