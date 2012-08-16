<?php
//	get list of calendar items
$sql="SELECT b.id, b.desc_".AC_LANG." as the_item FROM ".T_BOOKINGS_ITEMS." AS b WHERE b.state=1 ".$sql_condition." ORDER BY b.list_order";
$res=mysql_query($sql) or die("Error checking items<br>".mysql_Error());
if(mysql_num_rows($res)==0){
	//	no items in db
	$warning=$lang["warning_no_active_items"];
}else{
	while($row=mysql_fetch_assoc($res)){
		//	create an array of items to be able to confirm that the calendar 
		$user_items[]=$row["id"];
		$list_items.='<option value="'.$row["id"].'"';
		if($row["id"]==$_REQUEST["id_item"]) $list_items.=' selected="selected"';
		$list_items.='>'.$row["the_item"].'</option>';
	}
	//print_r($user_items);
	if( ($_REQUEST["id_item"]) && (!in_array($_REQUEST["id_item"],$user_items)) ){
		$warning.="item doesn't exist";
	}else{
		if(!isset($_REQUEST["id_item"])) $_REQUEST["id_item"]=$user_items[0];	# get first item in array of user items
		$the_file=AC_INLCUDES_ROOT."cal.inc.php";
		if(!file_exists($the_file)) die("<b>".$the_file."</b> not found");
		else		require_once($the_file);
		
		$xtra_js_files.='<script type="text/javascript" src="js/mootools-cal-admin.js"></script>
		';
		
		//	define js vars for calendar
		$xtra_js.="
		var date_hover 			= true;	//	true=on, false=off
		var show_message 		= true; //	true=on, false=off
		
		var url_ajax_cal 		= '".AC_DIR_AJAX."calendar.ajax.php'; 	//	ajax file for loading calendar via ajax
		var url_ajax_update 	= '".AC_DIR_AJAX."update_calendar.ajax.php'; //	ajax file for update calendar state
		var img_loading_day		= '".AC_DIR_IMAGES."ajax-loader-day.gif';	//	animated gif for loading	
		var img_loading_month	= '".AC_DIR_IMAGES."ajax-loader-month.gif';//	animated gif for loading	

		//	don't change these values
		var lang			=	'".AC_LANG."';	//	language
		var id_item			=	'".ID_ITEM."';	//	id of item to be modified (via ajax)
		var months_to_show	=	".AC_NUM_MONTHS.";	//	number of months to show
		var clickable_past	=	'".AC_ACTIVE_PAST_DATES."';
		";
		
		//	get calendar items
		$db_items	= '';
		$array_items= '';
		
				
		$contents.='
		<form>
		<table>
			<tr>
				<input type="hidden" name="page" value="'.ADMIN_PAGE.'">
				<td class="side" style="width:100px;">'.$lang["item_to_show"].'</td>
				<td>
					<select name="id_item" class="select" onchange="this.form.submit();">
						'.$list_items.'
					</select>
					<input type="submit" value="'.$lang["bt_change_item"].'" style="">
				</td>
				<td>&nbsp;&nbsp;</td>
				<td><input type="button" value="'.$lang["bt_add_item"].'" style="" onclick="document.location.href=\'index.php?page=items&action=new\'"></td>
			</tr>
			<tr>
				<td class="side">'.$lang["click_method"].':</td>
				<td>
					<select id="id_predefined_state"  class="select" >
						<option value="">'.$lang["states_method_click_through"].'</option>
						'.$sel_list_states.'
						<option value="free">'.$lang["available"].'</option>
					</select>
				</td>
			</tr>
		</table>
		</form>
		<div id="cal_wrapper">
			
			<div id="cal_controls">
				<div id="cal_prev" title="'.$lang["prev_X_months"].'"><img src="'.AC_DIR_IMAGES.'icon_prev.gif" class="cal_button"></div>
				<div id="cal_next" title="'.$lang["next_X_months"].'"><img src="'.AC_DIR_IMAGES.'icon_next.gif" class="cal_button"></div>
				<div id="ajax_message">'.$lang["inst_calendar_click"].'</div>
				<div class="clear"></div>
			</div>
			<div id="the_months">
				'.$calendar_months.'
			</div>
			<div id="key_wrapper">
				'.$calendar_states.'
			</div>
		</div>
		';
	}
}
?>