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
	$limit = getValueFromRequest("limit", 25);
	if(getValueFromRequest("rev") != "true") {
		$dir = "DESC";
	} else {
		$dir = "ASC";
	}

	if($dir == "DESC") {
		echo "<H1>Most popular value for tag &quot;$tag_html&quot;</H1>\n";
	} else {
		echo "<H1>Least popular value for tag &quot;$tag_html&quot;</H1>\n";
	}
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
	$getcount =& $conn->prepare("SELECT tag, value, count FROM tagpairs WHERE tag = ? ORDER BY count $dir LIMIT $limit");
	$result =& $conn->execute($getcount, $tag);
	if (DB::isError($result)) {
		die ("SELECT failed: " . $result->getMessage() . "\n");
	}
	echo "<TABLE>\n";
	echo "<TR><TH>tag</TH><TH>value</TH><TH>uses</TH>";
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
		printf('<TR class="%1$s"><TD>%2$s</TD><TD>%3$s</TD><TD class="count">%4$s</TD>', $style, displayTag($row[0]), displayTag($row[1]), displayNum($row[2]));
		if($dir == "ASC") {
			$getsuggestion =& $conn->prepare("SELECT value, count FROM tagpairs WHERE tag = ? AND SOUNDEX(value) = SOUNDEX(?) AND value <> ? ORDER BY count DESC LIMIT 1");
			$result2 =& $conn->execute($getsuggestion, array($row[0], $row[1], $row[1]));
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
	}
	echo "<A href=\"viewcomments.php?tag=$tag\">View all comments</A><BR>\n";
	echo "<BR><BR>\n";
	$result->free();

	$conn->disconnect();

	if($dir == "DESC") {
		echo "<A href=\"tagdetails.php?tag=$tag&limit=$limit&rev=true\">Rare values</A>\n";
	} else {
		echo "<A href=\"tagdetails.php?tag=$tag&limit=$limit\">Popular values</A>\n";
	}
	$limit += 25;
	if($dir == "DESC") {
		echo "<A href=\"tagdetails.php?tag=$tag&limit=$limit\">More values</A>\n";
	} else {
		echo "<A href=\"tagdetails.php?tag=$tag&limit=$limit&rev=true\">More values</A>\n";
	}
	echo "<A href=\"index.php\">Back to index page</A>\n";
?>
 </BODY>
</HTML>
