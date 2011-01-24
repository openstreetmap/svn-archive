<?php
require_once('../lib/latlong.php');
require_once('../lib/functionsnew.php');

class Feature
{

    function getCoords() { return array(); }
    function to_text() { return array(); }

    function toTextFull($nextFeature)
    {
        $text=array();
        $coords = $this->getCoords();
        $otherCoords=$nextFeature->getCoords();
        $dist=realdist($coords[count($coords)-1]['x'],
                $coords[count($coords)-1]['y'],
                $otherCoords[0]['x'],
                $otherCoords[0]['y']);
        if($dist>=0.05)
        {

            $dx = $otherCoords[0]['x']-$coords[count($coords)-1]['x'];
            $dy = $otherCoords[0]['y']-$coords[count($coords)-1]['y'];
            $text[] = "Continue for ".(round($dist,2))." km in a ".
                compass_direction(get_bearing($dx,$dy))." direction ";
        }    
        return array_merge($this->to_text(),$text);
    }
}

class Point extends Feature
{
    var $pt;

    function __construct($x,$y)
    {
        $this->pt=array();
        $this->pt['x'] = $x;
        $this->pt['y'] = $y;
    }

    function getCoords()
    {
        return array($this->pt);
    }

    function get_key_coords()
    {
        return $this->getCoords();
    }

    function to_xml()
    {
        echo "<point x='".$this->pt['x']."' y='".$this->pt['y']."' />";    
    }

    function serialise()
    {
        return "P ".round($this->pt['x'],2)." ".round($this->pt['y'],2);
    }
}
 

class Way extends Feature
{
    var $way;

    function __construct($in=null,$do_annotations=true,$start=0,$end=1)
    {
        if(is_int($in))
        {
            $q=
                ("SELECT osm_id, name,".
                "AsText(way),foot,horse,highway,designation,fmap_bearing FROM ".
                "planet_osm_line WHERE osm_id=$in");
            $result=pg_query($q);
            $row=pg_fetch_array($result,null,PGSQL_ASSOC);
            if(!$row)
                return null;
            $this->do_get_annotated_way($row,$do_annotations,$start,$end);
        }
        elseif(is_array($in))
        {
            $this->do_get_annotated_way($in,$do_annotations,$start,$end);
        }
        $this->start=$start;
        $this->end=$end;
    }

    function setAttribute($k,$v)
    {
        $this->way[$k] = $v;
    }

    function getAttribute($k)
    {
        return $this->way[$k];
    }

    function getStart()
    {
        return $this->start;
    }

    function getEnd()
    {
        return $this->end;
    }
    
    function getPoint($i)
    {
        return $this->way['points'][$i];
    }

    function getLastPoint()
    {
        return $this->way['points'][count($this->way['points'])-1];
    }

    function getCoords()
    {
        $reverse=$this->start > $this->end;
        $start=($reverse===true) ? count($this->way['points'])-1  :0;
        $end = ($reverse===true) ? -1: count($this->way['points']);
        $step = ($reverse===true) ?  -1:1;
        $coords=array();
        $j=0;
        for($i=$start; $i!=$end; $i+=$step)
        {
            list($coords[$j]['x'],$coords[$j]['y']) = explode(" ", 
                $this->way['points'][$i]);
            $j++;
        }
        return $coords;
    }

    function serialise()

    {
        return "W ".$this->way['osm_id']." ".
            round($this->start,2)." ".round($this->end,2);
    }
    
