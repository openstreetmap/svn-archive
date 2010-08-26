<?php
require_once("../lib/functionsnew.php");
$conn=dbconnect("otv");

session_start();

if(!isset($_SESSION['gatekeeper']) && $_GET['action']!='get')
{
    header("HTTP/1.1 401 Unauthorized");
    exit;
}

$cleaned = clean_input($_REQUEST);

switch($_GET['action'])
{
  case "add":

    $IDs = explode(",", $cleaned["panoIDs"]);

    mysql_query("INSERT INTO routes0 (user) VALUES ($_SESSION[gatekeeper])");
    insert_into_routes_table(mysql_insert_id(),$IDs);

    echo $routeid;
  break;

  case "modify":
      // we're deleting and reforming as the order might have changed
    mysql_query("DELETE FROM routes WHERE routeid=$cleaned[id]");
    $IDs = explode(",",$cleaned["panoIDs"]);
    insert_into_routes_table($cleaned['id'],$IDs);
  break;

  case "insert":
    $routeIDs = explode(",",$cleaned["id"]);
    $prevs=explode(",",$cleaned["prev"]);
    $new=$cleaned["new"];

    if(count($routeIDs) != count($prevs))
    {
        header("HTTP/1.1 400 Bad Request");
        exit;
    }

    $i=0;
    foreach ($routeIDs as $routeID)
    {
        $prev=$prevs[$i];
        mysql_query("UPDATE routes SET routepoint=routepoint+1 WHERE ".
                    "routepoint>$prev AND routeid=$routeID");
        mysql_query("INSERT INTO routes (routeid,fid,routepoint) VALUES ".
                "($routeID,$new,".($prev+1).")");
        $i++;
    }
    echo "insert";
    break;

  case "get":
    list($w,$s,$e,$n) = explode(",", $cleaned["bbox"]);
    header("Content-type: application/json");
    $curRoute = null;
    $routes = array();
    $lastrouteid = -1;
    $result=mysql_query
        ("SELECT r.routeid as routeid ,r.fid as fid,p.lat as lat,p.lon as lon ".
            "FROM routes r, panoramas p ".
            "WHERE r.fid=p.id AND ".
            "p.lat BETWEEN $s AND $n AND ".
            "p.lon BETWEEN $w AND $e ".
            "ORDER BY r.routeid,r.routepoint ");
    if(mysql_num_rows($result)>=1)
    {
    	$row=mysql_fetch_array($result);
        do
        {
            if($row['routeid'] != $lastrouteid)
            {
                $routes[$row['routeid']] = array(); 
                $lastrouteid = $row['routeid'];
            }
            $routes[$row['routeid']][] = array
                ("id"=>$row['fid'],"lat"=>$row['lat'],"lon"=>$row['lon']);
        }
        while($row=mysql_fetch_array($result));
    }
    echo json_encode($routes);
    break;

  case "delete":
    $result=mysql_query("SELECT * FROM routes0 WHERE id=$cleaned[id]");
    if(mysql_num_rows($result)==1)
    {
        $row=mysql_fetch_array($result);
        if($row['user']==$_SESSION['gatekeeper'])
        {
            mysql_query("DELETE FROM routes0 WHERE id=$cleaned[id]");
            mysql_query("DELETE FROM routes WHERE routeid=$cleaned[id]");
        }
        else
        {
            header("HTTP/1.1 401 Unauthorized");
        }
    }
    else
    {
        header("HTTP/1.1 404 Not Found");
    }
    break;

  case "getbearings":
       header("HTTP/1.1 503 Service Unavailable"); 
      /* kaput 
      $id = $_GET["id"];
    header("Content-type: text/xml");
    echo "<panorama>";
    $result=mysql_query("SELECT * FROM panoramas where id=$id"); 
    $row=mysql_fetch_array($result);
    echo "<direction>$row[direction]</direction>";
    $a = array("lastid","nextid");
    $result=mysql_query("SELECT * FROM routes WHERE fid=$id");
    echo "<neighbours>";
    while($row=mysql_fetch_array($result))
    {
        foreach($a as $k)
        {
            if($row[$k])
            {
                echo "<neighbour>";
                echo "<fid>$row[$k]</fid>";
                echo "<bearing>";
                echo get_bearing($id,$row[$k]);
                echo "</bearing>";
                echo "</neighbour>";
            }
        }
    }
    echo "</neighbours>";
    echo "</panorama>";
    */
  break;
}

mysql_close($conn);

function get_bearing($fid1,$fid2)
{
    $fids=array($fid1,$fid2);    
    $lats=array();
    $lons=array();
    for($i=0; $i<2; $i++)
    {
        $result=mysql_query("SELECT * FROM panoramas WHERE id=".$fids[$i]);
        $row=mysql_fetch_array($result);
        $lats[$i] = $row["lat"];
        $lons[$i] = $row["lon"];
        //echo "<lonlat$i>".$lons[$i]." ".$lats[$i]."</lonlat$i>";
    }

    $v= -((atan2 ($lats[1]-$lats[0], $lons[1]-$lons[0]) * (180.0 / M_PI))-90);
    return ($v < 0) ? $v+360: $v;
}

function insert_into_routes_table($routeid,$photoids)
{
    for($count=0; $count<count($photoids); $count++) 
    {
        $q="INSERT INTO routes (routeid,routepoint,fid) VALUES ".
                    "($routeid,$count,".$photoids[$count].")";
        mysql_query($q);
    }
}
?>
