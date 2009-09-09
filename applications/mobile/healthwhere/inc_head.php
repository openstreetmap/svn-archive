<?php
require ("inc_config.php");

/*
 * Function to get data from node XML
 * Parameters:
 * $node: node object
 * $type: "pharmacy" or "hospital"
 * $ph: array containing information about the pharmacy, hospital, whatever
*/
function node_parse ($node, $type, &$ph) {
	$ph ["lat"] = $node ["lat"];
	$ph ["lon"] = $node ["lon"];
	foreach ($node->tag as $tag) {
		if ($tag ["k"] == "name")
			$ph ["name"] = $tag ["v"];
		if ($tag ["k"] == "operator")
			$ph ["operator"] = $tag ["v"];

		if ($tag ["k"] == "addr:housename")
			$ph ["addr_housename"] = $tag ["v"];
		if ($tag ["k"] == "addr:housenumber")
			$ph ["addr_housenumber"] = $tag ["v"];
		if ($tag ["k"] == "addr:street")
			$ph ["addr_street"] = $tag ["v"];
		if ($tag ["k"] == "addr:city")
			$ph ["addr_city"] = $tag ["v"];
		if ($tag ["k"] == "addr:postcode")
			$ph ["addr_postcode"] = $tag ["v"];

		if ($tag ["k"] == "phone" || $tag ["k"] == "telephone" || $tag ["k"] == "telephone_number")
			$ph ["phone"] = $tag ["v"];
		if ($tag ["k"] == "opening_hours")
			$ph ["hours"] = $tag ["v"];
		if ($tag ["k"] == "description")
			$ph ["description"] = $tag ["v"];
		if ($tag ["k"] == "url" || $tag ["k"] == "website")
			$ph ["url"] = $tag ["v"];

		if ($type == "pharmacy") {
			if ($tag ["k"] == "dispensing")
				$ph ["dispensing"] = $tag ["v"];
		}
		elseif ($type == "hospital") {
			if ($tag ["k"] == "emergency")
				$ph ["emergency"] = $tag ["v"];
		}
	}
}

/*
 * Function to call when there is a fatal error
 * Parameters:
 * $log_string is saved in error log
 * $display_string is displayed to user
 * Returns nothing
*/
function death ($log_string, $display_string) {
	global $error_log;

	file_put_contents ($error_log, date ("Y-m-d H:i:s") . "\t$log_string\n", FILE_APPEND);
	echo "<p>$display_string</p>";
	die ();
}

/*
 * Function to log a message to the debug log
 * Parameter:
 * $message: message to be logged
 * Returns nothing
*/
function logdebug ($message) {
	global $debug_log;

	$message = date ('H:i d/m/Y') . ": $message\n";
	file_put_contents ($debug_log, $message, FILE_APPEND);
}
?>
