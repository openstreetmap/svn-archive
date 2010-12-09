<?php
require_once('../lib/latlong.php');
require_once('../lib/functionsnew.php');

function wholly_numeric($input)
{
    return preg_match("/^-?[\d\.]+$/",$input);
}

function line_length($x1,$y1,$x2,$y2)
{
    $dx=$x2-$x1;
    $dy=$y2-$y1;
    return sqrt($dx*$dx + $dy*$dy);
}

# Returns the slope angle of a contour line; 
# always in the range -90 -> 0 -> +90.
# 08/02/05 made more generalised by passing parameters as x1,x2,y1,y2
# rather than the line array.
function slope_angle($x1,$y1,$x2,$y2)
{
    $dy = $y2-$y1;
    $dx = $x2-$x1;
    /*
    $a = rad2deg(atan2($dy,$dx));
    return round($a-(180*($a>90&&$a<270))); 
    */
    $a = $dx ? round(rad2deg(atan($dy/$dx))) : 90;
    return $a; 
}

function get_high_level ($tags)
{
    $highlevel = array("pub" => array ("amenity","pub"),
              "car park"=>array("amenity","parking"),
              "viewpoint"=>array("tourism","viewpoint"),
              "hill"=>array("natural","peak"),
              "village"=>array("place","village"),
              "hamlet"=>array("place","hamlet"),
              "suburb"=>array("place","suburb"),
              "town"=>array("place","town"),
              "restaurant"=>array("amenity","restaurant"),
              "city"=>array("place","city"));

    foreach ($highlevel as $h=>$t)
    {
        if ($tags[$t[0]] && $tags[$t[0]] == $t[1])
            return $h;
    }
    return "unknown"; 
}


function get_photo($id,$width,$height)
{
    $file="/home/www-data/uploads/photos/$id.jpg";
    if(!file_exists($file))
        return false;
    else
    {
        header("Content-type: image/jpeg");

        if ($width && $height)
        {
            $origsz=getimagesize($file);
            $im=ImageCreate($width,$height);
            $im2=ImageCreateFromJPEG($file);
            ImageCopyResized($im,$im2,0,0,0,0,
                    $width,$height,$origsz[0],$origsz[1]);
            ImageJPEG($im);
            ImageDestroy($im);
            ImageDestroy($im2);
        }
        else
        {
            echo file_get_contents($file);
        }
    }
    return true;
}

function get_annotated_way($id,$do_annotations=true)
{
    $way=null;
    $q=
            ("SELECT osm_id, name,".
            "AsText(way),foot,horse,highway,designation,fmap_bearing FROM ".
            "planet_osm_line WHERE osm_id=$id");
    $result=pg_query($q);
    $row=pg_fetch_array($result,null,PGSQL_ASSOC);
    if(!$row)
        return null;
    return do_get_annotated_way($row,$do_annotations);
}

function do_get_annotated_way($row,$do_annotations=true)
{
        $way=array();
        $way["annotations"] = array();
        $tags = array("osm_id",
                     "name","highway","foot","horse","fmap_bearing",
                     "designation");
        foreach($tags as $tag)
        {
            if($row[$tag]!='')
                $way[$tag] = $row[$tag];
        }
        $way['points'] = array();
        preg_match("/LINESTRING\((.+)\)/",$row['astext'],$m);
        $way['points'] = explode(",", $m[1]);
        if($do_annotations==true)
        {
            $way['annwayid']=find_annotated_way($way);
            if($way['annwayid']>0)
            {
                $result2=pg_query
                ("SELECT * FROM annotations WHERE wayid=$way[annwayid] ".
                "ORDER BY annotationid");
                while($row2=pg_fetch_array($result2,NULL,PGSQL_ASSOC))
                    $way["annotations"][] = $row2;
            }
        }

    return $way;
}

