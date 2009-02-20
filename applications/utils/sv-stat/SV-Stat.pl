#!/usr/bin/perl -w
use utf8;
use Time::Local;
use Text::LevenshteinXS qw(distance);
use XML::Parser;
use strict; # geht leider noch nicht
use vars qw($ort $ortURL $ortHTML $bundesland $pw_schutz 
	    $standdate $csvSpalteStr $csvSpalteOrt
	    $osmosis_source $osmosis_polygon 
	    $osmosis_minLat $osmosis_maxLat
	    $osmosis_minLon $osmosis_maxLon
	    $bildSource $bugWikiText $bugWikiURL
	    $csvSeperator $nutzungsErlaubnis $printBild
	    $java_bin $osmosisjar $debug $autocoordinaten 
	    $nutzeLeisurePark
	    $strip_dash_names $link_unknown
	    $bzip2_bin
	    );
$java_bin="/usr/local/java/jre1.6.0_05.amd64/bin/java";
$osmosisjar="/home/sven/gps/hamburg-stat/osmosis-0.26/osmosis.jar";
$bzip2_bin="/bin/bzip2";
(-f $bzip2_bin ) or $bzip2_bin="/usr/bin/bzip2";
$csvSeperator=",";
$pw_schutz=9;
$standdate="";
$bugWikiURL="";
$bugWikiText="";
$nutzungsErlaubnis="";
$csvSpalteOrt=-1;
$csvSpalteStr=-1;
$nutzeLeisurePark=0;
$printBild=0;
$debug=0;
$bundesland="";
$bildSource="Quelle:unbekannt";
# Koordinaten aus *.coordinaten nehmen ->0 Koordinaten aus OSM "place=suburb" Tag erzeugen ->1 (Eine coordinaten.autogen Datei wird automatisch erzeugt) 
$autocoordinaten=1;
# Bindestriche in Straßennahmen aus .csv und OSM entfernen? Führt zu Missverständnissen, wenn die Striche weg sind und User auf dieser Basis neue Straßen taggen
$strip_dash_names=1;
# einen Link hinter nicht vorhandenen Straßen erstellen, damit man weiss wo man noch zum mappen hinfahren muss.
$link_unknown=1;
my $name="";
my $highway="";
my $leisure="";
my %streets;
my $timeMax=0;
my $timeMaxStr="unbekannt";
my $streetLat;
my $streetLon;
my %osmLat;
my %osmLon;
my %coordLat;
my %coordLon;
my $plz;
my %postal_code;
my $xmlstadtteil;
my $xmlnodeid;
my %stadtteillat;
my %stadtteillon;

