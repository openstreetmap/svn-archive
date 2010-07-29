<?php
require_once("../lib/functionsnew.php");
$conn=dbconnect("otv");

switch($_GET['action'])
{
  case "add":

    $IDs = explode(",", $_REQUEST["panoIDs"]);

    $result=mysql_query("SELECT MAX(routeid) AS maxid FROM routes");
    $row=mysql_fetch_array($result);
    $routeid = $row["maxid"]+1;
    for($count=0; $count<count($IDs); $count++) 
    {
        $lastid =($count>0) ? $IDs[$count-1]: "NULL";
        $nextid =($count<count($IDs)-1) ? $IDs[$count+1]: "NULL";
        $q="INSERT INTO routes (routeid,fid,lastid,nextid) VALUES ".
                    "($routeid,".$IDs[$count].",$lastid,$nextid)";
        $op .= "$q ";
        mysql_query($q);
    }

    echo $op;
  break;

  case "getbearings":
    
      $id = $_GET["id"];
    header("Content-type: text/xml");
    echo "<panorama>";
    $result=mysql_query("SELECT * FROM panoramas where id=$id") or die(mysql_errror());
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
?>
