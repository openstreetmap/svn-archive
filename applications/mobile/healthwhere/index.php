<?php
/*
Healthwhere, a web service to find local pharmacies and hospitals
Copyright (C) 2009-2010 Russell Phillips (russ@phillipsuk.org)

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

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

<?php
$aiDistances = array (2, 5, 10, 25, 50);
foreach ($aiDistances as $iDistance)
	if ($iDistance == $txtDistance)
		echo "<option value = '$iDistance' selected>$iDistance miles</option>\n";
	else
		echo "<option value = '$iDistance'>$iDistance miles</option>\n";
?>

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
