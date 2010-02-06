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

	echo "<H1>Comments on tag &quot;$tag_html&quot;</H1>\n";
	$dsn = "mysqli://$user:$passwd@$host/$db";
	$conn =& DB::connect($dsn);
	if (DB::isError($conn)) {
		die ("Cannot connect: " . $conn->getMessage() . "\n");
	}
	$conn->query("SET NAMES utf8");

	$getcomment =& $conn->prepare("SELECT id, comment FROM tagcomments WHERE tag = ? ORDER BY score DESC");
	$result =& $conn->execute($getcomment, $tag);
	if (DB::isError($result)) {
		echo "Sorry, can't retrieve comments for this tag";
	} else if ($result->numRows() > 0) {
		echo "<H3>Comment</H3>\n";
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
	echo "<BR><BR>\n";
	$result->free();

	$conn->disconnect();

	echo "<A href=\"tagdetails.php?tag=$tag\">Back to tag page</A>\n";
	echo "<A href=\"index.php\">Back to index page</A>\n";
?>
 </BODY>
</HTML>
