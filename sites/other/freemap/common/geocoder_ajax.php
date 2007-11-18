<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################
//header("Content-type: text/xml");
require_once('geocoder_funcs.php');



$pos = geocoder($_REQUEST['place'],$_REQUEST['country']);
echo "$pos[lat],$pos[long]";

?>
