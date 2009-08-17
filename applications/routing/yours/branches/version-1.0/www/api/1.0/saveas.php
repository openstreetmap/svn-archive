<?
if (isset($_GET['type'])) {
	$methode="get";
	$type = $_GET['type'];
} else if (isset($_POST['type'])) {
	$method="post";
	$type = $_POST['type'];
}

if (isset($_GET['data'])) {
	$data = $_GET['data'];
} else if (isset($_POST['data'])) {
	$data = $_POST['data'];
}

if (isset($type)) {
	switch ($type) {
	case 'gpx':
		GPX($data);
		break;
	case 'wpt':
		break;
	}
}

function GPX($data) {
	header('Content-Type: text/text');
	header('Content-Disposition: attachment; filename="route.gpx"');
	
	$xml  = '<?xml version="1.0" encoding="UTF-8" standalone="no" ?>'."\n";
	$xml .= '<gpx xmlns="http://www.topografix.com/GPX/1/1" creator="OpenStreetMap routing service" version="1.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">'."\n";
    $xml .= "\n";
    $xml .= '  <metadata>'."\n";
    $xml .= '    <link href="http://tile.openstreetmap.nl/~lambertus/routing/index.html">'."\n";
    $xml .= '      <text>OpenStreetMap routing service</text>'."\n";
    $xml .= '    </link>'."\n";
    $xml .= '    <time>'.date('c').'</time>'."\n";
  	$xml .= '  </metadata>'."\n";
  	$xml .= "\n";
	$xml .= '  <trk>'."\n";
    $xml .= '    <name>Route</name>'."\n";
    $xml .= '    <trkseg>'."\n";
    
	$route = split(",", trim($data));
    
	foreach ($route as $pair) {
	    	//echo $data.' '.count($route);
	    	$lonlat = split(' ', trim($pair));
	    	//echo $pair.' '.count($lonlat);
	    	if (count($lonlat) > 0) {    	
    			$xml .= '      <trkpt lon="'.$lonlat[0].'" lat="'.$lonlat[1].'"></trkpt>'."\n";
   		}
	}
	$xml .= '    </trkseg>'."\n";
	$xml .= '  </trk>'."\n";
	$xml .= '</gpx>'."\n"; 
	
	echo $xml;
}

?>
