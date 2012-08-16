<?php
/***********************************************/
/*
File:			calendar.ajax.php
Author: 		cbolson.com 
Script: 		availability calendar
Version:		3.02
Url: 			http://www.ajaxavailabilitycalendar.com
Date Created: 	2009-07-29   
Date Modified: 	2010-01-30

Use:			Called via ajax to draw calendar

Receives:		$_REQUEST["id_item"] 	- id of the item
				$_REQUEST["month"]		- calendar month
				$_REQUEST["year"]		- calendar year
*/
/***********************************************/

// include common file for ajax settings
$the_file=dirname(__FILE__)."/ajax-common.inc.php";
if(!file_exists($the_file)) die("<b>".$the_file."</b> not found");
else		require_once($the_file);

//	define variables
$id_item	= $_REQUEST["id_item"];
$the_month	= $_REQUEST["month"];
$the_year	= $_REQUEST["year"];

//	required data
if($id_item=="") 	die("no item defined");
if($the_month=="") 	die("no month defined");
if($the_year=="") 	die("no year defined");

// create the calendar
echo draw_cal($id_item,$the_month,$the_year);
?>