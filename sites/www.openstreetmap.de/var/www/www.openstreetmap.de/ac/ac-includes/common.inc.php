<?php
/***********************************************/
/*
File: 			common.inc.php
Author: 		cbolson.com 
Script: 		availability calendar
Version:		3.03.03
Modified:		2010-02-11

Use: 			get calendar configuration data and define constants
				get available languages from "lang" folder
				
Instructions:	No need to modify this file UNLESS you don't want to define the config via a database
*/
/***********************************************/
error_reporting(E_ALL ^ E_NOTICE);
ini_set("display_errors", 1); 



//	define tables
define("T_BOOKINGS_ITEMS",	"".AC_DB_PREFIX."bookings_items");		# calendar items
define("T_BOOKINGS",		"".AC_DB_PREFIX."bookings"); 			# bookings dates
define("T_BOOKING_STATES",	"".AC_DB_PREFIX."bookings_states");		# booking types (am, pm, etc)
define("T_BOOKING_UPDATE",	"".AC_DB_PREFIX."bookings_last_update");# holds las calendar update date
define("T_BOOKINGS_ADMIN",	"".AC_DB_PREFIX."bookings_admin_users");# admin users
define("T_BOOKINGS_CONFIG",	"".AC_DB_PREFIX."bookings_config");		# general config

//	get config from database - can be defined manually below if needed
$sql="SELECT cal_url,title,default_lang,start_day,date_format,click_past_dates,num_months,theme,version FROM ".T_BOOKINGS_CONFIG."";
$res=mysql_query($sql) or die("error getting calendar config data<br>".mysql_Error());
$row_config=mysql_fetch_assoc($res);

//	define calendar constants
define("AC_CALENDAR_PUBLIC"		, "".$row_config["cal_url"]."/");
define("AC_INLCUDES_PUBLIC"		, "".AC_CALENDAR_PUBLIC."ac-includes/");
define("AC_TITLE"				, "".$row_config["title"]."");
define("AC_DEFAULT_AC_LANG"			, "".$row_config["default_lang"]."");
define("AC_START_DAY"			, "".$row_config["start_day"]."");	
define("AC_DATE_DISPLAY_FORMAT"	, "".$row_config["date_format"]."");	
define("AC_ACTIVE_PAST_DATES"	, "".$row_config["click_past_dates"]."");
define("CAL_VERSION"			, "".$row_config["version"]."");

if(isset($_GET["num_months"]))	define("AC_NUM_MONTHS", "".$_GET["num_months"]."");
else define("AC_NUM_MONTHS"		, "".$row_config["num_months"]."");

define("AC_THEME"				, "".$row_config["theme"]."");


//	define directories
define("AC_CONTENTS_ROOT"		, AC_ROOT."ac-contents/");
define("AC_CONTENTS_PUBLIC"		, AC_CALENDAR_PUBLIC."ac-contents/");	#	content - themes, languages etc.
define("AC_DIR_AC_LANG"			, AC_CONTENTS_ROOT."lang/");			# 	lang folder
define("AC_DIR_ADMIN"			, AC_ROOT."ac-admin/");					#	administration
define("AC_DIR_AJAX"			, AC_INLCUDES_PUBLIC."ajax/");			#	ajax files
define("AC_DIR_JS"				, AC_INLCUDES_PUBLIC."js/");

//	contents for themes
define("AC_DIR_THEMES_ROOT"		, AC_CONTENTS_ROOT."themes/");
define("AC_DIR_THEMES"			, AC_CONTENTS_PUBLIC."themes/");
define("AC_DIR_CSS"				, AC_DIR_THEMES.AC_THEME."/css/");
define("AC_DIR_IMAGES"			, AC_DIR_THEMES.AC_THEME."/images/");

// logos
define("LOGO_CALENDAR_ADMIN"	, '<img src="images/logo_aac.png" title="Availability Calendar - Admin">');
define("LOGO_CALENDAR"			, '<img src="'.AC_DIR_IMAGES.'logo_aac.png" title="Availability Calendar">');


//	get available languages from lang file
$list_languages="";
$list_languages_config="";
$list_languages_web="";
if(isset($_REQUEST["lang"])) 	$cur_lang=$_REQUEST["lang"];
else 							$cur_lang=AC_DEFAULT_AC_LANG;

if ($handle = opendir(AC_DIR_AC_LANG)) {
	while (false !== ($file = readdir($handle))) { 
    	if ($file != "." && $file != ".." && substr($file,-8,4)=="lang") { 
    		//	define select list of languages for web and admin
    		if($_REQUEST["lang_file"]==$file) 	$selected=' selected="selected"';
			else $selected="";
			$list_languages.="<option value='".$file."' ".$selected.">".$file."</option>\n";
			
			
			//	get lang code
			$lang_code=str_replace(".lang.php","",$file);
			
			
			//	for web
			//select list for admin config
			if($cur_lang==$lang_code)	$selected=' selected="selected"';
			else  								$selected="";
			$list_languages_web.="<option value='".$lang_code."' ".$selected.">".$lang_code."</option>\n";
			
			
			//select list for admin config
			if($_REQUEST["lang"]==$lang_code)	$selected=' selected="selected"';
			elseif(AC_DEFAULT_AC_LANG==$lang_code) $selected=' selected="selected"';
			else $selected="";
			$list_languages_config.="<option value='".$lang_code."' ".$selected.">".$lang_code."</option>\n";
			
			//	define lang codes for admin
			$languages[$lang_code]=1;
		}
   	}
	closedir($handle); 
}

//	admin icons
$icons=array();
$icons["add"]		= '<img src="icons/icon_add.png" alt="add">';
$icons["edit"]		= '<img src="icons/icon_edit_s.png" alt="edit">';
$icons["delete"]	= '<img src="icons/icon_trash_s.png" alt="delete">';
$icons["calendar"]	= '<img src="icons/icon_calendar_s.png" alt="cal">';
$icons["tick"]		= '<img src="icons/icon_tick.png" alt="tick">';
$icons["cross"]		= '<img src="icons/icon_cross.png" alt="cross">';
$icons["pending"]	= '<img src="icons/icon_pending.png" alt="pending">';
?>