<html>
<head>
<title>Healthwhere</title>
<!-- <link rel="stylesheet" type="text/css" media="handheld" href="mobile.css" /> -->
<link rel="stylesheet" type="text/css" media="all" href="mobile.css" />
<?php
if (basename ($_SERVER["SCRIPT_FILENAME"]) == "index.php") {
?>
	<!-- Geo-location code for compatible browsers -->
	<script type="text/javascript">
	<!--
	function successCallback(position) {
		var latitude = position.coords.latitude
		var longitude = position.coords.longitude

		document.getElementById ("txtLatitude").value = latitude
		document.getElementById ("txtLongitude").value = longitude
		document.getElementById ("divLatLon").innerHTML = "<i>Latitude &amp; longitude have been filled in automatically. <a href = 'geolocation.php'>More information</a></i>"
	}

	function errorCallback(error) {
		// do nothing
	}

	if (typeof navigator.geolocation != "undefined")
		navigator.geolocation.getCurrentPosition (successCallback, errorCallback)
	// -->
	</script>
<?php
}
?>
</head>
<?php
if (basename ($_SERVER["SCRIPT_FILENAME"]) == "index.php")
	echo "<body onload = 'getlocation'>\n";
else
	echo "<body>\n";
?>
