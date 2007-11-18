<?php
header("Content-type: image/png");
$im = ImageCreateFromPNG("/var/www/images/flag.png");
ImageAlphaBlending($im,true);
ImageSaveAlpha($im,true);
$white=ImageColorAllocate($im,255,255,255);
ImageString($im,2,2,7,$_GET['n'],$white);
ImagePNG($im);
ImageDestroy($im);
?>
