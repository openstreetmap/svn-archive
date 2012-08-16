<?php
//	NOTE - $row_config is defined in the common file
if(isset($_POST["mod"])){
	$mod_item=mod_item(T_BOOKINGS_CONFIG,1,$_POST["mod"],false);
	if($mod_item=="OK"){
		//	reload to get fresh values and show message
		$url="index.php?page=".ADMIN_PAGE."&msg=mod_OK";
		header("Location:".$url."");
	}else{
		$warning=$lang["msg_mod_KO"];
		//echo mysql_error();
	}
}

//	get possible themes
$list_themes="";
$dir=AC_DIR_THEMES_ROOT;
if ($handle = opendir($dir)) {
	while (false !== ($file = readdir($handle))) { 
    	if ($file != "." && $file != "..") {
	    	//	define select list of themes
	    	if($row_config["theme"]==$file) 	$selected=' selected="selected"';
			else $selected="";
			$list_themes.="<option value='".$file."' ".$selected.">".$file."</option>\n";
		}
   	}
	closedir($handle); 
}else{
	$warning.="Unable to open themes directory: ".$dir."";
}

$contents.='
<form method="post" action="">
<input type="hidden" name="page" value="'.ADMIN_PAGE.'">
<table>
	<tr>
		<td class="side">'.$lang["title"].'</td>
		<td><input type="text" name="mod[title]" value="'.$row_config["title"].'" style="width:99%;"></td>
	</tr>
	<tr>
		<td class="side">'.$lang["cal_url"].'</td>
		<td><input type="text" name="mod[cal_url]" value="'.$row_config["cal_url"].'" style="width:99%;"></td>
	</tr>
	<tr>
		<td class="side">'.$lang["default_lang"].'</td>
		<td>
			<select name="mod[default_lang]" class="select" style="width:140px;">
				'.$list_languages_config.'
			</select>
		</td>
	</tr>
	<tr>
		<td class="side">'.$lang["num_months"].'</td>
		<td>
			<select name="mod[num_months]" class="select" style="width:140px;">
				'.list_numbers(1,12,$row_config["num_months"]).'
			</select>
		</td>
	</tr>
	<tr>
		<td class="side">'.$lang["start_day"].'</td>
		<td>
			<select name="mod[start_day]" class="select" style="width:140px;">
				<option value="sun"'; if($row_config["start_day"]=="sun") $contents.=' selected="selected"'; $contents.='>'.$lang["day_0"].'</option>
				<option value="mon"'; if($row_config["start_day"]=="mon") $contents.=' selected="selected"'; $contents.='>'.$lang["day_1"].'</option>
			</select>
		</td>
	</tr>
	<tr>
		<td class="side">'.$lang["date_format"].'</td>
		<td>
			<select name="mod[date_format]" class="select" style="width:140px;">
				<option value="us"'; if($row_config["date_format"]=="us") $contents.=' selected="selected"'; $contents.='>'.$lang["date_format_us"].'</option>
				<option value="eu"'; if($row_config["date_format"]=="eu") $contents.=' selected="selected"'; $contents.='>'.$lang["date_format_eu"].'</option>
			</select>
		</td>
	</tr>
	<tr>
		<td class="side">'.$lang["click_past_dates"].'</td>
		<td>
			<label><input type="radio" name="mod[click_past_dates]" value="on"'; if($row_config["click_past_dates"]=="on")	$contents.=' checked="checked"'; $contents.='>'.$lang["yes"].'</label>
			<label><input type="radio" name="mod[click_past_dates]" value="off"'; if($row_config["click_past_dates"]=="off") $contents.=' checked="checked"'; $contents.='>'.$lang["no"].'</label>
		</td>
	</tr>
	<tr>
		<td class="side">'.$lang["theme"].'</td>
		<td>
			<select name="mod[theme]" class="select" style="width:140px;">
				'.$list_themes.'
			</select>
		</td>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td><input type="submit" value="'.$lang["bt_save_changes"].'" style="width:140px;"></td>
	</tr>
	
</table>
</form>
';
?>