<?php
/***********************************************/
/*
File:			list_order.ajax.php
Author: 		cbolson.com 
Script: 		availability calendar
Version: 		3.03
Url: 			http://www.ajaxavailabilitycalendar.com
Date Created: 	2009-07-29   
Date Modified: 	2010-01-30

Use:			Update item orders in administration

Receives:		$_REQUEST["type"] 		- database table
				$_REQUEST["sort_order"]	- order of items
				$_REQUEST["order_field"]- db table that stores the order field
*/
/***********************************************/


//	admin only access
$admin_only=true;

// include common file for ajax settings
$the_file=dirname(__FILE__)."/ajax-common.inc.php";
if(!file_exists($the_file)) die("<b>".$the_file."</b> not found");
else		require_once($the_file);


//	define request vars
$the_table	=	$_GET["type"];
$sort_order	=	$_GET["sort_order"];
$order_field=	$_GET["order_field"];


//	check we have all the data
if( ($the_table=="") || ($sort_order=="") ){
	die("Error with datasss<br>".print_r($_GET));
}

//	split items
$ids = explode('|',$sort_order);
//print_r($ids);	
//	run the update query for each id
foreach($ids as $index=>$id){
	if($id != ''){
		$sql = "UPDATE ".$the_table." SET ".$order_field."=".$index." WHERE id = ".$id." LIMIT 1";
		mysql_query($sql) or die(mysql_error().'<br>'.$sql);
	}
}
?>