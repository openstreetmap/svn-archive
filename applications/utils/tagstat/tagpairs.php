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

	$limit = getValueFromRequest("limit", 25);
	if(getValueFromRequest("rev") != "true") {
		$dir = "DESC";
	} else {
		$dir = "ASC";
	}

	if($dir == "DESC") {
		echo "<H1>Most popular tag / value combinations</H1>\n";
	} else {
		echo "<H1>Least popular tag / value combinations</H1>\n";
	}
	$dsn = "mysqli://$user:$passwd@$host/$db";
	$conn =& DB::connect($dsn);
	if (DB::isError($conn)) {
		die ("Cannot connect: " . $conn->getMessage() . "\n");
	}
	$conn->query("SET NAMES utf8");

	$result =& $conn->query("SELECT tag, value, count FROM tagpairs ORDER BY count $dir LIMIT $limit");
	if (DB::isError($result)) {
		die ("SELECT failed: " . $result->getMessage() . "\n");
	}
	echo "<TABLE>\n";
	echo "<TR><TH>tag</TH><TH>value</TH><TH>uses</TH></TR>\n";
	$i = 1;
	while ($row =& $result->fetchRow()) {
		if($i % 2) {
			$style="odd";
		} else {
			$style="even";
		}
		printf('<TR class="%1$s"><TD><A href="tagdetails.php?tag=%5$s">%2$s</A></TD><TD>%3$s</TD><TD class="count">%4$s</TD></TR>', $style, displayTag($row[0]), displayTag($row[1]), displayNum($row[2]), $row[0]);
		echo "\n";
		$i++;
	}
	echo "</TABLE>\n";
	$result->free();
	$conn->disconnect();

	if($dir == "DESC") {
		echo "<A href=\"tagpairs.php?limit=$limit&rev=true\">Rare tag / value combinations</A>\n";
	} else {
		echo "<A href=\"tagpairs.php?limit=$limit\">Popular tag / value combinations</A>\n";
	}
	$limit += 25;
	if($dir == "DESC") {
		echo "<A href=\"tagpairs.php?limit=$limit\">More tag / value combinations</A>\n";
	} else {
		echo "<A href=\"tagpairs.php?limit=$limit&rev=true\">More tag / value combinations</A>\n";
	}
	echo "<A href=\"index.php\">Back to index page</A>\n";
?>
 </BODY>
</HTML>
