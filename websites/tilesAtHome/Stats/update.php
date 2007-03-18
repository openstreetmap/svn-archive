<?php
	
if($_SERVER["REMOTE_ADDR"] != $_SERVER["SERVER_ADDR"]){
	print "This page can only be run by the dev server\n";
	exit;
}


?>