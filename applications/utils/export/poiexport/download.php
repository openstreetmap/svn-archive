<?php
/**
 * Process a query on a postgis/postgresql OpenStreetMap database
 * and return a file in the requested format
 */


// Don't forget to rename config.php.example!
require	("config.php"); 

// Check to see if all the required parameters are given
if (!isset($_GET["output"]) || !isset($_GET["k"]) || !isset($_GET["v"]) || !isset($_GET["filename"])) {
    die("Error, Not all parameters supplied");
}

// Parse GET parameters
$output = pg_escape_string(utf8_encode($_GET["output"]));
$k = pg_escape_string(utf8_encode($_GET["k"]));
$v = pg_escape_string(utf8_encode($_GET["v"]));
$filename = pg_escape_string(utf8_encode($_GET["filename"]));

// Connect to the postgresql database server
$dbconn = pg_connect("host=" . $config['Database']['servername'] . " port=" . $config['Database']['port'] . " dbname=" . $config['Database']['dbname'] . " user=".$config['Database']['username'] . " password=" . $config['Database']['password']) or die('Could not connect: ' . pg_last_error());
pg_set_client_encoding("UTF-8");
// Initialize query
$query = "SELECT name, lat, lon
FROM (
  SELECT name, 
         x(transform(way, 4326)) AS lon, 
         y(transform(way, 4326)) AS lat 
  FROM planet_osm_point 
  WHERE $k='$v'
UNION
  SELECT name, 
         x(centroid(transform(way, 4326))) AS lon, 
         y(centroid(transform(way, 4326))) AS lat 
  FROM planet_osm_polygon 
  WHERE $k='$v'
) AS u1";

// Run query
$result = pg_query($query) or die('Query failed: ' . pg_last_error());

switch($output) {
    case "csv":
        asGarmin($result, $filename . ".csv");
        break;
    case "ov2":
        asOv2($result, $filename . ".ov2");
        break;
    case "gpx":
        asGpx($result, $filename . ".gpx");
        break;
    case "kml":
        asKml($result, $filename . ".kml");
        break;
    case "osm":
        asOSM($result, $filename . ".osm");
        break;
    default:
        die("No valid output specified");
}

pg_free_result($result);
pg_close($dbconn);

/**
 * Create a Garmin specific csv file
 *
 * Reference: http://www.poi-factory.com/garmin-csv-file-format (no official file description unfortunatelly)
 * 
 * @param postgresl resultset $data
 * @param string $filename
 */
function asGarmin($data, $filename) {
// Set headers
    header("Pragma: public");
    header("Expires: 0");
    header("Cache-Control: must-revalidate, post-check=0, pre-check=0");

    header("Cache-Control: public");
    header("Content-Description: File Transfer");
    header("Content-Disposition: attachment; filename=$filename");
    header("Content-Type: text/plain"); // Content-Type: text/html
    //header("Content-Transfer-Encoding: binary");

    while ($line = pg_fetch_array($data, null, PGSQL_ASSOC)) {
        echo $line["lon"] . ", " .
            $line["lat"] . ", \"" .
            $line["name"] . "\"\n";
    }
}

/**
 * Create a tomtom specific ov2 file
 *
 * Reference: http://www.tomtom.com/lib/doc/ttnavsdk3_manual.pdf
 * 
 * @param postgresql resultset $data
 * @param string $filename
 */
function asOv2($data, $filename) {
// Set headers
    header("Pragma: public");
    header("Expires: 0");
    header("Cache-Control: must-revalidate, post-check=0, pre-check=0");
    header("Cache-Control: public");

    header("Content-Description: File Transfer");
    header("Content-Disposition: attachment; filename=$filename");
    header("Content-Type: application/octet-stream"); // Content-Type: text/html
    header("Content-Transfer-Encoding: binary");

    while ($line = pg_fetch_array($data, null, PGSQL_ASSOC)) {
        echo chr(0x02) .
            pack("V",strlen($line["name"]) + 14) .
            pack("V",round($line["lon"] * 100000)) .
            pack("V",round($line["lat"] * 100000)) .
            utf8_decode($line["name"]) .
            chr(0x00);
    }
}

