<?php

# Das Passwort und den Usernamen bitte in der Password PHP festlegen
$username="sva00_tmc_ro";
$password="geheim";

$page="password.php";
if (file_exists($page)) { // Page exists
 
 // Show page
 include("./$page");

 } 

mysql_connect("localhost",$username,$password);
@mysql_select_db($database) or die( "Unable to select database");

$tmcversion="";
function tmcversion() 
{

if ($tmcversion!="") {
	return $tcmversion;
}
$query="SELECT version FROM locationdatasets";

$result=mysql_query($query);
$num=mysql_numrows($result);

if ($num==1) {
        $version=sprintf("%.2f",mysql_result($result,$i,"version"));
	$tmcversion=$version;
        return $version;
	}
}

function node_name($lcd) {
$query="SELECT snatdesc, ".
	"(SELECT name FROM names n WHERE n.nid=p.rnid) AS roadname,  ".
	"(SELECT name FROM names n WHERE n.nid=p.n1id) AS n1id_name, ".
	"(SELECT name FROM names n WHERE n.nid=p.n2id) AS n2id_name ".
	"FROM points p, subtypes s  WHERE p.lcd=$lcd and s.tcd=p.tcd and s.stcd=p.stcd  and s.class='P'";

#print "$query";
$result=mysql_query($query);

$num=mysql_numrows($result);
if ($num!=1) {
	print "<h2>Fehler ist kein Punkt</h2>";
}
$snatdesc=mysql_result($result,$i,"snatdesc");	
$roadname=mysql_result($result,$i,"roadname");	
$name1=mysql_result($result,$i,"n1id_name");		
$name2=mysql_result($result,$i,"n2id_name");	

return "$roadname $name1 $name2 ($snatdesc)";

}
function osm_link($type,$id)
{

$str.="[<a href=\"http://www.openstreetmap.org/browse/$type/$id\">Browse $type $id</a>]";

if ($type=="relation") {
	$str.="[<a href=\"http://localhost:8111/import?url=http://www.openstreetmap.org/api/0.6/relation/$id/full\">JOSM</a>][<a href=\"http://betaplace.emaitie.de/webapps.relation-analyzer/analyze.jsp?relationId=$id\">Relation-Analyser</a>]";

} else if ($type=="way") {
	$str.="[<a href=\"http://localhost:8111/import?url=http://www.openstreetmap.org/api/0.6/way/$id/full\">JOSM</a>]";
} else  {
	$str.="[<a href=\"http://localhost:8111/import?url=http://www.openstreetmap.org/api/0.6/node/$id\">JOSM</a>]";
}
return $str;
}


function validatorOutput($vtyp,$osmtyp,$id) {
	$vquery="select validstr from osmvalidator  where type='$osmtyp' and osm_id='$id' and validtype='$vtyp'";
	$vresult=mysql_query($vquery);
	$vnum=mysql_numrows($vresult);
	$k=0;
	$str="";
	if ($vnum>0) {
        $str="<br/><b>".ucfirst($vtyp)."</b><ul>\n";

	while ($k<$vnum) {
	  $v=mysql_result($vresult,$k,"validstr");
	 $v=htmlentities($v,ENT_QUOTES,"UTF-8");
	  $v=preg_replace("/Knotenpunkt: (\d*) ist nicht verbunden/","Knotenpunkt $1 <a href='http://www.openstreetmap.org/browse/node/$1'>[Browse OSM]</a> ist nicht verbunden.",$v);# FIXME Josm Link

  	  $k++;
	  $str.="<li>$v</li>\n";


	}
        $str.="</ul>";
        }

return $str;
}

function liStyle($found,$fehler,$warnung,$tipp,$count) {
	if (($found==$count) and ($fehler==0) and ($warung==0) and ($tipp==0)) {
	   return "<li class=\"gruen\">";
        }	
	return "<li>";
}
function zusammenfassung($found,$fehler,$warnung,$tipp,$count) {

if ($count==1) {
	if ($found) {
	  $str="<span class='found'>in OSM vorhanden</span>";
          if ($fehler>0) {
	    $str.=" <b class='error'>fehlerhaft!</b>";
          } else if ($warung>0) {
             $str.=" <b class='warning'>mit Warnung!</b>";
          } else if ($tipp>0) {
             $str.=" mit Tipp(s) zur Verbesserung!";
          }
        } else {
	  $str="<b class='error'>TMC Info fehlt in OSM</b>";
        }

} else {
	
	$str="in OSM <b>$found</b> von <b>$count</b> TMC Objekten";
	if (($warnung>0) or ($tipp>0) or ($fehler>0)) {
	  $str.=" davon";
	  if ($fehler>0) {
	    $str.=" <b class='error'>$fehler Fehlerhaft</b>";
          }
	  if ($warnung==1) {
	    $str.=" <b class='warning'>Eine Warnung</b>";
	  } else if ($warnung>1) {
	    $str.=" <b class='warning'>$fehler Warnungen</b>";
          }
	  if ($tipp>1) {
	    $str.=" $tipp Tipps";
          } else if ($tipp==1) {
	    $str.=" ein Tipp";
          }
        } else if ($found==$count) {
	  $str="<span class='found'>".$str."</span>";
        }
}

	return $str;


}
function zusammenfassungPunkt($lcd) {

	$query="select distinct o.lcd  from tmcorder t, tmcorder o, osm where t.lcd=$lcd and o.von>=t.von and  o.bis<=t.bis and osm.okey='LocationCode' and osm.ovalue=o.lcd";  

	$result=mysql_query($query);
	$inOSM=mysql_numrows($result);

	$query="select distinct o.lcd, v.validtype  from tmcorder t, tmcorder o, osmvalidator v where t.lcd=$lcd and o.von>=t.von and  o.bis<=t.bis and v.lcd=o.lcd";

	$result=mysql_query($query);
	$num=mysql_numrows($result);
	$i=0;
	$tipp=0;
	$fehler=0;
	$warnung=0;
	while ($i<$num) {

            $x=mysql_result($result,$i,"validtype");
  	    $i++;
	    if ($x=="tipp") {
	      $tipp++;
            } else if ($x=="warnung") {
	      $warnung++;
            } else if ($x=="fehler") {
	      $fehler++;
            } else {
	       print "<b>--------------- Unklarer Typ: $x</b>";
            }
        }
	if ($inOSM==0) {
	  $str="<b class='missing'>Fehlt in OSM</b>";
        } else {
	  $str="<span class='found'>In OSM vorhanden</span>";
        }
	if ($num>0) {

	  if ($fehler>0) {
	    $str.=" <b class='error'>fehlerhaft</b>";
          } else if ($warnung>0) {
	    $str.=" <b class='warning'>mit Warnung</b>";
          } else if ($tipp>0) {
	    $str.=" mit Tipps zur Verbesserung";
          }
        }
	return $str;


}

function tmc_timestamp() {
$query="select MAX(ts) as t from osm";
$result=mysql_query($query);
 $ts=mysql_result($result,$i,"t");
return $ts;
}

?>