<?php
$lcd=$_GET["lcd"];

include("function.php");


$query="SELECT snatdesc,roa_lcd,urban,junctionnumber,pol_lcd,oth_lcd,seg_lcd,xcoord,ycoord, ".
	"(SELECT name FROM names n WHERE n.nid=p.rnid) AS roadname,  ".
	"(SELECT name FROM names n WHERE n.nid=p.n1id) AS n1id_name, ".
	"(SELECT name FROM names n WHERE n.nid=p.n2id) AS n2id_name ".
	"FROM points p, subtypes s WHERE p.lcd=$lcd AND s.tcd=p.tcd AND s.stcd=p.stcd AND s.class='P'";

#print "$query";
$result=mysql_query($query);

$num=mysql_numrows($result);
if ($num!=1) {
	print "<h2 class='error'>Fehler ist kein Punkt</h2>";
}
$snatdesc=mysql_result($result,$i,"snatdesc");	
$junctionnumber=mysql_result($result,$i,"junctionnumber");	
$pol_lcd=mysql_result($result,$i,"pol_lcd");	
$oth_lcd=mysql_result($result,$i,"oth_lcd");	
$seg_lcd=mysql_result($result,$i,"seg_lcd");	
$roa_lcd=mysql_result($result,$i,"roa_lcd");	
$urban=mysql_result($result,$i,"urban");	
$x=mysql_result($result,$i,"xcoord");	
$y=mysql_result($result,$i,"ycoord");
$roadname=mysql_result($result,$i,"roadname");	
$name1=mysql_result($result,$i,"n1id_name");		
$name2=mysql_result($result,$i,"n2id_name");	
$query="select neg_off_lcd,pos_off_lcd from poffsets where lcd=$lcd";

$result=mysql_query($query);

$num=mysql_numrows($result);
$prev_lcd=0;
$next_lcd=0;
if ($num==1) {
	$prev_lcd=mysql_result($result,$i,"neg_off_lcd");	
	$next_lcd=mysql_result($result,$i,"pos_off_lcd");	
} 

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>OSM Validator Point <?php print $lcd;?> (<?=$roadname?> <?=$name1?> <?=$name2?>)</title>
<link rel="stylesheet" type="text/css" href="style.css" />
</head>
<body>

<?php


$x=$x/100000;	
$y=$y/100000;	
print "<h1>TMC Punkt $lcd</h1>\n";
print "<ul>";
if ($pol_lcd!=0) {
 print "<li>Administrative Ebene: <b><a href='area.php?lcd=$pol_lcd'>$pol_lcd</a></b></li>\n";
}
if ($oth_lcd!=0) {
 print "<li>Andere Ebene: <b><a href='area.php?lcd=$oth_lcd&other=1'>$oth_lcd</a></b></li>\n";
}
if ($seg_lcd!=0) {
 print "<li>Segment: <b><a href='segment.php?lcd=$seg_lcd'>$seg_lcd</a></b></li>\n";
}

if ($roa_lcd!=0) {
 print "<li>Road: <b><a href='road.php?lcd=$roa_lcd'>$roa_lcd</a></b></li>\n";
}

$posstr="";
$negstr="";
if ($prev_lcd!=0) {
 print "<li>Vorheriger Knotenpunkt: <b><a href='point.php?lcd=$prev_lcd'>$prev_lcd ".node_name($prev_lcd)."</a></b></li>\n";
 $posstr="Aus Richtung  ".node_name($prev_lcd). " kommend";
 $negstr="In Richtung ".node_name($prev_lcd)." fahrend";
}
if ($next_lcd!=0) {
 print "<li>N&auml;chster Knotenpunkt: <b><a href='point.php?lcd=$next_lcd'>$next_lcd ".node_name($next_lcd)."</a></b></li>\n";
 $negstr="Aus Richtung  ".node_name($next_lcd). " kommend";
 $posstr="In Richtung ".node_name($next_lcd)." fahrend";

}
if (($prev_lcd!=0) and ($next_lcd!=0)) {
 $posstr="Von ".node_name($prev_lcd)." nach ".node_name($next_lcd);
 $negstr="Von ".node_name($next_lcd)." nach ".node_name($prev_lcd);
}
print "<li>Beschreibung: <b>$snatdesc</b></li>\n";
if ($junctionnumber != "") {
print "<li>Kreuzungsnummer: <b>$junctionnumber</b></li>\n";
}
if ($roadname != "") {
print "<li>Stra&szlig;enname: <b>$roadname</b></li>\n";
}
if ($name1 != "") {
print "<li>Name1: <b>$name1</b></li>\n";
}
if ($name2!= "") {
print "<li>Name2: <b>$name2</b></li>\n";
}
if ($urban==1) {
print "<li>Gegend: <b>Inner</b>orts</li>\n";
} else {
print "<li>Gegend: <b>Au&szlig;er</b>orts</li>\n";
}

