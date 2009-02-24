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

	$limit = getIntFromRequest("limit", 25);
	$skip = getIntFromRequest("skip", 0);
	$dir = getValueFromRequest("dir", "DESC", array("ASC", "DESC"));
	$order = getValueFromRequest("order", "total", array("node", "way", "relation", "total"));

	$invdir = flipDir($dir);
	echo "<H1>".getPopWord($dir)." tag / tag combinations</H1>\n";

	$dsn = "mysqli://$user:$passwd@$host/$db";
	$conn =& DB::connect($dsn);
	if (DB::isError($conn)) {
		die ("Cannot connect: " . $conn->getMessage() . "\n");
	}
	$conn->query("SET NAMES utf8");

	$result =& $conn->query("SELECT SUM(uses) FROM tags");
	if (DB::isError($result)) {
		die ("SELECT failed: " . $result->getMessage() . "\n");
	}
	$row =& $result->fetchRow();
	$uses = $row[0];
	$result->free();

	$result =& $conn->query("SELECT SUM(c_total) FROM tagfriends");
	if (DB::isError($result)) {
		die ("SELECT failed: " . $result->getMessage() . "\n");
	}
	$row =& $result->fetchRow();
	$total = $row[0];
	$result->free();

	$result =& $conn->query("SELECT a.tag, b.tag, (c.c_total / $total) - ((a.uses / $uses) * (b.uses / $uses)) AS cor FROM tags a, tags b, tagfriends c WHERE a.tag=c.tag1 AND b.tag=c.tag2 AND (a.uses / $uses) * (b.uses / $uses) > 0.00000001 AND (c_total / $total) > 0.0001 ORDER BY cor $dir LIMIT $skip, $limit");
	if (DB::isError($result)) {
		die ("SELECT failed: " . $result->getMessage() . "\n");
	}
	echo "<TABLE>\n";
	echo "<TR>";
	echo "<TH>tag 1</TH>";
	echo "<TH>tag 2</TH>";
	echo "<TH><A href=\"tagcor.php?dir=$invdir&order=$order&limit=$limit\">correlation</A></TH>";
	echo "</TR>\n";
	$i = 1;
	while ($row =& $result->fetchRow()) {
		if($i % 2) {
			$style="odd";
		} else {
			$style="even";
		}
		printf('<TR class="%1$s"><TD><A href="tagdetails.php?tag=%2$s">%3$s</A></TD><TD><A href="tagdetails.php?tag=%4$s">%5$s</A></TD><TD class="count">%6$s</TD></TR>', $style, $row[0], displayTag($row[0]), $row[1], displayTag($row[1]), $row[2]);
		echo "\n";
		$i++;
	}
	echo "</TABLE>\n";
	$result->free();
	$conn->disconnect();

	echo "<A href=\"index.php\">Back to index page</A>\n";
?>
 </BODY>
</HTML>
