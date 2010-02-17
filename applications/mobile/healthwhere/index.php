<?php
require ('inc_head.php');
$txtPostcode = $_COOKIE ["Postcode"];
$txtLatitude = $_COOKIE ["Latitude"];
$txtLongitude = $_COOKIE ["Longitude"];

if (isset ($_COOKIE ["Distance"]))
	$txtDistance = $_COOKIE ["Distance"];
else
	$txtDistance = DEFAULT_MAX_DIST;

if (isset ($_COOKIE ["HourOffset"]))
	$iHourOffset = $_COOKIE ["HourOffset"];
else
	$iHourOffset = 0;
require_once ("inc_head_html.php");
?>
<b>Healthwhere</b>

<p>
<form action = "results.php" method = "get">
<table border = "0">
<tr><th colspan = "2">
Your Position
</th></tr>
<tr><td>UK Postcode:</td>
<td><input name = "txtPostcode" value = "<?=$txtPostcode?>"></td></tr>
<tr><td colspan = "2" align = "center">or</td></tr>
<tr><td>Latitude:</td>
<td><input name = "txtLatitude" id = "txtLatitude" value = "<?=$txtLatitude?>"></td></tr>
<tr><td>Longitude:</td>
<td><input name = "txtLongitude" id = "txtLongitude" value = "<?=$txtLongitude?>"></td></tr>
</table>
<div id = "divLatLon"></div>
</p>

<p>
<table border = "0">
<tr><th colspan = "2">
Other details
</th></tr>
<tr><td>
Max distance:</td><td>
<select name = "txtDistance">
<option value = "2">2 miles</option>
<option value = "5">5 miles</option>
<option value = "10">10 miles</option>
<option value = "25">25 miles</option>
<option value = "50">50 miles</option>
<option value = "100">100 miles</option>
</td>
</tr><tr>
<td>Current time:</td>
<td>
<select name = "selHourOffset">
<?php
for ($i=0; $i<=23; $i++) {
	echo "<option value = '";
	echo $i - (int) date ("G") . "'";
	if ($iHourOffset + (int) date ("G") == $i)
		echo " selected";
	echo ">" . str_pad ($i, 2, "0", STR_PAD_LEFT);
	echo ":" . date ("i") . "</option>\n";
}
?>
</select>
</td></tr>
</table>
</p>

<p>
<input type = "submit" value = "Find pharmacies" name = "btnSubmit" class = "default">&nbsp;
<input type = "submit" value = "Find hospitals" name = "btnSubmit" class = "default">
</p>
</form>

<?php
require ('inc_foot.php');
?>
