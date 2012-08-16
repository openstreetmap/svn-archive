<?php
//	define admin page table
$this_table=T_BOOKINGS_ITEMS;



//	delete item
if(isset($_POST["delete_it"])){
	if(delete_item($this_table,$_POST["id"])){
		//	delete bookings for this item
		$del="DELETE FROM ".T_BOOKINGS." WHERE id_item=".$_POST["id"]."";
		mysql_query($del) or die("Error deelting item bookings");
		header("Location:index.php?page=".ADMIN_PAGE."&msg=delete_OK");
	}else{
		$warning=$lang["msg_delete_KO"];
	}
}

//	modify item
if(isset($_POST["mod"])){
	if(mod_item($this_table,$_POST["id"],$_POST["mod"]))	header("Location:index.php?page=".ADMIN_PAGE."&id_modified=".$_POST["id"]."&msg=mod_OK");
	else 													$warning=$lang["msg_mod_KO"];
	
}

//	add new item
if(isset($_POST["add"])){
	//	define next list order
	$_POST["add"]["list_order"]	=get_next_order($this_table);
	$_POST["add"]["id_user"]	=$_SESSION["admin_id"];
	
	if(add_item($this_table,$_POST["add"],false))			header("Location:index.php?page=".ADMIN_PAGE."&id_added=".mysql_insert_id()."&msg=add_OK");
	else 													$warning=$lang["msg_add_KO"];
	
}


