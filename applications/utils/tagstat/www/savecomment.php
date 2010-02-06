<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HTML>
 <HEAD>
  <TITLE>tagstat</TITLE>
  <META http-equiv="content-type" content="text/html; charset=UTF-8">
  <LINK rel="stylesheet" type="text/css" href="style.css">
 </HEAD>
 <BODY>
<?php
	require_once('recaptcha-php-1.10/recaptchalib.php');
	require_once "DB.php";
	include("config.php");
	include("func.php");

	#request
	$tag = getValueFromRequest("tag");
	$tag_html = htmlspecialchars($tag);
	$comment = getValueFromRequest("comment");
	$captcha = getValueFromRequest("captcha");

	$spam = false;
	# is this a resend with captcha?
	if($captcha == "check") {
		$resp = recaptcha_check_answer ($privatekey,
			$_SERVER["REMOTE_ADDR"],
			$_REQUEST["recaptcha_challenge_field"],
			$_REQUEST["recaptcha_response_field"]);
			if (! $resp->is_valid) {
				$error = $resp->error;
			if($error == "incorrect-captcha-sol") {
				echo "You did not solve the captca correctly. ";
			} else {
				echo "Something failed. The error message was: $error";
			}
			$spam = true;
		}
	# is this a first sent
	} else {
		$spamwords = array("http", "url", "sex");
		while(list($id, $word) = each($spamwords)) {
			if(strpos($comment, $word) !== false) {
				$spam = true;
			}
		}
		if($spam) {
			echo "Your comment contains words commonly found in spam. ";
		}
	}

	# doesn't look ok
	if($spam) {
		echo "Please solve the following captcha so save your comment.";
		echo "<form action=\"savecomment.php\" method=\"post\">";
		echo recaptcha_get_html($publickey, $error);
		echo "<input type=\"hidden\" name=\"tag\" value=\"$tag\" />";
		echo "<input type=\"hidden\" name=\"comment\" value=\"$comment\" />";
		echo "<input type=\"hidden\" name=\"captcha\" value=\"check\" />";
		echo "<input type=\"submit\" name=\"submit\" value=\"Save\" />";
		echo "</form>";
	#save
	} else {
		echo "<H1>Saving for tag &quot;$tag_html&quot;</H1>\n";
		$dsn = "mysqli://$user:$passwd@$host/$db";
		$conn =& DB::connect($dsn);
		if (DB::isError($conn)) {
			die ("Cannot connect: " . $conn->getMessage() . "\n");
		}
		$conn->query("SET NAMES utf8");

		$insert = $conn->prepare('INSERT INTO tagcomments SET tag=?, comment=?, score=0');
		$result = $conn->execute($insert, array($tag, $comment));
		if (DB::isError($result)) {
			die ("INSERT failed: " . $result->getMessage() . "\n");
		}

		$id = 0;
		$getid = $conn->prepare('SELECT MAX(id) FROM tagcomments WHERE tag=? AND comment=? AND score=0');
		$result = $conn->execute($getid, array($tag, $comment));
		if (DB::isError($result) || $result->numRows() != 1) {
			die ("GET ID failed: " . $result->getMessage() . "\n");
		} else {
			$row =& $result->fetchRow();
			$id = $row[0];
		}
		if($id == 0) {
			die ("GET ID failed: got id 0");
		} else {
			echo "Comment saved as id $id<BR>";
		}

		$commentLenZ = strlen(gzcompress($comment));
		$newscore = 0;

		$getother =& $conn->prepare("SELECT id, comment, score FROM tagcomments WHERE tag=?");
		$result = $conn->execute($getother, $tag);
		if (DB::isError($result)) {
			die ("SELECT failed: " . $result->getMessage() . "\n");
		}
		while($row =& $result->fetchRow()) {
			$otherLenZ = strlen(gzcompress($row[1]));
			$sum = strlen(gzcompress($comment.$row[1]));
			$sim = ($commentLenZ + $otherLenZ) / $sum;
			$otherid = $row[0];
			$score = $row[2];
			//echo "id = $otherid, score = $score, similarity = $sim <BR>\n";
			if($score * ($sim - 1) > $newscore) {
				$newscore = $score * ($sim - 1);
			}
		}
		$result->free();

		$newscore = (int)$newscore;
		$updatescore =& $conn->prepare("UPDATE tagcomments SET score=? WHERE id=?");
		$result =& $conn->execute($updatescore, array($newscore, $id));
		if (DB::isError($result)) {
			die ("UPDATE score failed: " . $result->getMessage() . "\n");
		} else {
			echo "You post was granted an initial score of $newscore<BR><BR>\n";
		}

		$conn->disconnect();
	}
	echo "<BR>\n";

	#footer
	echo "<A href=\"tagdetails.php?tag=$tag&dir=ASC\">Rare values</A>\n";
	echo "<A href=\"tagdetails.php?tag=$tag\">Popular values</A>\n";
	echo "<A href=\"index.php\">Back to index page</A>\n";
?>
 </BODY>
</HTML>
