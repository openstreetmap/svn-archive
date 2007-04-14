<?php
// GeoRSS was worked out from
// - http://worldkit.org/doc/rss.php
// - http://georss.org/simple.html
// the xml written out by this is "like" georss. However georss itself is
// not being used as it doesn't seem to be geared for tiled retrieval, which
// is what we want.

session_start();

require_once("ajaxfunctions.php");
require_once('defines.php');

header("Content-type: text/xml");

$bbox = isset($_GET['bbox']) ? $_GET['bbox'] : $_GET['BBOX'];

if($bbox)
{
	list($bllon,$bllat,$trlon,$trlat) = explode(",",$bbox);
	$conn = mysql_connect("localhost",DB_USERNAME,DB_PASSWORD);
	mysql_select_db(DB_DBASE);

	$q = "select * from freemap_markers where lat between $bllat and $trlat ".
		 "and lon between $bllon and $trlon";

	echo "<features>\n";
	$result = mysql_query($q);

	$id=0;
	if(isset($_SESSION['gatekeeper']))
		$id = get_user_id($_SESSION['gatekeeper']);
	
	while($row = mysql_fetch_array($result))
	{
		if($row['private']==0 || $row['userid']==$id)
		{
			echo "<feature>\n";
			echo "<id>$row[id]</id>\n";
			$t = ($row['title']) ? $row['title']: " ";
			echo "<title>$t</title>\n";
			if($row['link'])
				echo "<link>$row[link]</link>\n";
			echo "<description>$row[description]</description>\n";
			echo "<lat>$row[lat]</lat>\n";
			echo "<lon>$row[lon]</lon>\n";
			echo "<type>$row[type]</type>\n";
			echo "</feature>\n";
		}
	}
	echo "</features>\n";
}
?>
