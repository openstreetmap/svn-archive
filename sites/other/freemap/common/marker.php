<?php
include('functionsnew.php');
include('freemap_functions.php');

// Script to manage markers

$expected_params = array (
	"add" => array("lat","lon","type","description"),
	"get" => array("bbox"), 
	"edit" => array ("id"),
	"delete" => array ("id")
	);

session_start();
$conn=dbconnect("osm");
$userid=isset($_SESSION['username'])?  get_user_id($_SESSION['username']) : 0;
$err=false;
$inp = clean_input($_REQUEST);
if (isset($inp['BBOX']))
{
	$inp['bbox'] = $inp['BBOX'];
	unset($inp['BBOX']);
}

if(isset($inp['action']) && in_array($inp['action'],
			array_keys($expected_params)))
{
	if(check_all_params_supplied($inp,$expected_params[$inp['action']]))
	{
		switch ($inp['action'])
		{
			case 'add':
				add_new_marker($inp['lat'],$inp['lon'],$inp['type'],
								$inp['description'], $userid);
				break;

			case 'get':
				$bbox = explode(",",$inp['bbox']);
				if(count($bbox)==4)
				{
					$markers = get_markers($bbox[0],$bbox[1],$bbox[2],$bbox[3],
										$userid);
					if($markers)
						to_georss($markers);
				}
				break;
	
			case 'edit':
				edit_marker($inp['id'], $inp, $expected_params['add'], $userid);
				break;
					
			case 'delete':
				delete_marker($inp['id'], $userid);
				delete_from_table("freemap_markers",$inp['id'],"id");
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
