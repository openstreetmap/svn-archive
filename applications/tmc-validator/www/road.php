<?php
$lcd=$_GET["lcd"];

include("function.php");

$lcd=$_GET["lcd"];
$other=$_GET["other"];
if ($lcd=="") {
$lcd="34196";
}

if ($other=="") {
 $other=0;
 $table="administrativearea";
} else {
 $other=1;
 $table="otherareas";
}


$query="SELECT roadnumber,pol_lcd,tnatdesc,
(select name from names n where n.nid=r.rnid) as name,
(select name from names n where n.nid=r.n1id) as name1,
(select name from names n where n.nid=r.n2id) as name2
FROM roads r, types t WHERE  r.tcd=t.tcd AND t.class=\"L\" AND lcd=$lcd ";


$result=mysql_query($query);
$num=mysql_numrows($result);

$i=0;
if ($num==1) {
$name=mysql_result($result,$i,"name");
$name1=mysql_result($result,$i,"name1");
$name2=mysql_result($result,$i,"name2");
$pol_lcd=mysql_result($result,$i,"pol_lcd");
$tnatdesc=mysql_result($result,$i,"tnatdesc");
$roadnumber=mysql_result($result,$i,"roadnumber");

}

if ($name == "") {
	$name="UNBENNANT $lcd";
}

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>OSM Validator Road <?php print $lcd;?> (<?=$name?>)</title>
<link rel="stylesheet" type="text/css" href="style.css" />
</head>
<body>
<?php

print "<h1>$name</h1>";
if ($pol_lcd!="") {
print "<a href=\"area.php?lcd=$pol_lcd\">eine Ebene hoch</a>";
}
print "<ul>";
print "<li><b>LCD:</b> $lcd</li>";
print "<li><b>POL_LCD:</b> $pol_lcd</li>";
print "<li><b>Stra&szlig;ennummer:</b> $roadnumber</li>";
print "<li><b>Art der Road:</b> $tnatdesc</li>";
if ($name1!="") {
print "<li><b>Name 1:</b> $name1</li>";
}
if ($name2!="") {
print "<li><b>Name 2:</b> $name2</li>";
}
print "</ul>";


$query="SELECT DISTINCT osm_id,type FROM osm WHERE okey=\"LocationCode\" AND ovalue=$lcd";
$result=mysql_query($query);
$num=mysql_numrows($result);
if ($num==0) {

print "<h1 class='missing'>Zu Erledigen!</h1><p>Diese Road fehlt in OpenStreetMap.</p>
<ul>
<li>Suche nach einer Straße als Relation: [<a href=\"http://www.informationfreeway.org/api/0.6/relation[name|ref|name:de|short_name:de=$name]\">XAPI Suche nach Relation</a>][<a href=\"http://localhost:8111/import?url=http://www.informationfreeway.org/api/0.6/relation[name|ref|name:de|short_name:de=$name\">JOSM</a>]
</ul>
<p>Diese dann mit folgenden Tags erg&auml;nzen: 
	<ul>
	<li><code>TMC:cid_58:tabcd_1:LocationCode = $lcd</code> <b>(ERFORDERLICH)</b></li>
	<li><code>TMC:cid_58:tabcd_1:Class = Road</code> (optional, tr&auml;gt der TMCbot ein)</li>
	<li><code>TMC:cid_58:tabcd_1:LCLversion = ".tmcversion()."</code> (optional, tr&auml;gt der TMCbot ein)</li>
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









$query="SELECT *,".
	"(SELECT name FROM names n WHERE n.nid=s.rnid) AS roadname, ".
	"(SELECT name FROM names n WHERE n.nid=s.n1id) AS n1id_name, ". 
	"(SELECT name FROM names n WHERE n.nid=s.n2id) AS n2id_name ".
	"FROM segments s WHERE s.roa_lcd=$lcd ORDER BY roadname,n1id_name,n2id_name";
$result=mysql_query($query);	

$num=mysql_numrows($result);
if ($num>0) {
$i=0;
print "<h2>Segmente</h2>";
print "<ul>";
while ($i<$num) {
	$slcd=mysql_result($result,$i,"lcd");	
	$roadname=mysql_result($result,$i,"roadname");	
	$name1=mysql_result($result,$i,"n1id_name");		
	$name2=mysql_result($result,$i,"n2id_name");	
	$i++;
	print "<li><a href=\"segment.php?lcd=$slcd\">$roadname $name1 $name2 $plcd</a> ".zusammenfassungPunkt($slcd)."</li>";
}
print "</ul>";
}

$query="SELECT lcd,xcoord,ycoord, ".
	"(SELECT name FROM names n WHERE n.nid=p.rnid) AS roadname,  ".
	"(SELECT name FROM names n WHERE n.nid=p.n1id) AS n1id_name, ".
	"(SELECT name FROM names n WHERE n.nid=p.n2id) AS n2id_name ".
	"FROM points p WHERE p.roa_lcd=$lcd ORDER BY ordernum, roadname,n1id_name,n2id_name";

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