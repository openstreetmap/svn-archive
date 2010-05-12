<html>
<head>
<title>Healthwhere</title>
<!-- viewport meta tag is used to inform Mobile Safari that the page is *not* 980px wide -->
<meta name="viewport" content="user-scalable=no, width=device-width" />
<link rel="stylesheet" type="text/css" href="style.css" media="all" />
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
		sURL = "http://www.openstreetmap.org/?mlat=" + latitude + "&mlon=" + longitude + "&zoom=17"
		document.getElementById ("divLatLon").innerHTML = "<i><a href = '" + sURL + "' title = 'map of your location'>Your location</a> has been filled in automatically. Note that this may not be accurate. <a href = 'geolocation.php'>More information</a></i>"
	}

	function errorCallback(error) {
		// do nothing
	}

	function getlocation () {
		if (typeof navigator.geolocation != "undefined")
			navigator.geolocation.getCurrentPosition (successCallback, errorCallback)
	}
	// -->
	</script>
<?php
}
?>
</head>
<?php
if (basename ($_SERVER["SCRIPT_FILENAME"]) == "index.php")
	echo "<body onload = 'getlocation ()'>\n";
else
	echo "<body>\n";
?>
