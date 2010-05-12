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

require_once ("inc_head.php");
require_once ("inc_edit.php");

$id = (int) $_GET ["id"];
$dist = (float) $_GET ["dist"];

// Get data from OSM
$sXML = file_get_contents ("http://www.openstreetmap.org/api/0.6/node/$id");
$xml = new SimpleXMLElement($sXML);

if ($sXML != "") {
	$iNodeCounter = 0;
	foreach ($xml->node[0]->tag as $tag) {
		if ($tag ["k"] == "addr:housename") {
			$addr_housename = (string) $tag ["v"];
			$i_housename = $iNodeCounter;
		}
		if ($tag ["k"] == "addr:housenumber") {
			$addr_housenumber = (string) $tag ["v"];
			$i_housenumber = $iNodeCounter;
		}
		if ($tag ["k"] == "addr:street") {
			$addr_street = (string) $tag ["v"];
			$i_street = $iNodeCounter;
		}
		if ($tag ["k"] == "addr:postcode" || $tag ["k"] == "postal_code" || $tag ["k"] == "postcode") {
			$addr_postcode = (string) $tag ["v"];
			$i_postcode = $iNodeCounter;
		}
		if ($tag ["k"] == "addr:city") {
			$addr_city = (string) $tag ["v"];
			$i_city = $iNodeCounter;
		}
		$iNodeCounter++;
	}
	if (isset ($_POST ["btnSubmit"])) {
		//Compare XML values from OSM to posted values. Where they differ, update XML
		if ($addr_housename != "" && $addr_housename != $_POST ["addr_housename"]) {
			$xml->node [0]->tag [$i_housename]['v'] = $_POST ["addr_housename"];
			$addr_housename = $_POST ["addr_housename"];
		}
		if ($addr_housenumber != "" && $addr_housenumber != $_POST ["addr_housenumber"]) {
			$xml->node [0]->tag [$i_housenumber]['v'] = $_POST ["addr_housenumber"];
			$addr_housenumber = $_POST ["addr_housenumber"];
		}
		if ($addr_street != "" && $addr_street != $_POST ["addr_street"]) {
			$xml->node [0]->tag [$i_street]['v'] = $_POST ["addr_street"];
			$addr_street = $_POST ["addr_street"];
		}
		if ($addr_postcode != "" && $addr_postcode != $_POST ["addr_postcode"]) {
			$xml->node [0]->tag [$i_postcode]['v'] = $_POST ["addr_postcode"];
			$addr_postcode = $_POST ["addr_postcode"];
		}
		if ($addr_city != "" && $addr_city != $_POST ["addr_city"]) {
			$xml->node [0]->tag [$i_city]['v'] = $_POST ["addr_city"];
			$addr_city = $_POST ["addr_city"];
		}
		//Add new values
		if ($addr_housename == "" && $_POST ["addr_housename"] != "") {
			$tag = $xml->node [0]->addChild("tag");
			$tag->addAttribute ("k", "addr:housename");
			$tag->addAttribute ("v", $_POST ["addr_housename"]);
			$addr_housename = $_POST ["addr_housename"];
		}
		if ($addr_housenumber == "" && $_POST ["addr_housenumber"] != "") {
			$tag = $xml->node [0]->addChild("tag");
			$tag->addAttribute ("k", "addr:housenumber");
			$tag->addAttribute ("v", $_POST ["addr_housenumber"]);
			$addr_housenumber = $_POST ["addr_housenumber"];
		}
		if ($addr_street == "" && $_POST ["addr_street"] != "") {
			$tag = $xml->node [0]->addChild("tag");
			$tag->addAttribute ("k", "addr:street");
			$tag->addAttribute ("v", $_POST ["addr_street"]);
			$addr_street = $_POST ["addr_street"];
		}
		if ($addr_postcode == "" && $_POST ["addr_postcode"] != "") {
			$tag = $xml->node [0]->addChild("tag");
			$tag->addAttribute ("k", "addr:postcode");
			$tag->addAttribute ("v", $_POST ["addr_postcode"]);
			$addr_postcode = $_POST ["addr_postcode"];
		}
		if ($addr_city == "" && $_POST ["addr_city"] != "") {
			$tag = $xml->node [0]->addChild("tag");
			$tag->addAttribute ("k", "addr:city");
			$tag->addAttribute ("v", $_POST ["addr_city"]);
			$addr_city = $_POST ["addr_city"];
		}

		//Update OSM
		if (isset ($_COOKIE ['csID']))
			$iCS = $_COOKIE ['csID'];
		else
			$iCS = osm_create_changeset ();
		osm_update_node ($id, $xml->asXML (), $iCS);
		//Changeset is not closed, in case further edits are made.
		//It will be closed automatically at the server
		//osm_close_changeset ($iCS);
		header("Location: " . BASE_URL . "/detail.php?id=$id&amp;dist=$dist&amp;edit=yes&amp;waynode=$waynode");
	}
require_once ("inc_head_html.php");
?>

	<p>
	<form action = "edit_addr.php?id=<?=$id?>&amp;name=<?php echo urlencode ($display_name); ?>&amp;dist=<?=$dist?>&amp;waynode=<?=$waynode?>" method = "post">
	<table border = "0">
	<tr><th colspan = "2" align = "center"><?php echo htmlentities (stripslashes ($_GET ['name'])); ?></th></tr>
	<tr><td>House Name:</td>
	<td><input name = "addr_housename" value = "<?=stripslashes ($addr_housename)?>" class = 'default'></td></tr>
	<tr><td>House Number:</td>
	<td><input name = "addr_housenumber" value = "<?=stripslashes ($addr_housenumber)?>" class = 'default'></td></tr>
	<tr><td>Street:</td>
	<td><input name = "addr_street" value = "<?=stripslashes ($addr_street)?>" class = 'default'></td></tr>
	<tr><td>City:</td>
	<td><input name = "addr_city" value = "<?=stripslashes ($addr_city)?>" class = 'default'></td></tr>
	<tr><td>Postcode:</td>
	<td><input name = "addr_postcode" value = "<?=stripslashes ($addr_postcode)?>" class = 'default'></td></tr>

	<tr><td align = "center"><input type = "reset" value = "Reset"></td>
	<td align = "center"><input type = "submit" value = "Submit" name = "btnSubmit"></td></tr>
	<tr><td align = "center" colspan = "2">
	<a href = "detail.php?id=<?=$id?>&amp;dist=<?=$dist?>&amp;waynode=<?=$waynode?>">Cancel</a>
	</td></tr>
	</table>
	</form>
	</p>
	<?php
}
else
	echo "<p>Error: unable to get existing data</p>\n";

require ("inc_foot.php");
?>