$delta="0.0005";
$lat1=$y-$delta;
$lat2=$y+$delta;
$lon1=$x-$delta;
$lon2=$x+$delta;

print "<li>Position: <b> $y,$x</b> [<a href=\"http://www.openstreetmap.org/?mlat=$y&mlon=$x&zoom=17\">OSM</a>][<a href=\"index.php?lat=$y&lon=$x&zoom=17\">TMC-Karte</a>][<a href=\"http://localhost:8111/load_and_zoom?left=$lon1&right=$lon2&bottom=$lat1&top=$lat2\">Josm</a>]</li>";


print "</ul>";

print '<img src="http://dev.openstreetmap.de/staticmap/staticmap.php?center='.$y.','.$x.'&zoom=17&size=500x400&markers='.$y.','.$x.',ol-marker" /><br/>';
#print '<iframe width="500" height="400" frameborder="0" scrolling="no" marginheight="0" marginwidth="0" src="http://www.openstreetmap.org/export/embed.html?bbox='.$lon1.",".$lat1.",".$lon2.",".$lat2."&mlat=$y&mlon=$x".'&layer=osmarender" style="border: 1px solid black"></iframe><br />';


$query="select  p2.lcd from points p1, points p2 where p1.xcoord=p2.xcoord and p1.ycoord=p2.ycoord and p1.lcd=$lcd and p1.lcd!=p2.lcd";

$result=mysql_query($query);
$num=mysql_numrows($result);
$i=0;
if ($num>0) {
print "<h2 class='warning'>Spezialfall doppelte TMC Punkte.</h2>
<p>Dieser TMC Punkt ist ein Spezialfall, da sich an diesem Punkt mehrere
TMC Wege kreuzen. Es exisierten f&uuml;r die gleiche Koordinate mehrere TMC Punkte.
Weitere Punkte auf der selben Koordinate sind:<ul>";
}
while ($i<$num) {
        $olcd=mysql_result($result,$i,"lcd");
        $i++;
	print "<li><a href='point.php?lcd=$olcd'>$olcd - ".node_name($olcd)."</a></li>";
}
if ($num>0) {
print "</ul>Sollte es auch in OSM zu mehreren TMC Punkten auf einem Node kommen, m&uuml;ssen stattdessen Relationen (type=TMC) eingesetzt werden. Einziger Member ist der gemeinsame Kontenpunkt.</p>";
};

$query="SELECT DISTINCT osm_id,type FROM osm WHERE okey=\"LocationCode\" AND ovalue=$lcd";
$result=mysql_query($query);
$num=mysql_numrows($result);
if ($num==0) {


print "<h1 class='missing'>Zu Erledigen!</h1><p>Dieser Punkt bzw. die TMC Information zu diesen Punkt fehlen in OpenStreetMap.</p>
<p> Es gibt nun zwei M&ouml;glichkeiten:
<ol>
<li>Das Segment oder die Road ist einspurig und es gibt genau einen Punkt, oder</li>
<li>Das Segment oder die Road ist mit mehreren Wegen in OSM vorhanden und es gibt diesen Punkt f&uuml;r jede Richtung einmal.</li>
</ol>

<p>Diese(n) Punkt(e) dann mit folgenden Tags erg&auml;nzen:
        <ul>
        <li><code>TMC:cid_58:tabcd_1:LocationCode = $lcd</code> <b>(ERFORDERLICH)</b></li>";
if ($next_lcd!=0) {
print "	<li><code>TMC:cid_58:tabcd_1:NextLocationCode = $next_lcd</code> (ERFORDERLICH, wird aber vom <a href=\"http://www.openstreetmap.org/user/TMCbot/edits\">TMCbot</a> automatisch eingetragen)\n";
} 
if ($prev_lcd!=0) {
 print " <li><code>TMC:cid_58:tabcd_1:PrevLocationCode = $prev_lcd</code> (ERFORDERLICH, wird aber vom TMCbot automatisch eingetragen)</li>\n";
}
print"        <li><code>TMC:cid_58:tabcd_1:Class = Point</code> (optional, automatisch vom TMCbot eingetragen)</li>
        <li><code>TMC:cid_58:tabcd_1:LCLversion = ".tmcversion()."</code> (optional, automatisch vom TMCbot eingetragen)</li>
       <li><code>TMC:cid_58:tabcd_1:Direction = richtung</code> (<b>evtl. ERFORDERLICH</b>)</br>Bei mehreren OSM Punkten f&uuml;r einen TCM Punkt, bitte als Richtung: <ul><li><code>positive</code> ($posstr) oder</li><li> <code>negative</code> ($negstr)</li><li> Sonst <code>both</code> (optional) angeben.</li></ul></li>
        </ul>
</p>";
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

?> 
<hr/>
Stand OSM Daten:
<? 
print tmc_timestamp();
mysql_close();
?>

</body>
</html>