sub getPrintable {
    my $str=shift;
    $str=~s/ä/ae/g;
    $str=~s/ö/oe/g;
    $str=~s/ü/ue/g;
    $str=~s/Ä/Ae/g;
    $str=~s/Ö/Oe/g;
    $str=~s/Ü/Ue/g;
    $str=~s/ß/sz/g;
    $str=~s/\W//g;
    return $str;

}
sub colorPrz {
    my $prozent=shift;
    my $color="#FFFFFF";
    if ($prozent==100) {
	$color="#00FF00";
    } elsif ($prozent>90) {
	$color="#00CC00";
    } elsif ($prozent>80) {
	$color="#009900";
    } elsif ($prozent>60) {
	$color="#FFFF00";
    } elsif ($prozent>40) {
	$color="#FFCC00";
    } elsif ($prozent>20) {
	$color="#FF9900";
    } else {
	$color="#FF0000";
    }
    return $color;
}
sub xmlStart { 
    my ($wert_des_zeigers,$starttag,%hash) = @_;
    if (defined($hash{'timestamp'}) and ($hash{'timestamp'}=~/(\d\d\d\d)\-(\d\d)\-(\d\d)T(\d\d):(\d\d):(\d\d)Z/)) {
	my $timeA = timegm($6,$5,$4,$3,$2-1,$1);
	if ($timeA>$timeMax) {
	    $timeMax=$timeA;
	    $timeMaxStr="$4:$5:$6 Uhr $3.$2.$1";
	    #print "TIME: $1 $2 $3 $4 $5 $6 \n";
	}
    }
    if ($starttag eq "way") {
	$name="";
	$highway="";
	$leisure="";
	$plz="";
	$streetLat="";
	$streetLon="";
	print "way-start\n" if $debug;
    } elsif ($starttag eq "tag") {
	if ($hash{'k'} eq "highway") {
	    $highway=$hash{'v'};
	    print "highway:$highway\n" if $debug;
	} elsif ($hash{'k'} eq "name") {
	    $name=$hash{'v'};
	    print "name:$name\n" if $debug;
	} elsif ($hash{'k'} eq "leisure") {
	    $leisure=$hash{'v'};
	} elsif ($hash{'k'} eq "postal_code") {
	    $plz=$hash{'v'};
	} elsif (($hash{'k'} eq "place") and ($hash{'v'} eq "suburb")) {
	    $xmlstadtteil=1;
	}
    } elsif ($starttag eq "node") {
	my $id=$hash{'id'};
	$xmlstadtteil=0;
	$name="";
	$xmlnodeid=$id;
	$osmLat{$id}=$hash{'lat'};
	$osmLon{$id}=$hash{'lon'};
    } elsif ($starttag eq "nd") {
	my $ref=$hash{'ref'};
	if (defined($osmLat{$ref})) {
	    $streetLat=$osmLat{$ref};
	    $streetLon=$osmLon{$ref};
	}
    }
}
sub xmlEnd { 
    my ($wert_des_zeigers,$endtag) = @_;
    if ($endtag eq "way" ) {
	print "way-end $name $highway\n" if $debug;
	if (($highway ne "") or
	    (($leisure eq "park") and ($nutzeLeisurePark==1))) {
	    utf8::decode($name);
	      if ($strip_dash_names==1) {
		  $name=~s/-/ /g;
	      }
	      if ($name ne "") {
		  $streets{$name}="nur in OSM";
		  $coordLat{$name}=$streetLat;
		  $coordLon{$name}=$streetLon;  
		  if ($plz ne "") {
		      if (defined($postal_code{$name})) {
			  my @plzar=split(/;/,$postal_code{$name});
			  my %plzh;
			  foreach my $p (@plzar) {
			      $plzh{$p}=1;
			  }
			  @plzar=split(/;/,$plz);
			  foreach my $p (@plzar) {
			      $plzh{$p}=1;
			  }
			  $postal_code{$name}=join(";",sort(keys(%plzh)));
		      } else {
			  $postal_code{$name}=$plz;
		      }
		  }
	      }
	  }
	print "$streetLat,$streetLon\n" if $debug;
    } elsif ($endtag eq "node") {
	if (($xmlstadtteil eq 1) and ($name ne "") and ($autocoordinaten eq 1)) {
	    $stadtteillat{$name}=$osmLat{$xmlnodeid};
	    $stadtteillon{$name}=$osmLon{$xmlnodeid};  
	    print "Neuer Stadtteil: $name $stadtteillon{$name} $stadtteillat{$name}\n" if $debug;
	}
    }

}

sub formatStadtteilAusSV {
    my $s=shift;
    $s=~s/\"$//;
    $s=~s/^\"//;
    $s=~s/(\s*)$//;
    return $s;

}
sub formatNameAusSV {
    my $n=shift;
    $n=~s/^\"//;
    $n=~s/\"$//;
    $n=~s/\s\s\d.*$//;
    $n=~s/\s*$//;
    if ($strip_dash_names==1) {
	 $n=~s/-/ /g;
     }
    $n=~s/(\s*)$//;
    $n=~s/str.$/straße/;
    $n=~s/\sStr.$/ Straße/;
    while ($n=~s/\s\s/ /) {
    }

    return $n;
}

