<?php

function add_new_marker ($lat, $lon, $type, $description, $userid)
{
	$q = 
		("INSERT INTO freemap_markers (userid,type,description,lat,lon)".
		 " VALUES ".
		 "('$userid','$type','$description','$lat','$lon')");
	echo "Doing query : $q";
	mysql_query($q) or die(mysql_error());
}

function edit_marker($id,$inp,$arr,$userid)
{
	$result = mysql_query("SELECT userid FROM freemap_markers WHERE id=$id");
	if (mysql_num_rows($result)==1)
	{
		$row=mysql_fetch_array($result);
		if($row['userid']==$userid || $row['userid']==0)
		{
			$relevant_inp = filter_array($inp,$arr);
			return edit_table("freemap_markers",$id,$relevant_inp,"id");
		}
	}
	return false;
}

function delete_marker($id, $userid)
{
	$result = mysql_query("SELECT userid FROM freemap_markers WHERE id=$id");
	if (mysql_num_rows($result)==1)
	{
		$row=mysql_fetch_array($result);
		if($row['userid']==$userid || $row['userid']==0)
		{
			delete_from_table("freemap_markers",$id,"id");
		}
	}
}


function add_new_walkroute ($lats, $lons, $title, $description, $userid,
							$private)
{
	$latarray = explode("," , $lats);
	$lonarray = explode("," , $lons);
	
	if(count($latarray) == count($lonarray))
	{

		mysql_query("INSERT INTO walkroutes ".
					"(userid,startlat,startlon,title,description,private)".
					"VALUES ".
					"($userid,$latarray[0],$lonarray[0],'$title',".
					"'$description',$private)")
				or die(mysql_error());
		$wrid = mysql_insert_id();



		for($count=0; $count<count($latarray); $count++)
		{
			mysql_query
				("INSERT INTO walkroutepoints (walkrouteid,lat,lon) VALUES ".
				 "($wrid,$latarray[$count],$lonarray[$count])") or die
				 	(mysql_error());
		}

		return $wrid;
	}
	return 0;	
}

function get_walkroute_by_id($id, $userid)
{
	$wr = null;
	$result = mysql_query
		("SELECT walkroutepoints.lat,walkroutepoints.lon, ".
		 "walkroutes.title,walkroutes.description,".
		 "walkroutes.startlat,walkroutes.startlon, walkroutes.id ".
		 "FROM walkroutes,walkroutepoints WHERE ".
		 "walkroutepoints.walkrouteid=walkroutes.id AND walkroutes.id=$id AND ".
		 "(walkroutes.userid=$userid OR walkroutes.private=0)");


	if(mysql_num_rows($result)>0)
	{
		while($row=mysql_fetch_assoc($result))
		{
			if($wr === null)
			{
				$wr = array();
				$wr['title'] = stripslashes($row['title']);
				$wr['description'] = 
					stripslashes($row['description']);
				$wr['startlon'] = $row['startlon'];
				$wr['startlat'] = $row['startlat'];
				$wr['id'] = $row['id'];
				$wr['points'] = array();
			}

			$curpoint = array();
			$curpoint['lat'] = $row['lat'];
			$curpoint['lon'] = $row['lon'];
			$wr['points'][] = $curpoint;
		}

		$result = mysql_query
		("SELECT walkrouteannotations.lat,walkrouteannotations.lon, ".
		 "walkrouteannotations.annotation ".
		 "FROM walkroutes,walkrouteannotations WHERE ".
		 "walkrouteannotations.walkrouteid=walkroutes.id AND ".
		 "walkroutes.id=$id AND ".
		 "(walkroutes.userid=$userid OR walkroutes.private=0)".
		 " ORDER BY walkrouteannotations.id");

		if(mysql_num_rows($result)>0)
		{
			$wr['annotations']=null;
			while($row=mysql_fetch_assoc($result))
			{
				if($wr['annotations']==null)
					$wr['annotations'] = array();
				$annotation = array();
				$annotation['lon'] = $row['lon'];
				$annotation['lat'] = $row['lat'];
				$annotation['annotation'] = $row['annotation'];
				$wr['annotations'][] = $annotation;
			}
		}
	}
	return $wr; 
}
	
