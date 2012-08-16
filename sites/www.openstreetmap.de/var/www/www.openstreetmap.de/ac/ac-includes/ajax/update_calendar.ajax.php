<?php
/***********************************************/
/*
File:			update_calendar.ajax.php
Author: 		cbolson.com 
Script: 		availability calendar
Version: 		3.03
Url: 			http://www.ajaxavailabilitycalendar.com
Date Created: 	2009-07-29   
Date Modified: 	2010-01-30
				
Use:			1. Get list of states from db in defined order.
				2. Get current state of date (and item) from db
				3. If no entry, create new row using first state from previously defined array
				4. If result, get "next()" item in array of states
				5. Update db to reflect new date
				6. If current state is last available - remove from database  - not booked
				7. Update db to reflect last update date for this item

Receives:		$_REQUEST["the_date"] 	- date to be modified
				$_REQUEST["id_item"]	- item to be modified
				$_REQUEST["lang"]		- language (uses default in config.inc.php if not set
*/
/***********************************************/


//	admin only access
$admin_only=true;

// include common file for ajax settings
$the_file=dirname(__FILE__)."/ajax-common.inc.php";
if(!file_exists($the_file)) die("<b>".$the_file."</b> not found");
else		require_once($the_file);

//	define request vars
$the_date	=	$_GET["the_date"];
$id_item	=	$_GET["id_item"];

//	clear cache to ensure data is up to date
header("Cache-Control: no-cache, must-revalidate"); // HTTP/1.1
header("Expires: Mon, 26 Jul 1997 05:00:00 GMT"); // Date in the past


//	check we have all the data
if( ($_REQUEST["id_item"]=="") || ($_REQUEST["the_date"]=="")){
	die("Error");
}

//$debug=true;
//	get states in order
$list_states=array();
$sql="SELECT * FROM ".T_BOOKING_STATES." WHERE state=1 ORDER BY list_order ASC";
$res=mysql_query($sql) or die("Error getting states");
while($row=mysql_fetch_assoc($res)){
	$list_states[$row["id"]]=array("class"=>"".$row["class"]."","desc"=>"".$row["desc_".AC_LANG.""]."");
}

if($_GET["id_state"]=="free"){
	//	remove from db
	$update="DELETE FROM ".T_BOOKINGS." WHERE id_item='".$id_item."' AND the_date='".$the_date."' LIMIT 1";
	$new_class="";
	$new_desc	=$lang["available"];
}else{
	
	//	get current state
	$sql="SELECT id_state FROM ".T_BOOKINGS." WHERE id_item='".$id_item."' AND the_date='".$the_date."'";
	$res=mysql_query($sql) or die("Error getting date data");
	if(mysql_num_rows($res)==0){
		//	new booking - define new state as first in $list_states;
		if($_GET["id_state"]!="")	$new_state	=	$_GET["id_state"];	//	admin has the option to "force" a state for all clicks
		else 						$new_state	=	key($list_states); 	//	should return the key (id) for the first item in the array
		if($debug) echo "<br>New state first key ".$new_state;
		$new_desc	=	$list_states[$new_state]["desc"];
		$new_class	=	$list_states[$new_state]["class"];
		$update		=	"INSERT INTO ".T_BOOKINGS." SET id_item='".$id_item."',the_date='".$the_date."', id_state='".$new_state."'";
	}else{
		$row=mysql_fetch_assoc($res);
		if($debug) echo "<br>".print_r($row);
		
		//	need to get next state in order
		$current_state_id=$row["id_state"];
		if($debug) echo "<br>Current ID: ".$current_state_id;
		
		//	loop though states array until we find this one
		foreach($list_states as $id=>$val){
			if($id==$current_state_id) break;
			//	advance the pointer to next
	 		next($list_states);
	 		//	stop if id is the same as current
	 		
		}
		
		//	define for db update
		if($_GET["id_state"]!="")	$new_state	=	$_GET["id_state"];
		else 						$new_state	=	key($list_states);
		if($debug) echo "<br>New State: ".$new_state;
		if($new_state==""){
			//	finished array - delete from db
			$update="DELETE FROM ".T_BOOKINGS." WHERE id_item='".$id_item."' AND the_date='".$the_date."' LIMIT 1";
			$new_class="";
			$new_desc	=$lang["available"];
		}else{
			$update="UPDATE ".T_BOOKINGS." SET id_state='".$new_state."' WHERE id_item='".$id_item."' AND the_date='".$the_date."' LIMIT 1";
			//	define for class to return
			$new_desc	=	$list_states[$new_state]["desc"];
			$new_class	=	$list_states[$new_state]["class"];
		}
		
	}
	//echo $update;
	
}

//	update db with new state
if(!mysql_query($update)) die("Error updating<br>".mysql_error()."<br>".$update);

//	update last update with now
$sql="SELECT * FROM `".T_BOOKING_UPDATE."` WHERE id_item='".$id_item."'";
if(!$res=mysql_query($sql)) die("ERROR GETTING CHECKING UPDATE DATE.<br>".mysql_error());
if(mysql_num_rows($res)==0)	$update="INSERT INTO `".T_BOOKING_UPDATE."` SET id_item='".$id_item."', date_mod=now()";
else						$update="UPDATE `".T_BOOKING_UPDATE."` SET date_mod=now() WHERE id_item='".$id_item."' LIMIT 1";
//echo $update;
mysql_query($update) or die("Error with last modified date");



if($debug) echo "<br>SQL: ".$update."<br>New Class: ";

//	format date for db modifying - the date is passed via ajax
$date_bits		=	explode("-",$the_date);
        
//	format date for display only
if(AC_DATE_DISPLAY_FORMAT=="us")	$date_format	=	$date_bits[1]."/".$date_bits[2]."/".$date_bits[0];
else 			        			$date_format	=	$date_bits[2]."/".$date_bits[1]."/".$date_bits[0];

echo $new_class."|".$date_format."|".$new_desc;
?>