if(isset($_REQUEST["action"])){
	$xtra_moo		.=	'
	new FormCheck("item_form");
	';
	
	
	switch($_REQUEST["action"]){
		case "new":
			$page_title_add=' - '.$lang["title_add"];
			$contents.='
			<form method="post" id="item_form">
			<table>
				<tr>
					<td class="side">'.$lang["id_ref_external"].'</td>
					<td><input type="text" name="add[id_ref_external]" style="width:100px;"><span class="note">'.$lang["note_id_ref_external"].'</span></td>
				</tr>
				<tr><td colspan="2">&nbsp;</td></tr>
					';
				foreach($languages as $code=>$val){
					$contents.='
					<tr>
						<td class="side">'.$lang["desc"].' : '.strtoupper($code).'</td>
						<td><input type="text" name="add[desc_'.$code.']" value="" class="validate[\'required\',\'length[0,100]\'] text-input"></td>
					</tr>
					';
				}
				$contents.='
				<tr>
					<td>&nbsp;</td>
					<td><input type="submit" value="'.$lang["bt_add"].'"></td>
				</tr>
			</table>
			</form>
			';
			break;
		case "edit":
			//	get item data
			if(!$row=get_item($this_table,$_REQUEST["id"],$sql_condition)){
				//	item doesn't exist (or user doesn't have permission to see)
				$warning.=$lang["warning_item_not_exist"];
			}else{
				$page_title_add=' - '.$lang["title_mod"].' - '.strtoupper($row["desc_".AC_LANG.""]);
				$contents.='
				<form method="post" id="item_form">
				<input type="hidden" name="id" value="'.$_REQUEST["id"].'"> 
				<table>
					<tr>
						<td class="side">'.$lang["id_ref_external"].'</td>
						<td><input type="text" name="mod[id_ref_external]" value="'.$row["id_ref_external"].'" style="width:100px;"><span class="note">'.$lang["note_id_ref_external"].'</span></td>
					</tr>
					<tr><td colspan="2">&nbsp;</td></tr>
					';
					foreach($languages as $code=>$val){
						$contents.='
						<tr>
							<td class="side">'.$lang["desc"].' : '.strtoupper($code).'</td>
							<td><input type="text" name="mod[desc_'.$code.']" value="'.$row["desc_".$code.""].'" class="validate[\'required\',\'length[0,100]\'] text-input"></td>
						</tr>
						';
					}
					$contents.='				
					<tr>
						<td>&nbsp;</td>
						<td><input type="submit" value="'.$lang["bt_save_changes"].'"></td>
					</tr>
				</table>
				</form>
				';
			}
			break;
		case "delete":
			//	get item details
			if(!$row=get_item($this_table,$_REQUEST["id"],$sql_condition)){
				//	item doesn't exist (or user doesn't have permission to see)
				$warning.=$lang["warning_item_not_exist"];
			}else{
				$page_title_add	= ' - '.$lang["title_delete"].' - '.strtoupper($row["desc_".AC_LANG.""]);
				$contents.='
				<form method="post" onSubmit="return confirm(\''.$lang["warning_delete_confirm"].'\');">
				<input type="hidden" name="delete_it" value="1">
				<input type="hidden" name="id" value="'.$_REQUEST["id"].'"> 
				<table>
					';
					foreach($languages as $code=>$val){
						$contents.='
						<tr>
							<td class="side">'.$lang["desc"].' : '.strtoupper($code).'</td>
							<td><strong>'.$row["desc_".$code.""].'</strong></td>
						</tr>
						';
					}
					$contents.='				
					<tr>
						<td>&nbsp;</td>
						<td><input type="submit" value="'.$lang["bt_delete"].'"></td>
					</tr>
				</table>
				</form>
				';
			}
			break;
	}
}
if(!isset($_REQUEST["action"])){
	$bt_add='<a href="?page='.ADMIN_PAGE.'&action=new" title="'.$lang["tip_add_new_item"].'">'.$icons["add"].'</a>';
	
	$xtra_moo.="
	$$('.options img').addEvent('mouseover',function(event){
		this.highlight();
	});
	";
	
	$sql="SELECT b.*,u.username FROM ".$this_table." AS b LEFT JOIN ".T_BOOKINGS_ADMIN." AS u ON u.id=b.id_user WHERE b.id<>0 ".$sql_condition." ORDER BY b.state DESC, b.list_order";
	
	$res=mysql_query($sql) or die("Error getting states<br>".mysql_Error());
	//	define start message
	if(mysql_num_rows($res)==0)	$start_message=$lang["warning_no_calendar_items"];
	else 						$start_message=$lang["inst_drag"];
	$cols=6;
	while($row=mysql_fetch_assoc($res)){
		$item_modified="";
		if($row["id"]==$_REQUEST["id_modified"]) 	$item_modified='<span class="modified">'.$lang["item_modified"].'</span>';
		if($row["id"]==$_REQUEST["id_added"]) 		$item_modified='<span class="modified">'.$lang["item_added"].'</span>';
		
		$list_states.='
		<tr alt="'.$row["id"].'" >
			<td class="handles" title="'.$lang["drag_to_order"].'"></td>
			<td class="center">'.$row["id"].'</td>
			<td>'.$row["username"].'</td>
			<!--<td class="center">'.$row["id_ref_external"].'</td>-->
			<td>'.$row["desc_".AC_LANG.""].' '.$item_modified.'</td>
			<td class="center">'.active_state($row["state"],$row["id"],$this_table).'</td>
			<td class="options">
				<a href="?page='.ADMIN_PAGE.'&action=edit&id='.$row["id"].'" title="'.$lang["tip_edit_item"].'">'.$icons["edit"].'</a>
				<a href="?page=bookings&id_item='.$row["id"].'" title="'.$lang["tip_see_item_calendar"].'">'.$icons["calendar"].'</a>
				<a href="?page='.ADMIN_PAGE.'&action=delete&id='.$row["id"].'" title="'.$lang["tip_delete_item"].'">'.$icons["delete"].'</a>
			</td>
		</tr>
		';
	}
	
	$contents.='
	<input type="hidden" name="sort_order" id="sort_order" value="">
	<table class="list" id="sortable" field="list_order" table="'.$this_table.'">
		<thead>
		<tr>
			<td class="spacer">&nbsp;</td>
			<td style="width:40px;">'.$lang["id"].'</td>
			<td style="width:160px;">'.$lang["username"].'</td>
			<!--<td style="width:100px;">'.$lang["id_ref_external"].'</td>-->
			<td>'.$lang["item"].'</td>
			<td class="states">'.$lang["state"].'</td>
			<td class="options">'.$lang["options"].'</td>
		</tr>
		</thead>
		<tbody>
		'.$list_states.'
		</tbody>
		<tfoot>
		<tr>
			<td colspan="'.($cols-1).'" class="spacer"><span class="note">'.$start_message.'</span></td>
			<td class="center">'.$bt_add.'</td>
		</tr>
		</tfoot>
	</table>
	';
	
}
?>