<?php
/*
script:	Ajax availability calendar
author: Chris Bolson

file: 	functions.inc.php
use: 	common functions for all pages
mod:	2012-03-08
*/

// set local time for newer php installations that require it (function at bottom of page)
date_default_timezone_set(getLocalTimezone());


//	 create calendar for given month
function draw_cal($id_item,$month,$year,$manage_type="avail"){
	global $lang;
	
	$month=sprintf("%02s",$month);
	//	define vars
	$today_timestamp	=   mktime(0,0,0,date('m'),date('d'),date('Y'));	# 	current timestamp - used to check if date is in past
	$this_month 		= 	getDate(mktime(0, 0, 0, $month, 1, $year));		# 	convert month to timestamp
	$first_week_day 	= $this_month["wday"];								# 	define first weekday (0-6)  
	$days_in_this_month = cal_days_in_month(CAL_GREGORIAN,$month,$year);	#	define number of days in week
	$day_counter_tot	=	0; #	count total number of days showin INCLUDING previous and next months - use to get 6th row of dates
	
	//	get num days in previous month - used to add dates to "empty" cells
	$month_last	= $month-1;
	$year_last	= $year;
	if($month_last<1){
		$month_last=12;
		$year_last=$year-1;	
	}
	$days_in_last_month = cal_days_in_month(CAL_GREGORIAN,$month_last,$year_last);
	
	//	day column titles - using first letter of each day
	if($show_week_num)	$list_day_titles='<li class="weeknum_spacer"></li>';
	
	if(AC_START_DAY=="sun"){
		for($k=0; $k<7; $k++){
			$weekday = mb_substr($lang["day_".$k.""],0,1,'UTF-8');
			$list_day_titles.='<li class="cal_weekday"> '.$weekday.'</li>';
		}
	}else{
		if ($first_week_day == 0)	$first_week_day =7;
		for($k=1; $k<=7; $k++){
			if($k==7) 	$weekday = mb_substr($lang["day_0"][0],0,1,'UTF-8');
			else		$weekday = mb_substr($lang["day_".$k.""],0,1,'UTF-8');
			$list_day_titles.='<li title="'.$lang["day_".$k.""].'"> '.$weekday.'</li>';
		}
	}
	
	
	//	Fill the first week of the month with the appropriate number of blanks.       
	$j=1;
	if(AC_START_DAY=="sun")	$first_week_day_start	=	$first_week_day;	# start sunday
	else						$first_week_day			=	$first_week_day-1;	# start monday
	
	
	$row_counter=0;
	
	if($first_week_day!=7){
		if($show_week_num)	$list_days.='<li class="weeknum">-</li>';
		$last_month_start_num=$days_in_last_month-$first_week_day+1;
		for($week_day = 0; $week_day < $first_week_day; $week_day++){
			$list_days.='<li class="cal_empty">'.$last_month_start_num.'</li>';   
			++$last_month_start_num;
			++$j;
			++$day_counter_tot;
			
			if($day_counter_tot % 7==1) ++$row_counter;
		}
	}
	$week_day=$j;
	
	//	get bookings for this month and item from database
	$booked_days=array();
	$sql = "
	SELECT 
		t1.the_date,
		t2.class,
		t2.desc_".AC_LANG." AS the_state
	FROM 
		".T_BOOKINGS." AS t1
		LEFT JOIN ".T_BOOKING_STATES." AS t2 ON t2.id=t1.id_state
	WHERE 
		t1.id_item=".$id_item." 
		AND MONTH(t1.the_date)=".$month." 
		AND YEAR(t1.the_date)=".$year."
	";
	if(!$res=mysql_query($sql))	die("ERROR checking id item availability dates<br>".mysql_error());
	while($row=mysql_fetch_assoc($res)){
		$booked_days[$row["the_date"]]=array("class"=>$row["class"],"state"=>$row["the_state"]);
	}
	
	
	
	//	loop thorugh days (til max in month) to draw calendar
	for($day_counter = 1; $day_counter <= $days_in_this_month; $day_counter++){
		//	reset xtra classes for each day
		//	note - these classes acumulate for each day according to state, current and clickable
		$day_classes 	=	"";
		$day_title_state=	" - ".$lang["available"];
		
		//	set all dates to clickable for now.... need to control this for admin OR for user side booking		
		$day_classes.=' clickable';
		
		
		//	turn date into timestamp for comparison with current timestamp (defined above)
		$date_timestamp =   mktime(0,0,0, $month,($day_counter),$year);
		
		//	get week number
		$week_num=date("W",$date_timestamp);
		if($week_num!=$last_week_num){
			//	new week
			//$list_days .= '<li>-</li>';
		}
		//	highlight current day
		if($date_timestamp==$today_timestamp)	$day_classes.=' today';
		
		//	format date for db modifying - the date is passed via ajax
		$date_db		=	$year."-".sprintf("%02s",$month)."-".sprintf("%02s",$day_counter);
        
        //	format date for display only
        if(AC_DATE_DISPLAY_FORMAT=="us")	$date_format	=	$month."/".$day_counter."/".$year;
        else 			        			$date_format	=	$day_counter."/".$month."/".$year;
        
		//	check if day is available
		if(array_key_exists($date_db,$booked_days)){
			$day_classes.=" ".$booked_days[$date_db]["class"];
			$day_title_state=" - ".$booked_days[$date_db]["state"];
		}
					
		
		//	check if date is past			
		if( $date_timestamp<$today_timestamp){
			$day_classes.=" past";	#add "past" class to be modified via mootools if required
			//	overwrite clickable state if CLICKABLE_PAST is off
			if(AC_ACTIVE_PAST_DATES=="off"){
				//	date is previous - strip out "clickable" from classes
				$day_classes=str_replace(' clickable','',$day_classes);
			}
		}
		
		//	add weekend class - used in javascript to alter class or set opacity
		$getdate=getdate($date_timestamp);
		$day_num=$getdate["wday"]+1;
		if ($day_num % 7 == 1)		$day_classes.=' weekend';
		elseif ($day_num % 6 == 1)	$day_classes.=' weekend';
		
		//'.$lang["day_".$getdate["wday"].""].'
		$list_days .= '
		<li class="'.$day_classes.' "  id="date_'.$date_db.'" title="'.$date_format.$day_title_state.'" data-date="'.$date_format.'">'.$day_counter.'</li>';
		
		//	reset weekday counter if 7 (6)
		$week_day %= 7;			#	reset weekday to 0
		++$week_day;			#	increase weekday counter
		++$day_counter_tot;		#	add 1 to total days shown
		//echo "<br>".$week_day;
		if($show_week_num){
			if ($week_day==1) $list_days .= '<li class="weeknum">'.$week_num.'</li>';
		}
		$last_week_num=$week_num;
		if($day_counter_tot % 7==1) ++$row_counter;
	}
	//	add empty days till end of row
	$next_month_day=1;

	while($row_counter<6){
		//add days until it does :)
		for($till_day = $week_day; $till_day <=7; $till_day++){
			$list_days .= '<li class="cal_empty">'.$next_month_day.'</li>'; 
			++$next_month_day;  
			++$day_counter_tot;		#	add 1 to total days shown
			
		if($day_counter_tot % 7==1) ++$row_counter;
		}
		$week_day=1;

	}
	//	add empty dates (with next month numbers) until we get to 7
	if($week_day > 1){
		for($till_day = $week_day; $till_day <=7; $till_day++){
			$list_days .= '<li class="cal_empty">'.$next_month_day.'</li>'; 
			++$next_month_day;  
			++$day_counter_tot;		#	add 1 to total days shown
		}
	}
	
	
	//	put it all together (parent div defined in parent file)
	$the_cal='
	<div id="'.$month.'_'.$year.'" class="cal_title">'.$lang["month_".$month.""].' '.$year.'</div>
	<ul class="cal_weekday">
		'.$list_day_titles.'
	</ul>
	<ul>
		'.$list_days.'
	</ul>
	<div class="clear"></div>
	';
	return $the_cal;
}


