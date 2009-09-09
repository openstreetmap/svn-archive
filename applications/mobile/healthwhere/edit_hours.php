<?php
require_once ("inc_head.php");
require_once ("inc_edit.php");

$id = (int) $_GET ["id"];
$dist = (float) $_GET ["dist"];

/* Parameters:
 * $open_hours: string from opening_hours tag
 * $daynum: number of the day to be checked
 * $hours: array of times. This will be modified */
function OpenTimes ($open_hours, $daynum, &$hours) {
	//Set default
	$hours ['amStart'] = "";
	$hours ['amEnd'] = "";
	$hours ['pmStart'] = "";
	$hours ['pmEnd'] = "";
	//Set up array of days as numbers - easier to compare as numbers
	$weekdays = array ("mo"=>1, "tu"=>2, "we"=>3, "th"=>4, "fr"=>5, "sa"=>6, "su"=>7);

	//Ensure $open_hours is lower case - makes life simpler
	$open_hours = strtolower ($open_hours);
	//split by semi-colons: each one is a set of days & times
	$days = explode (";", trim ($open_hours));
	//Check each set of days/times
	foreach ($days as $day) {
		$daytimes = explode (" ", trim ($day));
		//Turn $daytimes [0] into start & end days
		$dayrange = explode ("-", $daytimes [0]);
		$minday = $weekdays [$dayrange [0]];
		if (count ($dayrange) == 1)
			//Single day, so maxday is same as minday
			$maxday = $weekdays [$dayrange [0]];
		else
			//Two days
			$maxday = $weekdays [$dayrange [1]];
		//Determine if today is in the range
		if ($daynum >= $minday && $daynum <= $maxday) {
			//Today is in the day range - check the time
			$times = explode (",", $daytimes [1]);
			//Day off - specific case. Return from function immediately
			if ($times [0] == "off") {
				$hours ['amStart'] = "";
				$hours ['amEnd'] = "";
				$hours ['pmStart'] = "";
				$hours ['pmEnd'] = "";
				return;
			}
			if ($daynum == $minday && $daynum == $maxday) {
				$am = explode ("-", $times [0]);
				$hours ['amStart'] = $am [0];
				$hours ['amEnd'] = $am [1];
				$pm = explode ("-", $times [1]);
				$hours ['pmStart'] = $pm [0];
				$hours ['pmEnd'] = $pm [1];
				// This is a specific day, so return immediately
				return;
			}
			else {
				$am = explode ("-", $times [0]);
				$hours ['amStart'] = $am [0];
				$hours ['amEnd'] = $am [1];
				$pm = explode ("-", $times [1]);
				$hours ['pmStart'] = $pm [0];
				$hours ['pmEnd'] = $pm [1];
			}
		}
	}
	return;
}

// Get data from OSM
$sXML = file_get_contents ("http://www.openstreetmap.org/api/0.6/node/$id");
$xml = new SimpleXMLElement($sXML);

$days = array ("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su");
//Calculate opening_hours value based on submitted data. This isn't perfect,
//but should at least generate a valid string
$am = array ();
$hours = "";
if (isset ($_POST ["btnSubmit"])) {
	if (isset ($_POST ["chk247"]))
		$hours = "24/7";
	else {
		//Put each day into an array element
		for ($iCount = 0; $iCount <= 6; $iCount++) {
			$am [$iCount] = $_POST ["txt{$days [$iCount]}AmStart"] . "-" . $_POST ["txt{$days [$iCount]}AmEnd"];
			if ($_POST ["txt{$days [$iCount]}PmStart"] != "")
				$am [$iCount] .= "," . $_POST ["txt{$days [$iCount]}PmStart"] . "-" . $_POST ["txt{$days [$iCount]}PmEnd"];
		}
		if (($am [0] == $am [4]) && ($am [0] != "-")) {
			//Mon & Fri are the same. Set up a range
			$hours = "Mo-Fr {$am [0]}; ";
			//Add exceptions
			for ($iCount = 0; $iCount <= 3; $iCount++)
				if ($am [0] != $am [$iCount])
					$hours .= "; {$days [$iCount]} {$am [$iCount]}; ";
		}
		else {
			//Mon & Fri are not the same. Set up individual days
			for ($iCount = 0; $iCount <= 4; $iCount++)
				if ($am [$iCount] != "-")
					$hours .= $days [$iCount] . " {$am [$iCount]}; ";
		}
		//Add Saturday & Sunday
		if ($am [5] != "-")
			$hours .= "{$days [5]} {$am [5]}; ";
		if ($am [6] != "-")
			$hours .= "{$days [6]} {$am [6]};";
	}
}

