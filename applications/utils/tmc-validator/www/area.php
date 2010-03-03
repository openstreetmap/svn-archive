<?php

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


$query="SELECT name,pol_lcd,tnatdesc FROM $table a, names n, types t WHERE  n.nid=a.nid AND a.tcd=t.tcd AND t.class=\"A\" AND lcd=$lcd ";


$result=mysql_query($query);
$num=mysql_numrows($result);

$i=0;
if ($num==1) {
$name=mysql_result($result,$i,"name");
$pol_lcd=mysql_result($result,$i,"pol_lcd");
$tnatdesc=mysql_result($result,$i,"tnatdesc");

}

if ($name == "") {
	$name="UNBENNANT $num";
}

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<link rel="stylesheet" type="text/css" href="style.css" />
<title>OSM Validator Area <?php print $lcd;?> (<?=$name?>)</title>
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
print "<li><b>Art des Gebiets:</b> $tnatdesc</li>";
print "</ul>";


$query="SELECT DISTINCT osm_id,type FROM osm WHERE okey=\"LocationCode\" AND ovalue=$lcd";
$result=mysql_query($query);
$num=mysql_numrows($result);
if ($num==0) {

print "<h1 class='missing'>Zu Erledigen!</h1><p>Diese Area fehlt in OpenStreetMap. Bitte versuche, eine Gebietsrelation zu diesem TMC Gebiet (z.B. Kreis oder Gemeinderelation) zu finden.</p>
<ul>
<li>Suche nach einem Gebiet: [<a href=\"http://www.informationfreeway.org/api/0.6/relation[name|ref|name:de|short_name:de=$name]\">XAPI Suche nach Relation</a>][<a href=\"http://localhost:8111/import?url=http://www.informationfreeway.org/api/0.6/relation[name|ref|name:de|short_name:de=$name\">JOSM</a>]
<li>Wenn kein Gebiet mit dieser Info existiert, kann auch ein Punkt mit der Information getaggt werden. Suche nach einem Punkt: [<a href=\"http://www.informationfreeway.org/api/0.6/node[name|ref|name:de|short_name:de=$name]\">XAPI Suche nach Knoten</a>][<a href=\"http://localhost:8111/import?url=http://www.informationfreeway.org/api/0.6/node[name|ref|name:de|short_name:de=$name\">JOSM</a>]</li>
</ul>
<p>Diese dann mit folgenden Tags erg&auml;nzen: 
	<ul>
	<li><code>TMC:cid_58:tabcd_1:LocationCode = $lcd</code> <b>(ERFORDERLICH)</b></li>
	<li><code>TMC:cid_58:tabcd_1:Class = Area</code> (optional, wird autmatisch vom TMCbot erg&auml;nst)</li>
	<li><code>TMC:cid_58:tabcd_1:LCLversion = ".tmcversion()."</code> (optional, wird automatisch vom TMCbot erg&auml;nst)</li>
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





$query="SELECT t.lcd,name,found,fehler,warnung,tipp,(bis-von+1) as count FROM administrativearea a, names n, tmcorder t WHERE  n.nid=a.nid AND pol_lcd=$lcd AND a.lcd=t.lcd ORDER BY n.name";

$result=mysql_query($query);
$num=mysql_numrows($result);
$i=0;
if ($num>0) {
print "<h2>Unterebenen (Administrativ)</h2><ul>";

while ($i<$num) {
	$name=mysql_result($result,$i,"name");
	$nlcd=mysql_result($result,$i,"lcd");
	$found=mysql_result($result,$i,"found");
	$fehler=mysql_result($result,$i,"fehler");
	$warnung=mysql_result($result,$i,"warnung");
	$tipp=mysql_result($result,$i,"tipp");
	$count=mysql_result($result,$i,"count");
$i++;
print liStyle($found,$fehler,$warnung,$tipp,$count)."<a href=\"?lcd=$nlcd\">$name</a> (".zusammenfassung($found,$fehler,$warnung,$tipp,$count).")</li>";


}
print "</ul>";
}

$query="SELECT t.lcd,name,found,fehler,warnung,tipp,(bis-von+1) as count FROM otherareas o , names n, tmcorder t WHERE n.nid=o.nid AND pol_lcd=$lcd  AND o.lcd=t.lcd ORDER BY name";


$result=mysql_query($query);

$num=mysql_numrows($result);
if ($num>0) {
$i=0;
print "<h2>Andere Ebenen:</h2>";
print "<ul>";
while ($i<$num) {
	$olcd=mysql_result($result,$i,"lcd");	
	$name=mysql_result($result,$i,"name");	
	$found=mysql_result($result,$i,"found");
	$fehler=mysql_result($result,$i,"fehler");
	$warnung=mysql_result($result,$i,"warnung");
	$tipp=mysql_result($result,$i,"tipp");
	$count=mysql_result($result,$i,"count");
	$i++;
	print liStyle($found,$fehler,$warnung,$tipp,$count)."<a href=\"area.php?other=1&lcd=$olcd\">$name</a> (".zusammenfassung($found,$fehler,$warnung,$tipp,$count).")</li>";
}
print "</ul>";
}


$query="SELECT r.lcd,roadnumber,found,fehler,warnung,tipp,(bis-von+1) as count, ".
	"(SELECT name FROM names n WHERE n.nid=r.rnid) AS roadname, ".
	"(SELECT name FROM names n WHERE n.nid=r.n1id) AS n1id_name, ".
	"(SELECT name FROM names n WHERE n.nid=r.n2id) AS n2id_name ".
	"FROM roads r,  tmcorder t WHERE r.pol_lcd=$lcd and r.lcd=t.lcd";

$result=mysql_query($query);

$num=mysql_numrows($result);
if ($num>0) {
$i=0;
print "<h2>Roads</h2>";
print "<ul>";
while ($i<$num) {
	$rlcd=mysql_result($result,$i,"lcd");	
	$roadnum=mysql_result($result,$i,"roadnumber");	
	$roadname=mysql_result($result,$i,"roadname");	
	$name1=mysql_result($result,$i,"n1id_name");	
	$name2=mysql_result($result,$i,"n2id_name");
	$found=mysql_result($result,$i,"found");
	$fehler=mysql_result($result,$i,"fehler");
	$warnung=mysql_result($result,$i,"warnung");
	$tipp=mysql_result($result,$i,"tipp");
	$count=mysql_result($result,$i,"count");
	
	$i++;
	print liStyle($found,$fehler,$warnung,$tipp,$count)."<a href=\"road.php?lcd=$rlcd\">$roadnum, $roadname $name1 $name2</a> (".zusammenfassung($found,$fehler,$warnung,$tipp,$count).")</li>";
}
print "</ul>";
}



$query="SELECT s.lcd,found,fehler,warnung,tipp,(bis-von+1) as count,".
	"(SELECT name FROM names n WHERE n.nid=s.rnid) AS roadname, ".
	"(SELECT name FROM names n WHERE n.nid=s.n1id) AS n1id_name, ". 
	"(SELECT name FROM names n WHERE n.nid=s.n2id) AS n2id_name ".
	"FROM segments s, tmcorder t WHERE s.pol_lcd=$lcd AND s.lcd=t.lcd ORDER BY roadname,n1id_name,n2id_name";
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
	$found=mysql_result($result,$i,"found");
	$fehler=mysql_result($result,$i,"fehler");
	$warnung=mysql_result($result,$i,"warnung");
	$tipp=mysql_result($result,$i,"tipp");
	$count=mysql_result($result,$i,"count");

	$i++;
	print liStyle($found,$fehler,$warnung,$tipp,$count)."<a href=\"segment.php?lcd=$slcd\">$roadname $name1 $name2 $plcd</a> (".zusammenfassung($found,$fehler,$warnung,$tipp,$count).")</li>";
}
print "</ul>";
}

if ($other==1) {
$row="oth_lcd";
} else {
$row="pol_lcd";
}

$query="SELECT p.lcd,xcoord,ycoord, found,fehler,warnung,tipp, ".
	"(SELECT name FROM names n WHERE n.nid=p.rnid) AS roadname,  ".
	"(SELECT name FROM names n WHERE n.nid=p.n1id) AS n1id_name, ".
	"(SELECT name FROM names n WHERE n.nid=p.n2id) AS n2id_name ".
	"FROM points p, tmcorder t WHERE p.$row=$lcd AND p.lcd=t.lcd ORDER BY roadname,n1id_name,n2id_name";

#print "$query";
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
	$found=mysql_result($result,$i,"found");
	$fehler=mysql_result($result,$i,"fehler");
	$warnung=mysql_result($result,$i,"warnung");
	$tipp=mysql_result($result,$i,"tipp");
	$x=$x/100000;	
	$y=$y/100000;	
	$i++;
	print liStyle($found,$fehler,$warnung,$tipp,1)."<a href=\"point.php?lcd=$plcd\">$roadname $name1 $name2</a>, <a href=\"http://www.openstreetmap.org/?mlat=$y&mlon=$x&zoom=17\">OSM</a> (".zusammenfassung($found,$fehler,$warnung,$tipp,1).")</li>";
}
print "</ul>";
}
?> <hr/>
Stand OSM Daten:
<? print tmc_timestamp();
mysql_close();
?>

</body>
</html>