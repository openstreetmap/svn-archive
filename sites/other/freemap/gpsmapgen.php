<?php

require_once('dataset.php');
require_once('rules.php');
require_once('latlong.php');
require_once('functions.php');
require_once('defines.php');

class GPSMapGen
{
	var $mapdata, $styleRules, $w, $s, $e, $n;

	function GPSMapGen($w, $s, $e, $n, $z, $hwzl)
	{
		$this->zooms = $z;
		$this->hwzoom_limits = $hwzl;

		$this->w = $w;
		$this->s = $s;
		$this->e = $e;
		$this->n = $n;
		//echo "COORDS:$w $s $e $n<br/>";
		////print_r($this->mapdata);
		$this->styleRules = readStyleRules("freemap.xml");

		$conn = mysql_connect("localhost",DB_USERNAME,DB_PASSWORD);
		mysql_select_db(DB_DBASE);

		$this->mp_write_head();
		$segclause = $this->nodes();
		$segids = $this->segments($segclause);
		$this->ways($segids);

		mysql_close($conn);
	}

	// 160906 change to read each node from the database.
	// whinges about memory if you try and load all the data in and
	// then do further processing
	function nodes()
	{
		$clause =  null;

		// 070406 changed 'type' to ['tags']['class'] for nodes
		//$ids = array_keys($this->mapdata->nodes);
		//foreach ($this->mapdata->nodes as $id=>$node)

		$sql = "select id,latitude,longitude,visible,tags from ".
			   "nodes where latitude between ".
			   $this->s. " and ". $this->n. " and ".
			   "longitude between ". $this->w . " and ". $this->e;

		//echo "SQL is $sql";

		$result = mysql_query($sql);
		while($row=mysql_fetch_array($result))
		{
			$tags = $this->get_tag_array($row["tags"]);
			$style = getStyle($this->styleRules, $tags);
			//echo "NODE TAGS:";
			//print_r($tags);
			//echo "\n";
			//echo "NODE STYLE:";
			//print_r($style);
			// echo "\n";

			if(!$clause)
				$clause = $row['id'];
			else
				$clause .= "," .$row['id'];

			$zoomlimit = $this->zooms
							[$this->hwzoom_limits[$style['gpstype']]];
			//echo "Zoomlimit: $zoomlimit\n";
			//echo "Hwzoomlimits:";
			//print_r($this->hwzoom_limits);
			if($this->hwzoom_limits[$style['gpstype']]!=0)
			{
				$this->writePOI($row["latitude"], $row["longitude"], 
								$tags["name"], 
								$style["gpsmapcode"], $zoomlimit);
			}
		}
		return $clause;
	}

	function segments($clause)
	{
		$segs = array();
		$sql = "SELECT id FROM segments ".
				"WHERE node_a IN ($clause) OR node_b IN ($clause)";
		//echo "\nSegment SQL is $sql\n";
		$r = mysql_query($sql); 
		while($row=@mysql_fetch_array($r))
			$segs[] = $row["id"];

		return $segs;
	}	
			

	function mp_write_head()
	{
		echo("[IMG ID]\n");
		echo("ID=65536\n");
		echo("Name=Map1\n");	
		echo("TreSize=8000\n");	
		echo("RgnLimit=1024\n");	
		echo("Levels=".(count($this->zooms)+1)."\n");
	
		foreach ($this->zooms as $hwzoom=>$zoom)
			echo("Level".$zoom."=".$hwzoom."\n");

		echo("Level".count($this->zooms)."=".($hwzoom-1)."\n");
	
		echo("[END]\n\n");
	}

	function writePOI($lat,$lon,$name,$hexcode,$zoomlimit)
	{
		echo("[POI]\n");
		echo("Type=$hexcode\n");
		echo("Label=$name\n");
		echo("EndLevel=$zoomlimit\n");
		echo("Data0=");
		$this->mp_write_coord($lat,$lon);
		echo("\n");
		echo("[END]\n\n");
	}

	function mp_write_poly_start($name,$hexcode,$zoomlimit,$type)
	{
		echo("[$type]\n");
		echo("Type=$hexcode\n");
		if($name)
			echo("Label=$name\n");
		echo("EndLevel=$zoomlimit\n");
		echo("Data0=");
	}

	function mp_write_coord($lat,$lon)
	{
		echo("($lat,$lon)");
	}

