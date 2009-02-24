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

	$id = getValueFromRequest("id");
	$tag = getValueFromRequest("tag");
	$tag_html = htmlspecialchars($tag);
	$vote = getValueFromRequest("vote");

	echo "<H1>Saving vote for comment on tag &quot;$tag_html&quot;</H1>\n";
	$dsn = "mysqli://$user:$passwd@$host/$db";
	$conn =& DB::connect($dsn);
	if (DB::isError($conn)) {
		die ("Cannot connect: " . $conn->getMessage() . "\n");
	}

	$change = "0";
	if($vote == "up") {
		$change = "1";
	} else if ($vote == "down") {
		$change = "-1";
	} else {
		echo "Don't try to play with the URL I don't like that\n";
	}

	$updatescore =& $conn->prepare("UPDATE tagcomments SET score=score+? WHERE id=?");
	$result =& $conn->execute($updatescore, array($change, $id));
	if (DB::isError($result)) {
		die ("UPDATE failed: " . $result->getMessage() . "\n");
	}

	$conn->disconnect();

	echo "<A href=\"tagdetails.php?tag=$tag\">Back to $tag_html</A>\n";
	echo "<A href=\"index.php\">Back to index page</A>\n";
?>
 </BODY>
</HTML>
