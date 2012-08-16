<?php
//	active states
function active_state($state,$id,$table,$field='state'){
	global $icons,$lang;
	if($state==1) 		$icon=$icons["tick"];
	elseif($state==0) 	$icon=$icons["cross"];
	elseif($state==2) 	$icon=$icons["pending"];
	
	return '<span class="update_state" id="state_'.$id.'" rel="'.$table.'" state="'.$state.'" field="'.$field.'" title="'.$lang["click_update_state"].'">'.$icon.'</a>';
}
//	get item
function get_item($table,$id,$sql_condition=""){
	$sql="SELECT b.* FROM ".$table." AS b WHERE b.id='".$id."' $sql_condition LIMIT 1";
	$res=mysql_query($sql) or die("Error getting item.<br>".mysql_error());
	if(mysql_num_rows($res)==0) return false;
	else 						return mysql_fetch_assoc($res);
}

//	get last order number
function get_next_order($table){
	$sql="SELECT list_order FROM ".$table." WHERE state=1 ORDER BY list_order DESC";
	$res=mysql_query($sql) or die("Error getting highest list order");
	$row=mysql_fetch_assoc($res);
	return ($row["list_order"]+1);
}


//	add item
function add_item($table,$values,$debug=false){
	$add_data="";
	foreach($values AS $field=>$val){
		if($field=="password") 	$add_data.="`".$field."` = md5('".$val."'),";
		else 					$add_data.="`".$field."` = '".mysql_real_escape_string($val)."',";
	}
	$add_data=substr($add_data,0,-1);
	$add="INSERT INTO `".$table."` SET ".$add_data."";
	if($debug) echo $add."<br>";
	if(mysql_query($add)) 	return true;
	else{
		if($debug) echo "<br>".mysql_error();
		return false;
	}
}
//	modify item
function mod_item($table,$id_item,$values,$debug=false){
	$mod_data="";
	foreach($values AS $field=>$val){
		if($field=="password" && $val!="") 	$mod_data.="`".$field."` = md5('".$val."'),";
		else 								$mod_data.="`".$field."` = '".mysql_real_escape_string($val)."',";
	}
	$mod_data=substr($mod_data,0,-1);
	$update="UPDATE `".$table."` SET ".$mod_data." WHERE id='".$id_item."' LIMIT 1";
	if($debug) echo $update."<br>";
	if(mysql_query($update)) 	return true;
	else{
		if($debug) echo "<br>".mysql_error();
		return false;
	}
}

function delete_item($table,$id,$debug=false){
	$del="DELETE FROM ".$table." WHERE id='".$id."' LIMIT 1";
	if($debug) echo $del."<br>";
	if(mysql_query($del)) 	return true;
	else 					return false;
}

// multi_array_key_exists function.
function multi_array_key_exists( $needle, $haystack ) {
	foreach ( $haystack as $key => $value ) :
		if ( $needle == $key )
            return true;
        if ( is_array( $value ) ) :
             if ( multi_array_key_exists( $needle, $value ) == true )
                return true;
             else
                 continue;
        endif;
    endforeach;
    return false;
} 
?>