<?php
require_once('/home/www-data/private/defines.php');
require_once('../freemap_functions.php');
require_once('../../lib/functionsnew.php');

// title description type lat lon

// AJAX server script to add or retrieve annotations 
// Input: latitude and longitude of a clicked point, and associated 
// annotation data

session_start();
$pconn=pg_connect("dbname=freemap");
$username=null;
// If we provided HTTP authentication, check it
if(isset($_SERVER['PHP_AUTH_USER']))
{
	if(check_login($_SERVER['PHP_AUTH_USER'],
				   $_SERVER['PHP_AUTH_PW'],"freemap_users","pgsql"))
	{
		$username=$_SERVER['PHP_AUTH_USER'];
	}
	else
	{
		header("HTTP/1.1 401 Unauthorized");
		exit;
	}
}
elseif(isset($_SESSION['gatekeeper']))
	$username=$_SESSION['gatekeeper'];	

$id=($username!=null)?
	get_user_id($username,'freemap_users','username','id','pgsql') : 0;
$err=false;

// we want get and post input, but not anything else e.g. $_COOKIE
$getpost = array();
foreach ($_GET as $k=>$v)
	$getpost[$k] = $v;
foreach ($_POST as $k=>$v)
	$getpost[$k] = $v;

$cleaned = clean_input($getpost);

$expected_params = array (
	"add" => array("description","type","lat","lon"),
	"delete" => array ("id"),
	"get" => array("bbox"),
	"edit" => array("id","description"),
	"getById" => array ("id"),
	"getByCategory" => array ("q","by","format"),
	"getPhoto" => array ("id")
	);


$cleaned['action'] = (isset($cleaned['action'])) ? $cleaned['action']:'get';
$cleaned['bbox'] = (isset($cleaned['bbox'])) ? $cleaned['bbox']:"all";

if(isset($cleaned['action']) && in_array($cleaned['action'],
			array_keys($expected_params)))
{
	if(check_all_params_supplied($cleaned,
			$expected_params[$cleaned['action']]))
	{
		switch($cleaned['action'])
		{
			case "add":
					// find the nearest way to the annotation
					$way=0;
					$point=0;

					echo add_marker($cleaned['description'], 
							$cleaned['type'], $cleaned['lat'], $cleaned['lon'],
							$id,$way,$point);
				break;
			case "delete":
				if(!delete_marker($cleaned['id']))
					header("HTTP/1.1 404 Not Found");
				break;
			case "get":
				header("Content-type: text/xml");
				$markers=get_markers_by_bbox($cleaned['bbox']);
				to_georss($markers);
				break;
			case 'edit':
				edit_marker($cleaned['id'], $cleaned, 
							array("description"), $id);
				break;
			case 'getById':
				$marker=get_marker_by_id($cleaned['id']);
				marker_to_html($marker);
				break;

			case 'getByCategory':
				$markers=get_markers_by_category
					(explode(",",$cleaned['q']),explode(",",$cleaned['by']));
				switch($cleaned['format'])
				{
					case 'html':
						markers_to_html($markers, true);
						break;
					case 'rss':
						to_georss($markers);
						break;
				}
				break;

			case 'getPhoto':
				$marker=get_marker_by_id($cleaned['id']);
				if($marker['photoauth']==1 ||  (isset($_SESSION['gatekeeper'])
						&& get_user_level($_SESSION['gatekeeper'],
							'freemap_users','isadmin','username',
							'pgsql')==1))
				{
					if (!get_photo($cleaned['id'],
									$cleaned['width'],
									$cleaned['height']))
					{
						header("HTTP/1.1 404 Not Found");
					}
				}
				else
				{
					header("HTTP/1.1 401 Unauthorized");
				}
				break;

			case 'authorisePhoto':
				if (isset($_SESSION['gatekeeper']) &&
						get_user_level($_SESSION['gatekeeper'],'freemap_users',
						'isadmin','username','pgsql') == 1)
				{
					psql_query
						("UPDATE freemap_markers SET photoauth=1 WHERE ".
						 "id=$cleaned[id]");
					?>
					<html><body>
					Authorised.
					<a href='/freemap/admin.php'>Back to admin page</a>
					</body></html>
					<?php
				}
				break;
		}
	}
	else
	{
		header("HTTP/1.1 400 Bad Request");
	}
}
else
{
	header("HTTP/1.1 400 Bad Request");
}

pg_close($pconn);

?>
