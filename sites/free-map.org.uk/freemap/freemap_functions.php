<?php
require_once('latlong.php');
require_once('functionsnew.php');

function add_marker($description,$type,$lat,$lon,$userid,$way,$pt)
{
	$q=("insert into freemap_markers ".
	   "(userid,description,type,lat,lon,way,point)".
	   "values ($userid,'$description','$type',$lat,$lon,$way,$pt)");
	pg_query($q);
	$result=pg_query("select currval('freemap_markers_id_seq') as lastid");
	$row=pg_fetch_array($result,null,PGSQL_ASSOC);
	return $row["lastid"]; 
}

function edit_marker($id,$inp,$arr,$userid)
{
	$result = pg_query("SELECT userid FROM freemap_markers WHERE id=$id");
	$row=pg_fetch_array($result,null,PGSQL_ASSOC);
	if ($row)
	{
		if($row['userid']==$userid || $row['userid']==0)
		{
			$relevant_inp = filter_array($inp,$arr);
			return edit_table("freemap_markers",$id,$relevant_inp,"id","pgsql");
		}
	}
	return false;
}

function get_marker_by_id($id)
{
	$result=pg_query ("SELECT * FROM freemap_markers WHERE id=$id");
	$row=pg_fetch_array($result,null,PGSQL_ASSOC);
	return $row; 
}

function get_markers($w, $s, $e, $n, $userid)
{
	$markers=array();
	$q = "select * from freemap_markers where lat between $s and $n ".
		 "and lon between $w and $e";
	$result = pg_query($q);
	while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
	{
		if($row['userid']==$userid || $row['userid']==0)
			$markers[] = $row;
	}
	return $markers;
}
			
function get_markers_by_category($searchterm,$searchby)
{
	$markers=array();
	$q="SELECT * FROM freemap_markers WHERE ";
	$first=true;

	$searchterm = (is_array($searchterm)) ? $searchterm:array($searchterm);
	$searchby = (is_array($searchby)) ? $searchby:array($searchby);
	for($count=0; $count<count($searchterm); $count++)
	{
		if($first==false)
			$q .= " AND ";
		else
			$first=false;
		$q .= $searchby[$count]. " = '".$searchterm[$count]."' ";
	}
	$q .= " ORDER BY id";
	$result=pg_query($q);	
	while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
	{
		$markers[] = $row;
	}
	return $markers;
}
		
function to_georss($markers)
{
	header("Content-type: text/xml");
	echo "<rss version='2.0' xmlns:georss='http://www.georss.org/georss'>\n";
	echo "<channel>\n";

	foreach ($markers as $marker)
	{
		if($marker['private']==0 || $marker['userid']==$id)
		{
			echo "<item>\n";
			$t = ($marker['title']) ? $marker['title']: " ";
			echo "<title>$t</title>\n";
			// Really annoying oversight when XML was developed - having
			// to encode & as &amp; is so irritating...
			if(file_exists("/home/www-data/uploads/photos/$marker[id].jpg"))
			{
				echo "<link>/freemap/api/markers.php?id=$marker[id]&amp;".
					"action=getPhoto</link>\n";
			}
			$description=($marker['description']!=null 
							&& $marker['description']!="")
							? $marker['description'] : 'no description';
			echo "<description>$description</description>\n";
			//echo "<description>description</description>\n";
			// according to the rss 2.0 spec the guid is a unique string
			// identifier for each item. so this is acceptable. it means
			// that slippy clients can easily tell whether an item has 
			// already been added as a marker.
			echo "<guid>$marker[id]</guid>\n";
			echo "<georss:point>$marker[lat] $marker[lon]</georss:point>\n";
			echo "<georss:featuretypetag>$marker[type]</georss:featuretypetag>".
				"\n";
			echo "</item>\n";
		}
	}
	echo "</channel>\n";
	echo "</rss>\n";
}

