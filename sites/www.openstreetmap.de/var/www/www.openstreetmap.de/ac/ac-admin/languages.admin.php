<?php
if(isset($_POST["new_code"])){
	//$warning="go through db to find lang fields.....";

	/*
	1	-	get all tables
	2	-	get all fields in table
	3	-	IF field LIKE "_en" then we have lang column
	4	-	get column name substr(field_name,0,-3) - removing last 3 chars
	5	-	add new column with this name and NEW lang code
	6	-	copy data from lang defined col to new col - NO
	*/

	//	 vars to be sent in form
	$new_lang	=	$_POST["new_code"];	# new code for lang#
	
	//	 get all tables
	$sql_db="SHOW TABLES FROM ".AC_DB_NAME."";
	$res_db = mysql_query($sql_db) or die("Error getting DB TABLES");
	while ($row_db = mysql_fetch_row($res_db)) {
		//echo "Table: {$row[0]}\n";
	   	// now get fields within tables
	   	$this_table=$row_db[0];
		$sql_tables="SHOW COLUMNS FROM ".$row_db[0]."";
	  	$res_table = mysql_query($sql_tables) or die("Error getting TABLE COLUMNS");
		if (mysql_num_rows($res_table) > 0) {
	   		
			while ($row_table= mysql_fetch_assoc($res_table)) {
				//	 check if column looks like a lang column
		  		$this_field	=	$row_table["Field"];
				$this_type	=	$row_table["Type"];
				
				if(substr($this_field,-3,3)=="_en"){
					//	 is a lang column, now get name (without lang code)
					$this_lang_desc		=	substr($this_field,0,-2);		#	remove lang to get get base col desc
					$new_lang_col		=	$this_lang_desc.$new_lang;		#	define new col name
					
					//	 check if column exist allready
					$sql_check="SELECT ".$new_lang_col." FROM ".$this_table." LIMIT 1";
					if($res_check=mysql_query($sql_check)){
						// if it returns a result that means it is already there
						$warning="<br><span class='warning'>COLUMN ".$new_lang_col." already existe in table ".$this_table."</span>";
					}else{
						//	 now add new lang to db
						$add="ALTER TABLE `".$this_table."` ADD `".$new_lang_col."` ".$this_type."  NOT NULL";
						//echo "<br>ADD NEW COLUMN - ".$add;
						mysql_query($add) or die("Error ADDING AC_LANGUAGE");
						$warning.="New language field \"".$new_lang_col."\" added to the \"".$this_table."\" table<br>";
					}		#	if column exists
		   		}			#	detect if lang col (use _en)
			}				#	while table fields
		}					#	if results
	}						#	while tables
	
	//	 now copy lang file with new lang extension
	$file 		= 	AC_DIR_AC_LANG."en.lang.php";
	$newfile 	= 	AC_DIR_AC_LANG.$new_lang.".lang.php";	
	if (!copy($file, $newfile)){
		$warning.="failed to copy $file...<br>";
		$error	=true;
	}
	//else							//$warning.="Lang file <strong>".$new_lang."</strong> created<br>";
	
	//	 set file permisions for writing
	if(!chmod($newfile, 0777)){
		$warning.="Unable to change file permisions<br>";
		$error	=true;
	}
	//else		$warning.="File permisions modified.<br>";
	
	if(!isset($error)) header("Location:index.php?page=".ADMIN_PAGE."&id_added=".$new_lang."&msg=add_OK");
}

//	modify item
if(isset($_POST["modify_it"])){

	//	remove slashes added by php settings
	function stripslashes_deep($value){
	   return (is_array($value) ? array_map('stripslashes_deep', $value) : stripslashes($value));
	}
	if (get_magic_quotes_gpc()){
	   $_GET    = array_map('stripslashes_deep', $_GET);
	   $_POST  = array_map('stripslashes_deep', $_POST);
	   $_COOKIE = array_map('stripslashes_deep', $_COOKIE);
	} 

	//print_r($_POST["new_value"]);
	$filename =AC_DIR_AC_LANG.$_REQUEST["code"].'.lang.php';
	
	// open file and rewrite
	$mod_page="<?php\n \$lang=array();\n";
	
	foreach($_POST["new_value"] as $key=>$val){
		//$mod_page.="\"".$key."\" => \"".addslashes(trim($val))."\",\n";
		$val	=	trim($val);
		$val	=	str_replace('"','\"',$val);
		$val	=	str_replace('$','\$',$val);
		
		$val	=	htmlspecialchars($val,ENT_QUOTES);
		$val	=	str_replace("'","\'",$val);
		$mod_page.="\$lang[\"".$key."\"] = \"".$val."\";\n";
	}
	// remove last ","
	$mod_page .= "\n?>";
	//echo $mod_page;
	//exit;
	
	if(!$handle = @fopen($filename, "w"))	die ("<br>Cannot open lang file ".$filename."");
	if(!@fwrite($handle, $mod_page)) 	die("<br>CAN'T WRITE FILE - NO PERMISSION");
	else	$warning="AC_LANGUAGE FILE ".$_REQUEST["lang_file"]." MODIFIED";
	fclose($handle);
}




//	delete item
if(isset($_POST["delete_it"])){
	//	delete lang file and db fields
	if(file_exists(DIR_AC_LANG.$_REQUEST["code"].".lang.php")){
		unlink(DIR_AC_LANG.$_REQUEST["code"].".lang.php");
		//	we should remove the language fields from the database at this point - TO DO
		header("Location:index.php?page=".ADMIN_PAGE."&msg=delete_OK");
	}else{
		$warning=$lang["msg_delete_KO"];
	} 	
												
}


