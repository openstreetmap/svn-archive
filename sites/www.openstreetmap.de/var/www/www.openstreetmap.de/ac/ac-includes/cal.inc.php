<?php

/*
Author: cbolson.com 
Script: availability calendar
Version: 3.0
Url: http://www.cbolson.com/sandbox/availability-calender/v3.0/
Date Created: 29-07-2009   
File: cal.inc.php
Use: include months via ajax
*/



/****** INCLUDE FILES ******/

if(!isset($_SESSION["admin_id"])){
	//	general config
	$the_file="ac/ac-config.inc.php";
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
	if(!isset($_GET["lang"])) $_GET["lang"]=AC_DEFAULT_AC_LANG;
	define("AC_LANG", $_GET["lang"]);
	
	//	include lang file
	$numMonths=AC_NUM_MONTHS;
	$the_file=AC_DIR_AC_LANG.AC_LANG.".lang.php";
	if(!file_exists($the_file)) die("<b>".$the_file."</b> not found");
	else		require_once($the_file);
}


//	define id of item to modify
if(isset($_REQUEST["id_item"])) 		define("ID_ITEM",	$_REQUEST["id_item"]);	#	id sent via  url, form session etc
else{
	//	define default id manually
	//	define("ID_ITEM",	2);						
	//	or
	//	get first item from list
	$sql="SELECT id FROM ".T_BOOKINGS_ITEMS." WHERE state=1 ORDER BY list_order ASC LIMIT 1";
	$res=mysql_query($sql) or die("Error checking items<br>".mysql_Error());
	if(mysql_num_rows($res)==0){
		//	no items in db
		$no_id=true;
	}else{
		$row=mysql_fetch_assoc($res);
		define("ID_ITEM",	$row["id"]);
	}
}

if($no_id){
	//	no id - calendar hasn't been set up yet
	$calendar_months='
	<ul>
		<li>You have not yet added any calendar items to the database.</li>
		<li><a href="admin/index.php">Click here</a> to administer your calendar.</li>
	</ul>
	';
}else{
	//	define start month and year
	$this_year		=	date('Y');	# current year
	$this_month		=	date('m');	# current month
	
	//	create array of months from which to make calendars
	for($k=0; $k<AC_NUM_MONTHS; ++$k){
		
		//	add month layer to page - calendar loaded via ajax
		$calendar_months.='<div id="'.$this_month.'_'.$this_year.'" class="cal_month load_cal"></div>';
		if($this_month==12){
			//	start new year and reset month numbers
			$this_month	=	$this_month=1;	#	set to 1
			$this_year	=	$this_year+1;	#	add 1 to current year
		}else{
			++$this_month;
		}
	}
}


//	define calendar states for key
$list_states	= "";
$sel_list_states= "";

$sql="SELECT id,class,desc_".AC_LANG." AS the_desc FROM ".T_BOOKING_STATES." WHERE state=1 ORDER BY list_order ASC";
$res=mysql_query($sql) or die("Error getting states");
while($row=mysql_fetch_assoc($res)){
	$list_states.='<li class="'.$row["class"].'" title="'.$row["the_desc"].'"><span>'.$row["the_desc"].'</span></li>
	';
	$sel_list_states.='<option value="'.$row["id"].'">'.$row["the_desc"].'</option>';
}

$calendar_states='
<div id="key" class="cal_month">
	<div class="cal_title">'.$lang["legend"].'</div>
	<ul>
		<li><span>'.$lang["available"].'</span></li>
		'.$list_states.'
	</ul>
	<span style="font-size:0.8em; padding-left:4px;"><a href="http://www.ajaxavailabilitycalendar.com" title="Ajax Availability Calendar">Availability Calendar</a></span>
</div>
';
?>
