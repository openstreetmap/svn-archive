<?php


/// EGM metadata to OSM tags conversion
/// This script supposes an already defined $tags array, containing elements of the form $tags["$tags_namespace:$key"] = $value.
/// Those values will be mapped to OSM tags standards as best as it can be done.




// Temp variable to improbe code readability
$n = $tags_namespace;





// Naming is common to all features...

/**
 * All naming attributes are optional!
 * NAMN1 = Name of the feature, national primary language
 * NAMN2 = Name of the feature, secondary primary language
 * NAMA1, NAMA2 = NAMN1, NAMN2, but ascii-7 encoded (UTF-8 data already in NAMN1 and NAMN2, so this is pretty useless for OSM)
 * NLN1 = ISO 639-2/B 3 character Language Code for NAMN1
 * NLN2 = ISO 639-2/B 3 character Language Code for NAMN2
 *
 *
 * Other common attrs:
 * SCN = Map scale code - At what zoom level should this show up?
 * NA4 = Data origin (two-character ISO 3166 nation code)
 *
 */

if (isset($tags["$n:fcode"]))	// Is this EGM at all?
{
	
	if (isset($tags["$n:namn1"]) &&
	    $tags["$n:namn1"] != 'N_A' &&
	    $tags["$n:namn1"] != 'N/A' &&
	    $tags["$n:namn1"] != 'N_P')
	{
		$tags['name'] = $tags["$n:namn1"];
		$lang = $tags["$n:nln1"];
		$tags["name:$lang"] = $tags["$n:namn1"];
	}
	
	if (isset($tags["$n:namn1"]) &&
	    $tags["$n:namn2"] != 'N_A' &&
	    $tags["$n:namn2"] != 'N/A' &&
	    $tags["$n:namn2"] != 'N_P')
	{
		$lang = $tags["$n:nln2"];
		$tags["name:$lang"] = $tags["$n:namn2"];
	}
	
	
	
	switch($tags["$n:fcode"])
	{
		case "FA000":
			trigger_error("EGM FA000 (Administrative boundary) metadata conversion is not yet implemented",E_USER_WARNING); break;
		case "FA001":
			trigger_error("EGM FA000 (Administrative area) metadata conversion is not yet implemented",E_USER_WARNING); break;
		case "BA010":
			trigger_error("EGM BA010 (Coastline/shoreline) metadata conversion is not yet implemented",E_USER_WARNING); break;
		case "BA020":
			trigger_error("EGM BA020 (Foreshore) metadata conversion is not yet implemented",E_USER_WARNING); break;
		case "BA030":
			trigger_error("EGM BA030 (Island) metadata conversion is not yet implemented",E_USER_WARNING); break;
		case "BA040":
			trigger_error("EGM BA040 (Water, not inland) metadata conversion is not yet implemented",E_USER_WARNING); break;
		case "BH080":
			trigger_error("EGM BH080 (Lake) metadata conversion is not yet implemented",E_USER_WARNING); break;
		case "BH130":
			trigger_error("EGM BH130 (Reservoir) metadata conversion is not yet implemented",E_USER_WARNING); break;
		case "BH170":
			trigger_error("EGM BH170 (Spring/water hole) metadata conversion is not yet implemented",E_USER_WARNING); break;
		case "BH502":
			trigger_error("EGM BH502 (Watercourse) metadata conversion is not yet implemented",E_USER_WARNING); break;
		case "BI020":
			trigger_error("EGM BI020 (Dam/weir) metadata conversion is not yet implemented",E_USER_WARNING); break;
		case "BJ030":
			trigger_error("EGM BJ030 (Glacier) metadata conversion is not yet implemented",E_USER_WARNING); break;
		case "BJ100":
			trigger_error("EGM BJ100 (Snow field/ice field) metadata conversion is not yet implemented",E_USER_WARNING); break;
			
			
			
			
		case "AN010":	// Railroads
			// RSU = Seasonal availability - no OSM tag yet
			// FCO = Feature Configuration (number of tracks) - no OSM tag yet
			// RRA = Railway power source (electrified or not) - no OSM tag yet
			// GAW = Gauge width - no OSM tag yet
			// RGC = Railroad Gauge category
			if ($tags["$n:rgc"] == 2)
				$tags['railway'] = 'narrow_gauge';
			else if ($tags["$n:rgc"] == 998)
				$tags['railway'] = 'monorail';
			else
				$tags['railway'] = 'rail';
			
			// EXS = existence category (OK/abandoned/under construction)
			if ($tags["$n:exs"] == 6)
				$tags['railway'] = 'abandoned';
			
			// LOC = Location (bridge/underground)
			if ($tags["$n:loc"] == 25)
			{
				$tags['bridge'] = true;
				$tags['layer'] = 1;
			}
			else if ($tags["$n:loc"] == 40)
			{
				$tags['tunnel'] = true;
				$tags['layer'] = -1;
			}
			break;
			
			
		case "AP030":	// Roads
			// RSU = Seasonal availability - no OSM tag yet
			// EXS = Existence Category - no OSM tag yet
			// MED = Median category - no OSM tag yet
			
			// RST = Road/runway surface type
			if ($tags["$n:rst"] == 1)
				$tags['tracktype'] = 'grade1';
			else if ($tags["$n:rst"] == 2)
				$tags['tracktype'] = 'grade2';
			
			// RTT = Route Intended Use
			
			if ($tags["$n:rtt"] == 16)
				$tags['highway'] = 'motorway';
			else if ($tags["$n:rtt"] == 15)
				$tags['highway'] = 'trunk';
			else if ($tags["$n:rtt"] == 14)
				$tags['highway'] = 'primary';
			else if ($tags["$n:rtt"] == 984)
				$tags['highway'] = 'secondary';
			else
				$tags['highway'] = 'unclassified';
			
			
			// LOC = Location (bridge/underground)
			if ($tags["$n:loc"] == 25)
			{
				$tags['bridge'] = true;
				$tags['layer'] = 1;
			}
			else if ($tags["$n:loc"] == 40)
			{
				$tags['tunnel'] = true;
				$tags['layer'] = -1;
			}
			
			/// TODO !!!!
			// RTE# = Route number, european (international reference number). Up to 3 values.
			// RTN# = Route number, national (national reference number). Up to 3 values.
			// Route numbers *might* have the '#' symbol as a delimiter (EGM 3.0 only), or several tags (RTE1, RTE2, RTE3) can exist (EGM v2.5 only).
			
			break;
			
		case "AQ070":	// Ferry routes
			/// TODO: implement EGM v3 specs. Right now, only EGM 2.5 specs are taken into consideration.
			// RSU = Seasonal availability - no OSM tag yet
			// NA4# = Ferry route nation code: two ISO 3166 2-character nation codes, that make up the origin country and destination country. - no OSM tag yet.
			
			$tags['route'] = 'ferry';
			break;
			
		case "AQ090":
			trigger_error("EGM AQ090 (Entrance/exit) metadata conversion is not yet implemented",E_USER_WARNING); break;
		case "AQ125":
			trigger_error("EGM AQ125 (Railway station) metadata conversion is not yet implemented",E_USER_WARNING); break;
		case "GB005":
			trigger_error("EGM GB005 (Airport/airfield) metadata conversion is not yet implemented",E_USER_WARNING); break;
		
		
		
		case "AL020":
			// Built-up point or area
			// PPL Populated Place Category (Actual population)
			if ($tags["$n:ppl"] == -29997)	// PPL is unpopulated, PP1 and PP2 are.
			{
				$tags['population'] = ($tags["$n:pp1"] + $tags["$n:pp2"]) / 2;
			}
			else if ($tags["$n:ppl"] != -29999 && $tags["$n:ppl"] != -997)	// Field is not unpopulated
			{
				$tags['population'] = ($tags["$n:ppl"]);
			}
			// PP1 Population lower range - no OSM tag yet
			// PP2 Population higher range - no OSM tag yet
			// STS Status of built-up area.
			if ($tags["$n:sts"] == 1 ||	// Country capital
			$tags["$n:sts"] == 2 ||	// Regional administrative unit capital
			$tags["$n:sts"] == 3)	// Local administrative unit capital
				$tags['place'] = 'city';
			if ($tags["$n:sts"] == 4)	// Other cities
				$tags['place'] = 'town';
			if ($tags["$n:sts"] == 5)	// Other cities
				$tags['place'] = 'village';
			break;
			
			
			
			
		case "CA030":
			trigger_error("EGM CA030 (Height point) metadata conversion is not yet implemented",E_USER_WARNING); break;
		case "ZD040":
			trigger_error("EGM ZD040 (Named location) metadata conversion is not yet implemented",E_USER_WARNING); break;
		default:
			trigger_error("Unknown EGM fcode " . $tags["$n:fcode"] . " !",E_USER_WARNING); break;
	}
	
}



















