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

	$query = getValueFromRequest("query");

	echo "<H1>Search</H1>\n";

	if($query == "") {
		echo "<FORM action=\"search.php\" method=\"get\">\n";
		echo "<LABLE for=\"query\">Query term:</LABEL><BR>\n";
		echo "<INPUT name=\"query\" id=\"query\" size=\"40\"><BR>\n";
		echo "<INPUT type=\"submit\" value=\"Search\">\n";
		echo "</FORM>\n";
	} else {
		$dsn = "mysqli://$user:$passwd@$host/$db";
		$conn =& DB::connect($dsn);
		if (DB::isError($conn)) {
			die ("Cannot connect: " . $conn->getMessage() . "\n");
		}
		$conn->query("SET NAMES utf8");

		$search =& $conn->prepare("SELECT tag, uses FROM tags WHERE tag LIKE ? ORDER BY uses DESC LIMIT 10");
		$result = $conn->execute($search, "%$query%");
		if (DB::isError($result)) {
			die ("SELECT failed: " . $result->getMessage() . "\n");
		}
		if($result->numRows() > 0) {
			echo "<H3>Matching tags</H3>\n";
			echo "<TABLE>\n";
			echo "<TR><TH>tag</TH><TH>uses</TH></TR>\n";
			$i = 1;
			while ($row =& $result->fetchRow()) {
				if($i % 2) {
					$style="odd";
				} else {
					$style="even";
				}
				printf('<TR class="%1$s"><TD><A href="tagdetails.php?tag=%2$s">%4$s</A></TD><TD class="count">%3$s</TD></TR>', $style, $row[0], displayNum($row[1]), displayTag($row[0]));
				echo "\n";
				$i++;
			}
			echo "</TABLE>\n";
		}
		$result->free();

		$search =& $conn->prepare("SELECT tag, value, c_total FROM tagpairs WHERE value LIKE ? ORDER BY c_total DESC LIMIT 10");
		$result = $conn->execute($search, "%$query%");
		if (DB::isError($result)) {
			die ("SELECT failed: " . $result->getMessage() . "\n");
		}
		if($result->numRows() > 0) {
			echo "<H3>Matching values</H3>\n";
			echo "<TABLE>\n";
			echo "<TR><TH>tag</TH><TH>value</TH><TH>uses</TH></TR>\n";
			$i = 1;
			while ($row =& $result->fetchRow()) {
				if($i % 2) {
					$style="odd";
				} else {
					$style="even";
				}
				printf('<TR class="%1$s"><TD><A href="tagdetails.php?tag=%2$s">%4$s</A></TD><TD class="count">%5$s</TD><TD class="count">%3$s</TD></TR>', $style, $row[0], displayNum($row[2]), displayTag($row[0]), displayTag($row[1]));
				echo "\n";
				$i++;
			}
			echo "</TABLE>\n";
		}
		$result->free();

		$search =& $conn->prepare("SELECT tag1, tag2, c_total FROM tagfriends WHERE tag1 = ? OR tag2 = ? ORDER BY c_total DESC LIMIT 10");
		$result = $conn->execute($search, array("$query", "$query"));
		if (DB::isError($result)) {
			die ("SELECT failed: " . $result->getMessage() . "\n");
		}
		if($result->numRows() > 0) {
			echo "<H3>Commonly used along with</H3>\n";
			echo "<TABLE>\n";
			echo "<TR><TH>tag</TH><TH>uses</TH></TR>\n";
			$i = 1;
			while ($row =& $result->fetchRow()) {
				if($i % 2) {
					$style="odd";
				} else {
					$style="even";
				}
				$tag1 = $row[0];
				$tag2 = $row[1];
				if($tag1 == $query) {
					$othertag = $tag2;
				} else {
					$othertag = $tag1;
				}
				printf('<TR class="%1$s"><TD><A href="tagdetails.php?tag=%2$s">%3$s</A></TD><TD class="count">%4$s</TD></TR>', $style, $row[0], displayTag($row[0]), displayNum($row[2]));
				echo "\n";
				$i++;
			}
			echo "</TABLE>\n";
		}
		$result->free();

		$conn->disconnect();
	}

	echo "<BR><BR>\n";
	echo "<A href=\"search.php\">New search</A>\n";
	echo "<A href=\"index.php\">Back to index page</A>\n";
	?>
 </BODY>
</HTML>
