<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HTML>
 <HEAD>
  <TITLE>tagstat</TITLE>
  <META http-equiv="content-type" content="text/html; charset=UTF-8">
  <LINK rel="stylesheet" type="text/css" href="style.css">
 </HEAD>
 <BODY>
<?php
	require_once "DB.php";
	include("config.php");
	include("func.php");

	$tag = getValueFromRequest("tag");
	$tag_html = htmlspecialchars($tag);
	$id = getValueFromRequest("id", 0);

	echo "<H1>Write a comment for tag &quot;$tag_html&quot;</H1>\n";

	$dsn = "mysqli://$user:$passwd@$host/$db";
	$conn =& DB::connect($dsn);
	if (DB::isError($conn)) {
		die ("Cannot connect: " . $conn->getMessage() . "\n");
	}
	$conn->query("SET NAMES utf8");

	echo "<FORM action=\"savecomment.php\" method=\"post\">\n";
	echo "<INPUT type=\"hidden\" name=\"tag\" value=\"$tag\">\n";
	echo "<TEXTAREA name=\"comment\" cols=\"72\" rows=\"5\">";
	if($id != 0) {
		$select = $conn->prepare('SELECT comment FROM tagcomments WHERE id=?');
		$result = $conn->execute($select, $id);
		if (DB::isError($result)) {
			die ("SELECT failed: " . $result->getMessage() . "\n");
		}
		$row =& $result->fetchRow();
		echo $row[0];
		$result->free();
	}
	echo "</TEXTAREA>\n";
	echo "<BR>\n";
	echo "<INPUT type=\"submit\" value=\"Save\">\n";
	echo "</FORM>\n";

	$conn->disconnect();

	echo "<A href=\"tagdetails.php?tag=$tag&dir=ASC\">Rare values</A>\n";
	echo "<A href=\"tagdetails.php?tag=$tag\">Popular values</A>\n";
	echo "<A href=\"index.php\">Back to index page</A>\n";
?>
 </BODY>
</HTML>
