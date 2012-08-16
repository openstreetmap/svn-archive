<?php 
session_start();
function session_clear() {
// if session exists, unregister all variables that exist and destroy session
    $exists = "no";
    $session_array = explode(";",session_encode());
    for ($x = 0; $x < count($session_array); $x++) {
        $name  = substr($session_array[$x], 0, strpos($session_array[$x],"|")); 
        if (session_is_registered($name)) {
            session_unregister('$name');
            $exists = "yes";
        }
    }
    if ($exists != "no") {
        session_destroy();
	}
}
session_clear();
if(!session_is_registered(session_name())) {
    header("Location: index.php");
}else{
	echo"<h1 style=\"color:red;\">NOT Logged Out</h1>";
	echo"Please contact the system administrator.";
}
?>