function markers_to_html($markers, $controls)
{
	?>
	<html>
	<head>
	<script type='text/javascript'>
	function reload()
	{
		var keys='', values='';
		if(document.getElementById('type').value!= '')
		{
			keys+='type';
			values += document.getElementById('type').value;
		}
		if(document.getElementById('county').value!= '')
		{
			if(keys!='')
			{
				keys += ",";
				values+=",";
			}
			keys += 'county';
			values += document.getElementById('county').value;
		}
		window.location= "/freemap/api/markers.php?action=getByCategory&q="+
					values+"&by="+keys+"&format=html";
	}
	</script>
	<link rel='stylesheet' type='text/css' href='/freemap/css/freemap2.css' />
	</head>
	<body>
	<?php
	if($controls==true)
	{
		?>
		<div>
		Filter by:
		<label for='type'>Type </label>
		<select id='type'>
		<option value=''>all</option>
		<option>hazard</option>
		<option>directions</option>
		<option>info</option>
		</select>
		<label for='county'>County </label>
		<select id='county'>
		<option value=''>all</option>
		<option>Hampshire</option>
		<option>Wiltshire</option>
		<option>West Sussex</option>
		<option>Surrey</option>
		</select>
		<input type='button' value='Go!' onclick='reload()' />
		</div>
		<div>
		<?php
	}
	echo "<table>\n<th>ID</th><th>Type</th><th>County</th><th>Description</th>";
	foreach ($markers as $marker)
	{
		echo "<tr>\n";
		echo "<td><a href='/freemap/api/".
			"markers.php?action=getById&format=html&id=$marker[id]".
			"'>$marker[id]</a></td>\n";
		echo "<td>$marker[type]</td>\n";
		echo "<td>$marker[county]-</td>\n";
		echo "<td>$marker[description]</td>\n";
		echo "</tr>\n";
	}
	echo "</table>\n";
	echo "</div>\n";
	echo "</body>\n</html>\n";
}

function marker_to_html($marker)
{
	?>
	<html>
	<head>
	<title><?php echo $marker['id']; ?></title>
	<script type='text/javascript' src='/freemap/javascript/basicmap.js'>
	</script>
	<script type='text/javascript' 
	src='http://www.openlayers.org/api/2.5/OpenLayers.js'> </script>
	<script type='text/javascript' src='/freemap/javascript/lib/converter.js'>
	</script>
	<script type='text/javascript' src='/freemap/javascript/lib/get_osm_url.js'>
	</script>
	<link rel='stylesheet' type='text/css' href='/freemap/css/freemap2.css' />
	</head>
	<?php
	echo "<body onload='loadMap($marker[lon],$marker[lat],false)'>\n";
	echo "<h1>".ucfirst($marker['type']).
		" at coordinates $marker[lat],$marker[lon]</h1>\n";
	echo "<p>$marker[description]</p>\n";
	if(file_exists("/var/www/freemap/images/photos/$marker[id].png"))
	{
		echo "<p><img src='/freemap/images/photos/$marker[id].png' ".
		 "alt='Freemap annotation $id' /></p>";
	}
	echo "<div id='map'></div>\n";
	echo "<p><a href='/freemap/index.php'>Back to map</a></p>\n";
	echo "</body></html>\n";
}

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


function delete_marker($guid)
{
	$result=pg_query("SELECT * FROM freemap_markers WHERE id=$guid");
	$row=pg_fetch_array($result,null,PGSQL_ASSOC);
	if($row)
	{
		$q = "delete from freemap_markers where id=$guid";
		pg_query($q);
		return true;
	}
	return false;
}

