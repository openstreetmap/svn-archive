<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################

header("Content-type: text/xml");
$bbox = $_REQUEST['bbox'];
echo grabosm($bbox);

function grabosm($bbox)
{
	$url = "http://www.geograph.org.uk/earth.php?BBOX=$bbox";
	$ch=curl_init ($url);
	curl_setopt($ch,CURLOPT_RETURNTRANSFER,true);
	curl_setopt($ch,CURLOPT_HEADER,false);
	$resp=curl_exec($ch);
	curl_close($ch);
	return $resp;
}
?>