function get_user_walkroutes($userid)
{
	$routes=array();
	$result=mysql_query
				("SELECT * FROM walkroutes WHERE userid=$userid");
	while($row=mysql_fetch_assoc($result))
	{
		foreach ($row as $field=>$value)
			$row[$field] = stripslashes($row[$field]);
		$routes[]=$row;
	}
	return $routes;
}

function show_user_walkroutes($walkroutes)
{
	$first=true;
	foreach ($walkroutes as $walkroute)
	{
		if(!$first)
			echo "<hr/>";
		else
			$first=false;
		echo "<h2>$walkroute[title]</h2>";
		echo "<p>$walkroute[description]</p>";
		echo "<p><a href='/freemap/index.php?lat=$walkroute[startlat]&".
			 "lon=$walkroute[startlon]>Map</a></p>";
	}
}

	
function annotate_walkroute_point($wrid,$lat,$lon,$annotation,$userid)
{
	$result = mysql_query("SELECT userid FROM walkroutes WHERE id=$wrid");
	if(mysql_num_rows($result)==1)
	{
		$row=mysql_fetch_assoc($result);
		if($row['userid']==$userid || $row['private']==0)
		{
			mysql_query
				("INSERT INTO walkrouteannotations ".
				"(walkrouteid,lat,lon,annotation) VALUES ".
				"($wrid,$lat,$lon,'$annotation')");
		}
	}
}

function edit_walkroute($id,$inp,$arr,$userid)
{
	$result = mysql_query("SELECT userid FROM walkroutes WHERE id=$id");
	if (mysql_num_rows($result)==1)
	{
		$row=mysql_fetch_array($result);
		if($row['userid']==$userid || $row['private']==0)
		{
			$relevant_inp = filter_array($inp,$arr);
			return edit_table("walkroutes",$id,$relevant_inp,"id");
		}
	}
	return false;
}

function delete_walkroute($id, $userid)
{
	$result = mysql_query("SELECT userid FROM walkroutes WHERE id=$id");
	if (mysql_num_rows($result)==1)
	{
		$row=mysql_fetch_array($result);
		if($row['userid']==$userid || $row['private']==0)
		{
			delete_from_table("walkroutes",$id,"id");
			delete_from_table("walkroutepoints",$id,"walkrouteid");
		}
	}
}

function get_markers($w, $s, $e, $n, $userid)
{
	$markers=array();
	$q = "select * from freemap_markers where lat between $s and $n ".
		 "and lon between $w and $e";
	$result = mysql_query($q) or die(mysql_error());
	if(mysql_num_rows($result))
	{
		while($row=mysql_fetch_assoc($result))
		{
			if($row['userid']==$userid || $row['userid']==0)
				$markers[] = $row;
		}
	}
	return $markers;
}
			

function to_georss($markers)
{
	header("Content-type: text/xml");
	echo "<?xml version='1.0'?>\n";
	echo "<rss version='2.0' xmlns:georss='http://www.georss.org/georss'>\n";
	echo "<channel>\n";

	foreach ($markers as $marker)
	{
		echo "<item>\n";
		if(isset($marker['title']) && $marker['title']!=null)
			echo "<title>$marker[title]</title>\n";
		$description=($marker['description']!=null 
							&& $marker['description']!="")
							? $marker['description'] : '';
		echo "<description>$description</description>\n";
		// according to the rss 2.0 spec the guid is a unique string
		// identifier for each item. so this is acceptable. it means
		// that slippy clients can easily tell whether an item has 
		// already been added as a marker.
		echo "<guid>$marker[id]</guid>\n";
		echo "<georss:point>$marker[lat] $marker[lon]</georss:point>\n";
		echo "<georss:featuretypetag>$marker[type]".
				 "</georss:featuretypetag>\n";
		echo "</item>\n";
	}
	echo "</channel>\n";
	echo "</rss>\n";
}