if ($hours != "" && isset ($_POST ["btnSubmit"])) {
	$bUpdated = False;
	foreach ($xml->node[0]->tag as $tag) {
		if ($tag ['k'] == "opening_hours") {
			$open_hours = (string) $tag ['v'];
			if (($open_hours != $hours) && ($hours != "")) {
				$tag ['v'] = $hours;
				$open_hours = $hours;
				//Update OSM
				$iCS = osm_create_changeset ();
				osm_update_node ($id, $xml->asXML (), $iCS);
				//Changeset is not closed, in case further edits are made.
				//It will be closed automatically at the server
				//osm_close_changeset ($iCS);
				header("Location: " . BASE_URL . "/detail.php?id=$id&amp;dist=$dist&amp;edit=yes");
				$bUpdated = True;
			}
		}
	}
	if ($bUpdated == False) {
		// opening_hours tag does not exist - create new one
		$tag = $xml->node [0]->addChild("tag");
		$tag->addAttribute ("k", "opening_hours");
		$tag->addAttribute ("v", $hours);
		//Update OSM
		if (isset ($_COOKIE ['csID']))
			$iCS = $_COOKIE ['csID'];
		else
			$iCS = osm_create_changeset ();
		osm_update_node ($id, $xml->asXML (), $iCS);
		//Changeset is not closed, in case further edits are made.
		//It will be closed automatically at the server
		//osm_close_changeset ($iCS);
		header("Location: " . BASE_URL . "/detail.php?id=$id&amp;dist=$dist&amp;edit=yes");
	}
}
require_once ("inc_head_html.php");
?>

<form action = "edit_hours.php?id=<?=$id?>&amp;name=<?php echo urlencode ($display_name); ?>&amp;dist=<?=$dist?>" method = "post">
<p>
<table>
<tr><th colspan = "2" align = "center"><?php echo htmlentities ($_GET ['name']); ?></th></tr>
<tr><th colspan = "2" align = "center">Opening Hours</th></tr>
<tr><td colspan = "2" align = "center">
<?php
if ($open_hours == "24/7")
	$check = "checked";
else
	$check = "";
?>
<input type = "checkbox" <?=$check?> class = "default" name = "chk247"> Open 24/7
</td></tr>
<?php
for ($iCount = 0; $iCount <= 6; $iCount++) {
	echo "<tr><td>{$days [$iCount]}</td><td>";
	unset ($hours);
	OpenTimes ($open_hours, $iCount + 1, $hours);
	echo "<input name = 'txt{$days [$iCount]}AmStart' value = '" .
		"{$hours ["amStart"]}'> to ";
	echo "<input name = 'txt{$days [$iCount]}AmEnd' value = '" .
		"{$hours ["amEnd"]}'><br>";
	echo "<input name = 'txt{$days [$iCount]}PmStart' value = '" .
		"{$hours ["pmStart"]}'> to ";
	echo "<input name = 'txt{$days [$iCount]}PmEnd' value = '" .
		"{$hours ["pmEnd"]}'><br>";
}
?>

<tr><td align = "center" colspan = "2"><input type = "reset" value = "Reset">&nbsp;
&nbsp;<input type = "submit" value = "Submit" name = "btnSubmit"></td></tr>
<tr><td align = "center" colspan = "2">
<a href = "detail.php?id=<?=$id?>&amp;dist=<?=$dist?>">Cancel</a>
</td></tr>
</table>
</form>
</p>

<?php
require ("inc_foot.php");
?>