sub formatFehler {
    my $n=shift;
    $n=~s/strasse/stra<b>ss<\/b>e/;

    $n=~s/Strasse/Stra<b>ss<\/b>e/;
    $n=~s/stasse/s<b>tass<\/b>e/;
    $n=~s/Stasse/S<b>tass<\/b>e/;
    
    $n=~s/bruecke/br<b>ue<\/b>cke/;
    $n=~s/brüce/b<b>rüc<\/b>ce/;
    $n=~s/Str\./<b>Str.<\/b>/;
    $n=~s/str\./<b>str.<\/b>/;
    $n=~s/Staße/St<b>aß<\/b>e/;;
    $n=~s/staße/st<b>aß<\/b>e/;;
    
    return $n;
}

if (-f "localSettings.pm") {
    require "localSettings.pm";
}
if ($#ARGV>-1) {
    my $conffile=$ARGV[0];
    if (-f $conffile) {
	require $conffile;
    } else {
	die("Config Datei $conffile nicht gefunden");
    }
} else {
    print "Usage: $0 <ConfigDatei>\n";
    exit 1;
}

$ortHTML=$ort;
$ortHTML=~s/ü/&uuml;/g;
$ortHTML=~s/Ã¼/&uuml;/g;
$ortHTML=~s/ä/&auml;/g;
$ortHTML=~s/ö/&ouml;/g;
$ortHTML=~s/Ã¶/&ouml;/g;
$ortHTML=~s/Ü/&Uuml;/g;
$ortHTML=~s/Ä/&Auml;/g;
$ortHTML=~s/Ö/&Ouml;/g;
$ortHTML=~s/ß/&szlig;/g;
if ($bugWikiURL eq "") {
    $bugWikiURL="http://wiki.openstreetmap.org/index.php/$ortURL-stat";
}
if ($bugWikiText eq "") {
    $bugWikiText="$ortURL-stat";
}
$pw_schutz!=9 or die("Fehlerhafte configDatei: pw_schutz");
$standdate ne "" or die("Fehlerhafte configDatei: standdate");
$nutzungsErlaubnis ne "" or die("Fehlerhafte configDatei: nutzungsErlaubnis");



if (!(-d $ortURL)) {
    mkdir($ortURL);
} else {
    opendir(DIR,$ortURL);
    my @entry=readdir(DIR);
    closedir(DIR);
    foreach my $file (@entry) {
	if (-f "$ortURL/$file" ) {
	    unlink("$ortURL/$file");
	}
    }
}
if (!(-d "$ortURL/s")) {
    mkdir("$ortURL/s");
}  else {
    opendir(DIR,"$ortURL/s");
    my @entry=readdir(DIR);
    closedir(DIR);
    foreach my $file (@entry) {
	if (-f "$ortURL/s/$file" ) {
	    unlink("$ortURL/s/$file");
	}
    }

}
############################################################################
# Osmosis 
############################################################################
my @osmosiscmd=();
my $zuschnittStr="";
if (defined($osmosis_source))  {
    my $doit=0;
    if (-f "$ortURL.osm.bz2") {
	my @statsource=stat($osmosis_source);
	my @statdestination=stat("$ortURL.osm.bz2");
	$doit=($statsource[9]>$statdestination[9]);
	if ($doit==0) {
	    print "do not run osmosis: Source is older than Destination\n" if $debug;
	}
    } else {
	$doit=1;
    }
    if (defined($osmosis_minLat)) {
	defined($osmosis_maxLat) or die("osmosis_maxLat not defined");
	defined($osmosis_maxLon) or die("osmosis_maxLon not defined");
	defined($osmosis_minLon) or die("osmosis_minLon not defined");
	(-f $osmosis_source) or die("Osmosis Source $osmosis_source not found");
	@osmosiscmd=($java_bin,"-jar",$osmosisjar,
		     "--rx","file=$osmosis_source",
		     "--bb",
		     "top=$osmosis_maxLat",
		     "bottom=$osmosis_minLat",
		     "left=$osmosis_minLon",
		     "right=$osmosis_maxLon",
		     "--wx","file=$ortURL.osm");
	$zuschnittStr="Der Zuschnitt erfolgte durch die Koordinaten: ($osmosis_minLat,$osmosis_minLon),($osmosis_maxLat,$osmosis_maxLon).";
    } elsif (defined($osmosis_polygon)) {
	
	@osmosiscmd=($java_bin,"-jar",$osmosisjar,
		     "--rx","file=$osmosis_source",
		     "--bp","file=$osmosis_polygon",
		     "--wx","file=$ortURL.osm");
	$zuschnittStr="Der Zuschnitt erfolgte durch das Polygon: <a href=\"$osmosis_polygon\">$osmosis_polygon</a>.";
	my @cmd=("/bin/cp",$osmosis_polygon,$ortURL."/");
	system(@cmd)==0 or die;
    } else {
	die("osmosis_source is defined but no lat/lon or poly");
    }
    if ($doit==0) {
	@osmosiscmd=();
    }
}
if ($#osmosiscmd>-1) {
    if (-f "$ortURL.osm.bz2") {
	print "rm $ortURL.osm.bz2\n" if $debug;
	unlink("$ortURL.osm.bz2");
    }
    print join(" ",@osmosiscmd),"\n" if $debug;
    system(@osmosiscmd)==0 or die("Osmosis died with exit code: $?");
    # Bzip
    my @bzipcmd=($bzip2_bin,"$ortURL.osm");
    print join(" ",@bzipcmd),"\n" if $debug;
    system(@bzipcmd)==0 or die("Bzip2 died with exit code: $?");
}


