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
	$limit = getIntFromRequest("limit", 25);
	$skip = getIntFromRequest("skip", 0);
	$dir = getValueFromRequest("dir", "DESC", array("ASC", "DESC"));
	$order = getValueFromRequest("order", "total", array("node", "way", "relation", "total"));

	$invdir = flipDir($dir);
	echo "<H1>".getPopWord($dir)." values for tag &quot;$tag_html&quot;</H1>\n";

	$dsn = "mysqli://$user:$passwd@$host/$db";
	$conn =& DB::connect($dsn);
	if (DB::isError($conn)) {
		die ("Cannot connect: " . $conn->getMessage() . "\n");
	}
	$conn->query("SET NAMES utf8");


	$getcomment =& $conn->prepare("SELECT id, comment FROM tagcomments WHERE tag=? ORDER BY score DESC LIMIT 1");
	$result =& $conn->execute($getcomment, $tag);
	if (DB::isError($result)) {
		echo "Sorry, can't retrieve comments for this tag";
	} else if ($result->numRows() > 0) {
		echo "<H3>Comment</H3>\n";
		$row =& $result->fetchRow();
		$id = $row[0];
		echo "<DIV class=\"topcomment\">\n";
		echo htmlspecialchars($row[1], ENT_NOQUOTES)."<BR>\n";
		echo "<A href=\"votecomment.php?id=$id&tag=$tag&vote=up\" target=\"_blank\">Vote up</A> \n";
		echo "<A href=\"votecomment.php?id=$id&tag=$tag&vote=down\" target=\"_blank\">Vote down</A> \n";
		echo "<A href=\"writecomment.php?tag=$tag&id=$id\">Edit</A> \n";
		echo "</DIV>\n";
	} else {
		echo "<A href=\"writecomment.php?tag=$tag\">Write a comment for this tag</A>";
	}
	$result->free();


	echo "<H3>Stats</H3>\n";
	$getcount =& $conn->prepare("SELECT tag, value, c_total, c_node, c_way, c_relation FROM tagpairs WHERE tag = ? ORDER BY c_$order $dir LIMIT $skip, $limit");
	$result =& $conn->execute($getcount, $tag);
	if (DB::isError($result)) {
		die ("SELECT failed: " . $result->getMessage() . "\n");
	}
	$dirA = array("total" => $dir, "node" => $dir, "way" => $dir, "relation" => $dir);
	$dirA[$order] = $invdir;
	echo "<TABLE>\n";
	echo "<TR>";
	echo "<TH>tag</TH>";
	echo "<TH>value</TH>";
	echo "<TH><A href=\"tagdetails.php?tag=$tag&limit=$limit&skip=$skip&dir={$dirA["total"]}&order=total\">uses</A></TH>";
	echo "<TH><A href=\"tagdetails.php?tag=$tag&limit=$limit&skip=$skip&dir={$dirA["node"]}&order=node\">node</A></TH>";
	echo "<TH><A href=\"tagdetails.php?tag=$tag&limit=$limit&skip=$skip&dir={$dirA["way"]}&order=way\">way</A></TH>";
	echo "<TH><A href=\"tagdetails.php?tag=$tag&limit=$limit&skip=$skip&dir={$dirA["relation"]}&order=relation\">relation</A></TH>";
	if($dir == "ASC") {
		echo "<TH>suggestion</TH>";
	}
	echo "<TH>link</TH>\n";
	echo "</TR>\n";
	$i = 1;
	while ($row =& $result->fetchRow()) {
		if($i % 2) {
			$style="odd";
		} else {
			$style="even";
		}
		printf('<TR class="%1$s"><TD>%2$s</TD><TD>%3$s</TD><TD class="count">%4$s</TD><TD class="count">%5$s</TD><TD class="count">%6$s</TD><TD class="count">%7$s</TD>', $style, displayTag($row[0]), displayTag($row[1]), displayNum($row[2]), displayNum($row[3]), displayNum($row[4]), displayNum($row[5]));
		if($dir == "ASC") {
			$getsuggestion =& $conn->prepare("SELECT value, c_total FROM tagpairs WHERE tag = ? AND SOUNDEX(value) = SOUNDEX(?) AND value <> ? ORDER BY c_total DESC LIMIT 1");
			$result2 =& $conn->execute($getsuggestion, array($row[0], $row[1], $row[1]));
			if (!DB::isError($result2) && $result2->numRows() == 1) {
				$row2 =& $result2->fetchRow();
				if(($row2[0] != "") && ($row2[1] > 10)) {
					printf('<TD>%1$s (%2$s)</TD>', displayTag($row2[0]), displayNum($row2[1]));
				} else {
					echo "<TD>&nbsp;</TD>";
				}
				$result2->free();
			} else {
				echo "<TD>&nbsp;</TD>";
			}
		}
		echo "<TD><A href=\"http://www.informationfreeway.org/api/0.5/*[{$row[0]}={$row[1]}]\">xapi</A></TD>\n";
		echo "</TR>\n";
		$i++;
	}
	echo "</TABLE>\n";
	$result->free();

	$getrelated =& $conn->prepare("SELECT tag1, tag2, c_total FROM tagfriends WHERE tag1=? OR tag2=? AND c_total > 0 ORDER BY c_total DESC LIMIT 10");
	$result =& $conn->execute($getrelated, array($tag, $tag));
	if (DB::isError($result)) {
		echo "Sorry, can't retrieve related tags";
	} else if ($result->numRows() > 0) {
		echo "<H3>Related Tags</H3>\n";
		echo "<TABLE>\n";
		echo "<TR><TH>tag</TH><TH>uses</TH></TR>\n";
		$i = 1;
		while($row =& $result->fetchRow()) {
			if($i % 2) {
				$style="odd";
			} else {
				$style="even";
			}
			$tag1 = $row[0];
			$tag2 = $row[1];
			if($tag1 != $tag) {
				$tagother = $tag1;
			} else {
				$tagother = $tag2;
			}
			printf('<TR class="%1$s"><TD><A href="tagdetails.php?tag=%2$s">%3$s</A></TD><TD class="count">%4$s</TD></TR>', $style, $tagother, displayTag($tagother), displayNum($row[2]));
			echo "\n";
		}
		echo "</TABLE>\n";
	}
	$result->free();

	$getfurther =& $conn->prepare("SELECT id, comment FROM tagcomments WHERE tag=? ORDER BY score DESC LIMIT 11");
	$result =& $conn->execute($getfurther, $tag);
	if (DB::isError($result)) {
		echo "Sorry, can't retrieve further comments for this tag";
	} else if ($result->numRows() > 0) {
		echo "<H3>Further Comment</H3>\n";
		$row =& $result->fetchRow(); // Discard first one
		while($row =& $result->fetchRow()) {
			$id = $row[0];
			echo "<DIV class=\"comment\">\n";
			echo htmlspecialchars($row[1], ENT_NOQUOTES)."<BR>\n";
			echo "<A href=\"votecomment.php?id=$id&tag=$tag&vote=up\" target=\"_blank\">Vote up</A> \n";
			echo "<A href=\"votecomment.php?id=$id&tag=$tag&vote=down\" target=\"_blank\">Vote down</A> \n";
			echo "<A href=\"writecomment.php?tag=$tag&id=$id\">Edit</A> \n";
			echo "<DIV>\n";
			echo "<BR>\n";
		}
		echo "<A href=\"viewcomments.php?tag=$tag\">View all comments</A><BR>\n";
		echo "<BR><BR>\n";
	}
	$result->free();

	$conn->disconnect();

	echo "<BR><BR>\n";
	$next = $skip + $limit;
	echo "<A href=\"tagdetails.php?tag=$tag&limit=$limit&skip=$next&dir=$dir&order=$order\">".getOrdWord($dir)." values</A>\n";
	$prev = max($skip - $limit, 0);
	echo "<A href=\"tagdetails.php?tag=$tag&limit=$limit&skip=$prev&dir=$dir&order=$order\">".getOrdWord($invdir)." values</A>\n";
	$more = $limit + 25;
	echo "<A href=\"tagdetails.php?tag=$tag&limit=$more&skip=$skip&dir=$dir&order=$order\">More values</A>\n";
	echo "<A href=\"tagdetails.php?tag=$tag&limit=$limit&skip=$skip&dir=$invdir&order=$order\">".getPopWord($invdir)." values</A>\n";
	echo "<A href=\"index.php\">Back to index page</A>\n";
?>
 </BODY>
</HTML>
