<?php
require_once('/home/www-data/private/defines.php');
require_once('../lib/functionsnew.php');
require_once('freemap_functions.php');

// fernhurst -80454 6629930 (900913 spherical mercator)

// format for ways
// column name= 'astext'
// format = (x1 y1,x2 y2,x3 y3,...,xn yn)

$conn=pg_connect("dbname=gis user=gis");
$cleaned = clean_input($_REQUEST);


switch($_REQUEST['action'])
{
    case 'get':
        $x = $cleaned["x"];
        $y = $cleaned["y"];
        $n = (isset($_REQUEST["n"])) ? $cleaned['n'] : 0;
        $dist = (isset($_REQUEST['dist'])) ? $cleaned['dist']: 100;
        $what=(isset($_REQUEST["what"])) ? $_REQUEST['what']:"both";
        $foundnode=false;
        header("Content-type: text/xml");

        echo "<osmdata>";
        if($what!="ways")
        {
            $q=
            ("SELECT osm_id, name,amenity,man_made,\"natural\",tourism,place,".
            "AsText(way),".
            "Distance(GeomFromText('POINT($x $y)',900913),way) as dist FROM ".
            "planet_osm_point WHERE Distance".
            "(GeomFromText('POINT($x $y)',900913),way) < $dist ".
            "ORDER BY dist");
            if ($n != 0)
                $q .= " LIMIT $n";


            $result=pg_query($q);

            while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
            {
                $foundnode=true;
                // Only output known types-these are defined in get_high_level()
                $highlevel = get_high_level($row);
                if($highlevel!="unknown")
                {
                    echo "<node>\n";
                    echo "<type>$highlevel</type>\n";
                    if($row['name']!="")
                        echo "<name>$row[name]</name>\n";
                    echo "<osm_id>$row[osm_id]</osm_id>\n";

                    echo "</node>\n";
                }
            }
            pg_free_result($result);
        }

        if(($foundnode==false && $what=="both") || $what=="ways")
        {
            $q=("SELECT osm_id, name,highway,designation".
                "foot,horse,bicycle,AsText(way), ".
                "Distance(GeomFromText('POINT($x $y)',900913),way) as dist FROM ".
                "planet_osm_line WHERE Distance(GeomFromText('POINT($x $y)',".
                "900913),way) < 50 AND highway != '' ORDER BY dist LIMIT 1");

            $result2=pg_query($q);

            $row=pg_fetch_array($result2,null,PGSQL_ASSOC);
            if($row)
            {
                echo "<way>\n";
                echo "<osm_id>$row[osm_id]</osm_id>\n";
                $tags = array("name","highway","foot","horse");
                foreach($tags as $tag)
                {
                    if($row[$tag]!='')
                        echo "<$tag>$row[$tag]</$tag>\n";
                }
                $m=array();
                preg_match("/LINESTRING\((.+)\)/",$row['astext'],$m);
                $points = explode(",", $m[1]);
                foreach ($points as $point)
                    echo "<point>$point</point>\n";
                echo "</way>\n";
            }
            pg_free_result($result2);
        }
        echo "</osmdata>";

        break;

    case 'search':
        $query = $cleaned['q'];
        header("Content-type: text/xml");

        $q=
            ("SELECT osm_id, name,amenity,man_made,\"natural\",tourism,place,".
            "AsText(way) as p FROM ".
            "planet_osm_point WHERE name ILIKE '%$query%'");

        $result=pg_query($q);

        echo "<osmdata>";
        while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
        {
            // Only output known types - these are defined in get_high_level()
            $highlevel = get_high_level($row);
            if($highlevel!="unknown")
            {
                echo "<node>\n";
                $m = array();
                $a = preg_match ("/POINT\((.+)\)/",$row['p'],$m);
                list($x,$y)= explode(" ",$m[1]);
                echo "<x>$x</x><y>$y</y>\n";
                echo "<type>$highlevel</type>\n";
                if($row['name']!="")
                    echo "<name>".htmlentities($row[name])."</name>\n";
                echo "<osm_id>$row[osm_id]</osm_id>\n";
                echo "</node>\n";
            }
        }
        echo "</osmdata>";
        pg_free_result($result);
        break;

    case 'getById':
        $way=get_annotated_way($cleaned['osm_id']);
        header("Content-type: text/xml");
        annotated_way_to_xml($way);
        break;
}
    


pg_close($conn);

?>