############################################################################

my %foundIn;
my %missingIn;
my %nichtInOrt;
my %nichtImGV;
my %rausAusGV;
my %stadtteile;
if (-f "$ortURL.ausnahme.wiki") {
    open(FILE,"$ortURL.ausnahme.wiki") or die;
    my $count=0;
    foreach my $line (<FILE>) {
	if ($line=~/^==.*==/) {
	    $count++;
	}
	if (($line=~/^\*\s*(.*),\s*(\d*\.\d*)\s*,\s*(\d*\.\d*)/) and ($count==1)) {
	    ## Nicht im Ort
	    my $strname=$1;
	    utf8::decode($strname);
	    my $lat=$2;
	    my $lon=$3;
	    $nichtInOrt{$strname}{"lat"}=$lat;
	    $nichtInOrt{$strname}{"lon"}=$lon;
	    print "STR:$strname\n" if $debug;
	    print "$count$line" if $debug;
	}
	if (($line=~/^\*\s*(.*),\s*(.*\S)\s*,\s*(\d*\.\d*)\s*,\s*(\d*\.\d*)/) and ($count==2)) 
	{
            # Nicht im Gebietsverzeichnis
	    my $strname=$1;
	    utf8::decode($strname);
	    my $begr=$2;
	    my $lat=$3;
	    my $lon=$4;
	    $nichtImGV{$strname}{"lat"}=$lat;
	    $nichtImGV{$strname}{"lon"}=$lon;
	    $nichtImGV{$strname}{"begr"}=$begr;
            print "$count$line" if $debug;
	}
	if (($line=~/^\*\s*(.*),\s*(.*)/) and ($count==3)) {
            ## Aus Gebietsverzeichnis löschen
	    my $strname=$1;
	    my $stadtteil=$2;
	    utf8::decode($strname);
	    utf8::decode($stadtteil);
	    $rausAusGV{$strname}=$stadtteil;
	    print "$count$line" if $debug;
	    print $line;
	}
    }
    close(FILE);
}

############################################################################
$streetLat=$streetLon="";
my $xmlP = new XML::Parser ();
$xmlP->setHandlers (Start => \&xmlStart,
		    End => \&xmlEnd);

