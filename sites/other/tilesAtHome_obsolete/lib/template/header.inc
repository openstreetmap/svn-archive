<html>
<head>
<title><?php if ($title) {print $title." - ";} ?>tiles@home - OpenStreetMap</title>
<link rel="stylesheet" href="/styles.css">
</head>
<body>
<div style="position: absolute;right: 20px; top: 10px;">Server Time: <?=date("Y-m-d H:i:s")?></div> 
<div class="all">
  <h1 class="title"><a href="/"><img src="/Gfx/tah.png" alt="tiles@home" width="600" height="109"></a></h1>
  <p class="title"><?php if ($title) { print $title; } else { print " A project to render free maps of the whole world. "; } ?> </p>
  <hr>

