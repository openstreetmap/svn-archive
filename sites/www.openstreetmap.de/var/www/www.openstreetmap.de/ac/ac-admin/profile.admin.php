<?php
/*
just for modifying current user - NOT active in superadmin account
*/
//	define admin page table
$this_table=T_BOOKINGS_ADMIN;


//	modify item
if(isset($_POST["mod"])){
	//	add password to array - this is not in the array as it is need for formchecking
	if($_POST["password"]!="")$_POST["mod"]["password"]=$_POST["password"]; 
	
	if(mod_item($this_table,$_SESSION["admin_id"],$_POST["mod"],false))	header("Location:index.php?page=".ADMIN_PAGE."&msg=mod_OK");
	else 																$warning=$lang["msg_mod_KO"];

}


$xtra_moo.='new FormCheck("item_form")';
//	get item data
$row=get_item($this_table,$_SESSION["admin_id"]);
$page_title_add=' - '.$lang["title_mod"].'';
$contents.='
<form method="post" id="item_form">
<table>
	<tr>
		<td class="side">'.$lang["username"].'</td>
		<td><input type="text" name="mod[username]" value="'.$row["username"].'" style="width:99%;" class="validate[\'required\',\'length[0,100]\'] text-input"></td>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td class="note">'.$lang["note_password_mod"].'</td>
	</tr>
	<tr>
		<td class="side top">'.$lang["password"].'</td>
		<td><input type="password" id="password" name="password" value="" style="width:99%;"></td>
	</tr>
	<tr>
		<td class="side top">'.$lang["password_repeat"].'</td>
		<td><input type="password" name="password2" value="" style="width:99%;" class="validate[\'confirm[password]\'] text-input"></td>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td><input type="submit" value="'.$lang["bt_save_changes"].'"></td>
	</tr>
</table>
</form>
';


?>