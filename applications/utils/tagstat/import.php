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

	echo "<H1>Stats</H1>\n";
	$dsn = "mysqli://$user:$passwd@$host/$db";
	$conn =& DB::connect($dsn);
	if (DB::isError($conn)) {
		die ("Cannot connect: " . $conn->getMessage() . "\n");
	}
	$conn->query("SET NAMES utf8");

	echo "<H3>Number of tagpairs seen in the current import</H3>\n";
	$result =& $conn->query("SELECT COUNT(*) FROM tagpairs WHERE newcount <> 0;");
	if (DB::isError($result) || $result->numRows() != 1) {
		die ("SELECT failed: " . $result->getMessage() . "\n");
	}
	$row =& $result->fetchRow();
	echo displayNum($row[0])."\n";
	$result->free();

	echo "<H3>Number of tagpairs parsed in import</H3>\n";
	$result =& $conn->query("SELECT SUM(newcount) FROM tagpairs");
	if (DB::isError($result) || $result->numRows() != 1) {
		die ("SELECT failed: " . $result->getMessage() . "\n");
	}
	$row =& $result->fetchRow();
	echo displayNum($row[0])."\n";
	$result->free();

	echo "<H3>Number of tagpairs first seen in the current import</H3>\n";
	$result =& $conn->query("SELECT COUNT(*) FROM tagpairs WHERE count=0");
	if (DB::isError($result) || $result->numRows() != 1) {
		die ("SELECT failed: " . $result->getMessage() . "\n");
	}
	$row =& $result->fetchRow();
	echo displayNum($row[0])."\n";
	$result->free();


	echo "<BR><BR>\n";
	echo "<A href=\"index.php\">Back to index page</A>\n";

	$conn->disconnect();
?>
 </BODY>
</HTML>
