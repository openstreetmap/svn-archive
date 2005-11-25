<?php
	header("Content-type: text/xml\n\n");
	$url = $_SERVER['QUERY_STRING'];
	if (strpos($url,"http://geocoder.us/service/rest/") === 0 || 
		strpos($url,"http://brainoff.com/geocoder/rest/") === 0) {
			$handle = fopen($url, 'r');
			while (!feof($handle)) {
				$text .= fread($handle,4096);
			}
			fclose($handle);
			echo $text;
		//$ch = curl_init($url);
		//curl_exec($ch);
	}
?>
