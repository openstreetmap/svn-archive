<?php
 $browser = getenv("HTTP_USER_AGENT");
 $pos = strpos($browser, '/');
 $phone = substr($browser,0,$pos);
 print "Your phone is a ";
 print $phone;
?>

