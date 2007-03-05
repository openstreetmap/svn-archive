<html>
<head>
<title>OpenStreetMap tiles@home</title>
<link rel="stylesheet" href="../styles.css">
</head>
<body>
<div class="all">
<h1 class="title"><img src="../Gfx/tah.png" alt="tiles@home" width="600" height="109"></h1>
<p class="title">Logfiles</p>
<hr>

<?php
for($i = 1; $i <= 4; $i++){
  showLogfile("Priority $i", "messages_p$i.txt");
}

function showLogfile($Title, $Filename){
  print "<h2>$Title</h2>\n";
  
  print "<pre class=\"code\">";
  print system("tail -n 30 Data/".$Filename);
  print "</pre>\n";
}
?>

</div>
</body>
</html>