	function ways($segment_ids)
	{
		$way = null;
		$type="way";
		$segment_clause = "(".implode(",",$segment_ids).")";
         
		 /* This can be simplified enormously for our purposes */

		$sql = "select id from ${type}_segments where segment_id in ".
				"${segment_clause} group by id";
		$result=mysql_query($sql); 
		while($row=@mysql_fetch_array($result))
		{
			$way = $this->get_way($row["id"]); 
			//echo "Way $row[id]\n";
			$waynodes = array();
			$done = array();
			$first=true;
			$waynodes_i = array();

			$segcount = 0;
			/*
			foreach ($way["segs"] as $seg)
			{
				$sql2 = "select a.id as a_id, a.latitude as a_lat, ".
						"a.longitude as a_lon, b.latitude as b_lat, ".
						"b.longitude as b_lon, b.id as b_id ".
					"from nodes as a, nodes as b, segments as s ".
					"where s.id=$seg and s.node_a=a.id and s.node_b=b.id ";

				$result2 = mysql_query($sql2) or die (mysql_error());
				$row2 = mysql_fetch_array($result2);

				$waynodes_i[] = array ("lat"=>$row2['a_lat'],
									   "lon"=>$row2['a_lon'],
									   "seg"=>$segcount);
				$waynodes_i[] = array ("lat"=>$row2['b_lat'],
									   "lon"=>$row2['b_lon'],
									   "seg"=>$segcount);
				$segcount++;
			}

			

			// is first segment wrong way round?
			$wrong_first_seg = false;
			for($count=2; $count<count($waynodes_i); $count++)
			{
				if(abs($waynodes_i[0]['lat']-$waynodes_i[$count]['lat'])
				<0.000001
			    && abs
				($waynodes_i[0]['lon']-$waynodes_i[$count]['lon'])
				<0.000001 )
				{
					$wrong_first_seg = true;
					break;
				}
			}

			if($wrong_first_seg)
			{
				$waynodes[] = $waynodes_i[1];
				$waynodes[] = $waynodes_i[0];
			}
			else
			{
				$waynodes[] = $waynodes_i[0];
				$waynodes[] = $waynodes_i[1];
			}
			
			$crap_way = false;
			for($count=2; $count<count($waynodes_i) && !$crap_way; $count++)
			{
				$incl = true;
				for($count2=0; $count2<$count; $count2++)
				{
					if(abs($waynodes_i[$count2]['lat']-
						 $waynodes_i[$count]['lat'])<0.000001
			    	&& abs ($waynodes_i[$count2]['lon']-
						$waynodes_i[$count]['lon'])<0.000001 )
					{
						$incl = false;

						// if the matching node comes from a segment more than
						// one different in segment order from the current
						// segment, and we're the first node in the segment,
						// terminate this way. Bit drastic maybe but the only
						// way that we can prevent ways consisting of an
						// *unordered* list of segments disrupting the GPS map.
						//$seg1 = $waynodes_i[$count]['seg'];
						//$seg2 = $waynodes_i[$count2]['seg'];
						//if($seg1-$seg2 > 1) $crap_way = true;
						break;

					}
				}
				if($incl)
					$waynodes[] = $waynodes_i[$count];
			}
			*/
					

			//echo "WAYNODES:";
			//print_r($waynodes);
			//echo "\n";

			$way["style"] = getStyle($this->styleRules, $way["tags"]);
			
			if($this->hwzoom_limits[$way['style']['gpstype']]!=0)
			{
				//echo "yes<br/>";
				$zoomlimit = $this->zooms
							[$this->hwzoom_limits[$way['style']['gpstype']]];
				$lastid = $id;

				/* NOT BY SEG
				$this->mp_write_poly_start
					($way['tags']['name'],$way['style']['gpsmapcode'],
							$zoomlimit, "POLYLINE"); 
				$first=true;

				foreach($waynodes as $waynode)
				{
					if(!$first)
						echo ",";
					else
						$first=false;

					$this->mp_write_coord($waynode['lat'],$waynode['lon']);
				}
				echo("\n[END]\n\n");
				NOT BY SEG END */

				/* BY SEG */
				foreach ($way["segs"] as $seg)
				{
					$this->mp_write_poly_start
					($way['tags']['name'],$way['style']['gpsmapcode'],
							$zoomlimit, "POLYLINE"); 

					$sql2 = "select a.id as a_id, a.latitude as a_lat, ".
						"a.longitude as a_lon, b.latitude as b_lat, ".
						"b.longitude as b_lon, b.id as b_id ".
					"from nodes as a, nodes as b, segments as s ".
					"where s.id=$seg and s.node_a=a.id and s.node_b=b.id ";

					$result2 = mysql_query($sql2) or die (mysql_error());
					$row2 = mysql_fetch_array($result2);

					$this->mp_write_coord($row2['a_lat'],$row2[a_lon]);
					echo ",";
					$this->mp_write_coord($row2['b_lat'],$row2[b_lon]);
					echo("\n[END]\n\n");
				} 
				/* BY SEG... END */
			}
		}
	}

	// Adapted from same function in dao.rb

	function get_way($way_id,$version=null) 
	{
		$way = array(); 
		$type="way";

	  	$sql= "select k,v from ${type}_tags where id = $way_id";
		
		// again remove version stuff and ".  "version = $version";

      	$result = mysql_query($sql);
      	$way["tags"] = array();
	  	while($row=mysql_fetch_array($result))
			$way["tags"][$row["k"]]=$row["v"];
	

		$result = mysql_query("select segment_id as n from ".
							 "${type}_segments where id = $way_id ".
							 "order by sequence_id");


	  	$way["segs"]=array();
	  	while($row=mysql_fetch_array($result))
	  		$way["segs"][] = $row["n"];

		return $way;
	}

	function get_tag_array($tagstring)
	{
		$tags=array();
		$t = explode(";",$tagstring);
		foreach ($t as $t1)
		{
			$t2 = explode("=",$t1);
			if($t2 && $t2[0] && $t2[0]!="")
				$tags[$t2[0]] = $t2[1];
		}
		return $tags;
	}
}
?>
