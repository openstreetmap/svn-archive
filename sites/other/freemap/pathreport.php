<?php
require_once('ajaxfunctions.php');
require_once('defines.php');

// AJAX server script to add or retrieve path reports
// Input: latitude and longitude of a clicked point, and reports (for add)
// The way nearest the point is looked up

session_start();

$conn = mysql_connect("localhost",DB_USERNAME,DB_PASSWORD);
mysql_select_db(DB_DBASE);

$params = array ("action","lat","lon","report", "limit");

foreach ($params as $param)
{
	$cleaned[$param] = (isset($_REQUEST[$param])) ?
		mysql_real_escape_string($_REQUEST[$param]) : '';
}

$way = nearest_way ($cleaned['lat'],$cleaned['lon'],$cleaned['limit']);
if($way)
{
	switch($cleaned['action'])
	{
		case "add":
			$q="insert into pathreports (id,report) values ".
			"($way,'$cleaned[report]')";
			mysql_query($q);
			break;

		case "get":
			$msg = "Way no. $way: "; // . getwaytags($way);
			$q = "select * from pathreports where id=$way";
			$result=mysql_query($q);
			if(mysql_num_rows($result))
			{
				while ($row=mysql_fetch_array($result))
					$msg .= $row['report']. "; ";
			}
			echo $msg;
			break;
	}
}

mysql_close($conn);

function nearest_seg($lat, $lon, $limit)
{
	//echo "lat $lat lon $lon <br/>";

	$mindist = 1000000;
	$nearestseg = 0;

	$q = "select s.id, a.latitude as alat, a.longitude as alon, b.latitude as blat, b.longitude as blon from segments as s, nodes as a, nodes as b where s.node_a=a.id and s.node_b = b.id and b.latitude between $lat-$limit and $lat+$limit and b.longitude between $lon-$limit and $lon+$limit and a.latitude between $lat-$limit and $lat+$limit and a.longitude between $lon-$limit and $lon+$limit";

	//echo $q;

	$result = mysql_query($q) or die(mysql_error());

	if(mysql_num_rows($result))
	{
		while($row=mysql_fetch_array($result))
		{
			// same algorithm as in osmeditor2
			if(($dist = distp($lon,$lat,$row['alon'],$row['alat'],
								$row['blon'],$row['blat'])) < $mindist)
			{
				$mindist = $dist;
				$nearestseg = $row['id'];
			}
		}

		return $nearestseg;
	}
	return 0;
}

function nearest_way($lat,$lon,$limit)
{
	$seg = nearest_seg($lat,$lon,$limit);
	//echo "Nearest segment: $seg<br/>";
	if($seg)
	{
		$q = "select * from way_segments where segment_id=$seg limit 1";
		//echo "Way Query : $q <br/>";
		$result=mysql_query($q);
		if(mysql_num_rows($result))
		{
			$row=mysql_fetch_array($result);
			//echo "Found way $row[id]<br/>";
			return $row['id'];
		}
	}
	return 0;
}

function distp($px,$py,$x1,$y1,$x2,$y2)
{
	$u = (($px-$x1)*($x2-$x1)+($py-$y1)*($y2-$y1)) / 
		(pow($x2-$x1,2)+pow($y2-$y1,2));
	$xintersection = $x1+$u*($x2-$x1);
	$yintersection = $y1+$u*($y2-$y1);
	return ($u>=0 && $u<=1 ) ? dist($px,$py,$xintersection,$yintersection):
						999999;
}

function dist ($x1, $y1, $x2, $y2)
{
	$dx = $x1-$x2;
	$dy = $y1-$y2;
	return sqrt($dx*$dx + $dy*$dy);
}

function getwaytags($id)
{
	  	$sql= "select k,v from way_tags where id = $id";
		
      	$result = mysql_query($sql);
      	$tags = ""; 
	  	while($row=mysql_fetch_array($result))
			$tags .= "$row[k]=$row[v];";	

		return $tags;
}
?>
