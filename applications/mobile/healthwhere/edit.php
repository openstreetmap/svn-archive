<?php
require_once ("inc_head.php");
require_once ("inc_edit.php");

$id = (int) $_GET ["id"];
$dist = (float) $_GET ["dist"];
$display_name = $_GET ["name"];

// Get data from OSM
$sXML = file_get_contents ("http://www.openstreetmap.org/api/0.6/node/$id");
$xml = new SimpleXMLElement($sXML);

if ($sXML != '') {
	foreach ($xml->node[0]->tag as $tag) {
		if ($tag ['k'] == "name")
			$name = (string) $tag ['v'];
		if ($tag ['k'] == "operator")
			$operator = (string) $tag ['v'];
		if ($tag ['k'] == "dispensing")
			$dispensing = (string) $tag ['v'];
		if ($tag ['k'] == "emergency")
			$emergency = (string) $tag ['v'];
		if ($tag ['k'] == "phone" || $tag ['k'] == "telephone" || $tag ['k'] == "telephone_number")
			$phone = (string) $tag ['v'];
		if ($tag ['k'] == "description")
			$description = (string) $tag ['v'];
		if ($tag ['k'] == "url" || $tag ['k'] == "website")
			$website = (string) $tag ['v'];
	}
	if (isset ($_POST ["btnSubmit"])) {
		//Compare XML values from OSM to posted values. Where they differ, update XML
		if ($name != "" && $name != $_POST ["txtName"]) {
			$xml->node [0]->tag [$i_name]['v'] = $_POST ["txtName"];
			$name = $_POST ["txtName"];
		}
		if ($operator != "" && $operator != $_POST ["txtOperator"]) {
			$xml->node [0]->tag [$i_operator]['v'] = $_POST ["txtOperator"];
			$operator = $_POST ["txtOperator"];
		}
		if ($_COOKIE ["SearchType"] == "pharmacy") {
			if ($dispensing != "" && $dispensing != $_POST ["selDispensing"]) {
				$xml->node [0]->tag [$i_dispensing]['v'] = $_POST ["selDispensing"];
				$dispensing = $_POST ["selDispensing"];
			}
		}
		if ($_COOKIE ["SearchType"] == "hospital") {
			if ($emergency != "" && $emergency != $_POST ["selEmergency"]) {
				$xml->node [0]->tag [$i_emergency]['v'] = $_POST ["selEmergency"];
				$emergency = $_POST ["selEmergency"];
			}
		}
		if ($phone != "" && $phone != $_POST ["txtPhone"]) {
			$xml->node [0]->tag [$i_phone]['v'] = $_POST ["txtPhone"];
			$phone = $_POST ["txtPhone"];
		}
		if ($description != "" && $description != $_POST ["txtDescription"]) {
			$xml->node [0]->tag [$i_description]['v'] = $_POST ["txtDescription"];
			$description = $_POST ["txtDescription"];
		}
		if ($website != "" && $website != $_POST ["txtWebsite"]) {
			$xml->node [0]->tag [$i_website]['v'] = $_POST ["txtWebsite"];
			$website = $_POST ["txtWebsite"];
		}

		//Add new values
		if ($name == "" && $_POST ["txtName"] != "") {
			$tag = $xml->node [0]->addChild("tag");
			$tag->addAttribute ("k", "name");
			$tag->addAttribute ("v", $_POST ["txtName"]);
			$name = $_POST ["txtName"];
		}
		if ($operator == "" && $_POST ["txtOperator"] != "") {
			$tag = $xml->node [0]->addChild("tag");
			$tag->addAttribute ("k", "operator");
			$tag->addAttribute ("v", $_POST ["txtOperator"]);
			$name = $_POST ["txtOperator"];
		}
		if ($_COOKIE ["SearchType"] == "pharmacy") {
			if ($dispensing == "" && $_POST ["selDispensing"] != "") {
				$tag = $xml->node [0]->addChild("tag");
				$tag->addAttribute ("k", "dispensing");
				$tag->addAttribute ("v", $_POST ["selDispensing"]);
				$name = $_POST ["selDispensing"];
			}
		}
		if ($_COOKIE ["SearchType"] == "hospital") {
			if ($emergency == "" && $_POST ["selEmergency"] != "") {
				$tag = $xml->node [0]->addChild("tag");
				$tag->addAttribute ("k", "emergency");
				$tag->addAttribute ("v", $_POST ["selEmergency"]);
				$name = $_POST ["selEmergency"];
			}
		}
		if ($phone == "" && $_POST ["txtPhone"] != "") {
			$tag = $xml->node [0]->addChild("tag");
			$tag->addAttribute ("k", "phone");
			$tag->addAttribute ("v", $_POST ["txtPhone"]);
			$phone = $_POST ["txtPhone"];
		}
		if ($description == "" && $_POST ["txtDescription"] != "") {
			$tag = $xml->node [0]->addChild("tag");
			$tag->addAttribute ("k", "description");
			$tag->addAttribute ("v", $_POST ["txtDescription"]);
			$description = $_POST ["txtDescription"];
		}
		if ($website == "" && $_POST ["txtWebsite"] != "") {
			$tag = $xml->node [0]->addChild("tag");
			$tag->addAttribute ("k", "website");
			$tag->addAttribute ("v", $_POST ["txtWebsite"]);
			$website = $_POST ["txtWebsite"];
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
		header("Location: " . BASE_URL . "/detail.php?id=$id&amp;dist=$dist&amp;edit=yes");
	}
require_once ("inc_head_html.php");
?>
	<p>
	<form action = "edit.php?id=<?=$id?>&amp;name=<?php echo urlencode ($display_name); ?>&amp;dist=<?=$dist?>" method = "post">
	<table border = "0">
	<tr><th colspan = "2" align = "center"><?php echo htmlentities ($display_name); ?></th></tr>
	<tr><td>Name:</td>
	<td><input name = "txtName" value = "<?=$name?>" class = 'default'></td></tr>
	<tr><td>Operator:</td>
	<td><input name = "txtOperator" value = "<?=$operator?>" class = 'default'></td></tr>
	<?php
	if ($_COOKIE ["SearchType"] == "pharmacy") {
		echo "<tr><td>Dispensing:</td>\n";
		echo "<td><select name = 'selDispensing' class = 'default'>\n";
		$selected = $dispensing;
	}
	elseif ($_COOKIE ["SearchType"] == "hospital") {
		echo "<tr><td>Emergency:</td>\n";
		echo "<td><select name = 'selEmergency' class = 'default'>\n";
		$selected = $emergency;
	}
		echo "<option value = 'no'>no</option>\n";
		if ($selected == "yes")
			echo "<option value = 'yes' selected>";
		else
			echo "<option value = 'yes'>";
		echo "yes</option>\n";
	?>
	</select>
	</td></tr>
	<tr><td>Phone:</td>
	<td><input name = "txtPhone" value = "<?=$phone?>" class = 'default'></td></tr>
	<tr><td>Description:</td>
	<td><input name = "txtDescription" value = "<?=$description?>" class = 'default'></td></tr>
	<tr><td>Website:</td>
	<td><input name = "txtWebsite" value = "<?=$website?>" class = 'default'></td></tr>

	<tr><td align = "center"><input type = "reset" value = "Reset"></td>
	<td align = "center"><input type = "submit" value = "Submit" name = "btnSubmit"></td></tr>
	<tr><td align = "center" colspan = "2">
	<a href = "detail.php?id=<?=$id?>&amp;dist=<?=$dist?>">Cancel</a>
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
