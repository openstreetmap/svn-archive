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

function get_path_type($pathinfo)
{
    $destypes = array
        ("public_footpath" => "public footpath",
        "footpath" => "public footpath",
        "public_bridleway" => "public bridleway",
        "bridleway" => "public bridleway",
        "public_byway" => "byway",
        "byway" => "byway",
        "restricted_byway" => "restricted byway");

    return (isset($destypes[$pathinfo['designation']]))?
        $destypes[$pathinfo['designation']]:
        ($pathinfo['foot']==='permissive' ? "permissive $pathinfo[highway]" : 
        $pathinfo['highway']);
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

function get_ways_by_point($x,$y,$dist=100,$n=0,$snap=false)
{
    $ways=array();
    $q=("SELECT osm_id, name,highway,designation,".
                "foot,horse,fmap_bearing,AsText(way), ".
                "line_locate_point(way,PointFromText('POINT($x $y)',900913)) ".
                "as posn,".
                "Distance(GeomFromText('POINT($x $y)',900913),way) as dist ".
                "FROM ".
                "planet_osm_line WHERE Distance(GeomFromText('POINT($x $y)',".
                "900913),way) < $dist AND highway != '' ");

    $q .= "ORDER BY dist";

    if($n>0)
        $q .= " LIMIT $n";
    $result2=pg_query($q);

    while($row=pg_fetch_array($result2,null,PGSQL_ASSOC))
        $ways[] = $row;

    pg_free_result($result2);
    return $ways;
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


function get_route_annotations($id)
{
    $annotations=array();
    $result2=pg_query
        ("SELECT AsText(ann.xy),ann.text,ann.annotationid,".
        "line_locate_point(rte.route,ann.xy) AS posn FROM routes rte,".
        "annotations ann WHERE rte.id=$id AND ".
        "Distance(ann.xy,rte.route) < 100 ".
        "ORDER BY line_locate_point(rte.route,ann.xy)");

    while($row=pg_fetch_array($result2,null,PGSQL_ASSOC))
    {
        $a = preg_match ("/POINT\((.+)\)/",$row['astext'],$m);
        list($row['x'],$row['y'])= explode(" ",$m[1]);
        $annotations[] = $row;
    }
    return $annotations;
}

function get_bearing($dx,$dy)
{
    $ang=(-rad2deg(atan2($dy,$dx))) + 90;
    return ($ang<0 ? $ang+360:$ang);
}

function compass_direction($a)
{
    if($a<22.5 || $a>=337.5)
        return "N";
    else if($a<67.5)
        return "NE";
    else if($a<112.5)
        return "E";
    else if($a<157.5)
        return "SE";
    else if($a<202.5)
        return "S";
    else if($a<247.5)
        return "SW";
    else if($a<292.5)
        return "W";
    else
        return "NW";
}

function opposite_direction($dir)
{
    $dirs=array ("N","NE","E","SE","S","SW","W","NW");
    for($i=0; $i<8; $i++)
    {
        if($dirs[$i]==$dir)
            return $dirs[$i<4 ? $i+4:$i-4];
    }
    return null;
}

function render_tiles($im,$east,$north,$zoom,$width,$height)
{
    /*
    $filename="http://tilesrv.sucs.org/~nickw/render.php?".
                 "e=$e&n=$n&zoom=$zoom&w=$width&h=$height";

    $img=ImageCreateFromPNG($filename);
    ImageCopy($im,$img,0,0,0,0,$width,$height);
    */
            $nTileCols = 2+floor($width/256);
            $nTileRows = 2+floor($height/256);
            $topLeftX = metresToPixel($east,$zoom)-$width/2;
            $topLeftY = metresToPixel(-$north,$zoom)-$height/2;
            $topLeftXTile = floor($topLeftX/256);
            $topLeftYTile = floor($topLeftY/256);
            $curY = -$topLeftY%256;
            for($row=0; $row<$nTileRows; $row++)
            {        
                $curX = -$topLeftX%256;
                for($col=0; $col<$nTileCols; $col++)
                {
                    if($curX<$width && $curY<$height &&
                        $curX>-256 && $curY>-256)
                    {    
                        $filename="http://tilesrv.sucs.org/ofm/$zoom/".
                            ($topLeftXTile+$col)."/".
                            ($topLeftYTile+$row).".png";

                        $tile=ImageCreateFromPNG($filename);
                        ImageCopy($im,$tile,$curX,$curY,0,0,256,256);
                        //echo $filename;
                    }
                    
                    $curX+=256;
                }
                $curY+=256;
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

function metresToPixel($m,$zoom)
{
    return (pow(2,8+$zoom)*($m+20037508.34)) / 40075016.68;
}

function get_bounds($pts)
{
    $w=20037508.34;
    $e=-20037508.34;
    $n=-20037508.34;
    $s=20037508.34;

    for($i=0; $i<count($pts); $i++)
    {
        if($pts[$i]['x'] < $w)
            $w=$pts[$i]['x'];
        if($pts[$i]['x'] > $e)
            $e=$pts[$i]['x'];
        if($pts[$i]['y'] < $s)
            $s=$pts[$i]['y'];
        if($pts[$i]['y'] > $n)
            $n=$pts[$i]['y'];
    }
    return array($w,$s,$e,$n);
}

function get_required_dimensions($bounds,$zoom)
{
    $factor=(pow(2,8+$zoom)) / 40075016.68;
    $edist=$bounds[2]-$bounds[0];
    $ndist=$bounds[3]-$bounds[1];
    return array($edist*$factor,$ndist*$factor);
}
?>
