<?php
require_once('../lib/latlong.php');
require_once('../lib/functionsnew.php');

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

function get_annotated_way($id,$do_annotations=true)
{
	$way=null;
	$q=
			("SELECT osm_id, name,".
			"AsText(way),foot,horse,highway,designation ".
            "FROM ".
            "planet_osm_line WHERE osm_id=$id");
	$result=pg_query($q);
	$row=pg_fetch_array($result,null,PGSQL_ASSOC);
	if(!$row)
		return null;
	return do_get_annotated_way($row,$do_annotations);
}

function do_get_annotated_way($row,$do_annotations=true)
{
		$way=array();
		$way["annotations"] = array();
		$tags = array("osm_id",
					 "name","highway","foot","horse",
					 "designation");
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
		{
			$result2=pg_query
				("SELECT * FROM annotations WHERE wayid=$row[osm_id] ORDER BY annotationid");
			while($row2=pg_fetch_array($result2,NULL,PGSQL_ASSOC))
				$way["annotations"][] = $row2;
		}

	return $way;
}


function annotated_way_to_xml($way)
{
echo "<way>\n";
echo "<osm_id>$way[osm_id]</osm_id>\n";
$tags = array("name","highway","foot","horse",
			"designation");
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
echo "<annotation id='$annotation[annotationid]' seg='$annotation[seg]' wayid='$annotation[wayid]' x='$annotation[x]' y='$annotation[y]'>$annotation[text]</annotation>\n";
}
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
