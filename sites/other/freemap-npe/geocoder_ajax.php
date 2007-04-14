<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################
//header("Content-type: text/xml");
require_once('geocoder_funcs.php');
require_once('latlong.php');



$pos = geocoder($_REQUEST['place'],$_REQUEST['country']);
if($pos['lat']>=49 && $pos['lat']<=59 &&  $pos['long'] >= -7 && $pos['long']<=2)
{
	$gr = wgs84_ll_to_gr($pos);
	echo round($gr['e']).",".round($gr['n']);
}
else
{
	echo "0,0";
}

?>
