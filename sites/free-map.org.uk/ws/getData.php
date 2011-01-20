<?php
define('HGTDIR','/var/www/data/hgt');
require_once("../lib/latlong.php");
require_once("../lib/functionsnew.php");

function getData($w,$s,$e,$n,$options)
{
$conn=pg_connect("dbname=gis user=gis");
$sw = ll_to_sphmerc($s,$w);
$ne = ll_to_sphmerc($n,$e);

$ctype=("Content-type: " .(($options["format"]=="json") ? "application/json":
	"text/xml"));

header($ctype);

$plyrs = explode(",", $options["poi"]);
$wlyrs = explode(",", $options["way"]);


$first=true;
echo $options["format"] == "json" ? "{\"poi\":[" :"<rdata>";

if(isset($options["poi"]))
{
    $pqry = "SELECT *,(way).x,(way).y".
        " FROM planet_osm_point ".
        "WHERE (way).x BETWEEN $sw[e] AND $ne[e] AND ".
        "(way).y BETWEEN $sw[n] AND $ne[n] ";

	if($options["poi"]!="all")
		$pqry .= criteria($plyrs);



    $presult = pg_query($pqry);

    while($prow=pg_fetch_array($presult,null,PGSQL_ASSOC))
    {
		if($options["format"] != "json")
		{
			$ll = sphmerc_to_ll($prow['x'],$prow['y']);
        	echo "<poi lat='$ll[lat]' lon='$ll[lon]'>";

        	foreach ($prow as $k=>$v)
        	{
            	if ($v !='' && $k!="x" && $k!="y" && $k!="way")
				{
					echo "<tag k='$k' v=\"".
						addslashes(htmlentities($v))."\" />";
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
			$ll = sphmerc_to_ll($prow['x'],$prow['y']);
			$prow['x'] = $ll['lon'];
			$prow['y'] = $ll['lat'];
			echo assocToJSON($prow);
		}	
    }

    pg_free_result($presult);
}

if($options["format"]=="json")
	echo "], \"ways\":[";

if(isset($options["way"]))
{
    $wqry = "SELECT *,astext(way)".
        " FROM planet_osm_line ".
        "WHERE (Centroid(way)).x BETWEEN $sw[e] AND $ne[e] AND ".
        "(Centroid(way)).y BETWEEN $sw[n] AND $ne[n] ";

	if($options["way"]!="all")
    	$wqry .= criteria($wlyrs);
    //echo "QUERY is $wqry <br/>";

    $wresult = pg_query($wqry);

	$first=true;

    while($wrow=pg_fetch_array($wresult,null,PGSQL_ASSOC))
    {
        $m=array();
        preg_match("/LINESTRING\((.+)\)/",$wrow['astext'],$m);
        $poi = explode(",", $m[1]);
		if($options["format"]!="json")
		{
        	echo "<way>";
        	foreach ($poi as $point)
			{
				list($easting,$northing) = explode(" ",$point);
				$ll = sphmerc_to_ll($easting,$northing);
            	echo "<point lat='$ll[lat]' lon='$ll[lon]' />";
		    }
        	foreach ($wrow as $k=>$v)
        	{
            	if ($v!='' && $k !="way" && $k!="astext" )
				{
                	echo "<tag k='$k' v=\"".addslashes(htmlentities($v)).
						"\"/>";
				}
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
				$ll = sphmerc_to_ll($easting,$northing);
				echo " [ $ll[lon], $ll[lat] ]";
			}
			echo "],"; // end its points
			echo "\"tags\":"; // start its tags
			echo assocToJSON($wrow);
			echo "}"; // end the way
		}
    }
    pg_free_result($wresult);
}

if($options["format"]=="json")
	echo "] }"; // end whole json
else
	echo "</rdata>";

pg_close($conn);
}

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


/* 
{ npts:
  nrects:
  rects:
  	 [ { lon: , lat:, w:, s:, e:, n: } , ... ]
}


*/

function getSRTM($w,$s,$e,$n)
{
	$npts = 1201;
	$step = 1/1200;
	$e = floatval($e);
	$s = floatval($s);
	$w = floatval($w);
	$n = floatval($n);
	$int_w = floor($w);
	$int_e = floor($e);
	$int_s = floor($s);
	$int_n = floor($n);

	$nrects = (($int_e-$int_w)+1) * (($int_n-$int_s)+1);

	//$str = pack("nn",($npts-1),$nrects);
	echo "{ \"npts\": $npts, "; // \"nrects\": $nrects, ";
	echo "\"rects\": [ ";

	for($lon=$int_w; $lon<=$int_e; $lon++)
	{
		for($lat=$int_s; $lat<=$int_n; $lat++)
		{
			$idx_w = ($lon==$int_w) ? floor(($w-floor($w))/$step) : 0;
			$idx_e = ($lon==$int_e) ? ceil(($e-floor($e))/$step) : $npts-1; 
			$idx_n = ($lat==$int_n) ? floor((ceil($n)-$n)/$step) : 0;
			$idx_s = ($lat==$int_s) ? ceil((ceil($s)-$s)/$step) : $npts-1;
			$width = ($idx_e - $idx_w) + 1;
			//echo "Indices $idx_w $idx_n $idx_e $idx_s<br/>";
			//$str .= pack("nnnnnn",$lon,$lat,$idx_w,$idx_n,$idx_e,$idx_s);
			if(!($lon==$int_w && $lat==$int_s))
				echo ",";
			echo " { \"lon\": $lon, \"lat\": $lat,";
			echo "\"box\": [ $idx_w, $idx_n, $idx_e, $idx_s ],";
			$file = sprintf(HGTDIR . "/%s%02d%s%03d.hgt", 
				($lat<0 ? "S":"N"),
				($lat<0 ? -$lat:$lat),
				($lon<0 ? "W":"E"),
				($lon<0 ? -$lon:$lon) );
			$fp = fopen($file,"r");
			//echo $file;
			if($fp)
			{
				echo "\"heights\": [ ";
				for($row=$idx_n; $row<=$idx_s; $row++)
				{
					fseek($fp,($row*$npts+$idx_w)*2);
					$bytes=fread($fp,$width*2);
					//echo $bytes;
					//$str .= $bytes;
					for($byte=0; $byte<$width*2; $byte+=2)
					{
						if(!($byte==0 && $row==$idx_n))
							echo ",";

						printf("%d", ord($bytes[$byte])*256+
								ord($bytes[$byte+1]));
					}
				}
				fclose($fp);
				echo "]";
			}
			echo "}";
		}
	}

	//echo $str;
	echo "]}";
}
?>