open(FILE,"bzcat $ortURL.osm.bz2 |") or die("File $ortURL.osm.bz2 not found");
$xmlP->parse(*FILE);
close(FILE);
############################################################################
if ($autocoordinaten!=1) {
    open(FILE,"$ortURL.coordinaten") or die("File $ortURL.coordinaten not found");
    foreach my $line (<FILE>) {
	chop $line;
        if (!($line=~/^(\s*)$/))  {
	    my @arr=split /\t/,$line;
	    my $name=$arr[1];
#    utf8::decode($name);
	    $stadtteillat{$name}=$arr[2];
	    $stadtteillon{$name}=$arr[3];
    	    print "yyy $name $stadtteillat{$name} $stadtteillon{$name}\n" if $debug;
        }
    }
    close(FILE); 
} else {
    open(FILE,">$ortURL.coordinaten.autogen") or die("Cannot create file $ortURL.coordinaten.autogen");
    my $count=0;
    foreach my $line (sort (keys %stadtteillat)) {
	$count++;
	print FILE "$count\t$line\t$stadtteillat{$line}\t$stadtteillon{$line}\n";
    }
    close(FILE);
}


############################################################################
open(FILE,"$ortURL.csv") or die("Can not open $ortURL.csv");
my $old=<FILE>;#Kopfzeile
$old="";
my $summFound=0;
my $summMiss=0;

while (my $line=<FILE>) {
    chop $line;
    if (!($line=~/^(\s*)$/)) {
	my @fi=split /$csvSeperator/,$line;
	if ($old ne $fi[$csvSpalteStr]) {
	    $name=$fi[$csvSpalteStr];
	utf8::decode($name);	
	$name=formatNameAusSV($name);

	my $stadtteil=$fi[$csvSpalteOrt];
	if (($csvSpalteOrt==-1) or (!defined($stadtteil))) {
	    $stadtteil="";
	}
	utf8::decode($stadtteil);
	$stadtteil=&formatStadtteilAusSV($stadtteil);
	if ($stadtteil eq "") {
	    $stadtteil="$ort";
	}


	my $skip=0;
	$skip=1 if ($name=~/^(\d*)$/);
	
	if (defined($rausAusGV{$name})) {
	    if ($rausAusGV{$name} eq $stadtteil ) {
		$skip=1;
		print "Skipping: $name\n";
	    } else {
		print "Falscher Stadtteil \"",$rausAusGV{$name},"\"!=\"",$stadtteil,"\"\n";
	    }
	}
	if ($skip==0) {
	    #print "$name\n";
	    $old=$fi[$csvSpalteStr];
	    $stadtteile{$stadtteil}=1;
	    if (defined($streets{$name})) {
		$streets{$name}="in OSM und $ortHTML $stadtteil";
		push @{$foundIn{$stadtteil}},$name;
		$summFound++;
	    } else {
		$streets{$name}="nur in $ortHTML $stadtteil";
		push @{$missingIn{$stadtteil}},$name;
		$summMiss++;
		
	    }
	}}
    }
}
close(FILE);
############################################################################
foreach my $name (keys %nichtImGV) {
    print $name if $debug;
    if (defined($streets{$name})) {
	if ($streets{$name} eq "nur in OSM") {
	    $streets{$name}="Im Ausnahmeverzeichnis ($nichtImGV{$name}{'begr'})";
	}
    }
}
############################################################################
my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst)=localtime;
$year+=1900;
$mon++;
my $standd=sprintf("%d.%d.%d %d:%02d",$mday,$mon,$year,$hour,$min);
my $lbundesland=lc($bundesland);
my $stand="<p>Letze Aktualisierung: $standd Uhr (Neuester Timestamp im OSM File: $timeMaxStr) Copyright <a href='http://creativecommons.org/licenses/by-sa/2.0/'>CC-By-SA</a> Daten von <a href='http://download.geofabrik.de/'>http://download.geofabrik.de/</a><a href='http://download.geofabrik.de/osm/europe/germany/$lbundesland.osm.bz2'>osm/europe/germany/$lbundesland.osm.bz2</a>. Diese Daten werden wiederum regelmäßig von <a href='http://www.openstreetmap.org/'>www.openstreemap.org</a> geladen. ";
$stand.=$zuschnittStr;
if ($nutzeLeisurePark==1) {
    $stand.="</p><p>F&uuml;r den Abgleich werden alle Ways mit highway=* und leisure=park ausgwertet.";
} else {
    $stand.="</p><p>F&uuml;r den Abgleich werden alle Ways mit highway=* ausgwertet.";
}
$stand.="</p><p>$nutzungsErlaubnis";
if ($pw_schutz==1) {
    $stand.="<p>Wichtig: Um auf die Ortsteilseiten Zugriff zu bekommen, benötigst du ein Passwort. Dieses bekommst du, wenn du bei OSM mitwirkst und das
glaubhaft per E-Mail an:
<b>hh-osm-passwort</b> (AT) <b>sven.anders.im</b> bekundest. Weitergabe dieses Passworts und der Stadtteilseiten ist nicht gestattet!</p>";
    system("cp htaccess $ortURL/s/.htaccess");
} elsif (-f "$ortURL/s/.htaccess") {
    unlink("$ortURL/s/.htaccess") or die;
}

