<?php
$page_title=$lang["".PAGE."_title"];
$contents.="login";
if(isset($_POST["username"])){
	//	check login details
	//if OK, set session vars and reload page
	$username = $_POST["username"];
	$password = $_POST["password"];
	
	$sql="SELECT id,username,level FROM ".T_BOOKINGS_ADMIN." WHERE username='".mysql_real_escape_string($username)."' AND password='".md5(mysql_real_escape_string($password))."' AND state=1";
	$res=mysql_query($sql) or die("Error checking admin user<br>".mysql_Error());
	if(mysql_num_rows($res)==0){
		$warning="User not valid";
	}else{
		$row=mysql_fetch_assoc($res);
		$_SESSION["admin_id"]	=	$row["id"];
		$_SESSION["admin_name"]	=	$row["username"];
		$_SESSION["admin_lang"]	=	$_POST["lang"];
		$_SESSION["admin_level"]=	$row["level"];
		//	update table with visit
		$update="UPDATE ".T_BOOKINGS_ADMIN." SET date_visit=now(), visits=visits+1 WHERE id=".$row["id"]." LIMIT 1";
		mysql_query($update) or die("error updating user visit stats");
		
		header("Location:index.php");
	}
}else{
	$username = "";
	$password = "";
}


//	define login form
$contents='
<div id="login">
	<h2>'.$lang["admin_login"].'</h2>
	<div class="inner">
		<form method="post" action="index.php">
		<table>
			<tr>
				<td class="side">Name</td>
				<td><input type="text" name="username" value="'.$username.'" tabindex="1"></td>
			</tr>
			<tr>
				<td class="side">Password</td>
				<td><input type="password" name="password" value="'.$password.'" tabindex="2"></td>
			</tr>
			<tr>
				<td class="side">Language</td>
				<td>
					<select name="lang" class="select" tabindex="3">
						'.$list_languages_config.'
					</select>
					<input type="submit" value="Login" style="width:100px;" tabindex="4">
				</td>
			</tr>	
		</table>
		</form>
	</div>
</div>
';
?>