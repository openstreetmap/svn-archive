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
		echo "<H1>Popular tags</H1>\n";
	} else {
		echo "<H1>Least popular tags</H1>\n";
	}

	$dsn = "mysqli://$user:$passwd@$host/$db";
	$conn =& DB::connect($dsn);
	if (DB::isError($conn)) {
		die ("Cannot connect: " . $conn->getMessage() . "\n");
	}
	$conn->query("SET NAMES utf8");

	$result =& $conn->query("SELECT tag, uses FROM tags ORDER BY uses $dir LIMIT $limit");
	if (DB::isError($result)) {
		die ("SELECT failed: " . $result->getMessage() . "\n");
	}

	echo "<TABLE>\n";
	echo "<TR><TH>#</TH><TH>tag</TH><TH>uses</TH>";
	if($dir == "ASC") {
		echo "<TH>suggestion</TH>";
	}
	echo "</TR>\n";
	$i = 1;
	while ($row =& $result->fetchRow()) {
		if($i % 2) {
			$style="odd";
		} else {
			$style="even";
		}
		$rank = get_tag_rank($conn, $row[1]);
		printf('<TR class="%1$s"><TD>%4$s</TD><TD><A href="tagdetails.php?tag=%2$s">%5$s</A></TD><TD class="count">%3$s</TD>', $style, $row[0], displayNum($row[1]), $rank, displayTag($row[0]));
		if($dir == "ASC") {
			$getsuggestion =& $conn->prepare("SELECT tag, uses FROM tags WHERE SOUNDEX(tag) = SOUNDEX(?) AND tag <> ?ORDER BY uses DESC LIMIT 1");
			$result2 =& $conn->execute($getsuggestion, array($row[0], $row[0]));
			if (!DB::isError($result2) && $result2->numRows() == 1) {
				$row2 =& $result2->fetchRow();
				if(($row2[0] != "") && ($row2[1] > 10)) {
					printf('<TD>%1$s (%2$s)</TD>', displayTag($row2[0]), displayNum($row2[1]));
				} else {
					echo "<TD>&nbsp;</TD>";
				}
				$result2->free();
			}
		}
		echo "</TR>\n";
		$i++;
	}
	echo "</TABLE>\n";
	$result->free();
	$conn->disconnect();

	if($dir == "DESC") {
		echo "<A href=\"tags.php?limit=$limit&rev=true\">Rare tags</A>\n";
	} else {
		echo "<A href=\"tags.php?limit=$limit\">Popular tags</A>\n";
	}
	$limit += 25;
	if($dir == "DESC") {
		echo "<A href=\"tags.php?limit=$limit\">More tags</A>\n";
	} else {
		echo "<A href=\"tags.php?limit=$limit&rev=true\">More tags</A>\n";
	}
	echo "<A href=\"index.php\">Back to index page</A>\n";
?>
 </BODY>
</HTML>
