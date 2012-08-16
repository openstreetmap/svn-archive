<?php
/*
added in version 3.0.06
Check that ac-install.php file has been deleted for security.
*/
$the_file=AC_ROOT."ac-install.php";
if(file_exists($the_file)){
	echo '
	<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
	<html>
		<head>
			<title>Ajax Availability Calendar - Remove Install</title>
			<style type="text/css">
				body{font-family:verdana;font-size:0.8em;}
				#wrapper{width:600px;margin:20px auto;border:1px solid #006699;}
				#header{background:#006699;}
				#contents{ padding:20px;}
				#footer{background: #EEE; clear:both; padding:10px; font-size:0.8em;}
			</style>
		</head>
		<body>
		<body id="page_install">
		<div id="wrapper">
			<div id="header">
				<img src="http://www.ajaxavailabilitycalendar.com/images/logo_aac.png" title="Availability Calendar - Administration">
			</div>
			<div id="contents">Please remove the <strong>ac-install.php</strong> file.</div>
			
		</div>
	</body>
	</html>
	';
	exit();
}
?>