<?php
require_once ("results.php");
require_once ("inc_head_html.php");

$numfound = count ($asPharmacies);
echo "<p>" . $numfound;
if ($numfound == 1 && $search == "pharmacy")
	echo " pharmacy";
elseif ($numfound != 1 && $search == "pharmacy")
	echo " pharmacies";
elseif ($numfound == 1 && $search == "hospital")
	echo " hospital";
else
	echo " hospitals";
echo " found within $maxdist miles";
if ($txtPostcode != "")
	$locationtext = strtoupper (htmlentities ($txtPostcode));
else
	$locationtext = "your location";

//Only display map link in JS-capable browsers
$sMap = "\n<script type='text/javascript'>\n<!--\n";
$sMap .= "document.write (\" of <a href = 'http://www.openstreetmap.org/?mlat=$user_lat&mlon=$user_lon&zoom=17' title = 'map of $locationtext'>$locationtext</a>\")";
$sMap .= "\n// -->\n";
$sMap .= "</script>\n";
$sMap .= "<noscript>\n of $locationtext\n</noscript>\n";
echo $sMap;

echo "<br><a href = 'index.php'>Search again</a></p>\n";
echo "<p>Click on a name to see details</p>\n";

// Sort array
sort ($asPharmacies);
foreach ($asPharmacies as $sPharm)
	echo "<p>$sPharm</p>\n";

echo "<p><a href = 'index.php'>Search again</a></p>\n";

require ("inc_foot.php");
?>