$stand.=" Jegliche andere Nutzung ist nur nach Genehmigung gestattet.</p>";
open(INDEX,">$ortURL/index.html") or die;
open(STATI,">$ortURL/stat.csv") or die;
open(COLOR,">$ortURL/stadtteil.color") or die;
open(SCRIPT,">$ortURL/myscript.sh") or die;
print SCRIPT "#!/bin/sh\n";
my $out1="";
my $out2="";
my $out3="";
my $out4="";
my $out5="";
my $out6="";
my $kommentar="<p>Kommentare und Verbesserungsvorschläge bitte auf die Wiki Seite <a href='$bugWikiURL'>$bugWikiText</a> Danke!</p>";
print INDEX "<html><body>";
print INDEX "<h1>$ortHTML</h1>";
print INDEX $stand;
if ($printBild==1) {
    print INDEX "<p><img src=\"$ortURL.png\" alt=\"$ortHTML\" />$bildSource</p>\n";
}
print INDEX "<h1>Alphabetisch sortiert</h1>";
print INDEX "<table>";
print INDEX "<tr><th>Stadtteil</th><th>fehlende</th><th>gefundene</th><th>Gesamt</th><th>Prozent</th></tr>\n";
foreach my $stadtteil (sort (keys %stadtteile)) {
    
    my $prstadtteil=&getPrintable($stadtteil);
    if ($stadtteil ne "") {
	open(OUT,">$ortURL/s/$prstadtteil.html") or die;
	my $found=0;
	my $miss=0;
	print OUT "<html><body>\n";
	print OUT "<h1>$stadtteil</h1>\n";
	print OUT "$stand";
	if (defined($missingIn{$stadtteil})) {
	    print OUT " <h1>Es fehlen noch:</h1>\n<ul>\n";
	    foreach my $k (sort @{$missingIn{$stadtteil}}) {

		if ($link_unknown==1) {
		    my $ku=$k;
		    $ku=~s/ /+/g;
		    print OUT "<li>$k (<a href=\"http://maps.google.de/maps?f=q&source=s_q&hl=de&geocode=&q=$ku,+$ort\">GoogleMaps</a>)</li>\n";
		} else {
		    print OUT "<li>$k</li>\n";
		}	
		$miss++;
	    }
	    print OUT "</ul>";
	} else {
	    print OUT " Es fehlen keine Strassen mehr\n";
	}
	if (defined($foundIn{$stadtteil})) {
	    print OUT "</ul><h1>Bereits in OSM:</h1>\n<ul>\n";
	    foreach my $k (sort @{$foundIn{$stadtteil}}) {
		my $plz="";
		if (defined($postal_code{$k})) {
		    $plz=" ($postal_code{$k})";
		}
		print OUT "<li>$k$plz</li>\n";
		$found++;
	    }
	    print OUT "</ul>";
	}else {
	    print OUT " Es sind noch keine Strassen in OSM erfasst\n";
	}
	print OUT $kommentar;
	print OUT "<a href=\"Nur_in_OSM.html#$stadtteil\">Zu den nur in OSM erfassten Str. aus $stadtteil</a>";
	print OUT "<br/><a href=\"../index.html\">Startseite</a>";
	print OUT "</body></html>\n";
	my $summe=$found+$miss;
	my $prozent=$found/($summe)*100;
	my $color=&colorPrz($prozent);
	my $outstr="<tr bgcolor=\"$color\"><td><a href=\"s/$prstadtteil.html\">$stadtteil</a></td>\n".
	    "<td>$miss</td>\n<td>$found</td>\n<td>$summe</td>\n".
	    sprintf  "<td>%.1f %% </td></tr>\n\n",$prozent;
	print INDEX $outstr;
	print COLOR "$stadtteil,$color\n";
	print SCRIPT "echo -n .\n";
	print SCRIPT "./doStadteilMap.pl -o \"$prstadtteil.png\" --stadtteil \"$stadtteil\"\n";
	if ($prozent==100) {
	    $out1.=$outstr;
	} elsif ($prozent>80) {
	    $out2.=$outstr;
	} elsif ($prozent>60) {
	    $out3.=$outstr;
	} elsif ($prozent>40) {
	    $out4.=$outstr;
	} elsif ($prozent>20) {
	    $out5.=$outstr;
	} else {
	    $out6.=$outstr;
	}

	close(OUT);
    }
}
close COLOR;
close SCRIPT;
my $prozent=0;
if ($summFound+$summMiss>0) {
    $prozent=$summFound/($summFound+$summMiss)*100;
}