function get_cal_update_date($id_item){
	if(AC_DATE_DISPLAY_FORMAT=="us")	$date_format	= "%m-%d-%Y";
	else 								$date_format	= "%d-%m-%Y";
	
	$sql="SELECT DATE_FORMAT(date_mod, '".$date_format."') as date_mod FROM `".T_BOOKING_UPDATE."` WHERE id_item=".$id_item."";
	$res=mysql_query($sql) or die("error getting last calendar update date");
	$row=mysql_fetch_assoc($res);
	return $row["date_mod"];
}
//	get calendar items for select list
function sel_list_items($id_item_current){
	$list_items="";
	$sql="SELECT id, desc_".AC_LANG." as the_item FROM ".T_BOOKINGS_ITEMS." WHERE state=1 ORDER BY list_order";
	$res=mysql_query($sql) or die("Error checking items");
	while($row=mysql_fetch_assoc($res)){
		$list_items.='<option value="'.$row["id"].'"';
		if($row["id"]==$id_item_current) $list_items.=' selected="selected"';
		$list_items.='>'.$row["the_item"].'</option>';
	}
	return $list_items;
}

function list_numbers($start,$end,$num){
	$list_numbers='';
	for($k=$start;$k<=$end;$k++){
		$list_numbers.='<option value="'.$k.'"';
		if($k==$num) $list_numbers.=' selected="selected"';
		$list_numbers.='>'.$k.'</option>';
	}
	return $list_numbers;
}
//	get item title
function itemTitle($id){
	$sql="SELECT desc_".AC_LANG." as item_title FROM ".T_BOOKINGS_ITEMS." WHERE id=".$id."";
	$res=mysql_query($sql) or die("Error getting item name");
	$row=mysql_fetch_assoc($res);
	return $row["item_title"];
}
// set time zone for newer php installations
function getLocalTimezone(){
  	$iTime = time();
    $arr = localtime($iTime);
    $arr[5] += 1900;
    $arr[4]++;
    $iTztime = gmmktime($arr[2], $arr[1], $arr[0], $arr[4], $arr[3], $arr[5]);
   	$offset = doubleval(($iTztime-$iTime)/(60*60));
    $zonelist = array (
        'Kwajalein' 			=> -12.00,
        'Pacific/Midway' 		=> -11.00,
        'Pacific/Honolulu' 		=> -10.00,
        'America/Anchorage' 	=> -9.00,
        'America/Los_Angeles' 	=> -8.00,
        'America/Denver' 		=> -7.00,
        'America/Tegucigalpa' 	=> -6.00,
        'America/New_York' 		=> -5.00,
        'America/Caracas' 		=> -4.30,
        'America/Halifax' 		=> -4.00,
        'America/St_Johns' 		=> -3.30,
        'America/Argentina/Buenos_Aires' => -3.00,
        'America/Sao_Paulo' 	=> -3.00,
        'Atlantic/South_Georgia'=> -2.00,
        'Atlantic/Azores' 		=> -1.00,
        'Europe/Dublin' 		=> 0,
        'Europe/Belgrade' 		=> 1.00,
        'Europe/Minsk' 			=> 2.00,
        'Asia/Kuwait' 			=> 3.00,
        'Asia/Tehran' 			=> 3.30,
        'Asia/Muscat' 			=> 4.00,
        'Asia/Yekaterinburg' 	=> 5.00,
        'Asia/Kolkata' 			=> 5.30,
        'Asia/Katmandu' 		=> 5.45,
        'Asia/Dhaka' 			=> 6.00,
        'Asia/Rangoon' 			=> 6.30,
        'Asia/Krasnoyarsk' 		=> 7.00,
        'Asia/Brunei' 			=> 8.00,
        'Asia/Seoul' 			=> 9.00,
        'Australia/Darwin' 		=> 9.30,
        'Australia/Canberra' 	=> 10.00,
        'Asia/Magadan' 			=> 11.00,
        'Pacific/Fiji' 			=> 12.00,
        'Pacific/Tongatapu' 	=> 13.00
    );
    $index = array_keys($zonelist, $offset);
    if(sizeof($index)!=1){
        return false;
    }
    return $index[0];
}
?>