function find_annotated_way($way)
{
    list($x1,$y1) = explode(" ", $way['points'][0]);
    list($x2,$y2) = explode(" ", $way['points'][count($way['points'])-1]);
    $wx = ($x1<$x2) ? $x1:$x2;
    $wy = ($x1<$x2) ? $y1:$y2;
    $ex = ($x1<$x2) ? $x2:$x1;
    $ey = ($x1<$x2) ? $y2:$y1;
   	$q =  
        ("SELECT * FROM annotatedways WHERE wx BETWEEN $wx-10 AND $wx+10 AND ".
         "wy BETWEEN $wy-10 AND $wy+10 AND ex BETWEEN $ex-10 AND $ex+10 AND ". 
         "ey BETWEEN $ey-10 AND $ey+10 AND ".
		 "bearing BETWEEN $way[fmap_bearing]-10 AND $way[fmap_bearing]+10 ".
         "LIMIT 1");
	$result3=pg_query($q);
    return ($row3=pg_fetch_array($result3,NULL,PGSQL_ASSOC)) ?
        $row3['id'] : 0;
}

function annotated_way_to_xml($way,$reverse=false)
{
    echo "<way>\n";
    echo "<osm_id>$way[osm_id]</osm_id>\n";
    $tags = array("name","highway","foot","horse","designation","annwayid");
    foreach($tags as $tag)
    {
        if($way[$tag]!==null)
            echo "<$tag>$way[$tag]</$tag>\n";
    }
	$start=($reverse===true) ? count($way['points'])-1  :0;
	$end = ($reverse===true) ? -1: count($way['points']);
	$step = ($reverse===true) ?  -1:1;

	for($i=$start; $i!=$end; $i+=$step)
        echo "<point>{$way[points][$i]}</point>\n";

    way_annotations_to_xml($way['annotations'],$reverse);
    echo "</way>\n";
}

function way_annotations_to_xml($annotations,$reverse=false)
{
	$start=($reverse===true) ? count($annotations)-1  :0;
	$end = ($reverse===true) ? -1: count($annotations);
	$step = ($reverse===true) ?  -1:1;
	for($i=$start; $i!=$end; $i+=$step)
    {
        echo "<annotation id='{$annotations[$i][annotationid]}' ".
        "seg='{$annotations[$i][seg]}' wayid='{$annotations[$i][wayid]}' ".
		"x='{$annotations[$i][x]}'".
        " y='{$annotations[$i][y]}'>{$annotations[$i][text]}</annotation>\n";
    }
}

function node_to_xml($row)
{
    $highlevel=get_high_level($row);
    if($highlevel!="unknown")
    {
        echo "<node>\n";
        $m = array();
        $a = preg_match ("/POINT\((.+)\)/",$row['astext'],$m);
        list($x,$y)= explode(" ",$m[1]);
        echo "<x>$x</x><y>$y</y>\n";
        echo "<type>$highlevel</type>\n";
        if($row['name']!="")
            echo "<name>".htmlentities($row[name])."</name>\n";
        echo "<osm_id>$row[osm_id]</osm_id>\n";
        $result2=pg_query
            ("SELECT * FROM annotatednodes WHERE x BETWEEN $x-50 AND $x+50 ".
                "AND y BETWEEN $y-50 AND $y+50 AND type='$highlevel' AND ".
                "name='$row[name]'");
        if($row2=pg_fetch_array($result2,null,PGSQL_ASSOC))
        {
            echo "<description>$row2[description]</description>\n";
        }
        echo "</node>\n";
    }
}

function lonToX($lon,$zoom)
{

    return round  (0.5+floor( (pow(2,$zoom+8)*($lon+180)) / 360));
}

function latToY($lat,$zoom)
{
    $f = sin((M_PI/180)*$lat);

    $y = round(0.5+floor
    (pow(2,$zoom+7) + 0.5*log((1+$f)/(1-$f)) *
                         (-pow(2,$zoom+8)/(2*M_PI))));
    return $y;
} 
?>