function get_markers_by_bbox($bbox)
{
	if($bbox=="all")
	{
	$q = "select * from freemap_markers";
	}
	else
	{
	list($bllon,$bllat,$trlon,$trlat) = explode(",",$bbox);

	$q = "select * from freemap_markers where lat between $bllat and $trlat ".
		 "and lon between $bllon and $trlon";
	}

	$markers=array();


	$result = pg_query($q) ;
	while($row = pg_fetch_array($result,null,PGSQL_ASSOC))
	{
		$markers[]=$row;
	}
	return $markers;
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

// find the nearest highway (by ID) to a latlon
// note need to be connected to the PGSQL database first
// 50 seems bit low, up to 100. Difficult to tell with these Mercatastic 
// units TBH....
function get_nearest_highway($lat,$lon,$fulldetails=false,$mindist=100)
{
	$id=0;
	$nearest_pt=0;
	$merc=ll_to_merc($lat,$lon);
	$q=("SELECT osm_id, AsText(way),  ".
                "Distance(GeomFromText('POINT($merc[e] $merc[n]".
				")',4326),way) as dist FROM ".
                "planet_osm_line WHERE Distance(GeomFromText('POINT(".
				"$merc[e] $merc[n])',".
                "4326),way)<$mindist AND highway != '' ORDER BY dist LIMIT 1");
	$result=pg_query($q);

	if($row=pg_fetch_array($result,null,PGSQL_ASSOC))
	{
		$id=$row['osm_id'];
		if($fulldetails==true)
		{
			$m=array();
			preg_match("/LINESTRING\((.+)\)/",$row['astext'],$m);
			$points = explode(",", $m[1]);
			$shortest=99999999;
			for($count=0; $count<count($points); $count++)
			{
				$p=explode(" ",$points[$count]);
				$len=line_length($merc['e'],$merc['n'],$p[0],$p[1]);
				if($len<$shortest)
				{
					$shortest=$len;
					$nearest_pt = $count;
				}
			}
		}
	}
	pg_free_result($result);
	return array($id,$nearest_pt);
}

function get_way_segment($x,$y,$mindist=100)
{
	$q=("SELECT osm_id, AsText(way),  ".
			"line_locate_point(way,PointFromText('POINT($x $y)',4326))".
			"AS posn ".
                "FROM planet_osm_line WHERE Distance(GeomFromText('POINT(".
				"$x $y)',".
                "4326),way)<$mindist AND highway != '' ORDER BY ".
				"Distance(GeomFromText('POINT($x $y)',4326),way) LIMIT 1");
	$result=pg_query($q);

	if(!($row=pg_fetch_array($result,null,PGSQL_ASSOC)))
		return null;
	$way=$row['osm_id'];
	$posn=$row['posn'];
	$prev_before=1;
	$prev_after=1;

	$w=get_annotated_way($way);

	// Find all intersecting ways along the nearest way, and their 
	// distances along the way
	$q = "SELECT t2.highway,t2.osm_id as wayid,".
		"line_locate_point(t1.way,intersection(t1.way,t2.way)) as posn,".
		"astext(intersection(t1.way,t2.way)) as intn ".
		"from planet_osm_line t1, planet_osm_line t2 ".
		"where intersects(t1.way,t2.way) ".
		"and GeometryType(intersection(t1.way,t2.way))='POINT' ".
		"and t1.highway!='' and t2.highway!='' and ".
		"t1.osm_id=$way and t2.osm_id != $way";
	$result=pg_query($q);
	$before_point=null;
	$after_point=null;

	// Find the two intersections enclosing the supplied point
	// this seems a bit cumbersome - is there a better way to do this?
	$count=0;
	while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
	{
		$before=$posn-$row['posn'];
		$after=$row['posn']-$posn;
		if($before<$prev_before && $before>=0)
		{
			$prev_before=$before;
			$before_point=$row['intn'];
		}
		if($after<$prev_after && $after>=0)
		{
			$prev_after=$after;
			$after_point=$row['intn'];
		}
	}

	// return null if we couldn't find two intersections enclosing 
	// the supplied point
	if($before_point==null || $after_point==null)
		return null;

	preg_match("/POINT\((.+)\)/",$before_point,$m);
	$before_point=$m[1];
	preg_match("/POINT\((.+)\)/",$after_point,$m);
	$after_point=$m[1];

	
	$index_start=-1;
	$index_end=-1;
	for($count=0; $count<count($w['points']); $count++)
	{
		if($w['points'][$count]==$before_point)
		{
			$index_start=$count;
		}
		if($w['points'][$count]==$after_point)
		{
			$index_end=$count;
		}
	}

	$segment=array();
	$tags=array("osm_id","highway","foot","horse","description");
	foreach($tags as $tag)
	{
		if(isset($w[$tag]))
			$segment[$tag]=$w[$tag];
	}
	$segment['startpoint']=$index_start;
	$segment['endpoint']=$index_end;
	$segment['points']=array();
	$segment['annotations']=array();
	for($count=$index_start; $count<=$index_end; $count++)
		$segment['points'][] = $w['points'][$count];
	foreach($w['annotations'] as $a)
	{
		if($a['point']>=$index_start && $a['point']<=$index_end)
			$segment['annotations'][] = $a;
	}
	return $segment;
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

// get the annotations along a way 
function get_annotations($way,$photos_only=false)
{
	$annotations=array();
	$result=pg_query("SELECT * FROM freemap_markers WHERE way=$way ".
						"ORDER BY point");
	while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
	{
		if($photos_only==false ||
			file_exists("/home/www-data/uploads/photos/$row[id].jpg"))
		{
			$annotations[]=$row;
		}
	}

	return $annotations;
}

function get_annotated_way($id,$start=null,$end=null,$do_annotations=true)
{
	$way=null;
	$q=
			("SELECT osm_id, name,".
			"AsText(way),foot,horse,highway,description ".
            "FROM ".
            "planet_osm_line WHERE osm_id=$id");
	$result=pg_query($q);

	$row=pg_fetch_array($result,null,PGSQL_ASSOC);
	if($row)
	{
				$way=array();
				$tags = array("osm_id",
							 "name","description","highway","foot","horse");
				foreach($tags as $tag)
				{
					if($row[$tag]!='')
						$way[$tag] = $row[$tag];
				}
				$way['points'] = array();
                preg_match("/LINESTRING\((.+)\)/",$row['astext'],$m);
                $points = explode(",", $m[1]);
				$start1 = ($start===null) ? 0 : $start;
				$end1 = ($end===null) ? count($points)-1 :  $end;
				$step = ($end1<$start1) ? -1 : 1;  
				for($count=$start1; $count!=$end1+$step; $count+=$step)
					$way['points'][] = $points[$count];
				if($do_annotations==true)
					$way['annotations']=get_way_annotations($id,$start1,$end1);

	}
	return $way;
}

function get_way_annotations($id,$start=null,$end=null)
{
	$q = "SELECT * FROM freemap_markers WHERE way=$id";

	if ($start!==null && $end!==null)
	{
		$q .= " AND point BETWEEN ".min($start,$end)." AND ".
		 	max($start,$end). " ORDER BY point";
		if($start>$end)
			$q .=" DESC";
	}
	else
	{
		$q .= " ORDER BY point";
	}

	$annotations = array();
	$result2=pg_query($q);
	while($row2=pg_fetch_array($result2,null,PGSQL_ASSOC))
	{
		$annotations[] = $row2;
	}
	return $annotations;
}

function annotated_way_to_xml($way)
{
	echo "<way>\n";
	echo "<osm_id>$way[osm_id]</osm_id>\n";
	$tags = array("name","description","highway","foot","horse",
					"startpoint","endpoint");
	foreach($tags as $tag)
	{
		if($way[$tag])
			echo "<$tag>$way[$tag]</$tag>\n";
	}
	foreach ($way['points'] as $point)
		echo "<point>$point</point>\n";

	way_annotations_to_xml($way['annotations']);
	echo "</way>\n";
}

function way_annotations_to_xml($annotations)
{
	foreach($annotations as $annotation)
	{
		echo "<annotation>\n";
		echo "<id>$annotation[id]</id>\n";
		echo "<type>$annotation[type]</type>\n";
		echo "<description>$annotation[description]</description>\n";
		echo "<lat>$annotation[lat]</lat>\n<lon>$annotation[lon]</lon>\n";
		if(file_exists("/var/www-data/uploads/photos/$annotation[id].png"))
		{
			echo "<link>http://www.free-map.org.uk/freemap/api/markers.php".
				 "?action=getPhoto&id=$annotation[id]</link>\n";
		}
		echo "</annotation>\n";
	}
}

function annotated_way_to_html($way)
{
	?>
	<html>
	<head>
	<title><?php echo $wr["title"]; ?></title>
	<link rel='stylesheet' type='text/css' href='/freemap/css/freemap2.css' />
	<script type='text/javascript' src='/freemap/javascript/basicmap.js'>
	</script>
	<script type='text/javascript' 
	src='http://www.openlayers.org/api/2.5/OpenLayers.js'> </script>
	<script type='text/javascript' src='/freemap/javascript/lib/converter.js'>
	</script>
	<script type='text/javascript' src='/freemap/javascript/lib/get_osm_url.js'>
	</script>
	<script type='text/javascript' 
	src='/freemap/javascript/prototype/prototype.js'>
	</script>
	</head>
	<?php
	echo "<body onload='loadWay($way[osm_id])'>\n";
	echo "<h1>$way[osm_id]</h1>\n";
	$tags = array("name","description","highway","foot","horse");
	echo "<ul>\n";
	foreach($tags as $tag)
	{
		if($way[$tag])
			echo "<li>$tag : $way[$tag]</li>\n";
	}
	echo "</ul>\n";
	echo "<ol>\n";
	foreach($way['annotations'] as $annotation)
		echo "<li>$annotation[description]</li>\n";
	echo "</ol>\n";
	echo "<div id='map'></div>\n";
	echo "</body></html>\n";
}

function render_tiles($im,$lat,$lon,$zoom,$width,$height)
{
            $nTileCols = 2+floor($width/256);
            $nTileRows = 2+floor($height/256);
        	$topLeftX = lonToX($lon,$zoom)-$width/2;
        	$topLeftY = latToY($lat,$zoom)-$height/2;
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
						$filename="http://www.free-map.org.uk/".
							"cgi-bin/render2?x=".
							($topLeftXTile+$col).
							"&y=".
							($topLeftYTile+$row).
							"&z=".$zoom;
                        $tile=ImageCreateFromPNG($filename);
						ImageCopy($im,$tile,$curX,$curY,0,0,256,256);
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
?>