    function do_get_annotated_way($row,$do_annotations=true,$start=0,$end=1)
    {
        $lower=($start<$end) ? $start:$end;
        $higher=($lower==$start) ? $end:$start;

        $this->way=array();
        $this->way["annotations"] = array();
        foreach($row as $k=>$v)
        {
            $this->way[$k] = $v; 
        }
        preg_match("/LINESTRING\((.+)\)/",$row['astext'],$m);
        $this->way['points'] = explode(",", $m[1]);
        if($lower>0 || $higher<1)
        {
            $partway=array();
            $result = pg_query
            ("SELECT AsText(line_substring(way,$lower,$higher)) ".
            "FROM planet_osm_line WHERE osm_id=".$this->way['osm_id']);
            $row2 = pg_fetch_array($result,null,PGSQL_ASSOC);
            preg_match("/LINESTRING\((.+)\)/",$row2['astext'],$m);
            $partway=explode(",", $m[1]);
        }
        if($do_annotations==true)
        {
            if(isset($partway))
                $this->way['points'] = $partway;
            /*
            $q=
                ("SELECT ann.wayid,AsText(ann.xy),ann.text,ann.dir,".
                "line_locate_point".
                "(line_substring(pol.way,$lower,$higher),ann.xy) as posn ".
                "FROM planet_osm_line pol,annotations ann,".
                "wayannotations wa ".
                "WHERE pol.osm_id=wa.wayid AND ".
                "ann.id=wa.annid AND ".
                "wa.wayid=".$this->way['osm_id'].
                " AND line_locate_point(pol.way,ann.xy) BETWEEN ".
                "$lower AND $higher ".
                "ORDER BY line_locate_point(pol.way,ann.xy)");
            */
            $q=
            ("SELECT AsText(ann.xy),ann.text,ann.dir,".
            "line_locate_point".
            "(line_substring(pol.way,$lower,$higher),ann.xy) as posn ".
            "FROM planet_osm_line pol, annotations ann WHERE pol.osm_id=".
            ($this->way['osm_id'])." AND ".
            " line_locate_point(pol.way,ann.xy) BETWEEN ".
                "$lower AND $higher ".
            "AND Distance(line_substring(pol.way,$lower,$higher),ann.xy) < 100".
            " ORDER BY line_locate_point(pol.way,ann.xy)");
            $result2=pg_query($q);
            while($row2=pg_fetch_array($result2,NULL,PGSQL_ASSOC))
            {
                $a = preg_match ("/POINT\((.+)\)/",$row2['astext'],$m);
                list($row2['x'],$row2['y'])= explode(" ",$m[1]);
                $this->way["annotations"][] = $row2;
             
            }
        }
    }

    
function find_annotated_way()
{
    list($x1,$y1) = explode(" ", $this->way['points'][0]);
    list($x2,$y2) = explode(" ", $this->way['points']    
        [count($this->way['points'])-1]);
    $wx = ($x1<$x2) ? $x1:$x2;
    $wy = ($x1<$x2) ? $y1:$y2;
    $ex = ($x1<$x2) ? $x2:$x1;
    $ey = ($x1<$x2) ? $y2:$y1;
       $q =  
        ("SELECT * FROM annotatedways WHERE wx BETWEEN $wx-10 AND $wx+10 AND ".
         "wy BETWEEN $wy-10 AND $wy+10 AND ex BETWEEN $ex-10 AND $ex+10 AND ". 
         "ey BETWEEN $ey-10 AND $ey+10 AND ".
         "bearing BETWEEN ".($this->way[fmap_bearing]-10) .
         " AND ".($this->way[fmap_bearing]+10) .  " LIMIT 1");
    $result3=pg_query($q);
    return ($row3=pg_fetch_array($result3,NULL,PGSQL_ASSOC)) ?
        $row3['id'] : 0;
}

function to_xml($anndir=true)
{
    $reverse=$this->start > $this->end;
    echo "<way>\n";
    echo "<osm_id>".$this->way['osm_id']."</osm_id>\n";
    $tags = array("name","highway","foot","horse","designation");
    foreach($tags as $tag)
    {
        if($this->way[$tag]!==null)
            echo "<$tag>".$this->way[$tag]."</$tag>\n";
    }
    $start=($reverse===true) ? count($this->way['points'])-1  :0;
    $end = ($reverse===true) ? -1: count($this->way['points']);
    $step = ($reverse===true) ?  -1:1;

    for($i=$start; $i!=$end; $i+=$step)
        echo "<point>".$this->way[points][$i]."</point>\n";

    $this->way_annotations_to_xml($anndir);
    echo "</way>\n";
}

function to_text($anndir=true)
{
    $reverse=$this->start > $this->end;
    $text = array();
    $dist = round($this->get_way_distance($way), 2);
    $dir = ($reverse==true) ?
        opposite_direction(compass_direction($this->get_way_direction())):
        compass_direction($this->get_way_direction());
    $type = get_path_type($this->way);
    $text[] = "Follow the $type for $dist km in a general ".
        "$dir direction";

    $start=($reverse===true) ? count($this->way['annotations'])-1  :0;
    $end = ($reverse===true) ? -1: count($this->way['annotations']);
    $step = ($reverse===true) ?  -1:1;
    for($i=$start; $i!=$end; $i+=$step)
    {
        if($anndir===false || $this->way['annotations'][$i]['dir']==0 ||
            ($this->way['annotations'][$i]['dir']==1 && $reverse===false) ||
            ($this->way['annotations'][$i]['dir']==-1 && $reverse===true)
            )
        {
            $anndist=round (($reverse==true) ?
                (1-$this->way['annotations'][$i]['posn'])*$dist:
                $this->way['annotations'][$i]['posn']*$dist, 2);
            $text[] = 
                "After $anndist km : ".$this->way[annotations][$i]['text'];
        }
    }
    return $text;
}

function get_annotation_coords($anndir=true)
{
    $reverse=$this->start > $this->end;
    $coords=array();
    $start=($reverse===true) ? count($this->way['annotations'])-1  :0;
    $end = ($reverse===true) ? -1: count($this->way['annotations']);
    $step = ($reverse===true) ?  -1:1;
    for($i=$start; $i!=$end; $i+=$step)
    {
        if($anndir===false || $this->way['annotations'][$i]['dir']==0 ||
            ($this->way['annotations'][$i]['dir']==1 && $reverse===false) ||
            ($this->way['annotations'][$i]['dir']==-1 && $reverse===true)
            )
        {
            $coords[] = $this->way['annotations'][$i];
        }
    }
    return $coords;
}

function get_key_coords($nextFeature)
{
    $keycoords=array();
    $coords=$this->getCoords();
    $keycoords[0] = $coords[0];
    $keycoords=array_merge($keycoords,$this->get_annotation_coords());
    if($nextFeature)
    {
        $nextCoords=$nextFeature->getCoords();
        if(realdist($coords[count($coords)-1]['x'],
                $coords[count($coords)-1]['y'],
                $nextCoords[0]['x'],$nextCoords[0]['y'])>=0.05)
        {
            $keycoords=array_merge($keycoords,array($coords[count($coords)-1]));
        }
    }
    return $keycoords;
}

function way_annotations_to_xml($anndir=true)
{
    $reverse=$this->start > $this->end;
    $annotations=$this->way['annotations'];
    $start=($reverse===true) ? count($annotations)-1  :0;
    $end = ($reverse===true) ? -1: count($annotations);
    $step = ($reverse===true) ?  -1:1;
    for($i=$start; $i!=$end; $i+=$step)
    {
        if($anndir===false || $annotations[$i]['dir']==0 ||
            ($annotations[$i]['dir']==1 && $reverse===false) ||
            ($annotations[$i]['dir']==-1 && $reverse===true)
            )
        {
            echo "<annotation id='{$annotations[$i][annotationid]}' ".
            "seg='{$annotations[$i][seg]}' wayid='{$annotations[$i][wayid]}' ".
            "x='{$annotations[$i][x]}' ".
            "y='{$annotations[$i][y]}'>{$annotations[$i][text]}</annotation>\n";
        }
    }
}

function get_way_distance()
{
    /*
    $result=pg_query("SELECT length(way) ".
                        "FROM planet_osm_line WHERE ".
                        "osm_id=".$this->way['osm_id']);
    if($row=pg_fetch_array($result,null,PGSQL_ASSOC))
    {
        return ($row['length']*($higher-$lower))/1000;
    }

    return 0;
    */

    list($sx,$sy) = explode(" ",$this->way['points'][0]);
    list($ex,$ey) = explode(" ",$this->way['points']
        [count($this->way['points'])-1]);
    return realdist($sx,$sy,$ex,$ey);
}

function get_way_direction()
{
    list($x1,$y1) = explode(" ",$this->way['points'][0]);
    list($x2,$y2) = explode(" ",$this->way['points']
        [count($this->way['points'])-1]);
    $dx = $x2-$x1;
    $dy = $y2-$y1;
    return get_bearing($dx,$dy);
}

}
?>