/**
 * Create a generic GPX file
 *
 * Reference: http://www.topografix.com/gpx/1/1/
 * @param postgresql resultset $data
 * @param string $filename
 */
function asGpx($data, $filename) {
// Set headers
    header("Pragma: public");
    header("Expires: 0");
    header("Cache-Control: must-revalidate, post-check=0, pre-check=0");
    header("Cache-Control: public");

    header("Content-Description: File Transfer");
    header("Content-Disposition: attachment; filename=$filename");
    header("Content-Type: application/force-download"); // Content-Type: text/html
    //header("Content-Transfer-Encoding: binary");

    echo "<?xml version='1.0' encoding='UTF-8'?>\n" .
        "<gpx version=\"1.1\" creator=\"OSM-NL POI export\" xmlns=\"http://www.topografix.com/GPX/1/1\"\n" .
        "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" .
        "  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n" .
        "<metadata></metadata>\n";

    while ($line = pg_fetch_array($data, null, PGSQL_ASSOC)) {
        $name = htmlspecialchars($line["name"]);
        echo "<wpt lat=\"" . $line["lat"] . "\" lon=\"" . $line["lon"] . "\">\n" .
            "  <name>" . $name . "</name>\n" .
            "</wpt>\n";
    }
    echo "</gpx>";
}

/**
 * Create a Google Earth/Maps .kml file (2D)
 *
 * TODO: Add 3D option for Google Earth
 * Reference: http://code.google.com/intl/nl/apis/kml/documentation/kmlreference.html
 *
 * @param postgresql resultset $data
 * @param string $filename
 */
function asKml($data, $filename) {
// Set headers
    header("Pragma: public");
    header("Expires: 0");
    header("Cache-Control: must-revalidate, post-check=0, pre-check=0");
    header("Cache-Control: public");

    header("Content-Description: File Transfer");
    header("Content-Disposition: attachment; filename=$filename");
    header("Content-Type: application/vnd.google-earth.kml+xml"); // Content-Type: text/html

    echo "<?xml version='1.0' encoding='UTF-8'?>\n<kml xmlns='http://earth.google.com/kml/2.2'>\n";
    echo "<Document>\n";
    while ($line = pg_fetch_array($data, null, PGSQL_ASSOC)) {
       $name = htmlspecialchars($line["name"]);
        echo "<Placemark>\n\t<name>" . $name . "</name>\n" .
            "\t<Point>\n\t\t<coordinates>" . $line["lon"] . ", " . $line["lat"] . "</coordinates>\n\t</Point>\n</Placemark>\n";
    }
    echo "</Document>\n";
    echo "</kml>";
}

/**
 * Create a .osm file
 *
 * TODO: Needs fixing
 * Reference: http://wiki.openstreetmap.org/wiki/.osm
 *
 * @param postgresql resultset $data
 * @param string $filename
 */
function asOSM($data, $filename) {
// Set headers
    header("Pragma: public");
    header("Expires: 0");
    header("Cache-Control: must-revalidate, post-check=0, pre-check=0");
    header("Cache-Control: public");

    header("Content-Description: File Transfer");
    header("Content-Disposition: attachment; filename=$filename");
    header("Content-Type: text/xml"); // Content-Type: text/html

    echo '<?xml version="1.0" encoding="UTF-8"?>';
    echo "\n";
    echo '<osm version="0.6" generator="POI export">';
    echo "\n";
    echo '<bounds minlat="-180" minlon="-90" maxlat="180" maxlon="90"/>';
    echo "\n";

    while ($line = pg_fetch_array($data, null, PGSQL_ASSOC)) {
        //$name = htmlentities($line["name"], ENT_QUOTES);
        $name = htmlspecialchars($line["name"]);
        echo '<node id="-1" lat="' . $line["lat"] . '" lon="' .  $line["lon"] . '" version="1" changeset="-1" user="poiexport" uid="-1" visible="true" timestamp="2008-12-17T01:18:42Z">';
        echo "\n";
        echo '<tag k="name" v="' . $name . '"/>';
        echo "\n";
        echo '</node>';
        echo "\n";
    }
    echo "</osm>";
}
?>
