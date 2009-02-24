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

	echo "<H1>Tagstat</H1>\n";
	$dsn = "mysqli://$user:$passwd@$host/$db";
	$conn =& DB::connect($dsn);
	if (DB::isError($conn)) {
		die ("Cannot connect: " . $conn->getMessage() . "\n");
	}
	$conn->query("SET NAMES utf8");

	echo "<H3>Most popular tags</H3>\n";
	$result =& $conn->query("SELECT tag, uses FROM tags ORDER BY uses DESC LIMIT 10");
	if (DB::isError($result)) {
		die ("SELECT failed: " . $result->getMessage() . "\n");
	}
	echo "<TABLE>\n";
	echo "<TR><TH>#</TH><TH>tag</TH><TH>uses</TH></TR>\n";
	$i = 1;
	while ($row =& $result->fetchRow()) {
		if($i % 2) {
			$style="odd";
		} else {
			$style="even";
		}
		$rank = getTagRank($conn, $row[1]);
		printf('<TR class="%1$s"><TD>%4$s</TD><TD><A href="tagdetails.php?tag=%2$s">%5$s</A></TD><TD class="count">%3$s</TD></TR>', $style, $row[0], displayNum($row[1]), $rank, displayTag($row[0]));
		echo "\n";
		$i++;
	}
	echo "</TABLE>\n";
	$result->free();
	echo "<A href=\"tags.php\">More tags</A>\n";
	echo "<A href=\"tags.php?dir=ASC\">Rare tags</A>\n";


	echo "<H3>Most popular tag / value combinations</H3>\n";
	$result =& $conn->query("SELECT tag, value, c_total FROM tagpairs WHERE c_total > 0 ORDER BY c_total DESC LIMIT 10");
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
		printf('<TR class="%1$s"><TD>%2$s</TD><TD>%3$s</TD><TD class="count">%4$s</TD></TR>', $style, displayTag($row[0]), displayTag($row[1]), displayNum($row[2]));
		echo "\n";
		$i++;
	}
	echo "</TABLE>\n";
	$result->free();
	echo "<A href=\"tagpairs.php\">More tag / value pairs</A>\n";
	echo "<A href=\"tagpairs.php?dir=ASC\">Rare tag / value pairs</A>\n";


	$result =& $conn->query("SELECT tag1, tag2, c_total FROM tagfriends WHERE c_total > 0 ORDER BY c_total DESC LIMIT 10");
	if (DB::isError($result)) {
		die ("SELECT failed: " . $result->getMessage() . "\n");
	}
	if($result->numRows() > 0) {
		echo "<H3>Most popular tag / tag combinations</H3>\n";
		echo "<TABLE>\n";
		echo "<TR><TH>tag 1</TH><TH>tag 2</TH><TH>uses</TH></TR>\n";
		$i = 1;
		while ($row =& $result->fetchRow()) {
			if($i % 2) {
				$style="odd";
			} else {
				$style="even";
			}
			printf('<TR class="%1$s"><TD>%2$s</TD><TD>%3$s</TD><TD class="count">%4$s</TD></TR>', $style, displayTag($row[0]), displayTag($row[1]), displayNum($row[2]));
			echo "\n";
			$i++;
		}
		echo "</TABLE>\n";
		echo "<A href=\"tagfriends.php\">More tag / tag pairs</A>\n";
	}
	$result->free();

	$conn->disconnect();

	echo "<BR><BR>\n";
	echo "<A href=\"search.php\">Search</A>\n";
?>
 </BODY>
</HTML>
