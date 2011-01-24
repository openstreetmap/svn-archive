<?php
session_start();
include('../lib/functionsnew.php');
require_once('../common/defines.php');

$conn=pg_connect("dbname=gis user=gis");
$cleaned=clean_input($_GET);
$result=pg_query("SELECT * FROM panoramas WHERE ID=$cleaned[id]");
if(pg_num_rows($result)==1)
{
    $row=pg_fetch_array($result,null,PGSQL_ASSOC);
        switch($cleaned['action'])
        {
            case "rotate":
                if(isset($_SESSION['gatekeeper']))
                {
                    pg_query
                    ("UPDATE panoramas SET direction=$cleaned[angle] ".
                     "WHERE ID=$cleaned[id]");
                    echo "Angle set to $cleaned[angle]";
                }
                else
                {
                    header("HTTP/1.1 401 Unauthorized");
                }
                break;

            case "setAttributes":
                if(isset($_SESSION['gatekeeper']))
                {
                    if(isset($cleaned['x']) AND isset($cleaned['y']))
                    {
                        $q="UPDATE panoramas SET xy=PointFromText('POINT($cleaned[x] $cleaned[y])',900913) WHERE id=$cleaned[id]";
                        echo $q;
                        pg_query($q);
                    }
                }
                else
                {
                    header("HTTP/1.1 401 Unauthorized");
                }
                break;
            case "authorise":
                if(isset($_SESSION['admin']))
                {
                    pg_query("UPDATE panoramas SET authorised=1 ".
                                "WHERE ID=$cleaned[id]");
                    echo "Authorised";
                }
                else
                {
                    header("HTTP/1.1 401 Unauthorized");
                }
                break;
            case "delete":
                if( (isset($_SESSION['gatekeeper']) &&
                        $row['userid']==$_SESSION['gatekeeper']) ||
                            isset($_SESSION['admin']))
                {
                        pg_query
                            ("DELETE FROM panoramas WHERE ID=$cleaned[id]");
                        unlink(OTV_UPLOADS."/${cleaned[id]}.jpg");

                        $result2=pg_query("SELECT * FROM routes WHERE ".
                                            "fid=$cleaned[id]");
                        while($row2=pg_fetch_array($result2,
                                null,PGSQL_ASSOC))
                        {
                            pg_query("DELETE FROM routes WHERE ".
                                        "id=$row2[id]");
                            pg_query("UPDATE routes SET routepoint=".
                                        "routepoint-1 WHERE ".
                                        "routeid=$row2[routeid] AND ".
                                        "routepoint>$row2[routepoint]");
                        }
                        echo "Deleted";
                }
                else
                {
                    header("HTTP/1.1 401 Unauthorized");
                }
                break;
            case "moderate":
                if(isset($_SESSION['admin']))
                {
                    echo "<html>";
                    echo "<head>";
                    echo "<link rel='stylesheet' type='text/css' ".
                        "href='css/osv.css' />";
                    echo "</head>";
                    echo "<body><p>";
                    echo "<h1>Submitted photo $cleaned[id]</h1>\n";
                    echo "<p><img src='/panorama/$cleaned[id]' ".
                    "alt='Panorama $cleaned[id]' /></p>\n";
                    echo "<a href='/panorama/$cleaned[id]/authorise'>".
                        "Authorise</a> ";
                    echo 
                      "<a href='/panorama/$cleaned[id]/delete'>Delete</a>";
                    echo "</p></body></html>";
                }
                else
                {
                    header("Location: ".
                        "/common/user.php?action=login&redirect=".
                        "/panorama/$cleaned[id]/moderate");
                }
                break;
            case "getJSON":
                header("Content-type: application/json");
                echo json_encode($row);
                break;
            case "getAdjacent":
                header("Content-type: application/json");
                $adjacents=get_adjacent_panoramas($cleaned['id']);
                echo json_encode($adjacents);
                break;
			case "getTime":
				echo "$row[time]<br/>";
				echo date('H:i:s', $row['time']);
				$tm = 1270656310;
				echo "tm $tm time $row[time]<br/>";
				if($row['time']==$tm)
					echo "yes";
				echo date ("D d M Y, H:i:s e O" , $tm). "<br/>";
				echo date ("D d M Y, H:i:s e O" , $row['time'])."<br/>";
				break;
            default:
                if($row['authorised']==1 || isset($_SESSION['admin']) ||
                        (isset($_SESSION['gatekeeper']) &&
                        $row['userid']==$_SESSION['gatekeeper']))
                {
                    $file = OTV_UPLOADS."/${cleaned[id]}.jpg";
                    if(file_exists($file))
                    {
                        header("Content-type: image/jpeg");
                        if(isset($cleaned['resize']))
                        {
                            $imIn=ImageCreateFromJPEG($file);
                            list($wIn,$hIn)=getimagesize($file);
                            $wOut=round($wIn*$cleaned['resize']/100);
                            $hOut=round($hIn*$cleaned['resize']/100);
                            //echo "$wIn $wOut $hIn $hOut";
                            $imOut=ImageCreateTrueColor($wOut,$hOut);
                            ImageCopyResampled($imOut,$imIn,0,0,0,0,
                                        $wOut,$hOut,$wIn,$hIn);
                            ImageJPEG($imOut);
                            ImageDestroy($imOut);
                            ImageDestroy($imIn);
                        }
                        else
                        {
                            $f = file_get_contents($file);
                            echo $f;
                        }
                    }
                    else
                    {
                        header("HTTP/1.1 404 Not Found");
                    }
                }
                else
                {
                    header("HTTP/1.1 401 Unauthorized");
                }
                break;
        }
}
else
{
    header("HTTP/1.1 404 Not Found");
} 
pg_close($conn);


function get_adjacent_panoramas($id)
{
    $lastrow=null;
    $adjacents=array();
    $result=pg_query("select pan.id,pan.direction,pol.osm_id,AsText(pan.xy),".
        "line_locate_point(pol.way,pan.xy) as pos ".
        "from planet_osm_line pol, panoramas pan ".
        "where Distance(pan.xy,pol.way) < 100 and pol.osm_id in ".
        "(select pol.osm_id from planet_osm_line pol, panoramas pan ".
        "where Distance(pan.xy,pol.way)<100 and id=$id) order by ".
        "pol.osm_id,pos");
    while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
    {
		$added=false;

        if($lastrow && $lastrow['osm_id'] == $row['osm_id'])
        {
            if($row['id']==$id)
			{
                $adjacents[] = $lastrow;
				$added=true;
			}
            elseif($lastrow['id']==$id)
			{
                $adjacents[] = $row;
				$added=true;
			}
			if($added==true)
			{
				$m=array();
				$a = preg_match ("/POINT\((.+)\)/",
					$adjacents[count($adjacents)-1]['astext'],$m);
				list($adjacents[count($adjacents)-1]['x'],
					$adjacents[count($adjacents)-1]['y'])= explode(" ",$m[1]);
				unset($adjacents[count($adjacents)-1]['astext']);
			}
        }
        $lastrow=$row;
    }
    return $adjacents;
}
?>
