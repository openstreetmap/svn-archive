<?php
require_once('/home/www-data/private/defines.php');
require_once('../lib/functionsnew.php');
require_once('freemap_functions.php');
require_once('Way.php');

$conn=pg_connect("dbname=gis user=gis");
$cleaned = clean_input($_REQUEST,'pgsql');

$wayIDs=array();

switch($_REQUEST['action'])
{
    case 'create':
        if($cleaned['ways'])
        {
			$wayIDs=explode(",",$cleaned['ways']);
        }
        elseif($cleaned['x'] && $cleaned['y'])
        {
            $wayrows = get_ways_by_point($cleaned['x'],$cleaned['y'],100);
			foreach($wayrows as $row)
				$wayIDs[] = $row['osm_id']; 
        }
		else
        {    
            header("HTTP/1.1 400 Bad Request");
			exit;
        }

		$q= "INSERT INTO annotations(text,xy,dir) ".
                "VALUES ('$cleaned[text]',".
                "PointFromText('POINT ($cleaned[x] $cleaned[y])',900913)".
                ",$cleaned[dir])";
		echo $q;
		pg_query($q);
		$result=pg_query("SELECT currval('annotations_id_seq') AS lastid");
		$row=pg_fetch_array($result,null,PGSQL_ASSOC);
		foreach($wayIDs as $way)
		{
			pg_query("INSERT INTO wayannotations(wayid,annid) VALUES ".
				"($way,$row[lastid])");
        }
        break;
	case 'getByBbox':
		header("Content-type: application/atom+xml");
		echo to_georss(get_anns_by_bbox($cleaned['bbox'],$cleaned['auth']));


		break;

}

pg_close($conn);

function get_anns_by_bbox($bbox)
{
    list($w,$s,$e,$n) = explode(",",$bbox);

    $q = "select *,AsText(xy) from annotations where xy && GeomFromText('POLYGON(($w $s,$e $s,$e $n,$w $n,$w $s))',900913)";

    $anns=array();


    $result = pg_query($q);
    while($row = pg_fetch_array($result,null,PGSQL_ASSOC))
    {
        	$a = preg_match ("/POINT\((.+)\)/",$row['astext'],$m);
        	list($row['x'],$row['y'])= explode(" ",$m[1]);
        $anns[]=$row;
    }
    return $anns;
}

function to_georss($anns)
{
    echo "<feed xmlns='http://www.w3.org/2005/Atom' ".
    "xmlns:georss='http://www.georss.org/georss'>\n";

    echo "<title>annotations</title>\n";
    foreach ($anns as $ann)
    {
            $url=" ";
            echo "<entry>\n";
            echo "<title>Annotation $ann[id]</title>\n";
            echo "<link href='http://www.google.com' />\n";
            echo "<description>$ann[text]</description>\n";
            echo "<id>$ann[id]</id>\n";
			$m=array();
        	$a = preg_match ("/POINT\((.+)\)/",$ann['astext'],$m);
        	list($x,$y)= explode(" ",$m[1]);
            echo "<georss:point>$y $x</georss:point>\n";
            echo "</entry>\n";
    }
    echo "</feed>\n";
}
?>
