<?php

// Rendered ways server
// Provides ways straight from the database, optimised for rendering
// (i.e. coordinates)

require_once('../lib/latlong.php');
require_once('../lib/functionsnew.php');

// fernhurst -80454 6596680

// Input:
// poi=[comma separated tag list]
// way=[comma separated tag list]
// bbox=[bbox, in latlon]

$conn=pg_connect("dbname=gis user=gis");
$cleaned = clean_input($_REQUEST);
$cleaned["format"] = (isset($cleaned["format"])) ? $cleaned["format"]:"xml";

/*
$x = $cleaned["x"];
$y = $cleaned["y"];
$z = $cleaned["z"];
$goog = new GoogleProjection();
$ = $goog->fromPixelToLL(
*/

$bbox = $cleaned["bbox"];
$values = explode(",",$bbox);

if(count($values)!=4 || $values[0]<-180 || $values[0] > 180 ||
           $values[2]<-180 || $values[2] > 180 ||
           $values[1]<-90 || $values[2] > 90 ||
           $values[3]<-90 || $values[3] > 90 ||
           $values[0] > $values[2] || $values[1] > $values[3])
{
    header("HTTP/1.1 400 Bad Request");
    exit;
}

$sw = ll_to_sphmerc($values[1],$values[0]);
$ne = ll_to_sphmerc($values[3],$values[2]);

$ctype=("Content-type: " .(($cleaned["format"]=="json") ? "application/json":
	"text/xml"));

header($ctype);

$plyrs = explode(",", $cleaned["poi"]);
$wlyrs = explode(",", $cleaned["way"]);


$first=true;
echo $cleaned["format"] == "json" ? "{\"poi\":[" :"<rdata>";
if(isset($cleaned["poi"]))
{
    $pqry = "SELECT *,(way).x,(way).y".
        " FROM planet_osm_point ".
        "WHERE (way).x BETWEEN $sw[e] AND $ne[e] AND ".
        "(way).y BETWEEN $sw[n] AND $ne[n] ";

    $pqry .= criteria($plyrs);



    $presult = pg_query($pqry);

    while($prow=pg_fetch_array($presult,null,PGSQL_ASSOC))
    {
		if($cleaned["format"] != "json")
		{
        	echo "<poi x='$prow[x]' y='$prow[y]'>";

        	foreach ($prow as $k=>$v)
        	{
            	if ($v !='' && $k!="x" && $k!="y" && $k!="way")
				{
					echo "<tag k='$k' v='$v' />";
				}
        	}
        	echo "</poi>";
		}
		else
		{
			if($first==true)
				$first=false;
			else
				echo ",";
			echo assocToJSON($prow);
		}	
    }

    pg_free_result($presult);
}

if($cleaned["format"]=="json")
	echo "], \"ways\":[";

if(isset($cleaned["way"]))
{
    $wqry = "SELECT *,astext(way)".
        " FROM planet_osm_line ".
        "WHERE (Centroid(way)).x BETWEEN $sw[e] AND $ne[e] AND ".
        "(Centroid(way)).y BETWEEN $sw[n] AND $ne[n] ";

    $wqry .= criteria($wlyrs);
    //echo "QUERY is $wqry <br/>";

    $wresult = pg_query($wqry);

	$first=true;

    while($wrow=pg_fetch_array($wresult,null,PGSQL_ASSOC))
    {
        $m=array();
        preg_match("/LINESTRING\((.+)\)/",$wrow['astext'],$m);
        $poi = explode(",", $m[1]);
		if($cleaned["format"]!="json")
		{
        	echo "<way>";
        	foreach ($poi as $point)
            	echo "<point>$point</point>";
        	foreach ($wrow as $k=>$v)
        	{
            	if ($v!='' && $k !="way" && $k!="astext" )
                	echo "<tag k='$k' v='$v' />";
        	}
			echo "</way>";
		}
		else
		{
			if($first==true)
				$first=false;
			else
				echo ","; // next way
			echo "{ \"points\" :["; // start the way and its points
			for($i=0; $i<count($poi); $i++)
			{
				if($i != 0)
					echo ",";
				
				list($easting,$northing)=explode(" ",$poi[$i]);
				echo " [ $easting, $northing ]";
			}
			echo "],"; // end its points
			echo "\"tags\":"; // start its tags
			echo assocToJSON($wrow);
			echo "}"; // end the way
		}
    }
    pg_free_result($wresult);
}

if($cleaned["format"]=="json")
	echo "] }"; // end whole json
else
	echo "</rdata>";

pg_close($conn);

function criteria($lyrs)
{
    $qry="";
    if (count($lyrs) != 0)
    {
        $qry .= "AND (";
        for($i=0; $i<count($lyrs); $i++)
        {
            if($i!=0)
                $qry.=" OR ";
            if ($lyrs[$i] == "natural")
                $lyrs[$i] = "\"natural\"";
            $qry .= $lyrs[$i] . " <> '' ";
        }
        $qry .= ")";
    }
    return $qry;
}

function assocToJSON($assoc)
{
	$json .= "{";
	$first=true;
	foreach($assoc as $k=>$v)
	{
		if($v!='' && $k!='way' && $k!='astext')
		{
			if($first==true)
				$first=false;
			else
				$json .= ",";
			$json .= "\"$k\" : \"$v\"";
		}
	}
	$json .= "}";
	return $json;
}
?>
