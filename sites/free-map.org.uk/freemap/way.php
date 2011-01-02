<?php
require_once('/home/www-data/private/defines.php');
require_once('../lib/functionsnew.php');
require_once('freemap_functions.php');
require_once('Way.php');

$conn=pg_connect("dbname=gis user=gis");
$cleaned = clean_input($_REQUEST,'pgsql');


switch($_REQUEST['action'])
{
    case 'get':
        $way=new Way((int)$cleaned['id']);
        header("Content-type: text/xml");
        $way->to_xml(false);
        break;

    case 'annotate':
        if($cleaned['id'])
        {
            $way = new Way((int)$cleaned['id']);
        }
        else
        {
            $way = get_ways_by_point($cleaned['x'],$cleaned['y'],100,1);
            $way = count($way)==1 ? new Way($way[0]):null;
        }

        if($way===null)
        {    
            header("HTTP/1.1 404 Not Found");
        }
        else
        {
            $q= "INSERT INTO annotations(wayid,text,xy,dir) ".
                "VALUES (".$way->getAttribute('osm_id').",".
                "'$cleaned[text]',".
                "PointFromText('POINT ($cleaned[x] $cleaned[y])',900913)".
                ",$cleaned[dir])";
            pg_query($q);
            echo $q;
        }
        break;
}
    


pg_close($conn);

?>
