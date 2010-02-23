<?php
header('Content-Type: text/plain'); 


$left = $_GET["l"];
$top = $_GET["t"];
$right = $_GET["r"];
$bottom = $_GET["b"];
$zoom = $_GET["z"];

$LCDname["x"]=1;
function farbLCD($lcd,$was) {
             global $LCDname;
             global $LCDtipp;
             global $LCDwarnung;
             global $LCDfehler;
	     global $LCDFound;
	if ($FarbeHash[$lcd] =="") {
	   $squery="select DISTINCT osm_id from osm where okey='LocationCode' and ovalue='$lcd'";
           $sresult=mysql_query($squery);
           $nnum=mysql_numrows($sresult);
	   $LCDFound[$lcd]=$nnum;
	   $LCDfehler[$lcd]=0;
	   $LCDwarnung[$lcd]=0;
	   $LCDtipp[$lcd]=0;
           if ($nnum==0) {
             $FarbeHash[$lcd]="rot";

           } else if ($nnum>1) {
             $FarbeHash[$lcd]="rot";

           } else {

	     $squery="select distinct validtype from osmvalidator where lcd=$lcd ";
             $sresult=mysql_query($squery);
	     $nnum=mysql_numrows($sresult);
	     if ($nnum==0) {
               $FarbeHash[$lcd]="gruen";
             } else {
	       $k=0;
               $FarbeHash[$lcd]="gruen";
               while ($k<$nnum) {
                 $vt=mysql_result($sresult,$k,"validtype");
		 if ($vt == 'fehler' ) {
                    $FarbeHash[$lcd]="rot";
 	            $LCDfehler[$lcd]++;

                 } else if ($vt == 'warnung') {
	            if ($FarbeHash[$lcd]="") {
                     $FarbeHash[$lcd]="gelb";
                    }
	            $LCDwarnung[$lcd]++;
                 } else {
 	            $LCDtipp[$lcd]++;            
                 }
	         $k++;
               }
#FIXME
             }
           
           }
	   if (($was== "administrativearea") or ($was =="otherareas")) {
	     $squery="select name from $was a, names n where a.nid=n.nid and a.lcd=$lcd";	  
             $sresult=mysql_query($squery);


             $LCDname[$lcd]=mysql_result($sresult,0,"name");
#	     print "\n\nx=".$LCDname[$lcd]."\n";
           } else {
	     $squery="select roadnumber from $was where lcd=$lcd";	  

             $sresult=mysql_query($squery);
             $LCDname[$lcd]=mysql_result($sresult,0,"roadnumber");
           }
        }
	return $FarbeHash[$lcd];
}
function updateFarbe($oldFarbe,$newFarbe) {
 if (($oldFarbe=="rot") or ($newFarbe=="rot")) {
   return "rot";
 } else if (($oldFarbe=="gruen") and ($newFarbe=="gruen")) {
   return "gruen";
  } else
  return "gelb";
}

