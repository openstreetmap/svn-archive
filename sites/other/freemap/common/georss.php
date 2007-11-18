<?php
header("Content-type: text/xml");
// GeoRSS was worked out from
// - http://worldkit.org/doc/rss.php
// - http://georss.org/simple.html

session_start();

require_once("freemap_functions.php");
require_once('defines.php');


$bbox = isset($_GET['bbox']) ? $_GET['bbox'] : 
	(isset ($_GET['BBOX']) ? $_GET['BBOX']: "-7,49,2,59");

if($bbox)
{
	list($bllon,$bllat,$trlon,$trlat) = explode(",",$bbox);
	$conn = mysql_connect("localhost",DB_USERNAME,DB_PASSWORD);
	mysql_select_db(DB_DBASE);

	$q = "select * from freemap_markers where lat between $bllat and $trlat ".
		 "and lon between $bllon and $trlon";

	echo "<?xml version='1.0'?>\n";
	echo "<rss version='2.0' xmlns:georss='http://www.georss.org/georss'>\n";
	echo "<channel>\n";
	$result = mysql_query($q);

	$id=0;
	if(isset($_SESSION['gatekeeper']))
		$id = get_user_id($_SESSION['gatekeeper'],"freemap_users");
	
	while($row = mysql_fetch_array($result))
	{
		if($row['private']==0 || $row['userid']==$id)
		{
			echo "<item>\n";
			$t = ($row['title']) ? $row['title']: " ";
			echo "<title>$t</title>\n";
			if($row['link'])
				echo "<link>$row[link]</link>\n";
			$description=($row['description']!=null 
							&& $row['description']!="")
							? $row['description'] : 'no description';
			echo "<description>$description</description>\n";
			//echo "<description>description</description>\n";
			// according to the rss 2.0 spec the guid is a unique string
			// identifier for each item. so this is acceptable. it means
			// that slippy clients can easily tell whether an item has 
			// already been added as a marker.
			echo "<guid>$row[id]</guid>\n";
			echo "<georss:point>$row[lat] $row[lon]</georss:point>\n";
			echo "<georss:featuretypetag>$row[type]</georss:featuretypetag>\n";
			echo "</item>\n";
		}
	}
	echo "</channel>\n";
	echo "</rss>\n";
}
?>
