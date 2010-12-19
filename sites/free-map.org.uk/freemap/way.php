<?php
require_once('/home/www-data/private/defines.php');
require_once('../lib/functionsnew.php');
require_once('freemap_functions.php');

$conn=pg_connect("dbname=gis user=gis");
$cleaned = clean_input($_REQUEST,'pgsql');


switch($_REQUEST['action'])
{
    case 'get':
        $way=get_annotated_way($cleaned['id']);
        header("Content-type: text/xml");
        annotated_way_to_xml($way);
        break;

    case 'annotate':
        if($cleaned['id'])
            $way = get_annotated_way($cleaned['id']);
        else
        {
            $way = get_ways_by_point($cleaned['x'],$cleaned['y'],100,1);
            $way = count($way)==1 ? $way[0]:null;
        }

        if($way===null)
        {    
            header("HTTP/1.1 404 Not Found");
        }
        else
        {
            if($way['annwayid']==0)
            {
                list($x1,$y1) = explode(" ", $way['points'][0]);
                list($x2,$y2)=
                    explode(" ", $way['points'][count($way['points'])-1]);
                $wx=($x1<$x2)?$x1:$x2;
                $wy=($x1<$x2)?$y1:$y2;
                $ex=($x1<$x2)?$x2:$x1;
                $ey=($x1<$x2)?$y2:$y1;
                pg_query
                    ("INSERT INTO annotatedways (wx,wy,ex,ey,bearing) VALUES ".
                    "($wx,$wy,$ex,$ey,$way[fmap_bearing])");
                $result=pg_query
                    ("select currval('annotatedways_id_seq') as lastid");
                $row=pg_fetch_array($result,null,PGSQL_ASSOC);
                $way['annwayid'] =  $row["lastid"];    
            }

            $q= "INSERT INTO annotations(wayid,text,xy,dir) ".
                "VALUES ($way[annwayid],".
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
