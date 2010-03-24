<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################
session_start();
session_destroy();
if(isset($_GET['referrer']))
	header("Location: ".$_GET["referrer"]);
?>
