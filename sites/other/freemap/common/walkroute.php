<?php
include('functionsnew.php');
include('freemap_functions.php');

// Script to manage markers

$expected_params = array (
	"add" => array("lats","lons","visibility","title","description"),
	"getById" => array ("id","format"), 
	"get" => array ("bbox"), 
	"annotate" => array ("id","lat","lon","annotation"),
	"delete" => array ("id"),
	"edit"=>array("id"),
	"getmine"=>array(),
	);

$edit_params = array ("description","title");

session_start();
$conn=dbconnect('osm');
$userid=isset($_SESSION['gatekeeper'])?  
	get_user_id($_SESSION['gatekeeper'],'freemap_users') : 0;
$err=false;
$inp = clean_input($_REQUEST);

if(isset($inp['action']) && in_array($inp['action'],
			array_keys($expected_params)))
{
	if(check_all_params_supplied($inp,$expected_params[$inp['action']]))
	{
		switch ($inp['action'])
		{
			case 'add':
				if($inp['visibility']=='public' ||
					($inp['visibility']=='private' && $userid>0) )
				{
					$id= add_new_walkroute
						($inp['lats'],$inp['lons'],$inp['title'],
							$inp['description'],$userid,
							($inp['visibility']=='private') ? 1:0);
					echo $id;
				}
				else
				{
					echo 0; 
				}
				break;

			case 'getById':
				$wr = get_walkroute_by_id($inp['id'], $userid);
				if($wr)
				{
					header("Content-type: text/xml");
					header("Content-disposition: attachment; filename=".str_replace(" ","_",$wr['title']).".gpx");
					if($inp['format']=='gpx')
						walkroute_to_gpx($wr);
					else
						walkroute_to_xml($wr);
				}
				else
				{
					echo "No public walkroute with that ID";
				}
				break;

			case 'get':
				$bbox = explode(",",$inp['bbox']);
				if(count($bbox)==4)
				{
					$startpoints = get_walkroute_startpoints
							($bbox[0],$bbox[1],$bbox[2],$bbox[3],$userid);
					if($startpoints)
						to_georss($startpoints);
				}
				break;

			case 'getmine':

				if($userid>0)
				{
					$walkroutes = get_user_walkroutes($userid);
					?>
					<html>
					<head>
					<link rel='stylesheet' type='text/css' 
						href='/css/freemap2.css'/>
					<title><?php echo $_SESSION['gatekeeper']?>'s Routes</title>
					</head>
					<body>
					<h1><?php echo $_SESSION['gatekeeper']?>'s Routes</h1>
					<?php
					show_user_walkroutes($walkroutes);
					?>
					</body>
					</html>
					<?php
				}
				else
				{
					echo "You're not logged in.";
				}
				break;

			case 'annotate':
				annotate_walkroute_point
					($inp['id'],$inp['lat'],$inp['lon'],$inp['annotation'],
							$userid);
				break;

			case 'edit':
				edit_walkroute($inp['id'], $inp, $edit_params, $userid);
				break;
					
			case 'delete':
				delete_walkroute($inp['id'], $userid);
				break;

		}
	}
	else
	{
		echo ("Not all parameters were supplied for the requested action!");
		$err=true;
	}
}
else
{
	echo("Either no action or an unrecognised action was supplied");
	$err=true;
}
mysql_close($conn);


if(isset($inp['redirect']) && $err==false)
{
	header("Location: $inp[redirect]");
}

?>
