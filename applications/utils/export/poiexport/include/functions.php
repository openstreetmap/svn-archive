<?php
/* globals */
$VERSION = "v1.2";  // poi export version
$LANG = "en";       // default language file is english
$messages = null;   // array of message translations

if(isset($_SESSION['LANG'])) {
	$LANG = $_SESSION['LANG'];
}


/**
 *
 * Translate a text into the active language
 * @global string $LANG
 * @global string array $messages (see en.php for example)
 * @param string $s (message to look up and translate
 */
function msg($s) {
	global $LANG;
	global $messages;

	if (isset($messages[$s])) {
		echo $messages[$s];
	} else {
		echo $s;
	}
}

/**
 * include the required localization file
 */
function i18n() {
    global $LANG;
	if (!isset($LANG) OR !isset($_SESSION['LANG'])) {
		$LANG = split(",",$_SERVER["HTTP_ACCEPT_LANGUAGE"]);
		$LANG = split("-",$LANG[0]);
		$LANG = $LANG[0];
		if ($LANG == "") {
			$LANG = "en";
		}
	}
	global $messages;
        
	//if the language file does not exist, default to english
    // The @ is used to suppress warnings if a localization file does not
    // exist for the language
	if (@include_once 'include/'.$LANG.'.php'){
	} else {
		$LANG = 'en';
		include_once 'include/'.$LANG.'.php';
	}
	$_SESSION['LANG'] = $LANG;
}
?>