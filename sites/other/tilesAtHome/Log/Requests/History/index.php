<html>
<head>
<title>OpenStreetMap tiles@home</title>
<link rel="stylesheet" href="../../../styles.css">
</head>
<body>
<div class="all">
<h1 class="title"><img src="../../../Gfx/tah.png" alt="tiles@home" width="600" height="109"></h1>
<p class="title">Rendering request history</p>
<hr>

<?php
  /* Displays logfiles of recent requests for the tiles@home website
  ** OJW 2007
  ** License: GNU GPL v2 or at your option any later version
  */
  include("../../../connect/connect.php");
  include("../../../lib/requests.inc");
    
  RenderList("Pending", REQUEST_PENDING);
  RenderList("New", REQUEST_NEW);
  RenderList("Active", REQUEST_ACTIVE);
  RenderList("Completed", REQUEST_DONE);
    
function RenderList($Title, $Status){

}
  
  ?>
</div>
</body>
</html>
