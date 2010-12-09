<?php
require_once('../lib/functionsnew.php');
require_once('freemap_functions.php');
session_start();

if(!isset($_SESSION['gatekeeper']))
{
    header("HTTP/1.1 401 Unauthorized");
    echo "unauthorized";
    exit;
}

$conn=pg_connect("dbname=gis user=gis");
$cleaned = clean_input($_REQUEST,'pgsql');
$cleaned['what'] = (isset($cleaned['what']))? $cleaned['what']:'way';

switch($cleaned['what'])
{
case "node":
	$q= "SELECT name,amenity,man_made,\"natural\",tourism,place,".
            "AsText(way) FROM ".
            "planet_osm_point WHERE osm_id=$cleaned[id]";
	$result=pg_query($q);
	if($row=pg_fetch_array($result,null,PGSQL_ASSOC))
	{
		$highlevel = get_high_level($row);
		if($highlevel!="unknown")
		{
			$m = array();
			$a = preg_match ("/POINT\((.+)\)/",$row['astext'],$m);
			list($x,$y)= explode(" ",$m[1]);
			$result2=pg_query("SELECT * FROM annotatednodes WHERE ".
				"x BETWEEN $x-50 AND $x+50 AND ".
				"y BETWEEN $y-50 AND $y+50 AND ".
				"type='$type' AND name='$row[name]'");
			if($row2=pg_fetch_array($result,null,PGSQL_ASSOC))
			{
				pg_query("UPDATE annotatednodes SET description=".
					"'$cleaned[text]' WHERE id=$row2[id]");
			}
			else
			{
				pg_query("INSERT INTO annotatednodes".
				"(x,y,name,type,description) ".
					"VALUES ($x,$y,'$row[name]',".
					"'$highlevel','$cleaned[text]')");
			}
		}
	}
	break;

default:
	$way = get_annotated_way($cleaned['id']);
	if($way['annwayid']==0)
	{
    list($x1,$y1) = explode(" ", $way['points'][0]);
    list($x2,$y2) = explode(" ", $way['points'][count($way['points'])-1]);
    $wx=($x1<$x2)?$x1:$x2;
    $wy=($x1<$x2)?$y1:$y2;
    $ex=($x1<$x2)?$x2:$x1;
    $ey=($x1<$x2)?$y2:$y1;
    pg_query("INSERT INTO annotatedways (wx,wy,ex,ey,bearing) VALUES ".
            "($wx,$wy,$ex,$ey,$way[fmap_bearing])");
    $result=pg_query("select currval('annotatedways_id_seq') as lastid");
    $row=pg_fetch_array($result,null,PGSQL_ASSOC);
    $way['annwayid'] =  $row["lastid"];    
	}

	pg_query("UPDATE annotations set annotationId=annotationId+1 WHERE ".
        "wayid=$way[annwayid] and annotationId>=$cleaned[annotationId]");

	$q= "INSERT INTO annotations(wayid,annotationId,text,x,y,dir) VALUES ".
	"($way[annwayid],$cleaned[annotationId],'$cleaned[text]',".
	"$cleaned[x],$cleaned[y],$cleaned[dir])";
	pg_query($q);
	pg_close($conn);
	echo $q;
	break;
}
?>