print "lat	lon	icon	iconSize	iconOffset	title	description	popupSize\n";
if ($zoom>10) {
# lon
$fl=floor($left*100000);
$fr=floor($right*100000);
# lat
$ft=floor($top*100000);
$fb=floor($bottom*100000);

include "function.php";

$farbe="";
$zweitFarbe="gruen";
$query="SELECT p.lcd,xcoord,ycoord,pol_lcd,roa_lcd,seg_lcd,oth_lcd,found,fehler,warnung,tipp FROM points p,tmcorder o WHERE xcoord>=$fl and xcoord<=$fr and ycoord>=$fb and ycoord>=$ft and p.lcd=o.lcd order by xcoord,ycoord";

#print $query;
$result=mysql_query($query);
$num=mysql_numrows($result);
#print "$num";
$i=0;
$oldpos="";
$line="";
while ($i<$num) {
   $lcd=mysql_result($result,$i,"lcd");
  $xcoord=mysql_result($result,$i,"xcoord");
  $ycoord=mysql_result($result,$i,"ycoord");
  $pol_lcd=mysql_result($result,$i,"pol_lcd");
  $roa_lcd=mysql_result($result,$i,"roa_lcd");
  $seg_lcd=mysql_result($result,$i,"seg_lcd");
  $oth_lcd=mysql_result($result,$i,"oth_lcd");
  $found=mysql_result($result,$i,"found");
  $fehler=mysql_result($result,$i,"fehler");
  $warnung=mysql_result($result,$i,"warnung");
  $tipp=mysql_result($result,$i,"tipp");
#$tnatdesc=mysql_result($result,$i,"tnatdesc");



if ($found==0) {
$farbe="rot";
} else if ($fehler>0) {
$farbe="rot";
} else if (($warnung>0) or ($farbe=="gelb")) {
 $farbe="gelb";
} else {
 $farbe="gruen";
}
$zweitFarbe="gruen";
#$text="Zweitfarbe: $zweitFarbe";
$text="<ul><li>Punkt: <a href='point.php?lcd=$lcd'>$lcd</a>".zusammenfassung($found,$fehler,$warnung,$tipp,1)."</li>";
if ($pol_lcd>0) {
        $thisLCD=$pol_lcd;
	$zweitFarbe=updateFarbe($zweitFarbe,farbLCD($pol_lcd,"administrativearea"));
	$text.="<li>Gebiet: <a href='area.php?lcd=$pol_lcd'>".$LCDname[$pol_lcd]." ($pol_lcd)</a> ".zusammenfassung($LCDFound[$thisLCD],$LCDfehler[$thisLCD],$LCDwarnung[$thisLCD],$LCDtipp[$thisLCD],1)."</li>";

}

if ($oth_lcd>0) {
        $thisLCD=$oth_lcd;
	$zweitFarbe=updateFarbe($zweitFarbe,farbLCD($oth_lcd,"otherareas"));
	$text.="<li>Gebiet: <a href='area.php?lcd=$oth_lcd&other=1'>".$LCDname[$pol_lcd]." ($pol_lcd)</a> ".zusammenfassung($LCDFound[$thisLCD],$LCDfehler[$thisLCD],$LCDwarnung[$thisLCD],$LCDtipp[$thisLCD],1)."</li>";

}

if ($roa_lcd>0) {
        $thisLCD=$roa_lcd;
	$zweitFarbe=updateFarbe($zweitFarbe,farbLCD($roa_lcd,"roads"));
	$text.="<li>Road: <a href='road.php?lcd=$roa_lcd'>".$LCDname[$roa_lcd]." ($roa_lcd)</a> ".zusammenfassung($LCDFound[$thisLCD],$LCDfehler[$thisLCD],$LCDwarnung[$thisLCD],$LCDtipp[$thisLCD],1)."</li>";

}

if ($seg_lcd>0) {
        $thisLCD=$seg_lcd;
	$zweitFarbe=updateFarbe($zweitFarbe,farbLCD($seg_lcd,"segments"));
	$text.="<li>Segment: <a href='segment.php?lcd=$seg_lcd'>".$LCDname[$seg_lcd]." ($seg_lcd)</a> ".zusammenfassung($LCDFound[$thisLCD],$LCDfehler[$thisLCD],$LCDwarnung[$thisLCD],$LCDtipp[$thisLCD],1)."</li>";

}
$text.="</ul>"; 
#$text=$lcd;
#$text.="Zweitfarbe: $zweitFarbe";
$img=$farbe."-".$zweitFarbe.".png";
$pos=sprintf("%d\t%d",$xcoord,$ycoord);
if ($pos == $oldpos) {
	$text.="<hr/>Weiterer Punkt: ".$oldtext;
	$zweitFarbe=updateFarbe($zweitFarbe,$oldZweitFarbe);
	$farbe=updateFarbe($farbe,$oldFarbe);
        $img=$farbe."-".$zweitFarbe.".png";
#	$text.="<hr/>Weiterer Punkt: ".$oldtext."<hr/>".$oldZweitFarbe.$zweitFarbe;
} else {
print $line;
$line="";
}
$oldpos=$pos;
$oldtext=$text;
$oldZweitFarbe=$zweitFarbe;
$oldFarbe=$farbe;
$line.= sprintf("%f\t%f\t%s\t25,25\t-12,-12\t%s\t%s\t300,80\n", $ycoord/100000,$xcoord/100000,$img,"Punkt $lcd",$text);


# print "$line";
#\tlibrary-ohne-isil.png\t25,25\t-12,-12\tPunkt: $lcd\t$text\t200,80\n";
$i++;
}
print $line;
}
