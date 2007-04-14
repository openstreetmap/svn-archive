<h1>Test a version string</h1>

<form action="./" method="post">
<input name="version" type="text">
<input type="submit" value="Check">
</form>

<?php
include("../../lib/versions.inc");

    $ID = checkVersion($_POST["version"]);
    if($ID < 0){
      printf("<p>Unrecognised</p>\n"); 
    }
    else
    {
      printf("<p>Version <b>%d</b></p>\n", $ID); 
    }


?>
