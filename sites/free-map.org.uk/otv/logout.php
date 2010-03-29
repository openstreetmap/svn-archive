<?php
################################################################################
# This file forms part of the OpenTrailView source code.                       #
# (c) 2010 Nick Whitelegg                                                      #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################
session_start();
session_destroy();
header("Location: /otv/index.php");
?>
