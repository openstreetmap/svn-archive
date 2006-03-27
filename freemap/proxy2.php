<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################
require_once('defines.php');

$bbox = $_REQUEST['bbox'];
echo grabosm($bbox);

function grabosm($bbox)
{
	$url = "http://www.openstreetmap.org/api/0.2/map?bbox=$bbox";
	$ch=curl_init ($url);
	curl_setopt($ch,CURLOPT_RETURNTRANSFER,true);
	curl_setopt($ch,CURLOPT_HEADER,false);
	curl_setopt($ch,CURLOPT_USERPWD,OSM_LOGIN);
	$resp=curl_exec($ch);
	curl_close($ch);
	return $resp;
}
?>
