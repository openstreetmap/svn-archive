<?php

header("Content-type: image/png");

$file = "/var/www/freemap/images/stats/".date("dmy",
		strtotime("last tuesday")).".png";

if(!file_exists($file))
{
// approx England/Wales bounds in Mercator
$w = -640000;
$s = 6434000;
$e = 192000;
$n = 7310000;

$step =  5000; 
$scale = (isset($_REQUEST['scale']))? $_REQUEST['scale'] : 0.001; // pixels/m
$footyes = (isset($_REQUEST['footyes'])) ? 1:0;
$limit = ($footyes) ? 100: 50;
$boxsize = $step*$scale;
$countarr=array();
$imw=($e-$w)*$scale;
$imh=($n-$s)*$scale;

$im = ImageCreate($imw,$imh);
$backg=ImageColorAllocate($im,0,0,0);
$counts = array();

for($count=0; $count<=10; $count++)
    $cols[$count]=ImageColorAllocate($im,$count*25.5,0,255-($count*25.5));
$textcol=ImageColorAllocate($im,255,255,255);

$conn=pg_connect("dbname=freemap");

$rows = get_rights_of_way($w, $s, $e, $n);
//echo "Done<br/>";

foreach($rows as $row)
{
    //echo "Current ROW centroid $row[x] $row[y]<br/>";
    $px = world_to_pixel($row['x'],$row['y'],$scale,$w,$n);
    //echo "Pixels $px[x] $px[y]<br/>";
    $counts[$px['y']/$boxsize][$px['x']/$boxsize]++;
}

for($row=0; $row<$imh/$boxsize; $row++)
{
    for($col=0; $col<$imw/$boxsize; $col++)
    {
        if(isset($counts[$row][$col]))
        {
        /*
        $n_row = get_rights_of_way_count($wcount,$scount,
                                $wcount+$step, $scount+$step);
        */
            $n_row = ($counts[$row][$col]< $limit) ? 
					$counts[$row][$col]: $limit;
            //echo "row $row col $col, num ROWS = $n_row<br/>";
            $x = $col * $boxsize; 
            $y = $row * $boxsize;
            ImageFilledRectangle 
                ($im,$x,$y,$x+$boxsize,$y+$boxsize, $cols[$n_row/($limit/10)]);
        }
    }
}

$cities = get_cities();
foreach ($cities as $city)
{
    $px = world_to_pixel($city['x'],$city['y'],$scale,$w,$n);
    //echo "City $city[name] x $px[x] y $px[y]<br/>";
    ImageFilledEllipse($im,$px['x'],$px['y'],10,10,$textcol);
    ImageString($im,2,$px['x']+5,$px['y']+5,$city['name'],$textcol);
}

ImagePNG($im);
ImagePNG($im,$file);
ImageDestroy($im);

pg_close($conn);
}
else
{

	$im = ImageCreateFromPNG($file);
	ImagePNG($im);
	ImageDestroy($im);
}

function world_to_pixel($x,$y,$scale,$w,$n)
{
    return array("x"=>($x-$w)*$scale,"y"=>($n-$y)*$scale);
}

function get_rights_of_way($w,$s,$e,$n)
{
    $q = "SELECT (Centroid(way)).x,(Centroid(way)).y FROM planet_osm_line ".
        "WHERE ".
    /*(Centroid(way)).x BETWEEN $w AND $e ".
        "AND (Centroid(way)).y BETWEEN $s AND $n AND ".*/
        " (designation LIKE '%footpath%' OR designation ".
        "LIKE '%bridleway%' OR designation LIKE '%byway%' ".
        "OR foot='designated' OR horse='designated' OR ".
//        "((highway='footway' OR highway='path') AND foot='yes') OR ".
        "highway LIKE '%byway%' OR ".
        "(highway='bridleway' AND horse <> 'permissive'))";
    $result = pg_query($q);
    $rows=array();
    while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
        $rows[]=$row;
    pg_free_result($result);
    return $rows;
}

function get_cities()
{
    $details=array();
    $q = "SELECT (way).x,(way).y,name FROM planet_osm_point WHERE place='city'";
    $result=pg_query($q);
    while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
    {
        $details[] = $row;
    }
    return $details;
}
        


?>
