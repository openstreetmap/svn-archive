<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################

# 15/05/06 zoom no longer required: calculated from other input parameters

require_once('classes.php');
require_once('functions.php');

////////////////// SCRIPT BEGINS HERE /////////////////////

$defaults = array("WIDTH" => 400, 
			"HEIGHT" => 320,
			"landsat" => 1,
			"tp" => 0,
			"debug" => 0 );

$inp=array();

foreach ($defaults as $field=>$default)
{
	$inp[$field]=wholly_numeric($_GET[$field]) ?  $_GET[$field] : $default;
}

$bbox = explode(",",$_GET['BBOX']);
if(count($bbox)!=4)
{
	$error = "You need to supply a bounding box!";
}
elseif($bbox[0]<-180 || $bbox[0]>180 || $bbox[2]<-180 || $bbox[2]>180 ||
	 $bbox[1]<-90 || $bbox[1]>90 || $bbox[3]<-90 || $bbox[3]>90)
{
	$error = "Invalid latitude and/or longitude!";
}
else
{
	foreach($bbox as $i)
	{
		if(!wholly_numeric($i))	
			$error = "Invalid input. Goodbye!";
	}
}

if(!isset($error))
{
	$image = new Image($bbox[0], $bbox[1], $bbox[2], $bbox[3],
						$inp["WIDTH"],$inp["HEIGHT"], 
						$inp["landsat"],$inp["tp"],$inp["debug"]);
}

if (isset($error))
{
	echo "<html><body><strong>Error:</strong>$error</body></html>";
}
else
{
	
	if (!isset($_GET['debug']))
		header('Content-type: image/png'); 
		
	
	$contours = (isset($_GET['contours']));

	$image->draw($contours);
}


?>
