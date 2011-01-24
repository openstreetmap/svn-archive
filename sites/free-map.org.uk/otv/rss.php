<?php
include('../lib/functionsnew.php');
require_once('../common/defines.php');

session_start();


$conn=pg_connect("dbname=gis user=gis");
$cleaned = clean_input($_GET,'pgsql');
$cleaned['auth'] = (isset($cleaned['auth']))?$cleaned['auth']:1;
$admin = ($cleaned['auth']==0);
header("Content-type: application/atom+xml");
//header("Content-type: text/xml");
$title = $admin ?  "Unmoderated OTV Panoramas" : "Moderated OTV Panoramas";
to_georss(get_anns_by_bbox($cleaned['bbox'],$cleaned['auth']),$title,$admin);
pg_close($conn);


function get_anns_by_bbox($bbox,$auth)
{
    list($w,$s,$e,$n) = explode(",",$bbox);

    $q = "select pan.*,AsText(pan.xy) from panoramas pan where  ";
	
	if($auth==1)
	{
		$q .= "(pan.authorised=1 ";
		if(isset($_SESSION["gatekeeper"]))
			$q .= " or pan.userid=$_SESSION[gatekeeper]";
		$q .= ") and ";
	}
	$q .=
	"(xy && GeomFromText('POLYGON(($w $s,$e $s,$e $n,$w $n,$w $s))',900913))";

    $anns=array();


    $result = pg_query($q);
    while($row = pg_fetch_array($result,null,PGSQL_ASSOC))
    {
			$m=array();
        	$a = preg_match ("/POINT\((.+)\)/",$row['astext'],$m);
        	list($row['x'],$row['y'])= explode(" ",$m[1]);
        $anns[]=$row;
    }
    return $anns;
}

function to_georss($anns,$title,$admin)
{
    //echo "<rss version='2.0' ".
    echo "<feed xmlns='http://www.w3.org/2005/Atom' ".
    "xmlns:georss='http://www.georss.org/georss'".
    " xmlns:geo='http://www.w3.org/2003/01/geo/wgs84_pos#'>\n";
//    echo "<channel>\n";

    echo "<title>$title</title>\n";
    foreach ($anns as $ann)
    {
            $url=($admin)?
                "/panorama/$ann[id]/moderate": OTV_ROOT."/panorama/$ann[id]";
                
            echo "<entry>\n";
            $t = "Panorama $ann[id]"; 
            echo "<title>$t</title>\n";
            echo "<link href='$url' />\n";
            $description="none";
            echo "<summary>$ann[direction]</summary>\n";
            // according to the rss 2.0 spec the guid is a unique string
            // identifier for each item. so this is acceptable. it means
            // that slippy clients can easily tell whether an item has 
            // already been added as a ann.
            echo "<id>$ann[id]</id>\n";
            echo "<georss:point>$ann[y] $ann[x]</georss:point>\n";
            echo "<geo:dir>$ann[direction]</geo:dir>\n";
            echo "<georss:featuretypetag>panorama".
				"</georss:featuretypetag>".
                "\n";
            echo "</entry>\n";
    }
//    echo "</channel>\n";
//    echo "</rss>\n";
    echo "</feed>\n";
}

?>