my $seperator="<tr><td>&nbsp;</td><td></td><td></td><td></td></tr>\n";
my $color=&colorPrz($prozent); 
print INDEX $seperator;
print INDEX "<tr bgcolor=\"$color\"><td>$ortHTML gesamt</a></td>\n";	
print INDEX "<td>$summMiss</td>\n<td>$summFound</td>\n";
print STATI "$ort,$summMiss,$summFound\n";
printf INDEX "<td>%d</td>\n",($summFound+$summMiss);
printf INDEX "<td>%.1f %% </td></tr>\n\n",$prozent;
print INDEX "</table> <h1>Nach Abdeckungsgrad sortiert</h1>\n";
print INDEX "<table>";
print INDEX "<tr><th>Stadtteil</th><th>fehlende</th><th>gefundene</th><th>Gesamt</th><th>Prozent</th></tr>\n";
print INDEX $out1.$seperator if ($out1 ne "");
print INDEX $out2.$seperator if ($out2 ne "");
print INDEX $out3.$seperator if ($out3 ne "");
print INDEX $out4.$seperator if ($out4 ne "");
print INDEX $out5.$seperator if ($out5 ne "");
print INDEX $out6.$seperator if ($out6 ne "");
print INDEX "<tr bgcolor=\"$color\"><td>$ortHTML gesamt</a></td>\n";	
print INDEX "<td>$summMiss</td>\n<td>$summFound</td>\n";
printf INDEX "<td>%d</td>\n",($summFound+$summMiss);
printf INDEX "<td>%.1f %% </td></tr>\n\n",$prozent;
print INDEX  "</table>\n";
print INDEX "<h1>Straßen die nicht im Verzeichnis sondern nur in OSM sind</h1>";
print INDEX  "<a href=\"s/Nur_in_OSM.html\">Nur in OSM</a>\n";
print INDEX $kommentar;
print INDEX  "</body></html>\n";
close(INDEX);
close(STATI);
open(OUT,">$ortURL/s/Nur_in_OSM.html") or die;
print OUT "<html><body>\n<h1>Nur in OpenStreetMap</h1>\n<ul>";
my %stadtteilOut;
foreach my $k (sort (keys %streets)) {
    if ($streets{$k} eq "nur in OSM") {
     my $ausnahme=0;
     if (defined($nichtInOrt{$k}{"lat"})) {
       my $ent=(($nichtInOrt{$k}{"lat"}-$coordLat{$k})**2)+(($nichtInOrt{$k}{"lon"}-$coordLon{$k})**2);
       $ent=$ent*1000;
       if ($ent<2) {
         print OUT "<li>$k (nicht in $ortHTML / in Ausnahmeliste Entfernung: $ent )</li>\n";
         $ausnahme=1;
       }
     } 
     if ($ausnahme==0) {
	my $c=9999999999999999999;
	my $st="";
	foreach my $stadtteil (keys %stadtteillat) {
	    if (!(defined($coordLat{$k}))) {
		warn("coordLat $k undef $stadtteil");
	    }
	    if (!(defined($stadtteillon{$stadtteil}))) {
		warn("lon x$stadtteil"."x undef");
	    }
	    if (!(defined($stadtteillat{$stadtteil}))) {
		warn("lat x$stadtteil"."x undef");
	    }
	    my $ent=(($stadtteillat{$stadtteil}-$coordLat{$k})**2)+
		    (($stadtteillon{$stadtteil}-$coordLon{$k})**2);
	    if ($ent<$c) {
		$c=$ent;
		$st=$stadtteil;
	    }
	}
	my $dista=99999;
	my $mightbe="";
	foreach my $other (sort (keys %streets)) {
	    if ($streets{$other} ne "nur in OSM") {
		my $di=distance($k,$other);
#		print "$other $k\n";
		if ($dista>=$di) {
		    if ($dista>$di) {
			$mightbe="";
		    } else {
			$mightbe.=", ";
		    }
		    $dista=$di;
		    $mightbe.="$other ($streets{$other})"
		}
		
	    }
	}
	my $kk=&formatFehler($k);
	
        $c=$c*1000;
	my $pst=$st;
	utf8::decode($pst);
	my $str="<li>$kk (vermutlich in $pst, Entfernung: $c) <a href=\"http://www.informationfreeway.org/?lat=$coordLat{$k}&lon=$coordLon{$k}&zoom=17\">InfFree Karte</a>
<a href=\"http://www.openstreetmap.org/?mlat=$coordLat{$k}&mlon=$coordLon{$k}&zoom=16\">OSM Karte</a>";

	if ($dista<4) {
	    $str.="<br/>(Ähnliche Namen: $mightbe)\n";
	}
	    $str.="</li>\n";
	print OUT $str;

	if (!defined ( $stadtteilOut{$st})) {
	    $stadtteilOut{$st}="";
	}
	$stadtteilOut{$st}.=$str;
      }
    }
}
print OUT "</ul>";
print OUT "<h1>Nach Stadtteilen:</h1>\n";
foreach my $k (sort (keys %stadtteilOut)) {
    my $prK=&getPrintable($k);
    my $pk=$k;
    utf8::decode($pk);
    print OUT "<a name=\"$pk\"></a><h2><a href=\"$prK".".html\">$pk</a></h2>\n";
    print OUT "<ul>\n";
    print OUT $stadtteilOut{$k};
    print OUT "</ul>\n";
}
print OUT "<h1>Eingetragene Ausnahmen:</h1>\n";
print OUT "<ul>\n";
    
foreach my $k (sort (keys %streets)) {
    if ($streets{$k}=~/Im Ausnahmeverzeichni/) {
	print OUT "<li>\n";
    	print OUT "<b>".$k."</b> - ".$streets{$k};
    	print OUT "</ll>\n";
    }	
}
print OUT "</ul>\n";
print OUT "</body></html>\n";
close(OUT);

