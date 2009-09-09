<?php
/*
 * Functions to determine if a shop etc is open now. Main function is
 * OpenClosed - the other functions are called from OpenClosed
 * Takes string as defined at http://wiki.openstreetmap.org/wiki/Key:opening_hours
 * Limitations: Ignores month & monthday
*/

/*
 * Function to determine if current time is in the time range
 * Returns True if current time is in time range, False if not
 * $timerange: time text (eg "09:00-18:00", "09:30-12:00")
*/
function CheckTime ($timerange) {
	//Split into start & stop times
	$startstoptimes = explode ("-", $timerange);
	//Convert start time into minutes since midnight
	$hourmin = explode (":", $startstoptimes [0]);
	$startmins = ($hourmin [0] * 60) + ($hourmin [1]);
	//Convert stop time into minutes since midnight
	$hourmin = explode (":", $startstoptimes [1]);
	//Convert 00:xx to 24:xx so that $stopmins is calculated correctly
	if (intval ($hourmin [0]) == 0)
		$hourmin [0] = 24;
	$stopmins = ($hourmin [0] * 60) + ($hourmin [1]);
	//Get current time as minutes since midnight
	$iHourOffset = $_GET ["selHourOffset"];
	$currenttime = getdate ();
	$currentmins = (($currenttime ["hours"] + $iHourOffset) * 60) + ($currenttime ["minutes"]);
	//Determine if current time is in the range
	if ($currentmins >= $startmins && $currentmins <= $stopmins)
		return True;
	else
		return False;
}

/*
 * Function to determine if facility is open now
 * Returns True if it is open, False if it is closed
 * $open_hours: value of opening_hours tag
*/
function OpenClosed ($open_hours) {
	//simplest case
	if ($open_hours == "24/7")
		return True;

	//Set up array of days as numbers - easier to compare as numbers
	$weekdays = array ("mo"=>1, "tu"=>2, "we"=>3, "th"=>4, "fr"=>5, "sa"=>6, "su"=>7);
	//Get today's day as a number
	$today = $weekdays [strtolower (substr (date ("D"), 0, 2))];
	//Default to returning False
	$bOpen = False;

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
		if ($today >= $minday && $today <= $maxday) {
			//Today is in the day range - check the time
			$times = explode (",", $daytimes [1]);
			//Day off - specific case. Return from function immediately
			if ($times [0] == "off")
				return False;
			if ($today == $minday && $today == $maxday) {
				/*
				 * This is a specific day. Reset $bOpen to false,
				 * run CheckTime and return from function without
				 * further checks
				*/
				$bOpen = False;
				//Check each time
				foreach ($times as $time)
					if (CheckTime ($time))
						$bOpen = True;
				return $bOpen;
			}
			else {
				//Check each time
				foreach ($times as $time)
					if (CheckTime ($time))
						$bOpen = True;
			}
		}
	}
	return $bOpen;
}
?>
