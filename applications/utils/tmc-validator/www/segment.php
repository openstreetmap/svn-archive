<?php
$lcd=$_GET["lcd"];

include("function.php");

if ($lcd=="") {
$lcd="34196";
}

$query="SELECT DISTINCT roadnumber,(select name from names n where nid=rnid) as name,
(select name from names n where nid=s.n1id) as name1,
(select name from names n where nid=s.n2id) as name2,
roa_lcd,
pol_lcd,tnatdesc FROM segments s, names n, types t WHERE  s.tcd=t.tcd AND t.class=\"L\" AND lcd=$lcd ";

#print "$query";

$result=mysql_query($query);
$num=mysql_numrows($result);

$i=0;
if ($num==1) {
$name=mysql_result($result,$i,"name");
$pol_lcd=mysql_result($result,$i,"pol_lcd");
$roa_lcd=mysql_result($result,$i,"roa_lcd");
$name1=mysql_result($result,$i,"name1");
$name2=mysql_result($result,$i,"name2");
$tnatdesc=mysql_result($result,$i,"tnatdesc");
$roadnumber=mysql_result($result,$i,"roadnumber");

}

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>OSM Validator Segment <?php print $lcd;?> (<?= $name ?>)</title>
<link rel="stylesheet" type="text/css" href="style.css" />
</head>
<body>

<?php

if ($name == "") {
	$name="UNBENANNTes Segment";
	print "<h1>$roadname</h1>";
} else {
	print "<h1>$name</h1>";
}
if ($pol_lcd!="") {

}
print "<ul>";
print "<li><b>LCD:</b> $lcd</li>";
print "<li><b>Gebiet:</b> <a href=\"area.php?lcd=$pol_lcd\">$pol_lcd</a></li>";
print "<li><b>Road:</b> <a href=\"road.php?lcd=$roa_lcd\">$roa_lcd</a></li>";
print "<li><b>Stra&szlig;ennummer:</b> $roadnumber</li>";
if ($name1!="") {
print "<li><b>Name 1:</b> $name1</li>";
}
if ($name2!="") {
print "<li><b>Name 2:</b> $name2</li>";
}
print "<li><b>Art des Segments:</b> $tnatdesc</li>";
print "</ul>";


$query="SELECT DISTINCT osm_id,type FROM osm WHERE okey=\"LocationCode\" AND ovalue=$lcd";
$result=mysql_query($query);
$num=mysql_numrows($result);
if ($num==0) {

print "<h1 class='missing'>Zu Erledigen!</h1><p>Dieses Segment fehlt in OpenStreetMap.</p>
<p>Bitte eine Relation mit folgenden Tags erstellen: 
	<ul>
	<li><code>type = TMC</code> <b>(ERFORDERLICH)</b></li>
	<li><code>TMC:cid_58:tabcd_1:LocationCode = $lcd</code> <b>(ERFORDERLICH)</b></li>
	<li><code>TMC:cid_58:tabcd_1:Class = Segment</code> (optional, tr&auml;gt der TMCbot ein)</li>
	<li><code>TMC:cid_58:tabcd_1:LCLversion = ".tmcversion()."</code> (optional, tr&auml;gt der TMCbot ein)</li>
	</ul>
<p>Als Member alle Stra&szlig;en hinzuf&uuml;gen, die in diesem Segment liegen.</p>";

} else {
   $i=0;
   print "<h1 class='found'>In OSM gefunden:</h1><ul>\n";
   while ($i<$num) {
	$typ=mysql_result($result,$i,"type");	
	$id=mysql_result($result,$i,"osm_id");	
	$i++;
	print "<li>".osm_link($typ,$id);
	print validatorOutput("fehler",$typ,$id);
	print validatorOutput("warnung",$typ,$id);
	print validatorOutput("tipp",$typ,$id);

	print "</li>\n";

  }
 print "</ul>";

        
}










$query="SELECT lcd,xcoord,ycoord, ".
	"(SELECT name FROM names n WHERE n.nid=p.rnid) AS roadname,  ".
	"(SELECT name FROM names n WHERE n.nid=p.n1id) AS n1id_name, ".
	"(SELECT name FROM names n WHERE n.nid=p.n2id) AS n2id_name ".
	"FROM points p WHERE p.seg_lcd=$lcd ORDER BY ordernum,roadname,n1id_name,n2id_name";

$result=mysql_query($query);

$num=mysql_numrows($result);
if ($num>0) {
$i=0;
print "<h2>Punkte</h2>";
print "<ul>";
while ($i<$num) {
	$plcd=mysql_result($result,$i,"lcd");	
	$x=mysql_result($result,$i,"xcoord");	
	$y=mysql_result($result,$i,"ycoord");
	$roadname=mysql_result($result,$i,"roadname");	
	$name1=mysql_result($result,$i,"n1id_name");		
	$name2=mysql_result($result,$i,"n2id_name");	
	$x=$x/100000;	
	$y=$y/100000;	
	$i++;
	print "<li><a href=\"point.php?lcd=$plcd\">$roadname $name1 $name2</a>, <a href=\"http://www.openstreetmap.org/?mlat=$y&mlon=$x&zoom=17\">OSM</a> ".zusammenfassungPunkt($plcd)."</li>";
}
print "</ul>";
}


?> 
<hr/>
Stand OSM Daten:
<? 
print tmc_timestamp();
mysql_close();
?>
</body>
</html>