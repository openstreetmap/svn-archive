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
        $way->annotated_way_to_xml();
        break;

    case 'annotate':
        if($cleaned['id'])
            $way = new Way($cleaned['id']);
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
            if($way->getAttribute('annwayid')==0)
            {
                // should return member of way['points']
                list($x1,$y1) = explode(" ", $way->getPoint(0));
                list($x2,$y2)= explode(" ", $way->getLastPoint());
                $wx=($x1<$x2)?$x1:$x2;
                $wy=($x1<$x2)?$y1:$y2;
                $ex=($x1<$x2)?$x2:$x1;
                $ey=($x1<$x2)?$y2:$y1;
                pg_query
                    ("INSERT INTO annotatedways (wx,wy,ex,ey,bearing) VALUES ".
                    "($wx,$wy,$ex,$ey,".
                    $way->getAttribute('fmap_bearing').")");
                $result=pg_query
                    ("select currval('annotatedways_id_seq') as lastid");
                $row=pg_fetch_array($result,null,PGSQL_ASSOC);
                $way->setAttribute('annwayid',$row["lastid"]);    
            }

            $q= "INSERT INTO annotations(wayid,text,xy,dir) ".
                "VALUES (".$way->getAttribute('annwayid').",".
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