if(isset($_REQUEST["action"])){
	switch($_REQUEST["action"]){
		case "new":
			$xtra_moo		.=	'new FormCheck("item_form")';
			$contents.='
			<form method="post" id="item_form" onSubmit="return confirm(\''.$lang["warning_new_lang_confirm"].'\');">
			<table>
				<tr>
					<td class="side">'.$lang["new_lang_code"].'</td>
					<td style="width:80px;"><input type="text" name="new_code" size="3" maxlength="3" class="validate[\'required\'] text-input">
					<td><input type="submit" value="'.$lang["bt_add"].'"> </td>
				</tr>
				<tr>
					<td colspan="3" class="note">'.$lang["note_add_language"].'</td>
				</tr>
			</table>
			</form>
			';
			break;
		case "edit":
			$xtra_js_files.='
			<script type="text/javascript" src="js/mootools-flext.js"></script>
			';
			//	open lang file AND english version (for reference)
			//	texts for this page - var conflict
		//	$file_to_open	= 	$lang["lang_to_modify"];
		//	$translate		=	$lang["translation"];
		//	$finish			=	$lang["finish"];
			
			
			//	define lang file to open
			$filename = AC_DIR_AC_LANG.$_REQUEST["code"];
			$this_lang	=	$_REQUEST["code"];
			$orig_lang	=	"en";
			
			//	include english version for reference
			//if(AC_LANG!="en") include(DIR_AC_LANG.$orig_lang."lang.php");
			
			//	temp save user lang as it will be overwritten when we get the lang file to modify
			$user_lang=$lang;
			//	check lang file is writable
			if(!is_writable(AC_DIR_AC_LANG.$_REQUEST["code"].".lang.php")){
				$contents.='
				<div class="warning">
					Unable to modifiy the language file that you have chosen as you do not have "write" permissions.<br>
					To be able to modify the language files you most change the permissions to chmod 777.
					</div>
				';
			}else{
				//	get lang file to modify
				include(AC_DIR_AC_LANG.$_REQUEST["code"].".lang.php");
				//	rename lang var (array) for editing
				$lang_to_modify=$lang;
				
				//	reset user lang var
				$lang=$user_lang;
				
				$lang_vars="";
				foreach($lang_to_modify as $key=>$val){
					$val	=	str_replace("\'","&lsquo;",$val);	# convert chars to html asci;
					if(strlen($val)>60)	$input="<textarea name='new_value[".$key."]' style='width:99%;height:60px;' class='flext growme'>".$val."</textarea>";
					else				$input="<input type='text' style=' width:99%;' name='new_value[".$key."]' value='".$val."' >";
					$lang_vars.='
					<tr>
						<td class="side">'.$key.'</td>
						<td class="spacer">'.$input.'</td>
					</tr>
					 ';
					 //<td class='data'><em>".$lang_en[$key]."</em></td><td class='data'><em>".$lang_en[$key]."</em></td>
				}
				$page_title_add	= ' - '.$lang["title_mod"].' - '.strtoupper($_REQUEST["code"]);
				$contents.='
				<form method="post" id="item_form">
				<input type="hidden" name="code" value="'.$_REQUEST["code"].'"> 
				<input type="hidden" name="modify_it" value="1">
				<table class="data">
					<thead>
					<tr>
						<td>Code</td>
						<td>'.$lang["language"].': '.$this_lang.'</td>
						<!--<td>English</td>-->
						
					</tr>
					</thead>
					'.$lang_vars.'
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
			$page_title_add	= ' - '.$lang["title_delete"].' - '.strtoupper($_REQUEST["code"]);
			$contents.='
			<form method="post" onSubmit="return confirm(\''.$lang["warning_delete_confirm"].'\');">
			<input type="hidden" name="delete_it" value="1">
			<input type="hidden" name="code" value="'.$_REQUEST["code"].'"> 
			<table>
				<tr>
					<td class="side">'.$lang["language"].'</td>
					<td class="data"><strong>'.$_REQUEST["code"].'</strong></td>
				</tr>				
				<tr>
					<td>&nbsp;</td>
					<td><input type="submit" value="'.$lang["bt_delete"].'"></td>
				</tr>
			</table>
			</form>
			';
			break;
	}
}



if(!isset($_REQUEST["action"])){
	//	list current languages to be edited via admin
	ksort($languages);
	foreach($languages AS $code=>$i){
		$item_modified="";
		if($code==$_REQUEST["id_modified"]) 	$item_modified='<span class="modified">'.$lang["item_modified"].'</span>';
		if($code==$_REQUEST["id_added"]) 		$item_modified='<span class="modified">'.$lang["item_added"].'</span>';
		
		$list_items.='
		<tr>
			<td>'.$code.' '.$item_modified.'</td>
			<td class="options">
				<a href="?page='.ADMIN_PAGE.'&action=edit&code='.$code.'" title="'.$lang["tip_edit_item"].'">'.$icons["edit"].'</a>
				<a href="?page='.ADMIN_PAGE.'&action=delete&code='.$code.'" title="'.$lang["tip_delete_item"].'">'.$icons["delete"].'</a>
			</td>
		</tr>
		';
	}
	$bt_add='<a href="?page='.ADMIN_PAGE.'&action=new" title="'.$lang["tip_add_new_item"].'">'.$icons["add"].'</a>';
	
	
	$contents.='
	<table class="list">
		<thead>
		<tr>
			<td>'.$lang["language"].'</td>
			<td class="options">'.$lang["options"].'</td>
		</tr>
		</thead>
		<tbody>
		'.$list_items.'
		<tfoot>
		<tr>
			<td colspan="'.($cols-1).'" class="spacer">&nbsp;</td>
			<td class="center">'.$bt_add.'</td>
		</tr>
		</tfoot>
	</table>	
	';
}
?>