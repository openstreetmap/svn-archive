<?php
/***********************************************/
/*
File:	ajax-common.inc.php
Author: cbolson.com 
Script: availability calendar
Version:3.03
Url: 	http://www.ajaxavailabilitycalendar.com
Date 	Created: 2010-01-30
Use: 	Inlcuded in all ajax files
		Defines settings, connects to db, includes common files
*/
/***********************************************/
//	activate this to prevent the url from being accessed via the url
if(empty($_SERVER['HTTP_X_REQUESTED_WITH']) || strtolower($_SERVER['HTTP_X_REQUESTED_WITH']) != 'xmlhttprequest') {
	//	only allow ajax requests - no calling from the url
	header("location",$_SERVER["DOCUMENT_ROOT"]);
}

if(isset($admin_only)){
	//	some ajax pages should only be reached via the admin panel
	
	session_start();
	//	check only admin allowed -  no direct calls
	if(!isset($_SESSION["admin_id"])){
		die("KO");
	}
}
//	check we are getting fresh info and define charset
header("Cache-Control: private, must-revalidate");
header("Expires: Mon, 26 Jul 1997 05:00:00 GMT"); // Date in the past
header("Pragma: private");
header('Content-type: text/html; charset=utf-8');


//	general config
$the_file="../../ac-config.inc.php";
if(!file_exists($the_file)) die("<b>".$the_file."</b> not found");
else		require_once($the_file);
		
//	db connection
$the_file=AC_INLCUDES_ROOT."db_connect.inc.php";
if(!file_exists($the_file)) die("<b>".$the_file."</b> not found");
else		require_once($the_file);
	
//	common vars (db and lang)
$the_file=AC_INLCUDES_ROOT."common.inc.php";
if(!file_exists($the_file)) die("<b>".$the_file."</b> not found");
else		require_once($the_file);
	
//	calendar functions
$the_file=AC_INLCUDES_ROOT."functions.inc.php";
if(!file_exists($the_file)) die("<b>".$the_file."</b> not found");
else		require_once($the_file);
	
	
//	define language
if(!isset($_REQUEST["lang"])) $_REQUEST["lang"]=AC_DEFAULT_AC_LANG;
define("AC_LANG", $_REQUEST["lang"]);

//	include lang file
$the_file=AC_DIR_AC_LANG.AC_LANG.".lang.php";
if(!file_exists($the_file)) die("<b>".$the_file."</b> not found");
else		require_once($the_file);
?>