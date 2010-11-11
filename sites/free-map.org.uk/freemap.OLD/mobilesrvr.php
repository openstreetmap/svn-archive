<?php
require_once('/home/www-data/private/defines.php');
require_once('../lib/functionsnew.php');
require_once('freemap_functions.php');
require_once('latlong.php');

header("Content-type: text/xml");

$conn=pg_connect("dbname=freemap");
$cleaned = clean_input($_REQUEST);

$merc = ll_to_merc($cleaned['lat'],$cleaned['lon']);


$q= ("SELECT osm_id, name,amenity,man_made,\"natural\",tourism,place,".
			"AsText(way) as p,description FROM ".
            "planet_osm_point WHERE (amenity='pub' or \"natural\" = 'peak' ".
			"or place='village' or place='town' or place='hamlet') AND ".
              "Distance(GeomFromText('POINT($merc[e] $merc[n])',4326),way)".
                "<8000");

$result=pg_query($q);

echo "<items>\n";
echo "<pois>\n";
while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
{
	$foundnode=true;
	// Only output known types-these are defined in get_high_level()
	$highlevel = get_high_level($row);
	if($highlevel!="unknown")
	{
		echo "<item>\n";
		echo "<type>$highlevel</type>\n";
		if($row['name']!="")
			echo "<name>$row[name]</name>\n";

		$result2=pg_query
				("SELECT * FROM node_descriptions WHERE osm_id=".
					"$row[osm_id]");
		if($row2=pg_fetch_array($result2,null,PGSQL_ASSOC))
		{
			if($row2['description']!="")
           				echo "<description>$row2[description]".
						"</description>\n";
		}
		$m = array();
		$a = preg_match ("/POINT\((.+)\)/",$row['p'],$m);
		list($x,$y)= explode(" ",$m[1]);
		$latlon = merc_to_ll($x,$y);
		echo "<lat>$latlon[lat]</lat>\n<lon>$latlon[lon]</lon>\n";
		echo "<id>$row[osm_id]</id>\n";
		echo "</item>\n";
	}
}
echo "</pois>\n";
pg_free_result($result);

// won't give exact same bounds as for OSM POIs but doesn't really matter
// (one's a circle, one's a square)
$w = $merc['e'] - 3536;
$e = $merc['e'] + 3536;
$s = $merc['n'] - 3536;
$n = $merc['n'] + 3536;

$bl = merc_to_ll($w,$s);
$tr = merc_to_ll($e,$n);

$bbox = "$bl[lon],$bl[lat],$tr[lon],$tr[lat]";
$markers = get_markers_by_bbox($bbox);

echo "<annotations>\n";

foreach ($markers as $marker)
{
	echo "<item>\n";
	echo "<type>$marker[type]</type>\n";
	if($marker['name']!="")
		echo "<name>$marker[name]</name>\n";
	if($marker['description']!="")
		echo "<description>$marker[description]</description>\n";
	echo "<lat>$marker[lat]</lat>\n";
	echo "<lon>$marker[lon]</lon>\n";
	echo "<id>$marker[id]</id>\n";
	echo "</item>\n";
}
echo "</annotations>\n";
echo "</items>\n";
		
pg_close($conn);

?>
