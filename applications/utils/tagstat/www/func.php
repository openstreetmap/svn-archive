<?php
	function getString($string, $default) {
		if($string != "") {
			return $string;
		} else {
			return $default;
		}
	}

	function toUrl($param) {
		$parts = array();
		while(list($key, $val) = each($param)) {
			$parts[] = "$key=$val";
		}
		return implode("&amp;", $parts);
	}

	function cow($default, $overlay) {
		$out = $default;
		while(list($key, $val) = each($overlay)) {
			$out[$key] = $val;
		}
		return $out;
	}

	function getIntFromRequest($key, $default="", $valid=array()) {
		$out = getValueFromRequest($key, $default, $valid);
		if(!is_numeric($out)) {
			$out = $default;
		} else {
			$out = intval($out);
		}
		if((count($valid) != 0) && (array_search($out, $valid) === false)) {
			$out = $default;
		}
		return $out;
	}

	function getValueFromRequest($key, $default="", $valid=array()) {
		$out = $default;

		if(isset($_REQUEST[$key])) {
			$out = $_REQUEST[$key];
			if (get_magic_quotes_gpc() == 1){
				$out = stripslashes($out);
			}
		}

		if((count($valid) != 0) && (array_search($out, $valid) === false)) {
			$out = $default;
		}

		return $out;
	}

	function getValueFromCookie($key, $default="") {
		if(isset($_COOKIE[$key])) {
			return $_COOKIE[$key];
		} else {
			return $default;
		}
	}

	function displayNum($number) {
		return number_format($number, 0, ".", ",");
	}

	function displayTag($tag) {
		return str_replace(" ", "&nbsp;", str_replace(array("\n", "\r", "\r\n"), "\\n", $tag));
	}

	function getTagRank($conn, $count) {
		$getrank = $conn->prepare('SELECT COUNT(tag) FROM tags WHERE uses > ?');
		$result = $conn->execute($getrank, $count);
		if (DB::isError($result) || $result->numRows() != 1) {
			return 0;
		}
		$row =& $result->fetchRow();
		$count = 1+$row[0];
		$result->free();
		return $count;
	}

	function getPopWord($dir, $upper=true) {
		if($upper == true) {
			if($dir == "ASC") {
				return "Rare";
			} else {
				return "Popular";
			}
		} else {
			if($dir == "ASC") {
				return "rare";
			} else {
				return "popular";
			}
		}
	}

	function getOrdWord($dir, $upper=true) {
		if($upper == true) {
			if($dir == "ASC") {
				return "Previous";
			} else {
				return "Next";
			}
		} else {
			if($dir == "ASC") {
				return "previous";
			} else {
				return "next";
			}
		}
	}

	function flipDir($dir) {
		if($dir == "DESC") {
			return "ASC";
		} else {
			return "DESC";
		}
	}
?>