function get_walkroute_startpoints($w, $s, $e, $n, $userid)
{
	$walkroutes=array();
	$q = "select * from walkroutes where startlat between $s and $n ".
		 "and startlon between $w and $e";
	$result = mysql_query($q);
	if(mysql_num_rows($result))
	{
		while($row=mysql_fetch_assoc($result))
		{
			if($row['userid']==$userid || $row['private']==0)
			{
				$georss = array();
				$georss['lat'] =  $row['startlat'];
				$georss['lon'] = $row['startlon'];
				$georss['title'] = stripslashes($row['title']);
				$georss['description'] = 
					stripslashes($row['description']);
				$georss['type'] = 'walk';
				$georss['id'] =  $row['id'];
				$walkroutes[] = $georss;
			}
		}
	}
	return $walkroutes;
}

function walkroute_to_xml ($wr)
{
	echo "<walkroute>\n";
	echo "<id>$wr[id]</id>\n";
	echo "<title>$wr[title]</title>\n";
	echo "<description>".stripslashes($wr[description]).
		"</description>\n";
	echo "<startlat>$wr[startlat]</startlat>\n";
	echo "<startlon>$wr[startlon]</startlon>\n";
	echo "<points>\n";
	if(is_array($wr['points']))
		foreach ($wr['points'] as $point)
			echo "<point lat='$point[lat]' lon='$point[lon]' />\n";
	echo "</points>\n";

	if($wr['annotations']!=null)
	{
		echo "<annotations>\n";
		foreach($wr['annotations'] as $annotation)
		{
			echo "<annotation lat='$annotation[lat]' lon='$annotation[lon]'>\n";
			echo $annotation['annotation']."\n";
			echo "</annotation>\n";
		}
		echo "</annotations>\n";
	}

	echo "</walkroute>\n";
}

function walkroute_to_gpx($wr)
{
	echo "<gpx>\n";
	echo "<trk>\n";
	echo "<name>$wr[title]</name>\n";
	echo "<desc>$wr[description]</desc>\n";
	echo "<trkseg>\n";
	if (is_array($wr['points']))
		foreach ($wr['points'] as $point)
			echo "<trkpt lat='$point[lat]' lon='$point[lon]'></trkpt>\n";
	echo "</trkseg>\n";
	echo "</trk>\n";
	if($wr['annotations']!=null)
	{
		$count=1;
		foreach($wr['annotations'] as $annotation)
		{
			echo "<wpt lat='$annotation[lat]' lon='$annotation[lon]'>\n";
			echo "<name>".sprintf("%03d",$count++)."</name>\n";
			echo "</wpt>\n";
		}
	}
	echo "</gpx>\n";
}
	
function wholly_numeric($input)
{
	return preg_match("/^-?[\d\.]+$/",$input);
}

function get_mapview ($i)
{
		$mapviews = array (
			1 =>		array("id"=>1, "scale"=>0.3),
					array("id"=>2, "scale"=>0.6),
					array("id"=>3, "scale"=>1.2),
					array("id"=>4, "scale"=>3.125),
					array("id"=>5, "scale"=>6.25),
					array("id"=>6, "scale"=>12.5),
					array("id"=>7, "scale"=>25),
					array("id"=>8, "scale"=>50),
					array("id"=>9, "scale"=>100),
					array("id"=>10, "scale"=>200),
					array("id"=>11, "scale"=>400)
						);
		return $mapviews[$i];
}
# Might be breaking the law with this one. Global Megacorp has patented this
# algorithm. Well up yours I'm doing it anyway :-)
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
?>
?>
