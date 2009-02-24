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

	$result =& $conn->query("SELECT tag1, tag2, c_total, c_node, c_way, c_relation FROM tagfriends ORDER BY c_$order $dir LIMIT $skip, $limit");
	if (DB::isError($result)) {
		die ("SELECT failed: " . $result->getMessage() . "\n");
	}
	$dirA = array("total" => $dir, "node" => $dir, "way" => $dir, "relation" => $dir);
	$dirA[$order] = $invdir;
	echo "<TABLE>\n";
	echo "<TR>";
	echo "<TH>tag 1</TH>";
	echo "<TH>tag 2</TH>";
	echo "<TH><A href=\"tagfriends.php?limit=$limit&skip=$skip&dir={$dirA["total"]}&order=total\">uses</A></TH>";
	echo "<TH><A href=\"tagfriends.php?limit=$limit&skip=$skip&dir={$dirA["node"]}&order=node\">node</A></TH>";
	echo "<TH><A href=\"tagfriends.php?limit=$limit&skip=$skip&dir={$dirA["way"]}&order=way\">way</A></TH>";
	echo "<TH><A href=\"tagfriends.php?limit=$limit&skip=$skip&dir={$dirA["relation"]}&order=relation\">relation</A></TH>";
	echo "</TR>\n";
	$i = 1;
	while ($row =& $result->fetchRow()) {
		if($i % 2) {
			$style="odd";
		} else {
			$style="even";
		}
		printf('<TR class="%1$s"><TD><A href="tagdetails.php?tag=%2$s">%3$s</A></TD><TD><A href="tagdetails.php?tag=%4$s">%5$s</A></TD><TD class="count">%6$s</TD><TD class="count">%7$s</TD><TD class="count">%8$s</TD><TD class="count">%9$s</TD></TR>', $style, $row[0], displayTag($row[0]), $row[1], displayTag($row[1]), displayNum($row[2]), displayNum($row[3]), displayNum($row[4]), displayNum($row[5]));
		echo "\n";
		$i++;
	}
	echo "</TABLE>\n";
	$result->free();
	$conn->disconnect();

	$next = $skip + $limit;
	echo "<A href=\"tagfriends.php?limit=$limit&skip=$next&dir=$dir&order=$order\">".getOrdWord($dir)." tag / tag combinations</A>\n";
	$prev = max($skip - $limit, 0);
	echo "<A href=\"tagfriends.php?limit=$limit&skip=$pext&dir=$dir&order=$order\">".getOrdWord($invdir)." tag / tag combinations</A>\n";
	$more = $limit+25;
	echo "<A href=\"tagfriends.php?limit=$more&skip=$skip&dir=$dir&order=$order\">More tag / tag combinations</A>\n";
	echo "<A href=\"tagfriends.php?limit=$limit&skip=$skip&dir=$invdir&order=$order\">".getPopWord($invdir)." tag / tag combinations</A>\n";
	echo "<A href=\"index.php\">Back to index page</A>\n";
?>
 </BODY>
</HTML>
