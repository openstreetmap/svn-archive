<?php
include('../lib/functionsnew.php');

session_start();


$conn=dbconnect("otv");
$cleaned = clean_input($_GET);
$cleaned['auth'] = (isset($cleaned['auth']))?$cleaned['auth']:1;
$admin = ($cleaned['auth']==0);
if($admin && !isset($_SESSION['admin']))
	die("Not an admin");
header("Content-type: application/atom+xml");
$title = $admin ?  "Unmoderated OTV Panoramas" : "Moderated OTV Panoramas";
to_georss(get_markers_by_bbox($cleaned['bbox'],$cleaned['auth']),$title,$admin);
mysql_close($conn);


function get_markers_by_bbox($bbox,$auth)
{
    list($bllon,$bllat,$trlon,$trlat) = explode(",",$bbox);

    $q = "select * from panoramas where ";

	if($auth==1)
	{
		$q .= "(authorised=1 ";
		if(isset($_SESSION["gatekeeper"]))
			$q .= " or user=$_SESSION[gatekeeper]";
		$q .= ") ";
	}
	else
	{
		$q .= "authorised=0 ";
	}

    if(isset($bbox))    
    {    
        $q .= " and lat between $bllat and $trlat and lon between ".
            "$bllon and $trlon";
    }

    $markers=array();


    $result = mysql_query($q) or die(mysql_error()); 
    while($row = mysql_fetch_array($result))
    {
        $markers[]=$row;
    }
    return $markers;
}

function to_georss($markers,$title,$admin)
{
    //echo "<rss version='2.0' ".
    echo "<feed xmlns='http://www.w3.org/2005/Atom' ".
    "xmlns:georss='http://www.georss.org/georss'".
    " xmlns:geo='http://www.w3.org/2003/01/geo/wgs84_pos#'>\n";
//    echo "<channel>\n";

    echo "<title>$title</title>\n";
    foreach ($markers as $marker)
    {
            $url=($admin)?
                "/otv/panorama/$marker[ID]/moderate":
                "http://www.free-map.org.uk/otv/panorama/$marker[ID]";
                
            /*
            echo "<item>\n";
            $t = "Panorama $marker[ID]"; 
            echo "<title>$t</title>\n";
            if(file_exists("/home/www-data/uploads/otv/$marker[ID].jpg"))
            {
                echo "<link>http://www.free-map.org.uk/otv/pan.php?".
                    "id=$marker[ID]</link>\n";
            }
            $description="none";
            echo "<description>$marker[direction]</description>\n";
            // according to the rss 2.0 spec the guid is a unique string
            // identifier for each item. so this is acceptable. it means
            // that slippy clients can easily tell whether an item has 
            // already been added as a marker.
            echo "<guid>$marker[ID]</guid>\n";
            echo "<georss:point>$marker[lat] $marker[lon]</georss:point>\n";
            echo "<geo:dir>$marker[direction]</geo:dir>\n";
            echo "<georss:featuretypetag>panorama</georss:featuretypetag>".
                "\n";
            echo "</item>\n";
            */
            echo "<entry>\n";
            $t = "Panorama $marker[ID]"; 
            echo "<title>$t</title>\n";
            echo "<link href='$url' />\n";
            $description="none";
            echo "<summary>$marker[direction]</summary>\n";
            // according to the rss 2.0 spec the guid is a unique string
            // identifier for each item. so this is acceptable. it means
            // that slippy clients can easily tell whether an item has 
            // already been added as a marker.
            echo "<id>$marker[ID]</id>\n";
            echo "<georss:point>$marker[lat] $marker[lon]</georss:point>\n";
            echo "<geo:dir>$marker[direction]</geo:dir>\n";
            echo "<georss:featuretypetag>panorama</georss:featuretypetag>".
                "\n";
            echo "</entry>\n";
    }
//    echo "</channel>\n";
//    echo "</rss>\n";
    echo "</feed>\n";
}

?>
