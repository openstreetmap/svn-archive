<?php
	require_once "DB.php";
	include("config.php");
	include("func.php");

	header("Content-type: text/xml");

	$action = getValueFromRequest("action");

	echo "<search>\n";
	echo "<action>$action</action>\n";
	
	if(($action != "values") && ($action != "fulltext")) {
		echo "<error>This action is currently not supported</error>\n";
		echo "</search>\n";
		die();
	}

	$key = getValueFromRequest("key");

	echo "<key>$key</key>\n";

	$dsn = "mysqli://$user:$passwd@$host/$db";
	$conn =& DB::connect($dsn);
	if (DB::isError($conn)) {
		die("<error>Can't connect to DB: " . $conn->getMessage(). "</error></search>\n");
	}
	$conn->query("SET NAMES utf8");

	if($action == "values") {
		$search =& $conn->prepare("SELECT value, c_node, c_way, c_relation, c_other, c_total FROM tagpairs WHERE tag LIKE ? ORDER BY c_total DESC LIMIT 25");
		$result = $conn->execute($search, "$key");
		if (DB::isError($result)) {
			die ("<error>Can't select data: " . $result->getMessage() . "</error></search>\n");
		}
		if($result->numRows() > 0) {
			echo "<results>\n";
			while ($row =& $result->fetchRow()) {
				echo "<result>\n";
				echo "<value>{$row[0]}</value>\n";
				echo "<total>{$row[5]}</total>\n";
				echo "<onnode>{$row[1]}</onnode>\n";
				echo "<onway>{$row[2]}</onway>\n";
				echo "<onrelation>{$row[3]}</onrelation>\n";
				echo "<onother>{$row[4]}</onother>\n";
				echo "</result>\n";
			}
			echo "</results>\n";
		}
	} else if($action == "fulltext") {
		echo "<results>\n";
		$search =& $conn->prepare("SELECT tag, value, c_node, c_way, c_relation, c_other, c_total FROM tagpairs WHERE tag LIKE ? ORDER BY c_total DESC LIMIT 25");
		$result = $conn->execute($search, "%$key%");
		if (DB::isError($result)) {
			die ("<error>Can't select data: " . $result->getMessage() . "</error></results></search>\n");
		}
		if($result->numRows() > 0) {
			while ($row =& $result->fetchRow()) {
				echo "<result>\n";
				echo "<tag>{$row[0]}</tag>\n";
				echo "<value>{$row[1]}</value>\n";
				echo "<total>{$row[6]}</total>\n";
				echo "<onnode>{$row[2]}</onnode>\n";
				echo "<onway>{$row[3]}</onway>\n";
				echo "<onrelation>{$row[4]}</onrelation>\n";
				echo "<onother>{$row[5]}</onother>\n";
				echo "</result>";
			}
		}
		$search =& $conn->prepare("SELECT tag, value, c_node, c_way, c_relation, c_other, c_total FROM tagpairs WHERE value LIKE ? ORDER BY c_total DESC LIMIT 25");
		$result = $conn->execute($search, "%$key%");
		if (DB::isError($result)) {
			die ("<error>Can't select data: " . $result->getMessage() . "</error></results></search>\n");
		}
		if($result->numRows() > 0) {
			while ($row =& $result->fetchRow()) {
				echo "<result>\n";
				echo "<tag>{$row[0]}</tag>\n";
				echo "<value>{$row[1]}</value>\n";
				echo "<total>{$row[6]}</total>\n";
				echo "<onnode>{$row[2]}</onnode>\n";
				echo "<onway>{$row[3]}</onway>\n";
				echo "<onrelation>{$row[4]}</onrelation>\n";
				echo "<onother>{$row[5]}</onother>\n";
				echo "</result>\n";
			}
		}
		echo "</results>\n";
	}



	echo "</search>\n";
	$result->free();
	$conn->disconnect();
?>
