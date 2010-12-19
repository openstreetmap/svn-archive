<?php

require_once('../lib/functionsnew.php');
require_once('freemap_functions.php');

session_start();

$conn=pg_connect("dbname=gis user=gis");
$cleaned = clean_input($_REQUEST,'pgsql');

switch($cleaned['action'])
{
  case 'get':

    if($cleaned['source']=='db')
    {
        $points=explode(",",get_route($cleaned['id']));
    }
    else
    {
        $points = explode(",", $cleaned['route']);
    }
    $prevways=array();
    $foundways=array();
	$waystoadd=array();
    $pts = array();
    for($i=0; $i<count($points); $i++)
    {
        $prevdist=100;
        $pts[$i] = array();
		$pts[$i]['way']= 0;
        list($pts[$i]['x'],$pts[$i]['y'])=explode(" ", $points[$i]);
        $ways=get_ways_by_point($pts[$i]['x'],$pts[$i]['y'],100,0);
        if(count($ways)>0)
        {
            $waytoadd=null;
            // Add ways that are found in 2 consecutive points
            for($j=0; $j<count($ways); $j++)
            {
                for($k=0; $k<count($prevways); $k++)
                {
                    if($ways[$j]['osm_id']==$prevways[$k]['osm_id'] &&
                    $ways[$j]['dist'] < $prevdist)
                    {
                        $waytoadd=$ways[$j];
                        $prevdist=$ways[$j]['dist'];
                    }
                }
            }

            $pts[$i]['way'] = ($waytoadd===null ) ? 0 : $waytoadd['osm_id'];
			if($waytoadd!==null&&!isset($waystoadd[$waytoadd['osm_id']]))
			{
				$waystoadd[$waytoadd['osm_id']] = $waytoadd; 
				$lastway=$waytoadd['osm_id'];
			}
			if($pts[$i]['way']>0 && $pts[$i]['way']!=$pts[$i-1]['way'])
			{
				$waystoadd[$pts[$i-1]['way']]['end'] = $prevposn;
				for($j=0; $j<count($prevways); $j++)
				{
					if($prevways[$j]['osm_id']==$pts[$i]['way'])
					{
						$waystoadd[$pts[$i]['way']]['start'] = 
							$prevways[$j]['posn'];
						break;
					}
				}
			}
            $prevways=$ways;
			if($waytoadd!==null)
				$prevposn=$waytoadd['posn'];
        }
    }

	if(isset($waystoadd[$lastway]) && !isset($waystoadd[$lastway]['end']))
		$waystoadd[$lastway]['end'] = $prevposn;

	if(!isset($cleaned['format']) || $cleaned['format']=='xml')
	{
	header("Content-Type: text/xml");
	header("Content-Disposition: attachment; filename=route.xml");
    echo "<route>\n";
    for($i=0; $i<count($pts); $i++)
    {
		echo "<pt wayid='{$pts[$i][way]}' />\n";
        if($pts[$i]['way']==0 && ($i==count($pts)-1 || $pts[$i+1]['way']==0))
            echo "<point>{$pts[$i][x]} {$pts[$i][y]}</point>\n";
		

        if($i > 0 && $pts[$i]['way'] != $pts[$i+1]['way'] &&
            $pts[$i]['way']>0)
        {
            //echo "</way>\n";
        }

        if($i < count($pts)-1 && $pts[$i]['way'] != $pts[$i+1]['way'] &&
            $pts[$i+1]['way']>0)
        {
			$w1=$waystoadd[$pts[$i+1]['way']];
			echo "<wayinfo id='{$pts[$i+1][way]}' start='$w1[start]' end='$w1[end]' />\n";
			$w=do_get_annotated_way($waystoadd[$pts[$i+1]['way']],true,
				$waystoadd[$pts[$i+1]['way']]['start'],
				$waystoadd[$pts[$i+1]['way']]['end']);
				annotated_way_to_xml($w,
						$waystoadd[$pts[$i+1]['way']]['start'] >
						$waystoadd[$pts[$i+1]['way']]['end'] 
								);
        }
    }

	
    echo "</route>\n";
	}
    break;


  case 'add':
    if(isset($_SESSION['gatekeeper']))
    {
        $q=("INSERT INTO routes (route) VALUES ".
        "(GeomFromText('LINESTRING($cleaned[route])',900913))");
        echo $q;
        pg_query($q);
    }
    else
    {
        header("HTTP/1.1 401 Unauthorized");
    }    
    break;
}

pg_close($conn);

?>
